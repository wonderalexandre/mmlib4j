package mmlib4j.representation.tree.attribute;


public class Attribute {
	
	double value;
	int type;
	
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
	public static final int XMAX = 5;
	public static final int XMIN = 6;
	public static final int YMAX = 7;
	public static final int YMIN = 8;
	public static final int ALTITUDE = 9;
	public static final int PIXEL_XMAX = 10;
	public static final int PIXEL_XMIN = 11;
	public static final int PIXEL_YMAX = 12;
	public static final int PIXEL_YMIN = 13;
	public static final int PERIMETER = 14;
	public static final int CIRCULARITY = 15;
	public static final int COMPACTNESS2 = 16;
	
	//attributes based on moments
	public static final int MOMENT_CENTRAL_20 = 20;
	public static final int MOMENT_CENTRAL_02 = 21;
	public static final int MOMENT_CENTRAL_11 = 22;
	public static final int MOMENT_ORIENTATION = 23;
	public static final int COMPACTNESS = 24;
	public static final int ECCENTRICITY = 25;
	public static final int ELONGATION = 26;
	public static final int LENGTH_MAJOR_AXES = 27;
	public static final int LENGTH_MINOR_AXES = 28;
	
	//pattern euler
	public static final int NUM_HOLES = 30;
	
	public String toString(){
		return String.valueOf(value);
	}
	
}
