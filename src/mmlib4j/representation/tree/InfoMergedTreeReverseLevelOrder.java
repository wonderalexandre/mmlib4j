package mmlib4j.representation.tree;


import mmlib4j.images.GrayScaleImage;

/**
*  Must be built in reversed level order traversal
**/
public class InfoMergedTreeReverseLevelOrder extends InfoMergedTree {

	public InfoMergedTreeReverseLevelOrder(NodeLevelSets root, int numNodes, GrayScaleImage img) {							
		super(root, numNodes, img);
	}
	
	public void addNodeNotMerge(NodeLevelSets node){
		NodeLevelSets parent = node.getParent();
		if(parent != null){					
			this.numNode++;										
			
			if(map[parent.getId()] == null)
				map[parent.getId()] = new NodeMergedTree(node.getParent());			
			
			if(map[node.getId()] == null) 
				map[node.getId()] = new NodeMergedTree(node);						
			
			map[node.getId()].parent = map[parent.getId()];			
			map[parent.getId()].children.add(map[node.getId()]);
		}
	}		
	
	public void addNodeToMerge(NodeLevelSets node) {
		
		NodeLevelSets parent = node.getParent();	
		NodeMergedTree parentM = map[parent.getId()];	
		isMerged[node.getId()] = true;
		
		// When you comes from the leaves, the parent of node can not be allocated yet
		if(parentM == null) 	
			parentM = map[parent.getId()] = new NodeMergedTree(parent);						
		
		// This node is a parent that it was explored before
		if(map[node.getId()] != null) {	
			// Join compact node pixels	
			parentM.getCompactNodePixels().addAll(map[node.getId()].getCompactNodePixels());
			// Pass the children to parentM
			for(NodeMergedTree child_ : map[node.getId()].getChildren()) {						
				parentM.children.add(child_);
				map[child_.info.getId()].parent = map[parent.getId()];
			}
		} else {
			parentM.getCompactNodePixels().add(node.getCompactNodePixels());
		}

		// This makes a mapping from a removed node and its merged representation.
		map[node.getId()] = parentM;
		
	}
	
}
