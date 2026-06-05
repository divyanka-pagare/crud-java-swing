USE studentdb;

CREATE TABLE IF NOT EXISTS fee_payments (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    student_id     INT NOT NULL,
    total_fees     DECIMAL(10,2) NOT NULL,
    discount_amt   DECIMAL(10,2) DEFAULT 0.00,
    amount_paid    DECIMAL(10,2) NOT NULL,
    payment_mode   VARCHAR(20)  NOT NULL,
    payment_status VARCHAR(20)  DEFAULT 'Paid',
    paid_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

USE studentdb;

CREATE TABLE IF NOT EXISTS fee_payment_courses (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    fee_payment_id INT NOT NULL,
    student_id     INT NOT NULL,
    course_id      INT NOT NULL,
    FOREIGN KEY (fee_payment_id) REFERENCES fee_payments(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id)     REFERENCES students(id)     ON DELETE CASCADE,
    FOREIGN KEY (course_id)      REFERENCES courses(id)      ON DELETE CASCADE
);

USE studentdb;

-- add course tracking to fee_payments table
ALTER TABLE fee_payments ADD COLUMN IF NOT EXISTS courses_paid TEXT;

-- verify
DESCRIBE fee_payments;