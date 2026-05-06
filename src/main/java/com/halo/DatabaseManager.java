package com.halo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:halo_maps.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        createTables();
        seedGameTypes();
    }

    private static void createTables() {
        String mapTable = "CREATE TABLE IF NOT EXISTS map (" +
                "id             INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name           TEXT NOT NULL," +
                "size           TEXT NOT NULL CHECK(size IN ('Small','Medium','Large','XL'))," +
                "min_players    INTEGER NOT NULL," +
                "max_players    INTEGER NOT NULL," +
                "lighting       TEXT," +
                "image_filename TEXT," +
                "description    TEXT," +
                "waypoint_link  TEXT," +
                "date_added     TEXT NOT NULL DEFAULT (date('now'))," +
                "is_active      INTEGER NOT NULL DEFAULT 1," +
                "custom_modes   TEXT" +
                ");";

        String gameTypeTable = "CREATE TABLE IF NOT EXISTS game_type (" +
                "id             INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name           TEXT NOT NULL UNIQUE," +
                "is_pvp         INTEGER NOT NULL DEFAULT 1," +
                "player_parity  TEXT NOT NULL CHECK(player_parity IN ('EVEN_ONLY','ODD_ONLY','BOTH'))" +
                ");";

        String mapGameTypeTable = "CREATE TABLE IF NOT EXISTS map_game_type (" +
                "map_id         INTEGER NOT NULL," +
                "game_type_id   INTEGER NOT NULL," +
                "PRIMARY KEY (map_id, game_type_id)," +
                "FOREIGN KEY (map_id)       REFERENCES map(id)       ON DELETE CASCADE," +
                "FOREIGN KEY (game_type_id) REFERENCES game_type(id) ON DELETE CASCADE" +
                ");";

        String playSessionTable = "CREATE TABLE IF NOT EXISTS play_session (" +
                "id             INTEGER PRIMARY KEY AUTOINCREMENT," +
                "map_id         INTEGER NOT NULL," +
                "game_type_id   INTEGER NOT NULL," +
                "variant_name   TEXT," +
                "player_count   INTEGER NOT NULL," +
                "date_played    TEXT NOT NULL DEFAULT (date('now'))," +
                "notes          TEXT," +
                "FOREIGN KEY (map_id)       REFERENCES map(id)," +
                "FOREIGN KEY (game_type_id) REFERENCES game_type(id)" +
                ");";
        
        String gameTypeVariantTable = "CREATE TABLE IF NOT EXISTS game_type_variant (" +
                "id                 INTEGER PRIMARY KEY AUTOINCREMENT," +
                "game_type_id       INTEGER NOT NULL," +
                "official_name      TEXT NOT NULL," +
                "display_name       TEXT," +
                "description        TEXT," +
                "status             TEXT NOT NULL DEFAULT 'INVESTIGATING' " +
                "                   CHECK(status IN ('ACTIVE','INVESTIGATING','INACTIVE'))," +
                "is_slayer_adjacent INTEGER NOT NULL DEFAULT 0," +
                "notes              TEXT," +
                "date_added         TEXT NOT NULL DEFAULT (date('now'))," +
                "FOREIGN KEY (game_type_id) REFERENCES game_type(id) ON DELETE CASCADE" +
                ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(mapTable);
            stmt.execute(gameTypeTable);
            stmt.execute(mapGameTypeTable);
            stmt.execute(playSessionTable);
            stmt.execute(gameTypeVariantTable);

            System.out.println("Tables created successfully.");

        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    private static void seedGameTypes() {
        String checkSql = "SELECT COUNT(*) FROM game_type";

        String insertSql = "INSERT OR IGNORE INTO game_type (name, is_pvp, player_parity) VALUES " +
                "('Slayer (Team)',  1, 'EVEN_ONLY')," +
                "('Slayer (FFA)',   1, 'ODD_ONLY')," +
                "('CTF',           1, 'EVEN_ONLY')," +
                "('Oddball (Team)',1, 'EVEN_ONLY')," +
                "('Oddball (FFA)', 1, 'ODD_ONLY')," +
                "('KOTH',          1, 'BOTH')," +
                "('Infection',     1, 'BOTH')," +
                "('Assault',       1, 'EVEN_ONLY')," +
                "('VIP',           1, 'EVEN_ONLY')," +
                "('Attrition',     1, 'EVEN_ONLY')," +
                "('Elimination',   1, 'EVEN_ONLY')," +
                "('Escalation',    1, 'EVEN_ONLY')," +
                "('Extermination', 1, 'EVEN_ONLY')," +
                "('Extraction',    1, 'EVEN_ONLY')," +
                "('Firefight',     0, 'BOTH')," +
                "('Grifball',      1, 'EVEN_ONLY')," +
                "('Headhunter',    1, 'BOTH')," +
                "('Invasion',      1, 'EVEN_ONLY')," +
                "('Juggernaut',    1, 'BOTH')," +
                "('Land Grab',     1, 'EVEN_ONLY')," +
                "('Strongholds',   1, 'EVEN_ONLY')," +
                "('Total Control', 1, 'EVEN_ONLY');";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(checkSql);
            rs.next();
            int count = rs.getInt(1);

            if (count == 0) {
                stmt.executeUpdate(insertSql);
                System.out.println("Game types seeded successfully.");
            } else {
                System.out.println("Game types already seeded, skipping.");
            }

        } catch (SQLException e) {
            System.err.println("Error seeding game types: " + e.getMessage());
        }
    }
}