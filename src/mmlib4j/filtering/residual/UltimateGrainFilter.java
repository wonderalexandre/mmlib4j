package mmlib4j.filtering.residual;


import mmlib4j.datastruct.Queue;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.IMorphologicalTreeFiltering;
import mmlib4j.representation.tree.pruningStrategy.MappingStrategyOfPruning;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedAttribute;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;

/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class UltimateGrainFilter {

	private TreeOfShape tree;

	private boolean selectedForPruning[] = null;
	private boolean selectedForFiltering[] = null;
	private GrayScaleImage imgInput;
	private NodeToS root;

	private int[] residuesNodePos;
	private int[] associatedNodePos;
	private int[] residuesNodeNeg;
	private int[] associatedNodeNeg;
	
	private int[] residuesNodeType;
	private int[] associatedNodeType;
	
	private int typeParam;
	private int maxCriterion;
	
	
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
		this.computeUGF(paramValueMax, typeParam, new PruningBasedAttribute((IMorphologicalTreeFiltering) tree, typeParam));	
	}
	
	public void computeUGF(int paramValueMax, int typeParam, MappingStrategyOfPruning msp){
		this.computeUGF(paramValueMax, typeParam, msp, null);
	}
	
	
	public void computeUGF(int paramValueMax, int typeParam, MappingStrategyOfPruning msp, boolean selectedShape[]){
		long ti = System.currentTimeMillis();
		this.maxCriterion = paramValueMax;
		this.typeParam = typeParam;
		this.selectedForPruning = msp.getMappingSelectedNodes();
		this.selectedForFiltering = selectedShape;
		
		this.residuesNodePos = new int[tree.getNumNode()];
		this.associatedNodePos = new int[tree.getNumNode()];
		this.residuesNodeNeg = new int[tree.getNumNode()];
		this.associatedNodeNeg = new int[tree.getNumNode()];
		
		if (root.getChildren() != null) {
			for(NodeToS no: root.getChildren()){
				computeUGF(no, root.getLevel(), false, false, false);
			}
		}

		long tf = System.currentTimeMillis();
		//System.out.println("Tempo de execucao [Ultimate leveling]  "+ ((tf - ti) /1000.0)  + "s");		
	}
	
	
	
	private void computeUGF(NodeToS currentNode, int previous, boolean qPropag, boolean flagInit, boolean isCalculateResidue){
		int contrastPos = 0;
		int maxContrastPos;
		int linkedAttributesPos;
		int contrastNeg = 0;
		int maxContrastNeg;
		int linkedAttributesNeg;
		int pv = previous;
		boolean flagResido = false;
		boolean flagPropag = false;
		NodeToS parentNode = currentNode.getParent();
		
		if(currentNode.getAttributeValueOLD(typeParam) <= maxCriterion && selectedForPruning[parentNode.getId()]){
			flagInit = true;
		}
		
		
		if(flagInit){		
			if( selectedForPruning[parentNode.getId()] ){
				pv = parentNode.getLevel();
				isCalculateResidue = (selectedForFiltering == null? true : hasNodeSelectedInPrimitive(currentNode) );
			}
			else{
				flagResido = true;
				pv = previous;
			}
			
			if( isCalculateResidue){  //non filter?
				contrastPos =  Math.max(0, currentNode.getLevel() - pv);
				contrastNeg =  Math.max(0, pv - currentNode.getLevel());
			}
			
			//residuo positivo
			if (residuesNodePos[parentNode.getId()] >= contrastPos) {
				maxContrastPos = residuesNodePos[parentNode.getId()];
				linkedAttributesPos = associatedNodePos[parentNode.getId()];
			}
			else{
				maxContrastPos = contrastPos;
				flagPropag = true;
				if(flagResido && qPropag){
					linkedAttributesPos = associatedNodePos[parentNode.getId()];
				}
				else{
					linkedAttributesPos = currentNode.getAttributeValueOLD(typeParam) + 1;
				}
				
			}
			
			//residuo negativo
			if (residuesNodeNeg[parentNode.getId()] >= contrastNeg) {
				maxContrastNeg = residuesNodeNeg[parentNode.getId()];
				linkedAttributesNeg = associatedNodeNeg[parentNode.getId()];
			}
			else{
				maxContrastNeg = contrastNeg;
				flagPropag = true;
				if(flagResido && qPropag){
					linkedAttributesNeg = associatedNodeNeg[parentNode.getId()];
				}
				else{
					linkedAttributesNeg = currentNode.getAttributeValueOLD(typeParam) + 1;
				}
				
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
		}
		
		
		for(NodeToS no: currentNode.getChildren()){
			computeUGF(no, pv, flagPropag, flagInit, isCalculateResidue);
		}
	}
	
	
	
	
	private boolean[] getSelected(){
		boolean selected[] = new boolean[tree.getNumNode()];
		for(NodeToS node: tree.getListNodes()){
			if(node.getParent() != null){
				if ( node.getParent().getAttributeValueOLD(typeParam) != node.getAttributeValueOLD(typeParam)) {
					selected[node.getParent().getId()] = true;
				}
			}
		}
		return selected;
	}
	

	public boolean hasNodeSelectedInPrimitive_OLD(NodeToS currentNode){
		if(selectedForFiltering == null) return true;
		
		boolean result = selectedForPruning[currentNode.getId()];
		
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(currentNode);
		while(!fifo.isEmpty()){
			NodeToS node = fifo.dequeue();
			
			if(selectedForPruning[node.getId()] == result){
				
				if(selectedForFiltering[node.getId()]){ //extrair residuo do node selecionado
					return true;
				}
				
				for(NodeToS n: node.getChildren()){
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
	
	public boolean hasNodeSelectedInPrimitive(NodeToS currentNode){
		if(selectedForFiltering == null) return true;
		
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(currentNode);
		while(!fifo.isEmpty()){
			NodeToS node = fifo.dequeue();
			
			if(selectedForFiltering[node.getId()]){ 
				return true;
			}
				
			for(NodeToS n: node.getChildren()){
				if(selectedForPruning[n.getId()] == false)
					fifo.enqueue(n);
			}
			
		}
		return false;
	}
	
	/*
	public boolean isNewPrimitive(NodeToS currentNode){
		if(currentNode.getParent() == null) return true;
		return  Math.abs( currentNode.getAttributeValue(typeParam) - currentNode.getParent().getAttributeValue(typeParam) )  > gradualTransitions;
				//;//&& currentNode.isNodeMaxtree() == currentNode.getParent().isNodeMaxtree();
	}*/
	

	public GrayScaleImage getResiduesPos(){
		GrayScaleImage transformImg = ImageFactory.createGrayScaleImage(this.imgInput);;
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				transformImg.setPixel(p, residuesNodePos[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeToS son: no.getChildren()){
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
		GrayScaleImage transformImg = ImageFactory.createGrayScaleImage(this.imgInput);;
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				transformImg.setPixel(p, residuesNodeNeg[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeToS son: no.getChildren()){
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
		GrayScaleImage transformImg = ImageFactory.createGrayScaleImage(this.imgInput);;
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				transformImg.setPixel(p, Math.max(residuesNodePos[no.getId()], residuesNodeNeg[no.getId()]));
			}
			if(no.getChildren() != null){
				for(NodeToS son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return transformImg;
	}
	
	
	public GrayScaleImage getAssociateImagePos(){
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(32, imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				associateImg.setPixel(p, associatedNodePos[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeToS son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return associateImg;
	}
	
	public GrayScaleImage getAssociateImage(){
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(32, imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				if(residuesNodePos[no.getId()] > residuesNodeNeg[no.getId()])
					associateImg.setPixel(p, associatedNodePos[no.getId()]);
				else
					associateImg.setPixel(p, associatedNodeNeg[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeToS son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return associateImg;
	}
	
	public GrayScaleImage getAssociateImageNeg(){
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(32, imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				associateImg.setPixel(p, associatedNodeNeg[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeToS son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return associateImg;
	}
	
	public GrayScaleImage getAssociateImageByType( ){
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(32, imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				associateImg.setPixel(p, associatedNodeType[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeToS son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return associateImg;
	}
	
	public GrayScaleImage getResiduesByType( ){
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(this.imgInput);;
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				associateImg.setPixel(p, residuesNodeType[no.getId()]);
			}
			if(no.getChildren() != null){
				for(NodeToS son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
		return associateImg;
	}
	
	
	
	
	
	
	
	

	public double funcTextLocation(NodeToS node, int parentLevel){
		int width = node.getWidthNode();
		int heigth = node.getHeightNode();
		int areaBB = width * heigth;
		double ratioAreaBB = node.getArea() / (double) areaBB;
		double ratioWH = Math.max(width, heigth) / Math.min(width, heigth);
		int numHoles= node.getNumHoles();
		
		int psiArea = 0;
		if(1000 <= node.getArea() && node.getArea() < (this.imgInput.getWidth() * this.imgInput.getHeight())/3){
			psiArea = 1;
		}
		
		int psiHeight = 0;
		if(20 <= node.getHeightNode() && node.getHeightNode() < 300){
			psiHeight = 1;
		}
		
		int psiWidth = 0;
		if(10 <= node.getWidthNode() && node.getWidthNode() < 200){
			psiWidth = 1;
		}
		
		int psiHole = 0;
		if(numHoles <= 3){
			psiHole = 1;
		}
		
		int psiRect = 0;
		if(0.35 <= ratioAreaBB && ratioAreaBB <= 0.9)
			psiRect = 1;
		
		int psiRate = 0;
		if(ratioWH <= 4){
			psiRate = 1;
		}
		
		int psiColor = 0;
		//if(psiArea == 1 && psiHole == 1 && psiRect == 1 && psiRate == 1)
		//	System.out.println(node.getHomogeneity() + "   ==>> " + Math.abs(node.getLevel() - parentLevel));
		if(node.getHomogeneity() <= 15)
			psiColor = 1;
		
		
		
	//	System.out.printf("%d %d %d %d %d\n", psiArea, psiHole, psiRect, psiRate, psiColor);
		return psiArea * psiHole * psiRect * psiRate * psiColor * psiHeight * psiWidth;
	}
	
}