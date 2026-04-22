// Breadth-First Search

package com.simgraph.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.springframework.data.util.Pair;

import com.simgraph.dto.AlgorithmResponse;
import com.simgraph.model.ColorEnum;
import com.simgraph.model.ExecutionState;
import com.simgraph.model.GraphAdjacency;
import com.simgraph.model.Vertex;

public class BFS {

    public AlgorithmResponse execute(GraphAdjacency graph, int source) {

        List<ExecutionState> steps = new ArrayList<>();
        int stepNum = 0;

        for(Vertex v : graph.vertices){
            v.d = Integer.MAX_VALUE/2;
        }

        Queue<Vertex> cola = new LinkedList<>();
        Vertex s = graph.getVertex(source);
        s.d = 0;
        s.estado = ColorEnum.RED; // Solo la fuente se marca de rojo
        cola.offer(s);

        steps.add(snap(stepNum++, "Inicializando algoritmo con "+s.toString()+" como fuente", graph, cola, s, null));

        while(!cola.isEmpty()){
            Vertex u = cola.poll();
            for(Pair<Vertex, Integer> pair : u.adj){
                Vertex v = pair.getFirst();
                if(v.estado == ColorEnum.WHITE){ // blanco es que apenas se va a descubrir
                    v.estado = ColorEnum.GRAY; // gris es que se ha descubierto pero no se han explorado todos sus vecinos
                    v.d = u.d+1;
                    v.p = u;
                    cola.offer(v);

                    steps.add(snap(stepNum++, "Descubriendo "+v.toString()+" a través de "+u.toString(), graph, cola, u, v));
                }
            }
            u.estado = ColorEnum.BLACK; // negro es cuando ya se visitaron todos sus vecinos
            steps.add(snap(stepNum++, u.toString()+" completado", graph, cola, u, null));
        }

        return new AlgorithmResponse("BFS", steps, true, "BFS completado desde " + s.toString());
    }

    // Toma de snapshot del estado del algoritmo
    private ExecutionState snap(int step, String desc, GraphAdjacency graph, Queue<Vertex> queue, Vertex source, Vertex target) {
        // Etiquetas
        Map<Integer, String> labels = new HashMap<>();
        for(Vertex v : graph.vertices){
            String label = (v.d >= Integer.MAX_VALUE/2) ? "∞" : String.valueOf(v.d);
            labels.put(v.id, label);
        }

        // Colores
        Map<Integer, String> colors = new HashMap<>();
        for(Vertex v : graph.vertices){
            colors.put(v.id, v.estado.name().toLowerCase());
        }

        // Aristas del árbol BFS
        List<int[]> treeEdges = new ArrayList<>();
        for(Vertex v : graph.vertices){
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
        List<Integer> cola = queue.stream().map(v -> v.id).toList();

        return new ExecutionState(step,
            desc,
            colors,
            labels,
            treeEdges,
            highlighted,
            Map.of("queue", cola));
    }
}

