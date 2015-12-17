package mmlib4j.representation.tree.attribute;

import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.RealImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerDistanceTransform {

	RealImage imgsDT[];
	GrayScaleImage img;
	ThreadPoolExecutor pool;
	
	public ComputerDistanceTransform(int numNode, NodeLevelSets root, GrayScaleImage img){
		long ti = System.currentTimeMillis();
		imgsDT = new RealImage[256];
		this.img = img;
		pool =  new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		computerDT(root);
		while(pool.getActiveCount() != 0);
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [computer distance transform]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}
	
	public float distanceTransform(NodeLevelSets node, int p){
		return imgsDT[node.getLevel()].getPixel(p);
	}
	
	public RealImage getDistanceTransform(NodeLevelSets node){
		return imgsDT[node.getLevel()];
	}
	
	public RealImage getDistanceTransform(int level){
		return imgsDT[level];
	}

	public void computerDT(NodeLevelSets root){
		List<NodeLevelSets> children = root.getChildren();
		for(NodeLevelSets son: children){
			computerDT(son);
		}
		if(imgsDT[root.getLevel()] == null){
			imgsDT[root.getLevel()] = ImageFactory.createRealImage(img.getWidth(), img.getHeight());
			pool.execute(new ThreadNode(root, img, imgsDT[root.getLevel()]));
		}
	}
	

	class ThreadNode extends Thread {
		int level;
		boolean isMaxtree;
		GrayScaleImage img;
		RealImage imgDT;
		public ThreadNode(NodeLevelSets node, GrayScaleImage img, RealImage imgDT){
			this.level = node.getLevel();
			this.isMaxtree = node.isNodeMaxtree();
			this.img = img;
			this.imgDT = imgDT;
		}
			
		public void run() {
			for(int p=0; p < img.getSize(); p++){
				if ( (isMaxtree && img.getPixel(p) >= level) || (!isMaxtree && img.getPixel(p) <= level) ) // this is a foreground pixel
					imgDT.setPixel(p, Float.POSITIVE_INFINITY); // zero distance to foregorund
				else
					imgDT.setPixel(p, 0);	
			}
			
			distanceTransformFloat(img.getWidth(), img.getHeight(), imgDT.getPixels(), 1, (float) Math.sqrt(2));
		}
		

		public void distanceTransformFloat(int w, int h, float dpix[], float k1, float k2){
			
			float d1, d2, d3, d4, dmin;
			//L->R pass:
			for (int v = 0; v < h; v++) {
				for (int u = 0; u < w; u++) {
					int i = v * w + u;
					if (dpix[i]>0) { //not a foreground pixel
						//compute distances via neighboring pixels
						d1 = Float.POSITIVE_INFINITY;
						d2 = Float.POSITIVE_INFINITY;
						d3 = Float.POSITIVE_INFINITY;
						d4 = Float.POSITIVE_INFINITY;
						
						if (u>0) 			d1 = k1 + dpix[v*w+u-1];
						if (u>0 && v>0) 	d2 = k2 + dpix[(v-1)*w+u-1];
						if (v>0)			d3 = k1 + dpix[(v-1)*w+u];
						if (v>0 && u<w-1)	d4 = k2 + dpix[(v-1)*w+u+1];
						
						dmin = dpix[i];
						if (d1<dmin) dmin = d1;
						if (d2<dmin) dmin = d2;
						if (d3<dmin) dmin = d3;
						if (d4<dmin) dmin = d4;
						dpix[i] = dmin;
					}
				}
			}
			
			//R->L pass:
			for (int v = h - 1; v >= 0; v--) {
				for (int u = w - 1; u >= 0; u--) {
					int i = v * w + u;
					if (dpix[i] > 0) { //not a foreground pixel
						
						//compute distances via neighboring pixels
						d1 = Float.POSITIVE_INFINITY;
						d2 = Float.POSITIVE_INFINITY;
						d3 = Float.POSITIVE_INFINITY;
						d4 = Float.POSITIVE_INFINITY;
						
						if (u<w-1) 			d1 = k1 + dpix[v*w+u+1];
						if (u<w-1 && v<h-1)	d2 = k2 + dpix[(v+1)*w+u+1];
						if (v<h-1)			d3 = k1 + dpix[(v+1)*w+u];
						if (v<h-1 && u>0)	d4 = k2 + dpix[(v+1)*w+u-1];
						
						dmin = dpix[i];
						if (d1<dmin) dmin = d1;
						if (d2<dmin) dmin = d2;
						if (d3<dmin) dmin = d3;
						if (d4<dmin) dmin = d4;
						dpix[i] = dmin;
					}
				}
			}
			
		}
	}
	
	
}
