package mmlib4j.representation.tree.mst.tcl;

import java.util.Arrays;

import mmlib4j.datastruct.Queue;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.graph.Edge;
import mmlib4j.representation.graph.rag.RegionAdjcencyGraph;
import mmlib4j.representation.tree.mst.IMorphologicalTreeSegmentation;
import mmlib4j.representation.tree.mst.MorphologicalTreeByKruskal;
import mmlib4j.utils.ImageBuilder;

public class TreeOfCriticalLakes implements IMorphologicalTreeSegmentation{

	private NodeTCL root;
	private int numNode;
	private int heightTree;
	MorphologicalTreeByKruskal mtree;
	RegionAdjcencyGraph graph;
	private GrayScaleImage imgInput;

	
	private TreeOfCriticalLakes(GrayScaleImage img){
		this.imgInput = img;
	}
	
	public static TreeOfCriticalLakes getInstance(GrayScaleImage img, int attr){
		long ti = System.currentTimeMillis();
		TreeOfCriticalLakes instance = new TreeOfCriticalLakes(img);
		instance.graph = RegionAdjcencyGraph.getRAGByBasinsWatershed(img);
		instance.mtree = new MorphologicalTreeByKruskal(instance.graph);
		instance.createTCL(attr);
		//computerHeightNodes(this.root, 0);
		System.out.println("Height of tree:" + instance.heightTree);
		//computerAttribute(this.root);
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [create tree fo critical lakes] "+ ((tf - ti) /1000.0)  + "s");
		return instance;
	}

	
	/*
	Algoritmos de Meyer
		1 arestasOrdenadas <= arestas da AGM ordenadas por peso
		2 para cada aresta a de arestasOrdenadas faca
		3 	Sejam b1 e b2 os vertices ligados por a e p o peso da aresta a
		4 	Crie um novo vertice lagoCritico
		5 	Ligue os vizinhos de b1 e b2 a lagoCritico
		6 	Crie uma aresta orientada de b1 a lagoCritico com peso p
		7 	Crie uma aresta orientada de b2 a lagoCritico com peso p
		8 	Suprima a do conjunto de arestas
	*/
	public void createTCL(int attr){
		
		
		NodeTCL nodes[] = new NodeTCL[graph.getNumVerteces() * 2 - 1];
		//criando as folhas
		for(int i=0; i < graph.getNumVerteces(); i++){
			nodes[i] = new NodeTCL(i, graph.getCustVertex(i), imgInput, true);
			
			for(int p: graph.getVertexByIndex(i).getPixels())
				nodes[i].addPixel(p);
			//nodes[i].initAttributes(NUM_ATTRIBUTES);
		}
		
		Edge<Integer> mst[] = getMSTOrderByAttribute(attr);
		
		int parent[] = new int[graph.getNumVerteces() * 2 - 1];
		for(int i=0; i < parent.length; i++){
			parent[i] = i;
		}
		
		//criando e lincando os nos da arvore
		int indexParent = graph.getNumVerteces();
		for(int i=0; i < mst.length; i++){
			
			int r1 = findRoot(parent,  mst[i].getVertex1());
			int r2 = findRoot(parent,  mst[i].getVertex2());
			if(r1 != r2){
				parent[r1] = indexParent;
				parent[r2] = indexParent;
				
				
				nodes[indexParent] = new NodeTCL(indexParent, -1, imgInput, false);
				nodes[indexParent].left = nodes[r1];
				nodes[indexParent].right = nodes[r2];
				nodes[r1].parent = nodes[indexParent];
				nodes[r2].parent = nodes[indexParent];
				
				nodes[r1].attribute = mst[i].getWeight();
				nodes[r2].attribute = mst[i].getWeight();
				
				
				
				//nodes[r1].attributeValue[ATTRIBUTE_ALTITUDE] = mst[i].getWeight();
				//nodes[r2].attributeValue[ATTRIBUTE_ALTITUDE] = mst[i].getWeight();
				indexParent++;
				
			}
		}
		
		
		numNode = nodes.length;
		nodes[indexParent-1].attribute  = Math.max(nodes[indexParent-1].left.attribute,  nodes[indexParent-1].right.attribute);
		root = nodes[indexParent-1];
	}
	
	

    /*
     * Desenho da arvore com API Jung 
     *
    private void draw(NodeTCL no, DelegateTree<NodeTCL,Edge<NodeTCL>> guiTree){
        if (no != null){
        	guiTree.addEdge(new Edge<NodeTCL>(no.parent, no, no.attribute), no.parent, no);
    		draw(no.right, guiTree);
            draw(no.left, guiTree);
        }
    }
    
    
    public void draw() {
    	DelegateTree<NodeTCL,Edge<NodeTCL>> guiTree = new DelegateTree<NodeTCL, Edge<NodeTCL>>();
		guiTree.setRoot(getRoot());
		draw(getRoot().left, guiTree);
		draw(getRoot().right, guiTree);
		 
		 TreeLayout<NodeTCL,Edge<NodeTCL>> graphLayout = new TreeLayout<NodeTCL,Edge<NodeTCL>>(guiTree);
		 
		 final VisualizationViewer<NodeTCL,Edge<NodeTCL>> vv =  new VisualizationViewer<NodeTCL,Edge<NodeTCL>>(graphLayout);
		 
		 vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
		 vv.getRenderContext().setVertexLabelTransformer(new Transformer<NodeTCL,String>() {
			    public String transform(NodeTCL v) {
			    	return String.valueOf(v.level);
			    }
		 });
		 vv.getRenderContext().setEdgeLabelTransformer(new Transformer<Edge<NodeTCL>, String>() {
			 public String transform(Edge<NodeTCL> e) {
				return String.valueOf(e.getWeight());
			 }
		});
		 
		 vv.getRenderContext().setEdgeFontTransformer(new Transformer<Edge<NodeTCL>, Font>() {
			public Font transform(Edge<NodeTCL> arg0) {
				return new Font(Font.SANS_SERIF, Font.BOLD, 9);
			}			 
		});
		 vv.getRenderContext().setVertexFontTransformer(new Transformer<NodeTCL, Font>() {
				public Font transform(NodeTCL arg0) {
					return new Font(Font.SANS_SERIF, Font.BOLD, 11);
				}			 
			});		 
		 vv.getRenderContext().setEdgeDrawPaintTransformer(new Transformer<Edge<NodeTCL>, Paint>() {
			public Paint transform(Edge<NodeTCL> arg0) {
				return Color.BLACK;
			}			 
		});
		 vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<NodeTCL, Paint>() {
				public Paint transform(NodeTCL no) {
					if(no.isLeaf)
						return Color.RED;
					else
						return Color.BLUE;
				}			 
			});
		vv.getRenderContext().setVertexShapeTransformer(new Transformer<NodeTCL, Shape>() {
			public Shape transform(NodeTCL arg0) {
				return new Ellipse2D.Double(-3,-3,7,7);
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
	     JFrame frame = new JFrame("Arvore de lagos criticos (Kruskal)");   
	     frame.setSize(500, 500);
	     frame.setLayout(new BorderLayout());
	     frame.add(panel, BorderLayout.CENTER);
		 frame.add(scaleGrid, BorderLayout.SOUTH); 
		 frame.setVisible(true);
		 
	}
	
	public void drawOld(int attr){
		Edge<Integer> mst[] = getMSTOrderByAttribute(attr);
		UndirectedSparseGraph<Integer,Edge<Integer>> tree = new UndirectedSparseGraph<Integer,Edge<Integer>>();
		 //Forest<Integer,Edge<Integer>> tree = new DelegateTree<Integer,Edge<Integer>>();
		 for(Edge<Integer> edge: mst){ 
			 tree.addEdge(edge, edge.getVertex1(), edge.getVertex2());
		 }
		 
		 int contVertice = tree.getVertexCount();
		 for(int i=0; i < 3; i++){
			 Edge<Integer> edge = mst[i];
		 	int v = tree.getVertexCount();
		 	
		 	tree.removeEdge(edge);

			Edge<Integer> aresta1[] = tree.getIncidentEdges(edge.getVertex1()).toArray(new Edge[0]);
			Edge<Integer> aresta2[] = tree.getIncidentEdges(edge.getVertex2()).toArray(new Edge[0]);
		 	
			Edge<Integer> e1 = new Edge<Integer>(edge.getVertex1(), v, edge.getWeight());
			Edge<Integer> e2 = new Edge<Integer>(edge.getVertex2(), v, edge.getWeight());
			
			tree.addEdge(e1, e1.getVertex1(), e1.getVertex2());
			tree.addEdge(e2, e2.getVertex1(), e2.getVertex2());
			
			
			
			
			for(Edge<Integer> e: aresta1){
				
				tree.removeEdge(e);
				if(e.getVertex1() == edge.getVertex1())
					e.setVertex1(v);
				if(e.getVertex2() == edge.getVertex1() )
					e.setVertex2(v);	
				
				tree.addEdge(e, e.getVertex1(), e.getVertex2());
			}
			
			for(Edge<Integer> e: aresta2 ){
				
				tree.removeEdge(e);
				if(e.getVertex1() == edge.getVertex2() )
					e.setVertex1(v);
				if(e.getVertex2() == edge.getVertex2() )
					e.setVertex2(v);
				
				tree.addEdge(e, e.getVertex1(), e.getVertex2());
			}
		 }
		 
		 
		 
		 
		 
		 
		 
		 FRLayout<Integer,Edge<Integer> > graphLayout = new FRLayout<Integer,Edge<Integer>>(tree);
		 //TreeLayout<Integer,Edge<Integer>> graphLayout = new TreeLayout<Integer,Edge<Integer>>(tree);
		 final VisualizationViewer<Integer,Edge<Integer>> vv =  new VisualizationViewer<Integer,Edge<Integer>>(graphLayout);
		 
		 vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
		 vv.getRenderContext().setVertexLabelTransformer(new Transformer<Integer,String>() {
			    public String transform(Integer v) {
			    	if(v < graph.getNumVerteces())
			    		return  String.valueOf(graph.getCustVertex(v));
			    	else
			    		return "?";
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
				if(arg0 == graph.getNumVerteces()){
					return new Ellipse2D.Double(-10,-10,20,20);
				}
				else
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
	     JFrame frame = new JFrame("Arvore geradora (Kruskal)");   
	     frame.setSize(500, 500);
	     frame.setLayout(new BorderLayout());
	     frame.add(panel, BorderLayout.CENTER);
		 frame.add(scaleGrid, BorderLayout.SOUTH); 
		 frame.setVisible(true);
	}
  */
	
	private Edge<Integer>[] getMSTOrderByAttribute(int attr){
		Edge<Integer> mst[] = mtree.getEdgeMST();
		if(attr != -1){
			for(Edge<Integer> edge: mst){
				if(attr == ATTRIBUTE_AREA){
					int value1 = graph.getVertexByIndex(edge.getVertex1()).getArea();
					int value2 = graph.getVertexByIndex(edge.getVertex2()).getArea();
					edge.setWeight( Math.min(value1, value2) );
				}
				else if(attr == ATTRIBUTE_VOLUME){
					int value1 = graph.getVertexByIndex(edge.getVertex1()).getArea() * graph.getVertexByIndex(edge.getVertex1()).getLevel();
					int value2 = graph.getVertexByIndex(edge.getVertex2()).getArea() * graph.getVertexByIndex(edge.getVertex2()).getLevel();
					edge.setWeight( Math.min(value1, value2) );
				}
			}
		}
		Arrays.sort(mst);
		return mst;
	}
	
	/**
	 * Rotina do union-find
	 * @param zPar
	 * @param x
	 * @return
	 */
	private int findRoot(int zPar[], int x) {
		if (zPar[x] == x)
			return x;
		else {
			zPar[x] = findRoot(zPar, zPar[x]);
			return zPar[x];
		}
	}
	
	public NodeTCL getRoot(){
		return root;
	}
	
	  /**
     * Imprime a arvore binaria
     * @param no
     * @param h - tabulacao
     */
    private void printTree(NodeTCL no, int h){
        if (no != null){
        	printTree(no.right, h+2);
            for (int i= 1; i < h; i++)
                System.out.print("     ");
            System.out.println(" ["+ no.level + "; "+ no.attribute + "]");
            
            printTree(no.left, h+2);
        }
    }
    
    public void printTree() {
        System.out.println("Binary partition tree");
        System.out.println(">> Num nodes:" + numNode);
        System.out.println(">> Height of tree:" + heightTree);
        System.out.println("\nTree:\n");
        printTree(this.root, 1);
        System.out.println("\n");
    }

    
    public void computerNodeAttribute(NodeTCL node){
    	if(!node.isLeaf){
	    	node.area += (node.left.area + node.right.area); //area
			node.volume += (node.left.volume + node.right.volume); //volume	
			node.level = ( (node.left.level * node.left.getArea()) + (node.right.level * node.right.getArea()) ) / (node.left.getArea() + node.right.getArea());	
    	}
    }
    
    

	
    
    public void computerNodeLevel(NodeTCL node){
    	if(!node.isLeaf){
    		if(node.left.getArea() > node.right.getArea()){
    			node.level = node.left.level;
			} else if(node.left.getArea() < node.right.getArea()){
				node.level = node.right.level;
			}else{
				node.level = (node.left.level + node.right.level) / 2;
			}
    	}
    }
    
	/**
	 * Calcula a altura da arvore e conta um numero de nos
	 * @param node - root
	 * @param height - altura da raiz
	 */
	public void computerHeightNodes(NodeTCL node, int height){
		node.heightNode = height;
		this.numNode++;
		if(height > heightTree)
			heightTree = height;
		
		NodeTCL nodeLeft = node.left;
		NodeTCL nodeRight = node.right;
		if(nodeLeft != null)
			computerHeightNodes(nodeLeft, height + 1);
		if(nodeRight != null)
			computerHeightNodes(nodeRight, height + 1);
	}
	
	

	
	/**
	 * Type = 1 => area
	 * Type = 2 => volume
	 * Type = 3 => altura do node
	 */
	public GrayScaleImage segmentation(double attributeValue, int type) {
		long ti = System.currentTimeMillis();
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeTCL> fifo = new Queue<NodeTCL>();
		fifo.enqueue(this.root);
		int label = 0;
		boolean flagProcessed[] = new boolean[this.numNode];
		while(!fifo.isEmpty()){
			NodeTCL no = fifo.dequeue();
			if(!flagProcessed[no.id]){
				if(type == 3 && attributeValue > no.attribute){
					label += 1;
					Queue<NodeTCL> fifoPruning = new Queue<NodeTCL>();
					fifoPruning.enqueue(no);	
					while(!fifoPruning.isEmpty()){
						NodeTCL nodePruning = fifoPruning.dequeue();
						flagProcessed[nodePruning.id] = true;
						if(nodePruning.getChildren() != null)
							for(NodeTCL song: nodePruning.getChildren()){ 
								fifoPruning.enqueue(song);	 
							}
						for(Integer p: nodePruning.getPixels()){
							imgOut.setPixel(p, label);
						}
					}
					label += 1;
				}
				else{
					label += 1;
					flagProcessed[no.id] = true;
					for(Integer p: no.getPixels()){
						imgOut.setPixel(p, label);
					}
					if(no.getChildren() != null)
						for(NodeTCL son: no.children){
							fifo.enqueue(son);	 
						}		
				}
				
			}
		}
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [BPT - filtragem]  "+ ((tf - ti) /1000.0)  + "s");
		return imgOut;
	}
	
	
	 public static void main(String[] args) {
			
			byte pixels[] = new byte[] { 
					30, 30, 30, 30, 30, 25, 25, 25, 25, 25, 25,
					30, 31, 31, 30, 30, 25, 25, 25, 25, 25, 25,
					30, 30, 30, 30, 30, 00, 25, 25, 25, 25, 25,
					30, 30, 30, 30, 30, 00, 00, 00, 00, 00, 25,
					50, 50, 50, 30, 30, 00, 00, 00, 00, 00, 00,
					50, 50, 50, 50, 50, 00, 00, 00, 77, 77, 00,
					50, 50, 50, 50, 50, 00, 00, 00, 00, 00, 00
			};
		
			int width = 11;
			int height = 7;
			
			GrayScaleImage img = ImageFactory.createReferenceGrayScaleImage(8, pixels, width, height);;
			img = ImageBuilder.openGrayImage();
			TreeOfCriticalLakes tcl = TreeOfCriticalLakes.getInstance(img, IMorphologicalTreeSegmentation.ATTRIBUTE_AREA);
			//tcl.printTree();
		}
}