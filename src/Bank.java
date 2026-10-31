import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Small JDBC layer: all money changes and their statement entries commit together. */
public class Bank {
    public record Account(long number, String name, String type, BigDecimal balance, String mobile) {}
    public record Entry(Timestamp time, String type, BigDecimal amount, Long other, BigDecimal balance) {}
    private final String url, user, password;
    private static final BigDecimal MAX = new BigDecimal("9999999999.99");

    public Bank() throws Exception {
        Properties p = new Properties();
        Path file = Path.of(System.getProperty("config", "config.properties"));
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) { p.load(in); }
        }
        url = p.getProperty("db.url", "jdbc:mysql://127.0.0.1:3307/atm_bank");
        user = p.getProperty("db.user", "atm_user");
        password = p.getProperty("db.password", "");
        DriverManager.setLoginTimeout(5);
    }

    Connection connect() throws SQLException { return DriverManager.getConnection(url, user, password); }

    public void initialize() throws SQLException {
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS accounts (
                    account_number BIGINT AUTO_INCREMENT PRIMARY KEY,
                    customer_name VARCHAR(80) NOT NULL,
                    account_type VARCHAR(10) NOT NULL,
                    pin_hash VARCHAR(100) NOT NULL,
                    pin_salt VARCHAR(40) NOT NULL,
                    balance DECIMAL(12,2) NOT NULL CHECK (balance >= 0),
                    mobile_number VARCHAR(10) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    CHECK (account_type IN ('Savings', 'Current'))
                ) ENGINE=InnoDB AUTO_INCREMENT=1001001
                """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    account_number BIGINT NOT NULL,
                    transaction_type VARCHAR(20) NOT NULL,
                    amount DECIMAL(12,2) NOT NULL CHECK (amount >= 0),
                    related_account BIGINT NULL,
                    balance_after DECIMAL(12,2) NOT NULL,
                    transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (account_number) REFERENCES accounts(account_number),
                    FOREIGN KEY (related_account) REFERENCES accounts(account_number),
                    INDEX account_history (account_number, transaction_id)
                ) ENGINE=InnoDB
                """);
        }
    }

    public static BigDecimal money(String text, boolean allowZero) {
        try {
            BigDecimal amount = new BigDecimal(text.trim()).setScale(2, RoundingMode.UNNECESSARY);
            if (amount.signum() < 0 || (!allowZero && amount.signum() == 0) || amount.compareTo(MAX) > 0)
                throw new IllegalArgumentException("Enter a valid amount " + (allowZero ? "of zero or more." : "greater than zero."));
            return amount;
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException("Enter an amount with at most two decimal places.");
        }
    }

    public long createAccount(String name, String type, String pin, String mobile, String deposit) throws Exception {
        name = name.trim();
        if (name.length() < 2 || name.length() > 80) throw new IllegalArgumentException("Name must contain 2 to 80 characters.");
        if (!type.equals("Savings") && !type.equals("Current")) throw new IllegalArgumentException("Select Savings or Current.");
        if (!pin.matches("[0-9]{4}")) throw new IllegalArgumentException("PIN must contain exactly four digits.");
        if (!mobile.matches("[0-9]{10}")) throw new IllegalArgumentException("Mobile number must contain exactly ten digits.");
        BigDecimal amount = money(deposit, true);
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try (PreparedStatement s = c.prepareStatement("""
                    INSERT INTO accounts(customer_name, account_type, pin_hash, pin_salt, balance, mobile_number)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS)) {
                s.setString(1, name); s.setString(2, type); s.setString(3, hash(pin, salt));
                s.setString(4, Base64.getEncoder().encodeToString(salt));
                s.setBigDecimal(5, amount); s.setString(6, mobile); s.executeUpdate();
                long number;
                try (ResultSet keys = s.getGeneratedKeys()) { keys.next(); number = keys.getLong(1); }
                record(c, number, "OPENING", amount, null, amount);
                c.commit();
                return number;
            } catch (Exception e) { c.rollback(); throw e; }
        }
    }

    public Account login(long number, String pin) throws Exception {
        try (Connection c = connect(); PreparedStatement s = c.prepareStatement(
                "SELECT pin_hash, pin_salt FROM accounts WHERE account_number = ?")) {
            s.setLong(1, number);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    byte[] salt = Base64.getDecoder().decode(r.getString("pin_salt"));
                    byte[] stored = Base64.getDecoder().decode(r.getString("pin_hash"));
                    byte[] given = Base64.getDecoder().decode(hash(pin, salt));
                    if (MessageDigest.isEqual(stored, given)) return account(c, number, false);
                }
            }
        }
        throw new IllegalArgumentException("Account number or PIN is incorrect.");
    }

    public Account getAccount(long number) throws SQLException {
        try (Connection c = connect()) { return account(c, number, false); }
    }

    private Account account(Connection c, long number, boolean lock) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?" + (lock ? " FOR UPDATE" : "");
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, number);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("Account number does not exist.");
                return new Account(r.getLong("account_number"), r.getString("customer_name"),
                        r.getString("account_type"), r.getBigDecimal("balance"), r.getString("mobile_number"));
            }
        }
    }

    public BigDecimal deposit(long number, String value) throws SQLException {
        return change(number, money(value, false), false);
    }

    public BigDecimal withdraw(long number, String value) throws SQLException {
        return change(number, money(value, false), true);
    }

    private BigDecimal change(long number, BigDecimal amount, boolean withdraw) throws SQLException {
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                Account a = account(c, number, true);
                if (withdraw && a.balance().compareTo(amount) < 0)
                    throw new IllegalArgumentException("Insufficient balance. Please enter a smaller amount.");
                BigDecimal balance = a.balance().add(withdraw ? amount.negate() : amount);
                if (balance.compareTo(MAX) > 0) throw new IllegalArgumentException("The account balance limit would be exceeded.");
                updateBalance(c, number, balance);
                record(c, number, withdraw ? "WITHDRAWAL" : "DEPOSIT", amount, null, balance);
                c.commit();
                return balance;
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }

    public BigDecimal transfer(long from, long to, String value) throws SQLException {
        if (from == to) throw new IllegalArgumentException("Choose a different account for the transfer.");
        BigDecimal amount = money(value, false);
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                // Lock in account-number order to avoid opposite transfers deadlocking.
                Account first = account(c, Math.min(from, to), true);
                Account second = account(c, Math.max(from, to), true);
                Account sender = first.number() == from ? first : second;
                Account receiver = first.number() == to ? first : second;
                if (sender.balance().compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient balance for this transfer.");
                BigDecimal debit = sender.balance().subtract(amount);
                BigDecimal credit = receiver.balance().add(amount);
                if (credit.compareTo(MAX) > 0) throw new IllegalArgumentException("The recipient balance limit would be exceeded.");
                updateBalance(c, from, debit); updateBalance(c, to, credit);
                record(c, from, "TRANSFER_OUT", amount, to, debit);
                record(c, to, "TRANSFER_IN", amount, from, credit);
                c.commit();
                return debit;
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }

    private void updateBalance(Connection c, long number, BigDecimal balance) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("UPDATE accounts SET balance = ? WHERE account_number = ?")) {
            s.setBigDecimal(1, balance); s.setLong(2, number); s.executeUpdate();
        }
    }

    private void record(Connection c, long number, String type, BigDecimal amount, Long other, BigDecimal balance) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("""
                INSERT INTO transactions(account_number, transaction_type, amount, related_account, balance_after)
                VALUES (?, ?, ?, ?, ?)
                """)) {
            s.setLong(1, number); s.setString(2, type); s.setBigDecimal(3, amount);
            if (other == null) s.setNull(4, Types.BIGINT); else s.setLong(4, other);
            s.setBigDecimal(5, balance); s.executeUpdate();
        }
    }

    public List<Entry> statement(long number) throws SQLException {
        List<Entry> rows = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement s = c.prepareStatement("""
                SELECT * FROM transactions WHERE account_number = ? ORDER BY transaction_id DESC LIMIT 10
                """)) {
            s.setLong(1, number);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    long related = r.getLong("related_account");
                    Long other = r.wasNull() ? null : related;
                    rows.add(new Entry(r.getTimestamp("transaction_time"), r.getString("transaction_type"),
                            r.getBigDecimal("amount"), other, r.getBigDecimal("balance_after")));
                }
            }
        }
        return rows;
    }

    private static String hash(String pin, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, 65536, 256);
        try {
            byte[] key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(key);
        } finally { spec.clearPassword(); }
    }
}
