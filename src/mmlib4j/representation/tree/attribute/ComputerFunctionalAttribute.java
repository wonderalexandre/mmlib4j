package mmlib4j.representation.tree.attribute;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import mmlib4j.datastruct.PriorityQueueHeap;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.InfoMergedTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.InfoMergedTree.NodeMergedTree;
import mmlib4j.representation.tree.InfoMergedTreeLevelOrder;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.Utils;

public class ComputerFunctionalAttribute {
	
	private Attribute mumfordShaEnergy[];
	private double areaR[];
	private double volumeR[];	
	private int numNode;
	private InfoMergedTree mTree;
	
	public ComputerFunctionalAttribute(ComponentTree tree, boolean useHeuristic, GrayScaleImage img) {
		
		long ti = System.currentTimeMillis();				
		this.numNode = tree.getNumNode();
		
		areaR = new double[ numNode ];
		volumeR = new double[ numNode ];
		mumfordShaEnergy = new Attribute[numNode];
		mTree = new InfoMergedTreeLevelOrder(tree.getRoot(), tree.getNumNode(), img);
		
		for(NodeLevelSets node : tree.getListNodes()) {			
			NodeLevelSets parent = node.getParent();
			areaR[node.getId()] = node.getCompactNodePixels().size();
			volumeR[node.getId()] = areaR[node.getId()] * node.getLevel();
			mumfordShaEnergy[node.getId()] = new Attribute(Attribute.FUNCTIONAL_ATTRIBUTE, Double.MAX_VALUE);		
			if( parent != null ) {				
				mTree.addNodeNotMerge(node);			
				mumfordShaEnergy[node.getId()].value = calculateFunctional(mTree.getMap()[node.getId()]);
			}
		}
		
		PriorityQueueHeap<NodeMergedTree> queue = new PriorityQueueHeap<>(numNode);
		if(useHeuristic) { 
			for(int i = 1 ; i < numNode ; i++)
				queue.add(mTree.getMap()[i], mTree.getAttribute(mTree.getMap()[i], Attribute.SUM_GRAD_CONTOUR));		
			calculateEnergyByHeuristic(queue);
		} else {
			for(int i = 1 ; i < numNode ; i++) 		
				queue.add(mTree.getMap()[i], mumfordShaEnergy[i].value);			
			calculateEnergy(queue);
		}
		
		if( Utils.debug ) {
			long tf = System.currentTimeMillis();
			System.out.println( "Tempo de execucao [extraction of attribute - based on mumford-sha-energy]  " + ( ( tf - ti ) / 1000.0 )  + "s" );			
		}
	}
	
	public ComputerFunctionalAttribute(ComponentTree tree, GrayScaleImage img) {
		this(tree, true, img);		
	}
	
	public ComputerFunctionalAttribute(InfoMergedTree mTree, int numNode, boolean useHeuristic, boolean[] mapCorrection) {
		
		long ti = System.currentTimeMillis();				
		this.numNode = numNode;
		this.mTree = mTree;
		
		areaR = new double[ numNode ];
		volumeR = new double[ numNode ];
		mumfordShaEnergy = new Attribute[numNode];		
		
		for(NodeMergedTree node : mTree) {
			areaR[node.getId()] = node.getCompactNodePixels().size();
			volumeR[node.getId()] = areaR[node.getId()] * node.getLevel();			
			mumfordShaEnergy[node.getId()] = new Attribute(Attribute.FUNCTIONAL_ATTRIBUTE, Double.MAX_VALUE);
			if(node != mTree.getRoot()) {									
				mumfordShaEnergy[node.getId()].value = calculateFunctional( mTree.getMap()[node.getId()] );
			}
		}							
		
		PriorityQueueHeap<NodeMergedTree> queue = new PriorityQueueHeap<>(numNode);
		if(useHeuristic) {			
			for(NodeMergedTree node : mTree.getRoot().getChildren())
				if(mapCorrection[node.getId()])
					for(NodeMergedTree desc : node.getNodesDescendants())
						queue.add(desc, mTree.getAttribute(desc, Attribute.SUM_GRAD_CONTOUR));			
			calculateEnergyByHeuristic(queue);
		} else {
			for(NodeMergedTree node : mTree.getRoot().getChildren())
				if(mapCorrection[node.getId()])
					for(NodeMergedTree desc : node.getNodesDescendants())
						queue.add(desc, mumfordShaEnergy[desc.getId()].value);
			calculateEnergy(queue);
		}
		
		if( Utils.debug ) {
			long tf = System.currentTimeMillis();
			System.out.println( "Tempo de execucao [extraction of attribute - based on mumford-sha-energy]  " + ( ( tf - ti ) / 1000.0 )  + "s" );			
		}
	}
	
	private double pow2( double v ) {
		return v*v;
	}
	
	private double calculateFunctional(NodeMergedTree node) { 
		NodeMergedTree parent = mTree.getMap()[node.getId()].getParent();
		double p1 = pow2( volumeR[ node.getId() ] ) / areaR[ node.getId() ];
		double p2 = pow2( volumeR[ parent.getId() ] ) / areaR[ parent.getId() ];
		double p3 = pow2( volumeR[ node.getId() ] + volumeR[ parent.getId() ] ) / ( areaR[ node.getId() ] + areaR[ parent.getId() ] );
		return ( p1 + p2 - p3 ) / mTree.getAttribute(node, Attribute.PERIMETER_EXTERNAL);		
	}
	
	// Maximiza o atributo funcional baseado na heurística da soma do gradiente do contorno
	private void calculateEnergyByHeuristic(PriorityQueueHeap<NodeMergedTree> queue) {			
		double mumfordShaEnergyTmp;		
		while(!queue.isEmpty()) {			
			NodeMergedTree node = queue.removeMin();									
			NodeMergedTree parent = node.getParent();	
			mumfordShaEnergyTmp = calculateFunctional(node);			
			
			if( mumfordShaEnergyTmp > mumfordShaEnergy[node.getId()].value ) {
				mumfordShaEnergy[node.getId()].value = mumfordShaEnergyTmp;
			}
			
			mTree.updateNodeToMerge(node);			
			areaR[parent.getId()] += areaR[node.getId()];
			volumeR[parent.getId()] += volumeR[node.getId()];
		}
		
	}
	
	private void updateEnergy(PriorityQueueHeap<NodeMergedTree> queue, NodeMergedTree node) {
		double beforeEnergy = mumfordShaEnergy[node.getId()].value;
		double newEnergy = calculateFunctional(node);
		if(queue.contains(node)) {
			queue.updatePriorityElement(node, beforeEnergy, newEnergy);
		}		
		mumfordShaEnergy[node.getId()].value = newEnergy;
	}	
	
	// Maximiza o atributo funcional baseado em uma Heap
	private void calculateEnergy(PriorityQueueHeap<NodeMergedTree> queue) {				
		/* Greedy */
		while(!queue.isEmpty()) {			
			NodeMergedTree node = queue.removeMin();		
			NodeMergedTree parent = node.getParent();
						
			//merge: node and its parent
			mTree.updateNodeToMerge(node);
			
			/* Update parameters */
			areaR[parent.getId()] += areaR[node.getId()];
			volumeR[parent.getId()] += volumeR[node.getId()];						
				
			/* Update parent energy (if it is not the root) */
			if(parent != mTree.getRoot()) {
				updateEnergy(queue, parent);
			}
				
			/* Update for children of parent */			
			for(NodeMergedTree child : parent.getChildren()) {
				updateEnergy(queue, child);
			}											
		}	
		
	}
	
	public void addAttributeInNodesMtree(boolean[] mapCorrection){
		for(NodeMergedTree node_ : mTree.getRoot().getChildren()) {
			if(mapCorrection[node_.getId()]) {
				for(NodeMergedTree desc : node_.getNodesDescendants()) {
					if(!desc.isAttrModified()) {
						desc.setAttributes(new HashMap<Integer, Attribute>());
						desc.setIsAttrModified(true);
					}
					desc.addAttribute(Attribute.FUNCTIONAL_ATTRIBUTE, mumfordShaEnergy[desc.getId()]);
				}
			}
		}
	}
	
	public void addAttributeInNodesCT(SimpleLinkedList<NodeLevelSets> list) {
		for(NodeLevelSets node : list) {
			addAttributeInNodes(node);
		}		
	}
	
	public void addAttributeInNodesToS(SimpleLinkedList<NodeLevelSets> hashSet) {
		for(NodeLevelSets node : hashSet) {
			addAttributeInNodes(node);
		}
	} 
	
	public void addAttributeInNodes(NodeLevelSets node) {
		node.addAttribute(Attribute.FUNCTIONAL_ATTRIBUTE, mumfordShaEnergy[node.getId()]);
	}
	
	public static void main(String args[]) throws FileNotFoundException {
    	    	
    	Utils.debug = true;
    	System.out.println("Component tree");
    	GrayScaleImage input = ImageBuilder.openGrayImage(new File("/Users/gobber/Desktop/lena.jpg"));
    	int type = Attribute.FUNCTIONAL_ATTRIBUTE;
		
    	ConnectedFilteringByComponentTree tree1 = new ConnectedFilteringByComponentTree( input, AdjacencyRelation.getCircular( 1 ), true );
		tree1.computerAttributeBasedPerimeterExternal();
		new ComputerFunctionalAttribute(tree1, true, input).addAttributeInNodesCT(tree1.getListNodes());
		tree1.filteringByDirectRule(1000, type);
		
		
		ConnectedFilteringByComponentTree tree2 = new ConnectedFilteringByComponentTree(tree1.getMtree().reconstruction(), AdjacencyRelation.getCircular( 1 ), true);
		tree2.computerAttributeBasedPerimeterExternal();						
		new ComputerFunctionalAttribute(tree2, true, input).addAttributeInNodesCT(tree2.getListNodes());			
		
		/*NodeLevelSets[] nodeTree1 = new NodeLevelSets[tree1.getNumNode()];
		NodeLevelSets[] nodeTree2 = new NodeLevelSets[tree1.getNumNode()];
		
		for(NodeLevelSets n1 : tree1.getListNodes()) {
			nodeTree1[n1.getId()] = n1; 
		}
		
		for(NodeLevelSets n2 : tree2.getListNodes()) {
			nodeTree2[n2.getId()] = n2; 
		}
		
		for(int i = 0 ; i < tree1.getNumNode() ; i++) {
			if(nodeTree1[i].getAttributeValue(Attribute.FUNCTIONAL_ATTRIBUTE) != nodeTree2[i].getAttributeValue(Attribute.FUNCTIONAL_ATTRIBUTE))
				System.out.println("wrong");
		}*/
		
		for(NodeLevelSets node : tree2.getRoot().getChildren()) {
			System.out.println("id: " + node.getId() + " energy: " + node.getAttributeValue(Attribute.SUM_GRAD_CONTOUR));
		}
		
		for(NodeMergedTree node : tree1.getMtree().getRoot().getChildren()) {
			//System.out.println("id: " + node.getId() + " energy: " + tree1.getMtree().getAttribute(node,type));
			System.out.println("id: " + node.getId() + " energy: " + tree1.getMtree().getAttribute(node, Attribute.SUM_GRAD_CONTOUR));
		}
		
    }

}
