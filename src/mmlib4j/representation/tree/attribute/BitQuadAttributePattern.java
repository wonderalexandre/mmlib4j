package mmlib4j.representation.tree.attribute;


/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class BitQuadAttributePattern {

	
	//private int countPatternQ0 = 0;
		int countPatternQ4 = 0;
		int countPatternQ1 = 0;
		int countPatternQ3 = 0;
		int countPatternQ2 = 0;
		int countPatternQD = 0;
		
		public void clear(){
			countPatternQ4 = 0;
			//countPatternQ0 = 0;
			countPatternQ1 = 0;
			countPatternQ3 = 0;
			countPatternQ2 = 0;
			countPatternQD = 0;
		}
		
		public void merge(BitQuadAttributePattern attrPattern){
			this.countPatternQ4 = this.countPatternQ4 + attrPattern.countPatternQ4;
			//countPatternQ0 = 0;
			this.countPatternQ1 = this.countPatternQ1 + attrPattern.countPatternQ1;
			this.countPatternQ3 = this.countPatternQ3 + attrPattern.countPatternQ3;
			this.countPatternQ2 = this.countPatternQ2 + attrPattern.countPatternQ2;
			this.countPatternQD = this.countPatternQD + attrPattern.countPatternQD;
			
			//attrPattern.printPattern();
			//this.printPattern();
			
		}
		
		public String printPattern(){
			String s = "Q1: " + countPatternQ1;
			s += "\tQ2: " + countPatternQ2;
			s += "\tQ3: " + countPatternQ3;
			s += "\tQ4: " + countPatternQ4;
			s += "\tQD: " + countPatternQD;
			return s;
		}
		
		public int numberHoles8(){
			return 1 - numberEuler8();
		}
		
		public int numberHoles4(){
			return 1 - numberEuler4();
		}
		
		public int numberEuler8(){
			return (countPatternQ1 - countPatternQ3 - 2 * countPatternQD) / 4;
		}
		
		public int numberEuler4(){
			return (countPatternQ1 - countPatternQ3 + 2 * countPatternQD) / 4;
		}
		
		
		public int getArea(){
			return (countPatternQ1 + 2*countPatternQ2 + 3*countPatternQ3 + 4*countPatternQ4 + 2*countPatternQD) / 4;
		}
		
		//area de objetos continuos => Duda
		public double getArea2(){
			return (1.0/4.0*countPatternQ1 + 1.0/2.0*countPatternQ2 + 7.0/8.0*countPatternQ3 + countPatternQ4 + 3.0/4.0*countPatternQD);
		}
		
		public int getPerimeter(){
			return (countPatternQ1 + countPatternQ2 + countPatternQ3 + 2*countPatternQD);
		}
		
		//perimetro de objetos continuos => Duda
		public double getPerimeterDuda(){
			return (countPatternQ2 + ( (1.0/Math.sqrt(2.0)) * (countPatternQ1 + countPatternQ3 + 2*countPatternQD) ));
		}
		
		public double getCircularity(){
			return (4.0 * Math.PI * getArea()) / Math.pow(getPerimeter(), 2);
		}
		public double getCompacityDuda(){
			return Math.pow(getPerimeterDuda(), 2) / getArea2();
		}
		public double getCompacity(){
			return Math.pow(getPerimeter(), 2) / getArea();
		}
		
		
		public double getAreaAverage(){
			return (getArea()  / (double) numberEuler8());
		}
		
		public double getPerimeterAverage(){
			return (getPerimeter()  / (double) numberEuler8());
		}
		
		public double getLengthAverage(){
			return (getPerimeterAverage()  / 2.0);
		}
		
		public double getWidthAverage(){
			return (2* getAreaAverage()  / getPerimeterAverage());
		}
		
		public BitQuadAttributePattern getClone(){
			BitQuadAttributePattern b = new BitQuadAttributePattern();
			b.countPatternQ1 = this.countPatternQ1;
			b.countPatternQ2 = this.countPatternQ2;
			b.countPatternQ3 = this.countPatternQ3;
			b.countPatternQD = this.countPatternQD;
			b.countPatternQ4 = this.countPatternQ4;
			return b;
		}
}
