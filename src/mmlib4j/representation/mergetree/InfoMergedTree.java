package mmlib4j.representation.mergetree;

import java.util.HashMap;
import java.util.Iterator;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.NodeCT;

/*
 * This tree is useful to perform merges keeping the original tree unchanged.  
 **/
public abstract class InfoMergedTree implements Iterable<InfoMergedTree.NodeMergedTree> {
	
	int numNode;
	NodeMergedTree map[];
	boolean isMerged[];
	GrayScaleImage img;
	
	public abstract void addNodeNotMerge(NodeLevelSets node);
	
	public abstract void addNodeToMerge(NodeLevelSets node);

	public InfoMergedTree(NodeLevelSets root, int numNodes, GrayScaleImage img) {				
		this.map = new NodeMergedTree[numNodes];
		this.numNode = 1;
		this.map[root.getId()] = new NodeMergedTree(root, true);
		this.img = img;
		this.isMerged = new boolean[numNodes];		
	}
	
	public int getNumNode() {
		return numNode;
	}
	
	NodeLevelSets allocateNewNode(NodeMergedTree node_, NodeLevelSets node) {
		NodeLevelSets newNode = new NodeCT(node.isNodeMaxtree(), node.getId(), img, node.getCanonicalPixel()); 								
		newNode.setCompactNodePixels(node_.info.getCompactNodePixels().copy());
		map[node.getId()].info = newNode;
		map[node.getId()].fakeNode = false;
		map[node.getId()].attributes = node.getAttributes();
		return newNode;
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
		
		// When the parent is fake it must be copied to preserve it in original tree
		if(parentM.fakeNode) {	
			allocateNewNode(parentM, parent);			
		}								
		
		// Remove it from parentM
		parentM.getChildren().remove(map[node.getId()]);
		
		// Join compact node pixels	
		if(map[node.getId()].fakeNode)
			parentM.info.getCompactNodePixels().addAll(map[node.getId()].info.getCompactNodePixels().copy());
		else
			parentM.info.getCompactNodePixels().addAll(map[node.getId()].info.getCompactNodePixels());
		
		// Pass the children to parentM
		for(NodeMergedTree child_ : map[node.getId()].getChildren()) {
			parentM.children.add(child_);
			map[child_.getId()].parent = map[parentM.getId()];
		}
				
		// This makes a mapping from a removed node and its merged representation.
		map[node.getId()] = parentM;		
	}
	
	public GrayScaleImage reconstruction() {		
		GrayScaleImage imgOut = ImageFactory.instance.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());		
		//int cont = 0;
		//int numWrongNodes = 0;				
		
		for(NodeMergedTree node_ : this) {			
			NodeLevelSets node = node_.getInfo();						
			for(int p: node.getCompactNodePixels()){
				imgOut.setPixel(p, node.getLevel());
			}			
			/*if(isMerged[node.getId()]) 
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
			NodeLevelSets node = node_.getInfo();						
			NodeLevelSets newNode = node_.fakeNode ? allocateNewNode(node_, node) : node;
			newNode.setLevel(node.getLevel() + offset[node.getId()]);
		}
	}
	
	public Iterable<NodeMergedTree> skipRoot() {		
		Queue<NodeMergedTree> queue = new Queue<InfoMergedTree.NodeMergedTree>();
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
		Queue<NodeMergedTree> queue = new Queue<InfoMergedTree.NodeMergedTree>();
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
	
	public NodeMergedTree[] getMap() {
		return map;
	}
	
	public NodeMergedTree getRoot() {
		return map[0];
	}
	
	public class NodeMergedTree {		
				
		SimpleLinkedList<NodeMergedTree> children = new SimpleLinkedList<NodeMergedTree>();
		//SimpleLinkedList<Integer> pixels;
		HashMap<Integer, Attribute> attributes;
		boolean fakeNode = false;	
		boolean isAttrCloned = false;
		NodeMergedTree parent;
		NodeLevelSets info;		
		
		public NodeMergedTree(NodeLevelSets info, boolean fakeNode) {
			this.info = info;
			this.fakeNode = fakeNode;
			this.attributes = info.getAttributes();
			//this.pixels = info.getCompactNodePixels();
		}
		
		/*public SimpleLinkedList<Integer> getCompactNodePixels(){
			return pixels;
		}*/
		
		public HashMap<Integer, Attribute> getAttributes(){
			return attributes;
		} 
		
		public void addAttribute(int key, Attribute attr) {
			attributes.put(key, attr);
		}
		
		public double getAttributeValue(int key) {
			return attributes.get(key).value;
		}
		
		public boolean isFakeNode() {
			return fakeNode;
		}
		
		public boolean isAttrCloned() {
			return isAttrCloned;
		}
			
		public int hashCode() {
			return info.getId();
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
		
	}
	
}
