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

        Node n1 = map.addNode(408, 270);  // R. Ofiar Katynia
        Node n2 = map.addNode(394, 367);  // Bronowice
        Node n3 = map.addNode(577, 418);  // agh
        Node n4 = map.addNode(631, 365);  // nowy kleparz
        Node n5 = map.addNode(595, 495);  // jubilat
        Node n6 = map.addNode(754, 419);  // R. Mogilskie
        Node n7 = map.addNode(685, 421); // galeria krakowska
        Node n8 = map.addNode(1114, 382); // Pl. Centralny
        Node n9 = map.addNode(614, 541); // grunwaldzkie
        Node n10 = map.addNode(754, 480); // R. Grzegórzeckie
        Node n11 = map.addNode(656, 627); // R. Matecznego
        Node n12 = map.addNode(853, 713); // prokocim
        Node n13 = map.addNode(823, 561); // podgorze
        Node n14 = map.addNode(1270, 970); // wieliczka
        Node n15 = map.addNode(579, 831); // Borek Fałęcki
        Node n16 = map.addNode(782, 783); //miedzy borkiem a proko
        Node n17 = map.addNode(1189, 579); //rybitwy
        Node n18 = map.addNode(561, 55); //zielonki
        Node n19 = map.addNode(234, 307); // mydlniki
        Node n20 = map.addNode(311, 556); //wola justowska
        Node n21 = map.addNode(825, 319); //rondo mlynskie
        Node n22 = map.addNode(938, 211); //mistrzejowice
        Node n23 = map.addNode(831, 258); // rondo barei
        Node n24 = map.addNode(874, 383); //skrzyzowanie kolo tauron areny z jana pawla
        Node n25 = map.addNode(682, 570); //korona
        Node n26 = map.addNode(681, 482); //stradom
        Node n27 = map.addNode(482, 533); //salwator
        Node n28 = map.addNode(978, 418); //skrzyzowanie w hucie
        Node n29 = map.addNode(732, 281); //wyjazd na wegrzce opolska
        Node n30 = map.addNode(960, 486); //idk
        Node n31 = map.addNode(758, 516); //tez idk
        Node n32 = map.addNode(603, 240); // opolska wyjazd na zielonki


        // --- PÓŁNOCNA OBWODNICA (Opolska / 29 Listopada) ---
        map.addTwoWayRoad(n1, n4, 60);   // R. Ofiar Katynia -> Nowy Kleparz
        map.addTwoWayRoad(n4, n29, 40);  // Nowy Kleparz -> Wyjazd na Węgrzce (Opolska)
        map.addTwoWayRoad(n29, n21, 40); // Węgrzce -> R. Młyńskie
        map.addTwoWayRoad(n18, n4, 50);
        map.addTwoWayRoad(n32, n23, 40);
        map.addTwoWayRoad(n21, n6, 50);
        map.addTwoWayRoad(n29, n32, 50);
        map.addTwoWayRoad(n32, n18, 50);
        map.addTwoWayRoad(n32, n1, 50);

// --- CENTRUM I ALEJE ---
        map.addTwoWayRoad(n4, n7, 30);   // Nowy Kleparz -> Galeria Krk
        map.addTwoWayRoad(n7, n3, 30);   // Galeria -> AGH
        map.addTwoWayRoad(n3, n5, 25);   // AGH -> Jubilat
        map.addTwoWayRoad(n5, n27, 20);  // Jubilat -> Salwator
        map.addTwoWayRoad(n5, n26, 25);  // Jubilat -> Stradom (ul. Dietla)
        map.addTwoWayRoad(n26, n25, 20); // Stradom -> Korona (Krakowska)
        map.addTwoWayRoad(n26, n10, 35); // Stradom -> R. Grzegórzeckie (Dietla)
        map.addTwoWayRoad(n3, n4, 35);

// --- WISŁA I ZACHÓD ---
        map.addTwoWayRoad(n19, n1, 50);  // Mydlniki -> Ofiar Katynia
        map.addTwoWayRoad(n1, n2, 40); // brono -> ofiar
        map.addTwoWayRoad(n2, n3, 45);   // Bronowice -> AGH
        map.addTwoWayRoad(n20, n27, 40); // Wola Justowska -> Salwator
        map.addTwoWayRoad(n5, n9, 20);   // Jubilat -> Grunwaldzkie (Most Dębnicki)
        map.addTwoWayRoad(n9, n25, 30);  // Grunwaldzkie -> Korona (Most Grunwaldzki)
        map.addTwoWayRoad(n9, n11, 40);  // Grunwaldzkie -> Matecznego

// --- WSCHÓD I NOWA HUTA ---
        map.addTwoWayRoad(n7, n6, 25);   // Galeria -> R. Mogilskie
        map.addTwoWayRoad(n28, n24, 25); // Skrzyżowanie Huta -> Tauron Arena
        map.addTwoWayRoad(n24, n8, 40);  // Tauron Arena -> Pl. Centralny
        map.addTwoWayRoad(n21, n23, 30); // R. Młyńskie -> R. Barei
        map.addTwoWayRoad(n23, n22, 30); // R. Barei -> Mistrzejowice
        map.addTwoWayRoad(n10, n24, 45); // R. Grzegórzeckie -> Tauron Arena
        map.addTwoWayRoad(n13, n30, 40);
        map.addTwoWayRoad(n28, n30, 40);
        map.addTwoWayRoad(n31, n10, 40);
        map.addTwoWayRoad(n10, n6, 40);

// --- POŁUDNIE ---
        map.addTwoWayRoad(n13, n31, 40);
        map.addTwoWayRoad(n31, n26, 30);
        map.addTwoWayRoad(n11, n25, 30); // Matecznego -> Korona
        map.addTwoWayRoad(n25, n13, 20); // Korona -> Podgórze
        map.addTwoWayRoad(n13, n12, 50); // Podgórze -> Prokocim
        map.addTwoWayRoad(n12, n16, 30); // Prokocim -> Między Borkiem a Proko
        map.addTwoWayRoad(n16, n15, 30); // Między... -> Borek Fałęcki
        map.addTwoWayRoad(n11, n15, 50); // Matecznego -> Borek (Zakopiańska)
        map.addTwoWayRoad(n12, n14, 70); // Prokocim -> Wieliczka
        map.addTwoWayRoad(n17, n12, 40); // Rybitwy -> Prokocim
        map.addTwoWayRoad(n17, n8, 80);  // Rybitwy -> Pl. Centralny

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
        List<LightDto> lightsDto = map.getRoads().stream().map(road -> new LightDto(road.getId(),road.getTrafficLight().isGreen())).toList();
        return new SimulationStateDto(carsDto, lightsDto);
    }

    public MapDto getMapDto() {
        Map<Integer, NodeDto> nodeMap = getCityMap().getNodes().stream()
                .collect(Collectors.toMap(
                        Node::getId,
                        node -> new NodeDto((int) node.getId(), node.getX(), node.getY())
                ));

        List<NodeDto> nodesDto = new ArrayList<>(nodeMap.values());

        List<RoadDto> roadsDto = getCityMap().getRoads().stream()
                .map(road -> new RoadDto(
                        road.getId(),
                        nodeMap.get(road.getA().getId()),
                        nodeMap.get(road.getB().getId())
                ))
                .toList();

        return new MapDto(nodesDto,roadsDto);
    }
}