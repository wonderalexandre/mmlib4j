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
	private boolean isLog = true;
	private int xInfinito;
	private int yInfinito;
	
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
	
	protected BuilderTreeOfShapeByUnionFind(){ }
	
	public BuilderTreeOfShapeByUnionFind getClone(){
		
		BuilderTreeOfShapeByUnionFind b = new BuilderTreeOfShapeByUnionFind();
		b.interpWidth = this.interpWidth;
		b.interpHeight = this.interpHeight;
		b.imgWidth = this.imgWidth;
		b.imgHeight = this.imgHeight;
		b.parent = this.parent;
		b.isLog = this.isLog;
		b.xInfinito = this.xInfinito;
		b.yInfinito = this.yInfinito;
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
		this.xInfinito = xInfinito;
		this.yInfinito = yInfinito;
		
		sort( interpolateImage( ) );
		this.parent = createTreeByUnionFind();
		if( isInter )
			unInterpolateAndCreateTree( parent );
		else
			createTree( parent );
		
		posProcessing();
		
		//computeAttribute();
	
		this.img = getImageInterpolated();
		this.imgWidth = img.getWidth();
		this.imgHeight = img.getHeight();
			
	}
	
	/**
	 * Construcao da arvore de formas
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
	public void createTree( int parent[]  ) {
		
		long ti = System.currentTimeMillis();
		
		this.numNode = 0;
		
		NodeToS nodesMapTmp[] = new NodeToS[ parent.length ];
		
		for (int i = 0; i < imgR.length; i++) {
			int p = imgR[i];
			int pai = parent[p];
			
			if(p == pai){ //Note que:  p = pInfinito
				this.root = nodesMapTmp[p] = new NodeToS(numNode++, imgU[p], img, p);
				nodesMapTmp[p].addPixel( p );
				continue;
			}
			
			if(nodesMapTmp[pai] == null) {
				nodesMapTmp[pai] = new NodeToS(numNode++, imgU[pai], img, p);
			}
			if(nodesMapTmp[pai].children == null)	
				nodesMapTmp[pai].children = new LinkedList<NodeToS>();
			 
			if(imgU[p] != imgU[pai]){ //novo no
				if(nodesMapTmp[p] == null){
					nodesMapTmp[p] = new NodeToS(numNode++, imgU[p], img, p);
				}
				nodesMapTmp[p].parent = nodesMapTmp[pai];
				nodesMapTmp[pai].children.add(nodesMapTmp[p]);	
			}else{ 
				//mesmo no
				nodesMapTmp[p] = nodesMapTmp[pai];
			}
			nodesMapTmp[p].addPixel( p );
			
		}
		
		nodesMapTmp = null;
		//parent = null;
		//imgU = null;
		//imgR = null;
		
		long tf = System.currentTimeMillis();
        if(isLog)
        	System.out.println("Tempo de execucao [unInterpolate2] "+ ((tf - ti) /1000.0)  + "s");
		
	}
	
	/**
	 * Desinterpolacao da arvore e a transforma em uma arvore de estrutura ligada. 
	 * @return raiz da arvore
	 */
	
	public void unInterpolateAndCreateTree( int parent[]  ) {
		
		long ti = System.currentTimeMillis();
		
		this.numNode = 0;
		
		NodeToS nodesMapTmp[] = new NodeToS[parent.length];
		
		//WindowImages.show(ImageFactory.createGrayScaleImage(32, sumGradBoundary, interpWidth, interpHeight));
		
		for (int i = 0; i < imgR.length; i++) {
			
			int p = imgR[i];
			int pai = parent[p];
			int x = p % interpWidth;
			int y = p / interpWidth;
			int pixelUnterpolate = (x/2) + (y/2) * imgWidth;
			
			if(p == pai){ //Note que: p = pInfinito
				
				this.root = nodesMapTmp[p] = new NodeToS(numNode++, imgU[p], img, pixelUnterpolate);
				
				if(x % 2 == 1 && y % 2 == 1){
					nodesMapTmp[p].addPixel( pixelUnterpolate );
					
				}
				continue;
			}
			
			if(nodesMapTmp[pai] == null) {
				nodesMapTmp[pai] = new NodeToS(numNode++, imgU[pai], img, pixelUnterpolate);
			}
			if(nodesMapTmp[pai].children == null)	
				nodesMapTmp[pai].children = new LinkedList<NodeToS>();
			 
			if(imgU[p] != imgU[pai]){ //novo no
				if(nodesMapTmp[p] == null){
					nodesMapTmp[p] = new NodeToS(numNode++, imgU[p], img, pixelUnterpolate);
				}
				nodesMapTmp[p].parent = nodesMapTmp[pai];
				nodesMapTmp[pai].children.add(nodesMapTmp[p]);	
			}else{ 
				//mesmo no
				nodesMapTmp[p] = nodesMapTmp[pai];
			}
			
			if(x % 2 == 1 && y % 2 == 1){
				nodesMapTmp[p].addPixel( pixelUnterpolate );
				
			}
		}
		
		nodesMapTmp = null;
		//parent = null;
		//imgU = null;
		//imgR = null;
		
		long tf = System.currentTimeMillis();
        if(isLog)
        	System.out.println("Tempo de execucao [unInterpolate2] "+ ((tf - ti) /1000.0)  + "s");
		
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
		
		
		/* is_boundary ( initialized as false ) */
		
		//boolean [] is_boundary = new boolean[ imgR.length ]; 
		
		boolean [] is_boundary = new boolean[ imgR.length ]; 
		
		
		/* Attributes calculated on region ( area, sumGray ) */
		
		area = new int[ imgR.length ];
		
		sumGray = new int[ imgR.length ];
		
		
		/* Attributes calculated on contour ( sumGrad, appear, vanish, contourLength ) */
		
		imgGrad = EdgeDetectors.sobel( ImageFactory.createReferenceGrayScaleImage( 8, imgU, interpWidth, interpHeight ) );
		
		sumGrad = new int[ imgR.length ];
		
		appear = new int[ imgR.length ];
		
		vanish = new int[ imgR.length ];
		
		contourLength = new int[ imgR.length ];
		
		
		int parent[] = new int[ imgR.length ];
		
		int zPar[] = new int[ imgR.length ];
		
		
		for ( int p = 0; p < imgR.length; p++ ) {
			
			
			zPar[ p ] =  NIL;
			
			
			area[ p ] = 0;
			
			sumGray[ p ] = 0;
			
			contourLength[ p ] = 0;
			
			sumGrad[ p ] = 0;		
			
			
		}
		
		for( int i = imgR.length-1; i >= 0; i-- ) {
			
			
			int p = imgR[ i ];
			
			parent[ p ] = p;
			
			zPar[ p ] = p;
		
			
			area[ p ] = 1;
				
			sumGray[ p ] = ByteImage.toInt( imgU[ p ] );
			
			
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
			
			/* Attributes calculated using contour information */					
				
			for( Integer e : getBoundaries( p ) ) {			
					
				if( !is_boundary[ e ] ) {
						
					is_boundary[ e ] = true;
					
					contourLength[ p ] = contourLength[ p ] + 1;
						
					sumGrad[ p ] = sumGrad[ p ] + imgGrad.getPixel( e ); 
					
					appear[ e ] = p;
						
				} else {					
					
					/*if( imgU[ is_boundary[ e ] ] == imgU[ p ] ) {
						
						contourLength[ p ] = contourLength[ p ] - 1;
						
					}*/	
					
					is_boundary[ e ] = false;
					
					contourLength[ p ] = contourLength[ p ] - 1;
						
					sumGrad[ p ] = sumGrad[ p ] - imgGrad.getPixel( e );
					
					vanish[ e ] = p;
						
				}
					
			}	
			
		}
		
		// canonizacao da arvore
		
		for ( int i = 0; i < imgR.length; i++ ) {
			
			int p = imgR[ i ];
			
			int q = parent[ p ];
			
			if( imgU[ parent[ q ] ] == imgU[ q ] ){
				
				parent[ p ] = parent[ q ];
				
			}
			
		}
		
		zPar = null;
		
		long tf = System.currentTimeMillis();
        if(isLog)
        	System.out.println("Tempo de execucao [union-find] "+ ((tf - ti) /1000.0)  + "s");
		
        return parent;
	}
	
	public boolean isNotBoundarie( int p ) {
		
		int x = p % interpWidth;
		
    	int y = p / interpWidth;
		
		return ( x%2 == 1 && y%2 == 1 );
		
	}
	
	/* 
	 * 
	 * 
	 *  Pós-processamento da tree-of-shapes para obter o pixel canonico  
	 *  de cada Shape.
	 *  
	 *  
	 * */
	
	public void posProcessing() {
		
		shapes = new ArrayList<Integer>();
		
		for( int i = 0 ; i < imgR.length ; i++ ) {
			
			int p = imgR[ i ];
			
			if( p == parent[ p ] || imgU[ p ] != imgU[ parent[ p ] ] ) {
				
				shapes.add( p );		
				
			}
			
			/*if( imgU[ p ] == imgU[ parent[ p ] ] ) {
				
				area[ p ] = area[ parent[ p ] ];
				
				contourLength[ p ] = contourLength[ parent[ p ] ];
				
				sumGrad[ p ] = sumGrad[ parent[ p ] ];
				
				sumGray[ p ] = sumGray[ parent[ p ] ];
				
			}*/
			
		}
		
	}
	
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
	 * Devolve a lista de pixels do contorno do pixel de referencia, se o pixel não é um contorno do grid. 
	 * @param p => pixel de referencia
	 * @return pixel do contorno
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
	
	/* 
	 * 
	 * 
	 *  Ordena os nós da tree-of-shapes baseado na
	 *  
	 *  Magnitude do gradiente ao longo do contorno de cada shape.
	 *  
	 *  aDel = $ \mathcal{A}_{\nabla} $
	 *  
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
		
  		PriorityQueueToS queue = new PriorityQueueToS( );
  		
		int i = 0;
		
		boolean dejavu[] = new boolean[size]; //dejavu eh inicializado com false
		
		this.imgR = new int[size];
		this.imgU = new byte[size];
		
		int pInfinito = 0;//getInfinity(interpolation);
		
		//System.out.println("pInfinito (" + xInfinito + ", " + yInfinito + ")");
		
		queue.initial( pInfinito, this.median );
		
		dejavu[ pInfinito ] = true;
		
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
		if(isLog)
			System.out.println("Tempo de execucao [sort] "+ ((tf - ti) /1000.0)  + "s");
	}
	
	/**
	 * Devolve a lista de pixels do contorno do pixel de referencia, se o pixel não é um contorno do grid. 
	 * @param p => pixel de referencia
	 * @return pixel do contorno
	 */
    public Iterable<Integer> getBoundaries( int p ) {
    	
    	final int x = p % interpWidth;
    	final int y = p / interpWidth;
    	
        return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private int i = 0;
					public boolean hasNext() {
						while( i < px.length && ( x%2 == 1 && y%2 == 1 ) ) {
						//while( i < px.length ) {
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
		this.median = ( pixels[ img.getSize() / 2 - 1 ] + pixels[ img.getSize() / 2 + 1 ] ) / 2;			
		
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
						min = ByteImage.toInt( interpolation[qT][0] );
					}
				}
				else{
					if(this.median > max){
						max = this.median;
					}
					if(this.median < min){
						min = this.median;
					}
				}
			}
			interpolation[pT][0] = ByteImage.toByte(min);
			interpolation[pT][1] = ByteImage.toByte(max);
			
		}
        
        long tf = System.currentTimeMillis();
        if(isLog){
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
		
		
		/*GrayScaleImage input = ImageFactory.createGrayScaleImage( ImageFactory.DEPTH_8BITS, 
																  4, 3 );
																  		
		// First example of Thierry
		
		
		input.setPixel( 0, 0, 0 );
		
		input.setPixel( 1, 0, 0 );
		
		input.setPixel( 2, 0, 3 );
		
		input.setPixel( 3, 0, 2 );
		
		
		
		input.setPixel( 0, 1, 0 );
		
		input.setPixel( 1, 1, 1 );
		
		input.setPixel( 2, 1, 1 );
		
		input.setPixel( 3, 1, 2 );
		
		
		input.setPixel( 0, 2, 0 );		
		
		input.setPixel( 1, 2, 0 );
		
		input.setPixel( 2, 2, 2 );
		
		input.setPixel( 3, 2, 2 );*/
		
		
		// Second example of Thierry
		
		GrayScaleImage input = ImageFactory.createGrayScaleImage( ImageFactory.DEPTH_8BITS, 
				  												  5, 5 );
		
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
			
		
		int example[] = new int[] {
				
			/*5,5,5,5,5,5,
			5,4,4,4,4,5,
			5,4,2,2,4,5,
			5,4,2,2,4,5,
			5,4,4,4,4,5,
			5,5,5,5,5,5*/
				
			/*5,5,5,5,			
			5,2,2,5,
			5,2,2,5,
			5,5,5,5*/
			
			/*1,1,1,1,1,1,
			1,0,0,3,3,1,
			1,0,1,1,3,1,
			1,0,0,3,3,1,
			1,1,1,1,1,1*/
			
			/*5,5,5,
			5,2,5,
			5,5,5*/
				
			/*0,0,0,0,0,0,0,
			0,7,7,7,7,7,0,
			0,7,6,8,6,7,0,
			0,7,6,8,6,7,0,
			0,7,6,8,6,7,0,
			0,7,7,7,7,7,0,
			0,0,0,0,0,0,0,*/
				
			/*5,5,5,5,5,5,5,5,
			5,3,3,3,3,3,3,5,
			5,3,4,4,4,4,3,5,
			5,3,4,0,0,4,3,5,
			5,3,4,0,0,4,3,5,
			5,3,4,4,4,4,3,5,
			5,3,3,3,3,3,3,5,
			5,5,5,5,5,5,5,5*/
				
				5,5,5,5,5,5,5,5,
				5,3,3,3,3,3,3,5,
				5,3,1,1,1,1,3,5,
				5,3,1,0,0,1,3,5,
				5,3,1,0,0,1,3,5,
				5,3,1,1,1,1,3,5,
				5,3,3,3,3,3,3,5,
				5,5,5,5,5,5,5,5
			
			/*0,0,0,0,0,0,0,
			0,4,4,4,7,7,7,
			0,7,7,4,7,4,7,
			0,7,4,4,7,4,7,
			0,4,4,4,7,4,7,
			0,7,7,4,7,7,7,
			0,0,0,0,0,0,0*/
		
		};
		
		int width = 8;
		
		int height = 8;
		
		//
		
		double	volA=183,
			   
				areaA=46; // pai			
		
		
		double  volB=3,
				
				areaB=3; // filho
		
		//
		
		double contour = 8;
		
		//
		
		double ni = 3;
		
		//		
		
		double var = (Math.pow( volA + volB, 2 )/(areaA + areaB)) - ((volA*volA)/areaA) - ((volB*volB)/areaB) + (ni*contour);
		
		System.out.println( "Energy=" + var );
		
		BuilderTreeOfShapeByUnionFind build = new BuilderTreeOfShapeByUnionFind(ImageFactory.createReferenceGrayScaleImage(32, example, width, height), false);
		
		System.out.println("\n**********************ARVORE***********************");
		printTree(build.getRoot(), System.out, "<-");
		System.out.println("\n***************************************************");
		
		//BuilderTreeOfShapeByUnionFind build = new BuilderTreeOfShapeByUnionFind(input, false);
		
		/*System.out.println("imgU");
		
		for( int y = 0 ;  y < build.interpHeight ;  y++ ) {
			
			for( int x = 0 ; x < build.interpWidth ; x++ ) {
				
				int p = ByteImage.toInt( build.imgU[ x + y * build.interpWidth ] );
				
				if( x%2==1 && y%2==1 ) {
					
					System.out.printf( "(%3d,%3d) ", x + y * build.interpWidth, p );
				
				}else{
					
					System.out.printf( "[%3d,%3d] ", x + y * build.interpWidth, p );
					
				}
				
			}
			
			System.out.println();
			
		}
		
		System.out.println("parent");
		
		for( int y = 0 ;  y < build.interpHeight ;  y++ ) {
			
			for( int x = 0 ; x < build.interpWidth ; x++ ) {
				
				int p = build.parent[ x + y * build.interpWidth ];
				
				if( x%2==1 && y%2==1 ) {
					
					System.out.printf( "(%3d,%3d) ", x + y * build.interpWidth, p );
				
				}else{
					
					System.out.printf( "[%3d,%3d] ", x + y * build.interpWidth, p );
					
				}
				
			}
			
			System.out.println();
			
		}
		
		System.out.println( "contour" );
		
		for( int y = 0 ;  y < build.interpHeight ;  y++ ) {
			
			for( int x = 0 ; x < build.interpWidth ; x++ ) {
				
				int p = build.contourLength[ x + y * build.interpWidth ];
				
				if( x%2==1 && y%2==1 ) {
					
					System.out.printf( "(%3d,%3d) ", x + y * build.interpWidth, p );
				
				}else{
					
					System.out.printf( "[%3d,%3d] ", x + y * build.interpWidth, p );
					
				}
				
			}
			
			System.out.println();
			
		}
		
		/*GrayScaleImage img = ImageFactory.createGrayScaleImage( ImageFactory.DEPTH_8BITS, 6 , 5 );
		
		byte [] pixels = new byte [] {
				
			1,1,1,1,1,1,
			1,0,0,3,3,1,
			1,0,1,1,3,1,
			1,0,0,3,3,1,
			1,1,1,1,1,1
				
		};
		
		img.setPixels(6, 5, pixels);
		
		ImageBuilder.saveImage( img, new File("/home/gobber/img.png") );*/
		
		
	}
	
	public static void printTree(NodeToS no, PrintStream out, String s){
		out.printf(s + "[%3d; %3d]\n", no.level, no.getCanonicalPixels().size() );
		if(no.children != null)
			for(NodeToS son: no.children){
				printTree(son, out, s + "------");
			}
	}

}