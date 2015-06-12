package mmlib4j.representation.tree.attribute;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.utils.AdjacencyRelation;


/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class AttributePatternEuler {

	public int countPatternC4 = 0;
	public int countPatternC3 = 0;
	public int countPatternC2 = 0;
	public int countPatternC1 = 0;
	
	int countPatternChildrenC1 = 0;
	int countPatternChildrenC2 = 0;
	
	GrayScaleImage imgInput;
	AdjacencyRelation adj;
	boolean isMaxtree;
	
	public AttributePatternEuler(GrayScaleImage img, AdjacencyRelation adj, boolean isMaxtree){
		this.imgInput = img;
		this.adj = adj;
		this.isMaxtree = isMaxtree;
		if(isMaxtree)
			img.setPadding(Integer.MIN_VALUE);
		else
			img.setPadding(Integer.MAX_VALUE);
		
	}
	
	public void countPatternChildren(AttributePatternEuler patternChild) {
		this.countPatternChildrenC1 += patternChild.countPatternC1;
		this.countPatternChildrenC2 += patternChild.countPatternC2;
	}
	
	public void mergeChildren() {
		this.countPatternC1 = this.countPatternC1 + countPatternChildrenC1 - this.countPatternC3;
		this.countPatternC2 = this.countPatternC2 + countPatternChildrenC2 - this.countPatternC4;
	}

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

	public int getValue(int x, int y){
		if(isMaxtree)
			return imgInput.getValue(x, y);
		else
			return 255 - imgInput.getValue(x, y);
	}
	
	public void computerLocalPattern(int p) {
		int px = p % imgInput.getWidth();
		int py = p / imgInput.getWidth();

		if (adj == AdjacencyRelation.getAdjacency4()) {
			// padrao 1 => 4-conexo
			
				
				if (getValue(px + 1, py) < getValue(px, py)
						&& getValue(px, py - 1) < getValue(px, py))
					countPatternC1++;
				
				
				if (getValue(px - 1, py) < getValue(px, py)
						&& getValue(px, py - 1) < getValue(px, py))
					countPatternC1++;
				if (getValue(px - 1, py) < getValue(px, py)
						&& getValue(px, py + 1) < getValue(px, py))
					countPatternC1++;
				if (getValue(px + 1, py) < getValue(px, py)
						&& getValue(px, py + 1) < getValue(px, py))
					countPatternC1++;
	
				// padrao 2 => 4-conexo
				if (getValue(px - 1, py) > getValue(px, py)
						&& getValue(px, py + 1) > getValue(px, py)
						&& getValue(px - 1, py + 1) < getValue(px, py))
					countPatternC2++;
				if (getValue(px + 1, py) > getValue(px, py)
						&& getValue(px, py + 1) > getValue(px, py)
						&& getValue(px + 1, py + 1) < getValue(px, py))
					countPatternC2++;
				if (getValue(px + 1, py) > getValue(px, py)
						&& getValue(px, py - 1) > getValue(px, py)
						&& getValue(px + 1, py - 1) < getValue(px, py))
					countPatternC2++;
				if (getValue(px - 1, py) > getValue(px, py)
						&& getValue(px, py - 1) > getValue(px, py)
						&& getValue(px - 1, py - 1) < getValue(px, py))
					countPatternC2++;
				if (getValue(px + 1, py) >= getValue(px, py)
						&& getValue(px, py + 1) < getValue(px, py)
						&& getValue(px + 1, py + 1) > getValue(px, py))
					countPatternC2++;
				if (getValue(px - 1, py) >= getValue(px,py)
						&& getValue(px, py + 1) < getValue(px, py)
						&& getValue(px - 1, py + 1) > getValue(px, py))
					countPatternC2++;
				if (getValue(px - 1, py) >= getValue(px,py)
						&& getValue(px, py - 1) < getValue(px, py)
						&& getValue(px - 1, py - 1) > getValue(px, py))
					countPatternC2++;
				if (getValue(px + 1, py) >= getValue(px,py)
						&& getValue(px, py - 1) < getValue(px, py)
						&& getValue(px + 1, py - 1) > getValue(px, py))
					countPatternC2++;
				if (getValue(px - 1, py) < getValue(px, py)
						&& getValue(px, py - 1) >= getValue(px, py)
						&& getValue(px - 1, py - 1) >= getValue(px, py))
					countPatternC2++;
				if (getValue(px + 1, py) < getValue(px, py)
						&& getValue(px, py - 1) >= getValue(px, py)
						&& getValue(px + 1, py - 1) >= getValue(px, py))
					countPatternC2++;
				if (getValue(px + 1, py) < getValue(px, py)
						&& getValue(px, py + 1) >= getValue(px,py)
						&& getValue(px + 1, py + 1) >= getValue(px, py))
					countPatternC2++;
				if (getValue(px - 1, py) < getValue(px, py)
						&& getValue(px, py + 1) >= getValue(px,py)
						&& getValue(px - 1, py + 1) >= getValue(px, py))
					countPatternC2++;
	
				// padrao 3
				if (getValue(px, py + 1) > getValue(px, py)
						&& getValue(px + 1, py + 1) < getValue(px, py))
					countPatternC3++;
				if (getValue(px - 1, py + 1) < getValue(px, py)
						&& getValue(px, py + 1) > getValue(px, py))
					countPatternC3++;
				if (getValue(px, py - 1) > getValue(px, py)
						&& getValue(px - 1, py - 1) < getValue(px, py))
					countPatternC3++;
				if (getValue(px, py - 1) > getValue(px, py)
						&& getValue(px + 1, py - 1) < getValue(px, py))
					countPatternC3++;
				if (getValue(px - 1, py) > getValue(px, py)
						&& getValue(px - 1, py - 1) <= getValue(px, py))
					countPatternC3++;
				if (getValue(px + 1, py) > getValue(px, py)
						&& getValue(px + 1, py - 1) <= getValue(px, py))
					countPatternC3++;
				if (getValue(px + 1, py) > getValue(px, py)
						&& getValue(px + 1, py + 1) <= getValue(px, py))
					countPatternC3++;
				if (getValue(px - 1, py) > getValue(px, py)
						&& getValue(px - 1, py + 1) <= getValue(px, py))
					countPatternC3++;
	
				// padrao 4 => 4-conexo
				if (getValue(px + 1, py) > getValue(px, py)
						&& getValue(px, py - 1) > getValue(px, py)
						&& getValue(px + 1, py - 1) > getValue(px, py))
					countPatternC4++;
				if (getValue(px - 1, py) > getValue(px, py)
						&& getValue(px, py - 1) > getValue(px, py)
						&& getValue(px - 1, py - 1) > getValue(px, py))
					countPatternC4++;
				if (getValue(px - 1, py) > getValue(px, py)
						&& getValue(px, py + 1) > getValue(px, py)
						&& getValue(px - 1, py + 1) > getValue(px, py))
					countPatternC4++;
				if (getValue(px + 1, py) > getValue(px, py)
						&& getValue(px, py + 1) > getValue(px, py)
						&& getValue(px + 1, py + 1) > getValue(px, py))
					countPatternC4++;
			
		} 
		else {
				// padrao 1 => 8-conexo
				if (getValue(px + 1, py) < getValue(px, py)
						&& getValue(px, py - 1) < getValue(px, py)
						&& getValue(px + 1, py - 1) < getValue(px, py))
					countPatternC1++;
				if (getValue(px - 1, py) < getValue(px, py)
						&& getValue(px, py - 1) < getValue(px, py)
						&& getValue(px - 1, py - 1) < getValue(px, py))
					countPatternC1++;
				if (getValue(px - 1, py) < getValue(px, py)
						&& getValue(px, py + 1) < getValue(px, py)
						&& getValue(px - 1, py + 1) < getValue(px, py))
					countPatternC1++;
				if (getValue(px + 1, py) < getValue(px, py)
						&& getValue(px, py + 1) < getValue(px, py)
						&& getValue(px + 1, py + 1) < getValue(px, py))
					countPatternC1++;
	
				// padrao 2 => 8-conexo
				if (getValue(px, py + 1) < getValue(px, py)
						&& getValue(px + 1, py + 1) > getValue(px, py))
					countPatternC2++;
				if (getValue(px, py + 1) < getValue(px, py)
						&& getValue(px - 1, py + 1) > getValue(px, py))
					countPatternC2++;
				if (getValue(px, py - 1) < getValue(px, py)
						&& getValue(px - 1, py - 1) > getValue(px, py))
					countPatternC2++;
				if (getValue(px, py - 1) < getValue(px, py)
						&& getValue(px + 1, py - 1) > getValue(px, py))
					countPatternC2++;
				if (getValue(px - 1, py) < getValue(px, py)
						&& getValue(px - 1, py - 1) >= getValue(px, py))
					countPatternC2++;
				if (getValue(px + 1, py) < getValue(px, py)
						&& getValue(px + 1, py - 1) >= getValue(px, py))
					countPatternC2++;
				if (getValue(px + 1, py) < getValue(px, py)
						&& getValue(px + 1, py + 1) >= getValue(px, py))
					countPatternC2++;
				if (getValue(px - 1, py) < getValue(px, py)
						&& getValue(px - 1, py + 1) >= getValue(px, py))
					countPatternC2++;
	
				// padrao 3 => 8-conexo
				if (getValue(px - 1, py) < getValue(px, py)
						&& getValue(px, py + 1) < getValue(px, py)
						&& getValue(px - 1, py + 1) > getValue(px, py))
					countPatternC3++;
				if (getValue(px + 1, py) < getValue(px, py)
						&& getValue(px, py + 1) < getValue(px, py)
						&& getValue(px + 1, py + 1) > getValue(px, py))
					countPatternC3++;
				if (getValue(px + 1, py) < getValue(px, py)
						&& getValue(px, py - 1) < getValue(px, py)
						&& getValue(px + 1, py - 1) > getValue(px, py))
					countPatternC3++;
				if (getValue(px - 1, py) < getValue(px, py)
						&& getValue(px, py - 1) < getValue(px, py)
						&& getValue(px - 1, py - 1) > getValue(px, py))
					countPatternC3++;
				if (getValue(px + 1, py) <= getValue(px,py)
						&& getValue(px, py + 1) > getValue(px, py)
						&& getValue(px + 1, py + 1) < getValue(px, py))
					countPatternC3++;
				if (getValue(px - 1, py) <= getValue(px,py)
						&& getValue(px - 1, py + 1) < getValue(px, py)
						&& getValue(px, py + 1) > getValue(px, py))
					countPatternC3++;
				if (getValue(px - 1, py) <= getValue(px,py)
						&& getValue(px, py - 1) > getValue(px, py)
						&& getValue(px - 1, py - 1) < getValue(px, py))
					countPatternC3++;
				if (getValue(px + 1, py) <= getValue(px,py)
						&& getValue(px, py - 1) > getValue(px, py)
						&& getValue(px + 1, py - 1) < getValue(px, py))
					countPatternC3++;
				if (getValue(px - 1, py) > getValue(px, py)
						&& getValue(px, py - 1) <= getValue(px,py)
						&& getValue(px - 1, py - 1) <= getValue(px, py))
					countPatternC3++;
				if (getValue(px + 1, py) > getValue(px, py)
						&& getValue(px, py - 1) <= getValue(px,py)
						&& getValue(px + 1, py - 1) <= getValue(px, py))
					countPatternC3++;
				if (getValue(px + 1, py) > getValue(px, py)
						&& getValue(px, py + 1) <= getValue(px,py)
						&& getValue(px + 1, py + 1) <= getValue(px, py))
					countPatternC3++;
				if (getValue(px - 1, py) > getValue(px, py)
						&& getValue(px, py + 1) <= getValue(px,py)
						&& getValue(px - 1, py + 1) <= getValue(px, py))
					countPatternC3++;
	
				// padrao 4 => 8-conexo
				if (getValue(px + 1, py) > getValue(px, py)
						&& getValue(px, py - 1) > getValue(px, py))
					countPatternC4++;
				if (getValue(px - 1, py) > getValue(px, py)
						&& getValue(px, py - 1) > getValue(px, py))
					countPatternC4++;
				if (getValue(px - 1, py) > getValue(px, py)
						&& getValue(px, py + 1) > getValue(px, py))
					countPatternC4++;
				if (getValue(px + 1, py) > getValue(px, py)
						&& getValue(px, py + 1) > getValue(px, py))
					countPatternC4++;
				
		}
		
	}

	
}
