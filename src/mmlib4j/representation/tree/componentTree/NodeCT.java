package mmlib4j.representation.tree.componentTree;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.filtering.binary.Contour;
import mmlib4j.filtering.binary.ContourTracer;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.NodeLevelSets;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class NodeCT extends NodeLevelSets implements Cloneable{
	
	
	public boolean flagProcess;
	public boolean flagPruning = true;
	
	Contour contourE = null;
	
	public NodeCT(boolean isMaxtree, int numCreate, GrayScaleImage img, int canonicalPixel){
		this.isNodeMaxtree = isMaxtree;
		this.id = numCreate;
		this.img = img;
		this.canonicalPixel = canonicalPixel; 
		this.level = img.getPixel(canonicalPixel);
		xmin = ymin = Integer.MAX_VALUE;
		xmax = ymax = Integer.MIN_VALUE;
	}
	
	public NodeLevelSets getClone(){
		try {
			NodeCT no = (NodeCT) this.clone();
			no.isClone = true;
			no.children = new SimpleLinkedList<NodeLevelSets>();
			no.pixels = new SimpleLinkedList<Integer>();
			return no;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}	

	public NodeLevelSets getAncestral(int level){
		NodeLevelSets node = this;
		if(isNodeMaxtree){
			while(node.getLevel() > level){
				node = node.getParent();
			}
		}else{
			while(node.getLevel() < level){
				node = node.getParent();
			}
		}
		return node;
	}
	
	public Contour getContour() {
		if(contourE == null){
			ContourTracer c = new ContourTracer(true, isNodeMaxtree, img, level);
			int x = pixelYmin % img.getWidth();
			int y = pixelYmin / img.getWidth(); 
			this.contourE = c.findOuterContours(x, y);
		}
		return contourE;
	}
	
	
}