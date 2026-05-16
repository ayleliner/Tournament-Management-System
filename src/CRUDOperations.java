import java.sql.*;

public class CRUDOperations {

    //team operations

    public static void addTeam() {
        String name = InputHelper.getString("Enter team name: ");
        if (name.trim().isEmpty()) {
            System.out.println("[!] Team name cannot be empty or blank.");
            return;
        }   
        String date = InputHelper.getDate("Enter registration date");

        String sql = "INSERT INTO teams (name, created_at) VALUES (?, ?)";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, date);
            ps.executeUpdate();
            System.out.println("\n[✓] Team '" + name + "' added successfully!");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("[!] A team with that name already exists.");
        } catch (SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    public static void viewAllTeams() {
        String sql = "SELECT * FROM teams ORDER BY id";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║           REGISTERED TEAMS           ║");
            System.out.println("╠════╦═════════════════════╦════════════╣");
            System.out.printf("║ %-2s ║ %-19s ║ %-10s ║%n", "ID", "Team Name", "Registered");
            System.out.println("╠════╬═════════════════════╬════════════╣");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("║ %-2d ║ %-19s ║ %-10s ║%n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("created_at"));
            }
            if (!found) System.out.println("║       No teams registered yet.       ║");
            System.out.println("╚════╩═════════════════════╩════════════╝");
        } catch (SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.closeConnection(conn);
        }
    }

    public static void updateTeamName() {
        viewAllTeams();
        int id = InputHelper.getInt("\nEnter Team ID to update: ");
        String newName = InputHelper.getString("Enter new team name: ");

        String sql = "UPDATE teams SET name = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, newName);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("[✓] Team name updated successfully!");
            else          System.out.println("[!] Team ID not found.");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("[!] That team name already exists.");
        } catch (SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.closeConnection(conn);
        }
    }

    public static void deleteTeam() {
        viewAllTeams();
        int id = InputHelper.getInt("\nEnter Team ID to delete: ");
        if (!InputHelper.confirm("Are you sure you want to delete this team and ALL its match records?")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // TRANSACTION START

            // Delete related matches first (foreign key constraint)
            String deleteMatches = "DELETE FROM matches WHERE team1_id = ? OR team2_id = ?";
            PreparedStatement ps1 = conn.prepareStatement(deleteMatches);
            ps1.setInt(1, id);
            ps1.setInt(2, id);
            int matchesDeleted = ps1.executeUpdate();

            // Delete the team
            String deleteTeam = "DELETE FROM teams WHERE id = ?";
            PreparedStatement ps2 = conn.prepareStatement(deleteTeam);
            ps2.setInt(1, id);
            int rows = ps2.executeUpdate();

            if (rows > 0) {
                conn.commit(); // COMMIT
                System.out.println("[✓] Team deleted. " + matchesDeleted + " related match(es) also removed.");
                renumberTeams(); // Re-sequence IDs to stay 1,2,3...
            } else {
                conn.rollback(); // ROLLBACK
                System.out.println("[!] Team ID not found. No changes made.");
            }
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            System.out.println("[ERROR] " + e.getMessage());
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
            DBConnection.closeConnection(conn);
        }
    }

    // Re-sequences team IDs to be 1, 2, 3... after a deletion.
    // Also updates any match foreign keys so nothing breaks.
    private static void renumberTeams() {
        Connection conn = null;
        Statement st = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            st = conn.createStatement();
            st.execute("SET FOREIGN_KEY_CHECKS = 0");

            ResultSet rs = st.executeQuery("SELECT id FROM teams ORDER BY id");
            java.util.List<Integer> oldIds = new java.util.ArrayList<>();
            while (rs.next()) oldIds.add(rs.getInt("id"));
            rs.close();

            int newId = 1;
            for (int oldId : oldIds) {
                if (oldId != newId) {
                    PreparedStatement pm1 = conn.prepareStatement(
                        "UPDATE matches SET team1_id = ? WHERE team1_id = ?");
                    pm1.setInt(1, newId); pm1.setInt(2, oldId); pm1.executeUpdate(); pm1.close();

                    PreparedStatement pm2 = conn.prepareStatement(
                        "UPDATE matches SET team2_id = ? WHERE team2_id = ?");
                    pm2.setInt(1, newId); pm2.setInt(2, oldId); pm2.executeUpdate(); pm2.close();

                    PreparedStatement pt = conn.prepareStatement(
                        "UPDATE teams SET id = ? WHERE id = ?");
                    pt.setInt(1, newId); pt.setInt(2, oldId); pt.executeUpdate(); pt.close();
                }
                newId++;
            }

            st.execute("ALTER TABLE teams AUTO_INCREMENT = " + newId);
            st.execute("SET FOREIGN_KEY_CHECKS = 1");
            st.close();
            conn.commit();
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            System.out.println("[ERROR] Failed to renumber teams: " + e.getMessage());
        } finally {
            try { if (st != null) st.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
            DBConnection.closeConnection(conn);
        }
    }

    //match operations
    

    public static void addMatch() {
        viewAllTeams();
        System.out.println();
        int team1 = InputHelper.getInt("Enter Team 1 ID: ");
        int team2 = InputHelper.getInt("Enter Team 2 ID: ");

        if (team1 == team2) {
            System.out.println("[!] A team cannot play against itself.");
            return;
        }
        
        // Check for duplicate match
        String checkSql = "SELECT COUNT(*) FROM matches WHERE (team1_id = ? AND team2_id = ?) OR (team1_id = ? AND team2_id = ?)";
        Connection checkConn = null;
        try {
            checkConn = DBConnection.getConnection();
            PreparedStatement checkPs = checkConn.prepareStatement(checkSql);
            checkPs.setInt(1, team1);
            checkPs.setInt(2, team2);
            checkPs.setInt(3, team2);
            checkPs.setInt(4, team1);
            ResultSet checkRs = checkPs.executeQuery();
            if (checkRs.next() && checkRs.getInt(1) > 0) {
               System.out.println("[!] This match between these two teams already exists.");
               return;
            }
    } catch (SQLException e) {
        System.out.println("[ERROR] " + e.getMessage());
    } finally {
        DBConnection.closeConnection(checkConn);
    }

        String date = InputHelper.getDate("Enter match date");
      
        // Validate match date is not in the past
        java.time.LocalDate matchDate = java.time.LocalDate.parse(date);
        if (matchDate.isBefore(java.time.LocalDate.now())) {
           System.out.println("[!] Match date cannot be in the past.");
           return;
        }

        int year    = Integer.parseInt(date.substring(0, 4));

        String sql = "INSERT INTO matches (team1_id, team2_id, match_date, match_year, status) VALUES (?, ?, ?, ?, 'scheduled')";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, team1);
            ps.setInt(2, team2);
            ps.setString(3, date);
            ps.setInt(4, year);
            ps.executeUpdate();
            System.out.println("[✓] Match scheduled successfully!");
        } catch (SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    public static void viewMatches() {
        System.out.println("\n--- View Options ---");
        System.out.println("[1] All matches");
        System.out.println("[2] By year");
        System.out.println("[3] Scheduled only");
        System.out.println("[4] Completed only");
        int choice = InputHelper.getIntInRange("Choose: ", 1, 4);

        StringBuilder sql = new StringBuilder(
            "SELECT m.id, t1.name AS team1, t2.name AS team2, " +
            "m.team1_score, m.team2_score, m.match_date, m.match_year, m.status " +
            "FROM matches m " +
            "JOIN teams t1 ON m.team1_id = t1.id " +
            "JOIN teams t2 ON m.team2_id = t2.id"
        );

        int yearFilter = 0;
        if (choice == 2) {
            yearFilter = InputHelper.getYear("Enter year to filter");
            sql.append(" WHERE m.match_year = ?");
        } else if (choice == 3) {
            sql.append(" WHERE m.status = 'scheduled'");
        } else if (choice == 4) {
            sql.append(" WHERE m.status = 'completed'");
        }
        sql.append(" ORDER BY m.match_date DESC");

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            if (choice == 2) ps.setInt(1, yearFilter);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n╔═══╦══════════════════════╦══════════════════════╦═══════╦═══════╦════════════╦═══════════╗");
            System.out.printf("║%-3s║%-22s║%-22s║%-7s║%-7s║%-12s║%-11s║%n",
                "ID", " Team 1", " Team 2", " T1 SC", " T2 SC", " Date", " Status");
            System.out.println("╠═══╬══════════════════════╬══════════════════════╬═══════╬═══════╬════════════╬═══════════╣");

            boolean found = false;
            while (rs.next()) {
                found = true;
                String sc1 = rs.getObject("team1_score") == null ? " -" : " " + rs.getInt("team1_score");
                String sc2 = rs.getObject("team2_score") == null ? " -" : " " + rs.getInt("team2_score");
                System.out.printf("║%-3d║ %-21s║ %-21s║%-7s║%-7s║ %-11s║ %-10s║%n",
                    rs.getInt("id"),
                    rs.getString("team1"),
                    rs.getString("team2"),
                    sc1, sc2,
                    rs.getString("match_date"),
                    rs.getString("status"));
            }
            if (!found) System.out.println("║                         No matches found.                                          ║");
            System.out.println("╚═══╩══════════════════════╩══════════════════════╩═══════╩═══════╩════════════╩═══════════╝");
        } catch (SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    public static void updateMatchScore() {
        viewMatches();
        int matchId = InputHelper.getInt("\nEnter Match ID to update score: ");
        int score1  = InputHelper.getInt("Enter Team 1 score: ");
        int score2  = InputHelper.getInt("Enter Team 2 score: ");

        String sql = "UPDATE matches SET team1_score = ?, team2_score = ?, status = 'completed' WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, score1);
            ps.setInt(2, score2);
            ps.setInt(3, matchId);
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("[✓] Score updated! Match marked as completed.");
            else          System.out.println("[!] Match ID not found.");
        } catch (SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    public static void deleteMatch() {
        viewMatches();
        int id = InputHelper.getInt("\nEnter Match ID to delete: ");
        if (!InputHelper.confirm("Delete this match record?")) {
            System.out.println("Cancelled.");
            return;
        }

        String sql = "DELETE FROM matches WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("[✓] Match deleted successfully.");
            else          System.out.println("[!] Match ID not found.");
        } catch (SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    
    //rankings and win or lose records
    

    public static void viewRankings() {
        int year;
        while (true) {
            year = InputHelper.getInt("Enter year for rankings (or 0 for all-time): ");
            if (year == 0 || (year >= 2000 && year <= 2100)) break;
            System.out.println("[!] Please enter a valid year (2000-2100) or 0 for all-time.");
        }

        String sql =
            "SELECT t.name, " +
            "  SUM(CASE WHEN (m.team1_id = t.id AND m.team1_score > m.team2_score) OR " +
            "                (m.team2_id = t.id AND m.team2_score > m.team1_score) THEN 1 ELSE 0 END) AS wins, " +
            "  SUM(CASE WHEN (m.team1_id = t.id AND m.team1_score < m.team2_score) OR " +
            "                (m.team2_id = t.id AND m.team2_score < m.team1_score) THEN 1 ELSE 0 END) AS losses, " +
            "  SUM(CASE WHEN (m.team1_id = t.id OR m.team2_id = t.id) AND m.team1_score = m.team2_score THEN 1 ELSE 0 END) AS draws, " +
            "  SUM(CASE WHEN (m.team1_id = t.id AND m.team1_score > m.team2_score) OR " +
            "                (m.team2_id = t.id AND m.team2_score > m.team1_score) THEN 3 " +
            "       WHEN (m.team1_id = t.id OR m.team2_id = t.id) AND m.team1_score = m.team2_score THEN 1 ELSE 0 END) AS points " +
            "FROM teams t " +
            "LEFT JOIN matches m ON (m.team1_id = t.id OR m.team2_id = t.id) AND m.status = 'completed' " +
            (year > 0 ? "AND m.match_year = ? " : "") +
            "GROUP BY t.id, t.name " +
            "ORDER BY points DESC, wins DESC";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            if (year > 0) ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();

            String title = year > 0 ? "RANKINGS — " + year : "ALL-TIME RANKINGS";
            System.out.println("\n╔═════════════════════════════════════════════════╗");
            System.out.printf("║  %-47s║%n", title);
            System.out.println("╠════╦════════════════════╦═════╦══════╦═════╦══════╣");
            System.out.printf("║%-4s║ %-19s║%-5s║%-6s║%-5s║%-6s║%n", "RANK", "Team", "W", "L", "D", "PTS");
            System.out.println("╠════╬════════════════════╬═════╬══════╬═════╬══════╣");

            int rank = 1;
            while (rs.next()) {
                System.out.printf("║ %-3d║ %-19s║ %-4d║ %-5d║ %-4d║ %-5d║%n",
                    rank++,
                    rs.getString("name"),
                    rs.getInt("wins"),
                    rs.getInt("losses"),
                    rs.getInt("draws"),
                    rs.getInt("points"));
            }
            System.out.println("╚════╩════════════════════╩═════╩══════╩═════╩══════╝");
            System.out.println("  Points: Win = 3pts | Draw = 1pt | Loss = 0pts");
        } catch (SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    public static void viewWinLossRecord() {
        viewAllTeams();
        int teamId = InputHelper.getInt("\nEnter Team ID to view full record: ");

        String sql =
            "SELECT m.match_year, m.match_date, " +
            "  t1.name AS team1, t2.name AS team2, " +
            "  m.team1_score, m.team2_score, m.status, " +
            "  m.team1_id, m.team2_id " +
            "FROM matches m " +
            "JOIN teams t1 ON m.team1_id = t1.id " +
            "JOIN teams t2 ON m.team2_id = t2.id " +
            "WHERE (m.team1_id = ? OR m.team2_id = ?) AND m.status = 'completed' " +
            "ORDER BY m.match_date DESC";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();

            // Get team name
            PreparedStatement namePs = conn.prepareStatement("SELECT name FROM teams WHERE id = ?");
            namePs.setInt(1, teamId);
            ResultSet nameRs = namePs.executeQuery();
            if (!nameRs.next()) { System.out.println("[!] Team not found."); return; }
            String teamName = nameRs.getString("name");

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, teamId);
            ps.setInt(2, teamId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n  Win/Loss Record for: " + teamName);
            System.out.println("  ─────────────────────────────────────────────────────");
            System.out.printf("  %-6s %-12s %-22s %-8s %-6s%n", "Year", "Date", "Opponent", "Score", "Result");
            System.out.println("  ─────────────────────────────────────────────────────");

            int wins = 0, losses = 0, draws = 0;
            while (rs.next()) {
                boolean isTeam1 = rs.getInt("team1_id") == teamId;
                String opponent = isTeam1 ? rs.getString("team2") : rs.getString("team1");
                int myScore  = isTeam1 ? rs.getInt("team1_score") : rs.getInt("team2_score");
                int oppScore = isTeam1 ? rs.getInt("team2_score") : rs.getInt("team1_score");

                String result;
                if      (myScore > oppScore) { result = "WIN";  wins++;   }
                else if (myScore < oppScore) { result = "LOSS"; losses++; }
                else                         { result = "DRAW"; draws++;  }

                System.out.printf("  %-6d %-12s %-22s %d - %-5d %-6s%n",
                    rs.getInt("match_year"),
                    rs.getString("match_date"),
                    opponent, myScore, oppScore, result);
            }
            System.out.println("  ─────────────────────────────────────────────────────");
            System.out.printf("  TOTAL →  Wins: %d | Losses: %d | Draws: %d%n", wins, losses, draws);
        } catch (SQLException e) {	
            System.out.println("[ERROR] " + e.getMessage());
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
}
