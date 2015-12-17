package mmlib4j.representation.tree.componentTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ThreadPoolExecutor;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComponentTree {
	protected NodeCT root;
	protected NodeCT[] map;
	protected HashSet<NodeCT> listNode;
	protected LinkedList<NodeCT> listLeaves;
	
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
		//this.builder = new BuilderComponentTreeByUnionFind(img, adj, isMaxtree);
		this.builder = new BuilderComponentTreeByRegionGrowing(img, adj, isMaxtree);
		this.root = builder.getRoot();
		this.numNode = builder.getNunNode();
		this.map = builder.getMap();
		this.listNode = builder.getListNodes();
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
		for(NodeCT node: c.getListNodes()){
			node.attributes =  (HashMap<Integer, Attribute>) this.getSC( node.getCanonicalPixel() ).attributes.clone();
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
	
	public void computerAdjcencyNodes(){
		long ti = System.currentTimeMillis();
		
		for(NodeCT node: this.listNode){
			node.adjcencyNodes = new SimpleLinkedList<NodeCT>();
		}
		
		int flags[] = new int[listNode.size()];
		Arrays.fill(flags, -1);
		
		for(NodeCT node: listNode){
			for(int p: node.getCanonicalPixels()){
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
	}
	
	
	public void prunning(NodeCT node){
		if(node != root && map[node.getCanonicalPixel()] == node){
			NodeCT parent = node.parent;
			parent.children.remove(node);
			listLeaves = null;
			for(NodeCT no: node.getNodesDescendants()){
				listNode.remove(no);
				numNode--;
				for(int p: no.getCanonicalPixels()){
					parent.addPixel(p);
					map[p] = parent;	
				}
			}
		}
	}
	

	public static void prunning(ComponentTree tree, NodeCT node){
		if(node != tree.root){
			NodeCT parent = node.parent;
			parent.children.remove(node);
			tree.listLeaves = null;
			
			for(NodeCT no: node.getNodesDescendants()){
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
			node.children = new ArrayList<NodeCT>();
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
			NodeCT node = (NodeCT) node_.getInfo();
			for(NodeCT son: node.children){
				if(prunedTree.wasPruned(son)){
					for(int p: son.getPixelsOfCC()){
						imgOut.setPixel(p, node.level);
					}
				}
			}
			for(int p: node.getCanonicalPixels()){
				imgOut.setPixel(p, node.level);
			}
			for(InfoPrunedTree.NodePrunedTree son: node_.getChildren()){
				fifo.enqueue( son );	
			}
			
		}
		return imgOut;
	}
	
	
	public GrayScaleImage reconstruction(){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(this.root);
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			for(int p: no.getCanonicalPixels()){
				imgOut.setPixel(p, no.level);
			}
			
			for(NodeCT son: no.children){
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
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		
		for(NodeCT son: this.root.children){
			fifo.enqueue(son);
		}
		
		if(isMaxtree && root.level > inf){
			this.root = getExtendedBranchOfMaxtree(this.root, inf, this.root.level);
		}
		else if(!isMaxtree && root.level < sup){
			this.root = getExtendedBranchOfMintree(this.root, sup, this.root.level);
		}
		
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
		
			for(NodeCT son: no.children){
				fifo.enqueue(son);
			}
			
			if(isMaxtree){
				getExtendedBranchOfMaxtree(no, no.parent.level+1, no.level);
			}else{
				getExtendedBranchOfMintree(no, no.parent.level-1, no.level);
			}
		}
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [extended tree] "+ ((tf - ti) /1000.0)  + "s");
		createNodesMap();
		this.isExtendedTree = true;
	}
	
	private NodeCT getExtendedBranchOfMaxtree(NodeCT no, int inicio, int fim){
		if(inicio >= fim) return null;
		NodeCT ramoInicio = no.getClone();
		NodeCT ramoFim = ramoInicio;
		
		ramoFim.parent = no.parent;
		if(ramoFim.parent != null){
			ramoFim.parent.children.remove(no);
			ramoFim.parent.children.add(ramoFim);
		}
		ramoFim.level = inicio;
		ramoFim.id = numNode++;
		for(int i=inicio+1; i < fim; i++){
			NodeCT noAtual = no.getClone();
			
			noAtual.parent = ramoFim;
			noAtual.level = i;
			noAtual.id = numNode++;
			
			ramoFim.children.add(noAtual);
			ramoFim = noAtual;
			
			
		}
		ramoFim.children.add(no);
		no.parent = ramoFim;
		
		return ramoInicio;
	}
	
	private NodeCT getExtendedBranchOfMintree(NodeCT no, int inicio, int fim){
		if(inicio <= fim) return null;
		NodeCT ramoInicio = no.getClone();
		NodeCT ramoFim = ramoInicio;
		
		ramoFim.parent = no.parent;
		if(ramoFim.parent != null){
			ramoFim.parent.children.remove(no);
			ramoFim.parent.children.add(ramoFim);
		}
		ramoFim.level = inicio;
		ramoFim.id = numNode++;
		for(int i=inicio-1; i > fim; i--){
			NodeCT noAtual = no.getClone();
			
			noAtual.parent = ramoFim;
			noAtual.level = i;
			noAtual.id = numNode++;
			ramoFim.children.add(noAtual);
			ramoFim = noAtual;
			
			
		}
		ramoFim.children.add(no);
		no.parent = ramoFim;
		
		return ramoInicio;
	}
	
		
	public NodeCT getSC(int p){
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
			map = new NodeCT[imgInput.getSize()];
		listNode = new HashSet<NodeCT>();
		listLeaves = new LinkedList<NodeCT>();
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(this.root);
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			listNode.add(no);
			for(Integer p: no.getCanonicalPixels()){
				map[p] = no;
			}
			for(NodeCT son: no.children){
				fifo.enqueue(son);	 
			}
			if(no.children.isEmpty())
				listLeaves.add(no);	
		}
		
	}
	
	public NodeCT[] getNodesMap(){
		return map;
	}
	
	public void computerInforTree(NodeCT node, int height){
		node.isNodeMaxtree = isMaxtree;
		node.heightNode = height;
		if(height > heightTree)
			heightTree = height;
		
		if(node != root){
			node.numSiblings = node.parent.children.size();
		}
		
		for(NodeCT son: node.children){
			computerInforTree(son, height + 1);
			
			if(son.isLeaf())
				node.numDescendentLeaf += 1; 
			node.numDescendent += son.numDescendent;
			node.numDescendentLeaf += son.numDescendentLeaf;
			node.area += son.area;
		}
	}
	
	
	public HashSet<NodeCT> getListNodes(){
		return listNode;
	}
	
	public LinkedList<NodeCT> getLeaves(){
		if(listLeaves == null){
			listLeaves = new LinkedList<NodeCT>();
			for(NodeCT node: listNode){
				if(node.children.isEmpty())
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
	public NodeCT findNode(int pixel, int level){
		NodeCT node = map[pixel];
		if(isMaxtree){
			while(node.level > level){
				node = node.parent;
			}
		}else{
			while(node.level < level){
				node = node.parent;
			}
		}
		return node;
	}
	
	
	
	public NodeCT getRoot() {
		return root;
	}


	public GrayScaleImage getInputImage(){
		return imgInput;
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
