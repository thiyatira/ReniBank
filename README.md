# Reni Bank

Java console + JDBC + MySQL ATM project.
Thiyatira Reni N | RA2511003010637
Advanced Programming Practice
SRM Institute of Science and Technology, Kattankulathur

## Requirements

- JDK 17 or later
- MySQL 8.4

The MySQL driver is already included in `lib/`.

## Run (Windows PowerShell)

**1. Start MySQL** in one PowerShell window, if it is not already running:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" --defaults-file="C:\Users\N.Thiyatira Reni\mysql_data\my.ini" --console
```

**2. Start the app** in another PowerShell window:

```powershell
cd "C:\Users\N.Thiyatira Reni\Desktop\jdbc database\ReniBank"
java -cp "build;lib\*" Main
```

First use only, before step 2 (from the project folder):

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p -e "source setup.sql"
javac -d build src\Main.java
```

## Demo accounts

| Customer | Account | PIN | Balance |
|---|---|---|---|
| Reni | 1001003 | 1234 | INR 27,000.00 |
| Jabin | 1001005 | 2468 | INR 13,000.00 |
| Sandhya | 1001006 | 1357 | INR 14,500.00 |

Choose an option from 1 to 8. Account operations ask for the account number and PIN.

## Files

- `src/Main.java`: the whole app (menu, input/output, database code).
- `setup.sql`: database, tables and demo accounts.
- `lib/`: MySQL Connector/J driver and its license.

If your MySQL does not use port 3306, change `URL` at the top of `AccountDAO` in `src/Main.java`.

Source repository: https://github.com/thiyatira/ReniBank
