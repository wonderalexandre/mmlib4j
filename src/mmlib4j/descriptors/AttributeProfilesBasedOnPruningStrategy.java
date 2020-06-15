package mmlib4j.descriptors;

import mmlib4j.filtering.AttributeFilters;
import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedExtinctionValue;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedGradualTransition;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedMSER;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedTBMR;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;

public class AttributeProfilesBasedOnPruningStrategy {
			
	public final static int PRUNING_BASED_ON_MSER = 1;
	public final static int PRUNING_BASED_ON_MSER_ADAPTER = 2;
	public final static int PRUNING_BASED_ON_GRADUAL_TRANSITION = 3;
	public final static int PRUNING_BASED_ON_TBMR = 4;
	public final static int PRUNING_BASED_ON_EXTINCTION_VALUE = 5;
	
	
	public static GrayScaleImage[] getAttributeProfile(GrayScaleImage img, int attributeType, double thresholds[]) {
		return getAttributeProfile(img, attributeType, thresholds, AttributeFilters.PRUNING_MIN);
	}
	
	public static FilteringStrategyBasedOnPruning getStrategy(int typeStrategy) {
		switch (typeStrategy) {
		case PRUNING_BASED_ON_MSER:
			return new FilteringStrategyBasedOnPruning() {				
				@Override
				public GrayScaleImage filterBy(MorphologicalTree tree, int attributeType, double attributeValue) {
					return new PruningBasedMSER(tree, attributeType, 5).getPrunedTree(attributeValue).reconstruction();
				}
			};
		case PRUNING_BASED_ON_MSER_ADAPTER:
			return new FilteringStrategyBasedOnPruning() {				
				@Override
				public GrayScaleImage filterBy(MorphologicalTree tree, int attributeType, double attributeValue) {
					int delta = 5;
					return new PruningBasedMSER(tree, attributeType, delta).getPrunedTreeByAdaptativeThreshold(attributeValue).reconstruction();
				}
			};
		case PRUNING_BASED_ON_GRADUAL_TRANSITION:
			return new FilteringStrategyBasedOnPruning() {				
				@Override
				public GrayScaleImage filterBy(MorphologicalTree tree, int attributeType, double attributeValue) {
					int delta = 5;
					return new PruningBasedGradualTransition(tree, attributeType, delta).getPrunedTree(attributeValue).reconstruction();
				}
			};
		case PRUNING_BASED_ON_TBMR:
			return new FilteringStrategyBasedOnPruning() {				
				@Override
				public GrayScaleImage filterBy(MorphologicalTree tree, int attributeType, double attributeValue) {
					return new PruningBasedTBMR(tree, 0, tree.getInputImage().getSize()).getPrunedTree(attributeValue).reconstruction();
				}
			};
		case PRUNING_BASED_ON_EXTINCTION_VALUE:
			return new FilteringStrategyBasedOnPruning() {				
				@Override
				public GrayScaleImage filterBy(MorphologicalTree tree, int attributeType, double attributeValue) {
					return new PruningBasedExtinctionValue(tree, attributeType).getPrunedTree(attributeValue).reconstruction();
				}
			};
		default:
			return null;
		}
	}
	
	public static String[] getLabels(int attributeType, double thresholds[], int typeStrategy) {
		int lambdas = thresholds.length;
		String[] labels = new String[2 * lambdas + 1];
		
		for (int i = 0; i < lambdas; i++) {
			labels[lambdas-i-1] = Attribute.getNameAttribute(attributeType)+ ": "+ thresholds[i];								
		}
		
		labels[lambdas] = "input";
		
		for (int i = 0; i < lambdas; i++) {
			labels[i+lambdas+1] = Attribute.getNameAttribute(attributeType)+ ": "+ thresholds[i];							
		}
		
		return labels;
	}

	public static GrayScaleImage[] getAttributeProfile(GrayScaleImage img, int attributeType, double thresholds[], int typeStrategy) {
		
		int lambdas = thresholds.length;
		GrayScaleImage[] profiles = new GrayScaleImage[2 * lambdas + 1];
		FilteringStrategyBasedOnPruning strategy = getStrategy(typeStrategy);		
		
		ComponentTree tree;		
		tree = new ComponentTree(img, AdjacencyRelation.getAdjacency8(), false);
		
		for (int i = 0; i < lambdas; i++) {
			profiles[lambdas-i-1] = strategy.filterBy(tree, attributeType, thresholds[i]);		
		}

		profiles[lambdas] = img;
		tree = new ComponentTree(img, AdjacencyRelation.getAdjacency8(), true);		
		
		for (int i = 0; i < lambdas; i++) {
			profiles[i+lambdas+1] = strategy.filterBy(tree, attributeType, thresholds[i]);
		} 
		
		return profiles;
		
	}
	
	private interface FilteringStrategyBasedOnPruning {	
		public GrayScaleImage filterBy(MorphologicalTree tree, int attributeType, double attributeValue);
	}
	
	public static void main(String args[]) {
		
		GrayScaleImage imgInput  = ImageBuilder.openGrayImage();
		//GrayScaleImage imgInput  = ImageBuilder.openGrayImage(new File("/Users/gobber/Desktop/Images/lena.jpg"));
		int attributeType = Attribute.AREA;
		double thresholds[] = new double[] {100, 500, 1000, 5000};;
		int typeStrategy = AttributeProfilesBasedOnPruningStrategy.PRUNING_BASED_ON_EXTINCTION_VALUE;
		
		GrayScaleImage[] profiles = AttributeProfilesBasedOnPruningStrategy.getAttributeProfile(imgInput, attributeType, thresholds, typeStrategy);		
		String labels[] = AttributeProfilesBasedOnPruningStrategy.getLabels(attributeType, thresholds, typeStrategy);		
		
		WindowImages.show(profiles, labels);
		
	}
	
}
