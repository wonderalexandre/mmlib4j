package mmlib4j.datastruct;

import java.util.LinkedList;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class Stack<E> {
    /**
     * lista encadeadas
     */
    protected LinkedList<E> list;

    /**
     * construtor da classe
     */
    public Stack() {
        list = new LinkedList<E>();
    }

    /**
     * metodo que esvazia a pilha
     */
    public void clear() {
        list.clear();
    }

    /**
     * metodo que insere um objeto na pilha
     * 
     * @param object
     */
    public void push(E object) {
        list.addLast(object);
    }

    /**
     * retorna o objeto retirado da pilha
     * 
     * @return
     */
    public E pop() {
        return list.removeLast();
    }

    /**
     * metodo que retorna o objeto do topo da pilha
     * @return objeto do topo da pilha
     */
    public E getTop() {
        return list.getLast();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }

}
