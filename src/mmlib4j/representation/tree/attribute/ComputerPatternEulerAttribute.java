package mmlib4j.representation.tree.attribute;

import java.util.HashSet;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.PixelIndexer;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * Implementation of paper:
 * Juan Climent, Luiz S. Oliveira, A new algorithm for number of holes attribute filtering of grey-level images, Pattern Recognition Letters, 2014.
 */
public class ComputerPatternEulerAttribute extends AttributeComputedIncrementally{

	PatternEulerAttribute attr[];
	int numNode;
	GrayScaleImage img; 
	AdjacencyRelation adj;
	boolean isMaxtree;
	private int index;
	
	public ComputerPatternEulerAttribute(int numNode, NodeLevelSets root, GrayScaleImage img, AdjacencyRelation adj){
		long ti = System.currentTimeMillis();
		this.numNode = numNode;
		this.attr = new PatternEulerAttribute[numNode];
		this.img = img;
		this.adj = adj;
		img.setPixelIndexer( PixelIndexer.getDefaultValueIndexer(img.getWidth(), img.getHeight()) );
		
		computerAttribute(root);
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [extraction of attribute - euler number]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}

	public PatternEulerAttribute[] getAttribute(){
		return attr;
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
	}
	
	
	public void preProcessing(NodeLevelSets node) {
		attr[node.getId()] = new PatternEulerAttribute();
		this.isMaxtree = node.isNodeMaxtree();
		
		for(int p: node.getCanonicalPixels()){
			computerLocalPattern(node, p);
		}
		
	}
	
	public void mergeChildren(NodeLevelSets node, NodeLevelSets son) {
		attr[node.getId()].countPatternChildrenC1 += attr[son.getId()].countPatternC1;
		attr[node.getId()].countPatternChildrenC2 += attr[son.getId()].countPatternC2;
	}

	public void posProcessing(NodeLevelSets root) {
		//pos-processing root
		attr[root.getId()].countPatternC1 = attr[root.getId()].countPatternC1 + attr[root.getId()].countPatternChildrenC1 - attr[root.getId()].countPatternC3;
		attr[root.getId()].countPatternC2 = attr[root.getId()].countPatternC2 + attr[root.getId()].countPatternChildrenC2 - attr[root.getId()].countPatternC4;
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

		if (adj == AdjacencyRelation.getAdjacency4()) {
			// padrao 1 => 4-conexo
			if (getValue(px + 1, py) < getValue(px, py)
					&& getValue(px, py - 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC1++;
			if (getValue(px - 1, py) < getValue(px, py)
					&& getValue(px, py - 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC1++;
			if (getValue(px - 1, py) < getValue(px, py)
					&& getValue(px, py + 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC1++;
			if (getValue(px + 1, py) < getValue(px, py)
					&& getValue(px, py + 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC1++;
	
			// padrao 2 => 4-conexo
			if (getValue(px - 1, py) > getValue(px, py)
					&& getValue(px, py + 1) > getValue(px, py)
					&& getValue(px - 1, py + 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px + 1, py) > getValue(px, py)
					&& getValue(px, py + 1) > getValue(px, py)
					&& getValue(px + 1, py + 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px + 1, py) > getValue(px, py)
					&& getValue(px, py - 1) > getValue(px, py)
						&& getValue(px + 1, py - 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px - 1, py) > getValue(px, py)
					&& getValue(px, py - 1) > getValue(px, py)
					&& getValue(px - 1, py - 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px + 1, py) >= getValue(px, py)
					&& getValue(px, py + 1) < getValue(px, py)
					&& getValue(px + 1, py + 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px - 1, py) >= getValue(px,py)
					&& getValue(px, py + 1) < getValue(px, py)
					&& getValue(px - 1, py + 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px - 1, py) >= getValue(px,py)
					&& getValue(px, py - 1) < getValue(px, py)
					&& getValue(px - 1, py - 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px + 1, py) >= getValue(px,py)
					&& getValue(px, py - 1) < getValue(px, py)
					&& getValue(px + 1, py - 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px - 1, py) < getValue(px, py)
					&& getValue(px, py - 1) >= getValue(px, py)
					&& getValue(px - 1, py - 1) >= getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px + 1, py) < getValue(px, py)
					&& getValue(px, py - 1) >= getValue(px, py)
					&& getValue(px + 1, py - 1) >= getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px + 1, py) < getValue(px, py)
					&& getValue(px, py + 1) >= getValue(px,py)
					&& getValue(px + 1, py + 1) >= getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px - 1, py) < getValue(px, py)
					&& getValue(px, py + 1) >= getValue(px,py)
					&& getValue(px - 1, py + 1) >= getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
	
			// padrao 3
			if (getValue(px, py + 1) > getValue(px, py)
					&& getValue(px + 1, py + 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px - 1, py + 1) < getValue(px, py)
					&& getValue(px, py + 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px, py - 1) > getValue(px, py)
					&& getValue(px - 1, py - 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px, py - 1) > getValue(px, py)
					&& getValue(px + 1, py - 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px - 1, py) > getValue(px, py)
					&& getValue(px - 1, py - 1) <= getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px + 1, py) > getValue(px, py)
					&& getValue(px + 1, py - 1) <= getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px + 1, py) > getValue(px, py)
					&& getValue(px + 1, py + 1) <= getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px - 1, py) > getValue(px, py)
					&& getValue(px - 1, py + 1) <= getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
	
			// padrao 4 => 4-conexo
			if (getValue(px + 1, py) > getValue(px, py)
					&& getValue(px, py - 1) > getValue(px, py)
					&& getValue(px + 1, py - 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC4++;
			if (getValue(px - 1, py) > getValue(px, py)
					&& getValue(px, py - 1) > getValue(px, py)
					&& getValue(px - 1, py - 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC4++;
			if (getValue(px - 1, py) > getValue(px, py)
					&& getValue(px, py + 1) > getValue(px, py)
					&& getValue(px - 1, py + 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC4++;
			if (getValue(px + 1, py) > getValue(px, py)
					&& getValue(px, py + 1) > getValue(px, py)
					&& getValue(px + 1, py + 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC4++;
		} 
		else {
			// padrao 1 => 8-conexo
			if (getValue(px + 1, py) < getValue(px, py)
					&& getValue(px, py - 1) < getValue(px, py)
					&& getValue(px + 1, py - 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC1++;
			if (getValue(px - 1, py) < getValue(px, py)
					&& getValue(px, py - 1) < getValue(px, py)
					&& getValue(px - 1, py - 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC1++;
			if (getValue(px - 1, py) < getValue(px, py)
					&& getValue(px, py + 1) < getValue(px, py)
					&& getValue(px - 1, py + 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC1++;
			if (getValue(px + 1, py) < getValue(px, py)
					&& getValue(px, py + 1) < getValue(px, py)
					&& getValue(px + 1, py + 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC1++;
			
			// padrao 2 => 8-conexo
			if (getValue(px, py + 1) < getValue(px, py)
					&& getValue(px + 1, py + 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px, py + 1) < getValue(px, py)
					&& getValue(px - 1, py + 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px, py - 1) < getValue(px, py)
					&& getValue(px - 1, py - 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px, py - 1) < getValue(px, py)
					&& getValue(px + 1, py - 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px - 1, py) < getValue(px, py)
					&& getValue(px - 1, py - 1) >= getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px + 1, py) < getValue(px, py)
					&& getValue(px + 1, py - 1) >= getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px + 1, py) < getValue(px, py)
					&& getValue(px + 1, py + 1) >= getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
			if (getValue(px - 1, py) < getValue(px, py)
					&& getValue(px - 1, py + 1) >= getValue(px, py))
				attr[ node.getId() ].countPatternC2++;
	
			// padrao 3 => 8-conexo
			if (getValue(px - 1, py) < getValue(px, py)
					&& getValue(px, py + 1) < getValue(px, py)
					&& getValue(px - 1, py + 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px + 1, py) < getValue(px, py)
					&& getValue(px, py + 1) < getValue(px, py)
					&& getValue(px + 1, py + 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px + 1, py) < getValue(px, py)
					&& getValue(px, py - 1) < getValue(px, py)
					&& getValue(px + 1, py - 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px - 1, py) < getValue(px, py)
					&& getValue(px, py - 1) < getValue(px, py)
					&& getValue(px - 1, py - 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px + 1, py) <= getValue(px,py)
					&& getValue(px, py + 1) > getValue(px, py)
					&& getValue(px + 1, py + 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px - 1, py) <= getValue(px,py)
					&& getValue(px - 1, py + 1) < getValue(px, py)
					&& getValue(px, py + 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px - 1, py) <= getValue(px,py)
					&& getValue(px, py - 1) > getValue(px, py)
					&& getValue(px - 1, py - 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px + 1, py) <= getValue(px,py)
					&& getValue(px, py - 1) > getValue(px, py)
					&& getValue(px + 1, py - 1) < getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px - 1, py) > getValue(px, py)
					&& getValue(px, py - 1) <= getValue(px,py)
					&& getValue(px - 1, py - 1) <= getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px + 1, py) > getValue(px, py)
					&& getValue(px, py - 1) <= getValue(px,py)
					&& getValue(px + 1, py - 1) <= getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px + 1, py) > getValue(px, py)
					&& getValue(px, py + 1) <= getValue(px,py)
					&& getValue(px + 1, py + 1) <= getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			if (getValue(px - 1, py) > getValue(px, py)
					&& getValue(px, py + 1) <= getValue(px,py)
					&& getValue(px - 1, py + 1) <= getValue(px, py))
				attr[ node.getId() ].countPatternC3++;
			
			// padrao 4 => 8-conexo
			if (getValue(px + 1, py) > getValue(px, py)
					&& getValue(px, py - 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC4++;
			if (getValue(px - 1, py) > getValue(px, py)
					&& getValue(px, py - 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC4++;
			if (getValue(px - 1, py) > getValue(px, py)
					&& getValue(px, py + 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC4++;
			if (getValue(px + 1, py) > getValue(px, py)
					&& getValue(px, py + 1) > getValue(px, py))
				attr[ node.getId() ].countPatternC4++;
		}
		
	}
	
	
	 public class PatternEulerAttribute {
	
		public int countPatternC4 = 0;
		public int countPatternC3 = 0;
		public int countPatternC2 = 0;
		public int countPatternC1 = 0;
		
		
		
		int countPatternChildrenC1 = 0;
		int countPatternChildrenC2 = 0;
		
		public int getNumberHoles() {
			return 1 - (countPatternC1 - countPatternC2) / 4;
		}
	
		
		public String printPattern() {
			String s = "Q1: " + countPatternC1;
			s += "\tQ2: " + countPatternC2;
			s += "\tQ3: " + countPatternC3;
			s += "\tQ4: " + countPatternC4;
	
			return s;
		}
	
	
		
	}

}
