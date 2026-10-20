package edu.campusmarketplace.repository;

import edu.campusmarketplace.model.*;
import java.util.*;

public final class InMemoryMarketplaceRepository implements MarketplaceRepository {
    private final Map<UUID, Student> students = new LinkedHashMap<>();
    private final Map<UUID, Product> products = new LinkedHashMap<>();
    private final List<Order> orders = new ArrayList<>();
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
}
