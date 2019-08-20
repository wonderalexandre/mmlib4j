package mmlib4j.datastruct;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SimpleArrayList<T> implements Iterable<T> {

	T elementData[];
	int capacity;
	int minCapacity = 10;
	int size;
	
	public SimpleArrayList(){
		this(10);
	}
	
	public SimpleArrayList(int initialCapacity){
		this.capacity = (initialCapacity > 10)? initialCapacity: 10;
		this.elementData = (T[]) new Object[this.capacity];
		this.size = 0;
	}
	
	public void add(T elem){
		 if (capacity - size == 0){
			 int oldCapacity = elementData.length; 
			 capacity = oldCapacity + (oldCapacity >> 1);  
			 elementData = Arrays.copyOf(elementData, capacity);
		 }
	         
		elementData[size++] = elem;
	}
	
	public void sort(Comparator<T> comp) {
		Arrays.sort(elementData,0,size-1, comp);
	}
  
	public static void main(String args[]){
		SimpleArrayList<Integer> list = new SimpleArrayList<Integer>(10);
		for(int i=1; i< 100; i++){
			list.add(i);
			
			System.out.println(list.capacity + " " + list.size);
		}
		
		list.sort(new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}			
		});
		
		for(Integer x: list){
			System.out.println(x);
		}
		
		
	}
	
	public boolean isEmpty(){
		return (size == 0);
	}  
	
	public int size(){
		return size;
	}
	
	public int getIndex(T elem) {
		for(int i=0; i < size; i++) {
			if(elem.equals( elementData[i] )){
				return i;
			}
		}
		return -1;
	}
	
	
	public T get(int index){
		rangeCheck(index);
		
		return elementData[index];
	}
	
	public boolean removeElement(T elem) {
		int index = getIndex(elem);
		if(index == -1) {
			return false;
		}else {
			remove(index);
			return true;
		}
	}
	
    public T remove(int index) {
        rangeCheck(index);
        T oldValue = get(index);
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,numMoved);
        elementData[--size] = null; 

        return oldValue;
    }
    
    private void rangeCheck(int index) {
        if (index >= size)
        		throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
    }
	
	
	
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			 int cursor;       // index of next element to return
			 
			 public boolean hasNext() {
				 return cursor != size;
			 }

			 public T next() {
				 if (cursor >= size)
					 throw new NoSuchElementException();
				 return elementData[cursor++];
			 }
		};
	}

	
}
