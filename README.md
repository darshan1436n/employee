# employee

Assignment 2

## Shiftly (Plain Java, no Maven / no Spring Boot)

## Requirements
- JDK 17 or newer
- MySQL Server (MySQL Workbench is optional)
- MySQL Connector/J JDBC driver JAR (one manually downloaded library)

## Setup
1. Install and start MySQL Server.
2. Open MySQL Workbench or the MySQL command line and run `database/shiftly.sql`.
3. Download Connector/J from https://dev.mysql.com/downloads/connector/j/
4. Choose the Platform Independent ZIP, extract it, and copy its `mysql-connector-j-*.jar` into this project's `lib` folder. Rename it to `mysql-connector-j.jar`.
5. In `run.bat`, the default MySQL username is `root`. When prompted, enter your MySQL password. If your MySQL user/password differs, set `SHIFTLY_DB_USER` and `SHIFTLY_DB_PASSWORD` environment variables before running.
6. Double-click `run.bat` or open a terminal in this folder and run `run.bat`.
7. Visit http://localhost:8080

## Demo login
- Admin username: `admin`
- Admin password: `admin123`

Students register through Login / Register and log in with their email and password.

## Employee dashboard
- Visit http://localhost:8080/employee-dashboard.html for the React employee dashboard.
- Use the **Employee form** link to open `/employee-form.html` and add an employee.
- The dashboard loads its employee directory from JSONPlaceholder (`https://jsonplaceholder.typicode.com/users`).
- The React, Bootstrap, Bootstrap Icons, and font assets load from CDNs, so the browser needs internet access.

## Important
This version uses Java's built-in HTTP server and JDBC. There is no Maven, no Spring Boot, and no separate Tomcat installation. MySQL Connector/J is still needed so Java can connect to MySQL. This is an academic demo: for real deployment, add password hashing, proper server-side sessions/authorization, input validation, and CSRF protection.
