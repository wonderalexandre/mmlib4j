package mmlib4j.utils;

import mmlib4j.images.BinaryImage;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.Image2D;
import mmlib4j.images.RealImage;
import mmlib4j.images.impl.ImageFactory;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Graphic User Interface by Matlab
 * 
 * <b>Important note for the installation:</b><br>
 *      javaaddpath '<path>\mmlib4j.jar'<br>
 */
public class MatlabAdapter {



	/**
	 * Get an image.
	 * 
	 * @param Image2D
	 *            image
	 * @return an N x M array representing the input image
	 */
	private static Object toMatlab(Image2D image) {
		int width = image.getWidth();
		int height = image.getHeight();

		if(image instanceof GrayScaleImage){
			GrayScaleImage img = (GrayScaleImage) image;
			int[][] mat = new int[height][width];
			for(int x=0; x < img.getWidth(); x++){
				for(int y=0; y < img.getHeight(); y++){
					mat[y][x] = img.getPixel(x, y);
				}
			}
			return mat;
		}
		else if(image instanceof ColorImage){
			ColorImage img = (ColorImage) image;
			int[][][] mat = new int[height][width][3];
			for(int x=0; x < img.getWidth(); x++){
				for(int y=0; y < img.getHeight(); y++){
					mat[y][x][0] = img.getRed(x, y);
					mat[y][x][1] = img.getGreen(x, y);
					mat[y][x][2] = img.getBlue(x, y);
				}
			}
			return mat;
		}
		else if(image instanceof RealImage){
			double[][] mat = new double[height][width];
			RealImage img = (RealImage) image;
			for(int x=0; x < img.getWidth(); x++){
				for(int y=0; y < img.getHeight(); y++){
					mat[y][x] = img.getPixel(x, y);
				}
			}
			return mat;
		}
		if(image instanceof BinaryImage){
			BinaryImage img = (BinaryImage) image;
			short[][] mat = new short[height][width];
			for(int x=0; x < img.getWidth(); x++){
				for(int y=0; y < img.getHeight(); y++){
					mat[y][x] = (short) (img.getPixel(x, y)? 1:0);
				}
			}
			return mat;
		}
		else{
			System.out.println("Erro..");
			return null;
		}
	}

	/**
	 * Create a new image in ImageJ from a Matlab variable.
	 * 
	 * This method try to create a image (ImagePlus of ImageJ) from a Matlab's
	 * variable which should be an 2D or 3D array The recognize type are byte,
	 * short, int, float and double. The dimensionality of the 2 (image) or 3
	 * (stack of images)
	 * 
	 * @param object
	 *            Matlab variable
	 */
	public static GrayScaleImage tGrayScaleImage(Object object) {
		GrayScaleImage img = null;
		if (object instanceof byte[][]) {
			byte pixels[][] = (byte[][]) object;
			img = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_8BITS, pixels[0].length, pixels.length);
			for(int x = 0; x < img.getWidth(); x++){
				for(int y = 0; y < img.getHeight(); y++){
					img.setPixel(x,  y, pixels[y][x]);
				}
			}
		}
		else if (object instanceof short[][]){
			short pixels[][] = (short[][]) object;
			img = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_16BITS, pixels[0].length, pixels.length);
			for(int x = 0; x < img.getWidth(); x++){
				for(int y = 0; y < img.getHeight(); y++){
					img.setPixel(x,  y, pixels[y][x]);
				}
			}
		}
		else if (object instanceof int[][]){
			int pixels[][] = (int[][]) object;
			img = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_32BITS, pixels[0].length, pixels.length);
			for(int x = 0; x < img.getWidth(); x++){
				for(int y = 0; y < img.getHeight(); y++){
					img.setPixel(x,  y, pixels[y][x]);
				}
			}
		}
		return img;
		
	}

	public static ColorImage tColorImage(Object object) {
		int pixels[][][] = (int[][][]) object;
		ColorImage img = ImageFactory.createColorImage(pixels[0].length, pixels.length);
		for(int x = 0; x < img.getWidth(); x++){
			for(int y = 0; y < img.getHeight(); y++){
				img.setRed(x,  y, pixels[y][x][0]);
				img.setGreen(x,  y, pixels[y][x][1]);
				img.setBlue(x,  y, pixels[y][x][2]);
			}
		}
		return img;
	}

	

}