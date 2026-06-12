package com.simgraph.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class BinomialQueue<T extends Comparable<? super T>> {

    // Clase interna para representar un nodo de la cola binomial
    public static class BinNode<T>
    {
        public T          element;     // The data in the node
        public BinNode<T> parent;      // Padre del nodo.
        public BinNode<T> leftChild;   // Primer hijo.
        public BinNode<T> nextSibling; // Hermano derecho.
        public int rango;
        public int fila;
        public int columna;

        // Constructors
        BinNode( T theElement )
        {
            this( theElement, null, null, null, 0);
        }

        BinNode( T theElement, BinNode<T> p, BinNode<T> lt, BinNode<T> nt, int r)
        {
            element     = theElement;
            parent      = p;
            leftChild   = lt;
            nextSibling = nt;
            rango = r;
        }

        /**
         * Coloca al nodo en la posición que le corresponde en una cuadrícula.
         * @param nodo primer hijo de algún nodo.
         * @param filaPadre fila del padre de nodo.
         * @param columnaPadre columna del padre de nodo.
         */
        private void colocaHijo(BinNode<T> nodo, int filaPadre, int columnaPadre, int rangoPadre){
            int miFila = filaPadre+1;
            int miColumna;
            int desplazaIzq = 0; //Cuántas posiciones va a estar a la izquierda de su padre.
            if(rangoPadre >= 2){
                desplazaIzq = 1 << (rangoPadre-2);
            }

            miColumna = columnaPadre - desplazaIzq;
            nodo.fila = miFila;
            nodo.columna = miColumna;

            if(nodo.nextSibling != null){
                colocaHermano(nodo.nextSibling, miFila, miColumna, columnaPadre);
            }

            if(nodo.leftChild!= null){
                colocaHijo(nodo.leftChild, miFila, miColumna, nodo.rango);
            }
        }

        /**
         * Coloca al hermano de un nodo en la posición correcta en una cuadrícula.
         */
        private void colocaHermano(BinNode<T> nodo, int filaHI, int columnaHI, int columnaPadre){
            int miFila = filaHI;
            int miColumna;
            if(nodo.nextSibling == null){
                miColumna = columnaPadre;
            }else{
                miColumna = (columnaHI + columnaPadre)/2;
            }

            nodo.fila = miFila;
            nodo.columna = miColumna;

            if(nodo.nextSibling != null){
                colocaHermano(nodo.nextSibling, miFila, miColumna, columnaPadre);
            }

            if(nodo.leftChild != null){
                colocaHijo(nodo.leftChild, miFila, miColumna, nodo.rango);
            }
        }

        /**
         * Calcula la columna y fila de cada nodo en un árbol binomial
         * suponiendo que se coloca en una cuadrícula.
         */
        public void posiciones(){
            int nc; //Número de columnas.
            if(rango == 0){
                nc = 1;
            }else{
                nc = 1 << (rango-1);
            }

            fila = 0;
            columna = nc-1;

            if(leftChild != null){
                colocaHijo(leftChild, fila, columna, rango);
            }
        }
    } // BinNode

    // A partir de aquí se definen los atributos y métodos de la clase BinomialQueue2

    public int currentSize; // # items in priority queue
    public ArrayList<BinNode<T>> theTrees; // arreglo de árboles binomiales
    private HashMap<T, BinNode<T>> tabla; //Tabla hash que guarda la posición de cada elemento de tipo T.

    /**
     * Make the priority queue logically empty.
     */
    public void makeEmpty( )
    {
        currentSize = 0;
        tabla.clear();
        theTrees.clear();
    }

    /**
     * Construct an empty binomial queue.
     */
    public BinomialQueue()
    {
        theTrees = new ArrayList<>();
        tabla = new HashMap<>();
        currentSize = 0;
    }

    /**
     * Construct with a single item.
     */
    public BinomialQueue( T item ) 
    {
        currentSize = 1;
        BinNode<T> nuevo = new BinNode<>(item);
        tabla = new HashMap<>();
        tabla.put(item,nuevo);
        theTrees.add(nuevo);
    }

    /**
     * Return the result of merging equal-sized t1 and t2.
     */
    private BinNode<T> combineTrees( BinNode<T> t1, BinNode<T> t2 )
    {
        if( t1.element.compareTo( t2.element ) > 0 )
            return combineTrees( t2, t1 );
        t2.parent = t1; // El padre de t2 es t1.
        t2.nextSibling = t1.leftChild; // El hermano de t2 es el primer hijo de t1.
        t1.leftChild = t2; // El primer hijo de t1 es t2.
        t1.rango++; //Se incrementa el rango del nodo raíz.
        return t1;
    }

    /*
    * Devuelve cuántos nodos puede contener el ArrayList de árboles binomiales.
    */
    private int getCapacity()
    {
        return ( 1 << theTrees.size() ) - 1;
    }

    /**
     * Merge right-hand side (rhs) into this priority queue.
     * rhs becomes empty. rhs must be different from this.
     * @param rhs the other binomial queue.
     */
    public void merge( BinomialQueue<T> rhs )
    {
        if( this == rhs )    // Avoid aliasing problems
            return;

        currentSize += rhs.currentSize;
        tabla.putAll(rhs.tabla); // Copio los valores de la tabla de rhs a esta tabla.

        if(currentSize > getCapacity())
        {
            int oldNumTrees = theTrees.size( );
            int newNumTrees = Math.max( oldNumTrees, rhs.theTrees.size( ) ) + 1;
            while( oldNumTrees < newNumTrees )
            {
                theTrees.add( null );
                oldNumTrees++;
            }
        }

        BinNode<T> carry = null;
        for( int i = 0, j = 1; j <= currentSize; i++, j *= 2 )
        {
            BinNode<T> t1 = theTrees.get(i);
            BinNode<T> t2 = i < rhs.theTrees.size() ? rhs.theTrees.get(i) : null;

            int whichCase = t1 == null ? 0 : 1;
            whichCase += t2 == null ? 0 : 2;
            whichCase += carry == null ? 0 : 4;

            switch( whichCase )
            {
              case 0: /* No trees */
              case 1: /* Only this */
                break;
              case 2: /* Only rhs */
                theTrees.set(i, t2);
                rhs.theTrees.set(i, null);
                break;
              case 3: /* this and rhs */
                carry = combineTrees( t1, t2 );
                theTrees.set(i, null);
                rhs.theTrees.set(i, null);
                break;
              case 4: /* Only carry */
                theTrees.set(i, carry);
                carry = null;
                break;
              case 5: /* this and carry */
                carry = combineTrees( t1, carry );
                theTrees.set(i, null);
                break;
              case 6: /* rhs and carry */
                carry = combineTrees( t2, carry );
                rhs.theTrees.set(i, null);
                break;
              case 7: /* All three */
                theTrees.set(i, carry);
                carry = combineTrees( t1, t2 );
                rhs.theTrees.set(i, null);
                break;
            }
        }

        rhs.makeEmpty();
    }

    /**
     * Inserta un elemento en esta cola, manteniendo la propiedad heap-order.
     * @param x the item to insert.
     */
    public void insert( T x )
    {
        currentSize++;
        if(currentSize > getCapacity())
            theTrees.add(null);

        BinNode<T> t2 = new BinNode<>(x);
        tabla.put(x, t2);
        BinNode<T> carry = null;

        if(theTrees.get(0) == null){
            theTrees.set(0, t2);
            return;
        }else{
            carry = combineTrees(theTrees.get(0), t2);
            theTrees.set(0, null);
        }

        for( int i = 1; i<theTrees.size(); i++)
        {
            BinNode<T> t1 = theTrees.get( i );

            if(t1 == null){
                theTrees.set(i, carry);
                return;
            }else{
                carry = combineTrees(t1, carry);
                theTrees.set(i, null);
            }
        }
    }

        /**
     * Find index of tree containing the smallest item in the priority queue.
     * The priority queue must not be empty.
     * @return the index of tree containing the smallest item.
     */
    private int findMinIndex( )
    {
        int ci = 0;
        int minIndex;

        // Se encuentra al primer elemento no nulo
        while( ci < theTrees.size( ) && theTrees.get( ci ) == null )
            ci++;

        for( minIndex = ci; ci < theTrees.size(); ci++ )
            if( theTrees.get( ci ) != null &&
                theTrees.get( ci ).element.compareTo( theTrees.get( minIndex ).element ) < 0 )
                minIndex = ci;

        return minIndex;
    }

    /**
     * Test if the priority queue is logically empty.
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty( )
    {
        return currentSize == 0;
    }

    /**
     * Find the smallest item in the priority queue.
     * @return the smallest item, or throws exception if empty.
     */
    public T findMin( )
    {
        if( isEmpty() )
            throw new NoSuchElementException("Cola vacía");

        return theTrees.get( findMinIndex() ).element;
    }

    /**
     * Realiza un filtrado hacia arriba a partir del nodo que recibe.
     * @param nodo nodo a partir del cual se realiza filtrado hacia arriba.
     */
    private void percolateUp(BinNode<T> nodo){
        if(nodo.parent != null && nodo.element.compareTo(nodo.parent.element) < 0){
            T t = nodo.element;
            nodo.element = nodo.parent.element;
            nodo.parent.element = t;

            tabla.replace(nodo.element, nodo);
            tabla.replace(nodo.parent.element, nodo.parent);

            percolateUp(nodo.parent);
        }
    }

    /**
     * Se realiza un reacomodo de un elemento después de decrementar su llave.
     * @param elem elemento que decrementó su llave y se va a reacomodar.
     */
    public void decreaseKey(T elem){
        BinNode<T> nd = tabla.get(elem);
        if(nd != null)
            percolateUp(nd);
    }

    /**
     * Remove the smallest item from the priority queue.
     * @return the smallest item, or throws Exception if empty.
     */
    public T deleteMin( )
    {
        if( isEmpty( ) )
            throw new NoSuchElementException("Cola vacía.");

        int minIndex = findMinIndex();
        T minItem = theTrees.get(minIndex).element;
        tabla.remove(minItem);
        BinNode<T> deletedTree = theTrees.get(minIndex).leftChild;

        // Construct H''
        BinomialQueue<T> deletedQueue = new BinomialQueue<>( );
        for(int i=0; i<minIndex; i++){
            deletedQueue.theTrees.add(null);
        }
        
        deletedQueue.currentSize = ( 1 << minIndex ) - 1;
        for( int j = minIndex - 1; j >= 0; j-- )
        {
            deletedQueue.theTrees.set(j, deletedTree);
            deletedTree = deletedTree.nextSibling;
            deletedQueue.theTrees.get(j).nextSibling = null;
            deletedQueue.theTrees.get(j).parent = null;
        }

        // Construct H'
        theTrees.set(minIndex, null);
        currentSize -= deletedQueue.currentSize + 1;

        merge( deletedQueue );
        
        return minItem;
    }
}
