package com.strategygamev2.strategygamev2.Service;

import com.strategygamev2.strategygamev2.Cell;
import com.strategygamev2.strategygamev2.Model.MapCell;
import com.strategygamev2.strategygamev2.Model.Player;
import com.strategygamev2.strategygamev2.Repository.MapCellRepository;
import com.strategygamev2.strategygamev2.Resource;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class MapService {
    @Autowired
    private MapCellRepository mapCellRepository;

    @Value("10")
    private int size;

    public int getSize() {
        return size;
    }

    @PostConstruct
    public void init() {
        if (mapCellRepository.count() == 0) {
            initializeMap();
        }
    }

    public List<MapCell> initializeMap() {
        List<MapCell> mapCells = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                MapCell cell = new MapCell();
                cell.setX(i);
                cell.setY(j);
                cell.setResource(null); // No resource at start
                mapCells.add(cell);
            }
        }
        return mapCellRepository.saveAll(mapCells); // Save in DB
    }

    public List<MapCell> getMap() {
        return mapCellRepository.findAll();
    }

    public MapCell getCell(int x, int y) {
        return mapCellRepository.findByXAndY(x, y);
    }

    public List<MapCell> addResources(int numberOfResources) {
        List<MapCell> updatedCells = new ArrayList<>();
        List<MapCell> allCells = mapCellRepository.findAll();
        Random random = new Random();

        for (int i = 0; i < numberOfResources; i++) {
            // Choose a random cell
            MapCell cell = allCells.get(random.nextInt(100));

            // Skip if the cell is occupied by a player or already has a resource
            if (cell.getPlayer() != null || cell.getResource() != null) {
                i--; // Retry for another cell
                continue;
            }

            int prob = random.nextInt(10);
            if(prob < 2) //20% chance of placing a brick
                cell.setResource("brick");
            else if (prob < 5) {//30% chance of placing a stone
                cell.setResource("stone");
            } else { //50% chance of placing a wood
                cell.setResource("wood");
            }

            updatedCells.add(cell);
            // Save the cell
            mapCellRepository.save(cell);
        }
        return updatedCells;
    }

    public List<MapCell> getAllResources() {
        List<MapCell> cells = mapCellRepository.findAll(); // Fetch all cells from the database

        // Filter cells with resources and format them into a list of strings
        return cells.stream()
                .filter(cell -> cell.getResource() != null)
                .toList();
    }

}
