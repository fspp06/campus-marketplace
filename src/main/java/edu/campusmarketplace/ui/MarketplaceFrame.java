package edu.campusmarketplace.ui;

import edu.campusmarketplace.model.*;
import edu.campusmarketplace.service.MarketplaceService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Modern, production-grade desktop UI for the single-session Campus Marketplace.
 * Implements clean, modern styling (inspired by Airbnb, Vinted, and Notion) using pure Java 17+ Swing.
 */
public final class MarketplaceFrame extends JFrame implements ProductCardListener {
    private final MarketplaceService service;

    // View switching
    private final CardLayout rootLayout = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootLayout);
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);

    // Header & Navigation components
    private JLabel userPillLabel;
    private SegmentedNavBar navBar;

    // Content Panels
    private HomePanel homePanel;
    private BrowseProductsPanel browsePanel;
    private JPanel wishlistGrid;
    private JPanel myListingsGrid;
    private JLabel wishlistCountLabel;
    private JLabel myListingsCountLabel;

    // Orders components
    private final DefaultTableModel ordersModel = new DefaultTableModel(
            new Object[]{"Product", "Other student", "Role", "Total (\u20B9)", "Status", "Order ID"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private JTable ordersTable;
    private JLabel selectedOrderImage;
    private JLabel selectedOrderTitle;
    private JLabel selectedOrderMeta;
    private JTextArea selectedOrderDescription;

    // Profile components
    private JLabel profileName;
    private JLabel profileEmail;
    private JLabel profileStudentDetails;
    private JLabel profileReputation;

    public MarketplaceFrame(MarketplaceService service) {
        super("Campus Market");
        this.service = service;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1060, 720));
        setSize(1240, 820);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_CANVAS);

        rootPanel.add(buildAuthView(), "auth");
        rootPanel.add(buildMarketplaceView(), "market");
        setContentPane(rootPanel);
        rootLayout.show(rootPanel, "auth");
    }

    // =========================================================================
    // 1. Top-Level Views (Auth & Main Marketplace)
    // =========================================================================

    private JPanel buildAuthView() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(UITheme.BG_CANVAS);

        // Split hero card
        UITheme.CardPanel card = new UITheme.CardPanel(20, UITheme.SURFACE, UITheme.BORDER);
        card.setLayout(new GridLayout(1, 2));
        card.setPreferredSize(new Dimension(840, 500));

        // Left Hero Pane (Deep Emerald background with brand messaging)
        JPanel hero = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth() + 15, getHeight(), 20, 20);

                // Subtle background circle accents
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(getWidth() - 120, -50, 240, 240);
                g2.fillOval(-60, getHeight() - 140, 200, 200);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        hero.setOpaque(false);
        hero.setBorder(new EmptyBorder(44, 40, 40, 36));
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));

        UITheme.BadgeLabel eyebrow = new UITheme.BadgeLabel("CAMPUS COMMUNITY", new Color(0x19, 0x87, 0x54), new Color(0xEC, 0xFD, 0xF5));
        JLabel headline = UITheme.label("<html>Good finds.<br>Right on campus.</html>", 32f, true, Color.WHITE);
        JLabel blurb = UITheme.label("<html>Buy and sell the things students<br>need, right where you study.</html>", 15f, false, new Color(0xD1, 0xFA, 0xE5));

        hero.add(eyebrow);
        hero.add(Box.createVerticalStrut(26));
        hero.add(headline);
        hero.add(Box.createVerticalStrut(18));
        hero.add(blurb);
        hero.add(Box.createVerticalGlue());
        hero.add(UITheme.label("Safe \u00B7 Cash on Delivery \u00B7 Student-to-Student", 12f, false, new Color(0xA7, 0xF3, 0xD0)));

        // Right Action Pane
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(48, 40, 36, 40));
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));

        JLabel title = UITheme.label("Welcome", 26f, true, UITheme.TEXT_MAIN);
        JLabel sub = UITheme.label("Sign in to explore student listings.", 14f, false, UITheme.TEXT_MUTED);

        UITheme.ModernButton login = new UITheme.ModernButton("Log in", UITheme.ButtonStyle.PRIMARY);
        login.setPreferredSize(new Dimension(280, 42));
        login.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));
        login.addActionListener(e -> authDialog(false));

        UITheme.ModernButton signup = new UITheme.ModernButton("Create an account", UITheme.ButtonStyle.SECONDARY);
        signup.setPreferredSize(new Dimension(280, 42));
        signup.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));
        signup.addActionListener(e -> authDialog(true));

        actions.add(title);
        actions.add(Box.createVerticalStrut(6));
        actions.add(sub);
        actions.add(Box.createVerticalStrut(28));
        actions.add(login);
        actions.add(Box.createVerticalStrut(12));
        actions.add(signup);
        actions.add(Box.createVerticalGlue());

        JLabel footerNote = UITheme.label("Safe, verified student-to-student marketplace", 12f, false, UITheme.TEXT_MUTED);
        footerNote.setAlignmentX(Component.CENTER_ALIGNMENT);
        actions.add(footerNote);

        card.add(hero);
        card.add(actions);
        page.add(card);
        return page;
    }

    private JPanel buildMarketplaceView() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(UITheme.BG_CANVAS);

        // 1. Top Header Row (Brand Logo + User Greeting + Outlined Logout)
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(UITheme.SURFACE);
        topHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                new EmptyBorder(14, 28, 14, 28)
        ));

        // Left Brand Logo with geometric storefront badge icon
        JPanel brandBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandBox.setOpaque(false);

        JLabel brandIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect(0, 0, 32, 32, 8, 8);

                // Stylized market storefront roof / bag icon
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(8, 11, 16, 14, 3, 3);
                g2.drawArc(11, 6, 10, 10, 0, 180);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(32, 32);
            }
        };

        JLabel brandText = UITheme.label("Campus Market", 20f, true, UITheme.TEXT_MAIN);
        brandBox.add(brandIcon);
        brandBox.add(brandText);

        // Right User Greeting Pill + Outlined Log Out button
        JPanel userBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userBox.setOpaque(false);

        userPillLabel = new JLabel("Hi, Student") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF3, 0xF4, 0xF6));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                // Green dot
                g2.setColor(UITheme.PRIMARY);
                g2.fillOval(12, getHeight() / 2 - 4, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(d.width + 36, 34);
            }
        };
        userPillLabel.setFont(UITheme.bold(12.5f));
        userPillLabel.setForeground(UITheme.TEXT_MAIN);
        userPillLabel.setBorder(new EmptyBorder(0, 26, 0, 12));

        UITheme.ModernButton logoutBtn = new UITheme.ModernButton("Log out", UITheme.ButtonStyle.OUTLINE);
        logoutBtn.setPreferredSize(new Dimension(86, 34));
        logoutBtn.addActionListener(e -> {
            service.logOut();
            rootLayout.show(rootPanel, "auth");
        });

        userBox.add(userPillLabel);
        userBox.add(logoutBtn);

        topHeader.add(brandBox, BorderLayout.WEST);
        topHeader.add(userBox, BorderLayout.EAST);

        // 2. Navigation Pill Bar (Replacing JTabbedPane)
        navBar = new SegmentedNavBar(tabKey -> {
            contentLayout.show(contentPanel, tabKey);
            switch (tabKey) {
                case "home" -> { if (homePanel != null) homePanel.refresh(); }
                case "browse" -> { if (browsePanel != null) browsePanel.refresh(); }
                case "wishlist" -> refreshWishlist();
                case "my_items" -> refreshMyListings();
                case "orders" -> refreshOrders();
                case "profile" -> updateProfile();
            }
        });
        navBar.addTab("home", "Home");
        navBar.addTab("browse", "Browse products");
        navBar.addTab("wishlist", "Wishlist");
        navBar.addTab("my_items", "My items");
        navBar.addTab("orders", "My orders");
        navBar.addTab("profile", "Profile");

        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(topHeader, BorderLayout.NORTH);
        headerContainer.add(navBar, BorderLayout.SOUTH);
        page.add(headerContainer, BorderLayout.NORTH);

        // 3. Central Content Views
        homePanel = new HomePanel(service, () -> navBar.selectTab("browse"), () -> productDialog(null));
        browsePanel = new BrowseProductsPanel(service, this, () -> productDialog(null));
        contentPanel.add(homePanel, "home");
        contentPanel.add(browsePanel, "browse");
        contentPanel.add(buildWishlistView(), "wishlist");
        contentPanel.add(buildMyListingsView(), "my_items");
        contentPanel.add(buildOrdersView(), "orders");
        contentPanel.add(buildProfileView(), "profile");

        page.add(contentPanel, BorderLayout.CENTER);
        return page;
    }

    // =========================================================================
    // 2. Modern Content Views (Wishlist, My Items, Orders, Profile)
    // =========================================================================

    private JPanel buildWishlistView() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(UITheme.BG_CANVAS);
        page.setBorder(new EmptyBorder(16, 28, 20, 28));

        // Header
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(UITheme.label("Wishlist", 24f, true, UITheme.TEXT_MAIN));
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(UITheme.label("Items you have saved to revisit before they are sold.", 13.5f, false, UITheme.TEXT_MUTED));

        wishlistCountLabel = UITheme.label("", 13f, true, UITheme.PRIMARY);

        heading.add(titleBox, BorderLayout.WEST);
        heading.add(wishlistCountLabel, BorderLayout.EAST);
        page.add(heading, BorderLayout.NORTH);

        // Scrollable Responsive Grid
        wishlistGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
        wishlistGrid.setBackground(UITheme.BG_CANVAS);
        wishlistGrid.setBorder(new EmptyBorder(8, 0, 16, 0));

        JScrollPane scroll = new JScrollPane(wishlistGrid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.BG_CANVAS);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        page.add(scroll, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildMyListingsView() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(UITheme.BG_CANVAS);
        page.setBorder(new EmptyBorder(16, 28, 20, 28));

        // Header
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.add(UITheme.label("My items", 24f, true, UITheme.TEXT_MAIN));
        copy.add(Box.createVerticalStrut(4));
        copy.add(UITheme.label("Manage the products you currently have listed for other students.", 13.5f, false, UITheme.TEXT_MUTED));

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightControls.setOpaque(false);

        myListingsCountLabel = UITheme.label("", 13f, true, UITheme.PRIMARY);

        UITheme.ModernButton sellBtn = new UITheme.ModernButton("+  List a new item", UITheme.ButtonStyle.PRIMARY);
        sellBtn.setPreferredSize(new Dimension(160, 38));
        sellBtn.addActionListener(e -> productDialog(null));

        rightControls.add(myListingsCountLabel);
        rightControls.add(sellBtn);

        heading.add(copy, BorderLayout.WEST);
        heading.add(rightControls, BorderLayout.EAST);
        page.add(heading, BorderLayout.NORTH);

        // Scrollable Responsive Grid
        myListingsGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
        myListingsGrid.setBackground(UITheme.BG_CANVAS);
        myListingsGrid.setBorder(new EmptyBorder(8, 0, 16, 0));

        JScrollPane scroll = new JScrollPane(myListingsGrid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.BG_CANVAS);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        page.add(scroll, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildOrdersView() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UITheme.BG_CANVAS);
        panel.setBorder(new EmptyBorder(16, 28, 20, 28));

        // Header
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(UITheme.label("Your orders", 24f, true, UITheme.TEXT_MAIN));
        heading.add(Box.createVerticalStrut(4));
        heading.add(UITheme.label("Track your purchases and sales, and confirm when the exchange is complete.", 13.5f, false, UITheme.TEXT_MUTED));
        panel.add(heading, BorderLayout.NORTH);

        // Orders Table
        ordersTable = new JTable(ordersModel);
        ordersTable.setRowHeight(42);
        ordersTable.setFont(UITheme.regular(13f));
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordersTable.setShowVerticalLines(false);
        ordersTable.setGridColor(UITheme.BORDER);
        ordersTable.setSelectionBackground(new Color(0xEC, 0xFD, 0xF5));
        ordersTable.setSelectionForeground(UITheme.TEXT_MAIN);
        ordersTable.getTableHeader().setFont(UITheme.bold(12.5f));
        ordersTable.getTableHeader().setBackground(new Color(0xF3, 0xF4, 0xF6));
        ordersTable.getTableHeader().setForeground(UITheme.TEXT_MAIN);
        ordersTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
        ordersTable.setAutoCreateRowSorter(true);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        ordersTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        ordersTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) refreshSelectedOrder();
        });

        JScrollPane tableScroll = new JScrollPane(ordersTable);
        tableScroll.setBorder(UITheme.roundedBorder(12, UITheme.BORDER, 1));
        tableScroll.getViewport().setBackground(UITheme.SURFACE);
        tableScroll.getVerticalScrollBar().setUnitIncrement(16);

        // Body: Table on Left + Preview Card on Right
        JPanel body = new JPanel(new BorderLayout(20, 0));
        body.setOpaque(false);
        body.add(tableScroll, BorderLayout.CENTER);
        body.add(buildOrderPreviewCard(), BorderLayout.EAST);
        panel.add(body, BorderLayout.CENTER);

        // Bottom Actions Row
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        UITheme.ModernButton completeBtn = new UITheme.ModernButton("Mark completed", UITheme.ButtonStyle.PRIMARY);
        completeBtn.setPreferredSize(new Dimension(160, 38));
        completeBtn.addActionListener(e -> withSelectedOrder(o -> {
            service.completeOrder(o.getId());
            refreshAll();
        }));

        UITheme.ModernButton cancelBtn = new UITheme.ModernButton("Cancel order", UITheme.ButtonStyle.DANGER);
        cancelBtn.setPreferredSize(new Dimension(140, 38));
        cancelBtn.addActionListener(e -> withSelectedOrder(o -> {
            service.cancelOrder(o.getId());
            refreshAll();
        }));

        actions.add(completeBtn);
        actions.add(cancelBtn);

        UITheme.ModernButton reviewBtn = new UITheme.ModernButton("Rate Seller", UITheme.ButtonStyle.SECONDARY);
        reviewBtn.setPreferredSize(new Dimension(160, 38));
        reviewBtn.setToolTipText("Leave a star rating, tags, and feedback for the seller after completing an order");
        reviewBtn.addActionListener(e -> withSelectedOrder(o -> {
            if (!service.canReview(o.getProduct().getId())) {
                showError(new IllegalStateException(
                        service.hasReviewed(o.getProduct().getId())
                                ? "You have already reviewed the seller for this order."
                                : "You can only rate a seller after your order is completed."));
                return;
            }
            reviewDialog(o.getProduct());
        }));
        actions.add(reviewBtn);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildOrderPreviewCard() {
        UITheme.CardPanel preview = new UITheme.CardPanel(14, UITheme.SURFACE, UITheme.BORDER);
        preview.setLayout(new BoxLayout(preview, BoxLayout.Y_AXIS));
        preview.setPreferredSize(new Dimension(310, 0));
        preview.setBorder(new EmptyBorder(16, 16, 16, 16));

        selectedOrderImage = photoThumbnailLabel(null, 278, 160);
        selectedOrderTitle = UITheme.label("Select an order", 17f, true, UITheme.TEXT_MAIN);
        selectedOrderMeta = UITheme.label("Choose an order to view its item details.", 12.5f, false, UITheme.TEXT_MUTED);

        selectedOrderDescription = new JTextArea();
        selectedOrderDescription.setEditable(false);
        selectedOrderDescription.setLineWrap(true);
        selectedOrderDescription.setWrapStyleWord(true);
        selectedOrderDescription.setFont(UITheme.regular(12f));
        selectedOrderDescription.setForeground(new Color(0x4B, 0x55, 0x63));
        selectedOrderDescription.setBackground(UITheme.SURFACE);
        selectedOrderDescription.setBorder(new EmptyBorder(0, 0, 0, 0));

        preview.add(selectedOrderImage);
        preview.add(Box.createVerticalStrut(14));
        preview.add(selectedOrderTitle);
        preview.add(Box.createVerticalStrut(6));
        preview.add(selectedOrderMeta);
        preview.add(Box.createVerticalStrut(10));
        preview.add(selectedOrderDescription);
        preview.add(Box.createVerticalGlue());

        return preview;
    }

    private JPanel buildProfileView() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(UITheme.BG_CANVAS);

        UITheme.CardPanel card = new UITheme.CardPanel(16, UITheme.SURFACE, UITheme.BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(540, 380));
        card.setBorder(new EmptyBorder(34, 36, 34, 36));

        // Avatar circle
        JLabel avatar = new JLabel("", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xEC, 0xFD, 0xF5));
                g2.fillOval(0, 0, 60, 60);
                g2.setColor(UITheme.PRIMARY);
                g2.setFont(UITheme.bold(22f));
                FontMetrics fm = g2.getFontMetrics();
                String initial = profileName.getText().isEmpty() ? "S" : profileName.getText().substring(0, 1).toUpperCase();
                g2.drawString(initial, (60 - fm.stringWidth(initial)) / 2, (60 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(60, 60);
            }
        };
        avatar.setAlignmentX(Component.LEFT_ALIGNMENT);

        profileName = UITheme.label("", 24f, true, UITheme.TEXT_MAIN);
        profileEmail = UITheme.label("", 14f, false, UITheme.TEXT_MUTED);
        profileStudentDetails = UITheme.label("", 13.5f, false, UITheme.TEXT_MUTED);
        profileReputation = UITheme.label("", 13.5f, true, new Color(0xD9, 0x77, 0x06));

        profileName.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileStudentDetails.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileReputation.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(avatar);
        card.add(Box.createVerticalStrut(16));
        card.add(profileName);
        card.add(Box.createVerticalStrut(4));
        card.add(profileEmail);
        card.add(Box.createVerticalStrut(6));
        card.add(profileStudentDetails);
        card.add(Box.createVerticalStrut(8));
        card.add(profileReputation);
        card.add(Box.createVerticalStrut(22));

        UITheme.CardPanel note = new UITheme.CardPanel(10, new Color(0xF9, 0xFA, 0xFB), UITheme.BORDER);
        note.setLayout(new BoxLayout(note, BoxLayout.Y_AXIS));
        note.setBorder(new EmptyBorder(12, 14, 12, 14));
        note.setAlignmentX(Component.LEFT_ALIGNMENT);

        note.add(UITheme.label("LOCAL IN-MEMORY SESSION", 11f, true, UITheme.PRIMARY));
        note.add(Box.createVerticalStrut(4));
        note.add(UITheme.label("Your listings, photos, and orders exist in-memory while this app session is open.", 12.5f, false, UITheme.TEXT_MUTED));

        card.add(note);
        page.add(card);
        return page;
    }

    // =========================================================================
    // 3. ProductCardListener Implementation (Decoupled Card Callbacks)
    // =========================================================================

    @Override
    public void onDetails(Product product) {
        showProductDialog(product);
    }

    @Override
    public void onBuy(Product product) {
        checkoutDialog(product);
    }

    @Override
    public void onToggleWishlist(Product product) {
        try {
            if (service.isWishlisted(product.getId())) {
                service.removeFromWishlist(product.getId());
            } else {
                service.addToWishlist(product.getId());
            }
            refreshAll();
        } catch (RuntimeException ex) {
            showError(ex);
        }
    }

    @Override
    public void onEdit(Product product) {
        productDialog(product);
    }

    @Override
    public void onRemove(Product product) {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Remove \"" + product.getName() + "\" from your listings?",
                "Remove listing",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            try {
                service.removeProduct(product.getId());
                refreshAll();
            } catch (RuntimeException ex) {
                showError(ex);
            }
        }
    }

    // =========================================================================
    // 4. Modals & Dialogs (Preserving Accessibility Labels & Form Styling)
    // =========================================================================

    private void authDialog(boolean signup) {
        JTextField name = createModernTextField(22);
        JTextField email = createModernTextField(22);
        JTextField studentId = createModernTextField(22);
        JTextField department = createModernTextField(22);
        JTextField phone = createModernTextField(22);
        phone.setText("+91 ");
        JPasswordField password = createModernPasswordField(22);
        JComboBox<Integer> year = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6});
        year.setPreferredSize(new Dimension(240, 38));
        year.setBackground(UITheme.SURFACE);

        if (!signup) {
            email.setText("maya@campus.local");
            password.setText("campus123");
        }

        JPanel form = new JPanel(new GridLayout(signup ? 7 : 2, 2, 12, 14));
        form.setBorder(new EmptyBorder(16, 12, 12, 12));

        if (signup) {
            JLabel lName = UITheme.label("Full Name", 13f, true, UITheme.TEXT_MAIN);
            lName.setLabelFor(name);
            form.add(lName); form.add(name);
        }

        JLabel lEmail = UITheme.label("Campus Email", 13f, true, UITheme.TEXT_MAIN);
        lEmail.setLabelFor(email);
        form.add(lEmail); form.add(email);

        JLabel lPassword = UITheme.label("Password", 13f, true, UITheme.TEXT_MAIN);
        lPassword.setLabelFor(password);
        form.add(lPassword); form.add(password);

        if (signup) {
            JLabel lStudentId = UITheme.label("Student ID", 13f, true, UITheme.TEXT_MAIN);
            lStudentId.setLabelFor(studentId);
            form.add(lStudentId); form.add(studentId);

            JLabel lDept = UITheme.label("Department", 13f, true, UITheme.TEXT_MAIN);
            lDept.setLabelFor(department);
            form.add(lDept); form.add(department);

            JLabel lYear = UITheme.label("Year", 13f, true, UITheme.TEXT_MAIN);
            lYear.setLabelFor(year);
            form.add(lYear); form.add(year);

            JLabel lPhone = UITheme.label("Phone Number", 13f, true, UITheme.TEXT_MAIN);
            lPhone.setLabelFor(phone);
            form.add(lPhone); form.add(phone);
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                signup ? "Create your account" : "Log in to Campus Market",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) return;

        try {
            if (signup) {
                service.signUp(name.getText(), email.getText(), new String(password.getPassword()),
                        studentId.getText(), department.getText(), (Integer) year.getSelectedItem(), phone.getText());
            } else {
                service.logIn(email.getText(), new String(password.getPassword()));
            }
            refreshAll();
            navBar.selectTab("home");
            rootLayout.show(rootPanel, "market");
        } catch (RuntimeException ex) {
            showError(ex);
        }
    }

    private void productDialog(Product existing) {
        boolean editing = existing != null;
        JTextField name = createModernTextField(24);
        name.setText(editing ? existing.getName() : "");

        JTextField price = createModernTextField(24);
        price.setText(editing ? Double.toString(existing.getPrice()) : "");

        JTextField description = createModernTextField(24);
        description.setText(editing ? existing.getDescription() : "");

        JComboBox<Category> category = new JComboBox<>(service.categories().toArray(Category[]::new));
        category.setPreferredSize(new Dimension(260, 38));
        category.setBackground(UITheme.SURFACE);
        if (editing) category.setSelectedItem(existing.getCategory());

        final byte[][] image = {editing ? existing.getImageBytes() : null};
        JLabel preview = photoThumbnailLabel(image[0], 280, 160);

        UITheme.ModernButton upload = new UITheme.ModernButton("Choose photo", UITheme.ButtonStyle.SECONDARY);
        upload.setToolTipText("Select a JPG, PNG, or GIF up to 5 MB");
        upload.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Choose a product photo");
            chooser.setFileFilter(new FileNameExtensionFilter("Images (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
            if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

            try {
                File file = chooser.getSelectedFile();
                if (file.length() > 5L * 1024 * 1024) throw new IllegalArgumentException("Choose an image smaller than 5 MB.");
                byte[] bytes = Files.readAllBytes(file.toPath());
                BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(bytes));
                if (decoded == null) throw new IllegalArgumentException("That file is not a supported image format.");
                image[0] = bytes;
                preview.setIcon(createScaledIcon(decoded, 280, 160));
                preview.setText("");
            } catch (Exception ex) {
                showError(ex instanceof RuntimeException re ? re : new IllegalArgumentException("Could not read image file."));
            }
        });

        UITheme.ModernButton clearPhoto = new UITheme.ModernButton("Remove photo", UITheme.ButtonStyle.OUTLINE);
        clearPhoto.addActionListener(e -> {
            image[0] = null;
            preview.setIcon(null);
            preview.setText("No photo selected");
        });

        JPanel photoActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        photoActions.setOpaque(false);
        photoActions.add(upload);
        photoActions.add(clearPhoto);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 14, 10, 14));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        addFormField(form, c, 0, "Product name", name);
        addFormField(form, c, 1, "Price (\u20B9)", price);
        addFormField(form, c, 2, "Category", category);
        addFormField(form, c, 3, "Description", description);

        JCheckBox exchangeCheck = new JCheckBox("Open to swap / exchange with other student's item");
        exchangeCheck.setFont(UITheme.medium(12.5f));
        exchangeCheck.setForeground(UITheme.TEXT_MAIN);
        exchangeCheck.setOpaque(false);
        if (editing) exchangeCheck.setSelected(existing.isOpenToExchange());

        JTextField exchangePref = createModernTextField(22);
        exchangePref.setToolTipText("e.g. Looking for Casio fx-991, 3rd-sem CS books, or headphones");
        if (editing) exchangePref.setText(existing.getExchangePreference());
        exchangePref.setEnabled(exchangeCheck.isSelected());
        exchangeCheck.addActionListener(e -> exchangePref.setEnabled(exchangeCheck.isSelected()));

        addFormField(form, c, 4, "Exchange option", exchangeCheck);
        addFormField(form, c, 5, "Looking for in swap", exchangePref);

        c.gridx = 0; c.gridy = 6; c.gridwidth = 2;
        form.add(preview, c);
        c.gridy = 7;
        form.add(photoActions, c);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                editing ? "Edit your listing" : "List an item for sale",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) return;

        try {
            double amount = Double.parseDouble(price.getText().trim());
            boolean openToSwap = exchangeCheck.isSelected();
            String pref = exchangePref.getText().trim();
            if (editing) {
                service.editProduct(existing.getId(), name.getText(), amount, description.getText(), (Category) category.getSelectedItem(), image[0], openToSwap, pref);
            } else {
                service.addProduct(name.getText(), amount, description.getText(), (Category) category.getSelectedItem(), image[0], openToSwap, pref);
            }
            refreshAll();
        } catch (RuntimeException ex) {
            showError(ex);
        }
    }

    private void addFormField(JPanel panel, GridBagConstraints c, int row, String title, JComponent input) {
        c.gridx = 0; c.gridy = row; c.gridwidth = 1; c.weightx = 0;
        JLabel l = UITheme.label(title, 13f, true, UITheme.TEXT_MAIN);
        l.setLabelFor(input);
        panel.add(l, c);

        c.gridx = 1; c.weightx = 1;
        panel.add(input, c);
    }

    private void showProductDialog(Product p) {
        JDialog dialog = new JDialog(this, p.getName(), true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new GridLayout(1, 2, 28, 0));
        content.setBackground(UITheme.SURFACE);
        content.setBorder(new EmptyBorder(28, 28, 26, 28));

        // Left Image Preview
        JLabel imageLabel = photoThumbnailLabel(p.getImageBytes(), 390, 400);
        content.add(imageLabel);

        // Right Product Details
        JPanel details = new JPanel();
        details.setBackground(UITheme.SURFACE);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        UITheme.BadgeLabel tag = new UITheme.BadgeLabel(p.getCategory().name().toUpperCase(), UITheme.TAG_BG, UITheme.TAG_TEXT);
        JLabel title = UITheme.label(p.getName(), 24f, true, UITheme.TEXT_MAIN);
        JLabel price = UITheme.label("\u20B9" + String.format("%.0f", p.getPrice()), 22f, true, UITheme.PRICE_COLOR);
        JLabel seller = UITheme.label("Listed by " + p.getSeller().getName() + " (" + p.getSeller().getDepartment() + ")", 13f, false, UITheme.TEXT_MUTED);

        JTextArea description = new JTextArea(p.getDescription().isBlank() ? "No description provided." : p.getDescription());
        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setFont(UITheme.regular(13.5f));
        description.setForeground(new Color(0x37, 0x41, 0x51));
        description.setBackground(UITheme.SURFACE);
        description.setBorder(new EmptyBorder(12, 0, 12, 0));

        details.add(tag);
        details.add(Box.createVerticalStrut(10));
        details.add(title);
        details.add(Box.createVerticalStrut(6));
        details.add(price);
        details.add(Box.createVerticalStrut(4));
        details.add(seller);
        details.add(Box.createVerticalStrut(14));
        details.add(description);

        if (p.isOpenToExchange()) {
            details.add(Box.createVerticalStrut(10));
            UITheme.CardPanel swapCard = new UITheme.CardPanel(8, new Color(0xF0, 0xFD, 0xFA), new Color(0x99, 0xF6, 0xE4));
            swapCard.setLayout(new BoxLayout(swapCard, BoxLayout.Y_AXIS));
            swapCard.setBorder(new EmptyBorder(8, 12, 8, 12));
            swapCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            swapCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

            JLabel swapTitle = UITheme.label("SWAP / EXCHANGE ACCEPTED", 11.5f, true, new Color(0x0F, 0x76, 0x6E));
            String prefText = p.getExchangePreference().isBlank() ? "Seller is open to trade for study supplies or books." : p.getExchangePreference();
            JLabel swapDesc = UITheme.label("Looking for: " + prefText, 12f, false, UITheme.TEXT_MAIN);
            swapCard.add(swapTitle);
            swapCard.add(Box.createVerticalStrut(2));
            swapCard.add(swapDesc);
            details.add(swapCard);
        }

        details.add(Box.createVerticalGlue());

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        UITheme.ModernButton closeBtn = new UITheme.ModernButton("Close", UITheme.ButtonStyle.SECONDARY);
        closeBtn.setPreferredSize(new Dimension(80, 38));
        closeBtn.addActionListener(e -> dialog.dispose());

        if (!p.getSeller().equals(service.currentUser()) && "AVAILABLE".equals(p.getStatus().name())) {
            boolean isSaved = service.isWishlisted(p.getId());
            UITheme.ModernButton saveBtn = new UITheme.ModernButton(
                    isSaved ? "Saved" : "Save",
                    isSaved ? UITheme.ButtonStyle.PRIMARY : UITheme.ButtonStyle.SECONDARY
            );
            saveBtn.setPreferredSize(new Dimension(100, 38));
            saveBtn.setToolTipText(isSaved ? "Remove from your wishlist" : "Add to your wishlist");
            saveBtn.addActionListener(e -> {
                onToggleWishlist(p);
                dialog.dispose();
            });

            UITheme.ModernButton buyBtn = new UITheme.ModernButton("Buy (COD)", UITheme.ButtonStyle.PRIMARY);
            buyBtn.setPreferredSize(new Dimension(130, 38));
            buyBtn.setToolTipText("Place a cash-on-delivery order for this item");
            buyBtn.addActionListener(e -> {
                dialog.dispose();
                checkoutDialog(p);
            });

            actions.add(saveBtn);
            actions.add(buyBtn);

            if (p.isOpenToExchange()) {
                UITheme.ModernButton swapBtn = new UITheme.ModernButton("Propose Exchange", UITheme.ButtonStyle.SECONDARY);
                swapBtn.setPreferredSize(new Dimension(150, 38));
                swapBtn.setToolTipText("Trade one of your items for this listing");
                swapBtn.addActionListener(e -> {
                    dialog.dispose();
                    proposeExchangeDialog(p);
                });
                actions.add(swapBtn);
            }

            UITheme.ModernButton chatBtn = new UITheme.ModernButton("Chat with Seller", UITheme.ButtonStyle.SECONDARY);
            chatBtn.setPreferredSize(new Dimension(150, 38));
            chatBtn.setToolTipText("Contact " + p.getSeller().getName() + " with your phone number and meetup chat message");
            chatBtn.addActionListener(e -> chatWithSellerDialog(p));
            actions.add(chatBtn);
        }

        actions.add(closeBtn);
        details.add(actions);

        // --- Reviews Section ---
        details.add(Box.createVerticalStrut(14));
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        details.add(sep);
        details.add(Box.createVerticalStrut(10));

        JLabel reviewsHeading = UITheme.label("Seller Reputation (" + p.getSeller().getName() + ")", 15f, true, UITheme.TEXT_MAIN);
        reviewsHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        details.add(reviewsHeading);
        details.add(Box.createVerticalStrut(8));

        List<Review> reviews = service.reviewsForSeller(p.getSeller().getId());
        if (reviews.isEmpty()) {
            JLabel noReviews = UITheme.label("New seller \u00B7 No campus reviews yet.", 12.5f, false, UITheme.TEXT_MUTED);
            noReviews.setAlignmentX(Component.LEFT_ALIGNMENT);
            details.add(noReviews);
        } else {
            double avg = service.averageSellerRating(p.getSeller().getId());
            int rounded = (int) Math.round(avg);
            StringBuilder avgStars = new StringBuilder();
            for (int i = 1; i <= 5; i++) avgStars.append(i <= rounded ? "\u2605" : "\u2606");
            JLabel avgLabel = UITheme.symbolLabel(
                    avgStars + "  " + String.format("%.1f", avg) + " / 5  ("
                            + reviews.size() + (reviews.size() == 1 ? " review)" : " reviews)"),
                    13.5f, true, new Color(0xF5, 0x9E, 0x0B));
            avgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            details.add(avgLabel);
            details.add(Box.createVerticalStrut(10));

            JPanel reviewList = new JPanel();
            reviewList.setOpaque(false);
            reviewList.setLayout(new BoxLayout(reviewList, BoxLayout.Y_AXIS));

            for (Review rv : reviews) {
                UITheme.CardPanel reviewCard = new UITheme.CardPanel(8, new Color(0xF9, 0xFA, 0xFB), UITheme.BORDER);
                reviewCard.setLayout(new BoxLayout(reviewCard, BoxLayout.Y_AXIS));
                reviewCard.setBorder(new EmptyBorder(8, 12, 8, 12));
                reviewCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                reviewCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

                StringBuilder stars = new StringBuilder();
                for (int i = 1; i <= 5; i++) stars.append(i <= rv.getRating() ? "\u2605" : "\u2606");
                JLabel rvStars = UITheme.symbolLabel(stars.toString(), 13f, true, new Color(0xF5, 0x9E, 0x0B));
                JLabel rvName  = UITheme.label("  by " + rv.getReviewer().getName(), 12f, false, UITheme.TEXT_MUTED);
                JPanel rvHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                rvHeader.setOpaque(false);
                rvHeader.add(rvStars);
                rvHeader.add(rvName);
                rvHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
                reviewCard.add(rvHeader);

                if (!rv.getComment().isBlank()) {
                    reviewCard.add(Box.createVerticalStrut(4));
                    JLabel rvComment = UITheme.label("\"" + rv.getComment() + "\"", 12.5f, false, UITheme.TEXT_MAIN);
                    rvComment.setAlignmentX(Component.LEFT_ALIGNMENT);
                    reviewCard.add(rvComment);
                }

                if (!rv.getTags().isEmpty()) {
                    reviewCard.add(Box.createVerticalStrut(4));
                    JPanel tagFlow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                    tagFlow.setOpaque(false);
                    tagFlow.setAlignmentX(Component.LEFT_ALIGNMENT);
                    for (String t : rv.getTags()) {
                        tagFlow.add(new UITheme.BadgeLabel(t, UITheme.TAG_TEXT, UITheme.TAG_BG));
                    }
                    reviewCard.add(tagFlow);
                }

                reviewList.add(reviewCard);
                reviewList.add(Box.createVerticalStrut(6));
            }

            JScrollPane rvScroll = new JScrollPane(reviewList);
            rvScroll.setPreferredSize(new Dimension(0, 160));
            rvScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
            rvScroll.setBorder(BorderFactory.createEmptyBorder());
            rvScroll.getViewport().setBackground(UITheme.SURFACE);
            rvScroll.getVerticalScrollBar().setUnitIncrement(8);
            rvScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
            details.add(rvScroll);
        }

        // "Rate Seller" button - only for eligible buyers
        if (service.canReview(p.getId())) {
            details.add(Box.createVerticalStrut(10));
            UITheme.ModernButton writeReview = new UITheme.ModernButton("Rate Seller", UITheme.ButtonStyle.SECONDARY);
            writeReview.setAlignmentX(Component.LEFT_ALIGNMENT);
            writeReview.setPreferredSize(new Dimension(160, 36));
            writeReview.setToolTipText("You have completed an order with this seller - leave your feedback");
            writeReview.addActionListener(e -> {
                dialog.dispose();
                reviewDialog(p);
            });
            details.add(writeReview);
        }

        // More items from this seller
        List<Product> moreFromSeller = service.products().stream()
                .filter(other -> other.getSeller().equals(p.getSeller()) && !other.getId().equals(p.getId()))
                .toList();

        if (!moreFromSeller.isEmpty()) {
            details.add(Box.createVerticalStrut(10));
            JPanel moreBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            moreBox.setOpaque(false);
            moreBox.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel moreLabel = UITheme.label("More from " + p.getSeller().getName() + ":", 12f, true, UITheme.TEXT_MUTED);
            moreBox.add(moreLabel);

            for (Product other : moreFromSeller) {
                UITheme.ModernButton itemBtn = new UITheme.ModernButton(
                        other.getName() + " (\u20B9" + String.format("%.0f", other.getPrice()) + ")",
                        UITheme.ButtonStyle.OUTLINE
                );
                itemBtn.setFont(UITheme.medium(11f));
                itemBtn.setToolTipText("View " + other.getName());
                itemBtn.addActionListener(e -> {
                    dialog.dispose();
                    showProductDialog(other);
                });
                moreBox.add(itemBtn);
            }
            details.add(moreBox);
        }

        content.add(details);
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setSize(920, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void reviewDialog(Product p) {
        JDialog dialog = new JDialog(this, "Rate Seller: " + p.getSeller().getName(), true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UITheme.SURFACE);
        content.setBorder(new EmptyBorder(22, 26, 22, 26));

        // Header
        JLabel heading = UITheme.label("Rate your experience with " + p.getSeller().getName(), 18f, true, UITheme.TEXT_MAIN);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = UITheme.label("Deal item: " + p.getName() + "  \u00B7  Seller: " + p.getSeller().getDepartment() + ", Year " + p.getSeller().getYear(), 12.5f, false, UITheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(heading);
        content.add(Box.createVerticalStrut(4));
        content.add(sub);
        content.add(Box.createVerticalStrut(16));

        // Star Rating selector
        JLabel starTitle = UITheme.label("Overall Seller Rating", 13.5f, true, UITheme.TEXT_MAIN);
        starTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(starTitle);
        content.add(Box.createVerticalStrut(4));

        final int[] selectedRating = {5};
        JPanel starPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        starPanel.setOpaque(false);
        starPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton[] starBtns = new JButton[5];
        JLabel ratingText = UITheme.label("  5.0 / 5.0 (Excellent)", 13f, true, new Color(0xF5, 0x9E, 0x0B));

        Runnable updateStarDisplay = () -> {
            for (int i = 0; i < 5; i++) {
                starBtns[i].setText(i < selectedRating[0] ? "\u2605" : "\u2606");
            }
            String desc = switch (selectedRating[0]) {
                case 5 -> "  5.0 / 5.0 (Excellent)";
                case 4 -> "  4.0 / 5.0 (Very Good)";
                case 3 -> "  3.0 / 5.0 (Average)";
                case 2 -> "  2.0 / 5.0 (Poor)";
                default -> "  1.0 / 5.0 (Terrible)";
            };
            ratingText.setText(desc);
        };

        for (int i = 0; i < 5; i++) {
            final int starVal = i + 1;
            JButton btn = new JButton("\u2605");
            btn.setFont(UITheme.symbolFont(26f, Font.BOLD));
            btn.setForeground(new Color(0xF5, 0x9E, 0x0B));
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setMargin(new Insets(0, 2, 0, 2));
            btn.addActionListener(e -> {
                selectedRating[0] = starVal;
                updateStarDisplay.run();
            });
            starBtns[i] = btn;
            starPanel.add(btn);
        }
        starPanel.add(ratingText);
        content.add(starPanel);
        content.add(Box.createVerticalStrut(14));

        // Quick Tags Checkboxes
        JLabel tagsTitle = UITheme.label("Select Experience Badges", 13.5f, true, UITheme.TEXT_MAIN);
        tagsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(tagsTitle);
        content.add(Box.createVerticalStrut(6));

        JPanel tagsPanel = new JPanel(new GridLayout(2, 3, 10, 8));
        tagsPanel.setOpaque(false);
        tagsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        tagsPanel.setPreferredSize(new Dimension(500, 64));

        String[] badgeOptions = new String[]{
                "Item as described",
                "On-time meetup",
                "Tested in person",
                "Quick response",
                "Friendly senior",
                "Fair price"
        };
        List<JCheckBox> checkBoxes = new ArrayList<>();
        for (String tagText : badgeOptions) {
            JCheckBox cb = new JCheckBox(tagText);
            cb.setFont(UITheme.medium(12.5f));
            cb.setForeground(UITheme.TEXT_MAIN);
            cb.setOpaque(false);
            cb.setFocusPainted(false);
            if (tagText.equals("Item as described") || tagText.equals("On-time meetup")) {
                cb.setSelected(true);
            }
            checkBoxes.add(cb);
            tagsPanel.add(cb);
        }
        content.add(tagsPanel);
        content.add(Box.createVerticalStrut(14));

        // Comment area
        JLabel commentTitle = UITheme.label("Student Experience Feedback (Optional)", 13.5f, true, UITheme.TEXT_MAIN);
        commentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(commentTitle);
        content.add(Box.createVerticalStrut(4));

        JTextArea commentArea = new JTextArea(4, 30);
        commentArea.setFont(UITheme.regular(13f));
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane commentScroll = new JScrollPane(commentArea);
        commentScroll.setBorder(UITheme.roundedBorder(8, UITheme.BORDER, 1));
        commentScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(commentScroll);
        content.add(Box.createVerticalStrut(18));

        // Buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        UITheme.ModernButton cancel = new UITheme.ModernButton("Cancel", UITheme.ButtonStyle.SECONDARY);
        cancel.setPreferredSize(new Dimension(90, 38));
        cancel.addActionListener(e -> dialog.dispose());

        UITheme.ModernButton submit = new UITheme.ModernButton("Submit Review", UITheme.ButtonStyle.PRIMARY);
        submit.setPreferredSize(new Dimension(140, 38));
        submit.addActionListener(e -> {
            try {
                List<String> chosenTags = new ArrayList<>();
                for (JCheckBox cb : checkBoxes) {
                    if (cb.isSelected()) chosenTags.add(cb.getText());
                }
                service.addReview(p.getId(), selectedRating[0], commentArea.getText(), chosenTags);
                dialog.dispose();
                refreshAll();
                JOptionPane.showMessageDialog(this,
                        "Thank you! Your review for " + p.getSeller().getName() + " has been posted.",
                        "Review Submitted", JOptionPane.INFORMATION_MESSAGE);
            } catch (RuntimeException ex) {
                showError(ex);
            }
        });

        buttonRow.add(cancel);
        buttonRow.add(submit);
        content.add(buttonRow);

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setSize(580, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void checkoutDialog(Product p) {
        JDialog dialog = new JDialog(this, "Confirm Cash on Delivery", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(UITheme.SURFACE);
        content.setBorder(new EmptyBorder(26, 28, 24, 28));

        // Header
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(UITheme.label("Confirm your order", 22f, true, UITheme.TEXT_MAIN));
        heading.add(Box.createVerticalStrut(4));
        heading.add(UITheme.label("Cash on delivery exchange directly between students on campus.", 13f, false, UITheme.TEXT_MUTED));
        content.add(heading, BorderLayout.NORTH);

        // Summary Card
        UITheme.CardPanel summary = new UITheme.CardPanel(14, new Color(0xF9, 0xFA, 0xFB), UITheme.BORDER);
        summary.setLayout(new BorderLayout(16, 0));
        summary.setBorder(new EmptyBorder(14, 14, 14, 14));
        summary.add(photoThumbnailLabel(p.getImageBytes(), 170, 130), BorderLayout.WEST);

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.add(UITheme.label(p.getName(), 18f, true, UITheme.TEXT_MAIN));
        copy.add(Box.createVerticalStrut(6));
        copy.add(UITheme.label(p.getCategory().name() + "  \u00B7  Seller: " + p.getSeller().getName(), 12.5f, false, UITheme.TEXT_MUTED));
        copy.add(Box.createVerticalStrut(12));
        copy.add(UITheme.label("\u20B9" + String.format("%.0f", p.getPrice()), 20f, true, UITheme.PRICE_COLOR));
        copy.add(Box.createVerticalStrut(6));
        copy.add(new UITheme.BadgeLabel("PAYMENT: CASH ON DELIVERY", UITheme.TAG_BG, UITheme.TAG_TEXT));

        summary.add(copy, BorderLayout.CENTER);
        content.add(summary, BorderLayout.CENTER);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        UITheme.ModernButton cancel = new UITheme.ModernButton("Cancel", UITheme.ButtonStyle.SECONDARY);
        cancel.setPreferredSize(new Dimension(90, 38));
        cancel.addActionListener(e -> dialog.dispose());

        UITheme.ModernButton confirm = new UITheme.ModernButton("Place order", UITheme.ButtonStyle.PRIMARY);
        confirm.setPreferredSize(new Dimension(140, 38));
        confirm.addActionListener(e -> {
            try {
                Order order = service.placeOrder(p.getId());
                dialog.dispose();
                refreshAll();
                JOptionPane.showMessageDialog(
                        this,
                        "Your order has been placed successfully!\nOrder ID: " + order.getId(),
                        "Order Confirmed",
                        JOptionPane.INFORMATION_MESSAGE
                );
                navBar.selectTab("orders");
            } catch (RuntimeException ex) {
                showError(ex);
            }
        });

        actions.add(cancel);
        actions.add(confirm);
        content.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setSize(640, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void chatWithSellerDialog(Product p) {
        Student seller = p.getSeller();
        Student me = service.currentUser();

        JDialog dialog = new JDialog(this, "Chat with Seller: " + seller.getName(), true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UITheme.SURFACE);
        content.setBorder(new EmptyBorder(22, 26, 22, 26));

        // 1. Header & Seller Info Card
        JLabel heading = UITheme.label("Chat & Contact Seller", 18f, true, UITheme.TEXT_MAIN);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(heading);
        content.add(Box.createVerticalStrut(4));

        JLabel sub = UITheme.label("Send your contact phone number and message to coordinate a campus meetup.", 12.5f, false, UITheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sub);
        content.add(Box.createVerticalStrut(14));

        UITheme.CardPanel sellerCard = new UITheme.CardPanel(10, new Color(0xF9, 0xFA, 0xFB), UITheme.BORDER);
        sellerCard.setLayout(new BoxLayout(sellerCard, BoxLayout.Y_AXIS));
        sellerCard.setBorder(new EmptyBorder(12, 14, 12, 14));
        sellerCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        sellerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel sNameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        sNameRow.setOpaque(false);
        sNameRow.add(UITheme.label("Seller: " + seller.getName(), 14f, true, UITheme.TEXT_MAIN));
        sNameRow.add(new UITheme.BadgeLabel(seller.getDepartment() + " \u00B7 Year " + seller.getYear(), UITheme.TAG_TEXT, UITheme.TAG_BG));
        sellerCard.add(sNameRow);
        sellerCard.add(Box.createVerticalStrut(4));

        JLabel sPhoneLabel = UITheme.label("Seller Phone / WhatsApp: " + seller.getPhoneNumber(), 12.5f, true, UITheme.PRIMARY);
        sellerCard.add(sPhoneLabel);
        sellerCard.add(Box.createVerticalStrut(2));

        JLabel sItemLabel = UITheme.label("Listing: " + p.getName() + " (\u20B9" + String.format("%.0f", p.getPrice()) + ")", 12f, false, UITheme.TEXT_MUTED);
        sellerCard.add(sItemLabel);

        content.add(sellerCard);
        content.add(Box.createVerticalStrut(16));

        // 2. Buyer's Phone Number Input
        JLabel phoneTitle = UITheme.label("Your Phone Number (WhatsApp / Calling)", 13.5f, true, UITheme.TEXT_MAIN);
        phoneTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(phoneTitle);
        content.add(Box.createVerticalStrut(4));

        JTextField phoneInput = createModernTextField(26);
        phoneInput.setText(me.getPhoneNumber());
        phoneInput.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(phoneInput);
        content.add(Box.createVerticalStrut(4));

        JLabel phoneHelp = UITheme.label("The seller will use this number to message or call you back.", 11.5f, false, UITheme.TEXT_MUTED);
        phoneHelp.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(phoneHelp);
        content.add(Box.createVerticalStrut(14));

        // 3. Quick Message Options for Chat
        JLabel optionsTitle = UITheme.label("Quick Chat Options (Click to select)", 13f, true, UITheme.TEXT_MAIN);
        optionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(optionsTitle);
        content.add(Box.createVerticalStrut(6));

        JPanel optionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        optionsRow.setOpaque(false);
        optionsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] quickOptions = new String[]{
                "Hi, is this still available?",
                "Can we meet near the library?",
                "What time works best for you?",
                "Is the price negotiable?"
        };

        JTextArea messageInput = new JTextArea(4, 28);
        messageInput.setFont(UITheme.regular(13f));
        messageInput.setLineWrap(true);
        messageInput.setWrapStyleWord(true);
        messageInput.setText("Hi " + seller.getName() + ", I am interested in buying your " + p.getName() + ". Can we meet on campus?");
        messageInput.setBorder(new EmptyBorder(8, 10, 8, 10));

        for (String opt : quickOptions) {
            JButton chip = new JButton(opt);
            chip.setFont(UITheme.regular(11.5f));
            chip.setForeground(UITheme.PRIMARY);
            chip.setBackground(new Color(0xEC, 0xFD, 0xF5));
            chip.setBorder(UITheme.roundedBorder(6, new Color(0xA7, 0xF3, 0xD0), 1));
            chip.setFocusPainted(false);
            chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            chip.setMargin(new Insets(3, 8, 3, 8));
            chip.addActionListener(e -> {
                messageInput.setText(opt);
                messageInput.requestFocusInWindow();
            });
            optionsRow.add(chip);
        }
        content.add(optionsRow);
        content.add(Box.createVerticalStrut(10));

        // 4. Message Input Area
        JLabel msgTitle = UITheme.label("Chat Message", 13.5f, true, UITheme.TEXT_MAIN);
        msgTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(msgTitle);
        content.add(Box.createVerticalStrut(4));

        JScrollPane msgScroll = new JScrollPane(messageInput);
        msgScroll.setBorder(UITheme.roundedBorder(8, UITheme.BORDER, 1));
        msgScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(msgScroll);
        content.add(Box.createVerticalStrut(18));

        // 5. Actions Row
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        UITheme.ModernButton cancelBtn = new UITheme.ModernButton("Cancel", UITheme.ButtonStyle.SECONDARY);
        cancelBtn.setPreferredSize(new Dimension(90, 38));
        cancelBtn.addActionListener(e -> dialog.dispose());

        UITheme.ModernButton sendBtn = new UITheme.ModernButton("Send Message", UITheme.ButtonStyle.PRIMARY);
        sendBtn.setPreferredSize(new Dimension(140, 38));
        sendBtn.addActionListener(e -> {
            String enteredPhone = phoneInput.getText().trim();
            String enteredMsg = messageInput.getText().trim();

            if (enteredPhone.isBlank()) {
                showError(new IllegalArgumentException("Please enter your phone number so the seller can contact you."));
                return;
            }
            if (enteredMsg.isBlank()) {
                showError(new IllegalArgumentException("Please type a message for the chat."));
                return;
            }

            dialog.dispose();
            JOptionPane.showMessageDialog(
                    this,
                    "<html><b>Chat message sent to " + seller.getName() + "!</b><br><br>" +
                            "<b>Listing:</b> " + p.getName() + "<br>" +
                            "<b>Seller Contact:</b> " + seller.getPhoneNumber() + "<br>" +
                            "<b>Your Phone:</b> " + enteredPhone + "<br>" +
                            "<b>Message:</b> \"" + enteredMsg + "\"<br><br>" +
                            seller.getName() + " will call or message you on WhatsApp to coordinate the meetup on campus.</html>",
                    "Message Sent to Seller",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        actions.add(cancelBtn);
        actions.add(sendBtn);
        content.add(actions);

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setSize(560, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void proposeExchangeDialog(Product target) {
        Student me = service.currentUser();
        List<Product> myItems = service.myListings().stream()
                .filter(p -> "AVAILABLE".equals(p.getStatus().name()))
                .toList();

        if (myItems.isEmpty()) {
            int ans = JOptionPane.showConfirmDialog(
                    this,
                    "<html><b>You don't have any active listings to offer in exchange!</b><br><br>" +
                    "To swap items on campus, you must list an item of your own first.<br>" +
                    "Would you like to list a new item now?</html>",
                    "No Items Available to Swap",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );
            if (ans == JOptionPane.YES_OPTION) {
                productDialog(null);
            }
            return;
        }

        JDialog dialog = new JDialog(this, "Propose Exchange: " + target.getName(), true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UITheme.SURFACE);
        content.setBorder(new EmptyBorder(22, 26, 22, 26));

        // Header
        JLabel heading = UITheme.label("Propose Campus Item Swap", 18f, true, UITheme.TEXT_MAIN);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(heading);
        content.add(Box.createVerticalStrut(4));

        JLabel sub = UITheme.label("Trade one of your items for " + target.getSeller().getName() + "’s listing.", 12.5f, false, UITheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sub);
        content.add(Box.createVerticalStrut(14));

        // Target Summary Card
        UITheme.CardPanel targetCard = new UITheme.CardPanel(10, new Color(0xF0, 0xFD, 0xFA), new Color(0x99, 0xF6, 0xE4));
        targetCard.setLayout(new BoxLayout(targetCard, BoxLayout.Y_AXIS));
        targetCard.setBorder(new EmptyBorder(10, 14, 10, 14));
        targetCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        targetCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        JLabel targetTitle = UITheme.label("Item you want: " + target.getName() + " (\u20B9" + String.format("%.0f", target.getPrice()) + ")", 13.5f, true, UITheme.TEXT_MAIN);
        JLabel targetSeller = UITheme.label("Seller: " + target.getSeller().getName() + "  \u00B7  " + target.getSeller().getDepartment(), 12f, false, UITheme.TEXT_MUTED);
        String prefText = target.getExchangePreference().isBlank() ? "Open to general trades" : target.getExchangePreference();
        JLabel targetPref = UITheme.label("Seller looking for: " + prefText, 12f, true, new Color(0x0F, 0x76, 0x6E));

        targetCard.add(targetTitle);
        targetCard.add(Box.createVerticalStrut(2));
        targetCard.add(targetSeller);
        targetCard.add(Box.createVerticalStrut(2));
        targetCard.add(targetPref);
        content.add(targetCard);
        content.add(Box.createVerticalStrut(16));

        // Select offered item
        JLabel offerLabel = UITheme.label("Choose your item to offer in trade:", 13.5f, true, UITheme.TEXT_MAIN);
        offerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(offerLabel);
        content.add(Box.createVerticalStrut(4));

        JComboBox<Product> itemCombo = new JComboBox<>(myItems.toArray(new Product[0]));
        itemCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Product p) {
                    setText(p.getName() + " (" + p.getCategory().name() + " \u00B7 \u20B9" + String.format("%.0f", p.getPrice()) + ")");
                }
                return this;
            }
        });
        itemCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        itemCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(itemCombo);
        content.add(Box.createVerticalStrut(14));

        // Meetup Location / Notes
        JLabel meetupLabel = UITheme.label("Proposed Meetup Location & Time", 13.5f, true, UITheme.TEXT_MAIN);
        meetupLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(meetupLabel);
        content.add(Box.createVerticalStrut(4));

        JTextField meetupInput = createModernTextField(26);
        meetupInput.setText("Central Library foyer, 4:30 PM");
        meetupInput.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(meetupInput);
        content.add(Box.createVerticalStrut(18));

        // Actions Row
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        UITheme.ModernButton cancelBtn = new UITheme.ModernButton("Cancel", UITheme.ButtonStyle.SECONDARY);
        cancelBtn.setPreferredSize(new Dimension(90, 38));
        cancelBtn.addActionListener(e -> dialog.dispose());

        UITheme.ModernButton confirmBtn = new UITheme.ModernButton("Confirm Exchange Deal", UITheme.ButtonStyle.PRIMARY);
        confirmBtn.setPreferredSize(new Dimension(190, 38));
        confirmBtn.addActionListener(e -> {
            Product offered = (Product) itemCombo.getSelectedItem();
            if (offered == null) return;
            String notes = meetupInput.getText().trim();
            if (notes.isBlank()) notes = "Campus meetup";

            try {
                Order exchangeOrder = service.proposeExchange(target.getId(), offered.getId(), notes);
                dialog.dispose();
                refreshAll();

                JOptionPane.showMessageDialog(
                        this,
                        "<html><h3 style='color:#059669;'>Campus Exchange Confirmed!</h3><br>" +
                        "<b>Item you receive:</b> " + target.getName() + " (from " + target.getSeller().getName() + ")<br>" +
                        "<b>Item you give:</b> " + offered.getName() + "<br>" +
                        "<b>Seller Phone:</b> " + target.getSeller().getPhoneNumber() + "<br>" +
                        "<b>Meetup Spot:</b> " + notes + "<br><br>" +
                        "Both items are now marked as <b>Exchanged</b> on the campus marketplace.<br>" +
                        "You can review this deal anytime in <b>My orders</b>!</html>",
                        "Campus Exchange Pass",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (RuntimeException ex) {
                showError(ex);
            }
        });

        actions.add(cancelBtn);
        actions.add(confirmBtn);
        content.add(actions);

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setSize(540, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // =========================================================================
    // 5. Data Refresh & Synchronization
    // =========================================================================

    public void refreshAll() {
        Student current = service.currentUser();
        userPillLabel.setText("Hi, " + current.getName());

        if (homePanel != null) homePanel.refresh();
        updateProfile();
        browsePanel.refresh();
        refreshWishlist();
        refreshMyListings();
        refreshOrders();
    }

    private void refreshWishlist() {
        if (wishlistGrid == null) return;
        wishlistGrid.removeAll();

        List<Product> products = service.wishlistProducts();
        Student current = service.currentUser();

        for (Product product : products) {
            wishlistGrid.add(new ProductCardPanel(product, current, true, this, service.reviewsForSeller(product.getSeller().getId())));
        }

        int count = products.size();
        wishlistCountLabel.setText(count + (count == 1 ? " saved item" : " saved items"));

        if (products.isEmpty()) {
            wishlistGrid.add(createEmptySection("Your wishlist is empty", "Save items from Browse products to keep track of them."));
        }

        if (navBar != null) {
            navBar.updateTabTitle("wishlist", count > 0 ? "Wishlist (" + count + ")" : "Wishlist");
        }

        wishlistGrid.revalidate();
        wishlistGrid.repaint();
    }

    private void refreshMyListings() {
        if (myListingsGrid == null) return;
        myListingsGrid.removeAll();

        List<Product> products = service.myListings();
        Student current = service.currentUser();

        for (Product product : products) {
            boolean isSaved = service.isWishlisted(product.getId());
            myListingsGrid.add(new ProductCardPanel(product, current, isSaved, this, service.reviewsForSeller(product.getSeller().getId())));
        }

        myListingsCountLabel.setText(products.size() + (products.size() == 1 ? " listed item" : " listed items"));

        if (products.isEmpty()) {
            myListingsGrid.add(createEmptySection("No listings yet", "Click \"List a new item\" to sell textbooks, electronics, or supplies!"));
        }

        myListingsGrid.revalidate();
        myListingsGrid.repaint();
    }

    private void updateProfile() {
        Student s = service.currentUser();
        profileName.setText(s.getName());
        profileEmail.setText(s.getEmail());
        profileStudentDetails.setText("Student ID: " + s.getStudentId() + "  \u00B7  " + s.getDepartment() + "  \u00B7  Year " + s.getYear() + "  \u00B7  Phone: " + s.getPhoneNumber());

        double avg = service.averageSellerRating(s.getId());
        int count = service.sellerReviewCount(s.getId());
        if (count == 0) {
            profileReputation.setText("Seller Reputation: New seller (no reviews yet)");
            profileReputation.setForeground(UITheme.TEXT_MUTED);
        } else {
            profileReputation.setText(String.format("Seller Reputation: %.1f / 5.0 (%d campus review%s)", avg, count, count == 1 ? "" : "s"));
            profileReputation.setForeground(new Color(0xD9, 0x77, 0x06));
        }
    }

    private void refreshOrders() {
        if (ordersTable == null) return;
        ordersModel.setRowCount(0);

        for (Order order : service.myOrders()) {
            boolean buyer = order.getBuyer().equals(service.currentUser());
            Student other = buyer ? order.getSeller() : order.getBuyer();
            String titleText = order.getProduct().getName() + (order.isExchange() ? " [Swap]" : "");
            String roleText = order.isExchange() ? (buyer ? "Buyer (Swap)" : "Seller (Swap)") : (buyer ? "Buyer" : "Seller");
            String totalText = order.isExchange() ? "Item Swap" : ("\u20B9" + String.format("%.0f", order.getTotal()));

            ordersModel.addRow(new Object[]{
                    titleText,
                    other.getName(),
                    roleText,
                    totalText,
                    order.getStatus(),
                    order.getId()
            });
        }

        if (ordersModel.getRowCount() > 0) {
            ordersTable.setRowSelectionInterval(0, 0);
        } else {
            clearOrderPreview();
        }
    }

    private void refreshSelectedOrder() {
        if (ordersTable.getSelectedRow() < 0) {
            clearOrderPreview();
            return;
        }
        try {
            Order order = getSelectedOrder();
            Product product = order.getProduct();
            setPhotoThumbnail(selectedOrderImage, product.getImageBytes(), 278, 160, "No product photo");
            selectedOrderTitle.setText(product.getName());
            if (order.isExchange()) {
                selectedOrderMeta.setText("Campus Swap Deal  \u00B7  Status: " + order.getStatus());
                String detailsText = "Exchange Meetup: " + order.getMeetupNotes() +
                        "\n\nListing Item: " + product.getName() + " (" + product.getCategory().name() + ")" +
                        "\nOffered in Trade: " + (order.getOfferedProduct() != null ? order.getOfferedProduct().getName() : "Listed item") +
                        "\n\nBoth students meet on campus to inspect and swap items.";
                selectedOrderDescription.setText(detailsText);
            } else {
                selectedOrderMeta.setText("\u20B9" + String.format("%.0f", order.getTotal()) + "  \u00B7  Status: " + order.getStatus());
                selectedOrderDescription.setText(product.getDescription().isBlank() ? "No description provided." : product.getDescription());
            }
        } catch (RuntimeException ignored) {
            clearOrderPreview();
        }
    }

    private void clearOrderPreview() {
        if (selectedOrderImage == null) return;
        setPhotoThumbnail(selectedOrderImage, null, 278, 160, "Select an order");
        selectedOrderTitle.setText("Select an order");
        selectedOrderMeta.setText("Choose an order to view its item details.");
        selectedOrderDescription.setText("");
    }

    private Order getSelectedOrder() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) throw new IllegalArgumentException("Select an order first.");
        row = ordersTable.convertRowIndexToModel(row);
        UUID id = (UUID) ordersModel.getValueAt(row, 5);
        return service.myOrders().stream()
                .filter(o -> o.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));
    }

    private void withSelectedOrder(java.util.function.Consumer<Order> action) {
        try {
            action.accept(getSelectedOrder());
        } catch (RuntimeException ex) {
            showError(ex);
        }
    }

    private void showError(RuntimeException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage() == null ? "The action could not be completed." : ex.getMessage(),
                "Notice", JOptionPane.WARNING_MESSAGE);
    }

    private JPanel createEmptySection(String title, String subtitle) {
        UITheme.CardPanel card = new UITheme.CardPanel(16);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(500, 180));
        card.setBorder(new EmptyBorder(30, 32, 30, 32));

        JLabel t = UITheme.label(title, 18f, true, UITheme.TEXT_MAIN);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel st = UITheme.label(subtitle, 13f, false, UITheme.TEXT_MUTED);
        st.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalStrut(10));
        card.add(t);
        card.add(Box.createVerticalStrut(8));
        card.add(st);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(900, 240));
        wrapper.add(card);
        return wrapper;
    }

    // =========================================================================
    // 6. UI Helpers & Custom Component Styling
    // =========================================================================

    private JTextField createModernTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(UITheme.regular(13.5f));
        tf.setPreferredSize(new Dimension(240, 38));
        tf.setBorder(BorderFactory.createCompoundBorder(
                UITheme.roundedBorder(8, UITheme.BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    private JPasswordField createModernPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(UITheme.regular(13.5f));
        pf.setPreferredSize(new Dimension(240, 38));
        pf.setBorder(BorderFactory.createCompoundBorder(
                UITheme.roundedBorder(8, UITheme.BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    private JLabel photoThumbnailLabel(byte[] bytes, int width, int height) {
        JLabel label = new JLabel("No photo", SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(0xF3, 0xF4, 0xF6));
        label.setForeground(UITheme.TEXT_MUTED);
        label.setPreferredSize(new Dimension(width, height));
        label.setFont(UITheme.regular(12f));
        label.setBorder(UITheme.roundedBorder(10, UITheme.BORDER, 1));
        setPhotoThumbnail(label, bytes, width, height, "No photo");
        return label;
    }

    private void setPhotoThumbnail(JLabel label, byte[] bytes, int width, int height, String emptyText) {
        label.setIcon(null);
        label.setText(emptyText);
        if (bytes != null && bytes.length > 0) {
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                if (img != null) {
                    label.setIcon(createScaledIcon(img, width, height));
                    label.setText("");
                }
            } catch (Exception ignored) {}
        }
    }

    private ImageIcon createScaledIcon(BufferedImage image, int width, int height) {
        double scale = Math.min((double) width / image.getWidth(), (double) height / image.getHeight());
        int w = (int) (image.getWidth() * scale);
        int h = (int) (image.getHeight() * scale);
        Image scaled = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    // =========================================================================
    // 7. Modern Segmented Pill Navigation Bar
    // =========================================================================

    public static class SegmentedNavBar extends JPanel {
        public interface TabSelectionListener {
            void onTabSelected(String tabKey);
        }

        private final TabSelectionListener listener;
        private final List<PillTab> tabs = new ArrayList<>();
        private PillTab selectedTab;

        public SegmentedNavBar(TabSelectionListener listener) {
            this.listener = listener;
            setLayout(new FlowLayout(FlowLayout.LEFT, 8, 10));
            setBackground(UITheme.SURFACE);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
        }

        public void addTab(String key, String title) {
            PillTab tab = new PillTab(key, title);
            tab.addActionListener(e -> selectTab(tab));
            tabs.add(tab);
            add(tab);

            if (tabs.size() == 1) {
                selectTab(tab);
            }
        }

        public void selectTab(String key) {
            for (PillTab tab : tabs) {
                if (tab.key.equals(key)) {
                    selectTab(tab);
                    return;
                }
            }
        }

        public void updateTabTitle(String key, String title) {
            for (PillTab tab : tabs) {
                if (tab.key.equals(key)) {
                    tab.setText(title);
                    revalidate();
                    repaint();
                    return;
                }
            }
        }

        private void selectTab(PillTab tab) {
            if (selectedTab != null) selectedTab.setSelected(false);
            selectedTab = tab;
            selectedTab.setSelected(true);
            if (listener != null) listener.onTabSelected(tab.key);
        }

        private static class PillTab extends JButton {
            private final String key;
            private boolean isSelected = false;
            private boolean isHovered = false;

            public PillTab(String key, String title) {
                super(title);
                this.key = key;
                setFont(UITheme.bold(13f));
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setMargin(new Insets(8, 16, 8, 16));

                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseEntered(java.awt.event.MouseEvent e) { isHovered = true; repaint(); }
                    @Override public void mouseExited(java.awt.event.MouseEvent e) { isHovered = false; repaint(); }
                });
            }

            public void setSelected(boolean selected) {
                this.isSelected = selected;
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                if (isSelected) {
                    g2.setColor(UITheme.PRIMARY);
                    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
                    setForeground(Color.WHITE);
                } else if (isHovered) {
                    g2.setColor(new Color(0xF3, 0xF4, 0xF6));
                    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
                    setForeground(UITheme.TEXT_MAIN);
                } else {
                    setForeground(UITheme.TEXT_MUTED);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        }
    }
}
