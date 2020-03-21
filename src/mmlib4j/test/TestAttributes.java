package mmlib4j.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.Utils;

public class TestAttributes {
	
	public static ConnectedFilteringByComponentTree tree;	
	public final static int SIMPLIFY_DIRECT_RULE = 11;
	public final static int SIMPLIFY_SUBTRACTIVE_RULE = 12;
	public final static int SIMPLIFY_MIN_RULE = 13;
	public final static int SIMPLIFY_MAX_RULE = 14;
	
	public static void run(GrayScaleImage imgInput, 
						   double attrValue,
						   int type,
						   int filterStrategy) {
		Utils.debug = false;
		
		// Apply simplification		
		switch (filterStrategy) {
		case SIMPLIFY_MIN_RULE:
			tree.simplificationTreeByPruningMin(attrValue, type);	
			break;

		case SIMPLIFY_MAX_RULE:
			tree.simplificationTreeByPruningMax(attrValue, type);	
			break;
			
		case SIMPLIFY_DIRECT_RULE:
			tree.simplificationTreeByDirectRule(attrValue, type);	
			break;
			
		case SIMPLIFY_SUBTRACTIVE_RULE:
			tree.simplificationTreeBySubstractiveRule(attrValue, type);	
			break;	
			
		default:
			break;
		}
		
		ComputerBasicAttributeUpdateNaive comp = new ComputerBasicAttributeUpdateNaive(tree.getNumNodeIdMax(), tree.getRoot(), tree.getInputImage());
		comp.addAttributeInNodesCT(tree.getListNodes());
			
	}	
	
	public static void main(String args[]) throws FileNotFoundException{
		
		String logPath = "/Users/wonderalexandre/Desktop/";
		PrintStream o = new PrintStream(logPath + "log.txt");
		PrintStream console = System.out;		
		
		int type = Attribute.AREA;
		double[] thresholds = {49, 169, 361, 625, 961, 1369, 1849, 2401};
		int strategy = SIMPLIFY_MIN_RULE;
		
		//int type = Attribute.AREA;
		//double[] thresholds = {49, 169, 361, 625, 961, 1369, 1849, 2401};
		//int strategy = SIMPLIFY_MAX_RULE; 
		
		//int type = Attribute.STD_LEVEL;
		//double[] thresholds = {10, 20, 30, 40, 50, 60, 70, 80};
		//int strategy = SIMPLIFY_DIRECT_RULE;
		//int strategy = SIMPLIFY_SUBTRACTIVE_RULE;
		
		//int type = Attribute.MOMENT_OF_INERTIA;
		//double[] thresholds = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
		//int strategy = SIMPLIFY_DIRECT_RULE;
		//int strategy = SIMPLIFY_SUBTRACTIVE_RULE;
		
		File[] dataSets = {new File("/Users/wonderalexandre/Downloads/train_images")};
						   //new File("/home/gobber/Datasets/ICDAR-2019/test_part1_images")};
		
		for(File dataset : dataSets) {			
			File[] files = dataset.listFiles();
			Arrays.sort(files);
			for(File file : files) {
				if(file.getName().equals("gt_0.jpg")||file.getName().equals("gt_1401.jpg") || file.getName().equals("gt_2136.jpg")) {
					System.out.println("Skiping image: " + file.getName());
					continue;
				}
				System.setOut(console);
				System.out.println("Processing image: " + file.getName());
				System.setOut(o);
				System.out.println("--------------------------------------------");
				System.out.println("Processing image: " + file.getName());
				System.out.println("--------------------------------------------");
				GrayScaleImage imgInput = ImageBuilder.openGrayImage(file);
				tree = new ConnectedFilteringByComponentTree(imgInput, AdjacencyRelation.getAdjacency4(), true);
				tree.loadAttribute(type);
				for(double threshold: thresholds) {
					System.out.println("Threshold: " + threshold);					
					TestAttributes.run(imgInput, threshold, type, strategy);
					System.out.println("--------------------------------------------");
				}
			}
		}
		System.out.println("Finished!");
		o.close();
				
	}
}
