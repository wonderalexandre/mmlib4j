package mmlib4j.descriptors.shapeContext;

import java.util.ArrayList;
import java.util.List;

import mmlib4j.images.BinaryImage;
import mmlib4j.images.GrayScaleImage;

public class ImageShapeContext {

	private GrayScaleImage img;
	private List<ShapeContext> SC = new ArrayList<ShapeContext>();
    private List<Point> allPoints = new ArrayList<Point>();
    
	public ImageShapeContext(GrayScaleImage img){
		this.img = img;
	}
    
	public GrayScaleImage getImage(){
		return img;
	}
    
	public List<Point> getAllPoints() {
		if (allPoints.isEmpty()) {
			BinaryImage imgContours = new Canny().run(img, 1f,120,140);
			
            for( int i = 0; i < img.getHeight(); i++ ){
                for (int j = 0; j < img.getWidth(); j++){
                	boolean p[] = new boolean[9];
                	int cont = 0;
                    if(imgContours.isPixelForeground(j, i)){
                    	for(int y=-1; y <=1; y++){
                    		for(int x=-1; x <= 1; x++){
                    			if(imgContours.isPixelValid(x+j, y+i)){
                    				p[cont++] = imgContours.isPixelForeground(x+j, y+i);	
                    			}
                    		}
                    	}
                    	Point pt = new Point(j, i, Utility.tangent(p));
                    	allPoints.add(pt);
                    }
                } 
            }
        }
        return allPoints;

	}
	
	public void computeShapeContexts(int n, double median) {
	    
		SC.clear();
	    List<Point> allpts = getAllPoints();

	    if (n > allpts.size())
	        n = allpts.size();
 
	    List<Point> pts = Utility.sample(allpts, n);
 
	    for (int i = 0; i < pts.size(); i++) {
	        ShapeContext sc = new GeneralizedShapeContext(pts.get(i));
	        sc.compute(allpts, median);
	        SC.add(sc);
	    } 
	}
	
	public List<ShapeContext> getShapeContexts() {
	    return SC;
	}

}
