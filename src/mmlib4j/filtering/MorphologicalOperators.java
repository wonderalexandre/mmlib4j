package mmlib4j.filtering;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageAlgebra;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.Utils;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class MorphologicalOperators {

	static mmlib4j.filtering.RankFilters filter = new mmlib4j.filtering.RankFilters();
	
	public static GrayScaleImage closing(GrayScaleImage img, AdjacencyRelation adjEE){
		long ti = System.currentTimeMillis();
		GrayScaleImage imgOut = img.duplicate();
		filter.rankProcess(imgOut, adjEE, RankFilters.MAX);
		filter.rankProcess(imgOut, adjEE, RankFilters.MIN);
		long tf = System.currentTimeMillis();
		if(Utils.debug)
		System.out.println("Tempo de execucao [closing]  "+ ((tf - ti) /1000.0)  + "s");
		return imgOut;
	}

	public static GrayScaleImage opening(GrayScaleImage img, AdjacencyRelation adjEE){
		long ti = System.currentTimeMillis();
		GrayScaleImage imgOut = img.duplicate();
		filter.rankProcess(imgOut, adjEE, RankFilters.MIN);
		filter.rankProcess(imgOut, adjEE, RankFilters.MAX);
		long tf = System.currentTimeMillis();
		if(Utils.debug)
		System.out.println("Tempo de execucao [opening]  "+ ((tf - ti) /1000.0)  + "s");
		return imgOut;
	} 

	
	public static GrayScaleImage dilation(GrayScaleImage img, AdjacencyRelation adjEE){
		long ti = System.currentTimeMillis();
		GrayScaleImage imgOut = filter.rank(img, adjEE, RankFilters.MAX);
		long tf = System.currentTimeMillis();
		if(Utils.debug)
		System.out.println("Tempo de execucao [dilation]  "+ ((tf - ti) /1000.0)  + "s");
		return imgOut;
	}
	
	
	public static GrayScaleImage erosion(GrayScaleImage img, AdjacencyRelation adjEE){
		long ti = System.currentTimeMillis();
		GrayScaleImage imgOut = filter.rank(img, adjEE, RankFilters.MIN);
		long tf = System.currentTimeMillis();
		if(Utils.debug)
		System.out.println("Tempo de execucao [erosion]  "+ ((tf - ti) /1000.0)  + "s");
		return imgOut;
	}

	public static GrayScaleImage gradient(GrayScaleImage img, AdjacencyRelation adjB){
		long ti = System.currentTimeMillis();
		GrayScaleImage imgOut =ImageFactory.createGrayScaleImage(img);
		GrayScaleImage dilatacao = dilation(img, adjB);
		GrayScaleImage erode = erosion(img, adjB);
		for(int p=0; p < img.getSize(); p++){
			imgOut.setPixel(p, dilatacao.getPixel(p) - erode.getPixel(p));
		}
		long tf = System.currentTimeMillis();
		if(Utils.debug)
		System.out.println("Tempo de execucao [gradient]  "+ ((tf - ti) /1000.0)  + "s");
		return imgOut;
		
	}
	public static GrayScaleImage gradientInternal(GrayScaleImage img, AdjacencyRelation adjB){
		long ti = System.currentTimeMillis();
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img);
		GrayScaleImage erode = erosion(img, adjB);
		for(int p=0; p < img.getSize(); p++){
			imgOut.setPixel(p, img.getPixel(p) - erode.getPixel(p));
		}
		long tf = System.currentTimeMillis();
		if(Utils.debug)
		System.out.println("Tempo de execucao [gradient internal]  "+ ((tf - ti) /1000.0)  + "s");
		return imgOut;
	}
	
	public static GrayScaleImage gradientExternal(GrayScaleImage img, AdjacencyRelation adjB){
		long ti = System.currentTimeMillis();
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img);
		GrayScaleImage erode = erosion(img, adjB);
		for(int p=0; p < img.getSize(); p++){
			imgOut.setPixel(p, img.getPixel(p) - erode.getPixel(p));
		}
		long tf = System.currentTimeMillis();
		if(Utils.debug)
		System.out.println("Tempo de execucao [gradient external]  "+ ((tf - ti) /1000.0)  + "s");
		return imgOut;
	}
	
/*	public static GrayScaleImage toggleMapping (GrayScaleImage img, AdjacencyRelation adj){
		GrayScaleImage imgD = dilation(img, adj); 
		GrayScaleImage imgE = erosion(img, adj);
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img);
		int contrast1 = 15;
		int percentage = 80;
		int UNKNOWN_VALUE = 128;
		int HIGH_VALUE = 255;
		int LOW_VALUE = 0;
		for(int i=0; i < img.getSize(); i++){
			if ( (imgD.getPixel(i) - imgE.getPixel(i)) < contrast1 ) {
              	imgOut.setPixel(i, UNKNOWN_VALUE);
            }
            else {
                if ( (imgD.getPixel(i) - img.getPixel(i)) < percentage * (img.getPixel(i) - imgE.getPixel(i))/100.0 ) {
                	imgOut.setPixel(i, HIGH_VALUE);
                }    
                else {
                	imgOut.setPixel(i, LOW_VALUE);
                }
            }
		}
		return imgOut;
	}
*/	
	  
    /**
     * Aplicando o filtro alternado sequencial - 
     * Definicao: ASFoc, B(n) = (((((S . B) o B) . 2B) o 2B) ... . nB) o nB
     * @param img - Image de entrada
     * @param se - Um conjunto de elemento estruturante
     * @return IGrayScaleImage
     */
    public static GrayScaleImage asfCloseOpen(GrayScaleImage img, SimpleLinkedList<AdjacencyRelation> ses){
    	long ti = System.currentTimeMillis();
		GrayScaleImage imgOut = img.duplicate();
        for(AdjacencyRelation se: ses){
        
        	//closing
        	filter.rankProcess(imgOut, se, RankFilters.MAX);
    		filter.rankProcess(imgOut, se, RankFilters.MIN);
    		
    		//opening
    		filter.rankProcess(imgOut, se, RankFilters.MIN);
    		filter.rankProcess(imgOut, se, RankFilters.MAX);
    		
        }
        long tf = System.currentTimeMillis();
        if(Utils.debug)
		System.out.println("Tempo de execucao [asfCloseOpen]  "+ ((tf - ti) /1000.0)  + "s");
		return imgOut;
		
    }
    
    
    /**
     * Aplicando o filtro alternado sequencial - 
     * Definicao: ASFco, B(n) = (((((S o B) . B) o 2B) . 2B) ... o nB) . nB
     * @param img - Image de entrada
     * @param se - Um conjunto de elemento estruturante
     * @return IGrayScaleImage
     */
    public static GrayScaleImage asfOpenClose(GrayScaleImage img, SimpleLinkedList<AdjacencyRelation> ses){
    	long ti = System.currentTimeMillis();
		GrayScaleImage imgOut = img.duplicate();
		for(AdjacencyRelation se: ses){
        	
        	//opening
    		filter.rankProcess(imgOut, se, RankFilters.MIN);
    		filter.rankProcess(imgOut, se, RankFilters.MAX);
    		
        	//closing
        	filter.rankProcess(imgOut, se, RankFilters.MAX);
    		filter.rankProcess(imgOut, se, RankFilters.MIN);
        }
        long tf = System.currentTimeMillis();
        if(Utils.debug)
		System.out.println("Tempo de execucao [asfCloseOpen]  "+ ((tf - ti) /1000.0)  + "s");
		return imgOut;
    }
    
 
    
    /**
     * Aplica a operacao de abertura top-hat
     * Definicao de abertura top-hat: A - (A o B), sendo B o elemento estruturante
     * @param img
     * @param se
     * @return
     */
    public static GrayScaleImage openingTopHat(GrayScaleImage img, AdjacencyRelation se){
        return ImageAlgebra.subtraction(img, MorphologicalOperators.opening(img, se));
    }
    
    public static GrayScaleImage selfTopHat(GrayScaleImage img, AdjacencyRelation se){
        GrayScaleImage wth = MorphologicalOperators.openingTopHat(img, se);
        GrayScaleImage bth = MorphologicalOperators.closingTopHat(img, se);
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img);
        for(int i=0; i < img.getWidth(); i++)
            for(int j=0; j < img.getHeight(); j++)
                imgOut.setPixel(i, j,wth.getPixel(i, j) + bth.getPixel(i, j));
        
        return imgOut;
    }
    
    
    public static GrayScaleImage realceTopHat(GrayScaleImage img, AdjacencyRelation se){
        int pixel;
        GrayScaleImage wth = MorphologicalOperators.openingTopHat(img, se);
        GrayScaleImage bth = MorphologicalOperators.closingTopHat(img, se);
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img);
        for(int i=0; i < img.getWidth(); i++)
            for(int j=0; j < img.getHeight(); j++){
                pixel = img.getPixel(i, j) + wth.getPixel(i, j) - bth.getPixel(i, j);
                if(pixel > 255)
                	imgOut.setPixel(i, j, 255);
                else if(pixel < 0)
                	imgOut.setPixel(i, j, 0);
                else
                	imgOut.setPixel(i, j, pixel);
            }
        
        return imgOut;//ImageUtils.normalizedPixels(imgOut);
    }
    
    
    
    /**
     * Aplica a operacao de close top-hat (ou button-hat)
     * Definicao de fechamento top-hat: A - (A . B), sendo B o elemento estruturante
     * @param img
     * @param se
     * @return
     */
    public static GrayScaleImage closingTopHat(GrayScaleImage img, AdjacencyRelation se){
        return ImageAlgebra.subtraction(MorphologicalOperators.closing(img, se), img);
    }
    
    
    /**
     * Implementacao direta de ultimate opening
     * @param img - imagem de entrada
     * @param step - passo de incremento do elemento estruturante
     * @param maxCriterion - tamanho maximo de aberturas
     */
    public static GrayScaleImage[] ultimateOpening(GrayScaleImage img, int maxCriterion){
        GrayScaleImage imgR = ImageFactory.createGrayScaleImage(img);
        GrayScaleImage imgQ = ImageFactory.createGrayScaleImage(img);
        
        GrayScaleImage imgPreviousOpen = img.duplicate();
        GrayScaleImage imgCurrentOpen = null;
        GrayScaleImage imgCurrentRes = null;
        
        int step = 1;
        int size = 1;
        while(size < maxCriterion){
        	imgCurrentOpen = opening(img, AdjacencyRelation.getCircular(size));
            imgCurrentRes = ImageAlgebra.subtraction(imgPreviousOpen, imgCurrentOpen);
            for(int i=0; i < img.getSize(); i++){
                if(imgCurrentRes.getPixel(i) > 0 && imgCurrentRes.getPixel(i) >= imgR.getPixel(i)){
                    imgR.setPixel(i,imgCurrentRes.getPixel(i));
                    imgQ.setPixel(i, size);
                }
            }
            size += step;
            imgPreviousOpen = imgCurrentOpen;
        }
        return new GrayScaleImage[]{imgR, imgQ};
    }
    
    /**
     * Implementacao direta de ultimate opening
     * @param img - imagem de entrada
     * @param step - passo de incremento do elemento estruturante
     * @param maxCriterion - tamanho maximo de aberturas
     */
    public static GrayScaleImage[] ultimateClosing(GrayScaleImage img, int maxCriterion){
        GrayScaleImage imgR = ImageFactory.createGrayScaleImage(img);
        GrayScaleImage imgQ =ImageFactory.createGrayScaleImage(img);
        GrayScaleImage imgPreviousOpen = img.duplicate();
        GrayScaleImage imgCurrentOpen = null;
        GrayScaleImage imgCurrentRes = null;
        
        int size = 1;
        int step = 1;
        while(size < maxCriterion){
            imgCurrentOpen = closing(img, AdjacencyRelation.getCircular(size));
            imgCurrentRes = ImageAlgebra.subtraction(imgCurrentOpen, imgPreviousOpen);
            for(int i=0; i < img.getSize(); i++){
                if(imgCurrentRes.getPixel(i) >= imgR.getPixel(i) && imgCurrentRes.getPixel(i) > 0){
                    imgR.setPixel(i,imgCurrentRes.getPixel(i));
                    imgQ.setPixel(i, size);
                }
            }    
            size += step;
            imgPreviousOpen = imgCurrentOpen;
        }
        
        return new GrayScaleImage[]{imgR, imgQ};
    }
  
    
    public static void main(String args[]){
    	GrayScaleImage img = ImageBuilder.openGrayImage(ImageBuilder.windowOpenFile());
    	ultimateOpening(img, 100);
    	
    	
    }
    

}

