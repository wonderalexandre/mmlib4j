package mmlib4j.representation.tree.shapeSpace;

import java.io.File;

import mmlib4j.filtering.AttributeFilters;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.AbstractImageFactory;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.tos.TreeOfShape;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageAlgebra;
import mmlib4j.utils.ImageBuilder;

public class TreeOfComponentTree {
	
	// Original Component Tree
	private MorphologicalTree tree;
	private GrayScaleImage input;
	
	// Tree of Shape Space
	private MorphologicalTree shapeSpacetree;
	private GrayScaleImage imgAttr;
	
	public TreeOfComponentTree(MorphologicalTree tree, int attr, boolean isMaxtree) {
				
		this.tree = tree;
		this.input = tree.getInputImage();
		
		// Image of attr		
		imgAttr = ImageFactory.createGrayScaleImage(AbstractImageFactory.DEPTH_32BITS, input.getWidth(), input.getHeight());
		
		for(NodeLevelSets node : tree.getListNodes()) {			
			for(int p : node.getCompactNodePixels()) {
				imgAttr.setPixel(p, (int) node.getAttributeValue(attr));
			}			
		}
		if(tree instanceof ComponentTree)
			shapeSpacetree = new ComponentTree(imgAttr, ((ComponentTree) tree).getAdjacency(), isMaxtree);
		else
			shapeSpacetree  = new TreeOfShape(imgAttr);
		
	}
	
	// Pruning Shape Space Tree
	public void prunning(double value) {
		
		for(NodeLevelSets node : shapeSpacetree.getListNodes()) {
			// Merge in original tree (it does not modify the Shape Space Tree)
			if(node.getLevel() <= value) {				
				NodeLevelSets nodeM = tree.getNodesMap()[node.getCanonicalPixel()];
				tree.mergeParent(nodeM);
			}
		}
		
	}
	
	public GrayScaleImage reconstruction(){		
		return tree.reconstruction();		
	}
	
	public static void main(String args[]) {
		
		GrayScaleImage input = ImageBuilder.openGrayImage(new File("/Users/gobber/Desktop/lena.jpg"));
				
		MorphologicalTree tree = new ComponentTree(input, AdjacencyRelation.getAdjacency8(), true);		
		AttributeFilters af = new AttributeFilters(tree);
		Attribute.loadAttribute(tree, Attribute.AREA);
		
		TreeOfComponentTree TT = new TreeOfComponentTree(tree, Attribute.AREA, false);
		TT.prunning(100);
		GrayScaleImage output = TT.reconstruction();
		
		MorphologicalTree tree2 = new ComponentTree(input, AdjacencyRelation.getAdjacency8(), true);
		AttributeFilters af2 = new AttributeFilters(tree);
		GrayScaleImage img2 = af2.filteringByPruningMin(100, Attribute.AREA);
		
		System.out.println(ImageAlgebra.equals(output, img2));
		System.out.println(ImageAlgebra.isLessOrEqual(output, input));
		ImageBuilder.saveImage(output, new File("/Users/gobber/Desktop/output.png"));
		//ImageBuilder.saveImage(TT.imgAttr, new File("/Users/gobber/Desktop/attr.png"));
		
	}

}
