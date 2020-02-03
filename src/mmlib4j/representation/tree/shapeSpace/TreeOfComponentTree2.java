package mmlib4j.representation.tree.shapeSpace;


import mmlib4j.datastruct.PriorityQueueDial;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.AbstractImageFactory;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.ComponentTree;

public class TreeOfComponentTree2 {
	
	// Original Component Tree
	private ComponentTree tree;
	private int numNode;
	private GrayScaleImage input;
	
	// Tree of Shape Space
	private ComponentTree shapeSpacetree;
	private GrayScaleImage imgAttr;
	
	public TreeOfComponentTree2(ComponentTree tree, int attr, boolean isMaxtree) {
				
		this.tree = tree;
		this.numNode = tree.getNumNode();
		GrayScaleImage attrImg = AbstractImageFactory.instance.createGrayScaleImage(AbstractImageFactory.DEPTH_32BITS, 1, numNode);		
		// Imagem do atributo (apenas para os nós)
		for(NodeLevelSets node : tree.getListNodes()) {
			attrImg.setPixel(node.getId(), (int) node.getAttributeValue(attr));
		}
		
		// Queue
		PriorityQueueDial queue;
		if(isMaxtree)
			queue  = new PriorityQueueDial(attrImg, attrImg.maxValue()+1, PriorityQueueDial.LIFO, true);
		else
			queue  = new PriorityQueueDial(attrImg, attrImg.maxValue()+1, PriorityQueueDial.LIFO);
		
				
		
	}
	
	// Pruning Shape Space Tree
	public void prunning(double value) {
		
	
		
	}
	
	public GrayScaleImage reconstruction(){		
		return null;		
	}
	
	public static void main(String args[]) {
		
		/*GrayScaleImage input = ImageBuilder.openGrayImage(new File("/Users/gobber/Desktop/lena.jpg"));
				
		ComponentTree tree = new ComponentTree(input, AdjacencyRelation.getAdjacency8(), true);		
		ConnectedFilteringByComponentTree ctree = new ConnectedFilteringByComponentTree(tree);
		ctree.computerBasicAttribute();
		
		TreeOfComponentTree2 TT = new TreeOfComponentTree2(tree, 
														 Attribute.AREA,
														 false);
		
		TT.prunning(100);
		
		GrayScaleImage output = TT.reconstruction();
		
		ConnectedFilteringByComponentTree tree2 = new ConnectedFilteringByComponentTree(input, AdjacencyRelation.getAdjacency8(), true);
		GrayScaleImage img2 = tree2.filteringByPruning(100, Attribute.AREA);
		
		System.out.println(ImageAlgebra.equals(output, img2));
		System.out.println(ImageAlgebra.isLessOrEqual(output, input));
		ImageBuilder.saveImage(output, new File("/Users/gobber/Desktop/output.png"));*/
		
	}

}
