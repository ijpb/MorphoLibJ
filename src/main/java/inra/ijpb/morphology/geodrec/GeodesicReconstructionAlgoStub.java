/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import inra.ijpb.algo.AlgoStub;

/**
 * Implementation basis for geodesic reconstruction algorithms for planar images.
 * 
 * @author dlegland
 *
 */
public abstract class GeodesicReconstructionAlgoStub extends AlgoStub implements
		GeodesicReconstructionAlgo
{
	/**
	 * The connectivity of the algorithm, either 4 or 8.
	 */
	protected int connectivity = 4;
	
	/**
	 * boolean flag for toggling the display of debugging infos.
	 */
	public boolean verbose = false;
	
	public boolean showStatus = true;
	
	public boolean showProgress = false; 

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.geodrec.GeodesicReconstructionAlgo#getConnectivity()
	 */
	@Override
	public int getConnectivity()
	{
		return this.connectivity;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.geodrec.GeodesicReconstructionAlgo#setConnectivity(int)
	 */
	@Override
	public void setConnectivity(int conn)
	{
		this.connectivity = conn;
	}

}
