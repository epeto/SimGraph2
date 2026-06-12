package com.simgraph.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.data.util.Pair;

import com.simgraph.dto.AlgorithmResponse;
import com.simgraph.model.BinomialQueue;
import com.simgraph.model.ColorEnum;
import com.simgraph.model.ExecutionState;
import com.simgraph.model.GraphAdjacency;
import com.simgraph.model.Vertex;

public class Dijkstra {
    private static final int INF = Integer.MAX_VALUE / 2;

    //Suma sin overflow
    public static int sumaOF(int a, int b){
        if(a >= INF || b >= INF){
            return INF;
        }else{
            return a+b;
        }
    }

    // Un estado será una tupla de 6 valores:
    // [0] El id del vértice.
    // [1] El atributo d del vértice.
    // [2] La fila del nodo
    // [3] La columna del nodo
    // [4] La fila de su padre (o -1 si no tiene padre)
    // [5] La columna de su padre (o -1 si no tiene padre)
    private void bqStateAux(BinomialQueue.BinNode<Vertex> binomialNode, LinkedList<Integer[]> treeState){
        Integer[] nodeState = new Integer[6];

        nodeState[0] = binomialNode.element.id;
        nodeState[1] = binomialNode.element.d;
        nodeState[2] = binomialNode.fila;
        nodeState[3] = binomialNode.columna;

        if(binomialNode.parent != null){
            nodeState[4] = binomialNode.parent.fila;
            nodeState[5] = binomialNode.parent.columna;
        } else {
            nodeState[4] = -1;
            nodeState[5] = -1;
        }
        treeState.add(nodeState);

        if(binomialNode.leftChild != null){
            bqStateAux(binomialNode.leftChild, treeState);
        }

        if(binomialNode.nextSibling != null){
            bqStateAux(binomialNode.nextSibling, treeState);
        }
    }

    /**
     * Devuelve el estado de una cola binomial para que sea legible
     * en formato JSON.
     */
    private LinkedList<LinkedList<Integer[]>> bqState(BinomialQueue<Vertex> binomialQueue){
        LinkedList<LinkedList<Integer[]>> estado = new LinkedList<>();

        for(BinomialQueue.BinNode<Vertex> node : binomialQueue.theTrees){
            if(node != null){
                LinkedList<Integer[]> treeState = new LinkedList<>();

                // El primer elemento del estado del árbol será, convenientemente, el ancho y alto del árbol binomial.
                int height = node.rango+1;
                int width;
                if(node.rango == 0){
                    width = 1;
                }else{
                    width = 1 << (node.rango-1); // 2^(rango-1)
                }

                Integer[] dimensions = new Integer[]{height, width};
                treeState.add(dimensions);

                node.posiciones(); // Para calcular la fila y columna de cada nodo.
                bqStateAux(node, treeState);
                estado.add(treeState);
            }
        }
        return estado;
    }

    // Toma de snapshot
    private ExecutionState snap(int step,
                                String desc,
                                GraphAdjacency graph,
                                BinomialQueue<Vertex> bq,
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
            Map.of("binomialQueue", bqState(bq))
        );
    }

    public AlgorithmResponse execute(GraphAdjacency graph, int source) {

        List<ExecutionState> steps = new ArrayList<>();
        int stepNum = 0;

        BinomialQueue<Vertex> bq = new BinomialQueue<>();

        for(Vertex v : graph.vertices.values()){
            v.d = INF;
            v.p = null;
            v.estado = ColorEnum.WHITE;
        }

        Vertex s = graph.vertices.get(source);
        s.d = 0;
        s.estado = ColorEnum.RED; // Solo la fuente se marca de rojo

        // Se agregan todos los vértices a la cola binomial
        for(Vertex v : graph.vertices.values()){
            bq.insert(v);
        }

        steps.add(snap(stepNum++, "Inicializando Dijkstra desde " + s.toString(), graph, bq, null, null));

        while(!bq.isEmpty()){
            Vertex u = bq.deleteMin();

            for(Pair<Vertex, Integer> par : u.adj){
                Vertex v = par.getFirst();
                int distUV = par.getSecond(); // distancia de u a v
                if(v.d > sumaOF(u.d, distUV)){
                    v.d = sumaOF(u.d, distUV);
                    v.p = u;
                    bq.decreaseKey(v);
                    v.estado = ColorEnum.GREEN; // Para que se vea cuando cambia de distancia
                    steps.add(snap(stepNum++, "Relajando arista ("+u.toString()+", "+v.toString()+")", graph, bq, u, v));

                    // Luego se pinta de gris (señal de que ya se descubrió ese vértice)
                    if(v.estado != ColorEnum.RED && v.estado != ColorEnum.BLACK){
                        v.estado = ColorEnum.GRAY;
                    }
                }
            }

            if(u.estado != ColorEnum.RED){
                u.estado = ColorEnum.BLACK;
            }

            steps.add(snap(stepNum++, u.toString()+" completado", graph, bq, u, null));
        }

        return new AlgorithmResponse("Dijkstra", steps, true, "Dijkstra completado desde " + s.toString());
    }
}
