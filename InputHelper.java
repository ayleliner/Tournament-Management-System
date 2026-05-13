import java.util.Scanner;

public class InputHelper {

    private static final Scanner sc = new Scanner(System.in);

    public static String getString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    public static int getInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid input. Please enter a whole number.");
            }
        }
    }

    public static int getIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = getInt(prompt);
            if (value >= min && value <= max) return value;
            System.out.println("[!] Please enter a number between " + min + " and " + max + ".");
        }
    }

    public static String getDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (YYYY-MM-DD): ");
            String input = sc.nextLine().trim();
            if (input.matches("\\d{4}-\\d{2}-\\d{2}")) return input;
            System.out.println("[!] Invalid date format. Use YYYY-MM-DD (e.g., 2025-05-15).");
        }
    }

    public static int getYear(String prompt) {
        while (true) {
            int year = getInt(prompt + " (e.g., 2025): ");
            if (year >= 2000 && year <= 2100) return year;
            System.out.println("[!] Please enter a valid year (2000–2100).");
        }
    }

    public static boolean confirm(String prompt) {
        while (true) {
            System.out.print(prompt + " (yes/no): ");
            String input = sc.nextLine().trim().toLowerCase();
            if (input.equals("yes") || input.equals("y")) return true;
            if (input.equals("no")  || input.equals("n")) return false;
            System.out.println("[!] Type 'yes' or 'no'.");
        }
    }
    
    public static int getScore(String prompt) {
        while (true) {
            int score = getInt(prompt);
            if (score >= 0) return score;
            System.out.println("[!] Score cannot be negative. Please enter 0 or higher.");
        }
    }
    
    public static String getResult(String prompt) {
        while (true) {
            System.out.print(prompt + " (WIN/DRAW/LOSS): ");
            String input = sc.nextLine().trim().toUpperCase();
            if (input.equals("WIN") || input.equals("DRAW") || input.equals("LOSS")) return input;
            System.out.println("[!] Invalid result. Please enter WIN, DRAW, or LOSS only.");
        }
    }
}
