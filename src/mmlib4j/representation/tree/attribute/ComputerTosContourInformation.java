package mmlib4j.representation.tree.attribute;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import mmlib4j.filtering.EdgeDetectors;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.utils.Utils;

public class ComputerTosContourInformation {	
	
	
	private final static int NIL = -1;
	
	private final static int px[] = new int[]{1, 0,-1, 0};
	
  	private final static int py[] = new int[]{0, 1, 0,-1};
  	
  	
  	/* Attributes */
  	
	private Attribute contourLength[];
	
	private Attribute sumGrads[];
	
	private int appear[];
	
	
	private GrayScaleImage img;
	
	private int is_boundary[];	
	
	NodeLevelSets rootTree;
	
	private GrayScaleImage imgGrad;
	
	
	public ComputerTosContourInformation( int numNode, NodeLevelSets root, GrayScaleImage img ) {
		
		
		long ti = System.currentTimeMillis();
		
		
		this.img = img;				
		
		this.rootTree = root;
		
		this.imgGrad = EdgeDetectors.sobel( img );
		
		contourLength = new Attribute[ numNode ];				
		
		sumGrads = new Attribute[ numNode ];
		
		appear = new int[ img.getWidth() * img.getHeight() ];
		
		
		is_boundary = new int[ img.getWidth() * img.getHeight() ];
		
		
		for( int i = 0 ; i < is_boundary.length ; i++ ) {
			
			is_boundary[ i ] = NIL;
			
		}			
		
		computerAttribute( root );		
				
		if( Utils.debug ) {
			
			long tf = System.currentTimeMillis();
			
			System.out.println("Tempo de execucao [extraction of attribute - based on perimeter external]  "+ ((tf - ti) /1000.0)  + "s");
			
		}
		
	}
	

	public void computerAttribute( NodeLevelSets node ) {
		
		
		List<NodeLevelSets> children = node.getChildren();
		
		contourLength[ node.getId() ] = new Attribute( Attribute.CONTOUR_LENGTH );
		
		sumGrads[ node.getId() ] = new Attribute( Attribute.SUM_GRAD );
		
			
		if( node instanceof NodeToS ) {
				
			NodeToS n = ( NodeToS ) node;
			
			for( int p: n.getPixelsOfCC() ) {		
				
				if( isFace2( p ) ) {
						
					for( int e : getBoundaries( p ) ) {
							
						if( is_boundary[ e ] != node.getId() ) {
								
							is_boundary[ e ] = node.getId();
								
							contourLength[ node.getId() ].value++;
							
							sumGrads[ node.getId() ].value += imgGrad.getPixel( e );
							
							appear[ e ] = p; 
								
						} else {
								
							is_boundary[ e ] = NIL;
								
							contourLength[ node.getId() ].value--;
							
							sumGrads[ node.getId() ].value -= imgGrad.getPixel( e );
								
						}
						
					}
						
				}
				
			}	
			
		}	
		
		for( NodeLevelSets son: children ) {
			
			computerAttribute( son );
			
		}
		
	}
	
	
	public Attribute[] getAttribute() {
		
		return contourLength;
		
	}
	
	public void addAttributeInNodesCT( HashSet<NodeCT> list ) {
		
		for( NodeCT node: list ) {
			
			addAttributeInNodes( node );
			
		}
		
	}
	
	public void addAttributeInNodesToS( HashSet<NodeToS> hashSet ) {
		
		for( NodeLevelSets node: hashSet ) {
			
			addAttributeInNodes( node );
			
		}
		
	} 
	
	public void addAttributeInNodes( NodeLevelSets node ) {
		
		node.addAttribute( Attribute.CONTOUR_LENGTH, contourLength[ node.getId() ] );
		node.addAttribute( Attribute.SUM_GRAD, sumGrads[ node.getId() ] );
		
	}
	
    public boolean isFace2( int p ) {
    	
    	return isRealPixel( p ) || isFakePixelNextLine( p ) || isFakePixelSameLine( p );
    	
    }
    
    public boolean isFace2( int x, int y ) {
    	
    	return isRealPixel( x, y ) || isFakePixelNextLine( x, y ) || isFakePixelSameLine( x, y );
    	
    }
    
	public boolean isRealPixel( int p ) {
		
		return isRealPixel( p%img.getWidth(), p/img.getWidth() );
		
	}
	
	public boolean isRealPixel( int x, int y ) {
		
		return ( x%4 == 1 && y%4 == 1 );
		
	}
	
	public boolean isFakePixelSameLine( int p ) {
		
		return isFakePixelSameLine( p%img.getWidth(), p/img.getWidth() );
		
	} 
	
	public boolean isFakePixelSameLine( int x, int y ) {
		
		return !( x % 4 == 1 ) && ( x%2 == 1 && y%2 == 1 && y%4 == 1 );
		
	}
	
	public boolean isFakePixelNextLine( int p ) {
		
		return isFakePixelNextLine( p%img.getWidth(), p/img.getWidth() );
		
	}
	
	public boolean isFakePixelNextLine( int x, int y ) {
		
		return ( x%2 == 1 && ( y-1 )%2 == 0 ) && !( y % 4 == 1 );
		
	}
	
	public boolean isCircle( int x, int y ) {
		
		return ( x%2 == 0 && y%2 == 0 );
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
	
	/**
	 * Devolve a lista de pixels do contorno do pixel de referencia, se o pixel não é um contorno do grid. 
	 * @param p => pixel de referencia
	 * @return pixel do contorno e
	 */
    public Iterable<Integer> getBoundaries( int p ) {
    	
    	final int x = p % img.getWidth();
    	final int y = p / img.getWidth();
    	
        return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private int i = 0;
					public boolean hasNext() {
						while( i < px.length ) {
							int xx = px[i] + x;
							int yy = py[i] + y;							
				        	if(xx >= 0 && xx < img.getWidth() && yy >= 0 && yy < img.getWidth())
								return true;
				        	i++;
						} 
						return false;
					}
					public Integer next() {
						int pixel = (px[i] + x) + (py[i] + y) * img.getWidth();
						i++;
						return pixel;
					}
					public void remove() { }
					
				};
			}
		};
    }

}
