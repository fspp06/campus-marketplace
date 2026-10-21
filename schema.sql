-- Campus Marketplace Database Schema
-- Compatible with MySQL 8.0+

CREATE DATABASE IF NOT EXISTS campus_marketplace
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campus_marketplace;

-- 1. Students / Users
CREATE TABLE IF NOT EXISTS students (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    student_id VARCHAR(50) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    year INT NOT NULL,
    phone_number VARCHAR(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Categories
CREATE TABLE IF NOT EXISTS categories (
    name VARCHAR(100) PRIMARY KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Populate default categories
INSERT IGNORE INTO categories (name) VALUES
    ('Books & Notes'),
    ('Electronics & Gadgets'),
    ('Lab & Engineering Tools'),
    ('Hostel & Dorm Essentials'),
    ('Fashion & Bags'),
    ('Sports & Fitness'),
    ('Other Essentials');

-- 3. Products
CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(36) PRIMARY KEY,
    seller_id VARCHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    price DOUBLE NOT NULL,
    description TEXT,
    category_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    image_bytes LONGBLOB,
    open_to_exchange BOOLEAN DEFAULT FALSE,
    exchange_preference VARCHAR(255) DEFAULT '',
    FOREIGN KEY (seller_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (category_name) REFERENCES categories(name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Orders
CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    buyer_id VARCHAR(36) NOT NULL,
    seller_id VARCHAR(36) NOT NULL,
    offered_product_id VARCHAR(36),
    meetup_notes VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PLACED',
    total DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (buyer_id) REFERENCES students(id),
    FOREIGN KEY (seller_id) REFERENCES students(id),
    FOREIGN KEY (offered_product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Reviews
CREATE TABLE IF NOT EXISTS reviews (
    id VARCHAR(36) PRIMARY KEY,
    seller_id VARCHAR(36) NOT NULL,
    reviewer_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    badges VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES students(id),
    FOREIGN KEY (reviewer_id) REFERENCES students(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Wishlist
CREATE TABLE IF NOT EXISTS wishlist (
    student_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (student_id, product_id),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
