package mmlib4j.representation.tree.mst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.graph.Edge;
import mmlib4j.representation.graph.Graph;
import mmlib4j.representation.graph.rag.RegionAdjcencyGraph;
import mmlib4j.utils.Resampling;

/**
 * Implementacao do algoritmo para construcao de morphological tree (watershed, bpt, flat-zones tree) descrito em: 
 * Laurent Najman, Jean Cousty, and Benjamin Perret 
 * Playing with Kruskal: Algorithms for Morphological Trees in Edge-Weighted Graphs, ISMM, 2013.
 * @author wonderalexandre
 *
 */
public class MorphologicalTreeByKruskal {

	private Edge<Integer> mst[];
	private DisjointSetBPT qBT;
	private DisjointSetMSE qT;
	private DisjointSetEBPT qEBT;
	private int attribute[];
	private int attributeComp[];
	private int numVertexes;
	private int numEdges;
	Graph<Integer> graph;
	
	public MorphologicalTreeByKruskal(Graph<Integer> graph){
		
		List<Edge<Integer>> edges = graph.getEdges();
		Collections.sort(edges);
		this.graph = graph;
		this.numEdges = edges.size();
		this.numVertexes = graph.getNumVerteces();
		this.mst = new Edge[numVertexes-1];
		this.attributeComp = new int[numVertexes*2-1];
		this.attribute = new int[numVertexes*2-1];
		this.qEBT = new DisjointSetEBPT(numVertexes);
		
		Arrays.fill(attributeComp, -1);
		
		int e = 0;
		for(int i=0; i < graph.getNumVerteces(); i++)
			qEBT.makeSet(i);
		
		for(Edge<Integer> edge: edges){
			int cX = this.qEBT.findCanonical(edge.getVertex1());
			int cY = this.qEBT.findCanonical(edge.getVertex2());
			
			if(cX != cY){
				this.qEBT.union(cX, cY);
				this.mst[e] = edge;
				e += 1;
			}
			else{
				doSomething(edge, e);
			}
			
		}
		this.qBT = this.qEBT.qBt;
		this.qT = this.qEBT.qT;
		
		//getAttribute(qBT.size-1);
		//attribute = computeMergeAttributeMST();

		/*for(int n=0; n < qBT.size; n++){
			if(!qBT.isLeaf(n))
				getAttribute(n);
		}*/
	}
	
	public void doSomething(Edge<Integer> edge, int e){
		//attributeComp[e] =  1;
		
		//System.out.println(edge.getVertex1() +", "+ edge.getVertex2() + " = " + edge.getWeight());
	}
	
	
	public DisjointSetBPT canonizeBT(){
		DisjointSetBPT qCT = new DisjointSetBPT(qBT.parent.length+1);
		for(int n=0; n < qBT.parent.length; n++){
			qCT.parent[n] = qBT.parent[n];
			qCT.size += 1;
		}
		
		// canonizacao da arvore
		for (int n = qBT.parent.length-1; n >= numVertexes; n--) {
			if(qBT.parent[n] == -1) 
				continue;
			int p = qCT.parent[n];
			if (weightNode(p) == weightNode(n)) {
				for(int c: qBT.children[n]){
					qCT.parent[c] = p;
				}
				qCT.parent[n] = n;
			}	
		}
		// If needed, build the list of children
		for(int n=0; n < qCT.parent.length; n++){
			int p = qCT.parent[n]; 
			if (p >= 0 && p != n){
				qCT.children[p].add(n);	
			}
		}
		//qBT = qCT;
		return qCT;
		
	}
	/**
	 * Data: QBT
	 * Result: A binary array ws indicating which MST edges are watershed
	 */
	public boolean[] watershed(){
		int minima[] = new int[qBT.parent.length];
		//boolean ws[] = new boolean[numVertexes];
		boolean ws[] = new boolean[qBT.parent.length];
		for(int n=numVertexes; n < qBT.parent.length; n++){
			boolean flag = true;
			int nb = 0;
			for(int c: qBT.children[n]){
				int m = minima[c];
				nb = nb + m;
				if (m == 0) {
					flag = false;
				}
			}
			//ws[getEdge(n)] = flag;
			ws[n] = flag;
			if (nb != 0) {
				minima[n] = nb; 
			}else{
				if (qBT.parent[n] == -1) {
					minima[n] = 1; 
				}else{
					int p = qBT.parent[n];
					if (weightNode(n) < weightNode(p))
						minima[n] = 1; 
					else 
						minima[n] = 0;	
				}			
			}
		}
		return ws;
	}
	
	
	/*
	 * Data: QBT
	 * Result: a reweighted MST G corresponding to the attribute-based hierarchy
	 */
	public int[] computeMergeAttributeMST( ){
		int G[] = new int[mst.length];
		for(int n=0; n < qBT.size; n++){
			if(!qBT.isLeaf(n)){
				int	a1 = attribute[qBT.children[n].get(0)];
				int a2 = attribute[qBT.children[n].get(1)];
				G[getEdge(n)] = Math.min(a1, a2);	
			}
		}
		return G;
	}
	
	/**
	 * @param n - A node n of QBT
	 * @return The attribute at the time of the merging
	 */
	public int getAttribute(int n){
		if (qBT.isRoot(n) || (weightNode(qBT.parent[n]) != weightNode(n) )) {
			for(Integer c: qBT.children[n]){
				if(!qBT.isLeaf(c))
					getAttribute(c); 
			}
			attribute[n] = mst[getEdge(n)].getWeight();
		}
		else{
			int max = 0;
			for(Integer c: qBT.children[n]){
				int v = getAttribute(c);
				if(v > max) 
					max = v; 
			}
			attribute[n] = max;
		}
		return attribute[n];
	}
	
	public void print(int raiz, int parent[], ArrayList<Integer> children[]){
		System.out.println("Raiz: " + raiz);
		for(Integer son: children[raiz]){
			System.out.println("son:"+ son);
		}
		System.out.println();
		for(Integer son: children[raiz]){
			print(son, parent, children);
		}
	}
	
	
	
	/*
	 * Data: a (non-leaf) node n of QBT
	 * Result: the edge e of the MST corresponding to the nth node
	 */
	public int getEdge(int n){
		return n - numVertexes;
	}
	
	/*
	 * Data: a (non-leaf) node of the tree
	 * Result: the weight of the MST edge associated with the nth node of QBT 
	 */
	public int weightNode(int n){
		return (int) mst[getEdge(n)].getWeight();
	}
	
	
	public class DisjointSetBPT{
		public int parent[];
		public LinkedList<Integer> children[];
		public int size;
		
		public DisjointSetBPT(int numE){
			parent = new int[numE-1];
			children = new LinkedList[numE-1];
			for(int i=0; i < numE-1; i ++){
				children[i] = new LinkedList<Integer>();
			}
			size = 0;
		}
		
		public boolean isLeaf(int n){
			return children[n].size() == 0;
		}
		
		public boolean isRoot(int n){
			return qBT.parent[n] == -1;
		}
		
		void makeSet(int q){
			this.parent[q] = -1;
			this.size += 1;
		}
		
		int findCanonical(int q){
			while(this.parent[q] >= 0){ 
				q  = this.parent[q]; 
			}
			return q;
		}
		
		int union(int cx, int cy){
			this.parent[cx] = this.size; 
			this.parent[cy] = this.size;
			this.makeSet(this.size);
			return this.size - 1;
		}
	}
	
	public class DisjointSetMSE{
		int parent[];
		int rank[];
		int size;
		
		DisjointSetMSE(int numE){
			parent = new int[numE-1];
			rank = new int[numE-1];
			size = 0;
		}
		
		void makeSet(int q){
			this.parent[this.size] = -1; 
			this.rank[this.size] = 0; 
			this.size += 1;
		}
		
		int findCanonical(int q){
			int r = q;
			while(this.parent[r] >= 0){
				r = this.parent[r];
			}
			int tmp;
			while(this.parent[q] >= 0){ 
				tmp = q;
				q  = this.parent[q];
				this.parent[tmp] = r;
			}
			return r;
		}
		
		int union(int cx, int cy){
			if(this.rank[cx] > this.rank[cy]){
				//swap cx por cy;
				int tmp  = cx;
				cx = cy;
				cy = tmp;
			}
			if (this.rank[cx] == this.rank[cy]) { 
				this.rank[cy] += 1;
			}
			this.parent[cx] = cy;
			
			return cy;
			
		}
	}
	
	public class DisjointSetEBPT {
		int root[];
		DisjointSetBPT qBt;
		DisjointSetMSE qT;
		
		DisjointSetEBPT(int numV){
			root = new int[numV];
			qBt = new DisjointSetBPT(numV*2);
			qT = new DisjointSetMSE(numV*2);
		}
		
		void makeSet(int q){
			this.root[q] = q; 
			qBt.makeSet(q); 
			qT.makeSet(q);
			
		}
		
		int findCanonical(int q){
			return qT.findCanonical(q);
		}
		
		
		int union(int cx, int cy){
			int tu  = this.root[cx]; 
			int tv =  this.root[cy]; 
			
			// Union in QBT(without compression)
			qBt.parent[tu] = qBt.parent[tv] = qBt.size;
			
			// If children are needed, add them to the root
			qBt.children[qBt.size].add(tu); 
			qBt.children[qBt.size].add(tv);
			
			int c = qT.union(cx ,cy ); // Union in QT (with compression)
			this.root[c] = qBt.size; // Update the root of QEBT
			qBt.makeSet(qBt.size);
			
			//System.out.println(c +" => "+ qBt.size);
			return qBt.size-1;
		}
	}
	

	
	public Edge<Integer>[] getEdgeMST(){
		return mst;
	}
	
	public Edge<Integer> getEdgeMST(int p){
		return mst[p];
	}
	
	public DisjointSetBPT getBinaryPartitionTree(){
		return qBT;
	}
	
  /**
   * Imprime a arvore binaria
   * @param no
   * @param h - tabulacao
   */
  private void printTree(int no, int h){
	  if(no >= numVertexes){
		  int right = qBT.children[no].get(0); 
		  int left = qBT.children[no].get(1); 
		  printTree(right, h+2);
		  for (int i= 1; i < h; i++)
			  System.out.print("     ");
		  System.out.println(" ["+ no +";" + weightNode(no) + ";"+ qBT.children[no].size() +" ]");
		  printTree(left, h+2);
	  }else{
		  for (int i= 1; i < h; i++)
			  System.out.print("     ");
		  System.out.println(" ["+ no + ";>"+ graph.getCustVertex(no)  +"]");
      }
      
  }
  
  public void printTree() {
      System.out.println("Binary partition tree");
      System.out.println(">> Num nodes:" + qBT.size);
      System.out.println("\nTree:\n");
      printTree(qBT.size-1, 1);
      System.out.println("\n");
  }

/*
	public void draw(){
		UndirectedSparseGraph<Integer,Edge<Integer>> tree = new UndirectedSparseGraph<Integer,Edge<Integer>>();
		 //Forest<Integer,Edge<Integer>> tree = new DelegateTree<Integer,Edge<Integer>>();
		 for(Edge<Integer> edge: mst){
			 tree.addEdge(edge, edge.getVertex1(), edge.getVertex2());
		 }
		 
		 KKLayout<Integer,Edge<Integer> > graphLayout = new KKLayout<Integer,Edge<Integer>>(tree);
		 //TreeLayout<Integer,Edge<Integer>> graphLayout = new TreeLayout<Integer,Edge<Integer>>(tree);
		 final VisualizationViewer<Integer,Edge<Integer>> vv =  new VisualizationViewer<Integer,Edge<Integer>>(graphLayout);
		 
		 vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
		 vv.getRenderContext().setVertexLabelTransformer(new Transformer<Integer,String>() {
			    public String transform(Integer v) {
			        return  String.valueOf(graph.getCustVertex(v));
			    }
		 });
		 vv.getRenderContext().setEdgeLabelTransformer(new Transformer<Edge<Integer>, String>() {
			 public String transform(Edge<Integer> e) {
				return String.valueOf(e.getWeight());
			 }
		});
		 vv.getRenderContext().setEdgeDrawPaintTransformer(new Transformer<Edge<Integer>, Paint>() {
			public Paint transform(Edge<Integer> arg0) {
				return Color.GREEN;
			}			 
		});
		vv.getRenderContext().setVertexShapeTransformer(new Transformer<Integer, Shape>() {
			public Shape transform(Integer arg0) {
				return new Ellipse2D.Double(-7,-7,15,15);
			}
		});
		 
		 // vv.getRenderContext().setVertexFillPaintTransformer(new NodeShape());
		 // add a listener for ToolTips
		 vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray));

		 
		 
		 final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
		 graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
		 vv.setGraphMouse(graphMouse);
		 	
		 
		 final ScalingControl scaler = new CrossoverScalingControl();

		 JButton plus = new JButton("+");
		 plus.addActionListener(new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 scaler.scale(vv, 1.1f, vv.getCenter());
			 }
		 });
		 JButton minus = new JButton("-");
		 minus.addActionListener(new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 scaler.scale(vv, 1/1.1f, vv.getCenter());
			 }
		 });
	        	           
		 
		 
		 JPanel scaleGrid = new JPanel(new GridLayout(1,0));
		 scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
		 scaleGrid.add(plus);
		 scaleGrid.add(minus);
	        
		 final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
	     JFrame frame = new JFrame("Arvore geradora minima (Kruskal)");   
	     frame.setSize(500, 500);
	     frame.setLayout(new BorderLayout());
	     frame.add(panel, BorderLayout.CENTER);
		 frame.add(scaleGrid, BorderLayout.SOUTH); 
		 frame.setVisible(true);
	}
  */
	
	public static void main(String[] args) {
		long ti = System.currentTimeMillis();
			
		byte pixels[] = new byte[] { 
					30, 30, 30, 30, 30, 25, 25, 25, 25, 25, 25,
					30, 31, 31, 30, 30, 25, 25, 25, 25, 25, 25,
					30, 30, 30, 30, 30, 00, 25, 25, 25, 25, 25,
					30, 30, 30, 30, 30, 00, 00, 00, 00, 00, 25,
					50, 50, 50, 30, 30, 00, 00, 00, 00, 00, 00,
					50, 50, 50, 50, 50, 00, 00, 77, 77, 77, 00,
					50, 50, 50, 50, 50, 00, 00, 77, 80, 77, 00
		};
		int width = 11;
		int height = 7;
			
		for(int y=0; y < height; y++){
			for(int x=0; x < width; x++){
				int i = x + y * width;
				System.out.printf("%3d ", pixels[i]);
			}
			System.out.println();
		}
		GrayScaleImage img = ImageFactory.createReferenceGrayScaleImage(8, pixels, width, height);
		WindowImages.show(Resampling.nearestNeighbor(img, 50));
		//IGrayScaleImage img = ImageBuilder.openGrayImage();
		RegionAdjcencyGraph graph = RegionAdjcencyGraph.getRAGByFlatzone(img);
		//ImageGraph graph = ImageGraph.createGraphWithEdgeWeightByAbs(img, AdjacencyRelation.getCircular(1));
		//graph.print();
		MorphologicalTreeByKruskal mtree = new MorphologicalTreeByKruskal(graph);
		mtree.printTree();
		DisjointSetBPT bpt = mtree.getBinaryPartitionTree();
		mtree.printTree();
		
		//IGrayScaleImage img = ImageBuilder.openGrayImage();
		//Graph<Integer> graph = GraphImage.createGraphWithEdgeWeightByAbs(img, AdjacencyRelation.getCircular(1));
		//Graph<Integer> graph = RegionAdjcencyGraph.getRAGByFlatzone(img);
		//new MorphologicalTreeByKruskal(graph);
		
		//new MorphologicalTreeByKruskal(pixels, width, height, AdjacencyRelation.getCircular(1));
			
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao  "+ ((tf - ti) /1000.0)  + "s");

	}
		
}
