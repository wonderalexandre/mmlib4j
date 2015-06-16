package mmlib4j.representation.tree.attribute;

import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import mmlib4j.filtering.binary.DistanceTransforms;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.RealImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.INodeTree;
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
	
	public ComputerDistanceTransform(int numNode, INodeTree root, GrayScaleImage img){
		long ti = System.currentTimeMillis();
		imgsDT = new RealImage[256];
		this.img = img;
		pool =  new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		computerAttribute(root);
		while(pool.getActiveCount() != 0);
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [computer distance transform]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}
	
	public float distanceTransform(INodeTree node, int p){
		return imgsDT[node.getLevel()].getPixel(p);
	}
	

	public void computerAttribute(INodeTree root){
		List<INodeTree> children = root.getChildren();
		for(INodeTree son: children){
			computerAttribute(son);
		}
		if(imgsDT[root.getLevel()] == null){
			imgsDT[root.getLevel()] = ImageFactory.createFloatImage(img.getWidth(), img.getHeight());
			pool.execute(new ThreadNode(root, img, imgsDT[root.getLevel()]));
		}
	}
	

	class ThreadNode extends Thread {
		int level;
		boolean isMaxtree;
		GrayScaleImage img;
		RealImage imgDT;
		public ThreadNode(INodeTree node, GrayScaleImage img, RealImage imgDT){
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
			DistanceTransforms dt = new DistanceTransforms();
			dt.distanceTransformFloat(img.getWidth(), img.getHeight(), imgDT.getPixels(), 1, (float) Math.sqrt(2));
		}
	}
	
	
}
