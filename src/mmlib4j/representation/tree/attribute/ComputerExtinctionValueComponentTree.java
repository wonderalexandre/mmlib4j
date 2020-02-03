package mmlib4j.representation.tree.attribute;

import java.awt.Color;
import java.util.Comparator;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleArrayList;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.gui.WindowImages;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.AbstractImageFactory;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.NodeLevelSets;
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
	private ColorImage reconstruction(SimpleArrayList<ExtinctionValueNode> extincaoPorNode, int attributeValue1, int attributeValue2){
		//reconstrucao
		AdjacencyRelation adj = AdjacencyRelation.getCircular(4); 
		ColorImage imgOut = AbstractImageFactory.instance.createColorImage(tree.getInputImage().getWidth(), tree.getInputImage().getHeight());
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(tree.getRoot());
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCompactNodePixels()){
				imgOut.setGray(p, no.getLevel());
			}
			for(NodeLevelSets son: no.getChildren()){
				fifo.enqueue(son);	 
			}	
		}
		for(ExtinctionValueNode ev: extincaoPorNode){
			if(attributeValue1 < ev.extinctionValue &&  ev.extinctionValue < attributeValue2){
				NodeLevelSets no = ev.node;
				for(int i: adj.getAdjacencyPixels(imgOut, no.getCompactNodePixels().getFisrtElement())){
					imgOut.setPixel(i, Color.RED.getRGB());
				}
			}
		}
		return imgOut;
	}
	
	public GrayScaleImage segmentationByAttribute(int attValue1, int attValue2, int type){
		GrayScaleImage imgOut = AbstractImageFactory.instance.createGrayScaleImage(tree.getInputImage().getDepth(), tree.getInputImage().getWidth(), tree.getInputImage().getHeight());; 
		int partition[] = new int[tree.getNumNode()];
		boolean visitado[] = new boolean[tree.getNumNode()];
		SimpleArrayList<ExtinctionValueNode> extincaoPorNode = getExtinctionByAttribute(type);
		extincaoPorNode.sort(getComparator());
		int i = 0;
		for(ExtinctionValueNode ev: extincaoPorNode){
			if(attValue1 < ev.extinctionValue && ev.extinctionValue < attValue2){
				i++;
				NodeLevelSets node = ev.node;
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
				NodeLevelSets node = ev.node;
				NodeLevelSets nodeA = null;
				while (node != null && partition[node.getId()] == i){
					nodeA = node;
					node = node.getParent();
				}
				
				//reconstruir imagem da particao i => a raiz da particao esta enraizada em nodeA
				if(nodeA != null){ 
					Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
					fifo.enqueue(nodeA);
					while(!fifo.isEmpty()){
						NodeLevelSets no = fifo.dequeue();
						for(Integer p: no.getCompactNodePixels()){
							imgOut.setPixel(p, i);
						}
						for(NodeLevelSets son: no.getChildren()){
							fifo.enqueue(son);	 
						}	
					}
				}
			}		
		}
		return imgOut;
	}
	
	public GrayScaleImage segmentationByKmax(int kmax, int type){
		GrayScaleImage imgOut = AbstractImageFactory.instance.createGrayScaleImage(tree.getInputImage().getDepth(), tree.getInputImage().getWidth(), tree.getInputImage().getHeight());; 
		int partition[] = new int[tree.getNumNode()];
		boolean visitado[] = new boolean[tree.getNumNode()];
		SimpleArrayList<ExtinctionValueNode> extincaoPorNode = getExtinctionByAttribute(type);
		extincaoPorNode.sort(getComparator());
		if(kmax > extincaoPorNode.size()) kmax = extincaoPorNode.size();
		for(int i=1; i <= kmax; i++){
			NodeLevelSets node = extincaoPorNode.get(i-1).node;
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
			NodeLevelSets node = extincaoPorNode.get(i-1).node;
			NodeLevelSets nodeA = null;
			while (node != null && partition[node.getId()] == i){
				nodeA = node;
				node = node.getParent();
			}
			
			//reconstruir imagem da particao i => a raiz da particao esta enraizada em nodeA
			if(nodeA != null){ 
				Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
				fifo.enqueue(nodeA);
				while(!fifo.isEmpty()){
					NodeLevelSets no = fifo.dequeue();
					for(Integer p: no.getCompactNodePixels()){
						imgOut.setPixel(p, i);
					}
					if(no.getChildren() != null){
						for(NodeLevelSets son: no.getChildren()){
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
	private ColorImage reconstructionKmax(SimpleArrayList<ExtinctionValueNode> extincaoPorNode, int kmax){
		//reconstrucao
		AdjacencyRelation adj = AdjacencyRelation.getCircular(4); 
		ColorImage imgOut = AbstractImageFactory.instance.createColorImage(tree.getInputImage().getWidth(), tree.getInputImage().getHeight());
		Queue<NodeLevelSets> fifo = new Queue<NodeLevelSets>();
		fifo.enqueue(tree.getRoot());
		if(kmax > extincaoPorNode.size()) kmax = extincaoPorNode.size();
		while(!fifo.isEmpty()){
			NodeLevelSets no = fifo.dequeue();
			for(Integer p: no.getCompactNodePixels()){
				imgOut.setGray(p, no.getLevel());
			}
			if(no.getChildren() != null){
				for(NodeLevelSets son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}	
		}
		extincaoPorNode.sort(getComparator());
		for(int k=0; k < kmax; k++){
			NodeLevelSets no = extincaoPorNode.get(k).node;
			//System.out.println(no.getId() + "=> " + extincaoPorNode.get(k).extinctionValue);
			for(int i: adj.getAdjacencyPixels(imgOut, no.getCompactNodePixels().getFisrtElement())){
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
	
	private SimpleArrayList<ExtinctionValueNode> getExtinctionValue(int type) {
		this.type = type;
		if(type == Attribute.AREA || type == Attribute.HEIGHT || type == Attribute.WIDTH || type == Attribute.VOLUME)
			return getExtinctionByAttribute(type);
		else if(type == Attribute.ALTITUDE)
			return getExtinctionByAltitude();
		else
			 throw new RuntimeException("Erro: Este atributo nao foi implementado..");
	}
	
	private SimpleArrayList<ExtinctionValueNode> getExtinctionByAltitude(){
		int extincao;
		boolean visitado[] = new boolean[tree.getNumNode()];
		SimpleArrayList<ExtinctionValueNode> extincaoPorNode = new SimpleArrayList<ExtinctionValueNode>();		
		SimpleLinkedList<NodeLevelSets> folhas = tree.getLeaves();
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
		return extincaoPorNode;
	}
	
	
	/**
	 * Cria uma imagem filtrada. 
	 * Obs: Para gerar a imagem filtrada nao eh alterado a estrutura original da arvore 
	 * @param attributeValue - valor do atributo
	 * @param idAttribute - tipo do atributo
	 * @return imagem filtrada
	 */
	public SimpleArrayList<ExtinctionValueNode> getExtinctionValueCut(int type){
		this.type = type;
		SimpleArrayList<ExtinctionValueNode> extincaoPorNode;
		if(type == Attribute.ALTITUDE){
			extincaoPorNode = getExtinctionCutByAltitude(tree.getLeaves());
		}else{
			extincaoPorNode = getExtinctionCutByAttribute(type, tree.getLeaves());
		}
		extincaoPorNode.sort(getComparator());
		return extincaoPorNode;
	}
	
	
	
	
	public boolean[] getExtinctionValueNodeCT(int type){
		this.type = type;
		SimpleArrayList<ExtinctionValueNode> extincaoPorNode;
		if(type == Attribute.ALTITUDE){
			extincaoPorNode = getExtinctionCutByAltitude(tree.getLeaves());
		}else{
			extincaoPorNode = getExtinctionCutByAttribute(type, tree.getLeaves());
		}
		
		//ArrayList<NodeCT> list = new ArrayList<NodeCT>();
		boolean flag[] = new boolean[tree.getNumNode()];
		for(ExtinctionValueNode nodeEV: extincaoPorNode){
			NodeLevelSets node = nodeEV.nodeAncestral;
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
		SimpleArrayList<ExtinctionValueNode> extincaoPorNode;
		if(type == Attribute.ALTITUDE){
			extincaoPorNode = getExtinctionCutByAltitude(tree.getLeaves());
		}else{
			extincaoPorNode = getExtinctionCutByAttribute(type, tree.getLeaves());
		}
		
		//ArrayList<NodeCT> list = new ArrayList<NodeCT>();
		boolean flag[] = new boolean[tree.getNumNode()];
		for(ExtinctionValueNode nodeEV: extincaoPorNode){
			NodeLevelSets node = nodeEV.nodeAncestral;
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
		SimpleArrayList<ExtinctionValueNode> extincaoPorNode;
		if(type == Attribute.ALTITUDE){
			extincaoPorNode = getExtinctionCutByAltitude(prunedTree.getLeaves());
		}else{
			extincaoPorNode = getExtinctionCutByAttribute(type, prunedTree.getLeaves());
		}
		
		//ArrayList<NodeCT> list = new ArrayList<NodeCT>();
		boolean flag[] = new boolean[tree.getNumNode()];
		for(ExtinctionValueNode nodeEV: extincaoPorNode){
			NodeLevelSets node = nodeEV.nodeAncestral;
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
	
	
	
	private SimpleArrayList<ExtinctionValueNode> getExtinctionCutByAltitude(SimpleLinkedList folhas){
		int extincao; 
		boolean visitado[] = new boolean[tree.getNumNode()];
		SimpleArrayList<ExtinctionValueNode> extincaoPorNode = new SimpleArrayList<ExtinctionValueNode>();		
		for(Object obj: folhas){
			NodeCT folha = (NodeCT) obj;
			extincao = (int)tree.getRoot().getAttribute(Attribute.ALTITUDE).getValue();
			NodeLevelSets pai = folha.getParent();
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
	private SimpleArrayList<ExtinctionValueNode> getExtinctionByAttribute(int type){
		int extinction;
		boolean visitado[] = new boolean[tree.getNumNode()];
		SimpleArrayList<ExtinctionValueNode> extincaoPorNode = new SimpleArrayList<ExtinctionValueNode>();
		SimpleLinkedList<NodeLevelSets> folhas = tree.getLeaves();
		for(NodeLevelSets folha: folhas){
			extinction = (int) tree.getRoot().getAttribute(type).getValue();
			NodeLevelSets aux = folha;
			NodeLevelSets pai = aux.getParent();
			boolean flag = true;
			while (flag  &&  pai != null) {
				if (pai.getNumChildren() > 1) {
					for(NodeLevelSets filho: pai.getChildren()){  // verifica se possui irmao com area maior
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
	

	
	private SimpleArrayList<ExtinctionValueNode> getExtinctionCutByAttribute(int type, SimpleLinkedList<NodeLevelSets> folhas){
		int extinction;
		boolean visitado[] = new boolean[tree.getNumNode()];
		SimpleArrayList<ExtinctionValueNode> extincaoPorNode = new SimpleArrayList<ExtinctionValueNode>();
		for(NodeLevelSets folha: folhas){
			extinction = (int)tree.getRoot().getAttribute(type).getValue();
			NodeLevelSets aux = folha;
			NodeLevelSets pai = aux.getParent();
			boolean flag = true;
			while (flag  &&  pai != null) {
				if (pai.getNumChildren() > 1) {
					for(NodeLevelSets filho: pai.getChildren()){  // verifica se possui irmao com area maior
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
