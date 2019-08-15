package mmlib4j.datastruct;


import java.util.NoSuchElementException;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PriorityQueueHeap<T>  {
    private NodeQueue queue[];           
    private int n;                       
    private int mapIndexQueue[];
    
    private class NodeQueue{
    	double priority;
    	T element;
    	NodeQueue(T element, double priority) {
    		this.element = element;
    		this.priority = priority;
		}
    }
    
    /**
     * Initializes an empty priority queue with the given initial capacity.
     *
     * @param  initCapacity the initial capacity of this priority queue
     */
    public PriorityQueueHeap(int initCapacity) {
        queue = new PriorityQueueHeap.NodeQueue[initCapacity + 1];
        mapIndexQueue = new int[initCapacity + 1];
        n = 0;
    }

    /**
     * Returns true if this priority queue is empty.
     *
     * @return {@code true} if this priority queue is empty;
     *         {@code false} otherwise
     */
    public boolean isEmpty() {
        return n == 0;
    }

    /**
     * Returns the number of keys on this priority queue.
     *
     * @return the number of keys on this priority queue
     */
    public int size() {
        return n;
    }
    
    
    public boolean contains(T element) {
    	int index = mapIndexQueue[element.hashCode()];
    	return queue[index] != null;
    }

    public void updatePriorityElement(T element, double beforePriority, double afterPriority) {
    	int index = mapIndexQueue[element.hashCode()];
    	if(beforePriority >= afterPriority) {
    		decreaseKey(index, afterPriority);
    	}else {
    		increaseKey(index, afterPriority);
    	}
    	
    }
    
    /**
     * Returns a smallest key on this priority queue.
     *
     * @return a smallest key on this priority queue
     * @throws NoSuchElementException if this priority queue is empty
     */
    public double getPriorityMin() {
        if (isEmpty()) throw new NoSuchElementException("Priority queue underflow");
        return queue[1].priority;
    }

    /**
     * Returns the element with the smallest key on this priority queue.
     *
     * @return a smallest key on this priority queue
     * @throws NoSuchElementException if this priority queue is empty
     */
    public T getElementMin() {
        if (isEmpty()) throw new NoSuchElementException("Priority queue underflow");
        return queue[1].element;
    }
    
    // helper function to double the size of the heap array
    private void resize(int capacity) {
    	NodeQueue[] temp = new PriorityQueueHeap.NodeQueue[capacity];
        for (int i = 1; i <= n; i++) {
            temp[i] = queue[i];
        }
        queue = temp;
    }

    public void add(T element, double priority) {
    	// double size of array if necessary
        if (n == queue.length - 1) resize(2 * queue.length);
        n += 1;
        queue[n] = new NodeQueue(element, priority);
        mapIndexQueue[element.hashCode()] = n;
        swim(n);
    }
    
    /**
     * Decrease the key associated with index to the specified value.
     *
     * @param  the index of the priority to decrease
     * @param  the value of the priority to decrease
     */
    public void decreaseKey(int index, double priority) {
    	queue[index].priority = priority;
        swim(index);
    }

    /**
     * Increase the key associated with index to the specified value.
     *
     * @param  the index of the priority to increase
     * @param  the value of the priority to increase
     */
    public void increaseKey(int index, double priority) {
    	queue[index].priority = priority;
        sink(index);
    }
    
    
    /**
     * Removes and returns a smallest key on this priority queue.
     *
     * @return a smallest key on this priority queue
     * @throws NoSuchElementException if this priority queue is empty
     */
    public T removeMin() {
        if (isEmpty()) throw new NoSuchElementException("Priority queue underflow");
        T elem = queue[1].element;
        swap(1, n--);
        sink(1);
        queue[n+1] = null;         
        return elem;
    }

    private void swap(int i, int j) {
    	NodeQueue aux = queue[i];
        queue[i] = queue[j];
        queue[j] = aux;
        mapIndexQueue[queue[i].element.hashCode()] = i;
        mapIndexQueue[queue[j].element.hashCode()] = j;
    }

    public void remove(T elem) {
        if (isEmpty()) throw new NoSuchElementException("Priority queue underflow");
        int index = mapIndexQueue[elem.hashCode()];
        swap(index, n--);
        sink(index);
        swim(index);
        queue[n+1]=null;         
    }
    
   /***************************************************************************
    * Helper functions to restore the heap invariant.
    ***************************************************************************/

    private void swim(int k) {
        while (k > 1 && queue[k/2].priority > queue[k].priority) {
        	swap(k, k/2);
            k = k/2;
        }
    }

    private void sink(int k) {
        while (2*k <= n) {
            int j = 2*k;
            if (j < n && queue[j].priority > queue[j+1].priority) j++;
            if (queue[k].priority <= queue[j].priority) break;
            swap(k, j);
            k = j;
        }
    }  

    /**
     * Unit tests the {@code MinPQ} data type.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
    	PriorityQueueHeap<Integer> q = new PriorityQueueHeap<Integer>(11);
    	for(int i =0; i < 10; i++) {
    		double priority = Math.random();
    		q.add(i, priority);
    		System.out.println(i+ " = " + priority);
    	}
    	q.add(11, 0.5);
    	System.out.println(" =============== ");
    	q.updatePriorityElement(11, 0.5, 0.3);
    	q.updatePriorityElement(7, 0.5, 0.3);
    	while(!q.isEmpty()) {
    		double x = q.getPriorityMin();
    		Integer elem = q.removeMin();
    		System.out.println(elem + " = " + x);
    	}
    	
    }

}
