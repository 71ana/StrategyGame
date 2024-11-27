package com.strategygamev2.strategygamev2.Controller;

import com.strategygamev2.strategygamev2.Model.MapCell;
import com.strategygamev2.strategygamev2.Service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.List;

@Api(tags = "Map Operations", description = "Operations related to the game map.")
@RestController
@RequestMapping("/map")
public class MapController {

    @Autowired
    private MapService mapService;

    @ApiOperation(value = "Get the game map", notes = "Retrieve the entire game map with all cells.")
    @GetMapping
    public List<MapCell> geMap() {
        return mapService.getMap();
    }

    @ApiOperation(value = "Get a specific map cell", notes = "Retrieve a specific cell on the game map by its coordinates.")
    @GetMapping("/coordinates")
    public MapCell getCell(@ApiParam(value = "The x-coordinate of the map cell", required = true) @RequestParam int x,
                           @ApiParam(value = "The y-coordinate of the map cell", required = true) @RequestParam int y) {
        return mapService.getCell(x, y);
    }

    @ApiOperation(value = "Add resources to the map", notes = "Add a specified number of resources to the map.")
    @PostMapping("/addResources")
    public List<MapCell> addResources(@ApiParam(value = "Number of resources to add", required = true) @RequestParam int noRes) {
        return mapService.addResources(noRes);
    }

    @ApiOperation(value = "Get all resources with their locations", notes = "Retrieve a list of all resources on the map with their locations.")
    @GetMapping("/resources")
    public List<MapCell> getAllResourcesWithLocations() {
        return mapService.getAllResources();
    }
}
