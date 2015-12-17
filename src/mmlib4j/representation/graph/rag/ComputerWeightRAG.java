package mmlib4j.representation.graph.rag;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface ComputerWeightRAG<T> {

	/**
	 * 
	 * @param g 
	 * @param p - pixel ou indice do vertice 
	 * @param q - pixel ou indice do vertice
	 * @return
	 */
	public int computerCust(T graph, int p, int q);
}
