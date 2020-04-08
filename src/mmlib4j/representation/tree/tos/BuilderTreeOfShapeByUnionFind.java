package mmlib4j.representation.tree.tos;

import java.util.Arrays;
import java.util.Iterator;

import mmlib4j.datastruct.PriorityQueueToS;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.AbstractImageFactory;
import mmlib4j.images.impl.ByteImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 *
 * Implementacao do algoritmo para construcao da tree of shape descrito em: 
 * 
 * Thierry Geraud, Edwin Carlinet, Sebastien Crozet and Laurent Najman, 
 * A Quasi-linear Algorithm to Compute the Tree of Shapes of nD Images, ISMM, 2013.
 */
public class BuilderTreeOfShapeByUnionFind implements BuilderTreeOfShape {
	private final static int NIL = -1;
	private final static int px[] = new int[]{1, 0,-1, 0};
  	private final static int py[] = new int[]{0, 1, 0,-1};
	private int interpWidth;
	private int interpHeight;
	private int imgWidth;
	private int imgHeight;
	private int imgR[];
	private byte imgU[];
	private int parent[];
	private int numNode; 
	private int numNodeIdMax;
	private boolean isLog = true;
	private int xInfinite;
	private int yInfinite;
	
	GrayScaleImage img;
	private NodeToS root;
	
	
	
	protected BuilderTreeOfShapeByUnionFind(){ }
	
	public BuilderTreeOfShapeByUnionFind getClone(){
		BuilderTreeOfShapeByUnionFind b = new BuilderTreeOfShapeByUnionFind();
		b.interpWidth = this.interpWidth;
		b.interpHeight = this.interpHeight;
		b.imgWidth = this.imgWidth;
		b.imgHeight = this.imgHeight;
		b.parent = this.parent;
		b.isLog = this.isLog;
		b.xInfinite = this.xInfinite;
		b.yInfinite = this.yInfinite;
		b.img = this.img;	
		b.imgR = this.imgR;
		b.imgU = this.imgU;
		
		b.unInterpolateAndCreateTree( b.parent );
		return b;
	}			
	
	public BuilderTreeOfShapeByUnionFind(GrayScaleImage img, boolean isInter){
		this(img, -1, -1, isInter);
	}
	
	public BuilderTreeOfShapeByUnionFind(GrayScaleImage img, int xInfinito, int yInfinito, boolean isInter){
		this.imgWidth = img.getWidth();
		this.imgHeight = img.getHeight();
		this.img = img;
		if(img.getDepth() != 8)
			throw new RuntimeException("This implementation works only for 8-bits grayscale images.");
		this.xInfinite = xInfinito;
		this.yInfinite = yInfinito;		
		sort(interpolateImage());
		this.parent = createTreeByUnionFind();
		if(isInter)
			unInterpolateAndCreateTree( parent );
		else
			createTree(parent);
		this.img = getImageInterpolated();
		this.imgWidth = img.getWidth();
		this.imgHeight = img.getHeight();
			
	}
	
	/**
	 * Constru��o da arvore de formas
	 * @param pixels => pixels da imagem no formato de um vetor 1D, ou seja, (x,y) eh a posicao (x + y * width) do vetor  
	 * @param imgWidth => largura da imagem
	 * @param imgHeight => altura da imagem
	 * @return raiz da arvore 
	 */
	public NodeToS getRoot(){
		return root;
	}
	
	
	
	public int getxInfinite() {
		return xInfinite;
	}

	public void setxInfinite(int xInfinite) {
		this.xInfinite = xInfinite;
	}

	public int getyInfinite() {
		return yInfinite;
	}

	public void setyInfinite(int yInfinite) {
		this.yInfinite = yInfinite;
	}

	public int getNumNode(){
		return numNode;
	}

	public int getNumNodeIdMax() {
		return numNodeIdMax;
	}

	public void setNumNodeIdMax(int numNodeIdMax) {
		this.numNodeIdMax = numNodeIdMax;
	}
	
	public GrayScaleImage getInputImage(){
		return img;
	}
	
	public GrayScaleImage getImageInterpolated(){
		return ImageFactory.createReferenceGrayScaleImage(8, imgU, interpWidth, interpHeight);
	}
	
	/**
	 * Desinterpolacao da arvore e a transforma em uma arvore de estrutura ligada. 
	 * @return raiz da arvore
	 */
	public void createTree( int parent[]  ){
		long ti = System.currentTimeMillis();
		this.numNode = 0;
		NodeToS nodesMapTmp[] = new NodeToS[parent.length];
		for (int i = 0; i < imgR.length; i++) {
			int p = imgR[i];
			int pai = parent[p];
			
			if(p == pai){ //Note que:  p = pInfinito
				this.root = nodesMapTmp[p] = new NodeToS(numNode++, ByteImage.toInt(imgU[p]), img, p);
				nodesMapTmp[p].addPixel( p );
				continue;
			}
			
			if(nodesMapTmp[pai] == null) {
				nodesMapTmp[pai] = new NodeToS(numNode++, ByteImage.toInt(imgU[pai]), img, p);
			}
			 
			if(ByteImage.toInt(imgU[p]) != ByteImage.toInt(imgU[pai])){ //novo no
				if(nodesMapTmp[p] == null){
					nodesMapTmp[p] = new NodeToS(numNode++, ByteImage.toInt(imgU[p]), img, p);
				}
				nodesMapTmp[p].setParent( nodesMapTmp[pai] );
				nodesMapTmp[pai].addChildren(nodesMapTmp[p]);	
			}else{ 
				//mesmo no
				nodesMapTmp[p] = nodesMapTmp[pai];
			}
			nodesMapTmp[p].addPixel(p);
			
		}
		
		nodesMapTmp = null;
		//parent = null;
		//imgU = null;
		//imgR = null;
		this.numNodeIdMax = numNode;
		long tf = System.currentTimeMillis();
        if(Utils.debug)
        	System.out.println("Tempo de execucao [unInterpolate] "+ ((tf - ti) /1000.0)  + "s");
		
	}
	
	/**
	 * Desinterpolacao da arvore e a transforma em uma arvore de estrutura ligada. 
	 * @return raiz da arvore
	 */
	public void unInterpolateAndCreateTree(int parent[]){
		long ti = System.currentTimeMillis();
		this.numNode = 0;
		NodeToS nodesMapTmp[] = new NodeToS[parent.length];
		//WindowImages.show(AbstractImageFactory.instance.createGrayScaleImage(32, sumGradBoundary, interpWidth, interpHeight)); 
		for (int i = 0; i < imgR.length; i++) {
			int p = imgR[i];
			int pai = parent[p];
			int x = p % interpWidth;
			int y = p / interpWidth;
			int pixelUnterpolate = (x/2) + (y/2) * imgWidth;
			
			if(p == pai){ //Note que:  p = pInfinito
				this.root = nodesMapTmp[p] = new NodeToS(numNode++, ByteImage.toInt(imgU[p]), img, pixelUnterpolate);
				if(x % 2 == 1 && y % 2 == 1){
					nodesMapTmp[p].addPixel(pixelUnterpolate);					
				}
				continue;
			}
			
			if(nodesMapTmp[pai] == null) {
				nodesMapTmp[pai] = new NodeToS(numNode++, ByteImage.toInt(imgU[pai]), img, pixelUnterpolate);
			}
			 
			if(ByteImage.toInt(imgU[p]) != ByteImage.toInt(imgU[pai])){ //novo no
				if(nodesMapTmp[p] == null){
					nodesMapTmp[p] = new NodeToS(numNode++, ByteImage.toInt(imgU[p]), img, pixelUnterpolate);
				}
				nodesMapTmp[p].setParent( nodesMapTmp[pai] );
				nodesMapTmp[pai].addChildren(nodesMapTmp[p]);	
			}else{ 
				//mesmo no
				nodesMapTmp[p] = nodesMapTmp[pai];
			}
			
			if(x % 2 == 1 && y % 2 == 1){
				nodesMapTmp[p].addPixel(pixelUnterpolate);				
			}
		}
		
		nodesMapTmp = null;
		//parent = null;
		//imgU = null;
		//imgR = null;
		this.numNodeIdMax = numNode;
		long tf = System.currentTimeMillis();
        if(Utils.debug)
        	System.out.println("Tempo de execucao [unInterpolateAndCreateTree] "+ ((tf - ti) /1000.0)  + "s");
		
	}
	
	/**
	 * Rotina do union-find
	 * @param zPar
	 * @param x
	 * @return
	 */
	private int findRoot(int zPar[], int x) {
		if (zPar[x] == x)
			return x;
		else {
			zPar[x] = findRoot(zPar, zPar[x]);
			return zPar[x];
		}
	}
	
	/**
	 * Cria a arvore utilizando o algoritmo da union-find e canoniza a arvore
	 * @return mapa de parentesco
	 */
	private int[] createTreeByUnionFind( ) {
		long ti = System.currentTimeMillis();
		int parent[] = new int[imgR.length];
		int zPar[] = new int[imgR.length];
		
		for (int p = 0; p < imgR.length; p++) {
			zPar[p] =  NIL;
		}
		
		for(int i=imgR.length-1; i >= 0; i--){
			int p = imgR[i];
			parent[p] = p;
			zPar[p] = p;
			for (Integer n : getAdjPixels(p, zPar)) {
				int r = findRoot(zPar, n);
				if(p != r){
					parent[r] = p;
					zPar[r] = p;
				}
			}
			
		}
		
		// canonizacao da arvore
		for (int i = 0; i < imgR.length; i++) {
			int p = imgR[i];
			int q = parent[p];
			if(ByteImage.toInt(imgU[parent[q]]) == ByteImage.toInt(imgU[q])){
				parent[p] = parent[q];
			}
		}
		zPar = null;
		
		long tf = System.currentTimeMillis();
		if(Utils.debug)
        	System.out.println("Tempo de execucao [createTreeByUnionFind] "+ ((tf - ti) /1000.0)  + "s");
		
        return parent;
	}
	
	public int[] getOrderedPixels() {
		return imgR;
	}
	
	public byte[] getValuesOfOrderedPixels() {
		return imgU;
	}
	
	public int[] getParent() {
		return parent;
	}
	
	/**
	 * Ordena a imagem interpolada. 
 	 * Os pixels ordernados estao no vetor imgR[] e os nivel de cinza dos pixels estao no vetor imgU[]. 
 	 * Ordencao crescente dos pixels: imgR[lenght-1], imgR[lenght-2], ..., imgR[0]   
 	 * Ordencao decrescente dos pixels: imgR[0], imgR[1], ..., imgR[lenght-1]
	 * @param interpolation => pixels da imagem interpolada
	 */
	public void sort(byte interpolation[][]){
		long ti = System.currentTimeMillis();
  		//Algoritmo de ordenacao do paper
		int size = interpWidth * interpHeight;
  		PriorityQueueToS queue = new PriorityQueueToS();
		int i = 0;
		boolean dejavu[] = new boolean[size]; //dejavu eh inicializado com false
		this.imgR = new int[size];
		this.imgU = new byte[size];
		int pInfinito = getInfinity(interpolation, 0); //0 is min; 1 is max 
		queue.initial(pInfinito, ByteImage.toInt(interpolation[pInfinito][0]));
		dejavu[pInfinito] = true;
		while(!queue.isEmpty()){
			int h = queue.priorityPop();
			imgU[h] = ByteImage.toByte( queue.getCurrentPriority() ); //l = prioridade corrente da queue
			imgR[i] = h;
			for(Integer n: getAdjPixels(h, dejavu)){
				queue.priorityPush(n, ByteImage.toInt(interpolation[n][0]), ByteImage.toInt(interpolation[n][1]));
				dejavu[n] = true;
			}
			i++;
		}
		long tf = System.currentTimeMillis();
		if(Utils.debug)
			System.out.println("Tempo de execucao [sort] "+ ((tf - ti) /1000.0)  + "s");
	}
	

	private int getInfinity(byte interpolation[][], int OPT ){
		//long ti = System.currentTimeMillis(); y * width + x
		if(xInfinite > -1 && yInfinite > -1) return (2*yInfinite + 1) * interpWidth + (2*xInfinite + 1);
		
		  
		
		short order[] = new short[2*interpWidth + 2*interpHeight]; 
		//short orderOri[] = new short[2*interpWidth + 2*interpHeight]; 
		for(int px=0; px < interpWidth; px++){
			order[px] = interpolation[px + (0) * interpWidth][OPT];
			order[px + interpWidth] = interpolation[px + (interpHeight-1) * interpWidth][OPT];;
		}
		for(int py=0; py < interpHeight; py++){
			order[py + 2 * interpWidth] = interpolation[py * interpWidth][OPT];;
			order[py + interpHeight + 2 * interpWidth] = interpolation[(interpWidth-1) + py * interpWidth][OPT];;
		}
		
		//System.arraycopy(order, 0, orderOri, 0, order.length);
		
		Arrays.sort(order);
		int value = order[order.length/2];
		int pInf = -1;
		for(int px=0; px < interpWidth; px++){
			if(value == interpolation[px + 0 * interpWidth][OPT]) {
				pInf = (px + 0 * interpWidth);
				break;
			}
			else if(value == interpolation[px + (interpHeight-1) * interpWidth][OPT]) {
				pInf = (px + (interpHeight-1) * interpWidth);
				break;
			}
		}
		for(int py=0; py < interpHeight; py++){
			if(value == interpolation[0 + py * interpWidth][OPT]) {
				pInf = 0 + py * interpWidth;
				break;
			}
			else if(value == interpolation[(interpWidth-1) + py * interpWidth][OPT]) {
				pInf = (interpWidth-1) + py * interpWidth;
				break;
			}
			
		}
		
		xInfinite = ((int)(pInf % interpWidth)) / 2;
		yInfinite = ((int)(pInf / interpWidth)) / 2;
		
		return (2*xInfinite + 1) * interpWidth + (2*yInfinite + 1);
	}
	
	
	/**
	 * Devolve a lista de pixels adjacente a pixel de referencia. 
	 * @param p => pixel de referencia
	 * @param zPar => vetor dos pixels visitados
	 * @return lista de adjacentes
	 */
    public Iterable<Integer> getAdjPixels(int p, final int zPar[]) {
    	final int x = p % interpWidth;
    	final int y = p / interpWidth;
    	
        return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private int i = 0;
					public boolean hasNext() {
						while(i < px.length){
							int xx = px[i] + x;
							int yy = py[i] + y;
							int pixel = xx + yy * interpWidth;
				        	if(xx >= 0 && xx < interpWidth && yy >= 0 && yy < interpHeight && zPar[pixel] != NIL)
								return true;
				        	i++;
						} 
						return false;
					}
					public Integer next() {
						int pixel = (px[i] + x) + (py[i] + y) * interpWidth;
						i++;
						return pixel;
					}
					public void remove() { }
					
				};
			}
		};
    }
	
    /**
	 * Devolve a lista de pixels adjacente a pixel de referencia. 
	 * @param p => pixel de referencia
	 * @param dejavu => vetor dos pixels visitados
	 * @return lista de adjacentes
	 */
	private Iterable<Integer> getAdjPixels(int p, final boolean dejavu[]) {
    	final int x = p % interpWidth;
    	final int y = p / interpWidth;
    	
        return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private int i = 0;
					public boolean hasNext() {
						while(i < px.length){
							int xx = px[i] + x;
							int yy = py[i] + y;
							int pixel = xx + yy * interpWidth;
							if(xx >= 0 && xx < interpWidth && yy >= 0 && yy < interpHeight && !dejavu[pixel])
								return true;
							i++;
						} 
						return false;
					}
					public Integer next() {
						int pixel = (px[i] + x) + (py[i] + y) * interpWidth;
						i += 1;
						return pixel;
					}
					public void remove() { }
					
				};
			}
		};
        
    }
	
	
	//(x) even parity and (y) even parity => ( x - 1, y - 1 ) ; ( x + 1, y - 1 ); ( x - 1, y + 1 ) ; ( x + 1, y + 1 )
	private final static int adjCircleX[] = new int[]{-1, +1, -1, +1};
  	private final static int adjCircleY[] = new int[]{-1, -1, +1, +1};
	
  	//Rectangles H: (x) even parity and (y) odd parity => ( x, y - 1 ) ; ( x, y + 1 )
  	private final static int adjRetHorX[] = new int[]{0, 0};
  	private final static int adjRetHorY[] = new int[]{-1, +1};
	
  	//Rectangles V: (x) odd parity and (y) even parity => ( x + 1, y ) ; ( x - 1, y )
  	private final static int adjRetVerX[] = new int[]{+1, -1};
  	private final static int adjRetVerY[] = new int[]{0, 0};

	/**
	 * Interpola os pixels da imagem.   
	 * @param matrix => pixels da imagem 
	 * @return pixels interpolados.
	 */
	public byte[][] interpolateImage( ) {
		long ti = System.currentTimeMillis();
		
        this.interpWidth = (imgWidth*2+1);
        this.interpHeight = (imgHeight*2+1);
        byte interpolation[][] = new byte[interpWidth * interpHeight][2];
		
		// Pixels array
		short [] pixels = new short [ img.getSize() ];
		int x,y,pT;
		for( int p = 0; p < img.getSize(); p++ ) {
			
			// Copy pixels array to be used to calculate median
			pixels[ p ] = (short) img.getPixel( p );				
			
			x = p % img.getWidth();
			y = p / img.getWidth();
			pT = (2*y + 1) * interpWidth + (2*x + 1);
			
			// Get coords to set grid pixel value
			interpolation[pT][0] = interpolation[pT][1] = ByteImage.toByte( img.getPixel( p ) );
		}
		Arrays.sort( pixels );		
		
		// Calculated based on Thierry examples
		int median = ( pixels[ img.getSize() / 2 - 1 ] + pixels[ img.getSize() / 2 + 1 ] ) / 2;	
		int min, max;
		int adjX[] =  null;
		int adjY[] =  null;
		int qT, qX, qY;
		for(pT = 0; pT < interpolation.length; pT++ ) {
			x = pT % interpWidth;
			y = pT / interpWidth;
			
			if( x % 2 == 0 && y % 2 == 0 ) { // Circles		
				adjX = adjCircleX;
				adjY = adjCircleY;
			} 
			else if (x % 2 == 0 && y % 2 == 1) { // Vertical rectangles
				adjX = adjRetVerX;
				adjY = adjRetVerY;
			} 
			else if (x % 2 == 1 && y % 2 == 0) { // horiz rectangles
				adjX = adjRetHorX;
				adjY = adjRetHorY;
			} 
			else {	
				continue;
			}
			
			min = Integer.MAX_VALUE;
			max = Integer.MIN_VALUE;
			for(int i=0; i < adjX.length; i++){
				qY = (y + adjY[i]);
				qX = (x + adjX[i]);
				if(qY >= 0 && qX >= 0 && qY < interpHeight && qX < interpWidth){
					qT = qY * interpWidth + qX;
					if(ByteImage.toInt(interpolation[qT][1]) > max){
						max = ByteImage.toInt( interpolation[qT][1] );
					}
					if(ByteImage.toInt(interpolation[qT][0]) < min){
						min = ByteImage.toInt( interpolation[qT][1] );
					}
				}
				else{
					if(median > max){
						max = median;
					}
					if(median < min){
						min = median;
					}
				}
			}
			interpolation[pT][0] = ByteImage.toByte(min);
			interpolation[pT][1] = ByteImage.toByte(max);
		}
        
        long tf = System.currentTimeMillis();
        if(Utils.debug){
        	System.out.println("Tempo de execucao [interpolacao2] "+ ((tf - ti) /1000.0)  + "s");
	        
	        /*for(y=0; y < interpHeight; y++){
	        	for(x=0; x < interpWidth; x++){
	        		pT = y * interpWidth + x; 
	        		System.out.printf("[%3d, %3d] ", ByteImage.toInt(interpolation[pT][0]), ByteImage.toInt(interpolation[pT][1]));
	        	}
	        	System.out.println("");
	        }
	        */
        }
        return interpolation;
	}

	


	public static void main( String args [] ) {
		// First example of Thierry
		GrayScaleImage input1 = ImageFactory.createReferenceGrayScaleImage(ImageFactory.DEPTH_8BITS, new byte[]{
						0, 0, 3, 2,
						0, 1, 1, 2,
						0, 0, 2, 2}
		, 4, 3);
		
		
		// Second example of Thierry
		GrayScaleImage input2 = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_8BITS, 5, 5 );
		input2.setPixel(0, 0, 128 );
		input2.setPixel(1, 0, 124 );
		input2.setPixel(2, 0, 150 );
		input2.setPixel(3, 0, 137 );
		input2.setPixel(4, 0, 106 );
		input2.setPixel(0, 1, 116 );
		input2.setPixel(1, 1, 128 );
		input2.setPixel(2, 1, 156 );
		input2.setPixel(3, 1, 165 );
		input2.setPixel(4, 1, 117 );		
		input2.setPixel(0, 2, 117 );
		input2.setPixel(1, 2, 90 );
		input2.setPixel(2, 2, 131 );
		input2.setPixel(3, 2, 108 );
		input2.setPixel(4, 2, 151 );
		input2.setPixel(0, 3, 107 );
		input2.setPixel(1, 3, 87 );
		input2.setPixel(2, 3, 118 );
		input2.setPixel(3, 3, 109 );
		input2.setPixel(4, 3, 167 );
		input2.setPixel(0, 4, 107 );
		input2.setPixel(1, 4, 73 );
		input2.setPixel(2, 4, 125 );
		input2.setPixel(3, 4, 157 );
		input2.setPixel(4, 4, 117 );
		
		BuilderTreeOfShapeByUnionFind build = new BuilderTreeOfShapeByUnionFind(input1, false);
		
		
		
	}

}