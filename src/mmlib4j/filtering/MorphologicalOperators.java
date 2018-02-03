package mmlib4j.filtering;


import java.io.File;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageAlgebra;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.ImageUtils;
import mmlib4j.utils.Utils;

public class MorphologicalOperators {

	public static mmlib4j.images.GrayScaleImage closing(GrayScaleImage img, AdjacencyRelation adjEE){
		return MorphologicalOperators.erosion(MorphologicalOperators.dilation(img, adjEE), adjEE);
	}

	public static GrayScaleImage opening(GrayScaleImage img, AdjacencyRelation adjEE){
		return MorphologicalOperators.dilation(MorphologicalOperators.erosion(img, adjEE), adjEE);
	} 

	
	public static GrayScaleImage dilation(GrayScaleImage img, AdjacencyRelation adjEE){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		int max;
		for(int p = 0; p < img.getSize(); p++){
			max = 0;
			for(Integer q: adjEE.getAdjacencyPixels(img, p)){
				if(max < img.getPixel(q))
					max = img.getPixel(q);
			}
			imgOut.setPixel(p, max);
		}
		return imgOut;
	}
	
	
	public static GrayScaleImage erosion(GrayScaleImage img, AdjacencyRelation adjEE){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		int min;
		for(int p = 0; p < img.getSize(); p++){
			min = 255;
			for(Integer q: adjEE.getAdjacencyPixels(img, p)){
				if(min > img.getPixel(q)){
					min = img.getPixel(q);
				}
			}
			imgOut.setPixel(p, min);
		}
		return imgOut;
	}

	public static GrayScaleImage gradient(GrayScaleImage img, AdjacencyRelation adjB){
		long ti = System.currentTimeMillis();
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		GrayScaleImage dilatacao = dilation(img, adjB);
		GrayScaleImage erode = erosion(img, adjB);
		for(int p=0; p < img.getSize(); p++){
			imgOut.setPixel(p, dilatacao.getPixel(p) - erode.getPixel(p));
		}
		long tf = System.currentTimeMillis();
		if( Utils.debug ){
			System.out.println("Tempo de execucao [gradient]  "+ ((tf - ti) /1000.0)  + "s");
		}
		return imgOut;
		
	}
	public static GrayScaleImage gradientInternal(GrayScaleImage img, AdjacencyRelation adjB){
		long ti = System.currentTimeMillis();
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		GrayScaleImage erode = erosion(img, adjB);
		for(int p=0; p < img.getSize(); p++){
			imgOut.setPixel(p, img.getPixel(p) - erode.getPixel(p));
		}
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [gradient]  "+ ((tf - ti) /1000.0)  + "s");
		return imgOut;
		
	}
	
	  
    /**
     * Aplicando o filtro alternado sequencial - 
     * Definicao: ASFoc, B(n) = (((((S . B) o B) . 2B) o 2B) ... . nB) o nB
     * @param img - Image de entrada
     * @param se - Um conjunto de elemento estruturante
     * @return GrayScaleImage
     */
    public static GrayScaleImage asfCloseOpen(GrayScaleImage img, AdjacencyRelation se[]){
    	GrayScaleImage imgOut = img.duplicate();
        for(int i=0; i < se.length; i++){
            imgOut = MorphologicalOperators.opening(MorphologicalOperators.closing(imgOut, se[i]), se[i]);
        }
        return imgOut;
    }


    /**
     * Aplicando o filtro alternado sequencial - 
     * Definicao: ASFoc, B(n) = (((((S . B) o B) . 2B) o 2B) ... . nB) o nB
     * @param img - Image de entrada
     * @param se - Elemento estruturante
     * @param n - quantidade de iteracoes
     * @return GrayScaleImage[] - retorna uma vetor de n elementos, onde cada elemento representa um estagio da aplicacao do filtro
     */
    public static GrayScaleImage[] asfCloseOpen(GrayScaleImage img, int n){
        GrayScaleImage imgOut[] = new GrayScaleImage[n];
        for(int i=0; i < n; i++){
        	AdjacencyRelation se = AdjacencyRelation.getCircular(i);
            imgOut[i] = MorphologicalOperators.opening(MorphologicalOperators.closing(img, se), se);
        }
        return imgOut;
    }
    
    
    /**
     * Aplicando o filtro alternado sequencial - 
     * Definicao: ASFco, B(n) = (((((S o B) . B) o 2B) . 2B) ... o nB) . nB
     * @param img - Image de entrada
     * @param se - Um conjunto de elemento estruturante
     * @return GrayScaleImage
     */
    public static GrayScaleImage asfOpenClose(GrayScaleImage img, AdjacencyRelation se[]){
    	GrayScaleImage imgOut = img.duplicate();
        for(int i=0; i < se.length; i++){
            imgOut = MorphologicalOperators.closing(MorphologicalOperators.opening(imgOut, se[i]), se[i]);
        }
        return imgOut;
    }
    
    /**
     * Aplicando o filtro alternado sequencial - 
     * Definicao: ASFco, B(n) = (((((S o B) . B) o 2B) . 2B) ... o nB) . nB
     * @param img - Image de entrada
     * @param se - Elemento estruturante
     * @param n - quantidade de iteracoes
     * @return GrayScaleImage[] - retorna uma vetor de n elementos, onde cada elemento representa um estagio da aplicacao do filtro
     */
    public static GrayScaleImage[] asfOpenClose(GrayScaleImage img,  int n){
        GrayScaleImage imgOut[] = new GrayScaleImage[n];
        for(int i=0; i < n; i++){ 
        	AdjacencyRelation se = AdjacencyRelation.getCircular(i);
            imgOut[i] = MorphologicalOperators.closing(MorphologicalOperators.opening(img, se), se);
        }
        return imgOut;
    }
    
    /**
     * Aplica a operacao de abertura top-hat
     * Definicao de abertura top-hat: A - (A o Bi), sendo Bi uma familia de elemento estruturante
     * @param img
     * @param se
     * @return
     */
    public static GrayScaleImage[] transformOpeningTopHat(GrayScaleImage img, AdjacencyRelation ses[]){
        GrayScaleImage imgOuts[] = new GrayScaleImage[ses.length];
        for(int i=0; i < ses.length; i++){
            imgOuts[i] = ImageAlgebra.subtraction(img, MorphologicalOperators.opening(img, ses[i]));
        }
        return imgOuts;
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
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
        for(int i=0; i < img.getWidth(); i++)
            for(int j=0; j < img.getHeight(); j++)
                imgOut.setPixel(i, j,wth.getPixel(i, j) + bth.getPixel(i, j));
        
        return imgOut;
    }
    
    
    public static GrayScaleImage realceTopHat(GrayScaleImage img, AdjacencyRelation se){
        int pixel;
        GrayScaleImage wth = MorphologicalOperators.openingTopHat(img, se);
        GrayScaleImage bth = MorphologicalOperators.closingTopHat(img, se);
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
        for(int i=0; i < img.getWidth(); i++)
            for(int j=0; j < img.getHeight(); j++){
                pixel = img.getPixel(i, j) + wth.getPixel(i, j) - bth.getPixel(i, j);
                imgOut.setPixel(i, j, pixel);
            }
        
        return ImageUtils.normalizedPixels(imgOut);
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
     * Aplica a operacao de abertura top-hat
     * Definicao de abertura top-hat: (A . B) - A, sendo Bi uma familia de elemento estruturante
     * @param img
     * @param se
     * @return
     */
    public GrayScaleImage[] transformClosingTopHat(GrayScaleImage img, AdjacencyRelation ses[]){
        GrayScaleImage imgOuts[] = new GrayScaleImage[ses.length];
        for(int i=0; i < ses.length; i++){
            imgOuts[i] = ImageAlgebra.subtraction(MorphologicalOperators.closing(img, ses[i]), img);
        }
        return imgOuts;
    }
    

    
    /**
     * Implementacao direta de ultimate opening
     * @param img - imagem de entrada
     * @param step - passo de incremento do elemento estruturante
     * @param maxCriterion - tamanho maximo de aberturas
     */
    public static GrayScaleImage[] ultimateOpening(GrayScaleImage img, int maxCriterion){
        GrayScaleImage imgR = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
        GrayScaleImage imgQ = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
        
        GrayScaleImage imgPreviousOpen = img.duplicate();
        GrayScaleImage imgCurrentOpen = null;
        GrayScaleImage imgCurrentRes = null;
        
        double step = 2.5;
        int size = 1;
        double raio = 1.5;
        while(size < maxCriterion){
        	
            imgCurrentOpen = opening(img, AdjacencyRelation.getCircular(raio));
            
            imgCurrentRes = ImageAlgebra.subtraction(imgPreviousOpen, imgCurrentOpen);
            
            //imgCurrentRes = imgPreviousOpen.subtraction(imgCurrentOpen);
            
            for(int i=0; i < img.getSize(); i++){
                if(imgCurrentRes.getPixel(i) > 0 && imgCurrentRes.getPixel(i) >= imgR.getPixel(i)){
                    imgR.setPixel(i,imgCurrentRes.getPixel(i));
                    imgQ.setPixel(i, size);
                }
            }
            ImageBuilder.saveImage(imgR, new File("/Users/wonderalexandre/Desktop/uo/imgR_"+size+".png"));
            ImageBuilder.saveImage(imgQ.randomColor(), new File("/Users/wonderalexandre/Desktop/uo/imgQ_"+size+".png"));
            size++;
            raio *= 2.5;
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
        GrayScaleImage imgR = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
        GrayScaleImage imgQ = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
        GrayScaleImage imgPreviousOpen = img.duplicate();
        GrayScaleImage imgCurrentOpen = null;
        GrayScaleImage imgCurrentRes = null;
        
        int size = 1;
        int step = 1;
        while(size < maxCriterion){
        	
            imgCurrentOpen = closing(img, AdjacencyRelation.getCircular(size));            
            
            imgCurrentRes = ImageAlgebra.subtraction(imgCurrentOpen, imgPreviousOpen);
            
            //imgCurrentRes = imgCurrentOpen.subtraction(imgPreviousOpen);
            
            for(int i=0; i < img.getSize(); i++){
                if(imgCurrentRes.getPixel(i) >= imgR.getPixel(i) && imgCurrentRes.getPixel(i) > 0){
                    imgR.setPixel(i,imgCurrentRes.getPixel(i));
                    imgQ.setPixel(i, size);
                }
            }    
            ImageBuilder.saveImage(imgR, new File("/Users/wonderalexandre/Desktop/uo/imgR_"+size+".png"));
            ImageBuilder.saveImage(imgQ.randomColor(), new File("/Users/wonderalexandre/Desktop/uo/imgQ_"+size+".png"));
            size += step;
            imgPreviousOpen = imgCurrentOpen;
        }
        
        return new GrayScaleImage[]{imgR, imgQ};
    }
  
    
    public static void main(String args[]){
    	GrayScaleImage img = ImageBuilder.openGrayImage(ImageBuilder.windowOpenFile());
    	ultimateOpening(img, 200);
    	
    	
    }
    

}
