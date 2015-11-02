package mmlib4j.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import mmlib4j.images.BinaryImage;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.Image2D;
import mmlib4j.images.RealImage;
import mmlib4j.images.impl.BitImage;
import mmlib4j.images.impl.ImageFactory;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ImageBuilder {


	public static BufferedImage convertToImage(Image2D image) {
		BufferedImage bi = null;
		if (image instanceof GrayScaleImage){
			bi = new BufferedImage(image.getWidth(), image.getHeight(),	BufferedImage.TYPE_INT_RGB);
			GrayScaleImage img = (GrayScaleImage) image;
			for (int w = 0; w < img.getWidth(); w++) {
				for (int h = 0; h < img.getHeight(); h++) {
					bi.setRGB(w,h, new Color(img.getPixel(w, h), img.getPixel(w, h), img.getPixel(w, h)).getRGB());
				}
			}
		}
		else if (image instanceof BinaryImage){
			BinaryImage img = (BinaryImage) image;
			bi = new BufferedImage(image.getWidth(), image.getHeight(),	BufferedImage.TYPE_INT_RGB);
			for (int w = 0; w < img.getWidth(); w++) {
				for (int h = 0; h < img.getHeight(); h++) {
					if( img.isPixelForeground(w, h) )
						bi.setRGB(w, h, Color.WHITE.getRGB());
					else
						bi.setRGB(w, h, Color.BLACK.getRGB());
				}
			}
		}
		else if (image instanceof ColorImage){
			bi = new BufferedImage(image.getWidth(), image.getHeight(),	BufferedImage.TYPE_INT_RGB);
			ColorImage img = (ColorImage) image;
			for (int w = 0; w < img.getWidth(); w++) {
				for (int h = 0; h < img.getHeight(); h++) {
					bi.setRGB(w, h, img.getPixel(w, h));
				}
			}
		}
		else if (image instanceof RealImage){
			bi = new BufferedImage(image.getWidth(), image.getHeight(),	BufferedImage.TYPE_INT_RGB);
			RealImage img = (RealImage) image;
			double max = img.getPixelMax();
			double min = img.getPixelMin();
			int value;
			for (int w = 0; w < img.getWidth(); w++) {
				for (int h = 0; h < img.getHeight(); h++) {
					value = ImageUtils.normalized255(img.getPixel(w, h), max, min);
					bi.setRGB(w, h,  new Color(value, value, value).getRGB());
				}
			}
		}
		bi.getSource();
		return bi;
	}
	

	/**
	 * salvando uma imagem
	 * 
	 * @param image
	 * @throws IOException
	 */
	public static void saveImage(BufferedImage image, File file) {
		try {
			ImageIO.write(image, file.getName().substring(file.getName().length() - 3), file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * salvando uma imagem
	 * 
	 * @param image
	 * @throws IOException
	 */
	public static void saveImage(Image2D image, File file) {
		if (image instanceof GrayScaleImage)
			saveImage(ImageBuilder.convertToImage((GrayScaleImage) image),	file);
		else if (image instanceof BinaryImage)
			saveImage(ImageBuilder.convertToImage(((BinaryImage) image)), file);
		else if (image instanceof ColorImage)
			saveImage(ImageBuilder.convertToImage(((ColorImage) image)), file);
	}
	
	public static File windowSaveFile(){
   		JFileChooser fc = new JFileChooser();
   	    fc.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
   	    FileNameExtensionFilter filter = new FileNameExtensionFilter("Imagem PNG","png");
   	    fc.setFileFilter(filter);
   	    int res = fc.showSaveDialog(null);
   	    return fc.getSelectedFile();//.getSelectedFile().getParent() + "\\" + fc.getSelectedFile().getName() + ".png";
   	}

	public static File windowOpenFile() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.showOpenDialog(null);
		return fc.getSelectedFile();
	}
	
	public static File windowOpenFileDir() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.showOpenDialog(null);
		return fc.getSelectedFile();
	}
	
	public static File[] windowOpenDir() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.showOpenDialog(null);
		return fc.getSelectedFile().listFiles();
	}
	
	
	

	/**
	 * Construtor que recebe uma imagem e extrai a sua matriz de pixels
	 * 
	 * @param img
	 *            - Image
	 */
	public static BinaryImage convertToBinaryImage(BufferedImage image, int back) {
		int width = image.getWidth();
		int height = image.getHeight();
		BinaryImage img = new BitImage(width, height);
		if (image.getType() == BufferedImage.TYPE_BYTE_GRAY || image.getType() == BufferedImage.TYPE_BYTE_BINARY) {
			for (int w = 0; w < width; w++) {
				for (int h = 0; h < height; h++) {
					img.setPixel(w, h, image.getRaster().getSample(w, h, 0) != back);
				}
			}
		} else {
			int rgb, r, g, b;
			for (int w = 0; w < width; w++) {
				for (int h = 0; h < height; h++) {
					rgb = image.getRGB(w, h);
					
					// int alpha = (rgb >> 24) & 0xff;
					r = (int) ((rgb & 0x00FF0000) >>> 16); // Red level
					g = (int) ((rgb & 0x0000FF00) >>> 8); // Green level
					b = (int) (rgb & 0x000000FF); // Blue leve
					img.setPixel(w, h, (r + g + b) / 3 == back);
					
				}
			}
		}
		return img;
		
	}
	
	public static void convertColorSpaceRGBtoYIQ(ColorImage img){
		for(int p=0; p < img.getSize(); p++){
			int r = img.getRed(p);
			int g = img.getGreen(p);
			int b = img.getBlue(p);
			/*
			[ Y ]     [ 0.299   0.587   0.114 ] [ R ]
			[ I ]  =  [ 0.596  -0.275  -0.321 ] [ G ]
			[ Q ]     [ 0.212  -0.523   0.311 ] [ B ]
			*/

			int y = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
			int i = (int) Math.round(0.596 * r + -0.275 * g + -0.321 * b);
			int q = (int) Math.round(0.212 * r + -0.523 * g + 0.311 * b);
			img.setPixel(p, new int[]{y,i,q});
		}
	}

	public static GrayScaleImage getConvertColorSpaceRGBtoChong(ColorImage img){
		GrayScaleImage imgG = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		for(int p=0; p < img.getSize(); p++){
			int r = img.getRed(p);
			int g = img.getGreen(p);
			int b = img.getBlue(p);
			
			
			//b
			double x = Math.log( (0.947 * r + 0.295 * g + -0.131 * b) );
			double y = Math.log( (-0.118 * r + 0.993 * g + 0.737 * b) );
			double z = Math.log( (0.923 * r + -0.465 * g + 0.995 * b) );
			
			double xx = 0.271 * x + -0.228 * y + -0.181 * z;
			double yy = -0.565 * x + -0.772 * y + 0.129 * z;
			double zz = -0.416 * x + -0.458 * y + -0.458 * z;
			    
			
			
			int value = (int) Math.sqrt( Math.pow(r-xx, 2));
			if(value < 0) value = 0;
			else if(value > 255) value=255;
			int value1 = value;
			
			value = (int) Math.sqrt( Math.pow(g-yy, 2));
			if(value < 0) value = 0;
			else if(value > 255) value=255;
			int value2 = value;
			
			value = (int) Math.sqrt( Math.pow(b-zz, 2));
			if(value < 0) value = 0;
			else if(value > 255) value=255;
			int value3 = value;
			
			value= (value1 + value2 + value3)/3;
			if(value < 0) value = 0;
			else if(value > 255) value=255;
			imgG.setPixel(p, value );
		}
		return imgG;
	}

	public static void convertColorSpaceRGBtoChong(ColorImage img){
		for(int p=0; p < img.getSize(); p++){
			int r = img.getRed(p);
			int g = img.getGreen(p);
			int b = img.getBlue(p);
			
			//b
			
			double x = Math.exp( Math.round(0.947 * r + 0.295 * g + -0.131 * b) );
			double y = Math.exp( Math.round(-0.118 * r + 0.993 * g + 0.737 * b) );
			double z = Math.exp( Math.round(0.923 * r + -0.465 * g + 0.995 * b) );
			
			double xx = 0.271 * x + -0.228 * y + -0.181 * z;
			double yy = -0.565 * x + -0.772 * y + 0.129 * z;
			double zz = -0.416 * x + -0.458 * y + -0.458 * z;
			

			int value = (int) Math.sqrt( Math.pow(r-xx, 2));
			if(value < 0) value = 0;
			else if(value > 255) value=255;
			int value1 = value;
			
			value = (int) Math.sqrt( Math.pow(g-yy, 2));
			if(value < 0) value = 0;
			else if(value > 255) value=255;
			int value2 = value;
			
			value = (int) Math.sqrt( Math.pow(b-zz, 2));
			if(value < 0) value = 0;
			else if(value > 255) value=255;
			int value3 = value;
			
			img.setPixel(p, new int[]{value1, value2, value3});
			
		}
		
	}
	
	public static GrayScaleImage convertToGrayImage(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		GrayScaleImage img = ImageFactory.createGrayScaleImage(8, width, height);
		if (image.getType() == BufferedImage.TYPE_BYTE_GRAY || image.getType() == BufferedImage.TYPE_BYTE_BINARY) {
			for (int w = 0; w < width; w++) {
				for (int h = 0; h < height; h++) {
					img.setPixel(w, h,  image.getRaster().getSample(w, h, 0));
					
					//System.out.println(((image.getRaster().getSample(x, y, b).getRGB(x, y)(w, h) >> 16) & 0xff) == img.getPixel(w, h));
					
				}
			}
		} else {
			int rgb, r, g, b;
			for (int w = 0; w < width; w++) {
				for (int h = 0; h < height; h++) {
					rgb = image.getRGB(w, h);

					// int alpha = (rgb >> 24) & 0xff;
					r = (int) ((rgb & 0x00FF0000) >>> 16); // Red level
					g = (int) ((rgb & 0x0000FF00) >>> 8); // Green level
					b = (int) (rgb & 0x000000FF); // Blue level

					
					img.setPixel(w, h,  (int) Math.round(.299 * r + .587 * g + .114 * b)); // convertendo para niveis de cinza
					
				}
			}
		}
		return img;
	}

	public static ColorImage convertToRGBImage(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		ColorImage img = ImageFactory.createColorImage(width, height);
		for (int w = 0; w < width; w++) {
			for (int h = 0; h < height; h++) {
				img.setPixel(w, h, image.getRGB(w, h));				
			}
		}
		return img;
		
	}


	public static ColorImage openRGBImage( ) {
		try {
			return convertToRGBImage(ImageIO.read(windowOpenFile()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static ColorImage openRGBImage(File file) {
		try {
			return convertToRGBImage(ImageIO.read(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static GrayScaleImage openGrayImage(File file) {
		try {
			return convertToGrayImage(ImageIO.read(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static GrayScaleImage openGrayImage() {
		try {
			return convertToGrayImage(ImageIO.read(windowOpenFile()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static BinaryImage openBinaryImage(File file) {
		try {
			return convertToBinaryImage(ImageIO.read(file), 0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static BinaryImage openBinaryImage() {
		try {
			return convertToBinaryImage(ImageIO.read(windowOpenFile()), 0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static BinaryImage openBinaryImage(File file, int background) {
		try {
			return convertToBinaryImage(ImageIO.read(file), background);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	

}
