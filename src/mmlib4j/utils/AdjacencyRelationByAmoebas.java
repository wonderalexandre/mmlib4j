package mmlib4j.utils;

import java.util.Iterator;

import mmlib4j.datastruct.Queue;
import mmlib4j.gui.WindowImages;
import mmlib4j.images.BinaryImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class AdjacencyRelationByAmoebas {
	
	int px[];
	int py[];
	BinaryImage filterDomain;
	private int distance[][] = null;
	
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
		adj.filterDomain = ImageFactory.createBinaryImage(width, height);
		adj.filterDomain.initImage(true);
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
		adj.filterDomain = ImageFactory.createBinaryImage(r0*2+1, r0*2+1);
		
		i = 0;
		for (dy = -r0; dy <= r0; dy++) {
			for (dx = -r0; dx <= r0; dx++) {
				if (((dx * dx) + (dy * dy)) <= r2) {
					adj.px[i] =dx;
					adj.py[i] =dy;
					if ((dx == 0) && (dy == 0))
						i0 = i;
					i++;
					adj.filterDomain.setPixel(dx + adj.filterDomain.getWidth()/2, dy + adj.filterDomain.getHeight()/2, true);
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
	
	
	public void printDistance() {
		System.out.println("Matriz de distancias");
		for(int j=0; j < filterDomain.getHeight(); j++) {
			for(int i=0; i < filterDomain.getWidth(); i++) {
				System.out.print(distance[i][j] + "\t");
			}
			System.out.println("");
		}
	}

	public Iterable<Integer> getAdjacencyPixels(final GrayScaleImage img, final int threshod, final int x, final int y) {
		
		
		return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private int i = 0;
					int origemX, origemY;
					
					//construtor
					{
						distance = new int[filterDomain.getWidth()][filterDomain.getHeight()];
						boolean flags[][] = new boolean[filterDomain.getWidth()][filterDomain.getHeight()];
						origemX = filterDomain.getWidth()/2;
						origemY = filterDomain.getHeight()/2;
						
						for(int i=0; i < filterDomain.getWidth(); i++) { 
							for(int j=0; j < filterDomain.getHeight(); j++) {
								distance[i][j] = Integer.MAX_VALUE;
							}
						}
						distance[origemX][origemY] = 0;
						
						Queue<Pixel> queue = new Queue<Pixel>();
						queue.enqueue(new Pixel(origemX, origemY));
						while(!queue.isEmpty()) {
							Pixel p = queue.dequeue();
							flags[p.x][p.y] = true;
							for(Pixel q: getAdj(p)) {
								if(filterDomain.isPixelValid(q.x, q.y) && filterDomain.isPixelForeground(q.x, q.y) && flags[q.x][q.y] == false) {
									Pixel qTranslate = q.getTranslate(x-p.x, y-p.y);
									if(img.isPixelValid(qTranslate.x, qTranslate.y)) {
										queue.enqueue(q);
										int dist = 1 + distance[p.x][p.y] + amoebaDist(p.getTranslate(x-p.x, y-p.y), qTranslate);
										distance[q.x][q.y] = dist;//Math.min(distance[q.x][q.y], dist);
									}
								}
							}
							
						}
						
						//filterDomain is the amoeba
						for(int i=0; i < filterDomain.getWidth(); i++) {
							for(int j=0; j < filterDomain.getHeight(); j++) {
								if(distance[i][j] <= threshod) {
									filterDomain.setPixel(i,  j, true);
								}
								else {
									filterDomain.setPixel(i,  j, false);
								}
							}
						}
						
					}
					
					
					
					private Pixel[] getAdj(Pixel p) {
						return new Pixel[] {p.getTranslate(-1, 0), p.getTranslate(1, 0), p.getTranslate(0, -1), p.getTranslate(0, 1)};
					}
					
					private int amoebaDist(Pixel p, Pixel q) {
						return Math.abs( img.getPixel(p.x, p.y) - img.getPixel(q.x, q.y) );
					}
					
					public boolean hasNext() {
						while(i < px.length){
							if(img.isPixelValid(px[i] + x, py[i] + y)){
									if(filterDomain.getPixel(px[i]+origemX, py[i]+origemY)) {
										return true;
									}
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
	
	public static void main2(String args[]) {
		
		int width = 8;
		int height = 6;
		int pixels[] = {
				10,  9, 8, 7, 1, 1 , 4, 1,
				10,  9, 8, 7, 1, 1 , 4, 1,
				10,  9, 8, 7, 1, 1 , 4, 1,
				10,  9, 8, 7, 1, 1 , 4, 1,
				10,  9, 8, 7, 1, 0 , 4, 1,
				10,  9, 8, 7, 1, 0 , 4, 1
		};
		
		GrayScaleImage img = ImageFactory.createReferenceGrayScaleImage(32, pixels, width, height);
		
		for(int y=0; y < img.getHeight(); y++) {
			for(int x=0; x < img.getWidth(); x++) {
				System.out.print(img.getPixel(x, y) + "\t");
			}
			System.out.println();
		}
		System.out.println();
		
		AdjacencyRelationByAmoebas amoeba = AdjacencyRelationByAmoebas.getBox(5, 5);
		boolean A[][] = new boolean[5][5]; 
		
		int x=5, y=5;
		int limiar = 11;
		amoeba.getAdjacencyPixels(img, limiar, x, y).iterator();
		amoeba.printDistance();
		System.out.println("\n");
		System.out.println();
		for(int p: amoeba.getAdjacencyPixels(img, limiar, x, y)) {
			int px = p % img.getWidth() - x + 2;
			int py = p / img.getWidth() - y + 2;
			A[px][py] = true;
			pixels[p] = 0;
			
		}
		System.out.println();
		for(y=0; y < img.getHeight(); y++) {	
			for( x=0; x < img.getWidth(); x++) 	{
			
				System.out.print(img.getPixel(x, y) + "\t");
			}
			System.out.println();
		}
		System.out.println();	
		
		System.out.println();
		for(int i=0; i < A[0].length; i++) {
			for(int j=0; j < A.length; j++) {
				if(A[j][i])
					System.out.print(1 + "\t");
				else
					System.out.print(0 + "\t");
			}
			System.out.println("");
		}
		
		
	}
	
	public static void main(String args[]){
		AdjacencyRelationByAmoebas adjAmoeba = AdjacencyRelationByAmoebas.getCircular(9);
		AdjacencyRelation adj = AdjacencyRelation.getCircular(9);
		
		
		GrayScaleImage img = ImageBuilder.openGrayImage();

		GrayScaleImage imgNoise = ImageUtils.getGaussianNoise(img, 20);
		
		GrayScaleImage imgMedia = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());
		GrayScaleImage imgMediaAboema = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());

				
		for(int p=0; p < imgNoise.getSize(); p++){
			int cont = 0;
			int soma = 0; 
			for(int q: adj.getAdjacencyPixels(imgNoise, p)){
				soma += imgNoise.getPixel(q);
				cont++;
			}
			imgMedia.setPixel(p, soma / cont);
			
			cont = 0;
			soma = 0; 
			int limiar = 1000000;
			for(int q: adjAmoeba.getAdjacencyPixels(imgNoise, limiar, p)){
				soma += imgNoise.getPixel(q);
				cont++;
			}
			imgMediaAboema.setPixel(p, soma / cont);
		}
		
		
		//mse
		int somaAmeba = 0;
		int somaSE = 0;
		for(int p=0; p < img.getSize(); p++){
			somaSE += Math.abs(img.getPixel(p) - imgMedia.getPixel(p));
			somaAmeba += Math.abs(img.getPixel(p) - imgMediaAboema.getPixel(p));
		}
		System.out.println("MAE media (se) - " + (somaSE/(double) img.getSize()));
		System.out.println("MAE media (adaptativa) - " + somaAmeba/(double) img.getSize());
		
		WindowImages.show(img, "Image original sem ruido");
		WindowImages.show(imgNoise, "Image original com ruido");
		WindowImages.show(imgMedia, "Filtro de media classico");
		WindowImages.show(imgMediaAboema, "Filtro de media adaptativo");
		
	}
		
	
}
