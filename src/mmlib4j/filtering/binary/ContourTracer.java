package mmlib4j.filtering.binary;
import java.util.LinkedList;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.utils.Pixel;

/**
 * MMorph4J - Mathematical Morphology Library for Java 
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
	int width;
	int height;
	int level;
	boolean[][] pixels;
	GrayScaleImage img;
	boolean isMaxtree;
	
	public ContourTracer (int w, int h, boolean is8Connected) {
		width = w;
		height = h;
		this.pixels = new boolean[width+2][height+2];
		this.is8Connected = is8Connected;
	}
	
	public ContourTracer (int w, int h, boolean is8Connected, boolean isMaxtree, GrayScaleImage img, int level) {
		width = w;
		height = h;
		this.isMaxtree = isMaxtree;
		this.img = img;
		this.level = level;
		//this.pixels = new boolean[width+2][height+2];
		this.is8Connected = is8Connected;
	}

	public boolean isForeground(int x, int y){
		if(pixels == null){
			if(!img.isPixelValid(x, y)) return false;
			if(isMaxtree)
				return img.getPixel(x, y) >= level;
			else
				return img.getPixel(x, y) <= level;
		}
		
		return pixels[x][y] == FOREGROUND;
	}
	
	public void addPixelForeground(int p){
		int px = p % width;
		int qx = p / width;
		pixels[px+1][qx+1] = FOREGROUND;
	}
	
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
		if(pixels == null)
			cont.addPoint((pt.x) + width * (pt.y), dS);
		else
			cont.addPoint((pt.x-1) + width * (pt.y-1), dS); 
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
				if(pixels == null)
					cont.addPoint((pt.x) + width * (pt.y), dNext);
				else
					cont.addPoint((pt.x-1) + width * (pt.y-1), dNext);
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
	
	
	
	public Contour findOuterContours() {
		for (int y = 1; y < height+1; y++) {
			for (int x = 1; x < width+1; x++) {
				if (isForeground(x, y) == FOREGROUND) { 
					return traceOuterContour(x, y);	
				} 
			}
		}
		throw new RuntimeException("ops..");
	}
	
	public Contour findOuterContours(int x, int y) {
		return traceOuterContour(x, y);	
	}
	
	void findAllContours() {
		LinkedList<Contour> outerContours = new LinkedList<Contour>();
		LinkedList<Contour> innerContours = new LinkedList<Contour>();
		for (int y = 1; y < height+1; y++) {
			for (int x = 1; x < width+1; x++) {
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



