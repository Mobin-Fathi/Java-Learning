package service;

import exception.InvalidTaskException;
import exception.UserNotFoundException;
import model.Task;
import model.User;
import repository.TaskRepository;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class TaskService {

    private final Object tasksLock = new Object();
    private final TaskRepository repository;
    private final String USER_FILE = "users.txt";
    private final List<User> users = new ArrayList<>();

    public TaskService(TaskRepository repository) {
        this.repository = repository;
        loadUsersFromFile();
    }

    public Object getTasksLock() {
        return tasksLock;
    }

    // -----------------------------------------------------------------
    // TASK MANAGEMENT
    // -----------------------------------------------------------------

    public void addTask(Task task) throws InvalidTaskException {
        if (task.getPriority() < 1 || task.getPriority() > 5) {
            throw new InvalidTaskException("Priority must be between 1 and 5. Provided: " + task.getPriority());
        }
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new InvalidTaskException("Task title cannot be empty.");
        }

        synchronized (tasksLock) {
            repository.addTask(task);
        }
    }

    /**
     * Encapsulation: return a defensive copy (prevent escape reference)
     */
    public List<Task> getAllTasks() {
        synchronized (tasksLock) {
            List<Task> allTasks = repository.getAllTasks();
            return (allTasks != null) ? new ArrayList<>(allTasks) : new ArrayList<>();
        }
    }

    public List<Task> getTasksByOwner(String owner) {
        synchronized (tasksLock) {
            return getAllTasks().stream()
                    .filter(task -> task.getOwner() != null && task.getOwner().equals(owner))
                    .collect(Collectors.toList());
        }
    }

    public void updateTasks() {
        synchronized (tasksLock) {
            repository.updateTasks(getAllTasks());
        }
    }

    /**
     * Delete task by owner + title (thread-safe)
     */
    public void deleteTaskByTitle(String title, String owner) {
        synchronized (tasksLock) {

            List<Task> tasks = repository.getAllTasks();

            if (tasks != null) {
                boolean removed = tasks.removeIf(task ->
                        task.getOwner() != null &&
                                task.getOwner().equalsIgnoreCase(owner) &&
                                task.getTitle().equalsIgnoreCase(title)
                );

                if (removed) {
                    repository.updateTasks(tasks);
                }
            }
        }
    }

    /**
     * Mark task as done — rewritten using Stream API (NO for-loops)
     */
    public void markTaskAsDone(String title, String owner) {
        synchronized (tasksLock) {
            List<Task> tasks = repository.getAllTasks();

            tasks.stream()
                    .filter(t -> t.getOwner() != null &&
                            t.getOwner().equalsIgnoreCase(owner) &&
                            t.getTitle().equalsIgnoreCase(title))
                    .findFirst()
                    .ifPresent(task -> task.setDone(true));

            repository.updateTasks(tasks);
        }
    }

    /**
     * Update priority — rewritten using Stream API (NO loops)
     */
    public void updateTaskPriority(String title, String owner, int newPriority) throws InvalidTaskException {
        if (newPriority < 1 || newPriority > 5) {
            throw new InvalidTaskException("Priority must be between 1 and 5. Provided: " + newPriority);
        }

        synchronized (tasksLock) {
            List<Task> tasks = repository.getAllTasks();

            tasks.stream()
                    .filter(t -> t.getOwner() != null &&
                            t.getOwner().equalsIgnoreCase(owner) &&
                            t.getTitle().equalsIgnoreCase(title))
                    .findFirst()
                    .ifPresent(task -> task.setPriority(newPriority));

            repository.updateTasks(tasks);
        }
    }

    // ----------------------------------------------------------
    // ANALYTICS — Stream API (Phase 5 Requirement)
    // ----------------------------------------------------------

    public int getCompletedCountByOwner(String owner) {
        synchronized (tasksLock) {
            return (int) getAllTasks().stream()
                    .filter(t -> t.getOwner() != null && t.getOwner().equals(owner) && t.isDone())
                    .count();
        }
    }

    public int getPendingCountByOwner(String owner) {
        synchronized (tasksLock) {
            return (int) getAllTasks().stream()
                    .filter(t -> t.getOwner() != null && t.getOwner().equals(owner) && !t.isDone())
                    .count();
        }
    }

    /**
     * Completion percentage using Stream API only
     */
    public double getCompletionPercentageByOwner(String owner) {
        List<Task> tasks = getTasksByOwner(owner);

        if (tasks.isEmpty()) return 0.0;

        long completed = tasks.stream().filter(Task::isDone).count();

        return (completed * 100.0) / tasks.size();
    }

    /**
     * Phase 5 requirement — NO loops allowed.
     * Reset all tasks to not-done using forEach (functional).
     */
    public void resetCompletedTasks() {
        synchronized (tasksLock) {
            List<Task> tasks = getAllTasks();

            tasks.forEach(task -> task.setDone(false));

            repository.updateTasks(tasks);
        }
    }

    /**
     * REQUIRED BY PROJECT — groupingBy (Phase 5 – screenshot 330)
     */
    public Map<Boolean, List<Task>> getTasksGroupedByStatus(String owner) {
        synchronized (tasksLock) {
            return getTasksByOwner(owner).stream()
                    .collect(Collectors.groupingBy(Task::isDone));
        }
    }

    // -----------------------------------------------------------------
    // USER MANAGEMENT
    // -----------------------------------------------------------------

    public boolean registerUser(String username, String password) {
        boolean exists = users.stream()
                .anyMatch(u -> u.getUsername().equals(username));

        if (exists) return false;

        User user = new User(username, password);
        users.add(user);
        saveUserToFile(user);
        return true;
    }

    public boolean loginUser(String username, String password) {
        boolean isValid = users.stream()
                .anyMatch(u -> u.getUsername().equals(username) &&
                        u.getPassword().equals(password));

        if (!isValid) {
            throw new UserNotFoundException("Invalid username or password. Access denied.");
        }
        return true;
    }

    // -----------------------------------------------------------------
    // FILE I/O (USER STORAGE)
    // -----------------------------------------------------------------

    private void saveUserToFile(User user) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(USER_FILE, true)))) {
            out.println(user.getUsername() + "," + user.getPassword());
        } catch (IOException e) {
            System.err.println("Error saving user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadUsersFromFile() {
        File file = new File(USER_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;
            while ((line = br.readLine()) != null) {

                String[] parts = line.split(",");
                if (parts.length == 2) {
                    users.add(new User(parts[0], parts[1]));
                }
            }

        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
