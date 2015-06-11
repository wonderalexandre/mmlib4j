package mmlib4j.representation.tree;

import java.util.List;


/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface INodeTree {
	
	public INodeTree getParent();
	
	public int getArea();
	
	public List getChildren();
	
	public int getLevel();
	
	public int getId();
}
