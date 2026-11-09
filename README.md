# Reni Bank

Simple menu-driven Java console + JDBC + MySQL project for Windows.
**Thiyatira Reni N | RA2511003010637**
Advanced Programming Practice
SRM Institute of Science and Technology, Kattankulathur

## Windows setup

1. Install **JDK 17 or later**, **MySQL Server 8 or later**, and **MySQL Workbench**. Ensure Java's `bin` folder is in PATH and the MySQL service is running.
2. In MySQL Workbench, connect as root, open `sql/setup_windows.sql`, and execute the complete file. It creates the main database, four demo accounts and a separate empty test database.
3. Open PowerShell in this project folder and run:

```powershell
.\Start_Reni_Bank.bat
```

Choose an option from 1 to 8. Account operations ask for an account number and PIN.

The default MySQL port is **3306**. Edit `config.properties` if your settings differ. If `atm_user` already exists, use its existing password. Amounts are in Indian rupees.

| Customer | Account | PIN | Balance |
|---|---|---|---|
| Reni | 1001003 | 1234 | ₹27,000.00 |
| Madhav | 1001004 | 4321 | ₹19,500.00 |
| Jabin | 1001005 | 2468 | ₹13,000.00 |
| Sandhya | 1001006 | 1357 | ₹14,500.00 |

These are fictional demo accounts. Transactions persist in MySQL. Running setup again preserves existing account balances and transaction rows.

## Test

Run this in the same PowerShell window:

```powershell
.\Test_Reni_Bank.bat
```

It checks account creation, PIN validation, deposits, withdrawals, transfers and mini statements in `atm_bank_test`, without changing the four demo accounts. Expected result: **15 checks passed**. Edit `config.test.properties` if your MySQL settings differ.

## Code structure

- `src/Account.java`, `src/Transaction.java`: simple data models.
- `src/ATMView.java`: Scanner input and printed output.
- `src/AccountDAO.java`: JDBC queries and transaction handling.
- `src/ATMController.java`: connects the menu to the DAO.
- `src/Main.java`: starts the application.
- `lib/`: included MySQL Connector/J driver and license.
- `sql/`: table definitions, sample data and Windows setup.

Source repository: https://github.com/thiyatira/ReniBank
