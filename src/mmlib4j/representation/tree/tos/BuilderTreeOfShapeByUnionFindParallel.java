package mmlib4j.representation.tree.tos;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import mmlib4j.datastruct.PriorityQueueToS;
import mmlib4j.filtering.EdgeDetectors;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ByteImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.utils.ImageBuilder;
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
public class BuilderTreeOfShapeByUnionFindParallel implements BuilderTreeOfShape{
	private final static int NIL = -1;
	private final static int px[] = new int[]{1, 0,-1, 0};
  	private final static int py[] = new int[]{0, 1, 0,-1};
	private int interpWidth;
	private int interpHeight;
	private int imgR[];
	private byte imgU[];
	private int parent[];
	private int numNode; 	
	
	private int xInfinito=-1;
	private int yInfinito=-1;
	
	GrayScaleImage img;
	private NodeToS root;
	
	private int median;
	
	/* 
	 * 
	 * 	Parameters from article : 
	 * 
	 *  Efficient Computation of Attributes and Saliency Maps on Tree-Based Image Representations 
	 * 
	 **/
	
	GrayScaleImage imgGrad;
	int area [];
	int sumGrad [];
	int appear [];
	int vanish [];
	int sumGray [];
	int contourLength [];
	
	/**/
	
	ArrayList<Integer> shapes;
	
	
	private BuilderTreeOfShapeByUnionFindParallel(){ }	
			
	
	/* Arrumar depois */
	
	
	public BuilderTreeOfShapeByUnionFindParallel getClone() {
		
		BuilderTreeOfShapeByUnionFindParallel b = new BuilderTreeOfShapeByUnionFindParallel();
		
		b.interpWidth = this.interpWidth;
		b.interpHeight = this.interpHeight;
		b.parent = this.parent;
		
		b.xInfinito = this.xInfinito;
		b.yInfinito = this.yInfinito;
		b.img = this.img;	
		b.imgR = this.imgR;
		b.imgU = this.imgU;
		
		b.createTree( b.parent );
		
		return b;
	}
	
	//2-face m=[0 ate  3]
	//1-face m=[4 ate 11]
	//0-face m=[12 ate 15]
	// moves[x][0] movimentos em y, moves[x][1] movimentos em x
	
	private final static int[][] moves = {
		
		{ 0, 0 }, //bloco principal
		
		{ 2, 2 }, //bloco maior centro
		{ 2, 0 }, //bloco maior abaixo
		{ 0, 2 }, //bloco maior direita

		
		{ 1, 0 }, // 4 blocos pequenos (linhas) que olham para cima e para baixo
		{ 3, 0 }, 
		{ 1, 2 },
		{ 3, 2 },
		
		{ 0, 1 }, // 4 blocos pequenos (linhas) que olham para esquerda e para direita
		{ 0, 3 },
		{ 2, 1 },
		{ 2, 3 }, 
		
		{ 1, 1 }, // 4 blocos pequenos (bolinhas) que olham para 4 direções
		{ 3, 1 },
		{ 1, 3 },
		{ 3, 3 },
	};


	private final static int[][][] vizinhos = { //vizinhos para cada bloco
		{ },
		{ {-2, -2}, {-2, 2}, {2, -2}, {2, 2} },
		{ {2, 0}, {-2, 0} },
		{ {0, 2}, {0, -2} },
		
		{ {1, 0}, {-1, 0} }, // 4 para cima e baixo
		{ {1, 0}, {-1, 0} },
		{ {1, 0}, {-1, 0} },
		{ {1, 0}, {-1, 0} },
		{ {0, 1}, {0, -1} }, // 4 para esquerda e direita
		{ {0, 1}, {0, -1} },
		{ {0, 1}, {0, -1} },
		{ {0, 1}, {0, -1} },
		
		{ {-1, -1}, {-1, 1}, {1, -1}, {1, 1} }, //4 para as 4 direcoes
		{ {-1, -1}, {-1, 1}, {1, -1}, {1, 1} }, 
		{ {-1, -1}, {-1, 1}, {1, -1}, {1, 1} }, 
		{ {-1, -1}, {-1, 1}, {1, -1}, {1, 1} }, 
	};
	
	public BuilderTreeOfShapeByUnionFindParallel(GrayScaleImage img,  boolean isInter){

		super();
		
		this.img = img;
	
		sort( interpolateImage() );
		
		if( !isInter )
			this.img = getImageInterpolated();
		
		this.parent = createTreeByUnionFind();
		
		if( isInter ) {
			
			unInterpolateAndCreateTree( parent );
			
		}else {
			
			createTree( parent );
			
		}
		
		//posProcessing();
		
		/*Object obj[] = interpolateImageParallel( img );
		sort( obj );
		this.parent = createTreeByUnionFind();
		unInterpolateTree( parent );*/
	}
	
	public void createTree( int parent[]  ){
		
		long ti = System.currentTimeMillis();
		
		this.numNode = 0;
		
		NodeToS nodesMapTmp[] = new NodeToS[ parent.length ];
		
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
			if(nodesMapTmp[pai].children == null)	
				nodesMapTmp[pai].children = new LinkedList<NodeToS>();
			 
			if(imgU[p] != imgU[pai]){ //novo no
				if(nodesMapTmp[p] == null){
					nodesMapTmp[p] = new NodeToS(numNode++, ByteImage.toInt(imgU[p]), img, p);
				}
				nodesMapTmp[p].parent = nodesMapTmp[pai];
				nodesMapTmp[pai].children.add(nodesMapTmp[p]);	
			}else{ 
				//mesmo no
				nodesMapTmp[ p ] = nodesMapTmp[pai];
			}
			
			nodesMapTmp[ p ].addPixel( p );			
		}
		
		nodesMapTmp = null;
		//parent = null;
		//imgU = null;
		//imgR = null;
		
		long tf = System.currentTimeMillis();
		if(Utils.debug)
        	System.out.println("Tempo de execucao [unInterpolate2] "+ ((tf - ti) /1000.0)  + "s");
		
	}
	
	private BuilderTreeOfShapeByUnionFindParallel(GrayScaleImage img){
		this.img = img;
	}
	
	
	public static Object[] getImageInterpolate(GrayScaleImage img){
		BuilderTreeOfShapeByUnionFindParallel b = new BuilderTreeOfShapeByUnionFindParallel();
		return b.interpolateImageParallel(img);
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
	
	public int getNumNode(){
		return numNode;
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
	public void unInterpolateAndCreateTree( int parent[]  ) {
		
		long ti = System.currentTimeMillis();
		
		this.numNode = 0;
		
		this.root = null;
		
		NodeToS nodesMapTmp[] = new NodeToS[ parent.length ];
		
		boolean flags[] = new boolean[ parent.length ];
		
		//WindowImages.show(ImageFactory.createGrayScaleImage(32, sumGradBoundary, interpWidth, interpHeight));
		
		int p = imgR[0];
		int pai = parent[p];
		int x = p % interpWidth;
		int y = p / interpWidth;
		
		int pixelUnterpolate = (x/4-1) + (y/4) * img.getWidth();
		
		if( pixelUnterpolate < 0 ) pixelUnterpolate = 0;
		
		if( p == pai ) { //Note que:  p = pInfinito
			
			this.root = nodesMapTmp[p] = new NodeToS(numNode++, ByteImage.toInt( imgU[p] ), img, pixelUnterpolate);
			
			if( isRealPixel( x, y ) ) {
				
				nodesMapTmp[ p ].addPixel( pixelUnterpolate );
				
			}
			
		}
		
		//paralelisavel
		for ( int i = 1; i < imgR.length; i++ ) {
			
			p = imgR[i];
			pai = parent[p];
			x = p % interpWidth;
			y = p / interpWidth;
			
			pixelUnterpolate = (x/4-1) + (y/4) * img.getWidth();
			
			if( pixelUnterpolate < 0 ) pixelUnterpolate = 0;
			
			if( imgU[p] != imgU[pai] ) { //novo no
				
				if(nodesMapTmp[p] == null){
					nodesMapTmp[p] = new NodeToS(numNode++, ByteImage.toInt( imgU[p] ), img, pixelUnterpolate);
				}	
			
				if(nodesMapTmp[pai] == null){
					int xPai = pai % interpWidth;
					int yPai = pai / interpWidth;
					int pixelUnterpolatePai = (xPai/4-1) + (yPai/4) * img.getWidth();	
					int paiPai = parent[pai];
					nodesMapTmp[ pixelUnterpolatePai ] = new NodeToS( numNode++, ByteImage.toInt( imgU[paiPai] ), img, pixelUnterpolatePai);
				}
				
				nodesMapTmp[p].parent = nodesMapTmp[pai];
				if(!flags[p]){
					flags[p] = true;
					nodesMapTmp[pai].children.add(nodesMapTmp[p]);
				}
			}
			else{ 
				//mesmo no
				if(nodesMapTmp[pai] == null){
					int xPai = pai % interpWidth;
					int yPai = pai / interpWidth;
					int pixelUnterpolatePai = (xPai/4-1) + (yPai/4) * img.getWidth();	
					int paiPai = parent[pai];
					nodesMapTmp[ pixelUnterpolatePai ] = new NodeToS( numNode++, ByteImage.toInt( imgU[ paiPai ] ), img, pixelUnterpolatePai );
				}
				
				nodesMapTmp[p] = nodesMapTmp[pai];
			}
			
			if( isRealPixel( x, y ) ) {		
				
				nodesMapTmp[ p ].addPixel( pixelUnterpolate );
			}
		}
		
		long tf = System.currentTimeMillis();
        if(Utils.debug)
        	System.out.println("Tempo de execucao [unInterpolate] "+ ((tf - ti) /1000.0)  + "s");
		
        nodesMapTmp = null;
		/*parent = null;
		imgU = null;
		imgR = null;
		*/
		System.gc();
		
	}
	
	/**
	 * Desinterpolacao da arvore e a transforma em uma arvore de estrutura ligada. 
	 * @return mapa node
	 */
	
	public void unInterpolateTree2( int parent[]  ){
		this.numNode = 0;
		NodeToS nodesMap[] = new NodeToS[parent.length];
		//WindowImages.show(ImageFactory.createGrayScaleImage(32, sumGradBoundary, interpWidth, interpHeight));
		img = ImageFactory.createGrayScaleImage(8, interpWidth, interpHeight);
		
		for (int p = 0; p < imgU.length; p++) {
			img.setPixel(p, imgU[p]);
		}
		
		for (int i = 0; i < imgR.length; i++) {
			int p = imgR[i];
			int pai = parent[p];
			if(p == pai){
				this.root = nodesMap[p] = new NodeToS(numNode++, ByteImage.toInt(imgU[p]), img, p);
				nodesMap[p].addPixel( p );
			}
			else{
				if(img.getPixel(pai) != img.getPixel(p)){ //novo no
					if(nodesMap[p] == null){ 
						nodesMap[p] = new NodeToS(numNode++, ByteImage.toInt( imgU[p] ), img, p);	
					}
					nodesMap[p].parent = nodesMap[pai];
					nodesMap[pai].children.add(nodesMap[p]);
					nodesMap[p].addPixel( p );
				}else if (img.getPixel(pai) == img.getPixel(p)){ 
					//mesmo no
					nodesMap[p] = nodesMap[pai];
					nodesMap[pai].addPixel( p );				
				}	
			}			
		}

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
		
		int parent[] = new int[ imgR.length ];
		
		int zPar[] = new int[ imgR.length ];
		
		
		/* Is boundary image */	
		
		boolean is_boundary [] = new boolean[ imgR.length ];
		
		
		/* Attributes based on region */
		
		area = new int[ imgR.length ];
		
		sumGray = new int[ imgR.length ]; 
		
		
		/* Attributes calculated on contour ( sumGrad, appear, vanish, contourLength ) */
		
		imgGrad = EdgeDetectors.sobel( ImageFactory.createReferenceGrayScaleImage( 8, imgU, interpWidth, interpHeight ) );
		
		sumGrad = new int[ imgR.length ];
		
		appear = new int[ imgR.length ];
		
		vanish = new int[ imgR.length ];
			
		contourLength = new int[ imgR.length ];	
		
		
		for ( int p = 0; p < imgR.length; p++ ) {			
			
			zPar[ p ] =  NIL;
			
			
			area[ p ] = 0;
			
			sumGray[ p ] = 0;
			
			contourLength[ p ] = 0;
			
			sumGrad[ p ] = 0;
			
			if( !isFace1( p ) ) {
				
				is_boundary[ p ] = true;
				
			}
	
		}			
		
		for( int i = imgR.length-1 ; i >= 0 ; i-- ) {
			
			
			int p = imgR[ i ];					
			
			parent[ p ] = p;
			
			zPar[ p ] = p;
			
			
			if( isRealPixel( p ) ) { 
			
				area[ p ] = 1;
				
				sumGray[ p ] =  ByteImage.toInt( imgU[ p ] );
			
			}
			
			for ( Integer n : getAdjPixels( p, zPar ) ) {
				
				int r = findRoot( zPar, n );
				
				if( p != r ) {
					
					parent[ r ] = p;
					
					zPar[ r ] = p;
					
					area[ p ] = area[ p ] + area[ r ];
					
					sumGray[ p ] = sumGray[ p ] + sumGray[ r ];
					
					contourLength[ p ] = contourLength[ p ] + contourLength[ r ];
					
					sumGrad[ p ] = sumGrad[ p ] + sumGrad[ r ];
					
				}
				
			}							
			
			for( Integer e : getBoundaries( p )  ) {									
				
				if( !is_boundary[ e ] ) {
					
					is_boundary[ e ] = true;			
					
					if( isConnectedToReal2face( e ) != 0 ) {
					
						contourLength[ p ] = contourLength[ p ] + 1;
					
					}
					
					sumGrad[ p ] = sumGrad[ p ] + imgGrad.getPixel( e ); 
					
					appear[ e ] = p;
					
				} else {
																				
					is_boundary[ e ] = false;		
					
					if( isConnectedToReal2face( e ) != 0 ) {											
						
						contourLength[ p ] = contourLength[ p ] - 1;
					
					}
					
					sumGrad[ p ] = sumGrad[ p ] - imgGrad.getPixel( e );
					
					vanish[ e ] = p;
					
				}
				
			}
			
		}
	 	
		// canonizacao da arvore
		
		for ( int i = 0 ; i < imgR.length ; i++ ) {
			
			int p = imgR[ i ];
			
			int pai = parent[ p ];
			
			if( imgU[ parent[ pai ] ] == imgU[ pai ] ){
				
				parent[ p ] = parent[ pai ];
				
			}
			
		}
		
		zPar = null;
		
		
  		/*
  		System.out.println("\n\nparent\n");
  		for(int y=0; y < interpHeight; y++){
			for(int x=0; x < interpWidth; x++){
				int pmax = parent[x + y * interpWidth];
				System.out.printf("%3d &", pmax);
			}
			System.out.println();
		}*/
		
		long tf = System.currentTimeMillis();
        if(Utils.debug)
        	System.out.println("Tempo de execucao [union-find] "+ ((tf - ti) /1000.0)  + "s");
		
        return parent;
	}
	
	
	/**
	 * Ordena a imagem interpolada. 
 	 * Os pixels ordernados estao no vetor imgR[] e os nivel de cinza dos pixels estao no vetor imgU[]. 
 	 * Ordencao crescente dos pixels: imgR[lenght-1], imgR[lenght-2], ..., imgR[0]   
 	 * Ordencao decrescente dos pixels: imgR[0], imgR[1], ..., imgR[lenght-1]
	 * @param interpolation => pixels da imagem interpolada
	 */
	public void sort( Object interpolation[] ){
		long ti = System.currentTimeMillis();
  		//Algoritmo de ordenacao do paper
		int size = interpWidth * interpHeight;
  		PriorityQueueToS queue = new PriorityQueueToS();
		
  		boolean dejavu[] = new boolean[size]; //dejavu eh inicializado com false
  		this.imgR = new int[size];
  		this.imgU = new byte[size];
  		
  		short interpolation0[] = (short[]) interpolation[0];
  		short interpolation1[] = (short[]) interpolation[1];
  		
  		int pInfinito = getInfinity( interpolation0 ); 
  		//System.out.println("pInfinito (" + xInfinito + ", " + yInfinito + ")");
  		queue.initial(pInfinito, interpolation0[pInfinito]);
  		dejavu[pInfinito] = true;
  		int order = 0;
  		while(!queue.isEmpty()){
  			int h = queue.priorityPop();
  			imgU[h] = (byte) (0xFF & queue.getCurrentPriority()); //l = prioridade corrente da queue
  			imgR[order] = h;
  			for(int n: getAdjPixels(h, dejavu)){
  				queue.priorityPush(n, interpolation0[n], interpolation1[n]);
  				dejavu[n] = true;
  			}
  			order++;
  		}
  		
  		long tf = System.currentTimeMillis();
  		if(Utils.debug)
  			System.out.println("Tempo de execucao [sort] "+ ((tf - ti) /1000.0)  + "s");
	}
	
	
	public int findNivel(int vetor[], int p){
		for(int i=0; i < vetor.length; i++){
			if(vetor[i] == p)
				return i;
		}
		return -1;
	}
	
	private int getInfinity( short interpolation[] ){
		//long ti = System.currentTimeMillis();
		
		short order[] = new short[2*interpWidth + 2*interpHeight]; 
		//short orderOri[] = new short[2*interpWidth + 2*interpHeight]; 
		for(int px=0; px < interpWidth; px++){
			order[px] = interpolation[px + (0) * interpWidth];
			order[px + interpWidth] = interpolation[px + (interpHeight-1) * interpWidth];
		}
		for(int py=0; py < interpHeight; py++){
			order[py + 2 * interpWidth] = interpolation[py * interpWidth];
			order[py + interpHeight + 2 * interpWidth] = interpolation[(interpWidth-1) + py * interpWidth];
		}
		
		//System.arraycopy(order, 0, orderOri, 0, order.length);
		
		Arrays.sort(order);
		int value = order[order.length/2];
		
		for(int px=0; px < interpWidth; px++){
			if(value == interpolation[px + 0 * interpWidth])
				return (px + 0 * interpWidth);
			if(value == interpolation[px + (interpHeight-1) * interpWidth])
				return (px + (interpHeight-1) * interpWidth);
		}
		for(int py=0; py < interpHeight; py++){
			if(value == interpolation[0 + py * interpWidth])
				return 0 + py * interpWidth;
			if(value == interpolation[(interpWidth-1) + py * interpWidth])
				return (interpWidth-1) + py * interpWidth;
		}
		/*
		for(int p=0; p < order.length; p++){
			if(value == orderOri[p]){
				if(p < interpWidth){
					return p;
				}else if (p < 2*interpWidth){
					int x = p - interpWidth;
					int y = interpHeight-1;
					return (x + y * interpWidth);
				}
				else if (p <  interpHeight + 2 * interpWidth){
					int x = 0;
					int y = p - 2 * interpWidth;
					return (x + y * interpWidth);
				}else{
					int x = interpWidth - 1;
					int y = p - 2 * interpWidth;
					return (x + y * interpWidth);
				}
			}
		}*/
		return 0;
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
    
	private Iterable<Integer> getAdjPixels( int p, final boolean dejavu[] ) {
		
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
	
	/**
	 * 
	 *  Real pixels  
	 *  
	 *  	( i % 4 == 1 ) && ( j % 4 == 1 )
	 *  
	 *  Fake pixels in same real pixels line 
	 *  
	 *  	!( i % 4 == 1 ) && ( i%2 == 1 && j%2 == 1 && j%4 == 1 ) 
	 * 
	 *  Fake pixels in real pixels line + 1
	 *  
	 *  	( i%2 == 1 && (j-1)%2 == 0 ) && !( j % 4 == 1 )
	 * 
	 * */
	
	 /* Pixels fake na mesma linha */
	
	 private final static int adjFakePixelsSameLineX[] = new int [] { -2, +2 };
	 
	 private final static int adjFakePixelsSameLineY[] = new int [] { 0, 0 };
	
	 /* Pixels fake na próxima linha */
	 
	 private final static int adjFakePixelsNextLineX[] = new int [] { 0, 0 };
	 
	 private final static int adjFakePixelsNextLineY[] = new int [] { -2, +2 };
	 
	//(x) even parity and (y) even parity => ( x - 1, y - 1 ) ; ( x + 1, y - 1 ); ( x - 1, y + 1 ) ; ( x + 1, y + 1 )
	 
	private final static int adjCircleX[] = new int[]{-1, +1, -1, +1};
	
	private final static int adjCircleY[] = new int[]{-1, -1, +1, +1};	
	
	//Rectangles H: (x) even parity and (y) odd parity => ( x, y - 1 ) ; ( x, y + 1 )
	
  	private final static int adjRetHorX[] = new int[]{0, 0};
  	
  	private final static int adjRetHorY[] = new int[]{-1, +1};
	
  	//Rectangles V: (x) odd parity and (y) even parity => ( x + 1, y ) ; ( x - 1, y )
  	
  	private final static int adjRetVerX[] = new int[]{+1, -1};
  	
  	private final static int adjRetVerY[] = new int[]{0, 0};
  	
	
	public byte [][] interpolateImage() {
		
		long ti = System.currentTimeMillis();
		
		this.interpWidth = ( img.getWidth() * 4 ) - 1;
		
		this.interpHeight = ( img.getHeight() * 4 ) - 1;
		
		
		byte [][] interpolation = new byte[ this.interpWidth * this.interpHeight ][ 2 ];
		
		/* 
		 * 
		 *  Put real pixels in the grid and calculate mean 
		 * 
		 * */
		
		short [] pixels = new short [ img.getSize() ];
		
		int x, y, pT;
		
		for( int p = 0; p < img.getSize(); p++ ) {
					
			// Copy pixels array to be used to calculate median
			pixels[ p ] = ( short ) img.getPixel( p );				
					
			x = p % img.getWidth();
			y = p / img.getWidth();
			pT = (4*y + 1) * interpWidth + (4*x + 1);
					
			// Get coords to set grid pixel value
			interpolation[ pT ][ 0 ] = interpolation[ pT ] [ 1 ] = ByteImage.toByte( img.getPixel( p ) );
			
		}
		
		Arrays.sort( pixels );
		
		
		/* median value */
		
		this.median = ( pixels[ img.getSize() / 2 - 1 ] + pixels[ img.getSize() / 2 + 1 ] ) / 2;
		
		
		/* Fake pixels in same line */
		
		int min, max;
		
		int adjX [], adjY[];	
		
		adjX = adjFakePixelsSameLineX;
		
		adjY = adjFakePixelsSameLineY;
		
						
		for( y = 0 ; y < this.interpHeight ; y++ ) {
			
			for( x = 0 ; x < this.interpWidth ; x++ ) {		
				
				max = Integer.MIN_VALUE;
				
				if( isFakePixelSameLine( x, y ) ) {							
					
					for( int i = 0 ; i < adjX.length ; i++ ) {
						
						int qX = x + adjX[ i ], 
							qY = y + adjY[ i ];
						
						int qT = qY * interpWidth + qX;
						
						if( ByteImage.toInt( interpolation[ qT ][ 1 ] ) > max ) {
							
							max = ByteImage.toInt( interpolation[ qT ][ 1 ] );
							
						}
					
					}
					
					interpolation[ x + y * interpWidth ][ 0 ] = interpolation[ x + y * interpWidth ][ 1 ] = ByteImage.toByte( max );
					
				}
				
			}			
			
		}
		
		/* Fake pixels in next line */			
		
		adjX = adjFakePixelsNextLineX;
		
		adjY = adjFakePixelsNextLineY;
						
		for( y = 0 ; y < this.interpHeight ; y++ ) {
			
			for( x = 0 ; x < this.interpWidth ; x++ ) {			
				
				max = Integer.MIN_VALUE;
				
				if( isFakePixelNextLine( x, y ) ) {							
					
					for( int i = 0 ; i < adjX.length ; i++ ) {
						
						int qX = x + adjX[ i ], 
							qY = y + adjY[ i ];
						
						int qT = qY * interpWidth + qX;
						
						if( ByteImage.toInt( interpolation[ qT ][ 1 ] ) > max ) {
							
							max = ByteImage.toInt( interpolation[ qT ][ 1 ] );
							
						}
					
					}
					
					interpolation[ x + y * interpWidth ][ 0 ] = interpolation[ x + y * interpWidth ][ 1 ] = ByteImage.toByte( max );
					
				}
				
			}					
			
		}
		
		/* Circles and rects */	
						
		for( y = 0 ; y < this.interpHeight ; y++ ) {
			
			for( x = 0 ; x < this.interpWidth ; x++ ) {		
				
				if( isCircle( x, y ) ) {
					
					adjX = adjCircleX;
					
					adjY = adjCircleY;
					
				} else if( isHorizontalRect( x, y ) ){
					
					adjX = adjRetHorX;
					
					adjY = adjRetHorY;
					
				} else if( isVerticalRect( x, y ) ) {
					
					adjX = adjRetVerX;
					
					adjY = adjRetVerY;
					
				} else {
					
					continue;
					
				}
				
				min = Integer.MAX_VALUE;				
				max = Integer.MIN_VALUE;									
					
				for( int i = 0 ; i < adjX.length ; i++ ) {
						
					int qX = x + adjX[ i ], 
						qY = y + adjY[ i ];
						
					if( qY >= 0 && qX >= 0 && qY < interpHeight && qX < interpWidth ) {
						
						int qT = qY * interpWidth + qX;
						
						if( ByteImage.toInt( interpolation[ qT ][ 1 ] ) > max ) {
							
							max = ByteImage.toInt( interpolation[ qT ][ 1 ] );
							
						}
							
						if( ByteImage.toInt( interpolation[ qT ][ 0 ] ) < min ){
								
							min = ByteImage.toInt( interpolation[ qT ][ 0 ] );
								
						}
						
					} else {
							
						if( this.median < min ) {
								
							min = this.median;
								
						}
							
						if( this.median > max ) {
								
							max = this.median;
								
						}
							
					}
					
				}
					
				interpolation[ x + y * interpWidth ][ 0 ] = ByteImage.toByte( min );
							
				interpolation[ x + y * interpWidth ][ 1 ] = ByteImage.toByte( max );					
				
			}					
			
		}
			
		long tf = System.currentTimeMillis();
		
        if( Utils.debug ) {
        	
        	System.out.println("Tempo de execucao [interpolacao2] "+ ((tf - ti) /1000.0)  + "s");
        	
        }
		
		return interpolation;
				
	}
		
	/**
	 * Devolve a lista de pixels do contorno do pixel de referencia, se o pixel não é um contorno do grid. 
	 * @param p => pixel de referencia
	 * @return pixel do contorno e
	 */
    public Iterable<Integer> getBoundaries( int p ) {
    	
    	final int x = p % interpWidth;
    	final int y = p / interpWidth;
    	
        return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private int i = 0;
					public boolean hasNext() {
						while( i < px.length ) {
							int xx = px[i] + x;
							int yy = py[i] + y;							
				        	if(xx >= 0 && xx < interpWidth && yy >= 0 && yy < interpHeight)
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
	 *  
	 *  
	 *  Parity validation 
	 *  
	 *
	 **/
    
    public boolean isValidCoord( int x, int y ) {
    	
    	return x >= 0 && y >= 0 && x < interpWidth && y < interpHeight;
    	
    }
    
    public int isConnectedToReal2face( int e ) {
    	
    	int x = e%interpWidth, y = e/interpWidth;
    	
    	if( isHorizontalRect( x, y ) ) { 
    		
    		if( ( isValidCoord( x, y+1 ) && isRealPixel( x, y+1 ) ) || 
    			( isValidCoord( x, y-1 ) && isRealPixel( x, y-1 ) ) ) {
    			
    			if( isValidCoord( x, y-1 ) && isValidCoord( x, y+1 ) ) {
    				
    				if( imgU[ x + ( (y-1) * interpWidth ) ] == imgU[ x + ( (y+1) * interpWidth ) ] ) {
    					
    					return 2;
    					
    				}
    				
    			}
    			
    			return 1;
    			
    		}
    		
    	} else if( isVerticalRect( x, y ) ) {
    		
    		if( ( isValidCoord( x+1, y ) && isRealPixel( x+1, y ) ) || 
    			( isValidCoord( x-1, y ) && isRealPixel( x-1, y ) ) ) {
    			
    			if( isValidCoord( x+1, y ) && isValidCoord( x-1, y ) ) {
    				
    				if( imgU[ (x+1) + y * interpWidth ] == imgU[ (x-1) + y * interpWidth ] ) {
    					
    					return 2;
    					
    				}
    				
    			}
    			
    			return 1;
    			
    		}
    		
    	}
    	
    	return 0;
    	
    }
	
    public boolean isFace2( int p ) {
    	
    	return isRealPixel( p );
    	
    }
    
    public boolean isFace2( int x, int y ) {
    	
    	return isRealPixel( x, y );
    	
    }
    
	public boolean isRealPixel( int p ) {
		
		return isRealPixel( p%interpWidth, p/interpWidth );
		
	}
	
	public boolean isRealPixel( int x, int y ) {
		
		return ( x%4 == 1 && y%4 == 1 );
		
	}
	
	public boolean isFakePixelSameLine( int p ) {
		
		return isFakePixelSameLine( p%interpWidth, p/interpWidth );
		
	} 
	
	public boolean isFakePixelSameLine( int x, int y ) {
		
		return !( x % 4 == 1 ) && ( x%2 == 1 && y%2 == 1 && y%4 == 1 );
		
	}
	
	public boolean isFakePixelNextLine( int p ) {
		
		return isFakePixelNextLine( p%interpWidth, p/interpWidth );
		
	}
	
	public boolean isFakePixelNextLine( int x, int y ) {
		
		return ( x%2 == 1 && ( y-1 )%2 == 0 ) && !( y % 4 == 1 );
		
	}
	
	public boolean isCircle( int x, int y ) {
		
		return ( x%2 == 0 && y%2 == 0 );
	}
	
	public boolean isFace1( int p ) {
		
		return isHorizontalRect( p ) || isVerticalRect( p );
		
	}
	
	public boolean isFace1( int x, int y ) {
		
		return isHorizontalRect( x, y ) || isVerticalRect( x, y );
		
	}

	public boolean isHorizontalRect( int p ) {
		
		return isHorizontalRect( p%interpWidth, p/interpWidth );
		
	}
	
	public boolean isVerticalRect( int p ) { 
		
		return isVerticalRect( p%interpWidth, p/interpWidth );
		
	}
	
	public boolean isHorizontalRect( int x, int y ) {
		
		return ( x%2 == 1 && y%2 == 0 );
		
	}
	
	public boolean isVerticalRect( int x, int y ) { 
		
		return ( x%2 == 0 && y%2 == 1 );
		
	}
	
	/**
	 * Ordena a imagem interpolada. 
 	 * Os pixels ordernados estao no vetor imgR[] e os nivel de cinza dos pixels estao no vetor imgU[]. 
 	 * Ordencao crescente dos pixels: imgR[lenght-1], imgR[lenght-2], ..., imgR[0]   
 	 * Ordencao decrescente dos pixels: imgR[0], imgR[1], ..., imgR[lenght-1]
	 * @param interpolation => pixels da imagem interpolada
	 */
	
	public void sort( byte interpolation[][] ) {
		
		long ti = System.currentTimeMillis();
		
  		//Algoritmo de ordenacao do paper
		
		int size = interpWidth * interpHeight;
		
  		PriorityQueueToS queue = new PriorityQueueToS();
		
  		boolean dejavu[] = new boolean[ size ]; //dejavu eh inicializado com false
  		
  		this.imgR = new int[ size ];
  		
  		this.imgU = new byte[ size ];	
  		
  		int pInfinito = 0;
  		
  		queue.initial( pInfinito, this.median );
  		
  		dejavu[ pInfinito ] = true;
  		
  		int i = 0;
  		
  		while( !queue.isEmpty() ) {
  			
  			int h = queue.priorityPop();
  			
  			imgU[ h ] =  ByteImage.toByte( queue.getCurrentPriority() ); //l = prioridade corrente da queue
  			
  			imgR[ i ] = h;
  			
  			for( int n: getAdjPixels( h, dejavu ) ){
  				
  				queue.priorityPush( n, ByteImage.toInt( interpolation[ n ][ 0 ] ), ByteImage.toInt( interpolation [ n ][ 1 ] ) );
  				
  				dejavu[ n ] = true;
  				
  			}
  			
  			i++;
  			
  		}
  		
  		long tf = System.currentTimeMillis();
  		if( Utils.debug )
  			System.out.println( "Tempo de execucao [sort] "+ ((tf - ti) /1000.0)  + "s" );
	}
	
	/** 
	 * 
	 *  Pós-processamento da tree-of-shapes para obter o pixel canonico  
	 *  de cada Shape.
	 *  
	 **/
	
	public void posProcessing() {
		
		shapes = new ArrayList<>();
		
		shapes.add( imgR[ 0 ] );
		
		for( int i = 1 ; i < imgR.length ; i++ ) {
			
			int p = imgR[ i ];
			
			if( imgU[ p ] != imgU[ parent[ p ] ] ) {
				
				shapes.add( p );		
				
			}
			
			if( imgU[ p ] == imgU[ parent[ p ] ] ) {
				
		//		area[ p ] = area[ parent[ p ] ];
				
				//contourLength[ p ] = contourLength[ parent[ p ] ];
				
			//	sumGrad[ p ] = sumGrad[ parent[ p ] ];
				
			//	sumGray[ p ] = sumGray[ parent[ p ] ];
				
			}
			
		}
		
	}
	
	/**
	 * 
	 *   Calculo do atributo de energia
	 *   @return Atributo de energia A 
	 * 
	 * */

	public int [] computeAttribute() {
		
		ArrayList<Integer> Ch[] = new ArrayList[ imgR.length ];
		
		int areaR [] = new int[ imgR.length ];
		
		int sumGrayR[] = new int[ imgR.length ];
		
		int aDel[] = new int[ imgR.length ]; // $ \mathcal{A}_{\nabla} $
		
		/* 
		 * 
		 *  Atributo $ \mathcal{A}_{\lambda_s} $ do artigo
		 * 
		 *  Hierarchical image simplification and segmentation based on Mumford-Shah-salient level line selection
		 *  
		 *  Yongchao Xu, Thierry Géraud, Laurent Najman
		 * 
		 * */
		
		int A[] = new int[ imgR.length ];
		
		for( int i = 0 ; i < shapes.size() ; i++ ) {
			
			int t = shapes.get( i );
			
			areaR[ t ] = area[ t ];
			
			sumGrayR[ t ] = sumGray[ t ];
			
			if( t != parent[ t ] ) {
				
				if( Ch[ parent[ t ] ] == null ) {
					
					Ch[ parent[ t ] ] = new ArrayList<Integer>();
					
				}
				
				Ch[ parent[ t ] ].add( t );
				
			}
			
		}
		
		/*for( int i = 0 ; i < Ch.length ; i++ ) {
			
			int t = shapes.get( i );
			
			System.out.println( Ch[ t ] );
			
		}*/
		
		for( int i = 0 ; i < shapes.size() ; i++ ) {
			
			int t = shapes.get( i );
			
			if( t != parent[ t ] ) {
				
				areaR[ parent[ t ] ] = areaR[ parent[ t ] ] - area[ t ];
				
				sumGrayR[ parent[ t ] ] = sumGrayR[ parent[ t ] ] - sumGray[ t ];
				
			}
			
		}
		
		/*for( int i = 0 ; i < shapes.size() ; i++ ) {
			
			int t = shapes.get( i );
				
			System.out.println( contourLength[ t ] );
				
		}*/
		
		for( int i = 0 ; i < shapes.size() ; i++ ) {
			
			int t = shapes.get( i );
		
			aDel[ t ] = sumGrad[ t ] / contourLength[ t ];
					
			int tp = parent[ t ];
			
			A[ t ] = ( int ) ( ( pow2( sumGrayR[ t ] ) / areaR[ t ] ) + ( pow2( sumGrayR[ tp ] ) / areaR[ tp ] ) - ( pow2( sumGrayR[ t ] + sumGrayR[ tp ] ) / ( areaR[ t ] + areaR[ tp ] )  ) ) / contourLength[ t ];
			
		}			
		
		//int Rt [] = sortNodes( aDel );
		
		int Rt [] = null;
		
		/*for( int i = 0 ; i < Rt.length ; i++ ) {
			
			int t = Rt[ i ];
				
			System.out.println( t + " " + A[ t ] + " " + aDel[ i ] );
				
		}*/
		
		for( int i = 0 ; i < Rt.length ; i++ ) {
			
			int t = Rt[ i ];
			
			int tp = parent[ t ];
			
			int a = ( int ) ( ( pow2( sumGrayR[ t ] ) / areaR[ t ] ) + ( pow2( sumGrayR[ tp ] ) / areaR[ tp ] ) - ( pow2( sumGrayR[ t ] + sumGrayR[ tp ] ) / ( areaR[ t ] + areaR[ tp ] )  ) ) / contourLength[ t ];
			
			if( a > A[ t ] ) {
				
				A[ t ] = a;
				
			}
			
			if( !Ch[ tp ].isEmpty() ) {
			
				Ch[ tp ].remove( Ch[ tp ].indexOf( t ) );
				
			}	
			
			for( Integer tc : getChildren( t, Ch ) ) {										
				
				parent[ tc ] = tp;					
				
				Ch[ tp ].add( tc );
								
			}
			
			areaR[ tp ] = areaR[ tp ] + areaR[ t ];
			
			sumGrayR[ tp ] = sumGrayR[ tp ] + sumGrayR[ t ]; 
			
		}
			
		/*for( int i = 0 ; i < Rt.length ; i++ ) {
				
			int t = Rt[ i ];
				
			System.out.println( t + " " + A[ t ] + " " + aDel[ i ] );
				
		}*/
		
		return A;
		
	}
	
	/**
	 * Devolve todos os shapes filhos de um dado shape t 
	 * @param p => pixel de referencia
	 * @param Ch => ArrayList com os filhos de t
	 * @return filhos de t em Ch
	 */
    public Iterable<Integer> getChildren( final int t, final ArrayList<Integer> Ch [] ) {

        return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private int i = 0;
					public boolean hasNext() {								
			        	if( Ch[t] != null && i < Ch[ t ].size() )
							return true;				    
						return false;
					}
					public Integer next() {
						int son = Ch[ t ].get( i );
						i++;
						return son;
					}
					public void remove() { }
					
				};
			}
		};
    }
	
	/** 
	 * 
	 *  Ordena os nós da tree-of-shapes baseado na
	 *  
	 *  Magnitude do gradiente ao longo do contorno de cada shape.
	 *  
	 *  aDel = $ \mathcal{A}_{\nabla} $ 
	 *  
	 * */    
	
	/*int [] sortNodes( int [] aDel ) {
		
		int [] Rt = new int[ shapes.size() ];
		
		int maxPriority = aDel[ 0 ];
		
		for( int i = 1 ; i < aDel.length ; i++ ) {
			
			if( maxPriority < aDel[ i ] ) {
				
				maxPriority = aDel[ i ];
				
			}
			
		}
		
		PriorityQueueDial queue = new PriorityQueueDial( aDel, maxPriority, PriorityQueueDial.FIFO, false );

		for( int i = 0 ; i < shapes.size() ; i++ ) {
			
			queue.add( shapes.get( i ), aDel[ shapes.get( i ) ] );
			
		}
		
		int i = 0;
		
		while( !queue.isEmpty() ) {
			
			Rt[ i++ ] = queue.remove();
			
		}
		
		return Rt;
		
	}*/
	
	double pow2( int a ) {
		
		return a*a;
		
	}
	
	 /* End modifications */
	
	/**
	 * Interpola os pixels da imagem. 
	 * @param matrix => pixels da imagem 
	 * @return pixels interpolados.
	 */
	
	public Object[] interpolateImageParallel(final GrayScaleImage img) {
		
		long ti = System.currentTimeMillis();
		
        //para inicializar a interpolacao para depois usar max e min
        short maxValue = 257, minValue = -1; 

		this.interpWidth = (img.getWidth()*4-3);
        this.interpHeight = (img.getHeight()*4-3);
        final short interpolationMin[] = new short[interpWidth * interpHeight];
        final short interpolationMax[] = new short[interpWidth * interpHeight];
        final boolean infiniteMax[] = new boolean[interpWidth * interpHeight];
        final boolean infiniteMin[] = new boolean[interpWidth * interpHeight];

        //interpolation tem tamanho 4 vezes para x e y
        // tem 2 valores ( 0 -> menor e 1 -> maior )
        
        int px, py;
        for(int p=0; p < interpolationMin.length; p++){
        	px = p % interpWidth;
			py = p / interpWidth;
			if(px % 4 == 0 && py % 4 == 0){
				interpolationMin[p] = interpolationMax[p] = (short) img.getPixel((px/4) + (py/4) * img.getWidth());
				infiniteMax[p] = true;
	        	infiniteMin[p] = true;
			}else{
				interpolationMin[p] = maxValue;
	        	interpolationMax[p] = minValue;
			}	
        }
       
        
        class ThreadFace implements Runnable{
        	
        	int mBegin, mEnd;
        	
        	int cont = 1;
        	
        	public ThreadFace(int face){
        		if(face == 0){ //0-face m=[12 ate 15]
        			mBegin = 12; mEnd = 15;
        		}
        		else if(face == 1){ //1-face m=[4 ate 11]
        			mBegin = 4; mEnd = 11;
        		}
        		else if(face == 2){ //2-face m=[0 ate  3]
        			mBegin = 0; mEnd = 3;
        		}
        	}
	        	
        	public void run(){
        		
        		int px, py, vx, vy, pixelP, pixelV;
        		
        		for (int face = mBegin; face <= mEnd; face++) { // para todos os moves
        			
        			for (int y = 0; y < img.getHeight(); y++) { //para todos os pixels
        				
        				for (int x = 0; x < img.getWidth(); x++) { 
        					
        					px = x * 4 + moves[face][1]; // em x
        					py = y * 4 + moves[face][0]; // em y
	                            
        					// check bounds!!
        					if (py < 0 || py >= interpHeight || px < 0 || px >= interpWidth)
        						continue;
	                            
        					pixelP = px + py * interpWidth;
	                            
        					for (int k = 0; k < vizinhos[face].length; k++) {
        						
        						vx = px + vizinhos[face][k][1]; 
        						vy = py + vizinhos[face][k][0]; 
	                                   
        						// check bounds!!
        						if (vy < 0 || vy >= interpHeight || vx < 0 || vx >= interpWidth)
        							continue;
	                                    
        						pixelV = vx + vy * interpWidth;
        						
        						if (face >= 1 && face <= 3) { //2-face // para m = 1 ate m = 3 se tem o maximo para os 2 valores
        							if ( (!infiniteMax[pixelP] && infiniteMax[pixelV]) || (interpolationMax[pixelP] < interpolationMax[pixelV])) {
        								interpolationMin[pixelP] = interpolationMin[pixelV];
        								interpolationMax[pixelP] = interpolationMax[pixelV];
        								infiniteMax[pixelV] = true;
        								infiniteMax[pixelP] = true;
        							}
        						}
        						else { //m = [4 .. 15], maximo e minimo  
        							if ( (!infiniteMin[pixelP] && infiniteMin[pixelV])  || (interpolationMin[pixelP] > interpolationMin[pixelV])){
        								interpolationMin[pixelP] = interpolationMin[pixelV];
        								infiniteMax[pixelV] = true;
        								infiniteMax[pixelP] = true;
        							}
        							
        							if ( (!infiniteMax[pixelP] && infiniteMax[pixelV]) || (interpolationMax[pixelP] < interpolationMax[pixelV])){
        								interpolationMax[pixelP] = interpolationMax[pixelV];
        								infiniteMax[pixelV] = true;
        								infiniteMax[pixelP] = true;
        							}
        						}
        					}
        				}
        			}
        		}
	               
        	}
        }
        new ThreadFace(2).run(); //processa o 2-face
        
        //paralelisa 0-face e 1-face
        final Thread[] threads = new Thread[2]; 
        for(int i=0; i < threads.length; i++){
        	threads[i] = new Thread(new ThreadFace(i));
        	threads[i].setPriority(Thread.currentThread().getPriority());
        	threads[i].start();
        }
        for (final Thread thread : threads){
			try {
				if (thread != null) 
					thread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();	  //keep interrupted status (PlugInFilterRunner needs it)
			}
		}
		
        long tf = System.currentTimeMillis();
        if(Utils.debug)
        	System.out.println("Tempo de execucao [interpolacao parallel] "+ ((tf - ti) /1000.0)  + "s");
        return new Object[]{interpolationMin, interpolationMax};
	}
    
    public static void main(String[] args) {
    	
		long ti = System.currentTimeMillis();
		
		int pixels5 [] = new int[]{
				
				/*4, 4, 4, 4, 4, 4,
				4, 1, 1, 7, 7, 4,
				4, 1, 4, 4, 7, 4,
				4, 1, 1, 7, 7, 4,
				4, 4, 4, 4, 4, 4*/
				
				/*1,2,3,
				6,1,6,
				7,3,9*/ //			
				
				/*5,5,5,
				5,2,5,
				5,5,5*/
				
				/*1,1,1,1,1,1,
				1,0,0,3,3,1,
				1,0,1,1,3,1,
				1,0,0,3,3,1,
				1,1,1,1,1,1*/ // ok
				
				1,1,1,1,1,
				1,3,0,0,1,
				1,3,0,3,1,
				1,3,0,3,1,
				1,1,1,1,1
				
				/*5,5,5,5,
				5,2,2,5,
				5,2,2,5,
				5,5,5,5*/
				
				/*1,1,1,1,1,1,1,
				1,0,0,3,3,3,1,
				1,0,1,1,2,2,1,
				1,0,0,3,3,3,1,
				1,1,1,1,1,1,1*/
				
				/*24,24,24,24,24,24,
				24,24, 0, 0, 0,24,
				24, 0, 6, 8, 0,24,
				24,24, 0, 0,24,24,
				24,24,24,24,24,24*/ // ok
				
				
		};
		
		int width = 5;
		int height = 5;
		
		// Second example of Thierry
		
		GrayScaleImage input = ImageFactory.createGrayScaleImage( ImageFactory.DEPTH_8BITS, 5, 5 );
		
		input.setPixel( 0, 0, 128 );
				
		input.setPixel( 1, 0, 124 );
				
		input.setPixel( 2, 0, 150 );
						
		input.setPixel( 3, 0, 137 );
				
		input.setPixel( 4, 0, 106 );
				
				
		input.setPixel( 0, 1, 116 );
				
		input.setPixel( 1, 1, 128 );
				
		input.setPixel( 2, 1, 156 );
				
		input.setPixel( 3, 1, 165 );
				
		input.setPixel( 4, 1, 117 );		
				
				
		input.setPixel( 0, 2, 117 );
				
		input.setPixel( 1, 2, 90 );
				
		input.setPixel( 2, 2, 131 );
				
		input.setPixel( 3, 2, 108 );
				
		input.setPixel( 4, 2, 151 );
				
				
		input.setPixel( 0, 3, 107 );
				
		input.setPixel( 1, 3, 87 );
				
		input.setPixel( 2, 3, 118 );
				
		input.setPixel( 3, 3, 109 );
				
		input.setPixel( 4, 3, 167 );
				
				
		input.setPixel( 0, 4, 107 );
				
		input.setPixel( 1, 4, 73 );
				
		input.setPixel( 2, 4, 125 );
				
		input.setPixel( 3, 4, 157 );
				
		input.setPixel( 4, 4, 117 );
				
		
		BuilderTreeOfShapeByUnionFindParallel build = new BuilderTreeOfShapeByUnionFindParallel( ImageBuilder.openGrayImage(), false );
		
		//BuilderTreeOfShapeByUnionFindParallel build = new BuilderTreeOfShapeByUnionFindParallel( ImageFactory.createReferenceGrayScaleImage(ImageFactory.DEPTH_32BITS, pixels5, width, height), false );				
		
		//BuilderTreeOfShapeByUnionFindParallel build = new BuilderTreeOfShapeByUnionFindParallel( input, false );
		
		
		ConnectedFilteringByTreeOfShape filtering = new ConnectedFilteringByTreeOfShape( build );		
				
		filtering.ComputerXuAttribute();	
		
		
		/*int appear [] = ComputerTosContourInformation.appear;
		
		int vanish [] = ComputerTosContourInformation.vanish;
		
		
		for( int i = 0 ; i < build.getInputImage().getSize() ; i++ ) {
			
			if( build.isFace1( i ) ) {
				
				int na = appear[ i ], nv = vanish[ i ];
				
				while( na != nv ) {
					
					//System.out.println( "equals" );
					
					na = build.parent[ na ];
					
				}
				
			}
			
		}*/					
		
        /*NodeToS root = build.getRoot();                       
		
		System.out.println("\n**********************ARVORE***********************");
		
		printTree( root, System.out, "<-" );
		
		System.out.println("***************************************************\n");*/		
        
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao  "+ ((tf - ti) /1000.0)  + "s");		

	}
    
    public static void exploreTree( NodeToS no ) {						
			
		for( NodeToS son: no.children ) {
				
			exploreTree( son );
						
		}
		
	}
    
	public static void printTree( NodeToS no, PrintStream out, String s ) {
			
		out.printf(s + "[%3d; %.2f]\n", no.getId(), no.getAttributeValue( Attribute.SUM_GRAD_CONTOUR ) );		
		
		/*if( no.getAttributeValue( Attribute.CONTOUR_LENGTH ) <= 0 ) {
			
			System.out.println( "wrong " + no.getId() );
			
		}*/
		
		//if( no.children != null )
			
			for( NodeToS son: no.children ) {
				
				printTree( son, out, s + "------" );
				
			}
	}
	
}