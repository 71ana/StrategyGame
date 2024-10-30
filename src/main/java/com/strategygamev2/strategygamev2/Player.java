package com.strategygamev2.strategygamev2;

public class Player {
    private final String playerName;
    private int x, y;
    private int bricksCollected = 0;
    private int housesBuilt = 0;

    public Player(String playerName, int x, int y) {
        this.playerName = playerName;
        this.x = x;
        this.y = y;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void move(int newX, int newY) {
        x = newX;
        y = newY;
    }

    public void collectBrick() {
        bricksCollected++;
    }

    public boolean canBuildHouse() {
        return bricksCollected >= 3;
    }

    public void buildHouse() {
        if (canBuildHouse()) {
            housesBuilt++;
            bricksCollected -= 3;
        }
    }

    public int getHousesBuilt() {
        return housesBuilt;
    }
}
