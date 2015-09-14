package mmlib4j.datastruct;

import java.util.ArrayList;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PriorityQueue<T> {

	protected int numElements = 0;

    protected int priorityLowerBound = 0;
    
    protected ArrayList<Bucket<T>> buckets = null;
    
    protected int maxPriority;
    
    protected int policy = 0; //politica dos buckets --- fifo = 0 ou lifo = 1
    
    public PriorityQueue(int maxPriority) {
    	this(maxPriority, 0);
    }
    
    public PriorityQueue(int maxPriority, int policy) {
        this.maxPriority = maxPriority;
        this.policy = policy;
        buckets = new ArrayList<Bucket<T>>(maxPriority + 1);
        for (int i = 0; i <= maxPriority; i++) {
        	buckets.add( new Bucket<T>() );
        }
    }


    public T remove() {
        for (int i = priorityLowerBound; i <= maxPriority; i++) {
            if (!buckets.get(i).isEmpty()) {
                numElements--;
                priorityLowerBound = i;
                return buckets.get(i).remove();
            }
        }
        return null;
    }
    
    public T removeMax() {
        for (int i = maxPriority; i >= priorityLowerBound; i--) {
            if (!buckets.get(i).isEmpty()) {
                numElements--;
                maxPriority = i;
                return buckets.get(i).remove();
            }
        }
        return null;
    }

    public boolean remove(T element) {
    	for (int i = priorityLowerBound; i <= maxPriority; i++) {
        	if(buckets.get(i).remove(element)){
        		numElements--;
        		return true;
        	}
    	}
    	return false;
    }

    public boolean remove(T element, int priority) {
    	if(priority <= maxPriority && buckets.get(priority).remove(element)){
    		numElements--;
    		return true;
    	}
    	return false;
    }

    
        
    public void add(T element, int priority) {
        if (priority < priorityLowerBound) {
            priorityLowerBound = priority;
        }
        numElements++;
        if(priority <= maxPriority)
        	buckets.get(priority).add(element);
        else{ //fazer os bockets crescer
        	for (int i = maxPriority; i <= priority; i++) {
        		buckets.add( new Bucket<T>() );
        	}
        	this.maxPriority = priority;
        	buckets.get(priority).add(element);
        }
    }

    public boolean isEmpty() {
        return (numElements == 0);
    }

    public int size() {
        return numElements;
    }
    
    protected class Bucket<T>{
    	
    	protected Queue<T> fifo = null;
    	protected Stack<T> lifo = null;
    	
    	public Bucket(){
    		if(policy == 0){
    			fifo = new Queue<T>();
    		}else{
    			lifo = new Stack<T>();
    		}
    	}
    	
    	public void add(T element){
    		if(policy == 0){
    			fifo.enqueue(element);
    		}else{
    			lifo.push(element);
    		}
    	}
    	public T remove(){
    		if(policy == 0){
    			return fifo.dequeue();
    		}else{
    			return lifo.pop();
    		}
    	}
    	
    	public boolean remove(T element){
    		if(policy == 0){
    			return fifo.list.remove(element);
    		}else{
    			return lifo.list.remove(element);
    		}
    	}
    	
    	
    	public boolean isEmpty(){
    		if(policy == 0){
    			return fifo.isEmpty();
    		}else{
    			return lifo.isEmpty();
    		}
    	}
    	
    }

}
