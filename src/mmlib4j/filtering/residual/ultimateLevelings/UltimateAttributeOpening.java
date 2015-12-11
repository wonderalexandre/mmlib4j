package mmlib4j.filtering.residual.ultimateLevelings;


import java.util.ArrayList;

import mmlib4j.datastruct.Queue;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
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
	private NodeCT[] mapNodes;
	
	private ArrayList<NodeCT> nodeDistribution[];
	
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
		NodeCT root = tree.getRoot();
		maxContrastLUT = new int[tree.getNumNode()];
		associatedIndexLUT = new int[tree.getNumNode()];
		mapNodes = new NodeCT[tree.getNumNode()];
		nodesWithMaxResidues = new boolean[tree.getNumNode()];
		
		maxContrastLUT[root.getId()] = 0;		
		associatedIndexLUT[root.getId()] = 0;

		if (root.getChildren() != null) {
			for(NodeCT no: root.getChildren()){
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
	
	public boolean hasNodeSelectedInPrimitive(NodeCT currentNode){
		if(selectedForFiltering == null) return true;
		
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(currentNode);
		while(!fifo.isEmpty()){
			NodeCT node = fifo.dequeue();
			
			if(selectedForFiltering[node.getId()]){ 
				return true;
			}
				
			for(NodeCT n: node.getChildren()){
				if(selectedForPruning[n.getId()] == false)
					fifo.enqueue(n);
			}
			
		}
		return false;
	}
	

	
	private void computeUAO(NodeCT currentNode, boolean qPropag, boolean flagInit, boolean isCalculateResidue, NodeCT firstNodeInNR, NodeCT firstNodeNotInNR){
		int contrast = 0;
		boolean flagResido = false;
		boolean flagPropag = false;
		NodeCT parentNode = currentNode.getParent();
		
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
			NodeCT associatedNode;
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
					nodeDistribution[id] = new ArrayList<NodeCT>();
				nodeDistribution[id].add(currentNode); //computer granulometries
			}
		}
		
		for(NodeCT no: currentNode.getChildren()){
			computeUAO(no, flagPropag, flagInit, isCalculateResidue, firstNodeInNR, firstNodeNotInNR);
		}
	}
	
	
	public ArrayList<NodeCT>[] getNodeDistribuition(){
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
		GrayScaleImage imgA = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		boolean map[] = getNodesWithMaximumResidues();
		for(NodeCT node: tree.getListNodes()){
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
		GrayScaleImage transformImg = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(tree.getRoot());
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				transformImg.setPixel(p, maxContrastLUT[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeCT son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		
		return transformImg;
	}
	
	
	public boolean[] getNodesWithMaximumResidues(){
		return nodesWithMaxResidues;
	}
	public NodeCT[] getNodesMap(){
		NodeCT map[] = new NodeCT[imgInput.getSize()];
		
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(tree.getRoot());
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				map[p] = mapNodes[no.getId()];
			}
			if(no.getChildren() != null){
				for(NodeCT son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return map;
	}
	
	public GrayScaleImage getAssociateIndexImage(){
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_32BITS, imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(tree.getRoot());
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				associateImg.setPixel(p, associatedIndexLUT[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeCT son: no.getChildren()){
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