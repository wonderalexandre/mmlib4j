package mmlib4j.datastruct;

import java.util.LinkedList;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class Queue<E> {

    /** 
     * listas encadeadas
     */
    protected LinkedList<E> list;

    /**
     * construtor da classe
     */
    public Queue() {
        list = new LinkedList<E>();
    }

    /**
     * metodo que pega o object do inicio da fila
     * 
     * @return Object
     */
    public E getHead() {
        return list.getFirst();
    }

    /**
     * metodo que coloca um object no final da fila
     * 
     * @param object
     */
    public void enqueue(E object) {
        list.addLast(object);
    }

    /**
     * metodo que retira um object da fila
     * 
     * @return Object
     */
    public E dequeue() {
        return list.removeFirst();
    }


    /**
     * metodo que retorna a quantidade de elementos de um container
     * @return <int> retorna a quantidade de elementos de um container
     */
    public int size(){
        return list.size();
    }


    /**
     * metodo que retorna se um container esta vazio
     * @return true para container vazio e false se n�o
     */
    public boolean isEmpty(){
        return list.isEmpty();
    }

    /**
     * metodo que esvazia o container
     */
    public void clear(){
        list.clear();
    }
}
