package mmlib4j.filtering.color;

import mmlib4j.gui.WindowImages;
import mmlib4j.images.ColorImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.ImageBuilder;

public class ColorConstancy {

	
	public static void whitePatch(ColorImage imgIn, ColorImage imgOut){
		int maxR = 0;
		int maxG = 0;
		int maxB = 0;
		for(int p=0; p < imgIn.getSize(); p++){
			if(maxR < imgIn.getRed(p))
				maxR = imgIn.getRed(p);
			if(maxG < imgIn.getGreen(p))
				maxG = imgIn.getGreen(p);
			if(maxB < imgIn.getBlue(p))
				maxB = imgIn.getBlue(p);
		}
		
		double kr = 255.0/maxR;
		double kg = 255.0/maxG;
		double kb = 255.0/maxB;
		
		for(int p=0; p < imgIn.getSize(); p++){
			imgOut.setRed(p, (int) (kr * imgIn.getRed(p)));
			imgOut.setGreen(p, (int) (kg * imgIn.getGreen(p)));
			imgOut.setBlue(p, (int) (kb * imgIn.getBlue(p)));
		}
	}
	
	public static ColorImage whitePatch(ColorImage imgIn){
		ColorImage imgOut = ImageFactory.instance.createColorImage(imgIn.getWidth(), imgIn.getHeight());
		whitePatch(imgIn, imgOut);
		return imgOut;
	}

	public static ColorImage greyWorld(ColorImage imgIn){
		ColorImage imgOut = ImageFactory.instance.createColorImage(imgIn.getWidth(), imgIn.getHeight());
		greyWorld(imgIn, imgOut);
		return imgOut;
	}
	
	public static void greyWorld(ColorImage imgIn, ColorImage imgOut){
		int sumR = 0;
		int sumG = 0;
		int sumB = 0;
		for(int p=0; p < imgIn.getSize(); p++){
			sumR += imgIn.getRed(p);
			sumG += imgIn.getGreen(p);
			sumB += imgIn.getBlue(p);
		}
		double meanR = sumR/(double)imgIn.getSize();
		double meanG = sumG/(double)imgIn.getSize();
		double meanB = sumB/(double)imgIn.getSize();
		
		double avg = (meanR + meanG + meanB)/3; 
		
		double kr = avg/meanR;
		double kg = avg/meanG;
		double kb = avg/meanB;
		
		for(int p=0; p < imgIn.getSize(); p++){
			imgOut.setRed(p, (int) (kr * imgIn.getRed(p)));
			imgOut.setGreen(p, (int) (kg * imgIn.getGreen(p)));
			imgOut.setBlue(p, (int) (kb * imgIn.getBlue(p)));
		}
	}
	
	
	public static void toChromaticityColorSpace(ColorImage imgIn, ColorImage imgOut){
		double rgb;
		double epsilon=0.0001;
		for(int p=0; p < imgIn.getSize(); p++){
			rgb = imgIn.getRed(p) + imgIn.getGreen(p) + imgIn.getBlue(p) + epsilon;
			
			imgOut.setRed(p, (int) (imgIn.getRed(p) / rgb * 255) );
			imgOut.setGreen(p,(int) (imgIn.getGreen(p)/ rgb* 255));
			imgOut.setBlue(p, (int) (imgIn.getBlue(p)/ rgb* 255) );
		}
	}

	public static ColorImage toChromaticityColorSpace(ColorImage imgIn){
		ColorImage imgOut = ImageFactory.instance.createColorImage(imgIn.getWidth(), imgIn.getHeight());
		toChromaticityColorSpace(imgIn, imgOut);
		return imgOut;
	}

	public static void main(String args[]){
		ColorImage imgIn = ImageBuilder.openRGBImage();
		WindowImages.show(imgIn, whitePatch(imgIn));
		WindowImages.show(imgIn, greyWorld(imgIn));
		WindowImages.show(imgIn, toChromaticityColorSpace(imgIn));
	}
	
}
