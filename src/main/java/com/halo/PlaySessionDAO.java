package com.halo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PlaySessionDAO {

    // ─────────────────────────────────────────────
    // INSERT
    // ─────────────────────────────────────────────

    public int insertSession(PlaySession session) {
        String sql = "INSERT INTO play_session (map_id, game_type_id, variant_name, " +
                     "player_count, date_played, notes) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, session.getMapId());
            pstmt.setInt(2, session.getGameTypeId());
            pstmt.setString(3, session.getVariantName());
            pstmt.setInt(4, session.getPlayerCount());
            pstmt.setString(5, session.getDatePlayed());
            pstmt.setString(6, session.getNotes());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error inserting session: " + e.getMessage());
        }
        return -1;
    }

    // ─────────────────────────────────────────────
    // FETCH ALL FOR A MAP
    // ─────────────────────────────────────────────

    public List<PlaySession> getSessionsForMap(int mapId) {
        String sql = "SELECT * FROM play_session WHERE map_id = ? " +
                     "ORDER BY date_played DESC";
        List<PlaySession> sessions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mapId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                sessions.add(buildSession(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching sessions for map: " + e.getMessage());
        }
        return sessions;
    }

    // ─────────────────────────────────────────────
    // COUNT PLAYS FOR A MAP
    // ─────────────────────────────────────────────

    public int getPlayCountForMap(int mapId) {
        String sql = "SELECT COUNT(*) FROM play_session WHERE map_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mapId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Error counting plays: " + e.getMessage());
        }
        return 0;
    }

    // ─────────────────────────────────────────────
    // COUNT PLAYS IN LAST N DAYS
    // ─────────────────────────────────────────────

    public int getPlayCountInLastDays(int mapId, int days) {
        String sql = "SELECT COUNT(*) FROM play_session WHERE map_id = ? " +
                     "AND date_played >= date('now', '-" + days + " days')";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mapId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Error counting recent plays: " + e.getMessage());
        }
        return 0;
    }

    // ─────────────────────────────────────────────
    // GET LAST PLAYED DATE FOR A MAP
    // ─────────────────────────────────────────────

    public String getLastPlayedDate(int mapId) {
        String sql = "SELECT MAX(date_played) FROM play_session WHERE map_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mapId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString(1);

        } catch (SQLException e) {
            System.err.println("Error fetching last played date: " + e.getMessage());
        }
        return null;
    }

    // ─────────────────────────────────────────────
    // DELETE A SESSION
    // ─────────────────────────────────────────────

    public void deleteSession(int sessionId) {
        String sql = "DELETE FROM play_session WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error deleting session: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // GET ALL SESSIONS (for snapshot/stats)
    // ─────────────────────────────────────────────

    public List<PlaySession> getAllSessions() {
        String sql = "SELECT * FROM play_session ORDER BY date_played DESC";
        List<PlaySession> sessions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                sessions.add(buildSession(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all sessions: " + e.getMessage());
        }
        return sessions;
    }

    // ─────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────

    private PlaySession buildSession(ResultSet rs) throws SQLException {
        return new PlaySession(
                rs.getInt("id"),
                rs.getInt("map_id"),
                rs.getInt("game_type_id"),
                rs.getString("variant_name"),
                rs.getInt("player_count"),
                rs.getString("date_played"),
                rs.getString("notes")
        );
    }
}