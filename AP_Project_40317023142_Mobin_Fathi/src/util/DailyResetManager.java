package util;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DailyResetManager {
    private static final String LAST_RESET_FILE = "last_reset.txt";

    public static boolean isNewDay() {
        LocalDate today = LocalDate.now();
        LocalDate lastResetDate = readLastResetDate();

        return lastResetDate == null || !today.equals(lastResetDate);
    }

    public static void updateLastResetDate() {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(LAST_RESET_FILE, false)))) {
            writer.println(LocalDate.now());
        } catch (IOException e) {
            System.err.println("❌ Error saving last reset date: " + e.getMessage());
        }
    }

    private static LocalDate readLastResetDate() {
        File file = new File(LAST_RESET_FILE);

        if (!file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line == null || line.isBlank()) {
                return null;
            }
            return LocalDate.parse(line.trim());
        } catch (IOException | DateTimeParseException e) {
            // در صورت خراب بودن ساختار فایل تاریخ، مقدار نال برگردانده می‌شود تا دوباره تاریخ معتبر ایجاد شود.
            return null;
        }
    }
}
