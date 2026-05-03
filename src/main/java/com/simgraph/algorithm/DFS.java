package com.simgraph.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.springframework.data.util.Pair;

import com.simgraph.dto.AlgorithmResponse;
import com.simgraph.model.ColorEnum;
import com.simgraph.model.ExecutionState;
import com.simgraph.model.GraphAdjacency;
import com.simgraph.model.Vertex;

public class DFS{
    public AlgorithmResponse execute(GraphAdjacency graph, int source) {
        List<ExecutionState> steps = new ArrayList<>();
        int stepNum = 0;
        int dfindex = 0;
        Stack<Vertex> pila = new Stack<>();

        // Inicialización de los vértices
        for(Vertex v : graph.vertices.values()){
            v.iter = v.adj.listIterator();
        }

        // Primer vértice
        dfindex++;
        Vertex s = graph.vertices.get(source);
        s.estado = ColorEnum.RED; // Solo la fuente se marca de rojo
        s.d = dfindex;
        pila.push(s);

        steps.add(snap(stepNum++, "Inicializando algoritmo con "+s.toString()+" como fuente", graph, pila, s, null));

        while(!pila.isEmpty()){
            Vertex v = pila.peek();
            if(v.iter.hasNext()){
                Pair<Vertex, Integer> neighborPair = v.iter.next();
                Vertex neighbor = neighborPair.getFirst();
                if(neighbor.estado == ColorEnum.WHITE){
                    dfindex++;
                    neighbor.estado = ColorEnum.GRAY;
                    neighbor.d = dfindex;
                    neighbor.p = v;
                    pila.push(neighbor);
                    // Snapshot
                    steps.add(snap(stepNum++, "Descubriendo "+neighbor.toString()+" a través de "+v.toString(), graph, pila, v, neighbor));
                }
            } else {
                // Ya se visitaron todos los vecinos del vértice al tope
                if(v.estado != ColorEnum.RED){
                    v.estado = ColorEnum.BLACK;
                }
                pila.pop();
                steps.add(snap(stepNum++, v.toString()+" completado", graph, pila, v, null));
            }
        }

        return new AlgorithmResponse("DFS", steps, true, "DFS completado desde " + s.toString());
    }

    // Toma de snapshot
    private ExecutionState snap(int step,
                                String desc,
                                GraphAdjacency graph,
                                Stack<Vertex> stack,
                                Vertex source,
                                Vertex target) {
        // Etiquetas
        Map<Integer, String> labels = new HashMap<>();
        for(Vertex v : graph.vertices.values()){
            labels.put(v.id, String.valueOf(v.d));
        }

        // Colores
        Map<Integer, String> colors = new HashMap<>();
        for(Vertex v : graph.vertices.values()){
            colors.put(v.id, v.estado.name().toLowerCase());
        }

        // Aristas del árbol DFS
        List<int[]> treeEdges = new ArrayList<>();
        for(Vertex v : graph.vertices.values()){
            if(v.p != null){
                treeEdges.add(new int[]{v.p.id, v.id});
            }
        }

        // Para resaltar la arista que está siendo visitada
        List<int[]> highlighted;
        if(source != null && target != null){
            highlighted = List.of(new int[]{source.id, target.id});
        } else {
            highlighted = List.of();
        }

        //Extrae los id's de los vértices de la cola
        // TODO: hacer la interfaz en Javascript para la pila.
        List<Integer> pila = stack.stream().map(v -> v.id).toList();

        return new ExecutionState(step,
            desc,
            colors,
            labels,
            treeEdges,
            highlighted,
            Map.of("stack", pila));
    }
}
