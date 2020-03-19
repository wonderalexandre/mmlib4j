package mmlib4j.representation.tree.componentTree;


import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleArrayList;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.AbstractImageFactory;
import mmlib4j.representation.tree.InfoMergedTree;
import mmlib4j.representation.tree.InfoMergedTreeLevelOrder;
import mmlib4j.representation.tree.InfoMergedTreeReverseLevelOrder;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.InfoMergedTree.NodeMergedTree;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.ComputerAttributeBasedPerimeterExternal;
import mmlib4j.representation.tree.attribute.ComputerBasicAttribute;
import mmlib4j.representation.tree.attribute.ComputerBasicAttributeUpdate;
import mmlib4j.representation.tree.attribute.ComputerCentralMomentAttribute;
import mmlib4j.representation.tree.attribute.ComputerCentralMomentAttributeUpdate;
import mmlib4j.representation.tree.attribute.ComputerDistanceTransform;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValueComponentTree;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValueComponentTree.ExtinctionValueNode;
import mmlib4j.representation.tree.attribute.ComputerFunctionalAttribute;
import mmlib4j.representation.tree.attribute.ComputerMserComponentTree;
import mmlib4j.representation.tree.attribute.ComputerTbmrComponentTree;
import mmlib4j.representation.tree.attribute.ComputerViterbi;
import mmlib4j.representation.tree.attribute.bitquads.ComputerAttributeBasedOnBitQuads;
import mmlib4j.representation.tree.attribute.mergetree.ComputerBasicAttributeMergeTree;
import mmlib4j.representation.tree.attribute.mergetree.ComputerCentralMomentAttributeMergeTree;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedGradualTransition;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ConnectedFilteringByComponentTree extends ComponentTree implements MorphologicalTreeFiltering{
	
	private boolean hasComputerBasicAttribute = false;
	private boolean hasComputerAttributeBasedPerimeterExternal = false;
	private boolean hasComputerCentralMomentAttribute = false;
	private boolean hasComputerAttributeBasedBitQuads = false;
	private boolean hasComputerDistanceTransform = false;
	private boolean hasComputerFunctionalAttribute = false;
	private ComputerDistanceTransform dt = null;
	private InfoMergedTree mTree = null;
	
	public ConnectedFilteringByComponentTree(GrayScaleImage img, AdjacencyRelation adj, boolean isMaxtree){
		super(img, adj, isMaxtree);
		computerBasicAttribute();
	}
	
	public ConnectedFilteringByComponentTree(ComponentTree c) {
		super(c);
		computerBasicAttribute();
	}
	
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
		}
	}
	
	public ComputerDistanceTransform computerDistanceTransform(){
		if(!hasComputerDistanceTransform){
			dt = new ComputerDistanceTransform(numNode, getRoot(), imgInput);
			hasComputerDistanceTransform = true;
		}
		return dt;
	}
	
	/*
	public void computerPatternEulerAttribute(){
		if(!hasComputerAttributeBasedBitQuads){
			long ti = System.currentTimeMillis();
			new ComputerPatternEulerAttribute(numNode, getRoot(), imgInput, adj).addAttributeInNodesCT(getListNodes());
			hasComputerAttributeBasedBitQuads = true;
			if(Utils.debug){
				long tf = System.currentTimeMillis();
				System.out.println("Tempo de execucao [attribute euler] "+ ((tf - ti) /1000.0)  + "s");
			}
		}
	}*/
	
	public void computerFunctionalAttribute(){		
		if(!hasComputerFunctionalAttribute){
			computerAttributeBasedPerimeterExternal();
			new ComputerFunctionalAttribute(this, imgInput).addAttributeInNodesCT(getListNodes());
			hasComputerFunctionalAttribute = true;
		}
	}
	
	public void computerAttributeBasedBitQuads(){
		if(!hasComputerAttributeBasedBitQuads){
			new ComputerAttributeBasedOnBitQuads(this).addAttributeInNodesCT(getListNodes());
			hasComputerAttributeBasedBitQuads = true;
		}
	}

	public void computerCentralMomentAttribute(){
		if(!hasComputerCentralMomentAttribute){
			new ComputerCentralMomentAttribute(numNode, getRoot(), imgInput.getWidth()).addAttributeInNodesCT(getListNodes());
			hasComputerCentralMomentAttribute = true;
		}
	} 
	
	public void computerBasicAttribute(){
		if(!hasComputerBasicAttribute){
			new ComputerBasicAttribute(numNode, getRoot(), imgInput).addAttributeInNodesCT(getListNodes());
			hasComputerBasicAttribute = true;
		}
	}
	
	public void computerAttributeBasedPerimeterExternal(){
		if(!hasComputerAttributeBasedPerimeterExternal){
			new ComputerAttributeBasedPerimeterExternal(numNode, getRoot(), getInputImage()).addAttributeInNodesCT(getListNodes());
			hasComputerAttributeBasedPerimeterExternal = true;
		}
	}
		
	
	public void simplificationByCriterion(int alpha){
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(this.root);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			if(no != this.root && Math.abs(no.getLevel() - no.getParent().getLevel()) <= alpha){ //poda
				//merge
				NodeLevelSets parent = no.getParent(); 
				for(int p: no.getCompactNodePixels())
					parent.addPixel(p);
				
				
				parent.getChildren().remove(no);
				for(NodeLevelSets son: no.getChildren()){
					parent.getChildren().add(son);
					son.setParent( parent );
					fifo.enqueue(son);
				}
				
			}else{
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);
				}
			}
		}
		
		createNodesMap();
		computerInforTree(this.root, 0);

	}	
	
	/**
	 * Cria uma imagem filtrada. 
	 * Obs: Para gerar a imagem filtrada nao eh alterado a estrutura original da arvore 
	 * @param attributeValue - valor do atributo
	 * @param idAttribute - tipo do atributo
	 * @return imagem filtrada
	 */
	public GrayScaleImage getImageFiltered(double attributeValue, int type, int typeSimplification){
		
		if(typeSimplification == MorphologicalTreeFiltering.PRUNING_MIN)
			return filteringByPruningMin(attributeValue, type);
		else if(typeSimplification == MorphologicalTreeFiltering.PRUNING_MAX)
			return filteringByPruningMax(attributeValue, type);
		else if(typeSimplification == MorphologicalTreeFiltering.PRUNING_VITERBI)
			return filteringByPruningViterbi(attributeValue, type);
		else if(typeSimplification == MorphologicalTreeFiltering.DIRECT_RULE)
			return filteringByDirectRule(attributeValue, type);
		else if(typeSimplification == MorphologicalTreeFiltering.SUBTRACTIVE_RULE)
			return filteringBySubtractiveRule(attributeValue, type);
		/*
		if(typeSimplification == MorphologicalTreeFiltering.PRUNING_EXTINCTION_VALUE)
			return filteringByExtinctionValue(attributeValue, type);
		else 
			if(typeSimplification == MorphologicalTreeFiltering.PRUNING_MIN)
			return filteringByPruning(attributeValue, type);
		*/
		
		throw new RuntimeException("type filtering invalid");
	}
	
	public GrayScaleImage filteringByPruningMin(double attributeValue, int type){
		return getInfoPrunedTreeByMin(attributeValue, type).reconstruction();
	}
	
	public GrayScaleImage filteringByPruningMax(double attributeValue, int type){
		return getInfoPrunedTreeByMax(attributeValue, type).reconstruction();
	}
	
	public GrayScaleImage filteringByPruningViterbi(double attributeValue, int type){				
		return getInfoPrunedTreeByViterbi(attributeValue, type).reconstruction();
	}

	public GrayScaleImage filteringByDirectRule(double attributeValue, int type){
		return getInfoMergedTreeByDirectRule(attributeValue, type).reconstruction();
	}
	public GrayScaleImage filteringBySubtractiveRule(double attributeValue, int type) {		
		return getInfoMergedTreeBySubstractiveRule(attributeValue, type).reconstruction();
	}
		
	public InfoPrunedTree getInfoPrunedTree(double attributeValue, int attributeType, int typeSimplification){		
		if(typeSimplification == MorphologicalTreeFiltering.PRUNING_MIN)
			return getInfoPrunedTreeByMin(attributeValue, typeSimplification);
		else if(typeSimplification == MorphologicalTreeFiltering.PRUNING_MAX)
			return getInfoPrunedTreeByMax(attributeValue, typeSimplification);
		else if(typeSimplification == MorphologicalTreeFiltering.PRUNING_VITERBI)
			return getInfoPrunedTreeByViterbi(attributeValue, typeSimplification);						
		throw new RuntimeException("type filtering invalid");
	}
	
	
	/*
	 * Minimum decision: A node Nk is preserved if M(Nk) ≥ λ and if all its ancestors also satisfy this condition. 
	 * The node is removed otherwise.
	 */
	public InfoPrunedTree getInfoPrunedTreeByMin(double attributeValue, int type){
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(getRoot());
		while(!fifo.isEmpty()) {
			NodeLevelSets node = fifo.dequeue();
			if(getAttribute(node, type) >= attributeValue){ //not pruning <=> preserve			
				prunedTree.addNodeNotPruned(node);
				for(NodeLevelSets son: node.getChildren()) {
					fifo.enqueue(son);
				}
			}
		}
		return prunedTree;
	}
	
	/*
	 * Maximum decision: A node is removed if M(Nk) < λ and if all its descendant nodes satisfy the same relation.
	 * The node Nk is preserved otherwise.
	 */
	public InfoPrunedTree getInfoPrunedTreeByMax(double attributeValue, int type){
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		boolean criterion[] = new boolean[numNode]; 
		for(NodeLevelSets node: this.getListNodes().reverse()) { //reverse order: when a node is computed, means that all its descendants nodes also was computed
			boolean prunedDescendants = false;
			
			if(getAttribute(node, type) < attributeValue)
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
	
	/*
	 * Viterbi rule: A node is removed based on the optimum trellis path. 
	 * The node is preserved if it is preserved in trellis path, and it is removed
	 * otherwise. The optimum trellis path is obtained by the Viterbi Algorithm.
	 */
	public InfoPrunedTree getInfoPrunedTreeByViterbi(double attributeValue, int type){
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		boolean criterion[] = new ComputerViterbi(getRoot(), getNumNode(), attributeValue, type).getNodesByViterbi();		
		for(NodeLevelSets node : listNode) {
			if(!criterion[node.getId()]) {
				prunedTree.addNodeNotPruned(node);
			}
		}
		return prunedTree;
	}
	
	public void simplificationTree(double attributeValue, int attributeType, int typeSimplification) {
		if(typeSimplification == MorphologicalTreeFiltering.PRUNING_MIN)
			simplificationTreeByPruningMin(attributeValue, attributeType);
		else if(typeSimplification == MorphologicalTreeFiltering.PRUNING_MAX)
			simplificationTreeByPruningMax(attributeValue, attributeType);
		else if(typeSimplification == MorphologicalTreeFiltering.PRUNING_VITERBI)
			simplificationTreeByPruningViterbi(attributeValue, attributeType);
		else if(typeSimplification == MorphologicalTreeFiltering.DIRECT_RULE)
			simplificationTreeByDirectRule(attributeValue, attributeType);
		else if(typeSimplification == MorphologicalTreeFiltering.SUBTRACTIVE_RULE)
			simplificationTreeBySubstractiveRule(attributeValue, attributeType);
		else
			throw new RuntimeException("type filtering invalid");
	}
	
	public void simplificationTreeByPruningMin(double attributeValue, int type){
		boolean[] update = new boolean[numNodeIdMax];		
		boolean[] modified = new boolean[numNodeIdMax];		
		
		int newNumNodeIdMax = 1;
		NodeLevelSets parent;	
		
		for(NodeLevelSets node : listNode.reverse()) {
			if(node == getRoot())
				continue;							
			parent = node.getParent();				
			if(node.getAttributeValue(type) < attributeValue) {																
				ComponentTree.prunning(this, node);												
				update[parent.getId()] = true;
				modified[parent.getId()] = true;
			} else { 										
				// This helps to decrease the size of auxiliary structures
				if(node.getId() > newNumNodeIdMax)  															
					newNumNodeIdMax = node.getId();					
				// Pass the update to all ancestors
				if(update[node.getId()])
					update[parent.getId()] = true;
			}
			
		}				
		
		new ComputerBasicAttributeUpdate(numNodeIdMax, getRoot(), imgInput, update, modified);
		
		// Verify each attribute that was computed before
		if(hasComputerCentralMomentAttribute) {
			new ComputerCentralMomentAttributeUpdate(numNodeIdMax, getRoot(), update, modified);	
		}
		
		// Modify maxID to optimize memory
		numNodeIdMax = newNumNodeIdMax + 1;
	}
	
	public void simplificationTreeByPruningMax(double attributeValue, int type){	
		boolean[] update = new boolean[numNodeIdMax];		
		boolean[] modified = new boolean[numNodeIdMax];
		
		int newNumNodeIdMax = 1;
		NodeLevelSets parent;
		
		for(NodeLevelSets node : listNode.reverse()) {
			if(node == getRoot())
				continue;		
			parent = node.getParent();	
			if(node.getAttributeValue(type) < attributeValue && node.getChildren().isEmpty()) {		
				mergeParent(node);			
				update[parent.getId()] = true;
				modified[parent.getId()] = true;
			} else { 
				// This helps to decrease the size of auxiliary structures
				if(node.getId() > newNumNodeIdMax)
					newNumNodeIdMax = node.getId();
				// Pass the update to all ancestors
				if(update[node.getId()])
					update[parent.getId()] = true;
			}
			
		}		
		
		new ComputerBasicAttributeUpdate(numNodeIdMax, getRoot(), imgInput, update, modified);
		
		// Verify each attribute that was computed before
		if(hasComputerCentralMomentAttribute)
			new ComputerCentralMomentAttributeUpdate(numNodeIdMax, getRoot(), update, modified);	

		// Modify maxID to optimize memory		
		numNodeIdMax = newNumNodeIdMax + 1;
		
	}
	
	public void simplificationTreeByPruningViterbi(double attributeValue, int type){}
	
	/*
	 * Take care! This operation modifies the original tree structure. (root is not removed)
	 * */	
	public void simplificationTreeByDirectRule(double attributeValue, int type){
		boolean[] update = new boolean[numNodeIdMax];		
		boolean[] modified = new boolean[numNodeIdMax];
		
		int newNumNodeIdMax = 1;
		NodeLevelSets parent;
		
		for(NodeLevelSets node : listNode.reverse()) {
			if(node == getRoot())
				continue;								
			parent = node.getParent();								
			if(node.getAttributeValue(type) < attributeValue) {		
				mergeParent(node);			
				update[parent.getId()] = true;
				modified[parent.getId()] = true;
			} else { 
				// This helps to decrease the size of auxiliary structures
				if(node.getId() > newNumNodeIdMax)
					newNumNodeIdMax = node.getId();
				// Pass the update to all ancestors
				if(update[node.getId()])
					update[parent.getId()] = true;
			}
		}
		
		new ComputerBasicAttributeUpdate(numNodeIdMax, getRoot(), imgInput, update, modified);
		
		// Verify each attribute that was computed before
		if(hasComputerCentralMomentAttribute)
			new ComputerCentralMomentAttributeUpdate(numNodeIdMax, getRoot(), update, modified);	

		// Modify maxID to optimize memory		
		numNodeIdMax = newNumNodeIdMax + 1;
	}
	
	
	/*
	 * Take care! This operation modifies the original tree structure. (root is not removed)
	 * */	
	public void simplificationTreeBySubstractiveRule(double attributeValue, int type){
		boolean[] update = new boolean[numNodeIdMax];
		boolean[] modified = new boolean[numNodeIdMax];
		int offset[] = new int[numNodeIdMax];
		int newNumNodeIdMax = 1;				
		NodeLevelSets parent;
		
		// Compute offsets
		for(NodeLevelSets node : listNode) {
			if(node == getRoot())
				continue;
			parent = node.getParent();						
			if(node.getAttributeValue(type) < attributeValue) {
				offset[node.getId()] = offset[parent.getId()] - node.getLevel() + parent.getLevel();				
			} else {
				// This helps to decrease the size of auxiliary structures
				if(node.getId() > newNumNodeIdMax){
					newNumNodeIdMax = node.getId();
				}				
				offset[node.getId()] = offset[parent.getId()];									
			}
		}		
		
		// Propagate differences
		for(NodeLevelSets node : listNode.reverse()) {
			if(node == getRoot())
				continue;
			parent = node.getParent();			
			if(node.getAttributeValue(type) < attributeValue) {
				mergeParent(node);			
		 		update[parent.getId()] = true;				
		 		modified[parent.getId()] = true;
			} else {				
			 	node.setLevel(node.getLevel() + offset[node.getId()]);
			 	// This node was changed by offset
			 	if(offset[node.getId()] != 0) {			 		
			 		update[node.getId()] = true;
			 		modified[node.getId()] = true;
			 	}
				// Pass the update to all ancestors
			 	if(update[node.getId()])
					update[parent.getId()] = true;
			}
		}			
		
		// Always computed
		new ComputerBasicAttributeUpdate(numNodeIdMax, getRoot(), imgInput, update, modified);
		
		// Verify each attribute that was computed before
		if(hasComputerCentralMomentAttribute) {
			new ComputerCentralMomentAttributeUpdate(numNodeIdMax, getRoot(), update, modified);
		}
		
		// Modify maxID to optimize memory		
		numNodeIdMax = newNumNodeIdMax + 1; 		
	}
	
	/*
	 * This only exists if one of the following two methods was called before.
	 **/	
	public InfoMergedTree getMtree() {
		return mTree;
	}
	
	/*
	 * This operation keeps the original tree structure unchanged.
	 **/		
	public InfoMergedTree getInfoMergedTreeByDirectRule(double attributeValue, int type){			
		
		boolean[] update = new boolean[numNodeIdMax];
		NodeLevelSets parent;		
		int newNumNodeIdMax = 1;
		
		if(mTree == null) {			
			mTree = new InfoMergedTreeReverseLevelOrder(getRoot(), numNodeIdMax, imgInput);
			for(NodeLevelSets node : listNode.reverse()) {
				if(node == getRoot())
					continue;							
				parent = node.getParent();								
				if(node.getAttributeValue(type) < attributeValue) {						
					// Merge
					mTree.addNodeToMerge(node);
					update[parent.getId()] = true;			
				} else { 
					// Not merge
					mTree.addNodeNotMerge(node);
					// This helps to decrease the size of auxiliary structures
					if(node.getId() > newNumNodeIdMax)
						newNumNodeIdMax = node.getId();
					// Pass the update to all ancestors
					if(update[node.getId()])
						update[parent.getId()] = true;
				}
			}
		} else {
			for(NodeMergedTree node_ : mTree.skipRoot()) {						
				if(mTree.getAttribute(node_, type) < attributeValue) {	
					mTree.updateNodeToMerge(node_);	
					for(NodeMergedTree n : node_.getParent().getPathToRoot()) {
						if(update[n.getId()])
							break;
						update[n.getId()] = true;	
					}
				} else {
					if(node_.getId() > newNumNodeIdMax)
						newNumNodeIdMax = node_.getId();
				}			
			}
		}	
		
		new ComputerBasicAttributeMergeTree(numNodeIdMax, mTree, update).addAttributeInNodes();
		
		if(hasComputerCentralMomentAttribute)
			new ComputerCentralMomentAttributeMergeTree(numNodeIdMax, mTree, update).addAttributeInNodes();		
		
		// Modify maxID to optimize memory		
		numNodeIdMax = newNumNodeIdMax + 1;
		return mTree;
	}
	
	/*
	 * This operation keeps the original tree structure unchanged.
	 **/			
	public InfoMergedTree getInfoMergedTreeBySubstractiveRule(double attributeValue, int type){			
		
		int[] offset = new int[numNodeIdMax];
		boolean[] update = new boolean[numNodeIdMax];
		NodeLevelSets parent;		
		int newNumNodeIdMax = 1;
		
		if(mTree == null) {			
			mTree = new InfoMergedTreeLevelOrder(getRoot(), numNodeIdMax, imgInput);
			for(NodeLevelSets node : listNode) {
				if(node == getRoot())
					continue;								
				parent = node.getParent();								
				if(node.getAttributeValue(type) < attributeValue) {						
					// Merge
					mTree.addNodeToMerge(node);
					// Compute offsets
					offset[node.getId()] = offset[parent.getId()] - node.getLevel() + parent.getLevel();					
					update[parent.getId()] = true;		
					for(NodeLevelSets n : parent.getPathToRoot()) {
						if(update[n.getId()])
							break;
						update[n.getId()] = true;	
					}
				} else { 
					// Not merge
					mTree.addNodeNotMerge(node);
					// This helps to decrease the size of auxiliary structures
					if(node.getId() > newNumNodeIdMax)
						newNumNodeIdMax = node.getId();
					// Propagate offset
					offset[node.getId()] = offset[parent.getId()];
					// Pass the update to all ancestors
					if(offset[node.getId()] != 0) {
						for(NodeLevelSets n : node.getPathToRoot()) {
							if(update[n.getId()])
								break;
							update[n.getId()] = true;	
						}
					}
				}
			}
		} else {
			// First compute offsets and update	
			SimpleLinkedList<NodeLevelSets> nodesToMerge = new SimpleLinkedList<>();
			for(NodeMergedTree node_ : mTree.skipRoot()) {						
				if(mTree.getAttribute(node_, type) < attributeValue) {	
					offset[node_.getId()] = offset[node_.getParent().getId()] - node_.getLevel() + node_.getParent().getLevel();				
					for(NodeMergedTree n : node_.getParent().getPathToRoot()) {
						if(update[n.getId()])
							break;
						update[n.getId()] = true;	
					}
					nodesToMerge.add(node_.getInfo());
				} else {
					offset[node_.getId()] = offset[node_.getParent().getId()];					
					if(node_.getId() > newNumNodeIdMax)
						newNumNodeIdMax = node_.getId();
					// When offset != null this node changed the level value
					if(offset[node_.getId()] != 0) {
						for(NodeMergedTree n : node_.getPathToRoot()) {
							if(update[n.getId()])
								break;
							update[n.getId()] = true;	
						}
					}
				}
			}
			// Make merges 
			mTree.updateNodeToMergeAll(nodesToMerge);
		}
		
		// Correct offsets
		mTree.updateLevels(offset);
		
		// Correct attributes
		/*long ti = System.currentTimeMillis();
		ComputerBasicAttributeMergeTree cbasic = new ComputerBasicAttributeMergeTree(numNodeIdMax, mTree, update);
		cbasic.addAttributeInNodes();
		long tf = System.currentTimeMillis();
		timeInSec = (tf - ti) /1000.0;
		numberOfCalls = cbasic.numberOfCalls;*/
		
		new ComputerBasicAttributeMergeTree(numNodeIdMax, mTree, update).addAttributeInNodes();
		
		if(hasComputerCentralMomentAttribute)
			new ComputerCentralMomentAttributeMergeTree(numNodeIdMax, mTree, update).addAttributeInNodes();		
		
		// Modify maxID to optimize memory		
		numNodeIdMax = newNumNodeIdMax + 1;
		return mTree;
	}
	
	SimpleArrayList<ExtinctionValueNode> extincaoPorNode;
	ComputerExtinctionValueComponentTree extinctionValue;
	public GrayScaleImage filteringByExtinctionValue(double attributeValue, int type){
		loadAttribute(type);
		long ti = System.currentTimeMillis();
		if(extinctionValue == null){
			extinctionValue = new ComputerExtinctionValueComponentTree(this);
			extincaoPorNode = extinctionValue.getExtinctionValueCut(type);
		}
		if(extinctionValue.getType() != type)
			extincaoPorNode = extinctionValue.getExtinctionValueCut(type);
		
		GrayScaleImage imgOut = AbstractImageFactory.instance.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());;
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(getRoot());
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCompactNodePixels()){
				imgOut.setPixel(p, no.getLevel());
			}
			for(NodeLevelSets son: no.getChildren()){
				fifo.enqueue(son);	 
			}	
		}
		boolean flags[] = new boolean[this.getNumNode()];
		for(int k=extincaoPorNode.size()-1; k >= 0 ; k--){
			NodeLevelSets no = extincaoPorNode.get(k).node;

			if(extincaoPorNode.get(k).extinctionValue <= attributeValue && !flags[no.getId()]){ //poda
				int levelPropagation = no.getLevel();
				//propagacao do nivel do pai para os filhos 
				Queue<NodeLevelSets> fifoPruning = new Queue<NodeLevelSets>();
				fifoPruning.enqueue(no);	
				while(!fifoPruning.isEmpty()){
					NodeLevelSets nodePruning = fifoPruning.dequeue();
					flags[nodePruning.getId()] = true;
					 
					for(NodeLevelSets song: nodePruning.getChildren()){ 
						fifoPruning.enqueue(song);	 
					}
					for(Integer p: nodePruning.getCompactNodePixels())
						imgOut.setPixel(p, levelPropagation);
				}
			}
			
		}
		
		long tf = System.currentTimeMillis();
		if(Utils.debug)
			System.out.println("Tempo de execucao [Componente tree - extinction value - direct]  "+ ((tf - ti) /1000.0)  + "s");
		
		return imgOut;
	}
	
	private double getAttribute(NodeLevelSets node, int type){
		loadAttribute(type);
		return node.getAttributeValue(type);	
	}
	
	
	/**
	 * Cria uma imagem filtrada. 
	 * Obs: Para gerar a imagem filtrada nao eh alterado a estrutura original da arvore 
	 * @param attributeValue - valor do atributo
	 * @param idAttribute - tipo do atributo
	 * @return imagem filtrada
	 */
	public GrayScaleImage filteringByPruning(double attributeValue, int type){
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		for(NodeLevelSets no: listNode){
			if( !(getAttribute(no, type) <= attributeValue) ){ //nao poda			
				prunedTree.addNodeNotPruned(no);
			}
		}
		return prunedTree.reconstruction();
	}
	
	public InfoPrunedTree getPrunedTreeByExtinctionValue(double attributeValue, int type){
		long ti = System.currentTimeMillis();
		loadAttribute(type);
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		
		extinctionValue = new ComputerExtinctionValueComponentTree(this);
		extincaoPorNode = extinctionValue.getExtinctionValueCut(type);
		if(Utils.debug)
			System.out.println("EV: Tempo1: "+ ((System.currentTimeMillis() - ti) /1000.0)  + "s");
		
		boolean resultPruning[] = new boolean[this.getNumNode()];
		for(ExtinctionValueNode nodeEV: extincaoPorNode){
			
			if(getAttribute(nodeEV.nodeAncestral, type) < attributeValue){ //poda				
				NodeLevelSets node = nodeEV.nodeAncestral;	
				for(NodeLevelSets song: node.getChildren()){
					if(!resultPruning[song.getId()])
						for(NodeLevelSets n: song.getNodesDescendants())
							resultPruning[n.getId()] = true;	 
				}
				
			}
		}
		
		for(NodeLevelSets no: listNode){
			if( ! resultPruning[no.getId()]  ){ //nao poda
				prunedTree.addNodeNotPruned(no);
			}
		}
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [Componente tree - filtering by pruning extinction value]  "+ ((tf - ti) /1000.0)  + "s");
		}
		
		return prunedTree;
	}
	

	public GrayScaleImage reconstruction(InfoPrunedTree prunedTree){
		GrayScaleImage imgOut = AbstractImageFactory.instance.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<InfoPrunedTree.NodePrunedTree> fifo = new Queue<InfoPrunedTree.NodePrunedTree>();
		fifo.enqueue(prunedTree.getRoot());
		while(!fifo.isEmpty()){
			InfoPrunedTree.NodePrunedTree node_ = fifo.dequeue();
			NodeLevelSets node = node_.getInfo();
			for(NodeLevelSets son: node.getChildren()){				
				if(prunedTree.wasPruned(son)){
					for(int p: son.getPixelsOfCC()){						
						imgOut.setPixel(p, node.getLevel());
					}
				}
			}
			for(int p: node.getCompactNodePixels()){
				imgOut.setPixel(p, node.getLevel());
			}
			for(InfoPrunedTree.NodePrunedTree son: node_.getChildren()){			
				fifo.enqueue( son );	
			}
			
		}
		return imgOut;
	}
	
	/**
	 * Cria uma imagem filtrada. 
	 * Obs: Para gerar a imagem filtrada nao eh alterado a estrutura original da arvore 
	 * @param attributeValue - valor do atributo
	 * @param idAttribute - tipo do atributo
	 * @return imagem filtrada
	 */
	public InfoPrunedTree getPrunedTree(double attributeValue, int type){
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		for(NodeLevelSets no: listNode){
			if( !(getAttribute(no, type) <= attributeValue) ){ //nao poda
				prunedTree.addNodeNotPruned(no);
			}
		}
		return prunedTree;
	}
	
	public InfoPrunedTree getPrunedTreeByMSER(double attributeValue, int type, int delta){
		long ti = System.currentTimeMillis();
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		ComputerMserComponentTree mser = new ComputerMserComponentTree(this);
		boolean resultPruning[] = new boolean[this.getNumNode()];
		SimpleLinkedList<NodeLevelSets> list = mser.getNodesByMSER(delta);
		for(NodeLevelSets node: list){
			if(getAttribute(node, type) <= attributeValue){ //poda				
				for(NodeLevelSets song: node.getChildren()){
					for(NodeLevelSets n: song.getNodesDescendants())
						resultPruning[n.getId()] = true;	 
				}
			}
		}
		
		for(NodeLevelSets no: listNode){
			if( ! resultPruning[no.getId()]  ){ //nao poda
				prunedTree.addNodeNotPruned(no);
			}
		}
		
		
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [Componente tree - filtering by pruning mser]  "+ ((tf - ti) /1000.0)  + "s");
		}
		
		return prunedTree;
	}
	
	
	
	public InfoPrunedTree getPrunedTreeByGradualTransition(double attributeValue, int type, int delta){
		long ti = System.currentTimeMillis();
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		PruningBasedGradualTransition gt = new PruningBasedGradualTransition(this, type, delta); 
		boolean resultPruning[] = gt.getMappingSelectedNodes( );
		SimpleLinkedList<NodeLevelSets> list = gt.getListOfSelectedNodes( );
		for(NodeLevelSets obj: list){
			NodeLevelSets node = (NodeLevelSets) obj;
			if(getAttribute(node, type) <= attributeValue){ //poda				
				for(NodeLevelSets song: node.getChildren()){
					for(NodeLevelSets n: song.getNodesDescendants())
						resultPruning[n.getId()] = true;	 
				}
			}
		}
		
		for(NodeLevelSets no: listNode){
			if( ! resultPruning[no.getId()]  ){ //nao poda
				prunedTree.addNodeNotPruned(no);
			}
		}
		
		
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [Componente tree - filtering by pruning gradual transition]  "+ ((tf - ti) /1000.0)  + "s");
		}
		
		
		return prunedTree;
	}
	
	public InfoPrunedTree getPrunedTreeByTBMR(double attributeValue, int type, int tMin, int tMax){
		long ti = System.currentTimeMillis();
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		ComputerTbmrComponentTree tbmr = new ComputerTbmrComponentTree(this);
		boolean resultPruning[] = new boolean[this.getNumNode()];
		boolean result[] = tbmr.getSelectedNode(tMin, tMax);
		for(NodeLevelSets node: listNode){
			if(getAttribute(node, type) <= attributeValue && result[node.getId()]){ //poda				
				for(NodeLevelSets song: node.getChildren()){
					for(NodeLevelSets n: song.getNodesDescendants())
						resultPruning[n.getId()] = true;	 
				}
			}
		}
		
		for(NodeLevelSets no: listNode){
			if( ! resultPruning[no.getId()]  ){ //nao poda
				prunedTree.addNodeNotPruned(no);
			}
		}
		
		
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [Componente tree - filtering by pruning mser]  "+ ((tf - ti) /1000.0)  + "s");
		}
		
		return prunedTree;
	}

}