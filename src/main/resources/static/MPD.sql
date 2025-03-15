CREATE DATABASE PayMyBuddy;

use PayMyBuddy;
CREATE TABLE users (
             id INT AUTO_INCREMENT PRIMARY KEY,
             username VARCHAR(255) NOT NULL UNIQUE,
             email VARCHAR(255) NOT NULL UNIQUE,
             password VARCHAR(255) NOT NULL
);

CREATE TABLE transactions (
             id INT AUTO_INCREMENT PRIMARY KEY,
             sender_id INT NOT NULL,
             receiver_id INT NOT NULL,
             description TEXT NULL,
             amount DECIMAL(10,2) NOT NULL,
             FOREIGN KEY (sender_id) REFERENCES users(id),
             FOREIGN KEY (receiver_id) REFERENCES users(id)
);

CREATE TABLE user_connections (
             user_id INT NOT NULL,
             connection_id INT NOT NULL,
             PRIMARY KEY (user_id, connection_id),
             FOREIGN KEY (user_id) REFERENCES users(id),
             FOREIGN KEY (connection_id) REFERENCES users(id)
);