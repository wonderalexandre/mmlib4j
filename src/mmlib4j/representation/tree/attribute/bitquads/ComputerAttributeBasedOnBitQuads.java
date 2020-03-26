package mmlib4j.representation.tree.attribute.bitquads;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.PixelIndexer;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.AttributeComputedIncrementally;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.Utils;


public class ComputerAttributeBasedOnBitQuads extends AttributeComputedIncrementally{

	protected GrayScaleImage img;
	protected AdjacencyRelation adj;
	protected AttributeBasedOnBitQuads[] quadAttributes;
	protected PatternCounter patternCounter;
	
	public ComputerAttributeBasedOnBitQuads(ComponentTree tree) {
		long ti = System.currentTimeMillis();
		img = tree.getInputImage();
		adj = tree.getAdjacency();
		quadAttributes = new AttributeBasedOnBitQuads[tree.getNumNode()];
		img.setPixelIndexer(PixelIndexer.getDefaultValueIndexer(img.getWidth(), img.getHeight()));
		patternCounter = new PatternCounter(getFilenameFromCTType(tree));
		
		computerAttribute(tree.getRoot());
		
		
		if (Utils.debug) {
			for (AttributeBasedOnBitQuads attr: quadAttributes)
				System.out.println(attr.printPattern());
			
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [extraction of attributes - bit-quads]" + ((tf-ti)/1000.0) 
					+ "s");
		}
	}
	
	private String getFilenameFromCTType(ComponentTree tree) {
		if (tree.isMaxtree()) {
			if (tree.getAdjacency() == AdjacencyRelation.getAdjacency4())
				return "dt-max-tree-4c.dat";
			else
				return "dt-max-tree-8c.dat";
		}
		else { // min tree
			if (tree.getAdjacency() == AdjacencyRelation.getAdjacency4())
				return "dt-min-tree-4c.dat";
			else
				return "dt-min-tree-8c.dat";
		}
	}
	
	@Override
	public void preProcessing(NodeLevelSets node) {
		SimpleLinkedList<Integer> pixels = node.getCompactNodePixels();
		int nodeId = node.getId();
		quadAttributes[nodeId] = new AttributeBasedOnBitQuads();
		
		for (Integer p: pixels) {
			int px = p % img.getWidth();
			int py = p / img.getWidth();
			byte[] counting = patternCounter.count(px, py, img);
			
			quadAttributes[nodeId].nP1 += counting[PatternCounter.PatternType.P1.getValue()];
			quadAttributes[nodeId].nP2 += counting[PatternCounter.PatternType.P2.getValue()];
			quadAttributes[nodeId].nP3 += counting[PatternCounter.PatternType.P3.getValue()];
			quadAttributes[nodeId].nP4 += counting[PatternCounter.PatternType.P4.getValue()];
			quadAttributes[nodeId].nPD += counting[PatternCounter.PatternType.PD.getValue()];
			quadAttributes[nodeId].nP1T += counting[PatternCounter.PatternType.P1T.getValue()];
			quadAttributes[nodeId].nP2T += counting[PatternCounter.PatternType.P2T.getValue()];
			quadAttributes[nodeId].nP3T += counting[PatternCounter.PatternType.P3T.getValue()];
			quadAttributes[nodeId].nPDT += counting[PatternCounter.PatternType.PDT.getValue()];
		}
	}

	@Override
	public void mergeChildren(NodeLevelSets parent, NodeLevelSets son) {
		int nodeId = parent.getId();
		int childId = son.getId();
		
		quadAttributes[nodeId].nP1 += quadAttributes[childId].nP1;
		quadAttributes[nodeId].nP2 += quadAttributes[childId].nP2;
		quadAttributes[nodeId].nP3 += quadAttributes[childId].nP3;
		quadAttributes[nodeId].nPD += quadAttributes[childId].nPD;
		quadAttributes[nodeId].nP4 += quadAttributes[childId].nP4;
	}

	@Override
	public void posProcessing(NodeLevelSets parent) {
		int nodeId = parent.getId();
		
		quadAttributes[nodeId].nP1 -= quadAttributes[nodeId].nP1T;
		quadAttributes[nodeId].nP2 -= quadAttributes[nodeId].nP2T;
		quadAttributes[nodeId].nP3 -= quadAttributes[nodeId].nP3T;
		quadAttributes[nodeId].nPD -= quadAttributes[nodeId].nPDT;
	}
	
	public void addAttributeInNodesCT(SimpleLinkedList<NodeLevelSets> list) {
		for (NodeLevelSets node : list)
			addAttributeInNodes(node);
	}
	
	public void addAttributeInNodes(NodeLevelSets node)
	{
		int nodeId = node.getId();
		node.addAttribute(Attribute.BIT_QUADS_PERIMETER, new Attribute(Attribute.BIT_QUADS_PERIMETER, quadAttributes[nodeId].getPerimenter()));
		node.addAttribute(Attribute.BIT_QUADS_EULER_NUMBER, new Attribute(Attribute.BIT_QUADS_EULER_NUMBER, quadAttributes[nodeId].getEulerNumber()));
		node.addAttribute(Attribute.BIT_QUADS_HOLE_NUMBER, new Attribute(Attribute.BIT_QUADS_HOLE_NUMBER, quadAttributes[nodeId].getHoleNumber()));
		node.addAttribute(Attribute.BIT_QUADS_PERIMETER_CONTINUOUS, 
				new Attribute(Attribute.BIT_QUADS_PERIMETER_CONTINUOUS, quadAttributes[nodeId].getPerimeterContinuous()));
		node.addAttribute(Attribute.BIT_QUADS_CIRCULARITY, 
				new Attribute(Attribute.BIT_QUADS_CIRCULARITY, quadAttributes[nodeId].getCircularity(node.getArea())));
		node.addAttribute(Attribute.BIT_QUADS_AVERAGE_AREA, 
				new Attribute(Attribute.BIT_QUADS_AVERAGE_AREA, quadAttributes[nodeId].getAverageArea(node.getArea())));
		node.addAttribute(Attribute.BIT_QUADS_AVERAGE_PERIMETER, 
				new Attribute(Attribute.BIT_QUADS_AVERAGE_PERIMETER, quadAttributes[nodeId].getAveragePerimeter()));
		node.addAttribute(Attribute.BIT_QUADS_AVERAGE_LENGTH, 
				new Attribute(Attribute.BIT_QUADS_AVERAGE_LENGTH, quadAttributes[nodeId].getAverageLength()));
		node.addAttribute(Attribute.BIT_QUADS_AVERAGE_WIDTH, 
				new Attribute(Attribute.BIT_QUADS_AVERAGE_WIDTH, quadAttributes[nodeId].getAverageWidth(node.getArea())));
		node.addAttribute(Attribute.BIT_QUADS_AREA, new Attribute(Attribute.BIT_QUADS_AREA, quadAttributes[nodeId].getArea()));
		node.addAttribute(Attribute.BIT_QUADS_AREA_DUDA, new Attribute(Attribute.BIT_QUADS_AREA_DUDA, quadAttributes[nodeId].getAreaDuda()));
	}
	
	class AttributeBasedOnBitQuads
	{
		protected int nP1 = 0;
		protected int nP2 = 0;
		protected int nP3 = 0;
		protected int nP4 = 0;
		protected int nPD = 0;
		
		protected int nP1T = 0;
		protected int nP2T = 0;
		protected int nP3T = 0;
		protected int nPDT = 0;
		
		public int getEulerNumber() {
			if (adj == AdjacencyRelation.getAdjacency4())
				return (nP1 - nP3) / 4;
			else
				return (nP1 - nP3 - (2*nPD)) / 4;
		}
		
		public int getHoleNumber() {
			return 1 - getEulerNumber();
		}
		
		public int getPerimenter() {
			return nP1 + nP2 + nP3 + (2 * nPD);
		}
		
		public int getArea() {
			return (nP1 + (2*nP2) + (3*nP3) + (4*nP4) + (2*nPD)) / 4;
		}
		
		public double getAreaDuda() {
			return ((1./4.)*nP1) + ((1./2.)*nP2) + ((7./8.)*nP3) + nP4 + ((3./4.)*nPD);
		}
		
		public double getPerimeterContinuous() {
			return nP2 + ((nP1 + nP3) / 1.5);
		}
		
		public double getCircularity(int area) {
			return (4. * Math.PI * area) / Math.pow(getPerimeterContinuous(), 2);
		}
		
		public double getAverageArea(int area)
		{
			return area / (double)getEulerNumber();
		}
		
		public double getAveragePerimeter() {
			return (getPerimeterContinuous() / (double) getEulerNumber());
		}
		
		public double getAverageLength() {
			return getAveragePerimeter() / 2.0;
		}
		
		public double getAverageWidth(int area) {
			return (2.*getAverageArea(area) / getAveragePerimeter()); 
		}
		
		public String printPattern() {
			return "P1: " + nP1 + "\tP2: " + nP2 + "\tP3: " + nP3 + "\tPD: " + nPD + "\tP4: " + nP4 + 
					"\tP1T: " + nP1T + "\tP2T: " + nP2T + "\tP3T: " + nP3T + "\tPTD :" + nPDT;
		}
	}
}	