package mmlib4j.representation.tree.attribute;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerCentralMomentAttributeUpdate extends AttributeComputedIncrementallyUpdate {
	
	CentralMomentsAttribute attr[];
	int numNode;
	
	public ComputerCentralMomentAttributeUpdate(int numNode, NodeLevelSets root, boolean[] mapCorrection){
		long ti = System.currentTimeMillis();
		this.numNode = numNode;
		attr = new CentralMomentsAttribute[numNode];
		computerAttribute(root, mapCorrection);
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [extraction of attributes - moments]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}
	
	public void addAttributeInNodesCT(SimpleLinkedList<NodeLevelSets> list, boolean[] mapCorrection){
		for(NodeLevelSets node: list){
			if(mapCorrection[node.getId()])
				addAttributeInNodes(node);
		}
	}
	
	public void addAttributeInNodesCT(SimpleLinkedList<NodeLevelSets> list){
		for(NodeLevelSets node: list){
			addAttributeInNodes(node);
		}
	}
	
	public void addAttributeInNodesToS(SimpleLinkedList<NodeLevelSets> hashSet){
		for(NodeLevelSets node: hashSet){
			addAttributeInNodes(node);
		}
	} 
	
	public void addAttributeInNodes(NodeLevelSets node){	
		node.addAttribute(Attribute.VARIANCE_LEVEL, attr[node.getId()].variance);
		node.addAttribute(Attribute.LEVEL_MEAN, attr[node.getId()].levelMean);
		node.addAttribute(Attribute.STD_LEVEL, new Attribute(Attribute.STD_LEVEL, Math.sqrt(attr[node.getId()].variance.value)));		
	}
	
	public void preProcessing(NodeLevelSets node) {
		attr[node.getId()] = new CentralMomentsAttribute(node);
		attr[node.getId()].sumLevel2 += Math.pow(node.getLevel(), 2) * node.getCompactNodePixels().size();
	}
	
	public void mergeChildren(NodeLevelSets node, NodeLevelSets son) {
		double Sum = son.getAttributeValue(Attribute.VOLUME);
		double n = son.getAttributeValue(Attribute.AREA);
		double variance = son.getAttributeValue(Attribute.VARIANCE_LEVEL);
		double sumLevel2 = (n*variance) + (Math.pow(Sum, 2) / n);
		attr[node.getId()].sumLevel2 += sumLevel2; 
	}

	public void posProcessing(NodeLevelSets node) {
		double SumSq = attr[node.getId()].sumLevel2;
		double Sum = node.getAttributeValue(Attribute.VOLUME);
		double n = node.getAttributeValue(Attribute.AREA);		
		attr[node.getId()].variance.value = (SumSq - (Sum * Sum)/n)/(n);
		node.addAttribute(Attribute.VARIANCE_LEVEL, attr[node.getId()].variance);
	}
	
	public class CentralMomentsAttribute {		
		Attribute variance = new Attribute(Attribute.VARIANCE_LEVEL);
		Attribute levelMean = new Attribute(Attribute.LEVEL_MEAN);			
		double sumLevel2;						
		public CentralMomentsAttribute(NodeLevelSets node){
			this.levelMean = new Attribute(Attribute.LEVEL_MEAN, node.getVolume() / (double) node.getArea());
		}										
	}

}
