package mmlib4j.representation.tree.tos;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import mmlib4j.datastruct.Queue;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.pruningStrategy.ComputerExtinctionValueToS;
import mmlib4j.utils.AdjacencyRelation;


/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class TreeOfShape{
	protected NodeToS root;
	protected int width;
	protected int height;
	protected int numNode; 
	protected int heightTree;
	public final static int NUM_ATTRIBUTES = 10;
	protected AdjacencyRelation adj = AdjacencyRelation.getCircular(1.5);
	protected GrayScaleImage imgInput;
	protected NodeToS []map;
	protected HashSet<NodeToS> listNode;
	protected LinkedList<NodeToS> listLeaves;
	protected BuilderTreeOfShape build;
	
	public TreeOfShape(GrayScaleImage img){
		this(img, -1, -1);
	}
	
	protected TreeOfShape(BuilderTreeOfShape build){
		this.build = build;
		this.width = build.getInputImage().getWidth();
		this.height = build.getInputImage().getHeight();
		this.imgInput = build.getInputImage();
		this.root = build.getRoot();
		this.numNode = build.getNumNode();
		createNodesMap();
	}
	
	public TreeOfShape(GrayScaleImage img, int xInfinito, int yInfinito){
		long ti = System.currentTimeMillis();
		this.width = img.getWidth();
		this.height = img.getHeight();
		this.imgInput = img;
		this.build = new BuilderTreeOfShapeByUnionFindParallel(img, xInfinito, yInfinito);
		this.root = build.getRoot();
		this.numNode = build.getNumNode();
		
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [create tree of shape] "+ ((tf - ti) /1000.0)  + "s");
		computerHeightNodes(this.root, 0);
		computerAttribute(this.root);
		createNodesMap();
		tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [create tree of shape + attributes] "+ ((tf - ti) /1000.0)  + "s");
	}
	
	public GrayScaleImage getInputImage(){
		return imgInput;
	}

	public HashSet<NodeToS> getListNodes(){
		return listNode; 
	}
	
	
	public NodeToS[] getNodesMap(){
		return map;
	}
	
	public void createNodesMap(){
		map = new NodeToS[getWidth()*getHeight()];
		listLeaves = new LinkedList<NodeToS>();
		listNode = new HashSet<NodeToS>();
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(this.root);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				map[p] = no;
			}
			listNode.add(no);
			for(NodeToS son: no.children){
				fifo.enqueue(son);	 
			}
			if(no.isLeaf())
				listLeaves.add(no);
		}
		
	}
	
	public LinkedList<NodeToS> getLeaves(){
		if(listLeaves == null){
			listLeaves = new LinkedList<NodeToS>();
			for(NodeToS node: listNode){
				if(node.children.isEmpty())
					listLeaves.add(node);	
			}
		}
		return listLeaves;
	}
	

	public void computerHeightNodes(NodeToS node, int height){
		node.heightNode = height;
		if(node != root){
			if(node.parent.level <= node.level)
				node.isNodeMaxtree = true; //maxtree
			else if(node.parent.level >= node.level)
				node.isNodeMaxtree = false; //mintree
		}
		if(height > heightTree)
			heightTree = height;
		if(node.children != null){
			for(NodeToS son: node.children){
				computerHeightNodes(son, height + 1);
				if(node.isNodeMaxtree != son.isNodeMaxtree)
					node.countHoles++;
			}
		}
	}
	
	public Iterable<NodeToS> getPathToRoot(final NodeToS node){
		return new Iterable<NodeToS>() {
			public Iterator<NodeToS> iterator() {
				return new Iterator<NodeToS>() {
					NodeToS nodeRef = node;
					public boolean hasNext() {
						return nodeRef != null;
					}

					public NodeToS next() {
						NodeToS n = nodeRef;
						nodeRef = nodeRef.parent;
						return n;
					}

					public void remove() { }
					
				};
			}
		};
	}

	public void extendedTree(){
		extendedTree(0, 255);
	}
	
	public void extendedTree(int inf, int sup){
		long ti = System.currentTimeMillis();
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		
		for(NodeToS son: this.root.children){
			fifo.enqueue(son);
		}
		
		if(this.root.isNodeMaxtree && root.level > inf){
			this.root = getExtendedBranchOfMaxtree(this.root, inf, this.root.level);
		}
		else if(!this.root.isNodeMaxtree && root.level < sup){
			this.root = getExtendedBranchOfMintree(this.root, sup, this.root.level);
		}
		
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
		
			for(NodeToS son: no.children){
				fifo.enqueue(son);
			}
			
			if(no.isNodeMaxtree()){
				getExtendedBranchOfMaxtree(no, no.parent.level+1, no.level);
			}else{
				getExtendedBranchOfMintree(no, no.parent.level-1, no.level);
			}
		}
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [extended tree] "+ ((tf - ti) /1000.0)  + "s");
	}
	
	
	private NodeToS getExtendedBranchOfMaxtree(NodeToS no, int inicio, int fim){
		if(inicio >= fim) return null;
		NodeToS ramoInicio = no.getClone();
		NodeToS ramoFim = ramoInicio;
		
		ramoFim.parent = no.parent;
		if(ramoFim.parent != null){
			ramoFim.parent.children.remove(no);
			ramoFim.parent.children.add(ramoFim);
		}
		ramoFim.level = inicio;
		ramoFim.id = ++numNode;
		for(int i=inicio+1; i < fim; i++){
			NodeToS noAtual = no.getClone();
			
			noAtual.parent = ramoFim;
			noAtual.level = i;
			noAtual.id = ++numNode;
			
			ramoFim.children.add(noAtual);
			ramoFim = noAtual;
			
			
		}
		ramoFim.children.add(no);
		no.parent = ramoFim;
		
		return ramoInicio;
	}
	
	private NodeToS getExtendedBranchOfMintree(NodeToS no, int inicio, int fim){
		if(inicio <= fim) return null;
		NodeToS ramoInicio = no.getClone();
		NodeToS ramoFim = ramoInicio;
		
		ramoFim.parent = no.parent;
		if(ramoFim.parent != null){
			ramoFim.parent.children.remove(no);
			ramoFim.parent.children.add(ramoFim);
		}
		ramoFim.level = inicio;
		ramoFim.id = ++numNode;
		for(int i=inicio-1; i > fim; i--){
			NodeToS noAtual = no.getClone();
			
			noAtual.parent = ramoFim;
			noAtual.level = i;
			noAtual.id = ++numNode;
			ramoFim.children.add(noAtual);
			ramoFim = noAtual;
			
			
		}
		ramoFim.children.add(no);
		no.parent = ramoFim;
		
		return ramoInicio;
	}
	
	public NodeToS getSC(int p){
		return map[p]; 
	}
	
	

	/**
	 * Metodo utilizado para criar uma instancia da maxtree. 
	 * Os atributos computados sao crescentes
	 * @param img - imagem de entrada
	 * @return Maxtree
	 */
	public void computerAttribute(NodeToS root){
		root.initAttributes(NUM_ATTRIBUTES);
		root.numDescendent += root.children.size();
		for(NodeToS son: root.children){
			computerAttribute(son);
			root.attributeValue[Attribute.YMAX] = Math.max(root.attributeValue[Attribute.YMAX], son.attributeValue[Attribute.YMAX]); 
			root.attributeValue[Attribute.XMAX] = Math.max(root.attributeValue[Attribute.XMAX], son.attributeValue[Attribute.XMAX]);
			root.attributeValue[Attribute.YMIN] = Math.min(root.attributeValue[Attribute.YMIN], son.attributeValue[Attribute.YMIN]); 
			root.attributeValue[Attribute.XMIN] = Math.min(root.attributeValue[Attribute.XMIN], son.attributeValue[Attribute.XMIN]);
			root.attributeValue[Attribute.AREA] += son.attributeValue[Attribute.AREA]; //area
			root.attributeValue[Attribute.VOLUME] += son.attributeValue[Attribute.VOLUME]; //volume
			root.numDescendent += son.numDescendent;
			root.highest = Math.max(root.highest, son.highest);
			root.lowest = Math.min(root.lowest, son.lowest);
				
			root.sumX += son.sumX;
			root.sumY += son.sumY;
			root.sumYY += son.sumYY;
			root.sumXX += son.sumXX;
			root.sumXY += son.sumXY;
			root.area += son.area;
			
		}
		root.attributeValue[Attribute.WIDTH] = root.attributeValue[Attribute.XMAX] - root.attributeValue[Attribute.XMIN] + 1;
		root.attributeValue[Attribute.HEIGHT] = root.attributeValue[Attribute.YMAX] - root.attributeValue[Attribute.YMIN] + 1;
		root.attributeValue[Attribute.ALTITUDE] = Math.max(root.highest - root.level  + 1, root.level - root.lowest + 1);
		
	}
	


	public NodeToS getRoot() {
		return root;
	}


	public int getWidth() {
		return width;
	}


	public int getHeight() {
		return height;
	}

	public int getNumNode() {
		return numNode;
	}


	public int getHeightTree() {
		return heightTree;
	}

	public GrayScaleImage segmentation(double attributeValue, int type){
		return new ComputerExtinctionValueToS(this).segmentationByKmax((int) attributeValue, type);
	}

	


	public TreeOfShape getClone(){
		TreeOfShape treeClone = new TreeOfShape(this.build.getClone());
		
		treeClone.root.isNodeMaxtree = this.root.isNodeMaxtree;
		treeClone.root.area = this.root.area;
		treeClone.root.countHoles = this.root.countHoles;
		treeClone.root.heightNode = this.root.heightNode;
		if(this.root.attributeValue != null)
			treeClone.root.attributeValue = this.root.attributeValue.clone();
		if(this.root.moment != null)
			treeClone.root.moment = this.root.moment.clone();
		if(this.root.contour != null)
			treeClone.root.contour = this.root.contour.getClone();
		
		for(int p=0; p < imgInput.getSize(); p++){
			if(treeClone.map[p].attributeValue == null){
				NodeToS no = getSC(p);
				treeClone.map[p].isNodeMaxtree = no.isNodeMaxtree; 
				treeClone.map[p].area = no.area;
				treeClone.map[p].countHoles = no.countHoles;
				treeClone.map[p].heightNode = no.heightNode;
				
				if(no.attributeValue != null && treeClone.map[p].attributeValue == null)
					treeClone.map[p].attributeValue = no.attributeValue.clone();
				if(no.moment != null && treeClone.map[p].moment == null)
					treeClone.map[p].moment = no.moment.clone();
				if(no.contour != null && treeClone.map[p].contour == null)
					treeClone.map[p].contour = no.contour.getClone();
			}	
		}
		
		return treeClone;
	}
	

	public GrayScaleImage reconstruction(){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput);
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(this.root);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			for(int p: no.getCanonicalPixels()){
				imgOut.setPixel(p, no.level);
			}
			
			for(NodeToS son: no.children){
				fifo.enqueue(son);	 
			}
			
		}
		return imgOut;
	}
	
	public static void prunning(TreeOfShape tree, NodeToS node){
		if(node != tree.root){
			NodeToS parent = node.parent;
			parent.getChildren().remove(node);
			tree.listLeaves = null;
			
			for(NodeToS no: node.getNodesDescendants()){
				tree.listNode.remove(no);
				tree.numNode--;
				for(int p: no.getCanonicalPixels()){
					parent.addPixel(p);
					tree.map[p] = parent;	
				}
			}
		}else{
			tree.numNode = 1;
			tree.listNode.clear();
			tree.listNode.add(node);
			node.children.clear();
			for(int p=0; p < tree.getInputImage().getSize(); p++){
				tree.map[p] = node;
				node.addPixel(p);
			}
		}
	}
}

