import java.math.BigDecimal;

// Model: holds one customer's account details.
public class Account {
    private final long number;
    private final String name, type, mobile;
    private final BigDecimal balance;

    public Account(long number, String name, String type, BigDecimal balance, String mobile) {
        this.number = number;
        this.name = name;
        this.type = type;
        this.balance = balance;
        this.mobile = mobile;
    }

    public long getNumber() { return number; }
    public String getName() { return name; }
    public String getType() { return type; }
    public BigDecimal getBalance() { return balance; }
    public String getMobile() { return mobile; }
}
