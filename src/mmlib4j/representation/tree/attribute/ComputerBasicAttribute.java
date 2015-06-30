package mmlib4j.representation.tree.attribute;

import java.util.HashSet;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerBasicAttribute extends AttributeComputedIncrementally{
	
	BasicAttribute attr[];
	int numNode;
	GrayScaleImage img;
	boolean flagPerimeter[];
	public ComputerBasicAttribute(int numNode, NodeLevelSets root, GrayScaleImage img){
		long ti = System.currentTimeMillis();
		this.numNode = numNode;
		this.attr = new BasicAttribute[numNode];
		this.img = img;
		this.flagPerimeter = new boolean[img.getSize()];
		computerAttribute(root);
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [extraction of attribute - basics]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}

	public BasicAttribute[] getAttribute(){
		return attr;
	}
	
	public void addAttributeInNodesCT(HashSet<NodeCT> hashSet){
		for(NodeLevelSets node: hashSet){
			addAttributeInNodes(node);
		}
	} 
	
	public void addAttributeInNodesToS(HashSet<NodeToS> hashSet){
		for(NodeLevelSets node: hashSet){
			addAttributeInNodes(node);
		}
	} 
	
	public void addAttributeInNodes(NodeLevelSets node){
		node.addAttribute(Attribute.AREA, attr[ node.getId() ].area);
		node.addAttribute(Attribute.VOLUME, attr[ node.getId() ].volume);
		node.addAttribute(Attribute.WIDTH, attr[ node.getId() ].width);
		node.addAttribute(Attribute.HEIGHT, attr[ node.getId() ].height);
		node.addAttribute(Attribute.ALTITUDE, attr[ node.getId() ].altitude);
		node.addAttribute(Attribute.PERIMETER, attr[ node.getId() ].perimeter);
		node.addAttribute(Attribute.LEVEL, new Attribute(Attribute.LEVEL, node.getLevel()));
		node.addAttribute(Attribute.RECTANGULARITY, attr[ node.getId() ].rect);
		node.addAttribute(Attribute.RATIO_WIDTH_HEIGHT, attr[ node.getId() ].ratioWH);
	} 
	
	
	
	public void preProcessing(NodeLevelSets node) {
		attr[node.getId()] = new BasicAttribute();
		//area e volume
		attr[node.getId()].area.value = node.getCanonicalPixels().size();
		attr[node.getId()].volume.value = node.getCanonicalPixels().size() * node.getLevel();
		attr[node.getId()].highest = attr[node.getId()].lowest = node.getLevel(); 
		
		//largura e altura
		attr[node.getId()].xmax = node.getXmax();
		attr[node.getId()].xmin = node.getXmin();
		attr[node.getId()].ymax = node.getYmax();
		attr[node.getId()].ymin = node.getYmin();
		attr[node.getId()].pixelXmax = node.getPixelWithXmax();
		attr[node.getId()].pixelYmax = node.getPixelWithYmax();
		attr[node.getId()].pixelXmin = node.getPixelWithXmin();
		attr[node.getId()].pixelYmin = node.getPixelWithYmin();
		
		attr[node.getId()].perimeter.value = node.getNumPixelInFrame();
		
		for(int p: node.getCanonicalPixels()){
			for(int q: AdjacencyRelation.getAdjacency4().getAdjacencyPixels(img, p)){
				if(p != q){
					if( (node.isNodeMaxtree() && img.getValue(p) > img.getValue(q)) || (!node.isNodeMaxtree() && img.getValue(p) < img.getValue(q)) ){
						if(!flagPerimeter[q]){
							attr[node.getId()].perimeter.value += 1;
							flagPerimeter[q] = true;
						}else{
							flagPerimeter[q] = false;
						}
					}else if( (node.isNodeMaxtree() && img.getValue(p) < img.getValue(q)) || (!node.isNodeMaxtree() && img.getValue(p) > img.getValue(q)) ){
						if(!flagPerimeter[q]){
							attr[node.getId()].perimeter.value -= 1;
							flagPerimeter[q] = true;
						}else{
							flagPerimeter[q] = false;
						}
					}
				}
			}
		}
	}
	
	
	
	public void mergeChildren(NodeLevelSets node, NodeLevelSets son) {
		attr[node.getId()].area.value = attr[node.getId()].area.value + attr[son.getId()].area.value;
		attr[node.getId()].volume.value = attr[node.getId()].volume.value + attr[son.getId()].volume.value;
		
		if(attr[son.getId()].ymax > attr[node.getId()].ymax){
			attr[node.getId()].pixelYmax = attr[son.getId()].pixelYmax;
		}
		if(attr[son.getId()].xmax > attr[node.getId()].xmax){
			attr[node.getId()].pixelXmax = attr[son.getId()].pixelXmax;
		}
		if(attr[son.getId()].ymin <= attr[node.getId()].ymin){
			if(attr[son.getId()].ymin < attr[node.getId()].ymin)
				attr[node.getId()].pixelYmin = attr[son.getId()].pixelYmin;
			else{// if(attr[son.getId()].xmin < attr[node.getId()].xmin)
				
				int xNode = attr[node.getId()].pixelYmin % img.getWidth();
				int xSon = attr[son.getId()].pixelYmin % img.getWidth();
				if(xSon < xNode){
					attr[node.getId()].pixelYmin = attr[son.getId()].pixelYmin;	
				}
				
				
			}
		}
		if(attr[son.getId()].xmin < attr[node.getId()].xmin){
			attr[node.getId()].pixelXmin = attr[son.getId()].pixelXmin;
		}
		
		attr[node.getId()].ymax = Math.max(attr[node.getId()].ymax, attr[son.getId()].ymax);
		attr[node.getId()].xmax = Math.max(attr[node.getId()].xmax, attr[son.getId()].xmax);
		
		attr[node.getId()].ymin = Math.min(attr[node.getId()].ymin, attr[son.getId()].ymin);
		attr[node.getId()].xmin = Math.min(attr[node.getId()].xmin, attr[son.getId()].xmin);
		
		attr[node.getId()].highest = Math.max(attr[node.getId()].highest, attr[son.getId()].highest);
		attr[node.getId()].lowest = Math.min(attr[node.getId()].lowest, attr[son.getId()].lowest);
		
		attr[node.getId()].perimeter.value = attr[node.getId()].perimeter.value + attr[son.getId()].perimeter.value;
	}

	public void posProcessing(NodeLevelSets root) {
		//pos-processing root
		attr[root.getId()].width.value = attr[root.getId()].xmax - attr[root.getId()].xmin + 1;  
		attr[root.getId()].height.value = attr[root.getId()].ymax - attr[root.getId()].ymin + 1;
		root.setXmax( attr[ root.getId() ].xmax );
		root.setXmin( attr[ root.getId() ].xmin );
		root.setYmax( attr[ root.getId() ].ymax );
		root.setYmin( attr[ root.getId() ].ymin );
		root.setPixelWithXmax( attr[ root.getId() ].pixelXmax );
		root.setPixelWithYmax( attr[ root.getId() ].pixelYmax );
		root.setPixelWithXmin( attr[ root.getId() ].pixelXmin );
		root.setPixelWithYmin( attr[ root.getId() ].pixelYmin );
		
		attr[root.getId()].rect.value = root.getArea() / (attr[root.getId()].width.value * attr[root.getId()].height.value);
		attr[root.getId()].ratioWH.value =  Math.max(attr[root.getId()].width.value, attr[root.getId()].height.value) / Math.min(attr[root.getId()].width.value, attr[root.getId()].height.value);
		
		if(root.isNodeMaxtree()){
			attr[root.getId()].altitude.value = attr[root.getId()].highest - root.getLevel() + 1; 
		}
		else{
			attr[root.getId()].altitude.value = root.getLevel() - attr[root.getId()].lowest + 1;
		}
	}
	


	public class BasicAttribute {
		
		int highest;
		int lowest;
		Attribute rect = new Attribute(Attribute.RECTANGULARITY);
		Attribute ratioWH = new Attribute(Attribute.RATIO_WIDTH_HEIGHT);
		Attribute area = new Attribute(Attribute.AREA);
		Attribute volume = new Attribute(Attribute.VOLUME);
		Attribute altitude = new Attribute(Attribute.ALTITUDE);
		Attribute width = new Attribute(Attribute.WIDTH);
		Attribute height = new Attribute(Attribute.HEIGHT);
		Attribute perimeter = new Attribute(Attribute.PERIMETER);
		int xmax;
		int ymax;
		int xmin;
		int ymin;
		int pixelXmax;
		int pixelYmax;
		int pixelXmin;
		int pixelYmin;
	}
}
