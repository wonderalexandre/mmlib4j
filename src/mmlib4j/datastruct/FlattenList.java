package mmlib4j.datastruct;

import java.util.Iterator;

public class FlattenList<T> implements Flatten<T> {
	
	private SimpleLinkedList<Flatten<T>> lists;
	private int size = 0;
	
	public FlattenList() {
		lists = new SimpleLinkedList<>();		
	}
	
	public void add(Flatten<T> structure) {
		this.size += structure.size();
		this.lists.add(structure);		
	}
	
	public void addAll(FlattenList<T> flattenLists) {
		this.size += flattenLists.size();
		this.lists.addAll(flattenLists.lists);				
	}
	
	public int size() {
		return size;
	}
	
	public int lenght() {
		return lists.size();
	}
	
	@Override
	public Iterator<T> iterator() {
		final Iterator<Flatten<T>> allIterators = lists.iterator();		
		return new Iterator<T>() {						
			Iterator<T> iterator = allIterators.next().iterator();			
			@Override
			public boolean hasNext() {	
				if(iterator.hasNext()) {
					return true;
				} else if(allIterators.hasNext()) {
					iterator = allIterators.next().iterator();
					if(iterator.hasNext())
						return true;
				}
				return false;
			}
			@Override
			public T next() {
				return iterator.next();
			}					
		};
	}
		
}
