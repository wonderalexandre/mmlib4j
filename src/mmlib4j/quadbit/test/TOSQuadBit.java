package mmlib4j.quadbit.test;

import java.io.File;

import sun.reflect.generics.tree.Tree;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.images.impl.ShortImage;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.utils.ImageBuilder;

public class TOSQuadBit {
	public static void main(String[] args) throws Exception {
		/*int width = 4;
		int height = 3;
		int[] pixels = { 
				0, 0, 3, 2,
				0, 1, 1, 2,
				0, 0, 2, 2};
		
		GrayScaleImage img = ImageFactory.createReferenceGrayScaleImage(ImageFactory.DEPTH_32BITS, pixels, width, height);*/		
		
		GrayScaleImage img = ImageBuilder.openGrayImage(
				new File("/home/dennis/Documents/master/dissertation/ismm/code/images/dataset/piscine.png"));
		ConnectedFilteringByTreeOfShape tos = new ConnectedFilteringByTreeOfShape(img);
		tos.computerAttributeBasedBitQuads();		
		
		for (NodeToS node: tos.getListNodes()) {
			//System.out.println("naive area = " + node.getArea() + " ---> alg. area = " + node.getAttributeValue(Attribute.BIT_QUADS_AREA));			
			if (node.getArea() != (int)node.getAttributeValue(Attribute.BIT_QUADS_AREA)) {
				System.out.println("Fail");
				System.exit(1);
			}
			
		}
		
		System.out.println("Success");
	}
}
