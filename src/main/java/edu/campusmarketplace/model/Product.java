package edu.campusmarketplace.model;

import edu.campusmarketplace.enums.ProductStatus;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.UUID;
import javax.imageio.ImageIO;

public final class Product {
    private final UUID id;
    private final Student seller;
    private String name, description;
    private double price;
    private Category category;
    private ProductStatus status = ProductStatus.AVAILABLE;
    private byte[] imageBytes;
    private transient BufferedImage cachedImage;
    private boolean openToExchange = false;
    private String exchangePreference = "";

    public Product(Student seller, String name, double price, String description, Category category) {
        this(seller, name, price, description, category, null, false, "");
    }

    public Product(Student seller, String name, double price, String description, Category category, byte[] imageBytes) {
        this(seller, name, price, description, category, imageBytes, false, "");
    }

    public Product(Student seller, String name, double price, String description, Category category, byte[] imageBytes,
                   boolean openToExchange, String exchangePreference) {
        this(UUID.randomUUID(), seller, name, price, description, category, imageBytes, openToExchange, exchangePreference, ProductStatus.AVAILABLE);
    }

    public Product(UUID id, Student seller, String name, double price, String description, Category category, byte[] imageBytes,
                   boolean openToExchange, String exchangePreference, ProductStatus status) {
        this.id = id != null ? id : UUID.randomUUID();
        this.seller = seller;
        this.openToExchange = openToExchange;
        this.exchangePreference = exchangePreference == null ? "" : exchangePreference.trim();
        this.status = status != null ? status : ProductStatus.AVAILABLE;
        update(name, price, description, category, imageBytes);
    }

    public UUID getId() { return id; }
    public Student getSeller() { return seller; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public ProductStatus getStatus() { return status; }
    public byte[] getImageBytes() { return imageBytes == null ? null : imageBytes.clone(); }

    public BufferedImage getCachedImage() {
        if (cachedImage == null && imageBytes != null && imageBytes.length > 0) {
            try {
                cachedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            } catch (Exception ignored) {}
        }
        return cachedImage;
    }

    public boolean isOpenToExchange() { return openToExchange; }
    public String getExchangePreference() { return exchangePreference == null ? "" : exchangePreference; }

    public void setExchangeOptions(boolean openToExchange, String preference) {
        this.openToExchange = openToExchange;
        this.exchangePreference = preference == null ? "" : preference.trim();
    }

    public void update(String name, double price, String description, Category category) {
        update(name, price, description, category, imageBytes);
    }

    public void update(String name, double price, String description, Category category, byte[] imageBytes) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Product name is required.");
        if (!Double.isFinite(price) || price <= 0) throw new IllegalArgumentException("Price must be greater than zero.");
        if (category == null) throw new IllegalArgumentException("Choose a category.");
        this.name = name.trim();
        this.price = price;
        this.description = description == null ? "" : description.trim();
        this.category = category;
        this.imageBytes = imageBytes == null ? null : imageBytes.clone();
        this.cachedImage = null;
    }

    public void markSold() {
        if (status != ProductStatus.AVAILABLE) throw new IllegalStateException("Product is not available.");
        status = ProductStatus.SOLD;
    }

    public void markExchanged() {
        if (status != ProductStatus.AVAILABLE) throw new IllegalStateException("Product is not available.");
        status = ProductStatus.EXCHANGED;
    }

    public void restoreAvailable() {
        status = ProductStatus.AVAILABLE;
    }
}
