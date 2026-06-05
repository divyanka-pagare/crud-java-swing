SELECT SUM(amount_paid)
FROM fee_payments;

SELECT SUM(fp.amount_paid)
FROM fee_payments fp
LEFT JOIN fee_payment_courses fpc
ON fpc.fee_payment_id = fp.id;

DESC fee_payment_courses;

USE studentdb;

CREATE TABLE IF NOT EXISTS attendance (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    student_id      INT NOT NULL,
    course_id       INT NOT NULL,
    attendance_date DATE NOT NULL,
    status          ENUM('Present','Absent','Late') NOT NULL DEFAULT 'Present',
    remarks         VARCHAR(255),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id)  REFERENCES courses(id)  ON DELETE CASCADE,
    UNIQUE KEY uq_attendance (student_id, course_id, attendance_date)
);