package mmlib4j.representation.tree.attribute;

import java.io.File;

import mmlib4j.datastruct.PriorityQueueHeap;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.Utils;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Charles Gobber
 *
 * Implementations of algorithms to compute Functional Variational from: 
 * 
 * Coloma Ballester, Vicent Caselles, Laura Igual and Luis Garrido
 * Level Lines Selection with Variational Models for Segmentation and Encoding
 * 
 *    and
 * 
 * Yongchao Xu, Thierry Géraud, Laurent Najman
 * Salient Level Lines Selection Using the Mumford-Shah Functional
 */

public class ComputerFunctionalVariational {
	
	private int numNode;
	private NodeLevelSets rootTree;	
	private double scale = 1;		
	private NodeLevelSets mapNodes[]; 
	private Attribute functionalVariational [];	
	private double areaR[];	
	private double volumeR[];	
	private GrayScaleImage simplifiedImage;	
	private ComponentTree tree;
	
	public ComputerFunctionalVariational(ComponentTree tree, double scale, boolean useHeuristic) {
	
		long ti = System.currentTimeMillis();	
		this.tree = tree;
		this.rootTree = tree.getRoot();		
		this.numNode = tree.getNumNode();		
		this.scale = scale;	
				
		mapNodes = new NodeLevelSets[ numNode ];						
		areaR = new double[ numNode ];
		volumeR = new double[ numNode ];
		functionalVariational = new Attribute[numNode];
		
		for(NodeLevelSets node : tree.getListNodes()) {			
			mapNodes[node.getId()] = node;
			areaR[node.getId()] = node.getCompactNodePixels().size();
			volumeR[node.getId()] = areaR[node.getId()] * node.getLevel();
			functionalVariational[node.getId()] = new Attribute(Attribute.FUNCTIONAL_VARIATIONAL, 0);			
			mapNodes[node.getId()] = node;			
		}
		
		if(useHeuristic)
			calculateEnergyByHeuristic();
		else
			calculateEnergy();
		
		/* Reconstruct simplified image */		
		simplifiedImage = tree.reconstruction();			
		
		if( Utils.debug ) {			
			long tf = System.currentTimeMillis();			
			System.out.println( "Tempo de execucao [extraction of attribute - based on functional variactional]  " + ( ( tf - ti ) / 1000.0 )  + "s" );			
		}	

	}
	
	public ComputerFunctionalVariational(ComponentTree tree, double scale) {		
		this(tree, scale, false);		
	}
		
	public GrayScaleImage getSimplifiedImage() {		
		return simplifiedImage;		
	}

	public void setSimplifiedImage(GrayScaleImage simplifiedImage) {		
		this.simplifiedImage = simplifiedImage;		
	}
	
	/* Calculate inverse energy */	
	private double calculateVariational( NodeLevelSets node ) {
		double t1 = pow2( volumeR[ node.getId() ] ) / areaR[ node.getId() ];		
		double t2 = pow2( volumeR[ node.getParent().getId() ] ) / areaR[ node.getParent().getId() ];		
		double t3 = pow2( volumeR[ node.getId() ] + volumeR[ node.getParent().getId() ] ) / ( areaR[ node.getId() ] + areaR[ node.getParent().getId() ] );		
		return -(( t3 - t1 - t2 ) + (scale * node.getAttributeValue(Attribute.PERIMETER_EXTERNAL)));		
	}
		
	private void updateEnergy(PriorityQueueHeap<NodeLevelSets> queue, NodeLevelSets node) {
				
		double newEnergy = calculateVariational(node);
		
		if(queue.contains(node)) {		
			queue.remove(node);		
		}
		
		if(newEnergy <= 0) {			
			queue.add(node, newEnergy);			
		}
		
		functionalVariational[node.getId()].value = newEnergy;
		
	}
	
	/**
	 *   
	 * 	Coloma Ballester, Vicent Caselles, Laura Igual and Luis Garrido
	 * 	Level Lines Selection with Variational Models for Segmentation and Encoding
	 * 
	 * */
	
	private void calculateEnergy() {
		
		/* Initializing heap */			
		PriorityQueueHeap<NodeLevelSets> queue = new PriorityQueueHeap<NodeLevelSets>(numNode);		
		for( int i = 1 ; i < numNode ; i++ ) {
			functionalVariational[i].value = calculateVariational(mapNodes[i]);
			if(functionalVariational[i].value <= 0) {
				queue.add(mapNodes[i], functionalVariational[i].value);
			}
		}
		
		/* Greedy */
		while(!queue.isEmpty()) {
						
			NodeLevelSets node = queue.removeMin();							
			NodeLevelSets parent = node.getParent();						
			tree.mergeParent(node);
				
			/* Update parameters */									
			areaR[parent.getId()] += areaR[node.getId()];				
			volumeR[parent.getId()] += volumeR[node.getId()];						
				
			/* Update parent energy (if it is not the root) */			
			if(parent != rootTree) {	
				updateEnergy(queue, parent);
			}
				
			/* Update for children of parent */			
			SimpleLinkedList<NodeLevelSets> list = parent.getChildren();				
			for(NodeLevelSets child : list) {			
				updateEnergy(queue, child);					
			}			
			
		}	
		
	}
	
	private void calculateEnergyByHeuristic() {
		
		PriorityQueueHeap<NodeLevelSets> queue = new PriorityQueueHeap<NodeLevelSets>(numNode); 
		for( int i = 1 ; i < numNode ; i++ ) {
			functionalVariational[i].value = calculateVariational(mapNodes[i]);
			queue.add(mapNodes[i], mapNodes[i].getAttributeValue(Attribute.SUM_GRAD_CONTOUR));		
		}
		
		NodeLevelSets[] rt = new NodeLevelSets[numNode];
		boolean explored[] = new boolean[numNode];
		boolean repeat = true;
		for(int i = 1 ; i < numNode ;i++) {
			rt[i] = queue.removeMin();
		}			
		
		while(repeat) {			
			repeat = false;
			for(NodeLevelSets node: rt) {	

				if(node == null || explored[node.getId()])
					continue;
				
				NodeLevelSets parent = node.getParent();	
				functionalVariational[node.getId()].value = calculateVariational(node);
				if(functionalVariational[node.getId()].value <= 0) { 									
					repeat = true;
					explored[node.getId()] = true;
					tree.mergeParent(node);				
					areaR[parent.getId()] += areaR[node.getId()];					
					volumeR[parent.getId()] += volumeR[node.getId()];
				}
				
			}
			
		}

		
	}
		
	public void addAttributeInNodesCT(SimpleLinkedList<NodeLevelSets> list) {		
		for(NodeLevelSets node: list ) {			
			addAttributeInNodes( node );			
		}		
	}
	
	public void addAttributeInNodesToS(SimpleLinkedList<NodeLevelSets> hashSet ) {		
		for(NodeLevelSets node: hashSet) {			
			addAttributeInNodes( node );			
		}		
	} 
	
	public ComponentTree getSimplifiedTree() {		
		return tree;	
	}

	public void setSimplifiedTree(ComponentTree tree) {		
		this.tree = tree;		
	}

	
	public void addAttributeInNodes(NodeLevelSets node) {
		functionalVariational[node.getId()].value *= -1;
		node.addAttribute(Attribute.FUNCTIONAL_VARIATIONAL, functionalVariational[node.getId()]);		
	}	
	
	private double pow2(double v) {		
		return v*v;		
	}
	
	public static void main( String args[] ) {
		
		GrayScaleImage image = ImageBuilder.openGrayImage(new File("/Users/gobber/Desktop/lena.jpg"));
								
		// 50
		Utils.debug = true;
		ComponentTree tree = new ComponentTree(image, AdjacencyRelation.getAdjacency8(), false);	
		new ComputerBasicAttribute(tree.getNumNode(), 
													tree.getRoot(), 
													image).addAttributeInNodesCT(tree.getListNodes());
		new ComputerAttributeBasedPerimeterExternal(tree.getNumNode(), 
													tree.getRoot(), 
													tree.getInputImage()).addAttributeInNodesCT(tree.getListNodes());		
		
		ComputerFunctionalVariational fattr = new ComputerFunctionalVariational(tree, 1000, true);
		
		ImageBuilder.saveImage(fattr.getSimplifiedImage(), new File("/Users/gobber/Desktop/reconstructed.png"));	
		System.err.println("Finished");	
		
	}
}
