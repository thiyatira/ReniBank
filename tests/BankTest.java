import java.sql.*;

// Small checks using a separate test database, never the customer database.
public class BankTest {
    private static int passed;
    private static AccountDAO dao;

    public static void main(String[] args) throws Exception {
        dao = new AccountDAO();
        try (Connection connection = dao.getConnection()) {
            if (!connection.getCatalog().contains("_test"))
                throw new IllegalStateException("Tests require the separate atm_bank_test database.");
        }
        dao.initialize();
        long sender = 0, receiver = 0;
        try {
            sender = dao.createAccount("Test Sender", "Savings", "1234", "9000000091", "1000");
            receiver = dao.createAccount("Test Receiver", "Current", "4321", "9000000092", "500");
            check(dao.getAccount(sender).getName().equals("Test Sender"), "Create and read account");
            check(dao.login(sender, "1234").getNumber() == sender, "Correct PIN");
            long account = sender, other = receiver;
            rejects(() -> dao.login(account, "0000"), "Wrong PIN");
            balance(sender, "1000");
            dao.deposit(sender, "200");
            balance(sender, "1200");
            dao.withdraw(sender, "100");
            balance(sender, "1100");
            dao.transfer(sender, receiver, "250");
            balance(sender, "850");
            balance(receiver, "750");
            rejects(() -> dao.withdraw(account, "9999"), "Insufficient withdrawal");
            rejects(() -> dao.transfer(account, other, "9999"), "Insufficient transfer");
            rejects(() -> dao.transfer(account, account, "1"), "Self transfer");
            rejects(() -> dao.transfer(account, Long.MAX_VALUE, "1"), "Missing recipient");
            rejects(() -> dao.deposit(account, "-10"), "Negative deposit");
            balance(sender, "850");
            check(dao.statement(sender).get(0).getType().equals("TRANSFER_OUT"), "Mini statement");
            System.out.println("RESULT: " + passed + " checks passed.");
        } finally {
            try (Connection connection = dao.getConnection()) {
                for (long number : new long[]{sender, receiver}) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM transactions WHERE account_number=? OR related_account=?")) {
                        statement.setLong(1, number);
                        statement.setLong(2, number);
                        statement.executeUpdate();
                    }
                }
                for (long number : new long[]{sender, receiver}) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM accounts WHERE account_number=?")) {
                        statement.setLong(1, number);
                        statement.executeUpdate();
                    }
                }
            }
        }
    }

    private static void balance(long number, String expected) throws Exception {
        check(dao.getAccount(number).getBalance().toPlainString().equals(expected + ".00"), "Balance " + expected);
    }

    private static void check(boolean result, String name) {
        if (!result) throw new AssertionError(name);
        passed++;
        System.out.println("PASS: " + name);
    }

    private static void rejects(Action action, String name) throws Exception {
        try {
            action.run();
            throw new AssertionError(name);
        } catch (IllegalArgumentException expected) {
            check(true, name);
        }
    }

    interface Action { void run() throws Exception; }
}
