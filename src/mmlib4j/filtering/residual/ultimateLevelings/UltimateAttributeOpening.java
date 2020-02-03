package mmlib4j.filtering.residual.ultimateLevelings;


import java.util.ArrayList;

import mmlib4j.datastruct.Queue;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.AbstractImageFactory;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedAttribute;
import mmlib4j.segmentation.Labeling;
import mmlib4j.utils.Utils;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class UltimateAttributeOpening {

	private ComponentTree tree;
	private int[] maxContrastLUT;
	private int[] associatedIndexLUT;
	boolean selectedForPruning[] = null;
	boolean selectedForFiltering[] = null;
	private int typeParam;
	private int maxCriterion;
	private GrayScaleImage imgInput;
	private boolean computerDistribution = false; 
	boolean[] nodesWithMaxResidues;
	private NodeLevelSets[] mapNodes;
	
	private ArrayList<NodeLevelSets> nodeDistribution[];
	
	public int[] getMaxContrastLUT() {
		return this.maxContrastLUT;
	}

	public int[] getAssociatedIndexLUT() {
		return this.associatedIndexLUT;
	}

	public UltimateAttributeOpening(ComponentTree tree){
		this.tree = tree;
		this.imgInput = tree.getInputImage();
	}
	
	public void computeUAO(int paramValueMax, int typeParam, boolean mapNodePruning[]){
		this.computeUAO(paramValueMax, typeParam, mapNodePruning, null);
		
	}
	
	public void enableComputerDistribution(boolean b){
		computerDistribution = b;
		
	}
	
	
	public void computeUAO(int paramValueMax, int typeParam, boolean mapNodePruning[], boolean selectedShape[]){
		long ti = System.currentTimeMillis();
		this.typeParam = typeParam;
		this.maxCriterion = paramValueMax;
		if(computerDistribution){
			nodeDistribution = new ArrayList[maxCriterion+1];
		}
		this.selectedForPruning = mapNodePruning;
		this.selectedForFiltering = selectedShape;
		NodeLevelSets root = tree.getRoot();
		maxContrastLUT = new int[tree.getNumNode()];
		associatedIndexLUT = new int[tree.getNumNode()];
		mapNodes = new NodeLevelSets[tree.getNumNode()];
		nodesWithMaxResidues = new boolean[tree.getNumNode()];
		
		maxContrastLUT[root.getId()] = 0;		
		associatedIndexLUT[root.getId()] = 0;

		if (root.getChildren() != null) {
			for(NodeLevelSets no: root.getChildren()){
				computeUAO(no, false, false, false, null, root);
			}
		}
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [Ultimate attribute opening]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}

	
	public void computeUAO(int paramValueMax, int typeParam){
		this.computeUAO(paramValueMax, typeParam, new PruningBasedAttribute((MorphologicalTreeFiltering) tree, typeParam).getMappingSelectedNodes());	
	}
	
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
	

	
	private void computeUAO(NodeLevelSets currentNode, boolean qPropag, boolean flagInit, boolean isCalculateResidue, NodeLevelSets firstNodeInNR, NodeLevelSets firstNodeNotInNR){
		int contrast = 0;
		boolean flagResido = false;
		boolean flagPropag = false;
		NodeLevelSets parentNode = currentNode.getParent();
		
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
				contrast = (int) Math.abs( currentNode.getLevel() - firstNodeNotInNR.getLevel() );
			}
			
			int maxContrast;
			int associatedIndex;
			NodeLevelSets associatedNode;
			if (maxContrastLUT[parentNode.getId()] >= contrast) {
				maxContrast = maxContrastLUT[parentNode.getId()];
				associatedIndex = associatedIndexLUT[parentNode.getId()];
				associatedNode = mapNodes[parentNode.getId()];
			}
			else{
				maxContrast = contrast;
				if(flagResido && qPropag){
					associatedIndex = associatedIndexLUT[parentNode.getId()];
					associatedNode = mapNodes[parentNode.getId()];
				}
				else{
					associatedIndex = (int)currentNode.getAttributeValue(typeParam) + 1;
					nodesWithMaxResidues[currentNode.getId()] = true;
					associatedNode = currentNode;
				}
				flagPropag = true;
				
			}
			maxContrastLUT[currentNode.getId()] = maxContrast;
			associatedIndexLUT[currentNode.getId()] = associatedIndex;
			mapNodes[currentNode.getId()] = associatedNode;
			
			if(computerDistribution == true && associatedIndex != 0 && isCalculateResidue){
				if(nodeDistribution[id] == null)
					nodeDistribution[id] = new ArrayList<NodeLevelSets>();
				nodeDistribution[id].add(currentNode); //computer granulometries
			}
		}
		
		for(NodeLevelSets no: currentNode.getChildren()){
			computeUAO(no, flagPropag, flagInit, isCalculateResidue, firstNodeInNR, firstNodeNotInNR);
		}
	}
	
	
	public ArrayList<NodeLevelSets>[] getNodeDistribuition(){
		return nodeDistribution;
	}
	
	
	public void patternSpectrum(GrayScaleImage label){
		
		// indice X area dos indices
		int max = label.maxValue();
		System.out.println("indice X area dos indices");
		for(int i=0; i <= max; i++){
			System.out.print(label.countPixels(i) +"\t");
		}
		System.out.println("\nindice X quantidade de CC");
		//indice X quantidade de CC
		int h[] = Labeling.countFlatzone(label, tree.getAdjacency());
		for(int i=0; i < h.length; i++){
			System.out.print(h[i] +"\t");
		}
	}
	

	public GrayScaleImage getAttributeResidues(int attr){
		GrayScaleImage imgA = AbstractImageFactory.instance.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		boolean map[] = getNodesWithMaximumResidues();
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
	
	public GrayScaleImage getResidues(){
		GrayScaleImage transformImg = AbstractImageFactory.instance.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(tree.getRoot());
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCompactNodePixels()){
				transformImg.setPixel(p, maxContrastLUT[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		
		return transformImg;
	}
	
	
	public boolean[] getNodesWithMaximumResidues(){
		return nodesWithMaxResidues;
	}
	public NodeLevelSets[] getNodesMap(){
		NodeLevelSets map[] = new NodeLevelSets[imgInput.getSize()];
		
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(tree.getRoot());
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCompactNodePixels()){
				map[p] = mapNodes[no.getId()];
			}
			if(no.getChildren() != null){
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return map;
	}
	
	public GrayScaleImage getAssociateIndexImage(){
		GrayScaleImage associateImg = AbstractImageFactory.instance.createGrayScaleImage(AbstractImageFactory.DEPTH_32BITS, imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(tree.getRoot());
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCompactNodePixels()){
				associateImg.setPixel(p, associatedIndexLUT[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		
		//patternSpectrum(associateImg);
		//WindowImages.show(Labeling.labeling(getResidues(), AdjacencyRelation.getCircular(1.5)).randomColor());
		//System.out.println("eh monotone-planing (assoc): " + Levelings.isMonotonePlaning(associateImg, imgEntrada, AdjacencyRelation.getCircular(1.5)));
		return associateImg;
	}
	

}