package mmlib4j.representation.tree.attribute;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import mmlib4j.datastruct.PriorityQueueDial;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.BuilderComponentTree;
import mmlib4j.representation.tree.componentTree.BuilderComponentTreeByUnionFind;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.Utils;

public class ComputerFunctionalVariational {
	
	private int numNode;
	
	private NodeLevelSets rootTree;
	
	private double scale = 1;
	
	/* Tree */
	
	ComponentTree componentTree;
	
	/* Auxiliary structures */
	
	private NodeLevelSets mapNodes[];
	
	private double areaR[];
	
	private double volumeR[];
	
	private double contourLength[];
	
	private double maxEnergy = Double.MIN_VALUE;
	
	/* Image */
	
	private GrayScaleImage simplifiedImage;
	
	/* Attribute */
	
	private Attribute functionalVariational [];
	
	public ComputerFunctionalVariational( BuilderComponentTree builder, double scale ) {
		
		long ti = System.currentTimeMillis();
		
		componentTree = new ComponentTree( builder.getClone() );
		
		this.rootTree = componentTree.getRoot();
		
		this.numNode = componentTree.getNumNode();
		
		this.scale = scale;	
		
		/* Map nodes */
		
		mapNodes = new NodeLevelSets[ numNode ];
		
		/* Pre-processing Energy calculation */
		
		areaR = new double[ numNode ];
		
		volumeR = new double[ numNode ];
		
		contourLength = new double[ numNode ];				
		
		HashSet<NodeCT> hashSet = builder.getListNodes();
		
		for( NodeCT node : hashSet ) {			
			
			areaR[ node.getId() ] = node.getAttributeValue( Attribute.AREA );
			
			volumeR[ node.getId() ] = node.getAttributeValue( Attribute.VOLUME );
			
			contourLength[ node.getId() ] = node.getAttributeValue( Attribute.PERIMETER_EXTERNAL );				
			
		}
		
		run();
		
		if( Utils.debug ) {
			
			long tf = System.currentTimeMillis();
			
			System.out.println( "Tempo de execucao [extraction of attribute - based on functional variactional]  " + ( ( tf - ti ) / 1000.0 )  + "s" );
			
		}	
		
	}
	
	private void run() {	
		
		/* Remove values accumulated */
		
		removeChildrenAttribute( rootTree );		
		
		/* Energy */
		
		functionalVariational = new Attribute[ numNode ];
		
		prepareEnergy( rootTree );	
		
		/*for( int  i = 1 ;  i < numNode ; i++ ){
			
			System.out.println( "Level" + mapNodes[ i ].getLevel() + "Energy" + functionalVariactional[ i ] );
			
		}*/
		
		/* Calculate energy */
		
		calculateEnergy();
		
	}
	
	private void removeChildrenAttribute( NodeLevelSets node ) {
		
		List<NodeLevelSets> children = node.getChildren();		
		
		mapNodes[ node.getId() ] = node;
		
		if( node.getParent() != null && node != rootTree ) {
			
			areaR[ node.getParent().getId() ] -= areaR[ node.getId() ];
		
			volumeR[ node.getParent().getId() ] -= volumeR[ node.getId() ];
		
		}
		
		for( NodeLevelSets son : children ) {
			
			removeChildrenAttribute( son );
			
		}
		
	}
	
	/* Calculate energy */
	
	private double calculateVariational( NodeLevelSets node ) {
		
		double t1 = pow2( volumeR[ node.getId() ] ) / areaR[ node.getId() ];
		
		double t2 = pow2( volumeR[ node.getParent().getId() ] ) / areaR[ node.getParent().getId() ];
		
		//double t3 = pow2( volumeR[ node.getId() ] + volumeR[ node.getParent().getId() ] ) / ( volumeR[ node.getId() ] + volumeR[ node.getParent().getId() ] );
		
		double t3 = pow2( volumeR[ node.getId() ] + volumeR[ node.getParent().getId() ] ) / ( areaR[ node.getId() ] + areaR[ node.getParent().getId() ] );
		
		return ( t3 - t1 - t2 ) + ( scale * contourLength[ node.getId() ] );
		
	}
	
	/* Prepare Energy */		
	
	private void prepareEnergy( NodeLevelSets node ) {
		
		List<NodeLevelSets> children = node.getChildren();
		
		NodeLevelSets nodep = node.getParent();	
		
		functionalVariational[ node.getId() ] = new Attribute( Attribute.FUNCTIONAL_VARIATIONAL , 0 );
		
		if( nodep != null ) {	
			
			functionalVariational[ node.getId() ].value = calculateVariational( node );
			
			if( functionalVariational[ node.getId() ].value > maxEnergy ) {
				
				maxEnergy = functionalVariational[ node.getId() ].value;
				
			}		
			
		}
		
		for( NodeLevelSets child : children ) {
		
			prepareEnergy( child );
		
		}
		
	}
	
	private void calculateEnergy() {
		
		PriorityQueueDial queue = new PriorityQueueDial( numNode, ( int ) maxEnergy, PriorityQueueDial.FIFO, true );
		
		/* Initializing heap */	
		
		for( int i = 1 ; i < numNode ; i++ ) {			
			
			if( functionalVariational[ i ].value > 0 ) {			
			
				queue.add( i, ( int ) functionalVariational[ i ].value );
			
			}
			
		}		
		
		/* Greedy */
		
		while( !queue.isEmpty() ) {
			
			int id = queue.remove();		
			
			NodeCT node = ( NodeCT ) mapNodes[ id ];		
				
			NodeCT parent = node.getParent();
				
			componentTree.mergeFather( node );
				
			/* Update parameters */			
				
			areaR[ parent.getId() ] += areaR[ node.getId() ];
				
			volumeR[ parent.getId() ] += volumeR[ node.getId() ];						
				
			/* Update parent energy (if it is not the root) */
			
			if( parent != rootTree ) {
					
				double beforeEnergy = functionalVariational[ parent.getId() ].value;
				
				double newEnergy = calculateVariational( parent );
				
				if( beforeEnergy > 0 ) {
					
					queue.remove( parent.getId(), ( int ) beforeEnergy );
					
				}
				
				if( newEnergy > 0 ) {
					
					queue.add( parent.getId(), ( int ) newEnergy );
					
				}
				
				functionalVariational[ parent.getId() ].value = newEnergy;
					
			}
				
			/* Update for children of parent */
				
			for( NodeCT child : parent.getChildren() ) {
					
				double beforeEnergy = functionalVariational[ child.getId() ].value;
				
				double newEnergy = calculateVariational( child );
					
				if( beforeEnergy > 0 ) {
									
					queue.remove( child.getId(), ( int ) beforeEnergy );
						
				}
				
				if( newEnergy > 0 ) {	
					
					queue.add( child.getId(), ( int ) newEnergy );
					
				}
				
				functionalVariational[ child.getId() ].value = newEnergy;
					
			}
			
			mapNodes[ id ] = null;				
			
		}	
		
		/* Test removed */
		
		/*for( int j = 0 ; j < numNode ; j++ ){
			
			if( mapNodes[ j ] != null && functionalVariactional[ j ].value > 0 ) {
				
				System.out.println( "wrong!" );
				
			}
			
		}*/
		
		simplifiedImage = componentTree.reconstruction();
		
		ImageBuilder.saveImage( simplifiedImage, new File("/home/gobber/reconstructed.png"));
		
	}
	
	private double pow2( double v ) {
		
		return v*v;
		
	}
	
	public static void main( String args[] ) {
		
		GrayScaleImage image = ImageBuilder.openGrayImage();
		
		ConnectedFilteringByComponentTree connectedFilteringByComponentTree = new ConnectedFilteringByComponentTree( image, AdjacencyRelation.getCircular( 1 ), true );		
		
		connectedFilteringByComponentTree.computerFunctionalVariacionalAttribute( 2000 );
		
		System.err.println( "Finished" );
		
		/*Iterator<NodeCT> hashSet = connectedFilteringByComponentTree.getListNodes().iterator();
		
		while( hashSet.hasNext() ) {
			
			NodeCT node = hashSet.next();
			
			if( node.getAttributeValue( Attribute.AREA ) < 3000 ) {
				
				hashSet.remove();
				
				connectedFilteringByComponentTree.mergeFather( node );
				
			}
			
		}*/
				
		//ImageBuilder.saveImage( connectedFilteringByComponentTree.reconstruction(), new File("/home/gobber/reconstructed.png"));
		
	}
	
	public static void printTree(NodeCT no, PrintStream out, String s){
		out.printf(s + "[%3d; %3d]\n", no.getId(), no.getLevel() );
		if(!no.isLeaf())
			for(NodeCT son: no.getChildren()){
				printTree(son, out, s + "------");
			}
	}

}
