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
            try {
                view.showMenu();
                String choice = view.read("Choose an option");
                if (choice.equals("8")) return;
                if (choice.equals("1")) {
                    createAccount();
                    continue;
                }
                if (!choice.matches("[2-7]")) {
                    view.message("Choose an option from 1 to 8.");
                    continue;
                }
                long number = view.readNumber("Account number");
                Account account = dao.login(number, view.read("PIN"));
                switch (choice) {
                    case "2": view.showAccount(account); break;
                    case "3": view.showBalance(account.getBalance()); break;
                    case "4": view.showBalance(dao.deposit(number, view.read("Deposit amount in INR"))); break;
                    case "5": view.showBalance(dao.withdraw(number, view.read("Withdrawal amount in INR"))); break;
                    case "6":
                        long recipient = view.readNumber("Recipient account number");
                        view.showBalance(dao.transfer(number, recipient, view.read("Transfer amount in INR")));
                        break;
                    case "7": view.showStatement(dao.statement(number)); break;
                }
            } catch (IllegalStateException e) {
                return; // Input was closed.
            } catch (Exception e) {
                view.message(e.getMessage());
            }
        }
    }

    private void createAccount() throws Exception {
        String name = view.read("Customer name");
        String type = view.read("Account type (Savings/Current)");
        String pin = view.read("PIN (4 digits)");
        String mobile = view.read("Mobile number (10 digits)");
        String deposit = view.read("Initial deposit in INR");
        long number = dao.createAccount(name, type, pin, mobile, deposit);
        view.message("Account created. Account number: " + number);
    }
}
