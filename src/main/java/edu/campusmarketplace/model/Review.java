package edu.campusmarketplace.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * A buyer's rating, tags, and feedback for a seller following a completed transaction.
 * Associated with the seller (for their reputation), the reviewer (buyer), and the product (for deal context).
 */
public final class Review {
    private final UUID id = UUID.randomUUID();
    private final Student seller;
    private final Student reviewer;
    private final Product product;
    private final int rating;          // 1–5 inclusive
    private final String comment;
    private final List<String> tags;
    private final LocalDateTime createdAt;

    public Review(Student seller, Student reviewer, Product product, int rating, String comment, List<String> tags) {
        if (seller == null) throw new IllegalArgumentException("Seller is required.");
        if (reviewer == null) throw new IllegalArgumentException("Reviewer is required.");
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating must be between 1 and 5.");
        this.seller = seller;
        this.reviewer = reviewer;
        this.product = product;
        this.rating = rating;
        this.comment = (comment == null) ? "" : comment.trim();
        this.tags = (tags == null) ? List.of() : List.copyOf(tags);
        this.createdAt = LocalDateTime.now();
    }

    public Review(Product product, Student reviewer, int rating, String comment) {
        this(product != null ? product.getSeller() : null, reviewer, product, rating, comment, List.of());
    }

    public UUID getId()                 { return id; }
    public Student getSeller()          { return seller; }
    public Student getReviewer()        { return reviewer; }
    public Product getProduct()         { return product; }
    public int getRating()              { return rating; }
    public String getComment()          { return comment; }
    public List<String> getTags()       { return tags; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
