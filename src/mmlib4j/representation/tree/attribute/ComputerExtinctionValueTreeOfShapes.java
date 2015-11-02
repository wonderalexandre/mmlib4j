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
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerExtinctionValueTreeOfShapes implements ComputerExtinctionValue{

	private TreeOfShape tree;
	private int type;
	public ComputerExtinctionValueTreeOfShapes(TreeOfShape tree){
		this.tree = tree;
	}

	public static void main(String args[]){
		GrayScaleImage img  = ImageBuilder.openGrayImage();
		TreeOfShape ct = new TreeOfShape(img);
		ComputerExtinctionValueTreeOfShapes e = new ComputerExtinctionValueTreeOfShapes(ct);
		//ct.printTree();
		WindowImages.show(e.extinctionByAttribute(100, 1000, Attribute.AREA));
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
		ColorImage imgOut = ImageFactory.createCopyColorImage(tree.getInputImage());
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(tree.getRoot());
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				imgOut.setGray(p, no.getLevel());
			}
			if(no.getChildren() != null){
				for(NodeToS son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}	
		}
		for(ExtinctionValueNode ev: extincaoPorNode){
			if(attributeValue1 < ev.extinctionValue &&  ev.extinctionValue < attributeValue2){
				NodeToS no = ev.node;
				for(int i: adj.getAdjacencyPixels(imgOut, no.getCanonicalPixels().getFisrtElement())){
					imgOut.setPixel(i, Color.RED.getRGB());
				}
			}
		}
		return imgOut;
	}
	
	public GrayScaleImage segmentationByKmax(int kmax, int type){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(tree.getInputImage().getDepth(), tree.getInputImage().getWidth(), tree.getInputImage().getHeight()); 
		int partition[] = new int[tree.getNumNode()];
		boolean visitado[] = new boolean[tree.getNumNode()];
		ArrayList<ExtinctionValueNode> extincaoPorNode = getExtinctionByAttribute(type);
		Collections.sort(extincaoPorNode);
		if(kmax > extincaoPorNode.size()) kmax = extincaoPorNode.size();
		for(int i=1; i <= kmax; i++){
			NodeToS node = extincaoPorNode.get(i-1).node;
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
			NodeToS node = extincaoPorNode.get(i-1).node;
			NodeToS nodeA = null;
			while (node != null && partition[node.getId()] == i){
				nodeA = node;
				node = node.getParent();
			}
			
			//reconstruir imagem da particao i => a raiz da particao esta enraizada em nodeA
			if(nodeA != null){ 
				Queue<NodeToS> fifo = new Queue<NodeToS>();
				fifo.enqueue(nodeA);
				while(!fifo.isEmpty()){
					NodeToS no = fifo.dequeue();
					for(Integer p: no.getCanonicalPixels()){
						imgOut.setPixel(p, i);
					}
					if(no.getChildren() != null){
						for(NodeToS son: no.getChildren()){
							fifo.enqueue(son);	 
						}
					}	
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
				NodeToS node = ev.node;
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
				NodeToS node = ev.node;
				NodeToS nodeA = null;
				while (node != null && partition[node.getId()] == i){
					nodeA = node;
					node = node.getParent();
				}
				
				//reconstruir imagem da particao i => a raiz da particao esta enraizada em nodeA
				if(nodeA != null){ 
					Queue<NodeToS> fifo = new Queue<NodeToS>();
					fifo.enqueue(nodeA);
					while(!fifo.isEmpty()){
						NodeToS no = fifo.dequeue();
						for(Integer p: no.getCanonicalPixels()){
							imgOut.setPixel(p, i);
						}
						if(no.getChildren() != null){
							for(NodeToS son: no.getChildren()){
								fifo.enqueue(son);	 
							}
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
		ColorImage imgOut = ImageFactory.createCopyColorImage(tree.getInputImage());;
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(tree.getRoot());
		if(kmax > extincaoPorNode.size()) kmax = extincaoPorNode.size();
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			for(Integer p: no.getCanonicalPixels()){
				imgOut.setGray(p, no.getLevel());
			}
			if(no.getChildren() != null){
				for(NodeToS son: no.getChildren()){
					fifo.enqueue(son);	 
				}
			}	
		}
		Collections.sort(extincaoPorNode);
		for(int k=0; k < kmax; k++){
			NodeToS no = extincaoPorNode.get(k).node;
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
	
	
	public boolean[] getExtinctionValueNode(int type, InfoPrunedTree prunedTree){
		this.type = type;
		ArrayList<ExtinctionValueNode> extincaoPorNode;
		if(type == Attribute.ALTITUDE){
			extincaoPorNode = getExtinctionCutByAltitude(prunedTree.getLeaves());
		}else{
			extincaoPorNode = getExtinctionCutByAttribute(type, prunedTree.getLeaves());
		}
		boolean flag[] = new boolean[tree.getNumNode()];
		for(ExtinctionValueNode nodeEV: extincaoPorNode){
			NodeToS node = nodeEV.nodeAncestral;
			if(!flag[node.getId()]){
				flag[node.getId()] = true;
			}
		}
		return flag;
		
	}
	
	public boolean[] getExtinctionValueNode(int type, int t){
		this.type = type;
		ArrayList<ExtinctionValueNode> extincaoPorNode;
		if(type == Attribute.ALTITUDE){
			extincaoPorNode = getExtinctionCutByAltitude(tree.getLeaves());
		}else{
			extincaoPorNode = getExtinctionCutByAttribute(type, tree.getLeaves());
		}
		
		Collections.sort(extincaoPorNode);
		
		boolean flag[] = new boolean[tree.getNumNode()];
		for(ExtinctionValueNode nodeEV: extincaoPorNode){
			NodeToS node = nodeEV.nodeAncestral;
			if(!flag[node.getId()] && nodeEV.extinctionValue > t){
				flag[node.getId()] = true;
			}
		}
		return flag;
		
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
		LinkedList<NodeToS> folhas = tree.getLeaves();
		for(NodeToS folha: folhas){
			extincao = (int) tree.getRoot().getAttributeValue(Attribute.ALTITUDE);
			NodeToS pai = folha.getParent();
			while (pai != null &&  pai.getAttributeValue(Attribute.ALTITUDE) <= Math.abs(folha.getLevel() - pai.getLevel())) {
				if (visitado[pai.getId()]  &&  pai.getNumChildren() > 1  &&  pai.getAttributeValue(Attribute.ALTITUDE) == Math.abs(folha.getLevel() - pai.getLevel())) {  //EMPATE Grimaud,92
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
		LinkedList<NodeToS> folhas = tree.getLeaves();
		for(NodeToS folha: folhas){
			extinction = (int)tree.getRoot().getAttributeValue(type);
			NodeToS aux = folha;
			NodeToS pai = aux.getParent();
			boolean flag = true;
			while (flag  &&  pai != null) {
				if (pai.getNumChildren() > 1) {
					for(NodeToS filho: pai.getChildren()){  // verifica se possui irmao com area maior
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
				//extincaoPorNode[folha.getId()] = extinction;
				extincaoPorNode.add( new ExtinctionValueNode(folha, pai, extinction) );
			}
		}
		return extincaoPorNode;
	}
	
	
	
	public ArrayList<ExtinctionValueNode> getExtinctionValueCut(double attributeValue, int type){
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
	
	public int getType(){
		return type;
	}
	
	private ArrayList<ExtinctionValueNode> getExtinctionCutByAltitude(LinkedList folhas){
		int extincao;
		boolean visitado[] = new boolean[tree.getNumNode()];
		ArrayList<ExtinctionValueNode> extincaoPorNode = new ArrayList<ExtinctionValueNode>();		
		//LinkedList<NodeToS> folhas = tree.getLeaves();
		for(Object obj: folhas){
			NodeToS folha = (NodeToS) obj;
			extincao = (int) tree.getRoot().getAttributeValue(Attribute.ALTITUDE);
			NodeToS pai = folha.getParent();
			while (pai != null &&  pai.getAttributeValue(Attribute.ALTITUDE) <= Math.abs(folha.getLevel() - pai.getLevel())) {
				if (visitado[pai.getId()]  &&  pai.getNumChildren() > 1  &&  pai.getAttributeValue(Attribute.ALTITUDE) == Math.abs(folha.getLevel() - pai.getLevel())) {  //EMPATE Grimaud,92
					break;
				}
				visitado[pai.getId()] = true;
				pai = pai.getParent();
			}
			if (pai != null){
				extincao = Math.abs(folha.getLevel() - pai.getLevel());
				
				//extincaoPorNode[folha.getId()] = extincao;
				extincaoPorNode.add( new ExtinctionValueNode(folha, pai, extincao) );
			}

		}
		return extincaoPorNode;
	}
	
	/**
	 * extinction value by area, width bb, height bb
	 * @param attributeValue
	 * @param type
	 * @return
	 */
	private ArrayList<ExtinctionValueNode> getExtinctionCutByAttribute(int type, LinkedList folhas){
		int extinction;
		boolean visitado[] = new boolean[tree.getNumNode()];
		ArrayList<ExtinctionValueNode> extincaoPorNode = new ArrayList<ExtinctionValueNode>();
		//LinkedList<NodeToS> folhas = tree.getLeaves();
		for(Object obj: folhas){
			NodeToS folha = (NodeToS) obj;
			extinction = (int) tree.getRoot().getAttributeValue(type);
			NodeToS aux = folha;
			NodeToS pai = aux.getParent();
			boolean flag = true;
			while (flag  &&  pai != null) {
				if (pai.getNumChildren() > 1) {
					for(NodeToS filho: pai.getChildren()){  // verifica se possui irmao com area maior
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
		return extincaoPorNode;
	}
	
	public ColorImage extinctionByVolume(int attributeValue1, int attributeValue2) {
		int extincao;
		boolean visitado[] = new boolean[tree.getNumNode()];
		ArrayList<ExtinctionValueNode> extincaoPorNode = new ArrayList<ExtinctionValueNode>();		
		boolean continua = true;
		LinkedList<NodeToS> folhas = tree.getLeaves();
		for(NodeToS folha: folhas){
			extincao = (int) tree.getRoot().getAttributeValue(Attribute.VOLUME) * 2;
			NodeToS aux = folha;
			NodeToS pai = aux.getParent();
			while (continua  &&  pai != null) {
				if (pai.getNumChildren() > 1) {
					for(NodeToS filho: pai.getChildren()){  // verifica se possui irmao com area maior
						if(continua){
							
							int vF =  (int) filho.getAttributeValue(Attribute.VOLUME) + filho.getArea() * (filho.getLevel() - pai.getLevel());
							int vA =  (int) aux.getAttributeValue(Attribute.VOLUME) +   aux.getArea() * (aux.getLevel() - pai.getLevel());
							if (visitado[filho.getId()]  &&  filho != aux  &&  vF == vA) { //EMPATE Grimaud,92
								continua = false;
							}
							else if (filho != aux  &&  vF > vA) {
								continua = false;
							}
							visitado[filho.getId()] = true;
						}
					}
				}
				if (continua) {
					aux = pai;
					pai = aux.getParent();
				}
			}
			continua = true;
			if (pai != null){
				extincao = (int) aux.getAttributeValue(Attribute.VOLUME) + aux.getArea() * (aux.getLevel() - pai.getLevel());
			
				//extincaoPorNode[folha.getId()] = extincao;
				extincaoPorNode.add( new ExtinctionValueNode(folha, pai, extincao) );
			}

		}
		return reconstruction(extincaoPorNode, attributeValue1, attributeValue2);
	}
	
	public class ExtinctionValueNode implements Comparable<ExtinctionValueNode>{
		public NodeToS node;
		public NodeToS nodeAncestral;
		public int extinctionValue;
		public ExtinctionValueNode(NodeToS node, NodeToS nodeAncestral, int value){
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
