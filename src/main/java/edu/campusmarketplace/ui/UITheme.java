package edu.campusmarketplace.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern design system tokens and reusable UI components inspired by modern web apps
 * (Airbnb, Vinted, Notion) for pure Java 17+ Swing.
 */
public final class UITheme {
    private UITheme() {}

    // --- Color Palette ---
    public static final Color BG_CANVAS = new Color(0xF8, 0xF9, 0xFA); // #F8F9FA
    public static final Color SURFACE = Color.WHITE;                     // #FFFFFF
    public static final Color BORDER = new Color(0xE5, 0xE7, 0xEB);      // #E5E7EB
    public static final Color BORDER_FOCUS = new Color(0x0F, 0x51, 0x32);// #0F5132

    // Primary: Deep Forest / Emerald Green
    public static final Color PRIMARY = new Color(0x0F, 0x51, 0x32);       // #0F5132
    public static final Color PRIMARY_HOVER = new Color(0x15, 0x73, 0x47); // #157347
    public static final Color PRIMARY_PRESSED = new Color(0x0A, 0x3A, 0x24);

    // Neutral Secondary: Light gray background with dark slate text
    public static final Color SECONDARY_BG = new Color(0xF3, 0xF4, 0xF6);      // #F3F4F6
    public static final Color SECONDARY_HOVER = new Color(0xE5, 0xE7, 0xEB);   // #E5E7EB
    public static final Color SECONDARY_PRESSED = new Color(0xD1, 0xD5, 0xDB); // #D1D5DB
    public static final Color SECONDARY_TEXT = new Color(0x37, 0x41, 0x51);    // #374151

    // Typography Colors
    public static final Color TEXT_MAIN = new Color(0x11, 0x18, 0x27);  // #111827
    public static final Color TEXT_MUTED = new Color(0x6B, 0x72, 0x80); // #6B7280

    // Badges & Accents
    public static final Color PRICE_COLOR = new Color(0x0F, 0x51, 0x32);      // #0F5132
    public static final Color TAG_BG = new Color(0xEC, 0xFD, 0xF5);           // #ECFDF5 Mint tint
    public static final Color TAG_TEXT = new Color(0x06, 0x5F, 0x46);         // #065F46 Dark emerald
    public static final Color SOLD_BG = new Color(0xFE, 0xF2, 0xF2);          // #FEF2F2
    public static final Color SOLD_TEXT = new Color(0x99, 0x1B, 0x1B);        // #991B1B
    public static final Color DANGER_HOVER = new Color(0xFE, 0xE2, 0xE2);

    // --- Fonts ---
    private static final String FONT_FAMILY = resolveFontFamily();

    private static String resolveFontFamily() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = ge.getAvailableFontFamilyNames();
        for (String preferred : new String[]{"Segoe UI", "Inter", "Helvetica Neue", "Arial"}) {
            for (String f : fonts) {
                if (f.equalsIgnoreCase(preferred)) return f;
            }
        }
        return Font.SANS_SERIF;
    }

    public static Font font(float size, int style) {
        return new Font(FONT_FAMILY, style, (int) size);
    }

    public static Font regular(float size) {
        return font(size, Font.PLAIN);
    }

    public static Font medium(float size) {
        return font(size, Font.PLAIN);
    }

    public static Font bold(float size) {
        return font(size, Font.BOLD);
    }

    public static Font symbolFont(float size, int style) {
        return new Font("Segoe UI Symbol", style, (int) size);
    }

    public static JLabel label(String text, float size, boolean bold, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(size, bold ? Font.BOLD : Font.PLAIN));
        l.setForeground(color);
        return l;
    }

    public static JLabel symbolLabel(String text, float size, boolean bold, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(symbolFont(size, bold ? Font.BOLD : Font.PLAIN));
        l.setForeground(color);
        return l;
    }

    // --- Modern Rounded Card Panel ---
    public static class CardPanel extends JPanel {
        private final int cornerRadius;
        private Color backgroundColor;
        private Color borderColor;

        public CardPanel() {
            this(12, SURFACE, BORDER);
        }

        public CardPanel(int cornerRadius) {
            this(cornerRadius, SURFACE, BORDER);
        }

        public CardPanel(int cornerRadius, Color backgroundColor, Color borderColor) {
            super();
            this.cornerRadius = cornerRadius;
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        public void setBackgroundPaint(Color color) {
            this.backgroundColor = color;
            repaint();
        }

        public void setBorderPaint(Color color) {
            this.borderColor = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();

            if (backgroundColor != null) {
                g2.setColor(backgroundColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius));
            }

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 1f, height - 1f, cornerRadius, cornerRadius));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // --- Modern Button ---
    public enum ButtonStyle {
        PRIMARY, SECONDARY, OUTLINE, DANGER
    }

    public static class ModernButton extends JButton {
        private final ButtonStyle style;
        private final int cornerRadius;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public ModernButton(String text, ButtonStyle style) {
            this(text, style, 8);
        }

        public ModernButton(String text, ButtonStyle style, int cornerRadius) {
            super(text);
            this.style = style;
            this.cornerRadius = cornerRadius;
            setFont(UITheme.bold(12.5f));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(6, 8, 6, 8));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) { isHovered = true; repaint(); }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false; isPressed = false; repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) { isPressed = true; repaint(); }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false; repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();

            Color bg;
            Color text;
            Color border = null;

            switch (style) {
                case PRIMARY -> {
                    bg = isPressed ? PRIMARY_PRESSED : (isHovered ? PRIMARY_HOVER : PRIMARY);
                    text = Color.WHITE;
                }
                case SECONDARY -> {
                    bg = isPressed ? SECONDARY_PRESSED : (isHovered ? SECONDARY_HOVER : SECONDARY_BG);
                    text = SECONDARY_TEXT;
                }
                case OUTLINE -> {
                    bg = isPressed ? SECONDARY_HOVER : (isHovered ? SECONDARY_BG : SURFACE);
                    text = SECONDARY_TEXT;
                    border = isHovered ? TEXT_MUTED : BORDER;
                }
                case DANGER -> {
                    bg = isPressed ? SOLD_BG : (isHovered ? DANGER_HOVER : SURFACE);
                    text = SOLD_TEXT;
                    border = isHovered ? SOLD_TEXT : BORDER;
                }
                default -> {
                    bg = SECONDARY_BG;
                    text = SECONDARY_TEXT;
                }
            }

            if (!isEnabled()) {
                bg = new Color(0xF3, 0xF4, 0xF6);
                text = new Color(0x9C, 0xA3, 0xAF);
                border = null;
            }

            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius));

            if (border != null) {
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 1f, height - 1f, cornerRadius, cornerRadius));
            }

            g2.dispose();

            setForeground(text);
            super.paintComponent(g);
        }
    }

    // --- Search & Input Fields with Placeholder ---
    public static class SearchTextField extends JTextField {
        private String placeholder;
        private boolean hasFocus = false;

        public SearchTextField(String placeholder, int columns) {
            super(columns);
            this.placeholder = placeholder;
            setFont(UITheme.regular(13.5f));
            setForeground(TEXT_MAIN);
            setCaretColor(TEXT_MAIN);
            setOpaque(false);
            setBorder(new EmptyBorder(8, 36, 8, 12));

            addFocusListener(new java.awt.event.FocusListener() {
                @Override public void focusGained(java.awt.event.FocusEvent e) { hasFocus = true; repaint(); }
                @Override public void focusLost(java.awt.event.FocusEvent e) { hasFocus = false; repaint(); }
            });
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();

            // Background
            g2.setColor(SURFACE);
            g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 8, 8));

            // Border
            g2.setColor(hasFocus ? BORDER_FOCUS : BORDER);
            g2.setStroke(new BasicStroke(hasFocus ? 1.5f : 1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 1f, height - 1f, 8, 8));

            // Search Icon (Glass)
            int iconX = 12;
            int iconY = height / 2;
            g2.setColor(hasFocus ? PRIMARY : TEXT_MUTED);
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawOval(iconX, iconY - 6, 9, 9);
            g2.drawLine(iconX + 7, iconY + 2, iconX + 12, iconY + 7);

            g2.dispose();
            super.paintComponent(g);

            // Placeholder
            if (getText().isEmpty() && !hasFocus && placeholder != null) {
                Graphics2D gp = (Graphics2D) g.create();
                gp.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                gp.setFont(getFont());
                gp.setColor(TEXT_MUTED);
                FontMetrics fm = gp.getFontMetrics();
                int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
                gp.drawString(placeholder, 36, textY);
                gp.dispose();
            }
        }
    }

    // --- Category Badge Component ---
    public static class BadgeLabel extends JLabel {
        private final Color bg;
        private final Color fg;

        public BadgeLabel(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg;
            this.fg = fg;
            setFont(UITheme.bold(11f));
            setForeground(fg);
            setOpaque(false);
            setBorder(new EmptyBorder(3, 8, 3, 8));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static Border roundedBorder(int radius, Color color, int thickness) {
        return new Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(thickness));
                g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, width - 1f, height - 1f, radius, radius));
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(thickness + 2, thickness + 4, thickness + 2, thickness + 4);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        };
    }
}

