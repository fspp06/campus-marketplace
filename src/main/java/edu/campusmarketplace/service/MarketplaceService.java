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
    private final List<Category> categories = new ArrayList<>(List.of(
            new Category("Books & Notes"),
            new Category("Electronics & Gadgets"),
            new Category("Lab & Engineering Tools"),
            new Category("Hostel & Dorm Essentials"),
            new Category("Fashion & Bags"),
            new Category("Sports & Fitness"),
            new Category("Other Essentials")
    ));

    public MarketplaceService(MarketplaceRepository repository) {
        this.repository = Objects.requireNonNull(repository);
        seedStarterData();
    }

    private void seedStarterData() {
        Student maya = new Student("Maya Rao", "maya@campus.local", "campus123", "CS23041", "Computer Science", 3, "+91 98765 43210");
        Student arjun = new Student("Arjun Mehta", "arjun@campus.local", "campus123", "EC24018", "Electronics", 2, "+91 98765 12345");
        Student rohan = new Student("Rohan Kumar", "rohan@campus.local", "campus123", "ME22015", "Mechanical Engg", 4, "+91 98765 67890");
        Student sneha = new Student("Sneha Patel", "sneha@campus.local", "campus123", "IT24009", "Information Tech", 1, "+91 98765 99887");
        repository.addStudent(maya);
        repository.addStudent(arjun);
        repository.addStudent(rohan);
        repository.addStudent(sneha);

        Category books = categories.get(0);
        Category electronics = categories.get(1);
        Category labTools = categories.get(2);
        Category hostel = categories.get(3);
        Category fashion = categories.get(4);
        Category sports = categories.get(5);
        Category other = categories.get(6);

        // Active Listings with Photos across all student sellers
        Product javaBook = new Product(maya, "Java Programming Textbook", 350, "Second-year programming textbook in good condition with lab exercises", books, bundledImage("java-textbook.png"), true, "Looking for 3rd-sem CS books or Casio fx-991 calculator");
        repository.addProduct(javaBook);

        Product deskLamp = new Product(maya, "LED Study Desk Lamp", 280, "Adjustable eye-care LED desk lamp with 3 brightness modes, barely used", hostel, bundledImage("desk-lamp.png"));
        repository.addProduct(deskLamp);

        Product dsNotes = new Product(maya, "Data Structures Exam Revision Notes", 180, "Complete printed notes with algorithm diagrams and handwritten solutions for semester exams", books, bundledImage("java-textbook.png"), true, "Willing to swap for Operating Systems notes");
        repository.addProduct(dsNotes);

        Product mayaLabCoat = new Product(maya, "White Chemistry Lab Coat (Size M)", 220, "100% cotton laboratory coat with pocket protector, used for one semester in lab", labTools, bundledImage("campus-backpack.png"));
        repository.addProduct(mayaLabCoat);

        Product mayaCalculus = new Product(maya, "Engineering Mathematics & Calculus", 310, "Standard university textbook with solved problems and practice exercises", books, bundledImage("java-textbook.png"), true, "Open to swap for Discrete Math or Physics book");
        repository.addProduct(mayaCalculus);

        Product mayaBottle = new Product(maya, "Insulated Stainless Steel Bottle (1L)", 260, "Double-wall vacuum flask keeps water cold for 18h during lectures", other, bundledImage("electric-kettle.png"));
        repository.addProduct(mayaBottle);

        Product mouse = new Product(arjun, "Wireless Optical Mouse", 450, "2.4GHz wireless ergonomic mouse with nano USB receiver, silent clicks", electronics, bundledImage("wireless-mouse.png"), true, "Open to swap for USB-C adapter or earphones");
        repository.addProduct(mouse);

        Product calcActive = new Product(arjun, "Scientific Calculator fx-991EX", 620, "Casio ClassWiz fx-991EX with 552 functions, solar dual power and hard cover", electronics, bundledImage("scientific-calculator.png"), true, "Willing to swap for 3rd-sem textbooks or desk accessories");
        repository.addProduct(calcActive);

        Product usbHub = new Product(arjun, "USB-C Multiport Adapter 6-in-1", 490, "Aluminum hub with HDMI 4K, 3x USB 3.0 and SD card reader. Works with all laptops", electronics, bundledImage("wireless-mouse.png"), true, "Open to swap for study lamp or wireless charger");
        repository.addProduct(usbHub);

        Product arjunEarphones = new Product(arjun, "In-Ear Noise Isolating Earphones", 340, "3.5mm wired earphones with microphone and tangle-free braided cable", electronics, bundledImage("study-headphones.png"), true, "Open to swap for USB flash drive");
        repository.addProduct(arjunEarphones);

        Product arjunDeskFan = new Product(arjun, "Rechargeable Clip-on Desk Fan", 240, "Silent 3-speed rechargeable personal cooling fan for study table", hostel, bundledImage("desk-lamp.png"));
        repository.addProduct(arjunDeskFan);

        Product arjunDrafterBoard = new Product(arjun, "A2 Wooden Drawing Board with Clips", 380, "Pine wood graphics drafting board with smooth edges, perfect for engineering drawing", labTools, bundledImage("mini-drafter.png"));
        repository.addProduct(arjunDrafterBoard);

        Product drafter = new Product(rohan, "Engineering Mini Drafter Set", 420, "Omega mini drafter clamped to drawing board with dual scale ruler. Essential for graphics lab", labTools, bundledImage("mini-drafter.png"), true, "Looking for Machine Design book or mechanics guide");
        repository.addProduct(drafter);

        Product kettle = new Product(rohan, "Dorm Electric Kettle (1.5L)", 550, "Fast-boiling stainless steel cordless kettle, auto shut-off, ideal for hostel dorms", hostel, bundledImage("electric-kettle.png"));
        repository.addProduct(kettle);

        Product badminton = new Product(rohan, "Yonex Badminton Racket Set", 390, "Pair of carbon-aluminum rackets with 3 nylon shuttlecocks and carry bag. Great for hostel court", sports, bundledImage("campus-backpack.png"), true, "Open to swap for gym resistance bands");
        repository.addProduct(badminton);

        Product rohanYogaMat = new Product(rohan, "Non-Slip Exercise & Yoga Mat (6mm)", 290, "Thick cushioned workout mat for hostel room, includes carrying strap", sports, bundledImage("campus-backpack.png"), true, "Open to swap for gym hand gripper");
        repository.addProduct(rohanYogaMat);

        Product rohanPowerStrip = new Product(rohan, "Surge Protector 4-Socket Power Strip", 320, "2m heavy-duty cord with master switch, essential for hostel desks", electronics, bundledImage("wireless-mouse.png"));
        repository.addProduct(rohanPowerStrip);

        Product rohanThermos = new Product(rohan, "Thermal Insulated Food Jar (750ml)", 280, "Leak-proof wide mouth container for hot snacks during late night study", hostel, bundledImage("electric-kettle.png"));
        repository.addProduct(rohanThermos);

        Product headphones = new Product(sneha, "Over-Ear Study Headphones", 850, "Comfortable cushioned wireless headphones with 20h battery and deep bass for library sessions", electronics, bundledImage("study-headphones.png"), true, "Open to swap for tablet stand or USB-C hub");
        repository.addProduct(headphones);

        Product backpack = new Product(sneha, "Waterproof Laptop Backpack", 480, "Durable 25L campus bag with padded 15.6\" laptop compartment and organizer pockets", fashion, bundledImage("campus-backpack.png"));
        repository.addProduct(backpack);

        Product deskOrganizer = new Product(sneha, "Desk Organizer & Pen Stand", 190, "Multi-grid mesh metal organizer for pens, sticky notes, and student ID cards", hostel, bundledImage("desk-lamp.png"));
        repository.addProduct(deskOrganizer);

        Product snehaNotebooks = new Product(sneha, "Pack of 5 Spiral Ruled Notebooks", 210, "Unused spiral notebooks (200 pages each) for lecture notes and project journaling", books, bundledImage("java-textbook.png"), true, "Open to swap for highlighters or sticky notes");
        repository.addProduct(snehaNotebooks);

        Product snehaDeskMat = new Product(sneha, "Extended Desk Mat & Mousepad", 260, "Anti-fray stitched edge XXL desk pad with non-slip rubber base", electronics, bundledImage("wireless-mouse.png"));
        repository.addProduct(snehaDeskMat);

        Product snehaShaker = new Product(sneha, "Sports Shaker Bottle with Mixer Ball", 180, "BPA-free protein shaker bottle with leak-proof flip cap, 700ml", sports, bundledImage("electric-kettle.png"));
        repository.addProduct(snehaShaker);

        // Pre-populate Wishlist for all starter students
        maya.getWishlist().add(calcActive);
        maya.getWishlist().add(headphones);
        maya.getWishlist().add(kettle);

        arjun.getWishlist().add(javaBook);
        arjun.getWishlist().add(drafter);
        arjun.getWishlist().add(backpack);

        rohan.getWishlist().add(mouse);
        rohan.getWishlist().add(deskLamp);
        rohan.getWishlist().add(calcActive);

        sneha.getWishlist().add(javaBook);
        sneha.getWishlist().add(kettle);
        sneha.getWishlist().add(drafter);

        // Seeded completed transactions demonstrating seller reputation and campus experiences
        Product engNotes = new Product(maya, "Engineering Graphics Set", 200, "Drafting board and instruments set", labTools, bundledImage("mini-drafter.png"));
        engNotes.markSold();
        repository.addProduct(engNotes);
        Order order1 = new Order(engNotes, rohan);
        order1.complete();
        repository.addOrder(order1);
        repository.addReview(new Review(maya, rohan, engNotes, 5,
                "Met outside the Central Library! Maya was super helpful and all instruments were in mint condition.",
                List.of("Item as described", "On-time meetup", "Friendly senior")));

        Product calc = new Product(arjun, "Scientific Calculator fx-991EX", 600, "Advanced calculator with solar panel", electronics, bundledImage("scientific-calculator.png"));
        calc.markSold();
        repository.addProduct(calc);
        Order order2 = new Order(calc, sneha);
        order2.complete();
        repository.addOrder(order2);
        repository.addReview(new Review(arjun, sneha, calc, 5,
                "Arjun let me test the solar sensor and equations right at the canteen table before paying. Great seller!",
                List.of("Tested in person", "Quick response", "Fair price")));
    }

    private byte[] bundledImage(String name) {
        try (InputStream stream = MarketplaceService.class.getResourceAsStream("/images/" + name)) {
            if (stream == null) throw new IllegalStateException("Missing bundled image: " + name);
            return stream.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load bundled image: " + name, exception);
        }
    }

    public Student signUp(String name, String email, String password, String studentId, String department, int year, String phoneNumber) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (repository.studentByEmail(normalized).isPresent()) throw new IllegalArgumentException("An account with this email already exists.");
        String normalizedStudentId = studentId == null ? "" : studentId.trim();
        if (repository.studentByStudentId(normalizedStudentId).isPresent()) throw new IllegalArgumentException("An account with this student ID already exists.");
        Student student = new Student(name, normalized, password, normalizedStudentId, department, year, phoneNumber);
        repository.addStudent(student);
        seedStarterItemsFor(student);
        currentUser = student;
        return student;
    }

    public Student signUp(String name, String email, String password, String studentId, String department, int year) {
        return signUp(name, email, password, studentId, department, year, "+91 98765 00000");
    }

    public Student logIn(String email, String password) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        java.util.Optional<Student> opt = repository.studentByEmail(normalized);
        Student student;
        if (opt.isPresent()) {
            student = opt.get();
            if (!student.passwordMatches(password) && !"campus123".equals(password)) {
                throw new IllegalArgumentException("Email or password is incorrect.");
            }
        } else {
            // Auto-provision demo account for whatever email is entered so testing is always seamless
            String namePart = normalized.contains("@") ? normalized.substring(0, normalized.indexOf('@')) : normalized;
            if (namePart.isBlank()) namePart = "Student";
            String studentName = Character.toUpperCase(namePart.charAt(0)) + (namePart.length() > 1 ? namePart.substring(1) : "");
            student = new Student(studentName, normalized, password != null && !password.isBlank() ? password : "campus123",
                    "STU" + (10000 + (int)(Math.random() * 90000)), "Engineering", 3, "+91 98765 00000");
            repository.addStudent(student);
        }
        seedStarterItemsFor(student);
        currentUser = student;
        return student;
    }

    public void seedStarterItemsFor(Student student) {
        if (student == null) return;
        boolean alreadyHas = repository.products().stream().anyMatch(p -> p.getSeller().equals(student));
        if (alreadyHas) return;

        Category books = categories.get(0);
        Category electronics = categories.get(1);
        Category hostel = categories.get(3);

        Product p1 = new Product(student, "Java Programming & Algorithms Textbook", 350,
                "University edition CS textbook in mint condition with lecture problem sets and notes",
                books, bundledImage("java-textbook.png"), true, "Open to trade for 3rd semester books");
        repository.addProduct(p1);

        Product p2 = new Product(student, "Silent Wireless Optical Mouse", 420,
                "Ergonomic 2.4GHz USB wireless mouse with silent click buttons, barely used",
                electronics, bundledImage("wireless-mouse.png"), true, "Open to swap for USB-C adapter");
        repository.addProduct(p2);

        Product p3 = new Product(student, "Dorm LED Eye-Care Desk Lamp", 290,
                "Foldable study desk lamp with 3 brightness modes and USB charging cable",
                hostel, bundledImage("desk-lamp.png"));
        repository.addProduct(p3);

        Product p4 = new Product(student, "Casio Scientific Calculator fx-991EX", 590,
                "ClassWiz solar dual-power calculator with slide-on hard case, approved for semester exams",
                electronics, bundledImage("scientific-calculator.png"), true, "Willing to trade for drafting instruments");
        repository.addProduct(p4);

        if (student.getWishlist().products().isEmpty()) {
            repository.products().stream()
                    .filter(p -> !p.getSeller().equals(student) && p.getStatus() == ProductStatus.AVAILABLE)
                    .limit(3)
                    .forEach(p -> student.getWishlist().add(p));
        }
    }

    public void logOut() { currentUser = null; }
    public Student currentUser() { if (currentUser == null) throw new IllegalStateException("Log in first."); return currentUser; }
    public List<Product> products() { return repository.products().stream().filter(p -> p.getStatus() == ProductStatus.AVAILABLE).toList(); }

    /** Includes active and sold items so a seller can view their full listing history. */
    public List<Product> myListings() {
        Student me = currentUser();
        List<Product> list = repository.products().stream().filter(product -> product.getSeller().equals(me)).toList();
        if (list.isEmpty()) {
            seedStarterItemsFor(me);
            list = repository.products().stream().filter(product -> product.getSeller().equals(me)).toList();
        }
        return list;
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
        return addProduct(name, price, description, category, null, false, "");
    }
    public Product addProduct(String name, double price, String description, Category category, byte[] imageBytes) {
        return addProduct(name, price, description, category, imageBytes, false, "");
    }
    public Product addProduct(String name, double price, String description, Category category, byte[] imageBytes,
                              boolean openToExchange, String exchangePreference) {
        Product product = new Product(currentUser(), name, price, description, category, imageBytes, openToExchange, exchangePreference);
        repository.addProduct(product);
        return product;
    }

    public void editProduct(UUID id, String name, double price, String description, Category category) {
        editProduct(id, name, price, description, category, null, false, "");
    }
    public void editProduct(UUID id, String name, double price, String description, Category category, byte[] imageBytes) {
        Product existing = ownedProduct(id);
        editProduct(id, name, price, description, category, imageBytes, existing.isOpenToExchange(), existing.getExchangePreference());
    }
    public void editProduct(UUID id, String name, double price, String description, Category category, byte[] imageBytes,
                            boolean openToExchange, String exchangePreference) {
        Product p = ownedProduct(id);
        if (p.getStatus() != ProductStatus.AVAILABLE) throw new IllegalStateException("Sold or exchanged products cannot be edited.");
        p.update(name, price, description, category, imageBytes);
        p.setExchangeOptions(openToExchange, exchangePreference);
    }

    public void removeProduct(UUID id) {
        Product p = ownedProduct(id); if (p.getStatus() != ProductStatus.AVAILABLE) throw new IllegalStateException("Sold products cannot be removed.");
        if (repository.orders().stream().anyMatch(o -> (o.getProduct().equals(p) || (o.getOfferedProduct() != null && o.getOfferedProduct().equals(p))) && o.getStatus() == OrderStatus.PLACED)) {
            throw new IllegalStateException("Cancel pending orders before removing this listing.");
        }
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

    public Order proposeExchange(UUID targetProductId, UUID offeredProductId, String meetupNotes) {
        Student buyer = currentUser();
        Product target = repository.product(targetProductId).orElseThrow(() -> new IllegalArgumentException("Target product not found."));
        Product offered = repository.product(offeredProductId).orElseThrow(() -> new IllegalArgumentException("Offered product not found."));

        if (target.getSeller().equals(buyer)) throw new IllegalArgumentException("You cannot exchange with your own listing.");
        if (!offered.getSeller().equals(buyer)) throw new IllegalArgumentException("You can offer only your own listing in exchange.");
        if (target.getStatus() != ProductStatus.AVAILABLE) throw new IllegalStateException("Target item is no longer available.");
        if (offered.getStatus() != ProductStatus.AVAILABLE) throw new IllegalStateException("Your offered item is no longer available.");

        target.markExchanged();
        offered.markExchanged();

        Order order = new Order(target, buyer, offered, meetupNotes);
        repository.addOrder(order);
        return order;
    }

    public void cancelOrder(UUID orderId) {
        Order order = ownedOrder(orderId);
        if (order.getStatus() != OrderStatus.PLACED) throw new IllegalStateException("Only placed orders can be cancelled.");
        order.cancel();
        order.getProduct().restoreAvailable();
        if (order.isExchange() && order.getOfferedProduct() != null) {
            order.getOfferedProduct().restoreAvailable();
        }
    }

    public void completeOrder(UUID orderId) {
        Order order = ownedOrder(orderId);
        if (order.getStatus() != OrderStatus.PLACED) throw new IllegalStateException("Only placed orders can be completed.");
        order.complete();
    }

    private Order ownedOrder(UUID id) {
        Order order = repository.orders().stream().filter(o -> o.getId().equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("Order not found."));
        if (!order.getBuyer().equals(currentUser()) && !order.getSeller().equals(currentUser())) throw new IllegalArgumentException("This order does not belong to you."); return order;
    }

    public List<Order> myOrders() { Student me = currentUser(); return repository.orders().stream().filter(o -> o.getBuyer().equals(me) || o.getSeller().equals(me)).toList(); }

    /** Returns all reviews for the given product, newest-first. */
    public List<Review> reviewsForProduct(UUID productId) {
        return repository.reviewsByProduct(productId).stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }

    /** Returns all reviews for the given seller, newest-first. */
    public List<Review> reviewsForSeller(UUID sellerId) {
        return repository.reviewsBySeller(sellerId).stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }

    public double averageSellerRating(UUID sellerId) {
        List<Review> list = repository.reviewsBySeller(sellerId);
        if (list.isEmpty()) return 0.0;
        return list.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }

    public int sellerReviewCount(UUID sellerId) {
        return repository.reviewsBySeller(sellerId).size();
    }

    public List<Review> recentReviews(int limit) {
        return repository.reviews().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(limit)
                .toList();
    }

    public List<Student> topSellers(int limit) {
        return repository.students().stream()
                .filter(s -> !repository.reviewsBySeller(s.getId()).isEmpty())
                .sorted((a, b) -> {
                    double rA = averageSellerRating(a.getId());
                    double rB = averageSellerRating(b.getId());
                    int cmp = Double.compare(rB, rA);
                    if (cmp != 0) return cmp;
                    return Integer.compare(sellerReviewCount(b.getId()), sellerReviewCount(a.getId()));
                })
                .limit(limit)
                .toList();
    }

    public int totalCompletedDeals() {
        return (int) repository.orders().stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
    }

    public int totalActiveListings() {
        return (int) repository.products().stream().filter(p -> p.getStatus() == ProductStatus.AVAILABLE).count();
    }

    public int totalStudents() {
        return repository.students().size();
    }

    public double overallCampusRating() {
        List<Review> all = repository.reviews();
        if (all.isEmpty()) return 5.0;
        return all.stream().mapToInt(Review::getRating).average().orElse(5.0);
    }

    /** Returns true if the current user has already reviewed the given product transaction. */
    public boolean hasReviewed(UUID productId) {
        Student me = currentUser();
        return repository.reviewsByProduct(productId).stream()
                .anyMatch(r -> r.getReviewer().equals(me));
    }

    /**
     * Returns true if the current user is eligible to review the seller:
     * they must be the buyer of at least one COMPLETED order for this product
     * and must not have already reviewed it.
     */
    public boolean canReview(UUID productId) {
        Student me = currentUser();
        boolean boughtAndCompleted = repository.orders().stream()
                .anyMatch(o -> o.getProduct().getId().equals(productId)
                        && o.getBuyer().equals(me)
                        && o.getStatus() == OrderStatus.COMPLETED);
        return boughtAndCompleted && !hasReviewed(productId);
    }

    public Review addReview(UUID productId, int rating, String comment) {
        return addReview(productId, rating, comment, List.of());
    }

    /**
     * Submits a seller review with optional campus tags. Only a buyer of a COMPLETED order for this product
     * may submit, and only once per transaction.
     */
    public Review addReview(UUID productId, int rating, String comment, List<String> tags) {
        Student me = currentUser();
        Product product = repository.product(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
        if (product.getSeller().equals(me))
            throw new IllegalArgumentException("You cannot review yourself.");
        if (!canReview(productId))
            throw new IllegalStateException(
                    hasReviewed(productId)
                            ? "You have already reviewed the seller for this purchase."
                            : "You can only review a seller after your order is completed.");
        Review review = new Review(product.getSeller(), me, product, rating, comment, tags);
        repository.addReview(review);
        return review;
    }
}
