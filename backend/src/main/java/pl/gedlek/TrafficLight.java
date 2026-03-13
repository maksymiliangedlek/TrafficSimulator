package pl.gedlek;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TrafficLight {
    private boolean isGreen;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition greenCondition = lock.newCondition();

    public TrafficLight(boolean startGreen) {
        this.isGreen = startGreen;
    }

    public void waitForGreen() {
        lock.lock();
        try {
            while (!isGreen) {
                greenCondition.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public void toggleLight(boolean green) {
        lock.lock();
        try {
            this.isGreen = green;
            if (this.isGreen) {
                greenCondition.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isGreen() {
        return isGreen;
    }

    public static class SimulationUI extends Application {

        private CityMap map;
        // CopyOnWriteArrayList zapobiega błędom, gdy dodajemy auto podczas renderowania klatki!
        private List<Car> cars = new CopyOnWriteArrayList<>();
        private ScheduledExecutorService lightManager;

        // --- STAN INTERAKCJI UŻYTKOWNIKA ---
        private enum AppState { IDLE, WAITING_FOR_START, WAITING_FOR_TARGET }
        private AppState currentState = AppState.IDLE;
        private Node selectedStartNode = null;

        // UI Elements
        private Label statusLabel;
        private Label carCountLabel;

        public static void main(String[] args) {
            launch(args);
        }

        @Override
        public void start(Stage primaryStage) {
            setupBackendSimulation(); // Budujemy mapę (ale na razie bez aut!)

            // 1. GŁÓWNY LAYOUT (Panel boczny + Mapa)
            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: #1e272e;"); // Eleganckie ciemne tło

            // 2. PANEL BOCZNY (Sidebar)
            VBox sidebar = new VBox(20);
            sidebar.setPadding(new Insets(20));
            sidebar.setPrefWidth(250);
            sidebar.setStyle("-fx-background-color: #2f3640; -fx-border-color: #353b48; -fx-border-width: 0 2 0 0;");
            sidebar.setAlignment(Pos.TOP_CENTER);

            Label titleLabel = new Label("🚦 Symulator Ruchu");
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
            titleLabel.setTextFill(Color.WHITE);

            carCountLabel = new Label("Aktywne auta: 0");
            carCountLabel.setTextFill(Color.web("#bdc3c7"));

            statusLabel = new Label("Status: Gotowy");
            statusLabel.setTextFill(Color.web("#f1c40f"));
            statusLabel.setWrapText(true);

            Button addCarBtn = new Button("➕ Dodaj Auto");
            addCarBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand;");

            // Akcja przycisku: Zmiana stanu na "Czekam na punkt startowy"
            addCarBtn.setOnAction(e -> {
                currentState = AppState.WAITING_FOR_START;
                selectedStartNode = null;
                statusLabel.setText("👉 Kliknij skrzyżowanie STARTOWE na mapie.");
                statusLabel.setTextFill(Color.web("#00cec9"));
            });

            sidebar.getChildren().addAll(titleLabel, carCountLabel, addCarBtn, statusLabel);
            root.setLeft(sidebar);

            // 3. PŁÓTNO (Canvas)
            Canvas canvas = new Canvas(1000, 1000);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            root.setCenter(canvas);

            // --- OBSŁUGA KLIKANIA W MAPĘ ---
            canvas.setOnMouseClicked(event -> handleMapClick(event.getX(), event.getY()));

            // 4. PĘTLA RENDEROWANIA (60 FPS)
            AnimationTimer timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    render(gc);
                    // Aktualizujemy licznik aut w UI
                    carCountLabel.setText("Aktywne auta: " + cars.size());
                }
            };
            timer.start();

            // 5. ODPALENIE OKNA
            primaryStage.setScene(new Scene(root, 1250, 1000)); // Szersze okno, żeby zmieścił się sidebar
            primaryStage.setTitle("Symulator Ruchu Drogowego (Java 25 + UI)");
            primaryStage.setOnCloseRequest(e -> lightManager.shutdownNow());
            primaryStage.show();
        }

        // --- LOGIKA KLIKANIA ---
        private void handleMapClick(double mouseX, double mouseY) {
            if (currentState == AppState.IDLE) return; // Jeśli użytkownik nie kliknął "Dodaj Auto", ignorujemy

            // Szukamy skrzyżowania w promieniu 20 pikseli od kliknięcia
            Node clickedNode = findNodeAt(mouseX, mouseY, 20);

            if (clickedNode != null) {
                if (currentState == AppState.WAITING_FOR_START) {
                    selectedStartNode = clickedNode;
                    currentState = AppState.WAITING_FOR_TARGET;
                    statusLabel.setText("🎯 Teraz kliknij skrzyżowanie DOCELOWE.");
                    statusLabel.setTextFill(Color.web("#fd79a8"));
                }
                else if (currentState == AppState.WAITING_FOR_TARGET) {
                    if (clickedNode != selectedStartNode) { // Nie pozwalamy jechać do tego samego punktu
                        // TWORZYMY AUTO!
                        spawnCar(selectedStartNode, clickedNode);

                        // Resetujemy stan
                        currentState = AppState.IDLE;
                        selectedStartNode = null;
                        statusLabel.setText("✅ Auto dodane! Status: Gotowy");
                        statusLabel.setTextFill(Color.web("#f1c40f"));
                    }
                }
            }
        }

        // Pomocnicza metoda: Tworzy nowe auto i odpala mu wątek
        private void spawnCar(Node start, Node target) {
            Car car = new Car(start, target);
            cars.add(car);
            car.startDriving(cars.size()); // Odpalamy wątek Virtualny!
        }

        // Pomocnicza metoda: Wykrywanie kolizji myszki z punktem (Twierdzenie Pitagorasa)
        private Node findNodeAt(double x, double y, double radius) {
            for (Node node : map.getNodes()) {
                double distance = Math.hypot(node.getX() - x, node.getY() - y);
                if (distance <= radius) {
                    return node;
                }
            }
            return null;
        }

        // --- RYSOWANIE GRAFIKI ---
        private void render(GraphicsContext gc) {
            gc.clearRect(0, 0, 1000, 1000);

            // 1. Rysowanie Dróg
            gc.setLineWidth(6); // Grubsze drogi
            for (Road road : map.getRoads()) {
                if (road.getTrafficLight().isGreen()) {
                    gc.setStroke(Color.web("#27ae60", 0.6)); // Zielone światło
                } else {
                    gc.setStroke(Color.web("#c0392b", 0.6)); // Czerwone światło
                }
                gc.strokeLine(road.getA().getX(), road.getA().getY(), road.getB().getX(), road.getB().getY());
            }

            // 2. Rysowanie Skrzyżowań
            for (Node node : map.getNodes()) {
                // Jeśli to węzeł startowy wybrany przez usera, zrób mu "efekt halo"
                if (node == selectedStartNode) {
                    gc.setFill(Color.web("#00cec9", 0.4));
                    gc.fillOval(node.getX() - 20, node.getY() - 20, 40, 40);
                }
                // Zwykłe skrzyżowanie
                gc.setFill(Color.web("#7f8c8d"));
                gc.fillOval(node.getX() - 12, node.getY() - 12, 24, 24);
            }

            // 3. Rysowanie Samochodów
            gc.setFill(Color.web("#f1c40f")); // Żółte autka
            for (Car car : cars) {
                gc.fillOval(car.getCurrentX() - 6, car.getCurrentY() - 6, 12, 12);
            }
        }

        // --- SETUP BACKENDU (Sama mapa, zero aut na start) ---
        private void setupBackendSimulation() {
            map = new CityMap();

            // --- TWOJA DOJEBANA MAPA 17 WĘZŁÓW ---
            Node centerNW = map.addNode(300, 300); Node centerN  = map.addNode(500, 300); Node centerNE = map.addNode(700, 300);
            Node centerW  = map.addNode(300, 500); Node center   = map.addNode(500, 500); Node centerE  = map.addNode(700, 500);
            Node centerSW = map.addNode(300, 700); Node centerS  = map.addNode(500, 700); Node centerSE = map.addNode(700, 700);

            Node ringNW = map.addNode(100, 100); Node ringN  = map.addNode(500, 100); Node ringNE = map.addNode(900, 100);
            Node ringW  = map.addNode(100, 500);                                        Node ringE  = map.addNode(900, 500);
            Node ringSW = map.addNode(100, 900); Node ringS  = map.addNode(500, 900); Node ringSE = map.addNode(900, 900);

            // Ulice w centrum (50 km/h)
            map.addTwoWayRoad(centerNW, centerN, 50); map.addTwoWayRoad(centerN, centerNE, 50);
            map.addTwoWayRoad(centerW, center, 50);   map.addTwoWayRoad(center, centerE, 50);
            map.addTwoWayRoad(centerSW, centerS, 50); map.addTwoWayRoad(centerS, centerSE, 50);
            map.addTwoWayRoad(centerNW, centerW, 50); map.addTwoWayRoad(centerW, centerSW, 50);
            map.addTwoWayRoad(centerN, center, 50);   map.addTwoWayRoad(center, centerS, 50);
            map.addTwoWayRoad(centerNE, centerE, 50); map.addTwoWayRoad(centerE, centerSE, 50);

            // Drogi dojazdowe (70 km/h)
            map.addTwoWayRoad(ringNW, centerNW, 70); map.addTwoWayRoad(ringN, centerN, 70);
            map.addTwoWayRoad(ringNE, centerNE, 70); map.addTwoWayRoad(ringW, centerW, 70);
            map.addTwoWayRoad(ringE, centerE, 70);   map.addTwoWayRoad(ringSW, centerSW, 70);
            map.addTwoWayRoad(ringS, centerS, 70);   map.addTwoWayRoad(ringSE, centerSE, 70);

            // Obwodnica (90 km/h)
            map.addTwoWayRoad(ringNW, ringN, 90); map.addTwoWayRoad(ringN, ringNE, 90);
            map.addTwoWayRoad(ringNE, ringE, 90); map.addTwoWayRoad(ringE, ringSE, 90);
            map.addTwoWayRoad(ringSE, ringS, 90); map.addTwoWayRoad(ringS, ringSW, 90);
            map.addTwoWayRoad(ringSW, ringW, 90); map.addTwoWayRoad(ringW, ringNW, 90);

            // System Świateł - odpalamy cykl co 3 sekundy
            lightManager = Executors.newScheduledThreadPool(1);
            lightManager.scheduleAtFixedRate(() -> {
                for (Road road : map.getRoads()) {
                    road.getTrafficLight().toggleLight(!road.getTrafficLight().isGreen());
                }
            }, 3, 3, TimeUnit.SECONDS);

            // UWAGA: Usunęliśmy stąd pętlę wpuszczającą 200 aut.
            // Od teraz miasto startuje całkowicie puste i czeka na Ciebie!
        }
    }
}
