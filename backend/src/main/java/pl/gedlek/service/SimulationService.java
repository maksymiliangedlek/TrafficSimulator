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

        // --- DEFINICJA WĘZŁÓW (KOORDYNATY BEZ ZMIAN, ID POKOLEI) ---
        Node n1 = map.addNode(50.087487, 19.891674);  // R. Ofiar Katynia
        Node n2 = map.addNode(50.072678, 19.888011);  // Bronowice
        Node n3 = map.addNode(50.081264, 19.890526);  // Bronowice SK
        Node n4 = map.addNode(50.065871, 19.924420);  // AGH
        Node n5 = map.addNode(50.063850, 19.923631);  // Reymonta
        Node n6 = map.addNode(50.073963, 19.935771);  // Nowy Kleparz
        Node n7 = map.addNode(50.071362, 19.929043);  // RadioKrk
        Node n8 = map.addNode(50.073373, 19.945737);  // 29 Listopada
        Node n9 = map.addNode(50.055252, 19.927548);  // Jubilat
        Node n10 = map.addNode(50.066031, 19.959478); // R. Mogilskie
        Node n11 = map.addNode(50.0681, 19.9472);     // Galeria Krakowska
        Node n12 = map.addNode(50.0724, 20.0381);     // Pl. Centralny
        Node n13 = map.addNode(50.0487, 19.9328);     // R. Grunwaldzkie
        Node n14 = map.addNode(50.0574, 19.9587);     // R. Grzegórzeckie
        Node n15 = map.addNode(50.036367, 19.940637); // R. Matecznego
        Node n16 = map.addNode(50.018719, 19.988923); // Prokocim
        Node n17 = map.addNode(50.042000, 19.960975); // Podgórze
        Node n18 = map.addNode(49.9871, 20.0645);     // Wieliczka
        Node n19 = map.addNode(50.009236, 19.926077); // Borek Fałęcki
        Node n20 = map.addNode(50.0102, 19.9610);     // Kurdwanów
        Node n21 = map.addNode(50.0375, 20.0210);     // Rybitwy
        Node n22 = map.addNode(50.113962, 19.913862); // Zielonki
        Node n23 = map.addNode(50.083549, 19.847289); // Mydlniki
        Node n24 = map.addNode(50.064729, 19.880574); // Wola Justowska
        Node n25 = map.addNode(50.080734, 19.973531); // R. Młyńskie
        Node n26 = map.addNode(50.0980, 20.0050);     // Mistrzejowice
        Node n27 = map.addNode(50.085121, 19.970450); // Lublańska
        Node n28 = map.addNode(50.087709, 20.001392); // Wiślicka
        Node n29 = map.addNode(50.066988, 20.004184); // Tauron Arena
        Node n30 = map.addNode(50.044416, 19.945908); // Korona
        Node n31 = map.addNode(50.051773, 19.941630); // Stradom / Dietla
        Node n32 = map.addNode(50.052735, 19.916824); // Salwator
        Node n33 = map.addNode(50.085025, 20.019174); // R. Kocmyrzowskie
        Node n34 = map.addNode(50.086152, 19.954663); // Wyjazd na Węgrzce
        Node n35 = map.addNode(50.058209, 19.946603); // Hala Targowa
        Node n36 = map.addNode(50.045188, 19.973160); // Nowohucka / Klimeckiego
        Node n37 = map.addNode(50.0910, 19.9300);     // Opolska / Prądnicka

        // --- PÓŁNOCNA OBWODNICA (SZYBKO) ---
        map.addTwoWayRoad(n1, n6, 70);   // R. Ofiar Katynia - Nowy Kleparz
        map.addTwoWayRoad(n25, n10, 60);  // R. Młyńskie - R. Mogilskie
        map.addTwoWayRoad(n34, n37, 70);  // Węgrzce - Opolska
        map.addTwoWayRoad(n37, n22, 60);  // Opolska - Zielonki
        map.addTwoWayRoad(n37, n1, 70);   // Opolska - Ofiar Katynia
        map.addTwoWayRoad(n8, n34, 60);   // 29 Listopada - Węgrzce
        map.addTwoWayRoad(n8, n6, 50);    // 29 Listopada - Nowy Kleparz
        map.addTwoWayRoad(n27, n34, 70);  // Lublańska - Węgrzce

        // --- CENTRUM I ALEJE (MIEJSKO) ---
        map.addTwoWayRoad(n8, n11, 50);   // 29 Listopada - Galeria
        map.addTwoWayRoad(n5, n9, 40);    // Reymonta - Jubilat
        map.addTwoWayRoad(n9, n32, 40);   // Jubilat - Salwator
        map.addTwoWayRoad(n9, n31, 40);   // Jubilat - Stradom
        map.addTwoWayRoad(n31, n30, 40);  // Stradom - Korona
        map.addTwoWayRoad(n4, n7, 50);    // AGH - RadioKrk
        map.addTwoWayRoad(n7, n6, 50);    // RadioKrk - Nowy Kleparz
        map.addTwoWayRoad(n4, n5, 40);    // AGH - Reymonta
        map.addTwoWayRoad(n14, n35, 40);  // R. Grzegórzeckie - Hala Targowa
        map.addTwoWayRoad(n35, n31, 30);  // Hala Targowa - Stradom

        // --- WISŁA I ZACHÓD ---
        map.addTwoWayRoad(n23, n3, 60);   // Mydlniki - Bronowice SK
        map.addTwoWayRoad(n2, n3, 50);    // Bronowice - Bronowice SK
        map.addTwoWayRoad(n3, n1, 60);    // Bronowice SK - Ofiar Katynia
        map.addTwoWayRoad(n2, n4, 50);    // Bronowice - AGH
        map.addTwoWayRoad(n24, n32, 50);  // Wola Justowska - Salwator
        map.addTwoWayRoad(n9, n13, 50);   // Jubilat - R. Grunwaldzkie
        map.addTwoWayRoad(n13, n15, 60);  // R. Grunwaldzkie - Matecznego

        // --- WSCHÓD I NOWA HUTA ---
        map.addTwoWayRoad(n11, n10, 50);  // Galeria - R. Mogilskie
        map.addTwoWayRoad(n33, n29, 60);  // R. Kocmyrzowskie - Tauron Arena
        map.addTwoWayRoad(n29, n12, 60);  // Tauron Arena - Pl. Centralny
        map.addTwoWayRoad(n25, n27, 60);  // R. Młyńskie - Lublańska
        map.addTwoWayRoad(n27, n26, 60);  // Lublańska - Mistrzejowice
        map.addTwoWayRoad(n14, n29, 50);  // R. Grzegórzeckie - Tauron Arena
        map.addTwoWayRoad(n36, n14, 60);  // Nowohucka - R. Grzegórzeckie
        map.addTwoWayRoad(n14, n10, 50);  // R. Grzegórzeckie - R. Mogilskie
        map.addTwoWayRoad(n27, n28, 60);  // Lublańska - Wiślicka
        map.addTwoWayRoad(n28, n33, 60);  // Wiślicka - R. Kocmyrzowskie
        map.addTwoWayRoad(n28, n29, 60);  // Wiślicka - Tauron Arena
        map.addTwoWayRoad(n33, n12, 60);

        // --- POŁUDNIE (TRASA WYLOTOWA) ---
        map.addTwoWayRoad(n17, n36, 60);  // Podgórze - Nowohucka
        map.addTwoWayRoad(n15, n30, 50);  // Matecznego - Korona
        map.addTwoWayRoad(n30, n17, 50);  // Korona - Podgórze
        map.addTwoWayRoad(n17, n16, 60);  // Podgórze - Prokocim
        map.addTwoWayRoad(n16, n20, 50);  // Prokocim - Kurdwanów
        map.addTwoWayRoad(n20, n19, 50);  // Kurdwanów - Borek Fałęcki
        map.addTwoWayRoad(n15, n19, 60);  // Matecznego - Borek Fałęcki
        map.addTwoWayRoad(n16, n18, 80);  // Prokocim - Wieliczka
        map.addTwoWayRoad(n21, n16, 60);  // Rybitwy - Prokocim
        map.addTwoWayRoad(n21, n12, 80);  // Rybitwy - Pl. Centralny

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