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
                        player.setState("wait");
                        i.setState("wait");
                        trade(player, i);
                        player.setState("free");
                        i.setState("free");
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

    public synchronized boolean arePlayersAdjacent(Player player1, Player player2) {
        int x = Math.abs(player1.getX() - player2.getX());
        int y = Math.abs(player1.getY() - player2.getY());
        return x <= 1 && y <= 1;
    }

    /*
     * 2 wood = 1 stone
     * 3 wood = 1 brick
     * 2 stone = 1 brick
     *
     * To build house:
     * 2 bricks, 3 stones, 4 woods
     * */
    public void trade(Player player1, Player player2) {
        lock.lock();

        try {
            if (player1.hasExtraResources("wood", 6) && player1.resourceNeeded("stone") && player2.hasExtraResources("stone", 4) && player2.resourceNeeded("wood")) {
                System.out.println("Inventory before trade:");
                player1.printInventory();
                player2.printInventory();
                player1.tradeResources("wood", "stone", 2, 1);
                player2.tradeResources("stone", "wood",1, 2);
                System.out.println(player1.getPlayerName() + " traded 2 woods for 1 stone with " + player2.getPlayerName());
                System.out.println("x si y pt " + player1.getPlayerName() + ": " + player1.getX() + ", " + player1.getY());
                System.out.println("x si y pt " + player2.getPlayerName() + ": " + player2.getX() + ", " + player2.getY());
                System.out.println("Inventory after trade:");
                player1.printInventory();
                player2.printInventory();
            } else if (player1.hasExtraResources("wood", 6) && player1.resourceNeeded("brick") && player2.hasExtraResources("brick", 3) && player2.resourceNeeded("wood")) {
                System.out.println("Inventory before trade:");
                player1.printInventory();
                player2.printInventory();
                player1.tradeResources("wood", "brick", 3, 1);
                player2.tradeResources("brick", "wood", 1,3);
                System.out.println(player1.getPlayerName() + " traded 2 woods for 1 brick with " + player2.getPlayerName());
                System.out.println("x si y pt " + player1.getPlayerName() + ": " + player1.getX() + ", " + player1.getY());
                System.out.println("x si y pt " + player2.getPlayerName() + ": " + player2.getX() + ", " + player2.getY());
                System.out.println("Inventory after trade:");
                player1.printInventory();
                player2.printInventory();
            } else if (player1.hasExtraResources("stone", 4) && player1.resourceNeeded("brick") && player2.hasExtraResources("brick", 3) && player2.resourceNeeded("stone")) {
                System.out.println("Inventory before trade:");
                player1.printInventory();
                player2.printInventory();
                player1.tradeResources("stone","brick", 2, 1);
                player2.tradeResources("brick", "stone", 1, 2);
                System.out.println(player1.getPlayerName() + " traded 1 stone for 1 brick with " + player2.getPlayerName());
                System.out.println("x si y pt " + player1.getPlayerName() + ": " + player1.getX() + ", " + player1.getY());
                System.out.println("Inventory after trade:");
                System.out.println("x si y pt " + player2.getPlayerName() + ": " + player2.getX() + ", " + player2.getY());
                player1.printInventory();
                player2.printInventory();
            }
//            System.out.println("x si y pt player-1: " + player1.getX() + ", " + player1.getY());
//            System.out.println("x si y pt player-2: " + player2.getX() + ", " + player2.getY());
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