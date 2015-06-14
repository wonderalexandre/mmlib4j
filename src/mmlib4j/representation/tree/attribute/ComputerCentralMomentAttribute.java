package mmlib4j.representation.tree.attribute;

import java.util.HashSet;

import mmlib4j.representation.tree.INodeTree;
import mmlib4j.representation.tree.componentTree.NodeCT;

public class ComputerCentralMomentAttribute extends AttributeComputedIncrementally{
	
	CentralMomentsAttribute attr[];
	int numNode;
	int withImg;
	
	public ComputerCentralMomentAttribute(int numNode, INodeTree root, int withImg){
		this.numNode = numNode;
		this.withImg = withImg;
		attr = new CentralMomentsAttribute[numNode];
		computerAttribute(root);
	}

	public CentralMomentsAttribute[] getAttribute(){
		return attr;
	}
	
	public void addAttributeInNodesCT(HashSet<NodeCT> hashSet){
		for(INodeTree node: hashSet){
			node.addAttribute(Attribute.MOMENT_CENTRAL_02, attr[ node.getId() ].moment02);
			node.addAttribute(Attribute.MOMENT_CENTRAL_20, attr[ node.getId() ].moment20);
			node.addAttribute(Attribute.MOMENT_CENTRAL_11, attr[ node.getId() ].moment11);
			
			node.addAttribute(Attribute.COMPACTNESS, new Attribute(Attribute.COMPACTNESS, attr[ node.getId() ].compactness()));
			node.addAttribute(Attribute.ECCENTRICITY, new Attribute(Attribute.ECCENTRICITY, attr[ node.getId() ].eccentricity()));
			node.addAttribute(Attribute.ELONGATION, new Attribute(Attribute.ELONGATION, attr[ node.getId() ].elongation()));
			node.addAttribute(Attribute.LENGTH_MAJOR_AXES, new Attribute(Attribute.LENGTH_MAJOR_AXES, attr[ node.getId() ].getLengthMajorAxes()));
			node.addAttribute(Attribute.LENGTH_MINOR_AXES, new Attribute(Attribute.LENGTH_MINOR_AXES, attr[ node.getId() ].getLengthMinorAxes()));
			node.addAttribute(Attribute.MOMENT_ORIENTATION, new Attribute(Attribute.MOMENT_ORIENTATION, attr[ node.getId() ].getMomentOrientation()));
			
		}
	}
	
	
	public void initialization(INodeTree node) {
		attr[node.getId()] = new CentralMomentsAttribute(node.getCentroid(), node.getArea(), withImg);
		//area e volume
		
		for(int pixel: node.getCanonicalPixels()){
			int x = pixel % withImg;
			int y = pixel / withImg;
			attr[node.getId()].moment11.value += Math.pow(x - attr[node.getId()].xCentroid, 1) * Math.pow(y - attr[node.getId()].yCentroid, 1);
			attr[node.getId()].moment20.value += Math.pow(x - attr[node.getId()].xCentroid, 2) * Math.pow(y - attr[node.getId()].yCentroid, 0);
			attr[node.getId()].moment02.value += Math.pow(x - attr[node.getId()].xCentroid, 0) * Math.pow(y - attr[node.getId()].yCentroid, 2);
		
		}
		
		
	}
	
	public void updateChildren(INodeTree node, INodeTree son) {
		attr[node.getId()].moment11.value += attr[son.getId()].moment11.value;
		attr[node.getId()].moment02.value +=  attr[son.getId()].moment02.value;
		attr[node.getId()].moment20.value += attr[son.getId()].moment20.value;
		
	}

	public void posProcessing(INodeTree root) {
		//pos-processing root
		
	}
	
}
