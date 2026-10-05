# Employee Management System - Employee Portal

A full-stack, student-level web application enabling employees to record face scanner attendance, view attendance history, track work hours, apply for leaves, receive notifications, and manage their personal information.

## Key Features
- **Employee Authentication**: Secure session-based authentication with Employee Code & BCrypt password verification.
- **Interactive Dashboard**: Displays personalized welcome card, today's attendance state (Not Punched, Working, Day Completed), Punch In/Out timestamps, and calculated work hours.
- **Webcam Face Scanner Attendance**:
  - Live webcam preview with automated face detection overlay.
  - **Enroll Face**: Captures facial descriptor vector (128-dimensional embedding) and stores it in MySQL.
  - **Face Punch**: Compares live webcam descriptor against stored template using Euclidean distance matching.
  - Automatically handles Punch In (first scan) and Punch Out (subsequent scan on the same day) with precise work duration calculation.
  - Rejects scans when face does not match or when face is outside frame.
- **Attendance History**: Chronological log of past punches with work duration breakdown (`e.g. 09:30 AM - 06:15 PM = 8h 45m`) and punch method badges.
- **Leave Application**: Form to submit Casual, Sick, Emergency, or Other leave requests with date range and reason.
- **My Leave Requests**: Real-time status tracker (PENDING, APPROVED, REJECTED) with admin review notes.
- **In-App Notifications**: Alerts when leave requests are approved or rejected, with "Mark as Read" actions and unread count badge.
- **Employee Profile**: Clean view of full name, employee code, email, department, designation, and biometric enrollment status.

## Tech Stack
- **Backend**: Java 17+, Spring Boot 3, Spring JDBC (`JdbcTemplate`), Spring Security Crypto (BCrypt), Jackson.
- **Database**: MySQL (`employee_management`).
- **Face Recognition**: Browser-based `face-api.js` (with bundled offline model weights).
- **Frontend**: HTML5, CSS3, Vanilla JavaScript (Modern student dashboard UI, responsive cards, modals, tables).

## Setup & Execution
1. Ensure MySQL is running and `database.sql` has been imported into database `employee_management`.
2. Configure `src/main/resources/application.properties` with your MySQL username and password.
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. Access in your browser:
   ```
   http://localhost:8082
   ```

## Demo Credentials
- **Employee Code**: `EMP001`
- **Password**: `employee123`
*(Pre-seeded in database.sql)*
