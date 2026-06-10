import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {

    // ===================== DATABASE MANAGER =====================
    static class DatabaseManager {
        private static final String DB_DIR;
        private static final String DB_URL;
        static {
            // Lấy thư mục chứa file .class để đặt DB cạnh đó
            String loc = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            try {
                loc = java.net.URLDecoder.decode(loc, "UTF-8");
            } catch (Exception ignored) {
            }
            DB_DIR = new File(loc).getParent();
            DB_URL = "jdbc:sqlite:" + DB_DIR + "/products.db";
        }
        private Connection conn;

        public DatabaseManager() {
            try {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection(DB_URL);
                initTable();
                System.out.println("[DB] Kết nối CSDL thành công: " + DB_URL);
            } catch (Exception e) {
                System.err.println("[DB] Lỗi khởi tạo CSDL: " + e.getMessage());
            }
        }

        private void initTable() throws SQLException {
            String sql = "CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT NOT NULL," +
                    "price TEXT NOT NULL," +
                    "brand TEXT DEFAULT 'Adidas'," +
                    "short_desc TEXT," +
                    "description TEXT," +
                    "image_file TEXT)";
            conn.createStatement().execute(sql);

            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM products");
            if (rs.getInt(1) == 0) {
                insertSample();
            }
        }

        private void insertSample() throws SQLException {
            String sql = "INSERT INTO products(title, price, brand, short_desc, description, image_file) VALUES(?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            Object[][] data = {
                    { "4DFWD PULSE SHOES", "$160.00", "Adidas", "This product is excluded fr...",
                            "This product is excluded from all\npromotional discounts and offers.", "img1.png" },
                    { "FORUM MID SHOES", "$100.00", "Adidas", "This product is excluded fr...",
                            "This product is excluded from all\npromotional discounts and offers.", "img2.png" },
                    { "SUPERNOVA SHOES", "$150.00", "Adidas", "NMD City Stock 2",
                            "This product is excluded from all\npromotional discounts and offers.", "img3.png" },
                    { "NMD R1 SHOES", "$160.00", "Adidas", "NMD City Stock 2",
                            "This product is excluded from all\npromotional discounts and offers.", "img4.png" },
                    { "ULTRABOOST 22", "$120.00", "Adidas", "NMD City Stock 2",
                            "This product is excluded from all\npromotional discounts and offers.", "img5.png" },
                    { "STAN SMITH SHOES", "$160.00", "Adidas", "This product is excluded fr...",
                            "This product is excluded from all\npromotional discounts and offers.", "img6.png" },
            };
            for (Object[] row : data) {
                for (int i = 0; i < row.length; i++)
                    ps.setObject(i + 1, row[i]);
                ps.executeUpdate();
            }
            System.out.println("[DB] Đã chèn dữ liệu mẫu.");
        }

        public List<Product> getAllProducts() {
            return queryProducts("SELECT * FROM products");
        }

        public List<Product> searchProducts(String keyword) {
            String sql = "SELECT * FROM products WHERE LOWER(title) LIKE LOWER(?)";
            try {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, "%" + keyword + "%");
                return mapResultSet(ps.executeQuery());
            } catch (Exception e) {
                System.err.println("[DB] Lỗi tìm kiếm: " + e.getMessage());
                return new ArrayList<>();
            }
        }

        public boolean insertProduct(String title, String price, String brand, String shortDesc, String desc,
                String imageFile) {
            String sql = "INSERT INTO products(title, price, brand, short_desc, description, image_file) VALUES(?,?,?,?,?,?)";
            try {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, title);
                ps.setString(2, price);
                ps.setString(3, brand);
                ps.setString(4, shortDesc);
                ps.setString(5, desc);
                ps.setString(6, imageFile);
                ps.executeUpdate();
                return true;
            } catch (Exception e) {
                System.err.println("[DB] Lỗi thêm sản phẩm: " + e.getMessage());
                return false;
            }
        }

        /** Xóa sản phẩm theo id */
        public boolean deleteById(int id) {
            try {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE id = ?");
                ps.setInt(1, id);
                ps.executeUpdate();
                System.out.println("[DB] Đã xóa sản phẩm id=" + id);
                return true;
            } catch (Exception e) {
                System.err.println("[DB] Lỗi xóa sản phẩm: " + e.getMessage());
                return false;
            }
        }

        private List<Product> queryProducts(String sql) {
            try {
                return mapResultSet(conn.createStatement().executeQuery(sql));
            } catch (Exception e) {
                System.err.println("[DB] Lỗi truy vấn: " + e.getMessage());
                return new ArrayList<>();
            }
        }

        private List<Product> mapResultSet(ResultSet rs) throws SQLException {
            List<Product> list = new ArrayList<>();
            while (rs.next()) {
                Product p = new Product(
                        rs.getString("title"),
                        rs.getString("price"),
                        rs.getString("short_desc"));
                p.id    = rs.getInt("id");
                p.brand = rs.getString("brand");
                p.desc  = rs.getString("description");
                String imgFile = rs.getString("image_file");
                if (imgFile != null && !imgFile.isEmpty()) {
                    try {
                        File f = new File(DB_DIR, imgFile);
                        if (!f.exists())
                            f = new File(imgFile);
                        if (f.exists())
                            p.image = ImageIO.read(f);
                    } catch (Exception ignored) {
                    }
                }
                list.add(p);
            }
            return list;
        }

        public void close() {
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception ignored) {
            }
        }
    }

    // ===================== MODEL =====================
    static class Product {
        int id = -1;
        String title, price, brand = "Adidas";
        String desc = "This product is excluded from all\npromotional discounts and offers.";
        String shortDesc = "This product is excluded fr...";
        BufferedImage image;

        public Product(String title, String price, String shortDesc) {
            this.title = title;
            this.price = price;
            if (shortDesc != null)
                this.shortDesc = shortDesc;
        }
    }

    // ===================== CUSTOM ICONS & BUTTONS =====================
    static class VectorIcon implements Icon {
        int width, height, type;
        Color color;

        public VectorIcon(int type, int size, Color color) {
            this.width = size;
            this.height = size;
            this.type = type;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.translate(x, y);
            Stroke stroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g2.setStroke(stroke);
            if (type == 1) { // Search
                g2.drawOval(2, 2, width - 8, height - 8);
                g2.drawLine(width - 6, height - 6, width - 2, height - 2);
            } else if (type == 2) { // Add
                g2.drawLine(width / 2, 2, width / 2, height - 2);
                g2.drawLine(2, height / 2, width - 2, height / 2);
            } else if (type == 3) { // Refresh
                g2.drawArc(2, 2, width - 4, height - 4, 45, 270);
                g2.fillPolygon(new int[] { width / 2 + 3, width / 2 + 8, width / 2 - 2 }, new int[] { 2, 8, 8 }, 3);
            } else if (type == 4) { // Save
                g2.drawRect(2, 2, width - 4, height - 4);
                g2.drawLine(5, 6, width - 5, 6);
                g2.drawLine(5, 10, width - 5, 10);
                g2.fillRect(width / 2 - 2, 1, 4, 4);
            } else if (type == 5) { // Cart
                g2.drawLine(1, 2, 4, 2);
                g2.drawLine(4, 2, 6, 10);
                g2.drawLine(6, 10, width - 2, 10);
                g2.drawLine(width - 2, 10, width - 4, 4);
                g2.drawLine(4, 4, width - 4, 4);
                g2.fillOval(5, 12, 3, 3);
                g2.fillOval(11, 12, 3, 3);
            } else if (type == 6) { // Trash / Delete
                g2.drawLine(2, 4, width - 2, 4);        // nắp
                g2.drawLine(5, 4, 5, height - 2);       // cạnh trái thùng
                g2.drawLine(width - 5, 4, width - 5, height - 2); // cạnh phải
                g2.drawLine(5, height - 2, width - 5, height - 2); // đáy
                g2.drawLine(width / 2, 4, width / 2, height - 2);  // vách giữa
                g2.drawLine(4, 2, width - 4, 2);        // nắp trên
            }
            g2.dispose();
        }
    }

    static class RoundedButton extends JButton {
        private Color currentBg;
        private final Color originalBg;
        private final Color hoverBg;

        public RoundedButton(String text, Icon icon, Color bg) {
            super(text, icon);
            this.originalBg = bg;
            this.currentBg = bg;
            this.hoverBg = new Color(Math.min(255, bg.getRed() + 30), Math.min(255, bg.getGreen() + 30),
                    Math.min(255, bg.getBlue() + 30));
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setIconTextGap(8);
            setBorder(new EmptyBorder(8, 16, 8, 16));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    currentBg = hoverBg;
                    repaint();
                }

                public void mouseExited(MouseEvent e) {
                    currentBg = originalBg;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ===================== FIELDS =====================
    private final DatabaseManager db = new DatabaseManager();
    private List<Product> products = new ArrayList<>();
    private int selectedIndex = 0;
    private BufferedImage oldImage, newImage;
    private float alpha = 1.0f;
    private Timer animationTimer;

    // UI components
    private JPanel mainImagePanel;
    private JLabel mainTitleLabel, mainPriceLabel, mainBrandLabel;
    private JTextArea mainDescArea;
    private JPanel gridPanel;
    private JTextField searchField;

    // ===================== CONSTRUCTOR =====================
    public Main() {
        setTitle("Lab 04 - Shoes Store + SQLite Database");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 720);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        products = db.getAllProducts();

        JPanel rootPanel = new JPanel(new BorderLayout(0, 0));
        rootPanel.setBackground(Color.WHITE);

        rootPanel.add(buildTopBar(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(30, 0));
        center.setBackground(Color.WHITE);
        center.setBorder(new EmptyBorder(20, 30, 30, 30));
        center.add(buildLeftPanel(), BorderLayout.WEST);
        center.add(buildGridScrollPane(), BorderLayout.CENTER);

        rootPanel.add(center, BorderLayout.CENTER);
        add(rootPanel);

        if (!products.isEmpty())
            updateLeftPanel(0, false);
    }

    // ===================== TOP BAR =====================
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(15, 0));
        bar.setBackground(new Color(30, 30, 40));
        bar.setBorder(new EmptyBorder(14, 30, 14, 30));

        JLabel logo = new JLabel(" Shoes Store ");
        logo.setIcon(new VectorIcon(5, 20, Color.WHITE));
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(Color.WHITE);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(260, 34));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm sản phẩm...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 110), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        RoundedButton btnSearch = new RoundedButton("Tìm kiếm", new VectorIcon(1, 14, Color.WHITE),
                new Color(60, 120, 255));
        btnSearch.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());

        RoundedButton btnAdd = new RoundedButton("Thêm SP", new VectorIcon(2, 14, Color.WHITE),
                new Color(40, 180, 100));
        btnAdd.addActionListener(e -> showAddDialog());

        RoundedButton btnReset = new RoundedButton("Tất cả", new VectorIcon(3, 14, Color.WHITE),
                new Color(90, 90, 100));
        btnReset.addActionListener(e -> {
            searchField.setText("");
            products = db.getAllProducts();
            refreshGrid();
        });

        searchPanel.add(searchField, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnSearch);
        btnPanel.add(btnReset);
        btnPanel.add(btnAdd);

        bar.add(logo, BorderLayout.WEST);
        bar.add(searchPanel, BorderLayout.CENTER);
        bar.add(btnPanel, BorderLayout.EAST);
        return bar;
    }

    // ===================== LEFT PANEL =====================
    private JPanel buildLeftPanel() {
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(Color.WHITE);
        left.setPreferredSize(new Dimension(290, 0));

        mainImagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                int w = getWidth(), h = getHeight();

                // Hiệu ứng trượt (Slide) & Mờ (Fade)
                if (oldImage != null && alpha < 1.0f) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - alpha));
                    int slideOut = (int) (alpha * 40); // trượt sang trái
                    drawCentered(g2, oldImage, w, h, -slideOut, 0);
                }
                if (newImage != null) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    int slideIn = (int) ((1.0f - alpha) * 40); // trượt từ phải vào
                    drawCentered(g2, newImage, w, h, slideIn, 0);
                }
                g2.dispose();
            }

            private void drawCentered(Graphics2D g2, BufferedImage img, int w, int h, int offsetX, int offsetY) {
                double s = Math.min((double) w / img.getWidth(), (double) h / img.getHeight());
                int iw = (int) (img.getWidth() * s), ih = (int) (img.getHeight() * s);
                g2.drawImage(img, (w - iw) / 2 + offsetX, (h - ih) / 2 + offsetY, iw, ih, null);
            }
        };
        mainImagePanel.setBackground(Color.WHITE);
        mainImagePanel.setPreferredSize(new Dimension(290, 260));
        mainImagePanel.setMaximumSize(new Dimension(290, 260));
        mainImagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(290, 1));
        sep.setForeground(new Color(210, 210, 210));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainTitleLabel = makeLabel("TITLE", 22, Font.BOLD, new Color(40, 40, 50));
        mainPriceLabel = makeLabel("$0.00", 20, Font.BOLD, new Color(60, 120, 255));
        mainBrandLabel = makeLabel("Brand", 13, Font.PLAIN, new Color(130, 130, 130));

        mainDescArea = new JTextArea("Description");
        mainDescArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mainDescArea.setForeground(new Color(140, 140, 140));
        mainDescArea.setLineWrap(true);
        mainDescArea.setWrapStyleWord(true);
        mainDescArea.setEditable(false);
        mainDescArea.setFocusable(false);
        mainDescArea.setBackground(Color.WHITE);
        mainDescArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dbInfo = new JLabel(" IE303 - Công Nghệ Java - Bài tập thực hành");
        dbInfo.setIcon(new VectorIcon(4, 12, new Color(140, 140, 140)));
        dbInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        dbInfo.setForeground(new Color(140, 140, 140));
        dbInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Nút xóa sản phẩm đang chọn
        RoundedButton btnDelete = new RoundedButton("Xóa sản phẩm", new VectorIcon(6, 14, Color.WHITE), new Color(220, 60, 60));
        btnDelete.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDelete.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnDelete.addActionListener(e -> {
            if (products.isEmpty()) return;
            Product cur = products.get(selectedIndex);
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc muốn xóa sản phẩm:\n\"" + cur.title + "\"?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                if (cur.id >= 0) {
                    db.deleteById(cur.id);
                } else {
                    // fallback nếu chưa load id
                    JOptionPane.showMessageDialog(this, "Không tìm được ID sản phẩm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                products = db.getAllProducts();
                refreshGrid();
            }
        });

        left.add(mainImagePanel);
        left.add(Box.createRigidArea(new Dimension(0, 12)));
        left.add(sep);
        left.add(Box.createRigidArea(new Dimension(0, 12)));
        left.add(mainTitleLabel);
        left.add(Box.createRigidArea(new Dimension(0, 6)));
        left.add(mainPriceLabel);
        left.add(Box.createRigidArea(new Dimension(0, 6)));
        left.add(mainBrandLabel);
        left.add(Box.createRigidArea(new Dimension(0, 10)));
        left.add(mainDescArea);
        left.add(Box.createRigidArea(new Dimension(0, 16)));
        left.add(btnDelete);
        left.add(Box.createRigidArea(new Dimension(0, 12)));
        left.add(dbInfo);
        return left;
    }

    private JLabel makeLabel(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ===================== GRID =====================
    private JScrollPane buildGridScrollPane() {
        gridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        gridPanel.setBackground(Color.WHITE);
        populateGrid();

        JScrollPane sp = new JScrollPane(gridPanel);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setBackground(Color.WHITE);
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private void populateGrid() {
        gridPanel.removeAll();
        for (int i = 0; i < products.size(); i++)
            gridPanel.add(createCard(i));
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void refreshGrid() {
        selectedIndex = 0;
        populateGrid();
        if (!products.isEmpty())
            updateLeftPanel(0, false);
    }

    // ===================== PRODUCT CARD =====================
    private JPanel createCard(int index) {
        Product p = products.get(index);

        JPanel card = new JPanel() {
            boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Smooth hover effect background
                if (hovered && selectedIndex != index) {
                    g2.setColor(new Color(235, 240, 248));
                } else {
                    g2.setColor(new Color(245, 246, 248));
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                // Border for selected
                if (selectedIndex == index) {
                    g2.setColor(new Color(60, 120, 255));
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
                } else if (hovered) {
                    g2.setColor(new Color(200, 210, 225));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
                }
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(8, 8));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        JLabel title = new JLabel(p.title);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(new Color(40, 40, 50));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel(p.shortDesc);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(150, 150, 150));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(title);
        top.add(Box.createRigidArea(new Dimension(0, 3)));
        top.add(sub);

        JLabel imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (p.image != null)
            imgLabel.setIcon(new ImageIcon(p.image.getScaledInstance(150, 150, Image.SCALE_SMOOTH)));

        JPanel bot = new JPanel(new BorderLayout());
        bot.setOpaque(false);
        JLabel brand = new JLabel(p.brand);
        brand.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        brand.setForeground(new Color(140, 140, 140));
        JLabel price = new JLabel(p.price);
        price.setFont(new Font("Segoe UI", Font.BOLD, 15));
        price.setForeground(new Color(60, 120, 255));
        bot.add(brand, BorderLayout.WEST);
        bot.add(price, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);
        card.add(imgLabel, BorderLayout.CENTER);
        card.add(bot, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (selectedIndex != index) {
                    updateLeftPanel(index, true);
                    gridPanel.repaint();
                }
            }
        });
        return card;
    }

    // ===================== UPDATE LEFT =====================
    private void updateLeftPanel(int idx, boolean animate) {
        Product p = products.get(idx);
        mainTitleLabel.setText(p.title);
        mainPriceLabel.setText(p.price);
        mainBrandLabel.setText(p.brand);
        mainDescArea.setText(p.desc);

        if (animate) {
            if (animationTimer != null && animationTimer.isRunning())
                animationTimer.stop();
            oldImage = products.get(selectedIndex).image;
            newImage = p.image;
            alpha = 0.0f;
            // Tăng tốc độ và độ mượt (60fps, step nhỏ)
            animationTimer = new Timer(15, e -> {
                alpha += 0.06f;
                if (alpha >= 1.0f) {
                    alpha = 1.0f;
                    animationTimer.stop();
                }
                mainImagePanel.repaint();
            });
            animationTimer.start();
        } else {
            oldImage = null;
            newImage = p.image;
            alpha = 1.0f;
            mainImagePanel.repaint();
        }
        selectedIndex = idx;
    }

    // ===================== SEARCH =====================
    private void doSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) {
            products = db.getAllProducts();
        } else {
            products = db.searchProducts(kw);
        }
        refreshGrid();
    }

    // ===================== ADD DIALOG =====================
    private void showAddDialog() {
        JDialog dlg = new JDialog(this, "Thêm sản phẩm mới", true);
        dlg.setSize(420, 380);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 10, 20));
        form.setBackground(Color.WHITE);

        JTextField fTitle = new JTextField();
        JTextField fPrice = new JTextField("$0.00");
        JTextField fBrand = new JTextField("Adidas");
        JTextField fShort = new JTextField();
        JTextField fImg = new JTextField("img1.png");
        fImg.setEditable(false);
        JButton btnImg = new JButton("...");
        btnImg.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnImg.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Hình ảnh (JPG, PNG)", "jpg", "png", "jpeg"));
            if (chooser.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    File dest = new File(DatabaseManager.DB_DIR, file.getName());
                    java.nio.file.Files.copy(file.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    fImg.setText(file.getName());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "Lỗi khi tải ảnh: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JPanel imgPanel = new JPanel(new BorderLayout(5, 0));
        imgPanel.setOpaque(false);
        imgPanel.add(fImg, BorderLayout.CENTER);
        imgPanel.add(btnImg, BorderLayout.EAST);

        form.add(new JLabel("Tên sản phẩm:"));
        form.add(fTitle);
        form.add(new JLabel("Giá (vd: $100.00):"));
        form.add(fPrice);
        form.add(new JLabel("Thương hiệu:"));
        form.add(fBrand);
        form.add(new JLabel("Mô tả ngắn:"));
        form.add(fShort);
        form.add(new JLabel("File ảnh (chọn từ máy):"));
        form.add(imgPanel);

        RoundedButton btnSave = new RoundedButton("Lưu vào CSDL", new VectorIcon(4, 14, Color.WHITE),
                new Color(60, 120, 255));
        btnSave.addActionListener(e -> {
            String title = fTitle.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập tên sản phẩm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean ok = db.insertProduct(title, fPrice.getText().trim(),
                    fBrand.getText().trim(), fShort.getText().trim(),
                    "This product is excluded from all\npromotional discounts and offers.",
                    fImg.getText().trim());
            if (ok) {
                JOptionPane.showMessageDialog(dlg, "✅ Đã thêm sản phẩm \"" + title + "\" vào CSDL!");
                dlg.dispose();
                products = db.getAllProducts();
                refreshGrid();
            } else {
                JOptionPane.showMessageDialog(dlg, "❌ Lỗi khi lưu vào CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnRow = new JPanel();
        btnRow.setBackground(Color.WHITE);
        btnRow.setBorder(new EmptyBorder(10, 0, 15, 0));
        btnRow.add(btnSave);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setVisible(true);
    }

    // ===================== MAIN =====================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            Main app = new Main();
            app.setVisible(true);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> app.db.close()));
        });
    }
}
