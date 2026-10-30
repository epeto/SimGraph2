package com.simgraph.algorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.simgraph.dto.AlgorithmResponse;
import com.simgraph.model.ExecutionState;
import com.simgraph.model.GraphMatrix;


// TODO: modificar el script de javascript para desplegar la matriz

public class FloydWarshall{

    private static final int INF = Integer.MAX_VALUE / 2;

    public static int[][] clonaMatriz(int[][] original) {
        int n = original.length;
        int[][] copia = new int[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(original[i], 0, copia[i], 0, n);
        }
        return copia;
    }

    //Suma sin overflow
    public static int sumaOF(int a, int b){
        if(a >= INF || b >= INF){
            return INF;
        }else{
            return Math.min(a+b, INF);
        }
    }

    // Algoritmo principal
    public AlgorithmResponse execute(GraphMatrix graph) {
        int n = graph.getOrden();
        int[][] dist = new int[n][n]; // Matriz de distancias
        int[][] pred = new int[n][n]; // Matriz de predecesores
        int[] vertIds = graph.buildArray(); // Arreglo de IDs de vértices en orden
        List<ExecutionState> steps = new java.util.ArrayList<>();

        int step = 0;
        // Inicialización de las matrices
        for(int i=0; i<n; i++){
            for(int j=0; j<n; j++){
                if(i == j){
                    dist[i][j] = 0;
                } else {
                    dist[i][j] = (graph.matriz[i][j] != 0) ? graph.matriz[i][j] : INF;
                }
                pred[i][j] = (dist[i][j] < INF && i != j) ? i : -1;
            }
        }

        steps.add(snap(step++, "Inicialización de matrices", dist, pred, -1, -1, vertIds));
        for(int k=0; k<n; k++){
            for(int i=0; i<n; i++){
                for(int j=0; j<n; j++){
                    int oldDist = dist[i][j];
                    int newDist = sumaOF(dist[i][k], dist[k][j]);
                    if(oldDist > newDist){
                        dist[i][j] = newDist;
                        pred[i][j] = pred[k][j];
                        String oldDistRender = (oldDist >= INF) ? "∞" : String.valueOf(oldDist);
                        String desc = "D["+String.valueOf(vertIds[i])+"]["+String.valueOf(vertIds[j])+"]: "
                        + oldDistRender + " -> " + String.valueOf(newDist);
                        steps.add(snap(step++, desc, dist, pred, i, j, vertIds));
                    }
                }
            }
        }

        steps.add(snap(step++, "Algoritmo finalizado", dist, pred, -1, -1, vertIds));

        return new AlgorithmResponse("Floyd-Warshall", steps, true, "Floyd-Warshall finalizado");
    }

    
    private ExecutionState snap(int step, String description, int[][] dist, int[][] pred, int activeI, int activeJ, int[] vertIds) {
        Map<String, Object> extras = new HashMap<>();
        extras.put("matrix", clonaMatriz(dist));
        extras.put("vertices", vertIds);
        extras.put("activeI", activeI);
        extras.put("activeJ", activeJ);

        List<int[]> pathEdges = new java.util.ArrayList<>();
        Map<Integer, String> vertexColors = new HashMap<>();

        for(int vertId : vertIds){
            vertexColors.put(vertId, "white");
        }

        if(activeI != -1 && activeJ != -1 && pred[activeI][activeJ] != -1){
            vertexColors.put(vertIds[activeI], "orange");
            HashSet<Integer> visited = new HashSet<>(); // evita repetir valores de current y evita que se cicle
            int current = activeJ;
            while(current != -1 && current != activeI && !visited.contains(current)){
                visited.add(current);

                vertexColors.put(vertIds[current], "orange");
                if(pred[activeI][current] != -1){
                    pathEdges.add(new int[]{vertIds[pred[activeI][current]], vertIds[current]});
                }
                current = pred[activeI][current];
            }
        }

        return new ExecutionState(step, description, vertexColors, Map.of(), List.of(), pathEdges, extras);
    }
}

