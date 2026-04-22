package com.simgraph.dto;

import lombok.Data;

@Data
public class VertexDto implements Comparable<VertexDto>{
    private int id;

    @Override
    public int compareTo(VertexDto v) {
        return (this.id - v.id);
    }
}

