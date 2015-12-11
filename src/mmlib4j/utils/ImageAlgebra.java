package mmlib4j.utils;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ImageAlgebra {

	/**
     * Minimo entre duas imagem
     * @param imgA
     * @param imgB
     * @return
     */
    public static void minimum(GrayScaleImage imgA, GrayScaleImage imgB, GrayScaleImage imgM){
        for(int w=0; w < imgA.getWidth(); w++){
            for(int h=0; h < imgA.getHeight(); h++){
                if(imgA.getPixel(w,h) < imgB.getPixel(w,h))
                    imgM.setPixel(w, h, imgA.getPixel(w,h));
                else
                    imgM.setPixel(w, h, imgB.getPixel(w,h));
            }
        }
    }
    
    public static GrayScaleImage minimum(GrayScaleImage imgA, GrayScaleImage imgB){
        GrayScaleImage imgM = ImageFactory.createGrayScaleImage(imgA.getDepth(), imgA.getWidth(), imgA.getHeight());
        minimum(imgA, imgB, imgM);
        return imgM;
    }
        
    /**
     * Maximo entre duas imagens
     * @param imgA
     * @param imgB
     * @return
     */
    public static GrayScaleImage maximum(GrayScaleImage imgA, GrayScaleImage imgB){
        GrayScaleImage imgM = ImageFactory.createGrayScaleImage(imgA.getDepth(), imgA.getWidth(), imgA.getHeight());
        maximum(imgA, imgB, imgM);
        return imgM;
    }

    public static void maximum(GrayScaleImage imgA, GrayScaleImage imgB, GrayScaleImage imgM){
        for(int w=0; w < imgA.getWidth(); w++){
            for(int h=0; h < imgA.getHeight(); h++){
                if(imgA.getPixel(w,h) > imgB.getPixel(w,h))
                    imgM.setPixel(w, h, imgA.getPixel(w,h));
                else
                    imgM.setPixel(w, h, imgB.getPixel(w,h));
            }
        }
    }
    
    /**
     * Faz a diferenca entre duas imagens 
     * @param imgA - imagem A
     * @param imgB - imagem B
     * @return IGrayScaleImage com a diferenca entre a imagem A e a imagem B
     */
    public static void subtraction(GrayScaleImage imgA, GrayScaleImage imgB, GrayScaleImage imgOut){
        int tmp = 0;
        for(int i=0; i < imgA.getSize(); i++){
        	tmp = (imgA.getPixel(i) - imgB.getPixel(i)) < 0? 0:  (imgA.getPixel(i) - imgB.getPixel(i));
        	imgOut.setPixel(i, tmp);
        }
    }
  
    public static GrayScaleImage subtraction(GrayScaleImage imgA, GrayScaleImage imgB){
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgA.getDepth(), imgA.getWidth(), imgA.getHeight());
        subtraction(imgA, imgB, imgOut);
        return imgOut;
    }
    
    public static void subtractionAbs(GrayScaleImage imgA, GrayScaleImage imgB, GrayScaleImage imgOut){
        int tmp = 0;
        for(int x = 0 ; x < imgA.getWidth() ; x++){
            for(int y = 0 ; y < imgA.getHeight(); y++){
            	tmp = Math.abs(imgA.getPixel(x, y) - imgB.getPixel(x, y));
            	imgOut.setPixel(x, y, tmp);
            }
        }
    }
   
    public static GrayScaleImage subtractionAbs(GrayScaleImage imgA, GrayScaleImage imgB){
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgA.getDepth(), imgA.getWidth(), imgA.getHeight());
        subtractionAbs(imgA, imgB, imgOut);
        return imgOut;
    }

    public static void multiply(GrayScaleImage imgA, GrayScaleImage imgB, GrayScaleImage imgOut){
        int tmp = 0;
        for(int x = 0 ; x < imgA.getWidth() ; x++){
            for(int y = 0 ; y < imgA.getHeight(); y++){
            	tmp = (imgA.getPixel(x, y) * imgB.getPixel(x, y));
            	imgOut.setPixel(x, y, tmp);
            }
        }
    }
    
    public static GrayScaleImage multiply(GrayScaleImage imgA, GrayScaleImage imgB){
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgA.getDepth(), imgA.getWidth(), imgA.getHeight());
        multiply(imgA, imgB, imgOut);
        return imgOut;
    }
    

    public static void add(GrayScaleImage imgA, GrayScaleImage imgB, GrayScaleImage imgOut){
        int tmp = 0;
        int max = (int) Math.pow(2, imgA.getDepth()) - 1;
        for(int i=0; i < imgA.getSize(); i++){
        	tmp = (imgA.getPixel(i) + imgB.getPixel(i)) > max? max:  (imgA.getPixel(i) + imgB.getPixel(i));
        	imgOut.setPixel(i, tmp);
        }
    }
    
    public static GrayScaleImage add(GrayScaleImage imgA, GrayScaleImage imgB){
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgA.getDepth(), imgA.getWidth(), imgA.getHeight());
        add(imgA, imgB, imgOut);
        return imgOut;
    }
    
    public static boolean isLessOrEqual(GrayScaleImage imgA, GrayScaleImage imgB){
        boolean flag = true;
        for(int i=0; i < imgA.getSize(); i++){
            	if (!(imgA.getPixel(i) <= imgB.getPixel(i)))
            		return false;
        }
        return flag;
    }
    
    public static boolean isLess(GrayScaleImage imgA, GrayScaleImage imgB){
        boolean flag = true;
        for(int i=0; i < imgA.getSize(); i++){
            	if (!(imgA.getPixel(i) < imgB.getPixel(i)))
            		return false;
        }
        return flag;
    }
    
    public static boolean isGreaterOrEqual(GrayScaleImage imgA, GrayScaleImage imgB){
        boolean flag = true;
        for(int i=0; i < imgA.getSize(); i++){
            	if (!(imgA.getPixel(i) >= imgB.getPixel(i)))
            		return false;
        }
        return flag;
    }
    
    public static boolean isGreater(GrayScaleImage imgA, GrayScaleImage imgB){
        boolean flag = true;
        for(int i=0; i < imgA.getSize(); i++){
            	if (!(imgA.getPixel(i) > imgB.getPixel(i)))
            		return false;
        }
        return flag;
    }
    

    /**
     * Verifica se duas imagens sao iguais
     * @param imgA - IGrayScaleImage
     * @param imgB - IGrayScaleImage
     * @return true se forem iguais false caso contrario
     */
    public static boolean equals(GrayScaleImage imgA, GrayScaleImage imgB){
        for(int x = 0 ; x < imgA.getWidth() ; x++)
            for(int y = 0 ; y < imgA.getHeight(); y++)
                if(imgA.getPixel(x, y) != imgB.getPixel(x, y)) return false;
                
        return true;
    }
    

	public static boolean isPlanning(GrayScaleImage g, GrayScaleImage f, AdjacencyRelation adj){
		for(int p=0; p < f.getSize(); p++){	
			for (Integer q : adj.getAdjacencyPixels(g, p)) {
				if(g.getPixel(p) != g.getPixel(q)){
					if(! (f.getPixel(p) != f.getPixel(q))){
						return false;
					}	
				}
			}
		}		
		return true;
	}
	
	public static boolean isExtensive(GrayScaleImage g, GrayScaleImage f){
		for(int p=0; p < f.getSize(); p++){	
			if(! (g.getPixel(p) >= f.getPixel(p))){
				return false;
			}	
		}		
		return true;
	}
	
	public static boolean isAntiExtensive(GrayScaleImage g, GrayScaleImage f){
		for(int p=0; p < f.getSize(); p++){	
			if(! (g.getPixel(p) <= f.getPixel(p))){
				return false;
			}	
		}		
		return true;
	}
	

	public static boolean isMonotonePlaning(GrayScaleImage g, GrayScaleImage f, AdjacencyRelation adj){
		for(int p=0; p < f.getSize(); p++){	
			for (Integer q : adj.getAdjacencyPixels(g, p)) {
				if(g.getPixel(p) > g.getPixel(q)){
					if(! (f.getPixel(p) > f.getPixel(q))){
						return false;
					}	
				}
			}
		}	
		return true;
	}
	
	public static boolean isMonotoneDescPlaning(GrayScaleImage g, GrayScaleImage f, AdjacencyRelation adj){
		for(int p=0; p < f.getSize(); p++){	
			for (Integer q : adj.getAdjacencyPixels(g, p)) {
				if(g.getPixel(p) > g.getPixel(q)){
					if(! (f.getPixel(p) < f.getPixel(q))){
						return false;
					}	
				}
			}
		}	
		return true;
	}
	

	/**
	 * Retorna true se g � leveling de f
	 * @param f
	 * @param g
	 * @return
	 */
	public static boolean isLeveling(GrayScaleImage g, GrayScaleImage f, AdjacencyRelation adj){
		for(int p=0; p < f.getSize(); p++){	
			for (Integer q : adj.getAdjacencyPixels(f, p)) {
				if(g.getPixel(p) > g.getPixel(q)){
					if(f.getPixel(p) >= g.getPixel(p) &&  g.getPixel(q) >= f.getPixel(q)){
						;
					}	
					else{
						return false;
					}
				}
			}
		}	
		return true;
	}
	

	public static boolean isFlattening(GrayScaleImage g, GrayScaleImage f, AdjacencyRelation adj){
		for(int p=0; p < f.getSize(); p++){	
			for (Integer q : adj.getAdjacencyPixels(f, p)) {
				if(g.getPixel(p) > g.getPixel(q)){
					if( ( f.getPixel(p) >= g.getPixel(p) &&  g.getPixel(q) >= f.getPixel(q) ) || 
						( f.getPixel(q) >= g.getPixel(p) &&  g.getPixel(q) >= f.getPixel(p) )  ){
						;
					}	
					else{
						return false;
					}
				}
			}
		}	
		return true;
	}		
	
	
	
}
