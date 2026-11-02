import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** A simple menu-driven ATM window. All stored data is handled by Bank.java. */
public class ATMApp extends JFrame {
    static final Color ACCENT = new Color(58, 81, 104), INK = new Color(41, 47, 53);
    static final Color CREAM = new Color(249, 249, 247), MUTED = new Color(104, 111, 118);
    static final Color LINE = new Color(218, 222, 226), GREEN = new Color(59, 119, 72);
    static final Color SIDEBAR = new Color(239, 242, 244), SELECTED = new Color(217, 226, 234);
    static final Font BODY = new Font("SansSerif", Font.PLAIN, 15);
    static final String[] MENU = {"Create account", "Account details", "Balance enquiry",
            "Cash deposit", "Cash withdrawal", "Fund transfer", "Mini statement", "Exit"};
    final Bank bank;
    Bank.Account current;
    final JPanel content = new JPanel(new BorderLayout());
    final JPanel body = new JPanel();
    final JLabel title = new JLabel(), subtitle = new JLabel(), status = new JLabel(" ");
    final JLabel identity = new JLabel(" ");
    final Map<String, JComponent> inputs = new LinkedHashMap<>();
    final Map<String, JButton> navigation = new LinkedHashMap<>();
    JButton actionButton;
    String page = "Sign in";
    static final DecimalFormat CASH = new DecimalFormat("#,##0.00");

    public ATMApp(Bank bank) {
        super("Reni Bank | ATM Banking");
        this.bank = bank;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 780);
        setMinimumSize(new Dimension(980, 740));
        setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CREAM);
        setContentPane(root);
        root.add(sidebar(), BorderLayout.WEST);
        content.setBackground(CREAM);
        content.setBorder(new EmptyBorder(36, 38, 24, 38));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        title.setFont(BODY.deriveFont(Font.BOLD, 30));
        title.setForeground(INK);
        subtitle.setFont(BODY);
        subtitle.setForeground(MUTED);
        heading.add(title);
        heading.add(Box.createVerticalStrut(9));
        heading.add(subtitle);
        heading.add(Box.createVerticalStrut(28));
        content.add(heading, BorderLayout.NORTH);
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        content.add(body, BorderLayout.CENTER);
        status.setFont(BODY.deriveFont(13f));
        status.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(status, BorderLayout.SOUTH);
        root.add(content, BorderLayout.CENTER);
        showPage("Sign in");
    }

    private JPanel sidebar() {
        JPanel side = new JPanel();
        side.setBackground(SIDEBAR);
        side.setPreferredSize(new Dimension(246, 0));
        side.setBorder(new EmptyBorder(32, 23, 25, 23));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        JLabel mark = new JLabel("RB /");
        mark.setFont(BODY.deriveFont(Font.BOLD, 38));
        mark.setForeground(ACCENT);
        side.add(mark);
        side.add(Box.createVerticalStrut(8));
        JLabel name = new JLabel("RENI BANK");
        name.setFont(BODY.deriveFont(Font.BOLD, 14));
        name.setForeground(INK);
        side.add(name);
        side.add(Box.createVerticalStrut(40));
        for (int i = 0; i < MENU.length; i++) {
            String item = MENU[i];
            JButton button = new JButton((i + 1) + "   " + item);
            button.setFont(BODY.deriveFont(14f));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setMaximumSize(new Dimension(202, 42));
            button.setBorder(new EmptyBorder(9, 12, 9, 8));
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.setBackground(SIDEBAR);
            button.setForeground(INK);
            button.addActionListener(e -> {
                if (item.equals("Exit")) dispose();
                else if (current == null && !item.equals("Create account"))
                    message("Please sign in with your account number and PIN.", true);
                else showPage(item);
            });
            navigation.put(item, button);
            side.add(button);
            side.add(Box.createVerticalStrut(5));
        }
        side.add(Box.createVerticalGlue());
        identity.setFont(BODY.deriveFont(12f));
        identity.setForeground(MUTED);
        side.add(identity);
        side.add(Box.createVerticalStrut(10));
        JButton logout = new JButton("Sign in / Sign out");
        logout.setFont(BODY.deriveFont(12f));
        logout.setBorder(new EmptyBorder(6, 0, 6, 0));
        logout.setContentAreaFilled(false);
        logout.setForeground(ACCENT);
        logout.setFocusPainted(false);
        logout.addActionListener(e -> { current = null; identity.setText(" "); showPage("Sign in"); });
        side.add(logout);
        return side;
    }

    void showPage(String name) {
        page = name;
        inputs.clear();
        actionButton = null;
        body.removeAll();
        status.setText(" ");
        title.setText(name);
        subtitle.setText(current == null ? " " :
                "Account " + current.number() + "  /  " + current.type());
        navigation.forEach((key, value) -> value.setBackground(key.equals(name) ? SELECTED : SIDEBAR));
        try {
            if (current != null) current = bank.getAccount(current.number());
            switch (name) {
                case "Sign in" -> loginPage();
                case "Create account" -> createPage();
                case "Account details" -> detailsPage();
                case "Balance enquiry" -> balancePage();
                case "Cash deposit", "Cash withdrawal" -> cashPage(name.equals("Cash deposit"));
                case "Fund transfer" -> transferPage();
                case "Mini statement" -> statementPage();
                default -> throw new IllegalArgumentException("Unknown menu option.");
            }
        } catch (Exception e) { message(readable(e), true); }
        body.revalidate();
        body.repaint();
    }

    private JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(28, 30, 28, 30)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(panel);
        body.add(Box.createVerticalStrut(18));
        return panel;
    }

    private JLabel label(String text, float size, Color color, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(BODY.deriveFont(bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void text(JPanel panel, String value, float size, Color color, boolean bold) {
        panel.add(label(value, size, color, bold));
        panel.add(Box.createVerticalStrut(12));
    }

    private JComponent field(JPanel panel, String key, String caption, boolean secret) {
        panel.add(label(caption, 13, MUTED, true));
        panel.add(Box.createVerticalStrut(6));
        JTextField field = secret ? new JPasswordField() : new JTextField();
        field.setFont(BODY);
        field.setForeground(INK);
        field.setBackground(new Color(251, 252, 253));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(10, 12, 10, 12)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(field);
        panel.add(Box.createVerticalStrut(16));
        inputs.put(key, field);
        return field;
    }

    private JButton action(JPanel panel, String caption, Task task) {
        JButton button = new JButton(caption);
        button.setFont(BODY.deriveFont(Font.BOLD));
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(13, 22, 13, 22));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(e -> {
            try { task.run(); }
            catch (Exception ex) { message(readable(ex), true); }
        });
        panel.add(button);
        actionButton = button;
        return button;
    }

    private String value(String key) {
        JComponent field = inputs.get(key);
        if (field instanceof JPasswordField p) return new String(p.getPassword());
        if (field instanceof JTextField t) return t.getText().trim();
        return String.valueOf(((JComboBox<?>) field).getSelectedItem());
    }

    void setInput(String key, String value) {
        JComponent field = inputs.get(key);
        if (field instanceof JTextField t) t.setText(value);
        else ((JComboBox<?>) field).setSelectedItem(value);
    }

    private long number(String key) {
        try {
            long number = Long.parseLong(value(key));
            if (number <= 0) throw new NumberFormatException();
            return number;
        } catch (NumberFormatException e) { throw new IllegalArgumentException("Enter a valid account number."); }
    }

    private void loginPage() {
        JPanel panel = card();
        field(panel, "account", "ACCOUNT NUMBER", false);
        field(panel, "pin", "PIN", true);
        action(panel, "Sign in  →", () -> {
            current = bank.login(number("account"), value("pin"));
            identity.setText(current.name());
            showPage("Balance enquiry");
        });
        body.add(Box.createVerticalGlue());
    }

    private void createPage() {
        JPanel panel = card();
        JPanel columns = new JPanel(new GridLayout(1, 2, 24, 0));
        columns.setOpaque(false);
        columns.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel left = new JPanel(), right = new JPanel();
        for (JPanel p : new JPanel[]{left, right}) { p.setOpaque(false); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); }
        field(left, "name", "CUSTOMER NAME", false);
        left.add(label("ACCOUNT TYPE", 13, MUTED, true));
        left.add(Box.createVerticalStrut(6));
        JComboBox<String> type = new JComboBox<>(new String[]{"Savings", "Current"});
        type.setFont(BODY);
        type.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        type.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(type);
        left.add(Box.createVerticalStrut(16));
        inputs.put("type", type);
        field(left, "mobile", "MOBILE NUMBER  /  10 DIGITS", false);
        field(right, "pin", "CHOOSE PIN  /  4 DIGITS", true);
        field(right, "initial", "INITIAL DEPOSIT  /  ₹", false);
        columns.add(left); columns.add(right);
        panel.add(columns);
        panel.add(Box.createVerticalStrut(10));
        action(panel, "Create my account  →", () -> {
            long n = bank.createAccount(value("name"), value("type"), value("pin"), value("mobile"), value("initial"));
            current = bank.getAccount(n);
            identity.setText(current.name());
            showPage("Account details");
            message("Account created successfully. Your account number is " + n + ".", false);
        });
        body.add(Box.createVerticalGlue());
    }

    private void detailsPage() {
        JPanel panel = card();
        text(panel, current.name(), 25, INK, true);
        text(panel, current.type() + " account  /  Active", 14, GREEN, true);
        panel.add(Box.createVerticalStrut(18));
        String[][] rows = {{"Account number", String.valueOf(current.number())}, {"Customer name", current.name()},
                {"Account type", current.type()}, {"Mobile number", current.mobile()}, {"PIN", "••••"},
                {"Account balance", "₹ " + cash(current.balance())}};
        for (String[] row : rows) {
            JPanel line = new JPanel(new BorderLayout());
            line.setOpaque(false);
            line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            line.add(label(row[0], 14, MUTED, false), BorderLayout.WEST);
            line.add(label(row[1], 15, INK, true), BorderLayout.EAST);
            panel.add(line);
        }
        body.add(Box.createVerticalGlue());
    }

    private void balancePage() {
        JPanel panel = card();
        text(panel, "AVAILABLE BALANCE", 13, MUTED, true);
        text(panel, "₹ " + cash(current.balance()), 43, ACCENT, true);
        panel.add(Box.createVerticalStrut(16));
        action(panel, "Refresh balance", () -> { showPage("Balance enquiry"); message("Balance refreshed.", false); });
        body.add(Box.createVerticalGlue());
    }

    private void cashPage(boolean deposit) {
        JPanel panel = card();
        text(panel, "Available balance  /  ₹ " + cash(current.balance()), 17, ACCENT, true);
        panel.add(Box.createVerticalStrut(15));
        field(panel, "amount", "AMOUNT  /  ₹", false);
        action(panel, deposit ? "Deposit money  →" : "Withdraw money  →", () -> {
            BigDecimal balance = deposit ? bank.deposit(current.number(), value("amount")) :
                    bank.withdraw(current.number(), value("amount"));
            showPage(deposit ? "Cash deposit" : "Cash withdrawal");
            message((deposit ? "Deposit" : "Withdrawal") + " successful. Available balance: ₹ " + cash(balance), false);
        });
        body.add(Box.createVerticalGlue());
    }

    private void transferPage() {
        JPanel panel = card();
        text(panel, "Available balance  /  ₹ " + cash(current.balance()), 17, ACCENT, true);
        panel.add(Box.createVerticalStrut(12));
        field(panel, "recipient", "RECIPIENT ACCOUNT NUMBER", false);
        field(panel, "amount", "TRANSFER AMOUNT  /  ₹", false);
        action(panel, "Transfer money  →", () -> {
            long recipient = number("recipient");
            BigDecimal balance = bank.transfer(current.number(), recipient, value("amount"));
            showPage("Fund transfer");
            message("Transfer to " + recipient + " successful. Available balance: ₹ " + cash(balance), false);
        });
        body.add(Box.createVerticalGlue());
    }

    private void statementPage() throws Exception {
        JPanel panel = card();
        text(panel, "Available balance  /  ₹ " + cash(current.balance()), 14, ACCENT, true);
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Date & time", "Transaction", "Amount (₹)", "Other account", "Balance (₹)"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        DateTimeFormatter date = DateTimeFormatter.ofPattern("dd MMM, HH:mm");
        for (Bank.Entry e : bank.statement(current.number())) {
            String sign = (e.type().equals("WITHDRAWAL") || e.type().equals("TRANSFER_OUT")) ? "− " : "+ ";
            model.addRow(new Object[]{e.time().toLocalDateTime().format(date), e.type().replace('_', ' '),
                    sign + cash(e.amount()), e.other() == null ? "—" : e.other(), cash(e.balance())});
        }
        JTable table = new JTable(model);
        table.setFont(BODY.deriveFont(12f));
        table.setRowHeight(39);
        table.setShowVerticalLines(false);
        table.setGridColor(LINE);
        table.setSelectionBackground(SELECTED);
        table.getTableHeader().setFont(BODY.deriveFont(Font.BOLD, 11f));
        table.getTableHeader().setPreferredSize(new Dimension(0, 37));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.setPreferredSize(new Dimension(660, 400));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(scroll);
    }

    void message(String text, boolean error) { status.setForeground(error ? new Color(171, 51, 58) : GREEN); status.setText(text); }
    static String cash(BigDecimal value) { return CASH.format(value); }
    private static String readable(Exception e) {
        return e instanceof java.sql.SQLException ? "Database unavailable. Check MySQL and config.properties." :
                (e.getMessage() == null ? "The operation could not be completed." : e.getMessage());
    }
    @FunctionalInterface interface Task { void run() throws Exception; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                Bank bank = new Bank();
                bank.initialize();
                new ATMApp(bank).setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Cannot start ATM Banking.\nStart MySQL and check config.properties.\n\n" +
                        e.getMessage(), "Database connection", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
