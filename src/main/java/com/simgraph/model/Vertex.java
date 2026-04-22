package com.simgraph.model;

//Clase vértice

import java.util.ArrayList;

import org.springframework.data.util.Pair;

public class Vertex implements Comparable<Vertex>{
    public ArrayList<Pair<Vertex,Integer>> adj; //Lista de adyacencias (vertice, peso).
    public int id; //Identificador.
    public int d; //distancia o llave.
    public Vertex p; //Padre (o predecesor) de este vértice.
    public ColorEnum estado; //Indica el estado del vértice (si ha sido visitado o no, etc.)

    /**
     * Constructor de la clase vértice.
     * @param ident identificador del vértice, el cual es un número del 0 al n-1.
     */
    public Vertex(int ident){
        id = ident;
        adj = new ArrayList<>();
        d = 0;
        estado = ColorEnum.WHITE;
        p = null;
    }

    /**
     * Comprueba si 2 vértices son iguales.
     */
    @Override
    public boolean equals(Object o){
        boolean ret = false;
        if(o instanceof Vertex comp){
            if(comp.id == this.id){
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Agrega un vecino a este vértice
     * @param vec nuevo vecino.
     * @param peso peso de la arista de este vértice a vec.
     */
    public void agregaVecino(Vertex vec, int peso){
        boolean existe = false;
        for(Pair<Vertex, Integer> par : adj){
            if(par.getFirst().equals(vec)){
                existe = true;
                break;
            }
        }
        if(!existe){
            Pair<Vertex, Integer> nuevoPar = Pair.of(vec, peso);
            adj.add(nuevoPar);
        }
    }

    @Override
    public String toString(){
        return "v"+String.valueOf(id);
    }

    @Override
    public int compareTo(Vertex v) {
        return (this.d - v.d);
    }

    @Override
    public int hashCode(){
        return id;
    }
}

