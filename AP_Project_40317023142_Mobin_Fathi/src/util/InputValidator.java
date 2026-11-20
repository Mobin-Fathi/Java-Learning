package util;

import java.util.Scanner;

/**
 * یک کلاس کمکی برای بررسی و اعتبارسنجی ورودی‌های کاربر در کنسول.
 * این کلاس از بروز خطاهای رایج هنگام وارد کردن داده‌های نامعتبر جلوگیری می‌کند.
 */

public class InputValidator {
    private static Scanner scanner = new Scanner(System.in);

    // متدی برای گرفتن عدد صحیح بدون خطا
    public static int getIntInput(String message) {
        while (true) {
            try {
                System.out.print(message);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Error: Please enter a valid NUMBER.");
            }
        }
    }
}
