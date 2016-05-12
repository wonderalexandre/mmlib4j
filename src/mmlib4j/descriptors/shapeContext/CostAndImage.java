package mmlib4j.descriptors.shapeContext;



public class CostAndImage implements Comparable<CostAndImage> {

	double cost;
	ImageShapeContext img;
	
	public CostAndImage(double value, ImageShapeContext img){
		this.cost = value;
		this.img = img;
	}
	
	public int compareTo(CostAndImage o) {
		if(this.cost < o.cost) return -1;
		else if(this.cost > o.cost) return 1;
		else return 0;
	} 

	
}
