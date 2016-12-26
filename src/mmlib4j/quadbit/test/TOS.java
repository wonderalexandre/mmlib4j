package mmlib4j.quadbit.test;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.images.impl.IntegerImage;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;

public class TOS {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/*int width = 4;
		int height = 3;
		int[] pixels = { 
				0, 0, 200, 2,
				0, 1, 1, 2,
				0, 0, 2, 2};
		
		GrayScaleImage image = ImageFactory.createReferenceGrayScaleImage(ImageFactory.DEPTH_32BITS, pixels, width, height);
		ConnectedFilteringByTreeOfShape tos = new ConnectedFilteringByTreeOfShape(image);
		short[] u = tos.getBuilder().getImageU();
		
		int gwidth = (width*2+1);
		int gheight = (height*2+1);
		
		System.out.println();
		System.out.println();
		System.out.println();
		for (int py = 0; py < gheight; py++) {
			for (int px = 0; px < gwidth; px++) 
				System.out.print(u[py*gwidth + px] + ", ");
			System.out.println();
		}*/
		
		int width = 4;
		int height = 4;
		
		int Uwidth = width * 2 + 1;
		int Uheight = height * 2 + 1;
		
		int[] pixels = {
				0, 0, 1, 1,
				0, 1, 1, 1,
				0, 1, 1, 1,
				0, 0, 0, 0
		};
		
		GrayScaleImage image = ImageFactory.createReferenceGrayScaleImage(ImageFactory.DEPTH_32BITS, pixels, width, height);
		ConnectedFilteringByTreeOfShape tos = new ConnectedFilteringByTreeOfShape(image);
		short[] u = tos.getBuilder().getImageU();
		
		for (int py = 0; py < Uheight; py++) {
			for (int px = 0; px < Uwidth; px++) 
				System.out.print(u[py*Uwidth + px] + ", ");
			System.out.println();
		}
	}
}
