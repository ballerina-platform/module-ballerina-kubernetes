CREATE DATABASE cooldrinkdb;
USE cooldrinkdb;

CREATE TABLE cooldrink (
  id          INT AUTO_INCREMENT,
  name        VARCHAR(255),
  description VARCHAR(255),
  price       VARCHAR(255),
  PRIMARY KEY (id)
);

INSERT INTO cooldrink (name, description, price) VALUES ('Carbonara', 'Pizza with carbonara sauce', '$10.00');
INSERT INTO cooldrink (name, description, price) VALUES ('Chicken Hawaiian', 'Pizza with chicken and pineapple', '$15.00');
INSERT INTO cooldrink (name, description, price)
VALUES ('BBQ Chicken Bacon', 'Grilled Chicken with BBQ sauce and onion ', '$15.00');