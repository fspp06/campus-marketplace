package edu.campusmarketplace.repository;

import edu.campusmarketplace.model.*;
import java.util.*;

public final class InMemoryMarketplaceRepository implements MarketplaceRepository {
    private final Map<UUID, Student> students = new LinkedHashMap<>();
    private final Map<UUID, Product> products = new LinkedHashMap<>();
    private final List<Order>  orders  = new ArrayList<>();
    private final List<Review> reviews = new ArrayList<>();

    public void addStudent(Student value) { students.put(value.getId(), value); }
    public List<Student> students() { return List.copyOf(students.values()); }
    public Optional<Student> studentByEmail(String email) { return students.values().stream().filter(s -> s.getEmail().equalsIgnoreCase(email)).findFirst(); }
    public Optional<Student> studentByStudentId(String studentId) { return students.values().stream().filter(s -> s.getStudentId().equalsIgnoreCase(studentId)).findFirst(); }
    public void addProduct(Product value) { products.put(value.getId(), value); }
    public Optional<Product> product(UUID id) { return Optional.ofNullable(products.get(id)); }
    public List<Product> products() { return List.copyOf(products.values()); }
    public void removeProduct(UUID id) { products.remove(id); }
    public void addOrder(Order value) { orders.add(value); }
    public List<Order> orders() { return List.copyOf(orders); }
    public void addReview(Review value) { reviews.add(value); }
    public List<Review> reviews() { return List.copyOf(reviews); }
    public List<Review> reviewsByProduct(UUID productId) {
        return reviews.stream().filter(r -> r.getProduct() != null && r.getProduct().getId().equals(productId)).toList();
    }
    public List<Review> reviewsBySeller(UUID sellerId) {
        return reviews.stream().filter(r -> r.getSeller() != null && r.getSeller().getId().equals(sellerId)).toList();
    }
}
