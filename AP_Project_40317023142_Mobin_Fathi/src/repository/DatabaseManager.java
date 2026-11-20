package repository;

import model.Task;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager implements TaskRepository {

    private static final String FILE_PATH = "tasks.txt";

    // الگوی Singleton ایمن در محیط چندنخی
    private DatabaseManager() {
        // ایجاد فایل در صورتی که وجود نداشته باشد
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Failed to create tasks.txt file: " + e.getMessage());
            }
        }
    }

    private static class Holder {
        private static final DatabaseManager INSTANCE = new DatabaseManager();
    }

    public static DatabaseManager getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void addTask(Task task) {
        List<Task> tasks = loadData();
        tasks.add(task);
        saveData(tasks);
    }

    @Override
    public List<Task> getAllTasks() {
        return loadData();
    }

    @Override
    public void updateTasks(List<Task> tasks) {
        saveData(tasks);
    }

    // متد ذخیره اطلاعات در فایل با رعایت اصول
    private void saveData(List<Task> tasks) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH, false)))) {
            for (Task task : tasks) {
                // فرمت ذخیره‌سازی: Title,Priority,Deadline,IsDone,Owner
                writer.println(task.getTitle() + ","
                        + task.getPriority() + ","
                        + task.getDeadline() + ","
                        + task.isDone() + ","
                        + task.getOwner());
            }
        } catch (IOException e) {
            System.err.println("DatabaseManager: Error writing to data file: " + e.getMessage());
        }
    }

    // متد خواندن اطلاعات از فایل با رعایت بهینه‌سازی Heap با StringBuilder
    private List<Task> loadData() {
        List<Task> tasks = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return tasks;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // استفاده از StringBuilder برای پارس ایمن تر رشته‌ها
                StringBuilder sb = new StringBuilder(line);
                String[] parts = sb.toString().split(",");
                if (parts.length == 5) {
                    String title = parts[0];
                    int priority = Integer.parseInt(parts[1]);
                    LocalDate deadline = LocalDate.parse(parts[2]);
                    boolean isDone = Boolean.parseBoolean(parts[3]);
                    String owner = parts[4];

                    Task task = new Task(title, priority, deadline, owner);
                    task.setDone(isDone);
                    tasks.add(task);
                }
            }
        } catch (IOException e) {
            System.err.println("DatabaseManager: Error reading from data file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("DatabaseManager: Error parsing tasks data: " + e.getMessage());
        }
        return tasks;
    }
}
