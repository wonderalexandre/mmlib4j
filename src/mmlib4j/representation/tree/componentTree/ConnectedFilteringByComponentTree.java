package mmlib4j.representation.tree.componentTree;


import java.util.Iterator;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleArrayList;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.ComputerAttributeBasedPerimeterExternal;
import mmlib4j.representation.tree.attribute.ComputerBasicAttribute;
import mmlib4j.representation.tree.attribute.ComputerCentralMomentAttribute;
import mmlib4j.representation.tree.attribute.ComputerDistanceTransform;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValueComponentTree;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValueComponentTree.ExtinctionValueNode;
import mmlib4j.representation.tree.attribute.ComputerFunctionalAttribute;
import mmlib4j.representation.tree.attribute.ComputerFunctionalVariational;
import mmlib4j.representation.tree.attribute.ComputerMserComponentTree;
import mmlib4j.representation.tree.attribute.ComputerTbmrComponentTree;
import mmlib4j.representation.tree.attribute.bitquads.ComputerAttributeBasedOnBitQuads;
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
	private boolean hasComputerFuncitonalVariational = false;
	private ComputerFunctionalVariational fv = null;
	private ComputerDistanceTransform dt = null;
	
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
			case Attribute.MOMENT_COMPACTNESS:
			case Attribute.MOMENT_ECCENTRICITY:
			case Attribute.MOMENT_ELONGATION:
			case Attribute.MOMENT_LENGTH_MAJOR_AXES:
			case Attribute.MOMENT_LENGTH_MINOR_AXES:
			case Attribute.MOMENT_ORIENTATION:
			case Attribute.MOMENT_ASPECT_RATIO:
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
	
	public ComputerFunctionalVariational computerFunctionalVariational(double scale) {
		if(!hasComputerFuncitonalVariational) {
			computerAttributeBasedPerimeterExternal();
			fv = new ComputerFunctionalVariational(this, scale);
			fv.addAttributeInNodesCT(getListNodes());
		}
		return fv;
	} 
	
	public void computerFunctionalAttribute(){		
		if(!hasComputerFunctionalAttribute){
			computerAttributeBasedPerimeterExternal();
			new ComputerFunctionalAttribute(this).addAttributeInNodesCT(getListNodes());
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
				for(int p: no.getCanonicalPixels())
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
		else if(typeSimplification == MorphologicalTreeFiltering.RULE_DIRECT)
			return filteringByDirectRule(attributeValue, type);
		else if(typeSimplification == MorphologicalTreeFiltering.RULE_SUBTRACTIVE)
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
		return null;
	}
	
	public GrayScaleImage filteringByDirectRule(double attributeValue, int type){
		return null;
	}
	
	public GrayScaleImage filteringBySubtractiveRule(double attributeValue, int type){
		return null;
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
	
	public InfoPrunedTree getInfoPrunedTreeByViterbi(double attributeValue, int type){
		return null;
	}
	
	public void simplificationTree(double attributeValue, int attributeType, int typeSimplification) {
		if(typeSimplification == MorphologicalTreeFiltering.PRUNING_MIN)
			simplificationTreeByPruningMin(attributeValue, attributeType);
		else if(typeSimplification == MorphologicalTreeFiltering.PRUNING_MAX)
			simplificationTreeByPruningMax(attributeValue, attributeType);
		else if(typeSimplification == MorphologicalTreeFiltering.PRUNING_VITERBI)
			simplificationTreeByPruningViterbi(attributeValue, attributeType);
		else if(typeSimplification == MorphologicalTreeFiltering.RULE_DIRECT)
			simplificationTreeByDirectRule(attributeValue, attributeType);
		else if(typeSimplification == MorphologicalTreeFiltering.RULE_SUBTRACTIVE)
			simplificationTreeBySubstractiveRule(attributeValue, attributeType);
		
		
		throw new RuntimeException("type filtering invalid");
	}
	
	public void simplificationTreeByPruningMin(double attributeValue, int type){
		
		
	}
	
	public void simplificationTreeByPruningMax(double attributeValue, int type){
		
	}
	
	public void simplificationTreeByPruningViterbi(double attributeValue, int type){
		
	}
	
	public void simplificationTreeByDirectRule(double attributeValue, int type){
		
	}
	
	public void simplificationTreeBySubstractiveRule(double attributeValue, int type){
		
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
		
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());;
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(getRoot());
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
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
					for(Integer p: nodePruning.getCanonicalPixels())
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
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<InfoPrunedTree.NodePrunedTree> fifo = new Queue<InfoPrunedTree.NodePrunedTree>();
		fifo.enqueue( prunedTree.getRoot() );
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
			for(int p: node.getCanonicalPixels()){
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
			NodeLevelSets node = (NodeCT) obj;
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