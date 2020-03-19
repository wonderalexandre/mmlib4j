package mmlib4j.representation.tree;

import java.util.HashMap;
import java.util.Iterator;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.BinaryImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.BitImage;
import mmlib4j.images.impl.AbstractImageFactory;
import mmlib4j.representation.tree.attribute.Attribute;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public abstract class NodeLevelSets {

	public static final byte NODE_TYPE_MAXTREE = 0;
	public static final byte NODE_TYPE_MINTREE = 1;
	public static final byte NODE_TYPE_TREE_OF_SHAPE = 2;
	
	protected int level;
	protected int heightNode;
	protected GrayScaleImage img;
	protected int canonicalPixel;
	protected int id;
	private int numDescendent;
	private int numDescendentLeaf;
	protected int numNodeInSameBranch=-1;
	protected boolean isNodeMaxtree;
	protected NodeLevelSets parent;
	
	protected SimpleLinkedList<NodeLevelSets> children = new SimpleLinkedList<NodeLevelSets>();
	protected SimpleLinkedList<Integer> pixels = new SimpleLinkedList<Integer>();
	protected HashMap<Integer, Attribute> attributes = new HashMap<Integer, Attribute>();
	//protected SimpleLinkedList<NodeLevelSets> adjcencyNodes = new SimpleLinkedList<NodeLevelSets>();
	protected boolean isClone = false;
	protected int countPixelInFrame;
	
	//basic attribute node
	protected int xmin;
	protected int ymin;
	protected int xmax;
	protected int ymax;
	private int sumX;
	private int sumY;	
	protected int pixelXmax;
	protected int pixelXmin;
	protected int pixelYmin;
	protected int pixelYmax;
	private int area;
	protected int volume;
	
	
	public abstract NodeLevelSets getClone();
	
	
	public int getId(){
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	

	public int getLevel(){
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	

	public void setNodeType(boolean isNodeMaxtree) {
		this.isNodeMaxtree = isNodeMaxtree;
	}
	
	/*public SimpleLinkedList<NodeLevelSets> getAdjacencyNodes(){
		return adjcencyNodes;
	}
	
	public void addAdjacencyNode(NodeLevelSets node){
		adjcencyNodes.add(node);
	}*/
	
	public int getCanonicalPixel(){
		return canonicalPixel;
	}
	
	public boolean isNodeMaxtree(){
		return isNodeMaxtree;
	}
	
	public void addAttribute(int key, Attribute attr){
		attributes.put(key, attr);
	}
	public Attribute getAttribute(int key){
		return attributes.get(key);
	}
	
	public HashMap<Integer, Attribute> getAttributes(){
		return attributes;
	}
	
	public void setAttributes(HashMap<Integer, Attribute> attributes) {
		this.attributes = attributes;
	}
	
	public double getAttributeValue(int key){
		return attributes.get(key).getValue();
	}
	
	public boolean hasAttribute(int key){
		return attributes.containsKey(key);
	}
	
	public int getNumPixelInFrame(){
		return countPixelInFrame;
	}
	
	public boolean isClone(){
		return isClone;
	}
	
	public void setParent(NodeLevelSets p){
		parent = p;
	}
	
	public void addChildren(NodeLevelSets n){
		children.add(n);
	}
	
	public void setChildren(SimpleLinkedList<NodeLevelSets> children) {
		this.children = children;
	}
	

	public NodeLevelSets getParent(){
		return parent;
	}
	
	public SimpleLinkedList<NodeLevelSets> getChildren(){
		return children;
	}
	
	
	public int getCentroid(){
		int xc = getSumX() / getArea();
		int yc = getSumY() / getArea();
		
		return (xc + yc * img.getWidth());
	}
	
	public boolean isLeaf(){
		return children.isEmpty();
	}
	
	public int getNumChildren(){
		return children.size();
	}
	
	public int getXmin(){
		return xmin;
	}
	public int getYmin(){
		return ymin;
	}
	public int getXmax(){
		return xmax;
	}
	public int getYmax(){
		return ymax;
	}		
	
	public void addPixel(int p){		
		int x = p % img.getWidth();
		int y = p / img.getWidth();
		if(x < xmin){ 
			xmin = x;
			pixelXmin = p;
		}
		if(x > xmax) {
			xmax = x;
			pixelXmax = p;
		}
		if(y <= ymin) {
			if( y < ymin){
				ymin = y;
				pixelYmin = p;
			}
			else {
				if(x < pixelYmin % img.getWidth())
					pixelYmin = p;
			}
		}
		if(y > ymax){
			ymax = y;
			pixelYmax = p;
		}
		if(x == 0 || y == 0 || x == img.getWidth()-1 || y == img.getHeight()-1){
			countPixelInFrame++;
			if(x == 0 && y == 0)
				countPixelInFrame++;
			else if(x==0 && y == img.getHeight()-1)
				countPixelInFrame++;
			else if(x == img.getWidth()-1 && y == 0)
				countPixelInFrame++;
			else if(x == img.getWidth()-1 && y == img.getHeight()-1)
				countPixelInFrame++;
		}
		
		setSumX(getSumX() + x);
		setSumY(getSumY() + y);
		setArea(getArea() + 1);
		volume += (level);
		pixels.add(p);
	}
	

	public int getArea(){
		return area;
	}
	
	public void setVolume(int volume) {
		this.volume = volume;
	}
	
	public int getVolume(){
		return volume;
	}
	
	public void setXmin(int p) {
		xmin = p;
	}

	public void setYmin(int p) {
		ymin = p;
	}

	public void setXmax(int p) {
		xmax = p;
	}

	public void setYmax(int p) {
		ymax = p;
	}

	public void setPixelWithXmax(int p) {
		pixelXmax = p;
	}

	public void setPixelWithYmax(int p) {
		pixelYmax = p;
	}

	public void setPixelWithXmin(int p) {
		pixelXmin = p;
	}

	public void setPixelWithYmin(int p) {
		pixelYmin = p;
	}

	public int getPixelWithXmax() {
		return pixelXmax ;
	}

	public int getPixelWithYmax() {
		return pixelYmax;
	}

	public int getPixelWithXmin() {
		return pixelXmin;
	}

	public int getPixelWithYmin() {
		return pixelYmin;
	}

	public void setHeightNode(int height) {
		this.heightNode = height;
	}

	public int getHeightNode() {
		return heightNode;
	}


	/*public SimpleLinkedList<NodeLevelSets> getAdjcencyNodes() {
		return adjcencyNodes;
	}*/
	

	public int hashCode(){
		return id;
	}
	

	public Iterable<NodeLevelSets> getNodesDescendants(){
		final Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(this);
		
		return new Iterable<NodeLevelSets>() {
			public Iterator<NodeLevelSets> iterator() {
				return new Iterator<NodeLevelSets>() {
					private NodeLevelSets currentNode = nextNode(); 
					public boolean hasNext() {
						return currentNode != null;
					}
					public NodeLevelSets next() {
						NodeLevelSets tmp = currentNode;
						currentNode = nextNode(); 
						return tmp;
					}
					public void remove() {}
					private NodeLevelSets nextNode(){
						if(!fifo.isEmpty()){
							NodeLevelSets no = fifo.dequeue();
							for(NodeLevelSets son: no.getChildren()){
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
	
	
	public boolean isDescendant(NodeLevelSets node){
		return node.isAncestral(this);
	}
	
	public boolean isAncestral(NodeLevelSets nodeAncestral){
		NodeLevelSets tmp = this;
		while(tmp != null){
			if(tmp == nodeAncestral){
				return true;
			}
			tmp = tmp.getParent();
		}
		return  false;
	}
	
	public boolean isComparable(NodeLevelSets node){
		return isDescendant(node) || isAncestral(node);
	}
	
	public void setCompactNodePixels(SimpleLinkedList<Integer> pixels){
		this.pixels = pixels;
	}
	
	public SimpleLinkedList<Integer> getCompactNodePixels(){
		return pixels;
	}
	
	public int getNumCompactNodePixels(){
		return pixels.size();
	}
	
	public int getNumSiblings() {
		if(getParent() != null)
			return getParent().getChildren().size();
		else
			return 0;
	}
	
	public int getNumNodeInSameBranch() {
		return numNodeInSameBranch;		
	}
	
	public void setNumNodeInSameBranch(int numNodeInSameBranch) {
		this.numNodeInSameBranch = numNodeInSameBranch;		
	}
	
	public int getNumNodesInBranch(){
		if(numNodeInSameBranch != -1)
			return numNodeInSameBranch;
		
		numNodeInSameBranch = 1;
		
		//ancestrais
		NodeLevelSets node = this;
		while(node != null && node.getNumSiblings() == 0){
			node = node.getParent();
			numNodeInSameBranch++;
		}
		
		//descendentes
		node = this;
		while(node.getChildren().size() == 1){
			node = node.getChildren().getFisrtElement();
			numNodeInSameBranch++;
		}
		
		return numNodeInSameBranch;
	}

	public Iterable<NodeLevelSets> getPathToRoot(){
		final NodeLevelSets node = this;
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
	
	public Iterable<Integer> getPixelsOfCC(){
		final Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(this);
		
		return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private Iterator<Integer> pixelNode = nextNode(); 
					public boolean hasNext() {
						if(pixelNode == null)
							pixelNode = nextNode();
						
						boolean b = pixelNode.hasNext();
						if(!b){
							pixelNode = nextNode();
							if(pixelNode == null)
								return false;
							else
								b = pixelNode.hasNext();
						}
						
						return b;
					}
					public Integer next() {
						return pixelNode.next();
					}
					public void remove() {}
					private Iterator<Integer> nextNode(){
						if(!fifo.isEmpty()){
							NodeLevelSets no = fifo.dequeue();
							for(NodeLevelSets son: no.getChildren()){
								fifo.enqueue(son);
							}
							return pixelNode = no.getCompactNodePixels().iterator();
						}
						return null;
					}
				};
			}
		};
		
	}
	


	public GrayScaleImage createImageSC(int levelNotSC){
		GrayScaleImage imgOut = AbstractImageFactory.instance.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());;
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(this);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			
			if(no.getChildren() != null){
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);
				}
			}
			
			for(Integer p: no.getCompactNodePixels()){
				if(img.getPixel(p) == level)
					imgOut.setPixel(p, 255);
				else
					imgOut.setPixel(p, levelNotSC);
			}
		}
		return imgOut;
	}
	
	
	public GrayScaleImage createImageSC(){
		return createImageSC(level);
	}	
	
	
	public BinaryImage createImage(){
		BinaryImage img = new BitImage(this.img.getWidth(), this.img.getHeight());
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(this);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			
			if(no.getChildren() != null){
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);
				}
			}
			
			for(Integer p: no.getCompactNodePixels()){
				img.setPixel(p, true);
			}
			
		}
		return img;
	}

	public int getNumDescendentLeaf() {
		return numDescendentLeaf;
	}

	public void setNumDescendentLeaf(int numDescendentLeaf) {
		this.numDescendentLeaf = numDescendentLeaf;
	}

	public int getNumDescendent() {
		return numDescendent;
	}

	public void setNumDescendent(int numDescendent) {
		this.numDescendent = numDescendent;
	}

	public void setArea(int area) {
		this.area = area;
	}

	public int getSumX() {
		return sumX;
	}

	public void setSumX(int sumX) {
		this.sumX = sumX;
	}

	public int getSumY() {
		return sumY;
	}

	public void setSumY(int sumY) {
		this.sumY = sumY;
	}
	
}
