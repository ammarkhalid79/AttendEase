CREATE TABLE users (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 username VARCHAR(80) NOT NULL UNIQUE,
 email VARCHAR(160) NOT NULL UNIQUE,
 password_hash VARCHAR(100) NOT NULL,
 role VARCHAR(16) NOT NULL CHECK (role IN ('TEACHER','STUDENT')),
 active BOOLEAN NOT NULL
);
CREATE TABLE students (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 registration_number VARCHAR(40) NOT NULL UNIQUE,
 full_name VARCHAR(120) NOT NULL,
 email VARCHAR(160) NOT NULL UNIQUE,
 department VARCHAR(100) NOT NULL,
 semester INT NOT NULL CHECK (semester BETWEEN 1 AND 12),
 user_id BIGINT NOT NULL UNIQUE,
 FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE courses (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 course_code VARCHAR(30) NOT NULL UNIQUE,
 course_name VARCHAR(120) NOT NULL,
 description VARCHAR(1000) NOT NULL,
 teacher_id BIGINT NOT NULL,
 INDEX idx_course_teacher (teacher_id),
 FOREIGN KEY (teacher_id) REFERENCES users(id)
);
CREATE TABLE enrollments (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 student_id BIGINT NOT NULL,
 course_id BIGINT NOT NULL,
 UNIQUE (course_id, student_id),
 FOREIGN KEY (student_id) REFERENCES students(id),
 FOREIGN KEY (course_id) REFERENCES courses(id)
);
CREATE TABLE attendance_sessions (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 course_id BIGINT NOT NULL,
 session_date DATE NOT NULL,
 session_number INT NOT NULL CHECK (session_number > 0),
 UNIQUE (course_id, session_date, session_number),
 INDEX idx_session_date (session_date),
 FOREIGN KEY (course_id) REFERENCES courses(id)
);
CREATE TABLE attendance_records (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 session_id BIGINT NOT NULL,
 student_id BIGINT NOT NULL,
 status VARCHAR(12) NOT NULL CHECK (status IN ('PRESENT','ABSENT','LATE')),
 UNIQUE (session_id, student_id),
 FOREIGN KEY (student_id) REFERENCES students(id),
 FOREIGN KEY (session_id) REFERENCES attendance_sessions(id)
);
