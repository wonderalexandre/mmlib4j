package mmlib4j.utils;

import mmlib4j.images.GrayScaleImage;


public class BitQuads {
	
	private static  BitQuads instance = null;
	
	//private int lutQ0[][] = new int [1][512];
	private int lutQ4[][] = new int [1][512];
	private int lutQ1[][] = new int [4][512];
	private int lutQ2[][] = new int [4][512];
	private int lutQ3[][] = new int [4][512];
	private int lutQD[][] = new int [2][512];
	
	//private int countPatternQ0 = 0;
	private int countPatternQ4 = 0;
	private int countPatternQ1 = 0;
	private int countPatternQ3 = 0;
	private int countPatternQ2 = 0;
	private int countPatternQD = 0;
	
	private int width;
	private int height;
	private boolean pixels[][];
	int area;
	
	private BitQuads(int width, int height){
		this.width = width;
		this.height = height;
		pixels = new boolean[width][height];
		createLuts();
	}
	
	public static BitQuads getInstance(int width, int height){
		if(instance == null)
			instance = new BitQuads(width, height);
		return instance;
	}
	
	public void clear(){
		countPatternQ4 = 0;
		//countPatternQ0 = 0;
		countPatternQ1 = 0;
		countPatternQ3 = 0;
		countPatternQ2 = 0;
		countPatternQD = 0;
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
		return countPatternQ1 + countPatternQ2 + countPatternQ3 + 2*countPatternQD;
	}
	
	//perimetro de objetos continuos => Duda
	public double getPerimeter2(){
		return countPatternQ2 + ( (1.0/Math.sqrt(2)) * (countPatternQ1 + countPatternQ3 + 2*countPatternQD) );
	}
	
	public double getCircularity(){
		return (4.0 * Math.PI * getArea()) / Math.pow(getPerimeter(), 2);
	}
	
	public double getCircularity2(){
		return (4.0 * Math.PI * getArea2()) / Math.pow(getPerimeter2(), 2);
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
	
	
	public void createLuts(){
		
		//Q0 =  0 0
		//      0 0
		//createHMlut(new int[]{0, 0, 2, 0, 0, 2, 2, 2, 2}, lutQ0[0]);  //2 eh tanto faz => matriz 3 x 3
		
		//Q1 =  0 0   0 0   0 1   1 0
		//      1 0   0 1   0 0   0 0		
		createHMlut(new int[]{1, 0, 2, 0, 0, 2, 2, 2, 2}, lutQ1[0]);
		
		createHMlut(new int[]{0, 1, 2, 0, 0, 2, 2, 2, 2}, lutQ1[1]);
		createHMlut(new int[]{0, 0, 2, 1, 0, 2, 2, 2, 2}, lutQ1[2]);
		createHMlut(new int[]{0, 0, 2, 0, 1, 2, 2, 2, 2}, lutQ1[3]);
		
		//Q2 =  0 0   1 0   1 1   0 1
		//      1 1   1 0   0 0   0 1
		createHMlut(new int[]{0, 0, 2, 1, 1, 2, 2, 2, 2}, lutQ2[0]);
		createHMlut(new int[]{1, 0, 2, 1, 0, 2, 2, 2, 2}, lutQ2[1]);
		createHMlut(new int[]{1, 1, 2, 0, 0, 2, 2, 2, 2}, lutQ2[2]);
		createHMlut(new int[]{0, 1, 2, 0, 1, 2, 2, 2, 2}, lutQ2[3]);
		
		//Q3 =  0 1   1 0   1 1   1 1
		//      1 1   1 1   1 0   0 1
		createHMlut(new int[]{0, 1, 2, 1, 1, 2, 2, 2, 2}, lutQ3[0]);
		createHMlut(new int[]{1, 0, 2, 1, 1, 2, 2, 2, 2}, lutQ3[1]);
		createHMlut(new int[]{1, 1, 2, 1, 0, 2, 2, 2, 2}, lutQ3[2]);
		createHMlut(new int[]{1, 1, 2, 0, 1, 2, 2, 2, 2}, lutQ3[3]);

		//Q4 =  1 1
		//      1 1
		createHMlut(new int[]{1, 1, 2, 1, 1, 2, 2, 2, 2}, lutQ4[0]);
		
		//QD =  0 1   1 0
		//      1 0   0 1
		createHMlut(new int[]{0, 1, 2, 1, 0, 2, 2, 2, 2}, lutQD[0]);
		createHMlut(new int[]{1, 0, 2, 0, 1, 2, 2, 2, 2}, lutQD[1]);
	}

	private void createHMlut(int [] kernel, int [] lut){
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
	
	public void update(int x, int y){	
		pixels[x][y] = true;
		area++;
		
	}
	
	
	public void computerLocalHitOrMiss(int x, int y){
		int pattern = getPattern(x, y);
		for(int i=0; i < 4; i++){
			countPatternQ1 += lutQ1[i][pattern];
			countPatternQ2 += lutQ2[i][pattern];
			countPatternQ3 += lutQ3[i][pattern];
		}
		countPatternQD += lutQD[0][pattern];
		countPatternQD += lutQD[1][pattern];
	
		//countPatternQ0 += lutQ0[0][pattern];
		countPatternQ4 += lutQ4[0][pattern];
	}
	
	
	
	
	
	public static void main(String args[]) {
		GrayScaleImage img = ImageBuilder.openGrayImage(ImageBuilder.windowOpenFile()); 
		
		BitQuads hm = getInstance(img.getWidth(), img.getHeight());
		for(int x=0; x < img.getWidth(); x++)
			for(int y=0; y < img.getHeight(); y++)
			if(img.getPixel(x, y) != 0)
				hm.update(x, y);			
		
		
		AdjacencyRelation adj = AdjacencyRelation.getCircular(1.5);
		for(int x=0; x < img.getWidth(); x++){
			for(int y=0; y < img.getHeight(); y++){
				if(img.getPixel(x, y) != 0)
					hm.computerLocalHitOrMiss(x, y);
				
			}
		}
		
		
		System.out.println("4conexo - Euler:"+ hm.numberEuler4());
		System.out.println("4Numero de holes (4-hole e 8-cc): " + hm.numberHoles4());
		System.out.println("8conexo - Euler:"+ hm.numberEuler8());
		System.out.println("8Numero de holes (4-hole e 8-cc): " + hm.numberHoles8());
		System.out.println("Area:"+ hm.getArea());
		System.out.println("Perimeter:"+ hm.getPerimeter());
		System.out.println("Perimeter:"+ hm.getPerimeter2());
		System.out.println("Circularity:"+ hm.getCircularity());
		System.out.println("Circularity:"+ hm.getCircularity2());
		
		
		
	}


}
