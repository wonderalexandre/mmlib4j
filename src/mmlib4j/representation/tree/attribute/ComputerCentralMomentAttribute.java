package mmlib4j.representation.tree.attribute;

import java.util.HashSet;
import java.util.Iterator;

import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerCentralMomentAttribute extends AttributeComputedIncrementally{
	
	CentralMomentsAttribute attr[];
	int numNode;
	int withImg;
	
	public ComputerCentralMomentAttribute(int numNode, NodeLevelSets root, int withImg){
		long ti = System.currentTimeMillis();
		this.numNode = numNode;
		this.withImg = withImg;
		attr = new CentralMomentsAttribute[numNode];
		computerAttribute(root);
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [extraction of attribute - moments]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}

	public ComputerCentralMomentAttribute(){	}
	
	public CentralMomentsAttribute[] getAttribute(){
		return attr;
	}
	
	public void addAttributeInNodesCT(HashSet<NodeCT> list){
		for(NodeCT node: list){
			addAttributeInNodes(node);
		}
	}
	
	public void addAttributeInNodesToS(HashSet<NodeToS> hashSet){
		for(NodeLevelSets node: hashSet){
			addAttributeInNodes(node);
		}
	} 
	
	public void addAttributeInNodes(NodeLevelSets node){
		node.addAttribute(Attribute.MOMENT_CENTRAL_02, attr[ node.getId() ].moment02);
		node.addAttribute(Attribute.MOMENT_CENTRAL_20, attr[ node.getId() ].moment20);
		node.addAttribute(Attribute.MOMENT_CENTRAL_11, attr[ node.getId() ].moment11);
		node.addAttribute(Attribute.VARIANCE_LEVEL, attr[ node.getId() ].variance);
		node.addAttribute(Attribute.LEVEL_MEAN, attr[ node.getId() ].levelMean);
		node.addAttribute(Attribute.MOMENT_COMPACTNESS, new Attribute(Attribute.MOMENT_COMPACTNESS, attr[ node.getId() ].compactness()));
		node.addAttribute(Attribute.MOMENT_ECCENTRICITY, new Attribute(Attribute.MOMENT_ECCENTRICITY, attr[ node.getId() ].eccentricity()));
		node.addAttribute(Attribute.MOMENT_ELONGATION, new Attribute(Attribute.MOMENT_ELONGATION, attr[ node.getId() ].elongation()));
		node.addAttribute(Attribute.MOMENT_LENGTH_MAJOR_AXES, new Attribute(Attribute.MOMENT_LENGTH_MAJOR_AXES, attr[ node.getId() ].getLengthMajorAxes()));
		node.addAttribute(Attribute.MOMENT_LENGTH_MINOR_AXES, new Attribute(Attribute.MOMENT_LENGTH_MINOR_AXES, attr[ node.getId() ].getLengthMinorAxes()));
		node.addAttribute(Attribute.MOMENT_ORIENTATION, new Attribute(Attribute.MOMENT_ORIENTATION, attr[ node.getId() ].getMomentOrientation()));
		node.addAttribute(Attribute.MOMENT_ASPECT_RATIO, new Attribute(Attribute.MOMENT_ASPECT_RATIO, attr[ node.getId() ].getLengthMinorAxes() /  attr[ node.getId() ].getLengthMajorAxes() ));
	}
	
	public void preProcessing(NodeLevelSets node) {
		attr[node.getId()] = new CentralMomentsAttribute(node, withImg);
		//area e volume
		
		for(int pixel: node.getCanonicalPixels()){
			int x = pixel % withImg;
			int y = pixel / withImg;
			
			attr[node.getId()].variance.value += Math.pow(node.getLevel() - attr[node.getId()].levelMean.value, 2);
			attr[node.getId()].moment11.value += Math.pow(x - attr[node.getId()].xCentroid, 1) * Math.pow(y - attr[node.getId()].yCentroid, 1);
			attr[node.getId()].moment20.value += Math.pow(x - attr[node.getId()].xCentroid, 2) * Math.pow(y - attr[node.getId()].yCentroid, 0);
			attr[node.getId()].moment02.value += Math.pow(x - attr[node.getId()].xCentroid, 0) * Math.pow(y - attr[node.getId()].yCentroid, 2);
		
		}
		
		
	}
	
	public void mergeChildren(NodeLevelSets node, NodeLevelSets son) {
		attr[node.getId()].moment11.value += attr[son.getId()].moment11.value;
		attr[node.getId()].moment02.value +=  attr[son.getId()].moment02.value;
		attr[node.getId()].moment20.value += attr[son.getId()].moment20.value;
		attr[node.getId()].variance.value += attr[son.getId()].variance.value;
	}

	public void posProcessing(NodeLevelSets node) {
		//pos-processing root
		attr[node.getId()].variance.value = attr[node.getId()].variance.value / (double) node.getArea();
	}
	
	public static CentralMomentsAttribute getInstance(NodeLevelSets node, int widthImg){
		CentralMomentsAttribute c = new ComputerCentralMomentAttribute().new CentralMomentsAttribute();
		c.area = node.getArea();
		c.xCentroid = node.getCentroid() % widthImg;
		c.yCentroid = node.getCentroid() / widthImg;
		c.width = widthImg;
		c.moment02 = node.getAttribute(Attribute.MOMENT_CENTRAL_02);
		c.moment20 = node.getAttribute(Attribute.MOMENT_CENTRAL_20);
		c.moment11 = node.getAttribute(Attribute.MOMENT_CENTRAL_11);
		c.variance = node.getAttribute(Attribute.VARIANCE_LEVEL);
		return c;
	}
	
	
	public class CentralMomentsAttribute {

		Attribute moment20 = new Attribute(Attribute.MOMENT_CENTRAL_20);
		Attribute moment02 = new Attribute(Attribute.MOMENT_CENTRAL_02);
		Attribute moment11 = new Attribute(Attribute.MOMENT_CENTRAL_11);
		Attribute variance = new Attribute(Attribute.VARIANCE_LEVEL);
		Attribute levelMean = new Attribute(Attribute.LEVEL_MEAN);
		
		double area; 
		double xCentroid;
		double yCentroid;
		int width;
		
		
		public CentralMomentsAttribute(){}
		public CentralMomentsAttribute(NodeLevelSets node, int width){
			this.area = (double) node.getArea();
			this.xCentroid = node.getCentroid() % width;
			this.yCentroid = node.getCentroid() / width;
			this.width = width;
			this.levelMean = new Attribute(Attribute.LEVEL_MEAN,  node.getVolume() / (double) node.getArea());

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
		
		/**
		 * Shows elongation: 
		 *  -  for disc, eccentricity() = 0; 
		 *  -  for line, eccentricity() = 1;
		 *  -  Less robust than compactness()
		 *  0 <= eccentricity() ≤ 1 is normalised feature of eccentricity 
		 */
		public double eccentricity(){
			double a = moment20.value + moment02.value + Math.sqrt( Math.pow(moment20.value - moment02.value, 2) + 4 * Math.pow(moment11.value, 2));
			double b = moment20.value + moment02.value - Math.sqrt( Math.pow(moment20.value - moment02.value, 2) + 4 * Math.pow(moment11.value, 2));
			return a / b;
			
		}
		
		/**
		 * Shows radial distribution of points: for disc, compactness() = 1
		 * 0 <= compactness() <= 1 is normalised feature of compactness
		 * Robust: insensitive to noise and to rotation of discrete shape
		 */
		public double compactness(){
			return ( 1 / (2*Math.PI) ) *  ( area / (moment20.value + moment02.value) ); 
		}
		

		/**
		 * Shows elongation:
		 * Defined by Xu et al, Two Applications of Shape-Based Morphology: Blood Vessels Segmentation and a Generalization of Constrained Connectivity, ISMM, 2013
		 */
		public double elongation(){
			return area / (Math.PI * Math.pow(getLengthMajorAxes(), 2));
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
			 * O codigo acima eh equivalente o codigo abaixo
			 * 
			ArrayList<Integer> pixels = new ArrayList<Integer>();
			int xOld=0, yOld=0;
			int x=0, y=0;
			/*
			 * |xCentroid| +  |cos(angle) -sin(angle)| . |ra x cos(t)| 
			 * |yCentroid| +  |sin(angle)  cos(angle)| . |rb x sin(t)|
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
