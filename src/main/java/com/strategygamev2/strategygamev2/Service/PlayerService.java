package com.strategygamev2.strategygamev2.Service;

import com.strategygamev2.strategygamev2.Model.MapCell;
import com.strategygamev2.strategygamev2.Model.Player;
import com.strategygamev2.strategygamev2.Repository.MapCellRepository;
import com.strategygamev2.strategygamev2.Repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private MapCellRepository mapCellRepository;
    private final Lock lock = new ReentrantLock();
    private AtomicReference<Player> winner = new AtomicReference<>();

    @Value("${webhook.url}")
    private String webHookUrl;

    private void notifyWinner(Player player) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForEntity(webHookUrl, player, Void.class);
    }

    // Constants for building a house
    private static final int WOOD_REQUIRED = 1;
    private static final int STONE_REQUIRED = 1;
    private static final int BRICK_REQUIRED = 1;

    public Player createPlayer(String name, int x, int y) {
        // Check if the map cell is available
        MapCell cell = mapCellRepository.findByXAndY(x, y);
        if (cell == null) {
            throw new IllegalArgumentException("Invalid position: The specified map cell does not exist.");
        }
        if (cell.getPlayer() != null) {
            throw new IllegalStateException("Cell already occupied by another player.");
        }

        // Create the player
        Player player = new Player(name, x, y);
        player = playerRepository.save(player); // Save the player in the DB

        // Mark the map cell as occupied by this player
        cell.setPlayer(player);
        mapCellRepository.save(cell);

        return player;
    }

    public Player movePlayer(Long playerId, int newX, int newY) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (!canPlayerMove(player))
            return null;

        int currentX = player.getX();
        int currentY = player.getY();

        //Checking if the move is valid or not
        if (Math.abs(newX - currentX) > 1 || Math.abs(newY - currentY) > 1) {
            throw new IllegalArgumentException("Invalid move. You can only move one step at a time.");
        }

        // Fetch the current and target map cells
        MapCell currentCell = mapCellRepository.findByXAndY(currentX, currentY);
        MapCell targetCell = mapCellRepository.findByXAndY(newX, newY);

        if (targetCell == null || targetCell.getPlayer() != null) {
            throw new IllegalStateException("Target cell is invalid or already occupied.");
        }

        // Update the player's position
        player.setX(newX);
        player.setY(newY);
        playerRepository.save(player);

        // Update map cells
        if (currentCell != null) {
            currentCell.setPlayer(null); // Clear the current cell
            mapCellRepository.save(currentCell);
        }
        targetCell.setPlayer(player); // Mark the new cell
        mapCellRepository.save(targetCell);

        return player;
    }

    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found"));
    }

    public Player collectResource(Long playerId) {
        // Fetch the player
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with id: " + playerId));

        // Fetch the map cell at the player's current position
        MapCell cell = mapCellRepository.findByXAndY(player.getX(), player.getY());

        // Check if the cell has a resource to collect
        if (cell != null && cell.getResource() != null) {
            // Add resource to the player's inventory
            addResource(player, cell.getResource());

            // Remove the resource from the map cell
            cell.setResource(null);
            mapCellRepository.save(cell); // Persist changes to the map cell
        } else {
            throw new IllegalStateException("No resource to collect at player's current position.");
        }

        // Save and return the updated player
        return playerRepository.save(player);
    }

    public void deletePlayer(Long playerId) {
        Optional<Player> player = playerRepository.findById(playerId);
        if (player.isPresent()) {
            playerRepository.delete(player.get());
        } else {
            throw new IllegalArgumentException("Player not found");
        }
    }



    public void addResource(Player player, String resource) {
        // Update the player's inventory
        player.getInventory().merge(resource, 1, Integer::sum); // Add 1 unit of the resource
    }

    public String trade(Long playerId, Long targetPlayerId, String resourceToGive, int quantityToGive,
                        String resourceToReceive, int quantityToReceive) {
        lock.lock(); // Ensure thread safety during trade
        try {
            // Fetch both players
            Player player1 = playerRepository.findById(playerId)
                    .orElseThrow(() -> new RuntimeException("Player not found"));
            Player player2 = playerRepository.findById(targetPlayerId)
                    .orElseThrow(() -> new RuntimeException("Target Player not found"));
            player1.setState("wait");
            player2.setState("wait");

            if (!arePlayersAdjacent(player1, player2)) {
                return "Players are not adjacent!";
            }

            // Check if players have sufficient resources
            if (!hasEnoughResources(player1, resourceToGive, quantityToGive) ||
                    !hasEnoughResources(player2, resourceToReceive, quantityToReceive)) {
                return ("One of the players does not have enough resources for the trade.");
            }

            // Perform the trade
            updateResources(player1, resourceToGive, -quantityToGive);
            updateResources(player1, resourceToReceive, quantityToReceive);
            updateResources(player2, resourceToGive, quantityToGive);
            updateResources(player2, resourceToReceive, -quantityToReceive);

            // Save updated players
            playerRepository.save(player1);
            playerRepository.save(player2);

            player1.setState("free");
            player2.setState("free");

            return "Trade successful between " + player1.getPlayerName() + " and " + player2.getPlayerName();
        } finally {
            lock.unlock();
        }
    }

    private boolean hasEnoughResources(Player player, String resource, int quantity) {
        return player.getInventory().getOrDefault(resource, 0) >= quantity;
    }

    private void updateResources(Player player, String resource, int quantity) {
        player.getInventory().put(resource, player.getInventory().getOrDefault(resource, 0) + quantity);
    }

    public synchronized boolean arePlayersAdjacent(Player player1, Player player2) {
        int x = Math.abs(player1.getX() - player2.getX());
        int y = Math.abs(player1.getY() - player2.getY());
        return x <= 1 && y <= 1;
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public boolean canPlayerMove(Player player) {
        return "free".equals(player.getState());
    }

    public String buildHouse(Long playerId) {
        synchronized (lock) {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new RuntimeException("Player not found"));

            if (player.getState().equals("wait")) {
                return "Player is waiting for another player to finish trading.";
            }

            if (hasEnoughResources(player, "wood", WOOD_REQUIRED) &&
                    hasEnoughResources(player, "stone", STONE_REQUIRED) &&
                    hasEnoughResources(player, "brick", BRICK_REQUIRED)) {
                updateResources(player, "wood", -WOOD_REQUIRED);
                updateResources(player, "stone", -STONE_REQUIRED);
                updateResources(player, "brick", -BRICK_REQUIRED);
                player.setHousesBuilt(player.getHousesBuilt() + 1);

                playerRepository.save(player);

                // Check if the player has won
                if (player.getHousesBuilt() >= 5) {
                    winner.set(player);
                    notifyWinner(player);
                    return player.getPlayerName() + " has won the game!";
                }
                return player.getPlayerName() + " built a house. Total houses: " + player.getHousesBuilt();
            } else {
                return "Not enough resources to build a house.";
            }
        }
    }



    public Player checkWinner() {
        List<Player> players = playerRepository.findAll();

        for (Player player : players) {
            if (player.getHousesBuilt() >= 5) {
                return player;
            }
        }
        return null;
    }

}
