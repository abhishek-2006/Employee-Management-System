# Complete Employee Management System (EMS)

A comprehensive, student-level full-stack Employee Management System built with **Java Spring Boot**, **Spring JDBC**, **MySQL**, **HTML5/CSS3/JavaScript**, and **browser-based Face Recognition** (`face-api.js`).

The project is architected as two completely independent applications sharing a single centralized MySQL database:
1. **Admin Portal** (`EmployeeManagementSystem-Admin`) running on port `8081`
2. **Employee Portal** (`EmployeeManagementSystem-Employee`) running on port `8082`

---

## 1. System Requirements

Before running the applications, ensure your system has:
- **Operating System**: Windows 10/11, macOS, or Linux
- **Java Development Kit (JDK)**: JDK 17 or higher (JDK 17, 21, or 25 recommended)
- **Database**: MySQL Server 8.0+ or MariaDB (e.g. via XAMPP, WampServer, or standalone MySQL)
- **Web Browser**: Modern browser with webcam support (Google Chrome, Microsoft Edge, Firefox)
- **Build Tool**: Maven 3.8+ (Maven Wrapper `mvnw` / `mvnw.cmd` is also bundled in each project so installing global Maven is optional)

---

## 2. Java Installation Guide

1. Download Oracle JDK 17/21 LTS or Eclipse Temurin OpenJDK from:
   - [Oracle Java Downloads](https://www.oracle.com/java/technologies/downloads/) or [Adoptium](https://adoptium.net/)
2. Run the installer and complete the setup.
3. Verify Java installation in your terminal / command prompt:
   ```bash
   java -version
   javac -version
   ```

---

## 3. Maven Installation (Optional)

Both projects bundle `mvnw.cmd` (Windows) and `mvnw` (Linux/macOS), allowing you to run them without pre-installing Maven. If you prefer global Maven:
1. Download Apache Maven binary zip from [maven.apache.org](https://maven.apache.org/download.cgi).
2. Extract to `C:\Program Files\apache-maven` (or your preferred folder).
3. Add the `bin` directory to your system `PATH` environment variable.
4. Verify in terminal:
   ```bash
   mvn -version
   ```

---

## 4. MySQL Installation Guide

1. Download and install **MySQL Community Server** from [dev.mysql.com](https://dev.mysql.com/downloads/mysql/) OR install **XAMPP** from [apachefriends.org](https://www.apachefriends.org/).
2. Start the MySQL service:
   - If using XAMPP: Open XAMPP Control Panel and click **Start** next to MySQL.
   - If using standalone MySQL: Verify the Windows service `MySQL` is Running.

---

## 5. Database Setup

1. Open your terminal or MySQL client (e.g. MySQL Command Line, MySQL Workbench, or phpMyAdmin at `http://localhost/phpmyadmin`).
2. Run the provided `database.sql` script located in the project root:
   ```bash
   mysql -u root -p < database.sql
   ```
   *(If your root user has no password, simply omit `-p` or press Enter when prompted).*
3. The script automatically:
   - Creates the database `employee_management`
   - Creates tables: `admin`, `employees`, `attendance`, `leave_requests`, and `notifications`
   - Configures foreign keys and indexes
   - Seeds demo admin and employee accounts

---

## 6. How to Configure MySQL Password

If your MySQL root account uses a password, update both `application.properties` files:

1. **Admin Portal**:
   File: `EmployeeManagementSystem-Admin/src/main/resources/application.properties`
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/employee_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```

2. **Employee Portal**:
   File: `EmployeeManagementSystem-Employee/src/main/resources/application.properties`
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/employee_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```

---

## 7. How to Configure Email Notifications (Spring Mail)

When the Admin approves or rejects a leave request, the system automatically writes an in-app notification to MySQL. If live email delivery is also desired, configure your SMTP credentials in:
`EmployeeManagementSystem-Admin/src/main/resources/application.properties`

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-digit-gmail-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

> **Note**: For Gmail, use an **App Password** generated from your Google Account settings (Security -> 2-Step Verification -> App passwords). If left as placeholders, the application will simply log an info message and gracefully skip SMTP sending without throwing errors.

---

## 8. How to Run Admin Portal

1. Open a terminal and navigate to `EmployeeManagementSystem-Admin`:
   ```bash
   cd EmployeeManagementSystem-Admin
   ```
2. Start the application using Maven Wrapper:
   - On Windows:
     ```cmd
     mvnw.cmd spring-boot:run
     ```
   - On macOS / Linux:
     ```bash
     ./mvnw spring-boot:run
     ```
   *(Or if you have Maven installed: `mvn spring-boot:run`)*
3. Once started, open your browser and navigate to:
   ```
   http://localhost:8081
   ```

---

## 9. How to Run Employee Portal

1. Open a **new, separate terminal** window and navigate to `EmployeeManagementSystem-Employee`:
   ```bash
   cd EmployeeManagementSystem-Employee
   ```
2. Start the application using Maven Wrapper:
   - On Windows:
     ```cmd
     mvnw.cmd spring-boot:run
     ```
   - On macOS / Linux:
     ```bash
     ./mvnw spring-boot:run
     ```
   *(Or if you have Maven installed: `mvn spring-boot:run`)*
3. Once started, open your browser and navigate to:
   ```
   http://localhost:8082
   ```

---

## 10. Demo Login Credentials

### Admin Portal (`http://localhost:8081`)
| Field | Demo Credential |
| :--- | :--- |
| **Username** | `admin` |
| **Password** | `admin123` |

### Employee Portal (`http://localhost:8082`)
| Field | Demo Credential |
| :--- | :--- |
| **Employee Code** | `EMP001` |
| **Password** | `employee123` |

> *Passwords are securely hashed using BCrypt. These demo credentials are for evaluation and demonstration purposes and can be changed by the administrator.*

---

## 11. How to Enroll Face

1. Log in to the **Employee Portal** (`http://localhost:8082`) with `EMP001` / `employee123`.
2. Click on **Face Attendance** in the sidebar.
3. Click the **Start Camera** button and grant browser webcam permissions when prompted.
4. Center your face directly in front of the camera box with clear lighting.
5. Click **Enroll Face**.
6. The browser uses `face-api.js` to extract your 128-dimensional facial landmark descriptor and saves it securely into MySQL.
7. The status box will confirm: `"Face enrolled successfully."`

---

## 12. How to Punch In

1. Open **Face Attendance** in the Employee Portal.
2. Click **Start Camera** and look into the camera.
3. Click **Face Punch**.
4. The system detects your face and verifies it against your enrolled descriptor using Euclidean distance comparison:
   - If your face matches and you haven't punched in today, the system records your **Punch In** with the current time.
   - Status message displays: `"Punch In successful."`
5. Visit the **Dashboard** to see your Punch In timestamp and "Working (Punched In)" status.

---

## 13. How to Punch Out

1. At the end of the working day, go to **Face Attendance** in the Employee Portal.
2. Click **Start Camera** and look at the camera.
3. Click **Face Punch**.
4. The system verifies your face:
   - Since a Punch In already exists for today without a Punch Out, it logs your **Punch Out** timestamp.
   - It automatically calculates your total work duration (e.g. `8h 45m`).
   - Status message displays: `"Punch Out successful. Work hours: 8h 45m"`.
5. If an employee tries to punch again after both punch in and punch out are logged, the system prevents duplicate attendance with: `"Today's Punch In and Punch Out are already completed."`

---

## 14. How to Apply for Leave

1. In the Employee Portal, click **Apply Leave** in the sidebar.
2. Select the **Leave Type** (Casual, Sick, Emergency, or Other).
3. Choose the **From Date** and **To Date**.
4. Enter the **Reason** for leave.
5. Click **Submit Leave Application**.
6. The request is saved in MySQL with `PENDING` status.
7. Track the application in the **My Leave Requests** tab.

---

## 15. How Admin Approves / Rejects Leave

1. Log in to the **Admin Portal** (`http://localhost:8081`).
2. Click **Leave Requests** in the sidebar.
3. You will see all submitted applications with their status badges (`PENDING`, `APPROVED`, `REJECTED`).
4. Click **Review Request** on any pending leave.
5. A modal appears showing employee name, code, leave type, date range, and reason.
6. Enter an optional **Admin Note / Feedback** (e.g. *"Approved, please coordinate with team lead"*).
7. Click **Approve Request** or **Reject Request**.
8. The database updates the leave status and review timestamp.

---

## 16. How Notifications Work

1. As soon as the Admin approves or rejects a leave request, an in-app notification is inserted into the `notifications` table for that employee.
2. If SMTP email is configured, an automated email is dispatched to the employee's email address.
3. When the employee logs into the **Employee Portal**, an unread notification count badge appears on the sidebar.
4. Clicking **Notifications** displays all updates with title, message body, date, and a **Mark as Read** button.

---

## 17. Application Architecture & File Tree

```
samarth/
├── database.sql                               # Master MySQL schema & seed data
├── README.md                                  # Master project documentation
│
├── EmployeeManagementSystem-Admin/            # Admin Application (Port 8081)
│   ├── pom.xml                                # Maven build specification
│   ├── mvnw & mvnw.cmd                        # Maven wrapper scripts
│   ├── README.md                              # Admin documentation
│   └── src/
│       └── main/
│           ├── java/com/student/ems/admin/
│           │   ├── AdminApplication.java
│           │   ├── config/                    # Security & Session Interceptor
│           │   ├── controller/                # REST Controllers
│           │   ├── model/                     # Domain & DTO Models
│           │   ├── repository/                # Spring JDBC Repositories
│           │   └── service/                   # Business Services & Mail Dispatch
│           └── resources/
│               ├── application.properties     # Config (Port 8081, DB, Mail)
│               └── static/                    # Frontend UI (HTML5, CSS3, Vanilla JS)
│                   ├── index.html             # Admin Login Page
│                   ├── dashboard.html         # Admin Dashboard SPA
│                   ├── css/style.css          # Student-style Clean UI Theme
│                   └── js/app.js              # Admin Client Logic
│
└── EmployeeManagementSystem-Employee/         # Employee Application (Port 8082)
    ├── pom.xml                                # Maven build specification
    ├── mvnw & mvnw.cmd                        # Maven wrapper scripts
    ├── README.md                              # Employee documentation
    └── src/
        └── main/
            ├── java/com/student/ems/employee/
            │   ├── EmployeeApplication.java
            │   ├── config/                    # Security & Session Interceptor
            │   ├── controller/                # REST Controllers
            │   ├── model/                     # Domain & DTO Models
            │   ├── repository/                # Spring JDBC Repositories
            │   └── service/                   # Business Services & Biometric Matching
            └── resources/
                ├── application.properties     # Config (Port 8082, DB)
                └── static/                    # Frontend UI (HTML5, CSS3, Vanilla JS)
                    ├── index.html             # Employee Login Page
                    ├── dashboard.html         # Employee Dashboard SPA
                    ├── css/style.css          # Clean Theme Stylesheet
                    ├── js/app.js              # Employee Client Logic
                    ├── js/face-scanner.js     # Webcam & Face Recognition Handler
                    ├── js/face-api.min.js     # Bundled Face-API Library
                    └── models/                # Local Offline Face Recognition Models
```

---

## 18. Port Allocation Summary

| Application | Port | URL | Description |
| :--- | :--- | :--- | :--- |
| **Admin Portal** | `8081` | `http://localhost:8081` | Manage employees, attendance logs, and leave reviews |
| **Employee Portal** | `8082` | `http://localhost:8082` | Face scanner attendance, leave requests, profile |
| **MySQL Server** | `3306` | `localhost:3306` | Shared database `employee_management` |
