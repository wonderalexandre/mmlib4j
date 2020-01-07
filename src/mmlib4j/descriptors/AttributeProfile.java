package mmlib4j.descriptors;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.utils.AdjacencyRelation;

public class AttributeProfile {

	public static GrayScaleImage[] getAttributeProfile(GrayScaleImage img, int attributeType, double thresholds[]) {
		return getAttributeProfile(img, attributeType, thresholds, MorphologicalTreeFiltering.PRUNING_MIN);
	}
	
	public static GrayScaleImage[] getAttributeProfile(GrayScaleImage img, int attributeType, double thresholds[], int typeStrategy) {
		int lambdas = thresholds.length;
		GrayScaleImage[] profiles = new GrayScaleImage[2 * lambdas + 1];
		ConnectedFilteringByComponentTree tree;
		tree = new ConnectedFilteringByComponentTree(img, AdjacencyRelation.getAdjacency8(), false);

		for (int i=lambdas-1; i >= 0; i--) {
			//System.out.println("mintree["+i+", "+Attribute.getNameAttribute(attributeType)+"]: " + thresholds[i]);
			if(typeStrategy == MorphologicalTreeFiltering.PRUNING_MIN)
				profiles[i] = tree.filteringByPruningMin(thresholds[i], attributeType);
			else if(typeStrategy == MorphologicalTreeFiltering.PRUNING_MAX)
				profiles[i] = tree.filteringByPruningMax(thresholds[i], attributeType);
			else if(typeStrategy == MorphologicalTreeFiltering.PRUNING_VITERBI)
				profiles[i] = tree.filteringByPruningViterbi(thresholds[i], attributeType);
			else if(typeStrategy == MorphologicalTreeFiltering.DIRECT_RULE)
				profiles[i] = tree.filteringByDirectRule(thresholds[i], attributeType);
			if(typeStrategy == MorphologicalTreeFiltering.SUBTRACTIVE_RULE)
				profiles[i] = tree.filteringBySubtractiveRule(thresholds[i], attributeType);
			
			
		}

		profiles[lambdas] = img;
		tree = new ConnectedFilteringByComponentTree(img, AdjacencyRelation.getAdjacency8(), true);
		
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

	
}
