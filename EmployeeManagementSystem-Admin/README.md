# Employee Management System - Admin Portal

A full-stack, student-level web application for managing employees, attendance logs, and leave requests.

## Features
- **Admin Authentication**: Session-based login with BCrypt password hashing.
- **Admin Dashboard**: Real-time overview of Total Employees, Active Employees, Today's Present, Today's Punch Out, and Pending Leave Requests.
- **Employee Management**: Add new staff, view complete profiles, edit details, and toggle active/inactive account status.
- **Attendance Management**: Filterable attendance logs (by employee, date, department) with automated work duration calculation (`e.g. 8h 45m`) and Face Scan indicators.
- **Leave Management**: Review leave applications with Approve and Reject options and optional admin notes.
- **Notification & Email Dispatch**: In-app notifications are automatically saved to MySQL on leave review; dispatches automated emails via Spring Mail when SMTP is configured.

## Tech Stack
- **Backend**: Java 17+, Spring Boot 3, Spring JDBC (`JdbcTemplate`), Spring Security Crypto (BCrypt), Spring Mail.
- **Database**: MySQL (`employee_management`).
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
   http://localhost:8081
   ```

## Demo Credentials
- **Username**: `admin`
- **Password**: `admin123`
*(Pre-seeded in database.sql)*
