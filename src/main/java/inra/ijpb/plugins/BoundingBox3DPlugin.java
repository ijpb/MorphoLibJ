package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.measure.GeometricMeasures3D;

/**
 * Plugin for measuring geometric quantities such as volume, surface area 
 * @author David Legland
 *
 */
public class BoundingBox3DPlugin implements PlugIn {
  
    // ====================================================
    // Calling functions 

	/* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    public void run(String args) {
        ImagePlus imagePlus = IJ.getImage();

        ResultsTable table = GeometricMeasures3D.boundingBox(imagePlus.getStack());
        
 		// create string for indexing results
		String tableName = imagePlus.getShortTitle() + "-bounds"; 
    
		// show result
		table.show(tableName);
    }
    
}
