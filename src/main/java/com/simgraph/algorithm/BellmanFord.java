package com.simgraph.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.util.Pair;

import com.simgraph.dto.AlgorithmResponse;
import com.simgraph.model.ColorEnum;
import com.simgraph.model.ExecutionState;
import com.simgraph.model.GraphAdjacency;
import com.simgraph.model.Vertex;

public class BellmanFord {

    private static final int INF = Integer.MAX_VALUE / 2;

    //Suma sin overflow
    public static int sumaOF(int a, int b){
        if(a >= INF || b >= INF){
            return INF;
        }else{
            return a+b;
        }
    }

    // Toma de snapshot.
    private ExecutionState snap(int step,
                                String desc,
                                GraphAdjacency graph,
                                Vertex source,
                                Vertex target) {
        // Etiquetas
        Map<Integer, String> labels = new HashMap<>();
        for(Vertex v : graph.vertices.values()){
            String label = (v.d >= Integer.MAX_VALUE/2) ? "∞" : String.valueOf(v.d);
            labels.put(v.id, label);
        }

        // Colores
        Map<Integer, String> colors = new HashMap<>();
        for(Vertex v : graph.vertices.values()){
            colors.put(v.id, v.estado.name().toLowerCase());
        }

        // Aristas del árbol de distancias mínimas
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

        return new ExecutionState(step,
            desc,
            colors,
            labels,
            treeEdges,
            highlighted,
            Map.of()
        );
    }

    // Algoritmo principal
    public AlgorithmResponse execute(GraphAdjacency graph, int source) {
        
        List<ExecutionState> steps = new ArrayList<>();
        int stepNum = 0;

        for(Vertex v : graph.vertices.values()){
            v.d = INF;
            v.p = null;
            v.estado = ColorEnum.WHITE;
        }

        Vertex s = graph.vertices.get(source);
        s.d = 0;
        s.estado = ColorEnum.RED; // Solo la fuente se marca de rojo

        steps.add(snap(stepNum++, "Inicializando Bellman-Ford desde " + s.toString(), graph, null, null));

        for(int i=1; i<graph.vertices.size(); i++){
            for(Vertex u : graph.vertices.values()){
                for(Pair<Vertex,Integer> par : u.adj){
                    Vertex v = par.getFirst();
                    if(v.d > sumaOF(u.d, par.getSecond())){
                        v.estado = ColorEnum.GREEN; // Para que se vea cuando cambia de distancia
                        steps.add(snap(stepNum++, "Relajando arista ("+u.toString()+", "+v.toString()+")", graph, u, v));
                        v.d = sumaOF(u.d, par.getSecond());
                        v.p = u;

                        // Luego se pinta de gris (señal de que ya se descubrió ese vértice)
                        if(v.estado != ColorEnum.RED && v.estado != ColorEnum.BLACK){
                            v.estado = ColorEnum.GRAY;
                        }
                    }
                }

                // Cuando se ha acabado con u, se pinta de negro.
                if(u.estado != ColorEnum.RED){
                    u.estado = ColorEnum.BLACK;
                }
            }
        }

        steps.add(snap(stepNum++, "Bellman-Ford completado", graph, null, null));

        // Segunda parte: comprobación de ciclos de longitud negativa
        for(Vertex u : graph.vertices.values()){
            for(Pair<Vertex,Integer> par : u.adj){
                Vertex v = par.getFirst();
                if(v.d > sumaOF(u.d, par.getSecond())){
                    System.out.println("La gráfica tiene un ciclo de longitud negativa.");
                    return new AlgorithmResponse("Bellman-Ford", List.of(), false, "La gráfica tiene un ciclo de longitud negativa.");
                }
            }
        }

        return new AlgorithmResponse("Bellman-Ford", steps, true, "Bellman-Ford completado desde " + s.toString());
    }

}

