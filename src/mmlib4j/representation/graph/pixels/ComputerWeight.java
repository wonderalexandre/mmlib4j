package mmlib4j.representation.graph.pixels;

import mmlib4j.representation.graph.Edge;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface ComputerWeight {

	public Edge<Integer> get(ImageGraph g, int p, int q);
	
	public int computerCust(ImageGraph g, int p, int q);
}
