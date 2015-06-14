package mmlib4j.representation.tree.attribute;

import java.util.HashSet;
import java.util.Iterator;

import mmlib4j.representation.tree.INodeTree;
import mmlib4j.representation.tree.componentTree.NodeCT;

public class ComputerCentralMomentAttribute extends AttributeComputedIncrementally{
	
	CentralMomentsAttribute attr[];
	int numNode;
	int withImg;
	
	public ComputerCentralMomentAttribute(int numNode, INodeTree root, int withImg){
		this.numNode = numNode;
		this.withImg = withImg;
		attr = new CentralMomentsAttribute[numNode];
		computerAttribute(root);
	}

	public ComputerCentralMomentAttribute(){	}
	
	public CentralMomentsAttribute[] getAttribute(){
		return attr;
	}
	
	public void addAttributeInNodesCT(HashSet<NodeCT> hashSet){
		for(INodeTree node: hashSet){
			node.addAttribute(Attribute.MOMENT_CENTRAL_02, attr[ node.getId() ].moment02);
			node.addAttribute(Attribute.MOMENT_CENTRAL_20, attr[ node.getId() ].moment20);
			node.addAttribute(Attribute.MOMENT_CENTRAL_11, attr[ node.getId() ].moment11);
			
			node.addAttribute(Attribute.COMPACTNESS, new Attribute(Attribute.COMPACTNESS, attr[ node.getId() ].compactness()));
			node.addAttribute(Attribute.ECCENTRICITY, new Attribute(Attribute.ECCENTRICITY, attr[ node.getId() ].eccentricity()));
			node.addAttribute(Attribute.ELONGATION, new Attribute(Attribute.ELONGATION, attr[ node.getId() ].elongation()));
			node.addAttribute(Attribute.LENGTH_MAJOR_AXES, new Attribute(Attribute.LENGTH_MAJOR_AXES, attr[ node.getId() ].getLengthMajorAxes()));
			node.addAttribute(Attribute.LENGTH_MINOR_AXES, new Attribute(Attribute.LENGTH_MINOR_AXES, attr[ node.getId() ].getLengthMinorAxes()));
			node.addAttribute(Attribute.MOMENT_ORIENTATION, new Attribute(Attribute.MOMENT_ORIENTATION, attr[ node.getId() ].getMomentOrientation()));
			
		}
	}
	
	
	public void preProcessing(INodeTree node) {
		attr[node.getId()] = new CentralMomentsAttribute(node.getCentroid(), node.getArea(), withImg);
		//area e volume
		
		for(int pixel: node.getCanonicalPixels()){
			int x = pixel % withImg;
			int y = pixel / withImg;
			attr[node.getId()].moment11.value += Math.pow(x - attr[node.getId()].xCentroid, 1) * Math.pow(y - attr[node.getId()].yCentroid, 1);
			attr[node.getId()].moment20.value += Math.pow(x - attr[node.getId()].xCentroid, 2) * Math.pow(y - attr[node.getId()].yCentroid, 0);
			attr[node.getId()].moment02.value += Math.pow(x - attr[node.getId()].xCentroid, 0) * Math.pow(y - attr[node.getId()].yCentroid, 2);
		
		}
		
		
	}
	
	public void mergeChildren(INodeTree node, INodeTree son) {
		attr[node.getId()].moment11.value += attr[son.getId()].moment11.value;
		attr[node.getId()].moment02.value +=  attr[son.getId()].moment02.value;
		attr[node.getId()].moment20.value += attr[son.getId()].moment20.value;
		
	}

	public void posProcessing(INodeTree root) {
		//pos-processing root
		
	}
	
	
	public class CentralMomentsAttribute {

		Attribute moment20 = new Attribute(Attribute.MOMENT_CENTRAL_20);
		Attribute moment02 = new Attribute(Attribute.MOMENT_CENTRAL_02);
		Attribute moment11 = new Attribute(Attribute.MOMENT_CENTRAL_11);
		
		double area; 
		double xCentroid;
		double yCentroid;
		int width;
		
		public CentralMomentsAttribute(double xc, double yc, int area, int width){
			this.area = (double) area;
			this.xCentroid = xc;
			this.yCentroid = yc;
			this.width = width;
		}
		
		public CentralMomentsAttribute(INodeTree node, int widthImg){
			this.area = node.getArea();
			this.xCentroid = node.getCentroid() % widthImg;
			this.yCentroid = node.getCentroid() / widthImg;
			this.width = widthImg;
			this.moment02 = node.getAttribute(Attribute.MOMENT_CENTRAL_02);
			this.moment20 = node.getAttribute(Attribute.MOMENT_CENTRAL_20);
			this.moment11 = node.getAttribute(Attribute.MOMENT_CENTRAL_11);
		}
		
		public CentralMomentsAttribute(int centroid, int area, int width){
			this(centroid % width, centroid / width, area, width);
			
		}
		
		
		//=> moment[p][q] / norm;
		public double getFatorNormalized(int p, int q){
			final double norm = Math.pow( area, (p + q + 2.0) / 2.0);
			return norm; 
		}
		
		/**
		 * Direcao (angulo) do maior eixo 
		 */
		public double getMomentOrientation(){
			return 0.5 * Math.atan2(2 * moment11.value, moment20.value - moment02.value );
		}
		
		public double eccentricity(){
			double a = moment20.value + moment02.value + Math.sqrt( Math.pow(moment20.value - moment02.value, 2) + 4 * Math.pow(moment11.value, 2));
			double b = moment20.value + moment02.value - Math.sqrt( Math.pow(moment20.value - moment02.value, 2) + 4 * Math.pow(moment11.value, 2));
			return a / b;
			
		}
		
		public double compactness(){
			return 1/(2*Math.PI) *   area / (moment20.value + moment02.value); 
		}
		
		public double elongation(){
			return area / (getLengthMajorAxes() * getLengthMajorAxes());
		}
		

		public double getLengthMajorAxes(){
			double a = moment20.value + moment02.value + Math.sqrt( Math.pow(moment20.value - moment02.value, 2) + 4 * Math.pow(moment11.value, 2));
			return Math.sqrt( (2 * a) / area );
		}

		public double getLengthMinorAxes(){
			double b = moment20.value + moment02.value - Math.sqrt( Math.pow(moment20.value - moment02.value, 2) + 4 * Math.pow(moment11.value, 2));
			return Math.sqrt( (2 * b) / area );
		}
		
		public double getAngularCoefficientOfMajorAxes(){
			return Math.tan( getMomentOrientation() );
		}
		public double getLinearCoefficientOfMajorAxes(){
			return yCentroid - getAngularCoefficientOfMajorAxes() * xCentroid;
		}
		
		public double getAngularCoefficientOfMinorAxes(){
			return Math.tan( getMomentOrientation() + Math.toRadians(90) );
		}
		public double getLinearCoefficientOfMinorAxes(){
			return yCentroid - getAngularCoefficientOfMinorAxes() * xCentroid;
		}
		
		public Iterable<Integer> getPixelElippse(){
			final double angle = getMomentOrientation();
			final double ra = getLengthMajorAxes();
			final double rb = getLengthMinorAxes();
			return new Iterable<Integer>() {
				public Iterator<Integer> iterator() {
					return new Iterator<Integer>() {
						double t = 0;
						int xOld=0;
						int yOld=0;
						int x=0;
						int y=0;
						public boolean hasNext() {
							return t < 2.0 * Math.PI;
						}
						
						public Integer next() {
							/*
							 * |xCentroid| +  |cos(angle) -sin(angle)| . |ra x cos(t)| 
							 * |yCentroid| +  |sin(angle)  cos(angle)|   |rb x sin(t)|
							 */
							xOld = x;
							yOld = y;
							while(x == xOld && y == yOld){
								double raT = ra * Math.cos(t);
								double rbT = rb * Math.sin(t);
								x =  (int) (xCentroid + (Math.cos(angle) * raT) - (Math.sin(angle) * rbT));
								y =  (int) (yCentroid + (Math.sin(angle) * raT) + (Math.cos(angle) * rbT));
								t += 0.001;
								if(t > 2.0 * Math.PI) break;
							}
							
							return (x + y * width);
						}
						
						public void remove() { }
						
					};
				}
			};
			
			/*
			ArrayList<Integer> pixels = new ArrayList<Integer>();
			int xOld=0, yOld=0;
			int x=0, y=0;
			/*
			 * |xCentroid| +  |cos(angle) -sin(angle)| . |ra x cos(t)| 
			 * |yCentroid| +  |sin(angle)  cos(angle)|   |rb x sin(t)|
			 *
			for (double t = 0; t < 2.0 * Math.PI; ) {
				xOld = x;
				yOld = y;
				while(x == xOld && y == yOld){
					double raT = ra * Math.cos(t);
					double rbT = rb * Math.sin(t);
					x =  (int) (xCentroid + (Math.cos(angle) * raT) - (Math.sin(angle) * rbT));
					y =  (int) (yCentroid + (Math.sin(angle) * raT) + (Math.cos(angle) * rbT));
					t += 0.001;
				}
				int pixel = (x + y * width);
				pixels.add(pixel);
			}
			return pixels;
			*/
		}
		
		
		
		
	}

}
