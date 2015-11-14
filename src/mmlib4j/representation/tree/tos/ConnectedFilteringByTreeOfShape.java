package mmlib4j.representation.tree.tos;

import java.util.ArrayList;
import java.util.HashSet;

import mmlib4j.datastruct.Queue;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.ComputerAttributeBasedPerimeterExternal;
import mmlib4j.representation.tree.attribute.ComputerBasicAttribute;
import mmlib4j.representation.tree.attribute.ComputerCentralMomentAttribute;
import mmlib4j.representation.tree.attribute.ComputerDistanceTransform;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValueTreeOfShapes;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValueTreeOfShapes.ExtinctionValueNode;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ConnectedFilteringByTreeOfShape extends TreeOfShape implements MorphologicalTreeFiltering{
	
	private boolean hasComputerBasicAttribute = false;
	private boolean hasComputerAttributeBasedPerimeterExternal = false;
	private boolean hasComputerCentralMomentAttribute = false;
	private boolean hasComputerPatternEulerAttribute = false;
	private boolean hasComputerDistanceTransform = false;
	private ComputerDistanceTransform dt = null;
	
	public ConnectedFilteringByTreeOfShape(GrayScaleImage img){
		super(img, -1, -1);
		computerBasicAttribute();
	}
	
	protected ConnectedFilteringByTreeOfShape(BuilderTreeOfShapeByUnionFind build){
		super(build);
	}
	
	public ConnectedFilteringByTreeOfShape(GrayScaleImage img, int xInfinito, int yInfinito){
		super(img, xInfinito, yInfinito);
		computerBasicAttribute();
	}
	
	public HashSet<NodeToS> getListNodes(){
		return listNode; 
	}
	



	public ComputerDistanceTransform computerDistanceTransform(){
		if(!hasComputerDistanceTransform){
			long ti = System.currentTimeMillis();
			dt = new ComputerDistanceTransform(numNode, getRoot(), imgInput);
			hasComputerDistanceTransform = true;
			if(Utils.debug){
				long tf = System.currentTimeMillis();
				System.out.println("Tempo de execucao [computer distance transform] "+ ((tf - ti) /1000.0)  + "s");
			}
		}
		return dt;
	}
	
	public void computerPatternEulerAttribute(){
		if(!hasComputerPatternEulerAttribute){
			long ti = System.currentTimeMillis();
			for(NodeToS node: getListNodes()){
				node.addAttribute(Attribute.NUM_HOLES, new Attribute(Attribute.NUM_HOLES, node.getNumHoles()));
			}
			hasComputerPatternEulerAttribute = true;
			if(Utils.debug){
				long tf = System.currentTimeMillis();
				System.out.println("Tempo de execucao [attribute euler] "+ ((tf - ti) /1000.0)  + "s");
			}
		}
	}

	public void computerCentralMomentAttribute(){
		if(!hasComputerCentralMomentAttribute){
			new ComputerCentralMomentAttribute(numNode, getRoot(), imgInput.getWidth()).addAttributeInNodesToS(getListNodes());
			hasComputerCentralMomentAttribute = true;
		}
	}
	
	public void computerBasicAttribute(){
		if(!hasComputerBasicAttribute){
			long ti = System.currentTimeMillis();
			new ComputerBasicAttribute(numNode, getRoot(), imgInput).addAttributeInNodesToS(getListNodes());
			hasComputerBasicAttribute = true;
			if(Utils.debug){
				long tf = System.currentTimeMillis();
				System.out.println("Tempo de execucao [basic attribute] "+ ((tf - ti) /1000.0)  + "s");
			}
		}
	}
	
	public void computerAttributeBasedPerimeterExternal(){
		if(!hasComputerAttributeBasedPerimeterExternal){
			long ti = System.currentTimeMillis();
			new ComputerAttributeBasedPerimeterExternal(numNode, getRoot(), getInputImage()).addAttributeInNodesToS(getListNodes());
			hasComputerAttributeBasedPerimeterExternal = true;
			if(Utils.debug){
				long tf = System.currentTimeMillis();
				System.out.println("Tempo de execucao [external perimeter] "+ ((tf - ti) /1000.0)  + "s");
			}
		}
	}
		

	public void loadAttribute(int attr){
		switch(attr){
			case Attribute.ALTITUDE:
			case Attribute.AREA:
			case Attribute.VOLUME:
			case Attribute.WIDTH:
			case Attribute.HEIGHT:
			case Attribute.PERIMETER:
			case Attribute.LEVEL:
			case Attribute.RECTANGULARITY:
			case Attribute.RATIO_WIDTH_HEIGHT:
				computerBasicAttribute();
				break;
				
			case Attribute.MOMENT_CENTRAL_02:
			case Attribute.MOMENT_CENTRAL_20:
			case Attribute.MOMENT_CENTRAL_11:
			case Attribute.VARIANCE_LEVEL:
			case Attribute.LEVEL_MEAN:
			case Attribute.MOMENT_COMPACTNESS:
			case Attribute.MOMENT_ECCENTRICITY:
			case Attribute.MOMENT_ELONGATION:
			case Attribute.MOMENT_LENGTH_MAJOR_AXES:
			case Attribute.MOMENT_LENGTH_MINOR_AXES:
			case Attribute.MOMENT_ORIENTATION:
			case Attribute.MOMENT_ASPECT_RATIO:
				computerCentralMomentAttribute();
				break;
			
			case Attribute.PERIMETER_EXTERNAL:
			case Attribute.CIRCULARITY:
			case Attribute.COMPACTNESS:
			case Attribute.ELONGATION:
				computerAttributeBasedPerimeterExternal();
				break;
				
			case Attribute.NUM_HOLES:
				computerPatternEulerAttribute();
				break;
				
		}
	}
	
	
	private double getAttribute(NodeToS node, int type){
		loadAttribute(type);
		return node.getAttributeValue(type);
	}
	
	public GrayScaleImage reconstruction(InfoPrunedTree prunedTree){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<InfoPrunedTree.NodePrunedTree> fifo = new Queue<InfoPrunedTree.NodePrunedTree>();
		fifo.enqueue( prunedTree.getRoot() );
		while(!fifo.isEmpty()){
			InfoPrunedTree.NodePrunedTree node_ = fifo.dequeue();
			NodeToS node = (NodeToS) node_.getInfo();
			for(NodeToS son: node.getChildren()){
				if(prunedTree.wasPruned(son)){
					for(int p: son.getPixelsOfCC()){
						imgOut.setPixel(p, node.getLevel());
					}
				}
			}
			for(int p: node.getCanonicalPixels()){
				imgOut.setPixel(p, node.getLevel());
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
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(this.root);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			//double bb = no.getArea() / ((double) no.getWidthNode() * no.getHeightNode());
			if(getAttribute(no, type) <= attributeValue){// && bb > 0.85){ // ){ //poda
				int levelPropagation = no.parent == null ? no.level : no.parent.level;
				//propagacao do nivel do pai para os filhos 
				Queue<NodeToS> fifoPruning = new Queue<NodeToS>();
				fifoPruning.enqueue(no);	
				while(!fifoPruning.isEmpty()){
					NodeToS nodePruning = fifoPruning.dequeue();
					for(NodeToS song: nodePruning.children){
						fifoPruning.enqueue(song);
					}
					for(Integer p: nodePruning.getCanonicalPixels())
						imgOut.setPixel(p, levelPropagation);
				}
			}
			else{
				for(Integer p: no.getCanonicalPixels()){
					imgOut.setPixel(p, no.level);
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
		if(typePruning == MorphologicalTreeFiltering.PRUNING_EXTINCTION_VALUE)
			return filteringExtinctionValue(attributeValue, type);
		else	
			return filteringByPruning(attributeValue, type);
	}
	
	
	ArrayList<ExtinctionValueNode> extincaoPorNode;
	ComputerExtinctionValueTreeOfShapes extinctionValue;
	public GrayScaleImage filteringExtinctionValue(double attributeValue, int type){
		loadAttribute(type);
		long ti = System.currentTimeMillis();
		if(extinctionValue == null){
			extinctionValue = new ComputerExtinctionValueTreeOfShapes(this);
			extincaoPorNode = extinctionValue.getExtinctionValueCut(attributeValue, type);
		}
		if(extinctionValue.getType() != type)
			extincaoPorNode = extinctionValue.getExtinctionValueCut(attributeValue, type);
		
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());;
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(getRoot());
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
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
					for(Integer p: nodePruning.getCanonicalPixels()){
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
	
}