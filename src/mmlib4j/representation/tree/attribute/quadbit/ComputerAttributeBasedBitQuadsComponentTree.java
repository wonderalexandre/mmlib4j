package mmlib4j.representation.tree.attribute.quadbit;

import java.util.HashSet;

import mmlib4j.images.impl.PixelIndexer;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.quadbit.ComputerAttributeBasedBitQuads.BitQuadsCounting;
import mmlib4j.representation.tree.attribute.quadbit.maxtree.QuadBitFactoryMaxtree;
import mmlib4j.representation.tree.attribute.quadbit.mintree.QuadBitFactoryMintree;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.utils.AdjacencyRelation;

public class ComputerAttributeBasedBitQuadsComponentTree extends ComputerAttributeBasedBitQuads {
	protected ConnectedFilteringByComponentTree tree;
	protected AdjacencyRelation adj;
		
	public ComputerAttributeBasedBitQuadsComponentTree(ConnectedFilteringByComponentTree tree) {
		super(tree.getNumNode());
		this.tree = tree;
		this.adj = tree.getAdjacency();
		tree.getInputImage().setPixelIndexer(PixelIndexer.getDefaultValueIndexer(tree.getInputImage().getWidth(), 
				tree.getInputImage().getHeight()));
		if (tree.isMaxtree())
			this.qFactory = new QuadBitFactoryMaxtree(tree);
		else
			this.qFactory = new QuadBitFactoryMintree(tree);
		
		createPatternSets();
		computerAttribute(tree.getRoot());
	}
	
	protected void createPatternSets() {
		createPatternSetQ1();
		createPatternSetQ2();
		createPatternSetQD();
		createPatternSetQ3();
		createPatternSetQ4();
		
		createPatternSetQ1T();
		createPatternSetQ2T();
		createPatternSetQDT();
		createPatternSetQ3T();
	}
	
	@Override
	public void preProcessing(NodeLevelSets node) {
		countings[node.getId()] = new BitQuadsCounting();
		for (int p: node.getCanonicalPixels())
			computeLocalPattern(node, p);		
	}
	
	@Override
	public void mergeChildren(NodeLevelSets parent, NodeLevelSets son) {		
		countings[parent.getId()].childrenNQ1 += countings[son.getId()].nQ1;
		countings[parent.getId()].childrenNQ2 += countings[son.getId()].nQ2;
		countings[parent.getId()].childrenNQ3 += countings[son.getId()].nQ3;
		countings[parent.getId()].childrenNQ4 += countings[son.getId()].nQ4;
		countings[parent.getId()].childrenNQD += countings[son.getId()].nQD;
	}

	@Override
	public void posProcessing(NodeLevelSets parent) {					
		countings[parent.getId()].nQ1 = countings[parent.getId()].nQ1 + countings[parent.getId()].childrenNQ1 - countings[parent.getId()].nQ1T;
		countings[parent.getId()].nQ2 = countings[parent.getId()].nQ2 + countings[parent.getId()].childrenNQ2 - countings[parent.getId()].nQ2T;
		countings[parent.getId()].nQD = countings[parent.getId()].nQD + countings[parent.getId()].childrenNQD - countings[parent.getId()].nQDT;
		countings[parent.getId()].nQ3 = countings[parent.getId()].nQ3 + countings[parent.getId()].childrenNQ3 - countings[parent.getId()].nQ3T;
		countings[parent.getId()].nQ4 = countings[parent.getId()].nQ4 + countings[parent.getId()].childrenNQ4;
		
		addAttributeInNodes(parent, tree.getAdjacency());		
	}
	
	public void addAttributeInNodesCT(HashSet<NodeLevelSets> list, AdjacencyRelation adj) {
		for (NodeLevelSets node : list)
			addAttributeInNodes(node, adj);
	}
	
	protected void computeLocalPattern(NodeLevelSets node, int p) {
		int px = p % tree.getInputImage().getWidth();
		int py = p / tree.getInputImage().getWidth();
		
		countings[node.getId()].nQ1 += Q1.count(px, py);
		countings[node.getId()].nQ2 += Q2.count(px, py);
		countings[node.getId()].nQD += QD.count(px, py);
		countings[node.getId()].nQ3 += Q3.count(px, py);
		countings[node.getId()].nQ4 += Q4.count(px, py);
		
		countings[node.getId()].nQ1T += Q1T.count(px, py);
		countings[node.getId()].nQ2T += Q2T.count(px, py);
		countings[node.getId()].nQDT += QDT.count(px, py);
		countings[node.getId()].nQ3T += Q3T.count(px, py);
	}
	
}
