package com.simgraph.algorithm;

import com.simgraph.dto.AlgorithmResponse;
import com.simgraph.model.ExecutionState;
import com.simgraph.model.Graph;

import java.util.*;

/**
 * Finds biconnected components (blocks) using DFS with an edge stack.
 */
public class Blocks {

    private Map<Integer, Integer> disc, low, parent;
    private Map<Integer, String> colors;
    private Deque<int[]> edgeStack;
    private List<List<int[]>> blocks;
    private List<int[]> treeEdges;
    private List<ExecutionState> steps;
    private int time, stepNum;
    private Graph graph;

    public AlgorithmResponse execute(Graph graph) {
        this.graph = graph;
        disc = new LinkedHashMap<>(); low = new LinkedHashMap<>(); parent = new LinkedHashMap<>();
        colors = new LinkedHashMap<>(); edgeStack = new ArrayDeque<>();
        blocks = new ArrayList<>(); treeEdges = new ArrayList<>();
        steps = new ArrayList<>(); time = 0; stepNum = 0;

        for (int v : graph.getVertices()) {
            disc.put(v, -1); low.put(v, -1); parent.put(v, -1); colors.put(v, "white");
        }

        steps.add(snap("Initialize biconnected components search"));

        for (int v : graph.getVertices()) {
            if (disc.get(v) == -1) dfs(v);
        }

        steps.add(ExecutionState.builder()
                .step(stepNum++).description("Found " + blocks.size() + " biconnected component(s)")
                .vertexColors(new LinkedHashMap<>(colors)).vertexLabels(Map.of())
                .treeEdges(new ArrayList<>(treeEdges)).highlightedEdges(List.of())
                .extra(Map.of("blockCount", blocks.size()))
                .build());

        return AlgorithmResponse.builder()
                .algorithm("BLOCKS")
                .steps(steps)
                .success(true)
                .message(blocks.size() + " biconnected components found")
                .build();
    }

    private void dfs(int u) {
        disc.put(u, time); low.put(u, time); time++;
        colors.put(u, "gray");
        steps.add(snap("Discover vertex " + u));

        int children = 0;
        for (int[] nb : graph.getNeighbors(u)) {
            int v = nb[0];
            if (disc.get(v) == -1) {
                children++;
                parent.put(v, u);
                treeEdges.add(new int[]{u, v});
                edgeStack.push(new int[]{u, v});
                dfs(v);
                low.put(u, Math.min(low.get(u), low.get(v)));

                boolean isRoot = parent.get(u) == -1;
                if ((isRoot && children > 1) || (!isRoot && low.get(v) >= disc.get(u))) {
                    // Pop block
                    List<int[]> block = new ArrayList<>();
                    while (true) {
                        int[] top = edgeStack.pop();
                        block.add(top);
                        if (top[0] == u && top[1] == v) break;
                    }
                    blocks.add(block);
                    steps.add(snap("Found block #" + blocks.size() + " with " + block.size() + " edge(s)"));
                }
            } else if (v != parent.get(u) && disc.get(v) < disc.get(u)) {
                low.put(u, Math.min(low.get(u), disc.get(v)));
                edgeStack.push(new int[]{u, v});
                steps.add(snap("Back edge (" + u + "—" + v + "): low[" + u + "]=" + low.get(u)));
            }
        }
        colors.put(u, "black");
    }

    private ExecutionState snap(String desc) {
        Map<Integer, String> labels = new LinkedHashMap<>();
        for (int v : graph.getVertices()) {
            int d = disc.getOrDefault(v, -1), l = low.getOrDefault(v, -1);
            labels.put(v, d == -1 ? "" : d + "/" + l);
        }
        return ExecutionState.builder()
                .step(stepNum++).description(desc)
                .vertexColors(new LinkedHashMap<>(colors)).vertexLabels(labels)
                .treeEdges(new ArrayList<>(treeEdges)).highlightedEdges(List.of())
                .extra(Map.of())
                .build();
    }
}

