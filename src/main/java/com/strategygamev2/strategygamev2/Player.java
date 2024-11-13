package com.strategygamev2.strategygamev2;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Player {
    private final String playerName;
    private int x, y;
    private final HashMap<String, Integer> inventory;
    private int housesBuilt = 0;
    private final Lock lock = new ReentrantLock();
    private final ReentrantLock playerLock = new ReentrantLock();
    private String state;

    public Player(String playerName, int x, int y) {
        this.playerName = playerName;
        this.x = x;
        this.y = y;
        this.inventory = new HashMap<>();
        this.state = "free";
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
                && inventory.getOrDefault("stone", 0) >= 3
                && inventory.getOrDefault("wood", 0) >= 4;
    }

    public void buildHouse() {
        if (canBuildHouse()) {
            housesBuilt++;
            inventory.put("brick", inventory.get("brick") - 2);
            inventory.put("stone", inventory.get("stone") - 3);
            inventory.put("wood", inventory.get("wood") - 4);
        }
    }

    public boolean hasExtraResources(String resource, int minRequired) {
        return inventory.getOrDefault(resource, 0) > minRequired;
    }

    public boolean resourceNeeded(String resource) {
        switch (resource) {
            case "wood":
            {
                return inventory.getOrDefault(resource, 0) < 4;
            }
            case "stone":
            {
                return inventory.getOrDefault(resource, 0) < 3;
            }
            case "brick":
            {
                return inventory.getOrDefault(resource, 0) < 2;
            }
        }
        return false;
    }

    public void tradeResources(String resourceToGive, String resourceToReceive, int amountToGive, int amountToReceive) {
        inventory.put(resourceToGive, inventory.get(resourceToGive) - amountToGive + 1);
        inventory.put(resourceToReceive, inventory.get(resourceToReceive) + amountToReceive);
    }

    public int getHousesBuilt() {
        return housesBuilt;
    }

    public HashMap<String, Integer> getInventory() {
        return inventory;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void printInventory() {
        System.out.println(playerName + "'s Inventory: "+ inventory);
    }
}
