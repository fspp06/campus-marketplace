package edu.campusmarketplace.model;

import edu.campusmarketplace.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/** Cash-on-delivery order; no payment is collected by the application. */
public final class Order {
    private final UUID id = UUID.randomUUID();
    private final Product product;
    private final Student buyer, seller;
    private final double total;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private OrderStatus status = OrderStatus.PLACED;
    public Order(Product product, Student buyer) {
        this.product = product; this.buyer = buyer; this.seller = product.getSeller(); this.total = product.getPrice();
    }
    public UUID getId() { return id; }
    public Product getProduct() { return product; }
    public Student getBuyer() { return buyer; }
    public Student getSeller() { return seller; }
    public double getTotal() { return total; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public OrderStatus getStatus() { return status; }
    public void cancel() { if (status != OrderStatus.PLACED) throw new IllegalStateException("Only placed orders can be cancelled."); status = OrderStatus.CANCELLED; }
    public void complete() { if (status != OrderStatus.PLACED) throw new IllegalStateException("Only placed orders can be completed."); status = OrderStatus.COMPLETED; }
}
