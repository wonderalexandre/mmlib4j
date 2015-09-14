package mmlib4j.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import mmlib4j.images.BinaryImage;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.Image2D;
import mmlib4j.images.RealImage;
import mmlib4j.utils.ImageBuilder;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public abstract class WindowImages {
	
	public static WindowImages instance = new WindowSwing();
	
    public static void show(Image2D img, String titles) {
    	instance.showImpl(new Image2D[]{img}, new String[]{titles});
    }

    public static void show(Image2D img){
    	show(new Image2D[]{img});
    }
    
    public static void show(Image2D img[], String[] titles){
    	instance.showImpl(img,titles);
    }
    
    public static void show(Image2D img[]){
         String titles[] = new String[img.length];
        for(int i=0; i < img.length; i++){
            titles[i] = String.valueOf(i);
        }
        instance.showImpl(img, titles);
    }
    
    public abstract void close();
    
    
    public abstract void showImpl(Image2D[] img, String[] titles);
    	    
}

class WindowSwing extends WindowImages{

	private Map<String, BufferedImage> map = new HashMap<String, BufferedImage>();
    private static final String PROJECT_NAME = "MMLib4J";
    JFrame lastWindow;
    
	public void showImpl(Image2D[] img, String[] titles) {
        BufferedImage im[] = new BufferedImage[img.length];
        for(int i=0; i < img.length; i++){
        	if(img[i] instanceof GrayScaleImage)
        		im[i] = ImageBuilder.convertToImage((GrayScaleImage) img[i]);
        	else if(img[i] instanceof ColorImage)
        		im[i] = ImageBuilder.convertToImage((ColorImage) img[i]);
        	else if(img[i] instanceof BinaryImage)
        		im[i] = ImageBuilder.convertToImage((BinaryImage) img[i]);
        	else if(img[i] instanceof RealImage)
        		im[i] = ImageBuilder.convertToImage(img[i]);
        	else
        		throw new RuntimeException("Error: type image");
        }
        final JFrame dialog = getJFrame(im, PROJECT_NAME, titles, true);
        dialog.setVisible(true);
        
    }    
    
	public void close(){
		lastWindow.dispose();
	}
	
    public JFrame getJFrame(BufferedImage img[], String title, String titles[], boolean isSalvar){
        final JFrame dialog = new JFrame();
        dialog.setTitle(title);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        boolean flag = false;
        JPanel panelPrincipal = new JPanel();
        for(int i=0; i < img.length; i++){
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JLabel label = new JLabel();
            
            label.setIcon(new ImageIcon(img[i]));
            if(titles == null)
                label.setBorder(BorderFactory.createTitledBorder(img[i].toString()));
            else
                label.setBorder(BorderFactory.createTitledBorder(titles[i]));
            
            JButton btnSalvar = new JButton("Save");
            btnSalvar.setActionCommand(String.valueOf(btnSalvar.hashCode()));
            map.put(btnSalvar.getActionCommand(), img[i]);
            btnSalvar.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    JFileChooser fc = new JFileChooser();
                    if(fc.showSaveDialog(null)  == JFileChooser.APPROVE_OPTION)
                    	ImageBuilder.saveImage(map.get(e.getActionCommand()), fc.getSelectedFile());
                }
            });
            panel.add(label, BorderLayout.CENTER);
            if(isSalvar)
                panel.add(btnSalvar, BorderLayout.SOUTH);
            
            panelPrincipal.add(panel,  BorderLayout.CENTER);
            if(label.getIcon().getIconWidth() > 1024 || label.getIcon().getIconHeight() > 860) flag = true;
        }
        panelPrincipal.setBackground(Color.LIGHT_GRAY);
        dialog.add(panelPrincipal);
        dialog.pack();
        dialog.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
               dialog.dispose();
            }
        });
        if(flag){
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.getViewport().add( panelPrincipal );
            dialog.add(scrollPane);
            dialog.setSize(750, 580);
        }
        lastWindow = dialog;
        return dialog;
    }
}

