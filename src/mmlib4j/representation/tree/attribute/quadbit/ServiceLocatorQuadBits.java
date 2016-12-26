package mmlib4j.representation.tree.attribute.quadbit;

import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.attribute.quadbit.maxtree.QuadBitFactoryMaxtree;
import mmlib4j.representation.tree.attribute.quadbit.mintree.QuadBitFactoryMintree;
import mmlib4j.representation.tree.attribute.quadbit.tos.QuadBitFactoryFactoryTreeOfShapes;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;

public class ServiceLocatorQuadBits {
	
	private ServiceLocatorQuadBits()
	{}
	
	private static ServiceLocatorQuadBits instance;
	
	public static ServiceLocatorQuadBits getSingleton() {
		if (instance == null) 
			instance = new ServiceLocatorQuadBits();			
		return instance;
	}
	
	public QuadBitFactory findQuadBitFactory(MorphologicalTreeFiltering tree) {
		if (tree instanceof ConnectedFilteringByComponentTree) {
			ConnectedFilteringByComponentTree ctree = (ConnectedFilteringByComponentTree)tree;
			if (ctree.isMaxtree())
				return new QuadBitFactoryMaxtree(tree); // maxtree factory
			return new QuadBitFactoryMintree(tree); // mintree factory
		}
		return new QuadBitFactoryFactoryTreeOfShapes((ConnectedFilteringByTreeOfShape)tree); //Tree of Shapes factory
	}
}