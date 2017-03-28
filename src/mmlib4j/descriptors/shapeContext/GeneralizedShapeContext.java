package mmlib4j.descriptors.shapeContext;
import java.util.List;


public class GeneralizedShapeContext extends ShapeContext{
	
    private Bin histogram[][];//5, 12
    private double beta = 0.5;
	
	public GeneralizedShapeContext(Point point){
		super(point);
		histogram = new Bin[5][12];
	}
	
	Bin get(int i, int j){
		return histogram[i][j];
	}
	
		
	public double distance(ShapeContext sc) {
		double custoAngle = 0.0;
	    Bin g, h;
	    Bin aux = new Bin();
	    
	    GeneralizedShapeContext gsc =  (GeneralizedShapeContext) sc;
	    for (int k = 0; k < 5; k++) {
	        for (int l = 0; l < 12; l++) {
	            g = get(k,l);
	            h = gsc.get(k,l);
	            aux.sumTgX = g.sumTgX-h.sumTgX;
	            aux.sumTgY = g.sumTgY-h.sumTgY;
	            double gnorm = Math.hypot(g.sumTgX,g.sumTgY);
	            double hnorm = Math.hypot(h.sumTgX,h.sumTgY);
	            double auxnorm = Math.hypot(aux.sumTgX,aux.sumTgY);
	            if (gnorm + hnorm > 0)
	            	custoAngle += (auxnorm*auxnorm/(gnorm+hnorm));
	        }
	    }
	  //return sum;
	    
	    double custoSum = 0.0;
	    for (int k = 0; k < 5; k++) {
	    	for (int l = 0; l < 12; l++) {
	    		double g1 = this.get(k,l).countPoints;
 	           	double h1 = gsc.get(k,l).countPoints;
 	           	if (g1 + h1 > 0)
 	           		custoSum += ((g1 - h1) * (g1 - h1 )) / (g1 + h1);
	    	}
	    }
	 
	    return (1 - beta) * custoSum + beta * custoAngle;
	    //return sum;
	}

		
	public void compute(List<Point> points, double median) {
		int i, j, radBin, angBin, angle;
	    double dist;

	    for (i = 0; i < 5; i++) {
	        for (j = 0; j < 12; j++) {
	            histogram[i][j] = new Bin();
	        }
	    }
		for (i = 0; i < points.size(); i++) {
			// Invariancia a escala
			dist = Math.hypot(points.get(i).x - point.x, points.get(i).y - point.y)/median;

			angle = (int) ( Math.atan2(point.y - points.get(i).y, points.get(i).x - point.x) * 180 / Math.PI );
			if (dist != 0) {
				if (angle >= 360)
					angle -= 360;
				if (angle < 0)
					angle += 360;
				angBin = angularBin(angle);
				radBin = logRadialBin(dist);
				histogram[radBin][angBin].sumTgX += points.get(i).tg.x;
				histogram[radBin][angBin].sumTgY += points.get(i).tg.y;
				
				//counter points
				histogram[radBin][angBin].countPoints++;
				histogram[radBin][angBin].countPoints++;
			}
		}
		
		// Normalizacao
		double sum = 0;
		for (i = 0; i < 5; i++)
			for (j = 0; j < 12; j++)
				sum += histogram[i][j].countPoints;

		for (i = 0; i < 5; i++) {
			for (j = 0; j < 12; j++) {
				histogram[i][j].countPoints = histogram[i][j].countPoints / sum;
			}
		}
	}


		
}
