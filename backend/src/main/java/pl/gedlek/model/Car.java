package pl.gedlek.model;

import pl.gedlek.model.AStar;
import pl.gedlek.model.Node;
import pl.gedlek.model.Road;
import java.util.List;

public class Car {
    private final int id;
    private final Node start;
    private final Node target;
    private Node currentNode;
    private volatile double currentLat;
    private volatile double currentLng;

    public Car(int id, Node start, Node target) {
        this.id = id;
        this.start = start;
        this.target = target;
        this.currentLat = start.getLat();
        this.currentLng = start.getLng();
    }

    public void startDriving(int carId) {
        Thread.ofVirtual().start(() -> {
            while (true) {
                List<Road> path = AStar.findPath(start, target);
                if (path.isEmpty()) return;

                for (Road road : path) {
                    driveOnRoad(road, carId);
                }

                List<Road> back_path = AStar.findPath(target, start);
                if (back_path.isEmpty()) return;

                for (Road road : back_path) {
                    driveOnRoad(road, carId);
                }
            }
        });
    }

    private void driveOnRoad(Road road, int carId) {
        try {
            road.addCar();
            double distance = road.getDistance();
            long timeToTravel = (long) ((distance / road.getSpeedLimit()) * 100);

            double startLat = road.getA().getLat();
            double startLng = road.getA().getLng();
            double endLat = road.getB().getLat();
            double endLng = road.getB().getLng();

            double stopDistance = 100;
            double stopProgress = distance > stopDistance ? (distance - stopDistance) / distance : 0.0;

            double progress = 0.0;
            double step = timeToTravel > 0 ? 16.0 / timeToTravel : 1.0;

            while (progress < stopProgress) {
                progress += step;
                updatePosition(startLat, startLng, endLat, endLng, progress);
                Thread.sleep(16);
            }

            road.getTrafficLight().waitForGreen();

            while (progress < 1.0) {
                progress += step;
                double safeProgress = Math.min(progress, 1.0);
                updatePosition(startLat, startLng, endLat, endLng, safeProgress);
                Thread.sleep(16);
            }

            this.currentLat = endLat;
            this.currentLng = endLng;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            road.removeCar();
            this.currentNode = road.getB();
        }
    }

    private void updatePosition(double sLat, double sLng, double eLat, double eLng, double p) {
        this.currentLat = sLat + (eLat - sLat) * p;
        this.currentLng = sLng + (eLng - sLng) * p;
    }

    public double getCurrentLat() { return currentLat; }
    public double getCurrentLng() { return currentLng; }
    public int getId() { return id; }
    public Node getCurrentNode() { return currentNode; }
}