package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.IMorphologicalTreeFiltering;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PruningBasedExtinctionValue implements MappingStrategyOfPruning{

	private int delta;
	private int typeParam;
	private IMorphologicalTreeFiltering tree;
	private int num;
	
	public PruningBasedExtinctionValue(IMorphologicalTreeFiltering tree, int typeParam, int delta){
		this.tree = tree;
		this.typeParam = typeParam;
		this.delta = delta;
		
	}
	
	
	public boolean[] getMappingSelectedNodes() {
		this.num = 0;
		if(tree instanceof ComponentTree){
			return getExtinctionValueNodeCT(typeParam, delta);
		}
		else if(tree instanceof TreeOfShape){
			return getExtinctionValueNodeToS(typeParam, delta);
		}
		else
			return null;
	}
	
	public int getNumOfPruning(){
		return num;
	}
		
	public boolean[] getExtinctionValueNodeToS(int type, int valueMin){
		if(type == Attribute.ALTITUDE){
			return getExtinctionToSByAltitude( valueMin );
		}else{
			return getExtinctionToSByAttribute(type, valueMin);
		}
	}
	
	private boolean[] getExtinctionToSByAttribute(int type, int valueMin){
		boolean selected[] = new boolean[tree.getNumNode()];
		boolean visitado[] = new boolean[tree.getNumNode()];
		TreeOfShape tree = (TreeOfShape) this.tree;
		for(NodeToS folha: tree.getLeaves()){
			int extinction = (int)tree.getRoot().getAttribute(type).getValue();
			NodeToS aux = folha;
			NodeToS pai = aux.getParent();
			boolean flag = true;
			while (flag  &&  pai != null) {
				if (pai.getNumChildren() > 1) {
					for(NodeToS filho: pai.getChildren()){  // verifica se possui irmao com area maior
						if(flag){
							if (visitado[filho.getId()]  &&  filho != aux  &&  filho.getAttribute(type) == aux.getAttribute(type)) { //EMPATE Grimaud,92
								flag = false;
							}
							else if (filho != aux  &&  filho.getAttribute(type).getValue() > aux.getAttribute(type).getValue()) {
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
			extinction =(int) aux.getAttribute(type).getValue();
			if(!selected[aux.getId()] && extinction > valueMin){
				selected[aux.getId()] = true;
				this.num = this.num + 1;
			}
		}
		return selected;
	}
	
	private boolean[] getExtinctionToSByAltitude(int valueMin){
		boolean selected[] = new boolean[tree.getNumNode()];
		boolean visitado[] = new boolean[tree.getNumNode()];
		TreeOfShape tree = (TreeOfShape) this.tree;
		for(NodeToS folha: tree.getLeaves()){
			int extinction = (int)tree.getRoot().getAttribute(Attribute.ALTITUDE).getValue();
			NodeToS pai = folha.getParent();
			while (pai != null &&  pai.getAttribute(Attribute.ALTITUDE).getValue() <= Math.abs(folha.getLevel() - pai.getLevel())) {
				if (visitado[pai.getId()]  &&  pai.getNumChildren() > 1  &&  pai.getAttribute(Attribute.ALTITUDE).getValue() == Math.abs(folha.getLevel() - pai.getLevel())) {  //EMPATE Grimaud,92
					break;
				}
				visitado[pai.getId()] = true;
				pai = pai.getParent();
			}
			if (pai != null){
				extinction = Math.abs(folha.getLevel() - pai.getLevel());
				
				if(!selected[pai.getId()] && extinction > valueMin){
					selected[pai.getId()] = true;
					this.num = this.num + 1;
				}
			}
		}
		return selected;
	}
	
	
	private boolean[] getExtinctionValueNodeCT(int type, int valueMin){
		if(type == Attribute.ALTITUDE){
			return getExtinctionCTByAltitude(valueMin);
		}else{
			return getExtinctionCTByAttribute(type, valueMin);
		}
		
		
	}

	private boolean[] getExtinctionCTByAttribute(int type, int valueMin){
		boolean selected[] = new boolean[tree.getNumNode()];
		ComponentTree tree = (ComponentTree) this.tree;
		boolean visitado[] = new boolean[tree.getNumNode()];
		for(NodeCT folha: tree.getLeaves()){
			int extinction = (int)tree.getRoot().getAttribute(type).getValue();
			NodeCT aux = folha;
			NodeCT pai = aux.getParent();
			boolean flag = true;
			while (flag  &&  pai != null) {
				if (pai.getNumChildren() > 1) {
					for(NodeCT filho: pai.getChildren()){  // verifica se possui irmao com area maior
						if(flag){
							if (visitado[filho.getId()]  &&  filho != aux  &&  filho.getAttribute(type) == aux.getAttribute(type)) { //EMPATE Grimaud,92
								flag = false;
							}
							else if (filho != aux  &&  filho.getAttribute(type).getValue() > aux.getAttribute(type).getValue()) {
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
			extinction = (int) aux.getAttribute(type).getValue();
			if(!selected[aux.getId()] && extinction > valueMin){
				selected[aux.getId()] = true;
				this.num = this.num + 1;
			}
			/*
			if (pai != null){
				extinction = aux.getAttribute(type);
				//if(!selected[pai.getId()] && extinction > valueMin){
				if(extinction > valueMin){
					selected[aux.getId()] = true;
					//for(NodeCT filho: pai.getChildren()){
					//	selected[filho.getId()] = true;
					//	this.num = this.num + 1;
					//}
					
					
				}
			}*/	
		}
		return selected;
	}
	
	private boolean[] getExtinctionCTByAltitude(int valueMin){
		boolean selected[] = new boolean[tree.getNumNode()];
		ComponentTree tree = (ComponentTree) this.tree;
		boolean visitado[] = new boolean[tree.getNumNode()];
		for(NodeCT folha: tree.getLeaves()){
			int extinction = (int) tree.getRoot().getAttribute(Attribute.ALTITUDE).getValue();
			NodeCT pai = folha.getParent();
			while (pai != null &&  pai.getAttribute(Attribute.ALTITUDE).getValue() <= Math.abs(folha.getLevel() - pai.getLevel())) {
				if (visitado[pai.getId()]  &&  pai.getNumChildren() > 1  &&  pai.getAttribute(Attribute.ALTITUDE).getValue() == Math.abs(folha.getLevel() - pai.getLevel())) {  //EMPATE Grimaud,92
					break;
				}
				visitado[pai.getId()] = true;
				pai = pai.getParent();
			}
			if (pai != null){
				extinction = Math.abs(folha.getLevel() - pai.getLevel());
				if(!selected[pai.getId()] && extinction > valueMin){
					selected[pai.getId()] = true;
					this.num = this.num + 1;
				}
				
			}
		}
		return selected;
	}
	
	
	
}
