package mmlib4j.representation.tree.attribute;

import java.util.HashSet;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.INodeTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.utils.AdjacencyRelation;

public class ComputerBasicAttribute extends AttributeComputedIncrementally{
	
	BasicAttribute attr[];
	int numNode;
	GrayScaleImage img;
	INodeTree root;
	
	public ComputerBasicAttribute(int numNode, INodeTree root, GrayScaleImage img){
		this.numNode = numNode;
		this.attr = new BasicAttribute[numNode];
		this.img = img;
		this.root = root;
		computerAttribute(root);

	}

	public BasicAttribute[] getAttribute(){
		return attr;
	}
	
	public void addAttributeInNodesCT(HashSet<NodeCT> hashSet){
		for(INodeTree node: hashSet){
			node.addAttribute(Attribute.AREA, attr[ node.getId() ].area);
			node.addAttribute(Attribute.VOLUME, attr[ node.getId() ].volume);
			node.addAttribute(Attribute.WIDTH, attr[ node.getId() ].width);
			node.addAttribute(Attribute.HEIGHT, attr[ node.getId() ].height);
			node.addAttribute(Attribute.ALTITUDE, attr[ node.getId() ].altitude);
			node.addAttribute(Attribute.XMAX, attr[ node.getId() ].xmax);
			node.addAttribute(Attribute.XMIN, attr[ node.getId() ].xmin);
			node.addAttribute(Attribute.YMAX, attr[ node.getId() ].ymax);
			node.addAttribute(Attribute.YMIN, attr[ node.getId() ].ymin);
			node.addAttribute(Attribute.PIXEL_XMAX, attr[ node.getId() ].pixelXmax);
			node.addAttribute(Attribute.PIXEL_YMAX, attr[ node.getId() ].pixelYmax);
			node.addAttribute(Attribute.PIXEL_XMIN, attr[ node.getId() ].pixelXmin);
			node.addAttribute(Attribute.PIXEL_YMIN, attr[ node.getId() ].pixelYmin);
			node.addAttribute(Attribute.PERIMETER, attr[ node.getId() ].perimeter);
			node.addAttribute(Attribute.CIRCULARITY, new Attribute(Attribute.CIRCULARITY, getCircularity(node)));
			node.addAttribute(Attribute.COMPACTNESS2, new Attribute(Attribute.COMPACTNESS2, getCompacity(node)));
		}
	}
	
	
	public void preProcessing(INodeTree node) {
		attr[node.getId()] = new BasicAttribute();
		//area e volume
		attr[node.getId()].area.value = node.getCanonicalPixels().size();
		attr[node.getId()].volume.value = node.getCanonicalPixels().size() * node.getLevel();
		attr[node.getId()].highest = attr[node.getId()].lowest = node.getLevel(); 
		
		//largura e altura
		attr[node.getId()].xmax.value = node.getXmax();
		attr[node.getId()].xmin.value = node.getXmin();
		attr[node.getId()].ymax.value = node.getYmax();
		attr[node.getId()].ymin.value = node.getYmin();
		
		attr[node.getId()].perimeter.value = node.getNumPixelInFrame();
		for(int p: node.getCanonicalPixels()){
			
			for(int q: AdjacencyRelation.getAdjacency4().getAdjacencyPixels(img, p)){
				if(p != q){
					if(node.isNodeMaxtree()){
						if(img.getValue(p) > img.getValue(q)){
							attr[node.getId()].perimeter.value += 1;
							
						}else if(img.getValue(p) < img.getValue(q)){
							attr[node.getId()].perimeter.value -= 1;
						}
					}
					else if(!node.isNodeMaxtree()){
						if(img.getValue(p) < img.getValue(q)){
							attr[node.getId()].perimeter.value += 1;
							
						}else if(img.getValue(p) > img.getValue(q)){
							attr[node.getId()].perimeter.value -= 1;
						}
					}
				}
			}
		}
	}
	
	
	
	public void mergeChildren(INodeTree node, INodeTree son) {
		attr[node.getId()].area.value = attr[node.getId()].area.value + attr[son.getId()].area.value;
		attr[node.getId()].volume.value = attr[node.getId()].volume.value + attr[son.getId()].volume.value;
				
		if(attr[son.getId()].ymax.value > attr[node.getId()].ymax.value){
			attr[node.getId()].pixelYmax.value = attr[son.getId()].pixelYmax.value;
		}
		if(attr[son.getId()].xmax.value > attr[node.getId()].xmax.value){
			attr[node.getId()].pixelXmax.value = attr[son.getId()].pixelXmax.value;
		}
		if(attr[son.getId()].ymin.value < attr[node.getId()].ymin.value){
			attr[node.getId()].pixelYmin.value = attr[son.getId()].pixelYmin.value;
		}
		if(attr[son.getId()].xmin.value < attr[node.getId()].xmin.value){
			attr[node.getId()].pixelXmin.value = attr[son.getId()].pixelXmin.value;
		}
		
		attr[node.getId()].ymax.value = Math.max(attr[node.getId()].ymax.value, attr[son.getId()].ymax.value);
		attr[node.getId()].xmax.value = Math.max(attr[node.getId()].xmax.value, attr[son.getId()].xmax.value);
		
		attr[node.getId()].ymin.value = Math.min(attr[node.getId()].ymin.value, attr[son.getId()].ymin.value);
		attr[node.getId()].xmin.value = Math.min(attr[node.getId()].xmin.value, attr[son.getId()].xmin.value);
		
		attr[node.getId()].highest = Math.max(attr[node.getId()].highest, attr[son.getId()].highest);
		attr[node.getId()].lowest = Math.min(attr[node.getId()].lowest, attr[son.getId()].lowest);
		
		attr[node.getId()].perimeter.value = attr[node.getId()].perimeter.value + attr[son.getId()].perimeter.value;
	}

	public void posProcessing(INodeTree root) {
		//pos-processing root
		attr[root.getId()].width.value = attr[root.getId()].xmax.value - attr[root.getId()].xmin.value + 1;  
		attr[root.getId()].height.value = attr[root.getId()].ymax.value - attr[root.getId()].ymin.value + 1;
		
		if(root.isNodeMaxtree()){
			if(root.isLeaf())
				if(root.getParent() != null)
					attr[root.getId()].altitude.value = root.getLevel() - root.getParent().getLevel();
				else
					attr[root.getId()].altitude.value = root.getLevel();
				
			else
				attr[root.getId()].altitude.value = attr[root.getId()].highest - root.getLevel() + 1; 
		}
		else{
			if(root.isLeaf())
				if(root.getParent() != null)
					attr[root.getId()].altitude.value = root.getParent().getLevel() - root.getLevel();
				else
					attr[root.getId()].altitude.value = root.getLevel();
			else
				attr[root.getId()].altitude.value = root.getLevel() - attr[root.getId()].lowest + 1;
		}
	}
	
	
	public double getCircularity(INodeTree node){
		return (4.0 * Math.PI * node.getArea()) / Math.pow(attr[node.getId()].perimeter.value, 2);
	}
	public double getCompacity(INodeTree node){
		return Math.pow(attr[node.getId()].perimeter.value, 2) / node.getArea();
	}
	


	public class BasicAttribute {
		
		int highest;
		int lowest;
		
		Attribute area = new Attribute(Attribute.AREA);
		Attribute volume = new Attribute(Attribute.VOLUME);
		Attribute altitude = new Attribute(Attribute.ALTITUDE);
		Attribute width = new Attribute(Attribute.WIDTH);
		Attribute height = new Attribute(Attribute.HEIGHT);
		Attribute xmax = new Attribute(Attribute.XMAX);
		Attribute ymax = new Attribute(Attribute.YMAX);
		Attribute xmin = new Attribute(Attribute.XMIN);
		Attribute ymin = new Attribute(Attribute.YMIN);
		Attribute pixelXmax = new Attribute(Attribute.PIXEL_XMAX);
		Attribute pixelYmax = new Attribute(Attribute.PIXEL_YMAX);
		Attribute pixelXmin = new Attribute(Attribute.PIXEL_XMIN);
		Attribute pixelYmin = new Attribute(Attribute.PIXEL_YMIN);
		Attribute perimeter = new Attribute(Attribute.PERIMETER);	
	}
}
