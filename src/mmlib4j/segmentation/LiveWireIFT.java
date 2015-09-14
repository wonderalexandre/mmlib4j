package mmlib4j.segmentation;

import mmlib4j.datastruct.PriorityQueueDial;
import mmlib4j.datastruct.Queue;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.AdjacencyRelation;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class LiveWireIFT {

	
	
	public static GrayScaleImage removeBackground(GrayScaleImage img, GrayScaleImage imgMarcador){
		Queue<Integer> fifo = new Queue<Integer>();
		GrayScaleImage imgSemFundo = img.duplicate();
		AdjacencyRelation adj = AdjacencyRelation.getCircular(1.5f);
		boolean state[] = new boolean[img.getSize()];

		state[0] = true;
		fifo.enqueue(0);

		while(!fifo.isEmpty()){
			int p = fifo.dequeue();
			for(Integer q: adj.getAdjacencyPixels(img, p)){
				if (!state[q]){
					if (imgMarcador.getPixel(q) == -1){
						fifo.enqueue(q);
						imgSemFundo.setPixel(q, 255);
					}
					state[q] = true;
				}
			}
		}
		return imgSemFundo;
	}
	

	public static GrayScaleImage liveWire(AdjacencyRelation adj, GrayScaleImage img, int pixelBegin){
		long ti = System.currentTimeMillis();	
		 GrayScaleImage imgMapaPredecessores = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
		 GrayScaleImage imgMapaCusto = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
		 int NIL = -1;
		 boolean NO_PROCESSED = false;//GRAY
		 boolean PROCESSED = true; //BLACK 
		 boolean states[] = new boolean[img.getSize()];
		 int infinitoValue = 256 + 1;
		 PriorityQueueDial fifo = new PriorityQueueDial(img, infinitoValue, PriorityQueueDial.FIFO);
		  
		 for(int p = 0; p < img.getSize(); p++){
			 imgMapaPredecessores.setPixel(p, NIL);
			 if(pixelBegin == p){
				 imgMapaCusto.setPixel(p, img.getPixel(p) + 1);
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
						 imgMapaPredecessores.setPixel(q, p);
						 imgMapaCusto.setPixel(q, tmp);
						 fifo.add(q);	    
					 }
				 }
			 }
		 }
			
		 long tf = System.currentTimeMillis();
		 System.out.println("Tempo de execucao [liveWire]  "+ ((tf - ti) /1000.0)  + "s");
		 imgMapaCusto = null;
		 states = null;
		 
		 return imgMapaPredecessores;
	}
	
}

