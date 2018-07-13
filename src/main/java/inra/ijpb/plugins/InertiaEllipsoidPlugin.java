/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
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
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.measure.region3d.InertiaEllipsoid;

public class InertiaEllipsoidPlugin implements PlugIn
{
    // ====================================================
    // Class variables
    
	ImagePlus imagePlus;
	
    
    // ====================================================
    // Calling functions 
   

	/* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    public void run(String args)
    {
        ImagePlus imagePlus = IJ.getImage();
    
		if (imagePlus.getStackSize() == 1) 
		{
			IJ.error("Requires a Stack");
			return;
		}

        ResultsTable table;
        try 
        {
        	InertiaEllipsoid algo = new InertiaEllipsoid();
        	DefaultAlgoListener.monitor(algo);
        	table = algo.computeTable(imagePlus);
        } 
        catch (Exception ex) 
        {
        	String msg = ex.getMessage();
        	IJ.log(msg);
			IJ.error("Problem occured during Inertia Ellipsoid computation:\n" + msg);
        	ex.printStackTrace(System.err);
        	return;
        }

        // show table results
        String title = imagePlus.getShortTitle() + "-ellipsoid";
        table.show(title);
    }
}
