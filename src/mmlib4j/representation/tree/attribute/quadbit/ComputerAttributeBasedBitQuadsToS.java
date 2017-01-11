package mmlib4j.representation.tree.attribute.quadbit;

import mmlib4j.images.impl.PixelIndexer;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.quadbit.tos.QuadBitFactoryFactoryTreeOfShapes;
import mmlib4j.representation.tree.tos.ConnectedFilteringByTreeOfShape;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.utils.AdjacencyRelation;

public class ComputerAttributeBasedBitQuadsToS extends ComputerAttributeBasedBitQuads {
	protected ConnectedFilteringByTreeOfShape tree;
	
	public ComputerAttributeBasedBitQuadsToS(ConnectedFilteringByTreeOfShape tree) {
		super(tree.getNumNode());
		this.tree = tree;
		tree.getInputImage().setPixelIndexer(PixelIndexer.getDefaultValueIndexer(tree.getInputImage().getWidth(), 
				tree.getInputImage().getHeight()));
		
		this.qFactory = new QuadBitFactoryFactoryTreeOfShapes(tree); 
		
		createPatternsSets();
		computerAttribute(tree.getRoot());
	}

	protected void createPatternsSets() {
		createPatternSetQ1();
		createPatternSetQ2();
		createPatternSetQD();
		createPatternSetQ3();
		createPatternSetQ4();
		
		createPatternSetQ1T();
		createPatternSetQ2T();
		createPatternSetQDT();
		createPatternSetQ3T();
		
		createPatternSetQ1C4();
		createPatternSetQ1TC4();
	}
	
	protected boolean is8Connected(NodeToS node) {
		return node.isNodeMaxtree();
	}
	
	protected void computerLocalPattern(NodeToS node, int p) {
		int px = p % tree.getInputImage().getWidth();
		int py = p / tree.getInputImage().getWidth();
		
		countings[node.getId()].nQ1 += Q1C4.count(px, py);
		countings[node.getId()].nQ2 += Q2.count(px, py);
		//countings[node.getId()].nQD += QD.count(px, py);
		countings[node.getId()].nQ3 += Q3.count(px, py);
		countings[node.getId()].nQ4 += Q4.count(px, py);
		
		countings[node.getId()].nQ1T += Q1TC4.count(px, py);
		countings[node.getId()].nQ2T += Q2T.count(px, py);
		countings[node.getId()].nQ3T += Q3T.count(px, py);
		//countings[node.getId()].nQDT += QDT.count(px, py);
	}
	
	@Override
	public void preProcessing(NodeLevelSets node) {
		countings[node.getId()] = new BitQuadsCounting();
		for (int p: node.getCanonicalPixels())
			computerLocalPattern((NodeToS)node, p);
	}

	@Override
	public void mergeChildren(NodeLevelSets parent, NodeLevelSets son) {
		NodeToS parentToS = (NodeToS)parent, childToS = (NodeToS)son;
		
		countings[parentToS.getId()].childrenNQ1 += countings[childToS.getId()].nQ1;
		countings[parentToS.getId()].childrenNQ2 += countings[childToS.getId()].nQ2;
		countings[parentToS.getId()].childrenNQD += countings[childToS.getId()].nQD;
		countings[parentToS.getId()].childrenNQ3 += countings[childToS.getId()].nQ3;
		countings[parentToS.getId()].childrenNQ4 += countings[childToS.getId()].nQ4;
	}

	@Override
	public void posProcessing(NodeLevelSets parent) {
		NodeToS parentToS = (NodeToS)parent;
		
		/*System.out.println(is8Connected(parentToS));
		countings[parentToS.getId()].printValues();
		System.out.println();*/
		
		countings[parentToS.getId()].nQ1 += countings[parentToS.getId()].childrenNQ1 - countings[parentToS.getId()].nQ1T;		
		//countings[parentToS.getId()].nQD += countings[parentToS.getId()].childrenNQD - countings[parentToS.getId()].nQDT;
		countings[parentToS.getId()].nQ2 += countings[parentToS.getId()].childrenNQ2 - countings[parentToS.getId()].nQ2T;
		countings[parentToS.getId()].nQ3 += countings[parentToS.getId()].childrenNQ3 - countings[parentToS.getId()].nQ3T;
		countings[parentToS.getId()].nQ4 += countings[parentToS.getId()].childrenNQ4;
				
		//if (is8Connected(parentToS))
			//addAttributeInNodes(parentToS, AdjacencyRelation.getAdjacency8());
		//else 
			//addAttributeInNodes(parentToS, AdjacencyRelation.getAdjacency4());
		
		addAttributeInNodes(parentToS, AdjacencyRelation.getAdjacency8());
	}
}