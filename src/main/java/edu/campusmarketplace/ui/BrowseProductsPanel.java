package edu.campusmarketplace.ui;

import edu.campusmarketplace.model.Category;
import edu.campusmarketplace.model.Product;
import edu.campusmarketplace.model.Review;
import edu.campusmarketplace.model.Student;
import edu.campusmarketplace.service.MarketplaceService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.UUID;

/**
 * Modern product browsing panel with cohesive action bar, category filters,
 * live search, and a responsive grid of ProductCardPanels.
 */
public class BrowseProductsPanel extends JPanel {
    private final MarketplaceService service;
    private final ProductCardListener cardListener;
    private final Runnable onSellClicked;

    private UITheme.SearchTextField searchField;
    private JComboBox<String> categoryFilter;
    private JCheckBox onlySwapFilter;
    private JPanel cardsGrid;
    private JLabel countLabel;

    public BrowseProductsPanel(MarketplaceService service, ProductCardListener cardListener, Runnable onSellClicked) {
        this.service = service;
        this.cardListener = cardListener;
        this.onSellClicked = onSellClicked;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_CANVAS);
        setBorder(new EmptyBorder(16, 28, 20, 28));

        // 1. Top Header & Unified Toolbar
        add(buildHeaderSection(), BorderLayout.NORTH);

        // 2. Responsive Scrollable Cards Grid
        add(buildScrollableGrid(), BorderLayout.CENTER);
    }

    private JPanel buildHeaderSection() {
        JPanel header = new JPanel(new BorderLayout(0, 14));
        header.setOpaque(false);

        // Top Row: Title, Subtitle, and Item Count
        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = UITheme.label("Find your next favorite", 24f, true, UITheme.TEXT_MAIN);
        JLabel subtitle = UITheme.label("Student listings, shared locally on your campus.", 13.5f, false, UITheme.TEXT_MUTED);
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);

        countLabel = UITheme.label("", 13f, true, UITheme.PRIMARY);
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(titleBox, BorderLayout.WEST);
        topRow.add(countLabel, BorderLayout.EAST);
        header.add(topRow, BorderLayout.NORTH);

        // Action Bar Row (Consistent 40px height for all components)
        JPanel actionBar = new JPanel(new BorderLayout(12, 0));
        actionBar.setOpaque(false);

        // Left Controls: Search Field + Category Filter
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftControls.setOpaque(false);

        searchField = new UITheme.SearchTextField("Search textbook, tech, supplies...", 24);
        searchField.setPreferredSize(new Dimension(310, 40));

        categoryFilter = new JComboBox<>();
        categoryFilter.setFont(UITheme.medium(13f));
        categoryFilter.setBackground(UITheme.SURFACE);
        categoryFilter.setForeground(UITheme.TEXT_MAIN);
        categoryFilter.setPreferredSize(new Dimension(220, 40));
        categoryFilter.setFocusable(false);
        categoryFilter.setBorder(UITheme.roundedBorder(8, UITheme.BORDER, 1));
        categoryFilter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        categoryFilter.addItem("All categories");
        for (Category c : service.categories()) {
            categoryFilter.addItem(c.name());
        }

        onlySwapFilter = new JCheckBox("Swaps only");
        onlySwapFilter.setFont(UITheme.medium(12.5f));
        onlySwapFilter.setForeground(UITheme.TEXT_MAIN);
        onlySwapFilter.setOpaque(false);
        onlySwapFilter.setFocusPainted(false);
        onlySwapFilter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        onlySwapFilter.setToolTipText("Filter listings open to barter / swap");
        onlySwapFilter.addActionListener(e -> refresh());

        leftControls.add(searchField);
        leftControls.add(categoryFilter);
        leftControls.add(onlySwapFilter);

        // Right Control: "+ Sell an item" Primary Button
        UITheme.ModernButton sellBtn = new UITheme.ModernButton("+  Sell an item", UITheme.ButtonStyle.PRIMARY);
        sellBtn.setPreferredSize(new Dimension(150, 40));
        sellBtn.setToolTipText("List an item for other students to buy");
        sellBtn.addActionListener(e -> {
            if (onSellClicked != null) onSellClicked.run();
        });

        actionBar.add(leftControls, BorderLayout.WEST);
        actionBar.add(sellBtn, BorderLayout.EAST);

        header.add(actionBar, BorderLayout.SOUTH);

        // Live Search Listeners
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refresh(); }
            @Override public void removeUpdate(DocumentEvent e) { refresh(); }
            @Override public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        categoryFilter.addActionListener(e -> refresh());

        return header;
    }

    private JScrollPane buildScrollableGrid() {
        // Responsive WrapLayout preserves card proportions and reflows seamlessly
        cardsGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
        cardsGrid.setBackground(UITheme.BG_CANVAS);
        cardsGrid.setBorder(new EmptyBorder(8, 0, 16, 0));

        JScrollPane scrollPane = new JScrollPane(cardsGrid);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.BG_CANVAS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        return scrollPane;
    }

    /**
     * Refreshes the listings grid according to the current search query and category.
     */
    public void refresh() {
        if (cardsGrid == null) return;
        cardsGrid.removeAll();

        String query = searchField.getText().trim().toLowerCase();
        String selectedCat = (String) categoryFilter.getSelectedItem();
        boolean swapsOnly = onlySwapFilter != null && onlySwapFilter.isSelected();
        Student currentUser = service.currentUser();

        int displayedCount = 0;
        for (Product product : service.products()) {
            if (swapsOnly && !product.isOpenToExchange()) continue;

            // Text search across name, description, category
            String searchCorpus = (product.getName() + " " + product.getDescription() + " " + product.getCategory().name()).toLowerCase();
            if (!query.isEmpty() && !searchCorpus.contains(query)) continue;

            // Category filter
            if (selectedCat != null && !selectedCat.equals("All categories")
                    && !product.getCategory().name().equalsIgnoreCase(selectedCat)) continue;

            boolean isSaved = service.isWishlisted(product.getId());
            List<Review> reviews = service.reviewsForSeller(product.getSeller().getId());
            ProductCardPanel card = new ProductCardPanel(product, currentUser, isSaved, cardListener, reviews);
            cardsGrid.add(card);
            displayedCount++;
        }

        countLabel.setText(displayedCount + (displayedCount == 1 ? " listing" : " listings"));
        if (displayedCount == 0) cardsGrid.add(buildEmptyState(query, selectedCat));

        cardsGrid.revalidate();
        cardsGrid.repaint();
    }

    private JPanel buildEmptyState(String query, String selectedCat) {
        UITheme.CardPanel emptyCard = new UITheme.CardPanel(16);
        emptyCard.setLayout(new BoxLayout(emptyCard, BoxLayout.Y_AXIS));
        emptyCard.setPreferredSize(new Dimension(520, 220));
        emptyCard.setBorder(new EmptyBorder(28, 32, 28, 32));

        UITheme.BadgeLabel icon = new UITheme.BadgeLabel("NO MATCHES", UITheme.TAG_TEXT, UITheme.TAG_BG);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = UITheme.label("No listings found", 18f, true, UITheme.TEXT_MAIN);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        String detail = (!query.isEmpty() || (selectedCat != null && !selectedCat.equals("All categories")))
                ? "Try searching for something else or clearing the active category."
                : "No products are currently available. Be the first to list one!";
        JLabel sub = UITheme.label(detail, 13f, false, UITheme.TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        UITheme.ModernButton reset = new UITheme.ModernButton("Clear filters", UITheme.ButtonStyle.SECONDARY);
        reset.setAlignmentX(Component.CENTER_ALIGNMENT);
        reset.addActionListener(e -> {
            searchField.setText("");
            categoryFilter.setSelectedIndex(0);
            if (onlySwapFilter != null) onlySwapFilter.setSelected(false);
            refresh();
        });

        emptyCard.add(icon);
        emptyCard.add(Box.createVerticalStrut(10));
        emptyCard.add(msg);
        emptyCard.add(Box.createVerticalStrut(6));
        emptyCard.add(sub);
        emptyCard.add(Box.createVerticalStrut(16));
        emptyCard.add(reset);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(900, 300));
        wrapper.add(emptyCard);
        return wrapper;
    }
}

