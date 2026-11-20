
import controller.TaskController;
import repository.DatabaseManager;
import repository.TaskRepository;
import service.TaskService;
import view.ConsoleView;
import util.DailyResetManager;

public class Main {
    public static void main(String[] args) {

        TaskRepository repo = DatabaseManager.getInstance();
        TaskService service = new TaskService(repo);
        TaskController controller = new TaskController(service);

        ConsoleView view = new ConsoleView(controller);

        if (DailyResetManager.isNewDay()) {
            controller.resetCompletedTasks();
            DailyResetManager.updateLastResetDate();
        }

        view.start();
    }
}
