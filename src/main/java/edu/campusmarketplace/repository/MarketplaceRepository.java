package edu.campusmarketplace.repository;

import edu.campusmarketplace.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Storage boundary. This implementation intentionally keeps data in one process. */
public interface MarketplaceRepository {
    void addStudent(Student student);
    List<Student> students();
    Optional<Student> studentByEmail(String email);
    Optional<Student> studentByStudentId(String studentId);
    void addProduct(Product product);
    Optional<Product> product(UUID id);
    List<Product> products();
    void removeProduct(UUID id);
    void addOrder(Order order);
    List<Order> orders();
    void addReview(Review review);
    List<Review> reviews();
    List<Review> reviewsByProduct(UUID productId);
    List<Review> reviewsBySeller(UUID sellerId);
}
