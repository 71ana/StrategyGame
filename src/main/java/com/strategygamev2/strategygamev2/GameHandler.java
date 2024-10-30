package com.strategygamev2.strategygamev2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GameHandler {
    private final GameMap gameMap;
    private final List<Player> players;
    private final Lock lock = new ReentrantLock();
    private int totalHouses = 0;

    public GameHandler(int mapSize, int numPlayers) {
        this.gameMap = new GameMap(mapSize);
        this.players = new ArrayList<>();

        // Randomly place players on the map
        for (int i = 0; i < numPlayers; i++) {
            int x = (int) (Math.random() * mapSize);
            int y = (int) (Math.random() * mapSize);
            players.add(new Player("Player-" + (i + 1), x, y));
        }
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void movePlayer(Player player, int newX, int newY) {
        lock.lock();
        try {
            if (gameMap.isWithinBounds(newX, newY)) {
                player.move(newX, newY);

                // Collect resource if present
                Cell cell = gameMap.getCell(newX, newY);
                if (cell.hasResource()) {
                    player.collectBrick();
                    System.out.println(player.getPlayerName() + " collected a brick.");
                    cell.removeResource();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void buildHouse(Player player) {
        lock.lock();
        try {
            player.buildHouse();
            totalHouses++;
            System.out.println(player.getPlayerName() + " built a house. Total houses: " + totalHouses);
        } finally {
            lock.unlock();
        }
    }

    public boolean isGameOver() {
        return totalHouses >= 5;
    }

    public int getTotalHouses() {
        return totalHouses;
    }
}
