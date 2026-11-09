-- Run this file in MySQL Workbench as root on the Windows laptop.
-- These credentials are for the local classroom demonstration.
CREATE DATABASE IF NOT EXISTS atm_bank CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'atm_user'@'localhost' IDENTIFIED BY 'ReniBankDemo123';
CREATE USER IF NOT EXISTS 'atm_user'@'127.0.0.1' IDENTIFIED BY 'ReniBankDemo123';
GRANT SELECT, INSERT, UPDATE, CREATE, REFERENCES, INDEX ON atm_bank.* TO 'atm_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, CREATE, REFERENCES, INDEX ON atm_bank.* TO 'atm_user'@'127.0.0.1';
CREATE DATABASE IF NOT EXISTS atm_bank_test CHARACTER SET utf8mb4;
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, REFERENCES, INDEX ON atm_bank_test.* TO 'atm_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, REFERENCES, INDEX ON atm_bank_test.* TO 'atm_user'@'127.0.0.1';
USE atm_bank;

-- The app runs this file inside the database selected in config.properties.
-- For manual setup: mysql -u USER -p DATABASE < sql/schema.sql

CREATE TABLE IF NOT EXISTS accounts (
    account_number BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(80) NOT NULL,
    account_type VARCHAR(10) NOT NULL CHECK (account_type IN ('Savings', 'Current')),
    pin_hash VARCHAR(100) NOT NULL,
    pin_salt VARCHAR(40) NOT NULL,
    balance DECIMAL(12,2) NOT NULL CHECK (balance >= 0),
    mobile_number VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB AUTO_INCREMENT=1001001;

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(12,2) NOT NULL CHECK (amount >= 0),
    related_account BIGINT NULL,
    balance_after DECIMAL(12,2) NOT NULL,
    transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number),
    FOREIGN KEY (related_account) REFERENCES accounts(account_number),
    INDEX account_history (account_number, transaction_id)
) ENGINE=InnoDB;

-- Demo rows: existing account and transaction IDs are preserved.
-- Includes Reni, Jabin and Sandhya with their transaction history.
-- mysql -u USER -p DATABASE < sql/demo_data.sql

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

INSERT IGNORE INTO `accounts` (`account_number`, `customer_name`, `account_type`, `pin_hash`, `pin_salt`, `balance`, `mobile_number`, `created_at`) VALUES (1001003,'Reni','Savings','kvWh3mCInvPBHOuBpudlt9XcbiuO78TC4EFX9Lrl3sw=','Zx+2CsOg/mOyYU6tNmI+mA==',27000.00,'9000000001','2026-11-05 16:38:44');
INSERT IGNORE INTO `accounts` (`account_number`, `customer_name`, `account_type`, `pin_hash`, `pin_salt`, `balance`, `mobile_number`, `created_at`) VALUES (1001005,'Jabin','Savings','LmzMdIiTxtob3Czly2feJZEkrq3e/h08c1ALcdveTVo=','nBUnQg28mV4JmrMOI5aHxw==',13000.00,'9000000003','2026-11-05 16:38:44');
INSERT IGNORE INTO `accounts` (`account_number`, `customer_name`, `account_type`, `pin_hash`, `pin_salt`, `balance`, `mobile_number`, `created_at`) VALUES (1001006,'Sandhya','Savings','k43estM8v6n/+UD36qV33XIu/qq4RCSmYWgvGd8l0L8=','GUZpsnrAb8JclmbAXdjBdw==',14500.00,'9000000004','2026-11-05 16:38:44');

INSERT IGNORE INTO `transactions` (`transaction_id`, `account_number`, `transaction_type`, `amount`, `related_account`, `balance_after`, `transaction_time`) VALUES (9,1001003,'OPENING',25000.00,NULL,25000.00,'2026-11-05 16:38:44');
INSERT IGNORE INTO `transactions` (`transaction_id`, `account_number`, `transaction_type`, `amount`, `related_account`, `balance_after`, `transaction_time`) VALUES (11,1001005,'OPENING',12000.00,NULL,12000.00,'2026-11-05 16:38:44');
INSERT IGNORE INTO `transactions` (`transaction_id`, `account_number`, `transaction_type`, `amount`, `related_account`, `balance_after`, `transaction_time`) VALUES (12,1001006,'OPENING',15000.00,NULL,15000.00,'2026-11-05 16:38:44');
INSERT IGNORE INTO `transactions` (`transaction_id`, `account_number`, `transaction_type`, `amount`, `related_account`, `balance_after`, `transaction_time`) VALUES (13,1001003,'DEPOSIT',5000.00,NULL,30000.00,'2026-11-05 16:38:44');
INSERT IGNORE INTO `transactions` (`transaction_id`, `account_number`, `transaction_type`, `amount`, `related_account`, `balance_after`, `transaction_time`) VALUES (14,1001003,'WITHDRAWAL',2000.00,NULL,28000.00,'2026-11-05 16:38:44');
INSERT IGNORE INTO `transactions` (`transaction_id`, `account_number`, `transaction_type`, `amount`, `related_account`, `balance_after`, `transaction_time`) VALUES (15,1001003,'TRANSFER_OUT',1500.00,NULL,26500.00,'2026-11-05 16:38:44');
INSERT IGNORE INTO `transactions` (`transaction_id`, `account_number`, `transaction_type`, `amount`, `related_account`, `balance_after`, `transaction_time`) VALUES (17,1001006,'TRANSFER_OUT',500.00,1001003,14500.00,'2026-11-05 16:38:44');
INSERT IGNORE INTO `transactions` (`transaction_id`, `account_number`, `transaction_type`, `amount`, `related_account`, `balance_after`, `transaction_time`) VALUES (18,1001003,'TRANSFER_IN',500.00,1001006,27000.00,'2026-11-05 16:38:44');
INSERT IGNORE INTO `transactions` (`transaction_id`, `account_number`, `transaction_type`, `amount`, `related_account`, `balance_after`, `transaction_time`) VALUES (19,1001005,'DEPOSIT',1000.00,NULL,13000.00,'2026-11-05 16:38:44');
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

