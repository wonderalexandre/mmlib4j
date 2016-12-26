package mmlib4j.representation.tree.attribute.quadbit.maxtree;

import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.attribute.quadbit.NotProperSubsetQuadBit;
import mmlib4j.representation.tree.attribute.quadbit.NotSubsetQuadBit;
import mmlib4j.representation.tree.attribute.quadbit.ProperSubsetQuadBit;
import mmlib4j.representation.tree.attribute.quadbit.QuadBitFactory;
import mmlib4j.representation.tree.attribute.quadbit.SubsetQuadBit;

public class QuadBitFactoryMaxtree implements QuadBitFactory{
	
	private MorphologicalTreeFiltering tree;
	
	public QuadBitFactoryMaxtree(MorphologicalTreeFiltering tree) {
		this.tree = tree;
	}
	
	@Override
	public SubsetQuadBit createSubsetQuadBit(int dx, int dy) {
		return new SubsetQuadBitMaxTree(tree.getInputImage(), dx, dy);
	}

	@Override
	public ProperSubsetQuadBit createProperSubsetQuadBit(int dx, int dy) {
		return new ProperSubsetQuadBitMaxtree(tree.getInputImage(), dx, dy);
	}

	@Override
	public NotSubsetQuadBit createNotSubsetQuadBit(int dx, int dy) {
		return new NotSubsetQuadBitMaxtree(tree.getInputImage(), dx, dy);
	}

	@Override
	public NotProperSubsetQuadBit createNotProperSubsetQuadBit(int dx, int dy) {
		return new NotProperSubsetQuadBitMaxtree(tree.getInputImage(), dx, dy);
	}

}
