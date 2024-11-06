package com.strategygamev2.strategygamev2;

import java.util.HashMap;

public class Player {
    private final String playerName;
    private int x, y;
    private HashMap<String, Integer> inventory;
    private int housesBuilt = 0;

    public Player(String playerName, int x, int y) {
        this.playerName = playerName;
        this.x = x;
        this.y = y;
        this.inventory = new HashMap<>();
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

    //Collecting resources and adding them to the inventory
    public void collectResource(Resource resource){
        inventory.put(resource.getType(), inventory.getOrDefault(resource.getType(), 0) + 1);
    }

    public boolean canBuildHouse() {
        return inventory.getOrDefault("brick", 0) >= 2
                && inventory.getOrDefault("stone", 0) >= 1
                && inventory.getOrDefault("wood", 0) >= 1;
    }

    public void buildHouse() {
        if (canBuildHouse()) {
            housesBuilt++;
            inventory.put("brick", inventory.get("brick") - 2);
            inventory.put("stone", inventory.get("stone") - 1);
            inventory.put("wood", inventory.get("wood") - 1);
        }
    }

    public int getHousesBuilt() {
        return housesBuilt;
    }

    public HashMap<String, Integer> getInventory() {
        return inventory;
    }

    public void printInventory() {
        System.out.println(playerName + "'s Inventory: "+ inventory);
    }
}
