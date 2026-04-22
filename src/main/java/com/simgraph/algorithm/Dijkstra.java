package com.simgraph.algorithm;


import com.simgraph.dto.AlgorithmResponse;
import com.simgraph.model.BinomialQueue;
import com.simgraph.model.ExecutionState;
import com.simgraph.model.Graph;


import java.util.*;


public class Dijkstra {

    private static final int INF = Integer.MAX_VALUE / 2;

    public AlgorithmResponse execute(Graph graph, int source) {
        List<ExecutionState> steps = new ArrayList<>();
        int stepNum = 0;

        Map<Integer, Integer> dist   = new LinkedHashMap<>();
        Map<Integer, String>  colors = new LinkedHashMap<>();
        List<int[]>           treeEdges = new ArrayList<>();
        Set<Integer>          finished  = new HashSet<>();
        BinomialQueue         bq        = new BinomialQueue();

        // Insert all vertices into the heap with key = ∞
        for (int v : graph.getVertices()) {
            dist.put(v, INF);
            colors.put(v, "white");
            bq.insert(v, INF);
        }
        // Decrease-key source → 0
        dist.put(source, 0);
        colors.put(source, "gray");
        bq.decreaseKey(source, 0);

        steps.add(snap(stepNum++,
                "Initialize: all vertices inserted with ∞; decrease-key source " + source + " → 0",
                colors, dist, treeEdges, List.of(), bq.snapshot("decrease", source)));

        while (!bq.isEmpty()) {
            int[] top = bq.extractMin();
            if (top == null || top[1] >= INF) break; // remaining vertices are unreachable
            int u = top[0];
            finished.add(u);
            colors.put(u, "red");

            steps.add(snap(stepNum++,
                    "Extract-min: vertex " + u + " (dist=" + top[1] + ")",
                    colors, dist, treeEdges, List.of(), bq.snapshot("extract", u)));

            for (int[] nb : graph.getNeighbors(u)) {
                int v = nb[0], w = nb[1];
                if (!finished.contains(v) && dist.get(u) + w < dist.get(v)) {
                    dist.put(v, dist.get(u) + w);
                    bq.decreaseKey(v, dist.get(v));
                    colors.put(v, "orange");
                    treeEdges.removeIf(e -> e[1] == v);
                    treeEdges.add(new int[]{u, v});

                    steps.add(snap(stepNum++,
                            "Decrease-key: vertex " + v + " → dist=" + dist.get(v)
                                    + "  (via " + u + ", w=" + w + ")",
                            colors, dist, treeEdges, List.of(new int[]{u, v}),
                            bq.snapshot("decrease", v)));
                    colors.put(v, "gray");
                }
            }
            colors.put(u, "black");
        }

        steps.add(snap(stepNum, "Dijkstra complete", colors, dist, treeEdges, List.of(),
                bq.snapshot(null, -1)));

        return AlgorithmResponse.builder()
                .algorithm("DIJKSTRA")
                .steps(steps)
                .success(true)
                .message("Dijkstra completed from vertex " + source)
                .build();
    }

    private ExecutionState snap(int step, String desc, Map<Integer, String> colors,
            Map<Integer, Integer> dist, List<int[]> treeEdges, List<int[]> highlighted,
            Map<String, Object> bqState) {
        Map<Integer, String> labels = new LinkedHashMap<>();
        dist.forEach((v, d) -> labels.put(v, d >= INF ? "∞" : String.valueOf(d)));
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("bq", bqState);
        return ExecutionState.builder()
                .step(step).description(desc)
                .vertexColors(new LinkedHashMap<>(colors))
                .vertexLabels(labels)
                .treeEdges(new ArrayList<>(treeEdges))
                .highlightedEdges(new ArrayList<>(highlighted))
                .extra(extra)
                .build();
    }
}


