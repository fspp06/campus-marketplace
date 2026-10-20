package edu.campusmarketplace.ui;

import edu.campusmarketplace.model.Product;
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

/**
 * Modern, production-grade product card component (inspired by Airbnb/Vinted).
 * Fixes button truncation, ensures high-quality aspect-ratio image scaling with rounded top corners,
 * and maintains clean typography and layout hierarchy.
 */
public class ProductCardPanel extends UITheme.CardPanel {
    public static final int CARD_WIDTH = 276;
    public static final int CARD_HEIGHT = 385;
    public static final int THUMBNAIL_HEIGHT = 180;

    private final Product product;
    private final boolean isOwner;
    private final boolean isSaved;
    private final ProductCardListener listener;

    public ProductCardPanel(Product product, Student currentUser, boolean isSaved, ProductCardListener listener) {
        super(12, UITheme.SURFACE, UITheme.BORDER);
        this.product = product;
        this.isOwner = product.getSeller().equals(currentUser);
        this.isSaved = isSaved;
        this.listener = listener;

        setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setLayout(new BorderLayout(0, 0));

        // 1. Image Thumbnail
        add(new ThumbnailPanel(product.getImageBytes()), BorderLayout.NORTH);

        // 2. Card Body
        add(buildBodyPanel(), BorderLayout.CENTER);
    }

    private JPanel buildBodyPanel() {
        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(12, 14, 14, 14));

        // Top info section (Title, Price, Category tag, Seller, Description)
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        // Row 1: Category Tag + Price
        JPanel metaRow = new JPanel(new BorderLayout(8, 0));
        metaRow.setOpaque(false);

        String catName = product.getCategory().name().toUpperCase();
        UITheme.BadgeLabel tag = new UITheme.BadgeLabel(catName, UITheme.TAG_BG, UITheme.TAG_TEXT);
        JLabel price = UITheme.label("₹" + String.format("%.0f", product.getPrice()), 16f, true, UITheme.PRICE_COLOR);

        metaRow.add(tag, BorderLayout.WEST);
        metaRow.add(price, BorderLayout.EAST);
        info.add(metaRow);
        info.add(Box.createVerticalStrut(7));

        // Row 2: Product Title (15px bold, single line clamped)
        JLabel title = UITheme.label(truncate(product.getName(), 24), 15f, true, UITheme.TEXT_MAIN);
        title.setToolTipText(product.getName());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(title);
        info.add(Box.createVerticalStrut(3));

        // Row 3: Seller Name
        JLabel seller = UITheme.label("By " + product.getSeller().getName(), 12f, false, UITheme.TEXT_MUTED);
        seller.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(seller);
        info.add(Box.createVerticalStrut(6));

        // Row 4: Description (2 lines max)
        String descText = product.getDescription();
        if (descText == null || descText.isBlank()) {
            descText = "No description provided.";
        }
        JLabel desc = UITheme.label(truncate(descText, 62), 12f, false, new Color(0x4B, 0x55, 0x63));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(desc);

        body.add(info, BorderLayout.CENTER);

        // Bottom Action Button Row
        body.add(buildActionsRow(), BorderLayout.SOUTH);

        return body;
    }

    private JPanel buildActionsRow() {
        boolean isAvailable = "AVAILABLE".equals(product.getStatus().name());

        if (isOwner) {
            if (isAvailable) {
                // Owner actions: Edit, Remove, Details (Balanced 3-column grid)
                JPanel row = new JPanel(new GridLayout(1, 3, 6, 0));
                row.setOpaque(false);

                UITheme.ModernButton edit = createCardButton("Edit", UITheme.ButtonStyle.SECONDARY, "Edit this listing");
                edit.addActionListener(e -> { if (listener != null) listener.onEdit(product); });

                UITheme.ModernButton remove = createCardButton("Remove", UITheme.ButtonStyle.DANGER, "Permanently remove this listing");
                remove.addActionListener(e -> { if (listener != null) listener.onRemove(product); });

                UITheme.ModernButton details = createCardButton("Details", UITheme.ButtonStyle.SECONDARY, "View full details of this item");
                details.addActionListener(e -> { if (listener != null) listener.onDetails(product); });

                row.add(edit);
                row.add(remove);
                row.add(details);
                return row;
            } else {
                // Sold state
                JPanel row = new JPanel(new GridLayout(1, 2, 6, 0));
                row.setOpaque(false);

                UITheme.BadgeLabel soldBadge = new UITheme.BadgeLabel("Sold", UITheme.SOLD_BG, UITheme.SOLD_TEXT);
                soldBadge.setHorizontalAlignment(SwingConstants.CENTER);

                UITheme.ModernButton details = createCardButton("Details", UITheme.ButtonStyle.SECONDARY, "View full details of this item");
                details.addActionListener(e -> { if (listener != null) listener.onDetails(product); });

                row.add(soldBadge);
                row.add(details);
                return row;
            }
        } else if (isAvailable) {
            // Buyer actions: Save, Buy, Details (Balanced 3-column grid)
            JPanel row = new JPanel(new GridLayout(1, 3, 6, 0));
            row.setOpaque(false);

            String saveText = isSaved ? "♥ Saved" : "♡ Save";
            UITheme.ModernButton save = createCardButton(
                    saveText,
                    isSaved ? UITheme.ButtonStyle.OUTLINE : UITheme.ButtonStyle.SECONDARY,
                    isSaved ? "Remove from your wishlist" : "Add to your wishlist"
            );
            save.addActionListener(e -> { if (listener != null) listener.onToggleWishlist(product); });

            UITheme.ModernButton buy = createCardButton("Buy (COD)", UITheme.ButtonStyle.PRIMARY, "Place a cash-on-delivery order for this item");
            buy.addActionListener(e -> { if (listener != null) listener.onBuy(product); });

            UITheme.ModernButton details = createCardButton("Details", UITheme.ButtonStyle.SECONDARY, "View full details of this item");
            details.addActionListener(e -> { if (listener != null) listener.onDetails(product); });

            row.add(save);
            row.add(buy);
            row.add(details);
            return row;
        } else {
            // Unavailable
            JPanel row = new JPanel(new GridLayout(1, 2, 6, 0));
            row.setOpaque(false);

            UITheme.BadgeLabel unavailBadge = new UITheme.BadgeLabel("Unavailable", UITheme.SOLD_BG, UITheme.SOLD_TEXT);
            unavailBadge.setHorizontalAlignment(SwingConstants.CENTER);

            UITheme.ModernButton details = createCardButton("Details", UITheme.ButtonStyle.SECONDARY, "View full details of this item");
            details.addActionListener(e -> { if (listener != null) listener.onDetails(product); });

            row.add(unavailBadge);
            row.add(details);
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
        return text.length() > max ? text.substring(0, max - 1) + "…" : text;
    }

    /**
     * Top image container with 12px rounded top corners and bilinear scaled image centering.
     */
    private static class ThumbnailPanel extends JPanel {
        private BufferedImage cachedImage;

        public ThumbnailPanel(byte[] imageBytes) {
            setPreferredSize(new Dimension(CARD_WIDTH, THUMBNAIL_HEIGHT));
            setOpaque(false);
            if (imageBytes != null && imageBytes.length > 0) {
                try {
                    cachedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                } catch (Exception ignored) {
                    cachedImage = null;
                }
            }
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

            // Create clip area with rounded top corners (12px arc) and square bottom corners
            Area clip = new Area(new RoundRectangle2D.Float(0, 0, w, h + 14, 12, 12));
            clip.intersect(new Area(new Rectangle2D.Float(0, 0, w, h)));
            g2.setClip(clip);

            // Neutral background
            g2.setColor(new Color(0xF3, 0xF4, 0xF6));
            g2.fillRect(0, 0, w, h);

            if (cachedImage != null) {
                // Bilinear scaled centering without stretching or distortion
                double imgW = cachedImage.getWidth();
                double imgH = cachedImage.getHeight();
                double scale = Math.min((double) w / imgW, (double) h / imgH);
                int targetW = (int) (imgW * scale);
                int targetH = (int) (imgH * scale);
                int x = (w - targetW) / 2;
                int y = (h - targetH) / 2;

                g2.drawImage(cachedImage, x, y, targetW, targetH, null);
            } else {
                // Placeholder icon & text
                g2.setColor(new Color(0x9C, 0xA3, 0xAF));
                int cx = w / 2;
                int cy = h / 2 - 8;

                // Simple camera outline
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(cx - 20, cy - 14, 40, 28, 6, 6);
                g2.drawOval(cx - 8, cy - 6, 16, 16);
                g2.fillOval(cx + 10, cy - 10, 4, 4);

                g2.setFont(UITheme.regular(11.5f));
                FontMetrics fm = g2.getFontMetrics();
                String text = "No photo provided";
                g2.drawString(text, (w - fm.stringWidth(text)) / 2, cy + 30);
            }

            // Bottom border separator line
            g2.setColor(UITheme.BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(0, h - 1, w, h - 1);

            g2.dispose();
        }
    }
}
