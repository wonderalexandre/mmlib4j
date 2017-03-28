package mmlib4j.descriptors.shapeContext;
import java.util.List;



public abstract class ShapeContext {
	Point point;
	final double initialRadius = 0.125;

	public abstract void compute(List<Point> points, double median);
	
	public abstract double distance(ShapeContext sc);
	
	
	ShapeContext(Point point){
		this.point = point;
	}
	
	
	Point getPoint(){
		return point;
	}
	
	int angularBin(double angle){
		return (int) Math.floor(angle / 30);
	}
	
	int logRadialBin(double radius){
		double logRadius = Math.log(radius / initialRadius);
		int bin = (int) Math.floor(logRadius);
		if(bin < 0)
			bin = 0;
		else if(bin > 4)
			bin = 4;
		
		return bin;
	}
	
}
