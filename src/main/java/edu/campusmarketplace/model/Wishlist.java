package edu.campusmarketplace.model;

import java.util.*;

/** A student's saved products for the current application session. */
public final class Wishlist {
    private final Student owner;
    private final Map<UUID, Product> products = new LinkedHashMap<>();

    public Wishlist(Student owner) { this.owner = Objects.requireNonNull(owner); }
    public Student getOwner() { return owner; }
    public void add(Product product) { products.put(product.getId(), product); }
    public void remove(UUID productId) { products.remove(productId); }
    public boolean contains(UUID productId) { return products.containsKey(productId); }
    public List<Product> products() { return List.copyOf(products.values()); }
}
