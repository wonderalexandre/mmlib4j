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
	
	public static void main(String args[]) {
		
		SimpleLinkedList<Integer> a = new SimpleLinkedList<>();
		a.add(10);
		a.add(20);
		a.add(100);
		a.add(8);
		
		SimpleLinkedList<Integer> b = new SimpleLinkedList<>();
		b.add(56);
		b.add(78);
		b.add(17);
		b.add(99);		
		
		FlattenList<Integer> c = new FlattenList<>();
		c.add(a);
		c.add(b);
		
		FlattenList<Integer> h = new FlattenList<>();
		h.add(a);
		h.add(b);
		
		for(int p : c) {
			System.out.println(p);
		}
		
		System.out.println();
		for(int p : c) {
			System.out.println(p);
		}
		
		System.out.println();
		//c.add(d);
		c.addAll(h);
		System.out.println(c.size());
		//System.out.println(c.iterators.iterator().hasNext());
		
	}	
}
