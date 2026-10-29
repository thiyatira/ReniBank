-- Run this as a MySQL administrator, then set config.properties for atm_user.
-- The application also creates these two tables when first started.
CREATE DATABASE IF NOT EXISTS atm_bank;
USE atm_bank;

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
