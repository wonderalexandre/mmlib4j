package mmlib4j.representation.graph;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * @description
 * Uma aresta de um grafo
 * @param <W> - tipo de dado do peso
 */
public class Edge<V> implements Comparable<Edge<V>> {
	private V vertex1;
	private V vertex2;
	private int weight;

	public Edge(V v1, V v2, int w){
		vertex1 = v1;
		vertex2 = v2;
		weight = w;
	}
	
	public V getVertex1(){
		return vertex1;
	}
	
	public V getVertex2(){
		return vertex2;
	}
	
	public int getWeight(){
		return weight;
	}
	
	public void setVertex1(V vertex1) {
		this.vertex1 = vertex1;
	}

	public void setVertex2(V vertex2) {
		this.vertex2 = vertex2;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int compareTo(Edge<V> o) {
		if(this.weight < o.weight) return -1;
		else if(this.weight > o.weight) return 1;
		else return 0;
	}

	public String toString(){
		return "(" + this.vertex1 + ", " + this.vertex2 + ") = " + this.weight;
	}

}
