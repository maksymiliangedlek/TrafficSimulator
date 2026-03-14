package pl.gedlek.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Node{
    private final int id;
    private final int x;
    private final int y;
    private final List<Road> outgoingRoads;


    public Node(int id,int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.outgoingRoads = new ArrayList<>();
    }

    public int getX(){
        return this.x;
    }
    public int getY(){
        return this.y;
    }
    public int getId() {return this.id;}
    public List<Road> getOutgoingRoads() { return this.outgoingRoads; }
    
    public void addOutgoingRoad(Road road) {
        this.outgoingRoads.add(road);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node node)) return false;
        return x == node.x && y == node.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

}