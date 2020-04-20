package mmlib4j.representation.tree;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class InfoPrunedTree {
	
	private int numNode;
	private int attributeType;
	private double attributeValue;
	
	private GrayScaleImage imgInput;
	private NodePrunedTree root;
	private NodePrunedTree map[]; //mapping from id to Node. If map[id] = null then Node:ID was pruned 
	private SimpleLinkedList<NodeLevelSets> listLeaves;
	private MorphologicalTree originalTree;
	
	public int getAttributeType() {
		return attributeType;
	}

	public double getAttributeValue() {
		return attributeValue;
	}
	
	public void setAttributeType(int attributeType) {
		this.attributeType = attributeType;
	}

	public void setAttributeValue(double attributeValue) {
		this.attributeValue = attributeValue;
	}

	/*public InfoPrunedTree(NodeLevelSets root, int numNodes, int attributeType, double attributeValue, GrayScaleImage imgInput) {
		this.map = new NodePrunedTree[numNodes];
		this.root = map[root.hashCode()] = new NodePrunedTree(root);
        this.attributeType = attributeType;
        this.attributeValue = attributeValue;
        this.imgInput = imgInput;
        this.numNode = 1;
    }*/
	public InfoPrunedTree(MorphologicalTree tree, int attributeType, double attributeValue) {
		this.map = new NodePrunedTree[tree.getNumNodeIdMax()];
		this.root = map[root.hashCode()] = new NodePrunedTree(tree.getRoot());
        this.attributeType = attributeType;
        this.attributeValue = attributeValue;
        this.imgInput = tree.getInputImage();
        this.numNode = 1;
        this.originalTree = tree;
    }
	
	public MorphologicalTree getOriginalTree() {
		return originalTree;
	}
	
	public void addNodeNotPruned(NodeLevelSets node){
		NodeLevelSets parent = node.getParent();
		if(parent != null){
			this.numNode += 1;
			if(map[node.hashCode()] == null)
				map[node.hashCode()] = new NodePrunedTree(node);
			
			if(map[parent.hashCode()] == null)
				map[parent.hashCode()] = new NodePrunedTree(node.getParent());
			
			map[node.hashCode()].parent = map[parent.hashCode()];
			map[parent.hashCode()].children.add( map[node.hashCode()] );
		}
	}
	
	public NodePrunedTree[] getMap() {
		return map;
	}
	
	public boolean wasPruned(NodeLevelSets node){
		return map[node.hashCode()] == null;
	}
	
	public NodePrunedTree getNodeOfPrunedTree(NodeLevelSets node){
		return map[node.hashCode()];
	}
	
	public NodePrunedTree getRoot(){
		return root;
	}
	
	public int getNumLeaves(){
		return getLeaves().size();
	}
	
	public int getNunNode(){
		return numNode;
	}
	
	public SimpleLinkedList<NodeLevelSets> getLeaves(){
		if(listLeaves == null){
			listLeaves = new SimpleLinkedList<NodeLevelSets>();
			Queue<NodePrunedTree> fifo = new Queue<NodePrunedTree>();
			fifo.enqueue(root);
			while(!fifo.isEmpty()){
				NodePrunedTree node = fifo.dequeue();
				if(node.children.isEmpty()){
					listLeaves.add(node.info);
				}else{
					for(NodePrunedTree son: node.children){
						fifo.enqueue(son);
					}
				}
			}
		}
		return listLeaves;
	}
	
	public GrayScaleImage reconstruction(){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<InfoPrunedTree.NodePrunedTree> fifo = new Queue<InfoPrunedTree.NodePrunedTree>();
		fifo.enqueue(getRoot());
		while(!fifo.isEmpty()){
			InfoPrunedTree.NodePrunedTree node_ = fifo.dequeue();
			NodeLevelSets node = node_.getInfo();
			for(NodeLevelSets son: node.getChildren()){
				if(wasPruned(son)){
					for(int p: son.getPixelsOfCC()){
						imgOut.setPixel(p, node.getLevel());
					}
				}
			}
			for(int p: node.getCompactNodePixels()){
				imgOut.setPixel(p, node.getLevel());
			}
			for(InfoPrunedTree.NodePrunedTree son: node_.getChildren()){
				fifo.enqueue(son);	
			}
		}
		return imgOut;
	}
	
	public class NodePrunedTree {
		NodeLevelSets info;
		NodePrunedTree parent;
	    SimpleLinkedList<NodePrunedTree> children = new SimpleLinkedList<NodePrunedTree>();
	    
	    public NodePrunedTree(NodeLevelSets node){
			info = node;
		}
	    
	    public NodePrunedTree getParent(){
	    	return parent;
	    }
	    
	    public boolean isLeaf(){
	    	return children.isEmpty();
	    }
	    
	    public SimpleLinkedList<NodePrunedTree> getChildren(){
	    	return children;
	    }
	    
	    public NodeLevelSets getInfo(){
	    	return info;
	    }
	    
	}
}
