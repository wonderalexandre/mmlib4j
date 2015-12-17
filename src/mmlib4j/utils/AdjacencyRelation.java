package mmlib4j.utils;

import java.util.Iterator;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.Image2D;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class AdjacencyRelation {
	int px[];
	int py[];
	
	int origemX = Integer.MIN_VALUE;
	int origemY = Integer.MIN_VALUE;
	
	protected static AdjacencyRelation adj4 = null;
	protected static AdjacencyRelation adj8 = null;
	
	public int getSize(){
		return px.length;
	}
	
	protected AdjacencyRelation(int n) {
		px = new int[n];
		py = new int[n];
	}
	
	
	
	public AdjacencyRelation(int px[], int py[]) {
		this.px = px;
		this.py = py;
	}

	public static void main(String args[]){
		AdjacencyRelation adj = AdjacencyRelation.getCircular(2).leftSide();
		
		
		
		adj.print();
		
		AdjacencyRelation.getBox(3, 3).rightSide().print();
		
	}
	
	public int getX(int index){
		return px[index];
	}

	public int getY(int index){
		return py[index];
	}
	
	public int getIndexOrigemX(){
		if(origemX == Integer.MIN_VALUE){
			for(int p: px){
				if(p ==0)
					origemX = p;
			}
		}
		return origemX;
	}
	
	public int getIndexOrigemY(){
		if(origemY == Integer.MIN_VALUE){
			for(int p: py){
				if(p ==0)
					origemY = p;
			}
		}
		return origemY;
	}
	
	public void print(){
		String si;
		String sj;
		System.out.println("Ordem da lista:");
		for(int i=0; i < px.length; i++){
			si = (px[i] >=0? "+" + px[i]:String.valueOf(px[i]));
			sj = (py[i] >=0? "+" + py[i]:String.valueOf(py[i]));
			System.out.print("(" + si + ", " + sj + ") ");
		
		}
		System.out.println("\n\nVisualizacao em 2D");
		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		for(int p: px){
			if(minX > p)
				minX = p;
			if(maxX < p)
				maxX = p;
		}
		
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		for(int p: py){
			if(minY > p)
				minY = p;
			if(maxY < p)
				maxY = p;
		}
		
		int mat[][] = new int[(maxX - minX + 1) * 2][(maxY - minY + 1) * 2];
		int cx = mat.length / 2;
		int cy = mat[0].length / 2;
		
		for(int i=0; i< px.length; i++){
			mat[px[i] + cx][py[i] + cy] = 1;
		}
		
		for(int j=minY; j<= maxY; j++){
			for(int i=minX; i<= maxX; i++){		
				if(mat[i + cx][ j + cy] == 1){
					si = (i >=0? "+" + i:String.valueOf(i));
					sj = (j >=0? "+" + j:String.valueOf(j));
					System.out.print("(" + si + ", " + sj + ") ");
				}else{
					System.out.print("         ");
				}
			}
			System.out.println();
		}
		
	}
	
	
	public int[][] getEE(){
		String si;
		String sj;
		
		System.out.println("Ordem da lista:");
		for(int i=0; i < px.length; i++){
			si = (px[i] >=0? "+" + px[i]:String.valueOf(px[i]));
			sj = (py[i] >=0? "+" + py[i]:String.valueOf(py[i]));
			System.out.print("(" + si + ", " + sj + ") ");
		
		}
		System.out.println("\n\nVisualizacao em 2D");
		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		for(int p: px){
			if(minX > p)
				minX = p;
			if(maxX < p)
				maxX = p;
		}
		
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		for(int p: py){
			if(minY > p)
				minY = p;
			if(maxY < p)
				maxY = p;
		}
		
		int mat[][] = new int[(maxX - minX + 1) * 2][(maxY - minY + 1) * 2];
		int cx = mat.length / 2;
		int cy = mat[0].length / 2;
		
		for(int i=0; i< px.length; i++){
			mat[px[i] + cx][py[i] + cy] = 1;
		}
		
		for(int j=minY; j<= maxY; j++){
			for(int i=minX; i<= maxX; i++){		
				if(mat[i + cx][ j + cy] == 1){
					si = (i >=0? "+" + i:String.valueOf(i));
					sj = (j >=0? "+" + j:String.valueOf(j));
					System.out.print("1 & ");
				}else{
					System.out.print("0 & ");
				}
			}
			System.out.println();
		}
		return  mat;
	}
	
    /**
     * Conjunto de pixels vizinhos de 8
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public static Iterable<Integer> getAdjacency8(int i, int width, int height) {
    	if(adj8 == null)
    		adj8 = AdjacencyRelation.getCircular(1.5);
    	return adj8.getAdjacencyPixels(width, height, i);
    }
    
    public static Iterable<Integer> getAdjacency4(int i, int width, int height) {
    	if(adj4 == null)
    		adj4 = AdjacencyRelation.getCircular(1);
    	return adj4.getAdjacencyPixels(width, height, i);
    }
    

    public static AdjacencyRelation getAdjacency8(){
    	if(adj8 == null)
    		adj8 = AdjacencyRelation.getCircular(1.5);
    	return adj8;
    }
    
    public static AdjacencyRelation getAdjacency4(){
    	if(adj4 == null)
    		adj4 = AdjacencyRelation.getCircular(1);
    	return adj4;
    }
	
	public Iterable<Integer> getAdjacencyPixels(final Image2D img, final int x, final int y) {
        return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private int i = 0;
					public boolean hasNext() {
						while(i < px.length){
							if(img.isPixelValid(px[i] + x, py[i] + y))
								return true;
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
	
	public Iterable<Integer> getAdjacencyPixels(final Image2D img, final int i) {
    	return getAdjacencyPixels(img, i % img.getWidth(), i / img.getWidth());
    }
	
	public Iterable<Integer> getAdjacencyPixels(final int imgWidth, final int imgHeight, final int i) {
    	final int x = i % imgWidth;
    	final int y = i / imgWidth;
       
        return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private int i = 0;
					public boolean hasNext() {
						while(i < px.length){
							int xx = px[i] + x;
							int yy = py[i] + y;
							if(xx >= 0 && xx < imgWidth && yy >= 0 && yy < imgHeight)
								return true;
							i++;
						} 
						return false;
					}
					public Integer next() {
						int pixel = (px[i] + x) + (py[i] + y) * imgWidth;
						i += 1;
						return pixel;
					}
					public void remove() { }
					
				};
			}
		};
        
    }
	

	public Iterable<Integer> getAdjacencyPixelsWithoutValidation(final Image2D img, final int i) {
    	return getAdjacencyPixelsWithoutValidation(img, i % img.getWidth(), i / img.getWidth());
    }
	
	public Iterable<Integer> getAdjacencyPixelsWithoutValidation(final Image2D img, final int x, final int y) {
        return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private int i = 0;
					public boolean hasNext() {
						if(i < px.length)
							return true;
						else 
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
	
	
	public int[] getVectorX(){
		return px;
	}
	
	public int[] getVectorY(){
		return py;
	}
	

	
	public AdjacencyRelation CloneAdjRel(){
		AdjacencyRelation adj = new AdjacencyRelation(px.length);
		int i;  
		for(i=0; i < this.px.length; i++){
			adj.px[i] = this.px[i];
			adj.py[i] = this.py[i];
		}
		return adj;
	}
	


	public AdjacencyRelation rightSide(){
		int cont=0;
		for (int i=0; i < this.px.length; i++){
			if( (this.py[i]>0) || (this.px[i] >= 0 && this.py[i] ==0)){
				cont++;
			}
		}
		AdjacencyRelation adj = new AdjacencyRelation(cont);
		int index=0;
		for (int i=0; i < this.px.length; i++){
			if( (this.py[i]>0) || (this.px[i] >= 0 && this.py[i] ==0)){
				adj.px[index] = this.px[i];
				adj.py[index] = this.py[i];
				index++;
			}
		}
		return adj;
	}
	

	public AdjacencyRelation leftSide(){
		int cont=0;
		for (int i=0; i < this.px.length; i++){
			if( (this.py[i]<0) || (this.px[i] <= 0 && this.py[i] ==0)){
				cont++;
			}
		}
		AdjacencyRelation adj = new AdjacencyRelation(cont);
		int index=0;
		for (int i=0; i < this.px.length; i++){
			if( (this.py[i]<0) || (this.px[i] <= 0 && this.py[i] ==0)){
				adj.px[index] = this.px[i];
				adj.py[index] = this.py[i];
				index++;
			}
		}
		return adj;
	}


	public static SimpleLinkedList<AdjacencyRelation> getFamilyHorizontal(int begin, int end, int step){
		SimpleLinkedList<AdjacencyRelation> list = new SimpleLinkedList<AdjacencyRelation>();
		while(begin <= end){
			list.add(getHorizontal(begin));
			begin += step;
		}
		return list;
	}
	
	/**
	 * adjacencia linha horizontal
	 * @param r
	 * @return
	 */
	public static AdjacencyRelation getHorizontal(int r){
		AdjacencyRelation adj;
		int i,n,dx;

		n= 2 * r + 1;
		adj = new AdjacencyRelation(n);
		i=1;
		for(dx=-r; dx<=r; dx++){
			if(dx != 0){
				adj.px[i] = dx;
				adj.py[i] = 0;
				i++;
			}
		}
		/* place the central pixel at first */
		adj.px[0] = 0;
		adj.py[0] = 0;
		return(adj);
	}

	
	public static SimpleLinkedList<AdjacencyRelation> getFamilyBox(int beginW, int endW, int beginH, int endH, int stepW, int stepH){
		SimpleLinkedList<AdjacencyRelation> list = new SimpleLinkedList<AdjacencyRelation>();
		boolean flag = true;
		while(flag){
			list.add(getBox(beginW, beginH));
			if(!(beginW < endW) && !(beginH < endH))
				flag = false;
			if(beginW < endW)
				beginW += stepW;
			if(beginH < endH)
				beginH += stepH;
		}
		return list;
	}
	
	/**
	 * Adjacencia retangular
	 * @param width
	 * @param height
	 * @return
	 */
	public static AdjacencyRelation getBox(int width, int height){
		AdjacencyRelation adj;
		int i,dx,dy;

		if (width%2 == 0) width++;
		if (height%2 == 0) height++;

		adj = new AdjacencyRelation(width*height);
		i=0;
		for(dy=-height/2; dy<=height/2; dy++){
			for(dx=-width/2; dx<=width/2; dx++){
				//if ((dx != 0)||(dy != 0)){
					adj.px[i] = dx;
					adj.py[i] = dy;
					i++;
				//}
			}
		}
		/* place the central pixel at first */
		//adj.px[0] = 0;
		//adj.py[0] = 0;
		return(adj);
	}

	public static AdjacencyRelation getLineRight(int len){
    	AdjacencyRelation adj = new AdjacencyRelation(len);
		int i=0;
		for(int dx=0; dx<len; dx++){
			adj.px[i] = dx;
			adj.py[i] = 0;
			i++;
		}
		return(adj);
    }
	public static AdjacencyRelation getLineLeft(int len){
    	AdjacencyRelation adj = new AdjacencyRelation(len);
		int i=0;
		for(int dx=0; dx<len; dx++){
			adj.px[i] = -dx;
			adj.py[i] = 0;
			i++;
		}
		return(adj);
    }

	
	public static SimpleLinkedList<AdjacencyRelation> getFamilyCross(int beginW, int endW, int beginH, int endH, int stepW, int stepH){
		SimpleLinkedList<AdjacencyRelation> list = new SimpleLinkedList<AdjacencyRelation>();
		boolean flag = true;
		while(flag){
			list.add(getCross(beginW, beginH));
			if(!(beginW < endW) && !(beginH < endH))
				flag = false;
			if(beginW < endW)
				beginW += stepW;
			if(beginH < endH)
				beginH += stepH;
			
			
		}
		return list;
	}
	
	/**
	 * Adjacencia em forma de cruz
	 * @param width
	 * @param height
	 * @return
	 */
	public static AdjacencyRelation getCross(int width, int height){
		AdjacencyRelation adj;
		int i,dx,dy;

		if (width%2 == 0) width++;
		if (height%2 == 0) height++;

  
		adj = new AdjacencyRelation(width+height-1);
		i=1;
		for(dx=-width/2,dy=0;dx<=width/2;dx++){
			if (dx != 0){
				adj.px[i] = dx;
				adj.py[i] = dy;
				i++;
			}
		}
  
		for(dy=-height/2,dx=0;dy<=height/2;dy++){
			if (dy != 0){
				adj.px[i] = dx;
				adj.py[i] = dy;
				i++;
			}
		}
  

		/* place the central pixel at first */
		adj.px[0] = 0;
		adj.py[0] = 0;
		return(adj);	
	}

	public static SimpleLinkedList<AdjacencyRelation> getFamilyVertical(int begin, int end, int step){
		SimpleLinkedList<AdjacencyRelation> list = new SimpleLinkedList<AdjacencyRelation>();
		while(begin <= end){
			list.add(getVertical(begin));
			begin += step;
		}
		return list;
	}
	
	/**
	 * Adjacencia linha vertical
	 * @param r
	 * @return
	 */
	public static AdjacencyRelation getVertical(int r){
		AdjacencyRelation adj;
		int i,n,dy;

		n=2*r+1;
		adj = new AdjacencyRelation(n);
		i=1;
		for(dy=-r;dy<=r;dy++){
			if(dy!=0){//if (i != r){
				adj.px[i] = 0;
				adj.py[i] = dy;
				i++;
			}
		}
		/* place the central pixel at first */
		adj.px[0] = 0;
		adj.py[0] = 0;
		return(adj);
	}

	
	public static AdjacencyRelation getFastCircular(double raio) {

		int i, n, dx, dy, r0, r2, auxX, auxY, i0 = 0;
		n = 0;
		r0 = (int) raio;
		r2 = (int) (raio * raio);
		for (dy = -r0; dy <= r0; dy++) {
			for (dx = -r0; dx <= r0; dx++) {
				if (((dx * dx) + (dy * dy)) <= r2) {
					n++;
				}
			}
		}
		AdjacencyRelation adj = new AdjacencyRelation(n);

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

		/* place central pixel at first */
		auxX = adj.px[i0];
		auxY = adj.py[i0];
		adj.px[i0] = adj.px[0];
		adj.py[i0] = adj.py[0];
		
		adj.px[0] = auxX;
		adj.py[0] = auxY;
		
		return (adj);
	}
	
	public static SimpleLinkedList<AdjacencyRelation> getFamilyCircular(double begin, double end, double step){
		SimpleLinkedList<AdjacencyRelation> list = new SimpleLinkedList<AdjacencyRelation>();
		while(begin <= end){
			list.add(getCircular(begin));
			begin += step;
		}
		return list;
	}

	/**
	 * Adjacencia circular com a origem no centro do disco
	 * @param raio
	 * @return
	 */
	public static AdjacencyRelation getCircular(double raio) {
		if(adj8 != null && raio == 1.5){
			return adj8;
		}else if(adj4 != null && raio == 1){
			return adj4;
		}
			
		
		int i, j, k, n, dx, dy, r0, r2, i0 = 0;
		n = 0;
		r0 = (int) raio;
		r2 = (int) (raio * raio);
		for (dy = -r0; dy <= r0; dy++)
			for (dx = -r0; dx <= r0; dx++)
				if (((dx * dx) + (dy * dy)) <= r2)
					n++;

		AdjacencyRelation adj = new AdjacencyRelation(n);
		
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
	
	
	
	/**
	 * Adjacencia 4-connectados
	 * @return
	 */
	public static AdjacencyRelation getKAdjacency(){
		AdjacencyRelation A = new AdjacencyRelation(4);
		A.px[0] = 0; A.py[0] = -1;
		A.px[1] = 1; A.py[1] = -1;
		A.px[2] = 0; A.py[2] = +1;
		A.px[3] = +1; A.py[3] = +1;
		return(A);
	}
	
	
	
	public void set(int px, int py, int i){
		this.px[i] = px;
		this.py[i] = py;
	}
}
