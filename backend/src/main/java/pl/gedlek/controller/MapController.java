package pl.gedlek.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.gedlek.dto.MapDto;
import pl.gedlek.service.SimulationService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MapController {

    private final SimulationService simulationService;

    public MapController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/map")
    public ResponseEntity<MapDto> getMap() {
        MapDto mapDto = simulationService.getMapDto();
        return ResponseEntity.ok(mapDto);
    }
}