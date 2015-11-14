package mmlib4j.representation.tree.componentTree;

import java.util.ArrayList;
import java.util.LinkedList;

import mmlib4j.datastruct.Queue;
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
import mmlib4j.representation.tree.attribute.ComputerMserComponentTree;
import mmlib4j.representation.tree.attribute.ComputerPatternEulerAttribute;
import mmlib4j.representation.tree.attribute.ComputerTbmrComponentTree;
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
	private boolean hasComputerPatternEulerAttribute = false;
	private boolean hasComputerDistanceTransform = false;
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
			case Attribute.PERIMETER:
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
				computerAttributeBasedPerimeterExternal();
				break;
				
			case Attribute.NUM_HOLES:
				computerPatternEulerAttribute();
				break;
				
		}
	}
	
	public ComputerDistanceTransform computerDistanceTransform(){
		if(!hasComputerDistanceTransform){
			long ti = System.currentTimeMillis();
			dt = new ComputerDistanceTransform(numNode, getRoot(), imgInput);
			hasComputerDistanceTransform = true;
			if(Utils.debug){
				long tf = System.currentTimeMillis();
				System.out.println("Tempo de execucao [computer distance transform] "+ ((tf - ti) /1000.0)  + "s");
			}
		}
		return dt;
	}
	
	public void computerPatternEulerAttribute(){
		if(!hasComputerPatternEulerAttribute){
			long ti = System.currentTimeMillis();
			new ComputerPatternEulerAttribute(numNode, getRoot(), imgInput, adj).addAttributeInNodesCT(getListNodes());
			hasComputerPatternEulerAttribute = true;
			if(Utils.debug){
				long tf = System.currentTimeMillis();
				System.out.println("Tempo de execucao [attribute euler] "+ ((tf - ti) /1000.0)  + "s");
			}
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
			long ti = System.currentTimeMillis();
			new ComputerBasicAttribute(numNode, getRoot(), imgInput).addAttributeInNodesCT(getListNodes());
			hasComputerBasicAttribute = true;
			if(Utils.debug){
				long tf = System.currentTimeMillis();
				System.out.println("Tempo de execucao [basic attribute] "+ ((tf - ti) /1000.0)  + "s");
			}
		}
	}
	
	public void computerAttributeBasedPerimeterExternal(){
		if(!hasComputerAttributeBasedPerimeterExternal){
			long ti = System.currentTimeMillis();
			new ComputerAttributeBasedPerimeterExternal(numNode, getRoot(), getInputImage()).addAttributeInNodesCT(getListNodes());
			hasComputerAttributeBasedPerimeterExternal = true;
			if(Utils.debug){
				long tf = System.currentTimeMillis();
				System.out.println("Tempo de execucao [external perimeter] "+ ((tf - ti) /1000.0)  + "s");
			}
		}
	}
		
	
	public void simplificationByCriterion(int alpha){
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(this.root);
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			if(no != this.root && Math.abs(no.level - no.parent.level) <= alpha){ //poda
				//merge
				NodeCT parent = no.parent; 
				for(int p: no.pixels)
					parent.addPixel(p);
				
				
				parent.children.remove(no);
				for(NodeCT son: no.children){
					parent.children.add(son);
					son.parent = parent;
					fifo.enqueue(son);
				}
				
			}else{
				for(NodeCT son: no.children){
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
	public GrayScaleImage filtering(double attributeValue, int type, int typePruning, int typeRec){
		if(typePruning == MorphologicalTreeFiltering.PRUNING_EXTINCTION_VALUE)
			return filteringByExtinctionValue(attributeValue, type);
		else if(typePruning == MorphologicalTreeFiltering.PRUNING)
			return filteringByPruning(attributeValue, type);
		
		throw new RuntimeException("type filtering invalid");
	}
	
	
	ArrayList<ExtinctionValueNode> extincaoPorNode;
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
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(getRoot());
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				imgOut.setPixel(p, no.level);
			}
			for(NodeCT son: no.children){
				fifo.enqueue(son);	 
			}	
		}
		
		for(int k=extincaoPorNode.size()-1; k >= 0 ; k--){
			NodeCT no = extincaoPorNode.get(k).node;

			if(extincaoPorNode.get(k).extinctionValue <= attributeValue){ //poda
				int levelPropagation = no.level;
				//propagacao do nivel do pai para os filhos 
				Queue<NodeCT> fifoPruning = new Queue<NodeCT>();
				fifoPruning.enqueue(no);	
				while(!fifoPruning.isEmpty()){
					NodeCT nodePruning = fifoPruning.dequeue();
					if(nodePruning.children != null){ 
						for(NodeCT song: nodePruning.children){ 
							fifoPruning.enqueue(song);	 
						}
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
	
	private double getAttribute(NodeCT node, int type){
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
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(this.root);
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		for(NodeCT no: listNode){
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
				NodeCT node = nodeEV.nodeAncestral;	
				for(NodeCT song: node.children){
					if(!resultPruning[song.getId()])
						for(NodeCT n: song.getNodesDescendants())
							resultPruning[n.getId()] = true;	 
				}
				
			}
		}
		
		for(NodeCT no: listNode){
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
			NodeCT node = (NodeCT) node_.getInfo();
			for(NodeCT son: node.getChildren()){
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
		for(NodeCT no: listNode){
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
		LinkedList<NodeCT> list = mser.getNodesByMSER(delta);
		for(NodeCT node: list){
			if(getAttribute(node, type) <= attributeValue){ //poda				
				for(NodeCT song: node.children){
					for(NodeCT n: song.getNodesDescendants())
						resultPruning[n.getId()] = true;	 
				}
			}
		}
		
		for(NodeCT no: listNode){
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
		LinkedList<NodeLevelSets> list = gt.getListOfSelectedNodes( );
		for(NodeLevelSets obj: list){
			NodeCT node = (NodeCT) obj;
			if(getAttribute(node, type) <= attributeValue){ //poda				
				for(NodeCT song: node.children){
					for(NodeCT n: song.getNodesDescendants())
						resultPruning[n.getId()] = true;	 
				}
			}
		}
		
		for(NodeCT no: listNode){
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
		for(NodeCT node: listNode){
			if(getAttribute(node, type) <= attributeValue && result[node.getId()]){ //poda				
				for(NodeCT song: node.children){
					for(NodeCT n: song.getNodesDescendants())
						resultPruning[n.getId()] = true;	 
				}
			}
		}
		
		for(NodeCT no: listNode){
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