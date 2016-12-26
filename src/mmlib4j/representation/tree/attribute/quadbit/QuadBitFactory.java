package mmlib4j.representation.tree.attribute.quadbit;

public interface QuadBitFactory {
	SubsetQuadBit createSubsetQuadBit(int px, int py);
	ProperSubsetQuadBit createProperSubsetQuadBit(int px, int py);
	NotSubsetQuadBit createNotSubsetQuadBit(int px, int py);
	NotProperSubsetQuadBit createNotProperSubsetQuadBit(int px, int py);
}