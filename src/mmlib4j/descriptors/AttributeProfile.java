package mmlib4j.descriptors;

import java.io.File;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;

public class AttributeProfile {
	
	public static ConnectedFilteringByComponentTree tree;

	public static GrayScaleImage[] getAttributeProfile(GrayScaleImage img, int attributeType, double thresholds[]) {
		return getAttributeProfile(img, attributeType, thresholds, MorphologicalTreeFiltering.PRUNING_MIN);
	}
	
	public static GrayScaleImage[] getAttributeProfile(GrayScaleImage img, int attributeType, double thresholds[], int typeStrategy) {
		int lambdas = thresholds.length;
		GrayScaleImage[] profiles = new GrayScaleImage[2 * lambdas + 1];
		//ConnectedFilteringByComponentTree tree;
		tree = new ConnectedFilteringByComponentTree(img, AdjacencyRelation.getAdjacency4(), false);
		tree.loadAttribute(attributeType);				
		
		for (int i = 0; i < lambdas; i++) {
			//System.out.println("mintree["+i+", "+Attribute.getNameAttribute(attributeType)+"]: " + thresholds[i]);
			if(typeStrategy == MorphologicalTreeFiltering.PRUNING_MIN)
				profiles[lambdas-i-1] = tree.filteringByPruningMin(thresholds[i], attributeType);
			else if(typeStrategy == MorphologicalTreeFiltering.PRUNING_MAX)
				profiles[lambdas-i-1] = tree.filteringByPruningMax(thresholds[i], attributeType);
			else if(typeStrategy == MorphologicalTreeFiltering.PRUNING_VITERBI)
				profiles[lambdas-i-1] = tree.filteringByPruningViterbi(thresholds[i], attributeType);
			else if(typeStrategy == MorphologicalTreeFiltering.DIRECT_RULE)
				profiles[lambdas-i-1] = tree.filteringByDirectRule(thresholds[i], attributeType);
			if(typeStrategy == MorphologicalTreeFiltering.SUBTRACTIVE_RULE)
				profiles[lambdas-i-1] = tree.filteringBySubtractiveRule(thresholds[i], attributeType);
			
			
		}

		profiles[lambdas] = img;
		tree = new ConnectedFilteringByComponentTree(img, AdjacencyRelation.getAdjacency4(), true);
		tree.loadAttribute(attributeType);				
		
		for (int i = 0; i < lambdas; i++) {
			//System.out.println("maxtree["+i+", "+Attribute.getNameAttribute(attributeType)+"]: " + thresholds[i]);
			if(typeStrategy == MorphologicalTreeFiltering.PRUNING_MIN)
				profiles[i+lambdas+1] = tree.filteringByPruningMin(thresholds[i], attributeType);
			else if(typeStrategy == MorphologicalTreeFiltering.PRUNING_MAX)
				profiles[i+lambdas+1] = tree.filteringByPruningMax(thresholds[i], attributeType);
			else if(typeStrategy == MorphologicalTreeFiltering.PRUNING_VITERBI)
				profiles[i+lambdas+1] = tree.filteringByPruningViterbi(thresholds[i], attributeType);
			else if(typeStrategy == MorphologicalTreeFiltering.DIRECT_RULE)
				profiles[i+lambdas+1] = tree.filteringByDirectRule(thresholds[i], attributeType);
			if(typeStrategy == MorphologicalTreeFiltering.SUBTRACTIVE_RULE)
				profiles[i+lambdas+1] = tree.filteringBySubtractiveRule(thresholds[i], attributeType);
			
		}

		return profiles;

	}
	
	public static void main(String args[]) {
		
		GrayScaleImage imgInput  = ImageBuilder.openGrayImage(new File("/Users/gobber/Desktop/lena.jpg"));		
		
		GrayScaleImage[] profiles = AttributeProfile.getAttributeProfile(imgInput, 
																		 Attribute.MOMENT_OF_INERTIA, 
																		 new double[] {0.2},
																		 MorphologicalTreeFiltering.SUBTRACTIVE_RULE);
		
		ConnectedFilteringByComponentTree tree2 = new ConnectedFilteringByComponentTree(profiles[profiles.length-1], 
																						AdjacencyRelation.getAdjacency4(), 
																						true);
		tree2.loadAttribute(Attribute.MOMENT_OF_INERTIA);
		
		System.out.println(AttributeProfile.tree.getNumNode());
		System.out.println(tree2.getNumNode());						
		
		System.out.println();
		
		int i = 0;
		
		for(NodeLevelSets child : AttributeProfile.tree.getListNodes()) {			
			for(Integer att: child.getAttributes().keySet()) {
				System.out.print(Attribute.getNameAttribute(att) + ": " + child.getAttributeValue(att)+" ");
			}
			System.out.println();
			if (i > 20)
				break;
			i++;
		}
		
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
		
		//WindowImages.show(profiles[profiles.length-1]);
		
	}
}
