package mmlib4j.representation.tree.tos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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
import mmlib4j.representation.tree.attribute.BitQuadAttributePattern;


/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class NodeToS implements INodeTree, Cloneable{
	int level;
	GrayScaleImage img;
	private int canonicalPixel;
	int id;
	int attributeValue[]; //vetor de atributos crescentes
	int heightNode;
	int highest;
	int lowest;
	int numDescendent;
	boolean isClone = false;
	double moment[][];
	
	BitQuadAttributePattern attributePattern;
	
	boolean isNodeMaxtree;
	int xmin;
	int ymin;
	int xmax;
	int ymax;
	int sumX;
	int sumY;
	int sumYY;
	int sumXX;
	int sumXY;
	int area;
	
	int countHoles;
	
	public boolean flagPruning;
	public boolean flagProcess;
	
	NodeToS parent;
	List<NodeToS> children = new ArrayList<NodeToS>();
	private List<Integer> pixels = new ArrayList<Integer>();
	Contour contour = new Contour();
	
	public NodeToS(int numCreate, int level, GrayScaleImage img, int canonicalPixel){
		this.id = numCreate;
		this.level = level; 
		this.img = img;
		this.canonicalPixel = canonicalPixel;
		this.highest = this.lowest = level;
		xmin = ymin = Integer.MAX_VALUE;
		xmax = ymax = Integer.MIN_VALUE;
		moment = new double[4][4];
	}
	
	public int getId(){
		return id;
	}

	public NodeToS getParent(){
		return parent;
	}
	
	public List<NodeToS> getChildren(){
		return children;
	}
	
	public int getLevel(){
		return level;
	}
	
	public int getCanonicalPixel(){
		return canonicalPixel;
	}
	
	public boolean isLeaf(){
		return children.isEmpty();
	}
	
	public boolean isNodeMaxtree(){
		return isNodeMaxtree;
	}
	
	public int getNumHoles(){
		return countHoles;
	}
	public boolean isClone(){
		return isClone;
	}
	public NodeToS getClone(){
		try {
			NodeToS no = (NodeToS) this.clone();
			no.isClone = true;
			no.children = new ArrayList<NodeToS>();
			no.pixels = new ArrayList<Integer>();
			return no;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void addPixel(int p){		
		int x = p % img.getWidth();
		int y = p / img.getWidth();
		if(x < xmin) xmin = x;
		if(x > xmax) xmax = x;
		if(y < ymin) ymin = y;
		if(y > ymax) ymax = y;
		
		//if(x == 0 || y == 0 || x == width-1 || y == height-1)
		//	countPixelInFrame++;
		sumX += x;
		sumY += y;
		sumXX += x*x;
		sumYY += y*y;
		sumXY += x*y;
		area += 1;
		
		pixels.add(p);
	}
	
	public List<Integer> getPixels(){
		return pixels;
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
		this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_VOLUME] += this.pixels.size() * this.level; 

	}
	
	public int getNumChildren(){
		return children.size();
	}
	
	public int getWidthNode(){
		return (this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MAX] - this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MIN] + 1);
	}
	
	public int getHeightNode(){
		return (this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MAX] - this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MIN] + 1);
	}
	
	public int getArea(){
		return this.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_AREA];
	}
	

	public double getCircularity(){
		return (4.0 * Math.PI * getArea()) / Math.pow(getPerimeter(), 2);
	}
	
	public double getPerimeter(){
		return attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_PERIMETER];
	}
	
	
	public int getAttributeValue(int index){
		return attributeValue[index];
	}
	
	Double homogeneity = null;
	public double getHomogeneity(){
		if(homogeneity != null) return homogeneity;
		int count = 0;
		int sum = 0;
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(this);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			if(this.isNodeMaxtree == no.isNodeMaxtree){
				count += no.getArea();
				sum += (no.level+1) * no.getArea();
			}
			if(no.children != null){
				for(NodeToS son: no.children){
					fifo.enqueue(son);
				}
			}
		}
		
		double means = sum / (double) count;
		double var = 0;
		fifo.enqueue(this);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			if(this.isNodeMaxtree == no.isNodeMaxtree){ 
				var += Math.pow(no.level+1 - means, 2); 
			}
			if(no.children != null){
				for(NodeToS son: no.children){
					fifo.enqueue(son);
				}
			}
		}
		homogeneity = var / count;
		return homogeneity;
		
	}
	
	

	public int getCentroid(){
		int xc = (sumX / getArea());
		int yc = (sumY / getArea());
		return xc + yc * img.getWidth();     
	}
	
	public double getMoment(int p, int q){
		return moment[p][q];
	}
	
	public double getMomentNormalized(int p, int q){
		return moment[p][q] / Math.pow( getArea(), (p + q + 2.0) / 2.0);
	}
	
	public double getMomentOrientation(){
		return 0.5 * Math.atan2( 2 * moment[1][1], moment[2][0] - moment[0][2]);
	}
	
	public double eccentricity(){
		double a = moment[2][0] + moment[0][2] + Math.sqrt( Math.pow(moment[2][0] - moment[0][2], 2) + 4 * Math.pow(moment[1][1], 2));
		double b = moment[2][0] + moment[0][2] - Math.sqrt( Math.pow(moment[2][0] - moment[0][2], 2) + 4 * Math.pow(moment[1][1], 2));
		return a / b; 
	}
	
	public double getLengthMajorAxes(){
		double a = moment[2][0] + moment[0][2] + Math.sqrt( Math.pow(moment[2][0] - moment[0][2], 2) + 4 * Math.pow(moment[1][1], 2));
		return Math.sqrt( (2 * a) / getArea() );
	}

	public double getLengthMinorAxes(){
		double b = moment[2][0] + moment[0][2] - Math.sqrt( Math.pow(moment[2][0] - moment[0][2], 2) + 4 * Math.pow(moment[1][1], 2));
		return Math.sqrt( (2 * b) / getArea() );
	}

	public double getElongation(){
		return getArea() / Math.pow(2 * getLengthMajorAxes(), 2);
	}
	
	public void initMoment(){
		int xc = (sumX / getArea());
		int yc = (sumY / getArea());
		for(int pixel: pixels){
			int x = pixel % img.getWidth();
			int y = pixel / img.getWidth();
			for(int p=0; p < 4; p++){
				for(int q=0; q < 4; q++){
					moment[p][q] += Math.pow(x - xc, p) * Math.pow(y - yc, q); 
				}
			}
		}
	}
	
	public void updateMoment(double moment[][]){
		for(int p=0; p < 4; p++){
			for(int q=0; q < 4; q++){
				this.moment[p][q] += moment[p][q]; 
			}
		}
	}
	
	public BitQuadAttributePattern getPattern(){
		return attributePattern;
	}
	
	public void initAttributePattern(){
		attributePattern = new BitQuadAttributePattern();
	}
	

	public Iterable<Integer> getPixelsOfCC(){
		final Queue<NodeToS> fifo = new Queue<NodeToS>();
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
							NodeToS no = fifo.dequeue();
							for(NodeToS son: no.children){
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
	


	public Iterable<NodeToS> getNodesDescendants(){
		final Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(this);
		
		return new Iterable<NodeToS>() {
			public Iterator<NodeToS> iterator() {
				return new Iterator<NodeToS>() {
					private NodeToS currentNode = nextNode(); 
					public boolean hasNext() {
						return currentNode != null;
					}
					public NodeToS next() {
						NodeToS tmp = currentNode;
						currentNode = nextNode(); 
						return tmp;
					}
					public void remove() {}
					private NodeToS nextNode(){
						if(!fifo.isEmpty()){
							NodeToS no = fifo.dequeue();
							for(NodeToS son: no.children){
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
	
	
	public boolean isDescendant(NodeToS node){
		return node.isAncestral(this);
	}
	
	public boolean isAncestral(NodeToS nodeAncestral){
		NodeToS tmp = this;
		while(tmp != null){
			tmp = tmp.parent;
			if(tmp == nodeAncestral){
				return true;
			}
		}
		return  false;
	}
	
	
	public GrayScaleImage createImageSC(){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img);;
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(this);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			
			if(no.children != null){
				for(NodeToS son: no.children){
					fifo.enqueue(son);
				}
			}
			
			for(Integer p: no.pixels){
				if(img.getPixel(p) == level)
					imgOut.setPixel(p, 255);
				else
					imgOut.setPixel(p, 220);
			}
			
		}
		return imgOut;
	}
	
	
	
	public BinaryImage createImage(){
		BinaryImage imgOut = new BitImage(img.getWidth(), img.getHeight());
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(this);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			
			if(no.children != null){
				for(NodeToS son: no.children){
					fifo.enqueue(son);
				}
			}
			
			for(Integer p: no.pixels){
				imgOut.setPixel(p, true);
			}
			
		}
		return imgOut;
	}
	
	public int hashCode(){
		return id;
	}
}
