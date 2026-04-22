
package com.simgraph.model;

public abstract class AbstractGraph {
    protected boolean directed; // si es dirigida o no
    protected int tamano; // número de aristas

    public abstract int getOrden(); // número de vértices
}
