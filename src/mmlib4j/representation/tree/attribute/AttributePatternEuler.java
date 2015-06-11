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
	
	
	public void merge(AttributePatternEuler patternChild) {
		this.countPatternC1 = this.countPatternC1 + patternChild.countPatternC1 - this.countPatternC3;
		this.countPatternC2 = this.countPatternC2 + patternChild.countPatternC2 - this.countPatternC4;
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

	public void addPixel(int p, GrayScaleImage imgInput, AdjacencyRelation adj) {
		int px = p % imgInput.getWidth();
		int py = p / imgInput.getWidth();

		if (adj == AdjacencyRelation.getAdjacency4()) {
			// padrao 1 => 4-conexo
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.getPixel(px + 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py - 1) < imgInput.getPixel(px, py))
				countPatternC1++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.getPixel(px - 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py - 1) < imgInput.getPixel(px, py))
				countPatternC1++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.getPixel(px - 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) < imgInput.getPixel(px, py))
				countPatternC1++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.getPixel(px + 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) < imgInput.getPixel(px, py))
				countPatternC1++;

			// padrao 2 => 4-conexo
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px - 1, py + 1)
					&& imgInput.getPixel(px - 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py + 1) < imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px + 1, py + 1)
					&& imgInput.getPixel(px + 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py + 1) < imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px + 1, py - 1)
					&& imgInput.getPixel(px + 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py - 1) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py - 1) < imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px - 1, py - 1)
					&& imgInput.getPixel(px - 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py - 1) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py - 1) < imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px + 1, py + 1)
					&& imgInput.getPixel(px + 1, py) >= imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py + 1) > imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px - 1, py + 1)
					&& imgInput.getPixel(px - 1, py) >= imgInput.getPixel(px,py)
					&& imgInput.getPixel(px, py + 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py + 1) > imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px - 1, py - 1)
					&& imgInput.getPixel(px - 1, py) >= imgInput.getPixel(px,py)
					&& imgInput.getPixel(px, py - 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py - 1) > imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px + 1, py - 1)
					&& imgInput.getPixel(px + 1, py) >= imgInput.getPixel(px,py)
					&& imgInput.getPixel(px, py - 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py - 1) > imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px - 1, py - 1)
					&& imgInput.getPixel(px - 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py - 1) >= imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py - 1) >= imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px + 1, py - 1)
					&& imgInput.getPixel(px + 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py - 1) >= imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py - 1) >= imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px + 1, py + 1)
					&& imgInput.getPixel(px + 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) >= imgInput.getPixel(px,py)
					&& imgInput.getPixel(px + 1, py + 1) >= imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px - 1, py + 1)
					&& imgInput.getPixel(px - 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) >= imgInput.getPixel(px,py)
					&& imgInput.getPixel(px - 1, py + 1) >= imgInput.getPixel(px, py))
				countPatternC2++;

			// padrao 3
			if (imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px + 1, py + 1)
					&& imgInput.getPixel(px, py + 1) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py + 1) < imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px - 1, py + 1)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.getPixel(px - 1, py + 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) > imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px - 1, py - 1)
					&& imgInput.getPixel(px, py - 1) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py - 1) < imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px + 1, py - 1)
					&& imgInput.getPixel(px, py - 1) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py - 1) < imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px - 1, py - 1)
					&& imgInput.getPixel(px - 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py - 1) <= imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px + 1, py - 1)
					&& imgInput.getPixel(px + 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py - 1) <= imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px + 1, py + 1)
					&& imgInput.getPixel(px + 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py + 1) <= imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px - 1, py + 1)
					&& imgInput.getPixel(px - 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py + 1) <= imgInput.getPixel(px, py))
				countPatternC3++;

			// padrao 4 => 4-conexo
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px + 1, py - 1)
					&& imgInput.getPixel(px + 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py - 1) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py - 1) > imgInput.getPixel(px, py))
				countPatternC4++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px - 1, py - 1)
					&& imgInput.getPixel(px - 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py - 1) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py - 1) > imgInput.getPixel(px, py))
				countPatternC4++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px - 1, py + 1)
					&& imgInput.getPixel(px - 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py + 1) > imgInput.getPixel(px, py))
				countPatternC4++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px + 1, py + 1)
					&& imgInput.getPixel(px + 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py + 1) > imgInput.getPixel(px, py))
				countPatternC4++;
		} else {
			
			// 8-conexo
			
			// padrao 1 => 8-conexo
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px + 1, py - 1)
					&& imgInput.getPixel(px + 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py - 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py - 1) < imgInput.getPixel(px, py))
				countPatternC1++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px - 1, py - 1)
					&& imgInput.getPixel(px - 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py - 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py - 1) < imgInput.getPixel(px, py))
				countPatternC1++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px - 1, py + 1)
					&& imgInput.getPixel(px - 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py + 1) < imgInput.getPixel(px, py))
				countPatternC1++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px + 1, py + 1)
					&& imgInput.getPixel(px + 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py + 1) < imgInput.getPixel(px, py))
				countPatternC1++;

			// padrao 2 => 8-conexo
			if (imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px + 1, py + 1)
					&& imgInput.getPixel(px, py + 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py + 1) > imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px - 1, py + 1)
					&& imgInput.getPixel(px, py + 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py + 1) > imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px - 1, py - 1)
					&& imgInput.getPixel(px, py - 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py - 1) > imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px + 1, py - 1)
					&& imgInput.getPixel(px, py - 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py - 1) > imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px - 1, py - 1)
					&& imgInput.getPixel(px - 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py - 1) >= imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px + 1, py - 1)
					&& imgInput.getPixel(px + 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py - 1) >= imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px + 1, py + 1)
					&& imgInput.getPixel(px + 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py + 1) >= imgInput.getPixel(px, py))
				countPatternC2++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px - 1, py + 1)
					&& imgInput.getPixel(px - 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py + 1) >= imgInput.getPixel(px, py))
				countPatternC2++;

			// padrao 3 => 8-conexo
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px - 1, py + 1)
					&& imgInput.getPixel(px - 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py + 1) > imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px + 1, py + 1)
					&& imgInput.getPixel(px + 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py + 1) > imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px + 1, py - 1)
					&& imgInput.getPixel(px + 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py - 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py - 1) > imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px - 1, py - 1)
					&& imgInput.getPixel(px - 1, py) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py - 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py - 1) > imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px + 1, py + 1)
					&& imgInput.getPixel(px + 1, py) <= imgInput.getPixel(px,py)
					&& imgInput.getPixel(px, py + 1) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py + 1) < imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px - 1, py + 1)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.getPixel(px - 1, py) <= imgInput.getPixel(px,py)
					&& imgInput.getPixel(px - 1, py + 1) < imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) > imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px - 1, py - 1)
					&& imgInput.getPixel(px - 1, py) <= imgInput.getPixel(px,py)
					&& imgInput.getPixel(px, py - 1) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px - 1, py - 1) < imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px + 1, py - 1)
					&& imgInput.getPixel(px + 1, py) <= imgInput.getPixel(px,py)
					&& imgInput.getPixel(px, py - 1) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px + 1, py - 1) < imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px - 1, py - 1)
					&& imgInput.getPixel(px - 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py - 1) <= imgInput.getPixel(px,py)
					&& imgInput.getPixel(px - 1, py - 1) <= imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.isPixelValid(px + 1, py - 1)
					&& imgInput.getPixel(px + 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py - 1) <= imgInput.getPixel(px,py)
					&& imgInput.getPixel(px + 1, py - 1) <= imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px + 1, py + 1)
					&& imgInput.getPixel(px + 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) <= imgInput.getPixel(px,py)
					&& imgInput.getPixel(px + 1, py + 1) <= imgInput.getPixel(px, py))
				countPatternC3++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.isPixelValid(px - 1, py + 1)
					&& imgInput.getPixel(px - 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) <= imgInput.getPixel(px,py)
					&& imgInput.getPixel(px - 1, py + 1) <= imgInput.getPixel(px, py))
				countPatternC3++;

			// padrao 4 => 8-conexo
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.getPixel(px + 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py - 1) > imgInput.getPixel(px, py))
				countPatternC4++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py - 1)
					&& imgInput.getPixel(px - 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py - 1) > imgInput.getPixel(px, py))
				countPatternC4++;
			if (imgInput.isPixelValid(px - 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.getPixel(px - 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) > imgInput.getPixel(px, py))
				countPatternC4++;
			if (imgInput.isPixelValid(px + 1, py)
					&& imgInput.isPixelValid(px, py + 1)
					&& imgInput.getPixel(px + 1, py) > imgInput.getPixel(px, py)
					&& imgInput.getPixel(px, py + 1) > imgInput.getPixel(px, py))
				countPatternC4++;

		}
		
	}

	
}
