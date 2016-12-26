package mmlib4j.representation.tree.attribute.quadbit;

public abstract class QuadBitTreeBased {
	protected int dx;
	protected int dy;
	
	public QuadBitTreeBased(int dx, int dy) {
		this.dx = dx;
		this.dy = dy;
	}
	
	public abstract boolean match(int px, int py);
}