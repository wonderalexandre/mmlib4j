package mmlib4j.representation.tree.attribute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import mmlib4j.datastruct.PriorityQueueDial;
import mmlib4j.filtering.EdgeDetectors;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.BuilderComponentTree;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.BuilderTreeOfShape;
import mmlib4j.representation.tree.tos.BuilderTreeOfShapeByUnionFindParallel;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;
import mmlib4j.utils.Utils;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 *
 * Implementação do cálculo do atributo de energia para tree of shapes descrito em: 
 * 
 * Yongchao Xu, Thierry Géraud, Laurent Najman
 * Hierarchical image simplification and segmentation based on Mumford-Shah-salient level line selection
 * 
 * 
 */

public class ComputerXuAttribute {	
	
	
	private final static int NIL = -1;
	
	private final static int px[] = new int[]{1, 0,-1, 0};
	
  	private final static int py[] = new int[]{0, 1, 0,-1};
  	
  	
  	/* Attributes */
  	
	private Attribute contourLength[];
	
	private Attribute sumGrad[];
	
	private Attribute area[];
	
	private Attribute volume[];
	
	private Attribute mumfordShaEnergy[];
	
	private Attribute sumGradContour[];
	
	
//	private int appear[];
	
//	private int vanish[];
	
	
	private GrayScaleImage img;
	
	private int is_boundary[];	
	
	NodeLevelSets rootTree;
	
	int numNode;
	
	private GrayScaleImage imgGrad;
	
	
	/* Auxiliary structures */
	
	private double areaR[];
	
	private double volumeR[];
	
		
	/* Nodes */
	
	private NodeLevelSets [] nodes;
	
		
	/* Children */
	
	private Children [] Ch;
	
	public ComputerXuAttribute( BuilderComponentTree builder ) {
		
		long ti = System.currentTimeMillis();	
		
		ComponentTree componentTree = new ComponentTree( builder.getClone() );
		
		this.img = componentTree.getInputImage();
		
		rootTree = componentTree.getRoot();
		
		this.numNode = componentTree.getNumNode();
		
		HashSet<NodeCT> hashSet = builder.getListNodes();
		
		contourLength = new Attribute[ numNode ];
		
		sumGrad = new Attribute[ numNode ];
		
		area = new Attribute[ numNode ];
		
		volume = new Attribute[ numNode ];		
		
		for( NodeCT node : hashSet ) {
			
			contourLength[ node.getId() ] = node.getAttribute( Attribute.PERIMETER_EXTERNAL ); 
			
			sumGrad[ node.getId() ] = node.getAttribute( Attribute.SUM_GRAD );
			
			area[ node.getId() ] = node.getAttribute( Attribute.AREA );
			
			volume[ node.getId() ] = node.getAttribute( Attribute.VOLUME );
			
		}
		
		run();
		
		if( Utils.debug ) {
			
			long tf = System.currentTimeMillis();
			
			System.out.println( "Tempo de execucao [extraction of attribute - based on mumford-sha-energy]  " + ( ( tf - ti ) / 1000.0 )  + "s" );
			
		}	
		
	}
	
	public ComputerXuAttribute( BuilderTreeOfShape builder ) {
		
		long ti = System.currentTimeMillis();
		
		TreeOfShape treeOfShape = new TreeOfShape( builder.getClone() );		
		
		this.img = treeOfShape.getInputImage();				
		
		rootTree = treeOfShape.getRoot();
		
		this.numNode = treeOfShape.getNumNode();	
		
		/*HashSet<NodeToS> n = treeOfShape.getListNodes();			
		
		for( NodeToS node : n ) {						
			
			System.out.println( "Id : " + node.getId() );
			
		}*/	
		
		imgGrad = EdgeDetectors.sobel( img );
		
		contourLength = new Attribute[ numNode ];				
		
		sumGrad = new Attribute[ numNode ];
		
		area = new Attribute[ numNode ];
		
		volume = new Attribute[ numNode ];		
				 
		/*appear = new int[ img.getWidth() * img.getHeight() ];
		
		vanish = new int[ img.getWidth() * img.getHeight() ];*/		
		
		is_boundary = new int[ img.getWidth() * img.getHeight() ];		
		
		for( int i = 0 ; i < is_boundary.length ; i++ ) {
			
			is_boundary[ i ] = NIL;
			
			/*appear[ i ] = NIL;
			
			vanish[ i ] = NIL;*/
			
		}			
		
		computerAttribute( rootTree );
		
		run();
		
		if( Utils.debug ) {
			
			long tf = System.currentTimeMillis();
			
			System.out.println( "Tempo de execucao [extraction of attribute - based on mumford-sha-energy]  " + ( ( tf - ti ) / 1000.0 )  + "s" );
			
		}				
		
	}
	
	private void run() {			
		
		/* Pre-processing Energy calculation */
				
		areaR = new double[ numNode ];
		
		volumeR = new double[ numNode ];		
		
		//Ch = new ArrayList[ numNode ];			
				
		Ch = new Children[ numNode ];
		
		nodes = new NodeLevelSets[ numNode ];
						
		preProcessing( rootTree );	
		
		/*for( int i = 0 ; i < Ch.length; i++ ) {
			
			if( Ch[ i ] != null ) {
				
				System.out.print( "Id : " + i + " " );
			
				Ch[ i ].printChildren();
			
			}
			
		}*/
		
		/* Remove accumulated information in attributes based on region */
		
		removeChildrenAttribute( rootTree );	
		
		/* Energy calculation */
		
		sumGradContour = new Attribute[ numNode ];
		
		mumfordShaEnergy = new Attribute[ numNode ];			
		
		prepareEnergy( rootTree );
		
		/* Sort nodes in increasing gradient contour magnitude sum order and calculate energy attribute  */		
		
		calculateEnergy( sortNodes( numNode ) );
		
	}
	
	private void prepareEnergy( NodeLevelSets node ) {
		
		List<NodeLevelSets> children = node.getChildren();		
		
		int id = node.getId(); 
		
		/* Allocation */
		
		sumGradContour[ id ] = new Attribute( Attribute.SUM_GRAD_CONTOUR , 0 );
		
		mumfordShaEnergy[ id ] = new Attribute( Attribute.MUMFORD_SHA_ENERGY , 0 );
				
		/* Calculation */
		
		sumGradContour[ id ].value = sumGrad[ id ].value / contourLength[ id ].value;
		
		NodeLevelSets nodep = node.getParent();	
		
		if( nodep != null ) {
		
			int idp = nodep.getId();	
			
			double p1 = pow2( volumeR[ id ] ) / areaR[ id ];
			
			double p2 = pow2( volumeR[ idp ] ) / areaR[ idp ];
			
			double p3 = pow2( volumeR[ id ] + volumeR[ idp ] ) / ( areaR[ id ] + areaR[ idp ] );
			
			mumfordShaEnergy[ id ].value = ( p1 + p2 - p3 ) / contourLength[ id ].value;			
		
		}
		
		for( NodeLevelSets son : children ) {
			
			prepareEnergy( son );
			
		}
		
	}
	
	private double pow2( double v ) {
		
		return v*v;
		
	}

	private void removeChildrenAttribute( NodeLevelSets node ) {
				
		List<NodeLevelSets> children = node.getChildren();		
		
		if( node.getParent() != null && node.getParent().getId() != 0 ) {
		
			areaR[ node.getParent().getId() ] -= area[ node.getId() ].value;
		
			volumeR[ node.getParent().getId() ] -= volume[ node.getId() ].value;
		
		}
			
		for( NodeLevelSets son : children ) {
			
			removeChildrenAttribute( son );
			
		}
		
	}
	
	private void preProcessing( NodeLevelSets node ) {
				
		List<NodeLevelSets> children = node.getChildren();				
		
		areaR[ node.getId() ] = area[ node.getId() ].value;
		
		volumeR[ node.getId() ] = volume[ node.getId() ].value;	
				
		if( node.getParent() != null ) {
			
			if( Ch[ node.getParent().getId() ] == null ) {
				
				Ch[ node.getParent().getId() ] = new Children( children.size() );
				
			}
			
			Ch[ node.getParent().getId() ].insert( node.getId() );	
			
		}			
		
		nodes[ node.getId() ] = node;			
		
		for( NodeLevelSets son : children ) {
			
			preProcessing( son );
			
		}
		
	}
	

	public void computerAttribute( NodeLevelSets node ) {
		
		List<NodeLevelSets> children = node.getChildren();
			
		for( NodeLevelSets son : children ) {
			
			computerAttribute( son );
			
		}
				
		contourLength[ node.getId() ] = new Attribute( Attribute.CONTOUR_LENGTH, 0 );
		
		sumGrad[ node.getId() ] = new Attribute( Attribute.SUM_GRAD, 0 );
		
		area[ node.getId() ] = new Attribute( Attribute.FACE_2_AREA, 0 );
		
		volume[ node.getId() ] = new Attribute( Attribute.FACE_2_VOLUME, 0 );					
		
		if( node instanceof NodeToS ) {
				
			NodeToS n = ( NodeToS ) node;
			
			for( int p: n.getPixelsOfCC() ) {	
						
				if( isFace2( p ) ) {						
					
					area[ node.getId() ].value++;
					
					volume[ node.getId() ].value += img.getPixel( p );															
					
					for( int e : getBoundaries( p ) ) {
													
						if( is_boundary[ e ] != node.getId() ) {
								
							is_boundary[ e ] = node.getId();
								
							contourLength[ node.getId() ].value++;
							
							sumGrad[ node.getId() ].value += imgGrad.getPixel( e );							
							
						/*	if( appear[ e ] != NIL && vanish[ e ] == NIL ) {
								
								vanish[ e ] = p; 
								
							}
							
							
							if( appear[ e ] == NIL ) {
							
								appear[ e ] = p; 
							
							}*/
															
						} else {
														
							is_boundary[ e ] = NIL;
								
							contourLength[ node.getId() ].value--;
							
							sumGrad[ node.getId() ].value -= imgGrad.getPixel( e );							
							
						/*	if( vanish[ e ] == NIL ) {
							
								vanish[ e ] = p;
							
							}*/
															
						}
						
					}
						
				}
				
			}	
			
		}					
		
	}
	
	private void calculateEnergy( int [] Rt ) {
				
		double mumfordShaEnergyTmp;		
		
		for( int i = 0 ; i < Rt.length ; i++ ) {			
			
			int id = Rt[ i ];			
			
			NodeLevelSets t = nodes[ id ];
			
			NodeLevelSets tp = nodes[ id ].getParent();		
			
			double p1 = pow2( volumeR[ t.getId() ] ) / areaR[ t.getId() ];
			
			double p2 = pow2( volumeR[ tp.getId() ] ) / areaR[ tp.getId() ];
			
			double p3 = pow2( volumeR[ t.getId() ] + volumeR[ tp.getId() ] ) / ( areaR[ t.getId() ] + areaR[ tp.getId() ] );
			
			mumfordShaEnergyTmp = ( p1 + p2 - p3 ) / contourLength[ t.getId() ].value;																										
			
			if( mumfordShaEnergyTmp > mumfordShaEnergy[ t.getId() ].value ) {
				
				mumfordShaEnergy[ t.getId() ].value = mumfordShaEnergyTmp;
				
			}											
			
			Ch[ tp.getId() ].remove( t.getId() );
			
			if( Ch[ t.getId() ] != null ) {
			
				for( int tc : Ch[ t.getId() ].getChildren().keySet() ) {											
				
					nodes[ tc ].setParent( tp );
				
					Ch[ tp.getId() ].insert( tc );
				
				}
				
			}
			
			areaR[ tp.getId() ] = areaR[ tp.getId() ] + areaR[ t.getId() ];
			
			volumeR[ tp.getId() ] = volumeR[ tp.getId() ] + volumeR[ t.getId() ];		
			
		}
		
	}
	
	/**
	 * 
	 *
	 * 	Ordena os nós da árvore de acordo com a soma do gradiente ao longo do contorno
	 * 
	 * 
	 **/
	
	public int [] sortNodes( int numNode ) {
		
		int Rt [] = new int[ numNode-1 ];
		
		double maxPriority = sumGradContour[ 1 ].value;
		
		for( int i = 2 ; i < numNode ; i++ ) { 
			
			if( maxPriority < sumGradContour[ i ].value ) {
				
				maxPriority = sumGradContour[ i ].value;
				
			}
			
		}			
		
		//System.out.println( "Max : " + maxPriority );
		
		PriorityQueueDial queue = new PriorityQueueDial( sumGradContour, ( int ) maxPriority, PriorityQueueDial.FIFO, false ); 
		
		for( int i = 1 ; i < numNode ; i++ ) {
			
			queue.add( i, ( int ) sumGradContour[ i ].value );		
			
		}
		
		int i = 0;
		
		while( !queue.isEmpty() ) {
			
			Rt[ i ] = queue.remove();	
			
			//System.out.println( sumGradContour[ Rt[ i ] ] );
			
			i++;
			
		}		
		
		return Rt;
		
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
		
		if( node instanceof NodeCT ) {
			
			node.addAttribute( Attribute.SUM_GRAD_CONTOUR , sumGradContour[ node.getId() ] );
			node.addAttribute( Attribute.MUMFORD_SHA_ENERGY , mumfordShaEnergy[ node.getId() ] );
			
		} else {
		
			node.addAttribute( Attribute.CONTOUR_LENGTH, contourLength[ node.getId() ] );
			node.addAttribute( Attribute.SUM_GRAD, sumGrad[ node.getId() ] );
			node.addAttribute( Attribute.FACE_2_AREA, area[ node.getId() ] );
			node.addAttribute( Attribute.FACE_2_VOLUME, volume[ node.getId() ] );
			node.addAttribute( Attribute.SUM_GRAD_CONTOUR , sumGradContour[ node.getId() ] );
			node.addAttribute( Attribute.MUMFORD_SHA_ENERGY , mumfordShaEnergy[ node.getId() ] );
		
		}
		
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
	 * Devolve a lista de pixels do contorno do pixel de referencia. 
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
				        	if(xx >= 0 && xx < img.getWidth() && yy >= 0 && yy < img.getHeight())
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
   
    /**
     *
     * 
     *	Classe que representa os filhos de um dado no da árvore
     * 	são armazenados em um HashMap
     * 
     * 
     **/
    
    private class Children { 
    	
    	private HashMap<Integer, Integer> children;
    	
    	public Children( int size ) {
    		
    		this.children = new HashMap<>( size );
    		
    	}
    	
    	public HashMap<Integer, Integer> getChildren() {
    		
    		return children;
    		
    	}
    	    	
    	public void insert( int key ) {
    		
    		children.put( key, key );
    		
    	}
    	
    	public int remove( int key ) {
    		
    		return children.remove( key );
    		
    	}
    	    	
    	public void printChildren() {
    		
    		System.out.print( "Children : " );
    		
    		for( int son : children.keySet() ) {
    			
    			System.out.print( son + ", " );
    			
    		}
    		
    		System.out.println();
    		
    	}
    	
    }
    
}
