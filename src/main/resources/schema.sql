-- Таблица товаров (items)
CREATE TABLE IF NOT EXISTS items (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     title VARCHAR(255) NOT NULL,
                                     description VARCHAR(500),
                                     image VARCHAR(255),
                                     price DOUBLE NOT NULL,
                                     count INT NOT NULL
);

-- Таблица заказов (orders)
CREATE TABLE IF NOT EXISTS orders (
                                      id VARCHAR(36) PRIMARY KEY,
                                      order_date TIMESTAMP NOT NULL
);

-- Таблица элементов заказа (order_items)
CREATE TABLE IF NOT EXISTS order_items (
                                           id VARCHAR(36) PRIMARY KEY,
                                           item_id BIGINT NOT NULL,
                                           quantity INT NOT NULL,
                                           order_id VARCHAR(36) NOT NULL,
                                           FOREIGN KEY (item_id) REFERENCES items(id),
                                           FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Последовательность для товаров
CREATE SEQUENCE IF NOT EXISTS item_sequence START WITH 1 INCREMENT BY 1;
