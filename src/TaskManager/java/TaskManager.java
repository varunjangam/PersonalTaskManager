package TaskManager.java;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String description;
    private boolean completed;
    private LocalDate dueDate;
    private Priority priority;

    public enum Priority {
        HIGH, MEDIUM, LOW
    }

    public Task(String title, String description, LocalDate dueDate, Priority priority) {
        this.title = title;
        this.description = description;
        this.completed = false;
        this.dueDate = dueDate;
        this.priority = priority;
    }

    public void markCompleted() {
        completed = true;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String toString() {
        return String.format("%s %s (Due: %s) Priority: %s - %s",
                (completed ? "[Completed]" : "[Pending]"),
                title,
                dueDate,
                priority,
                description);
    }
}

public class TaskManager {
    private static ArrayList<Task> tasks = new ArrayList<>();
    private static Scanner sc = new Scanner(System.in);

    private static final String FILE_NAME = "tasks.dat";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        loadTasks();

        boolean running = true;

        while (running) {
            System.out.println("\nPersonal Task Manager");
            System.out.println("1. Add Task");
            System.out.println("2. View Tasks (Sorted by Due Date)");
            System.out.println("3. Mark Task Completed");
            System.out.println("4. Delete Task");
            System.out.println("5. Search Tasks");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            int choice = 0;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                // Invalid input handled below
            }

            switch (choice) {
                case 1:
                    addTask();
                    break;
                case 2:
                    viewTasksSortedByDate();
                    break;
                case 3:
                    markCompleted();
                    break;
                case 4:
                    deleteTask();
                    break;
                case 5:
                    searchTasks();
                    break;
                case 6:
                    running = false;
                    saveTasks();
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        sc.close();
    }

    static void addTask() {
        System.out.print("Enter task title: ");
        String title = sc.nextLine().trim();
        System.out.print("Enter task description: ");
        String description = sc.nextLine().trim();

        LocalDate dueDate = null;
        while (dueDate == null) {
            System.out.print("Enter due date (yyyy-MM-dd): ");
            String dateInput = sc.nextLine().trim();
            try {
                dueDate = LocalDate.parse(dateInput, DATE_FORMAT);
            } catch (Exception e) {
                System.out.println("Invalid date format. Please try again.");
            }
        }

        Task.Priority priority = null;
        while (priority == null) {
            System.out.print("Enter priority (HIGH, MEDIUM, LOW): ");
            String p = sc.nextLine().trim().toUpperCase();
            try {
                priority = Task.Priority.valueOf(p);
            } catch (Exception e) {
                System.out.println("Invalid priority. Please enter HIGH, MEDIUM, or LOW.");
            }
        }

        tasks.add(new Task(title, description, dueDate, priority));
        System.out.println("Task added successfully.");
    }

    static void viewTasksSortedByDate() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
            return;
        }

        tasks.sort(Comparator.comparing(Task::getDueDate));
        System.out.println("\nTasks (sorted by due date):");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, tasks.get(i));
        }
    }

    static void markCompleted() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
            return;
        }

        viewTasksSortedByDate();
        System.out.print("Enter task number to mark as completed: ");

        int num = readTaskNumber();
        if (num >= 1 && num <= tasks.size()) {
            Task task = tasks.get(num - 1);
            if (task.isCompleted()) {
                System.out.println("Task is already completed.");
            } else {
                task.markCompleted();
                System.out.println("Task marked as completed.");
            }
        } else {
            System.out.println("Invalid task number.");
        }
    }

    static void deleteTask() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
            return;
        }

        viewTasksSortedByDate();
        System.out.print("Enter task number to delete: ");

        int num = readTaskNumber();
        if (num >= 1 && num <= tasks.size()) {
            tasks.remove(num - 1);
            System.out.println("Task deleted.");
        } else {
            System.out.println("Invalid task number.");
        }
    }

    static void searchTasks() {
        System.out.print("Enter keyword to search in title/description (leave blank to skip): ");
        String keyword = sc.nextLine().toLowerCase().trim();

        System.out.print("Filter by status (PENDING/COMPLETED/ALL): ");
        String status = sc.nextLine().toUpperCase().trim();

        if (!status.equals("PENDING") && !status.equals("COMPLETED") && !status.equals("ALL")) {
            System.out.println("Invalid status filter. Showing all tasks.");
            status = "ALL";
        }

        List<Task> results = new ArrayList<>();

        for (Task t : tasks) {
            boolean matchesKeyword = keyword.isEmpty() ||
                    t.toString().toLowerCase().contains(keyword);
            boolean matchesStatus = status.equals("ALL") ||
                    (status.equals("PENDING") && !t.isCompleted()) ||
                    (status.equals("COMPLETED") && t.isCompleted());

            if (matchesKeyword && matchesStatus) {
                results.add(t);
            }
        }

        if (results.isEmpty()) {
            System.out.println("No matching tasks found.");
        } else {
            System.out.println("Search results:");
            for (Task t : results) {
                System.out.println(t);
            }
        }
    }

    static void saveTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(tasks);
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    static void loadTasks() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            tasks = (ArrayList<Task>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading tasks: " + e.getMessage());
        }
    }

    private static int readTaskNumber() {
        int num = -1;
        try {
            num = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            num = -1;
        }
        return num;
    }
}
