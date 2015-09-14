package mmlib4j.representation.tree.attribute;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.NodeLevelSets;
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
	NodeLevelSets rootTree;
	
	public ComputerAttributeBasedPerimeterExternal(int numNode, NodeLevelSets root, GrayScaleImage img){
		long ti = System.currentTimeMillis();
		this.img = img;		
		this.rootTree = root;
		perimeters = new Attribute[numNode];
		//pool =  new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		computerAttribute(root);
		//while(pool.getActiveCount() != 0);
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [extraction of attribute - based on perimeter]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}
	

	public void computerAttribute(NodeLevelSets node){
		List<NodeLevelSets> children = node.getChildren();
		perimeters[node.getId()] = new Attribute(Attribute.PERIMETER_EXTERNAL);
		
		if(node == rootTree){
			perimeters[node.getId()].value = img.getWidth() * 2 + img.getHeight() * 2;
		}else{
			if(node.getArea() < 3){
				perimeters[node.getId()].value = node.getArea();
			}else if(node.getParent().getArea() - node.getArea() < 3){
				perimeters[node.getId()].value = perimeters[node.getParent().getId()].value - 1;
			}else
				new ThreadNodeCTPerimeter(node, perimeters[node.getId()]).run();
				//pool.execute(new ThreadNodeCTPerimeter(node, perimeters[node.getId()]));
		}
		
		for(NodeLevelSets son: children){
			computerAttribute(son);
		}
		
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
		for(NodeLevelSets node: hashSet){
			addAttributeInNodes(node);
		}
	} 
	
	public void addAttributeInNodes(NodeLevelSets node){
		node.addAttribute(Attribute.PERIMETER_EXTERNAL, perimeters[node.getId()]);
		node.addAttribute(Attribute.CIRCULARITY, new Attribute(Attribute.CIRCULARITY, getCircularity(node)));
		node.addAttribute(Attribute.COMPACTNESS, new Attribute(Attribute.COMPACTNESS, getCompacity(node)));
		node.addAttribute(Attribute.ELONGATION, new Attribute(Attribute.ELONGATION, getElongation(node)));
	}
	
	public double getCircularity(NodeLevelSets node){
		return (4.0 * Math.PI * node.getArea()) / Math.pow(perimeters[node.getId()].getValue(), 2);
	}
	
	public double getCompacity(NodeLevelSets node){
		return Math.pow(perimeters[node.getId()].getValue(), 2) / node.getArea();
	}
	
	public double getElongation(NodeLevelSets node){
		return node.getArea() / Math.pow(perimeters[node.getId()].getValue(), 2);
	}
	//int cont;
	class ThreadNodeCTPerimeter extends Thread {
		private NodeLevelSets node;
		private Attribute perimeter;
		boolean imgBin[][];
		boolean is8Connected=true;
		int xmin, ymin;
		public ThreadNodeCTPerimeter(NodeLevelSets node, Attribute perimeter){
			this.node = node;
			this.perimeter = perimeter;
			if(node instanceof NodeToS){
				is8Connected = node.isNodeMaxtree() == true;
				
				xmin = node.getXmin();
				int xmax = node.getXmax();
				int ymax = node.getYmax();
				ymin = node.getYmin();
				imgBin = new boolean[xmax-xmin+1][ymax-ymin+1];
				NodeToS n = (NodeToS) node;
				for(int p: n.getPixelsOfCC()){
					int px = (p % img.getWidth()) - xmin;
					int py = (p / img.getWidth()) - ymin;
					imgBin[px][py] = true;
				}
			}
		}
		
		public void run() {
			//cont++;
			/*if(cont == 6331){
				BinaryImage b = ImageFactory.createBinaryImage(imgBin.length, imgBin[0].length);
				for(int x=0; x < imgBin.length; x++)
					for(int y=0; y < imgBin[0].length; y++)
						b.setPixel(x,  y, imgBin[x][y]);
				WindowImages.show( b, "imgBin" );
				WindowImages.show( node.createImage() );
			}*/
			perimeter.value = computerContour(node.getPixelWithYmin() % img.getWidth()-xmin, node.getPixelWithYmin() / img.getWidth()-ymin );
			//System.out.println(perimeter.value);
		}
		
		
		private boolean isForeground(int x, int y){
			
			if(imgBin == null){
				if(!img.isPixelValid(x, y)) return false;
				if(node.isNodeMaxtree())
					return img.getPixel(x, y) >= node.getLevel();
				else
					return img.getPixel(x, y) <= node.getLevel();
			}
			else{
				if((x >= 0 && x < imgBin.length && y >= 0 && y < imgBin[0].length))
					return imgBin[x][y];//.getPixel(x, y);
				else
					return false;
			}
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
		

		int findNextPoint (Pixel pt, int direction) { 
			if(is8Connected){
				for (int i = 0; i < delta.length - 1; i++) {
					int x = pt.x + delta[direction][0];
					int y = pt.y + delta[direction][1];
					if (!isForeground(x, y)) {
						direction = (direction + 1) % 8;
					} 
					else {						
						pt.x = x; pt.y = y; 
						break;
					}
				}
				return direction;
			}else{
				
				for (int i = 0; i < delta.length - 1; i++) {
					int x = pt.x + delta[direction][0];
					int y = pt.y + delta[direction][1];
					if (! isForeground(x, y) ) {
						direction = (direction + 2) % 8;
					} 
					else {						
						pt.x = x; pt.y = y; 
						break;
					}
				}
			}
			return direction;
		}
		
	}
	
}


