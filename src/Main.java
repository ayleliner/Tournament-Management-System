public class Main {

    public static void main(String[] args) {
        System.out.println("+------------------------------------------+");
        System.out.println("║   TOURNAMENT MANAGEMENT SYSTEM v1.0      ║");
        System.out.println("║   Connecting to database...              ║");
        System.out.println("+------------------------------------------+");

        //testing _db connection on startup
        try {
            DBConnection.getConnection().close();
            System.out.println("[✓] Database connected successfully!\n");
        } catch (Exception e) {
            System.out.println("[✗] Could not connect to database: " + e.getMessage());
            System.out.println("    Make sure XAMPP MySQL is running and tournament_db exists.");
            return;
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = InputHelper.getIntInRange("Enter choice: ", 1, 4);

            switch (choice) {
                case 1 -> teamsMenu();
                case 2 -> matchesMenu();
                case 3 -> statsMenu();
                case 4 -> {
                    System.out.println("\nGoodbye! 👋");
                    running = false;
                }
            }
        }
    }

    //main menu
    private static void printMainMenu() {
        System.out.println("\n+==========================================+");
        System.out.println("║              MAIN MENU                   ║");
        System.out.println("+==========================================+");
        System.out.println("║  [1]  Teams                              ║");
        System.out.println("║  [2]  Matches                            ║");
        System.out.println("║  [3]  Rankings & Records                 ║");
        System.out.println("║  [4]  Exit                               ║");
        System.out.println("╚══════════════════════════════════════════╝");
    }

    //teams submenu
    private static void teamsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n+==========================================+");
            System.out.println("║              TEAMS MENU                  ║");
            System.out.println("+==========================================+");
            System.out.println("║  [1]  Add a Team                         ║");
            System.out.println("║  [2]  View All Teams                     ║");
            System.out.println("║  [3]  Update Team Name                   ║");
            System.out.println("║  [4]  Delete a Team                      ║");
            System.out.println("║  [5]  Back to Main Menu                  ║");
            System.out.println("╚══════════════════════════════════════════╝");

            int choice = InputHelper.getIntInRange("enter choice: ", 1, 5);
            switch (choice) {
                case 1 -> CRUDOperations.addTeam();
                case 2 -> CRUDOperations.viewAllTeams();
                case 3 -> CRUDOperations.updateTeamName();
                case 4 -> CRUDOperations.deleteTeam();
                case 5 -> back = true;
            }
        }
    }

    //matches submenu
    private static void matchesMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n+==========================================+");
            System.out.println("║             MATCHES MENU                 ║");
            System.out.println("+==========================================+");
            System.out.println("║  [1]  Schedule a Match                   ║");
            System.out.println("║  [2]  View Matches                       ║");
            System.out.println("║  [3]  Update Match Score / Result        ║");
            System.out.println("║  [4]  Delete a Match Record              ║");
            System.out.println("║  [5]  Back to Main Menu                  ║");
            System.out.println("╚══════════════════════════════════════════╝");

            int choice = InputHelper.getIntInRange("enter choice: ", 1, 5);
            switch (choice) {
                case 1 -> CRUDOperations.addMatch();
                case 2 -> CRUDOperations.viewMatches();
                case 3 -> CRUDOperations.updateMatchScore();
                case 4 -> CRUDOperations.deleteMatch();
                case 5 -> back = true;
            }
        }
    }

    //this is the stats submenu will have options to view the rankings and win or loss records
    private static void statsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n+==========================================+");
            System.out.println("║         RANKINGS & RECORDS               ║");
            System.out.println("+==========================================+");
            System.out.println("║  [1]  View Rankings (by year / all-time) ║");
            System.out.println("║  [2]  View Team Win/Loss History         ║");
            System.out.println("║  [3]  Back to Main Menu                  ║");
            System.out.println("╚══════════════════════════════════════════╝");

            int choice = InputHelper.getIntInRange("enter choice: ", 1, 3);
            switch (choice) {
                case 1 -> CRUDOperations.viewRankings();
                case 2 -> CRUDOperations.viewWinLossRecord();
                case 3 -> back = true;
            }
        }
    }
}
