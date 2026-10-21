package edu.campusmarketplace.model;

import edu.campusmarketplace.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/** Cash-on-delivery order or campus item exchange agreement; no payment is collected by the application. */
public final class Order {
    private final UUID id = UUID.randomUUID();
    private final Product product;
    private final Student buyer, seller;
    private final double total;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private OrderStatus status = OrderStatus.PLACED;
    private final Product offeredProduct;
    private final String meetupNotes;

    public Order(Product product, Student buyer) {
        this(product, buyer, null, "");
    }

    public Order(Product product, Student buyer, Product offeredProduct, String meetupNotes) {
        this.product = product;
        this.buyer = buyer;
        this.seller = product.getSeller();
        this.offeredProduct = offeredProduct;
        this.meetupNotes = meetupNotes == null ? "" : meetupNotes.trim();
        this.total = (offeredProduct != null) ? 0 : product.getPrice();
    }

    public UUID getId() { return id; }
    public Product getProduct() { return product; }
    public Student getBuyer() { return buyer; }
    public Student getSeller() { return seller; }
    public double getTotal() { return total; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public OrderStatus getStatus() { return status; }
    public boolean isExchange() { return offeredProduct != null; }
    public Product getOfferedProduct() { return offeredProduct; }
    public String getMeetupNotes() { return meetupNotes; }

    public void cancel() {
        if (status != OrderStatus.PLACED) throw new IllegalStateException("Only placed orders can be cancelled.");
        status = OrderStatus.CANCELLED;
    }

    public void complete() {
        if (status != OrderStatus.PLACED) throw new IllegalStateException("Only placed orders can be completed.");
        status = OrderStatus.COMPLETED;
    }
}
