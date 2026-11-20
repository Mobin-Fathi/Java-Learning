package repository;

import model.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskRepository implements TaskRepository {

    // کلید String (نام کاربری مالک) و مقدار لیستی از کارهای او
    private final Map<String, ArrayList<Task>> userTasksMap = new HashMap<>();

    @Override
    public void addTask(Task task) {
        String owner = task.getOwner();
        if (owner == null) return;
        userTasksMap.putIfAbsent(owner, new ArrayList<>());
        userTasksMap.get(owner).add(task);
    }

    @Override
    public List<Task> getAllTasks() {
        List<Task> allTasks = new ArrayList<>();
        for (ArrayList<Task> tasks : userTasksMap.values()) {
            if (tasks != null) {
                allTasks.addAll(tasks);
            }
        }
        return allTasks;
    }

    @Override
    public void updateTasks(List<Task> updatedTasks) {
        userTasksMap.clear();
        if (updatedTasks != null) {
            for (Task task : updatedTasks) {
                addTask(task);
            }
        }
    }
}
