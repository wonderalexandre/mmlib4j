package mmlib4j.filtering;

import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.segmentation.Labeling;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ToggleMapping {

	
	public  static int LOW_VALUE = 0;
	public  static int HIGH_VALUE = 255;
	public  static int UNKNOWN_VALUE = 128;
	
	
	public static void toggleMapping (GrayScaleImage img, AdjacencyRelation adj, int contrast1, int percentage, GrayScaleImage imgOut){
		MorphologicalOperatorsBasedOnSE.dilation(img, adj, imgOut);//new ComponentTree(img, adj, false).filtering(40, IMorphologicalTreeFiltering.ATTRIBUTE_HEIGHT, IMorphologicalTreeFiltering.PRUNING, IMorphologicalTreeFiltering.RULE_DIRECT); 
		GrayScaleImage imgE = MorphologicalOperatorsBasedOnSE.erosion(img, adj);//new ComponentTree(img, adj, true).filtering(40, IMorphologicalTreeFiltering.ATTRIBUTE_HEIGHT, IMorphologicalTreeFiltering.PRUNING, IMorphologicalTreeFiltering.RULE_DIRECT);
		GrayScaleImage imgD = imgOut;
		for(int i=0; i < img.getSize(); i++){
			if ( (imgD.getPixel(i) - imgE.getPixel(i)) < contrast1 ) {
              	imgOut.setPixel(i, UNKNOWN_VALUE);
            }
            else {
                if ( (imgD.getPixel(i) - img.getPixel(i)) < percentage * (img.getPixel(i) - imgE.getPixel(i))/100.0 ) {
                	imgOut.setPixel(i, HIGH_VALUE);
                }    
                else {
                	imgOut.setPixel(i, LOW_VALUE);
                }
            }
		}
	}	
	
	
	public static GrayScaleImage toggleMapping (GrayScaleImage img, AdjacencyRelation adj){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		toggleMapping(img, adj, 15, 80, imgOut);
		return imgOut;
	}	
	
	
	public static void toggleMappingResidue (GrayScaleImage img, AdjacencyRelation adj, GrayScaleImage imgOut){
		MorphologicalOperatorsBasedOnSE.dilation(img, adj, imgOut); 
		GrayScaleImage imgE = MorphologicalOperatorsBasedOnSE.erosion(img, adj);
		GrayScaleImage imgD = imgOut;
		for(int i=0; i < img.getSize(); i++){
			if ( (imgD.getPixel(i) - img.getPixel(i)) < (img.getPixel(i) - imgE.getPixel(i)) ) {
				imgOut.setPixel(i, imgD.getPixel(i) - img.getPixel(i));
			}    
			else {
				imgOut.setPixel(i, img.getPixel(i) - imgE.getPixel(i));
			}
		}
		
	}
	
	public static GrayScaleImage toggleMappingResidue (GrayScaleImage img, AdjacencyRelation adj){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		toggleMappingResidue(img, adj, imgOut);
		return imgOut;
	}
	

	public static void toggleMapping (GrayScaleImage img, GrayScaleImage extensive, GrayScaleImage antiExtensive, int contrast1, int percentage, GrayScaleImage imgOut){
		for(int i=0; i < img.getSize(); i++){
			if ( (extensive.getPixel(i) - antiExtensive.getPixel(i)) < contrast1 ) {
              	imgOut.setPixel(i, UNKNOWN_VALUE);
            }
            else {
                if ( (extensive.getPixel(i) - img.getPixel(i)) < percentage * (img.getPixel(i) - antiExtensive.getPixel(i))/100.0 ) {
                	imgOut.setPixel(i, HIGH_VALUE);
                }    
                else {
                	imgOut.setPixel(i, LOW_VALUE);
                }
            }
		}
	}	

	public static GrayScaleImage toggleMapping (GrayScaleImage img, GrayScaleImage extensive, GrayScaleImage antiExtensive){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		int contrast1 = 15;
		int percentage = 80;
		toggleMapping(img, extensive, antiExtensive, contrast1, percentage, imgOut);
		return imgOut;
	}
	
	public static void doubleToggleMapping (GrayScaleImage img, AdjacencyRelation adj, int contrast1, int contrast2, int percentage, GrayScaleImage imgOut1, GrayScaleImage imgOut2){
		MorphologicalOperatorsBasedOnSE.dilation(img, adj, imgOut1);
		MorphologicalOperatorsBasedOnSE.erosion(img, adj, imgOut2);
		
		GrayScaleImage imgD = imgOut1;
		GrayScaleImage imgE =imgOut2;
		
		for(int i=0; i < img.getSize(); i++){
			if ( (imgD.getPixel(i) - imgE.getPixel(i)) < contrast1) {
				imgOut1.setPixel(i, UNKNOWN_VALUE);
				imgOut2.setPixel(i, UNKNOWN_VALUE);
			}
			else if ( (imgD.getPixel(i) - imgE.getPixel(i)) < contrast2) {
				imgOut1.setPixel(i, UNKNOWN_VALUE);
				
				if ( (imgD.getPixel(i) - img.getPixel(i)) < percentage * (img.getPixel(i) - imgE.getPixel(i))/100.0 ) {
                	imgOut2.setPixel(i, HIGH_VALUE);
                }    
                else {
                	imgOut2.setPixel(i, LOW_VALUE);
                }
			} 
			else {
				if ( (imgD.getPixel(i) - img.getPixel(i)) < percentage * (img.getPixel(i) - imgE.getPixel(i))/100.0 ) {
                	imgOut1.setPixel(i, HIGH_VALUE);
                	imgOut2.setPixel(i, HIGH_VALUE);
                }    
                else {
                	imgOut1.setPixel(i, LOW_VALUE);
                	imgOut2.setPixel(i, LOW_VALUE);
                }
			}
		}
		
	}
	
	public static GrayScaleImage[] doubleToggleMapping (GrayScaleImage img, AdjacencyRelation adj){
		GrayScaleImage imgOut1 = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		GrayScaleImage imgOut2 = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		
		int contrast1 = 50;
		int contrast2 = 16;	
		int percentage = 80;
		
		doubleToggleMapping(img, adj, contrast1, contrast2, percentage, imgOut1, imgOut2);
		
		/*
		WindowImages.show(imgOut1, "toggle1");
		WindowImages.show(Labeling.labeling(imgOut1, adj).randomColor(), "toggle (labeling1)");
		
		WindowImages.show(imgOut2, "toggle2");
		WindowImages.show(Labeling.labeling(imgOut2, adj).randomColor(), "toggle (labeling2)");
		*/
		return new GrayScaleImage[]{imgOut1, imgOut2};
		
	}
	

	public static void main(String args[]){
		GrayScaleImage img = ImageBuilder.openGrayImage();
		
		//doubleToggleMapping(img, AdjacencyRelation.getCircular(2.5));
		
		GrayScaleImage imgToggle = ToggleMapping.toggleMapping(img, AdjacencyRelation.getCircular(4));
		GrayScaleImage imgToggleLabeling = Labeling.labeling(imgToggle, AdjacencyRelation.getCircular(1.5));
		WindowImages.show(imgToggle, "toggle");
		WindowImages.show(imgToggleLabeling.randomColor(), "toggle (labeling)");
		
	}


} 
