package com.simgraph.dto;

import lombok.Data;

@Data
public class VertexDto implements Comparable<VertexDto>{
    private Integer id;

    @Override
    public int compareTo(VertexDto v) {
        return this.id.compareTo(v.id);
    }
}

