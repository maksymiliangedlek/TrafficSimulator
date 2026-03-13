package pl.gedlek.service;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import pl.gedlek.model.CityMap;
import pl.gedlek.dto.*;
import pl.gedlek.model.Car;
import pl.gedlek.model.Node;
import pl.gedlek.model.Road;

@Service
public class SimulationService {

    private CityMap map;
    private final List<Car> cars = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService lightManager;

    @PostConstruct
    public void initSimulation() {
        map = new CityMap();

        Node centerNW = map.addNode(300, 300); Node centerN  = map.addNode(500, 300); Node centerNE = map.addNode(700, 300);
        Node centerW  = map.addNode(300, 500); Node center   = map.addNode(500, 500); Node centerE  = map.addNode(700, 500);
        Node centerSW = map.addNode(300, 700); Node centerS  = map.addNode(500, 700); Node centerSE = map.addNode(700, 700);

        Node ringNW = map.addNode(100, 100); Node ringN  = map.addNode(500, 100); Node ringNE = map.addNode(900, 100);
        Node ringW  = map.addNode(100, 500);                                        Node ringE  = map.addNode(900, 500);
        Node ringSW = map.addNode(100, 900); Node ringS  = map.addNode(500, 900); Node ringSE = map.addNode(900, 900);

        map.addTwoWayRoad(centerNW, centerN, 50); map.addTwoWayRoad(centerN, centerNE, 50);
        map.addTwoWayRoad(centerW, center, 50);   map.addTwoWayRoad(center, centerE, 50);
        map.addTwoWayRoad(centerSW, centerS, 50); map.addTwoWayRoad(centerS, centerSE, 50);
        map.addTwoWayRoad(centerNW, centerW, 50); map.addTwoWayRoad(centerW, centerSW, 50);
        map.addTwoWayRoad(centerN, center, 50);   map.addTwoWayRoad(center, centerS, 50);
        map.addTwoWayRoad(centerNE, centerE, 50); map.addTwoWayRoad(centerE, centerSE, 50);

        map.addTwoWayRoad(ringNW, centerNW, 70); map.addTwoWayRoad(ringN, centerN, 70);
        map.addTwoWayRoad(ringNE, centerNE, 70); map.addTwoWayRoad(ringW, centerW, 70);
        map.addTwoWayRoad(ringE, centerE, 70);   map.addTwoWayRoad(ringSW, centerSW, 70);
        map.addTwoWayRoad(ringS, centerS, 70);   map.addTwoWayRoad(ringSE, centerSE, 70);

        map.addTwoWayRoad(ringNW, ringN, 90); map.addTwoWayRoad(ringN, ringNE, 90);
        map.addTwoWayRoad(ringNE, ringE, 90); map.addTwoWayRoad(ringE, ringSE, 90);
        map.addTwoWayRoad(ringSE, ringS, 90); map.addTwoWayRoad(ringS, ringSW, 90);
        map.addTwoWayRoad(ringSW, ringW, 90); map.addTwoWayRoad(ringW, ringNW, 90);

        lightManager = Executors.newScheduledThreadPool(1);
        lightManager.scheduleAtFixedRate(() -> {
            for (Road road : map.getRoads()) {
                road.getTrafficLight().toggleLight(!road.getTrafficLight().isGreen());
            }
        }, 3, 3, TimeUnit.SECONDS);
    }

    public void spawnCar(Node start, Node target) {
        Car car = new Car(cars.size()+1,start, target);
        cars.add(car);
        car.startDriving(cars.size());
    }
    public CityMap getCityMap() { return map; }
    public List<Car> getCars() { return cars; }

    public SimulationStateDto getDynamicState(){
        List<CarDto> carsDto = cars.stream()
                .map(car -> new CarDto((int) car.getId(), car.getCurrentX(), car.getCurrentY()))
                .toList();
        return new SimulationStateDto(carsDto);
    }
}