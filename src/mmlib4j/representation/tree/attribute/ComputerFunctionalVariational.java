package mmlib4j.representation.tree.attribute;

import java.io.File;

import mmlib4j.datastruct.PriorityQueueHeap;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.InfoMergedTree;
import mmlib4j.representation.tree.InfoMergedTreeLevelOrder;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.InfoMergedTree.NodeMergedTree;
import mmlib4j.representation.tree.componentTree.ComponentTree;
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
	private double scale = 1;		 
	private Attribute functionalVariational [];	
	private double areaR[];	
	private double volumeR[];	
	private GrayScaleImage simplifiedImage;	
	private ComponentTree tree;
	private InfoMergedTree mTree;
	
	public ComputerFunctionalVariational(ComponentTree tree, double scale, boolean useHeuristic) {
	
		long ti = System.currentTimeMillis();	
		this.tree = tree;		
		this.numNode = tree.getNumNode();		
		this.scale = scale;										
		this.areaR = new double[ numNode ];
		this.volumeR = new double[ numNode ];
		this.functionalVariational = new Attribute[numNode];
		this.mTree = new InfoMergedTreeLevelOrder(tree.getRoot(), tree.getNumNode(), tree.getInputImage());
		
		for(NodeLevelSets node : tree.getListNodes()) {		
			mTree.addNodeNotMerge(node);
			areaR[node.getId()] = node.getCompactNodePixels().size();
			volumeR[node.getId()] = areaR[node.getId()] * node.getLevel();
			functionalVariational[node.getId()] = new Attribute(Attribute.FUNCTIONAL_VARIATIONAL, 0);						
		}
		
		if(useHeuristic)
			calculateEnergyByHeuristic();
		else
			calculateEnergy();
		
		/* Reconstruct simplified image */		
		simplifiedImage = mTree.reconstruction();			
		
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
	private double calculateVariational(NodeMergedTree node) {
		double t1 = pow2( volumeR[ node.getId() ] ) / areaR[ node.getId() ];		
		double t2 = pow2( volumeR[ node.getParent().getId() ] ) / areaR[ node.getParent().getId() ];		
		double t3 = pow2( volumeR[ node.getId() ] + volumeR[ node.getParent().getId() ] ) / ( areaR[ node.getId() ] + areaR[ node.getParent().getId() ] );		
		return -(( t3 - t1 - t2 ) + (scale * node.getAttributeValue(Attribute.PERIMETER_EXTERNAL)));		
	}
		
	private void updateEnergy(PriorityQueueHeap<NodeMergedTree> queue, NodeMergedTree node) {				
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
		PriorityQueueHeap<NodeMergedTree> queue = new PriorityQueueHeap<NodeMergedTree>(numNode);		
		for( int i = 1 ; i < numNode ; i++ ) {
			functionalVariational[i].value = calculateVariational(mTree.getMap()[i]);
			if(functionalVariational[i].value <= 0) {
				queue.add(mTree.getMap()[i], functionalVariational[i].value);
			}
		}
		
		/* Greedy */
		while(!queue.isEmpty()) {
						
			NodeMergedTree node_ = queue.removeMin();							
			NodeMergedTree parent_ = node_.getParent();						
			mTree.updateNodeToMerge(node_.getInfo());
				
			/* Update parameters */									
			areaR[parent_.getId()] += areaR[node_.getId()];				
			volumeR[parent_.getId()] += volumeR[node_.getId()];						
				
			/* Update parent energy (if it is not the root) */			
			if(parent_ != mTree.getRoot()) {	
				//System.out.println(parent.getParent().getId());
				updateEnergy(queue, parent_);
			}
				
			/* Update for children of parent */			
			SimpleLinkedList<NodeMergedTree> list = parent_.getChildren();				
			for(NodeMergedTree child : list) {			
				updateEnergy(queue, child);					
			}			
			
		}	
		
	}
	
	private void calculateEnergyByHeuristic() {
		
		PriorityQueueHeap<NodeMergedTree> queue = new PriorityQueueHeap<NodeMergedTree>(numNode); 
		for( int i = 1 ; i < numNode ; i++ ) {
			functionalVariational[i].value = calculateVariational(mTree.getMap()[i]);
			queue.add(mTree.getMap()[i], mTree.getMap()[i].getAttributeValue(Attribute.SUM_GRAD_CONTOUR));		
		}
		
		NodeMergedTree[] rt = new NodeMergedTree[numNode];
		boolean explored[] = new boolean[numNode];
		boolean repeat = true;
		for(int i = 1 ; i < numNode ;i++) {
			rt[i] = queue.removeMin();
		}			
		
		while(repeat) {			
			repeat = false;
			for(NodeMergedTree node: rt) {	

				if(node == null || explored[node.getId()])
					continue;
				
				NodeMergedTree parent = node.getParent();	
				functionalVariational[node.getId()].value = calculateVariational(node);
				if(functionalVariational[node.getId()].value <= 0) { 									
					repeat = true;
					explored[node.getId()] = true;
					mTree.updateNodeToMerge(node.getInfo());
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
		
		ComputerFunctionalVariational fattr = new ComputerFunctionalVariational(tree, 100, false);
		
		ImageBuilder.saveImage(fattr.getSimplifiedImage(), new File("/Users/gobber/Desktop/reconstructed2.png"));	
		System.err.println("Finished");	
		
	}
}
