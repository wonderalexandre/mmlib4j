package mmlib4j.representation.tree.attribute;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import mmlib4j.filtering.binary.ContourTracer;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.INodeTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.utils.Pixel;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerAttributeBasedPerimeterExternal {

	private ThreadPoolExecutor pool;	
	private Attribute perimeters[];
	private GrayScaleImage img;
	private static final int[][] delta = { { 1,0}, { 1, 1}, {0, 1}, {-1, 1}, {-1,0}, {-1,-1}, {0,-1}, { 1,-1} };
	int levelRoot;
	

	public ComputerAttributeBasedPerimeterExternal(int numNode, INodeTree root, GrayScaleImage img){
		long ti = System.currentTimeMillis();
		this.img = img;
		
		this.levelRoot = root.getLevel();
		perimeters = new Attribute[numNode];
		pool =  new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		computerAttribute(root);
		while(pool.getActiveCount() != 0);
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [extraction of attribute - based on perimeter]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}
	

	public void computerAttribute(INodeTree root){
		List<INodeTree> children = root.getChildren();
		perimeters[root.getId()] = new Attribute(Attribute.PERIMETER_EXTERNAL);
		for(INodeTree son: children){
			computerAttribute(son);
		}
		if(root.getLevel() == levelRoot){
			perimeters[root.getId()].value = img.getWidth() * 2 + img.getHeight() * 2;
		}else
			pool.execute(new ThreadNodeCTPerimeter(root, perimeters[root.getId()]));
	}
	
	
	public Attribute[] getAttribute(){
		return perimeters;
	}
	
	public void addAttributeInNodesCT(HashSet<NodeCT> list){
		for(NodeCT node: list){
			addAttributeInNodes(node);
		}
	}
	
	public void addAttributeInNodesToS(HashSet<NodeToS> hashSet){
		for(INodeTree node: hashSet){
			addAttributeInNodes(node);
		}
	} 
	
	public void addAttributeInNodes(INodeTree node){
		node.addAttribute(Attribute.PERIMETER_EXTERNAL, perimeters[node.getId()]);
		node.addAttribute(Attribute.CIRCULARITY, new Attribute(Attribute.CIRCULARITY, getCircularity(node)));
		node.addAttribute(Attribute.COMPACTNESS, new Attribute(Attribute.COMPACTNESS, getCompacity(node)));
		node.addAttribute(Attribute.ELONGATION, new Attribute(Attribute.ELONGATION, getElongation(node)));
	}
	
	public double getCircularity(INodeTree node){
		return (4.0 * Math.PI * node.getArea()) / Math.pow(perimeters[node.getId()].getValue(), 2);
	}
	
	public double getCompacity(INodeTree node){
		return Math.pow(perimeters[node.getId()].getValue(), 2) / node.getArea();
	}
	
	public double getElongation(INodeTree node){
		return node.getArea() / Math.pow(perimeters[node.getId()].getValue(), 2);
	}
	

	
	class ThreadNodeCTPerimeter extends Thread {
		private INodeTree node;
		private Attribute perimeter;
		boolean pixels[][] = null;
		public ThreadNodeCTPerimeter(INodeTree node, Attribute perimeter){
			this.node = node;
			this.perimeter = perimeter;
			if(node instanceof NodeToS){
				pixels = new boolean[img.getWidth()][img.getHeight()];
				NodeToS nnode = (NodeToS) node;
				for(Integer p: nnode.getPixelsOfCC()){
					pixels[p%img.getWidth()][p/img.getWidth()] = true;
				}
			}
		}
			
		public void run() {
			perimeter.value = computerContour(node.getPixelWithYmin() % img.getWidth(), node.getPixelWithYmin() / img.getWidth() );
		}
		
		
		private boolean isForeground(int x, int y){
			if(!img.isPixelValid(x, y)) return false;
			if(pixels == null){
				if(!img.isPixelValid(x, y)) return false;
				if(node.isNodeMaxtree())
					return img.getPixel(x, y) >= node.getLevel();
				else
					return img.getPixel(x, y) <= node.getLevel();
			}
			return pixels[x][y];
		}
		
		double computerContour (int xS, int yS) {
			int xT, yT; 
			int xP, yP; 
			int xC, yC; 
			double perimeter = 1;
			Pixel pt = new Pixel(xS, yS, 0); 
			int dNext = findNextPoint(pt, 0);
			
			xP = xS; yP = yS;
			xC = xT = pt.x;
			yC = yT = pt.y;
			
			boolean done = (xS==xT && yS==yT);
			while (!done) {
				//pt.x = xC;
				//pt.y = yC;
				
				dNext = findNextPoint(pt, (dNext + 6) % 8);
				xP = xC;  
				yP = yC;	
				xC = pt.x; 
				yC = pt.y; 
				done = (xP==xS && yP==yS && xC==xT && yC==yT);
				if (!done) {
					if(dNext % 2 ==0)
						perimeter += 1;
					else
						perimeter += Math.sqrt(2);
					
				}
			}
			return perimeter;
		}
		
		private int findNextPoint (Pixel pt, int direction) { 
			for (int i = 0; i < 7; i++) {
				int x = pt.x + delta[direction][0];
				int y = pt.y + delta[direction][1];
				if (!isForeground(x, y)) {
					direction = (direction + 1) % 8;
				} 
				else {						
					pt.x = x; 
					pt.y = y; 
					break;
				}
			}
			
			return direction;
		}
		
		
	}
	
}


