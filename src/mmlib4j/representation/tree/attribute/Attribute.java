package mmlib4j.representation.tree.attribute;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;

import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.bitquads.ComputerAttributeBasedOnBitQuads;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedMSER;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class Attribute {
	
	public double value;
	int type;
	private static DecimalFormat df;
	
	public Attribute(int t){
		type = t;
	}
	
	public Attribute(int t, double v){
		type = t;
		value = v;
	}
	
	public double getValue(){
		return value;
	}
	
	public int getType(){
		return type;
	}
	
	//basic attributes
	public static final int AREA = 0;
	public static final int VOLUME = 1;
	public static final int HEIGHT = 2;
	public static final int WIDTH = 3;
	public static final int ALTITUDE = 4;
	public static final int LEVEL = 5;
	public static final int RATIO_WIDTH_HEIGHT = 6;
	public static final int RECTANGULARITY = 7;
	//public static final int PERIMETER = 10;
	public static final int VARIANCE_LEVEL = 11;
	public static final int LEVEL_MEAN = 12;
	public static final int STD_LEVEL = 13;
	
	public static final int PERIMETER_EXTERNAL = 14;
	public static final int CIRCULARITY = 15;
	public static final int COMPACTNESS = 16;
	public static final int ELONGATION = 17;
	
	
	//attributes based on moments
	public static final int MOMENT_CENTRAL_20 = 20;
	public static final int MOMENT_CENTRAL_02 = 21;
	public static final int MOMENT_CENTRAL_11 = 22;
	public static final int MOMENT_ORIENTATION = 23;
	public static final int MOMENT_COMPACTNESS = 24;
	public static final int MOMENT_ECCENTRICITY = 25;
	public static final int MOMENT_ELONGATION = 26;
	public static final int MOMENT_LENGTH_MAJOR_AXES = 27;
	public static final int MOMENT_LENGTH_MINOR_AXES = 28;
	public static final int MOMENT_ASPECT_RATIO = 29;
	public static final int MOMENT_OF_INERTIA = 30;
	
	//attribute based on bit quads
	//public static final int NUM_HOLES = 30;
	
	public static final int BIT_QUADS_PERIMETER = 31;
	public static final int BIT_QUADS_EULER_NUMBER= 32;
	public static final int BIT_QUADS_HOLE_NUMBER = 33;
	public static final int BIT_QUADS_PERIMETER_CONTINUOUS = 34;
	public static final int BIT_QUADS_CIRCULARITY = 35;
	public static final int BIT_QUADS_AVERAGE_AREA= 36;
	public static final int BIT_QUADS_AVERAGE_PERIMETER = 37;
	public static final int BIT_QUADS_AVERAGE_LENGTH= 38;
	public static final int BIT_QUADS_AVERAGE_WIDTH= 39;
	public static final int BIT_QUADS_AREA = 50;
	public static final int BIT_QUADS_AREA_DUDA = 51;
	
	//mser
	public static final int MSER = 40;
	
    // energy
	public static final int SUM_GRAD_CONTOUR = 52;
	public static final int FUNCTIONAL_ATTRIBUTE = 53;
	public static final int FUNCTIONAL_VARIATIONAL = 54;
	
	// others
	public static final int SUM_LEVEL_2 = 70;
	public static final int XMIN = 71;
	public static final int XMAX = 72;
	public static final int YMIN = 73;
	public static final int YMAX = 74;
	public static final int PIXEL_XMIN = 75;
	public static final int PIXEL_XMAX = 76;
	public static final int PIXEL_YMIN = 77;
	public static final int PIXEL_YMAX = 78;
	public static final int SUM_X = 79;
	public static final int SUM_Y = 80;
		
	// attributes based on node histogram
	public static final int ENTROPY = 81;
	public static final int ENERGY = 82;
	
	public static String getNameAttribute(int type) {
		switch(type){
		case ALTITUDE: return "ALTITUDE";	
		case AREA: return "AREA";
		case CIRCULARITY: return "CIRCULARITY";
		case COMPACTNESS: return "COMPACTNESS";
		case ELONGATION: return "ELONGATION";
		case HEIGHT: return "HEIGHT";
		case LEVEL: return "LEVEL";
		case MOMENT_ASPECT_RATIO: return "MOMENT_ASPECT_RATIO";
		case MOMENT_CENTRAL_02: return "MOMENT_CENTRAL_02";
		case MOMENT_CENTRAL_11: return "MOMENT_CENTRAL_11";
		case MOMENT_CENTRAL_20: return "MOMENT_CENTRAL_20";
		case MOMENT_COMPACTNESS: return "MOMENT_COMPACTNESS";
		case MOMENT_ECCENTRICITY: return "MOMENT_ECCENTRICITY";
		case MOMENT_ELONGATION: return "MOMENT_ELONGATION";
		case MOMENT_OF_INERTIA: return "MOMENT_OF_INERTIA";
		case MOMENT_LENGTH_MAJOR_AXES: return "MOMENT_LENGTH_MAJOR_AXES";
		case MOMENT_LENGTH_MINOR_AXES: return "MOMENT_LENGTH_MINOR_AXES";
		case MOMENT_ORIENTATION: return "MOMENT_ORIENTATION";
		case MSER: return "MSER";
		//case NUM_HOLES: return "NUM_HOLES";
		//case PERIMETER: return "PERIMETER";
		case PERIMETER_EXTERNAL: return "PERIMETER_EXTERNAL";
		case RECTANGULARITY: return "RECTANGULARITY";
		case RATIO_WIDTH_HEIGHT: return "RATIO_WIDTH_HEIGHT";
		case VARIANCE_LEVEL: return "VARIANCE_LEVEL";
		case STD_LEVEL: return "STD_LEVEL";
		case LEVEL_MEAN: return "LEVEL_MEAN";
		case VOLUME: return "VOLUME";
		case WIDTH: return "WIDTH";
		
		case BIT_QUADS_PERIMETER: return "PERIMETERS_QUAD";
		case BIT_QUADS_EULER_NUMBER: return "BIT_QUADS_NUMBER_EULER";
		case BIT_QUADS_HOLE_NUMBER: return "BIT_QUADS_NUMBER_HOLES";
		case BIT_QUADS_PERIMETER_CONTINUOUS: return "BIT_QUADS_PERIMETER_CONTINUOUS";
		case BIT_QUADS_CIRCULARITY: return "BIT_QUADS_CIRCULARITY";
		case BIT_QUADS_AVERAGE_AREA: return "BIT_QUADS_AREA_AVERAGE";
		case BIT_QUADS_AVERAGE_LENGTH: return "BIT_QUADS_LENGTH_AVERAGE";
		case BIT_QUADS_AVERAGE_WIDTH: return "BIT_QUADS_WIDTH_AVERAGE";
		case BIT_QUADS_AREA: return "BIT_QUADS_AREA";
		case BIT_QUADS_AREA_DUDA: return "BIT_QUADS_AREA_DUDA";
		
		// energy
		case SUM_GRAD_CONTOUR: return "SUM_GRAD_CONTOUR";
		case FUNCTIONAL_ATTRIBUTE: return "FUNCTIONAL_ATTRIBUTE";
		case FUNCTIONAL_VARIATIONAL: return "FUNCTIONAL_VARIATIONAL";
		
		//others
		case SUM_LEVEL_2: return "SUM_LEVEL_2";
		case XMIN: return "XMIN";
		case XMAX: return "XMAX";
		case YMIN: return "YMIN";
		case YMAX: return "YMAX";
		case PIXEL_XMIN: return "PIXEL_XMIN";
		case PIXEL_XMAX: return "PIXEL_XMAX"; 
		case PIXEL_YMIN: return "PIXEL_YMIN";
		case PIXEL_YMAX: return "PIXEL_YMAX";
		case SUM_X: return "SUM_X";
		case SUM_Y: return "SUM_Y";
		
		case ENTROPY: return "ENTROPY";
		case ENERGY: return "ENERGY";
		
		default: return "UNDEFINED";
	}
	}
	
	public String getHeader(){
		return getNameAttribute(type);
	}
	
	public static String printWithHeader(HashMap<Integer, Attribute> hashMap){
		StringBuffer sb = new StringBuffer();
		for(Attribute attr: hashMap.values()){
			sb.append(attr+ "\t");
		}
		return sb.toString();
	}
	
	public static String printHeader(HashMap<Integer, Attribute> hashMap){
		StringBuffer sb = new StringBuffer();
		for(Attribute attr: hashMap.values()){
			sb.append(attr.getHeader()+ "\t");
		}
		return sb.toString();
	}
	
	public static String printHeaderWEKA(HashMap<Integer, Attribute> hashMap, int classes[]){
		StringBuffer sb = new StringBuffer();
		sb.append("@relation Features\n");
		for(Attribute attr: hashMap.values()){
			sb.append("@attribute "+ attr.getHeader() +" real\n");
		}
		if(classes!=null){
			sb.append("@attribute classe {");
			for(int i=0; i < classes.length; i++){
				int c = classes[i];
				sb.append(c);
				if(classes.length > 1 && i < classes.length - 1)
					sb.append(", ");
			}
			sb.append("}\n");
		}
		sb.append("@data");		
		return sb.toString();
	}
	
	public static String print(HashMap<Integer, Attribute> hashMap, String token){
		StringBuffer sb = new StringBuffer();
		for(Attribute attr: hashMap.values()){
			sb.append(attr.getValueFormat()+ token);
		}
		return sb.toString();//.substring(0, sb.length()-token.length());
	}
	

	public String getValueFormat(){
		if(df == null){
			 df = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.ENGLISH));
		}
		return df.format(value);
	}
	
	public String toString(){
		return getHeader() +": "+ getValueFormat();
	}
	
	/**
	 * 
	 *	This method loads an attribute in tree structure.
	 *
	 *  @param tree Morphological tree.
	 * 	@param attr Type of attribute (see {@link Attribute}).  
	 * 
	 */
	public static void loadAttribute(MorphologicalTree tree, int attr) {
		if(!tree.getRoot().hasAttribute(attr)){
			switch(attr){
			case Attribute.ALTITUDE:
			case Attribute.AREA:
			case Attribute.VOLUME:
			case Attribute.WIDTH:
			case Attribute.HEIGHT:
			//case Attribute.PERIMETER:
			case Attribute.LEVEL:
			case Attribute.RECTANGULARITY:
			case Attribute.RATIO_WIDTH_HEIGHT:
			case Attribute.XMIN:
			case Attribute.XMAX:
			case Attribute.YMIN:
			case Attribute.YMAX:
			case Attribute.PIXEL_XMIN:
			case Attribute.PIXEL_XMAX: 
			case Attribute.PIXEL_YMIN: 
			case Attribute.PIXEL_YMAX: 
			case Attribute.SUM_X: 
			case Attribute.SUM_Y: 
				ComputerBasicAttribute.loadAttribute(tree);
				break;
			
			case Attribute.MSER:
				ComputerMSER.loadAttribute(tree);
				break;
				
			case Attribute.MOMENT_CENTRAL_02:
			case Attribute.MOMENT_CENTRAL_20:
			case Attribute.MOMENT_CENTRAL_11:
			case Attribute.VARIANCE_LEVEL:
			case Attribute.LEVEL_MEAN:
			case Attribute.STD_LEVEL:
			case Attribute.SUM_LEVEL_2:
			case Attribute.MOMENT_COMPACTNESS:
			case Attribute.MOMENT_ECCENTRICITY:
			case Attribute.MOMENT_ELONGATION:
			case Attribute.MOMENT_LENGTH_MAJOR_AXES:
			case Attribute.MOMENT_LENGTH_MINOR_AXES:
			case Attribute.MOMENT_ORIENTATION:
			case Attribute.MOMENT_ASPECT_RATIO:
			case Attribute.MOMENT_OF_INERTIA:
				ComputerCentralMomentAttribute.loadAttribute(tree);
				break;
			
			case Attribute.PERIMETER_EXTERNAL:
			case Attribute.CIRCULARITY:
			case Attribute.COMPACTNESS:
			case Attribute.ELONGATION:
			case Attribute.SUM_GRAD_CONTOUR:
				ComputerAttributeBasedPerimeterExternal.loadAttribute(tree);
				break;				
				
			//case Attribute.NUM_HOLES:
			case Attribute.BIT_QUADS_PERIMETER:
			case Attribute.BIT_QUADS_EULER_NUMBER:
			case Attribute.BIT_QUADS_HOLE_NUMBER:
			case Attribute.BIT_QUADS_PERIMETER_CONTINUOUS:
			case Attribute.BIT_QUADS_CIRCULARITY:
			case Attribute.BIT_QUADS_AVERAGE_AREA:
			case Attribute.BIT_QUADS_AVERAGE_PERIMETER:
			case Attribute.BIT_QUADS_AVERAGE_LENGTH:
			case Attribute.BIT_QUADS_AVERAGE_WIDTH:
				if(!(tree instanceof ComponentTree)) 
					throw new UnsupportedOperationException("This attribute doesn't work for all trees yet!");
				else 	
					ComputerAttributeBasedOnBitQuads.loadAttribute((ComponentTree)tree);
				break;
				
			case Attribute.FUNCTIONAL_ATTRIBUTE:
				Attribute.loadAttribute(tree, Attribute.SUM_GRAD_CONTOUR);			
				ComputerFunctionalAttribute.loadAttribute(tree);
				break;
			default:
				throw new RuntimeException("Unsupported attribute!\nAttribute name:"+ getNameAttribute(attr));
			}
		}
	}
	public static boolean hasAttribute(MorphologicalTree tree, int attr) {
		return tree.getRoot().hasAttribute(attr);
	}
	
	public static boolean hasComputerAttribute(MorphologicalTree tree, Class classe) {
		if(ComputerBasicAttribute.class.equals(classe))
			return hasAttribute(tree, Attribute.AREA) && hasAttribute(tree, Attribute.VOLUME) && hasAttribute(tree, Attribute.RATIO_WIDTH_HEIGHT);
		else if(ComputerCentralMomentAttribute.class.equals(classe))
			return hasAttribute(tree, Attribute.MOMENT_OF_INERTIA);
		else if(ComputerAttributeBasedPerimeterExternal.class.equals(classe))
			return hasAttribute(tree, Attribute.PERIMETER_EXTERNAL);
		else if(ComputerAttributeBasedOnBitQuads.class.equals(classe))
			return hasAttribute(tree, Attribute.BIT_QUADS_EULER_NUMBER);
		else if(ComputerFunctionalAttribute.class.equals(classe))
			return hasAttribute(tree, Attribute.FUNCTIONAL_ATTRIBUTE);
		
		return false;
	}
	
	public static double getMaxValue(MorphologicalTree tree, int type) {
		double max = tree.getRoot().getAttributeValue(type);
		for(NodeLevelSets node: tree.getListNodes()) {
			if(node.getAttributeValue(type) > max) {
				max = node.getAttributeValue(type);
			}
		}
		return max;
	}
	
	public static double getMinValue(MorphologicalTree tree, int type) {
		double min = tree.getRoot().getAttributeValue(type);
		for(NodeLevelSets node: tree.getListNodes()) {
			if(node.getAttributeValue(type) < min) {
				min = node.getAttributeValue(type);
			}
		}
		return min;
	}
	
	public static ComputerDistanceTransform computerDistanceTransform(MorphologicalTree tree){
		return new ComputerDistanceTransform(tree.getNumNodeIdMax(), tree.getRoot(), tree.getInputImage());
	}	

	public static void main(String args[]) {
		Class c = ComputerBasicAttribute.class;
		System.out.print( ComputerBasicAttribute.class.equals(c) );
	}
	
}
