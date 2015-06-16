package mmlib4j.representation.tree.tos;

import java.util.ArrayList;
import java.util.HashSet;

import mmlib4j.datastruct.Queue;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.IMorphologicalTreeFiltering;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.attribute.ComputerAttributeBasedPerimeterExternal;
import mmlib4j.representation.tree.attribute.ComputerBasicAttribute;
import mmlib4j.representation.tree.attribute.ComputerCentralMomentAttribute;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValueTreeOfShapes;
import mmlib4j.representation.tree.attribute.ComputerPatternEulerAttribute;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValueTreeOfShapes.ExtinctionValueNode;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ConnectedFilteringByTreeOfShape extends TreeOfShape implements IMorphologicalTreeFiltering{
	
	public ConnectedFilteringByTreeOfShape(GrayScaleImage img){
		super(img, -1, -1);
	}
	
	protected ConnectedFilteringByTreeOfShape(BuilderTreeOfShapeByUnionFind build){
		super(build);
	}
	
	public ConnectedFilteringByTreeOfShape(GrayScaleImage img, int xInfinito, int yInfinito){
		super(img, xInfinito, yInfinito);		
	}
	
	public HashSet<NodeToS> getListNodes(){
		return listNode; 
	}


	public void loadAttribute(){
		long ti = System.currentTimeMillis();
		new ComputerBasicAttribute(numNode, getRoot(), imgInput).addAttributeInNodesToS(getListNodes());
		new ComputerAttributeBasedPerimeterExternal(numNode, getRoot(), getInputImage()).addAttributeInNodesToS(getListNodes());
		new ComputerCentralMomentAttribute(numNode, getRoot(), imgInput.getWidth()).addAttributeInNodesToS(getListNodes());
		new ComputerPatternEulerAttribute(numNode, getRoot(), imgInput, adj).addAttributeInNodesToS(getListNodes());
		long tf = System.currentTimeMillis();
		if(Utils.debug)
			System.out.println("Tempo de execucao [computer attributes] "+ ((tf - ti) /1000.0)  + "s");
	}
	
	private double getAttribute(NodeToS node, int type){
		return node.getAttributeValue(type);
	}
	
	public GrayScaleImage reconstruction(InfoPrunedTree prunedTree){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(getInputImage());
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
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput);;
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
		if(typePruning == IMorphologicalTreeFiltering.EXTINCTION_VALUE)
			return filteringExtinctionValue(attributeValue, type);
		else	
			return filteringByPruning(attributeValue, type);
	}
	
	
	ArrayList<ExtinctionValueNode> extincaoPorNode;
	ComputerExtinctionValueTreeOfShapes extinctionValue;
	public GrayScaleImage filteringExtinctionValue(double attributeValue, int type){
		long ti = System.currentTimeMillis();
		if(extinctionValue == null){
			extinctionValue = new ComputerExtinctionValueTreeOfShapes(this);
			extincaoPorNode = extinctionValue.getExtinctionValueCut(attributeValue, type);
		}
		if(extinctionValue.getType() != type)
			extincaoPorNode = extinctionValue.getExtinctionValueCut(attributeValue, type);
		
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput);;
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