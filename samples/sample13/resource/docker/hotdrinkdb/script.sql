CREATE DATABASE hotdrinkdb;
USE hotdrinkdb;

CREATE TABLE hotdrink (
  id          INT AUTO_INCREMENT,
  name        VARCHAR(255),
  description VARCHAR(255),
  price       VARCHAR(255),
  PRIMARY KEY (id)
);

INSERT INTO hotdrink (name, description, price) VALUES ('BBQ Chicken', 'Burger with BBQ Chicken & sauce', '$15.00');
INSERT INTO hotdrink (name, description, price) VALUES ('Hamburger', 'Hamburger with onions', '$15.00');
INSERT INTO hotdrink (name, description, price)
VALUES ('Veggie Delight', 'Veggie Burger with onions,carrot,cheese & cucumber', '$12.00');