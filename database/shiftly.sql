CREATE DATABASE IF NOT EXISTS shiftly CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE shiftly;

CREATE TABLE IF NOT EXISTS users (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 full_name VARCHAR(150) NOT NULL,
 email VARCHAR(190) NOT NULL UNIQUE,
 phone VARCHAR(40) NOT NULL,
 password VARCHAR(255) NOT NULL,
 college VARCHAR(190)
);

CREATE TABLE IF NOT EXISTS jobs (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 title VARCHAR(180) NOT NULL,
 location VARCHAR(180) NOT NULL,
 duration VARCHAR(80),
 hours VARCHAR(100),
 payment VARCHAR(80),
 experience VARCHAR(180),
 job_type VARCHAR(80),
 description TEXT,
 posted_by VARCHAR(150),
 poster_phone VARCHAR(40),
 poster_email VARCHAR(190),
 open_status BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS applications (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 job_id BIGINT NOT NULL,
 applicant_name VARCHAR(150),
 applicant_email VARCHAR(190),
 applicant_phone VARCHAR(40),
 applicant_college VARCHAR(190),
 status VARCHAR(30) NOT NULL DEFAULT 'Pending',
 applied_date DATE NOT NULL,
 CONSTRAINT fk_application_job FOREIGN KEY(job_id) REFERENCES jobs(id)
);

INSERT INTO jobs(title,location,duration,hours,payment,experience,job_type,description,posted_by,poster_phone,poster_email,open_status)
SELECT 'Event Catering Assistant','Thrissur, Kerala','One day','4:00 PM – 10:00 PM','₹600/day','No prior experience required','Temporary','Assist the catering team with food serving, table arrangement, and event cleanup.','Administrator','0000000000','admin@shiftly.test',TRUE
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title='Event Catering Assistant');
