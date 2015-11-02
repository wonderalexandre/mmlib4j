package mmlib4j.representation.tree.mst.alphaTree;

import java.util.LinkedList;

import mmlib4j.datastruct.Queue;
import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.graph.pixels.ImageGraph;
import mmlib4j.representation.graph.rag.RegionAdjcencyGraph;
import mmlib4j.representation.tree.mst.IMorphologicalTreeSegmentation;
import mmlib4j.representation.tree.mst.MorphologicalTreeByKruskal;
import mmlib4j.utils.ImageBuilder;

public class AlphaTree implements IMorphologicalTreeSegmentation{

	private NodeAlphaTree root;
	private int numNode;
	private int heightTree;
	GrayScaleImage imgInput;
	
	
	private AlphaTree(GrayScaleImage img){
		this.imgInput = img;
	}
	
	public static AlphaTree getInstance(RegionAdjcencyGraph graph){
		long ti = System.currentTimeMillis();
		AlphaTree instance = new AlphaTree(graph.getInputImage()); 
		MorphologicalTreeByKruskal mtree = new MorphologicalTreeByKruskal(graph);
		instance.createTree(mtree, graph);
		instance.computerHeightNodes(instance.root, 0);
		System.out.println("Height of tree:" + instance.heightTree);
		instance.computerAttribute(instance.root);
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [create AlphaTree] "+ ((tf - ti) /1000.0)  + "s");
		return instance;
	}

	public static AlphaTree getInstance(ImageGraph graph){
		long ti = System.currentTimeMillis();
		AlphaTree instance = new AlphaTree(graph.getInputImage()); 
		MorphologicalTreeByKruskal mtree = new MorphologicalTreeByKruskal(graph);
		instance.createTree(mtree, graph);
		instance.computerHeightNodes(instance.root, 0);
		System.out.println("Height of tree:" + instance.heightTree);
		instance.computerAttribute(instance.root);
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [create AlphaTree] "+ ((tf - ti) /1000.0)  + "s");
		return instance;
	}

	public void createTree(MorphologicalTreeByKruskal mtree, RegionAdjcencyGraph graph){
		MorphologicalTreeByKruskal.DisjointSetBPT qBT = mtree.canonizeBT();
		NodeAlphaTree nodes[] = new NodeAlphaTree[qBT.size];
		numNode = qBT.size;
		//criando as folhas
		for(int i=0; i < graph.getNumVerteces(); i++){
			nodes[i] = new NodeAlphaTree(i, graph.getCustVertex(i), imgInput, true);
			nodes[i].alpha = 0;
			for(int p: graph.getVertexByIndex(i).getPixels())
				nodes[i].addPixel(p);
		}
		
		//criando e lincando os nos da arvore
		for(int son=0; son < qBT.size-1; son++){
			int parent = qBT.parent[son];
			if(nodes[parent] == null){
				nodes[parent] = new NodeAlphaTree(parent, -1, imgInput, false);
				nodes[parent].children = new LinkedList<NodeAlphaTree>();
				nodes[parent].alpha = mtree.weightNode(parent); 
			}
			nodes[parent].children.add(nodes[son]);		
			nodes[son].parent = nodes[parent];
			
		}
		root = nodes[qBT.size-1];
	}
	
	public void createTree(MorphologicalTreeByKruskal mtree, ImageGraph graph){
		MorphologicalTreeByKruskal.DisjointSetBPT qBT = mtree.canonizeBT();
		NodeAlphaTree nodes[] = new NodeAlphaTree[qBT.size];
		
		//criando as folhas
		for(int i=0; i < graph.getNumVerteces(); i++){
			nodes[i] = new NodeAlphaTree(i, graph.getCustVertex(i), imgInput, true);
			nodes[i].addPixel(i);
			nodes[i].alpha = 0;
		}
		
		//criando e lincando os nos da arvore
		for(int son=0; son < qBT.size-1; son++){
			int parent = qBT.parent[son];
			if(nodes[parent] == null){
				nodes[parent] = new NodeAlphaTree(parent, -1, imgInput, false);
				nodes[parent].children = new LinkedList<NodeAlphaTree>();
				nodes[parent].alpha = mtree.weightNode(parent);
			}
			nodes[parent].children.add(nodes[son]);		
			nodes[son].parent = nodes[parent];
		}
		root = nodes[qBT.size-1];
	}
	
	public NodeAlphaTree getRoot(){
		return root;
	}
	
    
    public void computerNodeLevel(NodeAlphaTree node){
    	if(!node.isLeaf){
    		int area= 0;
    		for(NodeAlphaTree son: node.children){
    			computerNodeLevel(son);
    			if(son.getArea() > area){
    				node.level = son.level;
    				area = son.getArea();
    			}
    		}
    	}
    }
	
	  /**
     * Imprime a arvore binaria
     * @param no
     * @param h - tabulacao
     */
    private void printTree(NodeAlphaTree no, int h){
        if (no != null){
        	if(no.children != null)
        		printTree(no.children.get(0), h+2);
            for (int i= 1; i < h; i++)
                System.out.print("     ");
            System.out.println(" ["+ no.level + "; "+ no.alpha +"; "+ no.getArea() +"]");
            if(no.children != null)
            	for(int i=1; i < no.children.size(); i++)
            		printTree(no.children.get(i), h+2);
        }
    }
    
    public void printTree() {
        System.out.println("Alpha tree");
        System.out.println(">> Num nodes:" + numNode);
        System.out.println(">> Height of tree:" + heightTree);
        System.out.println("\nTree:\n");
        printTree(this.root, 1);
        System.out.println("\n");
    }


	
    
	/**
	 * Calcula a altura da arvore e conta um numero de nos
	 * @param node - root
	 * @param height - altura da raiz
	 */
	public void computerHeightNodes(NodeAlphaTree node, int height){
		node.heightNode = height;
		if(height > heightTree)
			heightTree = height;
		if(node.children != null){
			for(NodeAlphaTree son: node.children){
				computerHeightNodes(son, height + 1);
			}
		}
	}
	

	/**
	 * Metodo utilizado para criar uma instancia da maxtree. 
	 * Os atributos computados sao crescentes
	 * @param img - imagem de entrada
	 * @return Maxtree
	 */
	public void computerAttribute(NodeAlphaTree root){
		if(root.children != null){ //computa os atributos para os filhos
			int count = 0;
			int level = 0;
			for(NodeAlphaTree son: root.children){
				computerAttribute(son);
				root.area += son.area; //area
				root.volume += son.volume; //volume
				
				count += son.getArea();
				level += son.getArea() * son.level;
			}
			if(level != 0)
				root.level = level / count;
		}
		
	}
	
	
	/**
	 * Type = 1 => area
	 * Type = 2 => volume
	 * Type = 3 => altura do node
	 */
	public GrayScaleImage segmentation(double attributeValue, int type) {
		long ti = System.currentTimeMillis();
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeAlphaTree> fifo = new Queue<NodeAlphaTree>();
		fifo.enqueue(this.root);
		int label = 0;
		boolean flagProcessed[] = new boolean[this.numNode];
		while(!fifo.isEmpty()){
			NodeAlphaTree no = fifo.dequeue();
			if(!flagProcessed[no.id]){
				if(type == 3 && attributeValue > no.alpha){
					label += 1;
					Queue<NodeAlphaTree> fifoPruning = new Queue<NodeAlphaTree>();
					fifoPruning.enqueue(no);	
					while(!fifoPruning.isEmpty()){
						NodeAlphaTree nodePruning = fifoPruning.dequeue();
						flagProcessed[nodePruning.id] = true;
						if(nodePruning.children != null)
							for(NodeAlphaTree song: nodePruning.children){ 
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
					if(no.children != null)
						for(NodeAlphaTree son: no.children){
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
						30, 30, 30, 30, 25, 25,
						30, 28, 28, 30, 25, 25,
						28, 28, 28, 40, 40, 25,
						26, 26, 26, 40, 40, 40,
						80, 80, 80, 40, 40, 40,
						80, 80, 80, 80, 40, 40,
			}; 
			int width = 6;
			int height = 6;
			//GrayScaleImage img = ImageFactory.createReferenceGrayScaleImage(8, pixels, width, height);
			GrayScaleImage img = ImageBuilder.openGrayImage();
			long ti = System.currentTimeMillis();
			AlphaTree alphaTree = AlphaTree.getInstance(RegionAdjcencyGraph.getRAGByFlatzone(img));
			alphaTree.printTree();
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao  "+ ((tf - ti) /1000.0)  + "s");
			WindowImages.show( alphaTree.segmentation(5, 3).randomColor() );
		}

}
