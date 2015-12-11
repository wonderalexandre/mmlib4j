package mmlib4j.representation.graph.pixels;

import java.util.ArrayList;
import java.util.List;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.graph.Edge;
import mmlib4j.representation.graph.Graph;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ImageGraph implements Graph<Integer> {

	private GrayScaleImage img;
	private AdjacencyRelation adj;
	private ComputerWeight weiht; 
	
	public ImageGraph(GrayScaleImage image, AdjacencyRelation adj, ComputerWeight weight){
		this.img = image;
		this.adj = adj;
		this.weiht = weight;
	}
	
	public Integer[] getVerteces(){
		Integer vertexes[] = new Integer[img.getSize()];
		for(int i=0; i < img.getSize(); i++)
			vertexes[i] = i;
		return vertexes;
	}
	
	public int getCustVertex(int p){
		return img.getPixel(p);
	}
	
	public int getVertexOfMinCust(){
		return img.minPixel();
	}
	
	public int getVertexOfMaxCust(){
		return img.maxPixel();
	}
	
	public Edge<Integer> getEdge(int p, int q){
		return new Edge<Integer>(p, q, weiht.computerCust(this, p, q));
	}
	
	public float getCustEdge(int p, int q){
		return weiht.computerCust(this, p, q);
	}
	
	public int getNumVerteces(){
		return img.getSize();
	}
	
	public GrayScaleImage getInputImage(){
		return img;
	}
	
	public Iterable<Integer> getAdjacencyVertex(int p){
		return adj.getAdjacencyPixels(img, p);
	}
	
	public List<Edge<Integer>> getEdges(){
		List<Edge<Integer>> edges = new ArrayList<Edge<Integer>>();
		boolean flags[] = new boolean[img.getSize()];
		for(int p=0; p < getNumVerteces(); p++){
			flags[p] = true;
			for(Integer q: getAdjacencyVertex(p)){
				if(!flags[q]){
					edges.add( weiht.get(this, p, q) );
				}
			}
		}
		return edges;
	}
	
	
	public static ImageGraph createGraphWithEdgeWeightByMeans(GrayScaleImage img, AdjacencyRelation adj){
		ComputerWeight w = new ComputerWeight() {
			public Edge<Integer> get(ImageGraph g, int p, int q) {
				return new Edge<Integer>(p, q, (g.img.getPixel(p) + g.img.getPixel(q))/2 ); 
			}
			public int computerCust(ImageGraph g, int p, int q) {
				return (g.img.getPixel(p) + g.img.getPixel(q))/2;
			}
			
		};
		return new ImageGraph(img, adj, w);
	}
	
	public static ImageGraph createGraphWithEdgeWeightByAbs(GrayScaleImage img, AdjacencyRelation adj){
		ComputerWeight w = new ComputerWeight() {
			public Edge<Integer> get(ImageGraph g, int p, int q) {
				return new Edge<Integer>(p, q, (int) Math.pow(g.img.getPixel(p) - g.img.getPixel(q), 2)); 
			}
			public int computerCust(ImageGraph g, int p, int q) {
				return (int) Math.pow(g.img.getPixel(p) - g.img.getPixel(q), 2);
			}
			
		};
		return new ImageGraph(img, adj, w);
	}
	
	public static void main(String args[]){
		GrayScaleImage img = ImageBuilder.openGrayImage(ImageBuilder.windowOpenFile());
		AdjacencyRelation adj = AdjacencyRelation.getCircular(1.5);
		
		ImageGraph graph = ImageGraph.createGraphWithEdgeWeightByMeans(img, adj);
		
		List<Edge<Integer>> edges = graph.getEdges();
		for(Edge<Integer> edge: edges){
			System.out.println("("+edge.getVertex1() + ", " + edge.getVertex2() + ", " + edge.getWeight() + ")");
		}
	}

}
