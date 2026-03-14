package pl.gedlek.ws;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.gedlek.dto.MapDto;
import pl.gedlek.dto.RoadDto;
import pl.gedlek.service.SimulationService;


@Component
public class SimulationBroadcaster {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final SimulationService simulationService;

    public SimulationBroadcaster(SimpMessagingTemplate simpMessagingTemplate,SimulationService simulationService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.simulationService = simulationService;

    }


    @Scheduled(fixedRate = 33)
    public void broadcastState() {
        var state = simulationService.getDynamicState();
        simpMessagingTemplate.convertAndSend("/topic/state", state);
    }
}