package mmlib4j.descriptors;

import java.io.File;

import mmlib4j.filtering.AttributeFilters;
import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.ComputerFunctionalVariational;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;

public class AttributeProfiles {
			
	public final static int ENERGY = 10; 
	public static FilteringStrategy strategy;
	
	
	public static GrayScaleImage[] getAttributeProfile(GrayScaleImage img, int attributeType, double thresholds[]) {
		return getAttributeProfile(img, attributeType, thresholds, MorphologicalTreeFiltering.PRUNING_MIN);
	}
	
	public static FilteringStrategy getStrategy(int typeStrategy) {
		switch (typeStrategy) {
		case AttributeFilters.PRUNING_MIN:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(AttributeFilters filter, double threshold, int attributeType) {
					return filter.filteringByPruningMin(threshold, attributeType);
				}
			};
		case AttributeFilters.PRUNING_MAX:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(AttributeFilters filter, double threshold, int attributeType) {
					return filter.filteringByPruningMax(threshold, attributeType);
				}
			};
		case AttributeFilters.PRUNING_VITERBI:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(AttributeFilters filter, double threshold, int attributeType) {
					return filter.filteringByPruningViterbi(threshold, attributeType);
				}
			};
		case AttributeFilters.DIRECT_RULE:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(AttributeFilters filter, double threshold, int attributeType) {
					return filter.filteringByDirectRule(threshold, attributeType);
				}
			};
		case AttributeFilters.SUBTRACTIVE_RULE:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(AttributeFilters filter, double threshold, int attributeType) {
					return filter.filteringBySubtractiveRule(threshold, attributeType);
				}
			};	
		case ENERGY:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(AttributeFilters filter, double threshold, int attributeType) {
					ComputerFunctionalVariational compFV = new ComputerFunctionalVariational((ComponentTree)filter.getTree(), threshold, true);
					return compFV.getSimplifiedImage();
				}
			};
			
		case AttributeFilters.SIMPLIFY_MIN:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(AttributeFilters filter, double threshold, int attributeType) {
					filter.simplificationTreeByPruningMin(threshold, attributeType);
					return filter.getTree().reconstruction();
				}
			};
			
		case AttributeFilters.SIMPLIFY_MAX:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(AttributeFilters filter, double threshold, int attributeType) {
					filter.simplificationTreeByPruningMax(threshold, attributeType);
					return filter.getTree().reconstruction();
				}
			};
			
		case AttributeFilters.SIMPLIFY_DIRECT:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(AttributeFilters filter, double threshold, int attributeType) {
					filter.simplificationTreeByDirectRule(threshold, attributeType);
					return filter.getTree().reconstruction();
				}
			};
		case AttributeFilters.SIMPLIFY_SUBTRACTIVE:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(AttributeFilters filter, double threshold, int attributeType) {
					filter.simplificationTreeBySubstractiveRule(threshold, attributeType);
					return filter.getTree().reconstruction();
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
		strategy = getStrategy(typeStrategy);		
		
		ComponentTree tree;
		AttributeFilters filter;		
		tree = new ComponentTree(img, AdjacencyRelation.getAdjacency8(), false);
		filter = new AttributeFilters(tree);
		filter.loadAttribute(attributeType);				
		
		for (int i = 0; i < lambdas; i++) {
			profiles[lambdas-i-1] = strategy.filterBy(filter, thresholds[i], attributeType);		
		}

		profiles[lambdas] = img;
		tree = new ComponentTree(img, AdjacencyRelation.getAdjacency8(), true);		
		filter = new AttributeFilters(tree);
		filter.loadAttribute(attributeType);				
		
		for (int i = 0; i < lambdas; i++) {
			profiles[i+lambdas+1] = strategy.filterBy(filter, thresholds[i], attributeType);
		} 
		
		return profiles;
		
	}
	
	private interface FilteringStrategy {	
		public GrayScaleImage filterBy(AttributeFilters tree, double threshold, int attributeType);
	}
	
	public static void main(String args[]) {
		
		//GrayScaleImage imgInput  = ImageBuilder.openGrayImage();
		GrayScaleImage imgInput  = ImageBuilder.openGrayImage(new File("/Users/gobber/Desktop/Images/lena.jpg"));
		int attributeType = Attribute.STD_LEVEL;
		double thresholds[] = new double[] {2, 3, 4};
		int typeStrategy = AttributeFilters.SIMPLIFY_SUBTRACTIVE;
		
		GrayScaleImage[] profiles = AttributeProfiles.getAttributeProfile(imgInput, attributeType, thresholds, typeStrategy);		
		String labels[] = AttributeProfiles.getLabels(attributeType, thresholds, typeStrategy);		
		
		WindowImages.show(profiles, labels);
		
	}
	
}
