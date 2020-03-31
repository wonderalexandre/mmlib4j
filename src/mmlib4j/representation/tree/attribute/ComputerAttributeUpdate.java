package mmlib4j.representation.tree.attribute;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.NodeLevelSets;

public class ComputerAttributeUpdate extends AttributeComputedIncrementallyUpdate {
	
	BasicAttribute attr[];
	int numNode;
	GrayScaleImage img;
	int numCalls=0;
	int numPixelsM=0;
	int numM = 0;
	
	public ComputerAttributeUpdate(int numNode, 
										NodeLevelSets root, 
										GrayScaleImage img, 
										boolean update[],
										boolean modified[]){		
		this.numNode = numNode;
		this.attr = new BasicAttribute[numNode];
		this.img = img;
		this.update = update;
		this.modified = modified;
		double ti = System.currentTimeMillis();
		computerAttribute(root);	
		double tf = System.currentTimeMillis();
		double time = ((tf - ti) /1000.0d);
		
		System.out.printf("(UpdateAttributes) Our time: %.6fs, |U|:%d, |M|:%d, |M|(pixels):%d\n", time, numCalls, numM, numPixelsM);
		/*if(Utils.debug){
			double tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [extraction of attribute - basics]  "+ ((tf - ti) /1000.0)  + "s");			
		}*/
	}
	
	public BasicAttribute[] getAttribute(){
		return attr;
	}
	
	public void preProcessing(NodeLevelSets node) {		
		
		numCalls++;
		
		attr[node.getId()] = new BasicAttribute();
		//volume
		node.getAttribute(Attribute.LEVEL).value = node.getLevel();
				
		if(node.getParent() != null && !modified[node.getParent().getId()]) {				
			node.getParent().getAttribute(Attribute.VOLUME).value -= node.getAttributeValue(Attribute.VOLUME);
			node.getParent().getAttribute(Attribute.SUM_LEVEL_2).value -= node.getAttributeValue(Attribute.SUM_LEVEL_2);
		}
		
		if(modified[node.getId()]) {				
			node.getAttribute(Attribute.VOLUME).value = node.getCompactNodePixels().size() * node.getLevel();
			node.getAttribute(Attribute.SUM_LEVEL_2).value = Math.pow(node.getLevel(), 2) * node.getCompactNodePixels().size();
			//
			numPixelsM += node.getCompactNodePixels().size();
			numM++;
		}			
		
		attr[node.getId()].highest = attr[node.getId()].lowest = node.getLevel(); 
	}	
	
	public void mergeChildren(NodeLevelSets node, NodeLevelSets son) {		
		if(modified[node.getId()] || update[son.getId()]) {
			node.getAttribute(Attribute.VOLUME).value += son.getAttributeValue(Attribute.VOLUME);
			node.getAttribute(Attribute.SUM_LEVEL_2).value += son.getAttributeValue(Attribute.SUM_LEVEL_2);
		}
		
		int highest = (int) (son.getAttributeValue(Attribute.ALTITUDE) + son.getLevel() - 1);
		attr[node.getId()].highest = Math.max(attr[node.getId()].highest, highest);
	
		int lowest = (int) (son.getLevel() - son.getAttributeValue(Attribute.ALTITUDE) + 1);			
		attr[node.getId()].lowest = Math.min(attr[node.getId()].lowest, lowest);
	}
	
	public void posProcessing(NodeLevelSets root) {		
		if(root.isNodeMaxtree()){
			attr[root.getId()].altitude.value = attr[root.getId()].highest - root.getLevel() + 1; 
		}
		else{
			attr[root.getId()].altitude.value = root.getLevel() - attr[root.getId()].lowest + 1;
		}
		root.addAttribute(Attribute.ALTITUDE, attr[root.getId()].altitude);	
		double SumSq = root.getAttributeValue(Attribute.SUM_LEVEL_2);
		double Sum = root.getAttributeValue(Attribute.VOLUME);
		double n = root.getAttributeValue(Attribute.AREA);				
		root.getAttribute(Attribute.VARIANCE_LEVEL).value = (SumSq - (Sum * Sum)/n)/(n);
		root.getAttribute(Attribute.STD_LEVEL).value = Math.sqrt(root.getAttributeValue(Attribute.VARIANCE_LEVEL));
		root.getAttribute(Attribute.LEVEL_MEAN).value = Sum/n;
	}

	public class BasicAttribute {		
		int highest;
		int lowest;
		Attribute altitude = new Attribute(Attribute.ALTITUDE);
	}

}
