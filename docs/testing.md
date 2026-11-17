# Testing AttendEase

Run `mvn test` from the directory containing `pom.xml`. Maven writes the test reports under `target/surefire-reports`. H2 runs the MVC and service integration tests in memory. A MySQL run is needed to verify the Flyway migration and MySQL-specific behavior.

- **Student creation** checks registration, normalized email, account role, and the stored password hash.
- **Duplicate registration** expects a domain error before creating an account.
- **Course creation and enrollment** check the saved course code and reject the second enrollment.
- **Attendance save and edit** check record and session counts after editing and verify that a late enrollment leaves an earlier roster intact.
- **Percentage calculation** expects 80% for 14 Present, 2 Late and 4 Absent.
- **Zero sessions** expect N/A and no warning.
- **Low attendance** warns below 75% and accepts exactly 75%.
- **Report filtering and ownership** check that status filtering preserves the percentage denominator and that a different teacher cannot report on the course.
- **Student access control** expects HTTP 403 when a student submits attendance, even with a CSRF token.
- **Shared layout** checks representative teacher, student and login pages through Spring MVC and Thymeleaf.

For a manual MySQL check, start with a fresh development database. Sign in as the teacher, add a student, enroll them, and mark the student Late. Open the same date and session, change it to Absent, then verify a single row in the report and CSV. Sign in as the student and verify the warning and personal history. Sign back in as the teacher, deactivate the account, and verify that the student loses access while the historical row remains. On a narrow screen, open and close the sidebar and confirm wide tables scroll inside their panels.
