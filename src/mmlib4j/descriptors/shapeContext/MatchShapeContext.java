package mmlib4j.descriptors.shapeContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.utils.ImageBuilder;

public class MatchShapeContext {
	
	public static final int SAMPLE_SIZE = 180;
	
	public static void main(String args[]){
		ImageShapeContext img1 = new ImageShapeContext( ImageBuilder.openGrayImage(ImageBuilder.windowOpenFile()) );
		 File dir[] = ImageBuilder.windowOpenDir();
		 List<ImageShapeContext> imgs = new ArrayList<ImageShapeContext>();
		 for(File f: dir){
			 if(f.getName().endsWith("png"))
				 imgs.add( new ImageShapeContext( ImageBuilder.openGrayImage(f)) );
		 }
		 
		 match(img1, imgs);
	}
	
	public static void match(ImageShapeContext img, List<ImageShapeContext> imgs){
		long ti = System.currentTimeMillis();
		
		Pruning pruning = new Pruning(SAMPLE_SIZE, 20);
		pruning.setImage(img);
		pruning.setImages(imgs);
		List<CostAndImage> distances = pruning.run(70);
		
		for(int i = 0; i < distances.size(); i++) {
			System.out.println("Imagem: " + i + "\t Distancia = " + distances.get(i).cost);
		}

		int num = distances.size();
		
		List<CostAndImage> v = new ArrayList<CostAndImage>();
		
		img.computeShapeContexts(SAMPLE_SIZE, Utility.median(img.getAllPoints()));
		double optimalCost;
		for(int i = 0; i < num; i++) {
			optimalCost = match(img, distances.get(i).img);
			v.add( new CostAndImage(optimalCost, distances.get(i).img));
			
		}
		Collections.sort(v);
		
		GrayScaleImage imgResult[] = new GrayScaleImage[num];
		String labels[] = new String[num];
		for (int i = 0; i < num; i++) {
			imgResult[i] = v.get(i).img.getImage();
			labels[i] = "" + v.get(i).cost;
		}
		
		long tf = System.currentTimeMillis();
		System.out.println("Tempo: " + ((tf - ti)/1000) + "s");
		WindowImages.show(imgResult, labels);
	}
	
	
	public static double match(ImageShapeContext img1, ImageShapeContext img2){
	
		 int i, j;		 
		 
		 int n1 = SAMPLE_SIZE;
		 int n2 = SAMPLE_SIZE;
		 
		 if (n1 > img1.getAllPoints().size()){ 
			 n1 = img1.getAllPoints().size();
		 }
		 
		 if (n2 > img2.getAllPoints().size() ){
			 n2 = img2.getAllPoints().size();
		 }
		 
		 if(n1 != n2){
			 n1 = n2 = Math.min(n1, n2);
			 img1.computeShapeContexts(n1, Utility.median(img1.getAllPoints()));	 
		 }
		 img2.computeShapeContexts(n2, Utility.median(img2.getAllPoints()));
		 List<ShapeContext> SC1 = img1.getShapeContexts();
		 List<ShapeContext> SC2 = img2.getShapeContexts();

		 /*
		 List<Point> pts1 = new ArrayList<Point>();
		 List<Point> pts2 = new ArrayList<Point>();
		 for (i = 0; i < SC1.size(); i++)
			 pts1.add(SC1.get(i).getPoint());

		 for (i = 0; i < SC2.size(); i++)
			 pts2.add(SC2.get(i).getPoint());
		  */

		 float Cost[][] = new float[SC1.size()][SC2.size()];
		 for (i = 0; i < SC1.size(); i++) {
			 ShapeContext sc1 = SC1.get(i);
			 for (j = 0; j < SC2.size(); j++) {
				 ShapeContext sc2 = SC2.get(j);
				 Cost[i][j] = (float) sc1.distance(sc2);
			 }
		 }
		 
		 int results[][] = HungarianAlgorithm.solve(Cost);
		 
		 double optimalCost = 0;
		 for (i = 0; i < results.length; i++){
			// System.out.println("Trabalhador: " + results[i][0]+ " Tarefa: " + results[i][1] + "  Custo: " + Cost[ results[i][0] ][ results[i][1] ]);
			
			 if(results[i] != null)
				 optimalCost += Cost[ results[i][0] ][ results[i][1] ];
			 
		 }
		 System.out.println("Custo total: " + optimalCost);
		 return optimalCost;
	}
}
