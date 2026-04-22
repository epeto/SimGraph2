package com.simgraph.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ExecutionState {
    private int step;
    private String description;
    /** vertex id → color: "white" | "gray" | "black" | "red" | "green" | "orange" */
    private Map<Integer, String> vertexColors;
    /** vertex id → display label (distance, SCC id, discovery time, etc.).
     * Si se desea que no se desplieguen las etiquetas, este atributo debe ser vacío.
     */
    private Map<Integer, String> vertexLabels;
    /** Edges belonging to the result structure (BFS tree, MST, Euler path, etc.) */
    private List<int[]> treeEdges;
    /** Edges highlighted in this specific step */
    private List<int[]> highlightedEdges;
    /** Algorithm-specific extra data (queue state, matrix, etc.) */
    private Map<String, Object> extra;
}

