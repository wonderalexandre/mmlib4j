package mmlib4j.representation.tree.attribute.quadbit.tos;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.attribute.quadbit.SubsetQuadBit;
import mmlib4j.representation.tree.attribute.quadbit.tos.ancestorship.Ancestorship;
import mmlib4j.representation.tree.attribute.quadbit.tos.ancestorship.Ancestorship4Connected;
import mmlib4j.representation.tree.attribute.quadbit.tos.ancestorship.AncestorshipDiagonal;
import mmlib4j.representation.tree.tos.BuilderTreeOfShape;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;
import mmlib4j.utils.AdjacencyRelation;

public class SubsetQuadBitTreeOfShapes extends SubsetQuadBit{
	private TreeOfShape tos;
	private GrayScaleImage image;

	public SubsetQuadBitTreeOfShapes(ConnectedFilteringByTreeOfShape tos, int px, int py) {
		super(px, py);
		this.tos = tos;
		this.image = tos.getInputImage();
	}
	
	@Override
	public boolean match(int px, int py) {	
		int qx = px + dx;
		int qy = py + dy;
		
		if (!image.isPixelValid(qx, qy)) 
			return false; 
		
		NodeToS scp = tos.getSC(py * image.getWidth() + px); 
		NodeToS scq = tos.getSC(qy * image.getWidth() + qx);
		
		if (scq.isAncestral(scp))
				return true;
			
		return false;
	}	
}