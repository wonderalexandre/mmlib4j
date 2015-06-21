package mmlib4j.segmentation;

import java.util.ArrayList;
import java.util.Stack;

import mmlib4j.datastruct.Queue;
import mmlib4j.images.BinaryImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.Pixel;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class Labeling {

	public static GrayScaleImage labeling(BinaryImage img, AdjacencyRelation adj){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
		
		int label = 1;
		for(int p=0; p < img.getSize(); p++){
			if(imgOut.getPixel(p) != 0) continue;
			imgOut.setPixel(p, label);
			Queue<Integer> fifo = new Queue<Integer>();
			fifo.enqueue(p);
			while(!fifo.isEmpty()){
				int pixel = fifo.dequeue();
				for(Integer q: adj.getAdjacencyPixels(img, pixel)){
					if(imgOut.getPixel(q) == 0 && img.getPixel(p) == img.getPixel(q)){
						imgOut.setPixel(q, label);
						fifo.enqueue(q);
					}
				}
			}
			label++;
		}
		return imgOut;
	}

	
	public static BinaryImage[] getSetLabelings(BinaryImage img, AdjacencyRelation adj){
		int quantidadeCC = numberConnectedComponent(img, adj);
		if(quantidadeCC == 0) return null;
		BinaryImage imgs[] = new BinaryImage[quantidadeCC];
		boolean flags[] = new boolean[img.getSize()];
		int label = 0;
		for(int p=0; p < img.getSize(); p++){
			if(flags[p] || img.isPixelBackground(p)) continue;
			flags[p] = true;
			imgs[label] = ImageFactory.createBinaryImage(img.getWidth(), img.getHeight());
			imgs[label].setPixel(p, true);
			Queue<Integer> fifo = new Queue<Integer>();
			fifo.enqueue(p);
			while(!fifo.isEmpty()){
				int pixel = fifo.dequeue();
				for(Integer q: adj.getAdjacencyPixels(img, pixel)){
					if(!flags[q] && img.getPixel(p) == img.getPixel(q) && !img.isPixelBackground(p)){
						imgs[label].setPixel(q, true);
						flags[q] = true;
						fifo.enqueue(q);
					}
				}
			}
			label++;
		}
		return imgs;
	}
	
	public static int numberConnectedComponent(BinaryImage img, AdjacencyRelation adj){
		boolean flags[] = new boolean[img.getSize()];
		int label = 0;
		for(int p=0; p < img.getSize(); p++){
			if(flags[p] || img.isPixelBackground(p)) continue;
			flags[p] = true;
			Queue<Integer> fifo = new Queue<Integer>();
			fifo.enqueue(p);
			while(!fifo.isEmpty()){
				int pixel = fifo.dequeue();
				for(Integer q: adj.getAdjacencyPixels(img, pixel)){
					if(!flags[q] && img.getPixel(p) == img.getPixel(q) && !img.isPixelBackground(p)){
						flags[q] = true;
						fifo.enqueue(q);
					}
				}
			}
			label++;
		}
		return label;
	}
	
	public static int getNumFlatzone(GrayScaleImage img, AdjacencyRelation adj){
		int label = 0;
		boolean flags[] = new boolean[img.getSize()];
		for(int p=0; p < img.getSize(); p++){
			if(flags[p]) continue;
			flags[p] = true;
			Queue<Integer> fifo = new Queue<Integer>();
			fifo.enqueue(p);
			while(!fifo.isEmpty()){
				int pixel = fifo.dequeue();
				for(Integer q: adj.getAdjacencyPixels(img, pixel)){
					if(!flags[q] && img.getPixel(p) == img.getPixel(q)){
						flags[q] = true;
						fifo.enqueue(q);
					}
				}
			}
			label++;
		}
		return label;
	}
	
	/**
	 * Devolve um histograma de zonas planas => h[k] = quantidade de CC de nivel k
	 * @param img
	 * @param adj
	 * @return
	 */
	 public static int[] countFlatzone(GrayScaleImage img, AdjacencyRelation adj){
		 boolean flags[] = new boolean[img.getSize()];
		 int labels[] = new int[img.maxValue()+1];
		 for(int p=0; p < img.getSize(); p++){
				if(flags[p]) continue;
				flags[p] = true;
				Queue<Integer> fifo = new Queue<Integer>();
				fifo.enqueue(p);
				while(!fifo.isEmpty()){
					int pixel = fifo.dequeue();
					for(Integer q: adj.getAdjacencyPixels(img, pixel)){
						if(!flags[q] && img.getPixel(p) == img.getPixel(q)){
							flags[q] = true;
							fifo.enqueue(q);
						}
					}
				}
				labels[img.getPixel(p)]++;
			}
			return labels; 
	 }
	
	 public static GrayScaleImage markerCenter(GrayScaleImage img, AdjacencyRelation adj){
			GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
			GrayScaleImage imgOutC = ImageFactory.createGrayScaleImage(8, img.getWidth(), img.getHeight());
			int label = 1;
			for(int p=0; p < img.getSize(); p++){
				if(imgOut.getPixel(p) != 0) continue;
				imgOut.setPixel(p, label);
				Queue<Integer> fifo = new Queue<Integer>();
				fifo.enqueue(p);
				int xCenter = 0;
				int yCenter = 0;
				int count = 0;
				while(!fifo.isEmpty()){
					int pixel = fifo.dequeue();
					xCenter += pixel % img.getWidth();
					yCenter += pixel / img.getWidth();
					count++;
					for(Integer q: adj.getAdjacencyPixels(img, pixel)){
						if(imgOut.getPixel(q) == 0 && img.getPixel(p) == img.getPixel(q)){
							imgOut.setPixel(q, label);
							fifo.enqueue(q);
						}
					}
				}
				if(count > 10){
					xCenter = xCenter / count;
					yCenter = yCenter / count;
					imgOutC.setPixel(xCenter, yCenter, 255);
				}
				label++;
			}
			return imgOutC;
		}
	
	public static GrayScaleImage labeling(GrayScaleImage img, AdjacencyRelation adj){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_32BITS, img.getWidth(), img.getHeight());
		
		int label = 1;
		for(int p=0; p < img.getSize(); p++){
			if(imgOut.getPixel(p) != 0) continue;
			imgOut.setPixel(p, label);
			Queue<Integer> fifo = new Queue<Integer>();
			fifo.enqueue(p);
			while(!fifo.isEmpty()){
				int pixel = fifo.dequeue();
				for(Integer q: adj.getAdjacencyPixels(img, pixel)){
					if(imgOut.getPixel(q) == 0 && img.getPixel(p) == img.getPixel(q)){
						imgOut.setPixel(q, label);
						fifo.enqueue(q);
					}
				}
			}
			label++;
		}
		return imgOut;
	}
	
	
	public static GrayScaleImage labeling(GrayScaleImage img, AdjacencyRelation adj, int k){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
		
		int label = 1;
		for(int p=0; p < img.getSize(); p++){
			if(imgOut.getPixel(p) != 0) continue;
			imgOut.setPixel(p, label);
			Queue<Integer> fifo = new Queue<Integer>();
			fifo.enqueue(p);
			while(!fifo.isEmpty()){
				int pixel = fifo.dequeue();
				for(Integer q: adj.getAdjacencyPixels(img, pixel)){
					if(imgOut.getPixel(q) == 0 && Math.abs( img.getPixel(p) - img.getPixel(q) ) <= k){
						imgOut.setPixel(q, label);
						fifo.enqueue(q);
					}
				}
			}
			label++;
		}
		return imgOut;
	}
	
	

    /**
     * Retorna uma lista com as altura das CCs
     */
    public static ArrayList<Integer> getHeights(BinaryImage imgIn){
    	BinaryImage img = imgIn.duplicate();
    	ArrayList<Integer> list = new ArrayList<Integer>();
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(32, img.getWidth(), img.getHeight());
        
        int labeling = 0;
        for(int w=0; w < img.getWidth(); w++)
            for(int h=0; h < img.getHeight(); h++){
            	//System.out.println("a = "+ img.getPixel(w, h));
                if(img.getPixel(w, h) ){
                	labeling++;
                    Stack<Pixel> q = new Stack<Pixel>();
                    q.push(new Pixel(w,h));
                    int max = 0;
                    int min = img.getHeight();
                    while (!q.isEmpty()) {
                    	Pixel p = q.pop();
                        if (img.isPixelValid(p.x, p.y) && img.getPixel(p.x, p.y) ) {
                        	
                            imgOut.setPixel(p.x,p.y, labeling);
                            img.setPixel(p.x,p.y, false);
                            
                            q.push(new Pixel(p.x-1,p.y-1));
                            q.push(new Pixel(p.x,p.y-1));
                            q.push(new Pixel(p.x-1,p.y));
                            q.push(new Pixel(p.x+1,p.y+1));
                            q.push(new Pixel(p.x,p.y+1));
                            q.push(new Pixel(p.x+1,p.y));
                            q.push(new Pixel(p.x+1,p.y-1));
                            q.push(new Pixel(p.x-1,p.y+1));
                            
                            if(p.y > max){
                            	max  = p.y;
                            }
                            if(p.y < min){
                            	min = p.y;
                            }
                        }
                    }
                    list.add(max - min + 1);
                }
            }
        
        return list;
    	
    }
	
}
