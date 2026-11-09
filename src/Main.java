// Main runner: creates the MVC objects and starts the menu.
public class Main {
    public static void main(String[] args) {
        try {
            AccountDAO dao = new AccountDAO();
            new ATMController(dao, new ATMView()).run();
        } catch (Exception e) {
            System.err.println("Cannot start Reni Bank: " + e.getMessage());
            System.err.println("Check MySQL and config.properties.");
            System.exit(1);
        }
    }
}
