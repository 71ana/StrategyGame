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
                    int prob = random.nextInt(10);
                    if(prob < 3) //30% chance of placing a brick
                        map[i][j].setResource(new Resource("brick"));
                    else if (prob < 6) {//30% chance of placing a stone
                        map[i][j].setResource(new Resource("stone"));
                    } else { //40% chance of placing a wood
                        map[i][j].setResource(new Resource("wood"));
                    }
                }
            }
        }
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
