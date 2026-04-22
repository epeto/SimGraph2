package com.simgraph.dto;

import lombok.Data;

import java.util.List;

@Data
public class AlgorithmRequest {
    private List<VertexDto> vertices;
    private List<EdgeDto> edges;
    private boolean directed;
    private int sourceVertex;
    private Integer targetVertex;
}

