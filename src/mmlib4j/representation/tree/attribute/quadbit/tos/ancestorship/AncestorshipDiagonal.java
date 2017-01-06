package mmlib4j.representation.tree.attribute.quadbit.tos.ancestorship;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.tos.TreeOfShape;

public class AncestorshipDiagonal implements Ancestorship{

	private TreeOfShape tos;
	private short[] imageUb;
	private int Uwidth;
	private int _dt1x;
	private int _dt1y;
	private int _dt2x;
	private int _dt2y;
	
	public AncestorshipDiagonal(GrayScaleImage image, TreeOfShape tos, int dt1x, int dt1y,
			int dt2x, int dt2y) {
		this.tos = tos;
		this.imageUb = tos.getBuilder().getImageU();
		this.Uwidth = image.getWidth() * 2 + 1;
		this._dt1x = dt1x;
		this._dt1y = dt1y;
		this._dt2x = dt2x;
		this._dt2y = dt2y;
	}

	private short getUbImageValue(int px, int py) {
		return imageUb[py * Uwidth + px];
	}
	
	private boolean verifyAPath(int px, int py, int qx, int qy, int dt1x, int dt1y,
			int dt2x, int dt2y) {
		
		int upx = 2 * px + 1, upy = 2 * py + 1;
		int uqx = 2 * qx + 1, uqy = 2 * qy + 1;
		int urx = upx + 2*dt1x, ury = upy + 2*dt1y;
		int ut1x = upx + dt1x, ut1y = upy + dt1y;		
		int ut2x = urx + dt2x, ut2y = ury + dt2y;
		
		short upValue = getUbImageValue(upx, upy);
		short ut1Value = getUbImageValue(ut1x, ut1y);
		short urValue = getUbImageValue(urx, ury);
		short uqValue = getUbImageValue(uqx, uqy);
		short ut2Value = getUbImageValue(ut2x, ut2y);
		
		if (caseOne(upValue, ut1Value, urValue)) {
			if (caseOne(urValue, ut2Value, uqValue) || caseTwo(urValue, ut2Value, uqValue) || 
					caseFour(urValue, ut2Value, uqValue))
				return false;
			
			/*It got case Two*/
			TreeOfShape etos = tos.getInterpolatedTree();
			
			if (etos.getSC(ut1y * Uwidth + ut1x).getDepth() < etos.getSC(ut2y * Uwidth + ut2x).getDepth())
				return false;
			return true;
			
		} else if (caseTwo(upValue, ut1Value, urValue)) {
			if (caseTwo(urValue, ut2Value, uqValue) || caseThree(urValue, ut2Value, uqValue) || caseFour(urValue, ut2Value, uqValue))
				return true;
			
			/* It got case One*/
			TreeOfShape etos = tos.getInterpolatedTree();
			
			if (etos.getSC(ut2y * Uwidth + ut2x).getDepth() >= etos.getSC(ut1y * Uwidth + ut1x).getDepth())
				return true;
			return false;
			
		} else if (caseThree(upValue, ut1Value, urValue)) {
			if (caseOne(urValue, ut2Value, uqValue) || caseTwo(urValue, ut2Value, uqValue))
				return false;
			/*It got either case three or case four*/
			return true;
		} else { //if (caseFour(upValue, ut1Value, uqValue)) It got case 4
			if (caseOne(urValue, ut2Value, uqValue))
				return false;
			/* It got either case two, three or four */
			return true;
		}
	}
	
	@Override
	public boolean isThereAncestorRelation(int px, int py, int qx, int qy) { 
		return verifyAPath(px, py, qx, qy, _dt1x, _dt1y, _dt2x, _dt2y) || verifyAPath(px, py, qx, qy, _dt2x, _dt2y, _dt1x, _dt1y);
	}
	
	private boolean caseOne(short upValue, short utValue, short urValue) {
		return upValue != utValue && urValue != utValue;
	}
	
	private boolean caseTwo(short upValue, short utValue, short urValue) {
		return upValue == utValue && urValue != utValue;
	}
	
	private boolean caseThree(short upValue, short utValue, short urValue) {
		return upValue != utValue && urValue == utValue;
	}
	
	private boolean caseFour(short upValue, short utValue, short urValue) {
		return upValue == utValue && urValue == utValue;
	}
}