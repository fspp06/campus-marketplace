package edu.campusmarketplace.ui;

import edu.campusmarketplace.model.Review;
import edu.campusmarketplace.service.MarketplaceService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Clean and simple Campus Home Panel:
 * Displays only the welcoming campus introduction banner and recent student reviews & experiences.
 */
public final class HomePanel extends JPanel {
    private final MarketplaceService service;
    private final Runnable onBrowseClick;
    private final Runnable onSellClick;

    private JPanel reviewsContainer;

    public HomePanel(MarketplaceService service, Runnable onBrowseClick, Runnable onSellClick) {
        super(new BorderLayout());
        this.service = service;
        this.onBrowseClick = onBrowseClick;
        this.onSellClick = onSellClick;

        setBackground(UITheme.BG_CANVAS);
        initUI();
        refresh();
    }

    private void initUI() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(UITheme.BG_CANVAS);
        scrollContent.setBorder(new EmptyBorder(24, 32, 32, 32));

        // 1. Introduction Hero Banner
        JPanel hero = buildIntroBanner();
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(hero);
        scrollContent.add(Box.createVerticalStrut(28));

        // 2. Student Reviews & Campus Experiences
        JPanel reviews = buildReviewsSection();
        reviews.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(reviews);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.BG_CANVAS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel buildIntroBanner() {
        JPanel hero = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                // Subtle circular accents
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(getWidth() - 180, -50, 240, 240);
                g2.fillOval(getWidth() - 360, getHeight() - 100, 200, 200);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        hero.setOpaque(false);
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(new EmptyBorder(32, 36, 32, 36));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);

        UITheme.BadgeLabel badge = new UITheme.BadgeLabel("CAMPUS COMMUNITY",
                new Color(0x19, 0x87, 0x54), new Color(0xEC, 0xFD, 0xF5));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel headline = UITheme.label("Welcome to Campus Marketplace", 26f, true, Color.WHITE);
        headline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = UITheme.label(
                "<html>Buy, sell, and trade pre-loved textbooks, lab equipment, and dorm essentials with fellow students.<br>" +
                        "Safe, student-verified deals with cash-on-delivery and friendly campus meetups.</html>",
                14f, false, new Color(0xD1, 0xFA, 0xE5));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonBar.setOpaque(false);
        buttonBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        UITheme.ModernButton browseBtn = new UITheme.ModernButton("Explore listings", UITheme.ButtonStyle.PRIMARY);
        browseBtn.setPreferredSize(new Dimension(150, 40));
        browseBtn.setBackground(Color.WHITE);
        browseBtn.setForeground(UITheme.PRIMARY);
        browseBtn.addActionListener(e -> {
            if (onBrowseClick != null) onBrowseClick.run();
        });

        UITheme.ModernButton sellBtn = new UITheme.ModernButton("+ Sell an item", UITheme.ButtonStyle.SECONDARY);
        sellBtn.setPreferredSize(new Dimension(140, 40));
        sellBtn.setBackground(new Color(255, 255, 255, 35));
        sellBtn.setForeground(Color.WHITE);
        sellBtn.addActionListener(e -> {
            if (onSellClick != null) onSellClick.run();
        });

        buttonBar.add(browseBtn);
        buttonBar.add(sellBtn);

        hero.add(badge);
        hero.add(Box.createVerticalStrut(12));
        hero.add(headline);
        hero.add(Box.createVerticalStrut(8));
        hero.add(sub);
        hero.add(Box.createVerticalStrut(18));
        hero.add(buttonBar);

        return hero;
    }

    private JPanel buildReviewsSection() {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBox.add(UITheme.label("Student Experiences & Seller Reviews", 18f, true, UITheme.TEXT_MAIN));
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(UITheme.label("Recent feedback from classmates about their campus meetups and purchases.", 13f, false, UITheme.TEXT_MUTED));
        header.add(titleBox, BorderLayout.WEST);

        section.add(header);
        section.add(Box.createVerticalStrut(14));

        reviewsContainer = new JPanel();
        reviewsContainer.setOpaque(false);
        reviewsContainer.setLayout(new BoxLayout(reviewsContainer, BoxLayout.Y_AXIS));
        reviewsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        section.add(reviewsContainer);
        return section;
    }

    public void refresh() {
        reviewsContainer.removeAll();
        List<Review> recents = service.recentReviews(10);
        if (recents.isEmpty()) {
            UITheme.CardPanel empty = new UITheme.CardPanel(10, UITheme.SURFACE, UITheme.BORDER);
            empty.setBorder(new EmptyBorder(18, 20, 18, 20));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            empty.add(UITheme.label("No student reviews yet. Complete a purchase on campus to write the first review!", 13f, false, UITheme.TEXT_MUTED));
            reviewsContainer.add(empty);
        } else {
            for (Review r : recents) {
                JPanel card = buildReviewCard(r);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                reviewsContainer.add(card);
                reviewsContainer.add(Box.createVerticalStrut(10));
            }
        }

        revalidate();
        repaint();
    }

    private JPanel buildReviewCard(Review r) {
        UITheme.CardPanel card = new UITheme.CardPanel(12, UITheme.SURFACE, UITheme.BORDER);
        card.setLayout(new BorderLayout(12, 6));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Center Details
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Top Row: Stars + Reviewer -> Seller + Item Badge
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) stars.append(i <= r.getRating() ? "\u2605" : "\u2606");
        JLabel starLabel = UITheme.symbolLabel(stars.toString(), 13.5f, true, new Color(0xF5, 0x9E, 0x0B));
        topRow.add(starLabel);

        String reviewerInfo = r.getReviewer().getName() + " (" + r.getReviewer().getDepartment() + ")";
        String sellerInfo = r.getSeller().getName() + " (" + r.getSeller().getDepartment() + ")";
        JLabel connection = UITheme.label(reviewerInfo + "  ->  " + sellerInfo, 13f, true, UITheme.TEXT_MAIN);
        topRow.add(connection);

        if (r.getProduct() != null) {
            UITheme.BadgeLabel itemBadge = new UITheme.BadgeLabel(
                    "Item: " + r.getProduct().getName(),
                    new Color(0x37, 0x41, 0x51),
                    new Color(0xF3, 0xF4, 0xF6));
            topRow.add(itemBadge);
        }

        center.add(topRow);
        center.add(Box.createVerticalStrut(6));

        // Comment
        if (!r.getComment().isBlank()) {
            JLabel comment = UITheme.label("\"" + r.getComment() + "\"", 13f, false, UITheme.TEXT_MAIN);
            comment.setAlignmentX(Component.LEFT_ALIGNMENT);
            center.add(comment);
            center.add(Box.createVerticalStrut(6));
        }

        // Tags
        if (!r.getTags().isEmpty()) {
            JPanel tagsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            tagsRow.setOpaque(false);
            tagsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (String tag : r.getTags()) {
                tagsRow.add(new UITheme.BadgeLabel(tag, UITheme.TAG_TEXT, UITheme.TAG_BG));
            }
            center.add(tagsRow);
        }

        card.add(center, BorderLayout.CENTER);
        return card;
    }
}
