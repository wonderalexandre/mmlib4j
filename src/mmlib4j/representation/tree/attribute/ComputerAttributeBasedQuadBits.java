package mmlib4j.representation.tree.attribute;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.PixelIndexer;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Dennis Jose da Silva
 * @author Wonder Alexandre Luz Alves
 * 
 * Implementation of paper:
 * 
 */
public class ComputerAttributeBasedQuadBits extends AttributeComputedIncrementally {
	
	private abstract class GrayScaleQuadBit	{
		public int xPosition;
		public int yPosition;
		
		public GrayScaleQuadBit(int xPosition, int yPosition){
			this.xPosition = xPosition;
			this.yPosition = yPosition;
		}
		
		public abstract boolean compare(int px, int py, GrayScaleImage img);
	}
	
	private class GreaterOrEqualQuadBit extends GrayScaleQuadBit {
		
		public GreaterOrEqualQuadBit(int xPosition, int yPosition) {
			super(xPosition, yPosition);			
		}

		public boolean compare(int px, int py, GrayScaleImage img) { 
			return getValue(px, py) <= getValue(px + xPosition, py + yPosition); 
		} 
	}
	
	private class GreaterQuadBit extends GrayScaleQuadBit {
		public GreaterQuadBit(int xPosition, int yPosition) {
			super(xPosition, yPosition);
		}
		
		public boolean compare(int px, int py, GrayScaleImage img) {
			return getValue(px, py) < getValue(px + xPosition, py + yPosition);
		}
	}
	
	private class LowerOrEqualQuadBit extends GrayScaleQuadBit {
		public LowerOrEqualQuadBit(int xPosition, int yPosition) {
			super(xPosition, yPosition);
		}
		
		public boolean compare(int px, int py, GrayScaleImage img) {
			return getValue(px, py) >= getValue(px + xPosition, py + yPosition); 
		}
	}
	
	private class LowerQuadBit extends GrayScaleQuadBit {
		public LowerQuadBit(int xPosition, int yPosition) {
			super(xPosition, yPosition);
		}
		
		public boolean compare(int px, int py, GrayScaleImage img) {
			return getValue(px, py) > getValue(px + xPosition, py + yPosition);
		}
	}
	
	private class GrayScalePattern {
		private List<GrayScaleQuadBit> quads;
		
		public GrayScalePattern(){
			quads = new ArrayList<ComputerAttributeBasedQuadBits.GrayScaleQuadBit>();
		}
		
		public GrayScalePattern appendQuad(GrayScaleQuadBit quad) {
			quads.add(quad);
			return this;
		}
		
		public boolean match(int px, int py, GrayScaleImage img) {
			for (GrayScaleQuadBit q : quads) {
				if (!q.compare(px, py, img))
					return false;
			}
			return true;
		}
	}
	
	private class GrayScalePatternGroup	{
		private List<GrayScalePattern> patterns;
		
		public GrayScalePatternGroup() {
			patterns = new ArrayList<ComputerAttributeBasedQuadBits.GrayScalePattern>();
		}
		
		public GrayScalePatternGroup appendPattern(GrayScalePattern pattern) {
			patterns.add(pattern);
			return this;
		}
		
		public int count(int px, int py, GrayScaleImage img) {
			int c = 0;
			for (GrayScalePattern pattern: patterns) {
				if (pattern.match(px, py, img))
					c++;
			}			
			return c;
		}
	}
	
	
	private AttributeBasedQuadBits attr[];
	private GrayScaleImage img; 
	private boolean isMaxtree;
	private int index;
	
	private GrayScalePatternGroup Q1;
	private GrayScalePatternGroup Q2;
	private GrayScalePatternGroup QD;
	private GrayScalePatternGroup Q3;
	
	private GrayScalePatternGroup Q1T;
	private GrayScalePatternGroup Q2T;
	private GrayScalePatternGroup QDT;
	private GrayScalePatternGroup Q3T;
	
	public ComputerAttributeBasedQuadBits(int numNode, NodeLevelSets root, GrayScaleImage img, AdjacencyRelation adj){
		long ti = System.currentTimeMillis();
		this.attr = new AttributeBasedQuadBits[numNode];
		this.img = img;
		img.setPixelIndexer( PixelIndexer.getDefaultValueIndexer(img.getWidth(), img.getHeight()) );
		initializePatterns();
		computerAttribute(root);
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [extraction of attribute - euler number]  "+ ((tf - ti) /1000.0)  + "s");
		}		
	}

	private void initializePatterns() {
		createQ1Patterns();
		createQ2Patterns();
		createQ3Patterns();
		createQDPatterns();
		
		createQ1TPatterns();
		createQ2TPattern();
		createQDTPattern();
		createQ3TPattern();
	}
	
	private void createQ1Patterns() {
		GrayScalePattern Q1P1 = new GrayScalePattern();
		GrayScalePattern Q1P2 = new GrayScalePattern();
		GrayScalePattern Q1P3 = new GrayScalePattern();
		GrayScalePattern Q1P4 = new GrayScalePattern();
		
		Q1P1.appendQuad(new LowerQuadBit(0, -1)).appendQuad(new LowerQuadBit(1, -1)).appendQuad(new LowerQuadBit(1, 0));
		Q1P2.appendQuad(new LowerQuadBit(-1, 0)).appendQuad(new LowerQuadBit(-1, -1)).appendQuad(new LowerQuadBit(0, -1));
		Q1P3.appendQuad(new LowerQuadBit(0, 1)).appendQuad(new LowerQuadBit(-1, 1)).appendQuad(new LowerQuadBit(-1, 0));
		Q1P4.appendQuad(new LowerQuadBit(1, 0)).appendQuad(new LowerQuadBit(1, 1)).appendQuad(new LowerQuadBit(0, 1));
		
		Q1 = new GrayScalePatternGroup();
		Q1.appendPattern(Q1P1).appendPattern(Q1P2).appendPattern(Q1P3).appendPattern(Q1P4);
	}
	
	private void createQ2Patterns() {
		GrayScalePattern Q2P1 = new GrayScalePattern();
		GrayScalePattern Q2P2 = new GrayScalePattern();
		GrayScalePattern Q2P3 = new GrayScalePattern();
		GrayScalePattern Q2P4 = new GrayScalePattern();
		
		GrayScalePattern Q2P5 = new GrayScalePattern();
		GrayScalePattern Q2P6 = new GrayScalePattern();
		GrayScalePattern Q2P7 = new GrayScalePattern();
		GrayScalePattern Q2P8 = new GrayScalePattern();
		
		Q2P1.appendQuad(new GreaterOrEqualQuadBit(1, 0)).appendQuad(new LowerQuadBit(1, 1)).appendQuad(new LowerQuadBit(0, 1));
		Q2P2.appendQuad(new GreaterOrEqualQuadBit(0, 1)).appendQuad(new LowerQuadBit(-1, 1)).appendQuad(new LowerQuadBit(-1, 0));
		Q2P3.appendQuad(new GreaterOrEqualQuadBit(-1, 0)).appendQuad(new LowerQuadBit(-1, -1)).appendQuad(new LowerQuadBit(0, -1));
		Q2P4.appendQuad(new GreaterOrEqualQuadBit(0, -1)).appendQuad(new LowerQuadBit(1, -1)).appendQuad(new LowerQuadBit(1, 0));
		
		Q2P5.appendQuad(new LowerQuadBit(0, 1)).appendQuad(new LowerQuadBit(-1, 1)).appendQuad(new GreaterQuadBit(-1, 0));
		Q2P6.appendQuad(new LowerQuadBit(-1, 0)).appendQuad(new LowerQuadBit(-1, -1)).appendQuad(new GreaterQuadBit(0, -1));
		Q2P7.appendQuad(new LowerQuadBit(0, -1)).appendQuad(new LowerQuadBit(1, -1)).appendQuad(new GreaterQuadBit(1, 0));
		Q2P8.appendQuad(new LowerQuadBit(1, 0)).appendQuad(new LowerQuadBit(1, 1)).appendQuad(new GreaterQuadBit(0, 1));
		
		Q2 = new GrayScalePatternGroup();
		Q2.appendPattern(Q2P1).appendPattern(Q2P2).appendPattern(Q2P3).appendPattern(Q2P4).appendPattern(Q2P5).appendPattern(Q2P6)
		  .appendPattern(Q2P7).appendPattern(Q2P8);
	}
	
	private void createQDPatterns() {
		GrayScalePattern QDP1 = new GrayScalePattern();
		GrayScalePattern QDP2 = new GrayScalePattern();
		GrayScalePattern QDP3 = new GrayScalePattern();
		GrayScalePattern QDP4 = new GrayScalePattern();
		
		QDP1.appendQuad(new LowerQuadBit(1, 0)).appendQuad(new GreaterOrEqualQuadBit(1, 1)).appendQuad(new LowerQuadBit(0, 1));
		QDP2.appendQuad(new LowerQuadBit(0, -1)).appendQuad(new GreaterOrEqualQuadBit(1, -1)).appendQuad(new LowerQuadBit(1, 0));
		QDP3.appendQuad(new LowerQuadBit(-1, 0)).appendQuad(new GreaterQuadBit(-1, -1)).appendQuad(new LowerQuadBit(0, -1));
		QDP4.appendQuad(new LowerQuadBit(0, 1)).appendQuad(new GreaterQuadBit(-1, 1)).appendQuad(new LowerQuadBit(-1, 0));
		
		QD = new GrayScalePatternGroup();
		QD.appendPattern(QDP1).appendPattern(QDP2).appendPattern(QDP3).appendPattern(QDP4);		
	}
	
	private void createQ3Patterns() {
		GrayScalePattern Q3P1 = new GrayScalePattern();
		GrayScalePattern Q3P2 = new GrayScalePattern();
		GrayScalePattern Q3P3 = new GrayScalePattern();
		GrayScalePattern Q3P4 = new GrayScalePattern();
		
		GrayScalePattern Q3P5 = new GrayScalePattern();
		GrayScalePattern Q3P6 = new GrayScalePattern();
		GrayScalePattern Q3P7 = new GrayScalePattern();
		GrayScalePattern Q3P8 = new GrayScalePattern();
		
		GrayScalePattern Q3P9 = new GrayScalePattern();
		GrayScalePattern Q3P10 = new GrayScalePattern();
		GrayScalePattern Q3P11 = new GrayScalePattern();
		GrayScalePattern Q3P12 = new GrayScalePattern();
		
		Q3P1.appendQuad(new GreaterQuadBit(0, 1)).appendQuad(new LowerQuadBit(-1, 1)).appendQuad(new GreaterQuadBit(-1, 0));
		Q3P2.appendQuad(new GreaterQuadBit(1, 0)).appendQuad(new LowerQuadBit(1, 1)).appendQuad(new GreaterQuadBit(0, 1));
		Q3P3.appendQuad(new GreaterQuadBit(0, -1)).appendQuad(new LowerQuadBit(1, -1)).appendQuad(new GreaterQuadBit(1, 0));
		Q3P4.appendQuad(new GreaterQuadBit(-1, 0)).appendQuad(new LowerQuadBit(-1, -1)).appendQuad(new GreaterQuadBit(0, -1));
		
		Q3P5.appendQuad(new GreaterOrEqualQuadBit(1, 0)).appendQuad(new GreaterQuadBit(1, 1)).appendQuad(new LowerQuadBit(0, 1));
		Q3P6.appendQuad(new LowerQuadBit(0, 1)).appendQuad(new GreaterQuadBit(-1, 1)).appendQuad(new GreaterOrEqualQuadBit(-1, 0));
		Q3P7.appendQuad(new GreaterOrEqualQuadBit(-1, 0)).appendQuad(new GreaterQuadBit(-1, -1)).appendQuad(new LowerQuadBit(0, -1));
		Q3P8.appendQuad(new LowerQuadBit(0, -1)).appendQuad(new GreaterQuadBit(1, -1)).appendQuad(new GreaterOrEqualQuadBit(1, 0));
		
		Q3P9.appendQuad(new LowerQuadBit(-1, 0)).appendQuad(new GreaterOrEqualQuadBit(-1, -1)).appendQuad(new GreaterOrEqualQuadBit(0, -1));
		Q3P10.appendQuad(new GreaterOrEqualQuadBit(0, -1)).appendQuad(new GreaterOrEqualQuadBit(1, -1)).appendQuad(new LowerQuadBit(1, 0));
		Q3P11.appendQuad(new LowerQuadBit(1, 0)).appendQuad(new GreaterOrEqualQuadBit(1, 1)).appendQuad(new GreaterOrEqualQuadBit(0, 1));
		Q3P12.appendQuad(new GreaterOrEqualQuadBit(0, 1)).appendQuad(new GreaterOrEqualQuadBit(-1, 1)).appendQuad(new LowerQuadBit(-1, 0));
		
		Q3 = new GrayScalePatternGroup();
		Q3.appendPattern(Q3P1).appendPattern(Q3P2).appendPattern(Q3P3).appendPattern(Q3P4).appendPattern(Q3P5).appendPattern(Q3P6)
		  .appendPattern(Q3P7).appendPattern(Q3P8).appendPattern(Q3P9).appendPattern(Q3P10).appendPattern(Q3P11).appendPattern(Q3P12);
	}
	
	private void createQ1TPatterns(){
		GrayScalePattern Q1TP1 = new GrayScalePattern();
		GrayScalePattern Q1TP2 = new GrayScalePattern();
		GrayScalePattern Q1TP3 = new GrayScalePattern();
		GrayScalePattern Q1TP4 = new GrayScalePattern();
		
		GrayScalePattern Q1TP5 = new GrayScalePattern();
		GrayScalePattern Q1TP6 = new GrayScalePattern();
		GrayScalePattern Q1TP7 = new GrayScalePattern();
		GrayScalePattern Q1TP8 = new GrayScalePattern();
		
		GrayScalePattern Q1TP9 = new GrayScalePattern();
		GrayScalePattern Q1TP10 = new GrayScalePattern();
		GrayScalePattern Q1TP11 = new GrayScalePattern();
		GrayScalePattern Q1TP12 = new GrayScalePattern();
		
		Q1TP1.appendQuad(new LowerQuadBit(0, 1)).appendQuad(new GreaterQuadBit(-1, 1)).appendQuad(new LowerQuadBit(-1, 0));
		Q1TP2.appendQuad(new LowerQuadBit(1, 0)).appendQuad(new GreaterQuadBit(1, 1)).appendQuad(new LowerQuadBit(0, 1));
		Q1TP3.appendQuad(new LowerQuadBit(0, -1)).appendQuad(new GreaterQuadBit(1, -1)).appendQuad(new LowerQuadBit(1, 0));
		Q1TP4.appendQuad(new LowerQuadBit(-1, 0)).appendQuad(new GreaterQuadBit(-1, -1)).appendQuad(new LowerQuadBit(0, -1));
		
		Q1TP5.appendQuad(new LowerOrEqualQuadBit(1, 0)).appendQuad(new LowerQuadBit(1, 1)).appendQuad(new GreaterQuadBit(0, 1));
		Q1TP6.appendQuad(new GreaterQuadBit(0, 1)).appendQuad(new LowerQuadBit(-1, 1)).appendQuad(new LowerOrEqualQuadBit(-1, 0));
		Q1TP7.appendQuad(new LowerOrEqualQuadBit(-1, 0)).appendQuad(new LowerQuadBit(-1, -1)).appendQuad(new GreaterQuadBit(0, -1));
		Q1TP8.appendQuad(new GreaterQuadBit(0, -1)).appendQuad(new LowerQuadBit(1, -1)).appendQuad(new LowerOrEqualQuadBit(1, 0));
		
		Q1TP9.appendQuad(new GreaterQuadBit(-1, 0)).appendQuad(new LowerOrEqualQuadBit(-1,-1)).appendQuad(new LowerOrEqualQuadBit(0, -1));
		Q1TP10.appendQuad(new LowerOrEqualQuadBit(0, -1)).appendQuad(new LowerOrEqualQuadBit(1, -1)).appendQuad(new GreaterQuadBit(1, 0));
		Q1TP11.appendQuad(new GreaterQuadBit(1, 0)).appendQuad(new LowerOrEqualQuadBit(1, 1)).appendQuad(new LowerOrEqualQuadBit(0, 1));
		Q1TP12.appendQuad(new LowerOrEqualQuadBit(0, 1)).appendQuad(new LowerOrEqualQuadBit(-1, 1)).appendQuad(new GreaterQuadBit(-1, 0));
		
		Q1T = new GrayScalePatternGroup();
		Q1T.appendPattern(Q1TP1).appendPattern(Q1TP2).appendPattern(Q1TP3).appendPattern(Q1TP4).appendPattern(Q1TP5).appendPattern(Q1TP6)
		   .appendPattern(Q1TP7).appendPattern(Q1TP8).appendPattern(Q1TP9).appendPattern(Q1TP10).appendPattern(Q1TP11).appendPattern(Q1TP12);
	}
	
	public void createQ2TPattern()	{
		GrayScalePattern Q2TP1 = new GrayScalePattern();
		GrayScalePattern Q2TP2 = new GrayScalePattern();
		GrayScalePattern Q2TP3 = new GrayScalePattern();
		GrayScalePattern Q2TP4 = new GrayScalePattern();
		
		GrayScalePattern Q2TP5 = new GrayScalePattern();
		GrayScalePattern Q2TP6 = new GrayScalePattern();
		GrayScalePattern Q2TP7 = new GrayScalePattern();
		GrayScalePattern Q2TP8 = new GrayScalePattern();
		
		Q2TP1.appendQuad(new GreaterQuadBit(0, -1)).appendQuad(new GreaterQuadBit(1, -1)).appendQuad(new LowerQuadBit(1, 0));
		Q2TP2.appendQuad(new GreaterQuadBit(1, 0)).appendQuad(new GreaterQuadBit(1, 1)).appendQuad(new LowerQuadBit(0, 1));
		Q2TP3.appendQuad(new GreaterQuadBit(0, 1)).appendQuad(new GreaterQuadBit(-1, 1)).appendQuad(new LowerQuadBit(-1, 0));
		Q2TP4.appendQuad(new GreaterQuadBit(-1, 0)).appendQuad(new GreaterQuadBit(-1, -1)).appendQuad(new LowerQuadBit(0, -1));
		
		Q2TP5.appendQuad(new LowerOrEqualQuadBit(-1, 0)).appendQuad(new GreaterQuadBit(-1, -1)).appendQuad(new GreaterQuadBit(0, -1));
		Q2TP6.appendQuad(new LowerOrEqualQuadBit(0, -1)).appendQuad(new GreaterQuadBit(1, -1)).appendQuad(new GreaterQuadBit(1, 0));
		Q2TP7.appendQuad(new LowerOrEqualQuadBit(1, 0)).appendQuad(new GreaterQuadBit(1, 1)).appendQuad(new GreaterQuadBit(0, 1));
		Q2TP8.appendQuad(new LowerOrEqualQuadBit(0, 1)).appendQuad(new GreaterQuadBit(-1, 1)).appendQuad(new GreaterQuadBit(-1, 0));
		
		Q2T = new GrayScalePatternGroup();
		Q2T.appendPattern(Q2TP1).appendPattern(Q2TP2).appendPattern(Q2TP3).appendPattern(Q2TP4).appendPattern(Q2TP5).appendPattern(Q2TP6)
		   .appendPattern(Q2TP7).appendPattern(Q2TP8);
	}
	
	public void createQDTPattern() {
		GrayScalePattern QDTP1 = new GrayScalePattern();
		GrayScalePattern QDTP2 = new GrayScalePattern();
		GrayScalePattern QDTP3 = new GrayScalePattern();
		GrayScalePattern QDTP4 = new GrayScalePattern();
		
		QDTP1.appendQuad(new GreaterQuadBit(0, -1)).appendQuad(new LowerQuadBit(1, -1)).appendQuad(new GreaterQuadBit(1, 0));
		QDTP2.appendQuad(new GreaterQuadBit(1, 0)).appendQuad(new LowerQuadBit(1, 1)).appendQuad(new GreaterQuadBit(0, 1));
		QDTP3.appendQuad(new GreaterQuadBit(0, 1)).appendQuad(new LowerQuadBit(-1, 1)).appendQuad(new GreaterQuadBit(-1, 0));
		QDTP4.appendQuad(new GreaterQuadBit(-1, 0)).appendQuad(new LowerQuadBit(-1, -1)).appendQuad(new GreaterQuadBit(0, -1));
		
		QDT = new GrayScalePatternGroup();
		QDT.appendPattern(QDTP1).appendPattern(QDTP2).appendPattern(QDTP3).appendPattern(QDTP4);
	}
	
	public void createQ3TPattern() {
		GrayScalePattern Q3TP1 = new GrayScalePattern();
		GrayScalePattern Q3TP2 = new GrayScalePattern();
		GrayScalePattern Q3TP3 = new GrayScalePattern();
		GrayScalePattern Q3TP4 = new GrayScalePattern();
		
		Q3TP1.appendQuad(new GreaterQuadBit(0, -1)).appendQuad(new GreaterQuadBit(1, -1)).appendQuad(new GreaterQuadBit(1, 0));
		Q3TP2.appendQuad(new GreaterQuadBit(-1, 0)).appendQuad(new GreaterQuadBit(-1, -1)).appendQuad(new GreaterQuadBit(0, -1));
		Q3TP3.appendQuad(new GreaterQuadBit(0, 1)).appendQuad(new GreaterQuadBit(-1, 1)).appendQuad(new GreaterQuadBit(-1, 0));
		Q3TP4.appendQuad(new GreaterQuadBit(1, 0)).appendQuad(new GreaterQuadBit(1, 1)).appendQuad(new GreaterQuadBit(0, 1));
		
		Q3T = new GrayScalePatternGroup();
		Q3T.appendPattern(Q3TP1).appendPattern(Q3TP2).appendPattern(Q3TP3).appendPattern(Q3TP4);
	}

	public void addAttributeInNodesCT(HashSet<NodeCT> list){
		for(NodeCT node: list){
			addAttributeInNodes(node);
		}
	}
	
	public void addAttributeInNodesToS(HashSet<NodeToS> hashSet){
		for(NodeLevelSets node: hashSet){
			addAttributeInNodes(node);
		}
	} 
	
	public void addAttributeInNodes(NodeLevelSets node){
		node.addAttribute(Attribute.NUM_HOLES, new Attribute(Attribute.NUM_HOLES, attr[ node.getId() ].getNumberHoles()));
		node.addAttribute(Attribute.PERIMETERS_QUAD, new Attribute(Attribute.PERIMETERS_QUAD, attr[ node.getId() ].getPerimeter()));
	}
	
	
	public void preProcessing(NodeLevelSets node) {
		attr[node.getId()] = new AttributeBasedQuadBits();
		this.isMaxtree = node.isNodeMaxtree();
		
		for(int p: node.getCanonicalPixels()){
			computerLocalPattern(node, p);
		}		
		
		System.out.println(attr[node.getId()].printPattern());
	}
	
	public void mergeChildren(NodeLevelSets node, NodeLevelSets son) {
		attr[node.getId()].countPatternChildrenC1 += attr[son.getId()].countPatternC1;
		attr[node.getId()].countPatternChildrenC2 += attr[son.getId()].countPatternC2;
		attr[node.getId()].countPatternChildrenCD += attr[son.getId()].countPatternCD;
		attr[node.getId()].countPatternChildrenC3 += attr[son.getId()].countPatternC3;
	}

	public void posProcessing(NodeLevelSets root) {
		//pos-processing root
		attr[root.getId()].countPatternC1 = attr[root.getId()].countPatternC1 + attr[root.getId()].countPatternChildrenC1 - attr[root.getId()].countPatternCT1;
		attr[root.getId()].countPatternC2 = attr[root.getId()].countPatternC2 + attr[root.getId()].countPatternChildrenC2 - attr[root.getId()].countPatternCT2;
		attr[root.getId()].countPatternC3 = attr[root.getId()].countPatternC3 + attr[root.getId()].countPatternChildrenC3 - attr[root.getId()].countPatternCT3;
		attr[root.getId()].countPatternCD = attr[root.getId()].countPatternCD + attr[root.getId()].countPatternChildrenCD - attr[root.getId()].countPatternCTD;
	}
	
	private int getValue(int x, int y){
		index = img.getIndex(x, y);
		if(isMaxtree)
			if(index == -1)
				return -1;
			else
				return img.getPixel(x, y);
		else
			if(index == -1)
				return 256;
			else
				return 255 - img.getPixel(x, y);
	}
	
	private void computerLocalPattern(NodeLevelSets node, int p) {
		int px = p % img.getWidth();
		int py = p / img.getWidth();
		
		attr[node.getId()].countPatternC1 += Q1.count(px, py, img);
		attr[node.getId()].countPatternC2 += Q2.count(px, py, img);
		attr[node.getId()].countPatternCD += QD.count(px, py, img);
		attr[node.getId()].countPatternC3 += Q3.count(px, py, img);
		
		attr[node.getId()].countPatternCT1 += Q1T.count(px, py, img);
		attr[node.getId()].countPatternCT2 += Q2T.count(px, py, img);
		attr[node.getId()].countPatternCTD += QDT.count(px, py, img);
		attr[node.getId()].countPatternCT3 += Q3T.count(px, py, img);
	}
	
	
	 public class AttributeBasedQuadBits {
	
		public int countPatternCD = 0;
		public int countPatternC3 = 0;
		public int countPatternC2 = 0;
		public int countPatternC1 = 0;
		
		public int countPatternCTD = 0;
		public int countPatternCT3 = 0;
		public int countPatternCT2 = 0;
		public int countPatternCT1 = 0;		
		
		int countPatternChildrenC1 = 0;
		int countPatternChildrenC2 = 0;
		int countPatternChildrenCD = 0;
		int countPatternChildrenC3 = 0;
		
		
		public int getNumberHoles() {
				return 1 - (countPatternC1 - countPatternC3 - (2 * countPatternCD)) / 4;
		}
	
		public int getPerimeter() {
			return countPatternC1 + countPatternC2 + countPatternC3 + (2*countPatternCD);
		}
		
		public String printPattern() {
			String s = "Q1: " + countPatternC1;
			s += "\tQ2: " + countPatternC2;
			s += "\tQ3: " + countPatternC3;
			s += "\tQD: " + countPatternCD;
			s += "\tQT1: " + countPatternCT1;
			s += "\tQT2: " + countPatternCT2;
			s += "\tQT3: " + countPatternCT3;
			s += "\tQTD: " + countPatternCTD;
			return s;
		}	
	}
}