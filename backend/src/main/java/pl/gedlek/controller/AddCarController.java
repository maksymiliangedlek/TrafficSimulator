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
    public void addCar(AddCarRequestDto addCarRequestDto) throws Exception {
        var dane = addCarRequestDto;
        Node start = simulationService.getCityMap().getNodeByXandY(dane.getstartX(),dane.getstartY());
        Node target = simulationService.getCityMap().getNodeByXandY(dane.gettargetX(),dane.gettargetY());

        simulationService.spawnCar(start,target);
    }

}