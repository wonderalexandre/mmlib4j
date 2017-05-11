package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;

public class PruningBasedMumfordShahEnergy implements MappingStrategyOfPruning {
	
	private MorphologicalTreeFiltering tree;
	
	private double minEnergy = 0;
	
	private double maxEnergy;
	
	public PruningBasedMumfordShahEnergy( MorphologicalTreeFiltering tree, int delta ) {
		
		this.tree = tree;
		
		tree.loadAttribute( Attribute.MUMFORD_SHA_ENERGY );
		
		double max = Double.MIN_VALUE;
		
		for( NodeCT node : ( ( ComponentTree ) tree ).getListNodes() ) {
			
			double energy = node.getAttributeValue( Attribute.MUMFORD_SHA_ENERGY );
			
			if( energy > max ) {
				
				max = energy;
				
			}
			
		}
		
		this.maxEnergy = delta * max / 50;
		
	}

	@Override
	public boolean[] getMappingSelectedNodes() {
		
		System.out.println( "in energy " + maxEnergy );
		
		boolean [] mumfordsha = new boolean[ tree.getNumNode() ];
		
		for( NodeCT node : ( ( ComponentTree ) tree ).getListNodes() ) {
			
			double energy = node.getAttributeValue( Attribute.MUMFORD_SHA_ENERGY );
			
			if( energy > maxEnergy ) {
				
				mumfordsha[ node.getId() ] = true;
				
			}
			
			/*if( ( energy > minEnergy ) && ( energy < maxEnergy ) ) {
				
				mumfordsha[ node.getId() ] = true;
				
			}*/
			
		}
		
		return mumfordsha;
		
	}

}
