package mmlib4j.representation.tree.attribute;

import java.io.File;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;

import mmlib4j.datastruct.PriorityQueueDial;
import mmlib4j.datastruct.Queue;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.BuilderComponentTree;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.BuilderTreeOfShape;
import mmlib4j.representation.tree.tos.BuilderTreeOfShapeByUnionFind;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.Utils;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Charles Gobber
 *
 * Implementacao dos algoritmos para computação do atributo variacional de dois autores: 
 * 
 * Coloma Ballester, Vicent Caselles, Laura Igual and Luis Garrido
 * Level Lines Selection with Variational Models for Segmentation and Encoding
 * 
 * Yongchao Xu, Thierry Géraud, Laurent Najman
 * Salient Level Lines Selection Using the Mumford-Shah Functional
 */

public class ComputerFunctionalVariational {
	
	private int numNode;
	
	private NodeLevelSets rootTree;
	
	private double scale = 1;
	
	/* Heurística do Xu */
	
	private boolean useHeuristic = false;
	
	/* Auxiliary structures */
	
	private NodeLevelSets mapNodes[];
	
	private double areaR[];
	
	private double volumeR[];
	
	private double maxEnergy = Double.MIN_VALUE;
	
	/* Image */
	
	private GrayScaleImage simplifiedImage;
	
	private MorphologicalTree simplifiedTree;
	
	/* Attribute */
	
	private Attribute functionalVariational [];
	
	// pass a clone
	
	public ComputerFunctionalVariational( ComponentTree componentTree, double scale, boolean useHeuristic ) {
		
		long ti = System.currentTimeMillis();
		
		//ComponentTree componentTree = new ComponentTree( builder );//.getClone();
		
		this.rootTree = componentTree.getRoot();
		
		this.numNode = componentTree.getNumNode();
		
		this.scale = scale;	
		
		this.useHeuristic = useHeuristic;
		
		/* Init the structures */
		
		init( rootTree );		
		
		/* Pre-processing */
		
		removeChildrenAttribute( rootTree );
		
		prepareEnergy( rootTree );		
		
		if( useHeuristic ) {
			
			calculateEnergyByHeuristic( componentTree );
			
		} else {
		
			/* Calculate energy */
		
			calculateEnergy( componentTree );
		
		}
		
		/* Reconstruct image */
		
		simplifiedImage = componentTree.reconstruction();
		
		simplifiedTree = componentTree;
		
		//ImageBuilder.saveImage( simplifiedImage, new File("/home/gobber/reconstructed.png"));
		
		if( Utils.debug ) {
			
			long tf = System.currentTimeMillis();
			
			System.out.println( "Tempo de execucao [extraction of attribute - based on functional variactional]  " + ( ( tf - ti ) / 1000.0 )  + "s" );
			
		}	
		
	}
	
	public ComputerFunctionalVariational( TreeOfShape treeOfShape, double scale, boolean useHeuristic ) {
		
		long ti = System.currentTimeMillis();
		
		//TreeOfShape treeOfShape = new TreeOfShape( builder ).getClone();
		
		this.rootTree = treeOfShape.getRoot();
		
		this.numNode = treeOfShape.getNumNode();
		
		this.scale = scale;
		
		this.useHeuristic = useHeuristic;
		
		/* Init some structures */
		
		init( rootTree );
		
		/* Pre-processing */
		
		removeChildrenAttribute( rootTree );
		
		prepareEnergy( rootTree );
		
		if( useHeuristic ) {
			
			calculateEnergyByHeuristic( treeOfShape );
			
		} else {
			
			/* Calculate energy */
		
			calculateEnergy( treeOfShape );
		
		}
		
		/* Reconstruct image */
		
		simplifiedImage = treeOfShape.reconstruction();
		
		simplifiedTree = treeOfShape;
		
		ImageBuilder.saveImage( simplifiedImage, new File("/home/gobber/reconstructed.png"));
		
		if( Utils.debug ) {
			
			long tf = System.currentTimeMillis();
			
			System.out.println( "Tempo de execucao [extraction of attribute - based on functional variactional]  " + ( ( tf - ti ) / 1000.0 )  + "s" );
			
		}	
		
	}	
	
	public GrayScaleImage getSimplifiedImage() {
		
		return simplifiedImage;
		
	}

	public void setSimplifiedImage( GrayScaleImage simplifiedImage ) {
		
		this.simplifiedImage = simplifiedImage;
		
	}

	private void init( NodeLevelSets root ){
		
		/* Map nodes ( para acessar os nós em O(1) ) */
		
		mapNodes = new NodeLevelSets[ numNode ];
		
		/* Init some structures */
		
		areaR = new double[ numNode ];
		
		volumeR = new double[ numNode ];
		
		functionalVariational = new Attribute[ numNode ];
		
		getChildrenAttribute( root );
		
	}
	
	private void getChildrenAttribute( NodeLevelSets node ) {
		
		List<NodeLevelSets> children = node.getChildren();		
			
		areaR[ node.getId() ] = node.getAttributeValue( Attribute.AREA );
		
		volumeR[ node.getId() ] = node.getAttributeValue( Attribute.VOLUME );		
		
		for( NodeLevelSets son : children ) {
			
			getChildrenAttribute( son );
			
		}
		
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
		
		double t3 = pow2( volumeR[ node.getId() ] + volumeR[ node.getParent().getId() ] ) / ( areaR[ node.getId() ] + areaR[ node.getParent().getId() ] );
		
		return ( t3 - t1 - t2 ) + ( scale * node.getAttributeValue( Attribute.PERIMETER_EXTERNAL ) );
		
		//return ( t3 - t1 - t2 ) + ( scale * contourLength[ node.getId() ] );
		
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
	
	private void updateEnergy( PriorityQueueDial queue, NodeLevelSets node ) {
		
		double beforeEnergy = functionalVariational[ node.getId() ].value;
		
		double newEnergy = calculateVariational( node );
		
		if( beforeEnergy > 0 ) {
			
			queue.remove( node.getId(), ( int ) beforeEnergy );
			
		}
		
		if( newEnergy > 0 ) {
			
			queue.add( node.getId(), ( int ) newEnergy );
			
		}
		
		functionalVariational[ node.getId() ].value = newEnergy;
		
	}
	
	/**
	 *   
	 *  Algoritmo para computar o atributo funcional utilizando fila de prioridade
	 *  em O(N²) onde N é o número de nós 
	 *   
	 * 	Coloma Ballester, Vicent Caselles, Laura Igual and Luis Garrido
	 * 	Level Lines Selection with Variational Models for Segmentation and Encoding
	 * 
	 * */
	
	private void calculateEnergy( MorphologicalTree tree ) {
		
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
			
			NodeLevelSets node = mapNodes[ id ];		
				
			NodeLevelSets parent = node.getParent();
			
			tree.mergeFather( node );
				
			/* Update parameters */		
			
			updateAttributes( node );
				
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
	 *  Algoritmo para computar o atributo funcional utilizando o atributo
	 *  heuristico de significância em O(N) onde N é o número de nós da árvore.
	 *   
	 * 	Yongchao Xu, Thierry Géraud, Laurent Najman
	 * 	Salient Level Lines Selection Using the Mumford-Shah Functional
	 * 
	 * */
	
	public int [] sortNodes() {
		
		int Rt [] = new int[ numNode-1 ];	
		double maxSumgrad = Double.MIN_VALUE;
		
		/* Get max value */
		
		for( int i = 1 ; i < numNode ; i++ ) {
			
			double sumGrad = mapNodes[ i ].getAttributeValue( Attribute.SUM_GRAD_CONTOUR );
			
			if( maxSumgrad <  sumGrad ) {
			
				maxSumgrad = sumGrad;
			
			}
			
		}
		
		PriorityQueueDial queue = new PriorityQueueDial( numNode, 
														( int ) maxSumgrad, 
														PriorityQueueDial.FIFO, false ); 
				
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
	
	private void calculateEnergyByHeuristic( MorphologicalTree tree ) {		
		
		int Rt [] = sortNodes();
		
		boolean explored[] = new boolean[ numNode ];
		
		explored[ rootTree.getId() ] = true;
		
		Queue<NodeLevelSets> nodesToMerge = new Queue<NodeLevelSets>();	
		
		Queue<NodeLevelSets> nodesToExplore = new Queue<NodeLevelSets>();
		
		for( int i = 0 ; i < Rt.length ; i++ ) {		
			
			if( mapNodes[ Rt[ i ] ] == null ) continue;
			
			nodesToExplore.enqueue( mapNodes[ Rt[ i ] ] );
			
			while( !nodesToExplore.isEmpty() ) {			
																			
				NodeLevelSets node = nodesToExplore.dequeue();
				
				while( node != null && explored[ node.getId() ] ) {				
					
					node = node.getParent();
					
				}
				
				if( node == null ) continue;
				
				functionalVariational[ node.getId() ].value = calculateVariational( node );
				
				if( functionalVariational[ node.getId() ].value > 0 ) {					
										
					NodeLevelSets parent = node.getParent();		
				
					nodesToMerge.enqueue( node );
					
					// atualiza atributos locais e da árvore
					
					updateAttributes( node );
									
					areaR[ parent.getId() ] += areaR[ node.getId() ];
					
					volumeR[ parent.getId() ] += volumeR[ node.getId() ];					
					
					mapNodes[ node.getId() ] = null;
					
					
					if( !explored[ parent.getId() ] ) {
						
						nodesToExplore.enqueue( parent );
						
					}
					
					List<NodeLevelSets> list =  parent.getChildren();
					
					for( NodeLevelSets child : list ) {
						
						if( !explored[ child.getId() ] ) {
						
							nodesToExplore.enqueue( child );
						
						}
						
					}
					
					explored[ node.getId() ] = true;
					
				} else {
					
					explored[ node.getId() ] = false;
					
				}
				
			}
			
		}
		
		while( !nodesToMerge.isEmpty() ) {
			
			NodeLevelSets node = nodesToMerge.dequeue();
			
			tree.mergeFather( node );
			
		}
		
		// Test removed 
		
		/*for( int j = 0 ; j < numNode ; j++ ){
			
			if( mapNodes[ j ] != null && functionalVariational[ j ].value > 0 ) {
				
				System.out.println( "wrong!" );
				
			}
			
		}*/		
		
	}
	
	// atualiza os atributos do pai do nó removido ( área e volume ) 	
	
	public void updateAttributes( NodeLevelSets node ) {
		
		NodeLevelSets parent = node.getParent();
		
		Attribute area = parent.getAttribute( Attribute.AREA );
		Attribute volume = parent.getAttribute( Attribute.VOLUME );		
		
		area.setValue( area.getValue() - areaR[ node.getId() ] );
		
		volume.setValue( volume.getValue() + ( parent.getLevel() * areaR[ node.getId() ] ) );
		
	}
	
	public void addAttributeInNodesCT(HashSet<NodeCT> list) {
		
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
		
		node.addAttribute( Attribute.FUNCTIONAL_VARIATIONAL, functionalVariational[ node.getId() ] );
		
	}	
	
	private double pow2( double v ) {
		
		return v*v;
		
	}
	
	public static void main( String args[] ) {
		
		GrayScaleImage image = ImageBuilder.openGrayImage();
		
		
		
		/*BuilderTreeOfShapeByUnionFind builderTreeOfShape = new BuilderTreeOfShapeByUnionFind( image, -1, -1, true );
		
		TreeOfShape treeOfShape = new TreeOfShape( builderTreeOfShape );
		
		new ComputerAttributeBasedPerimeterExternal(treeOfShape.getNumNode(), 
													treeOfShape.getRoot(), 
													treeOfShape.getInputImage())
													.addAttributeInNodesToS(treeOfShape.getListNodes());
		
		ComputerFunctionalVariational fattr = new ComputerFunctionalVariational( builderTreeOfShape, 1000, false );
		
		ImageBuilder.saveImage( fattr.getSimplifiedImage(), new File("/home/gobber/reconstructed.png"));*/
		
		/*ConnectedFilteringByTreeOfShape connectedFilteringByTreeOfShape = new ConnectedFilteringByTreeOfShape( image );		
		
		connectedFilteringByTreeOfShape.computerFunctionalVariacionalAttribute( 20, true );*/
				
		ConnectedFilteringByComponentTree connectedFilteringByComponentTree = new ConnectedFilteringByComponentTree( image, AdjacencyRelation.getCircular( 1 ), true );		
		
		// 50
		
		connectedFilteringByComponentTree.computerFunctionalVariacionalAttribute( 20, true );
	
		System.err.println( "Finished" );				
		
	}
	
	public static void printTree(NodeCT no, PrintStream out, String s){
		out.printf(s + "[%3d; %3d]\n", no.getId(), no.getLevel() );
		if(!no.isLeaf())
			for(NodeCT son: no.getChildren()){
				printTree(son, out, s + "------");
			}
	}

	public MorphologicalTree getSimplifiedTree() {
		
		return simplifiedTree;
		
	}

	public void setSimplifiedTree(MorphologicalTree tree) {
		
		this.simplifiedTree = tree;
		
	}

}
