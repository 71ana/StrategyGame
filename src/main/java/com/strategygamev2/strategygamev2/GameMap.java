package com.strategygamev2.strategygamev2;

import java.util.Random;

public class GameMap {
    private final int size;
    private final Cell[][] map;


    public GameMap(int size) {
        this.size = size;
        this.map = new Cell[size][size];
        initializeMap();
    }

    public void initializeMap() {
        Random random = new Random();

        //Initialize cells and randomly place resources
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                map[i][j] = new Cell();
                if (random.nextInt(10) < 3) { //30% chance of placing a resource
                    placeRandomResource(map[i][j]);
                }
            }
        }
    }

    public void placeRandomResource(Cell cell) {
        Random random = new Random();
        int prob = random.nextInt(10);
        if(prob < 3) //30% chance of placing a brick
            cell.setResource(new Resource("brick"));
        else if (prob < 6) {//30% chance of placing a stone
            cell.setResource(new Resource("stone"));
        } else { //40% chance of placing a wood
            cell.setResource(new Resource("wood"));
        }
    }

    public void respawnResource() {
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (!map[i][j].hasResource() && random.nextInt(10) < 3) {
                    placeRandomResource(map[i][j]);
                }
            }
        }
        System.out.println("A resource has been respawned on the map.");

    }

    public Cell getCell(int x, int y) {
        if(isWithinBounds(x,y)) {
            return map[x][y];
        }
        return null;
    }

    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    public int getSize() {
        return size;
    }
}
