package mmlib4j.representation.tree.componentTree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.filtering.binary.Contour;
import mmlib4j.images.BinaryImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.BitImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.IMorphologicalTreeFiltering;
import mmlib4j.representation.tree.INodeTree;
import mmlib4j.representation.tree.attribute.AttributePatternEuler;
import mmlib4j.representation.tree.attribute.BitQuadAttributePattern;
import mmlib4j.representation.tree.attribute.CentralMoments;


/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class NodeCT implements INodeTree, Cloneable{
	int level;
	int highest;
	int lowest;
	
	GrayScaleImage img;
	private int canonicalPixel;
	int id;
	int numDescendent;
	int numDescendentLeaf;
	int countPixelInFrame;
	double perimeter;
	
	public int attributeValueNC[]; //vetor de atributos decrescentes
	public int attributeValue[]; //vetor de atributos crescentes
	public CentralMoments moment;
	public BitQuadAttributePattern attributePattern;
	public AttributePatternEuler attributeEuler;
	public double mser;
	
	int heightNode;
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
	
	/*double moment20; 
	double moment02;
	double moment11;*/
	int area;
	
	public Contour getContour() {
		return contour;
	}

	public boolean isMaxtree() {
		return isMaxtree;
	}

	public Contour contour = null;
	
	boolean isMaxtree;
	public boolean flagProcess;
	public boolean flagPruning = true;
	boolean isClone = false;
	
	NodeCT parent;
	List<NodeCT> children = new ArrayList<NodeCT>();
	SimpleLinkedList<NodeCT> adjcencyNodes = new SimpleLinkedList<NodeCT>();
	SimpleLinkedList<Integer> pixels = new SimpleLinkedList<Integer>();
	
	
	
	public NodeCT(boolean isMaxtree, int numCreate, GrayScaleImage img, int canonicalPixel){
		this.isMaxtree = isMaxtree;
		this.id = numCreate;
		this.img = img;
		this.canonicalPixel = canonicalPixel; 
		this.level = img.getPixel(canonicalPixel);
		this.highest = lowest = level;
		xmin = ymin = Integer.MAX_VALUE;
		xmax = ymax = Integer.MIN_VALUE;
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
	
	public boolean isClone(){
		return isClone;
	}
	
	public int getCentroid(){
		int xc = sumX / getArea();
		int yc = sumY / getArea();
		
		return (xc + yc * img.getWidth());
	}
	
	public CentralMoments getMoments(){
		if(moment == null){
			double xc = sumX / (double)getArea();
			double yc = sumY / (double)getArea();
			moment = new CentralMoments(xc, yc, area, img.getWidth());
		}
		return moment; 
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
	
	public boolean isLeaf(){
		return children.isEmpty();
	}
	
	public int getNumChildren(){
		return children.size();
	}
	
	public int getNumDescendants(){
		return numDescendent;
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
		
		if(x == 0 || y == 0 || x == img.getWidth()-1 || y == img.getHeight()-1)
			countPixelInFrame++;
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
	
	public SimpleLinkedList<Integer> getPixels(){
		return pixels;
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
	
	
	
	public void initAttributes(int dimensionAttributes){
		this.attributeValue = new int[dimensionAttributes];
		//largura
		this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MAX] = this.xmax;
		this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MIN] = this.xmin;
		//altura
		this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MAX] = this.ymax;
		this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MIN] = this.ymin;
		//area
		this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_AREA] = this.pixels.size();
		//volume
		this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_VOLUME] = this.pixels.size() * this.level; 
		
	}

	public int xMinBoundBox(){
		return this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MIN];
	}
	public int xMaxBoundBox(){
		return this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MAX];
	}
	public int yMinBoundBox(){
		return this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MIN];
	}
	public int yMaxBoundBox(){
		return this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MAX];
	}
	
	
	public void initAttributesNC(int dimensionAttributes){
		this.attributeValueNC = new int[dimensionAttributes]; 

		//atributo decrecente
		this.attributeValueNC[IMorphologicalTreeFiltering.ATTRIBUTE_NC_VARIANCIA] = (int) (Math.pow(this.level, 2) * this.pixels.size());
		this.attributeValueNC[IMorphologicalTreeFiltering.ATTRIBUTE_NC_PERIMETRO] = 0;
	}
	
	
	public BitQuadAttributePattern getPattern(){
		return attributePattern;
	}
	
	public void initAttributePattern(){
		attributePattern = new BitQuadAttributePattern();
	}
	public void initMoment(){
		moment = new CentralMoments(sumX / getArea(), sumY / getArea(), getArea(), img.getWidth(), pixels);
	}
	
	public int getWidthNode(){
		return attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_WIDTH];
		//return (this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MAX] - this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MIN] + 1);
	}
	
	public int getHeightNode(){
		return attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_HEIGHT];
		//return (this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MAX] - this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MIN] + 1);
	}
	
	public int getArea(){
		return area;
	}
	
	public int getVolume(){
		return attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_VOLUME];
	}
	
	public double getCircularity(){
		return (4.0 * Math.PI * getArea()) / Math.pow(getPerimeterExternal(), 2);
	}
	
	public double getCompacity(){
		return Math.pow(getPerimeterExternal(),2) / getArea();
	}
	
	
	
	public double getPerimeterExternal(){
		return attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_PERIMETER];
	}
	
	public int getAttributeValue(int index){
		return attributeValue[index];
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
	
	Double homogeneity = null;
	public double getHomogeneity(){
		if(homogeneity != null) return homogeneity;
		int count = 0;
		int sum = 0;
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(this);
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			count += no.pixels.size();
			sum += (no.level+1) * no.pixels.size();
			if(no.children != null){
				for(NodeCT son: no.children){
					fifo.enqueue(son);
				}
			}
		}
		
		double means = sum / (double) count;
		double var = 0;
		fifo.enqueue(this);
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			var += Math.pow(no.level+1 - means, 2); 
			if(no.children != null){
				for(NodeCT son: no.children){
					fifo.enqueue(son);
				}
			}
		}
		homogeneity = Math.sqrt(var / count);
		return homogeneity;
		
	}
	
	Double levelMeans = null;
	public double getLevelMean(){
		if(levelMeans != null) return levelMeans;
		int count = 0;
		int sum = 0;
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(this);
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			count += no.pixels.size();
			sum += (no.level+1) * no.pixels.size();
			if(no.children != null){
				for(NodeCT son: no.children){
					fifo.enqueue(son);
				}
			}
		}
		
		levelMeans = sum / (double) count;
		
		return levelMeans;
		
	}
	
	public int hashCode(){
		return id;
	}
	
	
	public void binarization(BinaryImage imgOut){
		int min=255;
		int max=0;
		for(int p: pixels){
			if(min > this.img.getPixel(p))
				min = img.getPixel(p);
			if(max < this.img.getPixel(p))
				max = img.getPixel(p);
		}
		int d = (int) Math.round( 255 * (1 - ((max - min) / (max + min + 0.00001))));
		for(int p: pixels){
			imgOut.setPixel(p, d==1);
		}

	}
	
}