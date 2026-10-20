package edu.campusmarketplace.ui;

import edu.campusmarketplace.model.*;
import edu.campusmarketplace.service.MarketplaceService;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

/** Polished Swing experience for the single-session campus marketplace. */
public final class MarketplaceFrame extends JFrame {
    private static final Color INK = new Color(30, 41, 59);
    private static final Color MUTED = new Color(101, 116, 139);
    private static final Color BRAND = new Color(20, 93, 80);
    private static final Color CANVAS = new Color(246, 248, 247);
    private static final Color LINE = new Color(226, 232, 230);

    private final MarketplaceService service;
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final DefaultTableModel ordersModel = new DefaultTableModel(new Object[]{"Product", "Other student", "Role", "Total (₹)", "Status", "Order ID"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private JPanel listingGrid;
    private JPanel wishlistGrid;
    private JPanel myListingsGrid;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JLabel welcome;
    private JLabel profileName;
    private JLabel profileEmail;
    private JLabel profileStudentDetails;
    private JTable ordersTable;
    private JLabel selectedOrderImage;
    private JLabel selectedOrderTitle;
    private JLabel selectedOrderMeta;
    private JTextArea selectedOrderDescription;

    public MarketplaceFrame(MarketplaceService service) {
        super("Campus Market"); this.service = service;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setMinimumSize(new Dimension(1000, 680)); setSize(1220, 800); setLocationRelativeTo(null);
        getContentPane().setBackground(CANVAS);
        root.add(buildAuth(), "auth"); root.add(buildMarketplace(), "market"); setContentPane(root); cards.show(root, "auth");
    }

    private JPanel buildAuth() {
        JPanel page = new JPanel(new GridBagLayout()); page.setBackground(new Color(239, 245, 242));
        JPanel card = roundedPanel(Color.WHITE, 24); card.setLayout(new GridLayout(1, 2)); card.setPreferredSize(new Dimension(820, 490));
        JPanel intro = new JPanel(); intro.setBackground(BRAND); intro.setBorder(new EmptyBorder(42, 38, 38, 34)); intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        JLabel eyebrow = label("CAMPUS COMMUNITY", 12, true, new Color(190, 229, 207));
        JLabel headline = label("<html>Good finds.<br>Right on campus.</html>", 31, true, Color.WHITE);
        JLabel blurb = label("<html>Buy and sell the things students<br>need, right where you study.</html>", 15, false, new Color(222, 241, 231));
        intro.add(eyebrow); intro.add(Box.createVerticalStrut(28)); intro.add(headline); intro.add(Box.createVerticalStrut(20)); intro.add(blurb); intro.add(Box.createVerticalGlue());
        intro.add(label("A simple campus marketplace", 12, false, new Color(190, 229, 207)));

        JPanel actions = new JPanel(); actions.setBackground(Color.WHITE); actions.setBorder(new EmptyBorder(48, 38, 34, 38)); actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        JLabel title = label("Welcome", 26, true, INK); JLabel sub = label("Sign in to explore student listings.", 14, false, MUTED);
        JButton login = button("Log in", true), signup = button("Create an account", false);
        login.addActionListener(e -> authDialog(false)); signup.addActionListener(e -> authDialog(true));
        actions.add(title); actions.add(Box.createVerticalStrut(8)); actions.add(sub); actions.add(Box.createVerticalStrut(28));
        login.setAlignmentX(Component.LEFT_ALIGNMENT); signup.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.add(login); actions.add(Box.createVerticalStrut(12)); actions.add(signup); actions.add(Box.createVerticalStrut(24));
        JPanel access = roundedPanel(new Color(244, 248, 246), 14); access.setLayout(new BoxLayout(access, BoxLayout.Y_AXIS)); access.setBorder(new EmptyBorder(15, 16, 15, 16));
        access.add(label("STARTER ACCOUNTS", 11, true, BRAND)); access.add(Box.createVerticalStrut(8));
        access.add(label("maya@campus.local  ·  campus123", 12, false, INK)); access.add(Box.createVerticalStrut(5)); access.add(label("arjun@campus.local  ·  campus123", 12, false, INK));
        access.add(Box.createVerticalStrut(10)); access.add(label("Data resets when you close the app.", 11, false, MUTED)); actions.add(access);
        card.add(intro); card.add(actions); page.add(card); return page;
    }

    private JPanel buildMarketplace() {
        JPanel page = new JPanel(new BorderLayout()); page.setBackground(CANVAS);
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(Color.WHITE); header.setBorder(new EmptyBorder(15, 30, 15, 30));
        JLabel brand = label("campus market", 21, true, BRAND); welcome = label("", 13, false, MUTED);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0)); right.setOpaque(false); right.add(welcome);
        JButton logout = button("Log out", false); logout.addActionListener(e -> { service.logOut(); cards.show(root, "auth"); }); right.add(logout);
        header.add(brand, BorderLayout.WEST); header.add(right, BorderLayout.EAST); page.add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane(); tabs.setFont(new Font("SansSerif", Font.BOLD, 13)); tabs.setBackground(CANVAS); tabs.setBorder(new EmptyBorder(14, 22, 16, 22));
        tabs.addTab("  Browse products  ", buildListings());
        tabs.addTab("  Wishlist  ", buildWishlist());
        tabs.addTab("  My items  ", buildMyListings());
        tabs.addTab("  My orders  ", buildOrders());
        tabs.addTab("  Profile  ", buildProfile());
        page.add(tabs, BorderLayout.CENTER); return page;
    }

    private JPanel buildListings() {
        JPanel page = new JPanel(new BorderLayout(0, 18)); page.setBackground(CANVAS); page.setBorder(new EmptyBorder(10, 4, 4, 4));
        JPanel intro = new JPanel(new BorderLayout(10, 12)); intro.setOpaque(false);
        JPanel titleBox = new JPanel(); titleBox.setOpaque(false); titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(label("Find your next favorite", 25, true, INK)); titleBox.add(Box.createVerticalStrut(4)); titleBox.add(label("Student listings, shared locally on your campus.", 14, false, MUTED));
        intro.add(titleBox, BorderLayout.NORTH);
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0)); filters.setOpaque(false);
        searchField = new JTextField(22); searchField.putClientProperty("JTextField.placeholderText", "Search books, electronics…"); styleInput(searchField);
        categoryFilter = new JComboBox<>(); categoryFilter.setFont(new Font("SansSerif", Font.PLAIN, 13)); categoryFilter.setBackground(Color.WHITE); categoryFilter.setPreferredSize(new Dimension(160, 38));
        for (Category c : service.categories()) categoryFilter.addItem(c.name());
        categoryFilter.insertItemAt("All categories", 0); categoryFilter.setSelectedIndex(0);
        JButton sell = button("＋  Sell an item", true); sell.addActionListener(e -> productDialog(null));
        filters.add(searchField); filters.add(categoryFilter); filters.add(sell); intro.add(filters, BorderLayout.SOUTH); page.add(intro, BorderLayout.NORTH);
        listingGrid = new JPanel(new GridLayout(0, 3, 17, 17)); listingGrid.setBackground(CANVAS); listingGrid.setBorder(new EmptyBorder(2, 1, 14, 1));
        JScrollPane scroll = new JScrollPane(listingGrid); scroll.setBorder(BorderFactory.createEmptyBorder()); scroll.getViewport().setBackground(CANVAS); scroll.getVerticalScrollBar().setUnitIncrement(18); page.add(scroll, BorderLayout.CENTER);
        searchField.getDocument().addDocumentListener(new DocumentListener() { public void insertUpdate(DocumentEvent e) { refreshProducts(); } public void removeUpdate(DocumentEvent e) { refreshProducts(); } public void changedUpdate(DocumentEvent e) { refreshProducts(); } });
        categoryFilter.addActionListener(e -> refreshProducts()); return page;
    }

    private JPanel buildMyListings() {
        JPanel page = new JPanel(new BorderLayout(0, 18)); page.setBackground(CANVAS); page.setBorder(new EmptyBorder(10, 4, 4, 4));
        JPanel heading = new JPanel(new BorderLayout()); heading.setOpaque(false);
        JPanel copy = new JPanel(); copy.setOpaque(false); copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.add(label("My items", 25, true, INK)); copy.add(Box.createVerticalStrut(4)); copy.add(label("Manage the products you have listed for other students.", 14, false, MUTED));
        JButton sell = button("＋  List a new item", true); sell.addActionListener(e -> productDialog(null));
        heading.add(copy, BorderLayout.WEST); heading.add(sell, BorderLayout.EAST); page.add(heading, BorderLayout.NORTH);
        myListingsGrid = new JPanel(new GridLayout(0, 3, 17, 17)); myListingsGrid.setBackground(CANVAS); myListingsGrid.setBorder(new EmptyBorder(2, 1, 14, 1));
        JScrollPane scroll = new JScrollPane(myListingsGrid); scroll.setBorder(BorderFactory.createEmptyBorder()); scroll.getViewport().setBackground(CANVAS); scroll.getVerticalScrollBar().setUnitIncrement(18);
        page.add(scroll, BorderLayout.CENTER); return page;
    }

    private JPanel buildWishlist() {
        JPanel page = new JPanel(new BorderLayout(0, 18)); page.setBackground(CANVAS); page.setBorder(new EmptyBorder(10, 4, 4, 4));
        JPanel heading = new JPanel(); heading.setOpaque(false); heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(label("Wishlist", 25, true, INK)); heading.add(Box.createVerticalStrut(4)); heading.add(label("Save items you want to revisit before they are sold.", 14, false, MUTED));
        page.add(heading, BorderLayout.NORTH);
        wishlistGrid = new JPanel(new GridLayout(0, 3, 17, 17)); wishlistGrid.setBackground(CANVAS); wishlistGrid.setBorder(new EmptyBorder(2, 1, 14, 1));
        JScrollPane scroll = new JScrollPane(wishlistGrid); scroll.setBorder(BorderFactory.createEmptyBorder()); scroll.getViewport().setBackground(CANVAS); scroll.getVerticalScrollBar().setUnitIncrement(18);
        page.add(scroll, BorderLayout.CENTER); return page;
    }

    private JPanel buildProfile() {
        JPanel page = new JPanel(new GridBagLayout()); page.setBackground(CANVAS);
        JPanel card = roundedPanel(Color.WHITE, 18); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setPreferredSize(new Dimension(520, 370)); card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), new EmptyBorder(30, 32, 30, 32)));
        JLabel icon = label("●", 42, true, BRAND); icon.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileName = label("", 24, true, INK); profileEmail = label("", 14, false, MUTED); profileStudentDetails = label("", 13, false, MUTED);
        card.add(icon); card.add(Box.createVerticalStrut(10)); card.add(profileName); card.add(Box.createVerticalStrut(5)); card.add(profileEmail); card.add(Box.createVerticalStrut(8)); card.add(profileStudentDetails); card.add(Box.createVerticalStrut(24));
        card.add(label("Local account", 12, true, BRAND)); card.add(Box.createVerticalStrut(7)); card.add(label("Your items, orders and photos are stored only while this app is open.", 13, false, MUTED));
        page.add(card); return page;
    }

    private JPanel buildOrders() {
        JPanel panel = new JPanel(new BorderLayout(0, 16)); panel.setBackground(CANVAS); panel.setBorder(new EmptyBorder(18, 8, 8, 8));
        JPanel heading = new JPanel(); heading.setOpaque(false); heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS)); heading.add(label("Your orders", 24, true, INK)); heading.add(Box.createVerticalStrut(4)); heading.add(label("Track purchases and confirm when the exchange is complete.", 14, false, MUTED)); panel.add(heading, BorderLayout.NORTH);
        ordersTable = new JTable(ordersModel); ordersTable.setRowHeight(38); ordersTable.setFont(new Font("SansSerif", Font.PLAIN, 13)); ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); ordersTable.setShowVerticalLines(false); ordersTable.setGridColor(LINE); ordersTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12)); ordersTable.getTableHeader().setBackground(new Color(242, 246, 244)); ordersTable.setAutoCreateRowSorter(true);
        ordersTable.getSelectionModel().addListSelectionListener(event -> { if (!event.getValueIsAdjusting()) refreshSelectedOrder(); });
        JPanel body = new JPanel(new BorderLayout(16, 0)); body.setOpaque(false); body.add(new JScrollPane(ordersTable), BorderLayout.CENTER); body.add(buildOrderPreview(), BorderLayout.EAST); panel.add(body, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0)); actions.setOpaque(false);
        JButton complete = button("Mark completed", true), cancel = button("Cancel order", false);
        complete.addActionListener(e -> withOrder(o -> { service.completeOrder(o.getId()); refreshAll(); })); cancel.addActionListener(e -> withOrder(o -> { service.cancelOrder(o.getId()); refreshAll(); }));
        actions.add(complete); actions.add(cancel); panel.add(actions, BorderLayout.SOUTH); return panel;
    }

    private JPanel buildOrderPreview() {
        JPanel preview = roundedPanel(Color.WHITE, 16); preview.setLayout(new BoxLayout(preview, BoxLayout.Y_AXIS)); preview.setPreferredSize(new Dimension(300, 0)); preview.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), new EmptyBorder(15, 15, 15, 15)));
        selectedOrderImage = photoLabel(null, 268, 150); selectedOrderTitle = label("Select an order", 17, true, INK); selectedOrderMeta = label("Choose an order to see its item.", 12, false, MUTED);
        selectedOrderDescription = new JTextArea(); selectedOrderDescription.setEditable(false); selectedOrderDescription.setLineWrap(true); selectedOrderDescription.setWrapStyleWord(true); selectedOrderDescription.setFont(new Font("SansSerif", Font.PLAIN, 12)); selectedOrderDescription.setForeground(new Color(74, 85, 102)); selectedOrderDescription.setBackground(Color.WHITE); selectedOrderDescription.setBorder(new EmptyBorder(0, 0, 0, 0));
        preview.add(selectedOrderImage); preview.add(Box.createVerticalStrut(14)); preview.add(selectedOrderTitle); preview.add(Box.createVerticalStrut(6)); preview.add(selectedOrderMeta); preview.add(Box.createVerticalStrut(10)); preview.add(selectedOrderDescription); preview.add(Box.createVerticalGlue());
        return preview;
    }

    private void authDialog(boolean signup) {
        JTextField name = new JTextField(22), email = new JTextField(22), studentId = new JTextField(22), department = new JTextField(22);
        JPasswordField password = new JPasswordField(22); JComboBox<Integer> year = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6});
        styleInput(name); styleInput(email); styleInput(studentId); styleInput(department); styleInput(password); year.setPreferredSize(new Dimension(220, 38));
        JPanel form = new JPanel(new GridLayout(signup ? 6 : 2, 2, 10, 12)); form.setBorder(new EmptyBorder(12, 8, 8, 8));
        if (signup) { JLabel lName = new JLabel("Name"); lName.setLabelFor(name); form.add(lName); form.add(name); }
        JLabel lEmail = new JLabel("Email"); lEmail.setLabelFor(email); form.add(lEmail); form.add(email);
        JLabel lPassword = new JLabel("Password"); lPassword.setLabelFor(password); form.add(lPassword); form.add(password);
        if (signup) {
            JLabel lStudentId = new JLabel("Student ID"); lStudentId.setLabelFor(studentId); form.add(lStudentId); form.add(studentId);
            JLabel lDept = new JLabel("Department"); lDept.setLabelFor(department); form.add(lDept); form.add(department);
            JLabel lYear = new JLabel("Year"); lYear.setLabelFor(year); form.add(lYear); form.add(year);
        }
        int result = JOptionPane.showConfirmDialog(this, form, signup ? "Create your account" : "Welcome back", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        try { if (signup) service.signUp(name.getText(), email.getText(), new String(password.getPassword()), studentId.getText(), department.getText(), (Integer) year.getSelectedItem()); else service.logIn(email.getText(), new String(password.getPassword())); refreshAll(); cards.show(root, "market"); }
        catch (RuntimeException ex) { error(ex); }
    }

    private void refreshAll() {
        welcome.setText("Hi, " + service.currentUser().getName());
        updateProfile(); refreshProducts(); refreshWishlist(); refreshMyListings(); refreshOrders();
    }
    private void refreshProducts() {
        if (listingGrid == null) return; listingGrid.removeAll(); String query = searchField.getText().trim().toLowerCase(); String selected = (String) categoryFilter.getSelectedItem(); int count = 0;
        for (Product p : service.products()) {
            if (!query.isEmpty() && !(p.getName() + " " + p.getDescription() + " " + p.getCategory().name()).toLowerCase().contains(query)) continue;
            if (selected != null && !selected.equals("All categories") && !p.getCategory().name().equals(selected)) continue;
            listingGrid.add(productCard(p)); count++;
        }
        if (count == 0) { JPanel empty = new JPanel(new GridBagLayout()); empty.setOpaque(false); JLabel msg = label("No listings found. Try another search or add the first item.", 15, false, MUTED); empty.add(msg); listingGrid.add(empty); }
        listingGrid.revalidate(); listingGrid.repaint();
    }
    private void refreshMyListings() {
        if (myListingsGrid == null) return; myListingsGrid.removeAll(); int count = 0;
        for (Product product : service.myListings()) { myListingsGrid.add(productCard(product)); count++; }
        if (count == 0) { JPanel empty = new JPanel(new GridBagLayout()); empty.setOpaque(false); empty.add(label("You have not listed anything yet. Add your first item above.", 15, false, MUTED)); myListingsGrid.add(empty); }
        myListingsGrid.revalidate(); myListingsGrid.repaint();
    }
    private void refreshWishlist() {
        if (wishlistGrid == null) return; wishlistGrid.removeAll(); int count = 0;
        for (Product product : service.wishlistProducts()) { wishlistGrid.add(productCard(product)); count++; }
        if (count == 0) { JPanel empty = new JPanel(new GridBagLayout()); empty.setOpaque(false); empty.add(label("Your wishlist is empty. Save items from Browse products.", 15, false, MUTED)); wishlistGrid.add(empty); }
        wishlistGrid.revalidate(); wishlistGrid.repaint();
    }
    private void updateProfile() {
        Student currentStudent = service.currentUser();
        profileName.setText(currentStudent.getName());
        profileEmail.setText(currentStudent.getEmail());
        profileStudentDetails.setText(currentStudent.getStudentId() + "  ·  " + currentStudent.getDepartment() + "  ·  Year " + currentStudent.getYear());
    }
    private JPanel productCard(Product p) {
        JPanel card = roundedPanel(Color.WHITE, 16); card.setLayout(new BorderLayout(0, 0)); card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), new EmptyBorder(0, 0, 12, 0)));
        JLabel photo = photoLabel(p.getImageBytes(), 300, 180); card.add(photo, BorderLayout.NORTH);
        JPanel details = new JPanel(); details.setBackground(Color.WHITE); details.setBorder(new EmptyBorder(13, 15, 3, 15)); details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        JPanel titleRow = new JPanel(new BorderLayout(8, 0)); titleRow.setOpaque(false); JLabel name = label(p.getName(), 16, true, INK); JLabel price = label("₹" + String.format("%.0f", p.getPrice()), 17, true, BRAND); titleRow.add(name, BorderLayout.CENTER); titleRow.add(price, BorderLayout.EAST); details.add(titleRow);
        details.add(Box.createVerticalStrut(7)); details.add(label(p.getCategory().name() + "  ·  " + p.getSeller().getName(), 12, false, MUTED)); details.add(Box.createVerticalStrut(9));
        JLabel description = label(clip(p.getDescription(), 88), 12, false, new Color(74, 85, 102)); details.add(description); details.add(Box.createVerticalStrut(13));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0)); actions.setOpaque(false);
        if (p.getSeller().equals(service.currentUser())) {
            if (p.getStatus().name().equals("AVAILABLE")) {
                JButton edit = smallButton("Edit", false), remove = smallButton("Remove", false);
                edit.setToolTipText("Edit this listing"); remove.setToolTipText("Permanently remove this listing");
                edit.addActionListener(e -> productDialog(p)); remove.addActionListener(e -> confirmRemove(p)); actions.add(edit); actions.add(remove);
            } else {
                JLabel sold = label("Sold", 12, true, new Color(172, 76, 45)); actions.add(sold);
            }
        } else if (p.getStatus().name().equals("AVAILABLE")) {
            boolean isSaved = service.isWishlisted(p.getId());
            JButton save = smallButton(isSaved ? "♥ Saved" : "♡ Save", false);
            save.setToolTipText(isSaved ? "Remove from your wishlist" : "Add to your wishlist");
            save.addActionListener(e -> toggleWishlist(p));
            JButton buy = smallButton("Buy · COD", true);
            buy.setToolTipText("Place a cash-on-delivery order for this item");
            buy.addActionListener(e -> checkoutDialog(p)); actions.add(save); actions.add(buy);
        } else {
            actions.add(label("No longer available", 12, true, new Color(172, 76, 45)));
        }
        JButton view = smallButton("Details", false); view.setToolTipText("View full details of this item"); view.addActionListener(e -> showProduct(p)); actions.add(view); details.add(actions); card.add(details, BorderLayout.CENTER); return card;
    }

    private void productDialog(Product existing) {
        boolean editing = existing != null; JTextField name = new JTextField(editing ? existing.getName() : "", 24), price = new JTextField(editing ? Double.toString(existing.getPrice()) : "", 24), description = new JTextField(editing ? existing.getDescription() : "", 24);
        styleInput(name); styleInput(price); styleInput(description);
        JComboBox<Category> category = new JComboBox<>(service.categories().toArray(Category[]::new)); category.setPreferredSize(new Dimension(240, 36)); if (editing) category.setSelectedItem(existing.getCategory());
        final byte[][] image = {editing ? existing.getImageBytes() : null};
        JLabel preview = photoLabel(image[0], 280, 150); JButton upload = button("Choose photo", false), clearPhoto = button("Remove photo", false);
        upload.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(); chooser.setDialogTitle("Choose a product photo"); chooser.setFileFilter(new FileNameExtensionFilter("Images (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
            if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
            try { File file = chooser.getSelectedFile(); if (file.length() > 5L * 1024 * 1024) throw new IllegalArgumentException("Choose an image smaller than 5 MB."); byte[] bytes = Files.readAllBytes(file.toPath()); BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(bytes)); if (decoded == null) throw new IllegalArgumentException("That file is not a supported image."); image[0] = bytes; preview.setIcon(scaledIcon(decoded, 280, 150)); preview.setText(""); }
            catch (Exception ex) { error(ex instanceof RuntimeException re ? re : new IllegalArgumentException("Could not read that image.")); }
        });
        clearPhoto.addActionListener(e -> { image[0] = null; preview.setIcon(null); preview.setText("Add a product photo"); });
        JPanel photoActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); photoActions.add(upload); photoActions.add(clearPhoto);
        JPanel form = new JPanel(new GridBagLayout()); form.setBorder(new EmptyBorder(10, 12, 8, 12)); GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(5, 6, 5, 6); c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.HORIZONTAL;
        addField(form, c, 0, "Product name", name); addField(form, c, 1, "Price (₹)", price); addField(form, c, 2, "Category", category); addField(form, c, 3, "Description", description);
        c.gridx=0; c.gridy=4; c.gridwidth=2; form.add(preview, c); c.gridy=5; form.add(photoActions, c);
        if (JOptionPane.showConfirmDialog(this, form, editing ? "Edit your listing" : "List an item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try { double amount = Double.parseDouble(price.getText().trim()); if (editing) service.editProduct(existing.getId(), name.getText(), amount, description.getText(), (Category) category.getSelectedItem(), image[0]); else service.addProduct(name.getText(), amount, description.getText(), (Category) category.getSelectedItem(), image[0]); refreshAll(); }
        catch (RuntimeException ex) { error(ex); }
    }
    private void addField(JPanel panel, GridBagConstraints c, int row, String title, JComponent input) { c.gridx=0; c.gridy=row; c.gridwidth=1; c.weightx=0; JLabel l = label(title, 12, true, INK); l.setLabelFor(input); panel.add(l, c); c.gridx=1; c.weightx=1; panel.add(input, c); }
    private void showProduct(Product p) {
        JDialog dialog = new JDialog(this, p.getName(), true); dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new GridLayout(1, 2, 25, 0)); content.setBackground(Color.WHITE); content.setBorder(new EmptyBorder(26, 26, 24, 26));
        JLabel image = photoLabel(p.getImageBytes(), 390, 405); content.add(image);
        JPanel details = new JPanel(); details.setBackground(Color.WHITE); details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.add(label(p.getCategory().name().toUpperCase(), 11, true, BRAND)); details.add(Box.createVerticalStrut(10)); details.add(label(p.getName(), 26, true, INK)); details.add(Box.createVerticalStrut(8));
        details.add(label("₹" + String.format("%.0f", p.getPrice()), 23, true, BRAND)); details.add(Box.createVerticalStrut(8)); details.add(label("Listed by " + p.getSeller().getName(), 13, false, MUTED)); details.add(Box.createVerticalStrut(20));
        JTextArea description = new JTextArea(p.getDescription().isBlank() ? "No description provided." : p.getDescription()); description.setEditable(false); description.setLineWrap(true); description.setWrapStyleWord(true); description.setFont(new Font("SansSerif", Font.PLAIN, 14)); description.setForeground(INK); description.setBackground(Color.WHITE); description.setBorder(new EmptyBorder(0, 0, 0, 0)); details.add(description); details.add(Box.createVerticalGlue());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); actions.setOpaque(false);
        JButton close = button("Close", false); close.addActionListener(e -> dialog.dispose());
        if (!p.getSeller().equals(service.currentUser()) && p.getStatus().name().equals("AVAILABLE")) {
            JButton save = button(service.isWishlisted(p.getId()) ? "♥ Saved" : "♡ Save", false); save.addActionListener(e -> { toggleWishlist(p); dialog.dispose(); });
            JButton buy = button("Continue to COD", true); buy.addActionListener(e -> { dialog.dispose(); checkoutDialog(p); }); actions.add(save); actions.add(buy);
        }
        actions.add(close); details.add(actions); content.add(details);
        dialog.setContentPane(content); dialog.pack(); dialog.setSize(850, 500); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }
    private void checkoutDialog(Product p) {
        JDialog dialog = new JDialog(this, "Confirm cash on delivery", true); dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(0, 18)); content.setBackground(Color.WHITE); content.setBorder(new EmptyBorder(25, 28, 24, 28));
        JPanel heading = new JPanel(); heading.setOpaque(false); heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS)); heading.add(label("Confirm your order", 24, true, INK)); heading.add(Box.createVerticalStrut(5)); heading.add(label("No online payment is collected in this local marketplace.", 13, false, MUTED)); content.add(heading, BorderLayout.NORTH);
        JPanel summary = roundedPanel(new Color(247, 250, 248), 16); summary.setLayout(new BorderLayout(15, 0)); summary.setBorder(new EmptyBorder(14, 14, 14, 14)); summary.add(photoLabel(p.getImageBytes(), 170, 130), BorderLayout.WEST);
        JPanel copy = new JPanel(); copy.setOpaque(false); copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS)); copy.add(label(p.getName(), 18, true, INK)); copy.add(Box.createVerticalStrut(8)); copy.add(label(p.getCategory().name() + "  ·  Listed by " + p.getSeller().getName(), 12, false, MUTED)); copy.add(Box.createVerticalStrut(14)); copy.add(label("₹" + String.format("%.0f", p.getPrice()), 22, true, BRAND)); copy.add(Box.createVerticalStrut(8)); copy.add(label("Payment: cash on delivery", 12, true, BRAND)); summary.add(copy, BorderLayout.CENTER); content.add(summary, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 0)); actions.setOpaque(false); JButton cancel = button("Cancel", false), confirm = button("Place order", true); cancel.addActionListener(e -> dialog.dispose()); confirm.addActionListener(e -> { try { Order order = service.placeOrder(p.getId()); dialog.dispose(); refreshAll(); message("Your cash-on-delivery order has been placed.\nOrder: " + order.getId()); } catch (RuntimeException ex) { error(ex); } }); actions.add(cancel); actions.add(confirm); content.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(content); dialog.pack(); dialog.setSize(650, 390); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }
    private void confirmRemove(Product p) { if (JOptionPane.showConfirmDialog(this, "Remove “" + p.getName() + "” from your listings?", "Remove listing", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) try { service.removeProduct(p.getId()); refreshAll(); } catch (RuntimeException ex) { error(ex); } }
    private void toggleWishlist(Product product) { try { if (service.isWishlisted(product.getId())) service.removeFromWishlist(product.getId()); else service.addToWishlist(product.getId()); refreshAll(); } catch (RuntimeException ex) { error(ex); } }
    private void refreshOrders() {
        if (ordersTable == null) return;
        ordersModel.setRowCount(0);
        for (Order order : service.myOrders()) {
            boolean buyer = order.getBuyer().equals(service.currentUser());
            Student other = buyer ? order.getSeller() : order.getBuyer();
            ordersModel.addRow(new Object[]{order.getProduct().getName(), other.getName(), buyer ? "Buyer" : "Seller", String.format("%.2f", order.getTotal()), order.getStatus(), order.getId()});
        }
        if (ordersModel.getRowCount() > 0) ordersTable.setRowSelectionInterval(0, 0);
        else clearOrderPreview();
    }
    private void refreshSelectedOrder() {
        if (ordersTable.getSelectedRow() < 0) { clearOrderPreview(); return; }
        try {
            Order order = selectedOrder();
            Product product = order.getProduct();
            setPhotoLabel(selectedOrderImage, product.getImageBytes(), 268, 150, "No product photo");
            selectedOrderTitle.setText(product.getName());
            selectedOrderMeta.setText("₹" + String.format("%.0f", order.getTotal()) + "  ·  " + order.getStatus());
            selectedOrderDescription.setText(product.getDescription().isBlank() ? "No description provided." : product.getDescription());
        } catch (RuntimeException ignored) { clearOrderPreview(); }
    }
    private void clearOrderPreview() {
        if (selectedOrderImage == null) return;
        setPhotoLabel(selectedOrderImage, null, 268, 150, "Select an order");
        selectedOrderTitle.setText("Select an order");
        selectedOrderMeta.setText("Choose an order to see its item.");
        selectedOrderDescription.setText("");
    }
    private Order selectedOrder() { int row=ordersTable.getSelectedRow(); if(row<0) throw new IllegalArgumentException("Select an order first."); row=ordersTable.convertRowIndexToModel(row); UUID id=(UUID)ordersModel.getValueAt(row,5); return service.myOrders().stream().filter(o->o.getId().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Order not found.")); }
    private void withOrder(java.util.function.Consumer<Order> action) { try { action.accept(selectedOrder()); } catch(RuntimeException ex) { error(ex); } }
    private void error(RuntimeException ex) { message(ex.getMessage()==null ? "The action could not be completed." : ex.getMessage()); }
    private void message(String text) { JOptionPane.showMessageDialog(this, text, "Campus Market", JOptionPane.INFORMATION_MESSAGE); }
    private JLabel photoLabel(byte[] bytes, int width, int height) { JLabel label = new JLabel("Add a product photo", SwingConstants.CENTER); label.setOpaque(true); label.setBackground(new Color(239, 244, 241)); label.setForeground(new Color(139, 153, 146)); label.setPreferredSize(new Dimension(width, height)); label.setFont(new Font("SansSerif", Font.PLAIN, 13)); setPhotoLabel(label, bytes, width, height, "Add a product photo"); return label; }
    private void setPhotoLabel(JLabel label, byte[] bytes, int width, int height, String emptyText) { label.setIcon(null); label.setText(emptyText); if (bytes != null) try { BufferedImage image=ImageIO.read(new ByteArrayInputStream(bytes)); if(image!=null){label.setIcon(scaledIcon(image,width,height));label.setText("");} } catch(Exception ignored) {} }
    private ImageIcon scaledIcon(BufferedImage image, int width, int height) { double scale=Math.min((double)width/image.getWidth(),(double)height/image.getHeight()); int w=(int)(image.getWidth()*scale), h=(int)(image.getHeight()*scale); Image scaled=image.getScaledInstance(w,h,Image.SCALE_SMOOTH); return new ImageIcon(scaled); }
    private JPanel roundedPanel(Color color, int radius) { return new JPanel() { { setOpaque(false); } @Override protected void paintComponent(Graphics g) { Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(color); g2.fillRoundRect(0,0,getWidth(),getHeight(),radius,radius); g2.dispose(); super.paintComponent(g); } }; }
    private JLabel label(String text, int size, boolean bold, Color color) { JLabel l=new JLabel(text); l.setFont(new Font("SansSerif",bold?Font.BOLD:Font.PLAIN,size)); l.setForeground(color); return l; }
    private JButton button(String text, boolean primary) { JButton b=new JButton(text); b.setFont(new Font("SansSerif",Font.BOLD,13)); b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setPreferredSize(new Dimension(primary?170:150,38)); if(primary){b.setBackground(BRAND);b.setForeground(Color.WHITE);} else {b.setBackground(Color.WHITE);b.setForeground(INK);} return b; }
    private JButton smallButton(String text, boolean primary) { JButton b=button(text,primary); int width=text.startsWith("♡") || text.startsWith("♥") ? 92 : 80; b.setPreferredSize(new Dimension(width,32)); return b; }
    private void styleInput(JComponent c) { c.setFont(new Font("SansSerif",Font.PLAIN,14)); c.setPreferredSize(new Dimension(220,38)); }
    private String clip(String s, int max) { if(s==null || s.isBlank()) return "No description provided."; return s.length()>max?s.substring(0,max-1)+"…":s; }
}
