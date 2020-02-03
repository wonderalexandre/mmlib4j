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
public class ComputerCentralMomentAttributeMergeTree extends AttributeUpdatedIncrementally {
	
	CentralMomentsAttribute attr[];
	int numNode;
	InfoMergedTree mTree;
	
	public ComputerCentralMomentAttributeMergeTree(int numNode, InfoMergedTree mTree, boolean[] mapCorrection){
		long ti = System.currentTimeMillis();
		this.numNode = numNode;
		attr = new CentralMomentsAttribute[numNode];
		this.mapCorrection = mapCorrection;
		this.mTree = mTree;
		computerAttribute(mTree.getRoot());
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [extraction of attributes - moments]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}
	
	public void addAttributeInNodes(){
		for(NodeMergedTree node_ : mTree) {
			if(mapCorrection[node_.getId()])
				addAttributeInNode(node_);
		}
	}
	
	public void addAttributeInNode(NodeMergedTree node_){	
		node_.addAttribute(Attribute.VARIANCE_LEVEL, attr[ node_.getId() ].variance);
		node_.addAttribute(Attribute.LEVEL_MEAN, attr[ node_.getId() ].levelMean);
		node_.addAttribute(Attribute.STD_LEVEL, new Attribute(Attribute.STD_LEVEL, Math.sqrt( attr[ node_.getId() ].variance.value) ));		
	}
	
	public void preProcessing(NodeMergedTree node_) {		
		if(!node_.isAttrModified()) {
			//node_.attributes = (HashMap<Integer, Attribute>) node_.attributes.clone();
			node_.setAttributes(new HashMap<Integer, Attribute>());
			node_.setIsAttrModified(true);
		}		
		attr[node_.getId()] = new CentralMomentsAttribute(node_);
		attr[node_.getId()].sumLevel2 += Math.pow(node_.getLevel(), 2) * node_.getCompactNodePixels().size();
	}
	
	public void mergeChildrenUpdate(NodeMergedTree node_, NodeMergedTree son_) {	
		double sumLevel2 = son_.getAttributeValue(Attribute.VARIANCE_LEVEL) + (Math.pow(son_.getAttributeValue(Attribute.VOLUME), 2) / son_.getInfo().getAttributeValue(Attribute.AREA));
		attr[node_.getId()].sumLevel2 += sumLevel2;
	}
	
	public void mergeChildren(NodeMergedTree node_, NodeMergedTree son_) {
		attr[node_.getId()].sumLevel2 += attr[son_.getId()].sumLevel2; 
	}

	public void posProcessing(NodeMergedTree node_) {
		double SumSq = attr[node_.getId()].sumLevel2;
		double Sum = node_.getAttributeValue(Attribute.VOLUME);
		double n = node_.getInfo().getAttributeValue(Attribute.AREA);		
		attr[node_.getId()].variance.value = (SumSq - (Sum * Sum)/n)/(n-1);				
	}
	
	public class CentralMomentsAttribute {		
		Attribute variance = new Attribute(Attribute.VARIANCE_LEVEL);
		Attribute levelMean = new Attribute(Attribute.LEVEL_MEAN);			
		double sumLevel2;						
		public CentralMomentsAttribute(NodeMergedTree node_){
			//System.out.println(node_.getAttributeValue(Attribute.VOLUME));
			//System.out.println(node_.info.getAttributeValue(Attribute.AREA));
			this.levelMean = new Attribute(Attribute.LEVEL_MEAN, node_.getAttributeValue(Attribute.VOLUME) / node_.getInfo().getAttributeValue(Attribute.AREA));
		}										
	}

}
