package pl.gedlek.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Node {
    private final int id;
    private final double lat; // Zmiana z int x na double lat
    private final double lng; // Zmiana z int y na double lng
    private final List<Road> outgoingRoads;
    private String lightState = "GREEN"; // Twój stan świateł

    public Node(int id, double lat, double lng) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.outgoingRoads = new ArrayList<>();
    }

    public double getLat() { return this.lat; }
    public double getLng() { return this.lng; }
    public int getId() { return this.id; }
    public List<Road> getOutgoingRoads() { return this.outgoingRoads; }

    public void addOutgoingRoad(Road road) {
        this.outgoingRoads.add(road);
    }

    public String getLightState() { return lightState; }
    public void setLightState(String lightState) { this.lightState = lightState; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node node)) return false;
        return Double.compare(node.lat, lat) == 0 && Double.compare(node.lng, lng) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lng);
    }
}