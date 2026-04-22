package com.simgraph.dto;

import java.util.List;

import com.simgraph.model.ExecutionState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AlgorithmResponse {
    private String algorithm;
    private List<ExecutionState> steps;
    private boolean success;
    private String message;
}

