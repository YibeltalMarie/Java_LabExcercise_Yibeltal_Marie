# University RMI System

## Architecture — Single Responsibility Design

```
UniversityRMI/
├── java_libs/
│   └── mariadb-java-client-3.3.0.jar
└── src/
    └── university/
        ├── common/                      ← Shared by client AND server
        │   ├── UniversityService.java   ← RMI Remote Interface (the contract)
        │   ├── StudentDTO.java          ← Serializable student data object
        │   └── TeacherDTO.java          ← Serializable teacher data object
        │
        ├── db/                          ← Server-side only: database layer
        │   ├── DatabaseConnection.java  ← Responsibility: DB credentials & connections
        │   ├── StudentDAO.java          ← Responsibility: SQL for students
        │   └── TeacherDAO.java          ← Responsibility: SQL for teachers
        │
        ├── server/                      ← RMI server layer
        │   ├── UniversityServiceImpl.java  ← Implements remote interface, delegates to DAOs
        │   └── ServerMain.java             ← Starts registry, binds service
        │
        └── client/                      ← JavaFX GUI, talks ONLY via RMI
            └── UniversityClientApp.java ← 4 tabs: Add/Show Student, Add/Show Teacher
```

## Each class's single responsibility

| Class                   | Responsibility                                      |
|-------------------------|-----------------------------------------------------|
| `UniversityService`     | Define the remote contract (interface only)         |
| `StudentDTO`            | Carry student data safely across the network        |
| `TeacherDTO`            | Carry teacher data safely across the network        |
| `DatabaseConnection`    | Hold DB credentials and produce connections         |
| `StudentDAO`            | Execute student SQL (insert, select)                |
| `TeacherDAO`            | Execute teacher SQL (insert, select)                |
| `UniversityServiceImpl` | Implement remote methods, delegate to DAOs          |
| `ServerMain`            | Start RMI registry and register the service         |
| `UniversityClientApp`   | JavaFX UI, calls service via RMI only               |

## How RMI works here

```
Client                          Network             Server
------                          -------             ------
UniversityClientApp
  └─ looks up "UniversityService"  ──rmi://──►  Registry (port 1099)
       └─ calls service.addStudent(dto)  ──────►  UniversityServiceImpl
                                                      └─ StudentDAO.insert(dto)
                                                            └─ DatabaseConnection
                                                                  └─ MariaDB
```

## How to run

### Step 1 — Compile
```bash
chmod +x run.sh
./run.sh compile
```

### Step 2 — Start the server (keep this terminal open)
```bash
./run.sh server
```
You should see:
```
✓ MariaDB driver loaded.
========================================
  University RMI Server is running...
  Port    : 1099
  Service : UniversityService
========================================
```

### Step 3 — Start the client (new terminal)
```bash
./run.sh client
```

The GUI opens with 4 tabs:
- **Add Student** — fill form → click Add Student → calls `service.addStudent()` via RMI
- **Show Students** — click Refresh → calls `service.getStudents()` via RMI → shows table
- **Add Teacher** — same pattern for teachers
- **Show Teachers** — same pattern for teachers

## Database setup (MariaDB)
```sql
CREATE DATABASE university_db;
USE university_db;

CREATE TABLE students (
    id         INT PRIMARY KEY,
    name       VARCHAR(100),
    department VARCHAR(50),
    section    VARCHAR(10),
    year       INT
);

CREATE TABLE teacher (
    id         INT PRIMARY KEY,
    name       VARCHAR(100),
    department VARCHAR(50)
);
```
