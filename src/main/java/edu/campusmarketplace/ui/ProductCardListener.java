package edu.campusmarketplace.ui;

import edu.campusmarketplace.model.Product;

/**
 * Event callbacks for interactions on a ProductCardPanel.
 */
public interface ProductCardListener {
    void onDetails(Product product);
    void onBuy(Product product);
    void onToggleWishlist(Product product);
    void onEdit(Product product);
    void onRemove(Product product);
}

