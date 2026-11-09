# Reni Bank

Java console + JDBC + MySQL ATM project.
Thiyatira Reni N | RA2511003010637
Advanced Programming Practice
SRM Institute of Science and Technology, Kattankulathur

## Run with two PowerShell commands

Requires JDK 17 or later in PATH and MySQL 8.4 installed at the paths below.

**1. Start MySQL** in one PowerShell window, if it is not already running. Leave this window open:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" --defaults-file="C:\Users\N.Thiyatira Reni\mysql_data\my.ini" --console
```

**2. Start the app** by pasting this whole command into another PowerShell window:

```powershell
& {
    Set-Location "C:\Users\N.Thiyatira Reni\Desktop\jdbc database\ReniBank"
    if (!(Test-Path config.properties)) {
        & "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p -e "source sql/setup_windows.sql"
        if ($LASTEXITCODE -ne 0) { return }
        Copy-Item config.example.properties config.properties
    }
    New-Item -ItemType Directory -Force build | Out-Null
    javac --release 17 -encoding UTF-8 -cp "lib\*" -d build (Get-ChildItem src\*.java).FullName
    if ($LASTEXITCODE -eq 0) { java -cp "build;lib\*" Main }
}
```

On first use, enter your MySQL root password when prompted. Setup creates the database and demo accounts. Later runs compile and start the app. If your MySQL port or login differs, edit `config.properties`.

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
