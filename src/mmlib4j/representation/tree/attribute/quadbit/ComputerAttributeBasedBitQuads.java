package mmlib4j.representation.tree.attribute.quadbit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import mmlib4j.images.impl.PixelIndexer;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.AttributeComputedIncrementally;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.utils.AdjacencyRelation;

public class ComputerAttributeBasedBitQuads extends AttributeComputedIncrementally {
	private MorphologicalTreeFiltering tree;
	private AdjacencyRelation adj;
	private BitQuadsCounting countings[];
	private QuadBitFactory qFactory;
	
	private PatternSet Q1;
	private PatternSet Q2;
	private PatternSet Q3;
	private PatternSet QD;
	private PatternSet Q4;
	
	private PatternSet Q1T;
	private PatternSet Q2T;
	private PatternSet Q3T;
	private PatternSet QDT;
	
	public ComputerAttributeBasedBitQuads(MorphologicalTreeFiltering tree, AdjacencyRelation adj) {
		this.countings = new BitQuadsCounting[tree.getNumNode()];
		this.tree = tree;
		this.qFactory = ServiceLocatorQuadBits.getSingleton().findQuadBitFactory(tree);
		tree.getInputImage().setPixelIndexer(PixelIndexer.getDefaultValueIndexer(tree.getInputImage().getWidth(), 
				tree.getInputImage().getHeight()));
		this.adj = adj;
		createPatternSets();
		computerAttribute(tree.getRoot());		
		System.out.println("My Code");
	}
	
	private void createPatternSets() {
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
	
	private void createPatternSetQ1() {
		Q1 = new PatternSet();
		Pattern Q1P1 = new Pattern(), Q1P2 = new Pattern(),	Q1P3 = new Pattern(), Q1P4 = new Pattern();
		
		Q1P1.appendQuadBit(qFactory.createNotSubsetQuadBit(0, -1)).appendQuadBit(qFactory.createNotSubsetQuadBit(1, -1)) 
			.appendQuadBit(qFactory.createNotSubsetQuadBit(1, 0));		
		Q1P2.appendQuadBit(qFactory.createNotSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createNotSubsetQuadBit(-1, -1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(0, -1));
		Q1P3.appendQuadBit(qFactory.createNotSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createNotSubsetQuadBit(-1, 1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(0, 1));
		Q1P4.appendQuadBit(qFactory.createNotSubsetQuadBit(1, 0)).appendQuadBit(qFactory.createNotSubsetQuadBit(1, 1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(0, 1));
		
		Q1.appendPattern(Q1P1).appendPattern(Q1P2).appendPattern(Q1P3).appendPattern(Q1P4);
	}
	
	private void createPatternSetQ2() {
		Q2 = new PatternSet();
		Pattern Q2P1 = new Pattern(), Q2P2 = new Pattern(), Q2P3 = new Pattern(), Q2P4 = new Pattern(),
				Q2P5 = new Pattern(), Q2P6 = new Pattern(), Q2P7 = new Pattern(), Q2P8 = new Pattern();
		
		Q2P1.appendQuadBit(qFactory.createSubsetQuadBit(1, 0)).appendQuadBit(qFactory.createNotSubsetQuadBit(0, 1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(1, 1));
		Q2P2.appendQuadBit(qFactory.createSubsetQuadBit(0, 1)).appendQuadBit(qFactory.createNotSubsetQuadBit(-1, 1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(-1, 0));
		Q2P3.appendQuadBit(qFactory.createSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createNotSubsetQuadBit(-1, -1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(0, -1));
		Q2P4.appendQuadBit(qFactory.createSubsetQuadBit(0, -1)).appendQuadBit(qFactory.createNotSubsetQuadBit(1, -1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(1, 0));
		
		Q2P5.appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createNotSubsetQuadBit(-1, 1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(0, 1));
		Q2P6.appendQuadBit(qFactory.createProperSubsetQuadBit(0, -1)).appendQuadBit(qFactory.createNotSubsetQuadBit(-1, 0))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(-1, -1));
		Q2P7.appendQuadBit(qFactory.createProperSubsetQuadBit(1, 0)).appendQuadBit(qFactory.createNotSubsetQuadBit(1, -1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(0, -1));
		Q2P8.appendQuadBit(qFactory.createProperSubsetQuadBit(0, 1)).appendQuadBit(qFactory.createNotSubsetQuadBit(1, 1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(1, 0));
		
		Q2.appendPattern(Q2P1).appendPattern(Q2P2).appendPattern(Q2P3).appendPattern(Q2P4).appendPattern(Q2P5).appendPattern(Q2P6)
			.appendPattern(Q2P7).appendPattern(Q2P8);
	}
	
	private void createPatternSetQD() {
		QD = new PatternSet();
		Pattern QDP1 = new Pattern(), QDP2 = new Pattern(), QDP3 = new Pattern(),QDP4 = new Pattern();
		
		QDP1.appendQuadBit(qFactory.createNotSubsetQuadBit(1, 0)).appendQuadBit(qFactory.createSubsetQuadBit(1, 1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(0, 1));
		QDP2.appendQuadBit(qFactory.createNotSubsetQuadBit(0, -1)).appendQuadBit(qFactory.createSubsetQuadBit(1, -1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(1, 0));
		QDP3.appendQuadBit(qFactory.createNotSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createProperSubsetQuadBit(-1, -1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(0, -1));
		QDP4.appendQuadBit(qFactory.createNotSubsetQuadBit(0, 1)).appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(-1, 0));
		
		QD.appendPattern(QDP1).appendPattern(QDP2).appendPattern(QDP3).appendPattern(QDP4);
	}
	
	private void createPatternSetQ3() {
		Q3 = new PatternSet();
		Pattern Q3P1 = new Pattern(), Q3P2 = new Pattern(), Q3P3 = new Pattern(), Q3P4 = new Pattern(), Q3P5 = new Pattern(),
				Q3P6 = new Pattern(), Q3P7 = new Pattern(), Q3P8 = new Pattern(), Q3P9 = new Pattern(), Q3P10 = new Pattern(),
				Q3P11 = new Pattern(), Q3P12 = new Pattern();
		
		Q3P1.appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createNotSubsetQuadBit(-1, 1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(0, 1));
		Q3P2.appendQuadBit(qFactory.createProperSubsetQuadBit(1, 0)).appendQuadBit(qFactory.createNotSubsetQuadBit(1, 1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(0, 1));
		Q3P3.appendQuadBit(qFactory.createProperSubsetQuadBit(0, -1)).appendQuadBit(qFactory.createNotSubsetQuadBit(1, -1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(1, 0));
		Q3P4.appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createNotSubsetQuadBit(-1, -1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(0, -1));
		
		Q3P5.appendQuadBit(qFactory.createSubsetQuadBit(1, 0)).appendQuadBit(qFactory.createProperSubsetQuadBit(1, 1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(0, 1));
		Q3P6.appendQuadBit(qFactory.createNotSubsetQuadBit(0, 1)).appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 1))
			.appendQuadBit(qFactory.createSubsetQuadBit(-1, 0));
		Q3P7.appendQuadBit(qFactory.createSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createProperSubsetQuadBit(-1, -1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(0, -1));
		Q3P8.appendQuadBit(qFactory.createNotSubsetQuadBit(0, -1)).appendQuadBit(qFactory.createProperSubsetQuadBit(1, -1))
			.appendQuadBit(qFactory.createSubsetQuadBit(1, 0));
		
		Q3P9.appendQuadBit(qFactory.createNotSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createSubsetQuadBit(-1, -1))
			.appendQuadBit(qFactory.createSubsetQuadBit(0, -1));
		Q3P10.appendQuadBit(qFactory.createSubsetQuadBit(0, -1)).appendQuadBit(qFactory.createSubsetQuadBit(1, -1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(1, 0));
		Q3P11.appendQuadBit(qFactory.createNotSubsetQuadBit(1, 0)).appendQuadBit(qFactory.createSubsetQuadBit(1, 1))
			.appendQuadBit(qFactory.createSubsetQuadBit(0, 1));
		Q3P12.appendQuadBit(qFactory.createSubsetQuadBit(0, 1)).appendQuadBit(qFactory.createSubsetQuadBit(-1, 1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(-1, 0));
		
		Q3.appendPattern(Q3P1).appendPattern(Q3P2).appendPattern(Q3P3).appendPattern(Q3P4).appendPattern(Q3P5).appendPattern(Q3P6)
			.appendPattern(Q3P7).appendPattern(Q3P8).appendPattern(Q3P9).appendPattern(Q3P10).appendPattern(Q3P11).appendPattern(Q3P12);
	}
	
	private void createPatternSetQ4() {
		Q4 = new PatternSet();
		Pattern Q4P1 = new Pattern(), Q4P2 = new Pattern(), Q4P3 = new Pattern(), Q4P4 = new Pattern();
		
		Q4P1.appendQuadBit(qFactory.createSubsetQuadBit(1, 0)).appendQuadBit(qFactory.createSubsetQuadBit(1, 1))
			.appendQuadBit(qFactory.createSubsetQuadBit(0, 1));
		Q4P2.appendQuadBit(qFactory.createSubsetQuadBit(0, 1)).appendQuadBit(qFactory.createSubsetQuadBit(-1, 1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 0));
		Q4P3.appendQuadBit(qFactory.createSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createProperSubsetQuadBit(-1, -1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(0, -1));
		Q4P4.appendQuadBit(qFactory.createProperSubsetQuadBit(0, -1)).appendQuadBit(qFactory.createProperSubsetQuadBit(1, -1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(1, 0));
		
		Q4.appendPattern(Q4P1).appendPattern(Q4P2).appendPattern(Q4P3).appendPattern(Q4P4);
	}
	
	private void createPatternSetQ1T() {
		Q1T = new PatternSet();
		Pattern Q1TP1 = new Pattern(), Q1TP2 = new Pattern(), Q1TP3 = new Pattern(), Q1TP4 = new Pattern(), Q1TP5 = new Pattern(),
				Q1TP6 = new Pattern(), Q1TP7 = new Pattern(), Q1TP8 = new Pattern(), Q1TP9 = new Pattern(), Q1TP10 = new Pattern(),
				Q1TP11 = new Pattern(), Q1TP12 = new Pattern();
		
		Q1TP1.appendQuadBit(qFactory.createNotSubsetQuadBit(0, 1)).appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(-1, 0));
		Q1TP2.appendQuadBit(qFactory.createNotSubsetQuadBit(1, 0)).appendQuadBit(qFactory.createProperSubsetQuadBit(1, 1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(0, 1));
		Q1TP3.appendQuadBit(qFactory.createNotSubsetQuadBit(0, -1)).appendQuadBit(qFactory.createProperSubsetQuadBit(1, -1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(1, 0));
		Q1TP4.appendQuadBit(qFactory.createNotSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createProperSubsetQuadBit(-1, -1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(0, -1));
		
		Q1TP5.appendQuadBit(qFactory.createNotProperSubsetQuadBit(1, 0)).appendQuadBit(qFactory.createNotSubsetQuadBit(1, 1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(0, 1));
		Q1TP6.appendQuadBit(qFactory.createProperSubsetQuadBit(0, 1)).appendQuadBit(qFactory.createNotSubsetQuadBit(-1, 1))
			.appendQuadBit(qFactory.createNotProperSubsetQuadBit(-1, 0));
		Q1TP7.appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createNotSubsetQuadBit(-1, -1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(0, -1));
		Q1TP8.appendQuadBit(qFactory.createProperSubsetQuadBit(0, -1)).appendQuadBit(qFactory.createNotSubsetQuadBit(1, -1))
			.appendQuadBit(qFactory.createNotProperSubsetQuadBit(1, 0));
		
		Q1TP9.appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createNotProperSubsetQuadBit(-1, -1))
			.appendQuadBit(qFactory.createNotProperSubsetQuadBit(0, -1));
		Q1TP10.appendQuadBit(qFactory.createNotProperSubsetQuadBit(0, -1)).appendQuadBit(qFactory.createNotProperSubsetQuadBit(1, -1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(1, 0));
		Q1TP11.appendQuadBit(qFactory.createProperSubsetQuadBit(1, 0)).appendQuadBit(qFactory.createNotProperSubsetQuadBit(1, 1))
			.appendQuadBit(qFactory.createNotProperSubsetQuadBit(0, -1));
		Q1TP12.appendQuadBit(qFactory.createNotProperSubsetQuadBit(0, 1)).appendQuadBit(qFactory.createNotProperSubsetQuadBit(-1, 1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 0));
		
		Q1T.appendPattern(Q1TP1).appendPattern(Q1TP2).appendPattern(Q1TP3).appendPattern(Q1TP4).appendPattern(Q1TP5).appendPattern(Q1TP6)
			.appendPattern(Q1TP7).appendPattern(Q1TP8).appendPattern(Q1TP9).appendPattern(Q1TP10).appendPattern(Q1TP11).appendPattern(Q1TP12);
	}
	
	private void createPatternSetQ2T() {
		Q2T = new PatternSet();
		Pattern Q2TP1 = new Pattern(), Q2TP2 = new Pattern(), Q2TP3 = new Pattern(), Q2TP4 = new Pattern(), Q2TP5 = new Pattern(),
				Q2TP6 = new Pattern(), Q2TP7 = new Pattern(), Q2TP8 = new Pattern();
		
		Q2TP1.appendQuadBit(qFactory.createProperSubsetQuadBit(0, -1)).appendQuadBit(qFactory.createProperSubsetQuadBit(1, -1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(1, 0));
		Q2TP2.appendQuadBit(qFactory.createProperSubsetQuadBit(1, 0)).appendQuadBit(qFactory.createProperSubsetQuadBit(1, 1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(0, 1));
		Q2TP3.appendQuadBit(qFactory.createProperSubsetQuadBit(0, 1)).appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(-1, 0));
		Q2TP4.appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createProperSubsetQuadBit(-1, -1))
			.appendQuadBit(qFactory.createNotSubsetQuadBit(0, -1));
		
		Q2TP5.appendQuadBit(qFactory.createNotProperSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createProperSubsetQuadBit(-1, -1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(0, -1));
		Q2TP6.appendQuadBit(qFactory.createNotProperSubsetQuadBit(0, -1)).appendQuadBit(qFactory.createProperSubsetQuadBit(1, -1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(1, 0));
		Q2TP7.appendQuadBit(qFactory.createNotProperSubsetQuadBit(1, 0)).appendQuadBit(qFactory.createProperSubsetQuadBit(1, 1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(0, 1));
		Q2TP8.appendQuadBit(qFactory.createNotProperSubsetQuadBit(0, 1)).appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 0));
		
		Q2T.appendPattern(Q2TP1).appendPattern(Q2TP2).appendPattern(Q2TP3).appendPattern(Q2TP4).appendPattern(Q2TP5).appendPattern(Q2TP6)
			.appendPattern(Q2TP7).appendPattern(Q2TP8);
	}
	
	private void createPatternSetQDT() {
		QDT = new PatternSet();
		Pattern QDTP1 = new Pattern(), QDTP2 = new Pattern(), QDTP3 = new Pattern(), QDTP4 = new Pattern();
		
		QDTP1.appendQuadBit(qFactory.createProperSubsetQuadBit(0, -1)).appendQuadBit(qFactory.createNotSubsetQuadBit(1, -1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(1, 0));
		QDTP2.appendQuadBit(qFactory.createProperSubsetQuadBit(1, 0)).appendQuadBit(qFactory.createNotSubsetQuadBit(1, 1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(0, 1));
		QDTP3.appendQuadBit(qFactory.createProperSubsetQuadBit(0, 1)).appendQuadBit(qFactory.createNotProperSubsetQuadBit(-1, 1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 0));
		QDTP4.appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createNotProperSubsetQuadBit(-1, -1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(0, -1));
		
		QDT.appendPattern(QDTP1).appendPattern(QDTP2).appendPattern(QDTP3).appendPattern(QDTP4);
	}
	
	private void createPatternSetQ3T() {
		Q3T = new PatternSet();
		Pattern Q3TP1 = new Pattern(), Q3TP2 = new Pattern(), Q3TP3 = new Pattern(), Q3TP4 = new Pattern();
		
		Q3TP1.appendQuadBit(qFactory.createProperSubsetQuadBit(0, -1)).appendQuadBit(qFactory.createProperSubsetQuadBit(1, -1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(1, 0));
		Q3TP2.appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 0)).appendQuadBit(qFactory.createProperSubsetQuadBit(-1, -1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(0, -1));
		Q3TP3.appendQuadBit(qFactory.createProperSubsetQuadBit(0, 1)).appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(-1, 0));
		Q3TP4.appendQuadBit(qFactory.createProperSubsetQuadBit(1, 0)).appendQuadBit(qFactory.createProperSubsetQuadBit(1, 1))
			.appendQuadBit(qFactory.createProperSubsetQuadBit(0, 1));
		
		Q3T.appendPattern(Q3TP1).appendPattern(Q3TP2).appendPattern(Q3TP3).appendPattern(Q3TP4);
	}
	
	@Override
	public void preProcessing(NodeLevelSets node) {
		countings[node.getId()] = new BitQuadsCounting();
		for (int p: node.getCanonicalPixels())
			computeLocalPattern(node, p);		
	}
	
	private void computeLocalPattern(NodeLevelSets node, int p) {
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
		
		
		System.out.println(parent.getId() + "  ->  " + parent.getArea());
		countings[parent.getId()].printValues();
		System.out.println();
		
		countings[parent.getId()].nQ1 = countings[parent.getId()].nQ1 + countings[parent.getId()].childrenNQ1 - countings[parent.getId()].nQ1T;
		countings[parent.getId()].nQ2 = countings[parent.getId()].nQ2 + countings[parent.getId()].childrenNQ2 - countings[parent.getId()].nQ2T;
		countings[parent.getId()].nQD = countings[parent.getId()].nQD + countings[parent.getId()].childrenNQD - countings[parent.getId()].nQDT;
		countings[parent.getId()].nQ3 = countings[parent.getId()].nQ3 + countings[parent.getId()].childrenNQ3 - countings[parent.getId()].nQ3T;
		countings[parent.getId()].nQ4 = countings[parent.getId()].nQ4 + countings[parent.getId()].childrenNQ4;
		
		addAttributeInNodes(parent);		
	}
	
	public void addAttributeInNodesCT(HashSet<NodeLevelSets> list) {
		for (NodeLevelSets node : list)
			addAttributeInNodes(node);
	}
	
	public void addAttributeInNodes(NodeLevelSets node) {
		node.addAttribute(Attribute.BIT_QUADS_PERIMETER, new Attribute(Attribute.BIT_QUADS_PERIMETER, countings[node.getId()].getPerimeter()));
		node.addAttribute(Attribute.BIT_QUADS_NUMBER_EULER, new Attribute(Attribute.BIT_QUADS_NUMBER_EULER, countings[node.getId()].getNumberEuler(adj)));
		node.addAttribute(Attribute.BIT_QUADS_NUMBER_HOLES, new Attribute(Attribute.BIT_QUADS_NUMBER_HOLES, countings[node.getId()].getNumberOfHoles(adj)));
		node.addAttribute(Attribute.BIT_QUADS_PERIMETER_CONTINUOUS, new Attribute(Attribute.BIT_QUADS_PERIMETER_CONTINUOUS, countings[node.getId()].getPerimeterContinuous()));
		node.addAttribute(Attribute.BIT_QUADS_CIRCULARITY, new Attribute(Attribute.BIT_QUADS_CIRCULARITY, countings[node.getId()].getCircularity(node.getArea())));
		node.addAttribute(Attribute.BIT_QUADS_AREA_AVERAGE, new Attribute(Attribute.BIT_QUADS_AREA_AVERAGE, countings[node.getId()].getAreaAverage(adj, node.getArea())));
		node.addAttribute(Attribute.BIT_QUADS_PERIMETER_AVERAGE, new Attribute(Attribute.BIT_QUADS_PERIMETER_AVERAGE, countings[node.getId()].getPerimeterAverage(adj)));
		node.addAttribute(Attribute.BIT_QUADS_LENGTH_AVERAGE, new Attribute(Attribute.BIT_QUADS_LENGTH_AVERAGE, countings[node.getId()].getLengthAverage(adj)));
		node.addAttribute(Attribute.BIT_QUADS_WIDTH_AVERAGE, new Attribute(Attribute.BIT_QUADS_WIDTH_AVERAGE, countings[node.getId()].getWidthAverage(adj, node.getArea())));
		node.addAttribute(Attribute.BIT_QUADS_AREA, new Attribute(Attribute.BIT_QUADS_AREA, countings[node.getId()].getArea()));
		node.addAttribute(Attribute.BIT_QUADS_AREA_DUDA, new Attribute(Attribute.BIT_QUADS_AREA_DUDA, countings[node.getId()].getAreaDuda()));
	}

	class BitQuadsCounting {
		
		public int nQ1 = 0;
		public int nQ2 = 0;
		public int nQ3 = 0;
		public int nQ4 = 0;
		public int nQD = 0;
		public int nQ1T = 0;
		public int nQ2T = 0;
		public int nQ3T = 0;
		public int nQDT = 0;
		
		public int nQ1C4 = 0;
		public int nQ1TC4 = 0;
		
		public int childrenNQ1 = 0;
		public int childrenNQ2 = 0;
		public int childrenNQ3 = 0;
		public int childrenNQD = 0;
		public int childrenNQ4 = 0;
				
		public int getNumberEuler(AdjacencyRelation adj) {
			if (adj == AdjacencyRelation.getAdjacency4())
				return (nQ1C4 - nQ3) / 4;
			else
				return (nQ1 - nQ3 - (2 * nQD)) / 4;		
		}
		
		public int getNumberOfHoles(AdjacencyRelation adj) {
			return 1 - getNumberEuler(adj);
		}
		
		public int getPerimeter() {
			return nQ1 + nQ2 + nQ3 + (2*nQD);
		}
		
		public int getArea() {
			return (nQ1 + 2*nQ2 + 3*nQ3 + 4*nQ4 + 2*nQD) / 4;
		}
		
		public double getAreaDuda() {
			return (1.0/4.0 * nQ1 + 1.0/2.0 * nQ2 + 7.0/8.0*nQ3 + nQ4 + 3.0/4.0*nQD);
		}
		
		public double getPerimeterContinuous() {
			return nQ2 + ((nQ1 + nQ3) / 1.5); //Math.sqrt(2.0)
		}
		
		public double getCircularity(double area) {
			return (4.0 * Math.PI * area) / Math.pow(getPerimeterContinuous(), 2);
		}
		
		public double getAreaAverage(AdjacencyRelation adj, double area) {
			return (area / (double) getNumberEuler(adj));
		}
		
		public double getPerimeterAverage(AdjacencyRelation adj) {
			return (getPerimeterContinuous() / (double) getNumberEuler(adj));
		}
		
		public double getLengthAverage(AdjacencyRelation adj) {
			return (getPerimeterAverage(adj) / 2.0);
		}
		
		public double getWidthAverage(AdjacencyRelation adj, double area) {
			return (2.0 *  getAreaAverage(adj, area) / getPerimeterAverage(adj));
		}
		
		public void printValues() {
			System.out.println("nQ1: " + nQ1 + "\nnQ2: " + nQ2 + "\nnQ3: " + nQ3 + "\nnQ4: " + nQ4 + "\nnQD: " + nQD);
			System.out.println("CnQ1: " + childrenNQ1 + "\nCnQ2: " + childrenNQ2 + "\nCnQ3: " + childrenNQ3 + "\nCnQD: " + childrenNQD
					+ "\nCnQ4: " + childrenNQ4);
			System.out.println("nQ1T: " + nQ1T + "\nnQ2T: " + nQ2T + "\nnQ3T: " + nQ3T + "\nnQDT: " + nQDT);
		}
	}
	
	class Pattern
	{			
		public Pattern()
		{
			quadbits = new QuadBitTreeBased[MAX_INDEX];
			currentIndex = 0;
		}
		
		public boolean match(int px, int py)
		{
			for (QuadBitTreeBased q: quadbits) {
				if (!q.match(px, py))
					return false;
			}
			
			return true;
		}
		
		public Pattern appendQuadBit(QuadBitTreeBased quad) {			
			quadbits[currentIndex++] = quad;
			return this;
		}
		
		private QuadBitTreeBased[] quadbits;
		private int currentIndex;
		private final int MAX_INDEX = 3;
	}	
	
	class PatternSet
	{
		public PatternSet()
		{
			patterns = new ArrayList<Pattern>();
		}
		
		public int count(int px, int py) {
			int counting = 0;
			
			for (Pattern p: patterns) {				
				if (p.match(px, py))
					counting++;				
			}
			
			return counting;
		}
	
		public PatternSet appendPattern(Pattern pattern) {
			patterns.add(pattern);
			return this;
		}
		
		private List<Pattern> patterns;
	}
}