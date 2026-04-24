
@echo off
echo ============================================================
echo         University Management System - Test Automation
echo ============================================================
echo.

echo [Step 1] Running unit tests with JUnit 5 via Maven Surefire...
echo           Tests: UserServiceTest, CourseServiceTest,
echo                  EnrollmentServiceTest, JwtUtilTest
echo.

mvn clean test > test_report.txt 2>&1

echo [Step 2] Checking test results...
findstr /C:"BUILD SUCCESS" test_report.txt > nul
if %errorlevel% == 0 (
    echo  SUCCESS: All tests passed.
) else (
    echo  FAILURE: One or more tests failed. See test_report.txt for details.
)
echo.

echo [Step 3] Reports generated:
echo   - Test results (XML) : target\surefire-reports\
echo   - Coverage report    : target\jacoco-report\index.html
echo.
echo Full Maven output saved to: test_report.txt
echo.
echo Open the JaCoCo coverage report in your browser:
echo   target\jacoco-report\index.html
echo.
pause
