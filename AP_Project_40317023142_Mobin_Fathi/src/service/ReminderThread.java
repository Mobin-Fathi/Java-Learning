package service;

import model.Task;
import model.User;
import java.util.List;

public class ReminderThread implements Runnable {
    private final User currentUser;
    private final TaskService taskService;

    public ReminderThread(User currentUser, TaskService taskService) {
        this.currentUser = currentUser;
        this.taskService = taskService;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // ۱۰ ثانیه انتظار
                Thread.sleep(10000);

                // متد getTasksByOwner در کلاس TaskService به صورت خودکار نخ‌ایمن است
                // و نیازی نیست اینجا مجدداً قفل بگیریم تا از بروز Deadlock جلوگیری شود.
                List<Task> userTasks = taskService.getTasksByOwner(currentUser.getUsername());

                for (Task task : userTasks) {
                    if (!task.isDone() && (task.getPriority() == 4 || task.getPriority() == 5)) {
                        System.out.println("\n[⚠️ WARNING] Task '" + task.getTitle()
                                + "' with high priority (" + task.getPriority() + ") is pending!");
                    }
                }
            }
        } catch (InterruptedException e) {
            // ترد به آرامی و با موفقیت قطع/متوقف شد.
        }
    }
}
