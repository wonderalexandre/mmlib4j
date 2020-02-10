package mmlib4j.descriptors;

import java.io.File;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.AbstractImageFactory;
import mmlib4j.images.impl.MmlibImageFactory;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.InfoMergedTree.NodeMergedTree;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.ComputerFunctionalVariational;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;

public class AttributeProfiles {
			
	public final static int ENERGY = 10; 
	public final static int SIMPLIFY_DIRECT_RULE = 11;
	public final static int SIMPLIFY_SUBTRACTIVE_RULE = 12;
	public static ConnectedFilteringByComponentTree tree;	
	public static FilteringStrategy strategy;
	
	public static GrayScaleImage[] getAttributeProfile(AbstractImageFactory factory, GrayScaleImage img, int attributeType, double thresholds[]) {
		return getAttributeProfile(factory, img, attributeType, thresholds, MorphologicalTreeFiltering.PRUNING_MIN);
	}
	
	private static FilteringStrategy getStrategy(int typeStrategy) {
		switch (typeStrategy) {
		case MorphologicalTreeFiltering.PRUNING_MIN:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(ConnectedFilteringByComponentTree tree, double threshold, int attributeType) {
					return tree.filteringByPruningMin(threshold, attributeType);
				}
			};
		case MorphologicalTreeFiltering.PRUNING_MAX:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(ConnectedFilteringByComponentTree tree, double threshold, int attributeType) {
					return tree.filteringByPruningMax(threshold, attributeType);
				}
			};
		case MorphologicalTreeFiltering.PRUNING_VITERBI:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(ConnectedFilteringByComponentTree tree, double threshold, int attributeType) {
					return tree.filteringByPruningViterbi(threshold, attributeType);
				}
			};
		case MorphologicalTreeFiltering.DIRECT_RULE:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(ConnectedFilteringByComponentTree tree, double threshold, int attributeType) {
					return tree.filteringByDirectRule(threshold, attributeType);
				}
			};
		case MorphologicalTreeFiltering.SUBTRACTIVE_RULE:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(ConnectedFilteringByComponentTree tree, double threshold, int attributeType) {
					return tree.filteringBySubtractiveRule(threshold, attributeType);
				}
			};	
		case ENERGY:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(ConnectedFilteringByComponentTree tree, double threshold, int attributeType) {
					ComputerFunctionalVariational compFV = new ComputerFunctionalVariational(tree, threshold, true);
					return compFV.getSimplifiedImage();
				}
			};
		case SIMPLIFY_DIRECT_RULE:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(ConnectedFilteringByComponentTree tree, double threshold, int attributeType) {
					tree.simplificationTreeByDirectRule(threshold, attributeType);
					return tree.reconstruction();
				}
			};
		case SIMPLIFY_SUBTRACTIVE_RULE:
			return new FilteringStrategy() {				
				@Override
				public GrayScaleImage filterBy(ConnectedFilteringByComponentTree tree, double threshold, int attributeType) {
					tree.simplificationTreeBySubstractiveRule(threshold, attributeType);
					return tree.reconstruction();
				}
			};						
		default:			
			return null;
		}	
	}
	
	public static GrayScaleImage[] getAttributeProfile(AbstractImageFactory factory, GrayScaleImage img, int attributeType, double thresholds[], int typeStrategy) {
		
		AbstractImageFactory.instance = factory;
		int lambdas = thresholds.length;
		GrayScaleImage[] profiles = new GrayScaleImage[2 * lambdas + 1];
		strategy = getStrategy(typeStrategy);		
		
		tree = new ConnectedFilteringByComponentTree(img, AdjacencyRelation.getAdjacency4(), false);
		tree.loadAttribute(attributeType);				
		
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
	
	public interface FilteringStrategy {	
		public GrayScaleImage filterBy(ConnectedFilteringByComponentTree tree, double threshold, int attributeType);
	}
	
	public static void main(String args[]) {
		
		GrayScaleImage imgInput  = ImageBuilder.openGrayImage(new File("/Users/gobber/Desktop/img_teste_2.png"));
		int type = Attribute.MOMENT_OF_INERTIA;
		
		GrayScaleImage[] profiles = AttributeProfiles.getAttributeProfile(MmlibImageFactory.instance, 
																		 imgInput, 
																		 type, 
																		 new double[] {0.4},
																		 SIMPLIFY_SUBTRACTIVE_RULE);
		
		ConnectedFilteringByComponentTree tree2 = new ConnectedFilteringByComponentTree(profiles[profiles.length-1], 
																						AdjacencyRelation.getAdjacency4(), 
																						true);
		tree2.loadAttribute(type);
		
		//WindowImages.show(new Image2D[]{profiles[profiles.length-1], AttributeProfiles.tree.reconstruction()});		
		
		System.out.println("Nós árvore original: " + AttributeProfiles.tree.getNumNode());
		System.out.println("Nós nova árvore: " + tree2.getNumNode());
		
		for(NodeLevelSets node : AttributeProfiles.tree.getListNodes()) {
			NodeLevelSets node2 = tree2.getMap()[node.getCanonicalPixel()];
			for(Integer att: node.getAttributes().keySet()) {
				if(node.getAttributeValue(att) != node2.getAttributeValue(att)) {
					System.out.println(Attribute.getNameAttribute(att));
				}
			}
		}
		
		/*System.out.println();		
		int i = 0;		
		for(NodeMergedTree child_ : AttributeProfiles.tree.getMtree()) {			
			for(Integer att: child_.getInfo().getAttributes().keySet()) {
				System.out.print(Attribute.getNameAttribute(att) + ": " + AttributeProfiles.tree.getMtree().getAttribute(child_, att)+" ");
			}
			System.out.println();
			if (i > 20)
				break;
			i++;
		}
		
		System.out.println();
		tree2.loadAttribute(type);
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
		
		/*int numEquals = 0;
		int attToCompare[] = {Attribute.AREA, 
							  Attribute.VOLUME, 
							  Attribute.LEVEL_MEAN,
							  Attribute.LEVEL,
							  Attribute.RECTANGULARITY,
							  Attribute.VARIANCE_LEVEL,
							  Attribute.STD_LEVEL,
							  Attribute.MOMENT_OF_INERTIA,
							  Attribute.MOMENT_CENTRAL_02,
							  Attribute.MOMENT_CENTRAL_11,
							  Attribute.MOMENT_CENTRAL_20};
		
		boolean[] nodesMarked = new boolean[AttributeProfiles.tree.getNumNodeIdMax()];
		for(NodeLevelSets node : tree2.getListNodes()) {			
			int equal;
			for(NodeMergedTree node_ : AttributeProfiles.tree.getMtree()) {
				equal = 0;
				if(nodesMarked[node_.getId()])
					continue;
				for(Integer att: node.getAttributes().keySet()) {
					if(node.getAttributeValue(att) == AttributeProfiles.tree.getMtree().getAttribute(node_, att)) {						
						equal++;
					}
				}
				if(equal == node.getAttributes().keySet().size()) {
					nodesMarked[node_.getId()] = true;
					numEquals++;
				}
			}			
		}
		System.out.println();
		System.out.println("Equals attributes? : " + (numEquals == tree2.getNumNode()));*/
		
	}
	
}
