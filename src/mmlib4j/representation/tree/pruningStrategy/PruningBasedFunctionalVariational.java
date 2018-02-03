package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;

public class PruningBasedFunctionalVariational implements MappingStrategyOfPruning {
	
	private MorphologicalTreeFiltering tree;
	
	private double minEnergy = Double.MIN_VALUE;
	
	private double maxEnergy = Double.MAX_VALUE;
	
	private int minArea = Integer.MAX_VALUE;
	
	private int maxArea = Integer.MAX_VALUE;
	
	public PruningBasedFunctionalVariational( MorphologicalTreeFiltering tree, int minEnergy, double scale, boolean useHeuristic ) {
		
		this.tree = tree;
		
		( ( ConnectedFilteringByComponentTree ) tree ).computerFunctionalVariacionalAttribute(scale, useHeuristic);
		
		this.minEnergy = minEnergy;
		
	}
	
	public void setMaxEnergy( double maxEnergy ) {
		
		this.maxEnergy = maxEnergy; 
		
	}
	
	public void setMinEnergy( double minEnergy ) {
		
		this.minEnergy = minEnergy; 
		
	}
	
	public void setMinArea( int minArea ) {
		
		this.minArea = minArea; 
		
	}
	
	@Override
	public boolean[] getMappingSelectedNodes() {
	
		boolean [] functionalVariational = new boolean[ tree.getNumNode() ];
		
		for( NodeCT node : ( ( ComponentTree ) tree ).getListNodes() ) {			
			
			double energy = node.getAttributeValue( Attribute.FUNCTIONAL_VARIATIONAL );
			
			double area = node.getAttributeValue( Attribute.AREA );
			
			if( minEnergy < energy && energy < maxEnergy && ( minArea < area && area < maxArea )  ) {
				
				functionalVariational[ node.getId() ] = true;							
				
			}
			
			//System.out.println( "energy "+ energy );
			
		}
		
		return functionalVariational;
	}

}
