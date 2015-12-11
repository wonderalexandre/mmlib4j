package mmlib4j.datastruct;

import java.util.ArrayList;

import mmlib4j.images.GrayScaleImage;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PriorityQueueDial  {
 	protected int numElements = 0;
    protected int priorityLowerBound = 0;
    protected boolean isDual = false;
    protected ArrayList<Bucket> buckets = null;
    protected int maxPriority;
    protected int policy = 0; //politica dos buckets --- fifo = 0 ou lifo = 1
    public final static int FIFO = 0; //politica fifo
	public final static int LIFO = 1; //politica lifo
    protected NodeDDL vetorDDL[];
    protected GrayScaleImage img;
	
    public PriorityQueueDial(GrayScaleImage img, int maxPriority) {
    	this(img, maxPriority, 0, false);
    }
    public PriorityQueueDial(GrayScaleImage img, int maxPriority, boolean isDual) {
    	this(img, maxPriority, 0, isDual);
    }
    public PriorityQueueDial(GrayScaleImage img, int maxPriority, int policy){
    	this(img, maxPriority, policy, false);
    }
    public PriorityQueueDial(GrayScaleImage img, int maxPriority, int policy, boolean isDual) {
    	vetorDDL = new NodeDDL[img.getSize()];
    	this.img = img;
    	this.maxPriority = maxPriority;
    	this.policy = policy;
    	this.isDual = isDual;
    	buckets = new ArrayList<Bucket>(maxPriority + 1);
    	for (int i = 0; i <= maxPriority; i++) {
    		buckets.add( new Bucket(policy) );
    	}
    }
    
    protected PriorityQueueDial(){}

    public boolean contains(int element){
    	return vetorDDL[element] != null;
    }
    
    public void add(Integer element) {
    	this.add(element, img.getPixel(element));
    }

    private void add(Integer element, int priority) {
    	vetorDDL[element] = new NodeDDL(element);
    	if (priority < priorityLowerBound) {
    		priorityLowerBound = priority;
    	}
    	numElements++; 

    	if(priority <= maxPriority){
    		buckets.get(priority).add(vetorDDL[element]);
    	}else{ 
    		//fazer os bockets crescer
    		for (int i = maxPriority + 1; i <= priority; i++) {
    			buckets.add( new Bucket(policy) );
    		}
    		this.maxPriority = priority;
    		buckets.get(priority).add(vetorDDL[element]);
    	}
    }
   

    public void remove(Integer element) {
    	NodeDDL node = vetorDDL[element];
    	if(node == null){
    		return; //esse node nao entrou na fila
    	}else{
    		int priority = img.getPixel(node.pixel);
    		buckets.get(priority).remove(node);
    		vetorDDL[node.pixel] = null;
    		numElements--;
    	}
    	
    	
    	
    }

    public Integer remove() {
    	if(isDual){//remove o elemento de maior prioridade
    		for (int i = maxPriority; i >= priorityLowerBound; i--) {
    			if (!buckets.get(i).isEmpty()) {
    				maxPriority = i;
    				NodeDDL node = buckets.get(i).remove();
    				numElements--;
    				vetorDDL[node.pixel] = null;
    				return node.pixel;
    			}
    		}
    	}else{ //remove o elemento de menor prioridade 	
    		for (int i = priorityLowerBound; i <= maxPriority; i++) {
    			if (!buckets.get(i).isEmpty()) {
    				priorityLowerBound = i;
    				NodeDDL node = buckets.get(i).remove();
    				numElements--;
    				vetorDDL[node.pixel] = null;
    				return node.pixel;
    			}
    		}
    	}
    	throw new RuntimeException("no element");  	
    }

    public boolean isEmpty() {
    	return (numElements == 0);
    }

     
    public int size() {
    	return numElements;
    }
    
    public void clearAll(){
    	while(!isEmpty()){
    		remove();
    	}
    }
	
}

/**
 * Classe que representa um node da lista duplamente ligada
 */
class NodeDDL{
	int pixel;
	NodeDDL next;
	NodeDDL prev;
	NodeDDL(int p){ 
		this.pixel = p; 
	}
}

/**
 * Classe que representa um bucket da fila de prioridade
 */    
class Bucket{
	private NodeDDL first;
	private NodeDDL last;
	private int length = 0; 
	private int policy;
	Bucket(int policy){
		this.policy = policy;
	}
    	
	public void add(NodeDDL node){
		if (this.length == 0) { //lista vazia?
            this.first = node;
            this.last = node;
        } else { //adiciona no fim da lista
        	this.last.next = node;
        	node.prev = this.last;
        	this.last = node;	
        }
		length++;
	}
	
	public NodeDDL remove(){
		NodeDDL node = null;
		if(policy == 0 || this.length == 1){ //politica fifo
			node = this.first;
			this.first = this.first.next;
			if(this.first != null){
				this.first.prev = null;
			}
			if (this.length == 0) {
				this.last = null;
			}
		}else{ //politica lifo
			node = this.last;
			this.last = this.last.prev;
			this.last.next = null;
		}
		this.length--;
		node.prev = node.next = null;
		return node;
	}
    	
	public void remove(NodeDDL node){
		NodeDDL nodePrev = node.prev;
		NodeDDL nodeNext = node.next;
		if(nodePrev == null || length == 1){ //primeiro da lista
			this.first = this.first.next;
			if(this.first != null)
				this.first.prev = null;
			this.length--;
			if(length == 0){
				this.last = null;
			}
		}else if (nodeNext == null){ //ultimo da lista
			this.last = nodePrev;
			this.last.next = null;
			this.length--;
		}else{ //meio da lista
			nodePrev.next = nodeNext;
			nodeNext.prev = nodePrev;
			this.length--;
		}
		node.prev = node.next = null;
	}    	
    	
	public boolean isEmpty(){
		return (length == 0);
	}    
 

 }
