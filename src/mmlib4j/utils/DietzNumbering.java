package mmlib4j.utils;

import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.AttributeComputedIncrementally;


public class DietzNumbering extends AttributeComputedIncrementally{
	
	int cnt = 0;
	int preOrder[];
	int postOrder[];
	MorphologicalTreeFiltering tree;
	
	public DietzNumbering(MorphologicalTreeFiltering tree) {
		this.tree = tree;
		preOrder  = new int[tree.getNumNode()];
		postOrder = new int[tree.getNumNode()];
		computerAttribute(tree.getRoot());
	}
	
	public boolean isAncestor(NodeLevelSets u, NodeLevelSets v) {
		return (preOrder[u.getId()] <= preOrder[v.getId()] && postOrder[v.getId()] <= postOrder[u.getId()]);
	}
	
	public boolean isDescendant(NodeLevelSets u, NodeLevelSets v) {
		return isAncestor(v, u);
	}

	@Override
	public void preProcessing(NodeLevelSets v) {
		preOrder[v.getId()] = cnt++;
	}

	@Override
	public void mergeChildren(NodeLevelSets parent, NodeLevelSets son) {}

	@Override
	public void posProcessing(NodeLevelSets parent) {
		postOrder[parent.getId()] = cnt++;		
	}
}