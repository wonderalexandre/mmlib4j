package mmlib4j.representation.tree.tos;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;

import mmlib4j.datastruct.PriorityQueueToS;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ByteImage;
import mmlib4j.images.impl.ImageFactory;
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
	
	private BuilderTreeOfShapeByUnionFindParallel(){ }
	
		
	
	public BuilderTreeOfShapeByUnionFindParallel getClone(){
		BuilderTreeOfShapeByUnionFindParallel b = new BuilderTreeOfShapeByUnionFindParallel();
		b.interpWidth = this.interpWidth;
		b.interpHeight = this.interpHeight;
		b.parent = this.parent;
		
		b.xInfinito = this.xInfinito;
		b.yInfinito = this.yInfinito;
		b.img = this.img;	
		b.imgR = this.imgR;
		b.imgU = this.imgU;
		
		b.unInterpolateTree( b.parent );
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
	
	
	
	public BuilderTreeOfShapeByUnionFindParallel(GrayScaleImage img, int xInfinito, int yInfinito){
		super();
		this.img = img;
		this.xInfinito = xInfinito;
		this.yInfinito = yInfinito;
        
		
		Object obj[] = interpolateImageParallel( img );
		sort( obj );
		this.parent = createTreeByUnionFind();
		unInterpolateTree( parent );
		
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
	
	/**
	 * Desinterpolacao da arvore e a transforma em uma arvore de estrutura ligada. 
	 * @return raiz da arvore
	 */
	public void unInterpolateTree( int parent[]  ){
		long ti = System.currentTimeMillis();
		this.numNode = 0;
		this.root = null;
		NodeToS nodesMapTmp[] = new NodeToS[parent.length];
		boolean flags[] = new boolean[parent.length];
		//WindowImages.show(ImageFactory.createGrayScaleImage(32, sumGradBoundary, interpWidth, interpHeight));
		
		int p = imgR[0];
		int pai = parent[p];
		int x = p % interpWidth;
		int y = p / interpWidth;
		int pixelUnterpolate = (x/4) + (y/4) * img.getWidth();
		
		if(p == pai){ //Note que:  p = pInfinito
			this.root = nodesMapTmp[p] = new NodeToS(numNode++, ByteImage.toInt(imgU[p]), img, pixelUnterpolate);
			if(x % 4 == 0 && y % 4 == 0){
				nodesMapTmp[p].addPixel( pixelUnterpolate );
			}
		}
		
		
		//paralelisavel
		for (int i = 1; i < imgR.length; i++) {
			
			p = imgR[i];
			pai = parent[p];
			x = p % interpWidth;
			y = p / interpWidth;
			pixelUnterpolate = (x/4) + (y/4) * img.getWidth();	
			
			if(imgU[p] != imgU[pai]){ //novo no
				
				if(nodesMapTmp[p] == null){
					nodesMapTmp[p] = new NodeToS(numNode++, ByteImage.toInt(imgU[p]), img, pixelUnterpolate);
				}	
			
				if(nodesMapTmp[pai] == null){
					int xPai = pai % interpWidth;
					int yPai = pai / interpWidth;
					int pixelUnterpolatePai = (xPai/4) + (yPai/4) * img.getWidth();	
					int paiPai = parent[pai];
					nodesMapTmp[pixelUnterpolatePai] = new NodeToS(numNode++, ByteImage.toInt(imgU[paiPai]), img, pixelUnterpolatePai);
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
					int pixelUnterpolatePai = (xPai/4) + (yPai/4) * img.getWidth();	
					int paiPai = parent[pai];
					nodesMapTmp[pixelUnterpolatePai] = new NodeToS(numNode++, ByteImage.toInt(imgU[paiPai]), img, pixelUnterpolatePai);
				}
				
				nodesMapTmp[p] = nodesMapTmp[pai];
			}
			
			if(x % 4 == 0 && y % 4 == 0){
				nodesMapTmp[p].addPixel( pixelUnterpolate );
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
						nodesMap[p] = new NodeToS(numNode++, imgU[p], img, p);	
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
			int pai = parent[p];
			if(imgU[parent[pai]] == imgU[pai]){
				parent[p] = parent[pai];
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
  		
  		/*System.out.println("imgR");
		System.out.println("[nivel de cinza; pixel; parent]");
  		for(int y=0; y < interpHeight; y++){
			for(int x=0; x < interpWidth; x++){
				int pixel = x + y * interpWidth;
				//System.out.printf("(%2d, %2d) ", find(imgR, pixel), imgRZP[pixel]);
				System.out.printf("%3d & ", findNivel(imgR, pixel));
				
			}
			System.out.println();
		}
		
  		System.out.println("\n\nimgU\n");
  		for(int y=0; y < interpHeight; y++){
			for(int x=0; x < interpWidth; x++){
				int pmax = imgU[x + y * interpWidth];
				System.out.printf("%2d &", pmax);
			}
			System.out.println();
		}*/
  		
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
		
		int pixels5[] = new int[]{
				4, 4, 4, 4, 4, 4, 4, 4,
				4, 0, 0, 0, 7, 7, 7, 4,
				4, 0, 0, 0, 7, 7, 7, 4,
				4, 0, 0, 4, 4, 7, 7, 4,
				4, 0, 0, 0, 7, 7, 7, 4,
				4, 0, 0, 0, 7, 7, 7, 4,
				4, 4, 4, 4, 4, 4, 4, 4
				
		};
		
		int width = 8;
		int height = 7;
		
		pixels5 = new int[]{
				4, 4, 4, 4, 4, 4,
				4, 1, 1, 7, 7, 4,
				4, 1, 4, 4, 7, 4,
				4, 1, 1, 7, 7, 4,
				4, 4, 4, 4, 4, 4
				
		};
		width = 6;
		height = 5;
		//int width = 5;
		//int height = 2;
		
		System.out.println("[nivel de cinza; pixel; parent]");
		for(int y=0; y < height; y++){
			for(int x=0; x < width; x++){
				int p = pixels5[x + y * width];
				System.out.printf("[%d]", p);
				
			}
			System.out.println();
		}
		GrayScaleImage img = ImageFactory.createReferenceGrayScaleImage(32, pixels5, width, height);
		
		BuilderTreeOfShapeByUnionFindParallel builder = new BuilderTreeOfShapeByUnionFindParallel(ImageBuilder.openGrayImage());
		if(true) return;
		Object inter[] = (Object[]) BuilderTreeOfShapeByUnionFindParallel.getImageInterpolate(img);
		
		int interpWidth = (img.getWidth()*4-3);
        int interpHeight = (img.getHeight()*4-3);
        final short interpolation0[] = (short[]) inter[0];
        final short interpolation1[] = (short[]) inter[1];
		
        for(int y=0; y < interpHeight; y++){
			for(int x=0; x < interpWidth; x++){
				int pmax = interpolation1[x + y * interpWidth];
				int pmin = interpolation0[x + y * interpWidth];
				if(pmin == pmax){
					if(x % 4==0 && y % 4 ==0)
						System.out.printf(" %3d &", pmin);
					else if(x % 2==0 && y % 2 ==0)
						System.out.printf("  %d%d &", pmin,pmin);
					else 
						System.out.printf(" %d%d%d &", pmin,pmin,pmin);
					
				}
				else
					System.out.printf("  %d%d &", pmax, pmin);
				
			}
			System.out.println();
		}
        
        NodeToS root = builder.getRoot();
		
		System.out.println("\n**********************ARVORE***********************");
		printTree(root, System.out, "<-");
		System.out.println("***************************************************\n");
		
		
        
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao  "+ ((tf - ti) /1000.0)  + "s");
		
		

	}
    

	public static void printTree(NodeToS no, PrintStream out, String s){
		out.printf(s + "[%3d; %d]\n", no.getLevel(), no.getCanonicalPixels().size());
		if(no.children != null)
			for(NodeToS son: no.children){
				printTree(son, out, s + "------");
			}
	}


	
}