package mmlib4j.representation.tree.componentTree;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.utils.AdjacencyRelation;

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
	
	public GrayScaleImage getInputImage();
	
	public boolean isMaxtree();
	
	public AdjacencyRelation getAdjacencyRelation();

}