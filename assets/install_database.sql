CREATE USER IF NOT EXISTS hclus_user IDENTIFIED BY "";
CREATE DATABASE IF NOT EXISTS hclus_db;

USE hclus_db;

CREATE TABLE IF NOT EXISTS examples_1(X1 FLOAT, X2 FLOAT, X3 FLOAT);
CREATE TABLE IF NOT EXISTS empty_examples(X1 FLOAT, X2 FLOAT, X3 FLOAT);
CREATE TABLE IF NOT EXISTS no_numbers(description VARCHAR(32), value FLOAT);

INSERT INTO examples_1 VALUES (1, 2, 0), (0, 1, -1), (1, 3, 5), (1, 3, 4), (2, 2, 0);
INSERT INTO no_numbers VALUES ("a", 1), ("b", 2), ("c", 3);

GRANT ALL PRIVILEGES ON hclus_db.* TO hclus_user;
