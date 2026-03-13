package pl.gedlek;

import java.util.ArrayList;
import java.util.List;

public class CityMap{
    private final List<Node> nodes = new ArrayList<>();
    private final List<Road> roads = new ArrayList<>();
    private final List<Car> cars = new ArrayList<>();


    public Node addNode(int x, int y){
        Node node = new Node(x,y);
        nodes.add(node);
        return node;
    }
    public void addOneWayRoad(Node from, Node to, int speedLimit) {
        Road road = new Road(speedLimit, from, to);
        from.addOutgoingRoad(road);
        roads.add(road);
    }

    public void addTwoWayRoad(Node a, Node b, int speedLimit) {
        addOneWayRoad(a, b, speedLimit);
        addOneWayRoad(b, a, speedLimit);
    }

    public List<Node> getNodes() { return nodes; }
    public List<Road> getRoads() { return roads; }


}