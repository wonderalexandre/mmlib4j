package mmlib4j.representation.tree;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;

public abstract class AbstractMorphologicalTree implements MorphologicalTree{

	protected NodeLevelSets root;
	protected NodeLevelSets[] map;
	protected SimpleLinkedList<NodeLevelSets> listNode;
	protected SimpleLinkedList<NodeLevelSets> listLeaves;
	
	protected int numNode;
	protected int numNodeIdMax;
	protected int heightTree;
	protected boolean isExtendedTree;
	
	protected GrayScaleImage imgInput;
	
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
	
	
	public void prunning(NodeLevelSets node){
		if(node != getRoot()){
			NodeLevelSets parent = node.getParent();
			parent.getChildren().remove(node);
			listLeaves = null;
			
			for(NodeLevelSets no: node.getNodesDescendants()){				
				listNode.remove(no);
				numNode--;
				parent.getCompactNodePixels().addAll(no.getCompactNodePixels());
				for(int p: no.getCompactNodePixels()){
					map[p] = parent;	
				}
			}
		}else{
			numNode = 1;
			listNode.clear();
			listNode.add(node);
			node.setChildren( new SimpleLinkedList<NodeLevelSets>() );
			for(int p=0; p < getInputImage().getSize(); p++){
				map[p] = node;
				node.addPixel(p);
			}	
		}
	}
	
	/* Add by gobber */		
	public void mergeParent( NodeLevelSets node ) {
		if( node != root ) {	
			NodeLevelSets parent = node.getParent();
			parent.getChildren().remove( node );			
			//listNode.remove( node );
			numNode--;									
			
			parent.getCompactNodePixels().addAll(node.getCompactNodePixels());
			for( int p: node.getCompactNodePixels() ) {				
				//parent.getCompactNodePixels().add(p);
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
			node.setNumDescendent(0);
			node.setNumDescendentLeaf(0);
			node.setVolume(getInputImage().getSize() * node.getLevel());
			node.setNumNodeInSameBranch(1);
		}
	}
	
	
	public NodeLevelSets getSC(int p){
		return map[p];
	}
	
	public NodeLevelSets[] getNodesMap(){
		return map;
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
	
}
