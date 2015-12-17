package mmlib4j.filtering;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.utils.AdjacencyRelation;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class LinearFilters {
	
	static mmlib4j.filtering.RankFilters filter = new mmlib4j.filtering.RankFilters();
	
	public static GrayScaleImage median(GrayScaleImage img, AdjacencyRelation adj){
		return filter.rank(img, adj, RankFilters.MEDIAN);
	}
	
	public static GrayScaleImage mean(GrayScaleImage img, AdjacencyRelation adj){
		return filter.rank(img, adj, RankFilters.MEAN);
	}
	
	
}
