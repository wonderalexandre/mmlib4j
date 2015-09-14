package mmlib4j.filtering.binary;

import mmlib4j.datastruct.SimpleLinkedList;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class Contour {
	
	SimpleLinkedList<Integer> pixels = null;
	double perimeter;
	//int widthImg;
	
	public Contour () {
		this.perimeter = 0;
		this.pixels = new SimpleLinkedList<Integer>();
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
	

	/*
	byte[] makeChainCode8(int widthImg) {
		int m = pixels.size();
		if (m>1){
			int[] xPoints = new int[m];
			int[] yPoints = new int[m];
			int k = 0;
			Iterator<Integer> itr = pixels.iterator();
			while (itr.hasNext() && k < m) {
				Integer p = itr.next();
				xPoints[k] = p % widthImg;
				yPoints[k] = p / widthImg;
				k = k + 1;
			}
			return null;
		}
		else {	// use circles for isolated pixels
			//Point cn = points.get(0);
			return null;
		}
	}
	*/
	
	
	
}