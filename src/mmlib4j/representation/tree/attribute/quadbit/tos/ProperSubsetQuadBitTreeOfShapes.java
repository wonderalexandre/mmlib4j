package mmlib4j.representation.tree.attribute.quadbit.tos;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.attribute.quadbit.ProperSubsetQuadBit;
import mmlib4j.representation.tree.attribute.quadbit.tos.ancestorship.Ancestorship;
import mmlib4j.representation.tree.attribute.quadbit.tos.ancestorship.Ancestorship4Connected;
import mmlib4j.representation.tree.attribute.quadbit.tos.ancestorship.AncestorshipDiagonal;
import mmlib4j.representation.tree.tos.BuilderTreeOfShape;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;
import mmlib4j.representation.tree.tos.TreeOfShape;
import mmlib4j.utils.AdjacencyRelation;

public class ProperSubsetQuadBitTreeOfShapes extends ProperSubsetQuadBit {

	private BuilderTreeOfShape tosBuilder;
	private TreeOfShape tos;
	private GrayScaleImage image;
	private short[] imageU;
	private AdjacencyRelation adj4;
	private Ancestorship ancesorshipVerificator;
	
	
	public ProperSubsetQuadBitTreeOfShapes(ConnectedFilteringByTreeOfShape tos, int px, int py) {
		super(px, py);
		this.tos = tos;
		this.tosBuilder = tos.getBuilder();
		this.image = tos.getInputImage();
		this.imageU = this.tosBuilder.getImageU();
		this.adj4 = AdjacencyRelation.getAdjacency8();
		
		if (isConnectedByDiagonal(px, py))
			this.ancesorshipVerificator = new AncestorshipDiagonal(image, tos, px, 0, 0, py);
		else
			this.ancesorshipVerificator = new Ancestorship4Connected(image, tos);
	}
	
	private boolean isConnectedByDiagonal(int dx, int dy) {
		return Math.abs(dx) == Math.abs(dy); 
	}
	
	@Override
	public boolean match(int px, int py) {
		int upx = 2*px+1;
		int upy = 2*py+1;
		
		int Uwidth = (2 * image.getWidth()) + 1;
		//int Uheight = (2* image.getHeight()) + 1;
		
		int qx = px + dx;
		int qy = py + dy;
		
		int uqx = qx * 2 + 1;
		int uqy = qy * 2 + 1;
				
		if (!image.isPixelValid(qx, qy))
			return false;
		
		int[] dx = adj4.getVectorX();
		int[] dy = adj4.getVectorY();
		
		//for (int i = 0; i < dx.length; i++) {
		//	int ux = uqx + dx[i];
		//	int uy = uqy + dy[i];
			
			int ux = (upx + uqx) / 2;
			int uy = (upy + uqy) / 2;
		
			
			// There is ancestor relationship between SC(p,T) and SC(q,T)
			if (ancesorshipVerificator.isThereAncestorRelation(px, py, qx, qy) && 
					tos.getSC(py * image.getWidth() + px).getDepth() < tos.getSC(qy * image.getWidth() + qx).getDepth()) {
				// SC(q,T) \subset SC(p,T)
				return true;
			}
		//}
		return false;
	}
	
	
}
