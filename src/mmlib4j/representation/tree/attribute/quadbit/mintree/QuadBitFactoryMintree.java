package mmlib4j.representation.tree.attribute.quadbit.mintree;

import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.attribute.quadbit.NotProperSubsetQuadBit;
import mmlib4j.representation.tree.attribute.quadbit.NotSubsetQuadBit;
import mmlib4j.representation.tree.attribute.quadbit.ProperSubsetQuadBit;
import mmlib4j.representation.tree.attribute.quadbit.QuadBitFactory;
import mmlib4j.representation.tree.attribute.quadbit.SubsetQuadBit;


public class QuadBitFactoryMintree implements QuadBitFactory{

	private MorphologicalTreeFiltering tree;
	
	public QuadBitFactoryMintree(MorphologicalTreeFiltering tree) {
		this.tree = tree;
	}
	
	@Override
	public SubsetQuadBit createSubsetQuadBit(int dx, int dy) {
		return new SubsetQuadBitMintree(tree.getInputImage(), dx, dy);
	}

	@Override
	public ProperSubsetQuadBit createProperSubsetQuadBit(int dx, int dy) {
		return new ProperSubsetQuadBitMintree(tree.getInputImage(), dx, dy);
	}

	@Override
	public NotSubsetQuadBit createNotSubsetQuadBit(int dx, int dy) {
		return new NotSubsetQuadBitMintree(tree.getInputImage(), dx, dy);
	}

	@Override
	public NotProperSubsetQuadBit createNotProperSubsetQuadBit(int dx, int dy) {
		return new NotProperSubsetQuadBitMintree(tree.getInputImage(), dx, dy);
	}
}
