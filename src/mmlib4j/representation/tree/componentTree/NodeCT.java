package mmlib4j.representation.tree.componentTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.filtering.binary.Contour;
import mmlib4j.filtering.binary.ContourTracer;
import mmlib4j.images.BinaryImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.BitImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class NodeCT implements NodeLevelSets, Cloneable{
	int level;
	int heightNode;
	GrayScaleImage img;
	int canonicalPixel;
	int id;
	int numDescendent;
	int numDescendentLeaf;
	int numSiblings;
	int numNodeInSameBranch=-1;
	
	public boolean flagProcess;
	public boolean flagPruning = true;
	
	int countPixelInFrame;
	boolean isClone = false;
	boolean isNodeMaxtree;
	NodeCT parent;
	List<NodeCT> children = new ArrayList<NodeCT>();
	SimpleLinkedList<Integer> pixels = new SimpleLinkedList<Integer>();
	
	//basic attribute node
	int xmin;
	int ymin;
	int xmax;
	int ymax;
	int sumX;
	int sumY;	
	int pixelXmax;
	int pixelXmin;
	int pixelYmin;
	int pixelYmax;
	int area;
	int volume;
	HashMap<Integer, Attribute> attributes = new HashMap<Integer, Attribute>();
	
	Contour contourE = null;
	SimpleLinkedList<NodeCT> adjcencyNodes = null;
	
	public NodeCT(boolean isMaxtree, int numCreate, GrayScaleImage img, int canonicalPixel){
		this.isNodeMaxtree = isMaxtree;
		this.id = numCreate;
		this.img = img;
		this.canonicalPixel = canonicalPixel; 
		this.level = img.getPixel(canonicalPixel);
		xmin = ymin = Integer.MAX_VALUE;
		xmax = ymax = Integer.MIN_VALUE;
	}
	
	public boolean isMaxtree() {
		return isNodeMaxtree;
	}

	
	public SimpleLinkedList<NodeCT> getAdjacencyNodes(){
		return adjcencyNodes;
	}
	
	public void addAdjacencyNode(NodeCT node){
		adjcencyNodes.add(node);
	}
	
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
	
	public void setParent(NodeCT p){
		parent = p;
	}
	
	public void addChildren(NodeCT n){
		children.add(n);
	}
	
	public NodeCT getClone(){
		try {
			NodeCT no = (NodeCT) this.clone();
			no.isClone = true;
			no.children = new ArrayList<NodeCT>();
			no.pixels = new SimpleLinkedList<Integer>();
			return no;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public int getId(){
		return id;
	}
	
	public NodeCT getParent(){
		return parent;
	}
	
	public List<NodeCT> getChildren(){
		return children;
	}
	
	public int getLevel(){
		return level;
	}
	
	public int getCentroid(){
		int xc = sumX / getArea();
		int yc = sumY / getArea();
		
		return (xc + yc * img.getWidth());
	}
	
	public boolean isLeaf(){
		return children.isEmpty();
	}
	
	public int getNumChildren(){
		return children.size();
	}
	
	public int getNumDescendants(){
		return numDescendent;
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
		
		sumX += x;
		sumY += y;
		area += 1;
		volume += (level);
		pixels.add(p);
	}
	

	public NodeCT getAncestral(int level){
		NodeCT node = this;
		if(isNodeMaxtree){
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
	

	public Iterable<NodeCT> getNodesDescendants(){
		final Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(this);
		
		return new Iterable<NodeCT>() {
			public Iterator<NodeCT> iterator() {
				return new Iterator<NodeCT>() {
					private NodeCT currentNode = nextNode(); 
					public boolean hasNext() {
						return currentNode != null;
					}
					public NodeCT next() {
						NodeCT tmp = currentNode;
						currentNode = nextNode(); 
						return tmp;
					}
					public void remove() {}
					private NodeCT nextNode(){
						if(!fifo.isEmpty()){
							NodeCT no = fifo.dequeue();
							for(NodeCT son: no.children){
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
	
	
	public boolean isDescendant(NodeCT node){
		return node.isAncestral(this);
	}
	
	public boolean isAncestral(NodeCT nodeAncestral){
		NodeCT tmp = this;
		while(tmp != null){
			if(tmp == nodeAncestral){
				return true;
			}
			tmp = tmp.parent;
		}
		return  false;
	}
	
	public boolean isComparable(NodeCT node){
		return isDescendant(node) || isAncestral(node);
	}
	
	public SimpleLinkedList<Integer> getCanonicalPixels(){
		return pixels;
	}
	
	public int getNumCanonicalPixel(){
		return pixels.size();
	}
	
	public int getNumNodesInBranch(){
		if(numNodeInSameBranch != -1)
			return numNodeInSameBranch;
		
		numNodeInSameBranch = 1;
		
		//ancestrais
		NodeCT node = this;
		while(node != null && node.numSiblings == 0){
			node = node.parent;
			numNodeInSameBranch++;
		}
		
		//descendentes
		node = this;
		while(node.children.size() == 1){
			node = node.children.get(0);
			numNodeInSameBranch++;
		}
		
		return numNodeInSameBranch;
	}

	public Iterable<NodeCT> getPathToRoot(){
		final NodeCT node = this;
		return new Iterable<NodeCT>() {
			public Iterator<NodeCT> iterator() {
				return new Iterator<NodeCT>() {
					NodeCT nodeRef = node;
					public boolean hasNext() {
						return nodeRef != null;
					}

					public NodeCT next() {
						NodeCT n = nodeRef;
						nodeRef = nodeRef.parent;
						return n;
					}

					public void remove() { }
					
				};
			}
		};
	}
	
	public Iterable<Integer> getPixelsOfCC(){
		final Queue<NodeCT> fifo = new Queue<NodeCT>();
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
							NodeCT no = fifo.dequeue();
							for(NodeCT son: no.children){
								fifo.enqueue(son);
							}
							return pixelNode = no.pixels.iterator();
						}
						return null;
					}
				};
			}
		};
		
	}
	


	public GrayScaleImage createImageSC(int levelNotSC){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());;
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(this);
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			
			if(no.children != null){
				for(NodeCT son: no.children){
					fifo.enqueue(son);
				}
			}
			
			for(Integer p: no.pixels){
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
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(this);
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			
			if(no.children != null){
				for(NodeCT son: no.children){
					fifo.enqueue(son);
				}
			}
			
			for(Integer p: no.pixels){
				img.setPixel(p, true);
			}
			
		}
		return img;
	}
	

	public int hashCode(){
		return id;
	}
	
	public Contour getContour() {
		if(contourE == null){
			ContourTracer c = new ContourTracer(true, isNodeMaxtree, img, level);
			int x = pixelYmin % img.getWidth();
			int y = pixelYmin / img.getWidth(); 
			this.contourE = c.findOuterContours(x, y);
		}
		return contourE;
	}
	
	
	public int getArea(){
		return area;
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
	
}