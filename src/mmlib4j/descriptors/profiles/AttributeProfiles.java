package mmlib4j.descriptors.profiles;

import java.io.File;

import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.Image2D;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.images.impl.MmlibImageFactory;
import mmlib4j.representation.mergetree.InfoMergedTree.NodeMergedTree;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;

public class AttributeProfiles {
	
	public static ConnectedFilteringByComponentTree tree;
	
	public static FilteringStrategy strategy;
	
	public static GrayScaleImage[] getAttributeProfile(ImageFactory factory, GrayScaleImage img, int attributeType, double thresholds[]) {
		return getAttributeProfile(factory, img, attributeType, thresholds, MorphologicalTreeFiltering.PRUNING_MIN);
	}
	
	private static FilteringStrategy getStrategy(int typeStrategy) {
		switch (typeStrategy) {
		case MorphologicalTreeFiltering.PRUNING_MIN:
			return PruningByMin.instance;
		case MorphologicalTreeFiltering.PRUNING_MAX:
			return PruningByMax.instance;
		case MorphologicalTreeFiltering.PRUNING_VITERBI:
			return PruningByViterbi.instance;
		case MorphologicalTreeFiltering.DIRECT_RULE:
			return DirectRule.instance;
		case MorphologicalTreeFiltering.SUBTRACTIVE_RULE:
			return SubtractiveRule.instance;	
		default:			
			return null;
		}	
	}
	
	public static GrayScaleImage[] getAttributeProfile(ImageFactory factory, GrayScaleImage img, int attributeType, double thresholds[], int typeStrategy) {
		
		ImageFactory.instance = factory;
		int lambdas = thresholds.length;
		GrayScaleImage[] profiles = new GrayScaleImage[2 * lambdas + 1];
				
		tree = new ConnectedFilteringByComponentTree(img, AdjacencyRelation.getAdjacency4(), false);
		tree.loadAttribute(attributeType);
		
		strategy = getStrategy(typeStrategy);
		
		for (int i = 0; i < lambdas; i++) {
			profiles[lambdas-i-1] = strategy.filterBy(tree, thresholds[i], attributeType);								
		}

		profiles[lambdas] = img;
		tree = new ConnectedFilteringByComponentTree(img, AdjacencyRelation.getAdjacency4(), true);
		tree.loadAttribute(attributeType);				
		
		for (int i = 0; i < lambdas; i++) {
			profiles[i+lambdas+1] = strategy.filterBy(tree, thresholds[i], attributeType);							
		}
		
		return profiles;
		
	}
	
	public static void main(String args[]) {
		
		GrayScaleImage imgInput  = ImageBuilder.openGrayImage(new File("/Users/gobber/Desktop/lena.jpg"));		
		
		GrayScaleImage[] profiles = AttributeProfiles.getAttributeProfile(MmlibImageFactory.instance, 
																		 imgInput, 
																		 Attribute.MOMENT_OF_INERTIA, 
																		 new double[] {0.4, 0.7, 0.9},
																		 //new double[] {100, 1000},
																		 MorphologicalTreeFiltering.SUBTRACTIVE_RULE);
		
		ConnectedFilteringByComponentTree tree2 = new ConnectedFilteringByComponentTree(profiles[profiles.length-1], 
																						AdjacencyRelation.getAdjacency4(), 
																						true);
		
		WindowImages.show(new Image2D[]{profiles[profiles.length-1], AttributeProfiles.tree.reconstruction()});		
		
		tree2.loadAttribute(Attribute.MOMENT_OF_INERTIA);		
		System.out.println(AttributeProfiles.tree.getMtree().getNumNode());
		//System.out.println(AttributeProfile.tree.getNumNode());
		System.out.println(tree2.getNumNode());						
		
		System.out.println();		
		int i = 0;
		
		for(NodeMergedTree child_ : AttributeProfiles.tree.getMtree()) {			
			for(Integer att: child_.getAttributes().keySet()) {
				System.out.print(Attribute.getNameAttribute(att) + ": " + child_.getAttributeValue(att)+" ");
			}
			System.out.println();
			if (i > 20)
				break;
			i++;
		}
		
		/*for(NodeLevelSets child_ : AttributeProfiles.tree.getListNodes()) {			
			for(Integer att: child_.getAttributes().keySet()) {
				System.out.print(Attribute.getNameAttribute(att) + ": " + child_.getAttributeValue(att)+" ");
			}
			System.out.println();
			if (i > 20)
				break;
			i++;
		}*/
		
		System.out.println();
		System.out.println();
		 i = 0;
		for(NodeLevelSets child : tree2.getListNodes()) {
			for(Integer att: child.getAttributes().keySet()) {
				System.out.print(Attribute.getNameAttribute(att) + ": " + child.getAttributeValue(att)+" ");
			}
			System.out.println();
			if (i > 20)
				break;
			i++;
		}	
		
	}
	
}
