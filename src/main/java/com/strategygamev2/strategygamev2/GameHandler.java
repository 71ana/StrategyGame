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
                    cell.removeResource();
                }

                //Attempt to trade with any near player
                for (Player i : players) {
                    if (i != player && arePlayersAdjacent(i, player)) {
                        trade(player, i);
                    }
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

    public boolean arePlayersAdjacent(Player player1, Player player2) {
        int x = Math.abs(player1.getX() - player2.getX());
        int y = Math.abs(player1.getY() - player2.getY());
        return x <= 1 && y <= 1;
    }

    public void trade(Player player1, Player player2) {
        //We will try to lock both players to prevent deadlock
        //The order lock will be based on their player name
        Player firstLock = player1;
        Player secondLock = player2;

        if (player1.getPlayerName().compareTo(player2.getPlayerName()) > 0) {
            firstLock = player2;
            secondLock = player1;
        }

        firstLock.getLock().lock();
        secondLock.getLock().lock();
        try {
            if (player1.hasExtraResources("wood", 6) && player2.hasExtraResources("stone", 4)) {
                player1.tradeResources("wood", "stone", 2, 1);
                player2.tradeResources("stone", "wood",1, 2);
                System.out.println(player1.getPlayerName() + " traded 2 woods for 1 stone with " + player2.getPlayerName());
            } else if (player1.hasExtraResources("wood", 6) && player2.hasExtraResources("brick", 3)) {
                player1.tradeResources("wood", "brick", 3, 1);
                player2.tradeResources("brick", "wood", 1,3);
                System.out.println(player1.getPlayerName() + " traded 2 woods for 1 brick with " + player2.getPlayerName());
            } else if (player1.hasExtraResources("stone", 4) && player2.hasExtraResources("brick", 3)) {
                player1.tradeResources("stone","brick", 2, 1);
                player2.tradeResources("brick", "stone", 1, 2);
                System.out.println(player1.getPlayerName() + " traded 1 stone for 1 brick with " + player2.getPlayerName());
            }
        } finally {
            firstLock.getLock().unlock();
            secondLock.getLock().unlock();
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