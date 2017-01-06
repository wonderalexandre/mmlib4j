package mmlib4j.quadbit.test;

import java.io.File;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.images.impl.IntegerImage;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.utils.ImageBuilder;

public class TOS {

	public static void main(String[] args) {		
		GrayScaleImage image = ImageBuilder.openGrayImage(new File("/home/dennis/Documents/master/dissertation/ismm/code/images/test2.png"));
		ConnectedFilteringByTreeOfShape tos = new ConnectedFilteringByTreeOfShape(image);
		short[] u = tos.getBuilder().getImageU();
		
		GrayScaleImage uImage = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_16BITS, 2*image.getWidth() + 1, 2*image.getHeight() + 1);
		uImage.setPixels(2*image.getWidth() + 1, 2*image.getHeight() + 1, u);
		ImageBuilder.saveImage(uImage, new File("/home/dennis/Documents/master/dissertation/ismm/code/images/test2_u.png"));
		
		/*int width = 2;
		int height = 2;
		
		int Uwidth = width * 2 + 1;
		int Uheight = height * 2 + 1;
		
		int[] pixels = {
				1, 0,
				2, 1
		};
		
		GrayScaleImage image = ImageFactory.createReferenceGrayScaleImage(ImageFactory.DEPTH_32BITS, pixels, width, height);
		ConnectedFilteringByTreeOfShape tos = new ConnectedFilteringByTreeOfShape(image);
		short[] u = tos.getBuilder().getImageU();
		
		
		for (int py = 0; py < Uheight; py++) {
			for (int px = 0; px < Uwidth; px++) 
				System.out.print(u[py*Uwidth + px] + ", ");
			System.out.println();
		}
	}*/
	}
}
