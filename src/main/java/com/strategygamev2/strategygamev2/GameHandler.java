package com.strategygamev2.strategygamev2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GameHandler {
    private final GameMap gameMap;
    private final List<Player> players;
    private final Lock lock = new ReentrantLock();
    private final AtomicReference<Player> winner;
    private final ScheduledExecutorService respawnResourceExecutor;


    public GameHandler(int mapSize, int numPlayers) {
        this.gameMap = new GameMap(mapSize);
        this.players = new ArrayList<>();
        this.winner = new AtomicReference<>(null);
        this.respawnResourceExecutor = Executors.newScheduledThreadPool(1);

        //Randomly place players on the map
        for (int i = 0; i < numPlayers; i++) {
            int x = (int) (Math.random() * mapSize);
            int y = (int) (Math.random() * mapSize);
            players.add(new Player("Player-" + (i + 1), x, y));
        }

        //Schedule resource respawn every 20 seconds
        respawnResourceExecutor.scheduleAtFixedRate(() -> gameMap.respawnResource(), 20, 20, TimeUnit.SECONDS);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void movePlayer(Player player, int newX, int newY) {
        lock.lock();
        try {
            if (gameMap.isWithinBounds(newX, newY)) {
                player.move(newX, newY);

                //Collect resource if present
                Cell cell = gameMap.getCell(newX, newY);
                if (cell.hasResource()) {
                    player.collectResource(cell.getResource());
                    System.out.println(player.getPlayerName() + " collected the resource: " + cell.getResource().getType() + ". Inventory is now: " + player.getInventory());
                    Resource resource = cell.getResource();
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
            System.out.println(player.getPlayerName() + " built a house. Total houses: " + player.getHousesBuilt());
            if (player.getHousesBuilt() >= 2) {
                winner.set(player); //the player who built 5 houses is assigned as the winner
                respawnResourceExecutor.shutdownNow(); //stop respawning resources once we found a winner
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isGameOver() {
        return winner.get() != null;
    }

    public Player getWinner() {
        return winner.get();
    }

}


//atomic integer for incrementation etc. isGameOver flag!!!