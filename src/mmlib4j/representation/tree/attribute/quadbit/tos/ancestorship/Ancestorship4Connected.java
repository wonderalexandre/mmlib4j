package mmlib4j.representation.tree.attribute.quadbit.tos.ancestorship;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.tos.TreeOfShape;

public class Ancestorship4Connected implements Ancestorship {

	private GrayScaleImage image;
	private short[] imageUb;
	private int Uwidth;
	
	public Ancestorship4Connected(GrayScaleImage image, TreeOfShape tos) {
		this.image = image;
		this.imageUb = tos.getBuilder().getImageU();
		this.Uwidth = image.getWidth() * 2 + 1;
	}
	
	@Override
	public boolean isThereAncestorRelation(int px, int py, int qx, int qy) {
		int upx = px * 2 + 1, upy = py * 2 + 1;
		int uqx = qx * 2 + 1, uqy = qy * 2 + 1;
		int urx = (upx + uqx) / 2, ury = (upy + uqy) / 2; 
		
		short urValue = imageUb[ury * Uwidth + urx];
		return urValue == image.getValue(px, py) || urValue == image.getValue(qx, qy);
	}
}
