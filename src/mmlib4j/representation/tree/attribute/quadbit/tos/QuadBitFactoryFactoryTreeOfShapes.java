package mmlib4j.representation.tree.attribute.quadbit.tos;

import mmlib4j.representation.tree.attribute.quadbit.NotProperSubsetQuadBit;
import mmlib4j.representation.tree.attribute.quadbit.NotSubsetQuadBit;
import mmlib4j.representation.tree.attribute.quadbit.ProperSubsetQuadBit;
import mmlib4j.representation.tree.attribute.quadbit.QuadBitFactory;
import mmlib4j.representation.tree.attribute.quadbit.SubsetQuadBit;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;

public class QuadBitFactoryFactoryTreeOfShapes implements QuadBitFactory{

	private ConnectedFilteringByTreeOfShape tos;
	
	public QuadBitFactoryFactoryTreeOfShapes(ConnectedFilteringByTreeOfShape tos) {
		this.tos = tos;
	}
	
	@Override
	public SubsetQuadBit createSubsetQuadBit(int px, int py) {
		return new SubsetQuadBitTreeOfShapes(tos, px, py);
	}

	@Override
	public ProperSubsetQuadBit createProperSubsetQuadBit(int px, int py) {
		return new ProperSubsetQuadBitTreeOfShapes(tos, px, py);
	}

	@Override
	public NotSubsetQuadBit createNotSubsetQuadBit(int px, int py) {
		return new NotSubsetQuadBitTreeOfShapes(tos, px, py);
	}

	@Override
	public NotProperSubsetQuadBit createNotProperSubsetQuadBit(int px, int py) {
		return new NotProperSubsetQuadBitTreeOfShapes(tos, px, py);
	}
}
