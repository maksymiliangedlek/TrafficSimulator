package pl.gedlek.dto;

import java.util.List;

public record MapDto(List<NodeDto> nodes, List<RoadDto> roads) {}