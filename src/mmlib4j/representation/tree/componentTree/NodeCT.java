package mmlib4j.representation.tree.componentTree;

import java.util.ArrayList;
import java.util.Hashtable;
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
import mmlib4j.representation.tree.INodeTree;
import mmlib4j.representation.tree.attribute.Attribute;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class NodeCT implements INodeTree, Cloneable{
	int level;
	int heightNode;
	GrayScaleImage img;
	int canonicalPixel;
	int id;
	int numDescendent;
	int numDescendentLeaf;

	public boolean flagProcess;
	public boolean flagPruning = true;
	private int countPixelInFrame;
	boolean isClone = false;
	boolean isMaxtree;
	NodeCT parent;
	List<NodeCT> children = new ArrayList<NodeCT>();
	SimpleLinkedList<NodeCT> adjcencyNodes = new SimpleLinkedList<NodeCT>();
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
	Hashtable<Integer, Attribute> attributes = new Hashtable<Integer, Attribute>();
	public Contour contourE = null;

	
	public NodeCT(boolean isMaxtree, int numCreate, GrayScaleImage img, int canonicalPixel){
		this.isMaxtree = isMaxtree;
		this.id = numCreate;
		this.img = img;
		this.canonicalPixel = canonicalPixel; 
		this.level = img.getPixel(canonicalPixel);
		xmin = ymin = Integer.MAX_VALUE;
		xmax = ymax = Integer.MIN_VALUE;
	}
	
	public boolean isMaxtree() {
		return isMaxtree;
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
		return isMaxtree;
	}
	
	public void addAttribute(int key, Attribute attr){
		attributes.put(key, attr);
	}
	public Attribute getAttribute(int key){
		return attributes.get(key);
	}
	
	public double getAttributeValue(int key){
		return attributes.get(key).getValue();
	}
	
	public int getNumPixelInFrame(){
		return countPixelInFrame;
	}
	
	public boolean isClone(){
		return isClone;
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
		if(y < ymin) {
			ymin = y;
			pixelYmin = p;
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
		
		pixels.add(p);
	}
	

	public NodeCT getAncestral(int level){
		NodeCT node = this;
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
			tmp = tmp.parent;
			if(tmp == nodeAncestral){
				return true;
			}
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
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(this.img);;
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
			ContourTracer c = new ContourTracer(true, isMaxtree, img, level);
			int x = pixelYmin % img.getWidth();
			int y = pixelYmin / img.getWidth(); 
			this.contourE = c.findOuterContours(x, y);
		}
		return contourE;
	}
	
	
	public int getArea(){
		return area;
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