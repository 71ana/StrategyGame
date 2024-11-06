package com.strategygamev2.strategygamev2;

import java.util.Random;

public class PlayerTask implements Runnable {
    private final Player player;
    private final GameHandler gameHandler;
    private final Random random = new Random();

    public PlayerTask(Player player, GameHandler gameHandler) {
        this.player = player;
        this.gameHandler = gameHandler;
    }

    @Override
    public void run() {
        // Move the player in a random direction
        int newX = player.getX() + random.nextInt(3) - 1;
        int newY = player.getY() + random.nextInt(3) - 1;

        gameHandler.movePlayer(player, newX, newY);

        // Check if the player can build a house
        if (player.canBuildHouse()) {
            gameHandler.buildHouse(player);
        }
    }
}