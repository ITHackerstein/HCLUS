CREATE USER IF NOT EXISTS hclus_user IDENTIFIED BY "";
CREATE DATABASE IF NOT EXISTS hclus_db;

USE hclus_db;

CREATE TABLE IF NOT EXISTS examples_1(X1 FLOAT, X2 FLOAT, X3 FLOAT);
CREATE TABLE IF NOT EXISTS examples_2(X1 FLOAT, X2 FLOAT, X3 FLOAT);
CREATE TABLE IF NOT EXISTS empty_examples(X1 FLOAT, X2 FLOAT, X3 FLOAT);
CREATE TABLE IF NOT EXISTS no_numbers(description VARCHAR(32), value FLOAT);
CREATE TABLE IF NOT EXISTS iris(sepal_length DOUBLE, sepal_width DOUBLE, petal_length DOUBLE, petal_width DOUBLE, species VARCHAR(32));

INSERT INTO examples_1 VALUES (1, 2, 0), (0, 1, -1), (1, 3, 5), (1, 3, 4), (2, 2, 0);
INSERT INTO examples_2 VALUES (1, 2, 0), (0, 1, -1), (1, 3, 5);
INSERT INTO no_numbers VALUES ("a", 1), ("b", 2), ("c", 3);

SET GLOBAL local_infile = 1;
LOAD DATA LOCAL INFILE 'assets/iris.csv' INTO TABLE iris FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
ALTER TABLE iris DROP COLUMN species;

GRANT ALL PRIVILEGES ON hclus_db.* TO hclus_user;
