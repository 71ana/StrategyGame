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
        lock.lock();
        try {
            inventory.put(resource.getType(), inventory.getOrDefault(resource.getType(), 0) + 1);
        } finally {
            lock.unlock();
        }
    }

    public boolean canBuildHouse() {
        lock.lock();
        try {
            return inventory.getOrDefault("brick", 0) >= 2
                    && inventory.getOrDefault("stone", 0) >= 3
                    && inventory.getOrDefault("wood", 0) >= 4;
        } finally {
            lock.unlock();
        }
    }

    public void buildHouse() {
        lock.lock();
        try {
            if (canBuildHouse()) {
                housesBuilt++;
                inventory.put("brick", inventory.get("brick") - 2);
                inventory.put("stone", inventory.get("stone") - 3);
                inventory.put("wood", inventory.get("wood") - 4);
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean hasExtraResources(String resource, int minRequired) {
        lock.lock();
        try {
            return inventory.getOrDefault(resource, 0) > minRequired;
        } finally {
            lock.unlock();
        }
    }

    public void tradeResources(String resourceToGive, String resourceToReceive, int amountToGive, int amountToReceive) {
        lock.lock();
        try {
            inventory.put(resourceToGive, inventory.get(resourceToGive) - amountToGive);
            inventory.put(resourceToReceive, inventory.get(resourceToReceive) + amountToReceive);
        } finally {
            lock.unlock();
        }
    }

    public int getHousesBuilt() {
        lock.lock();
        try {
            return housesBuilt;
        } finally {
            lock.unlock();
        }
    }

    public HashMap<String, Integer> getInventory() {
        lock.lock();
        try {
            return inventory;
        } finally {
            lock.unlock();
        }
    }

    public ReentrantLock getLock() {
        return playerLock;
    }

    public void printInventory() {
        System.out.println(playerName + "'s Inventory: "+ inventory);
    }
}
