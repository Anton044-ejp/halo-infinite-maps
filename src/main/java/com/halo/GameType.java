package com.halo;

public class GameType {

    private int id;
    private String name;
    private boolean isPvp;
    private String playerParity;   // 'EVEN_ONLY', 'ODD_ONLY', 'BOTH'

    // ─────────────────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────────────────

    // Full constructor — used when loading from database
    public GameType(int id, String name, boolean isPvp, String playerParity) {
        this.id = id;
        this.name = name;
        this.isPvp = isPvp;
        this.playerParity = playerParity;
    }

    // New GameType constructor — used when adding a new gametype
    public GameType(String name, boolean isPvp, String playerParity) {
        this.name = name;
        this.isPvp = isPvp;
        this.playerParity = playerParity;
    }

    // ─────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────

    public int getId()              { return id; }
    public String getName()         { return name; }
    public boolean isPvp()          { return isPvp; }
    public String getPlayerParity() { return playerParity; }

    // ─────────────────────────────────────────────
    // SETTERS
    // ─────────────────────────────────────────────

    public void setId(int id)                       { this.id = id; }
    public void setName(String name)                { this.name = name; }
    public void setPvp(boolean isPvp)               { this.isPvp = isPvp; }
    public void setPlayerParity(String parity)      { this.playerParity = parity; }

    // ─────────────────────────────────────────────
    // UTILITY
    // ─────────────────────────────────────────────

    @Override
    public String toString() {
        return name;
    }
}