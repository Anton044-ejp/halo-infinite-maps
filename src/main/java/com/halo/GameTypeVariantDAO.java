package com.halo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GameTypeVariantDAO {

    // ─────────────────────────────────────────────
    // INSERT
    // ─────────────────────────────────────────────

    public int insertVariant(GameTypeVariant variant) {
        String sql = "INSERT INTO game_type_variant (game_type_id, official_name, " +
                     "display_name, description, status, is_slayer_adjacent, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, variant.getGameTypeId());
            pstmt.setString(2, variant.getOfficialName());
            pstmt.setString(3, variant.getDisplayName());
            pstmt.setString(4, variant.getDescription());
            pstmt.setString(5, variant.getStatus());
            pstmt.setInt(6, variant.isSlayerAdjacent() ? 1 : 0);
            pstmt.setString(7, variant.getNotes());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error inserting variant: " + e.getMessage());
        }
        return -1;
    }

    // ─────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────

    public void updateVariant(GameTypeVariant variant) {
        String sql = "UPDATE game_type_variant SET game_type_id=?, official_name=?, " +
                     "display_name=?, description=?, status=?, " +
                     "is_slayer_adjacent=?, notes=? WHERE id=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, variant.getGameTypeId());
            pstmt.setString(2, variant.getOfficialName());
            pstmt.setString(3, variant.getDisplayName());
            pstmt.setString(4, variant.getDescription());
            pstmt.setString(5, variant.getStatus());
            pstmt.setInt(6, variant.isSlayerAdjacent() ? 1 : 0);
            pstmt.setString(7, variant.getNotes());
            pstmt.setInt(8, variant.getId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating variant: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────

    public void deleteVariant(int variantId) {
        String sql = "DELETE FROM game_type_variant WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, variantId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error deleting variant: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // FETCH ALL FOR A GAMETYPE
    // ─────────────────────────────────────────────

    public List<GameTypeVariant> getVariantsForGameType(int gameTypeId) {
        String sql = "SELECT * FROM game_type_variant WHERE game_type_id = ? " +
                     "ORDER BY official_name ASC";
        List<GameTypeVariant> variants = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, gameTypeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                variants.add(buildVariant(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching variants: " + e.getMessage());
        }
        return variants;
    }

    // ─────────────────────────────────────────────
    // FETCH ALL ACTIVE + INVESTIGATING FOR RANDOMIZER
    // ─────────────────────────────────────────────

    public List<GameTypeVariant> getRandomizerPool(int gameTypeId) {
        String sql = "SELECT * FROM game_type_variant " +
                     "WHERE game_type_id = ? AND status != 'INACTIVE' " +
                     "ORDER BY official_name ASC";
        List<GameTypeVariant> variants = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, gameTypeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                variants.add(buildVariant(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching randomizer pool: " + e.getMessage());
        }
        return variants;
    }

    // ─────────────────────────────────────────────
    // FETCH ALL SLAYER ADJACENT FOR RANDOMIZER
    // ─────────────────────────────────────────────

    public List<GameTypeVariant> getSlayerAdjacentPool() {
        String sql = "SELECT * FROM game_type_variant " +
                     "WHERE is_slayer_adjacent = 1 AND status != 'INACTIVE' " +
                     "ORDER BY official_name ASC";
        List<GameTypeVariant> variants = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                variants.add(buildVariant(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching slayer adjacent pool: " + e.getMessage());
        }
        return variants;
    }

    // ─────────────────────────────────────────────
    // FETCH ONE BY ID
    // ─────────────────────────────────────────────

    public GameTypeVariant getVariantById(int id) {
        String sql = "SELECT * FROM game_type_variant WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return buildVariant(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching variant by id: " + e.getMessage());
        }
        return null;
    }

    // ─────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────

    private GameTypeVariant buildVariant(ResultSet rs) throws SQLException {
        return new GameTypeVariant(
                rs.getInt("id"),
                rs.getInt("game_type_id"),
                rs.getString("official_name"),
                rs.getString("display_name"),
                rs.getString("description"),
                rs.getString("status"),
                rs.getInt("is_slayer_adjacent") == 1,
                rs.getString("notes"),
                rs.getString("date_added")
        );
    }
}