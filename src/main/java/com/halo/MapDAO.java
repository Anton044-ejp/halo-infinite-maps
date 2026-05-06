package com.halo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MapDAO {

    // ─────────────────────────────────────────────
    // INSERT
    // ─────────────────────────────────────────────

    public int insertMap(Map map) {
        String sql = "INSERT INTO map (name, size, min_players, max_players, lighting, " +
                     "image_filename, description, waypoint_link, custom_modes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, map.getName());
            pstmt.setString(2, map.getSize());
            pstmt.setInt(3, map.getMinPlayers());
            pstmt.setInt(4, map.getMaxPlayers());
            pstmt.setString(5, map.getLighting());
            pstmt.setString(6, map.getImageFilename());
            pstmt.setString(7, map.getDescription());
            pstmt.setString(8, map.getWaypointLink());
            pstmt.setString(9, map.getCustomModes());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);  // return the new map's id
            }

        } catch (SQLException e) {
            System.err.println("Error inserting map: " + e.getMessage());
        }
        return -1;
    }

    // ─────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────

    public void updateMap(Map map) {
        String sql = "UPDATE map SET name=?, size=?, min_players=?, max_players=?, " +
                     "lighting=?, image_filename=?, description=?, waypoint_link=?, " +
                     "custom_modes=?, is_active=? WHERE id=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, map.getName());
            pstmt.setString(2, map.getSize());
            pstmt.setInt(3, map.getMinPlayers());
            pstmt.setInt(4, map.getMaxPlayers());
            pstmt.setString(5, map.getLighting());
            pstmt.setString(6, map.getImageFilename());
            pstmt.setString(7, map.getDescription());
            pstmt.setString(8, map.getWaypointLink());
            pstmt.setString(9, map.getCustomModes());
            pstmt.setInt(10, map.isActive() ? 1 : 0);
            pstmt.setInt(11, map.getId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating map: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // DEACTIVATE (soft delete)
    // ─────────────────────────────────────────────

    public void deactivateMap(int mapId) {
        String sql = "UPDATE map SET is_active = 0 WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mapId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error deactivating map: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // FETCH ONE
    // ─────────────────────────────────────────────

    public Map getMapById(int id) {
        String sql = "SELECT * FROM map WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return buildMap(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching map: " + e.getMessage());
        }
        return null;
    }

    // ─────────────────────────────────────────────
    // FETCH ALL
    // ─────────────────────────────────────────────

    public List<Map> getAllMaps() {
        String sql = "SELECT * FROM map WHERE is_active = 1 ORDER BY name ASC";
        List<Map> maps = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                maps.add(buildMap(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all maps: " + e.getMessage());
        }
        return maps;
    }

    // ─────────────────────────────────────────────
    // SEARCH — core filter query
    // ─────────────────────────────────────────────

    public List<Map> searchMaps(int playerCount, int gameTypeId, boolean ignoreParityRule) {
        List<Map> maps = new ArrayList<>();

        // Build parity clause based on odd/even and checkbox
        String parityClause;
        if (ignoreParityRule) {
            parityClause = "1=1";  // no parity filter
        } else if (playerCount % 2 == 0) {
            parityClause = "(gt.player_parity = 'EVEN_ONLY' OR gt.player_parity = 'BOTH')";
        } else {
            parityClause = "(gt.player_parity = 'ODD_ONLY' OR gt.player_parity = 'BOTH')";
        }

        String sql = "SELECT DISTINCT m.* FROM map m " +
                     "JOIN map_game_type mgt ON m.id = mgt.map_id " +
                     "JOIN game_type gt ON mgt.game_type_id = gt.id " +
                     "WHERE m.is_active = 1 " +
                     "AND m.min_players <= ? " +
                     "AND m.max_players >= ? " +
                     "AND mgt.game_type_id = ? " +
                     "AND " + parityClause + " " +
                     "ORDER BY m.name ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playerCount);
            pstmt.setInt(2, playerCount);
            pstmt.setInt(3, gameTypeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                maps.add(buildMap(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error searching maps: " + e.getMessage());
        }
        return maps;
    }
    
    // ─────────────────────────────────────────────
    // SEARCH MAPS BY NAME
    // ─────────────────────────────────────────────
    
    public List<Map> searchMapsByName(String name) {
        String sql = "SELECT * FROM map WHERE is_active = 1 AND LOWER(name) LIKE LOWER(?) ORDER BY name ASC";
        List<Map> maps = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                maps.add(buildMap(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error searching maps by name: " + e.getMessage());
        }
        return maps;
    }
    
    // ─────────────────────────────────────────────
    // GAMETYPES FOR A MAP
    // ─────────────────────────────────────────────

    public List<GameType> getGameTypesForMap(int mapId) {
        String sql = "SELECT gt.* FROM game_type gt " +
                     "JOIN map_game_type mgt ON gt.id = mgt.game_type_id " +
                     "WHERE mgt.map_id = ? ORDER BY gt.name ASC";
        List<GameType> gameTypes = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mapId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                gameTypes.add(new GameType(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("is_pvp") == 1,
                        rs.getString("player_parity")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching gametypes for map: " + e.getMessage());
        }
        return gameTypes;
    }

    // ─────────────────────────────────────────────
    // LINK GAMETYPE TO MAP
    // ─────────────────────────────────────────────

    public void addGameTypeToMap(int mapId, int gameTypeId) {
        String sql = "INSERT OR IGNORE INTO map_game_type (map_id, game_type_id) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mapId);
            pstmt.setInt(2, gameTypeId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error linking gametype to map: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // REMOVE GAMETYPE FROM MAP
    // ─────────────────────────────────────────────

    public void removeGameTypeFromMap(int mapId, int gameTypeId) {
        String sql = "DELETE FROM map_game_type WHERE map_id = ? AND game_type_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mapId);
            pstmt.setInt(2, gameTypeId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error removing gametype from map: " + e.getMessage());
        }
    }
    
    // ─────────────────────────────────────────────
    // REACTIVATE MAP
    // ─────────────────────────────────────────────
    public List<Map> getInactiveMaps() {
        String sql = "SELECT * FROM map WHERE is_active = 0 ORDER BY name ASC";
        List<Map> maps = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                maps.add(buildMap(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching inactive maps: " + e.getMessage());
        }
        return maps;
    }

    public void reactivateMap(int mapId) {
        String sql = "UPDATE map SET is_active = 1 WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mapId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error reactivating map: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // HELPER — build a Map object from a ResultSet row
    // ─────────────────────────────────────────────

    private Map buildMap(ResultSet rs) throws SQLException {
        return new Map(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("size"),
                rs.getInt("min_players"),
                rs.getInt("max_players"),
                rs.getString("lighting"),
                rs.getString("image_filename"),
                rs.getString("description"),
                rs.getString("waypoint_link"),
                rs.getString("date_added"),
                rs.getInt("is_active") == 1,
                rs.getString("custom_modes")
        );
    }
}