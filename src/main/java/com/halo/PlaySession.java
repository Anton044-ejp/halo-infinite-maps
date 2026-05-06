package com.halo;

public class PlaySession {

    private int id;
    private int mapId;
    private int gameTypeId;
    private String variantName;    // e.g. "Slayer BRs", "Team Rockets"
    private int playerCount;
    private String datePlayed;
    private String notes;

    // ─────────────────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────────────────

    // Full constructor — used when loading from database
    public PlaySession(int id, int mapId, int gameTypeId, String variantName,
                       int playerCount, String datePlayed, String notes) {
        this.id = id;
        this.mapId = mapId;
        this.gameTypeId = gameTypeId;
        this.variantName = variantName;
        this.playerCount = playerCount;
        this.datePlayed = datePlayed;
        this.notes = notes;
    }

    // New session constructor — used when logging a game night
    public PlaySession(int mapId, int gameTypeId, String variantName,
                       int playerCount, String notes) {
        this.mapId = mapId;
        this.gameTypeId = gameTypeId;
        this.variantName = variantName;
        this.playerCount = playerCount;
        this.notes = notes;
    }

    // ─────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────

    public int getId()              { return id; }
    public int getMapId()           { return mapId; }
    public int getGameTypeId()      { return gameTypeId; }
    public String getVariantName()  { return variantName; }
    public int getPlayerCount()     { return playerCount; }
    public String getDatePlayed()   { return datePlayed; }
    public String getNotes()        { return notes; }

    // ─────────────────────────────────────────────
    // SETTERS
    // ─────────────────────────────────────────────

    public void setId(int id)                       { this.id = id; }
    public void setMapId(int mapId)                 { this.mapId = mapId; }
    public void setGameTypeId(int gameTypeId)       { this.gameTypeId = gameTypeId; }
    public void setVariantName(String variantName)  { this.variantName = variantName; }
    public void setPlayerCount(int playerCount)     { this.playerCount = playerCount; }
    public void setDatePlayed(String datePlayed)    { this.datePlayed = datePlayed; }
    public void setNotes(String notes)              { this.notes = notes; }

    // ─────────────────────────────────────────────
    // UTILITY
    // ─────────────────────────────────────────────

    @Override
    public String toString() {
        return datePlayed + " — Map ID: " + mapId + " (" + playerCount + " players)";
    }
}