package mmlib4j.datastruct;

import java.util.ArrayList;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PriorityQueueToS  {
 	
	protected int numElements = 0;
    protected ArrayList<Integer> buckets[] = null;
    protected int currentPriority;
    
    public PriorityQueueToS( ) {
		this.buckets = new ArrayList[256];
		for(int i=0; i < buckets.length; i++){
			this.buckets[i] = new ArrayList<Integer>();
		}	
    }
    
	public void initial(int element, int priority) {
		numElements++;
		this.currentPriority = priority;
		buckets[priority].add(element);
	        
	}
	
	public int getCurrentPriority(){
		return currentPriority;
	}
	
	public boolean isEmpty(){
		return numElements == 0;
	}
	
	/**
	 * adiciona um elemento
	 * @param element
	 * @param lower
	 * @param upper
	 */
	public void priorityPush(int element, int lower, int upper){
		int priority;
		if(lower > currentPriority){
			priority = lower; 
		} else if(upper < currentPriority){
			priority = upper;
		}else{
			priority = currentPriority;
		}
		numElements++;
		buckets[priority].add(element);
	}
	
	/**
	 * Remove um elemento. A prioridade corrente da queue pode mudar: 
	 * Primeiramente, aumenta a prioridade, senao conseguir tenta diminuir
	 */
	public int priorityPop(){
		if (buckets[currentPriority].isEmpty()) {
			int i = currentPriority;
			int j = currentPriority;
			while(true){
				if (i < 256 && buckets[i].isEmpty()) { //aumenta
					i++;
				}
				if(i < 256 && !buckets[i].isEmpty()){ //se existe o proximo (aumentando)
					currentPriority = i;
					break;
				}
				
				if(j > 0 && buckets[j].isEmpty()){ //diminui
					j--;
				}
				if(!buckets[j].isEmpty()){ //se existe o proximo (diminuindo)
					currentPriority = j;
					break;
				}
				
			}
		}
		numElements--;
		return buckets[currentPriority].remove(buckets[currentPriority].size()-1);
	}
	
	
   
    
    
    
 }
