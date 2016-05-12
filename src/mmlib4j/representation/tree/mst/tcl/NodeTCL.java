package mmlib4j.representation.tree.mst.tcl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mmlib4j.images.GrayScaleImage;

public class NodeTCL{
	int id;
	int level;
	int width;
	int height;
	int attribute;
	int heightNode;
	int label;
	boolean isLeaf;
	boolean isWatershed;
	
	GrayScaleImage img;
	
	byte aux;
	
	int area;
	int volume;
	
	NodeTCL parent;
	NodeTCL left = null;
	NodeTCL right = null;
	LinkedList<Integer> pixels = new LinkedList<Integer>();
	ArrayList<NodeTCL> children = null;
	
	public NodeTCL(int numCreate, int level, GrayScaleImage img, boolean isLeaf){
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
	

	public NodeTCL getParent() {
		return parent;
	}
	
	public List<NodeTCL> getChildren(){
		if(children == null){
			children = new ArrayList<NodeTCL>();
			children.add(left);
			children.add(right);
		}
		return children;
	}
	
	public int getLevel(){
		return level;
	}
}
