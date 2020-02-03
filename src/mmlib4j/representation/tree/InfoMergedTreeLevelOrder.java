package mmlib4j.representation.tree;

import mmlib4j.images.GrayScaleImage;

/**
*  Must be built in level order traversal
**/
public class InfoMergedTreeLevelOrder extends InfoMergedTree {	

	public InfoMergedTreeLevelOrder(NodeLevelSets root, int numNodes, GrayScaleImage img) {							
		super(root, numNodes, img);
	}
	
	public void addNodeNotMerge(NodeLevelSets node){
		NodeLevelSets parent = node.getParent();
		if(parent != null){					
			this.numNode++;										
			
			if(map[parent.getId()] == null)
				map[parent.getId()] = new NodeMergedTree(node.getParent());			
			
			if(map[node.getId()] == null) {
				map[node.getId()] = new NodeMergedTree(node);
				map[parent.getId()].children.add(map[node.getId()]);
			}	
			
			map[node.getId()].parent = map[parent.getId()];			
		}
	}
	
	public void addNodeToMerge(NodeLevelSets node) {
		
		NodeLevelSets parent = node.getParent();	
		NodeMergedTree parentM = map[parent.getId()];
		isMerged[node.getId()] = true;
		
		// If node != null, it was entered before, during the children propagation below
		if(map[node.getId()] != null) 
			parentM.children.remove(map[node.getId()]);		
		
		// Join compact node pixels	
		parentM.getCompactNodePixels().add(node.getCompactNodePixels());
		
		// Add new fake children 
		for(NodeLevelSets child : node.getChildren()) {					
			NodeMergedTree child_ = new NodeMergedTree(child);
			map[child.getId()] = child_;
			parentM.children.add(child_);
			map[child.getId()].parent = map[parent.getId()];
		}
		
		// This makes a mapping from a removed node and its merged representation.
		map[node.getId()] = parentM;
		
	}
	
}
