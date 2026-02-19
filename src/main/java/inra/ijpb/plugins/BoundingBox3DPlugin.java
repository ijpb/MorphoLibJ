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
package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.measure.region3d.BoundingBox3D;

/**
 * Plugin for measuring extend of 3D bounding box from label images.
 *  
 * @author David Legland
 *
 */
public class BoundingBox3DPlugin implements PlugIn 
{
    // ====================================================
    // Calling functions 

	/* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    public void run(String args) 
    {
        ImagePlus imagePlus = IJ.getImage();

		if (imagePlus.getStackSize() == 1) {
			IJ.error("Requires a Stack");
			return;
		}
		
		
		ResultsTable table = new BoundingBox3D().computeTable(imagePlus);
        
 		// create string for indexing results
		String tableName = imagePlus.getShortTitle() + "-bbox"; 
    
		// show result
		table.show(tableName);
    }
    
}
