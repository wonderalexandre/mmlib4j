package mmlib4j.representation.tree.attribute;

import java.awt.Color;
import java.util.Iterator;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.gui.WindowImages;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedMSERForDIBCO;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;


/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class CentralMoments {

	double moment20;
	double moment02;
	double moment11;
	double area; 
	double xCentroid;
	double yCentroid;
	
	double perimeter = 0;
	
	int width;
	private CentralMoments(){ }
	
	public CentralMoments(double xc, double yc, int area, int width, SimpleLinkedList<Integer> pixels){
		this.area = (double) area;
		this.xCentroid = xc;
		this.yCentroid = yc;
		this.width = width;
		
		for(int pixel: pixels){
			int x = pixel % width;
			int y = pixel / width;
			moment11 += Math.pow(x - xCentroid, 1) * Math.pow(y - yCentroid, 1);
			moment20 += Math.pow(x - xCentroid, 2) * Math.pow(y - yCentroid, 0);
			moment02 += Math.pow(x - xCentroid, 0) * Math.pow(y - yCentroid, 2);
			
		}
	}
	
	
	public CentralMoments(double xc, double yc, int area, int width){
		this.area = (double) area;
		this.xCentroid = xc;
		this.yCentroid = yc;
		this.width = width;
		
	}
	
	public void addPixelOfCC(int pixel){
		int x = pixel % width;
		int y = pixel / width;
		moment11 += Math.pow(x - xCentroid, 1) * Math.pow(y - yCentroid, 1);
		moment20 += Math.pow(x - xCentroid, 2) * Math.pow(y - yCentroid, 0);
		moment02 += Math.pow(x - xCentroid, 0) * Math.pow(y - yCentroid, 2); 
	}
	
	public void updateMoment(CentralMoments m){
		this.moment11 += m.moment11;
		this.moment02 +=  m.moment02;
		this.moment20 += m.moment20;
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
		return 0.5 * Math.atan2(2 * moment11, moment20 - moment02 );
	}
	
	public double eccentricity(){
		//double a = Math.sqrt( Math.pow(moment[0][2] - moment[2][0], 2) + (4 * Math.pow(moment[1][1], 2)) );
		//return a /  (moment[2][0] + moment[0][2]);
		
		double a = moment20 + moment02 + Math.sqrt( Math.pow(moment20 - moment02, 2) + 4 * Math.pow(moment11, 2));
		double b = moment20 + moment02 - Math.sqrt( Math.pow(moment20 - moment02, 2) + 4 * Math.pow(moment11, 2));
		return a / b;
		
	}
	
	public double compactness(){
		return 1/(2*Math.PI) *   area / (moment20 + moment02); 
	}
	
	public double elongation(){
		return area / (getLengthMajorAxes() * getLengthMajorAxes());
	}
	

	public double getLengthMajorAxes(){
		double a = moment20 + moment02 + Math.sqrt( Math.pow(moment20 - moment02, 2) + 4 * Math.pow(moment11, 2));
		return Math.sqrt( (2 * a) / area );
	}

	public double getLengthMinorAxes(){
		double b = moment20 + moment02 - Math.sqrt( Math.pow(moment20 - moment02, 2) + 4 * Math.pow(moment11, 2));
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
	

	
	public static void main(String args[]){
		
		GrayScaleImage img = ImageBuilder.openGrayImage();
		ConnectedFilteringByComponentTree tree = new ConnectedFilteringByComponentTree(img, AdjacencyRelation.getAdjacency8(), true);
		PruningBasedMSERForDIBCO mser = new PruningBasedMSERForDIBCO(tree, 25);
		boolean mserNode[] = mser.getMappingSelectedNodes();
		for(NodeCT node: tree.getListNodes()){
			if(node != tree.getRoot() ){
				ColorImage imgNode = ImageFactory.createColorImage(img.getWidth(), img.getHeight());
				for(int pixel: node.getPixelsOfCC()){
					imgNode.setPixel(pixel, Color.WHITE.getRGB());
				}
				for(int q: AdjacencyRelation.getAdjacency8().getAdjacencyPixels(img, node.getCentroid())){
					imgNode.setPixel(q, Color.GREEN.getRGB());
				}
				
				//maior eixo
				double a = node.moment.getAngularCoefficientOfMajorAxes();
				double b = node.moment.getLinearCoefficientOfMajorAxes(); 
				int countMaiorEixo = 0;
				if(node.xMaxBoundBox() - node.xMinBoundBox() >= node.yMaxBoundBox() - node.yMinBoundBox()){
					
					for(int x=node.xMinBoundBox(); x <= node.xMaxBoundBox(); x++){
						int y = (int) (a*x+b);
						if(imgNode.isPixelValid(x, y)){
							imgNode.setPixel(x, y, Color.GREEN.getRGB());
							countMaiorEixo++;
						}
					}
				}else{
					for(int y=node.yMinBoundBox(); y <= node.yMaxBoundBox(); y++){
						int x = (int) ((y - b) / a);
						if(imgNode.isPixelValid(x, y)){
							imgNode.setPixel(x, y, Color.GREEN.getRGB());
							countMaiorEixo++;
						}
					}
				}
				System.out.println("Maior eixo: " + countMaiorEixo + "  =  " + 2*node.moment.getLengthMajorAxes());

				int countMenorEixo = 0;
				a = node.moment.getAngularCoefficientOfMinorAxes();
				b = node.moment.getLinearCoefficientOfMinorAxes(); 
				if(node.xMaxBoundBox() - node.xMinBoundBox() < node.yMaxBoundBox() - node.yMinBoundBox()){
					for(int x=node.xMinBoundBox(); x <= node.xMaxBoundBox(); x++){
						int y = (int) (a*x+b);
						if(imgNode.isPixelValid(x, y)){
							imgNode.setPixel(x, y, Color.GREEN.getRGB());
							countMenorEixo++;
						}
					}
				}else{
					for(int y=node.yMinBoundBox(); y <= node.yMaxBoundBox(); y++){
						int x = (int) ((y - b) / a);
						if(imgNode.isPixelValid(x, y)){
							imgNode.setPixel(x, y, Color.GREEN.getRGB());
							countMenorEixo++;
						}
					}
				}
				System.out.println("Menor eixo: " + countMenorEixo + "  =  " + 2* node.moment.getLengthMinorAxes());
				
				for(int p: node.moment.getPixelElippse()){
					int x = p % img.getWidth();
					int y = p / img.getWidth();
					if(imgNode.isPixelValid(x, y))
						imgNode.setPixel(x, y, Color.GREEN.getRGB());
					
				}
				
				//double A = 2 * node.moment.moment[1][1];
				//double B = node.moment.moment[2][0] - node.moment.moment[0][2];
				
				
				
				WindowImages.show(imgNode, "Angulo:" + Math.toDegrees(node.moment.getMomentOrientation()));
			}
		}
		
		/*
		BinaryImage imgs[] = Labeling.getSetLabelings(img, AdjacencyRelation.getAdjacency8());
		for(BinaryImage imgB: imgs){
			CentralMoments m = new CentralMoments();
		}*/
		
		
		//WindowImages.show(imgs);
		
	}
	
}
