package mmlib4j.representation.tree.attribute;

import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;

public interface ComputerExtinctionValue {

	public ColorImage extinctionByAttribute(int attributeValue1, int attributeValue2, int type);
	
	public ColorImage extinctionByKmax(int kmax, int type);
	
	public GrayScaleImage segmentationByKmax(int kmax, int type);
	
	public GrayScaleImage segmentationByAttribute(int attributeValue1, int attributeValue2, int type);
}
