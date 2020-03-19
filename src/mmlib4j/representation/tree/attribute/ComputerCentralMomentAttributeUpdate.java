package mmlib4j.representation.tree.attribute;

import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerCentralMomentAttributeUpdate extends AttributeComputedIncrementallyUpdate {
	
	int numNode;
	
	public ComputerCentralMomentAttributeUpdate(int numNode, NodeLevelSets root, boolean[] update, boolean[] modified){
		this.numNode = numNode;
		this.update = update;
		this.modified = modified;
		double ti = System.currentTimeMillis();
		computerAttribute(root);
		if(Utils.debug){
			double tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [extraction of attributes - moments]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}
	
	public void preProcessing(NodeLevelSets node) {					
		if(node.getParent() != null && !modified[node.getParent().getId()]) {				
			node.getParent().getAttribute(Attribute.SUM_LEVEL_2).value -= node.getAttributeValue(Attribute.SUM_LEVEL_2);							
		}		
		if(modified[node.getId()]) {		
			node.getAttribute(Attribute.SUM_LEVEL_2).value = Math.pow(node.getLevel(), 2) * node.getCompactNodePixels().size();
		}
	}
	
	public void mergeChildren(NodeLevelSets node, NodeLevelSets son) {			
		if(modified[node.getId()] || update[son.getId()])
			node.getAttribute(Attribute.SUM_LEVEL_2).value += son.getAttributeValue(Attribute.SUM_LEVEL_2);	
	}

	public void posProcessing(NodeLevelSets node) {		
		double SumSq = node.getAttributeValue(Attribute.SUM_LEVEL_2);
		double Sum = node.getAttributeValue(Attribute.VOLUME);
		double n = node.getAttributeValue(Attribute.AREA);				
		node.getAttribute(Attribute.VARIANCE_LEVEL).value = (SumSq - (Sum * Sum)/n)/(n);
		node.getAttribute(Attribute.STD_LEVEL).value = Math.sqrt(node.getAttributeValue(Attribute.VARIANCE_LEVEL));
		node.getAttribute(Attribute.LEVEL_MEAN).value = Sum/n;
	}	

}
