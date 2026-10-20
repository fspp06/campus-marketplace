package edu.campusmarketplace.model;
public record Category(String name) {
    public Category { if (name == null || name.isBlank()) throw new IllegalArgumentException("Category name is required."); name = name.trim(); }
    @Override public String toString() { return name; }
}
