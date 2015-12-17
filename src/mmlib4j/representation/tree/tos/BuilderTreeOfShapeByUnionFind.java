package mmlib4j.representation.tree.tos;

import java.util.Iterator;
import java.util.LinkedList;

import mmlib4j.datastruct.PriorityQueueToS;
import mmlib4j.images.GrayScaleImage;


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
	private short imgU[];
	private int parent[];
	private int numNode; 
	private boolean isLog = true;
	private int xInfinito;
	private int yInfinito;
	
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
		b.xInfinito = this.xInfinito;
		b.yInfinito = this.yInfinito;
		b.img = this.img;	
		b.imgR = this.imgR;
		b.imgU = this.imgU;
		
		b.unInterpolateTree( b.parent );
		return b;
	}
	
	
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
	
	
	public BuilderTreeOfShapeByUnionFind(GrayScaleImage img){
		this(img, -1, -1);
	}
	
	public BuilderTreeOfShapeByUnionFind(GrayScaleImage img, int xInfinito, int yInfinito){
		this.imgWidth = img.getWidth();
		this.imgHeight = img.getHeight();
		this.img = img;
		this.xInfinito = xInfinito;
		this.yInfinito = yInfinito;

		sort( interpolateImage( ) );
		this.parent = createTreeByUnionFind();
		unInterpolateTree( parent );
		
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
		NodeToS nodesMapTmp[] = new NodeToS[parent.length];
		//WindowImages.show(ImageFactory.createGrayScaleImage(32, sumGradBoundary, interpWidth, interpHeight)); 
		for (int i = 0; i < imgR.length; i++) {
			int p = imgR[i];
			int pai = parent[p];
			int x = p % interpWidth;
			int y = p / interpWidth;
			int pixelUnterpolate = (x/4) + (y/4) * imgWidth;
			
			if(p == pai){ //Note que:  p = pInfinito
				this.root = nodesMapTmp[p] = new NodeToS(numNode++, imgU[p], img, pixelUnterpolate);
				if(x % 4 == 0 && y % 4 == 0){
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
			
			if(x % 4 == 0 && y % 4 == 0){
				nodesMapTmp[p].addPixel( pixelUnterpolate );
				
			}
		}
		
		nodesMapTmp = null;
		//parent = null;
		//imgU = null;
		//imgR = null;
		
		long tf = System.currentTimeMillis();
        if(isLog)
        	System.out.println("Tempo de execucao [unInterpolate] "+ ((tf - ti) /1000.0)  + "s");
		
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
			if(imgU[parent[q]] == imgU[q]){
				parent[p] = parent[q];
			}
		}
		zPar = null;
		
		long tf = System.currentTimeMillis();
        if(isLog)
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
	public void sort(int interpolation[][]){
		long ti = System.currentTimeMillis();
  		//Algoritmo de ordenacao do paper
		int size = interpWidth * interpHeight;
  		PriorityQueueToS queue = new PriorityQueueToS( );
		int i = 0;
		boolean dejavu[] = new boolean[size]; //dejavu eh inicializado com false
		this.imgR = new int[size];
		this.imgU = new short[size];
		int pInfinito = getInfinity(interpolation); 
		//System.out.println("pInfinito (" + xInfinito + ", " + yInfinito + ")");
		queue.initial(pInfinito, interpolation[pInfinito][0]);
		dejavu[pInfinito] = true;
		while(!queue.isEmpty()){
			int h = queue.priorityPop();
			imgU[h] = (short) queue.getCurrentPriority(); //l = prioridade corrente da queue
			imgR[i] = h;
			for(Integer n: getAdjPixels(h, dejavu)){
				queue.priorityPush(n, interpolation[n][0], interpolation[n][1]);
				dejavu[n] = true;
			}
			i++;
		}
		long tf = System.currentTimeMillis();
		if(isLog)
			System.out.println("Tempo de execucao [sort] "+ ((tf - ti) /1000.0)  + "s");
	}
	
	
	private int getInfinity(int interpolation[][]){
		if(xInfinito != -1 && yInfinito != -1){
			return (xInfinito * 4) + ((yInfinito * 4) * interpWidth);
		}
		int hist[] = new int[256];
		for(int px=0; px < interpWidth; px++){
			hist[interpolation[px + 0 * interpWidth][0]]++;
			hist[interpolation[px + (interpHeight-1) * interpWidth][0]]++;
		}
		for(int py=0; py < interpHeight; py++){
			hist[interpolation[0 + py * interpWidth][0]]++;
			hist[interpolation[(interpWidth-1) + py * interpWidth][0]]++;
		}
		int max = 0;
		int level = 0;
		for(int i=0; i < hist.length; i++){
			if(max < hist[i]){
				max = hist[i];
				level = i;
			}
		}
		for(int px=0; px < interpWidth; px++){
			if(interpolation[px + 0 * interpWidth][0] == level) {
				xInfinito = px /4;
				yInfinito = 0;
				return px + 0 * interpWidth;
			}
			if(interpolation[px + (interpHeight-1) * interpWidth][0] == level){ 
				xInfinito = px/4;
				yInfinito = (interpHeight-1)/4;
				return px + (interpHeight-1) * interpWidth;
			}
		}
		for(int py=0; py < interpHeight; py++){
			if(interpolation[0 + py * interpWidth][0] == level){ 
				xInfinito = 0;
				yInfinito = py/4;
				return 0 + py * interpWidth; 
			}
			if(interpolation[(interpWidth-1) + py * interpWidth][0] == level){ 
				xInfinito = (interpWidth-1)/4;
				yInfinito = py/4;
				return (interpWidth-1) + py * interpWidth;
			}
		}
		xInfinito=0;
		yInfinito=0;
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
	public int[][] interpolateImage( ) {
		long ti = System.currentTimeMillis();
		

        //para inicializar a interpolacao para depois usar max e min
        int maxValue = 257, minValue = -1; 

        //interpolation tem tamanho 4 vezes para x e y
        // tem 2 valores ( 0 -> menor e 1 -> maior )
        this.interpWidth = (imgWidth*4-3);
        this.interpHeight = (imgHeight*4-3);
        int interpolation[][] = new int[interpWidth * interpHeight][2];
        
        for(int i=0; i < interpolation.length; i++){
        	interpolation[i][0] = maxValue;
        	interpolation[i][1] = minValue;
        }
        
        //executar interpolation
        //NOTES:
        // y e x sao os verdadeiros pixels da imagem, m sao as 16 translacoes desde os pixels aos 15 outros blocos criados na interpolacao
        // para m = 0 eh uma copiar
        // para os outros eh necessario ver as relacoes de vizinhanza
        // para m = 1 ate m = 3, se calcula o maior dos vizinhos
        // para m = 4 ate m = 15, se realiza a operacao de span 
        for (int m = 0; m < 16; m++) { // para todos os moves
            for (int y = 0; y < imgHeight; y++) { //para todos os pixels
                for (int x = 0; x < imgWidth; x++) { 
                    int px = x * 4 + moves[m][1]; // em x
                    int py = y * 4 + moves[m][0]; // em y
                    
                    // check bounds!!
                    if (py < 0 || py >= interpHeight || px < 0 || px >= interpWidth)
                        continue;
                    
                    int pixel = px + py * interpWidth;
                    if (m == 0) { //copy original values
                    	interpolation[pixel][0] = interpolation[pixel][1] = img.getPixel(x + y * imgWidth);
                    	//interpolation[pixel][0] = img.getPixel(x + y * imgWidth);
                        //interpolation[pixel][1] = img.getPixel(x + y * imgWidth);
                    }
                    else { // percorrer vizinhos
                        for (int k = 0; k < vizinhos[m].length; k++) {
                            int vx = px + vizinhos[m][k][1]; 
                            int vy = py + vizinhos[m][k][0]; 
                           
                            // check bounds!!
                            if (vy < 0 || vy >= interpHeight || vx < 0 || vx >= interpWidth)
                                continue;
                            
                            int pixelV = vx + vy * interpWidth; 
                            
                            
                            // para m = 1 ate m = 3 se tem o maximo para os 2 valores
                            if (m >= 1 && m <= 3) {
                                if (interpolation[pixel][1] < interpolation[pixelV][1]) {
                                	//interpolation[pixel][0] = interpolation[pixelV][1];
                                	interpolation[pixel][0] = interpolation[pixelV][0];
                                    interpolation[pixel][1] = interpolation[pixelV][1];
                                }
                            }
                            else { //m = [4 .. 15], maximo e minimo 
                                if (interpolation[pixel][0] > interpolation[pixelV][0])
                                    interpolation[pixel][0] = interpolation[pixelV][0];
                                
                                if (interpolation[pixel][1] < interpolation[pixelV][1])
                                    interpolation[pixel][1] = interpolation[pixelV][1];
                            }
                        }
                    }
                }
            }
        }
        long tf = System.currentTimeMillis();
        if(isLog)
        	System.out.println("Tempo de execucao [interpolacao] "+ ((tf - ti) /1000.0)  + "s");
        return interpolation;
	}

	

	

}