package mmlib4j.representation.tree.tos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import mmlib4j.datastruct.Queue;
import mmlib4j.filtering.binary.ContourTracer;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.IMorphologicalTreeFiltering;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.attribute.BitQuadsNodesTree;
import mmlib4j.representation.tree.pruningStrategy.ComputerExtinctionValueToS;
import mmlib4j.representation.tree.pruningStrategy.ComputerExtinctionValueToS.ExtinctionValueNode;
import mmlib4j.utils.Utils;


/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ConnectedFilteringByTreeOfShape extends TreeOfShape implements IMorphologicalTreeFiltering{
	
	protected boolean isLine = false;
	protected boolean processedLine = false;
	
	public ConnectedFilteringByTreeOfShape(GrayScaleImage img){
		super(img, -1, -1);
	}
	
	protected ConnectedFilteringByTreeOfShape(BuilderTreeOfShapeByUnionFind build){
		super(build);
		computerMoment(this.root);
		//computerAttributePattern();
		//setContours(false);
	}
	
	public ConnectedFilteringByTreeOfShape(GrayScaleImage img, int xInfinito, int yInfinito){
		super(img, xInfinito, yInfinito);
		//computerAttributePattern();
		computerMoment(this.root);
		//setContours(false);
		
	}
	
	public HashSet<NodeToS> getListNodes(){
		return listNode; 
	}

	public void computerMoment( ){
		computerMoment(this.root);
	}

	private ThreadPoolExecutor pool; 
	public void setContours(boolean b){
		isLine = b;
		if(!processedLine){
			pool =  new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
			processedLine = true;
			long ti = System.currentTimeMillis();
			computerCountors(root);
			while(pool.getActiveCount() != 0);
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [ToS - extraction of countors]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}
	
	public void computerCountors(NodeToS root){
		if(root.children != null){ //computa os atributos para os filhos
			for(NodeToS son: root.children){
				computerCountors(son);
			}
		}
		if(root.getArea() > 10){
			pool.execute(new ThreadNodeToSPerimeter(root, width, height));
		}
		
	}
	
	public void computerAttributePattern( ){
		long ti = System.currentTimeMillis();
		computerAttributePattern(getRoot());
		long tf = System.currentTimeMillis();
		System.out.println("Tempo de execucao [ToS - computer attribute pattern]  "+ ((tf - ti) /1000.0)  + "s");
	}
	
	private BitQuadsNodesTree bitQuads = null;
	public void computerAttributePattern(NodeToS node){
		if(bitQuads == null)
			bitQuads = new BitQuadsNodesTree(imgInput.getWidth(), imgInput.getHeight());
		
		node.initAttributePattern();
		
		for(NodeToS son: node.children){
			computerAttributePattern(son);
			node.attributePattern.merge(son.attributePattern);
		}
		
		//atualiza os pixels
		for(int p: node.getPixels()){ 
			bitQuads.updatePixel(p);
		}
		
		//calcula o hit-or-miss
		for(int w=node.xmin; w<= node.xmax; w++){
			for(int h=node.ymin; h<= node.ymax; h++){
				bitQuads.computerLocalHitOrMiss(node.attributePattern, w, h);
			}
		}
		
		
		//double c = (4.0 * Math.PI * root.getArea()) / Math.pow(root.getPattern().getPerimeter(), 2);
		//root.showNode(root.isLeaf()+"=>"+ root.attributePattern.numberHoles8());
		
	}

	

	

	private double getAttribute(NodeToS node, int type){
		if(type == IMorphologicalTreeFiltering.ATTRIBUTE_ECCENTRICITY)
			return node.eccentricity();
		else if(type == IMorphologicalTreeFiltering.ATTRIBUTE_MAJOR_AXES)
			return node.getLengthMajorAxes();
		else if(type == IMorphologicalTreeFiltering.ATTRIBUTE_ORIENTATION)
			return node.getMomentOrientation();
		else if(type == IMorphologicalTreeFiltering.ATTRIBUTE_CIRCULARITY)
			return (4.0 * Math.PI * node.getArea()) / (node.contour.getPixels().size());
		else if(type == IMorphologicalTreeFiltering.ATTRIBUTE_RETANGULARITY)
			return node.getArea() / (double) (node.getWidthNode() * node.getHeightNode());
		else
			return node.attributeValue[type];
	}

	
	public GrayScaleImage reconstruction(InfoPrunedTree prunedTree){
		return reconstruction(prunedTree, false);
	}
	
	public GrayScaleImage reconstruction(InfoPrunedTree prunedTree, boolean countors){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(getInputImage());
		Queue<InfoPrunedTree.NodePrunedTree> fifo = new Queue<InfoPrunedTree.NodePrunedTree>();
		fifo.enqueue( prunedTree.getRoot() );
		while(!fifo.isEmpty()){
			InfoPrunedTree.NodePrunedTree node_ = fifo.dequeue();
			NodeToS node = (NodeToS) node_.getInfo();
			for(NodeToS son: node.getChildren()){
				if(prunedTree.wasPruned(son)){
					if(!countors){
						for(int p: son.getPixelsOfCC()){
							imgOut.setPixel(p, node.getLevel());
						}
					}
				}
			}
			if(!countors){
				for(int p: node.getPixels()){
					imgOut.setPixel(p, node.getLevel());
				}
			}else{
				for(int p: node.contour.getPixels()){
					imgOut.setPixel(p, 255);
				}
			}
			for(InfoPrunedTree.NodePrunedTree son: node_.getChildren()){
				fifo.enqueue( son );	
			}
			
		}
		return imgOut;
	}
	

	public InfoPrunedTree getPrunedTree(double attributeValue, int type, int typePruning){
		long ti = System.currentTimeMillis();
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		for(NodeToS no: getListNodes()){
			if(! (getAttribute(no, type) <= attributeValue) ){ //poda
				
				prunedTree.addNodeNotPruned(no);
			}
		}
		System.out.println("Tempo de execucao [Tree of shapes - filtering by pruning]  "+ ((System.currentTimeMillis() - ti) /1000.0)  + "s");
		
		return prunedTree;
	}
	
	

	/**
	 * Cria uma imagem filtrada. 
	 * Obs: Para gerar a imagem filtrada nao eh alterado a estrutura original da arvore 
	 * @param attributeValue - valor do atributo
	 * @param idAttribute - tipo do atributo
	 * @return imagem filtrada
	 */
	public GrayScaleImage filteringByPruning(double attributeValue, int type){
		long ti = System.currentTimeMillis();
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput);;
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(this.root);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			//double bb = no.getArea() / ((double) no.getWidthNode() * no.getHeightNode());
			if(getAttribute(no, type) <= attributeValue){// && bb > 0.85){ // ){ //poda
				
				if(!isLine){
					int levelPropagation = no.parent == null ? no.level : no.parent.level;
					//propagacao do nivel do pai para os filhos 
					Queue<NodeToS> fifoPruning = new Queue<NodeToS>();
					fifoPruning.enqueue(no);	
					while(!fifoPruning.isEmpty()){
						NodeToS nodePruning = fifoPruning.dequeue();
						for(NodeToS song: nodePruning.children){
							fifoPruning.enqueue(song);
						}
						for(Integer p: nodePruning.getPixels())
							imgOut.setPixel(p, levelPropagation);
						
					}
				}else{
					if(isLine){
						if(no.parent != null)
							for(Integer p: no.parent.contour.getPixels()){
								imgOut.setPixel(p, 255);
							}
					}
				}
			}
			else{
				if(isLine){
					for(Integer p: no.contour.getPixels()){
						imgOut.setPixel(p, 255);
					}
				}else{
					for(Integer p: no.getPixels()){
						imgOut.setPixel(p, no.level);
					}
					
				}
				
				if(no.children != null){
					for(NodeToS son: no.children){
						fifo.enqueue(son);	 
					}
				}	
			}
		}
		if(Utils.debug)
			System.out.println("Tempo de execucao [tree of shapes - pruning - "+ ((System.currentTimeMillis() - ti) /1000.0)  + "s");

		return imgOut;//paintEdges(imgOut);
	}
	
	
	
	
	/**
	 * Cria uma imagem filtrada. 
	 * Obs: Para gerar a imagem filtrada nao eh alterado a estrutura original da arvore 
	 * @param attributeValue - valor do atributo
	 * @param idAttribute - tipo do atributo
	 * @return imagem filtrada
	 */
	public GrayScaleImage filtering(double attributeValue, int type, int typePruning){
		if(typePruning == IMorphologicalTreeFiltering.EXTINCTION_VALUE)
			return filteringExtinctionValue(attributeValue, type);
		else if(typePruning == IMorphologicalTreeFiltering.PRUNING)
			return filteringByPruning(attributeValue, type);
		else	
			return filteringByPruning(attributeValue, type);
	}
	
	
	ArrayList<ExtinctionValueNode> extincaoPorNode;
	ComputerExtinctionValueToS extinctionValue;
	public GrayScaleImage filteringExtinctionValue(double attributeValue, int type){
		long ti = System.currentTimeMillis();
		if(extinctionValue == null){
			extinctionValue = new ComputerExtinctionValueToS(this);
			extincaoPorNode = extinctionValue.getExtinctionValueCut(attributeValue, type);
		}
		if(extinctionValue.getType() != type)
			extincaoPorNode = extinctionValue.getExtinctionValueCut(attributeValue, type);
		
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput);;
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(getRoot());
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			for(Integer p: no.getPixels()){
				imgOut.setPixel(p, no.level);
			}
			if(no.children != null){
				for(NodeToS son: no.children){
					fifo.enqueue(son);	 
				}
			}	
		}
		
		for(int k=extincaoPorNode.size()-1; k >= 0 ; k--){
			NodeToS no = extincaoPorNode.get(k).node;

			if(extincaoPorNode.get(k).extinctionValue <= attributeValue){ //poda
				int levelPropagation = no.level;
				//propagacao do nivel do pai para os filhos 
				Queue<NodeToS> fifoPruning = new Queue<NodeToS>();
				fifoPruning.enqueue(no);	
				while(!fifoPruning.isEmpty()){
					NodeToS nodePruning = fifoPruning.dequeue();
					if(nodePruning.children != null){ 
						for(NodeToS song: nodePruning.children){ 
							fifoPruning.enqueue(song);	 
						}
					}
					for(Integer p: nodePruning.getPixels()){
						imgOut.setPixel(p, levelPropagation);
					}
				}
			}
			
		}
		
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [tree of shape - extinction value - direct]  "+ ((tf - ti) /1000.0)  + "s");
		}
		return imgOut;
	}
	
	

	/**
	 * Metodo utilizado para criar uma instancia da maxtree. 
	 * Os atributos computados sao crescentes
	 * @param img - imagem de entrada
	 * @return Maxtree
	 */
	public void computerMoment(NodeToS root){
		root.initMoment();
		if(root.children != null){ //computa os atributos para os filhos
			for(NodeToS son: root.children){
				computerMoment(son);
				root.updateMoment(son.moment);
			}
		}
	}

	
	
}



class ThreadNodeToSPerimeter extends Thread {
	NodeToS node;
	ContourTracer contour;
	public ThreadNodeToSPerimeter(NodeToS node, int w, int h){
		this.node = node;
		this.contour = new ContourTracer(w, h, true);
	}
		
	public void run() {
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(node);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			if(no.getChildren() != null){
				for(NodeToS son: no.getChildren()){
					fifo.enqueue(son);
				}
			}
			for(Integer p: no.getPixels()){
				contour.addPixelForeground(p);
			}
		}
		node.contour = contour.findOuterContours();
		node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_PERIMETER] = (int) node.contour.getPerimeter();
		//System.out.println("terminou: " + node.getId());
	}
	
}
