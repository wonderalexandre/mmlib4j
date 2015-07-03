package mmlib4j.representation.tree.componentTree;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;

import mmlib4j.datastruct.Queue;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class BuilderComponentTreeByRegionGrowing implements BuilderComponentTree {    
    int px[]; 
  	int py[]; 
	GrayScaleImage img;
	boolean isMaxtree;
    int numNode;
    int numNodeIdMax;
    
    int limiteInf = 0;
    int limiteSup = 255;
    static final int NIL = -1;
    static final int INQUEUE = -2;
    int indexOrder;
    
	
	int parent[];
	int imgR[];
	
	Queue<Integer> hQueue[]; 
	int levRoot[];
	
	HashSet<NodeCT> listNode;
    NodeCT root;
    NodeCT nodesMap[];
    
    
    
    /*
	public BuilderComponentTreeByRegionGrowing(int pixels[], int width, int height, AdjacencyRelation adj, boolean isMaxtree){
		this(new IntegerImage(pixels, width, height), adj, isMaxtree);
	}*/
	
	public BuilderComponentTreeByRegionGrowing(GrayScaleImage img, AdjacencyRelation adj, boolean isMaxtree){
		this.img = img;
		this.px = adj.getVectorX();
		this.py = adj.getVectorY();
		this.isMaxtree = isMaxtree;
		this.parent = new int[img.getSize()];
		this.imgR = new int[img.getSize()]; 
		this.indexOrder = img.getSize()-1;
		int maxValue = (int) Math.pow(2, img.getDepth());
		this.hQueue = new Queue[maxValue]; 
		this.levRoot = new int[maxValue];
		
		this.build();	
	}
	
	private BuilderComponentTreeByRegionGrowing(){ }

	@Override
	public BuilderComponentTree getClone() {
		BuilderComponentTreeByRegionGrowing b = new BuilderComponentTreeByRegionGrowing();
		b.px = this.px.clone();
		b.py = this.py.clone();
		b.img = this.img;
		b.numNode = this.numNode;
		b.isMaxtree = this.isMaxtree;
		b.imgR = this.imgR;
		b.limiteInf = this.limiteInf;
		b.limiteSup = this.limiteSup;
		b.numNodeIdMax = this.numNodeIdMax;
		b.parent = this.parent.clone();
		b.createTreeStructure();
		
		return b;
		
	}

	
	public void build() {
		if(isMaxtree){
    		build(img.minPixel(), img.minValue());
    	}
    	else{
    		build(img.maxPixel(), img.maxValue());
    	}
	}
	
	public NodeCT[] getMap(){
		return nodesMap;
	}

	public void setLimit(int inf, int sup){
		this.limiteInf = inf;
		this.limiteSup = sup;
	}
	
	public HashSet<NodeCT> getListNodes(){
		return listNode;
	}
	
    public void build(int pixel, int pixelLevel){  	
    	for(int i=0; i < parent.length; i++) parent[i] = NIL;
    	for(int i=0; i < levRoot.length; i++) levRoot[i] = NIL;
    	
    	hQueue[pixelLevel] = new Queue<Integer>();
    	hQueue[pixelLevel].enqueue(pixel);
		levRoot[pixelLevel] = pixel;
		flooding(pixelLevel, pixel);

		createTreeStructure( );
    }
    
    
    public int flooding(int lambda, int r){
    	while(hQueue[lambda] != null && !hQueue[lambda].isEmpty()){
    		int p = hQueue[lambda].dequeue();
    		parent[p] = r;
    		
    		if(p != r){
    			//insert front (S, p)
    			imgR[indexOrder--] = p;
    			//pixelsOrder.push(p);
    		}
    		
    		for(int n: getAdjPixelsNotProcessed(p, parent)){
    			int l = img.getPixel(n);
    			if(levRoot[l] == NIL){
    				levRoot[l] = n;
    			}
    			if(hQueue[l] == null) hQueue[l] = new Queue<Integer>(); 
    			hQueue[l].enqueue(n);
    			parent[n] = INQUEUE;
    				
    			if(isMaxtree){
	    			while(l > lambda){
	    				l = flooding(l, levRoot[l]);
	    			}
    			}else{
    				while(l < lambda){
	    				l = flooding(l, levRoot[l]);
	    			}
    			}
    		}
    	}
    	levRoot[lambda] = NIL;
    	int nextLambda;
    	if(isMaxtree){
    		nextLambda = lambda - 1;
    		while(nextLambda >= limiteInf && levRoot[nextLambda] == NIL){
    			nextLambda = nextLambda - 1;
    		}
    	}else{
    		nextLambda = lambda + 1;
    		while(nextLambda <= limiteSup && levRoot[nextLambda] == NIL){
    			nextLambda = nextLambda + 1;
    		}
    	}
    	if(nextLambda >= limiteInf && nextLambda <= limiteSup){
    		parent[r] = levRoot[nextLambda];
    	}
    	//insert front (S, r)
    	//pixelsOrder.push(r);
    	imgR[indexOrder--] = r;
    	
    //	printImage(parent, width, height);
    	
    	return nextLambda;
    }
    
    
    
    
    public Iterable<Integer> getAdjPixelsNotProcessed(int p, final int parent[]) {
    	final int x = p % img.getWidth();
    	final int y = p / img.getWidth();
        return new Iterable<Integer>() {			
        	public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					int i = 0;
					public boolean hasNext() {
						while(i < px.length){
							int xx = px[i] + x;
							int yy = py[i] + y;
							int pixel = xx + yy * img.getWidth();
				        	if(img.isPixelValid(xx, yy) && parent[pixel] == NIL)
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
    }
		
		
		
		

	/**
	 * Desinterpolacao da arvore e a transforma em uma arvore de estrutura ligada. 
	 * @return mapa node
	 */
	public void createTreeStructure( ){
		this.numNode = 0;
		this.nodesMap = new NodeCT[parent.length];
		this.listNode = new HashSet<NodeCT>();
		for(int i=0; i < this.imgR.length; i++){
			int p = this.imgR[i];
			int pai = this.parent[p];
			if(p == pai){
				this.root = nodesMap[p] = new NodeCT(isMaxtree, numNode++, img, p);
				this.listNode.add(nodesMap[p]);
				this.nodesMap[p].addPixel( p );
			}
			else{
				if(img.getPixel(pai) != img.getPixel(p)){ //novo no
					if(nodesMap[p] == null){ 
						this.nodesMap[p] = new NodeCT(isMaxtree, numNode++, img, p);
						this.listNode.add(nodesMap[p]);
					}
					this.nodesMap[p].parent = nodesMap[pai];
					this.nodesMap[pai].children.add(nodesMap[p]);
					this.nodesMap[p].addPixel( p );
				}
				else if (img.getPixel(pai) == img.getPixel(p)){ 
					//mesmo no
					this.nodesMap[p] = nodesMap[pai];
					this.nodesMap[pai].addPixel( p );				
				}
			}

		}
		numNodeIdMax = numNode;
		
	}
		
		
	public static void printTree(NodeCT no, PrintStream out, String s){
		out.printf(s + "[%3d; %d]\n", no.level, no.getCanonicalPixels().size());
		if(no.children != null)
			for(NodeCT son: no.children){
				printTree(son, out, s + "------");
			}
	}
	
	
	/**
	 * Constru��o da arvore de componentes
	 * @param pixels => pixels da imagem no formato de um vetor 1D, ou seja, (x,y) eh a posicao (x + y * width) do vetor  
	 * @param imgWidth => largura da imagem
	 * @param imgHeight => altura da imagem
	 * @param adj => relacao de adjacentes
	 * @param isNodeMaxtree => true para maxtree e false para mintree
	 * 
	 * @return raiz da arvore de componentes
	 */
	public NodeCT getRoot( ){
		return root;
	}
	
	public int getNunNode(){
		return numNode;
	}
	
	public int getNunNodeIdMax(){
		return numNodeIdMax;
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
		int width = 11;
		int height = 7;
		int pixels4[] = new int[] { 
				3, 3, 1, 4, 2,
				4, 1, 2, 3, 1
		};
		//int width = 5;
		//int height = 2;
		printImage(pixels2, width, height);
		
		GrayScaleImage img = ImageBuilder.openGrayImage();
		BuilderComponentTreeByRegionGrowing builder = new BuilderComponentTreeByRegionGrowing(img, AdjacencyRelation.getCircular(1.5), false);
		
		//builder.img = ImageBuilder.openGrayImage();
		//builder.reFloogind(78, img.convertToIndex(140, 165));
		NodeCT root = builder.getRoot();
		
		/*
		IGrayScaleImage g = new GrayScaleImage(pixels2, width, height);
		g.replaceValue(24, 70);
		builder.setLimit(24, 70);
		builder.imgU = g.getPixels();
		builder.flooding(70, 0);
		*/
		System.out.println("\n**********************ARVORE***********************");
		printTree(root, System.out, "<-");
		System.out.println("***************************************************\n");
		
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao  "+ ((tf - ti) /1000.0)  + "s");
		
		
	}
    public static void printImage(int[] parent, int width, int height){
    	System.out.println("[nivel de cinza]");
		for(int y=0; y < height; y++){
			for(int x=0; x < width; x++){
				int p = parent[x + y * width];
				System.out.printf("[%2d]", p);
				
			}
			System.out.println();
		}
		System.out.println();
    }
    
    public static void printImage(Integer[] parent, int width, int height){
    	System.out.println("[mapa de parentesco]");
		for(int y=0; y < height; y++){
			for(int x=0; x < width; x++){
				int p= (parent[x + y * width] == null? -1:parent[x + y * width]);
				System.out.printf("[%2d]", p);
				
			}
			System.out.println();
		}
		System.out.println();
    }

	@Override
	public int getNumNodeIdMax() {
		return numNodeIdMax;
	}

	
	
    
}
