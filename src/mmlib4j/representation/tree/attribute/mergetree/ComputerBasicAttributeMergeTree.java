package mmlib4j.representation.tree.attribute.mergetree;


import java.util.HashMap;

import mmlib4j.representation.tree.InfoMergedTree;
import mmlib4j.representation.tree.InfoMergedTree.NodeMergedTree;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerBasicAttributeMergeTree extends AttributeUpdatedIncrementally{
	
	BasicAttribute attr[];
	int numNode;
	InfoMergedTree mTree;
	
	public ComputerBasicAttributeMergeTree(int numNode, InfoMergedTree mTree, boolean mapCorrection[]){
		long ti = System.currentTimeMillis();
		this.numNode = numNode;
		this.attr = new BasicAttribute[numNode];
		this.mTree = mTree;
		this.mapCorrection = mapCorrection;
		computerAttribute(mTree.getRoot());
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [extraction of attribute - basics]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}
	
	public void addAttributeInNodes(){
		for(NodeMergedTree node_ : mTree) {
			if(mapCorrection[node_.getId()])
				addAttributeInNode(node_);
		}
	}
	
	public void addAttributeInNode(NodeMergedTree node_){
		node_.addAttribute(Attribute.VOLUME, attr[node_.getId()].volume);
		node_.addAttribute(Attribute.ALTITUDE, attr[node_.getId()].altitude);
	} 		
	
	public void preProcessing(NodeMergedTree node_) {				
		if(!node_.isAttrModified()) {
			node_.setAttributes(new HashMap<Integer, Attribute>());
			node_.setIsAttrModified(true);
		}		
		attr[node_.getId()] = new BasicAttribute();
		//area e volume
		attr[node_.getId()].volume.value = node_.getCompactNodePixels().size() * node_.getLevel();
		attr[node_.getId()].highest = attr[node_.getId()].lowest = node_.getLevel(); 
	}	
	
	public void mergeChildrenUpdate(NodeMergedTree node_, NodeMergedTree son_) {					
		attr[node_.getId()].volume.value = attr[node_.getId()].volume.value + son_.getInfo().getAttributeValue(Attribute.VOLUME);		
		// How to compute them?
		if(son_.getInfo().isNodeMaxtree()) {			
			int highest = (int) (son_.getInfo().getAttributeValue(Attribute.ALTITUDE) + son_.getLevel() - 1);
			attr[node_.getId()].highest = Math.max(attr[node_.getId()].highest, highest);
		}
		else{
			int lowest = (int) (son_.getLevel() - son_.getInfo().getAttributeValue(Attribute.ALTITUDE) + 1);			
			attr[node_.getId()].lowest = Math.min(attr[node_.getId()].lowest, lowest);
		}
		
	}
	
	public void mergeChildren(NodeMergedTree node_, NodeMergedTree son_) {				
		attr[node_.getId()].volume.value = attr[node_.getId()].volume.value + attr[son_.getId()].volume.value;
		attr[node_.getId()].highest = Math.max(attr[node_.getId()].highest, attr[son_.getId()].highest);
		attr[node_.getId()].lowest = Math.min(attr[node_.getId()].lowest, attr[son_.getId()].lowest);		
	}

	public void posProcessing(NodeMergedTree root_) {		
		if(root_.getInfo().isNodeMaxtree()){
			attr[root_.getId()].altitude.value = attr[root_.getId()].highest - root_.getLevel() + 1; 
		}
		else{
			attr[root_.getId()].altitude.value = root_.getLevel() - attr[root_.getId()].lowest + 1;
		}
	}

	public class BasicAttribute {		
		int highest;
		int lowest;
		Attribute volume = new Attribute(Attribute.VOLUME);
		Attribute altitude = new Attribute(Attribute.ALTITUDE);
	}
}
