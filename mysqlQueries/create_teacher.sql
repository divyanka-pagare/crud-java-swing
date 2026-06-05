USE studentdb;

CREATE TABLE IF NOT EXISTS teachers (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    experience        INT NOT NULL,
    specialization    VARCHAR(100) NOT NULL,
    available_time    VARCHAR(100) NOT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

