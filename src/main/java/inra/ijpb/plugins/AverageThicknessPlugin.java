/**
 * 
 */
package inra.ijpb.plugins;

import java.util.Map;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region2d.AverageThickness;

/**
 * @author dlegland
 *
 */
public class AverageThicknessPlugin implements PlugIn
{

    @Override
    public void run(String arg)
    {
        // selected image
        ImagePlus currentImage = IJ.getImage();
        
        // check if image is a label image
        if (!LabelImages.isLabelImageType(currentImage))
        {
            IJ.showMessage("Input image should be a label image");
            return;
        }
        
        // Execute the plugin
        AverageThickness op = new AverageThickness();
        Map<Integer, AverageThickness.Result> results = op.analyzeRegions(currentImage);
        
        // Display plugin result as table
        ResultsTable table = op.createTable(results);
        String tableName = currentImage.getShortTitle() + "-AvgThickness";
        table.show(tableName);
    }
}
