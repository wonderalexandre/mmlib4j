package mmlib4j.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class AttributeToCvs {

	private final String SEPARATOR = ";";
	
	private PrintWriter output;
	
	public AttributeToCvs( File file ) {
		
		try {
			
			output = new PrintWriter( new FileOutputStream( file ) );
			
		} catch ( FileNotFoundException e ) {
						
			e.printStackTrace();
			
		}
		
	}
	
	public void writeData( double sGrad, double energy ) {
		
		output.println( sGrad + SEPARATOR + energy );
		
	}
	
	public void destroy() {
		
		output.close();
		
	}
	
}
