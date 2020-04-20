package mmlib4j.filtering;

import java.util.Iterator;

import mmlib4j.datastruct.Queue;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.ComputerAttributeBasedPerimeterExternal;
import mmlib4j.representation.tree.attribute.ComputerBasicAttribute;
import mmlib4j.representation.tree.attribute.ComputerBasicAttributeUpdate;
import mmlib4j.representation.tree.attribute.ComputerCentralMomentAttribute;
import mmlib4j.representation.tree.attribute.ComputerCentralMomentAttributeUpdate;
import mmlib4j.representation.tree.attribute.ComputerDistanceTransform;
import mmlib4j.representation.tree.attribute.ComputerFunctionalAttribute;
import mmlib4j.representation.tree.attribute.ComputerViterbi;
import mmlib4j.representation.tree.attribute.bitquads.ComputerAttributeBasedOnBitQuads;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.utils.Utils;

/**
 * 
 * 
 * 	This class performs Attribute Filtering in Morphological Trees. 
 *  (this class in under construction)
 * 
 *	@author Charles Ferreira Gobber and Wonder Alexandre Luz Alves
 *	@version v2020
 * 	@since v2020
 *  @see Attribute
 *  @see MorphologicalTree
 * 
 */
public class AttributeFilters {
	
	public final static int PRUNING_MIN = 1;
	public final static int PRUNING_MAX = 2;
	public final static int PRUNING_VITERBI = 3;
	public final static int DIRECT_RULE = 4;
	public final static int SUBTRACTIVE_RULE = 5;
	
	public final static int SIMPLIFY_MIN = 6;
	public final static int SIMPLIFY_MAX = 7;
	public final static int SIMPLIFY_DIRECT = 8;
	public final static int SIMPLIFY_SUBTRACTIVE = 9;
	
	private boolean hasComputerBasicAttribute = true;
	private boolean hasComputerAttributeBasedPerimeterExternal = false;
	private boolean hasComputerCentralMomentAttribute = false;
	private boolean hasComputerAttributeBasedBitQuads = false;
	private boolean hasComputerFunctionalAttribute = false;
	
	private MorphologicalTree tree;	
	
	boolean[] update;		
	boolean[] modified;
	boolean[] prevupdate;
	
	public AttributeFilters(MorphologicalTree tree) {
		this.tree = tree;		
		update = new boolean[tree.getNumNodeIdMax()];
		modified = new boolean[tree.getNumNodeIdMax()];
		prevupdate = new boolean[tree.getNumNodeIdMax()];
		computerBasicAttribute();
	}
	
	/**
	 * 
	 *	This method loads an attribute in tree structure.
	 *
	 * 	@param attr Type of attribute (see {@link Attribute}).  
	 * 
	 */
	public void loadAttribute(int attr){
		switch(attr){
			case Attribute.ALTITUDE:
			case Attribute.AREA:
			case Attribute.VOLUME:
			case Attribute.WIDTH:
			case Attribute.HEIGHT:
			//case Attribute.PERIMETER:
			case Attribute.LEVEL:
			case Attribute.RECTANGULARITY:
			case Attribute.RATIO_WIDTH_HEIGHT:
			case Attribute.XMIN:
			case Attribute.XMAX:
			case Attribute.YMIN:
			case Attribute.YMAX:
			case Attribute.PIXEL_XMIN:
			case Attribute.PIXEL_XMAX: 
			case Attribute.PIXEL_YMIN: 
			case Attribute.PIXEL_YMAX: 
			case Attribute.SUM_X: 
			case Attribute.SUM_Y: 
				computerBasicAttribute();
				break;
				
			case Attribute.MOMENT_CENTRAL_02:
			case Attribute.MOMENT_CENTRAL_20:
			case Attribute.MOMENT_CENTRAL_11:
			case Attribute.VARIANCE_LEVEL:
			case Attribute.LEVEL_MEAN:
			case Attribute.STD_LEVEL:
			case Attribute.SUM_LEVEL_2:
			case Attribute.MOMENT_COMPACTNESS:
			case Attribute.MOMENT_ECCENTRICITY:
			case Attribute.MOMENT_ELONGATION:
			case Attribute.MOMENT_LENGTH_MAJOR_AXES:
			case Attribute.MOMENT_LENGTH_MINOR_AXES:
			case Attribute.MOMENT_ORIENTATION:
			case Attribute.MOMENT_ASPECT_RATIO:
			case Attribute.MOMENT_OF_INERTIA:
				computerCentralMomentAttribute();
				break;
			
			case Attribute.PERIMETER_EXTERNAL:
			case Attribute.CIRCULARITY:
			case Attribute.COMPACTNESS:
			case Attribute.ELONGATION:
			case Attribute.SUM_GRAD_CONTOUR:
				computerAttributeBasedPerimeterExternal();
				break;				
				
			//case Attribute.NUM_HOLES:
			case Attribute.BIT_QUADS_PERIMETER:
			case Attribute.BIT_QUADS_EULER_NUMBER:
			case Attribute.BIT_QUADS_HOLE_NUMBER:
			case Attribute.BIT_QUADS_PERIMETER_CONTINUOUS:
			case Attribute.BIT_QUADS_CIRCULARITY:
			case Attribute.BIT_QUADS_AVERAGE_AREA:
			case Attribute.BIT_QUADS_AVERAGE_PERIMETER:
			case Attribute.BIT_QUADS_AVERAGE_LENGTH:
			case Attribute.BIT_QUADS_AVERAGE_WIDTH:
				computerAttributeBasedBitQuads();
				break;
				
			case Attribute.FUNCTIONAL_ATTRIBUTE:
				computerFunctionalAttribute();
				break;
				
			default:
				throw new RuntimeException("Unsupported attribute!");
		}
	}
	
	/**
	 * 
	 *	This method returns the internal Morphological tree.
	 *
	 * 	@return The internal Morphological tree.
	 * 
	 */
	public MorphologicalTree getTree() {
		return tree;
	}
	
	/**
	 * 
	 *	This method computes the distance transform in tree.
	 * 
	 */
	public ComputerDistanceTransform computerDistanceTransform(){
		return new ComputerDistanceTransform(tree.getNumNodeIdMax(), tree.getRoot(), tree.getInputImage());
	}	
	
	/**
	 * 
	 *	This method computes some central moment attributes in tree.
	 *  (see {@link ComputerCentralMomentAttribute})
	 * 
	 */
	public void computerCentralMomentAttribute(){
		if(!hasComputerCentralMomentAttribute){
			new ComputerCentralMomentAttribute(tree.getNumNodeIdMax(), tree.getRoot(), tree.getInputImage().getWidth()).addAttributeInNodes(tree.getListNodes());
			hasComputerCentralMomentAttribute = true;
		}
	}
	
	/**
	 * 
	 *	This method computes some basic attributes in tree.
	 *  (see {@link ComputerBasicAttribute})
	 * 
	 */
	public void computerBasicAttribute(){
		if(!hasComputerBasicAttribute){
			new ComputerBasicAttribute(tree.getNumNodeIdMax(), tree.getRoot(), tree.getInputImage()).addAttributeInNodes(tree.getListNodes());
			hasComputerBasicAttribute = true;
		}
	}
	
	/**
	 * 
	 *	This method computes some attributes based on the external perimeter in tree.
	 *  (see {@link ComputerAttributeBasedPerimeterExternal})
	 * 
	 */
	public void computerAttributeBasedPerimeterExternal(){
		if(!hasComputerAttributeBasedPerimeterExternal){
			new ComputerAttributeBasedPerimeterExternal(tree.getNumNodeIdMax(), tree.getRoot(), tree.getInputImage()).addAttributeInNodes(tree.getListNodes());
			hasComputerAttributeBasedPerimeterExternal = true;
		}
	}
	
	/**
	 * 
	 *	This method computes the functional attribute.
	 *  (see {@link ComputerFunctionalAttribute})
	 * 
	 */
	public void computerFunctionalAttribute(){		
		if(!hasComputerFunctionalAttribute){
			computerAttributeBasedPerimeterExternal();			
			new ComputerFunctionalAttribute(tree, tree.getInputImage()).addAttributeInNodes(tree.getListNodes());
			hasComputerFunctionalAttribute = true;
		}
	}
	
	/**
	 * 
	 *	This method computes attributes based on bit quads.
	 *  (see {@link ComputerFunctionalAttribute})
	 * 
	 */
	public void computerAttributeBasedBitQuads(){
		if(!(tree instanceof ComponentTree)) {
			throw new UnsupportedOperationException("This attribute doesn't work for all trees yet!");
		} else if(!hasComputerAttributeBasedBitQuads){
			new ComputerAttributeBasedOnBitQuads((ComponentTree) tree).addAttributeInNodesCT(tree.getListNodes());
			hasComputerAttributeBasedBitQuads = true;
		}
	}
	
	/**
	 * 
	 * This method performs a filtering in tree based on the most common filtering rules.
	 * The tree structure is preserved.  
	 *  
	 * @param attributeValue The value of the threshold (same as $ i $).
	 * @param attributeType The type of the attribute.
	 * @param typeSimplification One of following filtering rules: PRUNING_MIN, PRUNING_MAX and PRUNING_VITERBI.
	 * @return The filtered image (an attribute filter).
	 * 
	 */	
	public GrayScaleImage getImageFiltered(double attributeValue, int attributeType, int typeSimplification) {
		if(typeSimplification == PRUNING_MIN)
			return filteringByPruningMin(attributeValue, attributeType);
		else if(typeSimplification == PRUNING_MAX)
			return filteringByPruningMax(attributeValue, attributeType);
		else if(typeSimplification == PRUNING_VITERBI)
			return filteringByPruningViterbi(attributeValue, attributeType);
		else if(typeSimplification == DIRECT_RULE)
			return filteringByDirectRule(attributeValue, attributeType);
		else if(typeSimplification == SUBTRACTIVE_RULE)
			return filteringBySubtractiveRule(attributeValue, attributeType);
		throw new RuntimeException("type filtering invalid");
	}

	/**
	 * 
	 * This method implements the min filtering rule in the tree received by the class constructor.
	 * In min rule if a node $ \tau $ is removed, then are all its descendants. The criterion of 
	 * remotion is $ \kappa(\tau) < i $, where $ \kappa $ is an attribute and $ i $ a threshold. 
	 * The tree structure is preserved.
	 *  
	 * @param attributeValue The value of the threshold (same as $ i $).
	 * @param attributeType The type of the attribute.
	 * @return The filtered image (an attribute filter).
	 * 
	 */	
	public GrayScaleImage filteringByPruningMin(double attributeValue, int attributeType){
		return getInfoPrunedTreeByMin(attributeValue, attributeType).reconstruction();
	}
	
	/**
	 * 
	 * This method implements the max filtering rule in the tree received by the class constructor.
	 * In max rule a node $ \tau $ is removed only if are all its descendants. The criterion of 
	 * remotion is $ \kappa(\tau) < i $, where $ \kappa $ is an attribute and $ i $ a threshold. 
	 * The tree structure is preserved.
	 *  
	 * @param attributeValue The value of the threshold (same as $ i $).
	 * @param attributeType The type of the attribute.
	 * @return The filtered image (an attribute filter).
	 * 
	 */	
	public GrayScaleImage filteringByPruningMax(double attributeValue, int attributeType){
		return getInfoPrunedTreeByMax(attributeValue, attributeType).reconstruction();
	}
	
	/**
	 * 
	 * This method performs pruning based on viterbi algorithm. 
	 * Viterbi rule: A node is removed based on the optimum trellis path. 
	 * The node is preserved if it is preserved in trellis path, and it is removed
	 * otherwise. The optimum trellis path is obtained by the Viterbi Algorithm. 
	 * The tree structure is preserved.
	 *  
	 * @param attributeValue The value of the threshold (same as $ i $).
	 * @param attributeType The type of the attribute.
	 * @return The filtered image (an attribute filter).
	 * 
	 */	
	public GrayScaleImage filteringByPruningViterbi(double attributeValue, int attributeType){				
		return getInfoPrunedTreeByViterbi(attributeValue, attributeType).reconstruction();
	}

	// It must be adapted from the original one
	public GrayScaleImage filteringByDirectRule(double attributeValue, int attributeType){
		//return getInfoMergedTreeByDirectRule(attributeValue, attributeType).reconstruction();
		throw new UnsupportedOperationException("This method doesn't work yet!");
	}
	
	// It must be adapted from the original one
	public GrayScaleImage filteringBySubtractiveRule(double attributeValue, int attributeType) {		
		//return getInfoMergedTreeBySubstractiveRule(attributeValue, attributeType).reconstruction();
		throw new UnsupportedOperationException("This method doesn't work yet!");
	}
	
	/**
	 * 
	 * This method returns a tree representation called InfoPrunedTree based on one of following filtering rules: 
	 * min, max and viterbi. The tree structure is preserved.  
	 *  
	 * @param attributeValue The value of the threshold (same as $ i $).
	 * @param attributeType The type of the attribute.
	 * @param typeSimplification One of following filtering rules: PRUNING_MIN, PRUNING_MAX and PRUNING_VITERBI.
	 * @return An InforPrunedTree that represents an attribute filter.
	 */
	public InfoPrunedTree getInfoPrunedTree(double attributeValue, int attributeType, int typeSimplification) {
		if(typeSimplification == PRUNING_MIN)
			return getInfoPrunedTreeByMin(attributeValue, attributeType);
		else if(typeSimplification == PRUNING_MAX)
			return getInfoPrunedTreeByMax(attributeValue, attributeType);
		else if(typeSimplification == PRUNING_VITERBI)
			return getInfoPrunedTreeByViterbi(attributeValue, attributeType);
		throw new RuntimeException("type Info Pruned Tree invalid");
	}
	
	/** 
	 * This method implements the min filtering rule in the tree received by the class constructor.
	 * In min rule if a node $ \tau $ is removed, then are all its descendants. The criterion of 
	 * remotion is $ \kappa(\tau) < i $, where $ \kappa $ is an attribute and $ i $ a threshold. 
	 * The tree structure is preserved.
	 * 
	 * @param attributeValue The value of the threshold (same as $ i $).
	 * @param attributeType The type of the attribute.
	 * @return An InfoPrunedTree constructed on min rule.
	 * 
	 */ 
	public InfoPrunedTree getInfoPrunedTreeByMin(double attributeValue, int attributeType){
		InfoPrunedTree prunedTree = new InfoPrunedTree(tree.getRoot(), tree.getNumNodeIdMax(), attributeType, attributeValue, tree.getInputImage());
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(tree.getRoot());
		while(!fifo.isEmpty()) {
			NodeLevelSets node = fifo.dequeue();
			if(node.getAttributeValue(attributeType) >= attributeValue){ //not pruning <=> preserve			
				prunedTree.addNodeNotPruned(node);
				for(NodeLevelSets son: node.getChildren()) {
					fifo.enqueue(son);
				}
			}
		}
		return prunedTree;
	}
	
	/** 
	 * 
	 * This method implements the max filtering rule in the tree received by the class constructor.
	 * In max rule a node $ \tau $ is removed only if are all its descendants. The criterion of 
	 * remotion is $ \kappa(\tau) < i $, where $ \kappa $ is an attribute and $ i $ a threshold. 
	 * The tree structure is preserved.
	 * 
	 * @param attributeValue The value of the threshold (same as $ i $).
	 * @param attributeType The type of the attribute.
	 * @return An InfoPrunedTree constructed on max rule.
	 * 
	 */ 
	public InfoPrunedTree getInfoPrunedTreeByMax(double attributeValue, int attributeType){
		InfoPrunedTree prunedTree = new InfoPrunedTree(tree.getRoot(), tree.getNumNodeIdMax(), attributeType, attributeValue, tree.getInputImage());
		boolean criterion[] = new boolean[tree.getNumNodeIdMax()]; 
		for(NodeLevelSets node: tree.getListNodes().reverse()) { //reverse order: when a node is computed, means that all its descendants nodes also was computed
			boolean prunedDescendants = false;
			
			if(node.getAttributeValue(attributeType) < attributeValue)
				criterion[node.getId()] = true;
			
			for(NodeLevelSets son: node.getChildren()) {
				criterion[node.getId()] = criterion[node.getId()] | criterion[son.getId()];
				prunedDescendants = prunedDescendants | criterion[son.getId()];
			}
				
			if(prunedDescendants || !criterion[node.getId()])		
				prunedTree.addNodeNotPruned(node);
					
		}
		return prunedTree;
	}
	
	/** 
	 * This method performs pruning based on viterbi algorithm. 
	 * Viterbi rule: A node is removed based on the optimum trellis path. 
	 * The node is preserved if it is preserved in trellis path, and it is removed
	 * otherwise. The optimum trellis path is obtained by the Viterbi Algorithm. 
	 * The tree structure is preserved.
	 * 
	 * @param attributeValue The value of the threshold (same as $ i $).
	 * @param attributeType The type of the attribute.
	 * @return An InfoPrunedTree constructed on viterbi rule.
	 * 
	 */ 
	public InfoPrunedTree getInfoPrunedTreeByViterbi(double attributeValue, int attributeType){
		InfoPrunedTree prunedTree = new InfoPrunedTree(tree.getRoot(), tree.getNumNodeIdMax(), attributeType, attributeValue, tree.getInputImage());
		boolean criterion[] = new ComputerViterbi(tree.getRoot(), tree.getNumNodeIdMax(), attributeValue, attributeType).getNodesByViterbi();		
		for(NodeLevelSets node : tree.getListNodes()) {
			if(!criterion[node.getId()]) {
				prunedTree.addNodeNotPruned(node);
			}
		}
		return prunedTree;
	}

	/**
	 * 
	 * This method performs a simplification in tree based on the most common filtering rules.
	 * This method modifies the tree structure.  
	 *  
	 * @param attributeValue The value of the threshold (same as $ i $).
	 * @param attributeType The type of the attribute.
	 * @param One of following filtering rules: SIMPLIFY_MIN, SIMPLIFY_MAX, SIMPLIFY_DIRECT or SIMPLIFY_SUBTRACTIVE.
	 * 
	 */	
	public void simplificationTree(double attributeValue, int attributeType, int typeSimplification) {
		if(typeSimplification == SIMPLIFY_MIN)
			simplificationTreeByPruningMin(attributeValue, attributeType);
		else if(typeSimplification == SIMPLIFY_MAX)
			simplificationTreeByPruningMax(attributeValue, attributeType);
		else if(typeSimplification == PRUNING_VITERBI)
			simplificationTreeByPruningViterbi(attributeValue, attributeType);
		else if(typeSimplification == SIMPLIFY_DIRECT)
			simplificationTreeByDirectRule(attributeValue, attributeType);
		else if(typeSimplification == SIMPLIFY_SUBTRACTIVE)
			simplificationTreeBySubstractiveRule(attributeValue, attributeType);
		else
			throw new RuntimeException("type filtering invalid");
	}
	
	private void updateAttributes() {
		new ComputerBasicAttributeUpdate(tree.getNumNodeIdMax(), tree.getRoot(), tree.getInputImage(), update, modified);
		if(hasComputerCentralMomentAttribute) 
			new ComputerCentralMomentAttributeUpdate(tree.getNumNodeIdMax(), tree.getRoot(), update, modified);
	}
	
	/**
	 * 
	 * This method implements the min filtering rule in the tree received by the class constructor.
	 * In min rule if a node $ \tau $ is removed, then are all its descendants. The criterion of 
	 * remotion is $ \kappa(\tau) < i $, where $ \kappa $ is an attribute and $ i $ a threshold. 
	 * This method modifies the tree structure. 
	 *  
	 * @param attributeValue The value of the threshold (same as $ i $).
	 * @param attributeType The type of the attribute.
	 * 
	 */	
	public void simplificationTreeByPruningMin(double attributeValue, int type){
		long ti = System.currentTimeMillis();
		NodeLevelSets parent;	
		
		for(NodeLevelSets node: tree.getListNodes().reverse()) {
			if(node == tree.getRoot())
				continue;
			else if(node.isLeaf() && !prevupdate[node.getId()]) {
				update[node.getId()] = false;
				modified[node.getId()] = false;
			}
			
			parent = node.getParent();
			if(!prevupdate[parent.getId()]) {
				update[parent.getId()] = false;
				modified[parent.getId()] = false;
			}			
						
			if(node.getAttributeValue(type) < attributeValue) {	
				tree.prunning(node);												
				update[parent.getId()] = true;
				modified[parent.getId()] = true;
				prevupdate[parent.getId()] = true;
			} else { 										
				// Pass the update to all ancestors
				if(update[node.getId()]) {
					update[parent.getId()] = true;
					prevupdate[parent.getId()] = true;
				}
			}			
			prevupdate[node.getId()] = false;
			
		}				
		
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [simplificationTreeByPruningMin]  "+ ((tf - ti) /1000.0)  + "s");
		}
		
		updateAttributes();	
		
	}
	
	/**
	 * 
	 * This method implements the max filtering rule in the tree received by the class constructor.
	 * In max rule a node $ \tau $ is removed only if are all its descendants. The criterion of 
	 * remotion is $ \kappa(\tau) < i $, where $ \kappa $ is an attribute and $ i $ a threshold. 
	 * This method modifies the tree structure. 
	 *  
	 * @param attributeValue The value of the threshold (same as $ i $).
	 * @param attributeType The type of the attribute.
	 * 
	 */	
	public void simplificationTreeByPruningMax(double attributeValue, int attributeType){	
		long ti = System.currentTimeMillis();
		NodeLevelSets parent;
		
		Iterator<NodeLevelSets> iterator = tree.getListNodes().reverse().iterator();
		while(iterator.hasNext()) {
			NodeLevelSets node = iterator.next();
			if(node == tree.getRoot())
				continue;
			else if(node.isLeaf() && !prevupdate[node.getId()]) {
				update[node.getId()] = false;
				modified[node.getId()] = false;
			}
			
			parent = node.getParent();
			if(!prevupdate[parent.getId()]) {
				update[parent.getId()] = false;
				modified[parent.getId()] = false;
			}
			
			if(node.getAttributeValue(attributeType) < attributeValue && node.getChildren().isEmpty()) {
				iterator.remove(); //remove in constant time
				tree.mergeParent(node);			
				update[parent.getId()] = true;
				modified[parent.getId()] = true;
				prevupdate[parent.getId()] = true;
			} else { 
				
				// Pass the update to all ancestors
				if(update[node.getId()]) {
					update[parent.getId()] = true;
					prevupdate[parent.getId()] = true;
				}
			}
			prevupdate[node.getId()] = false;
			
		}		
		
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [simplificationTreeByPruningMax]  "+ ((tf - ti) /1000.0)  + "s");
		}
		
		updateAttributes();
		
	}
	
	/**
	 * 
	 * This method implements the direct filtering rule in the tree received by the class constructor.
	 * In direct rule if a node $ \tau $ is removed it is merged with its parent. The criterion of 
	 * remotion is $ \kappa(\tau) < i $, where $ \kappa $ is an attribute and $ i $ a threshold.
	 * This method modifies the tree structure. 
	 *  
	 * @param attributeValue The value of the threshold (same as $ i $).
	 * @param attributeType The type of the attribute.
	 * 
	 */	
	public void simplificationTreeByDirectRule(double attributeValue, int attributeType){
		long ti = System.currentTimeMillis();
		
		NodeLevelSets parent;				
		Iterator<NodeLevelSets> iterator = tree.getListNodes().reverse().iterator();
		while(iterator.hasNext()) {
			NodeLevelSets node = iterator.next();
			if(node == tree.getRoot())
				continue;
			else if(node.isLeaf() && !prevupdate[node.getId()]) {
				update[node.getId()] = false;
				modified[node.getId()] = false;
			}
			
			parent = node.getParent();				
			if(!prevupdate[parent.getId()]) {
				update[parent.getId()] = false;
				modified[parent.getId()] = false;
			}
			
			if(node.getAttributeValue(attributeType) < attributeValue) {		
				iterator.remove(); //remove in constant time
				tree.mergeParent(node);			
				update[parent.getId()] = true;
				modified[parent.getId()] = true;
				prevupdate[parent.getId()] = true;
			} else { 
				// Pass the update to all ancestors
				if(update[node.getId()]) {
					update[parent.getId()] = true;
					prevupdate[parent.getId()] = true;
				}
			}
			
			prevupdate[node.getId()] = false;
		}
		
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [simplificationTreeByDirectRule]  "+ ((tf - ti) /1000.0)  + "s");
		}
		
		updateAttributes();	

	}
		
	/**
	 * 
	 * This method implements the subtractive filtering rule in the tree received by the class constructor.
	 * In subtractive rule if a node $ \tau $ is removed it is merged with its parent and all its descendants
	 * are lowered by its gray level. The criterion of remotion is $ \kappa(\tau) < i $, where $ \kappa $ is an attribute and $ i $ a threshold.
	 * This method modifies the tree structure. 
	 *  
	 * @param attributeValue The value of the threshold (same as $ i $).
	 * @param attributeType The type of the attribute.
	 * 
	 */	
	public void simplificationTreeBySubstractiveRule(double attributeValue, int attributeType){
		long ti = System.currentTimeMillis();
		int offset[] = new int[tree.getNumNodeIdMax()];			
		NodeLevelSets parent;
		
		// Compute offsets
		for(NodeLevelSets node : tree.getListNodes()) {
			if(node == tree.getRoot())
				continue;
			parent = node.getParent();						
			if(node.getAttributeValue(attributeType) < attributeValue) {
				offset[node.getId()] = offset[parent.getId()] - node.getLevel() + parent.getLevel();				
			} else {
				offset[node.getId()] = offset[parent.getId()];									
			}
		}		
		
		// Propagate differences
		Iterator<NodeLevelSets> iterator = tree.getListNodes().reverse().iterator();
		while(iterator.hasNext()) {
			NodeLevelSets node = iterator.next();
			if(node == tree.getRoot())
				continue;
			else if(node.isLeaf() && !prevupdate[node.getId()]) {
				update[node.getId()] = false;
				modified[node.getId()] = false;
			}
			
			parent = node.getParent();	
			if(!prevupdate[parent.getId()]) {
				update[parent.getId()] = false;
				modified[parent.getId()] = false;
			}
			
			if(node.getAttributeValue(attributeType) < attributeValue) {
				iterator.remove(); //remove in constant time
				tree.mergeParent(node);			
		 		update[parent.getId()] = true;				
		 		modified[parent.getId()] = true;
		 		prevupdate[parent.getId()] = true;
			} else {				
			 	node.setLevel(node.getLevel() + offset[node.getId()]);
			 	// This node was changed by offset
			 	if(offset[node.getId()] != 0) {			 		
			 		update[node.getId()] = true;
			 		modified[node.getId()] = true;
			 	}
				// Pass the update to all ancestors
			 	if(update[node.getId()]) {
					update[parent.getId()] = true;
					prevupdate[parent.getId()] = true;
			 	}
			}
			prevupdate[node.getId()] = false;
		}	
		
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [simplificationTreeBySubstractiveRule]  "+ ((tf - ti) /1000.0)  + "s");
		}
		
		updateAttributes();		
				
	}
	
	// We should think if it is possible to update the viterbi structures.
	// I believe we can use the update structure.
	public void simplificationTreeByPruningViterbi(double attributeValue, int attributeType){
		new UnsupportedOperationException("Not implemented yet");
	}

}
