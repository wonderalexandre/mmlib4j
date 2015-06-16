package mmlib4j.representation.tree.componentTree;

import java.util.HashSet;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface BuilderComponentTree {

	public void build( );
		
	public NodeCT getRoot( );
	
	public NodeCT[] getMap( );
	
	public HashSet<NodeCT> getListNodes();
	
	public int getNunNode();
	
	public int getNumNodeIdMax();
	
	public BuilderComponentTree getClone();
	
	
}
