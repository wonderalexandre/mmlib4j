package mmlib4j.filtering.residual;


import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.pruningStrategy.MappingStrategyOfPruning;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class UltimateAttributeOpenClose{

	private GrayScaleImage imgInput;
	private ProcessUAO process1;
	private ProcessUAO process2;
	
	private class ProcessUAO implements Runnable{
		private UltimateAttributeOpening uao;
		private int paramValueMax;
		private int typeParam;
		private MappingStrategyOfPruning ps;
		private boolean selectedShape[];
		
		ProcessUAO(ComponentTree tree){
			uao = new UltimateAttributeOpening(tree);
		}
		
		void setParameter(int paramValueMax, int typeParam, MappingStrategyOfPruning ps, boolean selectedShape[]){
			this.paramValueMax = paramValueMax;
			this.typeParam = typeParam;
			this.ps = ps;
			this.selectedShape = selectedShape;
		}
		
		public void run() {
			uao.computeUAO(paramValueMax, typeParam, ps, selectedShape);
		}	
	}
	
	public UltimateAttributeOpenClose(ComponentTree tree1, ComponentTree tree2){
		this.process1 = new ProcessUAO(tree1);
		this.process2 = new ProcessUAO(tree2);
		this.imgInput = tree1.getInputImage();	
	}
	
	public void computeUAO(int paramValueMax, int typeParam, MappingStrategyOfPruning ps1, MappingStrategyOfPruning ps2){
		long ti = System.currentTimeMillis();
		
		process1.setParameter(paramValueMax, typeParam, ps1, null);
		process2.setParameter(paramValueMax, typeParam, ps2, null);
		
		executeUAO();
		
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [Ultimate attribute opening/closing]  "+ ((tf - ti) /1000.0)  + "s");
	}
	
	
	public void computeUAO(int paramValueMax, int typeParam, MappingStrategyOfPruning ps1, boolean selectedShape1[], MappingStrategyOfPruning ps2, boolean selectedShape2[]){
		long ti = System.currentTimeMillis();
		
		process1.setParameter(paramValueMax, typeParam, ps1, selectedShape1);
		process2.setParameter(paramValueMax, typeParam, ps2, selectedShape2);
		
		executeUAO();
		
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [Ultimate attribute opening/closing]  "+ ((tf - ti) /1000.0)  + "s");		
	}

	
	public void computeUAO(int paramValueMax, int typeParam){
		long ti = System.currentTimeMillis();
		
		process1.setParameter(paramValueMax, typeParam, null, null);
		process2.setParameter(paramValueMax, typeParam, null, null);
		
		executeUAO();
		
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [Ultimate attribute opening/closing]  "+ ((tf - ti) /1000.0)  + "s");
			
	}


	private void executeUAO(){
		//paralelisa
        final Thread[] threads = new Thread[2]; 
        threads[0] = new Thread(process1);
        threads[1] = new Thread(process2);
       
        
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
		GrayScaleImage transformImg = ImageFactory.createGrayScaleImage(this.imgInput);
		GrayScaleImage res1 = process1.uao.getResidues();
		GrayScaleImage res2 = process2.uao.getResidues();
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
		GrayScaleImage associateImg = ImageFactory.createGrayScaleImage(32, imgInput.getWidth(), imgInput.getHeight());
		
		GrayScaleImage res1 = process1.uao.getResidues();
		GrayScaleImage res2 = process2.uao.getResidues();
		
		GrayScaleImage index1 = process1.uao.getAssociateIndexImage();
		GrayScaleImage index2 = process2.uao.getAssociateIndexImage();
		for(int p=0; p < associateImg.getSize(); p++){
			if(res1.getPixel(p) > res2.getPixel(p)){
				associateImg.setPixel(p, index1.getPixel(p));
			}else{
				associateImg.setPixel(p, index2.getPixel(p));
			}
		}
		
		return associateImg;
	}

	public GrayScaleImage getResiduesPos() {
		return process2.uao.getResidues();
	}
	

	public GrayScaleImage getResiduesNeg() {
		return process1.uao.getResidues();
	}

	public GrayScaleImage getAssociateIndexImagePos() {
		return process2.uao.getAssociateIndexImage();
	}
	
	public GrayScaleImage getAssociateIndexImageNeg() {
		return process1.uao.getAssociateIndexImage();
	}
}