@echo off
echo Starting Build Automation with Maven...

echo 1. Cleaning previous builds, resolving dependencies, compiling source code, and packaging the application...
mvn clean package > build_report.txt

echo Build complete! Check build_report.txt for the generated report.
pause