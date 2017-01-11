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
		if (args.length < 1) {
			System.err.println("Command line parameter error, You must enter with a image path file as the command line parameter.");
			System.exit(1);
		}
		
		System.out.println("Program running for image in: " + args[0]);
		
		GrayScaleImage img = ImageBuilder.openGrayImage(new File(args[0]));
		ConnectedFilteringByTreeOfShape tos = new ConnectedFilteringByTreeOfShape(img);
		tos.computerAttributeBasedBitQuads();		
		
		for (NodeToS node: tos.getListNodes()) {
			//System.out.println(node.getAttributeValue(Attribute.BIT_QUADS_AREA) + " (" + node.getArea() + ")");
			if (node.getArea() != (int)node.getAttributeValue(Attribute.BIT_QUADS_AREA)) {
				System.out.println("Fail");
				System.exit(1);
			}
			
		}
		System.out.println("==================================================");
		System.out.println("Success");
	}
}
