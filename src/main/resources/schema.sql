DROP TABLE IF EXISTS users, items, requests, bookings, comments CASCADE;
DROP TYPE IF EXISTS booking_status;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(100) NOT NULL,
    email varchar(320) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    description varchar(500),
    requester_id BIGINT REFERENCES users(id),
    created TIMESTAMP
);

CREATE TABLE IF NOT EXISTS items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(100),
    description varchar(500),
    is_available boolean,
    owner_id BIGINT REFERENCES users(id),
    request_id BIGINT REFERENCES requests(id)
);

--CREATE TYPE booking_status AS ENUM ('WAITING', 'APPROVED', 'REJECTED', 'CANCELED');

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    item_id BIGINT REFERENCES items(id),
    booker_id BIGINT REFERENCES users(id),
    status int
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    text varchar(1000),
    item_id BIGINT REFERENCES items(id),
    author_id BIGINT REFERENCES users(id),
    created TIMESTAMP
);