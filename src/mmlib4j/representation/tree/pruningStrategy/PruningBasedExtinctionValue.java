package mmlib4j.representation.tree.pruningStrategy;

import java.util.Comparator;

import mmlib4j.datastruct.SimpleArrayList;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PruningBasedExtinctionValue {//implements MappingStrategyOfPruning{

	
	private MorphologicalTree tree;
	
	public PruningBasedExtinctionValue(MorphologicalTree tree){
		this.tree = tree;
	}
	
	
	public boolean[] getMappingSelectedNodes(int typeParam, int deltaMin, int deltaMax) {
		int num = 0;
		boolean selected[] = new boolean[tree.getNumNodeIdMax()];
		SimpleArrayList<ExtinctionValueNode> extincaoPorNode = getExtinctionValue(typeParam);
		for(ExtinctionValueNode ev: extincaoPorNode){
			
			if(ev.extinctionValue >= deltaMin && ev.extinctionValue <= deltaMax){
				selected[ev.nodeAncestral.getId()] = true;
				for(NodeLevelSets filho:  ev.nodeAncestral.getChildren()){
					selected[filho.getId()] = true;
					num = num + 1;
				}
			}
		}
		return selected;
	}
	
	public GrayScaleImage filteringByExtinctionValue(double attributeValue, int type){
		return getPrunedTreeByExtinctionValue(attributeValue, type).reconstruction();
	}
	
	public InfoPrunedTree getPrunedTreeByExtinctionValue(double attributeValue, int type){
		long ti = System.currentTimeMillis();
		
		InfoPrunedTree prunedTree = new InfoPrunedTree(tree, type, attributeValue);
		
		SimpleArrayList<ExtinctionValueNode> extincaoPorNode = getExtinctionValue(type);
		if(Utils.debug)
			System.out.println("EV: Tempo1: "+ ((System.currentTimeMillis() - ti) /1000.0)  + "s");
		
		boolean resultPruning[] = new boolean[tree.getNumNodeIdMax()];
		for(ExtinctionValueNode nodeEV: extincaoPorNode){
			
			if(nodeEV.extinctionValue < attributeValue){ //poda				
				NodeLevelSets node = nodeEV.nodeAncestral;	
				for(NodeLevelSets song: node.getChildren()){
					if(!resultPruning[song.getId()])
						for(NodeLevelSets n: song.getNodesDescendants())
							resultPruning[n.getId()] = true;	 
				}
				
			}
		}
		
		for(NodeLevelSets no: tree.getListNodes()){
			if( ! resultPruning[no.getId()]  ){ //nao poda
				prunedTree.addNodeNotPruned(no);
			}
		}
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [Componente tree - filtering by pruning extinction value]  "+ ((tf - ti) /1000.0)  + "s");
		}
		
		return prunedTree;
	}
	
	/**
	 * Cria uma imagem filtrada. 
	 * Obs: Para gerar a imagem filtrada nao eh alterado a estrutura original da arvore 
	 * @param attributeValue - valor do atributo
	 * @param idAttribute - tipo do atributo
	 * @return imagem filtrada
	 */
	public SimpleArrayList<ExtinctionValueNode> getExtinctionValue(int type){
		SimpleArrayList<ExtinctionValueNode> extincaoPorNode;
		Attribute.loadAttribute(tree, type);
		if(type == Attribute.ALTITUDE){
			extincaoPorNode = getExtinctionByAltitude(tree.getLeaves());
		}else{
			extincaoPorNode = getExtinctionByAttribute(type, tree.getLeaves());
		}
		extincaoPorNode.sort(getComparator());
		return extincaoPorNode;
	}

	/**
	 * extinction value by area, width bb, height bb
	 * @param attributeValue
	 * @param type
	 * @return
	 */
	public SimpleArrayList<ExtinctionValueNode> getExtinctionByAttribute(int type, SimpleLinkedList<NodeLevelSets> folhas){
		int extinction;
		boolean visitado[] = new boolean[tree.getNumNode()];
		SimpleArrayList<ExtinctionValueNode> extincaoPorNode = new SimpleArrayList<ExtinctionValueNode>();
		for(NodeLevelSets folha: folhas){
			extinction = (int)tree.getRoot().getAttributeValue(type);
			NodeLevelSets aux = folha;
			NodeLevelSets pai = aux.getParent();
			boolean flag = true;
			while (flag  &&  pai != null) {
				if (pai.getNumChildren() > 1) {
					for(NodeLevelSets filho: pai.getChildren()){  // verifica se possui irmao com area maior
						if(flag){
							if (visitado[filho.getId()]  &&  filho != aux  &&  filho.getAttributeValue(type) == aux.getAttributeValue(type)) { //EMPATE Grimaud,92
								flag = false;
							}
							else if (filho != aux  &&  filho.getAttributeValue(type) > aux.getAttributeValue(type)) {
								flag = false;
							}
							visitado[filho.getId()] = true;
							
						}
					}
				}
				if (flag) {
					aux = pai;
					pai = aux.getParent();
				}
			}
			if (pai != null){
				extinction = (int) aux.getAttributeValue(type);
				extincaoPorNode.add( new ExtinctionValueNode(folha, pai, extinction) );
			}
		}
		
		extincaoPorNode.sort(getComparator());
		return extincaoPorNode;
	}
	
	public SimpleArrayList<ExtinctionValueNode> getExtinctionByAltitude(SimpleLinkedList<NodeLevelSets> folhas){
		int extincao;
		boolean visitado[] = new boolean[tree.getNumNode()];
		SimpleArrayList<ExtinctionValueNode> extincaoPorNode = new SimpleArrayList<ExtinctionValueNode>();		
		for(NodeLevelSets folha: folhas){
			extincao = (int) tree.getRoot().getAttribute(Attribute.ALTITUDE).getValue();
			NodeLevelSets pai = folha.getParent();
			while (pai != null &&  pai.getAttribute(Attribute.ALTITUDE).getValue() <= Math.abs(folha.getLevel() - pai.getLevel())) {
				if (visitado[pai.getId()]  &&  pai.getNumChildren() > 1  &&  pai.getAttribute(Attribute.ALTITUDE).getValue() == Math.abs(folha.getLevel() - pai.getLevel())) {  //EMPATE Grimaud,92
					break;
				}
				visitado[pai.getId()] = true;
				pai = pai.getParent();
			}
			if (pai != null)
				extincao = Math.abs(folha.getLevel() - pai.getLevel());
			
			//extincaoPorNode[folha.getId()] = extincao;
			extincaoPorNode.add( new ExtinctionValueNode(folha, pai, extincao) );

		}
		extincaoPorNode.sort(getComparator());
		return extincaoPorNode;
	}
	

	public Comparator<ExtinctionValueNode> getComparator(){
		return new Comparator<ExtinctionValueNode>() {
			public int compare(ExtinctionValueNode o1, ExtinctionValueNode o2) {
				return o1.compareTo(o2);
			}
		};
		
	}
	
	public class ExtinctionValueNode implements Comparable<ExtinctionValueNode>{
		
		public NodeLevelSets node;
		public NodeLevelSets nodeAncestral;
		public int extinctionValue;
		
		public ExtinctionValueNode(NodeLevelSets node, NodeLevelSets nodeAncestral, int value){
			this.node = node;
			this.nodeAncestral = nodeAncestral;
			extinctionValue = value;
		}
		public int compareTo(ExtinctionValueNode o) {
			if(this.extinctionValue > o.extinctionValue) return -1;
			else if(this.extinctionValue < o.extinctionValue) return 1;
			else return 0;
		} 
	}
	
}
