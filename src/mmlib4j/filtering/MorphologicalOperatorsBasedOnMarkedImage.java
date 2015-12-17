package mmlib4j.filtering;

import java.io.File;

import mmlib4j.datastruct.PriorityQueueDial;
import mmlib4j.datastruct.Queue;
import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.Image2D;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class MorphologicalOperatorsBasedOnMarkedImage {

	private static AdjacencyRelation adj = AdjacencyRelation.getCircular(1.5);
	
	/**
	 * Dilatacao por reconstrucao. 
	 * Algoritmo descrito no paper de Luc Vicent (1993), Morphological grayscale reconstruction in image analysis: application and efficient algorithms
	 * @param imgG
	 * @param imgMarked
	 * @return
	 * 
	 * Restricao
	 * imgG < imgMarked and Dominio(imgG) = Dominio(imgMarked) 
	 */
	public static GrayScaleImage dilationByReconstructionLucVicent(GrayScaleImage imgG, GrayScaleImage imgMarked){
		long ti = System.currentTimeMillis();
		GrayScaleImage imgF = imgMarked.duplicate();
		int max = 0;
		
		AdjacencyRelation adjR = adj;
		AdjacencyRelation adjL = adj;
		
		//varredura no sentido raster
		for(int p=0; p < imgG.getSize(); p++){
			max = imgF.getPixel(p);
			for(Integer q: adjR.getAdjacencyPixels(imgF, p)){
				if (max < imgF.getPixel(q)){
					max = imgF.getPixel(q);
				}
			}
			imgF.setPixel(p, Math.min(imgG.getPixel(p), max));
		}
	
		
		Queue<Integer> fifo = new Queue<Integer>();
		
		//varredura no sentido anti-raster
		for(int p=imgG.getSize()-1; p >= 0; p--){
			max = imgF.getPixel(p);
			for(Integer q: adjL.getAdjacencyPixels(imgF, p)){
				if (max < imgF.getPixel(q)){
					max = imgF.getPixel(q);
				}
			}
			imgF.setPixel(p, Math.min(imgG.getPixel(p), max));
			for(Integer q: adjL.getAdjacencyPixels(imgF, p)){
				if(imgF.getPixel(q) < imgF.getPixel(p) && imgF.getPixel(q) < imgG.getPixel(q)){
					fifo.enqueue(p);
					//break;
				}
			}
		}
		
		
		//propagacao
		while(!fifo.isEmpty()){
			Integer p = fifo.dequeue();
			for(Integer q: adj.getAdjacencyPixels(imgF, p)){
				if(imgF.getPixel(q) < imgF.getPixel(p) && imgG.getPixel(q) != imgF.getPixel(q)){
					imgF.setPixel(q, Math.min(imgF.getPixel(p), imgG.getPixel(q)));
					fifo.enqueue(q);
				}
			}
		}
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [reconstruction morphological - L.Vincent(1993)]  "+ ((tf - ti) /1000.0)  + "s");
		return imgF;
	}
	
	
	/**
	 * self reconstrucao. 
	 * Algoritmo de Luc Vicent (1993) descrito no livro do Soille (pag. 195)
	 * @param imgInput
	 * @param imgMarked
	 * @return
	 * 
	 * Restricao
	 * imgG < imgMarked and Dominio(imgG) = Dominio(imgMarked) 
	 */
	public static GrayScaleImage selfReconstructionLucVicent(GrayScaleImage imgInput, GrayScaleImage imgMarked){
		long ti = System.currentTimeMillis();
		GrayScaleImage imgM = imgMarked.duplicate();
		int max;
		int min;

		//varredura no sentido raster
		for(int p=0; p < imgInput.getSize(); p++){
			if(imgM.getPixel(p) < imgInput.getPixel(p)){
				max = imgM.getPixel(p);
				for(Integer q: adj.getAdjacencyPixels(imgM, p)){
					if (max < imgM.getPixel(q)){
						max = imgM.getPixel(q);
					}
				}
				imgM.setPixel(p, Math.min(imgInput.getPixel(p), max));
			}else{
				min = imgM.getPixel(p);
				for(Integer q: adj.getAdjacencyPixels(imgM, p)){
					if (min > imgM.getPixel(q)){
						min = imgM.getPixel(q);
					}
				}
				imgM.setPixel(p, Math.max(imgInput.getPixel(p), min));
			}
		}
		
		Queue<Integer> fifo = new Queue<Integer>();
		
		//varredura no sentido anti-raster
		for(int p=imgInput.getSize()-1; p >= 0; p--){
			if(imgM.getPixel(p) < imgInput.getPixel(p)){
				max = imgM.getPixel(p);
				for(Integer q: adj.getAdjacencyPixels(imgM, p)){
					if (max < imgM.getPixel(q)){
						max = imgM.getPixel(q);
					}
				}
				imgM.setPixel(p, Math.min(imgInput.getPixel(p), max));
			}else{
				min = imgM.getPixel(p);
				for(Integer q: adj.getAdjacencyPixels(imgM, p)){
					if (min > imgM.getPixel(q)){
						min = imgM.getPixel(q);
					}
				}
				imgM.setPixel(p, Math.max(imgInput.getPixel(p), min));
			}
			for(Integer q: adj.getAdjacencyPixels(imgM, p)){
				if(imgM.getPixel(q) < imgM.getPixel(p) && imgM.getPixel(q) < imgInput.getPixel(q)){
					fifo.enqueue(p);
					break;
				}else if(imgM.getPixel(q) > imgM.getPixel(p) && imgM.getPixel(q) > imgInput.getPixel(q)){
					fifo.enqueue(p);
					break;
				}
			}
		}
		
		
		//propagacao
		while(!fifo.isEmpty()){
			Integer p = fifo.dequeue();
			for(Integer q: adj.getAdjacencyPixels(imgM, p)){
				if(imgM.getPixel(q) < imgInput.getPixel(p)){
					if(imgM.getPixel(q) < imgM.getPixel(p) && imgInput.getPixel(q) != imgM.getPixel(q)){
						imgM.setPixel(q, Math.min(imgM.getPixel(p), imgInput.getPixel(q)));
						fifo.enqueue(q);
					}
				}else{
					if(imgM.getPixel(q) > imgM.getPixel(p) && imgInput.getPixel(q) != imgM.getPixel(q)){
						imgM.setPixel(q, Math.max(imgM.getPixel(p), imgInput.getPixel(q)));
						fifo.enqueue(q);
					}
				}
			}
			
		}
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [self reconstruction morphological - L.Vincent(1993)]  "+ ((tf - ti) /1000.0)  + "s");
		//WindowImages.show(imgF);
		return imgM;
	}
    
    
    /**
     * Preenchimento de buraco baseado em sup-reconstrucao
     * @param imgInput - imagem de entrada
     * @return IBinaryImage - imagem de saida
     */
    public static GrayScaleImage closingOfHoles(GrayScaleImage imgInput, int back){
        //criando o marcador
        GrayScaleImage imgMarker = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
        imgMarker.initImage(back);     
        int fore = (back == 255? 0: 255);
        //marcando as bordas com 1-pixel de espessura
        for(int i=0; i < imgMarker.getWidth(); i++){
            imgMarker.setPixel(i, 0, fore);
            imgMarker.setPixel(i, imgMarker.getHeight()-1, fore);
        }
        for(int j=0; j < imgMarker.getHeight(); j++){
            imgMarker.setPixel(0, j, fore);
            imgMarker.setPixel(imgMarker.getWidth()-1, j, fore);
        }
        return dilationByReconstructionLucVicent(imgInput, imgMarker);
    }
    
    
    public static GrayScaleImage skiz(GrayScaleImage img){
    	double raio = 1;
    	AdjacencyRelation adj;
    	AdjacencyRelation adj8 = AdjacencyRelation.getCircular(1.5);
    	GrayScaleImage uniao = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
    	GrayScaleImage imgErosion = img.duplicate();
    	for(int i=0; i < 110; i++){
    		adj = AdjacencyRelation.getCircular(raio);
    		GrayScaleImage imgRecOpen = MorphologicalOperatorsBasedOnSE.opening(imgErosion, adj8);
    		for(int p=0; p < img.getSize(); p++){
    			uniao.setPixel(p, Math.max(uniao.getPixel(p), imgErosion.getPixel(p) - imgRecOpen.getPixel(p)));
    		}
    		if(i % 2 == 0){
    			ImageBuilder.saveImage(uniao, new File("/users/wonderalexandre/Desktop/uniao_"+i+".png"));
    			ImageBuilder.saveImage(imgErosion, new File("/users/wonderalexandre/Desktop/erosao_"+i+".png"));
    			ImageBuilder.saveImage(imgRecOpen, new File("/users/wonderalexandre/Desktop/abertura_"+i+".png"));
    		}
    		//WindowImages.show(new IImage[]{uniao, imgErosion, imgRecOpen}, new String[]{"uniao_" + raio, "erosao_"+ raio, "abertura_"+raio});
    		imgErosion = MorphologicalOperatorsBasedOnSE.erosion(img, adj);
    		raio += 0.5;
    	}
    	return uniao;
    }
    
    public static GrayScaleImage ultimateErosion(GrayScaleImage img){
    	double raio = 1;
    	AdjacencyRelation adj;
    	AdjacencyRelation adj8 = AdjacencyRelation.getCircular(4);
    	GrayScaleImage uniao = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
    	GrayScaleImage imgErosion = img.duplicate();
    	for(int i=0; i < 100; i++){
    		adj = AdjacencyRelation.getCircular(raio);
    		
    		GrayScaleImage imgRecOpen = null;//dilationByReconstruction(img, MorphologicalOperators.opening(imgErosion, adj8));
    		for(int p=0; p < img.getSize(); p++){
    			uniao.setPixel(p, Math.max(uniao.getPixel(p), imgErosion.getPixel(p) - imgRecOpen.getPixel(p)));
    		}
    		if(i % 2 == 0){
    			ImageBuilder.saveImage(uniao, new File("/users/wonderalexandre/Desktop/UE/uniao_"+i+".png"));
    			ImageBuilder.saveImage(imgErosion, new File("/users/wonderalexandre/Desktop/UE/erosao_"+i+".png"));
    			ImageBuilder.saveImage(imgRecOpen, new File("/users/wonderalexandre/Desktop/UE/aberturaRec_"+i+".png"));
    		}
    		//WindowImages.show(new IImage[]{uniao, imgErosion, imgRecOpen}, new String[]{"uniao_" + raio, "erosao_"+ raio, "abertura_"+raio});
    		raio += 0.5;
    		imgErosion = MorphologicalOperatorsBasedOnSE.erosion(img, adj);
    		
    	}
    	return uniao;
    }
    

	public static GrayScaleImage erosionByReconstructionIFT(GrayScaleImage img, GrayScaleImage marked){
		long ti = System.currentTimeMillis();	
		 GrayScaleImage imgMapaCusto = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
		 boolean NO_PROCESSED = false;//GRAY
		 boolean PROCESSED = true; //BLACK 
		 boolean states[] = new boolean[img.getSize()];
		 int infinitoValue = img.maxValue() + 1;
		 PriorityQueueDial fifo = new PriorityQueueDial(imgMapaCusto, infinitoValue, PriorityQueueDial.FIFO);
		 
		 for(int p = 0; p < img.getSize(); p++){
			 imgMapaCusto.setPixel(p, marked.getPixel(p) );
			 fifo.add(p);
		 }
	
		 while(!fifo.isEmpty()){
			 int p = fifo.remove();
			 states[p] = PROCESSED;
			 
			 for(Integer q: adj.getAdjacencyPixels(img, p)){
				 if(states[q] == NO_PROCESSED){
					 int tmp = Math.max(imgMapaCusto.getPixel(p), img.getPixel(q));
					 if(tmp < imgMapaCusto.getPixel(q)){
						 if(states[q] == NO_PROCESSED){
							 fifo.remove(q);
						 }
						 imgMapaCusto.setPixel(q, tmp);
						 fifo.add(q);	    
					 }
				 }
			 } 
		 }
			
		 long tf = System.currentTimeMillis();
		 System.out.println("Tempo de execucao [erosionByReconstructionIFT]  "+ ((tf - ti) /1000.0)  + "s");
		 states = null;
		 return imgMapaCusto;
	}
	
	public static GrayScaleImage dilationByReconstructionIFT(GrayScaleImage img, GrayScaleImage marked){
		long ti = System.currentTimeMillis();	
		 GrayScaleImage imgMapaCusto = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
		 boolean NO_PROCESSED = false;//GRAY
		 boolean PROCESSED = true; //BLACK 
		 boolean states[] = new boolean[img.getSize()];
		 int infinitoValue = img.maxValue() + 1;
		 PriorityQueueDial fifo = new PriorityQueueDial(imgMapaCusto, infinitoValue, PriorityQueueDial.FIFO, true);
		 
		 for(int p = 0; p < img.getSize(); p++){
			 imgMapaCusto.setPixel(p, marked.getPixel(p) );
			 fifo.add(p);
		 }
	
		 while(!fifo.isEmpty()){
			 int p = fifo.remove();
			 states[p] = PROCESSED;
			 
			 for(Integer q: adj.getAdjacencyPixels(img, p)){
				 if(states[q] == NO_PROCESSED){
					 int tmp = Math.min(imgMapaCusto.getPixel(p), img.getPixel(q));
					 if(tmp > imgMapaCusto.getPixel(q)){
						 if(states[q] == NO_PROCESSED){
							 fifo.remove(q);
						 }
						 imgMapaCusto.setPixel(q, tmp);
						 fifo.add(q);	    
					 }
				 }
			 } 
		 }
			
		 long tf = System.currentTimeMillis();
		 System.out.println("Tempo de execucao [erosionByReconstructionIFT]  "+ ((tf - ti) /1000.0)  + "s");
		 states = null;
		 return imgMapaCusto;
	}
    
    public static void main(String args[]){
    	GrayScaleImage img = ImageBuilder.openGrayImage(ImageBuilder.windowOpenFile());
    	GrayScaleImage imgM = MorphologicalOperatorsBasedOnSE.opening(img, AdjacencyRelation.getCircular(5));
    	
    	
    	
    	GrayScaleImage imgOut = MorphologicalOperatorsBasedOnMarkedImage.dilationByReconstructionIFT(img, imgM);
    	GrayScaleImage imgOut2 = dilationByReconstructionLucVicent(img, imgM);
    	
    	System.out.println(imgOut2.equals(imgOut));
    	WindowImages.show(new Image2D[]{img, imgM, imgOut, imgOut2});
    	
    	/*IGrayScaleImage imgL = Labeling.labeling(imgM, AdjacencyRelation.getCircular(1.5));
    	for(int p=0; p < img.getSize();p++){
    		if(imgM.getPixel(p) == 0) imgL.setPixel(p, -1); 
    	}
    	AdjacencyRelation adj = AdjacencyRelation.getCircular(1.5);
    	IGrayScaleImage ws = OperatorsByIFT.watershedByMarker(adj, MorphlogicalOperators.gradient(img, adj), imgL);
    	//WindowImages.show(new IImage[]{img, imgM});
    	//WindowImages.show(new IImage[]{ws.randomColor()});
    	WindowWaltershed win = new WindowWaltershed(img);
    	win.setVisible(true);
    	int m[][] = new int[img.getWidth()][img.getHeight()];
    	for(int x=0; x < img.getWidth(); x++)
    		for(int y=0; y < img.getHeight(); y++)
    			m[x][y] = imgL.getPixel(x, y);
    	
    	
    	win.setMarker(m);
    	//ultimateErosion(img);
    	  */ 	
    }
    
    
    
	
}
