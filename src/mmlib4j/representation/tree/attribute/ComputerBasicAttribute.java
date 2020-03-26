package mmlib4j.representation.tree.attribute;


import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerBasicAttribute extends AttributeComputedIncrementally {
	
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
	
	public void addAttributeInNodesCT(SimpleLinkedList<NodeLevelSets> hashSet, boolean[] mapCorrection){
		for(NodeLevelSets node: hashSet){
			if(mapCorrection[node.getId()])
				addAttributeInNodes(node);
		}
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
		node.addAttribute(Attribute.AREA, attr[ node.getId() ].area);
		node.addAttribute(Attribute.VOLUME, attr[ node.getId() ].volume);
		node.addAttribute(Attribute.WIDTH, attr[ node.getId() ].width);
		node.addAttribute(Attribute.HEIGHT, attr[ node.getId() ].height);
		node.addAttribute(Attribute.ALTITUDE, attr[ node.getId() ].altitude);
		//node.addAttribute(Attribute.PERIMETER, attr[ node.getId() ].perimeter);
		node.addAttribute(Attribute.LEVEL, new Attribute(Attribute.LEVEL, node.getLevel()));
		node.addAttribute(Attribute.RECTANGULARITY, attr[ node.getId() ].rect);
		node.addAttribute(Attribute.RATIO_WIDTH_HEIGHT, attr[ node.getId() ].ratioWH);
		node.addAttribute(Attribute.SUM_X, attr[node.getId()].sumx);
		node.addAttribute(Attribute.SUM_Y, attr[node.getId()].sumy);
	} 	
	
	public void preProcessing(NodeLevelSets node) {
		attr[node.getId()] = new BasicAttribute();
		//area e volume
		attr[node.getId()].area.value = node.getCompactNodePixels().size();
		attr[node.getId()].volume.value = node.getCompactNodePixels().size() * node.getLevel();
		attr[node.getId()].highest = attr[node.getId()].lowest = node.getLevel(); 
		
		//largura e altura		
		for(int p: node.getCompactNodePixels()) {
			int x = p % img.getWidth();
			int y = p / img.getWidth();			
			if(x < attr[node.getId()].xmin){ 
				attr[node.getId()].xmin = x;
				attr[node.getId()].pixelXmin = p;
			}
			if(x > attr[node.getId()].xmax) {
				attr[node.getId()].xmax = x;
				attr[node.getId()].pixelXmax = p;
			}
			if(y <= attr[node.getId()].ymin) {
				if( y < attr[node.getId()].ymin){
					attr[node.getId()].ymin = y;
					attr[node.getId()].pixelYmin = p;
				}
				else {
					if(x < attr[node.getId()].pixelYmin % img.getWidth())
						attr[node.getId()].pixelYmin = p;
				}
			}
			if(y > attr[node.getId()].ymax){
				attr[node.getId()].ymax = y;
				attr[node.getId()].pixelYmax = p;
			}
			attr[node.getId()].sumx.value += x;
			attr[node.getId()].sumy.value += y;
		}
		
		/*attr[node.getId()].perimeter.value = node.getNumPixelInFrame();
		
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
		}*/
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
			else{				
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
		
		attr[node.getId()].sumx.value += attr[son.getId()].sumx.value;
		attr[node.getId()].sumy.value += attr[son.getId()].sumy.value;
		
		//attr[node.getId()].perimeter.value = attr[node.getId()].perimeter.value + attr[son.getId()].perimeter.value;
	}

	public void posProcessing(NodeLevelSets root) {
		//pos-processing root
		attr[root.getId()].width.value = attr[root.getId()].xmax - attr[root.getId()].xmin + 1;  
		attr[root.getId()].height.value = attr[root.getId()].ymax - attr[root.getId()].ymin + 1;
		
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
		Attribute sumx = new Attribute(Attribute.SUM_X);
		Attribute sumy = new Attribute(Attribute.SUM_Y);
		//Attribute perimeter = new Attribute(Attribute.PERIMETER);
		int xmax = Integer.MIN_VALUE;
		int ymax = Integer.MIN_VALUE;
		int xmin = Integer.MAX_VALUE;
		int ymin = Integer.MAX_VALUE;
		int pixelXmax = Integer.MIN_VALUE;
		int pixelYmax = Integer.MIN_VALUE;
		int pixelXmin = Integer.MAX_VALUE;
		int pixelYmin = Integer.MAX_VALUE;
	}
}
