-- ============================================
--   TOURNAMENT MANAGEMENT SYSTEM
--   Database Setup Script
-- ============================================

CREATE DATABASE IF NOT EXISTS tournament_db;
USE tournament_db;

-- TEAMS TABLE
CREATE TABLE IF NOT EXISTS teams (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    created_at  DATE NOT NULL
);

-- MATCHES TABLE
CREATE TABLE IF NOT EXISTS matches (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    team1_id      INT NOT NULL,
    team2_id      INT NOT NULL,
    team1_score   INT DEFAULT NULL,
    team2_score   INT DEFAULT NULL,
    match_date    DATE NOT NULL,
    match_year    INT NOT NULL,
    status        ENUM('scheduled', 'completed') DEFAULT 'scheduled',
    FOREIGN KEY (team1_id) REFERENCES teams(id),
    FOREIGN KEY (team2_id) REFERENCES teams(id)
);

-- Sample Data (optional - you can delete this)
INSERT INTO teams (name, created_at) VALUES
('Team Alpha', '2023-01-10'),
('Team Beta',  '2023-01-10'),
('Team Gamma', '2023-01-10'),
('Team Delta', '2023-01-10');

INSERT INTO matches (team1_id, team2_id, team1_score, team2_score, match_date, match_year, status) VALUES
(1, 2, 3, 1, '2023-03-15', 2023, 'completed'),
(3, 4, 2, 2, '2023-03-16', 2023, 'completed'),
(1, 3, 1, 0, '2024-04-10', 2024, 'completed'),
(2, 4, 4, 2, '2024-04-11', 2024, 'completed'),
(1, 4, NULL, NULL, '2025-05-01', 2025, 'scheduled'),
(2, 3, NULL, NULL, '2025-05-02', 2025, 'scheduled');
