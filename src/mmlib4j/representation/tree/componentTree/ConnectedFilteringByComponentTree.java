package mmlib4j.representation.tree.componentTree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import mmlib4j.datastruct.Queue;
import mmlib4j.filtering.binary.ContourTracer;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.IMorphologicalTreeFiltering;
import mmlib4j.representation.tree.INodeTree;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.attribute.BitQuadsNodesTree;
import mmlib4j.representation.tree.pruningStrategy.ComputerExtinctionValueCT;
import mmlib4j.representation.tree.pruningStrategy.ComputerMserCT;
import mmlib4j.representation.tree.pruningStrategy.ComputerTbmrCT;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedGradualTransition;
import mmlib4j.representation.tree.pruningStrategy.ComputerExtinctionValueCT.ExtinctionValueNode;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.Utils;


/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ConnectedFilteringByComponentTree extends ComponentTree implements IMorphologicalTreeFiltering{
	
	public ConnectedFilteringByComponentTree(GrayScaleImage img, AdjacencyRelation adj, boolean isMaxtree, boolean isCountours){
		super(img, adj, isMaxtree);
		long ti = System.currentTimeMillis();
		computerAttribute(this.root);
		computerMoment(this.root);
		//computerAttributePattern(this.root);
		computerAttributeEuler(this.root);
		if(isCountours)
			setContours(false);
		long tf = System.currentTimeMillis();
		if(Utils.debug)
			System.out.println("Tempo de execucao [computer attributes] "+ ((tf - ti) /1000.0)  + "s");
	}
	
	public ConnectedFilteringByComponentTree(GrayScaleImage img, AdjacencyRelation adj, boolean isMaxtree){
		super(img, adj, isMaxtree);
		long ti = System.currentTimeMillis();
		computerAttribute(this.root);
		computerMoment(this.root);
		//computerAttributePattern(this.root);
		computerAttributeEuler(root);
		//setContours(false);
		long tf = System.currentTimeMillis();
		if(Utils.debug)
			System.out.println("Tempo de execucao [computer attributes] "+ ((tf - ti) /1000.0)  + "s");
	}
	
	public ConnectedFilteringByComponentTree(ComponentTree c) {
		super(c);
		long ti = System.currentTimeMillis();
		computerAttribute(this.root);
		computerMoment(this.root);
		//computerAttributePattern(this.root);
		//setContours(false);
		long tf = System.currentTimeMillis();
		if(Utils.debug)
			System.out.println("Tempo de execucao [computer attributes] "+ ((tf - ti) /1000.0)  + "s");
		
	}
	
	
	public void computerMoment( ){
		computerMoment(this.root);
	}
		
	public void simplificationByCriterion(int alpha){
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(this.root);
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			if(no != this.root && Math.abs(no.level - no.parent.level) <= alpha){ //poda
				//merge
				NodeCT parent = no.parent; 
				for(int p: no.pixels)
					parent.addPixel(p);
				
				
				parent.children.remove(no);
				for(NodeCT son: no.children){
					parent.children.add(son);
					son.parent = parent;
					fifo.enqueue(son);
				}
				
			}else{
				for(NodeCT son: no.children){
					fifo.enqueue(son);
				}
			}
		}
		
		createNodesMap();
		computerHeightNodes(this.root, 0);
		computerAttribute(this.root);
		computerMoment(this.root);
		computerAttributePattern(this.root);
	}
	
	
	
	
	/**
	 * Cria uma imagem filtrada. 
	 * Obs: Para gerar a imagem filtrada nao eh alterado a estrutura original da arvore 
	 * @param attributeValue - valor do atributo
	 * @param idAttribute - tipo do atributo
	 * @return imagem filtrada
	 */
	public GrayScaleImage filtering(double attributeValue, int type, int typePruning, int typeRec){
		if(typePruning == IMorphologicalTreeFiltering.EXTINCTION_VALUE)
			return filteringByExtinctionValue(attributeValue, type);
		else if(typePruning == IMorphologicalTreeFiltering.PRUNING)
			return filteringByPruning(attributeValue, type);
		
		throw new RuntimeException("type filtering invalid");
	}
	
	
	ArrayList<ExtinctionValueNode> extincaoPorNode;
	ComputerExtinctionValueCT extinctionValue;
	public GrayScaleImage filteringByExtinctionValue(double attributeValue, int type){
		long ti = System.currentTimeMillis();
		if(extinctionValue == null){
			extinctionValue = new ComputerExtinctionValueCT(this);
			extincaoPorNode = extinctionValue.getExtinctionValueCut(type);
		}
		if(extinctionValue.getType() != type)
			extincaoPorNode = extinctionValue.getExtinctionValueCut(type);
		
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput);;
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(getRoot());
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			
			if(isLine){
				for(Integer p: no.contour.getPixels()){
					imgOut.setPixel(p, 255);
				}
			}else{
				for(Integer p: no.getPixels()){
					imgOut.setPixel(p, no.level);
				}
			}
			
			for(NodeCT son: no.children){
					fifo.enqueue(son);	 
			}	
		}
		
		for(int k=extincaoPorNode.size()-1; k >= 0 ; k--){
			NodeCT no = extincaoPorNode.get(k).node;

			if(extincaoPorNode.get(k).extinctionValue <= attributeValue){ //poda
				int levelPropagation = no.level;
				//propagacao do nivel do pai para os filhos 
				Queue<NodeCT> fifoPruning = new Queue<NodeCT>();
				fifoPruning.enqueue(no);	
				while(!fifoPruning.isEmpty()){
					NodeCT nodePruning = fifoPruning.dequeue();
					if(nodePruning.children != null){ 
						for(NodeCT song: nodePruning.children){ 
							fifoPruning.enqueue(song);	 
						}
					}
					if(isLine){
						for(Integer p: nodePruning.contour.getPixels()){
							imgOut.setPixel(p, 0);
						}
					}else{	
						for(Integer p: nodePruning.getPixels())
							imgOut.setPixel(p, levelPropagation);
					}
				}
			}
			
		}
		
		long tf = System.currentTimeMillis();
		if(Utils.debug)
			System.out.println("Tempo de execucao [Componente tree - extinction value - direct]  "+ ((tf - ti) /1000.0)  + "s");
		
		return imgOut;
	}
	
	private double getAttribute(NodeCT node, int type){
		if(type == IMorphologicalTreeFiltering.ATTRIBUTE_ECCENTRICITY)
			return node.moment.eccentricity();
		else if(type == IMorphologicalTreeFiltering.ATTRIBUTE_MAJOR_AXES)
			return node.moment.elongation();//return node.moment.getLengthMajorAxes();
		else if(type == IMorphologicalTreeFiltering.ATTRIBUTE_ORIENTATION)
			return node.moment.getMomentOrientation();
		else if(type == IMorphologicalTreeFiltering.ATTRIBUTE_CIRCULARITY)
			return  node.getCircularity();
		else if(type == IMorphologicalTreeFiltering.ATTRIBUTE_ELONGATION)
			return node.moment.elongation();
		else
			return node.attributeValue[type];
	}
	
	
	/**
	 * Cria uma imagem filtrada. 
	 * Obs: Para gerar a imagem filtrada nao eh alterado a estrutura original da arvore 
	 * @param attributeValue - valor do atributo
	 * @param idAttribute - tipo do atributo
	 * @return imagem filtrada
	 */
	public GrayScaleImage filteringByPruning(double attributeValue, int type){
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(this.root);
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		for(NodeCT no: listNode){
			if( !(getAttribute(no, type) <= attributeValue) ){ //nao poda
				prunedTree.addNodeNotPruned(no);
			}
		}
		return prunedTree.reconstruction(isLine);
	}
	
	public InfoPrunedTree getPrunedTreeByExtinctionValue(double attributeValue, int type){
		long ti = System.currentTimeMillis();
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		
		extinctionValue = new ComputerExtinctionValueCT(this);
		extincaoPorNode = extinctionValue.getExtinctionValueCut(type);
		if(Utils.debug)
			System.out.println("EV: Tempo1: "+ ((System.currentTimeMillis() - ti) /1000.0)  + "s");
		
		boolean resultPruning[] = new boolean[this.getNumNode()];
		for(ExtinctionValueNode nodeEV: extincaoPorNode){
			
			if(nodeEV.nodeAncestral.getAttributeValue(type) < attributeValue){ //poda				
				NodeCT node = nodeEV.nodeAncestral;	
				for(NodeCT song: node.children){
					if(!resultPruning[song.getId()])
						for(NodeCT n: song.getNodesDescendants())
							resultPruning[n.getId()] = true;	 
				}
				
			}
		}
		if(Utils.debug)
			System.out.println("EV: Tempo2: "+ ((System.currentTimeMillis() - ti) /1000.0)  + "s");
		
		for(NodeCT no: listNode){
			if( ! resultPruning[no.getId()]  ){ //nao poda
				prunedTree.addNodeNotPruned(no);
			}
		}
		if(Utils.debug)
			System.out.println("EV: Tempo3: "+ ((System.currentTimeMillis() - ti) /1000.0)  + "s");
		
		long tf = System.currentTimeMillis();
		if(Utils.debug)
			System.out.println("Tempo de execucao [Componente tree - filtering by pruning extinction value]  "+ ((tf - ti) /1000.0)  + "s");
		
		return prunedTree;
	}
	

	public GrayScaleImage reconstruction(InfoPrunedTree prunedTree){
		return reconstruction(prunedTree, false);
	}
	
	public GrayScaleImage reconstruction(InfoPrunedTree prunedTree, boolean countor){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(getInputImage());
		Queue<InfoPrunedTree.NodePrunedTree> fifo = new Queue<InfoPrunedTree.NodePrunedTree>();
		fifo.enqueue( prunedTree.getRoot() );
		while(!fifo.isEmpty()){
			InfoPrunedTree.NodePrunedTree node_ = fifo.dequeue();
			NodeCT node = (NodeCT) node_.getInfo();
			for(NodeCT son: node.getChildren()){
				if(prunedTree.wasPruned(son)){
					if(!countor){
						for(int p: son.getPixelsOfCC()){
							imgOut.setPixel(p, node.getLevel());
						}
					}
				}
			}
			if(!countor){
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
	
	/**
	 * Cria uma imagem filtrada. 
	 * Obs: Para gerar a imagem filtrada nao eh alterado a estrutura original da arvore 
	 * @param attributeValue - valor do atributo
	 * @param idAttribute - tipo do atributo
	 * @return imagem filtrada
	 */
	public InfoPrunedTree getPrunedTree(double attributeValue, int type){
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		for(NodeCT no: listNode){
			if( !(getAttribute(no, type) <= attributeValue) ){ //nao poda
				prunedTree.addNodeNotPruned(no);
			}
		}
		return prunedTree;
	}
	
	public InfoPrunedTree getPrunedTreeByMSER(double attributeValue, int type, int delta){
		long ti = System.currentTimeMillis();
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		ComputerMserCT mser = new ComputerMserCT(this);
		boolean resultPruning[] = new boolean[this.getNumNode()];
		LinkedList<NodeCT> list = mser.getNodesByMSER(delta);
		for(NodeCT node: list){
			if(node.getAttributeValue(type) <= attributeValue){ //poda				
				for(NodeCT song: node.children){
					for(NodeCT n: song.getNodesDescendants())
						resultPruning[n.getId()] = true;	 
				}
			}
		}
		
		for(NodeCT no: listNode){
			if( ! resultPruning[no.getId()]  ){ //nao poda
				prunedTree.addNodeNotPruned(no);
			}
		}
		
		long tf = System.currentTimeMillis();
		if(Utils.debug)
		System.out.println("Tempo de execucao [Componente tree - filtering by pruning mser]  "+ ((tf - ti) /1000.0)  + "s");
		
		return prunedTree;
	}
	
	
	
	public InfoPrunedTree getPrunedTreeByGradualTransition(double attributeValue, int type, int delta){
		long ti = System.currentTimeMillis();
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		PruningBasedGradualTransition gt = new PruningBasedGradualTransition(this, type, delta); 
		boolean resultPruning[] = gt.getMappingSelectedNodes( );
		LinkedList<INodeTree> list = gt.getListOfSelectedNodes( );
		for(INodeTree obj: list){
			NodeCT node = (NodeCT) obj;
			if(node.getAttributeValue(type) <= attributeValue){ //poda				
				for(NodeCT song: node.children){
					for(NodeCT n: song.getNodesDescendants())
						resultPruning[n.getId()] = true;	 
				}
			}
		}
		
		for(NodeCT no: listNode){
			if( ! resultPruning[no.getId()]  ){ //nao poda
				prunedTree.addNodeNotPruned(no);
			}
		}
		
		long tf = System.currentTimeMillis();
		if(Utils.debug)
		System.out.println("Tempo de execucao [Componente tree - filtering by pruning gradual transition]  "+ ((tf - ti) /1000.0)  + "s");
		
		return prunedTree;
	}
	
	public InfoPrunedTree getPrunedTreeByTBMR(double attributeValue, int type, int tMin, int tMax){
		long ti = System.currentTimeMillis();
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, getRoot(), getNumNode(), type, attributeValue);
		ComputerTbmrCT tbmr = new ComputerTbmrCT(this);
		boolean resultPruning[] = new boolean[this.getNumNode()];
		boolean result[] = tbmr.getSelectedNode(tMin, tMax);
		for(NodeCT node: listNode){
			if(node.getAttributeValue(type) <= attributeValue && result[node.getId()]){ //poda				
				for(NodeCT song: node.children){
					for(NodeCT n: song.getNodesDescendants())
						resultPruning[n.getId()] = true;	 
				}
			}
		}
		
		for(NodeCT no: listNode){
			if( ! resultPruning[no.getId()]  ){ //nao poda
				prunedTree.addNodeNotPruned(no);
			}
		}
		
		long tf = System.currentTimeMillis();
		if(Utils.debug)
		System.out.println("Tempo de execucao [Componente tree - filtering by pruning mser]  "+ ((tf - ti) /1000.0)  + "s");
		
		return prunedTree;
	}
	
	
	boolean isLine = false;
	boolean processedLine = false;
	public void setContours(boolean b){
		isLine = b;
		if(!processedLine){
			pool =  new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
			processedLine = true;
			long ti = System.currentTimeMillis();
			computerCountors(root);
			while(pool.getActiveCount() != 0);
			long tf = System.currentTimeMillis();
			if(Utils.debug)
			System.out.println("Tempo de execucao [component tree - extraction of countors]  "+ ((tf - ti) /1000.0)  + "s");
		}
	}
	/**
	 * Metodo utilizado para criar uma instancia da maxtree. 
	 * Os atributos computados sao crescentes
	 * @param img - imagem de entrada
	 * @return Maxtree
	 */
	public void computerCountors(NodeCT root){
		if(!root.children.isEmpty()){ 			
			for(NodeCT son: root.children){
				computerCountors(son);
			}
		}
		//perimetro
		pool.execute(new ThreadNodeCTPerimeter(root, imgInput.getWidth(), imgInput.getHeight()));
	}
	
	
	
	/**
	 * Metodo utilizado para criar uma instancia da maxtree. 
	 * Os atributos computados sao crescentes
	 * @param img - imagem de entrada
	 * @return Maxtree
	 */
	public void computerAttribute(NodeCT node){
		node.initAttributes(NUM_ATTRIBUTES);
		for(NodeCT son: node.children){
			computerAttribute(son);
			
			
			
			if(son.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MAX] > node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MAX]){
				node.pixelYmax = son.pixelYmax;
			}
			if(son.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MAX] > node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MAX]){
				node.pixelXmax = son.pixelXmax;
			}
			if(son.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MIN] < node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MIN]){
				node.pixelYmin = son.pixelYmin;
			}
			if(son.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MIN] < node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MIN]){
				node.pixelXmin = son.pixelXmin;
			}
			node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MAX] = Math.max(node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MAX], son.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MAX]); 
			node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MAX] = Math.max(node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MAX], son.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MAX]);
			
			node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MIN] = Math.min(node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MIN], son.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MIN]); 
			node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MIN] = Math.min(node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MIN], son.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MIN]);
			
			node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_AREA] += son.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_AREA]; //area
			node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_VOLUME] += son.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_VOLUME]; //volume
			
			node.highest = Math.max(node.highest, son.highest);
			node.lowest = Math.min(node.lowest, son.lowest);
		}
		
		node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_WIDTH] = node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MAX] - node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_X_MIN] + 1;
		node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_HEIGHT] = node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MAX] - node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_Y_MIN] + 1;
		if(isMaxtree){
			if(node.isLeaf())
				node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_ALTITUDE] = node.level;
			else
				node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_ALTITUDE] = node.highest - node.level  + 1;
		}
		else{
			if(node.isLeaf())
				node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_ALTITUDE] = node.level;
			else
				node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_ALTITUDE] = node.level - node.lowest + 1;
		}
	}
	
	
	
	/**
	 * Metodo utilizado para criar uma instancia da maxtree. 
	 * Os atributos computados sao crescentes
	 * @param img - imagem de entrada
	 * @return Maxtree
	 */
	public void computerMoment(NodeCT root){
		root.initMoment();
		if(root.children != null){ //computa os atributos para os filhos
			for(NodeCT son: root.children){
				computerMoment(son);
				root.moment.updateMoment(son.moment);
			}
		}
	}
	
	
	
	public void computerAttributeEuler(NodeCT root){
		root.initAttributeEuler();
		
		for(NodeCT son: root.children){
			computerAttributeEuler(son);
			/*root.attributeEuler.countPatternC1 += son.attributeEuler.countPatternC1;
			root.attributeEuler.countPatternC2 += son.attributeEuler.countPatternC2;
			root.attributeEuler.countPatternC3 += son.attributeEuler.countPatternC3;
			root.attributeEuler.countPatternC4 += son.attributeEuler.countPatternC4;*/
		}
		
		//computacao dos padroes
		for(int p: root.pixels){
			root.attributeEuler.addPixel(p, imgInput, adj);
		}
		
		for(NodeCT son: root.children){	
			root.attributeEuler.merge( son.attributeEuler );
		}
		
		
		
		
	}
	
	
	
	
	private BitQuadsNodesTree bitQuads = null;
	public void computerAttributePattern(NodeCT node){
		if(bitQuads == null)
			bitQuads = new BitQuadsNodesTree(imgInput.getWidth(), imgInput.getHeight());
		
		node.initAttributePattern();
		
		for(NodeCT son: node.children){
			computerAttributePattern(son);
			node.attributePattern.merge(son.attributePattern);
		}
		
		//atualiza os pixels
		for(int p: node.pixels){ 
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

	
	
	
	
}



class ThreadNodeCTPerimeter extends Thread {
	NodeCT node;
	ContourTracer contour;
	public ThreadNodeCTPerimeter(NodeCT node, int w, int h){
		this.node = node;
		this.contour = new ContourTracer(w, h, true, node.isMaxtree, node.img, node.level);
		//this.contour = new ContourTracer(w, h, true);
	}
		
	public void run() {
		/*Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(node);
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			if(no.getChildren() != null){
				for(NodeCT son: no.getChildren()){
					fifo.enqueue(son);
				}
			}
			for(Integer p: no.getPixels()){
				contour.addPixelForeground(p);
			}
		}*/
		node.contour = contour.findOuterContours(node.pixelYmin % node.img.getWidth(), node.pixelYmin / node.img.getWidth() );
		//node.contour = contour.findOuterContours();
		node.attributeValue[IMorphologicalTreeFiltering.ATTRIBUTE_PERIMETER] = (int) node.contour.getPerimeter();
		//System.out.println("terminou: " + node.getId());
	}
	
}

