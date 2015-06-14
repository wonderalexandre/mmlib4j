package mmlib4j.representation.tree.attribute;

import java.util.HashSet;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.INodeTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.utils.AdjacencyRelation;


/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerPatternEulerAttribute extends AttributeComputedIncrementally{

	PatternEulerAttribute attr[];
	int numNode;
	GrayScaleImage img; 
	AdjacencyRelation adj;
	boolean isMaxtree;
	
	public ComputerPatternEulerAttribute(int numNode, INodeTree root, GrayScaleImage img, AdjacencyRelation adj){
		this.numNode = numNode;
		this.attr = new PatternEulerAttribute[numNode];
		this.img = img;
		this.adj = adj;
		computerAttribute(root);
	}

	public PatternEulerAttribute[] getAttribute(){
		return attr;
	}
	
	public void addAttributeInNodesCT(HashSet<NodeCT> hashSet){
		for(INodeTree node: hashSet){
			node.addAttribute(Attribute.NUM_HOLES, new Attribute(Attribute.NUM_HOLES, attr[ node.getId() ].getNumberHoles()));
			
		}
	}
	
	public void initialization(INodeTree node) {
		attr[node.getId()] = new PatternEulerAttribute();
		this.isMaxtree = node.isNodeMaxtree();
		if(isMaxtree)
			img.setPadding(Integer.MIN_VALUE);
		else
			img.setPadding(Integer.MAX_VALUE);
		
		for(int p: node.getCanonicalPixels()){
			computerLocalPattern(node, p);
		}
		
	}
	
	public void updateChildren(INodeTree node, INodeTree son) {
		attr[node.getId()].countPatternChildrenC1 += attr[son.getId()].countPatternC1;
		attr[node.getId()].countPatternChildrenC2 += attr[son.getId()].countPatternC2;
	}

	public void posProcessing(INodeTree root) {
		//pos-processing root
		attr[root.getId()].countPatternC1 = attr[root.getId()].countPatternC1 + attr[root.getId()].countPatternChildrenC1 - attr[root.getId()].countPatternC3;
		attr[root.getId()].countPatternC2 = attr[root.getId()].countPatternC2 + attr[root.getId()].countPatternChildrenC2 - attr[root.getId()].countPatternC4;
	}
	

	private int getValue(int x, int y){
		if(isMaxtree)
			return img.getValue(x, y);
		else
			return 255 - img.getValue(x, y);
	}
	
	private void computerLocalPattern(INodeTree node, int p) {
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
