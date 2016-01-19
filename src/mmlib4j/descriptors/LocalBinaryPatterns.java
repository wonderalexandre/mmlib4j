package mmlib4j.descriptors;

import mmlib4j.filtering.Histogram;
import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.ImageBuilder;

public class LocalBinaryPatterns {

	int	adjX[];
	int adjY[];
	private static int lutLBPUniform[] = {0, 1, 2, 3, 4, 58 , 5, 6, 7, 58 , 58 , 58 , 8, 58 , 9, 10, 11, 58 , 58 , 58 , 58 , 58 , 58 , 58 , 12, 58 , 58 , 58 , 13, 58 , 14, 15, 16, 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 17, 58 , 58 , 58 , 58 , 58 , 58 , 58 , 18, 58 , 58 , 58 , 19, 58 , 20, 21, 22, 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 23, 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 24, 58 , 58 , 58 , 58 , 58 , 58 , 58 , 25, 58 , 58 , 58 , 26, 58 , 27, 28, 29, 30, 58 , 31, 58 , 58 , 58 , 32, 58 , 58 , 58 , 58 , 58 , 58 , 58 , 33, 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 34, 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 35, 36, 37, 58 , 38, 58 , 58 , 58 , 39, 58 , 58 , 58 , 58 , 58 , 58 , 58 , 40, 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 58 , 41, 42, 43, 58 , 44, 58 , 58 , 58 , 45, 58 , 58 , 58 , 58 , 58 , 58 , 58 , 46, 47, 48, 58 , 49, 58 , 58 , 58 , 50, 51, 52, 58 , 53, 54, 55, 56, 57};
	
	
	public LocalBinaryPatterns(){
		adjX = new int[8];
		adjY = new int[8];
		
		adjX[0] = -1; adjY[0] = 0;
		
		adjX[1] = -1; adjY[1] = +1;
		adjX[2] = 0; adjY[2] = +1;
		adjX[3] = +1; adjY[3] = +1;
		
		adjX[4] = +1; adjY[4] = 0;
		
		adjX[5] = +1; adjY[5] = -1;
		adjX[6] = 0; adjY[6] = -1;
		adjX[7] = -1; adjY[7] = -1;
		
	}
	
	public int getLBP(GrayScaleImage img, int px, int py, int threshold){
		int qx, qy;
		int code = 0;
		for(int i=0; i < adjX.length; i++){
			 qx = px + adjX[i];
			 qy = py + adjY[i];
			 if(img.getPixel(qx, qy) - img.getPixel(px, py) >= threshold){
				 code += Math.pow(2, i);
			 }
		}
		return code;
	}
	
	public int getLBP(GrayScaleImage img, int px, int py){
		return getLBP(img, px, py, 0);
	}
	
	public int getLBPUniform(GrayScaleImage img, int px, int py, int threshold){
		return lutLBPUniform[getLBP(img, py, py, threshold)];
	}
	
	public int getLBPUniform(GrayScaleImage img, int px, int py){
		return lutLBPUniform[getLBP(img, px, py)] == 58? 200: lutLBPUniform[getLBP(img, px, py)];
	}
	
	
	
	
	public static void main(String args[]){
		GrayScaleImage img = ImageBuilder.openGrayImage();
		GrayScaleImage imgLBP = ImageFactory.createGrayScaleImage(8, img.getWidth(), img.getHeight());
		GrayScaleImage imgLBPUniform = ImageFactory.createGrayScaleImage(8, img.getWidth(), img.getHeight());
		
		LocalBinaryPatterns lbp = new LocalBinaryPatterns();
		
		for(int x=1; x < img.getWidth()-1; x++){
			for(int y=1; y < img.getHeight()-1; y++){
				imgLBP.setPixel(x, y, lbp.getLBP(img, x, y));
				imgLBPUniform.setPixel(x, y, lbp.getLBPUniform(img, x, y));
			}
		}
		
		WindowImages.show(img, imgLBP, new Histogram(imgLBPUniform).equalisation());
		
	}
	
	/*
	private static int binaryNumber(String s){
		int code = 0;
		for(int i=0; i < s.length(); i++){
			if(s.charAt(i)=='1')
				code += Math.pow(2, i);
		}
		if(hist[code] == 1)
			System.out.println(s);
		hist[code]++;
		return code;
		
	}
	
	static int hist[] = new int[256];
	 


	public static void main(String args[]){
	
		System.out.println("U=0");
		System.out.println(binaryNumber("00000000"));
		System.out.println(binaryNumber("11111111"));
		
		System.out.println("\nU=2");
		System.out.println(binaryNumber("01111111")); //8
		System.out.println(binaryNumber("10111111"));
		System.out.println(binaryNumber("11011111"));
		System.out.println(binaryNumber("11101111"));
		System.out.println(binaryNumber("11110111"));
		System.out.println(binaryNumber("11111011"));
		System.out.println(binaryNumber("11111101"));
		System.out.println(binaryNumber("11111110"));
		
		System.out.println(binaryNumber("00111111")); //7
		System.out.println(binaryNumber("10011111"));
		System.out.println(binaryNumber("11001111"));
		System.out.println(binaryNumber("11100111"));
		System.out.println(binaryNumber("11110011"));
		System.out.println(binaryNumber("11111001"));
		System.out.println(binaryNumber("11111100")); 
		
		System.out.println(binaryNumber("01111110"));
		
		System.out.println(binaryNumber("00011111")); //6
		System.out.println(binaryNumber("10001111"));
		System.out.println(binaryNumber("11000111"));
		System.out.println(binaryNumber("11100011"));
		System.out.println(binaryNumber("11110001"));
		System.out.println(binaryNumber("11111000"));
		
		System.out.println(binaryNumber("01111100")); //2
		System.out.println(binaryNumber("00111110"));
		
		System.out.println(binaryNumber("00001111")); //5
		System.out.println(binaryNumber("10000111"));
		System.out.println(binaryNumber("11000011"));
		System.out.println(binaryNumber("11100001"));
		System.out.println(binaryNumber("11110000"));
		
		System.out.println(binaryNumber("01111000")); //4
		System.out.println(binaryNumber("00111100"));
		System.out.println(binaryNumber("00011110"));
		//System.out.println(binaryNumber("00001111"));
		
		System.out.println(binaryNumber("00000111")); //4
		System.out.println(binaryNumber("10000011"));
		System.out.println(binaryNumber("11000001"));
		System.out.println(binaryNumber("11100000"));
		
		
		System.out.println(binaryNumber("01110000")); //5
		System.out.println(binaryNumber("00111000"));
		System.out.println(binaryNumber("00011100"));
		System.out.println(binaryNumber("00001110"));
		//System.out.println(binaryNumber("00000111"));
		
		System.out.println(binaryNumber("00000011")); //3
		System.out.println(binaryNumber("10000001"));
		//System.out.println(binaryNumber("11000000"));
		
		
		System.out.println(binaryNumber("11000000")); //6
		System.out.println(binaryNumber("01100000"));
		System.out.println(binaryNumber("00110000"));
		System.out.println(binaryNumber("00011000"));
		System.out.println(binaryNumber("00001100"));
		System.out.println(binaryNumber("00000110"));
		 
		
		System.out.println(binaryNumber("10000000")); //8
		System.out.println(binaryNumber("01000000"));
		System.out.println(binaryNumber("00100000"));
		System.out.println(binaryNumber("00010000"));
		System.out.println(binaryNumber("00001000"));
		System.out.println(binaryNumber("00000100"));
		System.out.println(binaryNumber("00000010"));
		System.out.println(binaryNumber("00000001"));
		
		
		
		System.out.println("histograma");
		System.out.print("\n int LUT[] = {");
		int cont=0;
		for(int i=0; i < 256; i++){
			if(hist[i] == 1){
				System.out.print(cont++ + ", ");
			}else
				System.out.print("58 , ");
		}
		System.out.println("\ncont:"+ cont);
		
	}
	*/
	
}
