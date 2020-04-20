package mmlib4j.representation.tree.attribute;

import java.util.Iterator;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerCentralMomentAttribute extends AttributeComputedIncrementally {
	
	CentralMomentsAttribute attr[];
	int numNode;
	int widthImg;
	
	public ComputerCentralMomentAttribute(MorphologicalTree tree) {
		this(tree.getNumNodeIdMax(), tree.getRoot(), tree.getInputImage().getWidth());
	}
	
	public ComputerCentralMomentAttribute(int numNode, NodeLevelSets root, int widthImg){
		long ti = System.currentTimeMillis();
		this.numNode = numNode;
		this.widthImg = widthImg;
		attr = new CentralMomentsAttribute[numNode];
		computerAttribute(root);
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [extraction of attributes - moments]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}

	public ComputerCentralMomentAttribute(){	}
	
	
	public static void loadAttribute(MorphologicalTree tree) {
		new ComputerCentralMomentAttribute(tree).addAttributeInNodes(tree.getListNodes());
	}
	
	public CentralMomentsAttribute[] getAttribute(){
		return attr;
	}	
	
	/**
	 * 
	 * 	This method add the computed attributes in the list of nodes passed by parameter.
	 * 
	 * 	@param listNodes A list of nodes.
	 * 
	 */
	public void addAttributeInNodes(SimpleLinkedList<NodeLevelSets> listNodes){
		for(NodeLevelSets node: listNodes){
			addAttributeInNodes(node);
		}
	}
	
	/**
	 * 
	 * 	This method add the computed attributes in the list of nodes (Tree of Shapes) passed by parameter.
	 * 
	 * 	@param listNodes A list of nodes.
	 * 
	 *	@deprecated use {@link #addAttributeInNodes(SimpleLinkedList)} instead. 
	 * 
	 */
	@Deprecated
	public void addAttributeInNodesCT(SimpleLinkedList<NodeLevelSets> listNodes){
		for(NodeLevelSets node: listNodes){
			addAttributeInNodes(node);
		}
	} 
	
	/**
	 * 
	 * 	This method add the computed attributes in the list of nodes (Tree of Shapes) passed by parameter.
	 * 
	 * 	@param listNodes A list of nodes.
	 * 
	 *	@deprecated use {@link #addAttributeInNodes(SimpleLinkedList)} instead. 
	 * 
	 */
	@Deprecated
	public void addAttributeInNodesToS(SimpleLinkedList<NodeLevelSets> listNodes){
		for(NodeLevelSets node: listNodes){
			addAttributeInNodes(node);
		}
	} 
	
	public void addAttributeInNodes(NodeLevelSets node){
		node.addAttribute(Attribute.MOMENT_CENTRAL_02, attr[ node.getId() ].moment02);
		node.addAttribute(Attribute.MOMENT_CENTRAL_20, attr[ node.getId() ].moment20);
		node.addAttribute(Attribute.MOMENT_CENTRAL_11, attr[ node.getId() ].moment11);
		node.addAttribute(Attribute.SUM_LEVEL_2, new Attribute(Attribute.SUM_LEVEL_2, attr[node.getId()].sumLevel2));
		node.addAttribute(Attribute.VARIANCE_LEVEL, attr[ node.getId() ].variance);
		node.addAttribute(Attribute.LEVEL_MEAN, attr[ node.getId() ].levelMean);
		node.addAttribute(Attribute.STD_LEVEL, new Attribute(Attribute.STD_LEVEL, Math.sqrt( attr[ node.getId() ].variance.value) ));
		node.addAttribute(Attribute.MOMENT_COMPACTNESS, new Attribute(Attribute.MOMENT_COMPACTNESS, attr[ node.getId() ].compactness()));
		node.addAttribute(Attribute.MOMENT_ECCENTRICITY, new Attribute(Attribute.MOMENT_ECCENTRICITY, attr[ node.getId() ].eccentricity()));
		node.addAttribute(Attribute.MOMENT_ELONGATION, new Attribute(Attribute.MOMENT_ELONGATION, attr[ node.getId() ].elongation()));
		node.addAttribute(Attribute.MOMENT_LENGTH_MAJOR_AXES, new Attribute(Attribute.MOMENT_LENGTH_MAJOR_AXES, attr[ node.getId() ].getLengthMajorAxes()));
		node.addAttribute(Attribute.MOMENT_LENGTH_MINOR_AXES, new Attribute(Attribute.MOMENT_LENGTH_MINOR_AXES, attr[ node.getId() ].getLengthMinorAxes()));
		node.addAttribute(Attribute.MOMENT_ORIENTATION, new Attribute(Attribute.MOMENT_ORIENTATION, attr[ node.getId() ].getMomentOrientation()));
		node.addAttribute(Attribute.MOMENT_ASPECT_RATIO, new Attribute(Attribute.MOMENT_ASPECT_RATIO, attr[ node.getId() ].getLengthMinorAxes() /  attr[ node.getId() ].getLengthMajorAxes() ));
		node.addAttribute(Attribute.MOMENT_OF_INERTIA, new Attribute(Attribute.MOMENT_OF_INERTIA, attr[ node.getId() ].getMomentOfInertia()) );
	}
	
	public void preProcessing(NodeLevelSets node) {
		attr[node.getId()] = new CentralMomentsAttribute(node, widthImg);
		//area e volume		
		for(int pixel: node.getCompactNodePixels()){
			int x = pixel % widthImg;
			int y = pixel / widthImg;			
			// Note that, this optimization is only for p=2 and q=2			
			attr[node.getId()].sumX2 += x*x;
			attr[node.getId()].sumY2 += y*y;
		}		
		attr[node.getId()].sumLevel2 += Math.pow(node.getLevel(), 2) * node.getCompactNodePixels().size();
	}
	
	public void mergeChildren(NodeLevelSets node, NodeLevelSets son) {
		attr[node.getId()].sumX2 += attr[son.getId()].sumX2;
		attr[node.getId()].sumY2 += attr[son.getId()].sumY2;
		attr[node.getId()].sumLevel2 += attr[son.getId()].sumLevel2; 
	}

	public void posProcessing(NodeLevelSets node) {				
		//pos-processing root		
		double SumSq = attr[node.getId()].sumLevel2;
		double Sum = node.getAttributeValue(Attribute.VOLUME);
		double n = node.getAttributeValue(Attribute.AREA);				
		attr[node.getId()].variance.value = (SumSq - Math.pow(Sum, 2)/n)/n; 		
		// update		
		attr[node.getId()].moment02.value = attr[node.getId()].sumY2 - Math.pow(node.getAttributeValue(Attribute.SUM_Y), 2) / n;
		attr[node.getId()].moment20.value = attr[node.getId()].sumX2 - Math.pow(node.getAttributeValue(Attribute.SUM_X), 2) / n;		
	}
	
	public static CentralMomentsAttribute getInstance(NodeLevelSets node, int widthImg){
		CentralMomentsAttribute c = new ComputerCentralMomentAttribute().new CentralMomentsAttribute();
		c.area = node.getAttributeValue(Attribute.AREA);		
		int xc = (int)node.getAttributeValue(Attribute.SUM_X) / (int)node.getAttributeValue(Attribute.AREA);
		int yc = (int)node.getAttributeValue(Attribute.SUM_Y) / (int)node.getAttributeValue(Attribute.AREA);
		int centroid = (xc + yc * widthImg);		
		c.xCentroid = centroid % widthImg;
		c.yCentroid = centroid / widthImg;
		c.width = widthImg;
		c.moment02 = node.getAttribute(Attribute.MOMENT_CENTRAL_02);
		c.moment20 = node.getAttribute(Attribute.MOMENT_CENTRAL_20);		
		//
		// Is it not leading to the zero-sum mean property?
		// https://stats.stackexchange.com/questions/287718/zero-sum-property-of-the-difference-between-the-data-and-the-mean
		//
		c.moment11 = node.getAttribute(Attribute.MOMENT_CENTRAL_11);		
		c.variance = node.getAttribute(Attribute.VARIANCE_LEVEL);
		return c;
	}
	
	
	public class CentralMomentsAttribute {

		Attribute moment20 = new Attribute(Attribute.MOMENT_CENTRAL_20);
		Attribute moment02 = new Attribute(Attribute.MOMENT_CENTRAL_02);		
		//
		// Is it not leading to the zero-sum mean property?
		// https://stats.stackexchange.com/questions/287718/zero-sum-property-of-the-difference-between-the-data-and-the-mean
		//
		Attribute moment11 = new Attribute(Attribute.MOMENT_CENTRAL_11);
		Attribute variance = new Attribute(Attribute.VARIANCE_LEVEL);
		Attribute levelMean = new Attribute(Attribute.LEVEL_MEAN);
		
		double area; //moment00
		double xCentroid;
		double yCentroid;
		int width;
		
		// To compute moments and variance
		double sumLevel2;
		double sumX2;
		double sumY2;
		
		
		public CentralMomentsAttribute(){}
		
		public CentralMomentsAttribute(NodeLevelSets node, int width){
			this.area = (double) node.getAttributeValue(Attribute.AREA);
			int xc = (int)node.getAttributeValue(Attribute.SUM_X) / (int)node.getAttributeValue(Attribute.AREA);
			int yc = (int)node.getAttributeValue(Attribute.SUM_Y) / (int)node.getAttributeValue(Attribute.AREA);			
			int centroid = (xc + yc * width);
			this.xCentroid = centroid % width;
			this.yCentroid = centroid / width;																
			this.width = width;
			this.levelMean = new Attribute(Attribute.LEVEL_MEAN,  node.getAttributeValue(Attribute.VOLUME) / node.getAttributeValue(Attribute.AREA));

		}
						
		//=> moment[p][q] / norm;
		public double getFatorNormalized(int p, int q){
			return Math.pow(area, (p + q + 2.0) / 2.0);
		}
		
		
		public double getMomentOfInertia() {
			return (moment20.value / getFatorNormalized(2,0)) + 
					(moment02.value / getFatorNormalized(0,2)); 
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
			double a = getLengthMajorAxes();
			double b = getLengthMinorAxes();
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
			//return area / (Math.PI * Math.pow(getLengthMajorAxes()*2, 2));
			return ( Math.PI * getLengthMajorAxes() ) / ( 4 * area ); 
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
