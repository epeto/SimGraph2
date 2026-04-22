package com.simgraph.algorithm;

import com.simgraph.dto.AlgorithmResponse;
import com.simgraph.model.ExecutionState;
import com.simgraph.model.Graph;

import java.util.*;

public class BellmanFord {

    private static final int INF = Integer.MAX_VALUE / 2;

    public AlgorithmResponse execute(Graph graph, int source) {
        List<ExecutionState> steps = new ArrayList<>();
        int stepNum = 0;

        Map<Integer, Integer> dist = new LinkedHashMap<>();
        Map<Integer, Integer> parent = new LinkedHashMap<>();
        Map<Integer, String> colors = new LinkedHashMap<>();
        List<int[]> treeEdges = new ArrayList<>();

        for (int v : graph.getVertices()) {
            dist.put(v, INF);
            parent.put(v, -1);
            colors.put(v, "white");
        }
        dist.put(source, 0);
        colors.put(source, "gray");

        int n = graph.getOrder();
        List<int[]> edges = graph.getEdges();

        steps.add(snap(stepNum++, "Initialize Bellman-Ford from vertex " + source,
                colors, dist, treeEdges, List.of()));

        for (int i = 1; i < n; i++) {
            boolean updated = false;
            for (int[] e : edges) {
                int u = e[0], v = e[1], w = e[2];
                if (dist.get(u) < INF && dist.get(u) + w < dist.get(v)) {
                    dist.put(v, dist.get(u) + w);
                    parent.put(v, u);
                    updated = true;

                    treeEdges.removeIf(te -> te[1] == v);
                    treeEdges.add(new int[]{u, v});
                    colors.put(v, "orange");

                    steps.add(snap(stepNum++,
                            "Iteration " + i + ": relax (" + u + "→" + v + ") → dist[" + v + "]=" + dist.get(v),
                            colors, dist, treeEdges, List.of(new int[]{u, v})));
                    colors.put(v, "gray");
                }
            }
            if (!updated) {
                steps.add(snap(stepNum++, "Iteration " + i + ": no updates — converged early",
                        colors, dist, treeEdges, List.of()));
                break;
            }
        }

        // Check for negative-weight cycles
        boolean hasNegCycle = false;
        for (int[] e : edges) {
            int u = e[0], v = e[1], w = e[2];
            if (dist.get(u) < INF && dist.get(u) + w < dist.get(v)) {
                hasNegCycle = true;
                break;
            }
        }

        for (int v : graph.getVertices()) colors.put(v, "black");

        String finalMsg = hasNegCycle
                ? "Negative-weight cycle detected — shortest paths undefined"
                : "Bellman-Ford complete";
        steps.add(snap(stepNum, finalMsg, colors, dist, treeEdges, List.of()));

        return AlgorithmResponse.builder()
                .algorithm("BELLMAN_FORD")
                .steps(steps)
                .success(!hasNegCycle)
                .message(finalMsg)
                .build();
    }

    private ExecutionState snap(int step, String desc, Map<Integer, String> colors,
            Map<Integer, Integer> dist, List<int[]> treeEdges, List<int[]> highlighted) {
        Map<Integer, String> labels = new LinkedHashMap<>();
        dist.forEach((v, d) -> labels.put(v, d >= INF ? "∞" : String.valueOf(d)));
        return ExecutionState.builder()
                .step(step).description(desc)
                .vertexColors(new LinkedHashMap<>(colors))
                .vertexLabels(labels)
                .treeEdges(new ArrayList<>(treeEdges))
                .highlightedEdges(new ArrayList<>(highlighted))
                .extra(Map.of())
                .build();
    }
}

