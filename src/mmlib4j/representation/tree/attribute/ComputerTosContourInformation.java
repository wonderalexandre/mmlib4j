package mmlib4j.representation.tree.attribute;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.filtering.EdgeDetectors;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.ComputerAttributeBasedPerimeterExternal.ThreadNodeCTPerimeter;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.utils.Pixel;
import mmlib4j.utils.Utils;

public class ComputerTosContourInformation {
	
	private ThreadPoolExecutor pool;
	
	private Attribute contourLength[];
	
	private Attribute sumGrads[];
	
	private GrayScaleImage img;
	
	private static final int[][] delta = { { 1,0}, { 1, 1}, {0, 1}, {-1, 1}, {-1,0}, {-1,-1}, {0,-1}, { 1,-1} };
	
	NodeLevelSets rootTree;
	
	/* Gobber add */
	
	private GrayScaleImage imgGrad;
	
	public ComputerTosContourInformation( int numNode, NodeLevelSets root, GrayScaleImage img ) {
		
		long ti = System.currentTimeMillis();
		
		this.img = img;		
		
		
		this.rootTree = root;
		
		contourLength = new Attribute[ numNode ];	
		
		/* Gobber add */
		this.imgGrad = EdgeDetectors.sobel( img );
		sumGrads = new Attribute[ numNode ];
		
		computerAttribute( root );		
		
		if( Utils.debug ) {
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [extraction of attribute - based on perimeter external]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}
	

	public void computerAttribute( NodeLevelSets node ) {
		
		List<NodeLevelSets> children = node.getChildren();
		
		contourLength[node.getId()] = new Attribute( Attribute.PERIMETER_EXTERNAL );
		
		if( node == rootTree ) {
			
			contourLength[ node.getId() ].value = img.getWidth() * 2 + img.getHeight() * 2;
			
		} else {
			
				new ThreadNodeCTPerimeter( node, contourLength[ node.getId() ] ).run();
				//pool.execute(new ThreadNodeCTPerimeter(node, perimeters[node.getId()]));
		}
		
		for( NodeLevelSets son: children ) {
			
			computerAttribute( son );
			
		}
		
	}
	
	
	public Attribute[] getAttribute(){
		return contourLength;
	}
	
	public void addAttributeInNodesCT(HashSet<NodeCT> list){
		for(NodeCT node: list){
			addAttributeInNodes(node);
		}
	}
	
	public void addAttributeInNodesToS(HashSet<NodeToS> hashSet){
		
		for(NodeLevelSets node: hashSet) {
			
			addAttributeInNodes(node);
			
		}
		
	} 
	
	public void addAttributeInNodes(NodeLevelSets node){
		
		node.addAttribute( Attribute.CONTOUR_LENGTH, contourLength[ node.getId() ] );
		node.addAttribute( Attribute.CIRCULARITY, new Attribute( Attribute.CIRCULARITY, getCircularity( node ) ) );
		node.addAttribute( Attribute.COMPACTNESS, new Attribute( Attribute.COMPACTNESS, getCompacity( node ) ) );
		node.addAttribute( Attribute.ELONGATION, new Attribute( Attribute.ELONGATION, getElongation( node ) ) );
		
		/* Gobber add */
		
		//node.addAttribute( Attribute.SUM_GRAD, sumGrads[ node.getId() ] );		
		
	}
	
	public double getCircularity(NodeLevelSets node){
		
		return (4.0 * Math.PI * node.getArea()) / Math.pow(contourLength[node.getId()].getValue(), 2);
		
	}
	
	public double getCompacity(NodeLevelSets node){
		
		return Math.pow(contourLength[node.getId()].getValue(), 2) / node.getArea();
		
	}
	
	public double getElongation(NodeLevelSets node){
		
		return node.getArea() / Math.pow(contourLength[node.getId()].getValue(), 2);
		
	}
	
	//int cont;
	class ThreadNodeCTPerimeter extends Thread {
		
		private NodeLevelSets node;
		private Attribute contourLength;
		boolean imgBin[][];
		boolean is8Connected = true;
		int xmin, ymin;
		
		public ThreadNodeCTPerimeter(NodeLevelSets node, Attribute contourLength) {
			
			this.node = node;
			
			this.contourLength = contourLength;
			
			if( node instanceof NodeToS ) {
				
				is8Connected = node.isNodeMaxtree() == true;							
				
				xmin = node.getXmin();
				
				int xmax = node.getXmax();
				
				int ymax = node.getYmax();
				
				ymin = node.getYmin();
				
				imgBin = new boolean[ xmax-xmin+1 ][ ymax-ymin+1 ];			
				
				NodeToS n = ( NodeToS ) node;
				
				System.out.printf( "width = %d, height = %d \n" , xmax-xmin+1, ymax-ymin+1 );		
				
				for( int p: n.getPixelsOfCC() ) {
					
					int px = (p % img.getWidth()) - xmin;
					int py = (p / img.getWidth()) - ymin;		
					
				//	System.out.println( px + " " + py );
					
					imgBin[ px ][ py ] = true;
					
				}
				
			}
		}
		
		public void run() {
			
			contourLength.value = computerContour( node.getPixelWithYmin() % img.getWidth()-xmin, node.getPixelWithYmin() / img.getWidth()-ymin );
		
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
		
		double computerContour ( int xS, int yS ) {
			
			int xT, yT; 
			int xP, yP; 
			int xC, yC; 
			double contourLength = 1;
			Pixel pt = new Pixel( xS, yS, 0 ); 
			int dNext = findNextPoint( pt, 0 );
						
			xP = xS; yP = yS;
			xC = xT = pt.x;
			yC = yT = pt.y;			
			
			int cont = 1;
			
			//System.out.printf( "( px=%d, py=%d )", pt.x, pt.y );
			
			
			/* Gobber add */
			
			/*if( ( pt.x + xmin ) < img.getWidth() && ( pt.y + ymin ) < img.getHeight() ) {
		
				sumGrads[ node.getId() ].value += imgGrad.getPixel( pt.x + xmin, pt.y + ymin );
			
			}*/
			
			System.out.printf( "( px=%d, py=%d )\n", xS+xmin, yS+ymin );
			
			if( isFace1( pt.x + xmin, pt.y + ymin ) ) {
				
				contourLength +=1 ;
				
			}
			
			boolean done = (xS==xT && yS==yT);			
			
			while ( !done ) {
				//pt.x = xC;
				//pt.y = yC;
				
				dNext = findNextPoint(pt, (dNext + 6) % 8);
				xP = xC;  
				yP = yC;	
				xC = pt.x; 
				yC = pt.y; 
				done = (xP==xS && yP==yS && xC==xT && yC==yT);
				
				if ( !done ) {
					
					//System.out.printf( "( px=%d, py=%d )\n", pt.x+xmin, pt.y+ymin );
					
					cont++;
					
					if( isFace1( pt.x + xmin, pt.y + ymin ) ) {
						
						contourLength +=1 ;
						
					}
					
				}	
				
			}
			
			System.out.println( "Level >> " + node.getLevel() );
			
			System.out.println( "Pixels >>> " + cont );
			
			return contourLength;
		}
		

		int findNextPoint ( Pixel pt, int direction ) { 
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
	
	public boolean isFace1( int p ) {
		
		return isHorizontalRect( p ) || isVerticalRect( p );
		
	}
	
	public boolean isFace1( int x, int y ) {
		
		return isHorizontalRect( x, y ) || isVerticalRect( x, y );
		
	}

	public boolean isHorizontalRect( int p ) {
		
		return isHorizontalRect( p%img.getWidth(), p/img.getWidth() );
		
	}
	
	public boolean isVerticalRect( int p ) { 
		
		return isVerticalRect( p%img.getWidth(), p/img.getWidth() );
		
	}
	
	public boolean isHorizontalRect( int x, int y ) {
		
		return ( x%2 == 1 && y%2 == 0 );
		
	}
	
	public boolean isVerticalRect( int x, int y ) { 
		
		return ( x%2 == 0 && y%2 == 1 );
		
	}

}
