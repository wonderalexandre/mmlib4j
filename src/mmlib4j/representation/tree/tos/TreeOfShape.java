package mmlib4j.representation.tree.tos;

import java.util.HashMap;
import java.util.Iterator;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.AbstractImageFactory;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class TreeOfShape{
	protected NodeLevelSets root;
	protected int width;
	protected int height;
	protected int numNode; 
	protected int numNodeIdMax;
	protected int heightTree;
	protected AdjacencyRelation adj = AdjacencyRelation.getCircular(1.5);
	protected GrayScaleImage imgInput;
	protected NodeLevelSets []map;
	protected SimpleLinkedList<NodeLevelSets> listNode;
	protected SimpleLinkedList<NodeLevelSets> listLeaves;
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
		//this.build = new BuilderTreeOfShapeByUnionFindParallel(img, xInfinito, yInfinito);
		this.build = new BuilderTreeOfShapeByUnionFind(img, xInfinito, yInfinito, true);		
		this.root = build.getRoot();
		this.numNode = build.getNumNode();
		this.numNodeIdMax = build.getNumNodeIdMax();
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

	public SimpleLinkedList<NodeLevelSets> getListNodes(){
		return listNode; 
	}
	
	
	public NodeLevelSets[] getNodesMap(){
		return map;
	}
	
	public void createNodesMap(){
		map = new NodeLevelSets[getWidth()*getHeight()];
		listLeaves = new SimpleLinkedList<NodeLevelSets>();
		listNode = new SimpleLinkedList<NodeLevelSets>();
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(this.root);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCompactNodePixels()){
				map[p] = no;
			}
			listNode.add(no);
			for(NodeLevelSets son: no.getChildren()){
				fifo.enqueue(son);	 
			}
			if(no.isLeaf())
				listLeaves.add(no);
		}
		
	}
	
	public SimpleLinkedList<NodeLevelSets> getLeaves(){
		if(listLeaves == null){
			listLeaves = new SimpleLinkedList<NodeLevelSets>();
			for(NodeLevelSets node: listNode){
				if(node.getChildren().isEmpty())
					listLeaves.add(node);	
			}
		}
		return listLeaves;
	}
	

	public void computerInforTree(NodeLevelSets node, int height){
		
		node.setHeightNode( height );
		if(height > heightTree)
			heightTree = height;
		
		if(node != root){
			if(node.getParent().getLevel() < node.getLevel())
				node.setNodeType( true ); //maxtree
			else if(node.getParent().getLevel() > node.getLevel())
				node.setNodeType( false );//mintree
		}
		else{
			if(node.getLevel() == imgInput.minValue()){
				node.setNodeType( true ); //maxtree
			}else{
				node.setNodeType( false );//mintree
			}
		}
		
		for(NodeLevelSets son: node.getChildren()){
			computerInforTree(son, height + 1);
			
			if(son.isLeaf())
				node.setNumDescendentLeaf( node.getNumDescendentLeaf() + 1 ); 
			node.setNumDescendent(node.getNumDescendent() + son.getNumDescendent());
			node.setNumDescendentLeaf(node.getNumDescendentLeaf() + son.getNumDescendentLeaf());
			node.setSumX( node.getSumX() + son.getSumX() );
			node.setSumY( node.getSumY() + son.getSumY() );
			node.setArea( node.getArea() + son.getArea() );
		}
		
	}
	
	public Iterable<NodeLevelSets> getPathToRoot(final NodeLevelSets node){
		return new Iterable<NodeLevelSets>() {
			public Iterator<NodeLevelSets> iterator() {
				return new Iterator<NodeLevelSets>() {
					NodeLevelSets nodeRef = node;
					public boolean hasNext() {
						return nodeRef != null;
					}

					public NodeLevelSets next() {
						NodeLevelSets n = nodeRef;
						nodeRef = nodeRef.getParent();
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
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		this.isExtendedTree = true;
		this.sup = sup;
		this.inf = inf;
		for(NodeLevelSets son: this.root.getChildren()){
			fifo.enqueue(son);
		}
		
		if(this.root.isNodeMaxtree() && root.getLevel() > inf){
			this.root = getExtendedBranchOfMaxtree(this.root, inf, this.root.getLevel());
		}
		else if(!this.root.isNodeMaxtree() && root.getLevel() < sup){
			this.root = getExtendedBranchOfMintree(this.root, sup, this.root.getLevel());
		}
		
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
		
			for(NodeLevelSets son: no.getChildren()){
				fifo.enqueue(son);
			}
			
			if(no.isNodeMaxtree()){
				getExtendedBranchOfMaxtree(no, no.getParent().getLevel()+1, no.getLevel());
			}else{
				getExtendedBranchOfMintree(no, no.getParent().getLevel()-1, no.getLevel());
			}
		}
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [extended tree] "+ ((tf - ti) /1000.0)  + "s");
	}
	
	
	private NodeLevelSets getExtendedBranchOfMaxtree(NodeLevelSets no, int inicio, int fim){
		if(inicio >= fim) return null;
		NodeLevelSets ramoInicio = no.getClone();
		NodeLevelSets ramoFim = ramoInicio;
		
		ramoFim.setParent( no.getParent() );
		if(ramoFim.getParent() != null){
			ramoFim.getParent().getChildren().remove(no);
			ramoFim.getParent().addChildren(ramoFim);
		}
		ramoFim.setLevel( inicio );
		ramoFim.setId( ++numNode );
		for(int i=inicio+1; i < fim; i++){
			NodeLevelSets noAtual = no.getClone();
			
			noAtual.setParent( ramoFim );
			noAtual.setLevel( i );
			noAtual.setId( ++numNode );
			
			ramoFim.addChildren(noAtual);
			ramoFim = noAtual;
			
			
		}
		ramoFim.addChildren(no);
		no.setParent( ramoFim );
		
		return ramoInicio;
	}
	
	private NodeLevelSets getExtendedBranchOfMintree(NodeLevelSets no, int inicio, int fim){
		if(inicio <= fim) return null;
		NodeLevelSets ramoInicio = no.getClone();
		NodeLevelSets ramoFim = ramoInicio;
		
		ramoFim.setParent( no.getParent() );
		if(ramoFim.getParent() != null){
			ramoFim.getParent().getChildren().remove(no);
			ramoFim.getParent().addChildren(ramoFim);
		}
		ramoFim.setLevel( inicio );
		ramoFim.setId( ++numNode );
		for(int i=inicio-1; i > fim; i--){
			NodeLevelSets noAtual = no.getClone();
			
			noAtual.setParent( ramoFim );
			noAtual.setLevel( i );
			noAtual.setId( ++numNode );
			ramoFim.addChildren(noAtual);
			ramoFim = noAtual;
			
			
		}
		ramoFim.addChildren(no);
		no.setParent( ramoFim );
		
		return ramoInicio;
	}
	
	public NodeLevelSets getSC(int p){
		return map[p]; 
	}
	
	
	public NodeLevelSets getRoot() {
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
			c.map[p].setAttributes( (HashMap<Integer, Attribute>) this.map[p].getAttributes().clone() );
			//System.out.println(map[p].attributes);
		}
		if(this.isExtendedTree){
			c.extendedTree();
		}
		return c;
	}
	

	public GrayScaleImage reconstruction(){
		GrayScaleImage imgOut = AbstractImageFactory.instance.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(this.root);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(int p: no.getCompactNodePixels()){
				imgOut.setPixel(p, no.getLevel());
			}
			
			for(NodeLevelSets son: no.getChildren()){
				fifo.enqueue(son);	 
			}
			
		}
		return imgOut;
	}
	

	public GrayScaleImage reconstructionByDepth(){
		GrayScaleImage imgOut = AbstractImageFactory.instance.createGrayScaleImage(AbstractImageFactory.DEPTH_32BITS, imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(this.root);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(int p: no.getCompactNodePixels()){
				imgOut.setPixel(p, no.getHeightNode());
			}
			
			for(NodeLevelSets son: no.getChildren()){
				fifo.enqueue(son);	 
			}
			
		}
		return imgOut;
	}
		
	/* Add by gobber */	
	public void mergeParent( NodeLevelSets node ) {
		if( node != root ) {						
			if(node.getLevel() == 72)
				System.out.println(node.getId());
			NodeLevelSets parent = node.getParent();
			parent.getChildren().remove(node);			
			listNode.remove(node);
			numNode--;			
			for(int p: node.getCompactNodePixels()) {				
				//parent.getCompactNodePixels().add(p);
				parent.addPixel(p);
				map[p] = parent;				
			}			
			for(NodeLevelSets child : node.getChildren()) {							
				parent.addChildren(child);				
				child.setParent(parent);			
			}			
			/* update attributes */									
			node = null;			
		}
	}
	
	public static void prunning(TreeOfShape tree, NodeLevelSets node){
		if(node != tree.root){
			NodeLevelSets parent = node.getParent();
			parent.getChildren().remove(node);
			tree.listLeaves = null;
			
			for(NodeLevelSets no: node.getNodesDescendants()){
				tree.listNode.remove(no);
				tree.numNode--;
				for(int p: no.getCompactNodePixels()){
					parent.addPixel(p);
					tree.map[p] = parent;	
				}
			}
		}else{
			tree.numNode = 1;
			tree.listNode.clear();
			tree.listNode.add(node);
			node.getChildren().clear();
			for(int p=0; p < tree.getInputImage().getSize(); p++){
				tree.map[p] = node;
				node.addPixel(p);
			}
		}
	}
}

