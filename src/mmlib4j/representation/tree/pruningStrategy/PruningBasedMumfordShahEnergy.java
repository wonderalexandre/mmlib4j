package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;

public class PruningBasedMumfordShahEnergy implements MappingStrategyOfPruning {
	
	private MorphologicalTreeFiltering tree;
	
	private int minArea = Integer.MIN_VALUE;
	
	private int maxArea = Integer.MAX_VALUE;
	
	private double maxEnergy = Double.MAX_VALUE;
	
	private double minEnergy = Double.MIN_VALUE;
	
	public PruningBasedMumfordShahEnergy( MorphologicalTreeFiltering tree, int minEnergy, int maxEnergy ) {
		
		this.tree = tree;
		
		tree.loadAttribute( Attribute.MUMFORD_SHA_ENERGY );
		
		this.minEnergy = minEnergy;
		
		this.maxEnergy = maxEnergy;
		
	}
	
	public PruningBasedMumfordShahEnergy( MorphologicalTreeFiltering tree, int minEnergy, boolean useHeuristic ) {
		
		this.tree = tree;
		
		( ( ConnectedFilteringByComponentTree ) tree ).computerXuAttribute( useHeuristic );
		
		this.minEnergy = minEnergy;
		
	}
	
	public void setParameters( int minArea, int maxArea ) {
		
		this.minArea = minArea;
		
		this.maxArea = maxArea;
		
	}
	
	public void setMinEnergy( int minEnergy ) {
		
		this.minEnergy = minEnergy; 
		
	}

	public void setMinArea( int minArea ) {
		
		this.minArea = minArea; 
		
	}
	
	@Override
	public boolean[] getMappingSelectedNodes() {		
		
		boolean [] mumfordsha = new boolean[ tree.getNumNode() ];
		
		for( NodeCT node : ( ( ComponentTree ) tree ).getListNodes() ) {			
			
			double energy = node.getAttributeValue( Attribute.MUMFORD_SHA_ENERGY );
			
			double area = node.getAttributeValue( Attribute.AREA );
			
			if( minEnergy < energy && energy < maxEnergy && ( minArea < area && area < maxArea )  ) {							
				
				mumfordsha[ node.getId() ] = true;
				
			}
			
		}
		
		return mumfordsha;
		
	}

}
