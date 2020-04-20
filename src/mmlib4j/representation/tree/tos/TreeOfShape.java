package mmlib4j.representation.tree.tos;

import java.util.HashMap;
import java.util.Iterator;

import mmlib4j.datastruct.Queue;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.AbstractImageFactory;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.AbstractMorphologicalTree;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class TreeOfShape extends AbstractMorphologicalTree implements MorphologicalTree{
	
	protected int width;
	protected int height;
	
	protected BuilderTreeOfShape build;
	
	protected int sup = 255;
	protected int inf = 0;
	 	 
	public TreeOfShape(GrayScaleImage img){
		this(img, -1, -1);
	}
	
	public TreeOfShape(BuilderTreeOfShape build){
		long ti = System.currentTimeMillis();
		this.build = build;
		this.width = build.getInputImage().getWidth();
		this.height = build.getInputImage().getHeight();
		this.imgInput = build.getInputImage();
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
	
	public TreeOfShape(GrayScaleImage img, int xInfinito, int yInfinito){
		this(new BuilderTreeOfShapeByUnionFind(img, xInfinito, yInfinito, true));
	}
	
	public void createNodesMap(){
		map = new NodeLevelSets[getWidth()*getHeight()];
		super.createNodesMap();
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
			//node.setArea( node.getArea() + son.getArea() );
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
	

	public int getWidth() {
		return width;
	}


	public int getHeight() {
		return height;
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
	


	public GrayScaleImage reconstructionByDepth(){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(AbstractImageFactory.DEPTH_32BITS, imgInput.getWidth(), imgInput.getHeight());
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

	@Override
	public int getTypeOfTree() {
		return MorphologicalTree.TREE_OF_SHAPE;
	}
}

