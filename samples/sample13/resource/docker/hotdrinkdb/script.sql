CREATE DATABASE hotdrinkdb;
USE hotdrinkdb;

CREATE TABLE hotdrink (
  id          INT AUTO_INCREMENT,
  name        VARCHAR(255),
  description VARCHAR(255),
  price       DOUBLE,
  PRIMARY KEY (id)
);

INSERT INTO hotdrink (name, description, price) VALUES ('Espresso', '1 Shot of espresso in an espresso cup', 5.00);
INSERT INTO hotdrink (name, description, price) VALUES ('Cappuccino', 'Steamed milk, micro-foam & Sprinkle chocolate on top of the coffee', 6.00);
INSERT INTO hotdrink (name, description, price) VALUES ('Flat White', 'espresso & steamed milk', 3.00);