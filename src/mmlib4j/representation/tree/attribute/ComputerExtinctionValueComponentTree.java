package mmlib4j.representation.tree.attribute;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import mmlib4j.datastruct.Queue;
import mmlib4j.gui.WindowImages;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerExtinctionValueComponentTree implements ComputerExtinctionValue {

	private ComponentTree tree;
	private int type;
	
	public ComputerExtinctionValueComponentTree(ComponentTree tree){
		this.tree = tree;
	}

	public static void main(String args[]){
		GrayScaleImage img  = ImageBuilder.openGrayImage();
		ComponentTree ct = new ComponentTree(img, AdjacencyRelation.getCircular(1.5), false);
		ComputerExtinctionValueComponentTree e = new ComputerExtinctionValueComponentTree(ct);
		//ct.printTree();
		WindowImages.show(e.extinctionByAttribute(100, 500, Attribute.AREA));
	}
	
	/**
	 * Reconstrucao da imagem com base em um limiar para o valor de extincao
	 * @param extincaoPorNode
	 * @param attributeValue
	 * @return
	 */
	private ColorImage reconstruction(ArrayList<ExtinctionValueNode> extincaoPorNode, int attributeValue1, int attributeValue2){
		//reconstrucao
		AdjacencyRelation adj = AdjacencyRelation.getCircular(4); 
		ColorImage imgOut = ImageFactory.createColorImage(tree.getInputImage().getWidth(), tree.getInputImage().getHeight());
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(tree.getRoot());
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				imgOut.setGray(p, no.getLevel());
			}
			if(no.getChildren() != null){
				for(NodeCT son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}	
		}
		for(ExtinctionValueNode ev: extincaoPorNode){
			if(attributeValue1 < ev.extinctionValue &&  ev.extinctionValue < attributeValue2){
				NodeCT no = ev.node;
				for(int i: adj.getAdjacencyPixels(imgOut, no.getCanonicalPixels().getFisrtElement())){
					imgOut.setPixel(i, Color.RED.getRGB());
				}
			}
		}
		return imgOut;
	}
	
	public GrayScaleImage segmentationByAttribute(int attValue1, int attValue2, int type){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(tree.getInputImage().getDepth(), tree.getInputImage().getWidth(), tree.getInputImage().getHeight());; 
		int partition[] = new int[tree.getNumNode()];
		boolean visitado[] = new boolean[tree.getNumNode()];
		ArrayList<ExtinctionValueNode> extincaoPorNode = getExtinctionByAttribute(type);
		Collections.sort(extincaoPorNode);
		int i = 0;
		for(ExtinctionValueNode ev: extincaoPorNode){
			if(attValue1 < ev.extinctionValue && ev.extinctionValue < attValue2){
				i++;
				NodeCT node = ev.node;
				while(node != null && visitado[node.getId()] == false){
					visitado[node.getId()] = true;
					partition[node.getId()] = i;
					node = node.getParent();
					while(node != null && visitado[node.getId()] == true && partition[node.getId()] != 0){ ////se trecho ate raiz ja percorrido
						//zona de empate nao deve pertencer a nenhuma particao
						partition[node.getId()] = 0;
						node = node.getParent();	
					}
				}
			}
		}
		i = 0;
		for(ExtinctionValueNode ev: extincaoPorNode){
			if(attValue1 < ev.extinctionValue && ev.extinctionValue < attValue2){
				i++;
				NodeCT node = ev.node;
				NodeCT nodeA = null;
				while (node != null && partition[node.getId()] == i){
					nodeA = node;
					node = node.getParent();
				}
				
				//reconstruir imagem da particao i => a raiz da particao esta enraizada em nodeA
				if(nodeA != null){ 
					Queue<NodeCT> fifo = new Queue<NodeCT>();
					fifo.enqueue(nodeA);
					while(!fifo.isEmpty()){
						NodeCT no = fifo.dequeue();
						for(Integer p: no.getCanonicalPixels()){
							imgOut.setPixel(p, i);
						}
						if(no.getChildren() != null){
							for(NodeCT son: no.getChildren()){
								fifo.enqueue(son);	 
							}
						}	
					}
				}
			}		
		}
		return imgOut;
	}
	
	public GrayScaleImage segmentationByKmax(int kmax, int type){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(tree.getInputImage().getDepth(), tree.getInputImage().getWidth(), tree.getInputImage().getHeight());; 
		int partition[] = new int[tree.getNumNode()];
		boolean visitado[] = new boolean[tree.getNumNode()];
		ArrayList<ExtinctionValueNode> extincaoPorNode = getExtinctionByAttribute(type);
		Collections.sort(extincaoPorNode);
		if(kmax > extincaoPorNode.size()) kmax = extincaoPorNode.size();
		for(int i=1; i <= kmax; i++){
			NodeCT node = extincaoPorNode.get(i-1).node;
			while(node != null && visitado[node.getId()] == false){
				visitado[node.getId()] = true;
				partition[node.getId()] = i;
				node = node.getParent();
				while(node != null && visitado[node.getId()] == true && partition[node.getId()] != 0){ ////se trecho ate raiz ja percorrido
					//zona de empate nao deve pertencer a nenhuma particao
					partition[node.getId()] = 0;
					node = node.getParent();	
				}
			}
		}
		for(int i=1; i <= kmax; i++){
			NodeCT node = extincaoPorNode.get(i-1).node;
			NodeCT nodeA = null;
			while (node != null && partition[node.getId()] == i){
				nodeA = node;
				node = node.getParent();
			}
			
			//reconstruir imagem da particao i => a raiz da particao esta enraizada em nodeA
			if(nodeA != null){ 
				Queue<NodeCT> fifo = new Queue<NodeCT>();
				fifo.enqueue(nodeA);
				while(!fifo.isEmpty()){
					NodeCT no = fifo.dequeue();
					for(Integer p: no.getCanonicalPixels()){
						imgOut.setPixel(p, i);
					}
					if(no.getChildren() != null){
						for(NodeCT son: no.getChildren()){
							fifo.enqueue(son);	 
						}
					}	
				}
			}
			
		}
		return imgOut;
	}

	/**
	 * Reconstrucao da imagem com base em um limiar para o valor de extincao
	 * @param extincaoPorNode
	 * @param attributeValue
	 * @return
	 */
	private ColorImage reconstructionKmax(ArrayList<ExtinctionValueNode> extincaoPorNode, int kmax){
		//reconstrucao
		AdjacencyRelation adj = AdjacencyRelation.getCircular(4); 
		ColorImage imgOut = ImageFactory.createColorImage(tree.getInputImage().getWidth(), tree.getInputImage().getHeight());
		Queue<NodeCT> fifo = new Queue<NodeCT>();
		fifo.enqueue(tree.getRoot());
		if(kmax > extincaoPorNode.size()) kmax = extincaoPorNode.size();
		while(!fifo.isEmpty()){
			NodeCT no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				imgOut.setGray(p, no.getLevel());
			}
			if(no.getChildren() != null){
				for(NodeCT son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}	
		}
		Collections.sort(extincaoPorNode);
		for(int k=0; k < kmax; k++){
			NodeCT no = extincaoPorNode.get(k).node;
			//System.out.println(no.getId() + "=> " + extincaoPorNode.get(k).extinctionValue);
			for(int i: adj.getAdjacencyPixels(imgOut, no.getCanonicalPixels().getFisrtElement())){
				imgOut.setPixel(i, Color.RED.getRGB());
			}
		}
		
		return imgOut;
	}
	
	public ColorImage extinctionByAttribute(int attributeValue1, int attributeValue2, int type) {
		return reconstruction(getExtinctionValue(type), attributeValue1, attributeValue2);
	}
	
	public ColorImage extinctionByKmax(int kmax, int type) {
		return reconstructionKmax(getExtinctionValue(type), kmax);
	}
	
	private ArrayList<ExtinctionValueNode> getExtinctionValue(int type) {
		this.type = type;
		if(type == Attribute.AREA || type == Attribute.HEIGHT || type == Attribute.WIDTH || type == Attribute.VOLUME)
			return getExtinctionByAttribute(type);
		else if(type == Attribute.ALTITUDE)
			return getExtinctionByAltitude();
		else
			 throw new RuntimeException("Erro: Este atributo nao foi implementado..");
	}
	
	private ArrayList<ExtinctionValueNode> getExtinctionByAltitude(){
		int extincao;
		boolean visitado[] = new boolean[tree.getNumNode()];
		ArrayList<ExtinctionValueNode> extincaoPorNode = new ArrayList<ExtinctionValueNode>();		
		LinkedList<NodeCT> folhas = tree.getLeaves();
		for(NodeCT folha: folhas){
			extincao = (int) tree.getRoot().getAttribute(Attribute.ALTITUDE).getValue();
			NodeCT pai = folha.getParent();
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
		return extincaoPorNode;
	}
	
	
	/**
	 * Cria uma imagem filtrada. 
	 * Obs: Para gerar a imagem filtrada nao eh alterado a estrutura original da arvore 
	 * @param attributeValue - valor do atributo
	 * @param idAttribute - tipo do atributo
	 * @return imagem filtrada
	 */
	public ArrayList<ExtinctionValueNode> getExtinctionValueCut(int type){
		this.type = type;
		ArrayList<ExtinctionValueNode> extincaoPorNode;
		if(type == Attribute.ALTITUDE){
			extincaoPorNode = getExtinctionCutByAltitude(tree.getLeaves());
		}else{
			extincaoPorNode = getExtinctionCutByAttribute(type, tree.getLeaves());
		}
		Collections.sort(extincaoPorNode);
		return extincaoPorNode;
	}
	
	
	
	
	public boolean[] getExtinctionValueNodeCT(int type){
		this.type = type;
		ArrayList<ExtinctionValueNode> extincaoPorNode;
		if(type == Attribute.ALTITUDE){
			extincaoPorNode = getExtinctionCutByAltitude(tree.getLeaves());
		}else{
			extincaoPorNode = getExtinctionCutByAttribute(type, tree.getLeaves());
		}
		
		//ArrayList<NodeCT> list = new ArrayList<NodeCT>();
		boolean flag[] = new boolean[tree.getNumNode()];
		for(ExtinctionValueNode nodeEV: extincaoPorNode){
			NodeCT node = nodeEV.nodeAncestral;
			if(!flag[node.getId()]){
				flag[node.getId()] = true;
				//list.add(node);
			}
		}
		return flag;
		//return list;
	}
	
	public boolean[] getExtinctionValueNodeCT(int type, int value){
		this.type = type;
		ArrayList<ExtinctionValueNode> extincaoPorNode;
		if(type == Attribute.ALTITUDE){
			extincaoPorNode = getExtinctionCutByAltitude(tree.getLeaves());
		}else{
			extincaoPorNode = getExtinctionCutByAttribute(type, tree.getLeaves());
		}
		
		//ArrayList<NodeCT> list = new ArrayList<NodeCT>();
		boolean flag[] = new boolean[tree.getNumNode()];
		for(ExtinctionValueNode nodeEV: extincaoPorNode){
			NodeCT node = nodeEV.nodeAncestral;
			if(!flag[node.getId()] && nodeEV.extinctionValue > value){
				flag[node.getId()] = true;
				//list.add(node);
			}
		}
		return flag;
		//return list;
	}
	
	public boolean[] getExtinctionValueNodeCT(int type, InfoPrunedTree prunedTree){
		this.type = type;
		ArrayList<ExtinctionValueNode> extincaoPorNode;
		if(type == Attribute.ALTITUDE){
			extincaoPorNode = getExtinctionCutByAltitude(prunedTree.getLeaves());
		}else{
			extincaoPorNode = getExtinctionCutByAttribute(type, prunedTree.getLeaves());
		}
		
		//ArrayList<NodeCT> list = new ArrayList<NodeCT>();
		boolean flag[] = new boolean[tree.getNumNode()];
		for(ExtinctionValueNode nodeEV: extincaoPorNode){
			NodeCT node = nodeEV.nodeAncestral;
			if(!flag[node.getId()]){
				flag[node.getId()] = true;
				//list.add(node);
			}
		}
		return flag;
		//return list;
	}
	
	
	public int getType(){
		return type;
	}
	
	
	
	private ArrayList<ExtinctionValueNode> getExtinctionCutByAltitude(LinkedList folhas){
		int extincao; 
		boolean visitado[] = new boolean[tree.getNumNode()];
		ArrayList<ExtinctionValueNode> extincaoPorNode = new ArrayList<ExtinctionValueNode>();		
		for(Object obj: folhas){
			NodeCT folha = (NodeCT) obj;
			extincao = (int)tree.getRoot().getAttribute(Attribute.ALTITUDE).getValue();
			NodeCT pai = folha.getParent();
			while (pai != null &&  pai.getAttribute(Attribute.ALTITUDE).getValue() <= Math.abs(folha.getLevel() - pai.getLevel())) {
				if (visitado[pai.getId()]  &&  pai.getNumChildren() > 1  &&  pai.getAttribute(Attribute.ALTITUDE).getValue() == Math.abs(folha.getLevel() - pai.getLevel())) {  //EMPATE Grimaud,92
					break;
				}
				visitado[pai.getId()] = true;
				pai = pai.getParent();
			}
			if (pai != null){
				extincao = Math.abs(folha.getLevel() - pai.getLevel());
				extincaoPorNode.add( new ExtinctionValueNode(folha, pai, extincao) );
			}
			//extincaoPorNode[folha.getId()] = extincao;
			
		}
		return extincaoPorNode;
	}
	
	
	
	/**
	 * extinction value by area, width bb, height bb
	 * @param attributeValue
	 * @param type
	 * @return
	 */
	private ArrayList<ExtinctionValueNode> getExtinctionByAttribute(int type){
		int extinction;
		boolean visitado[] = new boolean[tree.getNumNode()];
		ArrayList<ExtinctionValueNode> extincaoPorNode = new ArrayList<ExtinctionValueNode>();
		LinkedList<NodeCT> folhas = tree.getLeaves();
		for(NodeCT folha: folhas){
			extinction = (int) tree.getRoot().getAttribute(type).getValue();
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
			if (pai != null)
				extinction = (int) aux.getAttribute(type).getValue();
		
			//extincaoPorNode[folha.getId()] = extinction;
			extincaoPorNode.add( new ExtinctionValueNode(folha, aux.getParent(), extinction) );
		}
		return extincaoPorNode;
	}
	

	
	private ArrayList<ExtinctionValueNode> getExtinctionCutByAttribute(int type, LinkedList folhas){
		int extinction;
		boolean visitado[] = new boolean[tree.getNumNode()];
		ArrayList<ExtinctionValueNode> extincaoPorNode = new ArrayList<ExtinctionValueNode>();
		for(Object obj: folhas){
			NodeCT folha = (NodeCT) obj;
			extinction = (int)tree.getRoot().getAttribute(type).getValue();
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
			if (pai != null){
				extinction = (int) aux.getAttribute(type).getValue();
				extincaoPorNode.add( new ExtinctionValueNode(folha, pai, extinction) );
			}
			//extincaoPorNode[folha.getId()] = extinction;
			
		}
		return extincaoPorNode;
	}
	
	
	public class ExtinctionValueNode implements Comparable<ExtinctionValueNode>{
		public NodeCT node;
		public NodeCT nodeAncestral;
		public int extinctionValue;
		
		public ExtinctionValueNode(NodeCT node, NodeCT nodeAncestral, int value){
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
