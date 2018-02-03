package mmlib4j.representation.tree.attribute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import mmlib4j.datastruct.PriorityQueueDial;
import mmlib4j.filtering.EdgeDetectors;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.BuilderComponentTree;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.BuilderTreeOfShape;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.Utils;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 *
 * Implementação do cálculo do atributo de energia para tree of shapes e component tree descrito em: 
 * 
 * Yongchao Xu, Thierry Géraud, Laurent Najman
 * Hierarchical image simplification and segmentation based on Mumford-Shah-salient level line selection
 * 
 */

public class ComputerXuAttribute {
  	
  	/* Attribute */
	
	private Attribute mumfordShaEnergy[];
	
	private NodeLevelSets rootTree;
	
	private int numNode;
	
	private double maxEnergy = Double.MIN_VALUE;
	
	private boolean useHeuristic = false;
		
	/* Nodes */
	
	private NodeLevelSets [] mapNodes;
	
	private double areaR[];
	
	private double volumeR[];	
		
	/* Children */
	
	private Children [] Ch;
		
	public ComputerXuAttribute( ComponentTree componentTree, boolean useHeuristic ) {
		
		long ti = System.currentTimeMillis();				
		
		//ComponentTree componentTree = new ComponentTree( builder.getClone() );
		
		rootTree = componentTree.getRoot();
		
		this.numNode = componentTree.getNumNode();
		
		this.useHeuristic = useHeuristic;
		
		run( componentTree );
		
		Ch = null;
		
		mapNodes = null;
		
		if( Utils.debug ) {
			
			long tf = System.currentTimeMillis();
			
			System.out.println( "Tempo de execucao [extraction of attribute - based on mumford-sha-energy]  " + ( ( tf - ti ) / 1000.0 )  + "s" );
			
		}	
		
	}
	
	public ComputerXuAttribute( TreeOfShape treeOfShape, boolean useHeuristic ) {
		
		long ti = System.currentTimeMillis();
		
		//TreeOfShape treeOfShape = new TreeOfShape( builder.getClone() );		
		
		rootTree = treeOfShape.getRoot();
		
		this.numNode = treeOfShape.getNumNode();
		
		this.useHeuristic = useHeuristic;
		
		run( treeOfShape );
		
		if( Utils.debug ) {
			
			long tf = System.currentTimeMillis();
			
			System.out.println( "Tempo de execucao [extraction of attribute - based on mumford-sha-energy]  " + ( ( tf - ti ) / 1000.0 )  + "s" );
			
		}				
		
	}
	
	private void run( MorphologicalTree tree ) {			
		
		/* Pre-processing Energy calculation */	
		
		areaR = new double[ numNode ];
		
		volumeR = new double[ numNode ];
		
		getChildrenAttribute( rootTree );
				
		Ch = new Children[ numNode ];
		
		mapNodes = new NodeLevelSets[ numNode ];
						
		preProcessing( rootTree );
		
		/* Energy calculation */
		
		mumfordShaEnergy = new Attribute[ numNode ];			
		
		prepareEnergy( rootTree );
		
		/* Sort nodes in increasing gradient contour magnitude sum order and calculate energy attribute  */		
		
		if( useHeuristic ) {
			
			calculateEnergyByHeuristic( sortNodes( numNode ) );
		
		} else {
		
			calculateEnergy( tree );
			
		}
		
	}
	
	private void getChildrenAttribute( NodeLevelSets node ) {
		
		List<NodeLevelSets> children = node.getChildren();		
			
		areaR[ node.getId() ] = node.getAttributeValue( Attribute.AREA );
		
		volumeR[ node.getId() ] = node.getAttributeValue( Attribute.VOLUME );		
		
		for( NodeLevelSets son : children ) {
			
			getChildrenAttribute( son );
			
		}
		
	}

	private void prepareEnergy( NodeLevelSets node ) {
		
		List<NodeLevelSets> children = node.getChildren(); 
		
		/* Allocation */
		
		mumfordShaEnergy[ node.getId() ] = new Attribute( Attribute.MUMFORD_SHA_ENERGY , 0 );
				
		/* Calculation */
		
		NodeLevelSets parent = node.getParent();	
		
		if( parent != null ) {
			
			mumfordShaEnergy[ node.getId() ].value = calculateFuncional( node );
			
			if( maxEnergy < mumfordShaEnergy[ node.getId() ].value ) {
				
				maxEnergy = mumfordShaEnergy[ node.getId() ].value;
				
			}
			
		}
		
		for( NodeLevelSets son : children ) {
			
			prepareEnergy( son );
			
		}
		
	}
	
	private double pow2( double v ) {
		
		return v*v;
		
	}
	
	private double calculateFuncional( NodeLevelSets node ) { 
		
		NodeLevelSets parent = node.getParent();
		
		double p1 = pow2( volumeR[ node.getId() ] ) / areaR[ node.getId() ];
		
		double p2 = pow2( volumeR[ parent.getId() ] ) / areaR[ parent.getId() ];
		
		double p3 = pow2( volumeR[ node.getId() ] + volumeR[ parent.getId() ] ) / ( areaR[ node.getId() ] + areaR[ parent.getId() ] );
		
		return ( p1 + p2 - p3 ) / node.getAttributeValue( Attribute.PERIMETER_EXTERNAL );
		
	}
	
	
	private void preProcessing( NodeLevelSets node ) {
		
		List<NodeLevelSets> children = node.getChildren();	
		
		NodeLevelSets parent = node.getParent();
				
		if( parent != null ) {
			
			if( node.getParent() != rootTree ) {
				
				areaR[ node.getParent().getId() ] -= areaR[ node.getId() ];
				
				volumeR[ node.getParent().getId() ] -= volumeR[ node.getId() ];
			
				/*parent.getAttribute( Attribute.AREA )
					  .setValue( parent.getAttributeValue(Attribute.AREA) - node.getAttributeValue(Attribute.AREA) );
			
				parent.getAttribute( Attribute.VOLUME )
				      .setValue( parent.getAttributeValue(Attribute.VOLUME) - node.getAttributeValue(Attribute.VOLUME) );*/
			
			}
			
			if( Ch[ parent.getId() ] == null ) {
				
				Ch[ parent.getId() ] = new Children( children.size() );
				
			}			
			
			Ch[ parent.getId() ].insert( node.getId() );	
			
		}			
		
		mapNodes[ node.getId() ] = node;			
		
		for( NodeLevelSets son : children ) {
			
			preProcessing( son );
			
		}
		
	}
	
	private void calculateEnergyByHeuristic( int [] Rt ) {
				
		double mumfordShaEnergyTmp;		
		
		for( int i = 0 ; i < Rt.length ; i++ ) {			
						
			int id = Rt[ i ];							
			
			NodeLevelSets node = mapNodes[ id ];
			
			NodeLevelSets parent = mapNodes[ id ].getParent();		
			
			mumfordShaEnergyTmp = calculateFuncional( node );
			
			if( mumfordShaEnergyTmp > mumfordShaEnergy[ node.getId() ].value ) {
				
				mumfordShaEnergy[ node.getId() ].value = mumfordShaEnergyTmp;
				
			}											
			
			Ch[ parent.getId() ].remove( node.getId() );
			
			if( Ch[ node.getId() ] != null ) {
			
				for( int childId : Ch[ node.getId() ].getChildren().keySet() ) {											
				
					mapNodes[ childId ].setParent( parent );
				
					Ch[ parent.getId() ].insert( childId );
				
				}
				
			}
			
			areaR[ node.getParent().getId() ] += areaR[ node.getId() ];
			
			volumeR[ node.getParent().getId() ] += volumeR[ node.getId() ];
			
			/*parent.getAttribute( Attribute.AREA )
			  	  .setValue( parent.getAttributeValue(Attribute.AREA) + node.getAttributeValue(Attribute.AREA) );
	
			parent.getAttribute( Attribute.VOLUME )
		      	  .setValue( parent.getAttributeValue(Attribute.VOLUME) + node.getAttributeValue(Attribute.VOLUME) );*/		
			
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
		
		double maxPriority = Double.MIN_VALUE;
		
		for( int i = 1 ; i < numNode ; i++ ) { 
			
			if( maxPriority < mapNodes[ i ].getAttributeValue( Attribute.SUM_GRAD_CONTOUR ) ) {
				
				maxPriority = mapNodes[ i ].getAttributeValue( Attribute.SUM_GRAD_CONTOUR );
				
			}
			
		}			
		
		PriorityQueueDial queue = new PriorityQueueDial( numNode, 
														( int ) maxPriority, 
														PriorityQueueDial.FIFO, 
														false ); 
		
		for( int i = 1 ; i < numNode ; i++ ) {
			
			queue.add( i, ( int ) mapNodes[ i ].getAttributeValue( Attribute.SUM_GRAD_CONTOUR ) );		
			
		}
		
		int i = 0;
		
		while( !queue.isEmpty() ) {
			
			Rt[ i ] = queue.remove();	
			
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
		
		node.addAttribute( Attribute.MUMFORD_SHA_ENERGY , mumfordShaEnergy[ node.getId() ] );
		
	}
	
	/**
	 *   
	 *  
	 *   
	 * 
	 * */
	
	private void updateEnergy( PriorityQueueDial queue, NodeLevelSets node ) {
		
		double beforeEnergy = mumfordShaEnergy[ node.getId() ].value;
		
		double newEnergy = calculateFuncional( node );
			
		queue.remove( node.getId(), ( int ) beforeEnergy );
	
		queue.add( node.getId(), ( int ) newEnergy );
		
		mumfordShaEnergy[ node.getId() ].value = newEnergy;
		
	}	
	
	private void calculateEnergy( MorphologicalTree tree ) {
		
		PriorityQueueDial queue = new PriorityQueueDial( numNode, ( int ) maxEnergy, PriorityQueueDial.FIFO, false );
		
		/* Initializing heap */	
		
		for( int i = 1 ; i < numNode ; i++ ) {		

			queue.add( i, ( int ) mumfordShaEnergy[ i ].value );
			
		}		
		
		/* Greedy */
		
		while( !queue.isEmpty() ) {
			
			int id = queue.remove();
			
			NodeLevelSets node = mapNodes[ id ];		
				
			NodeLevelSets parent = node.getParent();
			
			tree.mergeFather( node );
				
			/* Update parameters */
				
			areaR[ parent.getId() ] += areaR[ node.getId() ];
				
			volumeR[ parent.getId() ] += volumeR[ node.getId() ];						
				
			/* Update parent energy (if it is not the root) */
			
			if( parent != rootTree ) {
				
				updateEnergy( queue, parent );
					
			}
				
			/* Update for children of parent */
			
			List<NodeLevelSets> list = parent.getChildren();
				
			for( NodeLevelSets child : list ) {
									
				updateEnergy( queue, child );
					
			}
			
			mapNodes[ id ] = null;				
			
		}	
		
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
    		
    		this.children = new HashMap<Integer, Integer>( size );
    		
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
    
    public static void main(String args[]) {
    	
    	//GrayScaleImage image = ImageBuilder.openGrayImage();  
    	
    	Utils.debug = false;
    	
    	int example[] = new int[] {    				
    		1,1,1,1,1,1,1,
    		1,0,0,3,3,3,1,
    		1,0,1,1,2,2,1,
    		1,0,0,3,3,3,1,
    		1,1,1,1,1,1,1				    		
    	};
    		
    	int width = 7;
    		
    	int height = 5;   		
    	
    	ConnectedFilteringByTreeOfShape connectedFilteringByTreeOfShape = new ConnectedFilteringByTreeOfShape( ImageFactory.createReferenceGrayScaleImage(32, example, width, height) );		
		
		connectedFilteringByTreeOfShape.computerXuAttribute( false );
    	
		System.out.println("Tree of shapes");
		
    	for( NodeLevelSets node : connectedFilteringByTreeOfShape.getListNodes()  ){
			
			System.out.printf("Id=%d Energy=%.2f\n", node.getId(), node.getAttributeValue( Attribute.MUMFORD_SHA_ENERGY ) );
			
		}

		//
    	
    	System.out.println("Component tree");
		
		ConnectedFilteringByComponentTree connectedFilteringByComponentTree = new ConnectedFilteringByComponentTree( ImageFactory.createReferenceGrayScaleImage(32, example, width, height), AdjacencyRelation.getCircular( 1 ), true );		
		
		connectedFilteringByComponentTree.computerXuAttribute( false );
		
		for( NodeLevelSets node : connectedFilteringByComponentTree.getListNodes() ) {
			
			System.out.printf("Id=%d Energy=%.2f\n", node.getId(), node.getAttributeValue( Attribute.MUMFORD_SHA_ENERGY ) );
			
		}
		
		System.err.println( "Finished" );
    	
    }
    
}
