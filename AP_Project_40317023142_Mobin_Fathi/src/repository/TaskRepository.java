package repository;

import model.Task;
import java.util.List;

/**
 * اینترفیس مربوط به عملیات ذخیره‌سازی وظایف.
 * این بخش قوانین کلی برای ذخیره و بازیابی اطلاعات را مشخص می‌کند.
 */
public interface TaskRepository {
    void addTask(Task task);
    List<Task> getAllTasks();
    void updateTasks(List<Task> tasks);
}
