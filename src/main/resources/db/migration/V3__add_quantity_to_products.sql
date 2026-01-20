-- Add quantity column to products table
ALTER TABLE products ADD COLUMN quantity INT NOT NULL DEFAULT 0;

-- Update existing products to have a default quantity of 1
UPDATE products SET quantity = 1 WHERE quantity = 0;
