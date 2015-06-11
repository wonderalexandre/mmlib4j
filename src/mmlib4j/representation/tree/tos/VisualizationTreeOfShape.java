package mmlib4j.representation.tree.tos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.utils.ImageBuilder;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;


/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class VisualizationTreeOfShape extends JPanel {

    Forest<NodeToS,Integer> graph;
    VisualizationViewer<NodeToS,Integer> vv;
    TreeLayout<NodeToS,Integer> treeLayout;
    TreeOfShape compTree;
    
    public VisualizationTreeOfShape(TreeOfShape tree) {
    	this(tree, null, null);
    }
    
    public VisualizationTreeOfShape(TreeOfShape tree, boolean map1[], boolean map2[]) {
        super.setLayout(new BorderLayout());
    	this.compTree = tree;
    	graph = new DelegateTree<NodeToS,Integer>();
        
        ((DelegateTree)graph).setRoot(tree.getRoot());
        createTree(tree.getRoot());
        
        treeLayout = new TreeLayout<NodeToS,Integer>(graph);
        vv =  new VisualizationViewer<NodeToS,Integer>(treeLayout);
        vv.setBackground(Color.white);
        //vv.getRenderContext().setEdgeDrawPaintTransformer(edgeDrawPaintTransformer);
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
        vv.getRenderContext().setVertexLabelTransformer(new LabelNodesToS());
        vv.getRenderContext().setVertexFillPaintTransformer(new NodeShapeToS(map1, map2));
        // add a listener for ToolTips
        vv.setVertexToolTipTransformer(new LabelNodesToS());
        vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray));
        

        //Container content = getContentPane();
        
        
        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(graphMouse);
        vv.addGraphMouseListener(new GraphMouseListener<NodeToS>() {
			public void graphReleased(NodeToS v, MouseEvent me) {}
			public void graphPressed(NodeToS v, MouseEvent me) {}
			public void graphClicked(NodeToS v, MouseEvent me) {
				//JOptionPane.showMessageDialog(null, me.getPoint());
				WindowImages.show(v.createImageSC(), "Node:" + v.getLevel());
			}
		});
		
		
        final ScalingControl scaler = new CrossoverScalingControl();

        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1/1.1f, vv.getCenter());
            }
        });
        
        JPanel scaleGrid = new JPanel(new GridLayout(1,0));
        scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
        scaleGrid.add(plus);
        scaleGrid.add(minus);
        
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        
        super.add(panel, BorderLayout.CENTER);
        super.add(scaleGrid, BorderLayout.SOUTH);
        
        
    }
    
    public VisualizationTreeOfShape(InfoPrunedTree prunedTree, boolean map1[], boolean map2[]) {
        super.setLayout(new BorderLayout());
    	this.compTree = (TreeOfShape) prunedTree.getTree();
    	graph = new DelegateTree<NodeToS,Integer>();
        
        ((DelegateTree)graph).setRoot(compTree.getRoot());
        createTree(prunedTree.getRoot());
        
        treeLayout = new TreeLayout<NodeToS,Integer>(graph);
        vv =  new VisualizationViewer<NodeToS,Integer>(treeLayout);
        vv.setBackground(Color.white);
        //vv.getRenderContext().setEdgeDrawPaintTransformer(edgeDrawPaintTransformer);
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
        vv.getRenderContext().setVertexLabelTransformer(new LabelNodesToS());
        vv.getRenderContext().setVertexFillPaintTransformer(new NodeShapeToS(map1, map2));
        // add a listener for ToolTips
        vv.setVertexToolTipTransformer(new LabelNodesToS());
        vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray));
        

        //Container content = getContentPane();
        
        
        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(graphMouse);
        vv.addGraphMouseListener(new GraphMouseListener<NodeToS>() {
			public void graphReleased(NodeToS v, MouseEvent me) {}
			public void graphPressed(NodeToS v, MouseEvent me) {}
			public void graphClicked(NodeToS v, MouseEvent me) {
				//JOptionPane.showMessageDialog(null, me.getPoint());
				WindowImages.show(v.createImageSC(), "Node:" + v.getLevel());
			}
		});
		
		
        final ScalingControl scaler = new CrossoverScalingControl();

        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1/1.1f, vv.getCenter());
            }
        });
        
        JPanel scaleGrid = new JPanel(new GridLayout(1,0));
        scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
        scaleGrid.add(plus);
        scaleGrid.add(minus);
        
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        
        super.add(panel, BorderLayout.CENTER);
        super.add(scaleGrid, BorderLayout.SOUTH);
        
        
    }
    
    
    /**
     * 
     */
    private int id=0;
    private void createTree(NodeToS node) {
    	if(node != compTree.getRoot()){
    		((DelegateTree)graph).addChild(id++, node.getParent(), node);
    	}
    	for(NodeToS son: node.getChildren()){
    		createTree(son);
    	}   	
    }

    private void createTree(InfoPrunedTree.NodePrunedTree node) {
    	if(node.getInfo() != compTree.getRoot()){
    		((DelegateTree)graph).addChild(id++, node.getInfo().getParent(), node.getInfo());
    	}
    	for(InfoPrunedTree.NodePrunedTree son: node.getChildren()){
    		createTree(son);
    	}   	
    }

    /**
     * a driver for this demo
     */
    public static JFrame getInstance(TreeOfShape tree) {
        return getInstance(tree, null, null);
    }

    public static JFrame getInstance(TreeOfShape tree, boolean map1[], boolean map2[]) {
        JFrame frame = new JFrame("Tree of shape");
        Container content = frame.getContentPane();
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        content.add(new VisualizationTreeOfShape(tree, map1, map2));
        frame.pack();
        return frame;
    }
    
	public static JFrame getInstance(InfoPrunedTree prunedTree) {
        JFrame frame = new JFrame("Tree of shape");
        Container content = frame.getContentPane();
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        content.add(new VisualizationTreeOfShape(prunedTree, null, null));
        frame.pack();
        return frame;
    }
    

	public static JFrame getInstance(InfoPrunedTree prunedTree, boolean map1[], boolean map2[]) {
		JFrame frame = new JFrame("Tree of shape");
		Container content = frame.getContentPane();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		content.add(new VisualizationTreeOfShape(prunedTree, map1, map2));
		frame.pack();
		return frame;
	}
	
    public static void main(String args[]){
    	GrayScaleImage imgInput = ImageBuilder.openGrayImage();
    	TreeOfShape tree = new TreeOfShape(imgInput);
    	tree.extendedTree();
    	getInstance(tree).setVisible(true);;
    }
    
}

class LabelNodesToS implements Transformer<NodeToS,String> {

    public String transform(NodeToS v) {
        return  String.valueOf(v.getLevel());
    }
 }

class NodeShapeToS implements Transformer<NodeToS, Paint> {
	Transformer<NodeToS, Paint> t1 = new ConstantTransformer(Color.RED);
	Transformer<NodeToS, Paint> t1Leaf = new ConstantTransformer(Color.decode("0X990000"));
	Transformer<NodeToS, Paint> t1Selected = new ConstantTransformer(Color.GREEN);

	Transformer<NodeToS, Paint> t2 = new ConstantTransformer(Color.BLUE);
	Transformer<NodeToS, Paint> t2Leaf = new ConstantTransformer(Color.decode("0X000099"));
	Transformer<NodeToS, Paint> t2Selected = new ConstantTransformer(Color.GREEN);
	
	boolean selected[];
	boolean selected2[];
	
	NodeShapeToS(boolean map1[], boolean map2[]){
		selected = map1;
		selected2 = map2;
	}
	
	public Paint transform(NodeToS node) {
		
		if (node.isNodeMaxtree()) {
			if (selected != null && selected[node.hashCode()]) {
				return t1Selected.transform(node);

			} 
			else if (selected2 != null && selected2[node.hashCode()]) {
				return t1Leaf.transform(node);
			} 
			else {
				if (!node.isClone())
					return t1Leaf.transform(node);
				else
					return t1.transform(node);
			}
		} else {
			if (selected != null && selected[node.hashCode()]) {
				return t2Selected.transform(node);
			} 
			else if (selected2 != null && selected2[node.hashCode()]) {
				return t2Leaf.transform(node);
			} 
			else {
				if (!node.isClone())
					return t2Leaf.transform(node);
				else
					return t2.transform(node);
			}
		}
		
		
	}
	
	
}
