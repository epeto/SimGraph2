package com.simgraph.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.simgraph.algorithm.BFS;
import com.simgraph.algorithm.BellmanFord;
import com.simgraph.algorithm.Blocks;
import com.simgraph.algorithm.CutVertices;
import com.simgraph.algorithm.DFS;
import com.simgraph.algorithm.Dijkstra;
import com.simgraph.algorithm.EdgeClassification;
import com.simgraph.algorithm.FloydWarshall;
import com.simgraph.algorithm.Hierholzer;
import com.simgraph.algorithm.Kosaraju;
import com.simgraph.algorithm.Kruskal;
import com.simgraph.algorithm.Prim;
import com.simgraph.algorithm.TopologicalSort;
import com.simgraph.algorithm.TransitiveClosure;
import com.simgraph.dto.AlgorithmRequest;
import com.simgraph.dto.AlgorithmResponse;
import com.simgraph.model.AbstractGraph;
import com.simgraph.model.Graph;
import com.simgraph.model.GraphAdjacency;

@RestController
@RequestMapping("/api/algorithm")
public class AlgorithmController {

    @PostMapping("/{name}")
    public ResponseEntity<AlgorithmResponse> run(
            @PathVariable String name,
            @RequestBody AlgorithmRequest request) {

        AbstractGraph absGraph;

        switch(name.toLowerCase()){
            case "bfs":
            case "dfs":
            case "dijkstra":
            case "prim":
            case "topological_sort":
            case "cut_vertices":
            case "blocks":
            case "edge_classification":
                absGraph = new GraphAdjacency(request);
                break;
            default:
                absGraph = new GraphAdjacency(request); //TODO: cambiar después.
        }

        Graph graph = new Graph(request);
        int src = request.getSourceVertex();

        AlgorithmResponse response = switch (name.toLowerCase()) {
            case "bfs"                -> new BFS().execute((GraphAdjacency)absGraph, src);
            case "dfs"                -> new DFS().execute((GraphAdjacency)absGraph, src);
            case "dijkstra"           -> new Dijkstra().execute(graph, src);
            case "bellman_ford"       -> new BellmanFord().execute(graph, src);
            case "floyd_warshall"     -> new FloydWarshall().execute(graph);
            case "kruskal"            -> new Kruskal().execute(graph);
            case "prim"               -> new Prim().execute(graph, src);
            case "kosaraju"           -> new Kosaraju().execute(graph);
            case "topological_sort"   -> new TopologicalSort().execute(graph);
            case "hierholzer"         -> new Hierholzer().execute(graph);
            case "cut_vertices"       -> new CutVertices().execute(graph);
            case "blocks"             -> new Blocks().execute(graph);
            case "edge_classification"-> new EdgeClassification().execute(graph);
            case "transitive_closure" -> new TransitiveClosure().execute(graph);
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Unknown algorithm: " + name);
        };

        return ResponseEntity.ok(response);
    }
}


