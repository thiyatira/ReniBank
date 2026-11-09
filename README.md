# Reni Bank

Simple menu-driven Java console + JDBC + MySQL project for Windows.
**Thiyatira Reni N | RA2511003010637**
Advanced Programming Practice
SRM Institute of Science and Technology, Kattankulathur

## Windows setup and run

Use PowerShell with JDK 17 or later in PATH. If MySQL is not already running, start it in a separate PowerShell window and leave that window open:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" --defaults-file="C:\Users\N.Thiyatira Reni\mysql_data\my.ini" --console
```

In another PowerShell window, open the project folder:

```powershell
cd "C:\Users\N.Thiyatira Reni\Desktop\jdbc database\ReniBank"
```

For first-time setup, run the SQL file and enter your MySQL root password when prompted. This creates the main database, three demo accounts and an empty test database:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p -e "source sql/setup_windows.sql"
```

Compile and run the application:

```powershell
if (!(Test-Path config.properties)) { Copy-Item config.example.properties config.properties }
New-Item -ItemType Directory -Force build | Out-Null
javac --release 17 -encoding UTF-8 -cp "lib\*" -d build (Get-ChildItem src\*.java).FullName
java -cp "build;lib\*" Main
```

After compiling, start it again with just:

```powershell
java -cp "build;lib\*" Main
```

The console application's entry class is `Main`. Choose an option from 1 to 8. Account operations ask for an account number and PIN.

The default MySQL port is **3306**. Edit `config.properties` if your settings differ. If `atm_user` already exists, use its existing password. Amounts are in Indian rupees.

| Customer | Account | PIN | Balance |
|---|---|---|---|
| Reni | 1001003 | 1234 | ₹27,000.00 |
| Jabin | 1001005 | 2468 | ₹13,000.00 |
| Sandhya | 1001006 | 1357 | ₹14,500.00 |

These are fictional demo accounts. Transactions persist in MySQL. Running setup again preserves existing account balances and transaction rows.

## Test

From the same project folder, with MySQL running:

```powershell
if (!(Test-Path config.test.properties)) { Copy-Item config.test.example.properties config.test.properties }
New-Item -ItemType Directory -Force build | Out-Null
javac --release 17 -encoding UTF-8 -cp "build;lib\*" -sourcepath src -d build tests\BankTest.java
java "-Dconfig=config.test.properties" -cp "build;lib\*" BankTest
```

It checks account creation, PIN validation, deposits, withdrawals, transfers and mini statements in `atm_bank_test`, without changing the three demo accounts. Expected result: **15 checks passed**. Edit `config.test.properties` if your MySQL settings differ.

## Code structure

- `src/Account.java`, `src/Transaction.java`: simple data models.
- `src/ATMView.java`: Scanner input and printed output.
- `src/AccountDAO.java`: JDBC queries and transaction handling.
- `src/ATMController.java`: connects the menu to the DAO.
- `src/Main.java`: starts the application.
- `lib/`: included MySQL Connector/J driver and license.
- `sql/`: table definitions, sample data and Windows setup.

Source repository: https://github.com/thiyatira/ReniBank
