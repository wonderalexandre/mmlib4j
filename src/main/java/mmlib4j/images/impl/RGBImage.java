package mmlib4j.images.impl;

import mmlib4j.images.BinaryImage;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 *
 * @description
 * Classe que representa uma imagem digital. 
 * Essa classe utiliza somente as APIs do java para manipular os pixels da imagens  
 * 
 */ 
public class RGBImage extends AbstractImage2D implements ColorImage{

	private int pixels[]; //matriz de pixel da imagem
    int alpha = 255;
    
    /**
     * Construtor para criar uma nova imagem
     * @param width - largura
     * @param height - altura
     */
    RGBImage(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
    }

    RGBImage(int pixels[], int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }
    
    /**
     * Construtor para criar uma nova imagem com os dados da imagem de entrada
     * @param img - imagem de entrada
     */
    RGBImage(GrayScaleImage img) {
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.pixels = new int[width * height];
        for (int i = 0; i < getSize(); i++){
            	pixels[i] = ((alpha & 0xFF) << 24) |
            				((img.getPixel(i) & 0xFF) << 16) |
            				((img.getPixel(i) & 0xFF) << 8)  |
            				((img.getPixel(i) & 0xFF) << 0);
        }
    }
    

    /**
     * Construtor para criar uma nova imagem com os dados da imagem de entrada
     * @param img - imagem de entrada
     */
    RGBImage(BinaryImage img) {
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.pixels = new int[width * height];
        for (int i = 0; i < getSize(); i++){
        	pixels[i] = ((alpha & 0xFF) << 24) |
        				((img.getPixel(i)?1:0 & 0xFF) << 16) |
        				((img.getPixel(i)?1:0 & 0xFF) << 8)  |
        				((img.getPixel(i)?1:0 & 0xFF) << 0);
        } 
        
    }

    /**
     * Inverter os pixels da imagem [255 - pixel(x,y)]
     * @return
     */
    public ColorImage invert(){
        ColorImage imgOut = new RGBImage(this.getWidth(), this.getHeight());
        for(int w=0;w<this.getWidth();w++){
            for(int h=0;h<this.getHeight();h++){
            	for(int b=0; b < 3; b++)
            		imgOut.setRed(w, h, 255 - this.getRed(w, h));
            		imgOut.setGreen(w, h, 255 - this.getGreen(w, h));
            		imgOut.setBlue(w, h, 255 - this.getBlue(w, h));
            }
        } 
        return imgOut;
    }
    
    public void paintBoundBox(int x1, int y1, int x2, int y2, int c){
        int w = x2 - x1;
        int h = y2 - y1;
        for(int i=0; i < w; i++){
            for(int j=0; j < h; j++){
                if(i <= 1 || j <= 1 || i > w-3 || j > h-3)
                    setPixel(x1 + i, y1 + j, c);        
            }
        }
    }
    
    
    /**
     * Inicializar todos os pixel da imagem para um dado nivel de cinza
     * @param color
     */
    public void initImage(int color){
        for (int i = 0; i < getWidth(); i++){
            for (int j = 0; j < getHeight(); j++){
                setPixel(i,j, color);
            }
        }
    }
    
    /**
     * Cria uma copia da imagem original
     * @return BinaryImage - nova imagem
     */
    public ColorImage duplicate(){
        RGBImage clone = new RGBImage(getWidth(), getHeight());
        for (int i = 0; i < getWidth(); i++){
            for (int j = 0; j < getHeight(); j++){
                clone.setPixel(i,j,getPixel(i, j));
            }
        }
        return clone;
    }
    
    /**
     * Pega o valor do pixel (x, y)
     * @param x - largura
     * @param y - altura
     * @return float - valor do pixel
     */
    public int getPixel(int x, int y){
    	return pixels[y * width + x];
    }
    
    
    public void setAlpha(int value){
    	this.alpha = value;
    }
    
    /**
     * Modifica a matriz de pixel da imagem para os valores da matriz dada
     * @param matrix 
     */
    public void setPixels(int width, int height, int pixels[]){
    	this.width = width;
    	this.height = height;
        this.pixels = pixels;
    }
    public void setPixel(int x, int y, int[] value) {
    	setPixel(y * width + x, value);
    }
    
    public int[] getPixels() {
        return pixels;
    }
    
    public void setPixel(int x, int y, int rgb) {
        pixels[y * width + x] = rgb;
    }

    public void setPixel(int i, int[] value) {
    	pixels[i] = ((alpha & 0xFF) << 24) |
				((value[0] & 0xFF) << 16) |
				((value[1] & 0xFF) << 8)  |
				((value[2] & 0xFF) << 0);
	}
 
    
    public void setPixels(int[] matrix) {
        pixels = matrix;
        
    }


    public void setPixel(int i, int rgb){
    	pixels[i] = rgb;
    }
    
    public int getPixel(int i){
        return pixels[i];
    }
    
    /**
     * Converte uma imagem em RGB para niveis de cinza
     */
    public GrayScaleImage convertGrayScaleImage() {
        GrayScaleImage image = new ByteImage(this.getWidth(), this.getHeight());
        int r, g, b;
        for(int w=0;w<this.getWidth();w++){
            for(int h=0;h<this.getHeight();h++){
                r = this.getRed(w, h);
                g = this.getGreen(w, h);
                b = this.getBlue(w, h);
                image.setPixel(w, h, (int) Math.round(.299*r + .587*g + .114*b)); //convertendo para niveis de cinza
            }
        } 
        return image;
    }

    public GrayScaleImage getRed() {
        GrayScaleImage image = new ByteImage(this.getWidth(), this.getHeight());
        for(int p=0;p<this.getSize();p++){
        	image.setPixel(p, this.getRed(p)); 
        } 
        return image;
    }
    
    public GrayScaleImage getBlue() {
        GrayScaleImage image = new ByteImage(this.getWidth(), this.getHeight());
        for(int p=0;p<this.getSize();p++){
        	image.setPixel(p, this.getBlue(p)); 
        } 
        return image;
    }
    
    public GrayScaleImage getGreen() {
        GrayScaleImage image = new ByteImage(this.getWidth(), this.getHeight());
        for(int p=0;p<this.getSize();p++){
        	image.setPixel(p, this.getGreen(p)); 
        } 
        return image;
    }
   
    public void addSubImage(ColorImage img, int x, int y){
        for(int i=x; i < img.getWidth(); i++){
            for(int j=y; j < img.getHeight(); j++){
                if(this.isPixelValid(i, j))
                    setPixel(i, j, img.getPixel(i, j));
            }
        }
    }
    
    public void addSubImage(GrayScaleImage img, int x, int y){
        for(int i=x; i < img.getWidth(); i++){
            for(int j=y; j < img.getHeight(); j++){
                if(this.isPixelValid(i, j))
                    setPixel(i, j, img.getPixel(i, j));
            }
        }
    }


	public int getRed(int i) {
		return (pixels[i] >> 16) & 0xFF; //red
	}

	public int getGreen(int i) {
		return (pixels[i] >> 8) & 0xFF; //green
	}

	public int getBlue(int i) {
		return (pixels[i] >> 0) & 0xFF; //blue
	}

	public int getRed(int x, int y) {
		return getRed(y * width + x);
	}

	
	public int getGreen(int x, int y) {
		return getGreen(y * width + x);
	}

	
	public int getBlue(int x, int y) {
		return getBlue(y * width + x);
	}

	public void setRed(int x, int y, int value) {
		setRed(y * width + x, value);
	}

	public void setGreen(int x, int y, int value) {
		setGreen(y * width + x, value);
	}

	public void setBlue(int x, int y, int value) {
		setBlue(y * width + x, value);		
	}

	public void setRed(int i, int value) {
		pixels[i] = ((alpha & 0xFF) << 24) |
					((value & 0xFF) << 16) |
					((getGreen(i) & 0xFF) << 8)  |
					((getBlue(i) & 0xFF) << 0);
	}

	public void setGreen(int i, int value) {
		pixels[i] = ((alpha & 0xFF) << 24) |
					((getRed(i) & 0xFF) << 16) |
					((value & 0xFF) << 8)  |
					((getBlue(i) & 0xFF) << 0);
	}

	public void setBlue(int i, int value) {
		pixels[i] = ((alpha & 0xFF) << 24) |
					((getRed(i) & 0xFF) << 16) |
					((getGreen(i) & 0xFF) << 8)  |
					((value & 0xFF) << 0);
		
	}

	public void setGray(int i, int value) {
		pixels[i] = ((alpha & 0xFF) << 24) |
					((value & 0xFF) << 16) |
					((value & 0xFF) << 8)  |
					((value & 0xFF) << 0);
	}
	public void setGray(int x, int y, int value){
		setGray(y * width + x, value);
	}
    
    public int getGray(int i){
    	int r = this.getRed(i);
        int g = this.getGreen(i);
        int b = this.getBlue(i);
        return (r+g+b)/3;
    }
    
    public int getGray(int x, int y){
    	return getGray(y * width + x);
    }
    
    public int getDepth(){
    	return 32;
    }
    
}
