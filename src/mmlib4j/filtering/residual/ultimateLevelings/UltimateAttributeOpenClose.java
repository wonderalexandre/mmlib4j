package mmlib4j.filtering.residual.ultimateLevelings;


import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.pruningStrategy.MappingStrategyOfPruning;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.Utils;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class UltimateAttributeOpenClose{

	private GrayScaleImage imgInput;
	private ProcessUAO processMaxtree;
	private ProcessUAO processMintree;
	
	public UltimateAttributeOpenClose(final GrayScaleImage img){
		
		class ProcessCT implements Runnable{
			ConnectedFilteringByComponentTree tree;
			boolean isMaxtree;
			
			ProcessCT(boolean maxtree){
				isMaxtree = maxtree;
			}
			
			public void run() {
				this.tree = new ConnectedFilteringByComponentTree(img, AdjacencyRelation.getAdjacency8(), isMaxtree);
			}	
		}
		
		final Thread[] threads = new Thread[2]; 
		ProcessCT processMaxtree = new ProcessCT(true);
		ProcessCT processMintree = new ProcessCT(false); 
        threads[0] = new Thread(processMaxtree);
        threads[1] = new Thread(processMintree);
        executeThreads(threads);
        
        this.processMaxtree = new ProcessUAO(processMaxtree.tree);
		this.processMintree = new ProcessUAO(processMintree.tree);
		this.imgInput = img;	
        
	}
	
	public UltimateAttributeOpenClose(ComponentTree tree1, ComponentTree tree2){
		this.processMaxtree = new ProcessUAO(tree1);
		this.processMintree = new ProcessUAO(tree2);
		this.imgInput = tree1.getInputImage();	
	}
	
	public ConnectedFilteringByComponentTree getMaxtree(){
		return (ConnectedFilteringByComponentTree) processMaxtree.tree;
	}
	
	public ConnectedFilteringByComponentTree getMintree(){
		return (ConnectedFilteringByComponentTree) processMintree.tree;
	}
	
	
	public void computeUAO(int paramValueMax, int typeParam, boolean ps1[], boolean ps2[]){
		long ti = System.currentTimeMillis();
		
		processMaxtree.setParameter(paramValueMax, typeParam, ps1, null);
		processMintree.setParameter(paramValueMax, typeParam, ps2, null);
		
		executeUAO();
		
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [Ultimate attribute opening/closing]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}
	
	
	public void computeUAO(int paramValueMax, int typeParam, boolean ps1[], boolean selectedShape1[], boolean ps2[], boolean selectedShape2[]){
		long ti = System.currentTimeMillis();
		
		processMaxtree.setParameter(paramValueMax, typeParam, ps1, selectedShape1);
		processMintree.setParameter(paramValueMax, typeParam, ps2, selectedShape2);
		
		executeUAO();
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [Ultimate attribute opening/closing]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}

	
	public void computeUAO(int paramValueMax, int typeParam){
		long ti = System.currentTimeMillis();
		
		processMaxtree.setParameter(paramValueMax, typeParam, null, null);
		processMintree.setParameter(paramValueMax, typeParam, null, null);
		
		executeUAO();
		
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [Ultimate attribute opening/closing]  "+ ((tf - ti) /1000.0)  + "s");
		}
			
	}


	private void executeUAO(){
		//paralelisa
        final Thread[] threads = new Thread[2]; 
        threads[0] = new Thread(processMaxtree);
        threads[1] = new Thread(processMintree);
        executeThreads(threads);
       
	}
	
	public void executeThreads(final Thread threads[]){
		for (final Thread thread : threads){
        	try {
        		thread.setPriority(Thread.currentThread().getPriority());
        		thread.start();
        		if (thread != null) 
        			thread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();	  
			}
		}
	}
	
	public GrayScaleImage getResidues(){
		GrayScaleImage transformImg = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		GrayScaleImage res1 = processMaxtree.uao.getResidues();
		GrayScaleImage res2 = processMintree.uao.getResidues();
		for(int p=0; p < transformImg.getSize(); p++){
			if(res1.getPixel(p) > res2.getPixel(p)){
				transformImg.setPixel(p, res1.getPixel(p));
			}else{
				transformImg.setPixel(p, res2.getPixel(p));
			}
		}
		//System.out.println("isConnected: " + Levelings.isPlanning(transformImg, imgInput, AdjacencyRelation.getAdjacency8()));
		return transformImg;
	}
	
	public GrayScaleImage getAssociateIndexImage(){
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_32BITS, imgInput.getWidth(), imgInput.getHeight());
		
		GrayScaleImage res1 = processMaxtree.uao.getResidues();
		GrayScaleImage res2 = processMintree.uao.getResidues();
		
		GrayScaleImage index1 = processMaxtree.uao.getAssociateIndexImage();
		GrayScaleImage index2 = processMintree.uao.getAssociateIndexImage();
		for(int p=0; p < associateImg.getSize(); p++){
			if(res1.getPixel(p) > res2.getPixel(p)){
				associateImg.setPixel(p, index1.getPixel(p));
			}else{
				associateImg.setPixel(p, index2.getPixel(p));
			}
		}
		
		return associateImg;
	}

	public boolean[] getNodesMapWithMaximumResiduesPositive(){
		return processMaxtree.uao.getNodesWithMaximumResidues();
	}
	

	public NodeCT[] getNodesMapPositive(){
		return processMaxtree.uao.getNodesMap();
	}
	
	public NodeCT[] getNodesMapNegative(){
		return processMintree.uao.getNodesMap();
	}
	
	
	public boolean[] getNodesMapWithMaximumResiduesNegative(){
		return processMintree.uao.getNodesWithMaximumResidues();
	}
	
	
	public GrayScaleImage getResiduesPos() {
		return processMaxtree.uao.getResidues();
	}
	

	public GrayScaleImage getResiduesNeg() {
		return processMintree.uao.getResidues();
	}

	public GrayScaleImage getAssociateIndexImagePos() {
		return processMaxtree.uao.getAssociateIndexImage();
	}
	
	public GrayScaleImage getAssociateIndexImageNeg() {
		return processMintree.uao.getAssociateIndexImage();
	}
	
	public GrayScaleImage getAttributeResidues(int attr){
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(32, imgInput.getWidth(), imgInput.getHeight());
		
		GrayScaleImage res1 = processMaxtree.uao.getResidues();
		GrayScaleImage res2 = processMintree.uao.getResidues();
		
		GrayScaleImage index1 = processMaxtree.uao.getAttributeResidues(attr);
		GrayScaleImage index2 = processMintree.uao.getAttributeResidues(attr);
		for(int p=0; p < associateImg.getSize(); p++){
			if(res1.getPixel(p) > res2.getPixel(p)){
				associateImg.setPixel(p, index1.getPixel(p));
			}else{
				associateImg.setPixel(p, index2.getPixel(p));
			}
		}
		
		return associateImg;
	}
	
	
	
	

	private class ProcessUAO implements Runnable{
		private UltimateAttributeOpening uao;
		private int paramValueMax;
		private int typeParam;
		private boolean selectedForPruning[];
		private boolean selectedShape[];
		ComponentTree tree;
		
		ProcessUAO(ComponentTree tree){
			this.tree = tree;
			uao = new UltimateAttributeOpening(tree);
		}
		
		void setParameter(int paramValueMax, int typeParam, boolean mapNodePruning[], boolean selectedShape[]){
			this.paramValueMax = paramValueMax;
			this.typeParam = typeParam;
			this.selectedForPruning = mapNodePruning;
			this.selectedShape = selectedShape;
		}
		
		public void run() {
			uao.computeUAO(paramValueMax, typeParam, selectedForPruning, selectedShape);
		}	
	}
}