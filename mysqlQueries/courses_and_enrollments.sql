CREATE DATABASE studentdb;

USE studentdb;

CREATE TABLE students (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    email VARCHAR(100),
    password VARCHAR(100),
    phone VARCHAR(15),
    gender VARCHAR(20),
    skills VARCHAR(200),
    country VARCHAR(50),
    age INT,
    address TEXT,
    bio TEXT
);

USE studentdb;

SELECT * FROM students;

USE studentdb;

CREATE TABLE IF NOT EXISTS courses (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    fees        DECIMAL(10,2) NOT NULL,
    duration    VARCHAR(50)  NOT NULL
);

CREATE TABLE IF NOT EXISTS enrollments (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    student_id  INT NOT NULL,
    course_id   INT NOT NULL,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id)  REFERENCES courses(id)  ON DELETE CASCADE,
    UNIQUE KEY  uq_enroll (student_id, course_id)
);

INSERT INTO courses (course_name, fees, duration) VALUES
('Java Programming',      8000.00, '3 Months'),
('Python Programming',    7500.00, '3 Months'),
('Web Development',      10000.00, '4 Months'),
('AI / Machine Learning',15000.00, '6 Months'),
('Data Science',         12000.00, '5 Months'),
('Android Development',   9000.00, '4 Months');

USE studentdb;

