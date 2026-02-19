/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
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
