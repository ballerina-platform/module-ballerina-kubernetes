CREATE DATABASE cooldrinkdb;
USE cooldrinkdb;

CREATE TABLE cooldrink (
  id          INT AUTO_INCREMENT,
  name        VARCHAR(255),
  description VARCHAR(255),
  price       DOUBLE,
  PRIMARY KEY (id)
);

INSERT INTO cooldrink (name, description, price) VALUES ('Lime Soda', 'Sparkling Soda with Lime', 10.00);
INSERT INTO cooldrink (name, description, price) VALUES ('Mango Juice', 'Fresh Mango Juice with milk', 15.00);
INSERT INTO cooldrink (name, description, price)
VALUES ('Mojito', 'White rum, sugar, lime juice, soda water, and mint. ', 20.00);