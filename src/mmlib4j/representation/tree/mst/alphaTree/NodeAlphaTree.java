package mmlib4j.representation.tree.mst.alphaTree;

import java.util.LinkedList;
import java.util.List;

import mmlib4j.images.GrayScaleImage;

public class NodeAlphaTree {
	int id;
	int level;
	int heightNode;
	boolean isLeaf;
	int alpha;
	GrayScaleImage img;
	
	int area;
	int volume;
	
	NodeAlphaTree parent;
	List<NodeAlphaTree> children = null;
	LinkedList<Integer> pixels = new LinkedList<Integer>();
	
	public NodeAlphaTree(int numCreate, int level, GrayScaleImage img, boolean isLeaf){
		this.id = numCreate;
		this.level = level; 
		this.img = img;
		this.isLeaf = isLeaf;
	}
	
	public int getId(){
		return id;
	}
	
	public void addPixel(int p){
		area += 1;
		volume += (level);
		pixels.add(p);
	}
	
	public LinkedList<Integer> getPixels(){
		return pixels;
	}

	public int getArea(){
		return area;
	}

	public int getVolume(){
		return volume;
	}
	
	public NodeAlphaTree getParent() {
		return parent;
	}

	public List<NodeAlphaTree> getChildren(){
		return children;
	}
	
	public int getLevel(){
		return level;
	}
	
}
