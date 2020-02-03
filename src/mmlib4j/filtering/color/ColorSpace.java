package mmlib4j.filtering.color;

import mmlib4j.images.RealImage;
import mmlib4j.images.impl.AbstractImageFactory;
import mmlib4j.images.impl.RGBImage;

public class ColorSpace {

    //RWC:  white reference values and most RGB-XYZ matrices are from www.brucelindbloom.com.
    private static float[]    Lwhite = {95.047f, 100.0f, 108.883f};   // L* white reference
    
    public static RealImage[] getColorSpaceXYZ(RGBImage img){
        float red, green, blue;
        
        RealImage c1 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c2 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c3 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());

        float[][] matrix = new float[][] {{0.4124564f, 0.3575761f, 0.1804375f},
        								  {0.2126729f, 0.7151522f, 0.0721750f},
        								  {0.0193339f, 0.1191920f, 0.9503041f}};

        float X,Y,Z;
        for (int q = 0; q < img.getSize(); q++){
        	red = img.getRed(q) / 255f;    //R 0..1
        	green = img.getGreen(q) / 255f;     //G 0..1
        	blue =  img.getBlue(q) / 255f;         //B 0..1

        	//http://www.easyrgb.com/math.html    	
        	red = (red > 0.04045f)? (float) Math.pow((red +0.055)/1.055, 2.4) : red/12.92f;
        	green = (green > 0.04045f)? (float) Math.pow((green +0.055)/1.055, 2.4) : green/12.92f;
        	blue = (blue > 0.04045f)? (float) Math.pow((blue +0.055)/1.055, 2.4) : blue/12.92f;
      	  	
      	  	X = matrix[0][0]*red + matrix[0][1]*green + matrix[0][2]*blue;
      	  	Y = matrix[1][0]*red + matrix[1][1]*green + matrix[1][2]*blue;
      	  	Z = matrix[2][0]*red + matrix[2][1]*green + matrix[2][2]*blue;

      	  	c1.setPixel(q, X*100);
      	  	c2.setPixel(q, Y*100);
      	  	c3.setPixel(q, Z*100);
        }
        
        return new RealImage[]{c1, c2, c3};
    	
    }
    
    /**
     * http://www.easyrgb.com/math.html
     * and Ronnier Luo, The Colour Image Processing Handbook, Colour science, Springer, 27-65, 1998
     * @param img
     * @return
     */
    public static RealImage[] getColorSpaceLAB(RGBImage img){
    	RealImage xyz[] = getColorSpaceXYZ(img); 

    	RealImage c1 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c2 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c3 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());

        float l, a, b;
    	float fX, fY, fZ;
        float X=0, Y=0, Z=0;

        for(int q=0; q < img.getSize(); q++){
        	// RWC white reference
            X = xyz[0].getPixel(q) / Lwhite[0];
            Y = xyz[1].getPixel(q) / Lwhite[1];
            Z = xyz[2].getPixel(q) / Lwhite[2];

            if ( X > 0.008856f )
                fX = (new Double(Math.exp(Math.log(X)/3f))).floatValue();
            else
                fX = ((7.787f * X) + (16f/116f)); 

            if ( Y > 0.008856f )
            	fY = (new Double(Math.exp(Math.log(Y)/3f))).floatValue(); 
            else
            	fY = ((7.787f * Y) + (16f/116f));

            if ( Z > 0.008856f )
            	fZ =  (new Double(Math.exp(Math.log(Z)/3f))).floatValue(); 
            else
            	fZ = ((7.787f * Z) + (16f/116f)); 

            l = ( 116f * fY ) - 16f;
            a = 500f * ( fX - fY );
            b = 200f * ( fY - fZ );
            
            c1.setPixel(q, l);
      	  	c2.setPixel(q, a);
      	  	c3.setPixel(q, b);
        } 
        return new RealImage[]{c1, c2, c3};
    }   
    
    /**
     * Ref:
     * @ARTICLE{ChengHD00:art,
     *     author = {H.D. Cheng and X.H. Jiang and Y. Sun and J.L. Wang},
     *     title = {Color Image Segmentation: Advances and Prospects},
     *     journal = {Pattern Recognition},
     *     year = {2000},
     *     volume = {34},
     *     pages = {2259-2281},
     *     month = {September},
     *   }
     * @param img
     * @return
     */
    public static RealImage[] getColorSpaceYUV(RGBImage img){
    	RealImage c1 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c2 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c3 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());

        float Y, U, V;
        float red, green, blue;
    	for(int q=0; q < img.getSize(); q++){
    		red = img.getRed(q) / 255f;    //R 0..1
        	green = img.getGreen(q) / 255f;     //G 0..1
        	blue =  img.getBlue(q) / 255f;         //B 0..1
    		
            Y =  0.299f * red + 0.587f * green + 0.114f * blue;
            U = -0.147f * red - 0.289f * green + 0.437f * blue;
            V =  0.615f * red - 0.515f * green - 0.100f * blue;
            
            c1.setPixel(q, Y);
      	  	c2.setPixel(q, U);
      	  	c3.setPixel(q, V); 
        }        
    	
    	return new RealImage[]{c1, c2, c3};
    }
    

    // RWC; see www.equasys.de/colorconversion.html.
    // This is the matrix for full-range (0-255) YCbCr colors
    public static RealImage[] getColorSpaceYCbCr(RGBImage img){
    
    	RealImage c1 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c2 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c3 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());

        float Y, Cb, Cr;
    	for(int q=0; q < img.getSize(); q++){

    		Y =  0.299f * img.getRed(q) + 0.587f * img.getGreen(q) + 0.114f * img.getBlue(q);
    		Cb = -0.169f * img.getRed(q) - 0.331f * img.getGreen(q) + 0.500f *img.getBlue(q) + 128;
    		Cr =  0.500f * img.getRed(q) - 0.419f * img.getGreen(q) - 0.081f * img.getBlue(q) + 128;
              
    		c1.setPixel(q, Y);
    		c2.setPixel(q, Cb);
    		c3.setPixel(q, Cr); 
    	}        
    	return new RealImage[]{c1, c2, c3};     
    }
    

    /** 
      @ARTICLE{ChengHD00:art,
      author = {H.D. Cheng and X.H. Jiang and Y. Sun and J.L. Wang},
      title = {Color Image Segmentation: Advances and Prospects},
      journal = {Pattern Recognition},
      year = {2000},
      volume = {34},
      pages = {2259-2281},
      month = {September},
    }
    */
    public static RealImage[] getColorSpaceYIQ(RGBImage img){
        
    	RealImage c1 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c2 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c3 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());

        float Y, I, Q;
        float red, green, blue;
    	for(int q=0; q < img.getSize(); q++){
    		red = img.getRed(q) / 255f;    //R 0..1
        	green = img.getGreen(q) / 255f;     //G 0..1
        	blue =  img.getBlue(q) / 255f;         //B 0..1
    	
            Y = 0.299f * red + 0.587f * green + 0.114f * blue;
            I = 0.596f * red - 0.274f * green - 0.322f * blue;
            Q = 0.211f * red - 0.253f * green - 0.312f * blue;
            
            c1.setPixel(q, Y);
    		c2.setPixel(q, I);
    		c3.setPixel(q, Q); 
    	}        
    	return new RealImage[]{c1, c2, c3}; 
    }
    
    /**
     * http://www.easyrgb.com/math.html  AND
	     @INBOOK{RonnierLuoM98colour:chapterbook,
	      chapter = {Colour science},
	      pages = {27-65},
	      title = {The Colour Image Processing Handbook},
	      publisher = {Springer},
	      year = {1998},
	      editor = {R. E.N. Horne},
	      author = {Ronnier Luo},
	    }
     * @param img
     * @return
     */
    public static RealImage[] getColorSpaceLuv(RGBImage img){
    
    	RealImage xyz[] = getColorSpaceXYZ(img); 

    	RealImage c1 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c2 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c3 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());

        float x=0, y=0, z=0;
        x = Lwhite[0];
        y = Lwhite[1];
        z = Lwhite[2];

        float yn = 1f;

        /** un' corresponding to Yn */
        float unp = (4*x) /(x + 15*y + 3*z);

        /** vn' corresponding to Yn */
        float vnp = (9*y) /(x + 15*y + 3*z);
        
        float X=0, Y=0, Z=0;
        for(int q=0; q<img.getSize(); q++){

            X = xyz[0].getPixel(q);
            Y = xyz[1].getPixel(q);
            Z = xyz[2].getPixel(q);
            
            // As yn = 1.0, we will just consider Y value as yyn
            //yyn = (Y/yn);
            float f_yyn = Y / 100f;

            if (f_yyn > 0.008856f) {
            	f_yyn=new Double(Math.exp(Math.log(f_yyn)/3f)).floatValue();
            } else {
            	f_yyn = ((7.787f * f_yyn) + (16f/116f));
            }

            float up = (X == 0f && Y == 0f && Z == 0f)? 0f : (4f*X / ((X + 15f*Y + 3f*Z)));
            float vp = (X == 0f && Y == 0f && Z == 0f)? 0f : (9f*Y / ((X + 15f*Y + 3f*Z)));

            float l = (116f *f_yyn)-16f;
            float u = 13f  * l * (up - unp);
            float v = 13f  * l * (vp - vnp);
            
            c1.setPixel(q, l);
    		c2.setPixel(q, u);
    		c3.setPixel(q, v); 
    	}        
    	return new RealImage[]{c1, c2, c3};         
    }
    
    /**
     @BOOK{MalacaraD01:book,
          title = {Color Vision and Colorimetry, Theory and Applications},
          publisher = {SPIE International Society for Optical Engineering},
          year = {2001},
          author = {D. Malacara},
          address = {Bellingham, Washington USA},
        }
     * @param img
     * @return
     */
    public static RealImage[] getColorSpaceHSI(RGBImage img){
    	RealImage c1 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c2 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c3 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());

        float H, S, I;
        float red, green, blue;
    	for(int q=0; q < img.getSize(); q++){
    		red = img.getRed(q) / 255f;    //R 0..1
        	green = img.getGreen(q) / 255f;     //G 0..1
        	blue =  img.getBlue(q) / 255f;         //B 0..1

        	float var_Min = Math.min(red, green); //Min. value of RGB
            var_Min = Math.min(var_Min, blue);   
            float var_Max = Math.max(red, green); //Max. value of RGB
            var_Max = Math.max(var_Max, blue);
            float del_Max = var_Max - var_Min;      //Delta RGB value

            I =  (red + green + blue)/3f;         

            if ( del_Max == 0f ){               //This is a gray, no chroma...
                H =  0f;                    //HSL results = 0 ? 1
                S =  0f;           
            }
            else{                               //Chromatic data...                                   
               S = 1 - (var_Min / I);
               H = 0;
               float del_R = ( ( ( var_Max - red ) / 6f ) + ( del_Max / 2f ) ) / del_Max;
               float del_G = ( ( ( var_Max - green ) / 6f ) + ( del_Max / 2f ) ) / del_Max;
               float del_B = ( ( ( var_Max - blue ) / 6f ) + ( del_Max / 2f ) ) / del_Max;

               if      ( red == var_Max ) H = del_B - del_G;
               else if ( green == var_Max ) H = ( 1f / 3f ) + del_R - del_B;
               else if ( blue == var_Max ) H = ( 2f / 3f ) + del_G - del_R;

               if ( H < 0 )  H += 1;
               if ( H > 1 )  H -= 1;
            }
        
            c1.setPixel(q, H);
    		c2.setPixel(q, S);
    		c3.setPixel(q, I); 
    	}        
    	
    	return new RealImage[]{c1, c2, c3};        
    }     

    /**
     * http://www.easyrgb.com/math.html
     */
    public static RealImage[] getColorSpaceHSL(RGBImage img){
    	RealImage c1 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c2 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c3 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());

        float H, S, L;
        float red, green, blue;
    	for(int q=0; q < img.getSize(); q++){
    		red = img.getRed(q) / 255f;    //R 0..1
        	green = img.getGreen(q) / 255f;     //G 0..1
        	blue =  img.getBlue(q) / 255f;         //B 0..1

            H=0; S=0; L=0;
            float var_Min = Math.min(red, green); //Min. value of RGB
            var_Min = Math.min(var_Min, blue);   
            float var_Max = Math.max(red, green); //Max. value of RGB
            var_Max = Math.max(var_Max, blue);
            float del_Max = var_Max - var_Min;      //Delta RGB value

            L =  (var_Max + var_Min)/2;         

            if ( del_Max == 0f){                //This is a gray, no chroma...
                H =  0f;                        //HSL results = 0 ? 1
                S =  0f;           
            }
            else{                               //Chromatic data...                                              
               if ( L < 0.5f )  
            	   S = del_Max / ( var_Max + var_Min );
               else 
            	   S = del_Max / ( 2f - var_Max - var_Min );           

               float del_R = ( ( ( var_Max - red ) / 6f ) + ( del_Max / 2f ) ) / del_Max;
               float del_G = ( ( ( var_Max - green ) / 6f ) + ( del_Max / 2f ) ) / del_Max;
               float del_B = ( ( ( var_Max - blue ) / 6f ) + ( del_Max / 2f ) ) / del_Max;

               if      ( red == var_Max ) H = del_B - del_G;
               else if ( green == var_Max ) H = ( 1f / 3f ) + del_R - del_B;
               else if ( blue == var_Max ) H = ( 2f / 3f ) + del_G - del_R;

               if ( H < 0f )  H += 1f;
               if ( H > 1f )  H -= 1f;
            }
            c1.setPixel(q, H);
    		c2.setPixel(q, S);
    		c3.setPixel(q, L); 
    	}        
    	
    	return new RealImage[]{c1, c2, c3}; 
    }
    
    /**
     * HSV/HSB (HSV colour space is also known as HSB where B means brightness)
     *  http://www.easyrgb.com/math.html and http://www.easyrgb.com/
     *  
     * @param img
     * @return
     */
    public static RealImage[] getColorSpaceHSV(RGBImage img){
    	RealImage c1 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c2 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c3 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());

        float H, S, V;
        float red, green, blue;
    	for(int q=0; q < img.getSize(); q++){
    		red = img.getRed(q) / 255f;    //R 0..1
        	green = img.getGreen(q) / 255f;     //G 0..1
        	blue =  img.getBlue(q) / 255f;         //B 0..1
        	H=0; S=0; V=0;
        	
            float var_Min = Math.min(red, green); //Min. value of RGB
            var_Min = Math.min(var_Min, blue);   
            float var_Max = Math.max(red, green); //Max. value of RGB
            var_Max = Math.max(var_Max, blue);
            float del_Max = var_Max - var_Min;      //Delta RGB value

            V =  var_Max*1f;
            if ( del_Max == 0 ){                    //This is a gray, no chroma...      
                H =  0f;                            //HSV results = 0 ? 1
                S =  0f;           
            }
            else{                                   //Chromatic data...                                   
               S = del_Max / var_Max;
               float del_R = ( ( ( var_Max - red ) / 6f ) + ( del_Max / 2f ) ) / del_Max;
               float del_G = ( ( ( var_Max - green ) / 6f ) + ( del_Max / 2f ) ) / del_Max;
               float del_B = ( ( ( var_Max - blue ) / 6f ) + ( del_Max / 2f ) ) / del_Max;

               if      ( red == var_Max ) H = del_B - del_G;
               else if ( green == var_Max ) H = ( 1f / 3f ) + del_R - del_B;
               else if ( blue == var_Max ) H = ( 2f / 3f ) + del_G - del_R;

               if ( H < 0 )  H += 1;
               if ( H > 1 )  H -= 1;
            }            
            c1.setPixel(q, H);
    		c2.setPixel(q, S);
    		c3.setPixel(q, V); 
    	}        
    	
    	return new RealImage[]{c1, c2, c3}; 

  }
    
    /**
     * This is an idealized transformation using full gray component replacement.
     * Because CMYK is subtractive, invert the image to see the appearance of the plates. 
     */
    public static RealImage[] getColorSpaceCMYK(RGBImage img){
    	RealImage c1 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c2 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c3 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());
        RealImage c4 = AbstractImageFactory.instance.createRealImage(img.getWidth(), img.getHeight());

        float C, M, Y, K;
        float red, green, blue;
    	for(int q=0; q < img.getSize(); q++){
    		red = img.getRed(q) / 255f;    //R 0..1
        	green = img.getGreen(q) / 255f;     //G 0..1
        	blue =  img.getBlue(q) / 255f;         //B 0..1
        	
        	K = 1 - Math.max(Math.max(red, green), blue);
            C = 0;
            M = 0;
            Y = 0;

            if (K != 1) {
            	C = (1-red-K) / (1-K);
                M = (1-green-K) / (1-K);
                Y = (1-blue-K) / (1-K); 
            }

            c1.setPixel(q, C * 100);
    		c2.setPixel(q, M * 100);
    		c3.setPixel(q, Y * 100);
    		c3.setPixel(q, K * 100); 
    	}        
    	
    	return new RealImage[]{c1, c2, c3, c4};        
      }


}
