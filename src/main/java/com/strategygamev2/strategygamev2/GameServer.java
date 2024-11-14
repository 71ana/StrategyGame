package com.strategygamev2.strategygamev2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private static final int MAX_PLAYERS = 10;
    private static final int MAP_SIZE = 10;
    private final ExecutorService playerPool;
    private final GameHandler gameHandler;

    public GameServer() {
        this.playerPool = Executors.newFixedThreadPool(MAX_PLAYERS);
        this.gameHandler = new GameHandler(MAP_SIZE, MAX_PLAYERS);
    }

    public void startGame() {
        while(!gameHandler.isGameOver()){
            //Submit each player's task for this turn
            for(Player player : gameHandler.getPlayers()) {
                playerPool.submit(new PlayerTask(player, gameHandler));
            }

            //To generate a small break
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        playerPool.shutdown();
        System.out.println("Game Over! The winner is: " + gameHandler.getWinner().getPlayerName() + "! Congrats!:D");
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.startGame();
    }
}
