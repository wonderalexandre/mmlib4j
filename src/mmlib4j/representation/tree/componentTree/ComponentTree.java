package mmlib4j.representation.tree.componentTree;

import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComponentTree {
	protected NodeLevelSets root;
	protected NodeLevelSets[] map;
	protected SimpleLinkedList<NodeLevelSets> listNode;
	protected SimpleLinkedList<NodeLevelSets> listLeaves;
	
	protected int numNode;
	protected int numNodeIdMax;
	protected int heightTree;
	
	protected boolean isMaxtree;
	
	protected AdjacencyRelation adj;
	protected GrayScaleImage imgInput;
	
	public final static int NUM_ATTRIBUTES = 10;
	public final static int NUM_ATTRIBUTES_NC = 2;
	
	
	protected ThreadPoolExecutor pool;
	protected BuilderComponentTree builder;
	
	protected int sup = 255;
	protected int inf = 0;
	protected boolean isExtendedTree;
	
	public ComponentTree(GrayScaleImage img, AdjacencyRelation adj, boolean isMaxtree){
		long ti = System.currentTimeMillis();
		this.imgInput = img;
		this.isMaxtree = isMaxtree;
		this.adj = adj;
		this.builder = new BuilderComponentTreeByUnionFind(img, adj, isMaxtree);
		//this.builder = new BuilderComponentTreeByRegionGrowing(img, adj, isMaxtree);
		this.root = builder.getRoot();
		this.numNode = builder.getNunNode();
		this.map = builder.getMap();
		this.listNode = builder.getListNodes();
		this.numNodeIdMax = builder.getNumNodeIdMax();
		computerInforTree(this.root, 0);
		
		//computerAdjcencyNodes();
		long tf = System.currentTimeMillis();
		if(Utils.debug)
			System.out.println("Tempo de execucao [create component tree] "+ ((tf - ti) /1000.0)  + "s");
	}
	
	public ComponentTree(BuilderComponentTree builder){
		this.builder = builder;
		if(builder instanceof BuilderComponentTreeByUnionFind){
			BuilderComponentTreeByUnionFind builderUF = (BuilderComponentTreeByUnionFind) builder;
			this.imgInput = builderUF.img;
			this.isMaxtree = builderUF.isMaxtree;
			this.adj = new AdjacencyRelation(builderUF.px, builderUF.py);
		}else{
			BuilderComponentTreeByRegionGrowing builderUF = (BuilderComponentTreeByRegionGrowing) builder;
			this.imgInput = builderUF.img;
			this.isMaxtree = builderUF.isMaxtree;
			this.adj = new AdjacencyRelation(builderUF.px, builderUF.py);
		}
		
		this.root = builder.getRoot();
		this.numNode = builder.getNunNode();
		this.numNodeIdMax = builder.getNumNodeIdMax();
		this.map = builder.getMap();
		this.listNode = builder.getListNodes();
		
		computerInforTree(this.root, 0);
		
	}
	
	public ComponentTree getClone(){
		ComponentTree c = new ComponentTree(this.builder.getClone());
		c.isExtendedTree = this.isExtendedTree;
		c.sup = this.sup;
		c.inf = this.inf;
		
		for(NodeLevelSets node: c.getListNodes()){
			node.setAttributes((HashMap<Integer, Attribute>) node.getAttributes().clone());
		}
		if(this.isExtendedTree){
			c.extendedTree();
		}
		return c;
	}
	
	
	public ComponentTree(ComponentTree c){
		this.builder = c.builder;
		this.imgInput = c.imgInput;
		this.isMaxtree = c.isMaxtree;
		this.adj = c.adj;
		this.root = c.root;
		this.numNode = c.numNode;
		this.numNodeIdMax = c.numNodeIdMax;
		this.map = c.map;
		this.listNode = c.listNode;
	}
	protected ComponentTree(){}
	
	/*
	public SimpleLinkedList<NodeLevelSets> computerAdjcencyNodesX(){
		long ti = System.currentTimeMillis();
		
		int flags[] = new int[listNode.size()];
		Arrays.fill(flags, -1);
		
		for(NodeLevelSets node: listNode){
			for(int p: node.getCompactNodePixels()){
				for(int q: adj.getAdjacencyPixels(imgInput, p)){
					if(map[p] != map[q]){
						if(flags[map[q].getId()] != map[p].getId()){
							flags[map[q].getId()] = map[p].getId();
							map[p].addAdjacencyNode( map[q] );
						}
					}
				}	
			}
		}
		flags = null;
		long tf = System.currentTimeMillis();
		if(Utils.debug)
			System.out.println("Tempo de execucao [computerAdjcencyNodes] "+ ((tf - ti) /1000.0)  + "s");
	}*/
	
	
	public void prunning(NodeLevelSets node){
		if(node != root && map[node.getCanonicalPixel()] == node){
			NodeLevelSets parent = node.getParent();
			parent.getChildren().remove(node);
			listLeaves = null;
			for(NodeLevelSets no: node.getNodesDescendants()){
				listNode.remove(no);
				numNode--;
				for(int p: no.getCompactNodePixels()){
					parent.addPixel(p);
					map[p] = parent;	
				}
			}
		}
	}
	

	public static void prunning(ComponentTree tree, NodeLevelSets node){
		if(node != tree.root){
			NodeLevelSets parent = node.getParent();
			parent.getChildren().remove(node);
			tree.listLeaves = null;
			
			for(NodeLevelSets no: node.getNodesDescendants()){				
				tree.listNode.remove(no);
				tree.numNode--;
				for(int p: no.getCompactNodePixels()){
					//parent.addPixel(p);
					parent.getCompactNodePixels().add(p);
					tree.map[p] = parent;	
				}
			}
		}else{
			tree.numNode = 1;
			tree.listNode.clear();
			tree.listNode.add(node);
			node.setChildren( new SimpleLinkedList<NodeLevelSets>() );
			for(int p=0; p < tree.getInputImage().getSize(); p++){
				tree.map[p] = node;
				node.addPixel(p);
			}	
		}
	}
	
	public GrayScaleImage reconstruction(InfoPrunedTree prunedTree){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<InfoPrunedTree.NodePrunedTree> fifo = new Queue<InfoPrunedTree.NodePrunedTree>();
		fifo.enqueue( prunedTree.getRoot() );
		while(!fifo.isEmpty()){
			InfoPrunedTree.NodePrunedTree node_ = fifo.dequeue();
			NodeLevelSets node = node_.getInfo();
			for(NodeLevelSets son: node.getChildren()){
				if(prunedTree.wasPruned(son)){
					for(int p: son.getPixelsOfCC()){
						imgOut.setPixel(p, node.getLevel());
					}
				}
			}
			for(int p: node.getCompactNodePixels()){
				imgOut.setPixel(p, node.getLevel());
			}
			for(InfoPrunedTree.NodePrunedTree son: node_.getChildren()){
				fifo.enqueue( son );	
			}
			
		}
		return imgOut;
	}
	
	/*NodeLevelSets parent = node.getParent();
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
	
	/* Add by gobber */		
	public void mergeParent( NodeLevelSets node ) {
		if( node != root ) {						
			NodeLevelSets parent = node.getParent();
			parent.getChildren().remove( node );			
			listNode.remove( node );
			numNode--;									
			for( int p: node.getCompactNodePixels() ) {				
				parent.getCompactNodePixels().add(p);
				map[p] = parent;				
			}			
			for(NodeLevelSets child : node.getChildren()) {							
				parent.addChildren(child);				
				child.setParent(parent);			
			}			
			/* Update Node Attributes */	
			parent.setNumDescendent(parent.getNumDescendent()-1);
			if(node.isLeaf())
				parent.setNumDescendentLeaf(parent.getNumDescendentLeaf()-1);
			parent.setNumNodeInSameBranch(parent.getNumNodeInSameBranch()-1);
		} else { // Remove root?
			numNode = 1;
			listNode.clear();
			listNode.add(node);
			node.setChildren( new SimpleLinkedList<NodeLevelSets>() );
			for(int p=0; p < getInputImage().getSize(); p++){
				map[p] = node;
				node.addPixel(p);
			}
			/* Update Root Attributes? */	
			//node.setNumDescendent(0);
			//node.setNumDescendentLeaf(0);
			//node.setVolume(getInputImage().getSize() * node.getLevel());
			//node.setNumNodeInSameBranch(1);
		}
	}
	
	public GrayScaleImage reconstruction(){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
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
	

	public void extendedTree(){
		extendedTree(this.inf, this.sup);
	}

	public void extendedTree(int inf, int sup){
		
		long ti = System.currentTimeMillis();
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		
		for(NodeLevelSets son: this.root.getChildren()){
			fifo.enqueue(son);
		}
		
		if(isMaxtree && root.getLevel() > inf){
			this.root = getExtendedBranchOfMaxtree(this.root, inf, this.root.getLevel());
		}
		else if(!isMaxtree && root.getLevel() < sup){
			this.root = getExtendedBranchOfMintree(this.root, sup, this.root.getLevel());
		}
		
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
		
			for(NodeLevelSets son: no.getChildren()){
				fifo.enqueue(son);
			}
			
			if(isMaxtree){
				getExtendedBranchOfMaxtree(no, no.getParent().getLevel()+1, no.getLevel());
			}else{
				getExtendedBranchOfMintree(no, no.getParent().getLevel()-1, no.getLevel());
			}
		}
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [extended tree] "+ ((tf - ti) /1000.0)  + "s");
		createNodesMap();
		this.isExtendedTree = true;
	}
	
	private NodeLevelSets getExtendedBranchOfMaxtree(NodeLevelSets no, int inicio, int fim){
		if(inicio >= fim) return null;
		NodeLevelSets ramoInicio = no.getClone();
		NodeLevelSets ramoFim = ramoInicio;
		
		ramoFim.setParent( no.getParent() );
		if(ramoFim.getParent() != null){
			ramoFim.getParent().getChildren().remove(no);
			ramoFim.getParent().getChildren().add(ramoFim);
		}
		ramoFim.setLevel( inicio );
		ramoFim.setId( numNode++ );
		for(int i=inicio+1; i < fim; i++){
			NodeLevelSets noAtual = no.getClone();
			
			noAtual.setParent( ramoFim );
			noAtual.setLevel( i );
			noAtual.setId( numNode++ );
			
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
		ramoFim.setId( numNode++ );
		for(int i=inicio-1; i > fim; i--){
			NodeLevelSets noAtual = no.getClone();
			
			noAtual.setParent( ramoFim );
			noAtual.setLevel( i ); 
			noAtual.setId( numNode++ );
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
	
	public boolean isMaxtree(){
		return  isMaxtree;
	}
	
	public AdjacencyRelation getAdjacency(){
		return adj;
	}

	protected void createNodesMap(){
		if(map == null)
			map = new NodeLevelSets[imgInput.getSize()];
		listNode = new SimpleLinkedList<NodeLevelSets>();
		listLeaves = new SimpleLinkedList<NodeLevelSets>();
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(this.root);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			listNode.add(no);
			for(Integer p: no.getCompactNodePixels()){
				map[p] = no;
			}
			for(NodeLevelSets son: no.getChildren()){
				fifo.enqueue(son);	 
			}
			if(no.getChildren().isEmpty())
				listLeaves.add(no);	
		}
		
	}
	
	public NodeLevelSets[] getNodesMap(){
		return map;
	}
	
	public void computerInforTree(NodeLevelSets node, int height){
		node.setNodeType( isMaxtree );
		node.setHeightNode( height );
		if(height > heightTree)
			heightTree = height;
		
		for(NodeLevelSets son: node.getChildren()){
			computerInforTree(son, height + 1);
			if(son.isLeaf())
				node.setNumDescendentLeaf(node.getNumDescendentLeaf() + 1); 
			node.setNumDescendent(node.getNumDescendent() + son.getNumDescendent());
			node.setNumDescendentLeaf(node.getNumDescendentLeaf() + son.getNumDescendentLeaf());
			node.setArea(node.getArea() + son.getArea());
			node.setSumX(node.getSumX() + son.getSumX());
			node.setSumY(node.getSumY() + son.getSumY());			
		}				
	}	
	
	public SimpleLinkedList<NodeLevelSets> getListNodes(){
		return listNode;
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

	/**
	 * Encontrar o node contendo o pixel e com nivel de cinza level.
	 * Note que: Maxtree -> menorConjunto[ map[pixel].level ] >= level
	 *           Mintree -> menorConjunto[ map[pixel].level ] <= level
	 * @param pixel
	 * @param level
	 * @return
	 */
	public NodeLevelSets findNode(int pixel, int level){
		NodeLevelSets node = map[pixel];
		if(isMaxtree){
			while(node.getLevel() > level){
				node = node.getParent();
			}
		}else{
			while(node.getLevel() < level){
				node = node.getParent();
			}
		}
		return node;
	}
	
	
	
	public NodeLevelSets getRoot() {
		return root;
	}


	public GrayScaleImage getInputImage(){
		return imgInput;
	}
	
	public int getNumNodeIdMax() {
		return numNodeIdMax;
	}
	
	public int getNumNode() {
		return numNode;
	}
	
	public int getNumLeaves() {
		return getLeaves().size();
	}

	public int getHeightTree() {
		return heightTree;
	}

	public int getValueMin() {
		return imgInput.minValue();
	}

	public int getValueMax() {
		return imgInput.maxValue();
	}
	
}
