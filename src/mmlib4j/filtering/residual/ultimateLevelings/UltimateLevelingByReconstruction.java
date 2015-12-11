package mmlib4j.filtering.residual.ultimateLevelings;

import java.util.ArrayList;

import mmlib4j.datastruct.PriorityQueue;
import mmlib4j.datastruct.Queue;
import mmlib4j.filtering.LinearFilters;
import mmlib4j.filtering.MorphologicalOperatorsBasedOnSE;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.segmentation.Labeling;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageAlgebra;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class UltimateLevelingByReconstruction {
	private GrayScaleImage imgInput;
	ComponentTree mintree;
	ComponentTree maxtree;
	ComponentTree tree;
	
	/**
	 * marked family
	 */
	public static final int CLOSING = 0;
	public static final int EROSION = 1;
	public static final int OPENING = 2;
	public static final int DILATION = 3;
	public static final int DILATION_EROSION = 4;
	public static final int OPENING_CLOSING = 5;
	public static final int MEDIAN = 6;
	public static final int GAUSSIAN = 7;
	
	int type; 
	
	public UltimateLevelingByReconstruction(GrayScaleImage img, int type) {
		this.imgInput = img;
		this.mintree = new ComponentTree(img,AdjacencyRelation.getAdjacency8(), false);
		this.maxtree = new ComponentTree(img,AdjacencyRelation.getAdjacency8(), true);
		this.maxtree.extendedTree();
		this.mintree.extendedTree();
	}

	public UltimateLevelingByReconstruction(GrayScaleImage img,	ComponentTree tree, int type) {
		this.imgInput = img;
		this.type = type;
		
		if(type == OPENING || type == DILATION || type == CLOSING || type == EROSION){
			this.tree = tree;
			this.tree.extendedTree();
		}else{
			throw new RuntimeException("using other construct, for example: public UltimateLevelingByReconstruction(GrayScaleImage img,	ComponentTree mintree, ComponentTree maxtree, int type)");
		}
		
		
	}

	public UltimateLevelingByReconstruction(GrayScaleImage img,	ComponentTree mintree, ComponentTree maxtree, int type) {
		this.imgInput = img;
		this.mintree = mintree;
		this.maxtree = maxtree;
		this.maxtree.extendedTree();
		this.mintree.extendedTree();
		this.type = type;
	}

	int indexNodes[] = null;
	public boolean[] getPrimitivesFamily(ComponentTree tree, int raioMax, int step) {
		PriorityQueue<Integer> queue = new PriorityQueue<Integer>(255);
		boolean[] selected = new boolean[tree.getNumNode()];
		indexNodes = new int[tree.getNumNode()];
		GrayScaleImage imgMarcador = this.imgInput;
		GrayScaleImage img ;
		
		
		for (int raio = 1; raio <= raioMax; raio += step) {
			img = imgMarcador;
			//WindowImages.show(img);
			if(type == OPENING)
				imgMarcador = MorphologicalOperatorsBasedOnSE.opening(img, AdjacencyRelation.getCircular(raio));
			else if(type == DILATION)
				imgMarcador = MorphologicalOperatorsBasedOnSE.dilation(img, AdjacencyRelation.getAdjacency8());
			else if(type == EROSION)
				imgMarcador = MorphologicalOperatorsBasedOnSE.erosion(img, AdjacencyRelation.getAdjacency8());
			else if(type == CLOSING)
				imgMarcador = MorphologicalOperatorsBasedOnSE.closing(img, AdjacencyRelation.getCircular(raio));
			else if(type == MEDIAN){
				imgMarcador = LinearFilters.median(img, AdjacencyRelation.getAdjacency8());
				if(tree.isMaxtree()){
					ImageAlgebra.minimum(imgInput, imgMarcador);
				}else{
					ImageAlgebra.maximum(imgInput, imgMarcador);
				}
			}
			else if(type == GAUSSIAN){
				imgMarcador = LinearFilters.mean(img, AdjacencyRelation.getAdjacency8());
				if(tree.isMaxtree()){
					ImageAlgebra.minimum(imgInput, imgMarcador);
				}else{
					ImageAlgebra.maximum(imgInput, imgMarcador);
				}
			}
			
			
			for (int p = 0; p < img.getSize(); p++) {
				if (tree.isMaxtree()) {
					if (imgMarcador.getPixel(p) <= img.getPixel(p)) {
						queue.add(p, imgMarcador.getPixel(p));
					}
				} else {
					if (imgMarcador.getPixel(p) >= img.getPixel(p)) {
						queue.add(p, imgMarcador.getPixel(p));
					}
				}
				tree.getSC(p).flagProcess = false;
			}

			for (NodeCT node : tree.getListNodes()) {
				node.flagPruning = true;
			}

			int p;
			while (!queue.isEmpty()) {
				if (tree.isMaxtree())
					p = queue.removeMax();
				else
					p = queue.remove();

				NodeCT node = tree.getSC(p);
				if (!node.flagProcess) {
					node.flagProcess = true;
					if (tree.isMaxtree()) {
						while (node.flagPruning	&& imgMarcador.getPixel(p) < node.getLevel()) {
							node = node.getParent();
						}
						// Invariante (neste ponto): imgMarcador.getPixel(p) >=
						// node.level ==>ou seja, este node eh preservado na
						// arvore
					} else {
						while (node.flagPruning && imgMarcador.getPixel(p) > node.getLevel()) {
							node = node.getParent();
						}

					}

					node.flagPruning = false;
					while (node.getParent() != null && node.getParent().flagPruning) {
						node.getParent().flagPruning = false;
						node = node.getParent();
					}
				}
			}
			// ////////
			Queue<NodeCT> fifo = new Queue<NodeCT>();
			fifo.enqueue(tree.getRoot());
			while (!fifo.isEmpty()) {
				NodeCT no = fifo.dequeue();
				if (no.flagPruning) { // poda
					selected[no.getId()] = true;
					indexNodes[no.getId()] = raio;
					Queue<NodeCT> fifoPruning = new Queue<NodeCT>();
					
					fifoPruning.enqueue(no);
					int levelPropagation = no.getParent() == null ? no.getLevel(): no.getParent().getLevel();
					while (!fifoPruning.isEmpty()) {
						NodeCT nodePruning = fifoPruning.dequeue();
						indexNodes[nodePruning.getId()] = raio;
						if (nodePruning.getChildren() != null) {
							for (NodeCT song : nodePruning.getChildren()) {
								fifoPruning.enqueue(song);
							}
						}
						for (Integer pixel : nodePruning.getCanonicalPixels()) {
							imgMarcador.setPixel(pixel, levelPropagation);
						}
					}
					
					tree.prunning(no);
				} else {
					if (no.getChildren() != null) {
						for (NodeCT son : no.getChildren()) {
							fifo.enqueue(son);
						}
					}
				}
			}
			
		}

		return selected;
	}
	
	
	
	private int[] maxContrastLUT;
	private int[] associatedIndexLUT;
	
	private int[] maxContrastLUTPos;
	private int[] associatedIndexLUTPos;
	
	private int[] maxContrastLUTNeg;
	private int[] associatedIndexLUTNeg;
	
	boolean selectedForPruning[] = null;
	boolean selectedForFiltering[] = null;
	private int typeParam;
	private int maxCriterion;
	private boolean computerDistribution = false; 
	
	private ArrayList<NodeCT> nodeDistribution[];
	
	public int[] getMaxContrastLUTPos() {
		return this.maxContrastLUTPos;
	}

	public int[] getAssociatedIndexLUTPos() {
		return this.associatedIndexLUTPos;
	}

	public int[] getMaxContrastLUTNeg() {
		return this.maxContrastLUTNeg;
	}

	public int[] getAssociatedIndexLUTNeg() {
		return this.associatedIndexLUTNeg;
	}
	
	public void enableComputerDistribution(boolean b){
		computerDistribution = b;
	}
	
	
	private void computeUAO(int paramValueMax, boolean []selectedForPruning, boolean selectedShape[]){
		long ti = System.currentTimeMillis();
		this.maxCriterion = paramValueMax;
		
		this.selectedForPruning = selectedForPruning;
		this.selectedForFiltering = selectedShape;
		NodeCT root = tree.getRoot();
		
		maxContrastLUT = new int[tree.getNumNode()];
		associatedIndexLUT = new int[tree.getNumNode()];
		
		nodeDistribution = new ArrayList[maxCriterion+2];
		maxContrastLUT[root.getId()] = 0;		
		associatedIndexLUT[root.getId()] = 0;
		
		if (root.getChildren() != null) {
			for(NodeCT no: root.getChildren()){
				computeUAO(no, false, false, false, null, root);
			}
		}
		
		if(tree.isMaxtree()){
			maxContrastLUTPos = maxContrastLUT;
			associatedIndexLUTPos = associatedIndexLUT;
		}else{
			maxContrastLUTNeg = maxContrastLUT;
			associatedIndexLUTNeg = associatedIndexLUT;
		}
		
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [Ultimate attribute opening]  "+ ((tf - ti) /1000.0)  + "s");		
	}

	
	public void computeUAO(int paramValueMax, int step){
		if(type == OPENING || type == CLOSING || type == DILATION || type == EROSION){
			ComponentTree treeCopy = tree.getClone();
			treeCopy.extendedTree();
			this.computeUAO(paramValueMax, getPrimitivesFamily(treeCopy, paramValueMax, step), null);
		}
		else{
			if(type == OPENING_CLOSING)
				type = OPENING;
			if(type == DILATION_EROSION)
				type = EROSION;
			tree = maxtree;
			ComponentTree treeCopy = tree.getClone();
			treeCopy.extendedTree();
			this.computeUAO(paramValueMax, getPrimitivesFamily(treeCopy, paramValueMax, step), null);
			
			
			if(type == OPENING)
				type = CLOSING;
			if(type == EROSION)
				type = DILATION;
			tree = mintree;
			treeCopy = tree.getClone();
			treeCopy.extendedTree();
			this.computeUAO(paramValueMax, getPrimitivesFamily(treeCopy, paramValueMax, step), null);
			
		}
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
	
		if(selectedForPruning[currentNode.getId()]){
			flagInit = true;
		}
		
		
		if(flagInit){ //currentNode pertence a Nr(lambda)?		
			int id;
			if( selectedForPruning[currentNode.getId()] ){
				id = indexNodes[currentNode.getId()];
				firstNodeInNR = currentNode;
				firstNodeNotInNR = parentNode;
				isCalculateResidue = (selectedForFiltering == null? true : hasNodeSelectedInPrimitive(currentNode) );
			}
			else{
				//id = firstNodeInNR.getAttributeValue(typeParam);
				id = indexNodes[parentNode.getId()];;
				flagResido = true;
			}
			
			
			if( isCalculateResidue ){ //non Filter?
				contrast = (int) Math.abs( currentNode.getLevel() - firstNodeNotInNR.getLevel() );
			}
			
			int maxContrast;
			int associatedIndex;
			
			if (maxContrastLUT[parentNode.getId()] >= contrast) {
				maxContrast = maxContrastLUT[parentNode.getId()];
				associatedIndex = associatedIndexLUT[parentNode.getId()];
			}
			else{
				maxContrast = contrast;
				if(flagResido && qPropag){
					associatedIndex = associatedIndexLUT[parentNode.getId()];
				}
				else{
					associatedIndex = id;
				}
				flagPropag = true;
				
			}
			maxContrastLUT[currentNode.getId()] = maxContrast;
			associatedIndexLUT[currentNode.getId()] = associatedIndex;
			
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
	
	
	public GrayScaleImage getResiduesPos(){
		GrayScaleImage transformImg = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(maxtree.getRoot());
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				transformImg.setPixel(p, maxContrastLUTPos[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeCT son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return  transformImg;
	}
	
	public GrayScaleImage getResiduesNeg(){
		GrayScaleImage transformImg = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(mintree.getRoot());
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				transformImg.setPixel(p, maxContrastLUTNeg[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeCT son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return  transformImg;
	}
	
	public GrayScaleImage getResidues(){
		GrayScaleImage transformImg = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		if(maxtree != null && mintree != null){
			GrayScaleImage pos = getResiduesPos();
			GrayScaleImage neg = getResiduesNeg();
			
			for(int p=0; p < imgInput.getSize(); p++){
				transformImg.setPixel(p, Math.max(pos.getPixel(p), neg.getPixel(p)));
			}
			
		}else{

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
		}
		
		
		
		return transformImg;
	}
	
	public GrayScaleImage getAssociateIndexImagePos(){
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_32BITS, imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(maxtree.getRoot());
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				associateImg.setPixel(p, associatedIndexLUTPos[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeCT son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return associateImg;
	}
	public GrayScaleImage getAssociateIndexImageNeg(){
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_32BITS, imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(mintree.getRoot());
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				associateImg.setPixel(p, associatedIndexLUTNeg[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeCT son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return associateImg;
	}
	
	public GrayScaleImage getAssociateIndexImage(){
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_32BITS, imgInput.getWidth(), imgInput.getHeight());
		if(maxtree != null && mintree != null){
			GrayScaleImage pos = getResiduesPos();
			GrayScaleImage neg = getResiduesNeg();
			GrayScaleImage posIndex = getAssociateIndexImagePos();
			GrayScaleImage negIndex = getAssociateIndexImageNeg();
			for(int p=0; p < imgInput.getSize(); p++){
				if(pos.getPixel(p) > neg.getPixel(p)){
					associateImg.setPixel(p, posIndex.getPixel(p));
				}else{
					associateImg.setPixel(p, negIndex.getPixel(p));
				}
			}
		}else{
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
		}
		
		//patternSpectrum(associateImg);
		//WindowImages.show(Labeling.labeling(getResidues(), AdjacencyRelation.getCircular(1.5)).randomColor());
		//System.out.println("eh monotone-planing (assoc): " + Levelings.isMonotonePlaning(associateImg, imgEntrada, AdjacencyRelation.getCircular(1.5)));
		return associateImg;
	}
	

}
