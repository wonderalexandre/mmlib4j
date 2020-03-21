package mmlib4j.filtering.residual.ultimateLevelings;


import java.util.ArrayList;

import mmlib4j.datastruct.Queue;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.AbstractImageFactory;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedAttribute;
import mmlib4j.representation.tree.tos.TreeOfShape;
import mmlib4j.utils.Utils;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class UltimateGrainFilter {

	private TreeOfShape tree;

	private boolean selectedForPruning[] = null;
	private boolean selectedForFiltering[] = null;
	private GrayScaleImage imgInput;
	private NodeLevelSets root;

	private int[] residuesNodePos;
	private int[] associatedNodePos;
	private int[] residuesNodeNeg;
	private int[] associatedNodeNeg;
	boolean[] nodesWithMaxResiduesPos;
	boolean[] nodesWithMaxResiduesNeg;
	
	private int[] residuesNodeType;
	private int[] associatedNodeType;
	
	private int typeParam;
	private int maxCriterion;
	
	private boolean computerDistribution = false; 
	private ArrayList<NodeLevelSets> nodeDistribution[];
	private NodeLevelSets[] mapNodes;
	
	private int typeResiduo;
	public final static int RESIDUES_POS_MAX_MAX = 1;
	public final static int RESIDUES_POS_MIN_MAX = 2;
	public final static int RESIDUES_NEG_MIN_MIN = 3;
	public final static int RESIDUES_NEG_MAX_MIN = 4;
	
	
	public UltimateGrainFilter(TreeOfShape tree){
		this.tree = tree;
		this.root = tree.getRoot();
		this.imgInput = tree.getInputImage();
	}
	
	public void computeUAO(int paramValueMax, int typeParam){
		this.computeUGF(paramValueMax, typeParam, new PruningBasedAttribute((MorphologicalTreeFiltering) tree, typeParam).getMappingSelectedNodes());	
	}
	
	public void computeUGF(int paramValueMax, int typeParam, boolean mapNodePruning[]){
		this.computeUGF(paramValueMax, typeParam, mapNodePruning, null);
	}
	
	public void enableComputerDistribution(boolean b){
		computerDistribution = b;
	}
	
	
	public void computeUGF(int paramValueMax, int typeParam, boolean mapNodePruning[], boolean selectedShape[]){
		long ti = System.currentTimeMillis();
		this.maxCriterion = paramValueMax;
		this.typeParam = typeParam;
		this.selectedForPruning = mapNodePruning;
		this.selectedForFiltering = selectedShape;
		if(computerDistribution){
			nodeDistribution = new ArrayList[maxCriterion+1];
		}
		mapNodes = new NodeLevelSets[tree.getNumNode()];
		this.residuesNodePos = new int[tree.getNumNode()];
		this.associatedNodePos = new int[tree.getNumNode()];
		this.residuesNodeNeg = new int[tree.getNumNode()];
		this.associatedNodeNeg = new int[tree.getNumNode()];
		nodesWithMaxResiduesPos = new boolean[tree.getNumNode()];
		nodesWithMaxResiduesNeg = new boolean[tree.getNumNode()];
		
		if (root.getChildren() != null) {
			for(NodeLevelSets no: root.getChildren()){
				computeUGF(no, false, false, false, null, root);
			}
		}
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [Ultimate leveling]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}
	

	private void computeUGF(NodeLevelSets currentNode, boolean qPropag, boolean flagInit, boolean isCalculateResidue, NodeLevelSets firstNodeInNR, NodeLevelSets firstNodeNotInNR){
		boolean flagResido = false;
		boolean flagPropag = false;
		NodeLevelSets parentNode = currentNode.getParent();
		
		NodeLevelSets associatedNode;
		int maxContrastPos;
		int linkedAttributesPos;
		int maxContrastNeg;
		int linkedAttributesNeg;
		int contrastPos = 0;
		int contrastNeg = 0;
		
		if(currentNode.getAttributeValue(typeParam) <= maxCriterion && selectedForPruning[currentNode.getId()]){
			flagInit = true;
			//firstNodeNotInNR = parentNode;
			
		}
		
		if(flagInit){ //currentNode pertence a Nr(i)?		
			int id = (int)currentNode.getAttributeValue(typeParam);
			if( selectedForPruning[currentNode.getId()] ){
				firstNodeInNR = currentNode;
				firstNodeNotInNR = parentNode;
				isCalculateResidue = (selectedForFiltering == null? true : hasNodeSelectedInPrimitive(currentNode) );
			}
			else{
				id = (int) firstNodeInNR.getAttributeValue(typeParam);
				flagResido = true;
			}
			
			
			if( isCalculateResidue ){ //non Filter?
				contrastPos = Math.max(0, currentNode.getLevel() - firstNodeNotInNR.getLevel() );
				contrastNeg = Math.max(0, firstNodeNotInNR.getLevel() - currentNode.getLevel());
			}
			
			//residuo positivo
			if (residuesNodePos[parentNode.getId()] >= contrastPos) {
				maxContrastPos = residuesNodePos[parentNode.getId()];
				linkedAttributesPos = associatedNodePos[parentNode.getId()];
				associatedNode = mapNodes[parentNode.getId()];
			}
			else{
				maxContrastPos = contrastPos;
				if(flagResido && qPropag){
					linkedAttributesPos = associatedNodePos[parentNode.getId()];
					associatedNode = mapNodes[parentNode.getId()];
				}
				else{
					linkedAttributesPos = (int)currentNode.getAttributeValue(typeParam) + 1;
					nodesWithMaxResiduesPos[currentNode.getId()] = true;
					associatedNode = currentNode;
				}
				flagPropag = true;
				
			}
			
			//residuo negativo
			if (residuesNodeNeg[parentNode.getId()] >= contrastNeg) {
				maxContrastNeg = residuesNodeNeg[parentNode.getId()];
				linkedAttributesNeg = associatedNodeNeg[parentNode.getId()];
				associatedNode = mapNodes[parentNode.getId()];
			}
			else{
				maxContrastNeg = contrastNeg;
				if(flagResido && qPropag){
					linkedAttributesNeg = associatedNodeNeg[parentNode.getId()];
					associatedNode = mapNodes[parentNode.getId()];
				}
				else{
					linkedAttributesNeg = (int)currentNode.getAttributeValue(typeParam) + 1;
					nodesWithMaxResiduesNeg[currentNode.getId()] = true;
					associatedNode = currentNode;
				}
				flagPropag = true;
				
			}
			
			
			residuesNodePos[currentNode.getId()] = maxContrastPos;
			associatedNodePos[currentNode.getId()] = linkedAttributesPos;
			residuesNodeNeg[currentNode.getId()] = maxContrastNeg;
			associatedNodeNeg[currentNode.getId()] = linkedAttributesNeg;
			if(typeResiduo != 0){
				if(typeResiduo == RESIDUES_POS_MAX_MAX && currentNode.isNodeMaxtree() == true && parentNode.isNodeMaxtree() == true){
					residuesNodeType[currentNode.getId()] = maxContrastPos;
					associatedNodeType[currentNode.getId()] = linkedAttributesPos;
				}
				else if(typeResiduo == RESIDUES_POS_MIN_MAX && currentNode.isNodeMaxtree() == false && parentNode.isNodeMaxtree() == true){
					residuesNodeType[currentNode.getId()] = maxContrastPos;;
					associatedNodeType[currentNode.getId()] = linkedAttributesPos;		
				}
				else if(typeResiduo == RESIDUES_NEG_MIN_MIN && currentNode.isNodeMaxtree() == false && parentNode.isNodeMaxtree() == false){
					residuesNodeType[currentNode.getId()] = maxContrastNeg;
					associatedNodeType[currentNode.getId()] = linkedAttributesNeg;
				}
				else if(typeResiduo == RESIDUES_NEG_MAX_MIN && currentNode.isNodeMaxtree() == true && parentNode.isNodeMaxtree() == false){
					residuesNodeType[currentNode.getId()] = maxContrastNeg;
					associatedNodeType[currentNode.getId()] = linkedAttributesNeg;		
				}
			}
			mapNodes[currentNode.getId()] = associatedNode;
			
			if(computerDistribution == true && linkedAttributesPos+linkedAttributesNeg != 0 && isCalculateResidue){
				if(nodeDistribution[id] == null)
					nodeDistribution[id] = new ArrayList<NodeLevelSets>();
				nodeDistribution[id].add(currentNode); //computer granulometries
			}
		}
		
		for(NodeLevelSets no: currentNode.getChildren()){
			computeUGF(no, flagPropag, flagInit, isCalculateResidue, firstNodeInNR, firstNodeNotInNR);
		}
	}
	
	public boolean[] getNodesMapWithMaximumResiduesPos(){
		return nodesWithMaxResiduesPos;
	}
	
	public boolean[] getNodesMapWithMaximumResiduesNeg(){
		return nodesWithMaxResiduesNeg;
	}
	

	public ArrayList<NodeLevelSets>[] getNodeDistribuition(){
		return nodeDistribution;
	}
	
	public boolean[] getNodesMapWithMaximumResidues(){
		boolean map[] = new boolean[nodesWithMaxResiduesNeg.length];
		for(int i=0; i < map.length; i++){
			map[i] = nodesWithMaxResiduesNeg[i] || nodesWithMaxResiduesPos[i];
		}
		return map;
	}
	
	/*
	public boolean hasNodeSelectedInPrimitive_OLD(NodeLevelSets currentNode){
		if(selectedForFiltering == null) return true;
		
		boolean result = selectedForPruning[currentNode.getId()];
		
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(currentNode);
		while(!fifo.isEmpty()){
			NodeLevelSets node = fifo.dequeue();
			
			if(selectedForPruning[node.getId()] == result){
				
				if(selectedForFiltering[node.getId()]){ //extrair residuo do node selecionado
					return true;
				}
				
				for(NodeLevelSets n: node.getChildren()){
					if(selectedForPruning[n.getId()] != result)
						fifo.enqueue(n);
				}
			}
			else{
				if(selectedForFiltering[node.getId()]){ //extrair residuo do node selecionado
					return true;
				}	
			}
		}
		return false;
	}
	*/
	public boolean hasNodeSelectedInPrimitive(NodeLevelSets currentNode){
		if(selectedForFiltering == null) return true;
		
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(currentNode);
		while(!fifo.isEmpty()){
			NodeLevelSets node = fifo.dequeue();
			
			if(selectedForFiltering[node.getId()]){ 
				return true;
			}
				
			for(NodeLevelSets n: node.getChildren()){
				if(selectedForPruning[n.getId()] == false)
					fifo.enqueue(n);
			}
			
		}
		return false;
	}
	
	public GrayScaleImage getResiduesPos(){
		GrayScaleImage transformImg = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCompactNodePixels()){
				transformImg.setPixel(p, residuesNodePos[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}

		//System.out.println("eh conexo (pos): " + Levelings.isPlanning(transformImg, imgEntrada, AdjacencyRelation.getCircular(1)));
		//System.out.println("eh crescente (pos): " + Levelings.isMonotonePlaning(transformImg, imgEntrada, AdjacencyRelation.getCircular(1)));
		//System.out.println("eh decrescente (pos): " + Levelings.isMonotoneDescPlaning(transformImg, imgEntrada, AdjacencyRelation.getCircular(1)));
		
		return transformImg;
	}
	
	public GrayScaleImage getResiduesNeg(){
		GrayScaleImage transformImg = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCompactNodePixels()){
				transformImg.setPixel(p, residuesNodeNeg[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}

		//System.out.println("eh conexo (neg): " + Levelings.isPlanning(transformImg, imgEntrada, AdjacencyRelation.getCircular(1)));
		//System.out.println("eh crescente (neg): " + Levelings.isMonotonePlaning(transformImg, imgEntrada, AdjacencyRelation.getCircular(1)));
		//System.out.println("eh decrescente (neg): " + Levelings.isMonotoneDescPlaning(transformImg, imgEntrada, AdjacencyRelation.getCircular(1)));
		
		return transformImg;
	}
	
	public GrayScaleImage getResidues(){
		GrayScaleImage transformImg = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCompactNodePixels()){
				transformImg.setPixel(p, Math.max(residuesNodePos[no.getId()], residuesNodeNeg[no.getId()]));
			}
			if(no.getChildren() != null){
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return transformImg;
	}
	public GrayScaleImage getAttributeResidues(int attr){
		GrayScaleImage imgA = ImageFactory.createGrayScaleImage(AbstractImageFactory.DEPTH_32BITS, imgInput.getWidth(), imgInput.getHeight());
		boolean map[] = getNodesMapWithMaximumResidues();
		for(NodeLevelSets node: tree.getListNodes()){
			if(map[node.getId()]){
				int value = (int) node.getAttributeValue(attr);
				for(int p: node.getPixelsOfCC()){
					map[tree.getSC(p).getId()] = false;
					imgA.setPixel(p, value);
				}
			}
		}
		return imgA;
	}
	
	
	public GrayScaleImage getAssociateImagePos(){
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(AbstractImageFactory.DEPTH_32BITS, imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCompactNodePixels()){
				associateImg.setPixel(p, associatedNodePos[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return associateImg;
	}
	
	public GrayScaleImage getAssociateImage(){
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(AbstractImageFactory.DEPTH_32BITS, imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCompactNodePixels()){
				if(residuesNodePos[no.getId()] > residuesNodeNeg[no.getId()])
					associateImg.setPixel(p, associatedNodePos[no.getId()]);
				else
					associateImg.setPixel(p, associatedNodeNeg[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return associateImg;
	}
	
	public GrayScaleImage getAssociateImageNeg(){
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(AbstractImageFactory.DEPTH_32BITS, imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCompactNodePixels()){
				associateImg.setPixel(p, associatedNodeNeg[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return associateImg;
	}
	
	public GrayScaleImage getAssociateImageByType( ){
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(AbstractImageFactory.DEPTH_32BITS, imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCompactNodePixels()){
				associateImg.setPixel(p, associatedNodeType[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return associateImg;
	}
	
	
	public GrayScaleImage getResiduesByType( ){
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(AbstractImageFactory.DEPTH_32BITS, imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCompactNodePixels()){
				associateImg.setPixel(p, residuesNodeType[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return associateImg;
	}
	
	
	
	
	
	
	
	
	
}