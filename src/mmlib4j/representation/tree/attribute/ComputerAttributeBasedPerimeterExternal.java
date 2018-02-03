package mmlib4j.representation.tree.attribute;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import mmlib4j.filtering.EdgeDetectors;
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
	
	/* Attributes */
	
	private Attribute perimeters[];
	
	private Attribute sumGrad[];
	
	private Attribute sumGradContour[];
	
	private GrayScaleImage img;
	
	private static final int[][] delta = { { 1,0}, { 1, 1}, {0, 1}, {-1, 1}, {-1,0}, {-1,-1}, {0,-1}, { 1,-1} };
	
	NodeLevelSets rootTree;
	
	/* Gobber add */
	
	private GrayScaleImage imgGrad;
	
	public ComputerAttributeBasedPerimeterExternal( int numNode, NodeLevelSets root, GrayScaleImage img ) {
		
		long ti = System.currentTimeMillis();
		
		this.img = img;		
				
		this.rootTree = root;
		
		this.imgGrad = EdgeDetectors.sobel( img ); 
		
		perimeters = new Attribute[ numNode ];
		
		sumGrad = new Attribute[ numNode ];
		
		sumGradContour = new Attribute[ numNode ];
		
		computerAttribute( root );
		
		if( Utils.debug ) {
			
			long tf = System.currentTimeMillis();
			
			System.out.println("Tempo de execucao [extraction of attribute - based on perimeter external]  "+ ((tf - ti) /1000.0)  + "s");
			
		}
	}
	

	public void computerAttribute( NodeLevelSets node ) {
		
		List<NodeLevelSets> children = node.getChildren();
		
		perimeters[ node.getId() ] = new Attribute( Attribute.PERIMETER_EXTERNAL );
		
		sumGrad[ node.getId() ] = new Attribute( Attribute.SUM_GRAD );
		
		sumGradContour[ node.getId() ] = new Attribute( Attribute.SUM_GRAD_CONTOUR );
		
		if( node == rootTree ) {
			
			perimeters[ node.getId() ].value = img.getWidth() * 2 + img.getHeight() * 2;
			
			for( int cols = 0 ; cols < img.getWidth() ; cols++ ) {					
				
				sumGrad[ node.getId() ].value += ( imgGrad.getPixel( cols, 0 ) + imgGrad.getPixel( cols, imgGrad.getHeight()-1 ) );
				
			}
			
			for( int rows = 1 ; rows < img.getHeight()-1 ; rows++ ) {
				
				sumGrad[ node.getId() ].value += ( imgGrad.getPixel( 0, rows ) + imgGrad.getPixel( imgGrad.getWidth()-1, rows ) );
				
			}
			
			sumGradContour[ node.getId() ].value = sumGrad[ node.getId() ].value/perimeters[ node.getId() ].value;
			
		}else{
			
			if( node.getArea() < 3 ) {				
				
				perimeters[ node.getId() ].value = node.getArea();
				
				for( int pixel : node.getCanonicalPixels() ) {
					
					sumGrad[ node.getId() ].value += imgGrad.getPixel( pixel );
					
				}
				
				sumGradContour[ node.getId() ].value = sumGrad[ node.getId() ].value/perimeters[ node.getId() ].value;

			} else {
			
				new ThreadNodeCTPerimeter( node, 
										   perimeters[ node.getId() ], 
										   sumGrad[ node.getId() ], 
										   sumGradContour[ node.getId() ] ).run();
				
			}
		
		}
		
		for( NodeLevelSets son: children ) {
			
			computerAttribute( son );
			
		}
		
	}
	
	
	public Attribute[] getAttribute() {
		
		return perimeters;
		
	}
	
	public void addAttributeInNodesCT(HashSet<NodeCT> list) {
		
		for( NodeCT node: list ) {
			
			addAttributeInNodes( node );
			
		}
		
	}
	
	public void addAttributeInNodesToS(HashSet<NodeToS> hashSet) {
		
		for( NodeLevelSets node: hashSet ) {
			
			addAttributeInNodes( node );
			
		}
		
	} 
	
	public void addAttributeInNodes(NodeLevelSets node){
		
		node.addAttribute( Attribute.PERIMETER_EXTERNAL, perimeters[ node.getId() ] );
		node.addAttribute( Attribute.CIRCULARITY, new Attribute( Attribute.CIRCULARITY, getCircularity( node ) ) );
		node.addAttribute( Attribute.COMPACTNESS, new Attribute( Attribute.COMPACTNESS, getCompacity( node ) ) );
		node.addAttribute( Attribute.ELONGATION, new Attribute( Attribute.ELONGATION, getElongation( node ) ) );
		node.addAttribute( Attribute.SUM_GRAD , sumGrad[ node.getId() ] );		
		node.addAttribute( Attribute.SUM_GRAD_CONTOUR , sumGradContour[ node.getId() ] );	
		
	}
	
	public double getCircularity(NodeLevelSets node){
		
		return (4.0 * Math.PI * node.getArea()) / Math.pow(perimeters[node.getId()].getValue(), 2);
		
	}
	
	public double getCompacity(NodeLevelSets node){
		
		return Math.pow( perimeters[ node.getId() ].getValue(), 2 ) / node.getArea();
		
	}
	
	public double getElongation(NodeLevelSets node){
		
		return node.getArea() / Math.pow( perimeters[ node.getId() ].getValue(), 2 );
		
	}
	
	//int cont;
	class ThreadNodeCTPerimeter extends Thread {
		
		private NodeLevelSets node;
		private Attribute perimeter;
		private Attribute sumgrad;
		private Attribute sumGradContour;
		
		boolean imgBin[][];
		boolean is8Connected=true;
		int xmin, ymin;
		
		public ThreadNodeCTPerimeter( NodeLevelSets node, Attribute perimeter, Attribute sumgrad, Attribute sumGradContour ) {
			
			this.node = node;
			
			this.perimeter = perimeter;
			
			this.sumgrad = sumgrad;
			
			this.sumGradContour = sumGradContour;
			
			if( node instanceof NodeToS ) {
				
				is8Connected = node.isNodeMaxtree() == true;							
				
				xmin = node.getXmin();
				int xmax = node.getXmax();
				int ymax = node.getYmax();
				ymin = node.getYmin();
				imgBin = new boolean[ xmax-xmin+1 ][ ymax-ymin+1 ];			
				
				NodeToS n = ( NodeToS ) node;	
				
				for( int p: n.getPixelsOfCC() ) {
					
					int px = (p % img.getWidth()) - xmin;
					int py = (p / img.getWidth()) - ymin;		
					
					imgBin[ px ][ py ] = true;
					
				}
				
			}
		}
		
		public void run() {
			
			double values [] = computerContourAndSumGrad( node.getPixelWithYmin() % img.getWidth()-xmin, node.getPixelWithYmin() / img.getWidth()-ymin );
			
			perimeter.value = values[ 0 ];			
			
			sumgrad.value = values[ 1 ];
			
			sumGradContour.value = values[ 1 ]/values[ 0 ];
			
		}
		
		
		private boolean isForeground(int x, int y){
			
			if( imgBin == null ) {
				
				if(!img.isPixelValid(x, y)) return false;
				
				if(node.isNodeMaxtree())
					return img.getPixel(x, y) >= node.getLevel();
				else
					return img.getPixel(x, y) <= node.getLevel();
			}
			else{
				if((x >= 0 && x < imgBin.length && y >= 0 && y < imgBin[0].length))
					return imgBin[x][y];
				else
					return false;
			}
		}
		
		double [] computerContourAndSumGrad ( int xS, int yS ) {
			
			int xT, yT; 
			int xP, yP; 
			int xC, yC;
			
			double perimeter = 1;
			
			double sumgrad = imgGrad.getPixel( xS + xmin, yS + ymin );
			
			Pixel pt = new Pixel( xS, yS, 0 ); 
			int dNext = findNextPoint( pt, 0 );
						
			xP = xS; yP = yS;
			xC = xT = pt.x;
			yC = yT = pt.y;								
			
			boolean done = (xS==xT && yS==yT);
			
			while (!done) {
				
				dNext = findNextPoint(pt, (dNext + 6) % 8);
				xP = xC;  
				yP = yC;	
				xC = pt.x; 
				yC = pt.y; 
				done = (xP==xS && yP==yS && xC==xT && yC==yT);
				
				if ( !done ) {									
					
					sumgrad += imgGrad.getPixel( pt.x + xmin, pt.y + ymin );

					if(dNext % 2 ==0)
						perimeter += 1;
					else
						perimeter += Math.sqrt(2);
					
				}			
				
			}
			
			return new double [] { perimeter, sumgrad };
			
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


