package mmlib4j.representation.tree.mst.bpt;

import mmlib4j.datastruct.Queue;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.graph.rag.RegionAdjcencyGraph;
import mmlib4j.representation.tree.mst.IMorphologicalTreeSegmentation;
import mmlib4j.representation.tree.mst.MorphologicalTreeByKruskal;
import mmlib4j.utils.ImageBuilder;

public class BinaryPartitionTree implements IMorphologicalTreeSegmentation{

	private NodeBPT root;
	private int numNode;
	private int heightTree;
	MorphologicalTreeByKruskal mtree;
	private GrayScaleImage imgInput;
	
	private BinaryPartitionTree(GrayScaleImage img){
		this.imgInput = img;
	}
	

	public static BinaryPartitionTree getInstance(RegionAdjcencyGraph graph){
		long ti = System.currentTimeMillis();
		BinaryPartitionTree instance = new BinaryPartitionTree(graph.getInputImage()); 
		MorphologicalTreeByKruskal mtree = new MorphologicalTreeByKruskal(graph);
		instance.createBinaryPartitionTree(mtree, graph);
		instance.computerHeightNodes(instance.root, 0);
		System.out.println("Height of tree:" + instance.heightTree);
		instance.computerAttribute(instance.root);
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [create binary partition tree] "+ ((tf - ti) /1000.0)  + "s");
		return instance;
	}

	public void createBinaryPartitionTree(MorphologicalTreeByKruskal mtree, RegionAdjcencyGraph graph){
		MorphologicalTreeByKruskal.DisjointSetBPT qBT = mtree.getBinaryPartitionTree();
		NodeBPT nodes[] = new NodeBPT[qBT.size];
		//criando as folhas
		for(int i=0; i < graph.getNumVerteces(); i++){
			nodes[i] = new NodeBPT(i, graph.getCustVertex(i), imgInput, true);
			for(int p: graph.getVertexByIndex(i).getPixels())
				nodes[i].addPixel(p);
			nodes[i].orderTree = nodes[i].level;
			
		}
		
		//criando e lincando os nos da arvore
		for(int son=0; son < qBT.size-1; son++){
			int parent = qBT.parent[son];
			if(nodes[parent] == null){
				nodes[parent] = new NodeBPT(parent, -1, imgInput, false);
				nodes[parent].left = nodes[son];
				nodes[son].parent = nodes[parent];
					
			}else{
				nodes[parent].right = nodes[son];
				nodes[son].parent = nodes[parent];
				nodes[parent].orderTree = mtree.weightNode(parent);
			}
		}
		
		
		boolean ws[] = mtree.watershed();
		for(int i=0; i < ws.length; i++){
			if(ws[i]){
				//Edge<Integer> e = mtree.getEdgeMST(i);
				//nodes[e.getVertex1()].isWatershed = true;
				//nodes[e.getVertex2()].isWatershed = true;
				nodes[i].isWatershed = true;
			}
		}
		
		root = nodes[qBT.size-1];
	}
	

	
	public NodeBPT getRoot(){
		return root;
	}
	
	  /**
     * Imprime a arvore binaria
     * @param no
     * @param h - tabulacao
     */
    private void printTree(NodeBPT no, int h){
        if (no != null){
        	printTree(no.right, h+2);
        	
            for (int i= 1; i < h; i++)
                System.out.print("     ");
            System.out.println(" ["+ no.level + "; "+ no.orderTree +";"+ no.isWatershed +" ]");
            
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


	/**
	 * Metodo utilizado para criar uma instancia da maxtree. 
	 * Os atributos computados sao crescentes
	 * @param img - imagem de entrada
	 * @return Maxtree
	 */
    public void computerAttribute(NodeBPT node){
    	while(node != null){
    		switch (node.aux){
    			case 0: 
    				////////////////////////////
    				//Processamento pre-ordem
    				//aqui
    				////////////////////////////
    				node.aux++;
    				if(node.left != null){ 
    					node = node.left;
    				}
    				break;
    			case 1: 
    				////////////////////////////
    				//Processamento in-ordem
    				//aqui..
    				////////////////////////////
    				node.aux++;
    				if(node.right != null) {
    					node = node.right;
    				}
    				break;
    			case 2:
    				////////////////////////////
    				//Processamento pos-ordem
    				if(node != null) {
    					computerNodeAttribute(node);
    					computerNodeLevel(node);
    				}
    				////////////////////////////
    				
    				node.aux = 0;
    				node = node.parent;
    				break;
    		}
    	}
    }
    
    
    public void computerNodeAttribute(NodeBPT node){
    	if(!node.isLeaf){
	    	node.area += (node.left.area + node.right.area); //area
			node.volume += (node.left.volume + node.right.volume); //volume	
			node.level = ( (node.left.level * node.left.getArea()) + (node.right.level * node.right.getArea()) ) / (node.left.getArea() + node.right.getArea());	
    	}
    }
    
	
    
    public void computerNodeLevel(NodeBPT node){
    	if(!node.isLeaf){
    		//media => Merge order
    		/*
    		int n1 = node.left.getArea();
    		int n2 = node.right.getArea();
    		int mr1 = node.left.level;
    		int mr2 = node.right.level;
    		node.level = (n1*mr1 + n2*mr2) / (n1 + n2);
    		if( !(node.level <= 255 && node.level >= 0) ) System.out.println(node.level);
    		*/
    		 //mediana
    		if(node.left.orderTree > node.right.orderTree){
    			node.level = node.left.level;
			} else if(node.left.orderTree < node.right.orderTree){
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
	public void computerHeightNodes(NodeBPT node, int height){
		node.heightNode = height;
		this.numNode++;
		if(height > heightTree)
			heightTree = height;
		
		NodeBPT nodeLeft = node.left;
		NodeBPT nodeRight = node.right;
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
		Queue<NodeBPT> fifo = new Queue<NodeBPT>();
		fifo.enqueue(this.root);
		int label = 0;
		boolean flagProcessed[] = new boolean[this.numNode];
		while(!fifo.isEmpty()){
			NodeBPT no = fifo.dequeue();
			if(!flagProcessed[no.id]){
				if(type == 3 && attributeValue > no.orderTree){
					label += 1;
					Queue<NodeBPT> fifoPruning = new Queue<NodeBPT>();
					fifoPruning.enqueue(no);	
					while(!fifoPruning.isEmpty()){
						NodeBPT nodePruning = fifoPruning.dequeue();
						flagProcessed[nodePruning.id] = true;
						if(nodePruning.getChildren() != null)
							for(NodeBPT song: nodePruning.getChildren()){ 
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
						for(NodeBPT son: no.children){
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
						30, 30, 30, 30, 30, 25, 25, 25, 25, 25, 25,
						30, 30, 30, 30, 30, 00, 25, 25, 25, 25, 25,
						30, 30, 30, 30, 30, 00, 00, 00, 00, 00, 25,
						50, 50, 50, 30, 30, 00, 00, 00, 00, 00, 00,
						50, 50, 50, 50, 50, 00, 00, 00, 00, 00, 00,
						50, 50, 50, 50, 50, 00, 00, 00, 00, 00, 00
			};
			int width = 11;
			int height = 7;
			GrayScaleImage img = ImageFactory.createReferenceGrayScaleImage(8, pixels, width, height);

		 
		 
		 	//IGrayScaleImage 
		 	img = ImageBuilder.openGrayImage();
			long ti = System.currentTimeMillis();
			BinaryPartitionTree bpt = new BinaryPartitionTree(img);
			bpt.printTree();
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao  "+ ((tf - ti) /1000.0)  + "s");

		}
}
