package com.simgraph.dto;

import lombok.Data;

@Data
public class EdgeDto {
    private int source;
    private int target;
    private int weight = 1;
}

