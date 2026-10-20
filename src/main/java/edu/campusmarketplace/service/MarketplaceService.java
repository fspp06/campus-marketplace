package edu.campusmarketplace.service;

import edu.campusmarketplace.enums.OrderStatus;
import edu.campusmarketplace.enums.ProductStatus;
import edu.campusmarketplace.model.*;
import edu.campusmarketplace.repository.MarketplaceRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/** Application rules, independent of the Swing interface. */
public final class MarketplaceService {
    private final MarketplaceRepository repository;
    private Student currentUser;
    private final List<Category> categories = new ArrayList<>(List.of(new Category("Books"), new Category("Electronics"), new Category("Other")));
    public MarketplaceService(MarketplaceRepository repository) {
        this.repository = Objects.requireNonNull(repository);
        seedStarterData();
    }
    private void seedStarterData() {
        Student maya = new Student("Maya Rao", "maya@campus.local", "campus123", "CS23041", "Computer Science", 3);
        Student arjun = new Student("Arjun Mehta", "arjun@campus.local", "campus123", "EC24018", "Electronics", 2);
        repository.addStudent(maya); repository.addStudent(arjun);
        Category books = categories.get(0), electronics = categories.get(1), other = categories.get(2);
        repository.addProduct(new Product(maya, "Java Programming Textbook", 350, "Second-year programming textbook in good condition", books, bundledImage("java-textbook.png")));
        repository.addProduct(new Product(maya, "Desk Lamp", 280, "LED study lamp, barely used", other, bundledImage("desk-lamp.png")));
        repository.addProduct(new Product(arjun, "Wireless Mouse", 450, "Wireless mouse with USB receiver", electronics, bundledImage("wireless-mouse.png")));
    }
    private byte[] bundledImage(String name) {
        try (InputStream stream = MarketplaceService.class.getResourceAsStream("/images/" + name)) {
            if (stream == null) throw new IllegalStateException("Missing bundled image: " + name);
            return stream.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load bundled image: " + name, exception);
        }
    }
    public Student signUp(String name, String email, String password, String studentId, String department, int year) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (repository.studentByEmail(normalized).isPresent()) throw new IllegalArgumentException("An account with this email already exists.");
        String normalizedStudentId = studentId == null ? "" : studentId.trim();
        if (repository.studentByStudentId(normalizedStudentId).isPresent()) throw new IllegalArgumentException("An account with this student ID already exists.");
        Student student = new Student(name, normalized, password, normalizedStudentId, department, year);
        repository.addStudent(student); currentUser = student; return student;
    }
    public Student logIn(String email, String password) {
        Student student = repository.studentByEmail(email == null ? "" : email.trim()).orElseThrow(() -> new IllegalArgumentException("Email or password is incorrect."));
        if (!student.passwordMatches(password)) throw new IllegalArgumentException("Email or password is incorrect.");
        currentUser = student; return student;
    }
    public void logOut() { currentUser = null; }
    public Student currentUser() { if (currentUser == null) throw new IllegalStateException("Log in first."); return currentUser; }
    public List<Product> products() { return repository.products().stream().filter(p -> p.getStatus() == ProductStatus.AVAILABLE).toList(); }
    /** Includes active and sold items so a seller can view their full listing history. */
    public List<Product> myListings() {
        Student me = currentUser();
        return repository.products().stream().filter(product -> product.getSeller().equals(me)).toList();
    }
    public List<Product> wishlistProducts() {
        return currentUser().getWishlist().products().stream()
                .filter(product -> repository.product(product.getId()).isPresent()).toList();
    }
    public boolean isWishlisted(UUID productId) { return currentUser().getWishlist().contains(productId); }
    public void addToWishlist(UUID productId) {
        Product product = repository.product(productId).orElseThrow(() -> new IllegalArgumentException("Product not found."));
        if (product.getSeller().equals(currentUser())) throw new IllegalArgumentException("You cannot save your own listing.");
        if (product.getStatus() != ProductStatus.AVAILABLE) throw new IllegalStateException("Only available products can be saved.");
        currentUser().getWishlist().add(product);
    }
    public void removeFromWishlist(UUID productId) { currentUser().getWishlist().remove(productId); }
    public List<Category> categories() { return List.copyOf(categories); }
    public Product addProduct(String name, double price, String description, Category category) {
        Product product = new Product(currentUser(), name, price, description, category); repository.addProduct(product); return product;
    }
    public Product addProduct(String name, double price, String description, Category category, byte[] imageBytes) {
        Product product = new Product(currentUser(), name, price, description, category, imageBytes); repository.addProduct(product); return product;
    }
    public void editProduct(UUID id, String name, double price, String description, Category category) {
        Product p = ownedProduct(id); if (p.getStatus() != ProductStatus.AVAILABLE) throw new IllegalStateException("Sold products cannot be edited.");
        p.update(name, price, description, category);
    }
    public void editProduct(UUID id, String name, double price, String description, Category category, byte[] imageBytes) {
        Product p = ownedProduct(id); if (p.getStatus() != ProductStatus.AVAILABLE) throw new IllegalStateException("Sold products cannot be edited.");
        p.update(name, price, description, category, imageBytes);
    }
    public void removeProduct(UUID id) {
        Product p = ownedProduct(id); if (p.getStatus() != ProductStatus.AVAILABLE) throw new IllegalStateException("Sold products cannot be removed.");
        if (repository.orders().stream().anyMatch(o -> o.getProduct().equals(p) && o.getStatus() == OrderStatus.PLACED)) throw new IllegalStateException("Cancel the pending order before removing this listing.");
        repository.removeProduct(id);
        repository.students().forEach(student -> student.getWishlist().remove(id));
    }
    private Product ownedProduct(UUID id) {
        Product p = repository.product(id).orElseThrow(() -> new IllegalArgumentException("Product not found."));
        if (!p.getSeller().equals(currentUser())) throw new IllegalArgumentException("You can manage only your own listings."); return p;
    }
    public Order placeOrder(UUID productId) {
        Student buyer = currentUser(); Product p = repository.product(productId).orElseThrow(() -> new IllegalArgumentException("Product not found."));
        if (p.getSeller().equals(buyer)) throw new IllegalArgumentException("You cannot buy your own listing.");
        p.markSold(); Order order = new Order(p, buyer); repository.addOrder(order); return order;
    }
    public void cancelOrder(UUID orderId) {
        Order order = ownedOrder(orderId);
        if (order.getStatus() != OrderStatus.PLACED) throw new IllegalStateException("Only placed orders can be cancelled.");
        order.cancel(); order.getProduct().restoreAvailable();
    }
    public void completeOrder(UUID orderId) {
        Order order = ownedOrder(orderId);
        if (order.getStatus() != OrderStatus.PLACED) throw new IllegalStateException("Only placed orders can be completed."); order.complete();
    }
    private Order ownedOrder(UUID id) {
        Order order = repository.orders().stream().filter(o -> o.getId().equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("Order not found."));
        if (!order.getBuyer().equals(currentUser()) && !order.getSeller().equals(currentUser())) throw new IllegalArgumentException("This order does not belong to you."); return order;
    }
    public List<Order> myOrders() { Student me = currentUser(); return repository.orders().stream().filter(o -> o.getBuyer().equals(me) || o.getSeller().equals(me)).toList(); }
}
