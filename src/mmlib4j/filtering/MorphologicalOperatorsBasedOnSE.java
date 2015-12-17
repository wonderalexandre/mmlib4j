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
public class MorphologicalOperatorsBasedOnSE {

	static mmlib4j.filtering.RankFilters filter = new mmlib4j.filtering.RankFilters();
	
	
	public static void closing(GrayScaleImage img, AdjacencyRelation adjEE, GrayScaleImage imgOut){
		long ti = System.currentTimeMillis();
		filter.rankProcess(img, adjEE, RankFilters.MAX, imgOut);
		filter.rankProcess(imgOut.duplicate(), adjEE, RankFilters.MIN, imgOut);
		long tf = System.currentTimeMillis();
		if(Utils.debug)
			System.out.println("Tempo de execucao [closing]  "+ ((tf - ti) /1000.0)  + "s");
		
	}
	
	public static GrayScaleImage closing(GrayScaleImage img, AdjacencyRelation adjEE){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		closing(img, adjEE, imgOut);
		return imgOut;
	}

	public static void opening(GrayScaleImage img, AdjacencyRelation adjEE, GrayScaleImage imgOut){
		long ti = System.currentTimeMillis();
		filter.rankProcess(img, adjEE, RankFilters.MIN, imgOut);
		filter.rankProcess(imgOut.duplicate(), adjEE, RankFilters.MAX, imgOut);
		long tf = System.currentTimeMillis();
		if(Utils.debug)
			System.out.println("Tempo de execucao [opening]  "+ ((tf - ti) /1000.0)  + "s");
		
	}
	public static GrayScaleImage opening(GrayScaleImage img, AdjacencyRelation adjEE){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		opening(img, adjEE, imgOut);
		return imgOut;
	} 

	
	public static void dilation(GrayScaleImage img, AdjacencyRelation adjEE, GrayScaleImage imgOut){
		long ti = System.currentTimeMillis();
		filter.rankProcess(img, adjEE, RankFilters.MAX, imgOut);
		long tf = System.currentTimeMillis();
		if(Utils.debug)
		System.out.println("Tempo de execucao [dilation]  "+ ((tf - ti) /1000.0)  + "s");
	}
	
	public static GrayScaleImage dilation(GrayScaleImage img, AdjacencyRelation adjEE){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		dilation(img, adjEE, imgOut);
		return imgOut;
		
	}
	
	public static void erosion(GrayScaleImage img, AdjacencyRelation adjEE, GrayScaleImage imgOut){
		long ti = System.currentTimeMillis();
		filter.rankProcess(img, adjEE, RankFilters.MIN, imgOut);
		long tf = System.currentTimeMillis();
		if(Utils.debug)
		System.out.println("Tempo de execucao [erosion]  "+ ((tf - ti) /1000.0)  + "s");
	}
	
	public static GrayScaleImage erosion(GrayScaleImage img, AdjacencyRelation adjEE){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		erosion(img, adjEE, imgOut);
		return imgOut;
	}
	
	public static void gradient(GrayScaleImage img, AdjacencyRelation adjB, GrayScaleImage imgOut){
		long ti = System.currentTimeMillis();
		GrayScaleImage dilatacao = dilation(img, adjB);
		GrayScaleImage erode = erosion(img, adjB);
		for(int p=0; p < img.getSize(); p++){
			imgOut.setPixel(p, dilatacao.getPixel(p) - erode.getPixel(p));
		}
		long tf = System.currentTimeMillis();
		if(Utils.debug)
			System.out.println("Tempo de execucao [gradient]  "+ ((tf - ti) /1000.0)  + "s");
		
		
	}

	public static GrayScaleImage gradient(GrayScaleImage img, AdjacencyRelation adjB){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		gradient(img, adjB, imgOut);
		return imgOut;
		
	}
	

	public static void gradientInternal(GrayScaleImage img, AdjacencyRelation adjB, GrayScaleImage imgOut){
		long ti = System.currentTimeMillis();
		GrayScaleImage erode = erosion(img, adjB);
		for(int p=0; p < img.getSize(); p++){
			imgOut.setPixel(p, img.getPixel(p) - erode.getPixel(p));
		}
		long tf = System.currentTimeMillis();
		if(Utils.debug)
			System.out.println("Tempo de execucao [gradient internal]  "+ ((tf - ti) /1000.0)  + "s");
	}
	
	public static GrayScaleImage gradientInternal(GrayScaleImage img, AdjacencyRelation adjB){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		gradientInternal(img, adjB, imgOut);
		return imgOut;
	}
	
	public static void gradientExternal(GrayScaleImage img, AdjacencyRelation adjB, GrayScaleImage imgOut){
		long ti = System.currentTimeMillis();
		GrayScaleImage erode = erosion(img, adjB);
		for(int p=0; p < img.getSize(); p++){
			imgOut.setPixel(p, img.getPixel(p) - erode.getPixel(p));
		}
		long tf = System.currentTimeMillis();
		if(Utils.debug)
		System.out.println("Tempo de execucao [gradient external]  "+ ((tf - ti) /1000.0)  + "s");
	}

	
	public static GrayScaleImage gradientExternal(GrayScaleImage img, AdjacencyRelation adjB){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		gradientExternal(img, adjB, imgOut);
		return imgOut;
	}

	  
    /**
     * Aplicando o filtro alternado sequencial - 
     * Definicao: ASFoc, B(n) = (((((S . B) o B) . 2B) o 2B) ... . nB) o nB
     * @param img - Image de entrada
     * @param se - Um conjunto de elemento estruturante
     * @return IGrayScaleImage
     */
    public static void asfCloseOpen(GrayScaleImage img, SimpleLinkedList<AdjacencyRelation> ses, GrayScaleImage imgOut){
    	long ti = System.currentTimeMillis();
    	GrayScaleImage imgTmp = img.duplicate();
    	GrayScaleImage imgTmp2 = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
        for(AdjacencyRelation se: ses){
        
        	//closing
        	closing(imgTmp, se, imgTmp2);
        	opening(imgTmp2, se, imgOut);
        	
        	imgTmp = imgOut;
    		
        }
        long tf = System.currentTimeMillis();
        if(Utils.debug)
		System.out.println("Tempo de execucao [asfCloseOpen]  "+ ((tf - ti) /1000.0)  + "s");
    }
    
    /**
     * Aplicando o filtro alternado sequencial - 
     * Definicao: ASFoc, B(n) = (((((S . B) o B) . 2B) o 2B) ... . nB) o nB
     * @param img - Image de entrada
     * @param se - Um conjunto de elemento estruturante
     * @return IGrayScaleImage
     */
    public static GrayScaleImage asfCloseOpen(GrayScaleImage img, SimpleLinkedList<AdjacencyRelation> ses){
    	GrayScaleImage imgOut = img.duplicate();
    	asfCloseOpen(img, ses, imgOut);
		return imgOut;
		
    }
    
    /**
     * Aplicando o filtro alternado sequencial - 
     * Definicao: ASFco, B(n) = (((((S o B) . B) o 2B) . 2B) ... o nB) . nB
     * @param img - Image de entrada
     * @param se - Um conjunto de elemento estruturante
     * @return IGrayScaleImage
     */
    public static void asfOpenClose(GrayScaleImage img, SimpleLinkedList<AdjacencyRelation> ses, GrayScaleImage imgOut){
    	long ti = System.currentTimeMillis();
    	GrayScaleImage imgTmp = img.duplicate();
    	GrayScaleImage imgTmp2 = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
        for(AdjacencyRelation se: ses){
        
        	//closing
        	opening(imgTmp, se, imgTmp2);
        	closing(imgTmp2, se, imgOut);
        	
        	imgTmp = imgOut;
    		
        }
        long tf = System.currentTimeMillis();
        if(Utils.debug)
		System.out.println("Tempo de execucao [asfCloseOpen]  "+ ((tf - ti) /1000.0)  + "s");
    }
    
    public static GrayScaleImage asfOpenClose(GrayScaleImage img, SimpleLinkedList<AdjacencyRelation> ses){
    	GrayScaleImage imgOut = img.duplicate();
    	asfOpenClose(img, ses, imgOut);
		return imgOut;
    }
    
 
    
    /**
     * Aplica a operacao de abertura top-hat
     * Definicao de abertura top-hat: A - (A o B), sendo B o elemento estruturante
     * @param img
     * @param se
     * @return
     */
    public static void openingTopHat(GrayScaleImage img, AdjacencyRelation se, GrayScaleImage imgOut){
    	long ti = System.currentTimeMillis();
    	opening(img, se, imgOut);
    	ImageAlgebra.subtraction(img, imgOut, imgOut);
    	if(Utils.debug){
    		long tf = System.currentTimeMillis();
    		System.out.println("Tempo de execucao [openingTopHat]  "+ ((tf - ti) /1000.0)  + "s");
    	}
    }
    
    public static GrayScaleImage openingTopHat(GrayScaleImage img, AdjacencyRelation se){
    	GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
    	openingTopHat(img, se, imgOut);
    	return imgOut;
    }
    
    public static void selfTopHat(GrayScaleImage img, AdjacencyRelation se, GrayScaleImage imgOut){
        openingTopHat(img, se, imgOut);
        GrayScaleImage bth = closingTopHat(img, se);
        GrayScaleImage wth = imgOut;
        for(int p=0; p < img.getSize(); p++)
        	imgOut.setPixel(p, wth.getPixel(p) + bth.getPixel(p));
        
        
    }

    public static GrayScaleImage selfTopHat(GrayScaleImage img, AdjacencyRelation se){
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
        selfTopHat(img, se, imgOut);
        return imgOut;
    }
    
    public static void realceTopHat(GrayScaleImage img, AdjacencyRelation se, GrayScaleImage imgOut){
    	int pixel;
        MorphologicalOperatorsBasedOnSE.openingTopHat(img, se, imgOut);
        GrayScaleImage bth = MorphologicalOperatorsBasedOnSE.closingTopHat(img, se);
        GrayScaleImage wth = imgOut;
        for(int p=0; p < img.getSize(); p++){
        	pixel = img.getPixel(p) + wth.getPixel(p) - bth.getPixel(p);
        	if(pixel > 255)
        		imgOut.setPixel(p, 255);
        	else if(pixel < 0)
        		imgOut.setPixel(p, 0);
        	else
        		imgOut.setPixel(p, pixel);
        }
    }
    
    public static GrayScaleImage realceTopHat(GrayScaleImage img, AdjacencyRelation se){
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
        realceTopHat(img, se, imgOut);
        return imgOut;
    }
    
    
    
    /**
     * Aplica a operacao de close top-hat (ou button-hat)
     * Definicao de fechamento top-hat: A - (A . B), sendo B o elemento estruturante
     * @param img
     * @param se
     * @return
     */
    public static void closingTopHat(GrayScaleImage img, AdjacencyRelation se, GrayScaleImage imgOut){
    	long ti = System.currentTimeMillis();
    	closing(img, se, imgOut);
    	ImageAlgebra.subtraction(imgOut, img, imgOut);
    	if(Utils.debug){
    		long tf = System.currentTimeMillis();
    		System.out.println("Tempo de execucao [openingTopHat]  "+ ((tf - ti) /1000.0)  + "s");
    	}
    }
    
    public static GrayScaleImage closingTopHat(GrayScaleImage img, AdjacencyRelation se){
    	GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
    	closingTopHat(img, se, imgOut);
        return imgOut;
    }    
    
    /**
     * Implementacao direta de ultimate opening
     * @param img - imagem de entrada
     * @param step - passo de incremento do elemento estruturante
     * @param maxCriterion - tamanho maximo de aberturas
     */
    public static void ultimateOpening(GrayScaleImage img, int maxCriterion, GrayScaleImage imgOutR, GrayScaleImage imgOutIndex){
        
        GrayScaleImage imgPreviousOpen = img.duplicate();
        GrayScaleImage imgCurrentOpen = null;
        GrayScaleImage imgCurrentRes = null;
        
        int step = 1;
        int size = 1;
        while(size < maxCriterion){
        	imgCurrentOpen = opening(img, AdjacencyRelation.getCircular(size));
            imgCurrentRes = ImageAlgebra.subtraction(imgPreviousOpen, imgCurrentOpen);
            for(int i=0; i < img.getSize(); i++){
                if(imgCurrentRes.getPixel(i) > 0 && imgCurrentRes.getPixel(i) >= imgOutR.getPixel(i)){
                	imgOutR.setPixel(i,imgCurrentRes.getPixel(i));
                	imgOutIndex.setPixel(i, size);
                }
            }
            size += step;
            imgPreviousOpen = imgCurrentOpen;
        }
        
    }
    
    public static GrayScaleImage[] ultimateOpening(GrayScaleImage img, int maxCriterion){
        GrayScaleImage imgOutR = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_8BITS, img.getWidth(), img.getHeight());
        GrayScaleImage imgOutIndex = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_32BITS, img.getWidth(), img.getHeight());
        ultimateOpening(img, maxCriterion, imgOutR, imgOutIndex);
        return new GrayScaleImage[]{imgOutR, imgOutIndex};
    }
    
    /**
     * Implementacao direta de ultimate opening
     * @param img - imagem de entrada
     * @param step - passo de incremento do elemento estruturante
     * @param maxCriterion - tamanho maximo de aberturas
     */
    public static void ultimateClosing(GrayScaleImage img, int maxCriterion, GrayScaleImage imgR, GrayScaleImage imgQ){
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
    }
  
    public static GrayScaleImage[] ultimateClosing(GrayScaleImage img, int maxCriterion){
        GrayScaleImage imgR = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
        GrayScaleImage imgQ =ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
        ultimateClosing(img, maxCriterion, imgR, imgQ);
        return new GrayScaleImage[]{imgR, imgQ};
    }
    
    public static void main(String args[]){
    	GrayScaleImage img = ImageBuilder.openGrayImage(ImageBuilder.windowOpenFile());
    	ultimateOpening(img, 100);
    	
    	
    }
    

}

