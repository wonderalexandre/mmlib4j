package mmlib4j.representation.tree.attribute;

import java.io.File;
import java.util.Arrays;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.filtering.AttributeFilters;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageAlgebra;
import mmlib4j.utils.ImageBuilder;

/**
 * 
 * 	MMLib4J - Mathematical Morphology Library for Java
 * 	@author Charles Ferreira Gobber
 * 
 *	This class implements the Viterbi Algorithm to filter Component Trees 
 *  and we adapt also to Tree of Shapes. 
 * 
 * 	The Viterbi algorithm is applied to find a set of optimal decisions,
 *  and in this case we also impose the increasing condition. It means that,
 *  a non increase attribute will perform an increase filtering (pruning). 
 *  
 *  Useful References:
 *  
 *  [1] SALEMBIER, Philippe; OLIVERAS, Albert; GARRIDO, Luis. 
 *  	Antiextensive connected operators for image and sequence processing. 
 *  	IEEE Transactions on Image Processing, v. 7, n. 4, p. 555-570, 1998.
 * 
 * 	[2] SALEMBIER, Philippe; GARRIDO, Luis. 
 * 		Connected operators based on region-tree pruning. 
 * 		In: Mathematical Morphology and its Applications to Image and Signal Processing. 
 * 		Springer, Boston, MA, 2002. p. 169-178.
 *  
 **/

public class ComputerViterbi extends AttributeComputedIncrementally{

	private int numNode;
	private NodeLevelSets root;
	// The TrellisPath is equivalent to Preserved Path, 
	// because the Removed Path is unnecessary.
	private boolean[] TrellisPath;
	private double[] CostPathR;
	private double[] CostPathP;
	private double attributeValue;
	private int type;
	
	public ComputerViterbi(NodeLevelSets root, int numNode, double attributeValue, int type) {		
		this.type = type;
		this.attributeValue = attributeValue;
		this.root = root;
		this.numNode = numNode;
		this.TrellisPath = new boolean[numNode];
		this.CostPathR = new double[numNode];
		this.CostPathP = new double[numNode];		 				
		computerAttribute(root);		
	}
	
	public boolean[] getNodesByViterbi() {		
		boolean mapviterbi[] = new boolean[numNode];
		Arrays.fill(mapviterbi, true);
		SimpleLinkedList<NodeLevelSets> children = root.getChildren();		
		//
		// Backtracking the nodes from the root to selected trellis path.
		// Note that, since this is a pruning strategy, when the node is removed 
		// we do not need to go through the trellis in it descendants.
		//
		for(NodeLevelSets node : children) {			
			double costP = CostPathP[root.getId()] + CostPathP[node.getId()];
			double costR = CostPathR[root.getId()] + CostPathR[node.getId()];
			if(costP < costR) {				
				Queue<NodeLevelSets> queue =  new Queue<NodeLevelSets>();				
				queue.enqueue(node);				
				while(!queue.isEmpty()) {					
					NodeLevelSets n = queue.dequeue();					
					if(TrellisPath[n.getId()] == true) {							
						mapviterbi[n.getId()] = false;
						SimpleLinkedList<NodeLevelSets> nchildren = n.getChildren();
						for(NodeLevelSets child : nchildren) {			
							queue.enqueue(child);
						}		
						
					}
									
				}
				
			}
		}		
		return mapviterbi;		
	}
	
	@Override
	public void preProcessing(NodeLevelSets v) {
		// remove by criterion
		if(v.getAttributeValue(type) <= attributeValue) {
			CostPathP[v.getId()] = 1;
			CostPathR[v.getId()] = 0;
		}else { // preserve by criterion
			CostPathP[v.getId()] = 0;
			CostPathR[v.getId()] = 1;
		}
	}

	@Override
	public void mergeChildren(NodeLevelSets parent, NodeLevelSets son) {}

	@Override
	public void posProcessing(NodeLevelSets nodeki) {
		//
		// nodeki = child node to compute cost
		// nodek  = parent of nodeki
		// preserveCostKi = cost of preserve ki based on remove or preserve it 
		//
		if(nodeki.getParent() != null) {		
			NodeLevelSets nodek = nodeki.getParent();
			double preserveCostKi = 0;
			if(CostPathP[nodeki.getId()] <= CostPathR[nodeki.getId()]) {
				TrellisPath[nodeki.getId()] = true; // preserve node
				preserveCostKi = CostPathP[nodeki.getId()]; 
			}else {
				TrellisPath[nodeki.getId()] = false; // remove node
				preserveCostKi = CostPathR[nodeki.getId()];
			}							
			if(nodek != root) {
				CostPathP[nodek.getId()] += preserveCostKi;
				CostPathR[nodek.getId()] += CostPathR[nodeki.getId()];
			}			
		}
		
	}
	
	public static void main(String args[]) {
		
		GrayScaleImage input = ImageBuilder.openGrayImage(new File("/Users/gobber/Desktop/lena.jpg"));
		MorphologicalTree tree = new ComponentTree(input, AdjacencyRelation.getAdjacency8(), true);
		
		//tree.simplificationTreeByDirectRule(3000, Attribute.AREA);
		Attribute.loadAttribute(tree, Attribute.FUNCTIONAL_ATTRIBUTE);
		AttributeFilters af = new AttributeFilters(tree);
		af.simplificationTreeBySubstractiveRule(3000, Attribute.FUNCTIONAL_ATTRIBUTE);
		GrayScaleImage output = tree.reconstruction();
		//GrayScaleImage output = tree.filteringByPruningViterbi(3000, Attribute.AREA);
		
		MorphologicalTree tree2 = new ComponentTree(input, AdjacencyRelation.getAdjacency8(), true);
		AttributeFilters af2 = new AttributeFilters(tree2);
		GrayScaleImage img2 = af2.filteringByPruningMin(3000, Attribute.AREA);
		
		//System.out.println(ImageAlgebra.equals(output, img2));
		System.out.println(ImageAlgebra.isLessOrEqual(output, input));
		ImageBuilder.saveImage(output, new File("/Users/gobber/Desktop/output.png"));
		
	}

}
