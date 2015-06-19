package mmlib4j.representation.tree;

import java.util.LinkedList;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class InfoPrunedTree {
	
	private NodePrunedTree root;
	private NodePrunedTree map[];
	private int numNode;
	private MorphologicalTreeFiltering tree;
	private LinkedList<NodeLevelSets> listLeaves;

	private int attributeType;
	private double attributeValue;
	
	
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

	public InfoPrunedTree(MorphologicalTreeFiltering tree, NodeLevelSets root, int numNodes, int attributeType, double attributeValue) {
		this.tree = tree;
		this.map = new NodePrunedTree[numNodes];
		this.root = map[root.hashCode()] = new NodePrunedTree(root);
        this.attributeType = attributeType;
        this.attributeValue = attributeValue;
        this.numNode = 1;
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
	
	public MorphologicalTreeFiltering getTree(){
		return tree;
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
	
	public LinkedList<NodeLevelSets> getLeaves(){
		if(listLeaves == null){
			listLeaves = new LinkedList<NodeLevelSets>();
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
		if(tree instanceof ConnectedFilteringByComponentTree){
			return ((ConnectedFilteringByComponentTree) tree).reconstruction(this);
		}
		else if (tree instanceof ConnectedFilteringByTreeOfShape){
			return ((ConnectedFilteringByTreeOfShape) tree).reconstruction(this);
		}
		else{
			return null;
		}
	}
	

	public class NodePrunedTree{
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
