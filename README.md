# Smart Study Planner (CS 5800 Project)

## Project Overview
The Smart Study Planner is a Java-based application that helps students manage academic tasks, schedule study sessions, and adapt to changes in availability and workload. The system allows students to add tasks, set availability, automatically generate schedules using AI (OpenAI), mark sessions as complete, and adapt when tasks or availability change.

## System Architecture

### Classes Implemented:
1. **Student**: Manages student information, tasks, availability, and schedule
2. **Task**: Represents academic tasks with title, deadline, and effort
3. **Course**: Groups tasks by course with instructor information
4. **Session**: Represents individual study sessions with start/end times and completion status
5. **Availability**: Defines time slots available for scheduling
6. **Schedule**: Stores and displays study sessions for a week
7. **OpenAIService**: Interacts with OpenAI API for task prioritization and annotation
8. **LLMService**: Manages AI-based prioritization and annotation of tasks
9. **Scheduler**: Generates study schedules based on priorities and availability

## Features
- **Interactive Command-Line Interface**: User-friendly menu system for all operations
- **OpenAI Integration**: Uses OpenAI API for intelligent task prioritization
- **User Input**: Collects student information, availability, and tasks interactively
- **API Key Support**: Securely handles OpenAI API credentials
- **Add tasks** with title, deadline, and effort estimation
- **Set weekly availability** for study sessions with time validation
- **Generate automatic study schedules** using AI prioritization
- **View and manage schedules** by day and session
- **Mark study sessions as complete**
- **Adaptive replanning** when tasks or availability change

## Project Structure
```
study-planner/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/studyplanner/
│   │           ├── models/     # Task, Student, Course, etc.
│   │           ├── services/   # OpenAIService, LLMService
│   │           └── scheduler/  # Scheduler
│   │           └── Main.java
│   └── test/
│       └── java/
│           └── com/studyplanner/tests/
│               ├── StudentTest.java
│               ├── TaskTest.java
│               ├── CourseTest.java
│               ├── LLMServiceTest.java
│               ├── SchedulerTest.java
│               └── IntegrationTest.java
├── pom.xml
├── build.bat
└── README.md
```

## How to Compile and Run

### Prerequisites
- Java 17 or newer
- Maven (optional, for tests)
- OpenAI API key (optional for basic functionality)

### Compilation
```bash
javac -d bin src/main/java/com/studyplanner/models/*.java src/main/java/com/studyplanner/services/*.java src/main/java/com/studyplanner/scheduler/*.java src/main/java/com/studyplanner/*.java
```

### Execution
```bash
java -cp bin com.studyplanner.Main
```

### Running Tests
The tests have been modified to run without JUnit dependencies:
```bash
javac -cp ".;bin" -d bin src/test/java/com/studyplanner/tests/*.java
java -cp ".;bin" com.studyplanner.tests.StudentTest
java -cp ".;bin" com.studyplanner.tests.TaskTest
java -cp ".;bin" com.studyplanner.tests.CourseTest
java -cp ".;bin" com.studyplanner.tests.LLMServiceTest
java -cp ".;bin" com.studyplanner.tests.SchedulerTest
java -cp ".;bin" com.studyplanner.tests.IntegrationTest
```

## Design Patterns and Principles
- Object-Oriented Programming with proper encapsulation
- SOLID principles implementation
- Clear separation of concerns between models, services, and scheduler
- Comprehensive error handling and validation
- Secure API integration with OpenAI

## Test Coverage
- Unit tests for all major classes (without JUnit dependencies)
- Integration tests for end-to-end functionality
- Edge case handling for invalid inputs
- Conflict resolution testing

## Nvidia NIM Integration
The system includes integration with Nvidia NIM API for:
- **Task Prioritization**: Orders tasks by urgency and importance using AI
- **Task Annotation**: Provides study tips and insights for each task
- **Secure API Access**: Handles API key securely during runtime

To use Nvidia NIM features:
1. Obtain an API key that works with Nvidia NIM
2. Set the base URL to: https://integrate.api.nvidia.com/v1
3. Enter your API key when prompted in the interactive application
4. The system will use AI to prioritize your tasks automatically

## Interactive Features
The application provides an interactive menu system:
- **Task Management**: Add, view, and manage study tasks
- **Availability Setup**: Define your weekly study availability
- **Schedule Generation**: Create AI-prioritized study schedules
- **Session Tracking**: Mark completed study sessions
- **Real-time Updates**: View updated schedules as tasks change

## Workflow
1. User runs the interactive application
2. Enter personal information and OpenAI API key (optional)
3. Input weekly availability for study sessions
4. Add tasks with deadlines and estimated effort
5. Generate schedule using AI-powered prioritization
6. Mark sessions as complete as they're finished
7. System adapts to changes in tasks or availability

## Usage Example
The main program demonstrates:
- Interactive user input for all required information
- Secure handling of OpenAI API credentials
- Weekly availability configuration
- Task management with validation
- AI-powered prioritization using OpenAI API
- Automatic schedule generation
- Session completion tracking