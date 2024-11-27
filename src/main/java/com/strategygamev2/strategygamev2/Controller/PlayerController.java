package com.strategygamev2.strategygamev2.Controller;

import com.strategygamev2.strategygamev2.Model.Player;
import com.strategygamev2.strategygamev2.Service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.List;

@Api(tags = "Player Operations", description = "Operations related to managing players.")
@RestController
@RequestMapping("/players")
public class PlayerController {
    @Autowired
    private PlayerService playerService;

    @ApiOperation(value = "Create a new player", notes = "Create a new player with the specified name and coordinates.")
    @PostMapping("/create")
    public Player createPlayer(@ApiParam(value = "The player's name", required = true) @RequestParam String playerName,
                               @ApiParam(value = "The x-coordinate of the player", required = true) @RequestParam int x,
                               @ApiParam(value = "The y-coordinate of the player", required = true) @RequestParam int y) {
        return playerService.createPlayer(playerName, x, y);
    }

    @ApiOperation(value = "Move a player", notes = "Move the player to the new coordinates.")
    @PostMapping("/{id}/move")
    public Player movePlayer(@ApiParam(value = "The player's ID", required = true) @PathVariable Long id,
                             @ApiParam(value = "The new x-coordinate", required = true) @RequestParam int x,
                             @ApiParam(value = "The new y-coordinate", required = true) @RequestParam int y) {
        return playerService.movePlayer(id, x, y);
    }

    @ApiOperation(value = "Get player details", notes = "Retrieve the details of a player by their ID.")
    @GetMapping("/{id}")
    public Player getPlayerById(@PathVariable Long id) {
        return playerService.getPlayerById(id);
    }

    @ApiOperation(value = "Collect resources", notes = "Let the player collect resources.")
    @PostMapping("/{id}/collect")
    public Player collectResource(@PathVariable Long id) {
        return playerService.collectResource(id);
    }

    @ApiOperation(value = "Trade resources", notes = "Allow players to trade resources with each other.")
    @PostMapping("/{id}/trade")
    public String trade(@PathVariable Long id,
                        @ApiParam(value = "ID of the target player", required = true) @RequestParam Long targetPlayerId,
                        @ApiParam(value = "Resource the player wants to give", required = true) @RequestParam String resourceToGive,
                        @ApiParam(value = "Quantity of resource to give", required = true) @RequestParam int quantityToGive,
                        @ApiParam(value = "Resource the player wants to receive", required = true) @RequestParam String resourceToReceive,
                        @ApiParam(value = "Quantity of resource to receive", required = true) @RequestParam int quantityToReceive) {
        return playerService.trade(id, targetPlayerId, resourceToGive, quantityToGive, resourceToReceive, quantityToReceive);
    }

    @ApiOperation(value = "Build a house", notes = "Let the player build a house.")
    @PostMapping("/build-house/{playerId}")
    public String buildHouse(@PathVariable Long playerId) {
        return playerService.buildHouse(playerId);
    }

    @ApiOperation(value = "Check the winner", notes = "Check if a player has won the game.")
    @GetMapping("/winner")
    public Player checkWinner() {
        return playerService.checkWinner();
    }

    @ApiOperation(value = "Get all players", notes = "Retrieve a list of all players.")
    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }
}
