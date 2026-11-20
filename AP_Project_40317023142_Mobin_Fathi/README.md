# 📝 TaskFlow - Daily Task Management System

Advanced Programming (AP) Final Project - Summer 2026

## 📋 Project Overview
**TaskFlow** is a robust, multi-threaded, console-based task management application written in Java. It allows users to register, log in, and manage daily tasks with custom priorities, deadlines, and execution statuses. The application persists data to files, features a daemon reminder system, and automatically resets tasks at the start of a new day.

---

## 🏛️ System Architecture (MVC Pattern)
The project strictly adheres to the **Model-View-Controller (MVC)** architectural pattern to ensure clean separation of concerns and maintainable code:

1. **Model (`model` package):**
    * `User`: Represents system users.
    * `Task`: Represents tasks with validation rules, encapsulation, and sorting capabilities.
2. **View (`view` package):**
    * `ConsoleView`: Handles all user interactions, CLI menus, inputs, and progress visualizations.
3. **Controller (`controller` package):**
    * `TaskController`: Intermediates between the View and the Service layer. It prevents *Escape References* by returning unmodifiable lists (`Collections.unmodifiableList`) to the View.
4. **Service Layer (`service` package):**
    * `TaskService`: Contains core business logic, user session management, and coordinates repository actions.
5. **Repository Layer (`repository` package):**
    * `TaskRepository` & `InMemoryTaskRepository`: Abstract and handle task storage.
    * `DatabaseManager`: Implements persistent file-based I/O using `BufferedReader` and `PrintWriter` with memory-optimized `StringBuilder` parsing.

---

## 🏗️ Design Patterns & Data Structures

### 1. Singleton Pattern
The `DatabaseManager` class utilizes the **Singleton Design Pattern** to guarantee a single, centralized access point to the physical storage (`tasks.txt` and `users.txt`). This prevents file-locking and I/O conflict issues during simultaneous read/write cycles.

### 2. Task Prioritization (Sorting)
The `Task` class implements the `Comparable<Task>` interface. Tasks are dynamically sorted based on two criteria:
* **Priority**: Descending order (from highest priority 5 down to 1).
* **Deadline**: In case of a priority tie, tasks are sorted chronologically by their deadlines.

---

## 🧵 Concurrency & Thread-Safety

### 🛡️ Preventing Concurrency Issues
In a multi-threaded environment, concurrent modifications can cause severe crashes. To guarantee absolute **Thread-Safety**:
* All critical read/write operations on shared collections inside `TaskService` are wrapped inside `synchronized(tasksLock)` blocks. This prevents `ConcurrentModificationException` and state corruption.

### 🔔 Daemon Reminder Thread
The `ReminderThread` runs in the background as a **Daemon Thread**:
* It wakes up every **10 seconds** to scan the active user's pending tasks.
* To avoid **Deadlocks**, it does *not* acquire raw service locks directly. Instead, it queries the thread-safe `taskService.getTasksByOwner()` method.
* It alerts the console only for high-priority tasks (priority **4 or 5**) that are still pending.
* **Smart Alerting**: It keeps track of notified tasks to prevent console alert spamming; a notification for a specific task is displayed only once unless its status resets.

---

## 🚀 Execution Guide

### Prerequisites
* **Java Development Kit (JDK) 17** or higher installed.
* An IDE such as IntelliJ IDEA (Recommended) or VS Code.

### Compilation & Running Steps
1. Clone or copy the project files to your local workstation.
2. Open the project root folder in your IDE.
3. Locate the `Main.java` file inside the `src` directory.
4. Run the `main` method to launch the application.
5. The system will automatically initialize the necessary text databases (`tasks.txt`, `users.txt`, `last_reset.txt`) if they do not exist.

---

## 🛠️ Key Features
* 👤 **Secure Authentication**: Register and log in with inputs validated via `InputValidator`.
* 🔄 **Daily Auto-Reset**: Integrates `DailyResetManager` using Java's Date/Time API to automatically reset task progress at the turn of a new calendar day.
* 📊 **Visual Progress Bar**: Displays a graphical CLI progress bar (`█░░░░░░░░░ 10%`) representing the user's completed tasks ratio.
* ⚙️ **Robust Input Validation**: Immune to `NumberFormatException` and invalid switch choices through a loops-and-validation system.

---
**Developer:** [Your Name]  
**Term:** AP - Summer 2026
https://drive.google.com/file/d/1-fTivziqZuxM8uxpHX94hpnQfb-7_trI/view?usp=sharing