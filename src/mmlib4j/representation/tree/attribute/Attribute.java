package mmlib4j.representation.tree.attribute;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class Attribute {
	
	double value;
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
	public static final int PERIMETER = 10;
	public static final int VARIANCE_LEVEL = 11;
	public static final int LEVEL_MEAN = 12;
	
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
	
	
	//pattern euler
	public static final int NUM_HOLES = 30;
	
	//mser
	public static final int MSER = 40;
	
	public String getHeader(){
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
			case MOMENT_LENGTH_MAJOR_AXES: return "MOMENT_LENGTH_MAJOR_AXES";
			case MOMENT_LENGTH_MINOR_AXES: return "MOMENT_LENGTH_MINOR_AXES";
			case MOMENT_ORIENTATION: return "MOMENT_ORIENTATION";
			case MSER: return "MSER";
			case NUM_HOLES: return "NUM_HOLES";
			case PERIMETER: return "PERIMETER";
			case PERIMETER_EXTERNAL: return "PERIMETER_EXTERNAL";
			case RECTANGULARITY: return "RECTANGULARITY";
			case RATIO_WIDTH_HEIGHT: return "RATIO_WIDTH_HEIGHT";
			case VARIANCE_LEVEL: return "VARIANCE_LEVEL";
			case LEVEL_MEAN: return "LEVEL_MEAN";
			case VOLUME: return "VOLUME";
			case WIDTH: return "WIDTH";
			default: return "UNDEFINED";
		}
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
			 df = new DecimalFormat("0.0000000", new DecimalFormatSymbols(Locale.ENGLISH));
		}
		return df.format(value);
	}
	
	public String toString(){
		return getHeader() +": "+ getValueFormat();
	}
	
}
