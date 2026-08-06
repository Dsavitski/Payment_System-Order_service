DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM items;

INSERT INTO items(name, price, created_at, updated_at)
VALUES ('Phone', 1000.00, now(), now());