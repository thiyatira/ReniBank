# Reni Bank

ATM Banking Management System using **Java Swing, JDBC and MySQL**.  
Thiyatira Reni N · RA2511003010637 · Advanced Programming Practice

Create an account, view details and balance, deposit, withdraw, transfer, show the latest ten transactions, and exit.

## Setup

1. Install **Java 17+** and **MySQL 8+**.
2. Download MySQL Connector/J and place its `.jar` in `lib/`.
3. Change the example password in `sql/setup_example.sql`, then run that file as a MySQL administrator.
4. Copy `config.example.properties` to `config.properties` and enter the matching database settings. Change the port if your MySQL uses 3306.
5. Run `sh run.sh` on macOS/Linux, or `run.bat` on Windows. The app creates its tables automatically.

Optional: after the tables exist, import the demo data into a new, empty database:

```sh
mysql -u atm_user -p -h 127.0.0.1 -P 3307 atm_bank < sql/demo_data.sql
```

Use the port from your configuration. Import the demo data only once.

| Customer | Account | PIN | Balance |
|---|---|---|---|
| Reni | 1001003 | 1234 | ₹27,000.00 |
| Madhav | 1001004 | 4321 | ₹19,500.00 |
| Jabin | 1001005 | 2468 | ₹13,000.00 |
| Sandhya | 1001006 | 1357 | ₹14,500.00 |

These are fictional demo accounts. `src/ATMApp.java` contains the interface; `src/Bank.java` contains the JDBC operations. Keep your local `config.properties` out of Git.
