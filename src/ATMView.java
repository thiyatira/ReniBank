import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

// View: only reads keyboard input and prints results.
public class ATMView {
    private final Scanner input = new Scanner(System.in);

    public void showMenu() {
        System.out.println("\n--- RENI BANK ---");
        System.out.println("1. Create new account");
        System.out.println("2. Display account details");
        System.out.println("3. Balance enquiry");
        System.out.println("4. Cash deposit");
        System.out.println("5. Cash withdrawal");
        System.out.println("6. Fund transfer");
        System.out.println("7. Mini statement");
        System.out.println("8. Exit");
    }

    public String read(String prompt) {
        System.out.print(prompt + ": ");
        if (!input.hasNextLine()) return null;
        return input.nextLine().trim();
    }

    public String required(String prompt) {
        String value = read(prompt);
        if (value == null) throw new IllegalStateException("Input closed.");
        return value;
    }

    public long readNumber(String prompt) {
        try {
            long number = Long.parseLong(required(prompt));
            if (number > 0) return number;
        } catch (NumberFormatException e) { }
        throw new IllegalArgumentException("Enter a valid account number.");
    }

    public void showAccount(Account account) {
        System.out.println("\nAccount number: " + account.getNumber());
        System.out.println("Customer name: " + account.getName());
        System.out.println("Account type: " + account.getType());
        System.out.println("Mobile number: " + account.getMobile());
        System.out.println("PIN: ****");
        showBalance(account.getBalance());
    }

    public void showBalance(BigDecimal balance) {
        System.out.printf("Balance: INR %,.2f%n", balance);
    }

    public void showStatement(List<Transaction> transactions) {
        System.out.println("\nDATE AND TIME | TYPE | AMOUNT | OTHER ACCOUNT | BALANCE");
        for (Transaction item : transactions) {
            System.out.printf("%s | %s | INR %,.2f | %s | INR %,.2f%n",
                    item.getTime(), item.getType(), item.getAmount(),
                    item.getOtherAccount() == null ? "-" : item.getOtherAccount(),
                    item.getBalance());
        }
    }

    public void message(String text) {
        System.out.println(text);
    }
}
