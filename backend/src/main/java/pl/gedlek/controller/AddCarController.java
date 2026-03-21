package pl.gedlek.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import pl.gedlek.model.Node;
import pl.gedlek.dto.AddCarRequestDto;
import pl.gedlek.service.*;

@Controller
public class AddCarController {
    private final SimulationService simulationService;

    public AddCarController(SimulationService simulationService){
        this.simulationService = simulationService;
    }

    @MessageMapping("/addCar")
    public void addCar(AddCarRequestDto dto) {
        Node start = simulationService.getCityMap().getNodeByLatAndLng(dto.startLat(), dto.startLng());
        Node target = simulationService.getCityMap().getNodeByLatAndLng(dto.targetLat(), dto.targetLng());

        if (start != null && target != null) {
            simulationService.spawnCar(start, target);
            System.out.println("Auto dodane: " + start.getId() + " -> " + target.getId());
        } else {
            System.err.println("Błąd: Nie znaleziono węzłów GPS! Start: " + start + ", Target: " + target);
        }
    }
}