package mmlib4j.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import mmlib4j.representation.tree.attribute.Attribute;

public class AttributeToCvs {

	private final String SEPARATOR = ",";
	
	private PrintWriter output;
	
	private static AttributeToCvs instance;
	
	public static AttributeToCvs createInstance( File file ) {
		
		return instance = new AttributeToCvs( file );
		
	}
	
	public static AttributeToCvs getInstance() {
		
		return instance;
		
	}
	
	private AttributeToCvs ( File file ) {
		
		try {
			
			output = new PrintWriter( new FileOutputStream( file ) );
			
		} catch ( FileNotFoundException e ) {
						
			e.printStackTrace();
			
		}	
		
	}
	
	public void write( HashMap<Integer, Attribute> attributesValues, int ... attributes ) {
			
		StringBuffer data = new StringBuffer();			
		
		for( int attribute : attributes ) {
			
			data.append( attributesValues.get( attribute ).getValue() + SEPARATOR );
			
		}
		
		data.deleteCharAt( data.length() - 1 );
		
		output.println( data );
		
	}
	
	public void destroy() {
		
		output.close();
		
	}
	
}
