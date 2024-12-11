package com.strategygamev2.strategygamev2.Controller;

import com.strategygamev2.strategygamev2.Model.MapCell;
import com.strategygamev2.strategygamev2.Service.MapService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Map Operations", description = "Operations related to the game map.")
@RestController
@RequestMapping("/map")
public class MapController {

    @Autowired
    private MapService mapService;

    @Operation(summary = "Get the game map", description = "Retrieve the entire game map with all cells.")
    @GetMapping
    public List<MapCell> geMap() {
        return mapService.getMap();
    }

    @Operation(summary = "Get a specific map cell", description = "Retrieve a specific cell on the game map by its coordinates.")
    @GetMapping("/coordinates")
    public MapCell getCell(@Parameter(description = "The x-coordinate of the map cell", required = true) @RequestParam int x,
                           @Parameter(description = "The y-coordinate of the map cell", required = true) @RequestParam int y) {
        return mapService.getCell(x, y);
    }

    @Operation(summary = "Add resources to the map", description = "Add a specified number of resources to the map.")
    @PostMapping("/addResources")
    public List<MapCell> addResources(@Parameter(name = "Number of resources to add", required = true) @RequestParam int noRes) {
        return mapService.addResources(noRes);
    }

    @Operation(summary = "Get all resources with their locations", description = "Retrieve a list of all resources on the map with their locations.")
    @GetMapping("/resources")
    public List<MapCell> getAllResourcesWithLocations() {
        return mapService.getAllResources();
    }
}