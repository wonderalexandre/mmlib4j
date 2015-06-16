package mmlib4j.representation.graph.rag;

import java.util.LinkedList;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class RAGVertex {

	private int label;
	private int rep;
	private int levelRep;
	int xmin = Integer.MAX_VALUE;
	int ymin = Integer.MAX_VALUE;
	int xmax = Integer.MIN_VALUE;
	int ymax = Integer.MIN_VALUE;
	int width;
	LinkedList<Integer> pixels = new LinkedList<Integer>();
	LinkedList<RAGVertex> adjacency = new LinkedList<RAGVertex>();
	
	
	public RAGVertex(int label, int width){
		this.label = label;
		this.width = width;
	}
	
	public void setRep(int rep, int levelRep){
		this.rep = rep;
		this.levelRep = levelRep;
	}
	
	public int getPixelRep(){
		return rep;
	}
	
	public int getLabel(){
		return label;
	}

	public int getLevel(){
		return levelRep;
	}

	public LinkedList<Integer> getPixels() {
		return pixels;
	}

	public void addPixel(int p){
		int x = p % width;
		int y = p / width;
		if(x < xmin) xmin = x;
		if(x > xmax) xmax = x;
		if(y < ymin) ymin = y;
		if(y > ymax) ymax = y;
		
		
		pixels.add(p);
	}
	
	public LinkedList<RAGVertex> getAdjacency() {
		return adjacency;
	}

	public void setAdjacency(LinkedList<RAGVertex> adjacency) {
		this.adjacency = adjacency;
	}
	

	public int getWidthNode(){
		return (xmax - xmin + 1);
	}
	
	public int getHeightNode(){
		return (ymax - ymin + 1);
	}
	
	public int getArea(){
		return pixels.size();
	}
	
}
