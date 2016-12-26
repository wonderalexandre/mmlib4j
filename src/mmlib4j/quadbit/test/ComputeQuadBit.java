package mmlib4j.quadbit.test;

import java.io.File;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;

public class ComputeQuadBit {

	public static void main(String[] args) {
		GrayScaleImage img = ImageBuilder.openGrayImage(new File("/home/dennis/Documents/master/dissertation/ismm/code/images/test.png"));
		ConnectedFilteringByComponentTree tos = new ConnectedFilteringByComponentTree(img, AdjacencyRelation.getAdjacency8(), true);
		tos.computerAttributeBasedBitQuads();
		
		for (NodeCT node: tos.getListNodes()) {
			System.out.println(node.getAttributeValue(Attribute.BIT_QUADS_AREA));
		}
	}
	
}
