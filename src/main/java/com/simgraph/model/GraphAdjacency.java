
// Representación de gráfica por listas de adyacencias

package com.simgraph.model;

import java.util.ArrayList;
import java.util.List;

import com.simgraph.dto.AlgorithmRequest;
import com.simgraph.dto.EdgeDto;
import com.simgraph.dto.VertexDto;

public class GraphAdjacency extends AbstractGraph{

    public List<Vertex> vertices; // lista de vértices

    public GraphAdjacency(AlgorithmRequest request){
        this.directed = request.isDirected();
        vertices = new ArrayList<>();

        // Se agregan los vértices
        List<VertexDto> sortedVertices = request.getVertices();
        sortedVertices.sort(null); // Solo para asegurar que la gráfica indexa a partir del 0
        for (var v : sortedVertices) {
            vertices.add(new Vertex(v.getId()));
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
