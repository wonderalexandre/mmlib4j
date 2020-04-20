package mmlib4j.representation.tree.attribute;

import java.io.File;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.filtering.AttributeFilters;
import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;

public class ComputerAttributeBasedHistogram extends AttributeComputedIncrementally{

	HistogramAttributes[] attr;
	
	public ComputerAttributeBasedHistogram(MorphologicalTree tree) {
		this(tree.getNumNodeIdMax(), tree.getRoot());
	}
	
	public ComputerAttributeBasedHistogram(int numNode, NodeLevelSets root) {
		attr = new HistogramAttributes[numNode];
		computerAttribute(root);
	}
	
	private double log2(double v) {
		if(v == 0)
			return 0;
		else
			return Math.log(v)/Math.log(2);
	}
	
	public static void loadAttribute(MorphologicalTree tree) {
		new ComputerAttributeBasedHistogram(tree).addAttributeInNodes(tree.getListNodes());
	}
	
	public void addAttributeInNodes(SimpleLinkedList<NodeLevelSets> listNodes){
		for(NodeLevelSets node: listNodes){
			addAttributeInNodes(node);
		}
	}
	
	public void addAttributeInNodes(NodeLevelSets node){
		node.addAttribute(Attribute.ENTROPY, attr[node.getId()].entropy);
		node.addAttribute(Attribute.ENERGY, attr[node.getId()].energy);
	}
	
	@Override
	public void preProcessing(NodeLevelSets node) {
		attr[node.getId()] = new HistogramAttributes();
		attr[node.getId()].init = attr[node.getId()].end = node.getLevel();		
	}

	@Override
	public void mergeChildren(NodeLevelSets node, NodeLevelSets son) {									
		if(attr[node.getId()].hist == null) {
			attr[node.getId()].hist = attr[son.getId()].hist;											
			attr[node.getId()].hist[node.getLevel()] += node.getCompactNodePixels().size();								
		} else { // after the for below the histogram of the node may changed, since its parent can have the same pointer reference
			for(int i = attr[son.getId()].init ; i <= attr[son.getId()].end ; i++) {
				attr[node.getId()].hist[i] += attr[son.getId()].hist[i];					
			}
		}			
		attr[node.getId()].init = Math.min(attr[node.getId()].init, attr[son.getId()].init);
		attr[node.getId()].end = Math.max(attr[node.getId()].end, attr[son.getId()].end);			
	}	

	@Override
	public void posProcessing(NodeLevelSets node) {					
		if(node.isLeaf()) {			
			attr[node.getId()].hist = new int[256];
			attr[node.getId()].hist[node.getLevel()] = node.getCompactNodePixels().size();
		} else { // here the histogram is right for the node
			for(int i = attr[node.getId()].init ; i <= attr[node.getId()].end ; i++) {	
				double prob = attr[node.getId()].hist[i] / node.getAttributeValue(Attribute.AREA);
				attr[node.getId()].energy.value += (prob * prob);
				attr[node.getId()].entropy.value += (prob * log2(prob));									
			}
			attr[node.getId()].entropy.value *= -1;
		}		
	}
	
	class HistogramAttributes{
		public int[] hist;
		Attribute entropy = new Attribute(Attribute.ENTROPY);
		Attribute energy = new Attribute(Attribute.ENERGY);
		int init;
		int end;
	}
	
	public static void main(String args[]) {
		
		GrayScaleImage imgInput  = ImageBuilder.openGrayImage(new File("/Users/gobber/Desktop/Images/lena.jpg"));
		//GrayScaleImage imgInput  = ImageBuilder.openGrayImage(new File("/Users/gobber/Desktop/Images/img_teste_1.png"));		
		ComponentTree tree = new ComponentTree(imgInput, AdjacencyRelation.getAdjacency4(), true);
		
		new ComputerBasicAttribute(tree.getNumNode(), tree.getRoot(), tree.getInputImage()).addAttributeInNodes(tree.getListNodes());
		new ComputerAttributeBasedHistogram(tree.getNumNode(), tree.getRoot()).addAttributeInNodes(tree.getListNodes());
		
		AttributeFilters filter = new AttributeFilters(tree);
		
		filter.simplificationTreeBySubstractiveRule(6, Attribute.ENTROPY);
		//filter.simplificationTreeBySubstractiveRule(0.01, Attribute.ENERGY);
		
		WindowImages.show(tree.reconstruction());
		
		System.out.println("Finished");
		
	}

}
