package com.strategygamev2.strategygamev2;

public class Cell {
    private Resource resource;

    public boolean hasResource() {
        return resource != null;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public void removeResource() {
        resource = null;
    }
}
