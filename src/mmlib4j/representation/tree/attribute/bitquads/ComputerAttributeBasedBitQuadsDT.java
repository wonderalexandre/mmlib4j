package mmlib4j.representation.tree.attribute.bitquads;

import java.io.IOException;
import java.util.HashSet;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.PixelIndexer;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.AttributeComputedIncrementally;
import mmlib4j.representation.tree.attribute.bitquads.PatternsCounter.PatternType;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.utils.AdjacencyRelation;

public class ComputerAttributeBasedBitQuadsDT extends AttributeComputedIncrementally {
	protected GrayScaleImage img;
	protected AdjacencyRelation adj; 
	protected AttributeBasedBitQuads[] bitquads;
	protected PatternsCounter patternsCounter;
	
	public ComputerAttributeBasedBitQuadsDT(ConnectedFilteringByComponentTree tree) throws IOException {
		img = tree.getInputImage();
		adj = tree.getAdjacency();
		bitquads = new AttributeBasedBitQuads[tree.getNumNode()];
		img.setPixelIndexer(PixelIndexer.getDefaultValueIndexer(img.getWidth(), img.getHeight()));
		patternsCounter = new PatternsCounter();
		computerAttribute(tree.getRoot());
	}
	
	@Override
	public void preProcessing(NodeLevelSets node) {
		SimpleLinkedList<Integer> pixels = node.getCanonicalPixels();
		int nodeId = node.getId();
		bitquads[nodeId] = new AttributeBasedBitQuads();
		
		for (Integer p: pixels) {
			int px = p % img.getWidth();
			int py = p / img.getWidth();
			byte[] countings = patternsCounter.count(px, py, img);
			
			bitquads[node.getId()].nQ1 += countings[PatternType.Q1.getValue()];
			bitquads[node.getId()].nQ2 += countings[PatternType.Q2.getValue()];
			bitquads[node.getId()].nQ3 += countings[PatternType.Q3.getValue()];
			bitquads[node.getId()].nQ4 += countings[PatternType.Q4.getValue()];
			bitquads[node.getId()].nQD += countings[PatternType.QD.getValue()];
			
			bitquads[node.getId()].nQ1T += countings[PatternType.Q1T.getValue()];
			bitquads[node.getId()].nQ2T += countings[PatternType.Q2T.getValue()];
			bitquads[node.getId()].nQ3T += countings[PatternType.Q3T.getValue()];
			bitquads[node.getId()].nQDT += countings[PatternType.QDT.getValue()];
		}
	}

	@Override
	public void mergeChildren(NodeLevelSets node, NodeLevelSets son) {
		int nodeId = node.getId();
		int childId = son.getId();
		
		bitquads[nodeId].nQ1 += bitquads[childId].nQ1;
		bitquads[nodeId].nQ2 += bitquads[childId].nQ2;
		bitquads[nodeId].nQ3 += bitquads[childId].nQ3;
		bitquads[nodeId].nQD += bitquads[childId].nQD;
		bitquads[nodeId].nQ4 += bitquads[childId].nQ4;
	}

	@Override
	public void posProcessing(NodeLevelSets node) {
		int nodeId = node.getId();
		
		bitquads[nodeId].nQ1 -= bitquads[nodeId].nQ1T;
		bitquads[nodeId].nQ2 -= bitquads[nodeId].nQ2T;
		bitquads[nodeId].nQ3 -= bitquads[nodeId].nQ3T;
		bitquads[nodeId].nQD -= bitquads[nodeId].nQDT;
	}
	
	public void addAttributeInNodesCT(HashSet<NodeCT> list) {
		for(NodeCT node: list){
			addAttributeInNodes(node);
		}
	}
	
	public void addAttributeInNodes(NodeLevelSets node) {
		node.addAttribute(Attribute.BIT_QUADS_PERIMETER, new Attribute(Attribute.BIT_QUADS_PERIMETER, bitquads[node.getId()].getPerimeter()));
		node.addAttribute(Attribute.BIT_QUADS_NUMBER_EULER, new Attribute(Attribute.BIT_QUADS_PERIMETER, bitquads[node.getId()].getNumberEuler()));
		node.addAttribute(Attribute.BIT_QUADS_NUMBER_HOLES, new Attribute(Attribute.BIT_QUADS_PERIMETER, bitquads[node.getId()].getNumberHoles()));
		node.addAttribute(Attribute.BIT_QUADS_PERIMETER_CONTINUOUS, new Attribute(Attribute.BIT_QUADS_PERIMETER, bitquads[node.getId()].getPerimeterContinuous()));
		node.addAttribute(Attribute.BIT_QUADS_CIRCULARITY, new Attribute(Attribute.BIT_QUADS_PERIMETER, bitquads[node.getId()].getCircularity(node.getArea())));
		node.addAttribute(Attribute.BIT_QUADS_AREA_AVERAGE, new Attribute(Attribute.BIT_QUADS_PERIMETER, bitquads[node.getId()].getAreaAverage(node.getArea())));
		node.addAttribute(Attribute.BIT_QUADS_PERIMETER_AVERAGE, new Attribute(Attribute.BIT_QUADS_PERIMETER, bitquads[node.getId()].getPerimeterAverage()));
		node.addAttribute(Attribute.BIT_QUADS_LENGTH_AVERAGE, new Attribute(Attribute.BIT_QUADS_PERIMETER, bitquads[node.getId()].getLengthAverage()));
		node.addAttribute(Attribute.BIT_QUADS_WIDTH_AVERAGE, new Attribute(Attribute.BIT_QUADS_PERIMETER, bitquads[node.getId()].getWidthAverage(node.getArea())));
		node.addAttribute(Attribute.BIT_QUADS_AREA, new Attribute(Attribute.BIT_QUADS_PERIMETER, bitquads[node.getId()].getArea()));
		node.addAttribute(Attribute.BIT_QUADS_AREA_DUDA, new Attribute(Attribute.BIT_QUADS_PERIMETER, bitquads[node.getId()].getAreaDuda()));
	}
		
	class AttributeBasedBitQuads {
		protected int nQ1 = 0;
		protected int nQ2 = 0;
		protected int nQ3 = 0;
		protected int nQ4 = 0;
		protected int nQD = 0;
		
		protected int nQ1T = 0;
		protected int nQ2T = 0;
		protected int nQ3T = 0;
		protected int nQDT = 0;
		
		public int getNumberEuler() {
			if (adj == AdjacencyRelation.getAdjacency4())
				return (nQ1 - nQ3) / 4;
			else
				return (nQ1 - nQ3 - (2 * nQD)) / 4;
		}
		
		public int getNumberHoles() {
			return 1 - getNumberEuler();
		}
		
		public int getPerimeter() {
			return nQ1 + nQ2 + nQ3 + (2 * nQD);
		}
		
		public int getArea() {
			return (nQ1 + (2*nQ2) + (3*nQ3) + (4*nQ4) + (2*nQD));
		}
		
		public double getAreaDuda() {
			return ((1./4.) * nQ1) + ((1./2.) * nQ2) + ((7./8.) * nQ3) + nQ4 + ((3./4.) * nQD); 
		}
		
		public double getPerimeterContinuous() {
			return nQ2 + ((nQ1 + nQ3)/ 1.5);
		}
		
		public double getCircularity(int area) {
			return (4.* Math.PI * area) / Math.pow(getPerimeterContinuous(), 2);
		}
		
		public double getAreaAverage(int area) {
			return (area / (double) getNumberEuler());
		}
		
		public double getPerimeterAverage() {
			return (getPerimeterContinuous() / (double) getNumberEuler());
		}
		
		public double getLengthAverage() {
			return (getPerimeterAverage() / 2.0);
		}
		
		public double getWidthAverage(int area) {
			return (2. * getAreaAverage(area) / getPerimeterAverage());
		}
		
		public String printPattern() {
			return "Q1: " + nQ1 + "\tQ2: " + nQ2 + "\tQ3: " + nQ3 + "\tQD: " + nQD + "\tQ4: " + nQ4 + "\tQ1T: "
					+ nQ1T + "\tQ2T: " + nQ2T + "\tQ3T: " + nQ3T + "\tQTD" + nQDT;
		}
	}
}
