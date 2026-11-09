import java.sql.SQLException;

// Controller: connects the console view to the JDBC DAO.
public class ATMController {
    private final AccountDAO dao;
    private final ATMView view;

    public ATMController(AccountDAO dao, ATMView view) {
        this.dao = dao;
        this.view = view;
    }

    public void run() {
        while (true) {
            view.showMenu();
            String choice = view.read("Choose an option");
            if (choice == null || choice.equals("8")) {
                view.message("Thank you for using Reni Bank.");
                return;
            }
            try {
                switch (choice) {
                    case "1": createAccount(); break;
                    case "2": view.showAccount(verifyAccount()); break;
                    case "3": view.showBalance(verifyAccount().getBalance()); break;
                    case "4": deposit(); break;
                    case "5": withdraw(); break;
                    case "6": transfer(); break;
                    case "7": view.showStatement(dao.statement(verifyAccount().getNumber())); break;
                    default: view.message("Choose an option from 1 to 8.");
                }
            } catch (IllegalStateException e) {
                view.message(e.getMessage());
                return;
            } catch (SQLException e) {
                view.message("Database error: " + e.getMessage());
            } catch (Exception e) {
                view.message(e.getMessage());
            }
        }
    }

    private Account verifyAccount() throws Exception {
        long number = view.readNumber("Account number");
        return dao.login(number, view.required("PIN"));
    }

    private void createAccount() throws Exception {
        String name = view.required("Customer name");
        String type = view.required("Account type (Savings/Current)");
        String pin = view.required("PIN (4 digits)");
        String mobile = view.required("Mobile number (10 digits)");
        String deposit = view.required("Initial deposit in INR");
        long number = dao.createAccount(name, type, pin, mobile, deposit);
        view.message("Account created. Account number: " + number);
    }

    private void deposit() throws Exception {
        Account account = verifyAccount();
        String amount = view.required("Deposit amount in INR");
        view.showBalance(dao.deposit(account.getNumber(), amount));
    }

    private void withdraw() throws Exception {
        Account account = verifyAccount();
        String amount = view.required("Withdrawal amount in INR");
        view.showBalance(dao.withdraw(account.getNumber(), amount));
    }

    private void transfer() throws Exception {
        Account account = verifyAccount();
        long recipient = view.readNumber("Recipient account number");
        String amount = view.required("Transfer amount in INR");
        view.showBalance(dao.transfer(account.getNumber(), recipient, amount));
    }
}
