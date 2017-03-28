package mmlib4j.filtering;

import java.io.File;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.utils.ImageBuilder;

public class EdgeDetectors {
	
	static EdgeOperators operators = new EdgeOperators();
	
	public static GrayScaleImage sobel( GrayScaleImage img ) {
		
		return operators.detectEdges( img, EdgeOperators.SOBEL );
		
	}
	
	public static GrayScaleImage prewitt( GrayScaleImage img ) {
		
		return operators.detectEdges( img, EdgeOperators.PREWITT );
		
	}
	
	public static void main( String [] args ) {
		
		GrayScaleImage inputImg = ImageBuilder.openGrayImage();
		
		/*GrayScaleImage inputImg = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_8BITS, 3, 3);
		
		inputImg.setPixels( 3, 3, new byte []{
				
				1,2,3,
				4,5,6,
				7,8,9
				
		});*/
		
		ImageBuilder.saveImage( EdgeDetectors.sobel( inputImg ) , new File("/home/gobber/gradient.png"));
		
	}

}
