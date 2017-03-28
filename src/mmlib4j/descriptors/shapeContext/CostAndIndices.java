package mmlib4j.descriptors.shapeContext;

import java.util.List;

public class CostAndIndices {
	double cost;
	List<ShapeContext> sc1;
	List<ShapeContext> sc2;
	
	public CostAndIndices(double cost, List<ShapeContext> sc1, 	List<ShapeContext> sc2) {
		this.cost = cost;
		this.sc1 = sc1;
		this.sc2 = sc2;
	}
	
}
