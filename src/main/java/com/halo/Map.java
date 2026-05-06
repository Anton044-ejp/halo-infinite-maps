package com.halo;

import java.util.List;

public class Map {

    private int id;
    private String name;
    private String size;           // 'Small', 'Medium', 'Large', 'XL'
    private int minPlayers;
    private int maxPlayers;
    private String lighting;
    private String imageFilename;
    private String description;
    private String waypointLink;
    private String dateAdded;
    private boolean isActive;
    private String customModes;    // freeform text for minigame modes
    private List<GameType> gameTypes; // populated when needed

    // ─────────────────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────────────────

    // Full constructor — used when loading from database
    public Map(int id, String name, String size, int minPlayers, int maxPlayers,
               String lighting, String imageFilename, String description,
               String waypointLink, String dateAdded, boolean isActive, String customModes) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.lighting = lighting;
        this.imageFilename = imageFilename;
        this.description = description;
        this.waypointLink = waypointLink;
        this.dateAdded = dateAdded;
        this.isActive = isActive;
        this.customModes = customModes;
    }

    // New map constructor — used when adding a map (no id or date yet)
    public Map(String name, String size, int minPlayers, int maxPlayers,
               String lighting, String imageFilename, String description,
               String waypointLink, String customModes) {
        this.name = name;
        this.size = size;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.lighting = lighting;
        this.imageFilename = imageFilename;
        this.description = description;
        this.waypointLink = waypointLink;
        this.customModes = customModes;
        this.isActive = true;
    }

    // ─────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────

    public int getId()                  { return id; }
    public String getName()             { return name; }
    public String getSize()             { return size; }
    public int getMinPlayers()          { return minPlayers; }
    public int getMaxPlayers()          { return maxPlayers; }
    public String getLighting()         { return lighting; }
    public String getImageFilename()    { return imageFilename; }
    public String getDescription()      { return description; }
    public String getWaypointLink()     { return waypointLink; }
    public String getDateAdded()        { return dateAdded; }
    public boolean isActive()           { return isActive; }
    public String getCustomModes()      { return customModes; }
    public List<GameType> getGameTypes(){ return gameTypes; }

    // ─────────────────────────────────────────────
    // SETTERS
    // ─────────────────────────────────────────────

    public void setId(int id)                           { this.id = id; }
    public void setName(String name)                    { this.name = name; }
    public void setSize(String size)                    { this.size = size; }
    public void setMinPlayers(int minPlayers)           { this.minPlayers = minPlayers; }
    public void setMaxPlayers(int maxPlayers)           { this.maxPlayers = maxPlayers; }
    public void setLighting(String lighting)            { this.lighting = lighting; }
    public void setImageFilename(String imageFilename)  { this.imageFilename = imageFilename; }
    public void setDescription(String description)      { this.description = description; }
    public void setWaypointLink(String waypointLink)    { this.waypointLink = waypointLink; }
    public void setDateAdded(String dateAdded)          { this.dateAdded = dateAdded; }
    public void setActive(boolean active)               { this.isActive = active; }
    public void setCustomModes(String customModes)      { this.customModes = customModes; }
    public void setGameTypes(List<GameType> gameTypes)  { this.gameTypes = gameTypes; }

    // ─────────────────────────────────────────────
    // UTILITY
    // ─────────────────────────────────────────────

    @Override
    public String toString() {
        return name + " (" + size + ", " + minPlayers + "-" + maxPlayers + " players)";
    }
}