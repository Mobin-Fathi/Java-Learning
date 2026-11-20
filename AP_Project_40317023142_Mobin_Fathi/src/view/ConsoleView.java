package view;

import controller.TaskController;
import exception.InvalidTaskException;
import exception.UserNotFoundException;
import model.Task;
import model.User;
import service.ReminderThread;
import service.TaskService;
import util.InputValidator;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleView {
    private final TaskController controller;
    private final Scanner scanner;
    private Thread reminderThread;

    public ConsoleView(TaskController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            boolean isLoggedIn = controller.isLoggedIn();
            System.out.println("\n--- TaskFlow Menu ---");

            if (!isLoggedIn) {
                System.out.println("1. Login / Register");
                System.out.println("2. Exit");
                System.out.print("Your choice: ");

                String input = scanner.nextLine();
                if (input.equals("1")) {
                    loginOrRegister();
                } else if (input.equals("2")) {
                    stopReminderThread();
                    System.out.println("Exiting TaskFlow. Goodbye!");
                    return;
                } else {
                    System.out.println("❌ Invalid choice. Please login first.");
                }
            } else {
                System.out.println("Logged in as: " + controller.getCurrentUser());
                System.out.println("1. Add New Task");
                System.out.println("2. View Tasks by Priority");
                System.out.println("3. Manage Tasks (Stats, Edit, Delete, Done)");
                System.out.println("4. Logout");
                System.out.print("Your choice: ");

                String input = scanner.nextLine();
                switch (input) {
                    case "1":
                        addTask();
                        break;
                    case "2":
                        viewTasksByPriority();
                        break;
                    case "3":
                        manageTasksView();
                        break;
                    case "4":
                        stopReminderThread();
                        controller.logout();
                        System.out.println("✅ Logged out successfully.");
                        break;
                    default:
                        System.out.println("❌ Invalid choice!");
                }
            }
        }
    }

    private void loginOrRegister() {
        System.out.println("\n--- Authentication ---");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.print("Choice: ");
        String choice = scanner.nextLine();

        System.out.print("Enter Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        try {
            if (choice.equals("1")) {
                if (controller.register(username, password)) {
                    System.out.println("✅ Registration successful. Logged in as " + username + ".");
                    startReminderThread();
                } else {
                    System.out.println("❌ Username already exists.");
                }
            } else if (choice.equals("2")) {
                controller.login(username, password);
                System.out.println("✅ Welcome back, " + username + "!");
                startReminderThread();
            } else {
                System.out.println("❌ Invalid choice!");
            }
        } catch (UserNotFoundException e) {
            System.out.println("❌ Login Error: " + e.getMessage());
        }
    }

    private void startReminderThread() {
        stopReminderThread();

        User currentUser = controller.getCurrentUserObject();
        TaskService service = controller.getTaskService();

        if (currentUser != null && service != null) {
            ReminderThread reminder = new ReminderThread(currentUser, service);
            reminderThread = new Thread(reminder);
            reminderThread.setDaemon(true);
            reminderThread.start();
        }
    }

    private void stopReminderThread() {
        if (reminderThread != null && reminderThread.isAlive()) {
            reminderThread.interrupt();
        }
    }

    private void addTask() {
        try {
            System.out.print("Enter Task Title: ");
            String title = scanner.nextLine();

            // استفاده از کلاس کمکی InputValidator جهت بهبود پایداری برنامه
            int priority = InputValidator.getIntInput("Enter Priority (1-5): ");

            System.out.print("Enter Deadline (YYYY-MM-DD): ");
            String dateString = scanner.nextLine();
            LocalDate deadline = LocalDate.parse(dateString);

            controller.addTask(title, priority, deadline);
            System.out.println("✅ Task added!");

        } catch (DateTimeParseException e) {
            System.out.println("❌ Error: Invalid date format. Please use YYYY-MM-DD.");
        } catch (InvalidTaskException e) {
            System.out.println("❌ Task Validation Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Unexpected error: " + e.getMessage());
        }
    }

    private void viewTasksByPriority() {
        System.out.println("\n--- Your Tasks (Sorted by Priority) ---");

        List<Task> tasks = controller.showPrioritizedTasks();

        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }

        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            String status = t.isDone() ? "[✅ DONE]" : "[⏳ PENDING]";
            System.out.println((i + 1) + ". " + status + " " + t.getTitle()
                    + " | Prio: " + t.getPriority() + " | Date: " + t.getDeadline());
        }
    }

    private void manageTasksView() {
        List<Task> tasks = controller.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("No tasks to manage.");
            return;
        }

        while (true) {
            System.out.println("\n--- Progress Report ---");
            double percentage = controller.getCompletionPercentage();
            System.out.println("Completed: " + controller.getCompletedCount() + " | Pending: " + controller.getPendingCount());

            int filled = (int) (percentage / 10);
            System.out.print("Progress: [");
            for (int i = 0; i < 10; i++) System.out.print(i < filled ? "█" : "░");
            System.out.println("] " + String.format("%.1f%%", percentage));

            System.out.println("\n1. Mark as Done | 2. Edit Priority | 3. Delete | 4. Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    handleAction("done");
                    return; // پس از انجام کار به منوی اصلی برگردد
                case "2":
                    handleAction("edit");
                    return;
                case "3":
                    handleAction("delete");
                    return;
                case "4":
                    return; // خروج از متد و برگشت به منوی قبلی
                default:
                    System.out.println("❌ Invalid choice! Please enter a number between 1 and 4.");
            }
        }
    }

    private void handleAction(String action) {
        // تغییر مهم: برای تطابق ایندکس کاربر، لیست نمایش داده شده و مرتب شده (showPrioritizedTasks) را مبنا قرار می‌دهیم.
        List<Task> sortedTasks = controller.showPrioritizedTasks();
        if (sortedTasks.isEmpty()) {
            System.out.println("No tasks available.");
            return;
        }

        System.out.println("\nSelect task to perform action:");
        for (int i = 0; i < sortedTasks.size(); i++) {
            Task t = sortedTasks.get(i);
            String status = t.isDone() ? "[✅ DONE]" : "[⏳ PENDING]";
            System.out.println((i + 1) + ". " + status + " " + t.getTitle() + " (Prio: " + t.getPriority() + ")");
        }

        int num = InputValidator.getIntInput("Enter task number (0 to cancel): ");
        if (num == 0) return;

        if (num < 1 || num > sortedTasks.size()) {
            System.out.println("Invalid task number.");
            return;
        }

        Task selectedTask = sortedTasks.get(num - 1);
        String taskTitle = selectedTask.getTitle();

        if (action.equals("done")) {
            controller.markTaskAsDone(taskTitle);
            System.out.println("✅ Status updated.");
        } else if (action.equals("delete")) {
            controller.deleteTask(taskTitle);
            System.out.println("🗑️ Task deleted.");
        } else if (action.equals("edit")) {
            int p = InputValidator.getIntInput("Enter new priority (1-5): ");
            if (p >= 1 && p <= 5) {
                controller.updateTaskPriority(taskTitle, p);
                System.out.println("✅ Priority updated.");
            } else {
                System.out.println("Invalid priority. Please enter a number between 1 and 5.");
            }
        }
    }
}
