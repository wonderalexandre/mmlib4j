package mmlib4j.filtering.binary;
import java.util.LinkedList;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.utils.Pixel;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ContourTracer {
	static final boolean FOREGROUND = true;
	static final boolean BACKGROUND = false;
	static final int[][] delta = { 
			{ 1,0}, { 1, 1}, {0, 1}, {-1, 1}, 
			{-1,0}, {-1,-1}, {0,-1}, { 1,-1}};
	
	private boolean is8Connected;
	int level;
	GrayScaleImage img;
	boolean isMaxtree;
	//boolean[][] pixels;
	
	public ContourTracer (boolean is8Connected, boolean isMaxtree, GrayScaleImage img, int level) {
		this.isMaxtree = isMaxtree;
		this.img = img;
		this.level = level;
		//pixels = new boolean[img.getWidth()][img.getHeight()];
		this.is8Connected = is8Connected;
	}

	public boolean isForeground(int x, int y){
		if(!img.isPixelValid(x, y)) return false;
		if(isMaxtree)
			return img.getPixel(x, y) >= level;
		else
			return img.getPixel(x, y) <= level;
		
		/*
		if(!img.isPixelValid(x, y)) return false;
		if(pixels == null){
			if(!img.isPixelValid(x, y)) return false;
			if(isMaxtree)
				return img.getPixel(x, y) >= level;
			else
				return img.getPixel(x, y) <= level;
		}
		
		return pixels[x][y] == FOREGROUND;
		*/
	}
	/*
	public void addPixelForeground(int p){
		int px = p % img.getWidth();
		int qx = p / img.getWidth();
		pixels[px][qx] = FOREGROUND;
	}
	*/
	public Contour traceOuterContour (int cx, int cy) {
		return traceContour(cx, cy, 0); 
	}
	
	public Contour traceInnerContour(int cx, int cy) {
		return traceContour(cx, cy, 1); 
	}
	
	Contour traceContour (int xS, int yS, int dS) {
		Contour cont = new Contour();
		int xT, yT; 
		int xP, yP; 
		int xC, yC; 
		Pixel pt = new Pixel(xS, yS, dS); 
		int dNext = findNextPoint(pt, dS);
		cont.addPoint((pt.x) + img.getWidth() * (pt.y), dS);
		 
		xP = xS; yP = yS;
		xC = xT = pt.x;
		yC = yT = pt.y;
		
		boolean done = (xS==xT && yS==yT);

		while (!done) {
			pt = new Pixel(xC, yC, dNext);
			
			int dSearch = (dNext + 6) % 8;
			dNext = findNextPoint(pt, dSearch);
			xP = xC;  yP = yC;	
			xC = pt.x; yC = pt.y; 
			done = (xP==xS && yP==yS && xC==xT && yC==yT);
			if (!done) {
				cont.addPoint((pt.x) + img.getWidth() * (pt.y), dNext);
			}
		}
		return cont;
	}
	
	int findNextPoint (Pixel pt, int direction) { 
		if(is8Connected){
			for (int i = 0; i < delta.length - 1; i++) {
				int x = pt.x + delta[direction][0];
				int y = pt.y + delta[direction][1];
				if (!isForeground(x, y)) {
					direction = (direction + 1) % 8;
				} 
				else {						
					pt.x = x; pt.y = y; 
					break;
				}
			}
			return direction;
		}else{
			for (int i = 0; i < delta.length - 1; i++) {
				int x = pt.x + delta[direction][0];
				int y = pt.y + delta[direction][1];
				if (! isForeground(x, y) == BACKGROUND) {
					direction = (direction + 2) % 8;
				} 
				else {						
					pt.x = x; pt.y = y; 
					break;
				}
			}
		}
		return direction;
	}
	
	
	public Contour findOuterContours(int x, int y) {
		return traceOuterContour(x, y);	
	}
	
	void findAllContours() {
		LinkedList<Contour> outerContours = new LinkedList<Contour>();
		LinkedList<Contour> innerContours = new LinkedList<Contour>();
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				if (isForeground(x, y) == FOREGROUND) { 
					Contour oc = traceOuterContour(x, y);
					outerContours.add(oc);	
				} 
				else {	
					// BACKGROUND pixel
					Contour ic = traceInnerContour(x, y-1);
					innerContours.add(ic);
				}
			}
		}
	}
	
}



