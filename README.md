# Reni Bank

Java Swing + JDBC + MySQL ATM project for Windows.  
**Thiyatira Reni N | RA2511003010637**  
Advanced Programming Practice  
SRM Institute of Science and Technology, Kattankulathur

## Windows setup

1. Install **JDK 17 or later**, **MySQL Server 8 or later**, and **MySQL Workbench**. Ensure Java's `bin` folder is in PATH and the MySQL service is running.
2. In MySQL Workbench, connect as root, open `sql/setup_windows.sql`, and execute the complete file once. It creates the database and the four demo accounts.
3. Double-click **Start_Reni_Bank.bat**. It copies the example configuration if needed, compiles the Java files, and opens the app.

The default MySQL port is **3306**. If yours differs, change it in `config.properties`. The example configuration and setup SQL use the same local demo password. If you change that password, update both files. If `atm_user` already exists, use its password in the configuration.

| Customer | Account | PIN | Balance |
|---|---|---|---|
| Reni | 1001003 | 1234 | ₹27,000.00 |
| Madhav | 1001004 | 4321 | ₹19,500.00 |
| Jabin | 1001005 | 2468 | ₹13,000.00 |
| Sandhya | 1001006 | 1357 | ₹14,500.00 |

These are fictional demo accounts. Import the setup SQL only into a new database; transactions persist in MySQL.

## Files

- `src/ATMApp.java`: menu and forms for all eight operations.
- `src/Bank.java`: validation, PIN checks and JDBC operations.
- `lib/`: included MySQL Connector/J driver and license.
- `sql/`: table definitions, sample data and Windows setup.

Source repository: https://github.com/thiyatira/ReniBank
