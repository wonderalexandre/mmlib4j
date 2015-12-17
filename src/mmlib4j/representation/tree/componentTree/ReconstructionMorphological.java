package mmlib4j.representation.tree.componentTree;

import mmlib4j.datastruct.PriorityQueue;
import mmlib4j.datastruct.Queue;
import mmlib4j.filtering.MorphologicalOperatorsBasedOnSE;
import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageAlgebra;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ReconstructionMorphological {
	private GrayScaleImage imgInput;
	ComponentTree mintree;
	ComponentTree maxtree;
	
	public ReconstructionMorphological(GrayScaleImage img){
		this.imgInput = img;
		this.mintree = new ComponentTree(img, AdjacencyRelation.getAdjacency8(), false);
		this.maxtree = new ComponentTree(img, AdjacencyRelation.getAdjacency8(), true);
		this.maxtree.extendedTree();
		this.mintree.extendedTree();
	}
	
	public ReconstructionMorphological(GrayScaleImage img, AdjacencyRelation adj, boolean isMaxtree){
		this.imgInput = img;
		if(isMaxtree){
			this.maxtree = new ComponentTree(img, adj, isMaxtree);
			this.maxtree.extendedTree();
		}else{	
			this.mintree = new ComponentTree(img, adj, isMaxtree);
			this.mintree.extendedTree();
		}
		
	}
	
	public ReconstructionMorphological(GrayScaleImage img, boolean isMaxtree){
		this(img, AdjacencyRelation.getAdjacency8(), isMaxtree);	
	}
	
	public ReconstructionMorphological(GrayScaleImage img, ComponentTree mintree, ComponentTree maxtree){
		this.imgInput = img;
		this.mintree = mintree;
		this.maxtree = maxtree;
		this.maxtree.extendedTree(); 
		this.mintree.extendedTree();
	}
	
	
	/**
	 * Restricao: imgMarcador <= imgInput 
	 * @param imgMarcador
	 * @return
	 */
	public GrayScaleImage reconstructionByDilation(GrayScaleImage imgMarcador){
		reconstructionMorphological(imgMarcador, maxtree);
		GrayScaleImage imgOut = imgInput.duplicate();
		reconstructionImageOfSubtree(maxtree.getRoot(), imgOut);
		return imgOut;
	}

	public static GrayScaleImage dilationByReconstruction(GrayScaleImage imgInput, GrayScaleImage imgMarcador){
		BuilderComponentTreeByRegionGrowing tree = new BuilderComponentTreeByRegionGrowing(imgInput, AdjacencyRelation.getAdjacency8(), true);
		return dilationByReconstruction(imgInput.duplicate(), imgMarcador, tree);
	}
	
	public static GrayScaleImage dilationByReconstruction(GrayScaleImage imgInput, GrayScaleImage imgMarcador, AdjacencyRelation adj){
		BuilderComponentTreeByRegionGrowing tree = new BuilderComponentTreeByRegionGrowing(imgInput, adj, true);
		return dilationByReconstruction(imgInput.duplicate(), imgMarcador, tree);
	}
	

	public static GrayScaleImage dilationByReconstruction(BuilderComponentTreeByRegionGrowing tree, GrayScaleImage imgMarcador){
		return dilationByReconstruction(tree.img.duplicate(), imgMarcador, tree);
	}

	
	public static GrayScaleImage dilationByReconstruction(GrayScaleImage imgInput, GrayScaleImage imgMarcador, BuilderComponentTreeByRegionGrowing tree){
		long ti = System.currentTimeMillis();
		PriorityQueue<Integer> queue = new PriorityQueue<Integer>(255);
		
		for(int p=0; p < imgInput.getSize(); p++){
			if(imgMarcador.getPixel(p) <= imgInput.getPixel(p)){
				queue.add(p, imgMarcador.getPixel(p));
			}
			tree.nodesMap[p].flagProcess = false;
			tree.nodesMap[p].flagPruning = true;
		}
		int p;
		while(!queue.isEmpty()){
			p = queue.removeMax();
			NodeCT node = tree.nodesMap[p]; 
			if(!node.flagProcess){
				node.flagProcess = true;
				while(node.flagPruning && imgMarcador.getPixel(p) < node.level){
					node = node.parent;
				}
				//Invariante (neste ponto): imgMarcador.getPixel(p) >= node.level ==>ou seja, este node eh preservado na arvore
				node.flagPruning = false;
				while(node.parent != null && node.parent.flagPruning){
					node.parent.flagPruning = false;
					node = node.parent;
				}
			}
		}
		
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(tree.root);
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			if(no.flagPruning){ //poda
				Queue<NodeCT> fifoPruning = new Queue<NodeCT>();
				fifoPruning.enqueue(no);	
				int levelPropagation = no.parent == null ? no.level : no.parent.level;
				while(!fifoPruning.isEmpty()){
					NodeCT nodePruning = fifoPruning.dequeue();
					for(NodeCT song: nodePruning.children){ 
						fifoPruning.enqueue(song);
					}
					for(Integer pixel: nodePruning.getCanonicalPixels()){
						imgInput.setPixel(pixel, levelPropagation);
					}
				}
			}else{
				for(NodeCT son: no.children){
					fifo.enqueue(son);	 
				}
			}
		}
		long tf = System.currentTimeMillis();
		if(Utils.debug)
		System.out.println("Tempo de execucao [Componente tree - dilation by reconstruction]  "+ ((tf - ti) /1000.0)  + "s");
		return imgInput;
	}
	
	
	/**
	 * Restricao: imgMarcador >= imgInput
	 * @param imgMarcador
	 * @return
	 */
	public GrayScaleImage reconstructionByErosion(GrayScaleImage imgMarcador){
		reconstructionMorphological(imgMarcador, mintree);
		GrayScaleImage imgOut = imgInput.duplicate();
		reconstructionImageOfSubtree(mintree.getRoot(), imgOut);
		return imgOut;
	}
	
	
	/**
	 * Reconstrucao de uma subarvore processada
	 * @param root
	 * @param imgOut
	 */
	public void reconstructionImageOfSubtree(NodeCT root, GrayScaleImage imgOut){
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(root);
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			if(no.flagPruning){ //poda
				Queue<NodeCT> fifoPruning = new Queue<NodeCT>();
				fifoPruning.enqueue(no);	
				int levelPropagation = no.parent == null ? no.level : no.parent.level;
				while(!fifoPruning.isEmpty()){
					NodeCT nodePruning = fifoPruning.dequeue();
					if(nodePruning.children != null){ 
						for(NodeCT song: nodePruning.children){ 
							fifoPruning.enqueue(song);
						}
					}
					for(Integer pixel: nodePruning.getCanonicalPixels()){
						imgOut.setPixel(pixel, levelPropagation);
					}
				}

			}else{
				if(no.children != null){
					for(NodeCT son: no.children){
						fifo.enqueue(son);	 
					}
				}
			}
		}
	}

	public GrayScaleImage selfReconstruction(final GrayScaleImage imgMarcador){
		long ti = System.currentTimeMillis();
		final GrayScaleImage recO = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_8BITS, 0, 0);
		final GrayScaleImage recC = ImageFactory.createGrayScaleImage(ImageFactory.DEPTH_8BITS, 0, 0);

		//paralelisa
        final Thread[] threads = new Thread[2]; 
        threads[0] = new Thread(){
        	public void run(){
        		GrayScaleImage out = reconstructionByDilation(ImageAlgebra.minimum(imgInput, imgMarcador));
        		recO.setPixels(out.getWidth(), out.getHeight(), out.getPixels());
        	}
        };
        threads[1] = new Thread(){
        	public void run(){
        		GrayScaleImage out = reconstructionByErosion(ImageAlgebra.maximum(imgInput, imgMarcador));
        		recC.setPixels(out.getWidth(), out.getHeight(), out.getPixels());
        	}
        };
        for (final Thread thread : threads){
        	try {
        		thread.setPriority(Thread.currentThread().getPriority());
        		thread.start();
        		if (thread != null) 
        			thread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();	  
			}
		}
		
		
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		for(int p: imgOut.scanForward()){
			//if(mintree.getSC(p).flagPruning && maxtree.getSC(p).flagPruning){
			//	System.out.println("ops...");
			//}
			
			if(imgMarcador.getPixel(p) < imgInput.getPixel(p)){
				imgOut.setPixel(p, recO.getPixel(p));
			}
			else if(imgMarcador.getPixel(p) > imgInput.getPixel(p)){
				imgOut.setPixel(p, recC.getPixel(p));
			}
			else{
				imgOut.setPixel(p, imgInput.getPixel(p));
			}
		}
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [Componente tree - reconstruction morphological (leveling)]  "+ ((tf - ti) /1000.0)  + "s");
		
		return imgOut;
	}
    
	
	
	public void reconstructionMorphological(GrayScaleImage imgMarcador, ComponentTree tree){
		long ti = System.currentTimeMillis();
		PriorityQueue<Integer> queue = new PriorityQueue<Integer>(255);
		
		for(NodeCT node: tree.listNode){
			node.flagPruning = true;
		}
		
		for(int p=0; p < imgInput.getSize(); p++){
			if(tree.isMaxtree){
				if(imgMarcador.getPixel(p) <= imgInput.getPixel(p)){
					queue.add(p, imgMarcador.getPixel(p));
				}
			}else{
				if(imgMarcador.getPixel(p) >= imgInput.getPixel(p)){
					queue.add(p, imgMarcador.getPixel(p));
				}
			}
			tree.map[p].flagProcess = false;
		}

		
		int p;
		
		while(!queue.isEmpty()){
			if(tree.isMaxtree)
				p = queue.removeMax();
			else
				p = queue.remove();
			
			NodeCT node = tree.map[p]; 
			if(!node.flagProcess){
				node.flagProcess = true;
				if(tree.isMaxtree){
					while(node.flagPruning && imgMarcador.getPixel(p) < node.level){
						node = node.parent;
					}
					//Invariante (neste ponto): imgMarcador.getPixel(p) >= node.level ==>ou seja, este node eh preservado na arvore
				}else{
					while(node.flagPruning && imgMarcador.getPixel(p) > node.level){
						node = node.parent;
					}
					
				}
				
				node.flagPruning = false;
				while(node.parent != null && node.parent.flagPruning){
					node.parent.flagPruning = false;
					node = node.parent;
				}
			}
		}
		
		
	}
	
	

	
	public boolean[] getPrimitivesFamily(GrayScaleImage imgMarcador, int raioMax, int step){
		ComponentTree tree = maxtree;
		
		PriorityQueue<Integer> queue = new PriorityQueue<Integer>(255);
		boolean[] selected = new boolean[tree.getNumNode()];
		
		for(int raio=1; raio <= raioMax; raio += step){
			
			imgMarcador = MorphologicalOperatorsBasedOnSE.opening(imgMarcador, AdjacencyRelation.getCircular(raio));
			
			for(int p=0; p < imgInput.getSize(); p++){
				if(tree.isMaxtree){
					if(imgMarcador.getPixel(p) <= imgInput.getPixel(p)){
						queue.add(p, imgMarcador.getPixel(p));
					}
				}else{
					if(imgMarcador.getPixel(p) >= imgInput.getPixel(p)){
						queue.add(p, imgMarcador.getPixel(p));
					}
				}
				tree.map[p].flagProcess = false;
			}
			
			
			for(NodeCT node: tree.listNode){
				node.flagPruning = true;
			}
			
			int p;
			while(!queue.isEmpty()){
				if(tree.isMaxtree)
					p = queue.removeMax();
				else
					p = queue.remove();
				
				NodeCT node = tree.map[p]; 
				if(!node.flagProcess){
					node.flagProcess = true;
					if(tree.isMaxtree){
						while(node.flagPruning && imgMarcador.getPixel(p) < node.level){
							node = node.parent;
						}
						//Invariante (neste ponto): imgMarcador.getPixel(p) >= node.level ==>ou seja, este node eh preservado na arvore
					}else{
						while(node.flagPruning && imgMarcador.getPixel(p) > node.level){
							node = node.parent;
						}
						
					}
					
					node.flagPruning = false;
					while(node.parent != null && node.parent.flagPruning){
						node.parent.flagPruning = false;
						node = node.parent;
					}
				}
			}
			//////////
			Queue<NodeCT> fifo = new Queue<NodeCT>();
			fifo.enqueue(tree.root);
			while(!fifo.isEmpty()){
				NodeCT no = fifo.dequeue();
				if(no.flagPruning){ //poda
					selected[no.getId()] = true;
					Queue<NodeCT> fifoPruning = new Queue<NodeCT>();
					fifoPruning.enqueue(no);	
					int levelPropagation = no.parent == null ? no.level : no.parent.level;
					while(!fifoPruning.isEmpty()){
						NodeCT nodePruning = fifoPruning.dequeue();
						if(nodePruning.children != null){ 
							for(NodeCT song: nodePruning.children){ 
								fifoPruning.enqueue(song);
							}
						}
						for(Integer pixel: nodePruning.getCanonicalPixels()){
							imgMarcador.setPixel(pixel, levelPropagation);
						}
					}

				}else{
					if(no.children != null){
						for(NodeCT son: no.children){
							fifo.enqueue(son);	 
						}
					}
				}
			}	
		}
		
		return selected;
	}
	
	public static void main(String args[]) throws Exception{
		GrayScaleImage imgMask  = ImageBuilder.openGrayImage();
		GrayScaleImage imgMasked  = ImageBuilder.openGrayImage();
		WindowImages.show(imgMask, "entrada");
		WindowImages.show(imgMasked, "marcador");
		
		
		ReconstructionMorphological rec = new ReconstructionMorphological(imgMask);
		GrayScaleImage lev1 = rec.selfReconstruction(imgMasked);
		WindowImages.show(lev1, "leveling tree");
		
		
	}
	
}
