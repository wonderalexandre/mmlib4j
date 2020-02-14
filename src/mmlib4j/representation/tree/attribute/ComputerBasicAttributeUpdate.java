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
public class ComputerBasicAttributeUpdate extends AttributeComputedIncrementallyUpdate {
	
	BasicAttribute attr[];
	int numNode;
	GrayScaleImage img;
	
	public ComputerBasicAttributeUpdate(int numNode, NodeLevelSets root, GrayScaleImage img, boolean mapCorrection[]){
		long ti = System.currentTimeMillis();
		this.numNode = numNode;
		this.attr = new BasicAttribute[numNode];
		this.img = img;
		computerAttribute(root, mapCorrection);
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
	
	public void addAttributeInNodesCT(SimpleLinkedList<NodeLevelSets> hashSet){
		for(NodeLevelSets node: hashSet){
			addAttributeInNodes(node);
		}
	} 
	
	public void addAttributeInNodesToS(SimpleLinkedList<NodeLevelSets> hashSet){
		for(NodeLevelSets node: hashSet){
			addAttributeInNodes(node);
		}
	} 
	
	public void addAttributeInNodes(NodeLevelSets node){
		node.addAttribute(Attribute.VOLUME, attr[ node.getId() ].volume);
		node.addAttribute(Attribute.ALTITUDE, attr[ node.getId() ].altitude);
	} 	
	
	public void preProcessing(NodeLevelSets node) {
		attr[node.getId()] = new BasicAttribute();
		//area e volume
		node.getAttribute(Attribute.LEVEL).value = node.getLevel();
		attr[node.getId()].volume.value = node.getCompactNodePixels().size() * node.getLevel();
		attr[node.getId()].highest = attr[node.getId()].lowest = node.getLevel(); 
	}	
	
	public void mergeChildren(NodeLevelSets node, NodeLevelSets son) {				
		attr[node.getId()].volume.value = attr[node.getId()].volume.value + son.getAttributeValue(Attribute.VOLUME);
		
		int highest = (int) (son.getAttributeValue(Attribute.ALTITUDE) + son.getLevel() - 1);
		attr[node.getId()].highest = Math.max(attr[node.getId()].highest, highest);
	
		int lowest = (int) (son.getLevel() - son.getAttributeValue(Attribute.ALTITUDE) + 1);			
		attr[node.getId()].lowest = Math.min(attr[node.getId()].lowest, lowest);
	}
	
	public void posProcessing(NodeLevelSets root) {
		root.setVolume( (int) attr[ root.getId() ].volume.value );
		
		if(root.isNodeMaxtree()){
			attr[root.getId()].altitude.value = attr[root.getId()].highest - root.getLevel() + 1; 
		}
		else{
			attr[root.getId()].altitude.value = root.getLevel() - attr[root.getId()].lowest + 1;
		}		
		
		root.addAttribute(Attribute.VOLUME, attr[ root.getId() ].volume);
		root.addAttribute(Attribute.ALTITUDE, attr[ root.getId() ].altitude);		
	}

	public class BasicAttribute {		
		int highest;
		int lowest;
		Attribute volume = new Attribute(Attribute.VOLUME);
		Attribute altitude = new Attribute(Attribute.ALTITUDE);
	}
}
