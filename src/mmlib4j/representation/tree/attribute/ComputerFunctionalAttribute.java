package mmlib4j.representation.tree.attribute;

import java.io.File;
import java.io.FileNotFoundException;

import mmlib4j.datastruct.PriorityQueueHeap;
import mmlib4j.datastruct.SimpleArrayList;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.Utils;

public class ComputerFunctionalAttribute {
	
	private Attribute mumfordShaEnergy[];
	private NodeLevelSets [] mapNodes;
	private double areaR[];
	private double volumeR[];	
	private SimpleArrayList<Integer> [] mapChildren;
	private NodeLevelSets [] mapParent;
	private int numNode;
	private NodeLevelSets root;
	private double minEnergy = Double.MAX_VALUE;
	private double maxEnergy = Double.MIN_VALUE;
	
	public ComputerFunctionalAttribute(ComponentTree tree, boolean useHeuristic) {
		
		long ti = System.currentTimeMillis();				
		this.numNode = tree.getNumNode();
		this.root = tree.getRoot();
		
		mapChildren = new SimpleArrayList[ numNode ];
		mapNodes = new NodeLevelSets[ numNode ];						
		mapParent = new NodeLevelSets[ numNode ];
		
		areaR = new double[ numNode ];
		volumeR = new double[ numNode ];
		mumfordShaEnergy = new Attribute[numNode];
		
		for(NodeLevelSets node : tree.getListNodes()) {
			
			NodeLevelSets parent = node.getParent();
			mapNodes[node.getId()] = node;
			areaR[node.getId()] = node.getCompactNodePixels().size();
			volumeR[node.getId()] = areaR[node.getId()] * node.getLevel();
			mumfordShaEnergy[ node.getId() ] = new Attribute(Attribute.FUNCTIONAL_ATTRIBUTE, 0);
			
			if( parent != null ) {
				if( mapChildren[ parent.getId() ] == null ) {
					mapChildren[ parent.getId() ] = new SimpleArrayList<Integer>(parent.getChildren().size()); 
							
				}			
				mapParent[node.getId()] = parent;
				mapChildren[parent.getId()].add(node.getId());
				mumfordShaEnergy[node.getId()].value = calculateFunctional( node );
			}					
			
		}
		
		if(useHeuristic)
			calculateEnergyByHeuristic();
		else
			calculateEnergy();
		
		for(NodeLevelSets node : tree.getListNodes()) {		
			if(mumfordShaEnergy[node.getId()].value > maxEnergy)
				maxEnergy = mumfordShaEnergy[node.getId()].value;		
			if(mumfordShaEnergy[node.getId()].value < minEnergy)
				minEnergy = mumfordShaEnergy[node.getId()].value;		
		}
		
		mapChildren = null;
		mapNodes = null;
		if( Utils.debug ) {
			long tf = System.currentTimeMillis();
			System.out.println( "Tempo de execucao [extraction of attribute - based on mumford-sha-energy]  " + ( ( tf - ti ) / 1000.0 )  + "s" );			
		}
	}
	
	public ComputerFunctionalAttribute(ComponentTree tree) {
		this(tree, true);		
	}
	
	private double pow2( double v ) {
		return v*v;
	}
	
	private double calculateFunctional(NodeLevelSets node) { 
		NodeLevelSets parent = mapParent[node.getId()];
		double p1 = pow2( volumeR[ node.getId() ] ) / areaR[ node.getId() ];
		double p2 = pow2( volumeR[ parent.getId() ] ) / areaR[ parent.getId() ];
		double p3 = pow2( volumeR[ node.getId() ] + volumeR[ parent.getId() ] ) / ( areaR[ node.getId() ] + areaR[ parent.getId() ] );
		return ( p1 + p2 - p3 ) / node.getAttributeValue(Attribute.PERIMETER_EXTERNAL);		
	}
	
	// Maximiza o atributo funcional baseado na heurística da soma do gradiente do contorno
	private void calculateEnergyByHeuristic() {
		
		PriorityQueueHeap<NodeLevelSets> queue = new PriorityQueueHeap<NodeLevelSets>(numNode); 
		for( int i = 1 ; i < numNode ; i++ ) {
			queue.add(mapNodes[i], mapNodes[i].getAttributeValue(Attribute.SUM_GRAD_CONTOUR));		
		}
		
		double mumfordShaEnergyTmp;		
		while(!queue.isEmpty()) {			
			NodeLevelSets node = queue.removeMin();									
			NodeLevelSets parent = mapParent[node.getId()];	
			mumfordShaEnergyTmp = calculateFunctional(node);			
			
			if( mumfordShaEnergyTmp > mumfordShaEnergy[node.getId()].value ) {
				mumfordShaEnergy[node.getId()].value = mumfordShaEnergyTmp;
			}				
			mapChildren[parent.getId()].removeElement(node.getId());
			if( mapChildren[node.getId()] != null ) {
				for( int childId : mapChildren[node.getId()]) {											
					mapParent[childId] = parent;
					mapChildren[parent.getId()].add(childId);
				}
			}
			areaR[parent.getId()] += areaR[node.getId()];
			volumeR[parent.getId()] += volumeR[node.getId()];
		}
		
	}
	
	private void updateEnergy(PriorityQueueHeap<NodeLevelSets> queue, NodeLevelSets node) {
		double beforeEnergy = mumfordShaEnergy[node.getId()].value;
		double newEnergy = calculateFunctional(node);
		if(queue.contains(node)) {
			queue.updatePriorityElement(node, beforeEnergy, newEnergy);
		}		
		mumfordShaEnergy[node.getId()].value = newEnergy;
	}	
	
	// Maximiza o atributo funcional baseado em uma Heap
	private void calculateEnergy() {
		
		PriorityQueueHeap<NodeLevelSets> queue = new PriorityQueueHeap<NodeLevelSets>(numNode);
		
		for(int i = 1 ; i < numNode ; i++) {		
			queue.add(mapNodes[i],  mumfordShaEnergy[i].value);
		}		
		
		/* Greedy */
		while(!queue.isEmpty()) {
			
			NodeLevelSets node = queue.removeMin();		
			NodeLevelSets parent = mapParent[node.getId()];
						
			//merge: node and its parent
			mapChildren[parent.getId()].removeElement(node.getId());
			if(mapChildren[node.getId()] != null)
				for(Integer id: mapChildren[node.getId()]) {
					mapParent[id] = parent;
					mapChildren[parent.getId()].add(id);
				}
			/* Update parameters */
			areaR[parent.getId()] += areaR[node.getId()];
			volumeR[parent.getId()] += volumeR[node.getId()];						
				
			/* Update parent energy (if it is not the root) */
			if(parent != root) {
				updateEnergy(queue, parent);
			}
				
			/* Update for children of parent */
			for(Integer id: mapChildren[ parent.getId() ]) {
				NodeLevelSets child = mapNodes[id];
				updateEnergy(queue, child);
			}								
			
		}	
		
	}
	
	public void addAttributeInNodesCT( SimpleLinkedList<NodeLevelSets> list ) {
		for( NodeLevelSets node : list ) {
			addAttributeInNodes( node );
		}		
	}
	
	public void addAttributeInNodesToS( SimpleLinkedList<NodeLevelSets> hashSet ) {
		for( NodeLevelSets node : hashSet ) {
			addAttributeInNodes(node);
		}
	} 
	
	public void addAttributeInNodes(NodeLevelSets node) {
		mumfordShaEnergy[node.getId()].value = (mumfordShaEnergy[node.getId()].value - minEnergy) / (maxEnergy - minEnergy);
		node.addAttribute(Attribute.FUNCTIONAL_ATTRIBUTE, mumfordShaEnergy[node.getId()]);
	}
	
	public static void main(String args[]) throws FileNotFoundException {
    	    	
    	Utils.debug = true;
    	System.out.println("Component tree");
    	GrayScaleImage input = ImageBuilder.openGrayImage(new File("/Users/gobber/Desktop/mumford-image-test.png"));
		ConnectedFilteringByComponentTree connectedFilteringByComponentTree = new ConnectedFilteringByComponentTree( input, AdjacencyRelation.getCircular( 1 ), false );
		connectedFilteringByComponentTree.computerAttributeBasedPerimeterExternal();
		new ComputerFunctionalAttribute(connectedFilteringByComponentTree, false).addAttributeInNodesCT(connectedFilteringByComponentTree.getListNodes());
		GrayScaleImage imout = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_32BITS, 
																input.getWidth(), 
																input.getHeight());
		
		SimpleLinkedList<NodeLevelSets> nodes = new SimpleLinkedList<NodeLevelSets>();
		for(NodeLevelSets leaf : connectedFilteringByComponentTree.getLeaves()) {			
			if(leaf.getLevel() == 100) {
				double bestv = Double.MIN_VALUE;
				NodeLevelSets bestNode = leaf;
				for(NodeLevelSets n : leaf.getPathToRoot())
					if(bestv < n.getAttributeValue(Attribute.FUNCTIONAL_ATTRIBUTE)) {
						bestv = n.getAttributeValue(Attribute.FUNCTIONAL_ATTRIBUTE);
						bestNode = n;
					}
				nodes.add(bestNode);				
			}			
		}		
		
		for(NodeLevelSets n : nodes) {
			for(int p : n.getPixelsOfCC())
				imout.setPixel(p, n.getLevel());
		}
		ImageBuilder.saveImage(imout, new File("/Users/gobber/Desktop/out.png"));
    }

}
