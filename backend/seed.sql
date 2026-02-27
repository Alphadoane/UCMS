-- Basic seed data
INSERT INTO users (username, password_hash, full_name, email, admission_no)
VALUES ('student1', NULL, 'Student One', 'student1@example.com', 'ADM123')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- Additional sample users for login
INSERT INTO users (username, password_hash, full_name, email, admission_no)
VALUES ('student2', NULL, 'Student Two', 'student2@example.com', 'ADM124')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

INSERT INTO users (username, password_hash, full_name, email, admission_no)
VALUES ('admin', NULL, 'Admin User', 'admin@example.com', NULL)
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- Virtual campus
INSERT INTO zoom_rooms (course_code, course_title, start_time, join_url)
VALUES
('CSC101', 'Intro to CS', NOW() + INTERVAL 1 DAY, 'https://example.com/zoom/csc101'),
('MAT201', 'Calculus II', NOW() + INTERVAL 2 DAY, 'https://example.com/zoom/mat201');

-- Academics
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

-- Voting
INSERT INTO elections (title) VALUES ('Student Council President')
ON DUPLICATE KEY UPDATE title=VALUES(title);

INSERT INTO candidates (election_id, name)
SELECT e.id, 'Alice' FROM elections e WHERE e.title='Student Council President';
INSERT INTO candidates (election_id, name)
SELECT e.id, 'Bob' FROM elections e WHERE e.title='Student Council President';
