package com.halo;

public class GameTypeVariant {

    private int id;
    private int gameTypeId;
    private String officialName;
    private String displayName;      // optional — null falls back to officialName in UI
    private String description;      // quick reference rules summary
    private String status;           // 'ACTIVE', 'INVESTIGATING', 'INACTIVE'
    private boolean isSlayerAdjacent;
    private String notes;            // investigation notes
    private String dateAdded;

    // ─────────────────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────────────────

    // Full constructor — loading from database
    public GameTypeVariant(int id, int gameTypeId, String officialName,
                           String displayName, String description, String status,
                           boolean isSlayerAdjacent, String notes, String dateAdded) {
        this.id = id;
        this.gameTypeId = gameTypeId;
        this.officialName = officialName;
        this.displayName = displayName;
        this.description = description;
        this.status = status;
        this.isSlayerAdjacent = isSlayerAdjacent;
        this.notes = notes;
        this.dateAdded = dateAdded;
    }

    // New variant constructor — adding a new variant
    public GameTypeVariant(int gameTypeId, String officialName, String displayName,
                           String description, String status, boolean isSlayerAdjacent,
                           String notes) {
        this.gameTypeId = gameTypeId;
        this.officialName = officialName;
        this.displayName = displayName;
        this.description = description;
        this.status = status;
        this.isSlayerAdjacent = isSlayerAdjacent;
        this.notes = notes;
    }

    // ─────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────

    public int getId()                  { return id; }
    public int getGameTypeId()          { return gameTypeId; }
    public String getOfficialName()     { return officialName; }
    public String getDisplayName()      { return displayName; }
    public String getDescription()      { return description; }
    public String getStatus()           { return status; }
    public boolean isSlayerAdjacent()   { return isSlayerAdjacent; }
    public String getNotes()            { return notes; }
    public String getDateAdded()        { return dateAdded; }

    // ─────────────────────────────────────────────
    // SETTERS
    // ─────────────────────────────────────────────

    public void setId(int id)                           { this.id = id; }
    public void setGameTypeId(int gameTypeId)           { this.gameTypeId = gameTypeId; }
    public void setOfficialName(String officialName)    { this.officialName = officialName; }
    public void setDisplayName(String displayName)      { this.displayName = displayName; }
    public void setDescription(String description)      { this.description = description; }
    public void setStatus(String status)                { this.status = status; }
    public void setSlayerAdjacent(boolean slayerAdjacent) { this.isSlayerAdjacent = slayerAdjacent; }
    public void setNotes(String notes)                  { this.notes = notes; }
    public void setDateAdded(String dateAdded)          { this.dateAdded = dateAdded; }

    // ─────────────────────────────────────────────
    // UTILITY
    // ─────────────────────────────────────────────

    // Returns display name if set, otherwise falls back to official name
    public String getEffectiveName() {
        return (displayName != null && !displayName.trim().isEmpty())
                ? displayName : officialName;
    }

    @Override
    public String toString() {
        return getEffectiveName() + " [" + status + "]";
    }
}