package pl.gedlek.service;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

        Node n1 = map.addNode(50.087487, 19.891674);  // R. Ofiar Katynia
        Node n2 = map.addNode(50.072678, 19.888011);  // Bronowice
        Node n2_1 = map.addNode(50.081264, 19.890526); //bronowice sk
        Node n3 = map.addNode(50.065871, 19.924420);  // AGH
        Node n3_1 = map.addNode(50.063850, 19.923631);//reymonta
        Node n4 = map.addNode(50.073963, 19.935771);  // Nowy Kleparz
        Node n4_1 = map.addNode(50.071362, 19.929043);  // RadioKrk
        Node n4_2 = map.addNode(50.073373, 19.945737); //29list
        Node n5 = map.addNode(50.055252, 19.927548);  // Jubilat
        Node n6 = map.addNode(50.066031, 19.959478);  // R. Mogilskie
        Node n7 = map.addNode(50.0681, 19.9472);  // Galeria Krakowska
        Node n8 = map.addNode(50.0724, 20.0381);  // Pl. Centralny
        Node n9 = map.addNode(50.0487, 19.9328);  // R. Grunwaldzkie
        Node n10 = map.addNode(50.0574, 19.9587); // R. Grzegórzeckie
        Node n11 = map.addNode(50.036367, 19.940637); // R. Matecznego
        Node n12 = map.addNode(50.018719, 19.988923); // Prokocim
        Node n13 = map.addNode(50.042000, 19.960975); // Podgórze (Powstańców Śląskich/Wielicka)
        Node n14 = map.addNode(49.9871, 20.0645); // Wieliczka
        Node n15 = map.addNode(50.009236, 19.926077); // Borek Fałęcki
        Node n16 = map.addNode(50.0102, 19.9610); // Między Borkiem a Proko (Kurdwanów)
        Node n17 = map.addNode(50.0375, 20.0210); // Rybitwy
        Node n18 = map.addNode(50.113962, 19.913862); // Zielonki
        Node n19 = map.addNode(50.083549, 19.847289); // Mydlniki
        Node n20 = map.addNode(50.064729, 19.880574); // Wola Justowska
        Node n21 = map.addNode(50.080734, 19.973531); // R. Młyńskie
        Node n22 = map.addNode(50.0980, 20.0050); // Mistrzejowice
        Node n23 = map.addNode(50.085121, 19.970450); // Lublanska
        Node n23_1 = map.addNode(50.087709, 20.001392);//Wyslicka
        Node n24 = map.addNode(50.066988, 20.004184); // Tauron Arena
        Node n25 = map.addNode(50.044416, 19.945908); // Korona
        Node n26 = map.addNode(50.051773, 19.941630); // Stradom / Dietla
        Node n27 = map.addNode(50.052735, 19.916824); // Salwator
        Node n28 = map.addNode(50.085025, 20.019174); // Skrzyżowanie w Hucie (Kocmyrzowskie)
        Node n29 = map.addNode(50.086152, 19.954663); // Wyjazd na Węgrzce (Opolska/29 Listopada)
        Node n30 = map.addNode(50.058209, 19.946603); //Hala targowa
        Node n31 = map.addNode(50.045188, 19.973160); // Nowohucka / Klimeckiego
        Node n32 = map.addNode(50.0910, 19.9300); // Opolska / Prądnicka


        // --- PÓŁNOCNA OBWODNICA (Opolska / 29 Listopada) ---
        map.addTwoWayRoad(n1, n4, 60);
        map.addTwoWayRoad(n21, n6, 50);
        map.addTwoWayRoad(n29, n32, 50);
        map.addTwoWayRoad(n32, n18, 50);
        map.addTwoWayRoad(n32, n1, 50);
        map.addTwoWayRoad(n4_2, n29, 50);
        map.addTwoWayRoad(n4_2, n4, 50);
        map.addTwoWayRoad(n23, n29, 50);

        // --- CENTRUM I ALEJE ---
        map.addTwoWayRoad(n4_2, n7, 30);
        map.addTwoWayRoad(n3_1, n5, 25);
        map.addTwoWayRoad(n5, n27, 20);
        map.addTwoWayRoad(n5, n26, 25);
        map.addTwoWayRoad(n26, n25, 20);
        map.addTwoWayRoad(n3, n4_1, 35);
        map.addTwoWayRoad(n4_1, n4, 35);
        map.addTwoWayRoad(n3, n3_1, 35);
        map.addTwoWayRoad(n10, n30, 35);
        map.addTwoWayRoad(n30, n26, 35);

        // --- WISŁA I ZACHÓD ---
        map.addTwoWayRoad(n19, n2_1, 50);
        map.addTwoWayRoad(n2, n2_1, 45);
        map.addTwoWayRoad(n2_1, n1, 45);
        map.addTwoWayRoad(n2, n3, 45);
        map.addTwoWayRoad(n20, n27, 40);
        map.addTwoWayRoad(n5, n9, 20);
        map.addTwoWayRoad(n9, n11, 40);

        // --- WSCHÓD I NOWA HUTA ---
        map.addTwoWayRoad(n7, n6, 25);
        map.addTwoWayRoad(n28, n24, 25);
        map.addTwoWayRoad(n24, n8, 40);
        map.addTwoWayRoad(n21, n23, 30);
        map.addTwoWayRoad(n23, n22, 30);
        map.addTwoWayRoad(n10, n24, 45);
        map.addTwoWayRoad(n31, n10, 40);
        map.addTwoWayRoad(n10, n6, 40);
        map.addTwoWayRoad(n23, n23_1, 40);
        map.addTwoWayRoad(n23_1, n28, 40);
        map.addTwoWayRoad(n23_1, n24, 40);


        // --- POŁUDNIE ---
        map.addTwoWayRoad(n13, n31, 40);
        map.addTwoWayRoad(n11, n25, 30);
        map.addTwoWayRoad(n25, n13, 20);
        map.addTwoWayRoad(n13, n12, 50);
        map.addTwoWayRoad(n12, n16, 30);
        map.addTwoWayRoad(n16, n15, 30);
        map.addTwoWayRoad(n11, n15, 50);
        map.addTwoWayRoad(n12, n14, 70);
        map.addTwoWayRoad(n17, n12, 40);
        map.addTwoWayRoad(n17, n8, 80);

        lightManager = Executors.newScheduledThreadPool(1);
        lightManager.scheduleAtFixedRate(() -> {
            for (Road road : map.getRoads()) {
                road.getTrafficLight().toggleLight(!road.getTrafficLight().isGreen());
            }
        }, 3, 3, TimeUnit.SECONDS);
    }

    public void spawnCar(Node start, Node target) {
        Car car = new Car(cars.size() + 1, start, target);
        cars.add(car);
        car.startDriving(cars.size());
    }

    public CityMap getCityMap() { return map; }
    public List<Car> getCars() { return cars; }

    public SimulationStateDto getDynamicState() {
        List<CarDto> carsDto = cars.stream()
                .map(car -> new CarDto((int) car.getId(), car.getCurrentLat(), car.getCurrentLng()))
                .toList();

        List<LightDto> lightsDto = map.getRoads().stream()
                .map(road -> new LightDto(road.getId(), road.getTrafficLight().isGreen()))
                .toList();

        return new SimulationStateDto(carsDto, lightsDto);
    }

    public MapDto getMapDto() {
        Map<Integer, NodeDto> nodeMap = getCityMap().getNodes().stream()
                .collect(Collectors.toMap(
                        Node::getId,
                        node -> new NodeDto((int) node.getId(), node.getLat(), node.getLng())
                ));

        List<NodeDto> nodesDto = new ArrayList<>(nodeMap.values());

        List<RoadDto> roadsDto = getCityMap().getRoads().stream()
                .map(road -> new RoadDto(
                        road.getId(),
                        nodeMap.get(road.getA().getId()),
                        nodeMap.get(road.getB().getId())
                ))
                .toList();

        return new MapDto(nodesDto, roadsDto);
    }
}