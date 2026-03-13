package pl.gedlek.model;

import java.util.concurrent.atomic.AtomicInteger;


public class Road{
    private final AtomicInteger carCounter = new AtomicInteger(0);
    private final double distance;
    private final int speedLimit;
    private final Node a;
    private final Node b;
    private final TrafficLight trafficLight;

    public Road(int speedLimit, Node a, Node b) {
        this.speedLimit = speedLimit;
        this.a = a;
        this.b = b;
        this.trafficLight = new TrafficLight(Math.random() > 0.5);
        this.distance = Math.hypot(b.getX() - a.getX(), b.getY() - a.getY());
    }
    public int getSpeedLimit(){
        return speedLimit;
    }
    public Node getA(){
        return this.a;
    }
    public Node getB(){
        return this.b;
    }
    public double getDistance(){
        return this.distance;
    }
    public void addCar(){
        this.carCounter.getAndIncrement();
    }
    public void removeCar(){
        this.carCounter.getAndDecrement();
    }
    public int getCarCount(){
        return carCounter.get();
    }
    public TrafficLight getTrafficLight() {
        return trafficLight;
    }
}