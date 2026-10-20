package edu.campusmarketplace.model;

import edu.campusmarketplace.enums.ProductStatus;
import java.util.UUID;

public final class Product {
    private final UUID id = UUID.randomUUID();
    private final Student seller;
    private String name, description;
    private double price;
    private Category category;
    private ProductStatus status = ProductStatus.AVAILABLE;
    private byte[] imageBytes;
    public Product(Student seller, String name, double price, String description, Category category) {
        this(seller, name, price, description, category, null);
    }
    public Product(Student seller, String name, double price, String description, Category category, byte[] imageBytes) {
        this.seller = seller; update(name, price, description, category, imageBytes);
    }
    public UUID getId() { return id; }
    public Student getSeller() { return seller; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public ProductStatus getStatus() { return status; }
    public byte[] getImageBytes() { return imageBytes == null ? null : imageBytes.clone(); }
    public void update(String name, double price, String description, Category category) {
        update(name, price, description, category, imageBytes);
    }
    public void update(String name, double price, String description, Category category, byte[] imageBytes) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Product name is required.");
        if (!Double.isFinite(price) || price <= 0) throw new IllegalArgumentException("Price must be greater than zero.");
        if (category == null) throw new IllegalArgumentException("Choose a category.");
        this.name = name.trim(); this.price = price; this.description = description == null ? "" : description.trim(); this.category = category;
        this.imageBytes = imageBytes == null ? null : imageBytes.clone();
    }
    public void markSold() { if (status != ProductStatus.AVAILABLE) throw new IllegalStateException("Product is not available."); status = ProductStatus.SOLD; }
    public void restoreAvailable() { status = ProductStatus.AVAILABLE; }
}
