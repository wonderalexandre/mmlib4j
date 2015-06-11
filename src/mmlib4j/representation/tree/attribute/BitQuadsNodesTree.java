package mmlib4j.representation.tree.attribute;



/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class BitQuadsNodesTree {
	
	//private int lutQ0[][] = new int [1][512];
	private final static int lutQ4[][] = new int [1][512];
	private final static int lutQ1[][] = new int [4][512];
	private final static int lutQ2[][] = new int [4][512];
	private final static int lutQ3[][] = new int [4][512];
	private final static int lutQD[][] = new int [2][512];
	

	private int width;
	private int height;
	private boolean pixels[][];
	
	
	public BitQuadsNodesTree(int width, int height){
		this.width = width;
		this.height = height;
		pixels = new boolean[width][height];
		//createLuts();
	}
	
	

	static{
		/*
		//Q0 =  0 0
		//      0 0
		createHMlut(new int[]{0, 0, 2, 
								0, 0, 2, 
								2, 2, 2}, lutQ0[0]);  //2 eh tanto faz => matriz 3 x 3
		*/
		//Q1 =  0 0   0 0   0 1   1 0
		//      1 0   0 1   0 0   0 0		
		createHMlut(new int[]{1, 0, 2, 
							  0, 0, 2, 
							  2, 2, 2}, lutQ1[0]);
		createHMlut(new int[]{0, 1, 2, 
							  0, 0, 2, 
							  2, 2, 2}, lutQ1[1]);
		createHMlut(new int[]{0, 0, 2, 
							  1, 0, 2, 
							  2, 2, 2}, lutQ1[2]);
		createHMlut(new int[]{0, 0, 2, 
							  0, 1, 2, 
							  2, 2, 2}, lutQ1[3]);
		
		//Q2 =  0 0   1 0   1 1   0 1
		//      1 1   1 0   0 0   0 1
		createHMlut(new int[]{0, 0, 2, 
							  1, 1, 2, 
							  2, 2, 2}, lutQ2[0]);
		createHMlut(new int[]{1, 0, 2, 
							  1, 0, 2, 
							  2, 2, 2}, lutQ2[1]);
		createHMlut(new int[]{1, 1, 2, 
							  0, 0, 2, 
							  2, 2, 2}, lutQ2[2]);
		createHMlut(new int[]{0, 1, 2, 
							  0, 1, 2, 
							  2, 2, 2}, lutQ2[3]);
		
		//Q3 =  0 1   1 0   1 1   1 1
		//      1 1   1 1   1 0   0 1
		createHMlut(new int[]{0, 1, 2, 
							  1, 1, 2, 
							  2, 2, 2}, lutQ3[0]);
		createHMlut(new int[]{1, 0, 2, 
							  1, 1, 2, 
							  2, 2, 2}, lutQ3[1]);
		createHMlut(new int[]{1, 1, 2, 
						 	  1, 0, 2, 
						 	  2, 2, 2}, lutQ3[2]);
		createHMlut(new int[]{1, 1, 2, 
							  0, 1, 2, 
							  2, 2, 2}, lutQ3[3]);

		//Q4 =  1 1
		//      1 1
		createHMlut(new int[]{1, 1, 2, 
							  1, 1, 2, 
							  2, 2, 2}, lutQ4[0]);
		
		//QD =  0 1   1 0
		//      1 0   0 1
		createHMlut(new int[]{0, 1, 2, 
							  1, 0, 2, 
							  2, 2, 2}, lutQD[0]);
		createHMlut(new int[]{1, 0, 2, 
							  0, 1, 2, 
							  2, 2, 2}, lutQD[1]);
	}

	private static void createHMlut(int [] kernel, int [] lut){
		int i, j, match, toMatch;

		for(i=0;i<512;i++)
			lut[i]=1;

		toMatch=0;
		for(j=0;j<9;j++){
			if (kernel[j]!=2)
				toMatch++;
		}

		//make lut
		for(i=0;i<512;i++){
			match=0;
			for(j=0;j<9;j++){
				if (kernel[j]!=2){
					if (   (((i & (int)Math.pow(2,j))!=0)? 1:0) == kernel[j]) 
						match++;
				}
			}
			if (match!=toMatch){
				lut[i]=0;
			}
		}
		
	
		
	}
	
	
	private int getPixel(int x, int y){
		return pixels[x][y]? 1 : 0;
	}
	
	
	/**
	 * IGrayScaleImage => 0 background e 1 foreground
	 * @param x
	 * @param y
	 * @param img
	 * @return
	 */
	private int getPattern(int x, int y){
		int pattern = -1;
		if(x >= 1 && x < width-1 && y >= 1 && y < height-1){
			pattern = 
					getPixel(x-1,y-1) +
					getPixel(x  ,y-1) * 2 +
					getPixel(x+1,y-1) * 4 +
					getPixel(x-1,y  ) * 8 +
					getPixel(x  ,y  ) * 16 +
					getPixel(x+1,y  ) * 32 +
					getPixel(x-1,y+1) * 64 +
					getPixel(x  ,y+1) * 128 +
					getPixel(x+1,y+1) * 256;
		}
		else if(y == 0 && x>=1 && x < width-1){//upper row
			pattern = 
					getPixel(x-1,y  ) * 8 +
					getPixel(x  ,y  ) * 16 +
					getPixel(x+1,y  ) * 32 +
					getPixel(x-1,y+1) * 64 +
					getPixel(x  ,y+1) * 128 +
					getPixel(x+1,y+1) * 256;
		}
		else if(y == height-1 && x >= 1 && x < width-1){//lower row
			pattern =
					getPixel(x-1,y-1) +
					getPixel(x  ,y-1) * 2 +
					getPixel(x+1,y-1) * 4 +
					getPixel(x-1,y  ) * 8 +
					getPixel(x  ,y  ) * 16 +
					getPixel(x+1,y  ) * 32;
		}
		else if(x == 0 && y >= 1 && y < height-1){//left column
			pattern =
					getPixel(x  ,y-1) * 2 +
					getPixel(x+1,y-1) * 4 +
					getPixel(x  ,y  ) * 16 +
					getPixel(x+1,y  ) * 32 +
					getPixel(x  ,y+1) * 128 +
					getPixel(x+1,y+1) * 256;
		}
		else if(x == width-1 && y >= 1 && y < height-1){//right column
			pattern =
					getPixel(x-1,y-1) +
					getPixel(x  ,y-1) * 2 + 
					getPixel(x-1,y  ) * 8 +
					getPixel(x  ,y  ) * 16 +
					getPixel(x-1,y+1) * 64 +
					getPixel(x  ,y+1) * 128; 
		}
		else if(x == 0 && y == 0){//upper left corner
			pattern = 
					getPixel(x,y) * 16 + 
					getPixel(x+1,y) * 32 + 
					getPixel(x,y+1) * 128 + 
					getPixel(x+1,y+1) * 256;
		}
		else if(x == width-1 && y == 0){//upper right corner
			pattern = 
					getPixel(x-1,y) * 8 + 
					getPixel(x,y) * 16 + 
					getPixel(x-1,y+1) * 64 + 
					getPixel(x,y+1) * 128;
		}
		else if(x == 0 && y == height-1){ //lower left corner
			pattern = getPixel(x,y-1) * 2 + 
					getPixel(x+1,y-1) * 4 + 
					getPixel(x,y) * 16 + 
					getPixel(x+1,y) * 32;
		}
		else if(x == width-1 && y == height-1){//lower right corner
			pattern = 
					getPixel(x-1,y-1) + 
					getPixel(x,y-1) * 2 + 
					getPixel(x-1,y) * 8 + 
					getPixel(x  ,y) * 16;
		}
		return pattern;
	}
	
	
	public void updatePixel(int x, int y){
		pixels[x][y] = true;
	}
	
	public void updateLocalHitOrMissLeaf(BitQuadAttributePattern attrPattern, int pattern){
		for(int i=0; i < 4; i++){
			attrPattern.countPatternQ1 += lutQ1[i][pattern];
			attrPattern.countPatternQ2 += lutQ2[i][pattern];
			attrPattern.countPatternQ3 += lutQ3[i][pattern];
		}
		attrPattern.countPatternQD += lutQD[0][pattern];
		attrPattern.countPatternQD += lutQD[1][pattern];
	
		//countPatternQ0 += lutQ0[0][pattern];
		attrPattern.countPatternQ4 += lutQ4[0][pattern];
	}
	
	public void updatePixel(int p){
		updatePixel(p % width, p / width);
	
	}
	
	/*
	public void computerLocalHitOrMiss(IGrayScaleImage img, int x, int y){
		for(int i=-1; i <= 1; i++){
			for(int j=-1; j <= 1; j++){
				if(img.isPixelValid(x + i, y + j))
					pixels[x+i][y+j] = img.getPixel(x+i, y+j) == 1? true : false;
			}	
		}
		computerLocalHitOrMiss(x, y);
	}*/
	
	public void computerLocalHitOrMiss(BitQuadAttributePattern attrPattern, int p){
		computerLocalHitOrMiss(attrPattern, p % width, p / width);
	}
	
	
	public void computerLocalHitOrMiss(BitQuadAttributePattern attrPattern, int x, int y){
		int pattern = getPattern(x, y);
		for(int i=0; i < 4; i++){
			attrPattern.countPatternQ1 += lutQ1[i][pattern];
			attrPattern.countPatternQ2 += lutQ2[i][pattern];
			attrPattern.countPatternQ3 += lutQ3[i][pattern];
		}
		attrPattern.countPatternQD += lutQD[0][pattern];
		attrPattern.countPatternQD += lutQD[1][pattern];
	
		//countPatternQ0 += lutQ0[0][pattern];
		attrPattern.countPatternQ4 += lutQ4[0][pattern];
	}

	
}
