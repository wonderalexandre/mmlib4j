package mmlib4j.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.ImageBuilder;

public class ImageGraphics {

    public static int lineWidth = 1;
   
    
    /**
     * Desenha uma reta na imagem
     * @param img
     * @param p1
     * @param p2
     * @param c
     * @return
     */
    public static ColorImage drawLine(GrayScaleImage img, Point p1, Point p2, Color c){
    	ColorImage imgOut = ImageFactory.createColorImage(img.getWidth(), img.getHeight());
        imgOut.addSubImage(img, 0, 0);
        drawLine(imgOut, p1, p2, c);  
        return imgOut;
    }
    
    /**
     * Desenha uma reta na imagem
     * @param imgOut
     * @param p1
     * @param p2
     * @param c
     */
    public static void drawLine(ColorImage imgOut, Point p1, Point p2, Color c){
        
        int dx = p2.x - p1.x;
        int dy = p2.y - p1.y;
        int n = Math.max(Math.abs(dx), Math.abs(dy));
        double xinc = (double) dx / n;
        double yinc = (double) dy / n;
        double x = p1.x < 0? p1.x - 0.5 : p1.x + 0.5;
        double y = p1.y < 0? p1.y - 0.5 : p1.y + 0.5;
        n++;

        do {
            if (lineWidth==1)
                drawPixel(imgOut, (int)x, (int)y, c);
            else if (lineWidth==2)
                drawDot2(imgOut, (int)x, (int)y, c);
            else
                drawDot(imgOut, (int)x, (int)y, c);
            x += xinc;
            y += yinc;
        } while (--n>0);
    }
    
    /**
     * Plota um pixel
     * @param img
     * @param x
     * @param y
     * @param c
     */
    public static void drawPixel(ColorImage img, int x, int y, Color c){
        if(img.isPixelValid(x, y))
            img.setPixel(x, y, c.getRGB());
    }
    
    /**
     * Plota um ponto (4-conexidade)
     * @param img
     * @param x
     * @param y
     * @param c
     */
    public static void drawDot(ColorImage img, int x, int y, Color c){
        drawPixel(img, x, y, c);
        drawPixel(img, x-1, y, c);
        drawPixel(img, x, y-1, c);
        drawPixel(img, x-1, y-1, c);
    }
    
    public static void drawDot2(ColorImage img, int x, int y, Color c){
        drawPixel(img, x, y, c);
        drawPixel(img, x, y-1, c);
        drawPixel(img, x, y+1, c);
        
        drawPixel(img, x-1, y, c);
        drawPixel(img, x-1, y-1, c);
        drawPixel(img, x-1, y+1, c);

        drawPixel(img, x+1, y, c);
        drawPixel(img, x+1, y-1, c);
        drawPixel(img, x+1, y+1, c);
    }
    
    
    
    /**
     * Desenha um retangulo
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public static void drawRectangle(ColorImage img, Point p1, int width, int height, Color c) {
        if (width<1 || height<1)
            return;
        
        drawLine(img, p1, new Point(p1.x + width-1, p1.y), c);
        drawLine(img, new Point(p1.x + width-1, p1.y), new Point(p1.x + width-1, p1.y + height-1), c);
        drawLine(img, p1, new Point(p1.x, p1.y + height-1), c);
        drawLine(img, new Point(p1.x, p1.y + height-1), new Point(p1.x + width-1, p1.y + height-1), c);
    }
    
    /**
     * Desenha um retangulo
     * @param img
     * @param p1
     * @param p2
     * @param c
     */
    public static void drawRectangle(ColorImage img, Point p1, Point p2, Color c) {
        drawLine(img, p1, new Point(p1.x, p2.y), c);
        drawLine(img, p1, new Point(p2.x, p1.y), c);
        drawLine(img, p2, new Point(p1.x, p2.y), c);
        drawLine(img, p2, new Point(p2.x, p1.y), c);
    }

    /**
     * Desenha um retangulo
     * @param img
     * @param p1
     * @param p2
     * @param c
     */
    public static void drawRectangle(ColorImage img, Point p1, Point p2, Point p3, Point p4, Color c) {
        drawLine(img, p1, p2, c);
        drawLine(img, p1, p4, c);
        drawLine(img, p2, p3, c);
        drawLine(img, p3, p4, c);
    }

    
    /**
     * Desenha um retangulo
     * @param img
     * @param p1
     * @param width
     * @param height
     * @param c
     * @return
     */
    public static ColorImage drawRectangle(GrayScaleImage img, Point p1, int width, int height, Color c){
    	ColorImage imgOut = ImageFactory.createColorImage(img.getWidth(), img.getHeight());
        imgOut.addSubImage(img, 0, 0);
        drawRectangle(imgOut, p1, width, height, c);  
        return imgOut;
    }
    
    /**
     * Desenha um retangula
     * @param img
     * @param p1
     * @param p2
     * @param c
     * @return
     */
    public static ColorImage drawRectangle(GrayScaleImage img, Point p1, Point p2, Color c){
    	ColorImage imgOut = ImageFactory.createColorImage(img.getWidth(), img.getHeight());
        imgOut.addSubImage(img, 0, 0);
        drawRectangle(imgOut, p1, p2, c);  
        return imgOut;
    }
    
    public static int getStringWidth(String s, FontMetrics fontMetrics, Graphics g) {
        java.awt.geom.Rectangle2D r = fontMetrics.getStringBounds(s, g);
        return (int)r.getWidth();
    }


    /**
     * Desenha um texto em uma imagem
     * @param img
     * @param s
     * @param font
     * @param c
     * @param x
     * @param y
     */
    public static void drawOval(ColorImage img, String s, Font font, Color c, int x, int y) {  
        
        BufferedImage bi = ImageBuilder.convertToImage(img);
        
        Graphics2D g2 =  bi.createGraphics();  
        FontMetrics metrics = g2.getFontMetrics(font);
        int alpha = 0;
        if(metrics != null)
            alpha = metrics.getHeight() - metrics.getDescent();
        g2.drawImage(bi,0, 0, null);
        g2.setFont(font);
        g2.setColor(c);
        g2.drawString(s, x, y + alpha);
        
        int rgb, r, g, b;
        for(int w=0;w<img.getWidth();w++){
            for(int h=0;h<img.getHeight();h++){
                rgb = bi.getRGB(w,h);
                r = (int)((rgb&0x00FF0000)>>>16); // Red level
                g = (int)((rgb&0x0000FF00)>>>8); // Green level
                b = (int) (rgb&0x000000FF); // Blue level
                img.setPixel(w, h, new Color(r, g, b).getRGB()); //convertendo para niveis de cinza
            }
        }
                
    }  
    
    
    
    /**
     * Desenha um texto em uma imagem
     * @param img
     * @param s
     * @param font
     * @param c
     * @param x
     * @param y
     */
    public static void drawString(ColorImage img, String s, Font font, Color c, int x, int y) {  
    	BufferedImage bi = ImageBuilder.convertToImage(img);    
        
        Graphics2D g2 =  bi.createGraphics();  
        FontMetrics metrics = g2.getFontMetrics(font);
        int alpha = 0;
        if(metrics != null)
            alpha = metrics.getHeight() - metrics.getDescent();
        g2.drawImage(bi,0, 0, null);
        g2.setFont(font);
        g2.setColor(c);
        g2.drawString(s, x, y + alpha);
        
        int rgb, r, g, b;
        for(int w=0;w<img.getWidth();w++){
            for(int h=0;h<img.getHeight();h++){
                rgb = bi.getRGB(w,h);
                r = (int)((rgb&0x00FF0000)>>>16); // Red level
                g = (int)((rgb&0x0000FF00)>>>8); // Green level
                b = (int) (rgb&0x000000FF); // Blue level
                img.setPixel(w, h, new Color(r, g, b).getRGB()); //convertendo para niveis de cinza
            }
        }
                
    }  
    
    /**
     * Desenha um texto em uma imagem
     * @param img
     * @param s
     * @param font
     * @param c
     * @param x
     * @param y
     * @return
     */
    public static ColorImage drawString(GrayScaleImage img, String s, Font font, Color c, int x, int y) {
    	ColorImage imgOut = ImageFactory.createColorImage(img.getWidth(), img.getHeight());
        imgOut.addSubImage(img, 0, 0);
        drawString(imgOut, s, font, c, x, y);
        return imgOut;
    }
    
    
}
