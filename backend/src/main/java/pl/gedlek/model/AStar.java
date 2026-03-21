package pl.gedlek.model;

import pl.gedlek.model.Node;
import pl.gedlek.model.Road;

import java.util.*;

public class AStar {

    private static double calculateHeuristic(Node a, Node b) {
        return GeoUtils.calculateDistance(a.getLat(), a.getLng(), b.getLat(), b.getLng());
    }

    public static List<Road> findPath(Node start, Node target) {
        Map<Node, Road> cameFrom = new HashMap<>();
        Map<Node, Double> gScore = new HashMap<>();
        gScore.put(start, 0.0);

        Map<Node, Double> fScore = new HashMap<>();
        fScore.put(start, calculateHeuristic(start, target));

        PriorityQueue<Node> openSet = new PriorityQueue<>(
                Comparator.comparingDouble(node -> fScore.getOrDefault(node, Double.MAX_VALUE))
        );
        openSet.add(start);

        while (!openSet.isEmpty()){
            var currentNode = openSet.poll();

            if (currentNode == target){
                List<Road> route = new ArrayList<>();
                while(currentNode != start){
                    route.add(cameFrom.get(currentNode));
                    currentNode = cameFrom.get(currentNode).getA();
                }
                return route.reversed();
            }

            var possibleRoads = currentNode.getOutgoingRoads();
            for(var road : possibleRoads){
                var nextNode = road.getB();

                var tentativeGScore = gScore.get(currentNode) + road.getDistance();
                var currentNeighborGScore = gScore.getOrDefault(nextNode, Double.MAX_VALUE);

                if(tentativeGScore < currentNeighborGScore){
                    cameFrom.put(nextNode, road);
                    gScore.put(nextNode, tentativeGScore);
                    fScore.put(nextNode, tentativeGScore + calculateHeuristic(nextNode, target));

                    if(!openSet.contains(nextNode)){
                        openSet.add(nextNode);
                    }
                    else {
                        openSet.remove(nextNode);
                        openSet.add(nextNode);
                    }
                }
            }
        }
        return new ArrayList<>();
    }
}