import java.math.BigDecimal;
import java.sql.Timestamp;

// Model: holds one transaction from the mini statement.
public class Transaction {
    private final Timestamp time;
    private final String type;
    private final BigDecimal amount, balance;
    private final Long otherAccount;

    public Transaction(Timestamp time, String type, BigDecimal amount,
                       Long otherAccount, BigDecimal balance) {
        this.time = time;
        this.type = type;
        this.amount = amount;
        this.otherAccount = otherAccount;
        this.balance = balance;
    }

    public Timestamp getTime() { return time; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public Long getOtherAccount() { return otherAccount; }
    public BigDecimal getBalance() { return balance; }
}
