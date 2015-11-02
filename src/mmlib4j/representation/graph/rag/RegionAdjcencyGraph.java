package mmlib4j.representation.graph.rag;

import java.util.ArrayList;
import java.util.Collections;

import mmlib4j.filtering.MorphologicalOperatorsBasedOnSE;
import mmlib4j.images.BinaryImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.BitImage;
import mmlib4j.representation.graph.Edge;
import mmlib4j.representation.graph.Graph;
import mmlib4j.segmentation.Labeling;
import mmlib4j.segmentation.RegionalMinimaByIFT;
import mmlib4j.segmentation.ThresholdGlobal;
import mmlib4j.segmentation.WatershedByIFT;
import mmlib4j.utils.AdjacencyRelation;



/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class RegionAdjcencyGraph implements Graph<Integer>{


	protected RAGVertex vertices[];
	protected ArrayList<ArrayList<Edge<Integer>>> edgesByVertex;
	protected ArrayList<Edge<Integer>> edges;
	protected GrayScaleImage labels;
	protected GrayScaleImage img;
	protected AdjacencyRelation adj8; 
	protected ComputerWeightRAG<RegionAdjcencyGraph> computerWeight;
	
	protected RegionAdjcencyGraph(GrayScaleImage img, GrayScaleImage labels, ComputerWeightRAG<RegionAdjcencyGraph> compWeight){
		this.adj8 = AdjacencyRelation.getCircular(1.5);
		this.img = img;
		this.labels = labels;
		this.computerWeight= compWeight;
	}
	
	public static RegionAdjcencyGraph getRAGByBasinsWatershed(GrayScaleImage imgIn){
		return getRAGByBasinsWatershed(imgIn, false);
	}
	
	public static RegionAdjcencyGraph getRAGByBasinsWatershed(GrayScaleImage imgIn, boolean flag){
		AdjacencyRelation adj8 = AdjacencyRelation.getCircular(1.5);
		GrayScaleImage img = MorphologicalOperatorsBasedOnSE.gradient(imgIn, adj8);
		GrayScaleImage imgMinima = RegionalMinimaByIFT.extractionOfRegionalMinima(img);
		GrayScaleImage labels = WatershedByIFT.watershedByMarker(img, imgMinima);
		//IGrayScaleImage labels = IFT.watershedByHBacia(adj8, img, 1);
		//WindowImages.show(IFT.watershedLineByMarked(adj8, img, imgMinima));
		ComputerWeightRAG<RegionAdjcencyGraph> compWeight = new ComputerWeightRAG<RegionAdjcencyGraph>() {
			public int computerCust(RegionAdjcencyGraph g, int p, int q) {
				return Math.max(g.img.getPixel(p), g.img.getPixel(q));
			}
		};
		
		RegionAdjcencyGraph rag = new RegionAdjcencyGraph(img, labels, compWeight);
		rag.createRAG(ThresholdGlobal.upperSet(imgMinima, 0), flag);
		
		return rag;
	}

	
	public static RegionAdjcencyGraph getRAGByFlatzone(GrayScaleImage img, AdjacencyRelation adj){
		return getRAGByFlatzone(img, false, adj);
	}
	
	public static RegionAdjcencyGraph getRAGByFlatzone(GrayScaleImage img){
		return getRAGByFlatzone(img, true, AdjacencyRelation.getCircular(1.5));
	}
	
	public static RegionAdjcencyGraph getRAGByFlatzone(GrayScaleImage img, boolean flag, AdjacencyRelation adj8){
		GrayScaleImage labels = Labeling.labeling(img, adj8);
		boolean flagLabels[] = new boolean[labels.maxValue()+1];
		BinaryImage imgRepresentanteLabel = new BitImage(img.getWidth(), img.getHeight());
		for(int p=0; p < img.getSize(); p++){
			int value = labels.getPixel(p);
			if(!flagLabels[value]){
				flagLabels[value] = true;
				imgRepresentanteLabel.setPixel(p, true);
			}
		}
		ComputerWeightRAG<RegionAdjcencyGraph> compWeight = new ComputerWeightRAG<RegionAdjcencyGraph>() {
			public int computerCust(RegionAdjcencyGraph g, int p, int q) {
				return Math.abs(g.img.getPixel(p) - g.img.getPixel(q));
			}
		};

		RegionAdjcencyGraph rag = new RegionAdjcencyGraph(img, labels, compWeight);
		rag.createRAG(imgRepresentanteLabel, flag);
		//rag.print();
		return rag;
	}
	
	public GrayScaleImage getInputImage(){
		return img;
	}
	
	public int getNumVerteces(){
		return vertices.length;
	}
	
	public int getCustVertex(int p){
		return vertices[p].getLevel();
	}
	
	public int getCustEdge(int p, int q){
		return getEdge(vertices[p], vertices[q]).getWeight();
	}
	
	

	public void print(){
		Integer v[] = getVerteces();
		
		System.out.println("Lista de vertices:");
		for(int i=0; i < v.length; i++){
			System.out.println("=>Indice:" + v[i] + "\t Level:" + vertices[v[i]].getLevel() + "\t Label:" + vertices[v[i]].getLabel());
		}
		ArrayList<Edge<Integer>> edges = getEdges();
		Collections.sort(edges);
		System.out.println("\nLista de arestas:");
		for(Edge<Integer> e: edges){
			System.out.println(">> (" + e.getVertex1() + ", " + e.getVertex2() + ") = " + e.getWeight());
		}
		
	}
	
	public Integer[] getVerteces(){
		Integer v[] = new Integer[getNumVerteces()];
		for(int i=0; i < getNumVerteces(); i++){
			v[i] = i;
		}
		return v;
	}
	
	public RAGVertex[] getRAGVertexes(){
		return vertices;
	}
	
	/**
	 * Pega o vertice que contem o pixel p
	 * @param p
	 * @return
	 */
	public RAGVertex getVertexByPixel(int p){
		return vertices[labels.getPixel(p) - 1];
	}
	
	/**
	 * Pega o vertice que contem o pixel p
	 * @param p
	 * @return
	 */
	public RAGVertex getVertexByIndex(int p){
		return vertices[p];
	}
	
	
	/**
	 * Retorna o conjunto de aresta que incidem em v (ou seja, os vizinhos de v)
	 * @param v
	 * @return
	 */
	public ArrayList<Edge<Integer>> getEdges(RAGVertex v){
		return edgesByVertex.get(v.getLabel() - 1);
	}
	
	public ArrayList<Edge<Integer>> getEdges(int index){
		return edgesByVertex.get(vertices[index].getLabel() - 1);
	}
	
	public ArrayList<Edge<Integer>> getEdges(){
		return edges;
	}
	
	/**
	 * Pega a aresta que liga v1 a v2
	 * @param v1
	 * @param v2
	 * @return
	 */
	public Edge<Integer> getEdge(RAGVertex v1, RAGVertex v2){
		if (v1.getLabel() < v2.getLabel()) {
			return edges.get(v1.getLabel()-1);
		} else {
			return edges.get(v2.getLabel()-1);
		}
	}
	
	
	
	/**
	 * Cria o grafo
	 * @param imgRep
	 */
	protected void createRAG(BinaryImage imgRep, boolean isEdgesAdjByVertex) {
		int numPrimitiveCBs = labels.maxValue();
		vertices = new RAGVertex[numPrimitiveCBs];

		//criando os vertices
		for (int i = 0; i < numPrimitiveCBs; i++) {
			vertices[i] = new RAGVertex(i+1, img.getWidth());
		}
		
		//adicionando os pixels nos vertices
		for(int p=0; p < labels.getSize(); p++){
			getVertexByPixel(p).addPixel(p);
			if(imgRep.isPixelForeground(p)){
				getVertexByPixel(p).setRep(p, img.getPixel(p));
			}
		}

		// criando as arestas por vertices
		edgesByVertex = new ArrayList<ArrayList<Edge<Integer>>>(numPrimitiveCBs);
		//edges = new Edge[numPrimitiveCBs][numPrimitiveCBs];
		edges = new ArrayList<Edge<Integer>>(numPrimitiveCBs);
		
		for (int i = 0; i < numPrimitiveCBs; i++) {
			edgesByVertex.add(new ArrayList<Edge<Integer>>());
		}
		
		for(int p=0; p < labels.getSize(); p++){
			for(Integer q: adj8.getAdjacencyPixels(labels, p)){
				if(labels.getPixel(p) != labels.getPixel(q)){
					createEdge(p, q);
				}
			}
		}
		
		if(isEdgesAdjByVertex)
			for(ArrayList<Edge<Integer>> edges: edgesByVertex){
				for(Edge<Integer> e: edges){
					vertices[e.getVertex1()].adjacency.add( vertices[e.getVertex2()] );
				}
			}
		
	}
	
	
	
	
	/**
	 * Cria uma aresta entre dois vertice, cujo o peso e a menor transicao entre as duas regioes
	 * @param p
	 * @param q
	 */
	protected void createEdge(int p, int q) {
		RAGVertex basin1 = getVertexByPixel(p);
		RAGVertex basin2 = getVertexByPixel(q);
		
		int weight = Math.max(img.getPixel(p), img.getPixel(q));
		RAGVertex b1, b2;

		if (basin1.getLabel() < basin2.getLabel()) {
			b1 = basin1;
			b2 = basin2;
		} else {
			b1 = basin2;
			b2 = basin1;
		}
		
		ArrayList<Edge<Integer>> edgesB1 = this.getEdges(b1);
		boolean redundantEdge = false;
		for (Edge<Integer> edge : edgesB1) {
			if (vertices[edge.getVertex2()] == b2) {
				redundantEdge = true;
				if (edge.getWeight() > weight) {
					edge.setWeight(weight);	
				}
				break;
			}
		}
		
		if (!redundantEdge) {
			Edge<Integer> edge = new Edge<Integer>(b1.getLabel()-1, b2.getLabel()-1, computerWeight.computerCust(this, p, q));
			edgesB1.add(edge); // adds to b1
			this.getEdges(b2).add(edge); // adds to b2
			
			edges.add(edge);
			//edges[b1.getLabel()-1][b2.getLabel()-1] = edge;
			//edges[b2.getLabel()-1][b1.getLabel()-1] = edge;
		}
	}
	
	


}

