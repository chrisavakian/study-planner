@echo off
echo Building Smart Study Planner...

if not exist bin mkdir bin

echo Compiling main source files...
javac -d bin src\main\java\com\studyplanner\models\*.java src\main\java\com\studyplanner\services\*.java src\main\java\com\studyplanner\scheduler\*.java src\main\java\com\studyplanner\*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    exit /b 1
)

echo Main source files compiled successfully.

echo Checking if JUnit is available...
if exist junit-platform-console-standalone-1.9.2.jar (
    echo Compiling test files with JUnit...
    javac -cp ".;bin" -d bin src\test\java\com\studyplanner\tests\*.java
    
    if %ERRORLEVEL% NEQ 0 (
        echo Test compilation failed!
        exit /b 1
    )
    
    echo Running tests...
    java -cp ".;bin" org.junit.platform.console.ConsoleLauncher --scan-classpath --include-classname=".*Test" --details=tree
) else (
    echo JUnit not found. Skipping tests.
    echo To run tests, download junit-platform-console-standalone-1.9.2.jar from Maven Central or run with Maven:
    echo mvn test
)

echo Build completed.