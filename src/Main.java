import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Main runner: creates the MVC objects and starts the menu.
public class Main {
    public static void main(String[] args) {
        new ATMController(new AccountDAO(), new ATMView()).run();
    }
}

// Models: one customer's account, and one mini statement row.
record Account(long number, String name, String type, BigDecimal balance, String mobile) {}

record Transaction(Timestamp time, String type, BigDecimal amount, Long otherAccount, BigDecimal balance) {}

// Controller: connects the console view to the JDBC DAO.
class ATMController {
    private final AccountDAO dao;
    private final ATMView view;

    ATMController(AccountDAO dao, ATMView view) {
        this.dao = dao;
        this.view = view;
    }

    void run() {
        while (true) {
            try {
                view.showMenu();
                String choice = view.read("Choose an option");
                if (choice.equals("8")) return;
                if (choice.equals("1")) {
                    long number = dao.createAccount(view.read("Customer name"), view.read("Account type (Savings/Current)"),
                            view.read("PIN (4 digits)"), view.read("Mobile number (10 digits)"), view.read("Initial deposit in INR"));
                    view.message("Account created. Account number: " + number);
                    continue;
                }
                if (!choice.matches("[2-7]")) {
                    view.message("Choose an option from 1 to 8.");
                    continue;
                }
                long number = view.readNumber("Account number");
                Account account = dao.login(number, view.read("PIN"));
                switch (choice) {
                    case "2" -> view.showAccount(account);
                    case "3" -> view.showBalance(account.balance());
                    case "4" -> view.showBalance(dao.deposit(number, view.read("Deposit amount in INR")));
                    case "5" -> view.showBalance(dao.withdraw(number, view.read("Withdrawal amount in INR")));
                    case "6" -> {
                        long recipient = view.readNumber("Recipient account number");
                        view.showBalance(dao.transfer(number, recipient, view.read("Transfer amount in INR")));
                    }
                    case "7" -> view.showStatement(dao.statement(number));
                }
            } catch (IllegalStateException e) {
                return; // Input was closed.
            } catch (Exception e) {
                view.message(e.getMessage());
            }
        }
    }
}

// View: only reads keyboard input and prints results.
class ATMView {
    private final Scanner input = new Scanner(System.in);

    void showMenu() {
        System.out.println("""

                --- RENI BANK ---
                1. Create new account
                2. Display account details
                3. Balance enquiry
                4. Cash deposit
                5. Cash withdrawal
                6. Fund transfer
                7. Mini statement
                8. Exit""");
    }

    String read(String prompt) {
        System.out.print(prompt + ": ");
        if (!input.hasNextLine()) throw new IllegalStateException("Input closed.");
        return input.nextLine().trim();
    }

    long readNumber(String prompt) {
        try {
            long number = Long.parseLong(read(prompt));
            if (number > 0) return number;
        } catch (NumberFormatException e) { }
        throw new IllegalArgumentException("Enter a valid account number.");
    }

    void showAccount(Account a) {
        System.out.println("\nAccount number: " + a.number());
        System.out.println("Customer name: " + a.name());
        System.out.println("Account type: " + a.type());
        System.out.println("Mobile number: " + a.mobile());
        System.out.println("PIN: ****");
        showBalance(a.balance());
    }

    void showBalance(BigDecimal balance) {
        System.out.printf("Balance: INR %,.2f%n", balance);
    }

    void showStatement(List<Transaction> rows) {
        System.out.println("\nDATE AND TIME | TYPE | AMOUNT | OTHER ACCOUNT | BALANCE");
        for (Transaction t : rows)
            System.out.printf("%s | %s | INR %,.2f | %s | INR %,.2f%n", t.time(), t.type(), t.amount(),
                    t.otherAccount() == null ? "-" : t.otherAccount(), t.balance());
    }

    void message(String text) {
        System.out.println(text);
    }
}

// DAO: all JDBC work. Each money change and its statement entry commit together.
class AccountDAO {
    private static final BigDecimal MAX = new BigDecimal("9999999999.99");
    // Matches setup.sql. Change the port here if your MySQL does not use 3306.
    private static final String URL = "jdbc:mysql://localhost:3306/atm_bank", USER = "atm_user", PASSWORD = "ReniBankDemo123";

    private interface Work<T> { T run(Connection c) throws SQLException; }

    AccountDAO() {
        DriverManager.setLoginTimeout(5);
    }

    long createAccount(String name, String type, String pin, String mobile, String deposit) throws SQLException {
        if (name.length() < 2 || name.length() > 80) throw new IllegalArgumentException("Name must contain 2 to 80 characters.");
        if (!type.equals("Savings") && !type.equals("Current")) throw new IllegalArgumentException("Select Savings or Current.");
        if (!pin.matches("[0-9]{4}")) throw new IllegalArgumentException("PIN must contain exactly four digits.");
        if (!mobile.matches("[0-9]{10}")) throw new IllegalArgumentException("Mobile number must contain exactly ten digits.");
        BigDecimal amount = money(deposit, true);
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return inTransaction(c -> {
            try (PreparedStatement s = prepare(c, """
                    INSERT INTO accounts(customer_name, account_type, pin_hash, pin_salt, balance, mobile_number)
                    VALUES (?, ?, ?, ?, ?, ?)""", name, type, hash(pin, salt), Base64.getEncoder().encodeToString(salt), amount, mobile)) {
                s.executeUpdate();
                try (ResultSet keys = s.getGeneratedKeys()) {
                    keys.next();
                    long number = keys.getLong(1);
                    record(c, number, "OPENING", amount, null, amount);
                    return number;
                }
            }
        });
    }

    Account login(long number, String pin) throws SQLException {
        try (Connection c = connect(); PreparedStatement s = prepare(c, "SELECT * FROM accounts WHERE account_number = ?", number);
             ResultSet r = s.executeQuery()) {
            if (r.next()) {
                byte[] salt = Base64.getDecoder().decode(r.getString("pin_salt"));
                byte[] stored = Base64.getDecoder().decode(r.getString("pin_hash"));
                if (MessageDigest.isEqual(stored, Base64.getDecoder().decode(hash(pin, salt))))
                    return new Account(number, r.getString("customer_name"), r.getString("account_type"),
                            r.getBigDecimal("balance"), r.getString("mobile_number"));
            }
        }
        throw new IllegalArgumentException("Account number or PIN is incorrect.");
    }

    BigDecimal deposit(long number, String value) throws SQLException {
        BigDecimal amount = money(value, false);
        return inTransaction(c -> post(c, number, amount, "DEPOSIT", null, null, "The account balance limit would be exceeded."));
    }

    BigDecimal withdraw(long number, String value) throws SQLException {
        BigDecimal amount = money(value, false);
        return inTransaction(c -> post(c, number, amount.negate(), "WITHDRAWAL", null,
                "Insufficient balance. Please enter a smaller amount.", null));
    }

    BigDecimal transfer(long from, long to, String value) throws SQLException {
        if (from == to) throw new IllegalArgumentException("Choose a different account for the transfer.");
        BigDecimal amount = money(value, false);
        return inTransaction(c -> {
            // Lock in account-number order to avoid opposite transfers deadlocking.
            lockBalance(c, Math.min(from, to));
            lockBalance(c, Math.max(from, to));
            BigDecimal debit = post(c, from, amount.negate(), "TRANSFER_OUT", to, "Insufficient balance for this transfer.", null);
            post(c, to, amount, "TRANSFER_IN", from, null, "The recipient balance limit would be exceeded.");
            return debit;
        });
    }

    List<Transaction> statement(long number) throws SQLException {
        List<Transaction> rows = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement s = prepare(c,
                "SELECT * FROM transactions WHERE account_number = ? ORDER BY transaction_id DESC LIMIT 10", number);
             ResultSet r = s.executeQuery()) {
            while (r.next())
                rows.add(new Transaction(r.getTimestamp("transaction_time"), r.getString("transaction_type"),
                        r.getBigDecimal("amount"), r.getObject("related_account", Long.class), r.getBigDecimal("balance_after")));
        }
        return rows;
    }

    // Adds delta to a locked balance, checks limits, and records the statement entry.
    private BigDecimal post(Connection c, long number, BigDecimal delta, String type, Long other,
                            String tooLow, String tooHigh) throws SQLException {
        BigDecimal balance = lockBalance(c, number).add(delta);
        if (balance.signum() < 0) throw new IllegalArgumentException(tooLow);
        if (balance.compareTo(MAX) > 0) throw new IllegalArgumentException(tooHigh);
        try (PreparedStatement s = prepare(c, "UPDATE accounts SET balance = ? WHERE account_number = ?", balance, number)) {
            s.executeUpdate();
        }
        record(c, number, type, delta.abs(), other, balance);
        return balance;
    }

    private BigDecimal lockBalance(Connection c, long number) throws SQLException {
        try (PreparedStatement s = prepare(c, "SELECT balance FROM accounts WHERE account_number = ? FOR UPDATE", number);
             ResultSet r = s.executeQuery()) {
            if (!r.next()) throw new IllegalArgumentException("Account number does not exist.");
            return r.getBigDecimal(1);
        }
    }

    private void record(Connection c, long number, String type, BigDecimal amount, Long other, BigDecimal balance) throws SQLException {
        try (PreparedStatement s = prepare(c, """
                INSERT INTO transactions(account_number, transaction_type, amount, related_account, balance_after)
                VALUES (?, ?, ?, ?, ?)""", number, type, amount, other, balance)) {
            s.executeUpdate();
        }
    }

    private <T> T inTransaction(Work<T> work) throws SQLException {
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                T result = work.run(c);
                c.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private PreparedStatement prepare(Connection c, String sql, Object... values) throws SQLException {
        PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < values.length; i++) s.setObject(i + 1, values[i]);
        return s;
    }

    private static BigDecimal money(String text, boolean allowZero) {
        try {
            BigDecimal amount = new BigDecimal(text).setScale(2, RoundingMode.UNNECESSARY);
            if (amount.signum() < 0 || (!allowZero && amount.signum() == 0) || amount.compareTo(MAX) > 0)
                throw new IllegalArgumentException("Enter a valid amount " + (allowZero ? "of zero or more." : "greater than zero."));
            return amount;
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException("Enter an amount with at most two decimal places.");
        }
    }

    private static String hash(String pin, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, 65536, 256);
        try {
            return Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("PIN hashing is unavailable.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
