package mmlib4j.datastruct;

import java.util.Iterator;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class SimpleLinkedList<T> implements Iterable<T> {
	private NodeLL<T> first;
	private NodeLL<T> last;
	private int length = 0; 
	private int policy = 0; //0 = FIFO; 1 = LIFO
	
	public SimpleLinkedList( ){
		this.policy = 0;
	}
	
	public void add(T elem){
		add(new NodeLL<T>(elem));
	}
	
	
	public boolean isEmpty(){
		return (length == 0);
	}  
	
	public int size(){
		return length;
	}
	
	public T getFisrtElement(){
		return first.value;
	}
	
	public T removeFirstElement(){
		NodeLL<T> node = null;
		node = this.first;
		this.first = this.first.next;
		if(this.first != null){
			this.first.prev = null;
		}
		if (this.length == 0) {
			this.last = null;
		}
		
		this.length--;
		node.prev = node.next = null;
		return node.value;
	}
	
	public void addAll(SimpleLinkedList<T> l){
		if(l.isEmpty()) return;
		if (this.length == 0) { //lista vazia?
            this.first = l.first;
            this.last = l.last;
        } 
		else { //adiciona no fim da lista
        	this.last.next = l.first;
        	l.first.prev = this.last;
        	this.last = l.last;	
        }
		
		length += l.size() ;
	}
	
	public void remove(T e){
		Iterator<T> it = iterator();
		while(it.hasNext()){
			if(e.equals(it.next())){
				it.remove();
				return;
			}
		}
	}
	
	public Iterator<T> iterator() {
		
		return new Iterator<T>() {
			NodeLL<T> iter = first;
			NodeLL<T> iterPrev = null;
			
			@Override
			public boolean hasNext() {
				return iter != null;
			}

			@Override
			public T next() {
				T v = iter.value;
				iterPrev = iter;
				iter = iter.next;
				return v;
			}

			@Override
			public void remove() {
				SimpleLinkedList.this.remove(iterPrev);
			}
		};
	}
	
	
	 void add(NodeLL<T> node){
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
	
	 NodeLL<T> remove(){
		NodeLL<T> node = null;
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
    	
	 void remove(NodeLL<T> node){
		NodeLL<T> nodePrev = node.prev;
		NodeLL<T> nodeNext = node.next;
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
    	
	  
 
     
     public static void main(String args[]){
    	 SimpleLinkedList<Integer> l = new SimpleLinkedList<Integer>();
    	 l.add(1);
    	 l.add(2);
    	 l.add(3);
    	 l.add(4);
    	 l.add(5);
    	 
    	 SimpleLinkedList<Integer> l2 = new SimpleLinkedList<Integer>();
    	 l2.add(2);
    	 l2.add(6);
    	 l2.add(1);
    	 l2.add(5);
    	 l2.add(3);
    	 l2.add(4);
    	 
    	 
    	 l2.remove(0);
    	 
    	 
    	 
    	 for(int i : l2){
    		 System.out.println("L2 =>"+i);
    	 }
    	 
    	 l.addAll(l2);
    	 
    	 for(int i : l){
    		  System.out.println(i);
    	 }
     }

	


}

/**
 * Classe que representa um node da lista duplamente ligada
 */
class NodeLL<T>{
	T value;
	NodeLL<T> next;
	NodeLL<T> prev;
	NodeLL(T p){ 
		this.value = p; 
	}
}
