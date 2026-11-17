# AttendEase

AttendEase is a Java 21 and Spring Boot application for university attendance. Teachers manage students, courses, enrollments, class sessions, and CSV reports. Students sign in to see their own courses, history, and warnings.

## Run locally

1. Install a Java 21 JDK, Maven 3.9 or newer, and MySQL Server 8.0.16 or newer in the MySQL 8 series. Verify with `java -version`, `mvn -version`, and `mysql --version`. Start MySQL and confirm Maven is using Java 21.
2. Open a terminal in this directory. Connect as a MySQL administrator using `mysql -u root -p`. Run the following inside the MySQL client. Replace the example password with a password of your choice.

   ```sql
   SOURCE database.sql;
   CREATE USER 'attendease_app'@'localhost' IDENTIFIED BY 'REPLACE_WITH_YOUR_PASSWORD';
   GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
     ON attendease.* TO 'attendease_app'@'localhost';
   EXIT;
   ```

   Flyway applies the versioned schema when the application starts. The application account needs the migration privileges above when applying migrations.

3. Set environment variables in the terminal that will start Spring Boot. The PowerShell example prompts for passwords. The teacher password must contain at least 8 characters and fit in 72 UTF-8 bytes.

   ```powershell
   $env:DB_URL = 'jdbc:mysql://localhost:3306/attendease'
   $env:DB_USER = 'attendease_app'
   $env:DB_PASSWORD = Read-Host 'MySQL password' -MaskInput
   $env:ATTENDEASE_TEACHER_PASSWORD = Read-Host 'Initial teacher password' -MaskInput
   mvn spring-boot:run
   ```

   In Bash, set equivalent variables with `export` and use `read -rs` to read passwords without echoing. Local development uses plain HTTP. When running behind HTTPS, set `SERVER_SERVLET_SESSION_COOKIE_SECURE=true`.

4. Open [localhost:8080](http://localhost:8080) and sign in as `teacher` or `teacher@attendease.example` with the password from step 3. The environment variable creates the account once. Changing the variable after creation leaves the stored password in place.

## Try the demo

On a development database, set these variables before running the application. A demo-only database can omit `ATTENDEASE_TEACHER_PASSWORD`.

```powershell
$env:SPRING_PROFILES_ACTIVE = 'demo'
$env:ATTENDEASE_DEMO_PASSWORD = Read-Host 'Demo password' -MaskInput
mvn spring-boot:run
```

Sign in as `demo-teacher` or any student from `DEMO-001` through `DEMO-005` using the chosen demo password. Demo startup creates one teacher, five students, three courses and three dated sessions per course. Use `DEMO-001` to demonstrate a low-attendance warning. Demo accounts and data persist in the database after switching profiles.

## Attendance policy

Present and Late count as attended. A student's course percentage is `100 Ã— (Present + Late) / eligible conducted sessions`. A saved session records the roster at the time of marking, so later enrollments begin with subsequent sessions. Deactivation blocks student access and excludes the account from new sessions while preserving its saved attendance. Zero eligible sessions show **N/A**. Attendance below **75%** triggers a warning.

Reports show unique conducted class sessions and the number of student-session records used in the percentage. Course, student, and date filters constrain the summary. The status filter constrains the visible rows and CSV.

## Build and test

```powershell
mvn test
mvn package
java -jar target/attendease-1.0.0.jar
```

Tests use H2 for the integration suite and Mockito for student service unit tests. The packaged application uses the MySQL connection above.

## Shared page structure

All pages render through `src/main/resources/templates/layout.html`. It supplies the common HTML, Bootstrap 5 and Bootstrap Icons, role-appropriate navigation, feedback and asset links. Shared styles live in `src/main/resources/static/css/parent.css` and shared interactions in `static/js/parent.js`. Maven bundles the Bootstrap assets as WebJars.

## Publish to GitHub

Create an empty repository on GitHub. From this directory run `git branch -M main`, `git remote add origin <your-repository-url>`, and `git push -u origin main`. For coursework changes, create a feature branch, commit the relevant files, push the branch, and open a pull request against `main`. Keep database passwords and `.env` files outside Git.

- [Architecture and assignment checklist](docs/architecture.md)
- [Database design](docs/database-design.md)
- [Testing guide](docs/testing.md)
