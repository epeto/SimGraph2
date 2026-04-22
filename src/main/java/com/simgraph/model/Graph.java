// Esta clase va a desaparecer cuando termine todos los algoritmos.

package com.simgraph.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.simgraph.dto.AlgorithmRequest;
import com.simgraph.dto.EdgeDto;
import com.simgraph.dto.VertexDto;

public class Graph {

    private final List<Integer> vertices;
    private final List<int[]> edges; // {source, target, weight}
    private final boolean directed;
    private final Map<Integer, List<int[]>> adj; // vertex → [{neighbor, weight}]

    public Graph(AlgorithmRequest request) {
        this.directed = request.isDirected();
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.adj = new LinkedHashMap<>();

        List<VertexDto> sortedVertices = request.getVertices();
        sortedVertices.sort(null); // Solo para asegurar que la gráfica indexa a partir del 0
        for (var v : sortedVertices) {
            vertices.add(v.getId());
            adj.put(v.getId(), new ArrayList<>());
        }

        for (EdgeDto e : request.getEdges()) {
            addEdge(e.getSource(), e.getTarget(), e.getWeight());
        }
    }

    private void addEdge(int u, int v, int w) {
        edges.add(new int[]{u, v, w});
        adj.get(u).add(new int[]{v, w});
        if (!directed) {
            adj.get(v).add(new int[]{u, w});
        }
    }

    public List<Integer> getVertices() { return vertices; }
    public List<int[]> getEdges() { return edges; }
    public boolean isDirected() { return directed; }
    public int getOrder() { return vertices.size(); }

    public List<int[]> getNeighbors(int v) {
        return adj.getOrDefault(v, Collections.emptyList());
    }

    /** Returns the transposed (reversed) graph. Only meaningful for directed graphs. */
    public Graph transpose() {
        AlgorithmRequest req = new AlgorithmRequest();
        req.setDirected(true);
        List<com.simgraph.dto.VertexDto> vDtos = new ArrayList<>();
        for (int v : vertices) {
            var vd = new com.simgraph.dto.VertexDto();
            vd.setId(v);
            vDtos.add(vd);
        }
        req.setVertices(vDtos);
        List<EdgeDto> eDtos = new ArrayList<>();
        for (int[] e : edges) {
            var ed = new EdgeDto();
            ed.setSource(e[1]);
            ed.setTarget(e[0]);
            ed.setWeight(e[2]);
            eDtos.add(ed);
        }
        req.setEdges(eDtos);
        return new Graph(req);
    }
}

