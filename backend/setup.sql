-- Create database and select it
CREATE DATABASE IF NOT EXISTS test;
USE test;

-- ===== Schema =====
-- Users and profile
CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NULL,
  full_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  admission_no VARCHAR(100) NULL
);

-- Zoom rooms (virtual campus)
CREATE TABLE IF NOT EXISTS zoom_rooms (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_code VARCHAR(50) NOT NULL,
  course_title VARCHAR(255) NOT NULL,
  start_time DATETIME NOT NULL,
  join_url VARCHAR(1024) NOT NULL
);

-- Attendance per room
CREATE TABLE IF NOT EXISTS attendance (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  room_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  attended_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (room_id) REFERENCES zoom_rooms(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Academics: course registration
CREATE TABLE IF NOT EXISTS course_registration (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  course_code VARCHAR(50) NOT NULL,
  course_name VARCHAR(255) NOT NULL,
  credits INT NOT NULL,
  semester VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Academics: course work
CREATE TABLE IF NOT EXISTS course_work (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  course_code VARCHAR(50) NOT NULL,
  assignment VARCHAR(255) NOT NULL,
  marks INT NOT NULL,
  max_marks INT NOT NULL,
  due_date DATE NOT NULL,
  status VARCHAR(50) NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Academics: exam card
CREATE TABLE IF NOT EXISTS exam_card (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  course_code VARCHAR(50) NOT NULL,
  course_name VARCHAR(255) NOT NULL,
  exam_date DATE NULL,
  venue VARCHAR(255) NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Academics: exam audit
CREATE TABLE IF NOT EXISTS exam_audit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  course_code VARCHAR(50) NOT NULL,
  audit_note VARCHAR(255) NOT NULL,
  status VARCHAR(50) NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Academics: exam result
CREATE TABLE IF NOT EXISTS exam_result (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  course_code VARCHAR(50) NOT NULL,
  exam_type VARCHAR(50) NOT NULL,
  marks INT NOT NULL,
  max_marks INT NOT NULL,
  grade VARCHAR(5) NOT NULL,
  semester VARCHAR(50) NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Academics: academic leave
CREATE TABLE IF NOT EXISTS academic_leave (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  start_date DATE NULL,
  end_date DATE NULL,
  status VARCHAR(50) NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Academics: clearance
CREATE TABLE IF NOT EXISTS clearance (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  department VARCHAR(100) NULL,
  status VARCHAR(50) NULL,
  remark VARCHAR(255) NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Voting
CREATE TABLE IF NOT EXISTS elections (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS candidates (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  election_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  FOREIGN KEY (election_id) REFERENCES elections(id)
);

CREATE TABLE IF NOT EXISTS votes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  election_id BIGINT NOT NULL,
  candidate_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (election_id) REFERENCES elections(id),
  FOREIGN KEY (candidate_id) REFERENCES candidates(id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  UNIQUE KEY uniq_vote (election_id, user_id)
);

-- ===== Seed data =====
INSERT INTO users (username, password_hash, full_name, email, admission_no)
VALUES ('student1', NULL, 'Student One', 'student1@example.com', 'ADM123')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

INSERT INTO zoom_rooms (course_code, course_title, start_time, join_url)
VALUES
('CSC101', 'Intro to CS', NOW() + INTERVAL 1 DAY, 'https://example.com/zoom/csc101'),
('MAT201', 'Calculus II', NOW() + INTERVAL 2 DAY, 'https://example.com/zoom/mat201');

INSERT INTO course_registration (user_id, course_code, course_name, credits, semester, status)
SELECT id, 'CSC101', 'Intro to CS', 3, '2025 Spring', 'registered' FROM users WHERE username='student1'
ON DUPLICATE KEY UPDATE status='registered';

INSERT INTO course_work (user_id, course_code, assignment, marks, max_marks, due_date, status)
SELECT id, 'CSC101', 'Assignment 1', 18, 20, CURDATE() + INTERVAL 7 DAY, 'pending' FROM users WHERE username='student1';

INSERT INTO exam_card (user_id, course_code, course_name, exam_date, venue)
SELECT id, 'CSC101', 'Intro to CS', CURDATE() + INTERVAL 30 DAY, 'Hall A' FROM users WHERE username='student1';

INSERT INTO exam_audit (user_id, course_code, audit_note, status)
SELECT id, 'CSC101', 'Syllabus coverage verified', 'ok' FROM users WHERE username='student1';

INSERT INTO exam_result (user_id, course_code, exam_type, marks, max_marks, grade, semester)
SELECT id, 'CSC101', 'Midterm', 85, 100, 'A', '2025 Spring' FROM users WHERE username='student1';

INSERT INTO academic_leave (user_id, start_date, end_date, status)
SELECT id, CURDATE() - INTERVAL 10 DAY, CURDATE() - INTERVAL 7 DAY, 'approved' FROM users WHERE username='student1';

INSERT INTO clearance (user_id, department, status, remark)
SELECT id, 'Library', 'clear', 'No pending books' FROM users WHERE username='student1';

INSERT INTO elections (title) VALUES ('Student Council President')
ON DUPLICATE KEY UPDATE title=VALUES(title);

INSERT INTO candidates (election_id, name)
SELECT e.id, 'Alice' FROM elections e WHERE e.title='Student Council President';
INSERT INTO candidates (election_id, name)
SELECT e.id, 'Bob' FROM elections e WHERE e.title='Student Council President';
