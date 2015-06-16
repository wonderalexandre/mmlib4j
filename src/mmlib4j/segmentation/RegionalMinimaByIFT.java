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
public class RegionalMinimaByIFT {

	private final static AdjacencyRelation adj = AdjacencyRelation.getCircular(1.5);
	
	public static GrayScaleImage extractionOfRegionalMinima(GrayScaleImage img){
		long ti = System.currentTimeMillis();	
		 GrayScaleImage imgMapaPredecessores = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
		 GrayScaleImage imgMapaCusto = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
		 int NIL = -1;
		 boolean NO_PROCESSED = false;//GRAY
		 boolean PROCESSED = true; //BLACK 
		 boolean states[] = new boolean[img.getSize()];
		 int infinitoValue = img.maxValue() + 1;
		 PriorityQueueDial fifo = new PriorityQueueDial(imgMapaCusto, infinitoValue, PriorityQueueDial.LIFO);
		 
		 for(int p = 0; p < img.getSize(); p++){
			 imgMapaPredecessores.setPixel(p, NIL);
			 imgMapaCusto.setPixel(p, img.getPixel(p));
			 fifo.add(p);
			 
		 }
		 	 
		 while(!fifo.isEmpty()){
			 int p = fifo.remove();
			 states[p] = PROCESSED;
			 
			 if(imgMapaPredecessores.getPixel(p) == NIL){
				 imgMapaCusto.setPixel(p, img.getPixel(p));
			 }
			 
			 for(Integer q: adj.getAdjacencyPixels(img, p)){
				 if(states[q] == NO_PROCESSED){
					 int tmp = img.getPixel(p) <= img.getPixel(q)?  imgMapaCusto.getPixel(p) : infinitoValue;
					 if(tmp <= imgMapaCusto.getPixel(q)){
						 if(states[q] == NO_PROCESSED){
							 fifo.remove(q);
						 }
						 imgMapaPredecessores.setPixel(q, p);
						 imgMapaCusto.setPixel(q, tmp);
						 fifo.add(q);	    
					 }
				 }
			 } 
		 }
			
		 //pos-processamento
		 GrayScaleImage imgMinimo = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
		 int label = 0;
		 for(int i=0; i < imgMinimo.getSize(); i++){
			 if(imgMapaPredecessores.getPixel(i) == NIL){
				 label += 1;
				 imgMinimo.setPixel(i, label);	 
			 }
		 }
		 
		 long tf = System.currentTimeMillis();
		 System.out.println("Tempo de execucao [regional minima]  "+ ((tf - ti) /1000.0)  + "s");
		 imgMapaCusto = null;
		 imgMapaPredecessores = null;
		 states = null;
		 
		 return imgMinimo;
	}

}
