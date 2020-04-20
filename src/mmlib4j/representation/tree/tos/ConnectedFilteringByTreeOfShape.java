package mmlib4j.representation.tree.tos;

import java.io.File;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleArrayList;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.ComputerAttributeBasedPerimeterExternal;
import mmlib4j.representation.tree.attribute.ComputerBasicAttribute;
import mmlib4j.representation.tree.attribute.ComputerBasicAttributeUpdate;
import mmlib4j.representation.tree.attribute.ComputerCentralMomentAttribute;
import mmlib4j.representation.tree.attribute.ComputerCentralMomentAttributeUpdate;
import mmlib4j.representation.tree.attribute.ComputerDistanceTransform;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValueTreeOfShapes;
import mmlib4j.representation.tree.attribute.ComputerExtinctionValueTreeOfShapes.ExtinctionValueNode;
import mmlib4j.utils.ImageAlgebra;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
@Deprecated
public class ConnectedFilteringByTreeOfShape extends TreeOfShape implements MorphologicalTreeFiltering{
	
	private boolean hasComputerBasicAttribute = false;
	private boolean hasComputerAttributeBasedPerimeterExternal = false;
	private boolean hasComputerCentralMomentAttribute = false;
	private boolean hasComputerDistanceTransform = false;
	private ComputerDistanceTransform dt = null;
	
	public ConnectedFilteringByTreeOfShape(GrayScaleImage img){
		super(img, -1, -1);
		computerBasicAttribute();
	}
	
	public ConnectedFilteringByTreeOfShape(BuilderTreeOfShape build){
		super(build);
		computerBasicAttribute();
	}
	
	public ConnectedFilteringByTreeOfShape(GrayScaleImage img, int xInfinito, int yInfinito){
		super(img, xInfinito, yInfinito);
		computerBasicAttribute();
	}
	
	public SimpleLinkedList<NodeLevelSets> getListNodes(){
		return listNode; 
	}
	
	public ComputerDistanceTransform computerDistanceTransform(){
		if(!hasComputerDistanceTransform){
			dt = new ComputerDistanceTransform(numNode, getRoot(), imgInput);
			hasComputerDistanceTransform = true;
		}
		return dt;
	}
	
	
	public void computerCentralMomentAttribute(){
		if(!hasComputerCentralMomentAttribute){
			new ComputerCentralMomentAttribute(numNode, getRoot(), imgInput.getWidth()).addAttributeInNodes(getListNodes());
			hasComputerCentralMomentAttribute = true;
		}
	}
	
	public void computerBasicAttribute(){
		if(!hasComputerBasicAttribute){
			new ComputerBasicAttribute(numNode, getRoot(), imgInput).addAttributeInNodes(getListNodes());
			hasComputerBasicAttribute = true;
		}
	}
	
	public void computerAttributeBasedPerimeterExternal(){
		if(!hasComputerAttributeBasedPerimeterExternal){
			new ComputerAttributeBasedPerimeterExternal(numNode, getRoot(), getInputImage()).addAttributeInNodes(getListNodes());
			hasComputerAttributeBasedPerimeterExternal = true;
		}
	}
		

	public void loadAttribute(int attr){
		switch(attr){
			case Attribute.ALTITUDE:
			case Attribute.AREA:
			case Attribute.VOLUME:
			case Attribute.WIDTH:
			case Attribute.HEIGHT:
			//case Attribute.PERIMETER:
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
			case Attribute.MOMENT_OF_INERTIA:
				computerCentralMomentAttribute();
				break;
			
			case Attribute.PERIMETER_EXTERNAL:
			case Attribute.CIRCULARITY:
			case Attribute.COMPACTNESS:
			case Attribute.ELONGATION:
			case Attribute.SUM_GRAD_CONTOUR:
				computerAttributeBasedPerimeterExternal();
				break;				
				
		}
	}
	
	
	private double getAttribute(NodeLevelSets node, int type){
		loadAttribute(type);
		return node.getAttributeValue(type);
	}
	
	public GrayScaleImage reconstruction(InfoPrunedTree prunedTree){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(imgInput.getDepth(), imgInput.getWidth(), imgInput.getHeight());
		Queue<InfoPrunedTree.NodePrunedTree> fifo = new Queue<InfoPrunedTree.NodePrunedTree>();
		fifo.enqueue(prunedTree.getRoot());
		while(!fifo.isEmpty()){
			InfoPrunedTree.NodePrunedTree node_ = fifo.dequeue();
			NodeLevelSets node = node_.getInfo();
			for(NodeLevelSets son: node.getChildren()){
				if(prunedTree.wasPruned(son)){
					for(int p: son.getPixelsOfCC()){
						imgOut.setPixel(p, node.getLevel());
					}
				}
			}
			for(int p: node.getCompactNodePixels()){
				imgOut.setPixel(p, node.getLevel());
			}
			for(InfoPrunedTree.NodePrunedTree son: node_.getChildren()){
				fifo.enqueue(son);	
			}
		}
		return imgOut;
	}
	

	public InfoPrunedTree getPrunedTree(double attributeValue, int type, int typePruning){
		//long ti = System.currentTimeMillis();
		InfoPrunedTree prunedTree = new InfoPrunedTree(this, type, attributeValue);
		for(NodeLevelSets no: getListNodes()){
			if(! (getAttribute(no, type) <= attributeValue) ){ //poda
				prunedTree.addNodeNotPruned(no);
			}
		}
		//System.out.println("Tempo de execucao [Tree of shapes - filtering by pruning]  "+ ((System.currentTimeMillis() - ti) /1000.0)  + "s");		
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
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(this.root);
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			//double bb = no.getArea() / ((double) no.getWidthNode() * no.getHeightNode());
			if(getAttribute(no, type) <= attributeValue){// && bb > 0.85){ // ){ //poda
				int levelPropagation = no.getParent() == null ? no.getLevel() : no.getParent().getLevel();
				//propagacao do nivel do pai para os filhos 
				Queue<NodeLevelSets> fifoPruning = new Queue<NodeLevelSets>();
				fifoPruning.enqueue(no);	
				while(!fifoPruning.isEmpty()){
					NodeLevelSets nodePruning = fifoPruning.dequeue();
					for(NodeLevelSets song: nodePruning.getChildren()){
						fifoPruning.enqueue(song);
					}
					for(Integer p: nodePruning.getCompactNodePixels())
						imgOut.setPixel(p, levelPropagation);
				}
			}
			else{
				for(Integer p: no.getCompactNodePixels()){
					imgOut.setPixel(p, no.getLevel());
				}
				
				
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);	 
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
		/*if(typePruning == MorphologicalTreeFiltering.PRUNING_EXTINCTION_VALUE)
			return filteringExtinctionValue(attributeValue, type);
		else	*/
			return filteringByPruning(attributeValue, type);
	}
	
	
	SimpleArrayList<ExtinctionValueNode> extincaoPorNode;
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
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(getRoot());
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCompactNodePixels()){
				imgOut.setPixel(p, no.getLevel());
			}
			for(NodeLevelSets son: no.getChildren()){
				fifo.enqueue(son);	 
			}	
		}
		
		for(int k=extincaoPorNode.size()-1; k >= 0 ; k--){
			NodeLevelSets no = extincaoPorNode.get(k).node;

			if(extincaoPorNode.get(k).extinctionValue <= attributeValue){ //poda
				int levelPropagation = no.getLevel();
				//propagacao do nivel do pai para os filhos 
				Queue<NodeLevelSets> fifoPruning = new Queue<NodeLevelSets>();
				fifoPruning.enqueue(no);	
				while(!fifoPruning.isEmpty()){
					NodeLevelSets nodePruning = fifoPruning.dequeue(); 
					for(NodeLevelSets song: nodePruning.getChildren()){ 
						fifoPruning.enqueue(song);	 
					}
					for(Integer p: nodePruning.getCompactNodePixels()){
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

	@Override
	public GrayScaleImage getImageFiltered(double attributeValue, int attributeType, int typeSimplification) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InfoPrunedTree getInfoPrunedTree(double attributeValue, int attributeType, int typeSimplification) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void simplificationTree(double attributeValue, int attributeType, int typeSimplification) {
		// TODO Auto-generated method stub		
	}
	
	public void simplificationTreeByDirectRule(double attributeValue, int type){
		boolean[] update = new boolean[numNodeIdMax];
		boolean[] modified = new boolean[numNodeIdMax];
		int newNumNodeIdMax = 1;
		NodeLevelSets parent;
		
		for(NodeLevelSets node : listNode.reverse()) {
			if(node == getRoot())
				continue;								
			parent = node.getParent();								
			if(node.getAttributeValue(type) < attributeValue) {						
				mergeParent(node);
				update[parent.getId()] = true;		
				modified[parent.getId()] = true;
			} else { 
				// This helps to decrease the size of auxiliary structures
				if(node.getId() > newNumNodeIdMax)
					newNumNodeIdMax = node.getId();
				// Pass the mapCorrection to all ancestors
				if(update[node.getId()])
					update[parent.getId()] = true;
			}
		}
		
		new ComputerBasicAttributeUpdate(numNodeIdMax, getRoot(), imgInput, update, modified);
		
		// Verify each attribute that was computed before
		if(hasComputerCentralMomentAttribute) {
			new ComputerCentralMomentAttributeUpdate(numNodeIdMax, getRoot(), update, modified);
		}
		
		// Modify maxID to optimize memory		
		numNodeIdMax = newNumNodeIdMax + 1;
	}
	
	public static void main(String args[]) {
		
		GrayScaleImage imgInput  = ImageBuilder.openGrayImage(new File("/Users/gobber/Desktop/img_teste_2.png"));
		int type = Attribute.AREA;
		double t = 10000;
		Utils.debug = false;
		
		ConnectedFilteringByTreeOfShape tree = new ConnectedFilteringByTreeOfShape(imgInput);		
		tree.loadAttribute(type);						
		tree.simplificationTreeByDirectRule(t, type);
		
		// Print new tree
		ConnectedFilteringByTreeOfShape tree2 = new ConnectedFilteringByTreeOfShape(tree.reconstruction());
		tree2.loadAttribute(type);
		
		System.out.println("Nós árvore filtrada: " + tree.getNumNode());
		System.out.println("Nós árvore cópia: " + tree2.getNumNode());				
		
		for(int p = 0 ; p < imgInput.getSize() ; p++) {
			NodeLevelSets node1 = tree.getSC(p);
			NodeLevelSets node2 = tree2.getSC(p);
			for(Integer att: node1.getAttributes().keySet()) {
				if(att == Attribute.ALTITUDE)
					continue;
				if(node1.getAttributeValue(att) != node2.getAttributeValue(att)) {
					//System.out.println(Attribute.getNameAttribute(att));
				}				
			}		
		}	
		
		System.out.println("Images iguais? " + ImageAlgebra.equals(tree.reconstruction(), tree2.reconstruction()));
			
	}
	
}