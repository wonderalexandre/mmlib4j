package mmlib4j.representation.tree;

import java.io.File;
import java.util.Arrays;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.mergetree.ComputerBasicAttributeMergeTree;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.Utils;

public class TestAttributes {
	public static double[] run(GrayScaleImage imgInput, double attrValue, int type) {		
		ConnectedFilteringByComponentTree tree = new ConnectedFilteringByComponentTree(imgInput, AdjacencyRelation.getAdjacency4(), true);
		tree.loadAttribute(type);
		System.out.println("Number of nodes: "+ tree.getNumNode());
		
		Utils.debug = true;
		tree.getInfoMergedTreeByDirectRule(attrValue, type);
		InfoMergedTree mTree = tree.getMtree();
		System.out.println("Number of nodes: "+ mTree.getNumNode());
		
		boolean[] mapCorrection = new boolean[tree.getNumNodeIdMax()];
		Arrays.fill(mapCorrection, true);
		long ti = System.currentTimeMillis();
		ComputerBasicAttributeMergeTree cbasic = new ComputerBasicAttributeMergeTree(tree.getNumNodeIdMax(), mTree, mapCorrection);
		long tf = System.currentTimeMillis();
		double timeInSec = (tf - ti) /1000.0;
		
		//System.out.println(tree.numberOfCalls);
		System.out.println(cbasic.numberOfCalls);
		
		//return new double[]{tree.timeInSec, tree.numberOfCalls, timeInSec, cbasic.numberOfCalls};
		return null;
	}	
	
	public static void main(String args[]) {
		
		GrayScaleImage imgInput = ImageBuilder.openGrayImage(new File("/Users/gobber/Desktop/zac-gray.png"));
		Utils.debug = true;
		TestAttributes.run(imgInput, 0.1, Attribute.MOMENT_OF_INERTIA);
		
	}
}
