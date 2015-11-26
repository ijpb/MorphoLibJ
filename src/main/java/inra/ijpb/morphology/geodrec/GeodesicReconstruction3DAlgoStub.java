/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import ij.IJ;
import inra.ijpb.algo.AlgoStub;

/**
 * Implementation basis for geodesic reconstruction algorithms for 3D images.
 * 
 * @author dlegland
 *
 */
public abstract class GeodesicReconstruction3DAlgoStub extends AlgoStub implements
		GeodesicReconstruction3DAlgo
{
	/**
	 * The connectivity of the algorithm, either 6 or 26.
	 */
	protected int connectivity = 6;
	
	/**
	 * Boolean flag for the display of debugging infos.
	 */
	public boolean verbose = false;
	
	/**
	 * Boolean flag for the display of algorithm state in ImageJ status bar
	 */
	public boolean showStatus = true;
	
	/**
	 * Boolean flag for the display of algorithm progress in ImageJ status bar
	 */
	public boolean showProgress = false; 

	
	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.geodrec.GeodesicReconstruction3DAlgo#getConnectivity()
	 */
	@Override
	public int getConnectivity()
	{
		return this.connectivity;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.geodrec.GeodesicReconstruction3DAlgo#setConnectivity(int)
	 */
	@Override
	public void setConnectivity(int conn)
	{
		this.connectivity = conn;
	}

	
	protected void showStatus(String status)
	{
		if (this.showStatus) 
		{
			IJ.showStatus(status);
		}
	}
	
	protected void showProgress(double current, double max)
	{
		if (showProgress) 
		{
			IJ.showProgress(current / max);
		}
	}
	
	protected void showProgress(double current, double max, String msg)
	{
		if (showProgress) 
		{
			IJ.showProgress(current / max);
			if (msg != null && !msg.isEmpty())
			{
				trace(msg);
			}
		}
	}

	protected void trace(String traceMessage)
	{
		// Display current status
		if (verbose) 
		{
			System.out.println(traceMessage);
		}
	}
}
