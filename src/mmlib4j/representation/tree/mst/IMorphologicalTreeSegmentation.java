package mmlib4j.representation.tree.mst;

import mmlib4j.images.GrayScaleImage;

public interface IMorphologicalTreeSegmentation {
	
	public final static int ATTRIBUTE_AREA = 0;
	public final static int ATTRIBUTE_VOLUME = 1;
	
	public GrayScaleImage segmentation(double attributeValue, int type);

	
}
