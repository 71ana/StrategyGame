package com.strategygamev2.strategygamev2.Controller;

import com.strategygamev2.strategygamev2.Model.Player;
import com.strategygamev2.strategygamev2.Service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Player Operations", description = "Operations related to managing players.")
@RestController
@RequestMapping("/players")
public class PlayerController {
    @Autowired
    private PlayerService playerService;

    @Operation(summary = "Create a new player", description = "Create a new player with the specified name and coordinates.")
    @PostMapping("/create")
    public Player createPlayer(@Parameter(description = "The player's name", required = true) @RequestParam String playerName,
                               @Parameter(description = "The x-coordinate of the player", required = true) @RequestParam int x,
                               @Parameter(description = "The y-coordinate of the player", required = true) @RequestParam int y) {
        return playerService.createPlayer(playerName, x, y);
    }

    @Operation(summary = "Move a player", description = "Move the player to the new coordinates.")
    @PostMapping("/{id}/move")
    public Player movePlayer(@Parameter(description = "The player's ID", required = true) @PathVariable Long id,
                             @Parameter(description = "The new x-coordinate", required = true) @RequestParam int x,
                             @Parameter(description = "The new y-coordinate", required = true) @RequestParam int y) {
        return playerService.movePlayer(id, x, y);
    }

    @Operation(summary = "Get player details", description = "Retrieve the details of a player by their ID.")
    @GetMapping("/{id}")
    public Player getPlayerById(@PathVariable Long id) {
        return playerService.getPlayerById(id);
    }

    @Operation(summary = "Collect resources", description = "Let the player collect resources.")
    @PostMapping("/{id}/collect")
    public Player collectResource(@PathVariable Long id) {
        return playerService.collectResource(id);
    }

    @Operation(summary = "Trade resources", description = "Allow players to trade resources with each other.")
    @PostMapping("/{id}/trade")
    public String trade(@PathVariable Long id,
                        @Parameter(description = "ID of the target player", required = true) @RequestParam Long targetPlayerId,
                        @Parameter(description = "Resource the player wants to give", required = true) @RequestParam String resourceToGive,
                        @Parameter(description = "Quantity of resource to give", required = true) @RequestParam int quantityToGive,
                        @Parameter(description = "Resource the player wants to receive", required = true) @RequestParam String resourceToReceive,
                        @Parameter(description = "Quantity of resource to receive", required = true) @RequestParam int quantityToReceive) {
        return playerService.trade(id, targetPlayerId, resourceToGive, quantityToGive, resourceToReceive, quantityToReceive);
    }

    @Operation(description = "Build a house", summary = "Let the player build a house.")
    @PostMapping("/build-house/{playerId}")
    public String buildHouse(@PathVariable Long playerId) {
        return playerService.buildHouse(playerId);
    }

    @Operation(summary = "Delete a player", description = "Remove a player by their ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePlayer(@PathVariable Long id) {
        boolean isDeleted = playerService.deletePlayer(id);
        if (isDeleted) {
            return ResponseEntity.ok("Player deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Player not found.");
        }
    }


    @Operation(description = "Check the winner", summary = "Check if a player has won the game.")
    @PostMapping("/winner")
    public Player gotWinner(@RequestParam Player winner) {
        return playerService.checkWinner();
    }

    @Operation(description = "Get all players", summary = "Retrieve a list of all players.")
    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }
}
