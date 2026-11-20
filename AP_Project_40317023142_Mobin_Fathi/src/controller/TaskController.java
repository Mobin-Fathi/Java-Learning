package controller;

import model.Task;
import service.TaskService;
import exception.InvalidTaskException;
import model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class TaskController {

    private final TaskService taskService;
    private User currentUser;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    public boolean register(String username, String password) {
        if (taskService.registerUser(username, password)) {
            this.currentUser = new User(username, password);
            return true;
        }
        return false;
    }

    public boolean login(String username, String password) {
        if (taskService.loginUser(username, password)) {
            this.currentUser = new User(username, password);
            return true;
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getCurrentUser() {
        return (currentUser != null) ? currentUser.getUsername() : null;
    }

    public User getCurrentUserObject() {
        return this.currentUser;
    }

    public TaskService getTaskService() {
        return this.taskService;
    }

    public void addTask(String title, int priority, LocalDate deadline) throws InvalidTaskException {
        if (currentUser == null) {
            throw new InvalidTaskException("No user is currently logged in!");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidTaskException("Task title cannot be empty!");
        }
        if (priority < 1 || priority > 5) {
            throw new InvalidTaskException("Priority must be between 1 and 5!");
        }
        if (deadline == null) {
            throw new InvalidTaskException("Deadline cannot be null!");
        }

        Task task = new Task(title, priority, deadline, currentUser.getUsername());
        taskService.addTask(task);
    }

    public List<Task> getAllTasks() {
        if (currentUser == null) {
            return Collections.emptyList();
        }
        List<Task> tasks = taskService.getTasksByOwner(currentUser.getUsername());
        return Collections.unmodifiableList(new ArrayList<>(tasks));
    }

    public void markTaskAsDone(String title) {
        if (currentUser != null) {
            taskService.markTaskAsDone(title, currentUser.getUsername());
        }
    }

    public void deleteTask(String title) {
        if (currentUser != null) {
            taskService.deleteTaskByTitle(title, currentUser.getUsername());
        }
    }

    public void updateTaskPriority(String title, int newPriority) {
        if (currentUser != null) {
            try {
                taskService.updateTaskPriority(title, currentUser.getUsername(), newPriority);
            } catch (InvalidTaskException e) {
                throw new RuntimeException("Failed to update priority: " + e.getMessage(), e);
            }
        }
    }

    public double getCompletionPercentage() {
        if (currentUser == null) return 0.0;
        return taskService.getCompletionPercentageByOwner(currentUser.getUsername());
    }

    public int getCompletedCount() {
        if (currentUser == null) return 0;
        return taskService.getCompletedCountByOwner(currentUser.getUsername());
    }

    public int getPendingCount() {
        if (currentUser == null) return 0;
        return taskService.getPendingCountByOwner(currentUser.getUsername());
    }

    public void resetCompletedTasks() {
        if (currentUser != null) {
            taskService.resetCompletedTasks();
        }
    }

    public List<Task> showPrioritizedTasks() {
        if (currentUser == null) {
            return Collections.emptyList();
        }

        List<Task> userTasks = taskService.getTasksByOwner(this.currentUser.getUsername());
        PriorityQueue<Task> priorityQueue = new PriorityQueue<>(userTasks);
        List<Task> sortedTasks = new ArrayList<>();

        while (!priorityQueue.isEmpty()) {
            sortedTasks.add(priorityQueue.poll());
        }

        return Collections.unmodifiableList(sortedTasks);
    }
}
