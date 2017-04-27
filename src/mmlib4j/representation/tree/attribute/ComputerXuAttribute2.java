package mmlib4j.representation.tree.attribute;

import java.util.HashSet;
import java.util.Iterator;

import mmlib4j.filtering.EdgeDetectors;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerXuAttribute2 extends AttributeComputedIncrementally{

	public static void main(String args[]) throws Exception{

		int pixels5[] = new int[]{
				
				/*4, 4, 4, 4, 4, 4,
				4, 1, 1, 7, 7, 4,
				4, 1, 4, 4, 7, 4,
				4, 1, 1, 7, 7, 4,
				4, 4, 4, 4, 4, 4*/
				
				/*1,2,3,
				6,1,6,
				7,3,9*/ //			
				
				/*5,5,5,
				5,2,5,
				5,5,5*/
				
				/*1,1,1,1,1,1,
				1,0,0,3,3,1,
				1,0,1,1,3,1,
				1,0,0,3,3,1,
				1,1,1,1,1,1*/ // ok
				
				/*24,24,24,24,24,24,
				24,24, 0, 0, 0,24,
				24, 0, 6, 8, 0,24,
				24,24, 0, 0,24,24,
				24,24,24,24,24,24*/ // ok
				/*
				1,1,1,1,1,1,1,
				1,0,0,3,3,3,1,
				1,0,1,1,2,2,1,
				1,0,0,3,3,3,1,
				1,1,1,1,1,1,1
				*/
				
				1,1,1,1,1,
				1,0,3,3,1,
				1,0,3,0,1,
				1,0,3,0,1,
				1,1,1,1,1
				
		};
		
		int width = 5;
		int height = 5;

		//BuilderTreeOfShapeByUnionFindParallel2 build = new BuilderTreeOfShapeByUnionFindParallel2( ImageBuilder.openGrayImage(), -1, -1 );
	
		//BuilderTreeOfShapeByUnionFindParallel2 build = new BuilderTreeOfShapeByUnionFindParallel2( ImageFactory.createReferenceGrayScaleImage(32, pixels5, width, height), -1, -1 );
	//	ComputerXuAttribute computer = new ComputerXuAttribute(build.getNumNode(), build.getRoot(), ImageFactory.createReferenceGrayScaleImage(8, build.imgU, build.interpWidth, build.interpHeight));
		
		
		/*XuAttribute attr[] = computer.getAttribute();
		for(NodeToS node: ){
			
		}*/
		
	}
	
	
	XuAttribute attr[];
	int numNode;
	GrayScaleImage imgU;
	GrayScaleImage imgGrad;
	GrayScaleImage appear;
	GrayScaleImage vanish;
	boolean is_boundary[];
	private final static int px[] = new int[]{1, 0,-1, 0};
  	private final static int py[] = new int[]{0, 1, 0,-1};
  	
	public ComputerXuAttribute2(int numNode, NodeLevelSets root, GrayScaleImage imgU){
		long ti = System.currentTimeMillis();
		this.numNode = numNode;
		this.attr = new XuAttribute[numNode];
		this.imgU = imgU;
		appear = ImageFactory.createGrayScaleImage(32, imgU.getWidth(), imgU.getHeight());
		vanish = ImageFactory.createGrayScaleImage(32, imgU.getWidth(), imgU.getHeight());
		this.imgGrad = EdgeDetectors.sobel(imgU);
		this.is_boundary = new boolean[imgU.getSize()];
		computerAttribute(root);
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [extraction of attribute - basics]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}

	public XuAttribute[] getAttribute(){
		return attr;
	}
	
	
	public void addAttributeInNodesToS(HashSet<NodeToS> hashSet){
		for(NodeLevelSets node: hashSet){
			addAttributeInNodes(node);
		}
	} 
	
	public void addAttributeInNodes(NodeLevelSets node){
		
		/*
		node.addAttribute(Attribute.AREA, attr[ node.getId() ].area);
		//adicionar a energia
		*/
	} 
	
	
	
	public void preProcessing(NodeLevelSets node) {
		attr[node.getId()].area = node.getArea();
		for(int p: node.getCanonicalPixels()){
			attr[node.getId()].sumGray += imgU.getPixel(p);
		
		
		if(isFace2(p))
			for( Integer e : getBoundaries( p )  ) {									
				if( !is_boundary[ e ] ) {
					is_boundary[ e ] = true;										
					attr[node.getId()].contourLength += 1;
					attr[node.getId()].sumGrad += imgGrad.getPixel( e ); 
					appear.setPixel(e, p);
				} else {
					is_boundary[ e ] = false;
					attr[node.getId()].contourLength -= 1;
					attr[node.getId()].sumGrad -= imgGrad.getPixel( e );
					vanish.setPixel(e, p);
				}
			}		
		}
	}
	
	
	
	public void mergeChildren(NodeLevelSets node, NodeLevelSets son) {
		//merge
		attr[node.getId()].area += attr[son.getId()].area;
		attr[node.getId()].sumGray += attr[son.getId()].sumGray;
		attr[node.getId()].contourLength += attr[son.getId()].contourLength;
		attr[node.getId()].sumGrad += attr[son.getId()].sumGrad;
	}

	public void posProcessing(NodeLevelSets root) {
		
	}
	


	public class XuAttribute {
		int area;
		int sumGray; 
		int sumGrad;
		int contourLength;
	}
	
	

	public boolean isRealPixel( int p ) {
		return isRealPixel( p%imgU.getWidth(), p/imgU.getWidth() );
	}
	
	public boolean isRealPixel( int x, int y ) {
		return ( x%4 == 1 && y%4 == 1 );
	}

	public boolean isFace2( int x, int y ) {
		return isRealPixel(x, y) || isFakePixelSameLine(x, y) || isFakePixelNextLine(x, y);
	}

	public boolean isFace2( int p ) {
		return isRealPixel(p) || isFakePixelSameLine(p) || isFakePixelNextLine(p);
	}
	
	public boolean isFakePixelSameLine( int p ) {
		
		return isFakePixelSameLine( p%imgU.getWidth(), p/imgU.getWidth() );
		
	} 
	
	public boolean isFakePixelSameLine( int x, int y ) {
		
		return !( x % 4 == 1 ) && ( x%2 == 1 && y%2 == 1 && y%4 == 1 );
		
	}
	
	public boolean isFakePixelNextLine( int p ) {
		
		return isFakePixelNextLine( p%imgU.getWidth(), p/imgU.getWidth() );
		
	}
	
	public boolean isFakePixelNextLine( int x, int y ) {
		
		return ( x%2 == 1 && ( y-1 )%2 == 0 ) && !( y % 4 == 1 );
		
	}
	
	
	
	/**
	 * Devolve a lista de pixels do contorno do pixel de referencia, se o pixel não é um contorno do grid. 
	 * @param p => pixel de referencia
	 * @return pixel do contorno
	 */
    public Iterable<Integer> getBoundaries( int p ) {
    	
    	final int x = p % imgU.getWidth();
    	final int y = p / imgU.getWidth();
    	
        return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private int i = 0;
					public boolean hasNext() {
						while( i < px.length) {
							int xx = px[i] + x;
							int yy = py[i] + y;							
				        	if(xx >= 0 && xx < imgU.getWidth() && yy >= 0 && yy < imgU.getHeight())
								return true;
				        	i++;
						} 
						return false;
					}
					public Integer next() {
						int pixel = (px[i] + x) + (py[i] + y) * imgU.getWidth();
						i++;
						return pixel;
					}
					public void remove() { }
					
				};
			}
		};
    }
	
}
