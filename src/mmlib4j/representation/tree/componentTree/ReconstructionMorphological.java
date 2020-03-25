package mmlib4j.representation.tree.componentTree;

import mmlib4j.datastruct.PriorityQueue;
import mmlib4j.datastruct.Queue;
import mmlib4j.filtering.MorphologicalOperatorsBasedOnSE;
import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.AbstractImageFactory;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.NodeLevelSets;
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
	boolean flagProcessMaxtree[];
	boolean flagPruningMaxtree[];
	boolean flagProcessMintree[];
	boolean flagPruningMintree[];
	
	
	public ReconstructionMorphological(GrayScaleImage img){
		this.imgInput = img;
		this.mintree = new ComponentTree(img, AdjacencyRelation.getAdjacency8(), false);
		this.maxtree = new ComponentTree(img, AdjacencyRelation.getAdjacency8(), true);
		this.maxtree.extendedTree();
		this.mintree.extendedTree();
		flagProcessMaxtree = new boolean[maxtree.getNumNode()];
		flagPruningMaxtree = new boolean[maxtree.getNumNode()];
		flagProcessMintree = new boolean[mintree.getNumNode()];
		flagPruningMintree = new boolean[mintree.getNumNode()];
	}
	
	public ReconstructionMorphological(GrayScaleImage img, AdjacencyRelation adj, boolean isMaxtree){
		this.imgInput = img;
		if(isMaxtree){
			this.maxtree = new ComponentTree(img, adj, isMaxtree);
			this.maxtree.extendedTree();
			flagProcessMaxtree = new boolean[maxtree.getNumNode()];
			flagPruningMaxtree = new boolean[maxtree.getNumNode()];
		}else{	
			this.mintree = new ComponentTree(img, adj, isMaxtree);
			this.mintree.extendedTree();
			flagProcessMintree = new boolean[mintree.getNumNode()];
			flagPruningMintree = new boolean[mintree.getNumNode()];
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
		flagProcessMaxtree = new boolean[maxtree.getNumNode()];
		flagPruningMaxtree = new boolean[maxtree.getNumNode()];
		flagProcessMintree = new boolean[mintree.getNumNode()];
		flagPruningMintree = new boolean[mintree.getNumNode()];
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
		boolean flagProcess[] = new boolean[tree.getNunNode()];
		boolean flagPruning[] = new boolean[tree.getNunNode()];
		for(int p=0; p < imgInput.getSize(); p++){
			if(imgMarcador.getPixel(p) <= imgInput.getPixel(p)){
				queue.add(p, imgMarcador.getPixel(p));
			}
			flagProcess[tree.nodesMap[p].getId()] = false;
			flagPruning[tree.nodesMap[p].getId()] = true;
		}
		int p;
		while(!queue.isEmpty()){
			p = queue.removeMax();
			NodeLevelSets node = tree.nodesMap[p]; 
			if(! flagProcess[node.getId()]){
				flagProcess[node.getId()] = true;
				while(flagPruning[node.getId()] && imgMarcador.getPixel(p) < node.getLevel()){
					node = node.getParent();
				}
				//Invariante (neste ponto): imgMarcador.getPixel(p) >= node.level ==>ou seja, este node eh preservado na arvore
				flagPruning[node.getId()] = false;
				while(node.getParent() != null && flagPruning[node.getParent().getId()]){
					flagPruning[node.getParent().getId()] = false;
					node = node.getParent();
				}
			}
		}
		
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(tree.root);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			if(flagPruning[no.getId()]){ //poda
				Queue<NodeLevelSets> fifoPruning = new Queue<NodeLevelSets>();
				fifoPruning.enqueue(no);	
				int levelPropagation = no.getParent() == null ? no.getLevel() : no.getParent().getLevel();
				while(!fifoPruning.isEmpty()){
					NodeLevelSets nodePruning = fifoPruning.dequeue();
					for(NodeLevelSets song: nodePruning.getChildren()){ 
						fifoPruning.enqueue(song);
					}
					for(Integer pixel: nodePruning.getCompactNodePixels()){
						imgInput.setPixel(pixel, levelPropagation);
					}
				}
			}else{
				for(NodeLevelSets son: no.getChildren()){
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
	public void reconstructionImageOfSubtree(NodeLevelSets root, GrayScaleImage imgOut){
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(root);
		boolean flagPruning;
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			if(no.isNodeMaxtree())
				flagPruning = flagPruningMaxtree[no.getId()];
			else
				flagPruning = flagPruningMintree[no.getId()];
			
			if(flagPruning){ //poda
				Queue<NodeLevelSets> fifoPruning = new Queue<NodeLevelSets>();
				fifoPruning.enqueue(no);	
				int levelPropagation = no.getParent() == null ? no.getLevel() : no.getParent().getLevel();
				while(!fifoPruning.isEmpty()){
					NodeLevelSets nodePruning = fifoPruning.dequeue();
					for(NodeLevelSets song: nodePruning.getChildren()){ 
						fifoPruning.enqueue(song);
					}
					for(Integer pixel: nodePruning.getCompactNodePixels()){
						imgOut.setPixel(pixel, levelPropagation);
					}
				}

			}else{
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}
		}
	}

	public GrayScaleImage selfReconstruction(final GrayScaleImage imgMarcador){
		long ti = System.currentTimeMillis();
		final GrayScaleImage recO = ImageFactory.createGrayScaleImage(AbstractImageFactory.DEPTH_8BITS, 0, 0);
		final GrayScaleImage recC = ImageFactory.createGrayScaleImage(AbstractImageFactory.DEPTH_8BITS, 0, 0);

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
		
		for(NodeLevelSets node: tree.getListNodes()){
			if(tree.isMaxtree){
				flagPruningMaxtree[node.getId()] = true;
			}else {
				flagPruningMintree[node.getId()] = true;
			}
		}
		
		for(int p=0; p < imgInput.getSize(); p++){
			if(tree.isMaxtree){
				if(imgMarcador.getPixel(p) <= imgInput.getPixel(p)){
					queue.add(p, imgMarcador.getPixel(p));
				}
				flagProcessMaxtree[tree.getSC(p).getId()] = false;
				//tree.map[p].flagProcess = false;
			}else{
				if(imgMarcador.getPixel(p) >= imgInput.getPixel(p)){
					queue.add(p, imgMarcador.getPixel(p));
				}
				flagProcessMintree[tree.getSC(p).getId()] = false;
				//tree.map[p].flagProcess = false;
			}
			
		}

		
		int p;
		boolean flagProcess;
		while(!queue.isEmpty()){
			if(tree.isMaxtree)
				p = queue.removeMax();
			else
				p = queue.remove();
			
			NodeLevelSets node = tree.getSC(p); 
			if(tree.isMaxtree)
				flagProcess = flagProcessMaxtree[node.getId()];
			else
				flagProcess = flagProcessMintree[node.getId()];
			if(!flagProcess){
				
				if(tree.isMaxtree)
					flagProcess = flagProcessMaxtree[node.getId()] = true;
				else
					flagProcess = flagProcessMintree[node.getId()] = true;
				
				if(tree.isMaxtree){
					while(flagPruningMaxtree[node.getId()] && imgMarcador.getPixel(p) < node.getLevel()){
						node = node.getParent();
					}
					//Invariante (neste ponto): imgMarcador.getPixel(p) >= node.level ==>ou seja, este node eh preservado na arvore
				}else{
					while(flagPruningMintree[node.getId()] && imgMarcador.getPixel(p) > node.getLevel()){
						node = node.getParent();
					}
					
				}
				
				if(tree.isMaxtree) {
					flagPruningMaxtree[node.getId()] = false;
					while(node.getParent() != null && flagPruningMaxtree[node.getParent().getId()]){
						flagPruningMaxtree[node.getParent().getId()] = false;
						node = node.getParent();
					}
				}
				else {
					flagPruningMintree[node.getId()] = false;
					while(node.getParent() != null && flagPruningMintree[node.getParent().getId()]){
						flagPruningMintree[node.getParent().getId()] = false;
						node = node.getParent();
					}
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
					flagProcessMaxtree[ tree.getSC(p).getId() ] = false;
				}else{
					if(imgMarcador.getPixel(p) >= imgInput.getPixel(p)){
						queue.add(p, imgMarcador.getPixel(p));
					}
					flagProcessMintree[ tree.getSC(p).getId() ] = false;
				}
				
			}
			
			
			for(NodeLevelSets node: tree.getListNodes()){
				if(tree.isMaxtree)
					flagPruningMaxtree[ node.getId() ] = true;
				else
					flagPruningMintree[ node.getId() ] = true;
			}
			
			int p;
			while(!queue.isEmpty()){
				if(tree.isMaxtree)
					p = queue.removeMax();
				else
					p = queue.remove();
				
				NodeLevelSets node = tree.getSC(p);
				boolean flagProcess;
				if(tree.isMaxtree)
					flagProcess = flagProcessMaxtree[ node.getId() ];
				else
					flagProcess = flagProcessMintree[ node.getId() ];
				
				if(!flagProcess){
					
					if(tree.isMaxtree) {
						flagProcess = flagProcessMaxtree[ node.getId() ] = true;
						while(flagPruningMaxtree[ node.getId() ] && imgMarcador.getPixel(p) < node.getLevel()){
							node = node.getParent();
						}
						//Invariante (neste ponto): imgMarcador.getPixel(p) >= node.level ==>ou seja, este node eh preservado na arvore
						flagPruningMaxtree[ node.getId() ] = true;
						while(node.getParent() != null && flagPruningMaxtree[node.getParent().getId()]){
							flagPruningMaxtree[node.getParent().getId()] = false;
							node = node.getParent();
						}
					}
					else {
						flagProcess = flagProcessMintree[ node.getId() ] = true;
						while(flagPruningMintree[ node.getId() ] && imgMarcador.getPixel(p) > node.getLevel()){
							node = node.getParent();
							
						}
						flagPruningMintree[ node.getId() ] = true;
						while(node.getParent() != null && flagPruningMintree[node.getParent().getId()]){
							flagPruningMintree[node.getParent().getId()] = false;
							node = node.getParent();
						}
					}
					
					
				}
			}
			//////////
			boolean flagPruning[] = null;
			if(tree.isMaxtree())
				flagPruning = flagPruningMaxtree;
			else
				flagPruning = flagPruningMintree;
			Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
			fifo.enqueue(tree.getRoot());
			while(!fifo.isEmpty()){
				NodeLevelSets no = fifo.dequeue();
				if(flagPruning[no.getId()]){ //poda
					selected[no.getId()] = true;
					Queue<NodeLevelSets> fifoPruning = new Queue<NodeLevelSets>();
					fifoPruning.enqueue(no);	
					int levelPropagation = no.getParent() == null ? no.getLevel() : no.getParent().getLevel();
					while(!fifoPruning.isEmpty()){
						NodeLevelSets nodePruning = fifoPruning.dequeue();
						for(NodeLevelSets song: nodePruning.getChildren()){ 
							fifoPruning.enqueue(song);
						}
						for(Integer pixel: nodePruning.getCompactNodePixels()){
							imgMarcador.setPixel(pixel, levelPropagation);
						}
					}

				}else{
					for(NodeLevelSets son: no.getChildren()){
						fifo.enqueue(son);	 
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
