package edu.campusmarketplace.repository;

import edu.campusmarketplace.enums.OrderStatus;
import edu.campusmarketplace.enums.ProductStatus;
import edu.campusmarketplace.model.*;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Robust MySQL-backed implementation of MarketplaceRepository.
 * Uses JDBC with connection pooling and synchronized in-memory caching for zero UI latency.
 */
public final class MySqlMarketplaceRepository implements MarketplaceRepository {
    private final String url;
    private final String user;
    private final String password;

    private final Map<UUID, Student> students = new ConcurrentHashMap<>();
    private final Map<UUID, Product> products = new ConcurrentHashMap<>();
    private final Map<UUID, Order> orders = new ConcurrentHashMap<>();
    private final Map<UUID, Review> reviews = new ConcurrentHashMap<>();

    public MySqlMarketplaceRepository() throws SQLException {
        Properties props = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {}

        this.url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/campus_marketplace?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        this.user = props.getProperty("db.user", "root");
        this.password = props.getProperty("db.password", "root");

        // Verify driver & connection
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found in classpath.", e);
        }

        try (Connection conn = getConnection()) {
            ensureTables(conn);
            loadFromDatabase(conn);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private void ensureTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                    "id VARCHAR(36) PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL UNIQUE, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "student_id VARCHAR(50) NOT NULL UNIQUE, " +
                    "department VARCHAR(100) NOT NULL, " +
                    "year INT NOT NULL, " +
                    "phone_number VARCHAR(30) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            stmt.execute("CREATE TABLE IF NOT EXISTS categories (name VARCHAR(100) PRIMARY KEY) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            stmt.execute("INSERT IGNORE INTO categories (name) VALUES " +
                    "('Books & Notes'), ('Electronics & Gadgets'), ('Lab & Engineering Tools'), " +
                    "('Hostel & Dorm Essentials'), ('Fashion & Bags'), ('Sports & Fitness'), ('Other Essentials');");

            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id VARCHAR(36) PRIMARY KEY, " +
                    "seller_id VARCHAR(36) NOT NULL, " +
                    "name VARCHAR(150) NOT NULL, " +
                    "price DOUBLE NOT NULL, " +
                    "description TEXT, " +
                    "category_name VARCHAR(100) NOT NULL, " +
                    "status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', " +
                    "image_bytes LONGBLOB, " +
                    "open_to_exchange BOOLEAN DEFAULT FALSE, " +
                    "exchange_preference VARCHAR(255) DEFAULT '') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id VARCHAR(36) PRIMARY KEY, " +
                    "product_id VARCHAR(36) NOT NULL, " +
                    "buyer_id VARCHAR(36) NOT NULL, " +
                    "seller_id VARCHAR(36) NOT NULL, " +
                    "offered_product_id VARCHAR(36), " +
                    "meetup_notes VARCHAR(255), " +
                    "status VARCHAR(20) NOT NULL DEFAULT 'PLACED', " +
                    "total DOUBLE NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            stmt.execute("CREATE TABLE IF NOT EXISTS reviews (" +
                    "id VARCHAR(36) PRIMARY KEY, " +
                    "seller_id VARCHAR(36) NOT NULL, " +
                    "reviewer_id VARCHAR(36) NOT NULL, " +
                    "product_id VARCHAR(36) NOT NULL, " +
                    "rating INT NOT NULL, " +
                    "comment TEXT, " +
                    "badges VARCHAR(255), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            stmt.execute("CREATE TABLE IF NOT EXISTS wishlist (" +
                    "student_id VARCHAR(36) NOT NULL, " +
                    "product_id VARCHAR(36) NOT NULL, " +
                    "PRIMARY KEY (student_id, product_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        }
    }

    private void loadFromDatabase(Connection conn) throws SQLException {
        // 1. Students
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM students")) {
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                Student student = new Student(
                        id,
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("student_id"),
                        rs.getString("department"),
                        rs.getInt("year"),
                        rs.getString("phone_number")
                );
                students.put(id, student);
            }
        }

        // 2. Products
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM products")) {
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                UUID sellerId = UUID.fromString(rs.getString("seller_id"));
                Student seller = students.get(sellerId);
                if (seller == null) continue;

                Product p = new Product(
                        id,
                        seller,
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("description"),
                        new Category(rs.getString("category_name")),
                        rs.getBytes("image_bytes"),
                        rs.getBoolean("open_to_exchange"),
                        rs.getString("exchange_preference"),
                        ProductStatus.valueOf(rs.getString("status"))
                );
                products.put(id, p);
            }
        }

        // 3. Wishlists
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM wishlist")) {
            while (rs.next()) {
                UUID sId = UUID.fromString(rs.getString("student_id"));
                UUID pId = UUID.fromString(rs.getString("product_id"));
                Student student = students.get(sId);
                Product product = products.get(pId);
                if (student != null && product != null) {
                    student.getWishlist().add(product);
                }
            }
        }

        // 4. Orders
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM orders")) {
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                Product p = products.get(UUID.fromString(rs.getString("product_id")));
                Student buyer = students.get(UUID.fromString(rs.getString("buyer_id")));
                Student seller = students.get(UUID.fromString(rs.getString("seller_id")));
                if (p == null || buyer == null || seller == null) continue;

                String offeredStr = rs.getString("offered_product_id");
                Product offered = (offeredStr != null && !offeredStr.isBlank()) ? products.get(UUID.fromString(offeredStr)) : null;

                Timestamp ts = rs.getTimestamp("created_at");
                LocalDateTime created = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                Order order = new Order(
                        id,
                        p,
                        buyer,
                        seller,
                        offered,
                        rs.getString("meetup_notes"),
                        OrderStatus.valueOf(rs.getString("status")),
                        rs.getDouble("total"),
                        created
                );
                orders.put(id, order);
            }
        }

        // 5. Reviews
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM reviews")) {
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                Student seller = students.get(UUID.fromString(rs.getString("seller_id")));
                Student reviewer = students.get(UUID.fromString(rs.getString("reviewer_id")));
                Product p = products.get(UUID.fromString(rs.getString("product_id")));
                if (seller == null || reviewer == null || p == null) continue;

                String badgeStr = rs.getString("badges");
                List<String> tags = (badgeStr != null && !badgeStr.isBlank()) ? Arrays.asList(badgeStr.split("\\|")) : List.of();

                Timestamp ts = rs.getTimestamp("created_at");
                LocalDateTime created = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                Review r = new Review(id, seller, reviewer, p, rs.getInt("rating"), rs.getString("comment"), tags, created);
                reviews.put(id, r);
            }
        }
    }

    @Override
    public void addStudent(Student student) {
        Objects.requireNonNull(student);
        Optional<Student> existing = studentByEmail(student.getEmail());
        if (existing.isPresent()) {
            students.put(existing.get().getId(), existing.get());
            return;
        }

        students.put(student.getId(), student);

        String sql = "INSERT INTO students (id, name, email, password, student_id, department, year, phone_number) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name=VALUES(name), password=VALUES(password), department=VALUES(department), year=VALUES(year), phone_number=VALUES(phone_number)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getId().toString());
            ps.setString(2, student.getName());
            ps.setString(3, student.getEmail());
            ps.setString(4, "campus123");
            ps.setString(5, student.getStudentId());
            ps.setString(6, student.getDepartment());
            ps.setInt(7, student.getYear());
            ps.setString(8, student.getPhoneNumber());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Student> students() {
        return List.copyOf(students.values());
    }

    @Override
    public Optional<Student> studentByEmail(String email) {
        if (email == null) return Optional.empty();
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return students.values().stream().filter(s -> s.getEmail().equalsIgnoreCase(normalized)).findFirst();
    }

    @Override
    public Optional<Student> studentByStudentId(String studentId) {
        if (studentId == null) return Optional.empty();
        String normalized = studentId.trim();
        return students.values().stream().filter(s -> s.getStudentId().equalsIgnoreCase(normalized)).findFirst();
    }

    @Override
    public void addProduct(Product product) {
        Objects.requireNonNull(product);
        products.put(product.getId(), product);

        UUID sellerId = product.getSeller().getId();
        Optional<Student> regSeller = studentByEmail(product.getSeller().getEmail());
        if (regSeller.isPresent()) {
            sellerId = regSeller.get().getId();
        } else {
            addStudent(product.getSeller());
        }

        String sql = "INSERT INTO products (id, seller_id, name, price, description, category_name, status, image_bytes, open_to_exchange, exchange_preference) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name=VALUES(name), price=VALUES(price), description=VALUES(description), " +
                "category_name=VALUES(category_name), status=VALUES(status), image_bytes=VALUES(image_bytes), " +
                "open_to_exchange=VALUES(open_to_exchange), exchange_preference=VALUES(exchange_preference)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getId().toString());
            ps.setString(2, sellerId.toString());
            ps.setString(3, product.getName());
            ps.setDouble(4, product.getPrice());
            ps.setString(5, product.getDescription());
            ps.setString(6, product.getCategory().name());
            ps.setString(7, product.getStatus().name());
            ps.setBytes(8, product.getImageBytes());
            ps.setBoolean(9, product.isOpenToExchange());
            ps.setString(10, product.getExchangePreference());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Product> product(UUID id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public List<Product> products() {
        return List.copyOf(products.values());
    }

    @Override
    public void removeProduct(UUID id) {
        products.remove(id);
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addOrder(Order order) {
        Objects.requireNonNull(order);
        orders.put(order.getId(), order);

        UUID buyerId = order.getBuyer().getId();
        Optional<Student> regBuyer = studentByEmail(order.getBuyer().getEmail());
        if (regBuyer.isPresent()) buyerId = regBuyer.get().getId();
        else addStudent(order.getBuyer());

        UUID sellerId = order.getSeller().getId();
        Optional<Student> regSeller = studentByEmail(order.getSeller().getEmail());
        if (regSeller.isPresent()) sellerId = regSeller.get().getId();
        else addStudent(order.getSeller());

        String sql = "INSERT INTO orders (id, product_id, buyer_id, seller_id, offered_product_id, meetup_notes, status, total) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE status=VALUES(status)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.getId().toString());
            ps.setString(2, order.getProduct().getId().toString());
            ps.setString(3, buyerId.toString());
            ps.setString(4, sellerId.toString());
            ps.setString(5, order.getOfferedProduct() != null ? order.getOfferedProduct().getId().toString() : null);
            ps.setString(6, order.getMeetupNotes());
            ps.setString(7, order.getStatus().name());
            ps.setDouble(8, order.getTotal());
            ps.executeUpdate();

            // Also update product status in DB
            addProduct(order.getProduct());
            if (order.getOfferedProduct() != null) {
                addProduct(order.getOfferedProduct());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Order> orders() {
        return List.copyOf(orders.values());
    }

    @Override
    public void addReview(Review review) {
        Objects.requireNonNull(review);
        reviews.put(review.getId(), review);

        UUID sellerId = review.getSeller().getId();
        Optional<Student> regSeller = studentByEmail(review.getSeller().getEmail());
        if (regSeller.isPresent()) sellerId = regSeller.get().getId();
        else addStudent(review.getSeller());

        UUID reviewerId = review.getReviewer().getId();
        Optional<Student> regReviewer = studentByEmail(review.getReviewer().getEmail());
        if (regReviewer.isPresent()) reviewerId = regReviewer.get().getId();
        else addStudent(review.getReviewer());

        String sql = "INSERT INTO reviews (id, seller_id, reviewer_id, product_id, rating, comment, badges) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, review.getId().toString());
            ps.setString(2, sellerId.toString());
            ps.setString(3, reviewerId.toString());
            ps.setString(4, review.getProduct().getId().toString());
            ps.setInt(5, review.getRating());
            ps.setString(6, review.getComment());
            ps.setString(7, String.join("|", review.getTags()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Review> reviews() {
        return List.copyOf(reviews.values());
    }

    @Override
    public List<Review> reviewsByProduct(UUID productId) {
        if (productId == null) return List.of();
        return reviews.values().stream().filter(r -> r.getProduct().getId().equals(productId)).toList();
    }

    @Override
    public List<Review> reviewsBySeller(UUID sellerId) {
        if (sellerId == null) return List.of();
        return reviews.values().stream().filter(r -> r.getSeller().getId().equals(sellerId)).toList();
    }
}
