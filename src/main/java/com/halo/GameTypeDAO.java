package com.halo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GameTypeDAO {

    // ─────────────────────────────────────────────
    // FETCH ALL
    // ─────────────────────────────────────────────

    public List<GameType> getAllGameTypes() {
        String sql = "SELECT * FROM game_type ORDER BY name ASC";
        List<GameType> gameTypes = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                gameTypes.add(new GameType(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("is_pvp") == 1,
                        rs.getString("player_parity")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching game types: " + e.getMessage());
        }
        return gameTypes;
    }

    // ─────────────────────────────────────────────
    // FETCH PVP ONLY
    // ─────────────────────────────────────────────

    public List<GameType> getPvpGameTypes() {
        String sql = "SELECT * FROM game_type WHERE is_pvp = 1 ORDER BY name ASC";
        List<GameType> gameTypes = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                gameTypes.add(new GameType(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("is_pvp") == 1,
                        rs.getString("player_parity")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching PvP game types: " + e.getMessage());
        }
        return gameTypes;
    }

    // ─────────────────────────────────────────────
    // FETCH PVE ONLY (Firefight etc)
    // ─────────────────────────────────────────────

    public List<GameType> getPveGameTypes() {
        String sql = "SELECT * FROM game_type WHERE is_pvp = 0 ORDER BY name ASC";
        List<GameType> gameTypes = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                gameTypes.add(new GameType(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("is_pvp") == 1,
                        rs.getString("player_parity")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching PvE game types: " + e.getMessage());
        }
        return gameTypes;
    }

    // ─────────────────────────────────────────────
    // FETCH ONE BY ID
    // ─────────────────────────────────────────────

    public GameType getGameTypeById(int id) {
        String sql = "SELECT * FROM game_type WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new GameType(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("is_pvp") == 1,
                        rs.getString("player_parity")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error fetching game type by id: " + e.getMessage());
        }
        return null;
    }

    // ─────────────────────────────────────────────
    // INSERT — for adding custom gametypes later
    // ─────────────────────────────────────────────

    public int insertGameType(GameType gameType) {
        String sql = "INSERT OR IGNORE INTO game_type (name, is_pvp, player_parity) " +
                     "VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, gameType.getName());
            pstmt.setInt(2, gameType.isPvp() ? 1 : 0);
            pstmt.setString(3, gameType.getPlayerParity());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error inserting game type: " + e.getMessage());
        }
        return -1;
    }

    // ─────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────

    public void updateGameType(GameType gameType) {
        String sql = "UPDATE game_type SET name=?, is_pvp=?, player_parity=? WHERE id=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, gameType.getName());
            pstmt.setInt(2, gameType.isPvp() ? 1 : 0);
            pstmt.setString(3, gameType.getPlayerParity());
            pstmt.setInt(4, gameType.getId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating game type: " + e.getMessage());
        }
    }
}