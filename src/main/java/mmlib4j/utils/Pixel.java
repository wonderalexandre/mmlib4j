package mmlib4j.utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class Pixel {
	public int x;
	public int y;
	public int direction;
	
	public Pixel(int x, int y){
		this.x = x;
		this.y = y;
	}


    public void translate(int dx, int dy) {
    	this.x += dx;
    	this.y += dy;
    }
    
    public Pixel(int x, int y, int direction){
		this.x = x;
		this.y = y;
		this.direction = direction;
	}
	
}
