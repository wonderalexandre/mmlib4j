package mmlib4j.representation.tree.componentTree;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.representation.tree.NodeLevelSets;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface BuilderComponentTree {

	public void build( );
		
	public NodeLevelSets getRoot( );
	
	public NodeLevelSets[] getMap( );
	
	public SimpleLinkedList<NodeLevelSets> getListNodes();
	
	public int getNunNode();
	
	public int getNumNodeIdMax();
	
	public BuilderComponentTree getClone();
	
	
}
