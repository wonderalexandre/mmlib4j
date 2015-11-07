package mmlib4j.representation.tree.mst;

import junit.framework.Assert;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.graph.rag.RegionAdjcencyGraph;
import org.junit.Test;

import static mmlib4j.images.impl.ImageFactory.createReferenceGrayScaleImage;
import static mmlib4j.representation.graph.rag.RegionAdjcencyGraph.getRAGByFlatzone;

/**
 * @author Renã Souza on 07/11/15.
 */
public class MorphologicalTreeByKruskalTest {

    @Test
    public void testNodesNumber(){

        byte pixels[] = new byte[] {
               0, 1, 3, 3,
               0, 2, 4, 4
        };

        int width = 4;
        int height = 2;
        GrayScaleImage img = createReferenceGrayScaleImage(8, pixels, width, height);

        RegionAdjcencyGraph graph = getRAGByFlatzone(img);
        MorphologicalTreeByKruskal mtree = new MorphologicalTreeByKruskal(graph);
        MorphologicalTreeByKruskal.DisjointSetBPT bpt = mtree.getBinaryPartitionTree();

        //Nodes number
        Assert.assertEquals(9, bpt.size);
    }

}
