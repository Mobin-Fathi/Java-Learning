package model;

import java.time.LocalDate;
import java.util.Objects;

public class Task implements Comparable<Task> {

    private String title;
    private int priority;      // 1 تا 5
    private final LocalDate deadline;
    private boolean done;
    private final String owner;

    public Task(String title, int priority, LocalDate deadline, boolean done, String owner) {
        // اعتبارسنجی برای جلوگیری از مقادیر نامعتبر در هسته مدل
        this.title = (title != null) ? title.trim() : "Untitled Task";
        this.priority = (priority >= 1 && priority <= 5) ? priority : 3; // پیش‌فرض 3 در صورت مقدار نامعتبر
        this.deadline = (deadline != null) ? deadline : LocalDate.now();
        this.done = done;
        this.owner = (owner != null) ? owner.trim() : "unknown";
    }

    public Task(String title, int priority, LocalDate deadline, String owner) {
        this(title, priority, deadline, false, owner);
    }

    // ===== Getters =====
    public String getTitle() {
        return title;
    }

    public int getPriority() {
        return priority;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public boolean isDone() {
        return done;
    }

    public String getOwner() {
        return owner;
    }

    // ===== Setters =====
    public void setDone(boolean done) {
        this.done = done;
    }

    public void setPriority(int priority) {
        if (priority >= 1 && priority <= 5) {
            this.priority = priority;
        }
    }

    public void setTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
        }
    }

    /**
     * بهبود متد مقایسه برای PriorityQueue:
     * اولویت اول: عدد اولویت بزرگتر (۵ بالاترین اولویت).
     * اولویت دوم: تاریخ ددلاین نزدیک‌تر (صعودی بر اساس زمان).
     */
    @Override
    public int compareTo(Task other) {
        // ابتدا مقایسه بر اساس اولویت عددی (نزولی)
        int priorityCompare = Integer.compare(other.priority, this.priority);
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        // اگر اولویت‌ها برابر بودند، بر اساس تاریخ ددلاین مقایسه کن (تاریخ نزدیک‌تر اولویت دارد)
        return this.deadline.compareTo(other.deadline);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return priority == task.priority &&
                done == task.done &&
                Objects.equals(title, task.title) &&
                Objects.equals(deadline, task.deadline) &&
                Objects.equals(owner, task.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, priority, deadline, done, owner);
    }

    @Override
    public String toString() {
        return "Title: " + title +
                " | Priority: " + priority +
                " | Deadline: " + deadline +
                " | Done: " + done;
    }
}
