-- For manual setup on another computer.
-- Replace ChangeThisLocalPassword before running as a MySQL administrator.
CREATE DATABASE IF NOT EXISTS atm_bank;
CREATE USER IF NOT EXISTS 'atm_user'@'127.0.0.1'
    IDENTIFIED BY 'ChangeThisLocalPassword';
GRANT SELECT, INSERT, UPDATE, CREATE, REFERENCES, INDEX
    ON atm_bank.* TO 'atm_user'@'127.0.0.1';

-- Table definitions are also applied automatically by the Java application.
USE atm_bank;
