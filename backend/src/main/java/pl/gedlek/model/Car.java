package pl.gedlek.model;

import pl.gedlek.model.AStar;
import pl.gedlek.model.Node;
import pl.gedlek.model.Road;

import java.util.List;

public class Car{
    private final int id;
    private final Node start;
    private final Node target;
    private Node currentNode;
    private volatile double currentX;
    private volatile double currentY;

    public Car(int id,Node start, Node target) {
        this.id = id;
        this.start = start;
        this.target = target;
        this.currentX = start.getX();
        this.currentY = start.getY();
    }

    public void startDriving(int carId) {
        Thread.ofVirtual().start(() -> {
            while (true) {
                List<Road> path = AStar.findPath(start, target);

                if (path.isEmpty()) {
                    return;
                }

                for (Road road : path) {
                    driveOnRoad(road, carId);
                }

                List<Road> back_path = AStar.findPath(target, start);
                if (path.isEmpty()) {
                    return;
                }
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
            long timeToTravel = (long) ((distance / road.getSpeedLimit()) * 1000);

            double startX = road.getA().getX();
            double startY = road.getA().getY();
            double endX = road.getB().getX();
            double endY = road.getB().getY();

            double stopDistance = 25.0;
            double stopProgress = distance > stopDistance ? (distance - stopDistance) / distance : 0.0;

            double progress = 0.0;
            double step = timeToTravel > 0 ? 16.0 / timeToTravel : 1.0;

            while (progress < stopProgress) {
                progress += step;
                this.currentX = startX + (endX - startX) * progress;
                this.currentY = startY + (endY - startY) * progress;
                Thread.sleep(16);
            }


            road.getTrafficLight().waitForGreen();

            while (progress < 1.0) {
                progress += step;
                double safeProgress = Math.min(progress, 1.0);
                this.currentX = startX + (endX - startX) * safeProgress;
                this.currentY = startY + (endY - startY) * safeProgress;
                Thread.sleep(16);
            }

            this.currentX = endX;
            this.currentY = endY;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            road.removeCar();
            this.currentNode = road.getB();
        }
    }

    public double getCurrentX() { return currentX; }
    public double getCurrentY() { return currentY; }
    public int getId() { return id; }
    public Node getCurrentNode() {
        return currentNode;
    }

}