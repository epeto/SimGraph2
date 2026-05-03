
// Representación de gráfica por listas de adyacencias
// En este caso, se reemplaza la lista de adyacencias por un map, para
// poder utilizar cualquier id, sin necesidad de indexar de 0 a n-1.

package com.simgraph.model;
import java.util.TreeMap;

import com.simgraph.dto.AlgorithmRequest;
import com.simgraph.dto.EdgeDto;

public class GraphAdjacency extends AbstractGraph{

    public TreeMap<Integer, Vertex> vertices; // mapa de vértices

    public GraphAdjacency(AlgorithmRequest request){
        this.directed = request.isDirected();
        vertices = new TreeMap<>();

        // Se agregan los vértices
        for (var v : request.getVertices()) {
            vertices.put(v.getId(), new Vertex(v.getId()));
        }

        // Se agregan las aristas
        for (EdgeDto e : request.getEdges()) {
            Vertex source = vertices.get(e.getSource());
            Vertex target = vertices.get(e.getTarget());

            source.agregaVecino(target, e.getWeight());
            if (!directed) {
                target.agregaVecino(source, e.getWeight());
            }
        }
    }

    @Override
    public int getOrden() {
        return vertices.size();
    }

    public Vertex getVertex(int id) {
        return vertices.get(id);
    }
}
