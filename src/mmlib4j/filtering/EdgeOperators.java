package mmlib4j.filtering;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.AdjacencyRelation;

public class EdgeOperators {
	
	/* 
	 * 
	 *  Magnitude do Gradiente
	 * 
	 *   \forallp \in img, G( p ) = \sqrt( Gx( p )^2 + Gy( p )^2 )
	 *  
	 *   Máscaras extraidas do livro : Digital Image Processing An Algorithmic Introduction. 
	 *  
	 * */
	
	public static final int SOBEL = 1;
	
	public static final int PREWITT = 2;
	
	
	int Hx[], Hy[];
	
	final AdjacencyRelation adj = AdjacencyRelation.getAdjacency8();
	
	
	final int prewittX[] = { 0, -1, -1, 0, 1, 1, 1, 0, -1 };
	
	final int prewittY[] = { 0, 0, -1, -1, -1, 0, 1, 1, 1 }; 
	
	
	final int sobelX[] = { 0, -2, -1, 0, 1, 2, 1, 0, -1 }; 
	
	final int sobelY[] = { 0, 0, -1, -2, -1, 0, 1, 2, 1 };
	
	
	public GrayScaleImage detectEdges( GrayScaleImage img, int type ) {
		
		GrayScaleImage imgContour = ImageFactory.createGrayScaleImage( img.getDepth(), 
		 		   													   img.getWidth(), 
		 		   													   img.getHeight() );
		
		switch( type ) {
		
		case PREWITT : 				
			Hx = prewittX;
			Hy = prewittY;
			break;
			
		default :
			
		case SOBEL :  
			Hx = sobelX;
			Hy = sobelY;
			break;
	
		}
		
		
		double gx, gy;
		
		int i;
		
		for( int p = 0 ; p < img.getSize() ; p++ ) {
											
			gx = 0; gy = 0; i = 0;
			
			for( Integer q : adj.getAdjacencyPixels( img, p )  ) {
				
				gx += img.getPixel( q ) * Hx[ i ];
				
				gy += img.getPixel( q ) * Hy[ i ];
				
				i++;
				
			}	
			
			imgContour.setPixel( p, ( int ) Math.sqrt( gx * gx + gy * gy ) );					
			
		}
		
		return imgContour;
		
	}

}
