/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.measure.RegionAdjacencyGraph;
import inra.ijpb.measure.RegionAdjacencyGraph.LabelPair;

import java.util.Set;

/**
 * @author dlegland
 *
 */
public class RegionAdjacencyGraphPlugin implements PlugIn
{

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0)
	{
		ImagePlus image = IJ.getImage();
		
		Set<LabelPair> adjList = RegionAdjacencyGraph.computeAdjacencies(image);
		
		ResultsTable table = new ResultsTable();
		table.setPrecision(0);
		
		// populate the table with the list of adjacencies
		for (LabelPair pair : adjList)
		{
			table.incrementCounter();
			table.addValue("Label 1", pair.label1);
			table.addValue("Label 2", pair.label2);
		}
		
		String newName = image.getShortTitle() + "-RAG";
		table.show(newName);
	}
}
