package mmlib4j.filtering.binary;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.utils.ImageBuilder;

/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class EulerCharacteristic {
	
	
	private int lutP1[][] = new int [4][512];
	private int lutP2[][] = new int [4][512];
	private int lutP3[][] = new int [2][512];
	
	
	private int countPatternP1 = 0;
	private int countPatternP2 = 0;
	private int countPatternP3 = 0;
	
	private GrayScaleImage img;
	
	
	
	public EulerCharacteristic(GrayScaleImage img){
		createLuts();
		this.img = img;
	}
	
	public void clear(){
		countPatternP1 = 0;
		countPatternP2 = 0;
		countPatternP3 = 0;
	}
	
	public int numberHoles8(){
		return 1 - numberEuler8();
	}
	
	public int numberHoles4(){
		return 1 - numberEuler4();
	}
	
	public int numberEuler8(){
		return (countPatternP1 - countPatternP2 - 2 * countPatternP3) / 4;
	}
	
	public int numberEuler4(){
		return (countPatternP1 - countPatternP2 + 2 * countPatternP3) / 4;
	}
	
	public void createLuts(){
		//P1 =  0 0   0 0   0 1   1 0
		//      1 0   0 1   0 0   0 0		
		createHMlut(new int[]{1, 0, 2, 0, 0, 2, 2, 2, 2}, lutP1[0]);
		createHMlut(new int[]{0, 1, 2, 0, 0, 2, 2, 2, 2}, lutP1[1]);
		createHMlut(new int[]{0, 0, 2, 1, 0, 2, 2, 2, 2}, lutP1[2]);
		createHMlut(new int[]{0, 0, 2, 0, 1, 2, 2, 2, 2}, lutP1[3]);
		
		//P2 =  0 1   1 0   1 1   1 1
		//      1 1   1 1   1 0   0 1
		createHMlut(new int[]{0, 1, 2, 1, 1, 2, 2, 2, 2}, lutP2[0]);
		createHMlut(new int[]{1, 0, 2, 1, 1, 2, 2, 2, 2}, lutP2[1]);
		createHMlut(new int[]{1, 1, 2, 1, 0, 2, 2, 2, 2}, lutP2[2]);
		createHMlut(new int[]{1, 1, 2, 0, 1, 2, 2, 2, 2}, lutP2[3]);

		//P3 =  0 1   1 0
		//      1 0   0 1
		createHMlut(new int[]{0, 1, 2, 1, 0, 2, 2, 2, 2}, lutP3[0]);
		createHMlut(new int[]{1, 0, 2, 0, 1, 2, 2, 2, 2}, lutP3[1]);
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
					if ((((i & (int)Math.pow(2,j))!=0)?1:0) == kernel[j]) match++;
				}
			}
			if (match!=toMatch){
				lut[i]=0;
			}
		}
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
		if(x >= 1 && x < img.getWidth()-1 && y >= 1 && y < img.getHeight()-1){
			pattern = 
					img.getPixel(x-1,y-1) +
					img.getPixel(x  ,y-1) * 2 +
					img.getPixel(x+1,y-1) * 4 +
					img.getPixel(x-1,y  ) * 8 +
					img.getPixel(x  ,y  ) * 16 +
					img.getPixel(x+1,y  ) * 32 +
					img.getPixel(x-1,y+1) * 64 +
					img.getPixel(x  ,y+1) * 128 +
					img.getPixel(x+1,y+1) * 256;
		}
		if(y == 0 && x>=1 && x < img.getWidth()-1){//upper row
			pattern = 
					img.getPixel(x-1,y  ) * 8 +
					img.getPixel(x  ,y  ) * 16 +
					img.getPixel(x+1,y  ) * 32 +
					img.getPixel(x-1,y+1) * 64 +
					img.getPixel(x  ,y+1) * 128 +
					img.getPixel(x+1,y+1) * 256;
		}
		if(y == img.getHeight()-1 && x >= 1 && x < img.getWidth()-1){//lower row
			pattern =
					img.getPixel(x-1,y-1) +
					img.getPixel(x  ,y-1) * 2 +
					img.getPixel(x+1,y-1) * 4 +
					img.getPixel(x-1,y  ) * 8 +
					img.getPixel(x  ,y  ) * 16 +
					img.getPixel(x+1,y  ) * 32;
		}
		if(x == 0 && y >= 1 && y < img.getHeight()-1){//left column
			pattern =
					img.getPixel(x  ,y-1) * 2 +
					img.getPixel(x+1,y-1) * 4 +
					img.getPixel(x  ,y  ) * 16 +
					img.getPixel(x+1,y  ) * 32 +
					img.getPixel(x  ,y+1) * 128 +
					img.getPixel(x+1,y+1) * 256;
		}
		if(x == img.getWidth()-1 && y >= 1 && y < img.getHeight()-1){//right column
			pattern =
					img.getPixel(x-1,y-1) +
					img.getPixel(x  ,y-1) * 2 + 
					img.getPixel(x-1,y  ) * 8 +
					img.getPixel(x  ,y  ) * 16 +
					img.getPixel(x-1,y+1) * 64 +
					img.getPixel(x  ,y+1) * 128; 
		}
		if(x == 0 && y == 0){//upper left corner
			pattern = 
					img.getPixel(x,y) * 16 + 
					img.getPixel(x+1,y) * 32 + 
					img.getPixel(x,y+1) * 128 + 
					img.getPixel(x+1,y+1) * 256;
		}
		if(x == img.getWidth()-1 && y == 0){//upper right corner
			pattern = 
					img.getPixel(x-1,y) * 8 + 
					img.getPixel(x,y) * 16 + 
					img.getPixel(x-1,y+1) * 64 + 
					img.getPixel(x,y+1) * 128;
		}
		if(x == 0 && y == img.getHeight()-1){ //lower left corner
			pattern = img.getPixel(x,y-1) * 2 + 
					img.getPixel(x+1,y-1) * 4 + 
					img.getPixel(x,y) * 16 + 
					img.getPixel(x+1,y) * 32;
		}
		if(x == img.getWidth()-1 && y == img.getHeight()-1){//lower right corner
			pattern = 
					img.getPixel(x-1,y-1) + 
					img.getPixel(x,y-1) * 2 + 
					img.getPixel(x-1,y) * 8 + 
					img.getPixel(x  ,y) * 16;
		}
		return pattern;
	}
	
	public void computerLocalHitOrMiss(int x, int y){
		
		int pattern = getPattern(x, y);
		for(int i=0; i < 4; i++){
			countPatternP1 += lutP1[i][pattern];
			countPatternP2 += lutP2[i][pattern];
		}
		countPatternP3 += lutP3[0][pattern];
		countPatternP3 += lutP3[1][pattern];
		
	}
	
	
	
	
	public GrayScaleImage getImage(){
		return img;
	}
	
	public static void main(String args[]) {
		GrayScaleImage img = ImageBuilder.openGrayImage(ImageBuilder.windowOpenFile()); 
		
		for(int i=0; i < img.getSize(); i++){
			//img.setPixel(i,  img.getPixel(i) == 0? 0 : 1  );
			img.setPixel(i,  img.getPixel(i) == 0? 0 : 1  );
		}
		EulerCharacteristic hm = new EulerCharacteristic(img);
		
		
		for(int x=0; x < img.getWidth(); x++){
			for(int y=0; y < img.getHeight(); y++){
				hm.computerLocalHitOrMiss(x, y);
			}
		}
		
		
		System.out.println("4conexo - Euler:"+ hm.numberEuler4());
		System.out.println("4Numero de holes (4-hole e 8-cc): " + hm.numberHoles4());
		
		System.out.println("8conexo - Euler:"+ hm.numberEuler8());
		System.out.println("8Numero de holes (4-hole e 8-cc): " + hm.numberHoles8());
		
		
	}


}
