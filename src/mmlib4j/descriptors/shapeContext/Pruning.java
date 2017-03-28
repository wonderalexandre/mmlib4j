package mmlib4j.descriptors.shapeContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Pruning {

	List<ImageShapeContext> images;
	ImageShapeContext img;
    int sampleQuery;
	int sampleImages;
    
    public Pruning(int sampleQuery, int sampleImages){
    	this.sampleQuery = sampleQuery;
    	this.sampleImages = sampleImages;
    }
    
    public void setImage(ImageShapeContext img){
    	this.img = img;
    }
    
    public void setImages(List<ImageShapeContext> images){
    	this.images = images;
    }

    public List<CostAndImage> run(int thresholding){
    	long ti = System.currentTimeMillis();
    	List<CostAndImage> list;
    	if(sampleQuery <= sampleImages)
    		list = run();
    	else
    		list = run2();
    	long tf = System.currentTimeMillis();
        System.out.println("Tempo: " + ((tf - ti)/1000) + "s");
    	return list.subList(0, thresholding);
    }
    
    
    /**
     * Poda: pouco pontos da query e muito ponto das imagens
     * @param count
     * @return
     */
    public List<CostAndImage> run(){
    	
        List<CostAndImage> distances = new ArrayList<CostAndImage>();
        double N;
        double d = 0;
        int num = sampleQuery;

        if (img == null || images.size() == 0)
            return distances;

        img.computeShapeContexts(num, Utility.median(img.getAllPoints()));
        List<ShapeContext> SCQuery = img.getShapeContexts();
        num = img.getAllPoints().size();

		List<ShapeContext> SCShape[] = new ArrayList[images.size()];
        double minValues[][] = new double[num][images.size()];
        

        for (int n = 0; n < images.size(); n++) {
            System.out.println("Imagem: " + n);
            images.get(n).computeShapeContexts(sampleImages, Utility.median(images.get(n).getAllPoints()));
            SCShape[n] = images.get(n).getShapeContexts();
        }

       // List<CostAndIndices> scs = new ArrayList<CostAndIndices>(images.size());

        // Para cada imagem
        for (int n = 0; n < images.size(); n++) {
            // Para cada SC da imagem de consulta
            for (int i = 0; i < SCQuery.size(); i++) {
                ShapeContext scQuery = SCQuery.get(i);
                //Para cada SC da imagem
                ShapeContext scShape = SCShape[n].get(0);
                double min = scQuery.distance(scShape);
                for (int j = 0; j < SCShape[n].size(); j++) {
                    scShape = SCShape[n].get(j); //ShapeContext scShape = SCShape[n].get(j);
                    double cost = scQuery.distance(scShape);
                    if (cost < min) {
                        min = cost;
                    }
                }
                minValues[i][n] = min;
            }
        }

        for (int n = 0; n < images.size(); n++) {
                for (int i = 0; i < num; i++) {
                    double sum = 0;
                    double value = 0;
                    for (int j = 0; j < images.size(); j++) {
                        double minValue = minValues[i][j];
                        if (j == n)
                            value = minValue;
                        sum = sum + minValue;
                    }

                    N = sum/images.size();

                    if (N > 0)
                        d = d + value/N;
                }

                d = d/num;
                distances.add(new CostAndImage(d,images.get(n)));
        }
        Collections.sort(distances);
        return distances;    	
    }
	  
    
    /**
     * Poda: Muito ponto da query e poucos pontos das imagens
     * @param count
     * @return
     */
    public List<CostAndImage> run2(){
    	
        List<CostAndImage> distances = new ArrayList<CostAndImage>();
        
        double d = 0;
        int num = sampleImages;

        if (img == null || images.size() == 0)
            return distances;

        img.computeShapeContexts(sampleQuery, Utility.median(img.getAllPoints()));
        List<ShapeContext> SCQuery = img.getShapeContexts();

		List<ShapeContext> SCShape[] = new ArrayList[images.size()];
        for (int n = 0; n < images.size(); n++) {
            images.get(n).computeShapeContexts(num, Utility.median(images.get(n).getAllPoints()));
            SCShape[n] = images.get(n).getShapeContexts();
        }

        double minValues[][] = new double[num][images.size()];
        // Para cada imagem
        for (int n = 0; n < images.size(); n++) {
            // Para cada SC da imagem de consulta
            for (int j = 0; j < SCShape[n].size(); j++) {
        		//Para cada SC da imagem
        		ShapeContext scShape = SCShape[n].get(j); 
                double min = Double.MAX_VALUE;
                for (int i = 0; i < SCQuery.size(); i++) {
                    ShapeContext scQuery = SCQuery.get(i);
                    double cost = scShape.distance(scQuery);
                    if (cost < min) {
                        min = cost;
                    }
                }
                minValues[j][n] = min;
        	}
        }
        double N;
        for (int n = 0; n < images.size(); n++) {
                for (int i = 0; i < num; i++) {
                    double sum = 0;
                    double value = 0;
                    for (int j = 0; j < images.size(); j++) {
                        double minValue = minValues[i][j];
                        if (j == n)
                            value = minValue;
                        sum = sum + minValue;
                    }

                    N = sum/images.size();

                    if (N > 0)
                        d = d + value/N;
                }

                d = d/num;
                distances.add(new CostAndImage(d,images.get(n)));
        }
        Collections.sort(distances);
        return distances;    	
    }
	
}

