package com.strategygamev2.strategygamev2.Model;

import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String playerName;
    private int x, y;
    private int housesBuilt;
    private String state;

    @OneToOne
    @JoinColumn(name = "map_cell_id")
    private MapCell currentCell;

    public MapCell getCurrentCell() {
        return currentCell;
    }

    public void setCurrentCell(MapCell currentCell) {
        this.currentCell = currentCell;
    }

    public void setInventory(Map<String, Integer> inventory) {
        this.inventory = inventory;
    }

    @ElementCollection
    @CollectionTable(name = "player_inventory", joinColumns = @JoinColumn(name = "player_id"))
    @MapKeyColumn(name = "resource_name")
    @Column(name = "quantity")
    private Map<String, Integer> inventory = new HashMap<>();


    public Player(String playerName, int x, int y) {
        this.playerName = playerName;
        this.x = x;
        this.y = y;
        this.state = "free";
        this.housesBuilt = 0;
    }

    public Player() {}

    public Long getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getHousesBuilt() {
        return housesBuilt;
    }

    public void setHousesBuilt(int housesBuilt) {
        this.housesBuilt = housesBuilt;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Map<String, Integer> getInventory() {
        return inventory;
    }

    public void setInventory(HashMap<String, Integer> resources) {
        this.inventory = resources;
    }
}
