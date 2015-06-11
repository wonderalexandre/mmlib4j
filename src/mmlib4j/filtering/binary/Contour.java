package mmlib4j.filtering.binary;

import mmlib4j.datastruct.SimpleLinkedList;

/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class Contour {
	
	SimpleLinkedList<Integer> pixels = new SimpleLinkedList<Integer>();
	double perimeter;
	
	public Contour () {
		perimeter = 0;
	}
	
	void addPoint (Integer n, int direction){
		pixels.add(n);
		if(direction % 2 ==0)
			perimeter += 1;
		else
			perimeter += Math.sqrt(2);
	}
	
	public double getPerimeter(){
		return perimeter;
	}

	public SimpleLinkedList<Integer> getPixels(){
		return pixels;
	}
	
	public Contour getClone(){
		Contour c = new Contour();
		c.pixels = new SimpleLinkedList<Integer>();
		for(int p: this.pixels){
			c.pixels.add(p);
		}
		c.perimeter = this.perimeter;
		return c;
	}
	
}