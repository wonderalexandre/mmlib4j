package mmlib4j.representation.tree.mst.bpt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mmlib4j.images.GrayScaleImage;

public class NodeBPT {
	int id;
	int level;
	int orderTree;
	int heightNode;
	boolean isLeaf;
	boolean isWatershed;
	GrayScaleImage img;
	byte aux;
	
	int area;
	int volume;
	NodeBPT parent;
	NodeBPT left = null;
	NodeBPT right = null;
	LinkedList<Integer> pixels = new LinkedList<Integer>();
	List<NodeBPT> children = null;
	
	public NodeBPT(int numCreate, int level, GrayScaleImage img, boolean isLeaf){
		this.id = numCreate;
		this.level = level; 
		this.img = img;
		this.isLeaf = isLeaf;
	}
	
	public int getId(){
		return id;
	}
	
	public void addPixel(int p){
		pixels.add(p);
		area+=1;
		volume += img.getPixel(p);
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

	public NodeBPT getParent() {
		return parent;
	}
	
	public List<NodeBPT> getChildren(){
		if(children == null && left != null && right != null){
			children = new ArrayList<NodeBPT>();
			children.add(left);
			children.add(right);
		}
		return children;
	}
	public int getLevel(){
		return level;
	}
}
