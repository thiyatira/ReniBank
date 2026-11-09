# Reni Bank

Java console + JDBC + MySQL ATM project.
Thiyatira Reni N | RA2511003010637
Advanced Programming Practice
SRM Institute of Science and Technology, Kattankulathur

## Run

Requires JDK 17 or later and MySQL 8.4. These commands assume the database is set up and the Java files are compiled.

**1. Start MySQL** in one PowerShell window, if it is not already running:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" --defaults-file="C:\Users\N.Thiyatira Reni\mysql_data\my.ini" --console
```

**2. Start the app** in another PowerShell window:

```powershell
cd "C:\Users\N.Thiyatira Reni\Desktop\jdbc database\ReniBank"
java -cp "build;lib\*" Main
```

First use: run `sql/setup_windows.sql` in MySQL, copy `config.example.properties` to `config.properties`, and compile the Java files in `src` into `build`. Edit `config.properties` if your MySQL settings differ.

## Demo accounts

| Customer | Account | PIN | Balance |
|---|---|---|---|
| Reni | 1001003 | 1234 | INR 27,000.00 |
| Jabin | 1001005 | 2468 | INR 13,000.00 |
| Sandhya | 1001006 | 1357 | INR 14,500.00 |

Choose an option from 1 to 8. Account operations ask for the account number and PIN.

## Files

- `src/Account.java`, `src/Transaction.java`: data models.
- `src/ATMView.java`: keyboard input and printed output.
- `src/AccountDAO.java`: JDBC operations.
- `src/ATMController.java`: menu logic.
- `src/Main.java`: starts the application.
- `sql/setup_windows.sql`: database, tables and demo data.
- `lib/`: MySQL Connector/J and its license.

Source repository: https://github.com/thiyatira/ReniBank
