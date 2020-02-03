package mmlib4j.filtering;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.AbstractImageFactory;
import mmlib4j.images.impl.MmlibImageFactory;
import mmlib4j.utils.AdjacencyRelation;

/**
 *  Magnitude do Gradiente
 * 
 *   \forallp \in img, G( p ) = \sqrt( Gx( p )^2 + Gy( p )^2 )
 *  
 *   Máscaras extraidas do livro : Digital Image Processing An Algorithmic Introduction. 
 * @author Charles Goober
 *
 */
public class EdgeOperators {
	
	static final AdjacencyRelation adj = AdjacencyRelation.getAdjacency8();
	
	static final int prewittX[] = { 0, -1, -1, 
							 0,  1, 1, 
							 1,  0, -1 };
	
	static final int prewittY[] = { 0, 0, -1, 
							-1,-1, 0, 
							 1, 1, 1 }; 
	
	static final int sobelX[] = { 0, -2, -1, 
						   0,  1,  2, 
						   1,  0, -1 }; 
	
	static final int sobelY[] = { 0, 0, -1,
						  -2, -1, 0,
						   1,  2, 1 };
	
	
	public static GrayScaleImage detectEdges( GrayScaleImage img, int maskX[], int maskY[], GrayScaleImage imgOut) {
		double gx, gy;
		int i;
		for( int p = 0 ; p < img.getSize() ; p++ ) {
			gx = 0; gy = 0; i = 0;
			for( Integer q : adj.getAdjacencyPixels( img, p )  ) {
				gx += img.getPixel( q ) * maskX[ i ];
				gy += img.getPixel( q ) * maskY[ i ];
				i++;
			}	
			imgOut.setPixel( p, ( int ) Math.sqrt( gx * gx + gy * gy ) );					
		}
		return imgOut;
	}
	
	public static GrayScaleImage sobel( GrayScaleImage img ) {
		GrayScaleImage imgOut = AbstractImageFactory.instance.createGrayScaleImage( img.getDepth(), 
					   img.getWidth(), 
					   img.getHeight() );
		
		return detectEdges( img, sobelX, sobelY, imgOut);
	}
	
	public static GrayScaleImage sobel( GrayScaleImage img, GrayScaleImage imgOut) {
		return detectEdges( img, sobelX, sobelY, imgOut);	
	}
	
	public static GrayScaleImage prewitt( GrayScaleImage img ) {
		GrayScaleImage imgOut = AbstractImageFactory.instance.createGrayScaleImage( img.getDepth(), 
				   img.getWidth(), 
				   img.getHeight() );
		
		return detectEdges( img, prewittX, prewittY, imgOut);	
	}
	
	public static GrayScaleImage prewitt( GrayScaleImage img, GrayScaleImage imgOut) {
		return detectEdges( img, prewittX, prewittY, imgOut );	
	}
	

}
