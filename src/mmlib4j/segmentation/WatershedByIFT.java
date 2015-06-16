package mmlib4j.segmentation;

import mmlib4j.datastruct.PriorityQueueDial;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.AdjacencyRelation;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class WatershedByIFT {

	private final static AdjacencyRelation adj = AdjacencyRelation.getCircular(1.5);
	
	public static GrayScaleImage watershedByMarker(GrayScaleImage img, GrayScaleImage marked){
		long ti = System.currentTimeMillis();	
		 GrayScaleImage imgMapaPredecessores = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
		 GrayScaleImage imgMapaCusto = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
		 GrayScaleImage imgLabel = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
		 int NIL = -1;
		 boolean NO_PROCESSED = false;//GRAY
		 boolean PROCESSED = true; //BLACK 
		 boolean states[] = new boolean[img.getSize()];
		 int infinitoValue = img.maxValue() + 1;
		 PriorityQueueDial fifo = new PriorityQueueDial(imgMapaCusto, infinitoValue, PriorityQueueDial.FIFO);
		 
		 for(int p = 0; p < img.getSize(); p++){
			 imgMapaPredecessores.setPixel(p, NIL);
			 if(marked.getPixel(p) != -1){
				 imgMapaCusto.setPixel(p, img.getPixel(p) + 1);
				 imgLabel.setPixel(p, marked.getPixel(p));
				 fifo.add(p);
			 }else{
				 imgMapaCusto.setPixel(p, infinitoValue);
				}
		 }
	
		 
		 while(!fifo.isEmpty()){
			 int p = fifo.remove();
			 states[p] = PROCESSED;
			 
			 if(imgMapaPredecessores.getPixel(p) == NIL){
				 imgMapaCusto.setPixel(p, img.getPixel(p));
			 }
			 
			 for(Integer q: adj.getAdjacencyPixels(img, p)){
				 if(states[q] == NO_PROCESSED){
					 int weightEdge = (img.getPixel(p) + img.getPixel(q))/2;
					 int tmp = Math.max(imgMapaCusto.getPixel(p), weightEdge);
					 if(tmp < imgMapaCusto.getPixel(q)){
						 if(states[q] == NO_PROCESSED){
							 fifo.remove(q);
						 }
						 imgLabel.setPixel(q, imgLabel.getPixel(p));
						 imgMapaPredecessores.setPixel(q, p);
						 imgMapaCusto.setPixel(q, tmp);
						 fifo.add(q);	    
					 }
				 }
			 } 
		 }
			
		 long tf = System.currentTimeMillis();
		 System.out.println("Tempo de execucao [watershedPorMarcador]  "+ ((tf - ti) /1000.0)  + "s");
		 imgMapaCusto = null;
		 imgMapaPredecessores = null;
		 states = null;
		 
		 return imgLabel;
	}

	
	
	public static GrayScaleImage watershedByHBasin(GrayScaleImage img, int k){
		long ti = System.currentTimeMillis();	
		 GrayScaleImage imgMapaPredecessores = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
		 GrayScaleImage imgMapaCusto = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
		 GrayScaleImage imgLabel = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
		 int NIL = -1;
		 boolean NO_PROCESSED = false;//GRAY
		 boolean PROCESSED = true; //BLACK 
		 boolean states[] = new boolean[img.getSize()];
		 int infinitoValue = img.maxValue() + 1;
		 PriorityQueueDial fifo = new PriorityQueueDial(imgMapaCusto, infinitoValue, PriorityQueueDial.FIFO);
		 
		 GrayScaleImage marked = img.duplicate();
		 marked.add(k);
		 
		 for(int p = 0; p < img.getSize(); p++){
			 imgMapaPredecessores.setPixel(p, NIL);
			 imgMapaCusto.setPixel(p, marked.getPixel(p) + 1);
			 fifo.add(p);
		 }
	
		 int label = 1;
		 while(!fifo.isEmpty()){
			 int p = fifo.remove();
			 states[p] = PROCESSED;
			 
			 if(imgMapaPredecessores.getPixel(p) == NIL){
				 imgMapaCusto.setPixel(p, img.getPixel(p));
				 imgLabel.setPixel(p, label++);
			 }
			 
			 for(Integer q: adj.getAdjacencyPixels(img, p)){
				 if(states[q] == NO_PROCESSED){
					 int weightEdge = (img.getPixel(p) + img.getPixel(q))/2;
					 int tmp = Math.max(imgMapaCusto.getPixel(p), weightEdge);
					 if(tmp < imgMapaCusto.getPixel(q)){
						 if(states[q] == NO_PROCESSED){
							 fifo.remove(q);
						 }
						 imgLabel.setPixel(q, imgLabel.getPixel(p));
						 imgMapaPredecessores.setPixel(q, p);
						 imgMapaCusto.setPixel(q, tmp);
						 fifo.add(q);	    
					 }
				 }
			 } 
		 }
			
		 long tf = System.currentTimeMillis();
		 System.out.println("Tempo de execucao [watershedPorMarcador]  "+ ((tf - ti) /1000.0)  + "s");
		 imgMapaCusto = null;
		 imgMapaPredecessores = null;
		 states = null;
		 
		 return imgLabel;
	}
}
