package mmlib4j.representation.tree.componentTree;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;

import mmlib4j.datastruct.PriorityQueueDial;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.Utils;



/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 *
 * Implementacao do algoritmo para construcao da component tree descrito em: 
 * C. Berger , Th. Geraud , R. Levillain , N. Widynski,
 * Effective component tree computation with application to pattern recognition in astronomical imaging,
 * ICIP, 2007
 */
public class BuilderComponentTreeByUnionFind implements BuilderComponentTree{
	
	final int NIL = -1;
	int px[]; 
  	int py[]; 
  	
	int imgR[];
	int numNode;
	int numNodeIdMax;
	boolean isMaxtree;
	
	int parent[];
	NodeCT nodesMap[];
	HashSet<NodeCT> listNode;
	
	NodeCT root;
	GrayScaleImage img;
	
	
	public BuilderComponentTreeByUnionFind(GrayScaleImage img, AdjacencyRelation adj, boolean isMaxtree){
		this.img = img;
		this.px = adj.getVectorX();
		this.py = adj.getVectorY();
		this.isMaxtree = isMaxtree;
		this.build();
	}
	
	private BuilderComponentTreeByUnionFind(){}

	@Override
	public void build() {
		long ti = System.currentTimeMillis();
		
		sort();
		createTreeByUnionFind();
		createTreeStructure( );
		imgR = null;
		long tf = System.currentTimeMillis();
		if(Utils.debug)
			System.out.println("Tempo de execucao [criacao da arvore - union-find]  "+ ((tf - ti) /1000.0)  + "s");
	}
	
	public NodeCT getRoot( ){
		return root;
	}
	
	public int getNunNode(){
		return numNode;
	}
	
	public HashSet<NodeCT> getListNodes(){
		return listNode;
	}
	
	public NodeCT[] getMap(){
		return nodesMap;
	}
	
	
	/**
	 * Desinterpolacao da arvore e a transforma em uma arvore de estrutura ligada. 
	 * @return mapa node
	 */
	public void createTreeStructure( ){
		this.numNode = 0;
		this.nodesMap = new NodeCT[parent.length];
		this.listNode = new HashSet<NodeCT>();
		for (int i = 0; i < imgR.length; i++) {
			int p = imgR[i];
			int pai = parent[p];
			if(p == pai){
				this.root = nodesMap[p] = new NodeCT(isMaxtree, numNode++, img, p);
				this.listNode.add(nodesMap[p]);
				this.nodesMap[p].addPixel( p );
			}
			else{
				if(img.getPixel(pai) != img.getPixel(p)){ //novo no
					if(this.nodesMap[p] == null){ 
						this.nodesMap[p] = new NodeCT(isMaxtree, numNode++, img, p);
						this.listNode.add(nodesMap[p]);
					}
					this.nodesMap[p].parent = nodesMap[pai];
					this.nodesMap[pai].children.add(nodesMap[p]);
					this.nodesMap[p].addPixel( p );
				}else if (img.getPixel(pai) == img.getPixel(p)){ 
					//mesmo no
					this.nodesMap[p] = nodesMap[pai];
					this.nodesMap[pai].addPixel( p );				
				}	
			}			
		}
		numNodeIdMax = numNode;
	}
	
	
	
	
	/**
	 * Devolve a lista de pixels adjacente a pixel de referencia. 
	 * @param p => pixel de referencia
	 * @param zPar => vetor dos pixels visitados
	 * @return lista de adjacentes
	 */
    public Iterable<Integer> getAdjPixels(int p, final int zPar[]) {
    	final int x = p % img.getWidth();
    	final int y = p / img.getWidth();
        return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private int i = 0;
					public boolean hasNext() {
						while(i < px.length){
							int xx = px[i] + x;
							int yy = py[i] + y;
							int pixel = xx + yy * img.getWidth();
				        	if(img.isPixelValid(xx, yy) && zPar[pixel] != NIL)
								return true;
				        	i++;
						} 
						return false;
					}
					public Integer next() {
						int pixel = (px[i] + x) + (py[i] + y) * img.getWidth();
						i++;
						return pixel;
					}
					public void remove() { }
					
				};
			}
		};
        
    	/*
   	 	== Equals code ==
   		int xx, yy, pixel;
   		LinkedList<Integer> neighbors = new LinkedList<Integer>();        
        for(int i=0; i < px.length; i++){
       		xx = px[i] + x;
       		yy = py[i] + y;
       		pixel = xx + yy * width;
       		if(xx >= 0,& xx < width && yy >= 0 && yy < height && zPar[pixel] != NIL)
       			neighbors.add( pixel );
        	}
        }
        return neighbors;
       */
    }
    
	
	/**
	 * Devolve a lista de pixels adjacente a pixel de referencia. 
	 * @param p => pixel de referencia
	 * @return lista de adjacentes
	 */
    public Iterable<Integer> getAdjPixels(int p) {
    	
    	final int x = p % img.getWidth();
    	final int y = p / img.getWidth();
        return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private int i = 0;
					public boolean hasNext() {
						while(i < px.length){
							int xx = px[i] + x;
							int yy = py[i] + y;
							if(img.isPixelValid(xx, yy))
								return true;
							i++;
						} 
						return false;
					}
					public Integer next() {
						int pixel = (px[i] + x) + (py[i] + y) * img.getWidth();
						i++;
						return pixel;
					}
					public void remove() { }
					
				};
			}
		};
		/*
   	 	== Equals code ==
   		int xx, yy, pixel;
   		LinkedList<Integer> neighbors = new LinkedList<Integer>();        
        for(int i=0; i < px.length; i++){
       		xx = px[i] + x;
       		yy = py[i] + y;
       		pixel = xx + yy * width;
       		if(xx >= 0 && xx < width && yy >= 0 && yy < height)
       			neighbors.add( pixel );
        }
        return neighbors;
       */
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
	private void createTreeByUnionFind( ) {
		parent = new int[img.getSize()];
		int zPar[] = new int[img.getSize()];
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
			
			if(img.getPixel(parent[q]) == img.getPixel(q)){
				parent[p] = parent[q];
			}
		}
		zPar = null;
		
	}

	
	/**
	 * Ordena os pixels das zonas planas da imagem pelas regionais minimas ou maximas 
 	 * Os pixels ordernados estao no vetor imgR[] e os nivel de cinza dos pixels estao no vetor imgU[]. 
 	 * Ordencao crescente dos pixels: imgR[lenght-1], imgR[lenght-2], ..., imgR[0]   
 	 * Ordencao decrescente dos pixels: imgR[0], imgR[1], ..., imgR[lenght-1]
	 * @param img => pixels da imagem
	 * @param isNodeMaxtree => true para maxtree e false para mintree 
	 */
	public void sort(){
		PriorityQueueDial fifo = null;
		int maxValue = (int) Math.pow(2, img.getDepth());
		if(!isMaxtree)
			fifo = new PriorityQueueDial(img, maxValue, PriorityQueueDial.LIFO, true);
		else
			fifo = new PriorityQueueDial(img, maxValue, PriorityQueueDial.LIFO);
		
		for(int p=0; p < img.getSize(); p++){
			fifo.add(p);
		}
		this.imgR = new int[img.getSize()];
		int indexOrder = 0;
		
		while(!fifo.isEmpty()){
			int h = fifo.remove();
			imgR[indexOrder] = h;
			
			for(Integer n: getAdjPixels(h)){  
				if(img.getPixel(h) == img.getPixel(n) && fifo.contains(n)){
					fifo.remove(n);
					fifo.add(n);
				}
			}
			indexOrder++;
		}
	}
		
	public int find(int vetor[], int p){
		for(int i=0; i < vetor.length; i++){
			if(vetor[i] == p)
				return i;
		}
		return -1;
	}
    
    
    public static void main(String[] args) {
    	
		long ti = System.currentTimeMillis();
		
		int pixels[] = new int[] { 
				24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
				24,  0,  0,  0,  0,  0,  0,  0,  0, 24, 24,
				24,  0, 45, 45, 45,  0,  5,  5,  0, 24, 24,
				24,  0, 45, 45, 45,  0,  5,  5,  0, 24, 24,
				24,  0, 45, 45, 45,  0,  5,  5,  0, 24, 24,
				24,  0,  0,  0,  0,  0,  0,  0,  0, 24, 24,
				24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24
		};
		int pixels2[] = new int[] { 
				24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
				24, 70, 70, 70, 70, 70, 70, 70, 70, 24, 24,
				24, 70, 85, 85, 85, 70, 95, 95, 70, 24, 24,
				24, 70, 85, 85, 85, 70, 95, 95, 70, 24, 24,
				24, 70, 85, 85, 85, 70, 95, 95, 70, 24, 24,
				24, 70, 70, 70, 70, 70, 70, 70, 70, 24, 24,
				24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24
		};
		int pixels3[] = new int[] { 
				24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
				24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
				24, 24, 85, 85, 85, 24, 24, 24, 24, 24, 24,
				24, 24, 85, 85, 85, 24, 24, 24, 24, 24, 24,
				24, 24, 85, 85, 85, 24, 24, 24, 24, 24, 24,
				24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
				24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24
		};
		
		int pixels5[] = new int[]{
				4, 4, 4, 4, 4, 4, 4, 4, 4,
				4, 7, 7, 7, 7, 7, 7, 7, 4,
				4, 7, 0, 0, 0, 0, 0, 7, 4,
				4, 7, 0, 2, 7, 2, 0, 7, 4,
				4, 7, 0, 2, 7, 2, 0, 7, 4,
				4, 7, 0, 2, 7, 2, 0, 7, 4,
				4, 7, 0, 0, 0, 0, 0, 7, 4,
				4, 7, 7, 7, 7, 7, 7, 7, 4,
				4, 4, 4, 4, 4, 4, 4, 4, 4
		};
		
		int width = 9;
		int height = 9;
		int pixels4[] = new int[] { 
				3, 3, 1, 4, 2,
				4, 1, 2, 3, 1
		};
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
		
		
		BuilderComponentTreeByUnionFind builder = new BuilderComponentTreeByUnionFind(ImageFactory.createReferenceGrayScaleImage(32, pixels5, width, height), AdjacencyRelation.getCircular(1), false);
		//BuilderComponentTreeByUnionFind builder = new BuilderComponentTreeByUnionFind(ImageBuilder.openGrayImage(), AdjacencyRelation.getCircular(1.5), false);
		NodeCT root = builder.getRoot();
		
		System.out.println("\n**********************ARVORE***********************");
		printTree(root, System.out, "<-");
		System.out.println("***************************************************\n");
		
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao  "+ ((tf - ti) /1000.0)  + "s");
		
		

	}
	
	
	public static void printTree(NodeCT no, PrintStream out, String s){
		out.printf(s + "[%3d; %d]\n", no.level, no.getCanonicalPixels().size());
		if(no.children != null)
			for(NodeCT son: no.children){
				printTree(son, out, s + "------");
			}
	}

	@Override
	public BuilderComponentTreeByUnionFind getClone() {
		BuilderComponentTreeByUnionFind b = new BuilderComponentTreeByUnionFind();
		b.px = this.px;
		b.py = this.py;
		b.img = this.img;
		b.numNode = this.numNode;
		b.isMaxtree = this.isMaxtree;
		b.imgR = this.imgR;
		b.parent = this.parent;
		b.numNodeIdMax = this.numNodeIdMax;
		b.createTreeStructure( );
		return b;
	}
	
	@Override
	public int getNumNodeIdMax() {
		return numNodeIdMax;
	}
	
	
	
}
