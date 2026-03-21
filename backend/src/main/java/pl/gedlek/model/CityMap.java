package pl.gedlek.model;

import java.util.ArrayList;
import java.util.List;

public class CityMap {
    private final List<Node> nodes = new ArrayList<>();
    private final List<Road> roads = new ArrayList<>();

    public Node addNode(double lat, double lng) {
        Node node = new Node(nodes.size() + 1, lat, lng);
        nodes.add(node);
        return node;
    }

    public void addOneWayRoad(Node from, Node to, int speedLimit) {
        Road road = new Road(roads.size() + 1, speedLimit, from, to);
        from.addOutgoingRoad(road);
        roads.add(road);
    }

    public void addTwoWayRoad(Node a, Node b, int speedLimit) {
        addOneWayRoad(a, b, speedLimit);
        addOneWayRoad(b, a, speedLimit);
    }

    public List<Node> getNodes() { return nodes; }
    public List<Road> getRoads() { return roads; }

    public Node getNodeByLatAndLng(double lat, double lng) {
        double epsilon = 0.00001;
        return nodes.stream()
                .filter(n -> Math.abs(n.getLat() - lat) < epsilon &&
                        Math.abs(n.getLng() - lng) < epsilon)
                .findFirst()
                .orElse(null);
    }

}