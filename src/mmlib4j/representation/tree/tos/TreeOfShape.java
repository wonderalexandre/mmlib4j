package mmlib4j.representation.tree.tos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import mmlib4j.datastruct.Queue;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class TreeOfShape{
	protected NodeToS root;
	protected int width;
	protected int height;
	protected int numNode; 
	protected int heightTree;
	protected AdjacencyRelation adj = AdjacencyRelation.getCircular(1.5);
	protected GrayScaleImage imgInput;
	protected NodeToS []map;
	protected HashSet<NodeToS> listNode;
	protected LinkedList<NodeToS> listLeaves;
	protected BuilderTreeOfShape build;
	
	protected int sup = 255;
	protected int inf = 0;
	protected boolean isExtendedTree;
	
	
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
		computerInforTree(this.root, 0);
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
		computerInforTree(this.root, 0);
		createNodesMap();
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [create tree of shape] "+ ((tf - ti) /1000.0)  + "s");
		}
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
	

	public void computerInforTree(NodeToS node, int height){
		
		node.heightNode = height;
		if(height > heightTree)
			heightTree = height;
		
		if(node != root){
			node.numSiblings = node.parent.children.size();
			if(node.parent.level < node.level)
				node.isNodeMaxtree = true; //maxtree
			else if(node.parent.level > node.level)
				node.isNodeMaxtree = false; //mintree
		}
		
		else{
			node.numSiblings = 0;
			if(node.getLevel() == imgInput.minValue()){
				node.isNodeMaxtree = true;
			}else{
				node.isNodeMaxtree = false;
			}
		}
		
		for(NodeToS son: node.children){
			computerInforTree(son, height + 1);
			
			if(node.isNodeMaxtree != son.isNodeMaxtree)
				node.countHoles++;
			if(son.isLeaf())
				node.numDescendentLeaf += 1; 
			node.numDescendent += son.numDescendent;
			node.numDescendentLeaf += son.numDescendentLeaf;
			node.sumX += son.sumX;
			node.sumY += son.sumY;
			node.area += son.area;
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
		this.isExtendedTree = true;
		this.sup = sup;
		this.inf = inf;
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


	public TreeOfShape getClone(){
		TreeOfShape c = new TreeOfShape(this.build.getClone());
		c.isExtendedTree = this.isExtendedTree;
		c.sup = this.sup;
		c.inf = this.inf;
		for(int p=0; p < this.map.length; p++){
			c.map[p].attributes = (HashMap<Integer, Attribute>) this.map[p].attributes.clone();
			//System.out.println(map[p].attributes);
		}
		if(this.isExtendedTree){
			c.extendedTree();
		}
		return c;
	}
	

	public GrayScaleImage reconstruction(){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
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

