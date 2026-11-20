import controller.TaskController;
import exception.InvalidTaskException;
import model.Task;
import repository.InMemoryTaskRepository;
import repository.TaskRepository;
import service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskControllerTest {

    private TaskController controller;
    private TaskService service;

    @BeforeEach
    void setUp() {
        // استفاده از ریپازیتوری در حافظه برای تست که به فایل وابسته نباشد
        TaskRepository repo = new InMemoryTaskRepository();
        service = new TaskService(repo);
        controller = new TaskController(service);

        // ثبت‌نام و ورود کاربر تستی
        controller.register("testUser", "pass123");
        controller.login("testUser", "pass123");
    }

    // تست ۱: بررسی اینکه تسک برای کاربر فعلی ثبت می‌شود
    @Test
    void testGetTasksByOwnerFilter() throws InvalidTaskException {
        controller.addTask("Task 1", 3, LocalDate.now());
        List<Task> tasks = controller.getAllTasks();

        assertFalse(tasks.isEmpty(), "Tasks should not be empty");
        assertEquals("testUser", tasks.get(0).getOwner(), "Owner should be testUser");
    }

    // تست ۲: حالت مرزی - اولویت غیرمجاز
    @Test
    void testAddTaskWithInvalidPriority() {
        assertThrows(InvalidTaskException.class, () ->
                        controller.addTask("Invalid Task", 10, LocalDate.now()),
                "Should throw exception for priority > 5");
    }

    // تست ۳: محاسبه درصد پیشرفت
    @Test
    void testCompletionPercentage() throws InvalidTaskException {
        controller.addTask("Task 1", 1, LocalDate.now());
        controller.addTask("Task 2", 2, LocalDate.now());

        controller.markTaskAsDone("Task 1");

        double percentage = controller.getCompletionPercentage();
        assertEquals(50.0, percentage, "Percentage should be 50% when 1 out of 2 tasks is done");
    }

    // تست ۴: مرتب‌سازی اولویت‌محور
    @Test
    void testShowPrioritizedTasks() throws InvalidTaskException {
        controller.addTask("Low Prio", 1, LocalDate.now());
        controller.addTask("High Prio", 5, LocalDate.now());

        List<Task> sortedTasks = controller.showPrioritizedTasks();

        // با توجه به منطق پروژه: High Priority باید اول باشد
        assertEquals("High Prio", sortedTasks.get(0).getTitle(), "First task should be High Prio");
        assertEquals("Low Prio", sortedTasks.get(1).getTitle(), "Second task should be Low Prio");
    }

    // تست ۵: حذف تسک
    @Test
    void testDeleteTask() throws InvalidTaskException {
        controller.addTask("Delete Me", 3, LocalDate.now());
        controller.deleteTask("Delete Me");

        List<Task> tasks = controller.getAllTasks();
        assertTrue(tasks.isEmpty(), "Task list should be empty after deletion");
    }
}
