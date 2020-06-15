package mmlib4j.representation.tree;

import java.util.HashMap;
import java.util.Iterator;

import mmlib4j.datastruct.FlattenList;
import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.attribute.Attribute;

/*
 * This tree is useful to perform merges keeping the original tree unchanged.  
 **/
public abstract class InfoMergedTree implements Iterable<InfoMergedTree.NodeMergedTree>, InfoTree {
	
	int numNode;
	NodeMergedTree map[];
	boolean isMerged[];
	MorphologicalTree tree;
	GrayScaleImage img;
	SimpleLinkedList<NodeLevelSets> listLeaves;
			
	public abstract void addNodeNotMerge(NodeLevelSets node);
	
	public abstract void addNodeToMerge(NodeLevelSets node);
	
	public InfoMergedTree(MorphologicalTree tree) {				
		this.tree = tree;
		this.map = new NodeMergedTree[tree.getNumNode()];
		this.numNode = 1;
		this.map[tree.getRoot().getId()] = new NodeMergedTree(tree.getRoot());
		this.img = tree.getInputImage();
		this.isMerged = new boolean[tree.getNumNode()];		
	}
	
	public int getNumNode() {
		return numNode;
	}
	
	public MorphologicalTree getInputTree() {
		return tree;
	}
	

	public int getNumLeaves(){
		return getLeaves().size();
	}
	
	public SimpleLinkedList<NodeLevelSets> getLeaves(){
		if(listLeaves == null){
			listLeaves = new SimpleLinkedList<NodeLevelSets>();
			Queue<NodeMergedTree> fifo = new Queue<NodeMergedTree>();
			fifo.enqueue(getRoot());
			while(!fifo.isEmpty()){
				NodeMergedTree node = fifo.dequeue();
				if(node.children.isEmpty()){
					listLeaves.add(node.info);
				}else{
					for(NodeMergedTree son: node.children){
						fifo.enqueue(son);
					}
				}
			}
		}
		return listLeaves;
	}
	
	public void updateNodeToMergeAll(SimpleLinkedList<NodeLevelSets> nodes) {
		for(NodeLevelSets node : nodes) {
			updateNodeToMerge(node);
		}
	}
	
	public void updateNodeToMerge(NodeMergedTree node_) {
		updateNodeToMerge(node_.info);
	}
	
	public void updateNodeToMerge(NodeLevelSets node) {				
		this.numNode--;
		NodeLevelSets parent = map[node.getId()].parent.info;	
		NodeMergedTree parentM = map[parent.getId()];
		isMerged[node.getId()] = true;			
		
		// Remove it from parentM
		parentM.getChildren().remove(map[node.getId()]);
		
		// Join compact node pixels		
		parentM.getCompactNodePixels().addAll(map[node.getId()].getCompactNodePixels());
		
		// Pass the children to parentM
		for(NodeMergedTree child_ : map[node.getId()].getChildren()) {
			parentM.children.add(child_);
			map[child_.getId()].parent = map[parentM.getId()];
		}
				
		// This makes a mapping from a removed node and its merged representation.
		map[node.getId()] = parentM;		
	}
	
	public GrayScaleImage reconstruction() {		
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());		
		/*int cont = 0;
		int numWrongNodes = 0;*/						
		for(NodeMergedTree node_ : this) {							
			for(int p: node_.getCompactNodePixels()){
				imgOut.setPixel(p, node_.getLevel());
			}			
			/*if(isMerged[node_.getId()]) 
				numWrongNodes++;			
			cont++;*/
		}		
		/*System.out.println("numNodes: " + cont);
		System.out.println("Real numNode: " + numNode);
		System.out.println("Wrong nodes: " + numWrongNodes);*/		
		return imgOut;		
	}
	
	public void updateLevels(int[] offset) {				
		for(NodeMergedTree node_ : skipRoot()) {
			node_.addOffset(offset[node_.getId()]);
		}
	}
	
	public Iterable<NodeMergedTree> skipRoot() {		
		final Queue<NodeMergedTree> queue = new Queue<InfoMergedTree.NodeMergedTree>();
		for(NodeMergedTree node : getRoot().getChildren()) {
			queue.enqueue(node);
		}
		return new Iterable<NodeMergedTree> () {
			public Iterator<NodeMergedTree> iterator() {
				return new Iterator<NodeMergedTree> () {
					NodeMergedTree node_;
					public boolean hasNext() {
						return !queue.isEmpty();
					}
					public NodeMergedTree next() {
						node_ = queue.dequeue();				
						for(NodeMergedTree child_ : node_.getChildren())
							queue.enqueue(child_);				
						return node_;
					}					
					public void remove() {
						updateNodeToMerge(node_);
					}
					
				};
			}			
		};
	}
	
	@Override
	public Iterator<NodeMergedTree> iterator() {
		final Queue<NodeMergedTree> queue = new Queue<InfoMergedTree.NodeMergedTree>();
		queue.enqueue(getRoot());
		return new Iterator<NodeMergedTree>() {
			NodeMergedTree node_;
			@Override
			public boolean hasNext() {
				return !queue.isEmpty();
			}
			@Override
			public NodeMergedTree next() {
				node_ = queue.dequeue();				
				for(NodeMergedTree child : node_.getChildren())
					queue.enqueue(child);				
				return node_;
			}			
			public void remove() {
				updateNodeToMerge(node_);
			}
		};
	}
	
	public double getAttribute(NodeMergedTree node_, int type) {
		if(node_.getAttributes().containsKey(type))
			return node_.getAttributeValue(type);
		else
			return node_.getInfo().getAttributeValue(type);
	}
	
	public NodeMergedTree[] getMap() {
		return map;
	}
	
	public NodeMergedTree getRoot() {
		return map[0];
	}
	
	public class NodeMergedTree {		
				
		SimpleLinkedList<NodeMergedTree> children = new SimpleLinkedList<NodeMergedTree>();		
		FlattenList<Integer> pixels = new FlattenList<Integer>();				
		HashMap<Integer, Attribute> attributes;
		boolean isAttrModified = false;
		NodeMergedTree parent;
		NodeLevelSets info;		
		int offset = 0;
		
		public NodeMergedTree(NodeLevelSets info) {
			this.info = info;
			this.attributes = info.getAttributes();
			this.pixels.add(info.getCompactNodePixels());
		}
		
		public FlattenList<Integer> getCompactNodePixels(){
			return pixels;
		}
		
		public void setAttributes(HashMap<Integer, Attribute> attributes) {
			this.attributes = attributes;
		}
		
		public HashMap<Integer, Attribute> getAttributes(){
			return attributes;
		} 
		
		public void addAttribute(int key, Attribute attr) {
			attributes.put(key, attr);
		}
		
		public double getAttributeValue(int key) {
			return attributes.get(key).value;
		}
		
		public void setIsAttrModified(boolean isAttrModified) {
			this.isAttrModified = isAttrModified;
		}
		
		public boolean isAttrModified() {
			return isAttrModified;
		}
			
		public int hashCode() {
			return info.getId();
		}
		
		public void addOffset(int offset) {
			this.offset += offset;
		}
		
		public int getLevel() {
			return info.getLevel() + offset;
		}
		
		public int getId() {
			return info.getId();
		}
		
		public NodeLevelSets getInfo() {
			return info;
		}
		
		public NodeMergedTree getParent() {
			return parent;
		}
		
		public SimpleLinkedList<NodeMergedTree> getChildren(){
	    	return children;
	    }
		
		public Iterable<NodeMergedTree> getPathToRoot() {		
			final NodeMergedTree node = this;
			return new Iterable<NodeMergedTree>() {
				public Iterator<NodeMergedTree> iterator() {
					return new Iterator<NodeMergedTree>() {
						NodeMergedTree nodeRef = node;
						public boolean hasNext() {
							return nodeRef != null;
						}

						public NodeMergedTree next() {
							NodeMergedTree n = nodeRef;
							nodeRef = nodeRef.getParent();
							return n;
						}

						public void remove() { }
						
					};
				}
			};
		}
		
		public Iterable<NodeMergedTree> getNodesDescendants(){
			final Queue<NodeMergedTree> fifo = new Queue<NodeMergedTree>();
			fifo.enqueue(this);
			
			return new Iterable<NodeMergedTree>() {
				public Iterator<NodeMergedTree> iterator() {
					return new Iterator<NodeMergedTree>() {
						private NodeMergedTree currentNode = nextNode(); 
						public boolean hasNext() {
							return currentNode != null;
						}
						public NodeMergedTree next() {
							NodeMergedTree tmp = currentNode;
							currentNode = nextNode(); 
							return tmp;
						}
						public void remove() {}
						private NodeMergedTree nextNode(){
							if(!fifo.isEmpty()){
								NodeMergedTree no = fifo.dequeue();
								for(NodeMergedTree son: no.getChildren()){
									fifo.enqueue(son);
								}
								return no;
							}
							return null;
						}
					};
				}
			};
			
		}
		
	}
	
}
