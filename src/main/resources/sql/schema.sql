-- ООО «Обувь» — схема базы данных
-- Кодировка: UTF-8
-- СУБД: PostgreSQL
-- Нормальная форма: 3НФ

-- Удаление существующих таблиц (для повторного запуска скрипта)
DROP TABLE IF EXISTS order_items    CASCADE;
DROP TABLE IF EXISTS orders         CASCADE;
DROP TABLE IF EXISTS order_statuses CASCADE;
DROP TABLE IF EXISTS products       CASCADE;
DROP TABLE IF EXISTS units          CASCADE;
DROP TABLE IF EXISTS suppliers      CASCADE;
DROP TABLE IF EXISTS manufacturers  CASCADE;
DROP TABLE IF EXISTS categories     CASCADE;
DROP TABLE IF EXISTS users          CASCADE;
DROP TABLE IF EXISTS roles          CASCADE;

-- Роли пользователей
CREATE TABLE roles (
    id   SERIAL      PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Пользователи системы
CREATE TABLE users (
    id            SERIAL       PRIMARY KEY,
    login         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(64)  NOT NULL,          -- SHA-256 hex
    role_id       INTEGER      NOT NULL REFERENCES roles(id),
    last_name     VARCHAR(100) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    middle_name   VARCHAR(100)                    -- отчество (может быть NULL)
);

-- Категории обуви
CREATE TABLE categories (
    id   SERIAL       PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Производители обуви
CREATE TABLE manufacturers (
    id   SERIAL       PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE
);

-- Поставщики
CREATE TABLE suppliers (
    id   SERIAL       PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE
);

-- Единицы измерения (например: пара, штука)
CREATE TABLE units (
    id   SERIAL      PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Товары (обувь)
CREATE TABLE products (
    id              SERIAL         PRIMARY KEY,
    name            VARCHAR(200)   NOT NULL,
    category_id     INTEGER        NOT NULL REFERENCES categories(id),
    manufacturer_id INTEGER        NOT NULL REFERENCES manufacturers(id),
    supplier_id     INTEGER        NOT NULL REFERENCES suppliers(id),
    unit_id         INTEGER        NOT NULL REFERENCES units(id),
    description     TEXT,
    price           NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    discount        NUMERIC(5, 2)  NOT NULL DEFAULT 0
                                   CHECK (discount >= 0 AND discount <= 100),
    image_path      VARCHAR(500),                 -- путь к файлу изображения
    stock_quantity  INTEGER        NOT NULL DEFAULT 0
                                   CHECK (stock_quantity >= 0)
);

-- Статусы заказов
CREATE TABLE order_statuses (
    id   SERIAL       PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Заказы
CREATE TABLE orders (
    id             SERIAL       PRIMARY KEY,
    article        VARCHAR(100) NOT NULL UNIQUE,  -- артикул заказа
    user_id        INTEGER      REFERENCES users(id),
    status_id      INTEGER      NOT NULL REFERENCES order_statuses(id),
    pickup_address TEXT         NOT NULL,
    order_date     DATE         NOT NULL,
    delivery_date  DATE
);

-- Позиции заказа
CREATE TABLE order_items (
    id             SERIAL         PRIMARY KEY,
    order_id       INTEGER        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id     INTEGER        NOT NULL REFERENCES products(id),
    quantity       INTEGER        NOT NULL CHECK (quantity > 0),
    price_at_order NUMERIC(10, 2) NOT NULL CHECK (price_at_order >= 0)
);

-- ============================================================
-- Начальные данные
-- ============================================================

INSERT INTO roles (name) VALUES
    ('ADMIN'),
    ('MANAGER'),
    ('CLIENT');

-- Пароли (plaintext → SHA-256 hex):
--   admin    → admin123
--   manager  → manager123
--   client   → client123
INSERT INTO users (login, password_hash, role_id, last_name, first_name, middle_name)
VALUES
    ('admin',
     encode(sha256('admin123'::bytea), 'hex'),
     1, 'Иванов', 'Иван', 'Иванович'),
    ('manager',
     encode(sha256('manager123'::bytea), 'hex'),
     2, 'Петрова', 'Мария', 'Сергеевна'),
    ('client',
     encode(sha256('client123'::bytea), 'hex'),
     3, 'Сидоров', 'Алексей', 'Петрович');

INSERT INTO categories (name) VALUES
    ('Мужская обувь'),
    ('Женская обувь'),
    ('Детская обувь'),
    ('Спортивная обувь'),
    ('Сапоги и ботинки');

INSERT INTO manufacturers (name) VALUES
    ('Ralf Ringer'),
    ('Ecco'),
    ('Nike'),
    ('Adidas'),
    ('Котофей');

INSERT INTO suppliers (name) VALUES
    ('ООО ТоргОбувь'),
    ('ИП Смирнов А.В.'),
    ('АО СпортТрейд'),
    ('ООО Детский мир');

INSERT INTO units (name) VALUES
    ('пара'),
    ('штука');

INSERT INTO order_statuses (name) VALUES
    ('Новый'),
    ('В обработке'),
    ('Отправлен'),
    ('Доставлен'),
    ('Отменён');

INSERT INTO products
    (name, category_id, manufacturer_id, supplier_id, unit_id,
     description, price, discount, stock_quantity)
VALUES
    ('Туфли классические мужские', 1, 1, 1, 1,
     'Классические кожаные туфли на кожаной подошве. Идеальны для офиса.',
     4500.00, 0, 12),
    ('Кроссовки беговые мужские', 4, 3, 3, 1,
     'Лёгкие беговые кроссовки с амортизирующей подошвой.',
     6990.00, 10, 25),
    ('Туфли женские на каблуке', 2, 2, 1, 1,
     'Элегантные женские туфли из натуральной кожи.',
     5200.00, 20, 8),
    ('Ботинки зимние мужские', 5, 1, 2, 1,
     'Утеплённые зимние ботинки с противоскользящей подошвой.',
     7800.00, 0, 0),
    ('Кеды детские', 3, 5, 4, 1,
     'Яркие детские кеды с липучками. Размеры 28-35.',
     1990.00, 5, 40),
    ('Кроссовки женские', 4, 4, 3, 1,
     'Стильные женские кроссовки для повседневной носки.',
     5500.00, 18, 15),
    ('Сапоги женские зимние', 5, 2, 2, 1,
     'Высокие зимние сапоги на натуральном меху.',
     9900.00, 0, 3),
    ('Балетки женские', 2, 2, 1, 1,
     'Удобные балетки из замши. Подходят для офиса и прогулок.',
     2800.00, 25, 0);

INSERT INTO orders (article, user_id, status_id, pickup_address, order_date, delivery_date)
VALUES
    ('ORD-2026-001', 3, 2, 'г. Москва, ул. Ленина, 10, ПВЗ №1',
     '2026-04-01', '2026-04-05'),
    ('ORD-2026-002', 3, 1, 'г. Москва, пр. Мира, 22, ПВЗ №3',
     '2026-04-03', NULL),
    ('ORD-2026-003', NULL, 3, 'г. Санкт-Петербург, ул. Садовая, 5, ПВЗ №7',
     '2026-03-28', '2026-04-06');

INSERT INTO order_items (order_id, product_id, quantity, price_at_order)
VALUES
    (1, 2, 1, 6990.00),
    (1, 5, 2, 1990.00),
    (2, 3, 1, 5200.00),
    (3, 1, 1, 4500.00),
    (3, 7, 1, 9900.00);
