/**
 * Representación de una gráfica por matrices
 * Se establece la precondición de que la gráfica es inmutable.
 */

package com.simgraph.model;
import java.util.TreeMap;

import com.simgraph.dto.AlgorithmRequest;
import com.simgraph.dto.EdgeDto;

public class GraphMatrix extends AbstractGraph {

    public int[][] matriz; // matriz de adyacencias
    public TreeMap<Integer, Integer> vertMat; // Mapea id's de vértices a índices de la matriz

    public GraphMatrix(AlgorithmRequest request){
        tamano = request.getEdges().size();
        this.directed = request.isDirected();
        vertMat = new TreeMap<>();

        // Se agregan los vértices
        int index = 0;
        for (var v : request.getVertices()) {
            vertMat.put(v.getId(), index);
            index++;
        }

        // Se inicializa la matriz de adyacencias
        matriz = new int[vertMat.size()][vertMat.size()];
        for (int i = 0; i < vertMat.size(); i++) {
            for (int j = 0; j < vertMat.size(); j++) {
                matriz[i][j] = 0; // No hay arista
            }
        }

        // Se agregan las aristas
        for (EdgeDto e : request.getEdges()) {
            int sourceIndex = vertMat.get(e.getSource());
            int targetIndex = vertMat.get(e.getTarget());

            matriz[sourceIndex][targetIndex] = e.getWeight();

            if (!directed) {
                matriz[targetIndex][sourceIndex] = e.getWeight();
            }
        }
    }

    /**
     * Obtiene el peso de la arista entre dos vértices dados por sus IDs.
     * @param sourceId ID del vértice de origen
     * @param targetId ID del vértice de destino
     * @return peso de la arista
     */
    public int getWeight(int sourceId, int targetId) {
        Integer sourceIndex = vertMat.get(sourceId);
        Integer targetIndex = vertMat.get(targetId);

        if (sourceIndex == null || targetIndex == null) {
            throw new IllegalArgumentException("No se encontró el vértice indicado.");
        }

        return matriz[sourceIndex][targetIndex];
    }

    @Override
    public int getOrden() {
        return matriz.length;
    }

    /**
     * Devuelve un arreglo que mapea el índice de la matriz al ID del vértice.
     * @return arreglo con los IDs de los vértices en orden
     */
    public int[] buildArray(){
        int[] array = new int[vertMat.size()];
        for (var entry : vertMat.entrySet()) {
            array[entry.getValue()] = entry.getKey();
        }
        return array;
    }
}
