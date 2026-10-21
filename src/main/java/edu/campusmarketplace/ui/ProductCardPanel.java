package edu.campusmarketplace.ui;

import edu.campusmarketplace.model.Product;
import edu.campusmarketplace.model.Review;
import edu.campusmarketplace.model.Student;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Modern, production-grade product card component (inspired by Airbnb/Vinted).
 * Fixes button truncation, ensures high-quality aspect-ratio image scaling with rounded top corners,
 * and maintains clean typography and layout hierarchy.
 * Displays average star ratings from buyer reviews when available.
 */
public class ProductCardPanel extends UITheme.CardPanel {
    public static final int CARD_WIDTH = 276;
    public static final int CARD_HEIGHT = 400;
    public static final int THUMBNAIL_HEIGHT = 180;

    private final Product product;
    private final boolean isOwner;
    private final boolean isSaved;
    private final ProductCardListener listener;
    private final List<Review> reviews;

    public ProductCardPanel(Product product, Student currentUser, boolean isSaved,
                            ProductCardListener listener, List<Review> reviews) {
        super(12, UITheme.SURFACE, UITheme.BORDER);
        this.product = product;
        this.isOwner = product.getSeller().equals(currentUser);
        this.isSaved = isSaved;
        this.listener = listener;
        this.reviews = reviews == null ? List.of() : reviews;

        setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setLayout(new BorderLayout(0, 0));

        // 1. Image Thumbnail
        add(new ThumbnailPanel(product), BorderLayout.NORTH);

        // 2. Card Body
        add(buildBodyPanel(), BorderLayout.CENTER);
    }

    private JPanel buildBodyPanel() {
        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(12, 14, 14, 14));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        // Row 1: Category Tag + Swap Badge + Price
        JPanel metaRow = new JPanel(new BorderLayout(8, 0));
        metaRow.setOpaque(false);
        String catName = product.getCategory().name().toUpperCase();
        UITheme.BadgeLabel tag = new UITheme.BadgeLabel(catName, UITheme.TAG_BG, UITheme.TAG_TEXT);

        JPanel tagGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tagGroup.setOpaque(false);
        tagGroup.add(tag);
        if (product.isOpenToExchange() && "AVAILABLE".equals(product.getStatus().name())) {
            UITheme.BadgeLabel swapBadge = new UITheme.BadgeLabel("SWAP OK", new Color(0x02, 0x84, 0xC7), new Color(0xE0, 0xF2, 0xFE));
            swapBadge.setToolTipText("Seller is open to exchange/swap this item!");
            tagGroup.add(swapBadge);
        }

        JLabel price = UITheme.label("\u20B9" + String.format("%.0f", product.getPrice()), 16f, true, UITheme.PRICE_COLOR);
        metaRow.add(tagGroup, BorderLayout.WEST);
        metaRow.add(price, BorderLayout.EAST);
        info.add(metaRow);
        info.add(Box.createVerticalStrut(7));

        // Row 2: Product Title (15px bold, clamped)
        JLabel title = UITheme.label(truncate(product.getName(), 24), 15f, true, UITheme.TEXT_MAIN);
        title.setToolTipText(product.getName());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(title);
        info.add(Box.createVerticalStrut(3));

        // Row 3: Seller Name
        JLabel seller = UITheme.label("By " + product.getSeller().getName(), 12f, false, UITheme.TEXT_MUTED);
        seller.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(seller);
        info.add(Box.createVerticalStrut(4));

        // Row 4: Star Rating Summary
        JPanel starRow = buildStarRow();
        starRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(starRow);
        info.add(Box.createVerticalStrut(5));

        // Row 5: Description (clamped to 2 lines)
        String descText = product.getDescription();
        if (descText == null || descText.isBlank()) descText = "No description provided.";
        JLabel desc = UITheme.label(truncate(descText, 58), 12f, false, new Color(0x4B, 0x55, 0x63));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(desc);

        body.add(info, BorderLayout.CENTER);
        body.add(buildActionsRow(), BorderLayout.SOUTH);
        return body;
    }

    /** Builds star rating pill (e.g. 4.9 / 5.0) or "New seller" pill for the seller on this card. */
    private JPanel buildStarRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        row.setOpaque(false);
        if (reviews.isEmpty()) {
            JLabel none = UITheme.label("New seller", 11f, false, UITheme.TEXT_MUTED);
            row.add(none);
        } else {
            double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
            int rounded = (int) Math.round(avg);
            StringBuilder stars = new StringBuilder();
            for (int i = 1; i <= 5; i++) stars.append(i <= rounded ? "\u2605" : "\u2606");
            JLabel starLabel = UITheme.symbolLabel(stars.toString(), 12.5f, true, new Color(0xF5, 0x9E, 0x0B));
            JLabel scoreLabel = UITheme.label(String.format(" %.1f", avg), 11.5f, true, UITheme.TEXT_MAIN);
            JLabel countLabel = UITheme.label(" (" + reviews.size() + ")", 11f, false, UITheme.TEXT_MUTED);
            row.add(starLabel);
            row.add(scoreLabel);
            row.add(countLabel);
            row.setToolTipText("Seller rating: " + String.format("%.1f", avg) + " / 5 based on " + reviews.size() + " campus review(s)");
        }
        return row;
    }

    private JPanel buildActionsRow() {
        boolean isAvailable = "AVAILABLE".equals(product.getStatus().name());
        boolean isExchanged = "EXCHANGED".equals(product.getStatus().name());

        if (isOwner) {
            if (isAvailable) {
                // Owner + Available: Edit | Remove | Details
                JPanel row = new JPanel(new GridLayout(1, 3, 6, 0));
                row.setOpaque(false);
                UITheme.ModernButton edit = createCardButton("Edit", UITheme.ButtonStyle.SECONDARY, "Edit this listing");
                edit.addActionListener(e -> { if (listener != null) listener.onEdit(product); });
                UITheme.ModernButton remove = createCardButton("Remove", UITheme.ButtonStyle.DANGER, "Permanently remove this listing");
                remove.addActionListener(e -> { if (listener != null) listener.onRemove(product); });
                UITheme.ModernButton details = createCardButton("Details", UITheme.ButtonStyle.SECONDARY, "View full details of this item");
                details.addActionListener(e -> { if (listener != null) listener.onDetails(product); });
                row.add(edit); row.add(remove); row.add(details);
                return row;
            } else {
                // Owner + Sold/Exchanged: Status badge | Details
                JPanel row = new JPanel(new GridLayout(1, 2, 6, 0));
                row.setOpaque(false);
                String badgeText = isExchanged ? "Exchanged" : "Sold";
                UITheme.BadgeLabel statusBadge = new UITheme.BadgeLabel(badgeText, UITheme.SOLD_BG, UITheme.SOLD_TEXT);
                statusBadge.setHorizontalAlignment(SwingConstants.CENTER);
                UITheme.ModernButton details = createCardButton("Details", UITheme.ButtonStyle.SECONDARY, "View full details");
                details.addActionListener(e -> { if (listener != null) listener.onDetails(product); });
                row.add(statusBadge); row.add(details);
                return row;
            }
        } else if (isAvailable) {
            // Buyer + Available: Save | Buy (COD) | Details
            JPanel row = new JPanel(new GridLayout(1, 3, 6, 0));
            row.setOpaque(false);
            UITheme.ModernButton save = createCardButton(
                    isSaved ? "Saved" : "Save",
                    isSaved ? UITheme.ButtonStyle.PRIMARY : UITheme.ButtonStyle.SECONDARY,
                    isSaved ? "Remove from your wishlist" : "Add to your wishlist"
            );
            save.addActionListener(e -> { if (listener != null) listener.onToggleWishlist(product); });
            UITheme.ModernButton buy = createCardButton("Buy (COD)", UITheme.ButtonStyle.PRIMARY, "Place a cash-on-delivery order for this item");
            buy.addActionListener(e -> { if (listener != null) listener.onBuy(product); });
            UITheme.ModernButton details = createCardButton("Details", UITheme.ButtonStyle.SECONDARY, "View full details of this item");
            details.addActionListener(e -> { if (listener != null) listener.onDetails(product); });
            row.add(save); row.add(buy); row.add(details);
            return row;
        } else {
            // Inactive: Exchanged / Sold / Unavailable badge | Details
            JPanel row = new JPanel(new GridLayout(1, 2, 6, 0));
            row.setOpaque(false);
            String badgeText = isExchanged ? "Exchanged" : ("SOLD".equals(product.getStatus().name()) ? "Sold" : "Unavailable");
            UITheme.BadgeLabel statusBadge = new UITheme.BadgeLabel(badgeText, UITheme.SOLD_BG, UITheme.SOLD_TEXT);
            statusBadge.setHorizontalAlignment(SwingConstants.CENTER);
            UITheme.ModernButton details = createCardButton("Details", UITheme.ButtonStyle.SECONDARY, "View full details of this item");
            details.addActionListener(e -> { if (listener != null) listener.onDetails(product); });
            row.add(statusBadge); row.add(details);
            return row;
        }
    }

    private UITheme.ModernButton createCardButton(String text, UITheme.ButtonStyle style, String tooltip) {
        UITheme.ModernButton btn = new UITheme.ModernButton(text, style);
        btn.setFont(UITheme.bold(12f));
        btn.setMargin(new Insets(5, 4, 5, 4));
        btn.setToolTipText(tooltip);
        return btn;
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max - 3) + "..." : text;
    }

    /**
     * Top image container: 12px rounded top corners, bilinear-scaled centering, camera placeholder.
     */
    private static class ThumbnailPanel extends JPanel {
        private final BufferedImage cachedImage;

        public ThumbnailPanel(Product product) {
            setPreferredSize(new Dimension(CARD_WIDTH, THUMBNAIL_HEIGHT));
            setOpaque(false);
            this.cachedImage = product != null ? product.getCachedImage() : null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth();
            int h = getHeight();

            // Clip with rounded top corners only
            Area clip = new Area(new RoundRectangle2D.Float(0, 0, w, h + 14, 12, 12));
            clip.intersect(new Area(new Rectangle2D.Float(0, 0, w, h)));
            g2.setClip(clip);

            g2.setColor(new Color(0xF3, 0xF4, 0xF6));
            g2.fillRect(0, 0, w, h);

            if (cachedImage != null) {
                double scale = Math.min((double) w / cachedImage.getWidth(), (double) h / cachedImage.getHeight());
                int targetW = (int) (cachedImage.getWidth() * scale);
                int targetH = (int) (cachedImage.getHeight() * scale);
                g2.drawImage(cachedImage, (w - targetW) / 2, (h - targetH) / 2, targetW, targetH, null);
            } else {
                // Camera placeholder icon
                g2.setColor(new Color(0x9C, 0xA3, 0xAF));
                int cx = w / 2, cy = h / 2 - 8;
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(cx - 20, cy - 14, 40, 28, 6, 6);
                g2.drawOval(cx - 8, cy - 6, 16, 16);
                g2.fillOval(cx + 10, cy - 10, 4, 4);
                g2.setFont(UITheme.regular(11.5f));
                FontMetrics fm = g2.getFontMetrics();
                String text = "No photo provided";
                g2.drawString(text, (w - fm.stringWidth(text)) / 2, cy + 30);
            }

            // Bottom separator
            g2.setColor(UITheme.BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(0, h - 1, w, h - 1);
            g2.dispose();
        }
    }
}
