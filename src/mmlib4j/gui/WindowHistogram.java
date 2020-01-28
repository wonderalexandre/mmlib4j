package mmlib4j.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import mmlib4j.filtering.Histogram;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.ImageBuilder;

/**
 * 
 * @author wonder
 * Classe utilizado para exibir o histograma da imagem na tela
 */
public class WindowHistogram {
    static final int WIN_WIDTH = 300;
    static final int WIN_HEIGHT = 210;
    static final int HIST_WIDTH = 256;
    static final int HIST_HEIGHT = 128;
    static final int BAR_HEIGHT = 12;
    static final int XMARGIN = 20;
    static final int YMARGIN = 10;
    
    /**
     * Desenha o histograma
     * @param img
     * @return
     */
    public ColorImage drawHistogram(GrayScaleImage img){
        ColorImage imgOut = ImageFactory.instance.createColorImage(WIN_WIDTH, WIN_HEIGHT);
        imgOut.initImage(Color.WHITE.getRGB());
        Histogram histogram = new Histogram(img);
        int h[] = histogram.getHistogram();
        
        int maxCount = histogram.getMaxPeak();
        
        drawPlot(imgOut, maxCount, h);
        int x = XMARGIN + 1;
        int y = YMARGIN + HIST_HEIGHT + 2;
        drawUnscaledColorBar(imgOut, x-1, y, 256, BAR_HEIGHT);
        y += BAR_HEIGHT+2;
        drawText(imgOut, histogram, img, x, y);
        return imgOut;
    }
    
    /**
     * Desenha o texto no histograma
     * @param imgOut
     * @param histogram
     * @param x
     * @param y
     */
    private void drawText(ColorImage imgOut, Histogram histogram, GrayScaleImage img, int x, int y) {
        NumberFormat formatter = new DecimalFormat("0.00");
        
        ImageGraphics.drawString(imgOut, "0", new Font(null, Font.PLAIN, 10), Color.BLACK, x, y);
        ImageGraphics.drawString(imgOut, "255", new Font(null, Font.PLAIN, 10), Color.BLACK, x+241, y);
        y += 20;
        ImageGraphics.drawString(imgOut, "Mean: " + formatter.format(img.meanValue()), new Font(null, Font.PLAIN, 10), Color.BLACK, x, y);
        ImageGraphics.drawString(imgOut, "Value Min: " + img.minValue(), new Font(null, Font.PLAIN, 10), Color.BLACK, x + 160, y);
        y+=12;
        ImageGraphics.drawString(imgOut, "Standard Deviatio: " + formatter.format(histogram.getStandardDeviatio()), new Font(null, Font.PLAIN, 10), Color.BLACK, x, y);
        
        ImageGraphics.drawString(imgOut, "Value Max: " + img.maxValue(), new Font(null, Font.PLAIN, 10), Color.BLACK, x + 160, y);
        y+=12;
        ImageGraphics.drawString(imgOut, "Peak Max: " + histogram.getMaxPeak(), new Font(null, Font.PLAIN, 10), Color.BLACK, x, y);
        ImageGraphics.drawString(imgOut, "Peak Mean: " + formatter.format(histogram.getMeanPeak()), new Font(null, Font.PLAIN, 10), Color.BLACK, x + 160, y);
    
    }
    
    /**
     * Desenhar os picos do histograma
     * @param img
     * @param maxCount
     * @param h
     */
    private void drawPlot(ColorImage img, int maxCount, int h[]) {
        if (maxCount==0) maxCount = 1;
       
        ImageGraphics.drawRectangle(img, new Point(XMARGIN, YMARGIN), HIST_WIDTH+2, HIST_HEIGHT+1, Color.BLACK);
        int  y;
        for (int i = 0; i<HIST_WIDTH; i++) {
            y = (int)((double)HIST_HEIGHT*h[i])/maxCount;
            if (y>HIST_HEIGHT)
                y = HIST_HEIGHT;
            ImageGraphics.drawLine(img, new Point(i+XMARGIN, YMARGIN+HIST_HEIGHT), new Point(i+XMARGIN, YMARGIN+HIST_HEIGHT-y), Color.BLACK);
        }
    }
    
    /**
     * Desenha a barra de escala de niveis de cinza
     * @param img
     * @param x
     * @param y
     * @param width
     * @param height
     */
    private void drawUnscaledColorBar(ColorImage img, int x, int y, int width, int height) {
        for (int i = 0; i < 256; i++) {
           ImageGraphics.drawLine(img, new Point(x + i, y + 0), new Point(x + i, y + height), new Color(i,i,i));
        }
        ImageGraphics.drawRectangle(img, new Point(x, y), width+2, height, Color.BLACK);
    }
    
    
    public static void show(GrayScaleImage img){
        WindowHistogram win = new WindowHistogram();
        WindowImages.show(win.drawHistogram(img), "Histogram");
    }
    
    public static ColorImage getGraphic(GrayScaleImage img){
    	return new WindowHistogram().drawHistogram(img);
    }
    
    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        GrayScaleImage img = ImageBuilder.openGrayImage();
        WindowImages.show(img);
        WindowHistogram.show(img);

    }

}
