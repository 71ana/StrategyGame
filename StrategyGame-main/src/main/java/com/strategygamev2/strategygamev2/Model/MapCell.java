package com.strategygamev2.strategygamev2.Model;

import jakarta.persistence.*;

@Entity
public class MapCell {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int x, y;

    private String resource;

    @OneToOne(mappedBy = "currentCell", cascade = CascadeType.ALL)
   
    private Player player;
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
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

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

}
