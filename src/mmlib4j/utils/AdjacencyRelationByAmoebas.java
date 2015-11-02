package mmlib4j.utils;

import java.util.Iterator;

import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.Image2D;
import mmlib4j.images.impl.ImageFactory;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class AdjacencyRelationByAmoebas {
	
	int px[];
	int py[];
	
	protected AdjacencyRelationByAmoebas(int n) {
		px = new int[n];
		py = new int[n];
	}
	
	
	/**
	 * Adjacencia retangular
	 * @param width
	 * @param height
	 * @return
	 */
	public static AdjacencyRelationByAmoebas getBox(int width, int height){
		AdjacencyRelationByAmoebas adj;
		int i,dx,dy;

		if (width%2 == 0) width++;
		if (height%2 == 0) height++;

		adj = new AdjacencyRelationByAmoebas(width*height);
		i=0;
		for(dy=-height/2; dy<=height/2; dy++){
			for(dx=-width/2; dx<=width/2; dx++){
					adj.px[i] = dx;
					adj.py[i] = dy;
					i++;
			}
		}
		
		return(adj);
	}


	
	/**
	 * Adjacencia circular com a origem no centro do disco
	 * @param raio
	 * @return
	 */
	public static AdjacencyRelationByAmoebas getCircular(double raio) {
		
		int i, j, k, n, dx, dy, r0, r2, i0 = 0;
		n = 0;
		r0 = (int) raio;
		r2 = (int) (raio * raio);
		for (dy = -r0; dy <= r0; dy++)
			for (dx = -r0; dx <= r0; dx++)
				if (((dx * dx) + (dy * dy)) <= r2)
					n++;

		AdjacencyRelationByAmoebas adj = new AdjacencyRelationByAmoebas(n);
		
		i = 0;
		for (dy = -r0; dy <= r0; dy++) {
			for (dx = -r0; dx <= r0; dx++) {
				if (((dx * dx) + (dy * dy)) <= r2) {
					adj.px[i] =dx;
					adj.py[i] =dy;
					if ((dx == 0) && (dy == 0))
						i0 = i;
					i++;
				}
			}
		}
		
		double aux;
		double da[] = new double[n];
		double dr[] = new double[n];

		/* Set clockwise */
		for (i = 0; i < n; i++) {
			dx = adj.px[i];
			dy = adj.py[i];
			dr[i] = Math.sqrt((dx * dx) + (dy * dy));
			if (i != i0) {
				da[i] = (Math.atan2(-dy, -dx) * 180.0 / Math.PI);
				if (da[i] < 0.0)
					da[i] += 360.0;
			}
		}
		da[i0] = 0.0;
		dr[i0] = 0.0;

		/* place central pixel at first */
		aux = da[i0];
		da[i0] = da[0];
		da[0] = aux;

		aux = dr[i0];
		dr[i0] = dr[0];
		dr[0] = aux;

		int auxX, auxY;
		auxX = adj.px[i0];
		auxY = adj.py[i0];
		adj.px[i0] = adj.px[0];
		adj.py[i0] = adj.py[0];
		
		adj.px[0] = auxX;
		adj.py[0] = auxY;
		

		/* sort by angle */
		for (i = 1; i < n - 1; i++) {
			k = i;
			for (j = i + 1; j < n; j++)
				if (da[j] < da[k]) {
					k = j;
				}
			aux = da[i];
			da[i] = da[k];
			da[k] = aux;
			aux = dr[i];
			dr[i] = dr[k];
			dr[k] = aux;

			auxX = adj.px[i];
			auxY = adj.py[i];
			adj.px[i] = adj.px[k];
			adj.py[i] = adj.py[k];
			
			adj.px[k] = auxX;
			adj.py[k] = auxY;
		}

		/* sort by radius for each angle */
		for (i = 1; i < n - 1; i++) {
			k = i;
			for (j = i + 1; j < n; j++)
				if ((dr[j] < dr[k]) && (da[j] == da[k])) {
					k = j;
				}
			aux = dr[i];
			dr[i] = dr[k];
			dr[k] = aux;

			auxX = adj.px[i];
			auxY = adj.py[i];
			adj.px[i] = adj.px[k];
			adj.py[i] = adj.py[k];
			
			adj.px[k] = auxX;
			adj.py[k] = auxY;
			
		}
		
		return (adj);
	}
	

	public Iterable<Integer> getAdjacencyPixels(final GrayScaleImage img, final int threshod, final int x, final int y) {
        
		return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private int i = 0;
					public boolean hasNext() {
						while(i < px.length){
							if(img.isPixelValid(px[i] + x, py[i] + y)){
								double amoebaDist = Math.abs( img.getPixel(x, y) - img.getPixel(px[i] + x, py[i] + y)); 
								if(amoebaDist <= threshod)
									return true;
							}
							i++;
						} 
						return false;
					}
					public Integer next() {
						int pixel = (px[i] + x) + (py[i] + y) * img.getWidth();
						i += 1;
						return pixel;
					}
					public void remove() { }
					
				};
			}
		};
    }
	
	public Iterable<Integer> getAdjacencyPixels(final GrayScaleImage img, int threshold, final int i) {
    	return getAdjacencyPixels(img, threshold, i % img.getWidth(), i / img.getWidth());
    }
	
		
	public static void main(String args[]){
		AdjacencyRelationByAmoebas adjAmoeba = AdjacencyRelationByAmoebas.getCircular(10);
		AdjacencyRelation adj = AdjacencyRelation.getCircular(10);
		
		
		GrayScaleImage imgOriginal = ImageBuilder.openGrayImage();
		GrayScaleImage img = ImageBuilder.openGrayImage();
		GrayScaleImage imgMedia = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		GrayScaleImage imgMediaAboema = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		
		for(int p=0; p < img.getSize(); p++){
			int cont = 0;
			int soma = 0; 
			for(int q: adj.getAdjacencyPixels(img, p)){
				soma += img.getPixel(q);
				cont++;
			}
			imgMedia.setPixel(p, soma / cont);
			
			cont = 0;
			soma = 0; 
			for(int q: adjAmoeba.getAdjacencyPixels(img, 50, p)){
				soma += img.getPixel(q);
				cont++;
			}
			imgMediaAboema.setPixel(p, soma / cont);
		}
		
		
		//mse
		int somaAmeba = 0;
		int somaSE = 0;
		for(int p=0; p < img.getSize(); p++){
			somaSE += Math.abs(imgOriginal.getPixel(p) - imgMedia.getPixel(p));
			somaAmeba += Math.abs(imgOriginal.getPixel(p) - imgMediaAboema.getPixel(p));
		}
		System.out.println("MSE media (se) - " + (somaSE/(double) img.getSize()));
		System.out.println("MSE media (adaptativa) - " + somaAmeba/(double) img.getSize());
		
		WindowImages.show(new Image2D[]{imgOriginal, imgMedia, imgMediaAboema});
		
	}
	
	

	

	
	
}
