/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.ImageStack;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.Strel3D;

/**
 * Creates a 3D strel by replicating a 2D strel on a given number of slices.
 * 
 * @author dlegland
 *
 */
public class ExtrudedStrel extends AbstractStrel3D
{
	Strel strel2d;
	
	int sizeZ;
	int offsetZ;
	
	public ExtrudedStrel(Strel strel2d, int nSlices)
	{
		this.strel2d = strel2d;
		if (nSlices < 1)
		{
			throw new IllegalArgumentException("Requires at least one slice for creating extruded structuring element");
		}
		this.sizeZ = nSlices;
		this.offsetZ = (nSlices - 1) / 2;
	}
	
	
	public ExtrudedStrel(Strel strel2d, int nSlices, int offset)
	{
		this.strel2d = strel2d;
		if (nSlices < 1)
		{
			throw new IllegalArgumentException("Requires at least one slice for creating extruded structuring element");
		}
		this.sizeZ = nSlices;
		this.offsetZ = offset;
	}
	
	
	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.Strel3D#getSize()
	 */
	@Override
	public int[] getSize()
	{
		int[] sizes2d = strel2d.getSize();
		int[] sizes = new int[3];
		sizes[0] = sizes2d[0];
		sizes[1] = sizes2d[1];
		sizes[2] = sizeZ;
		return sizes;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.Strel3D#getMask3D()
	 */
	@Override
	public int[][][] getMask3D()
	{
		// get info from planar mask
		int[][] mask2d = this.strel2d.getMask();
		int[] size2d = this.strel2d.getSize();
		
		// allocate memory for 3D mask
		int[][][] mask = new int[this.sizeZ][size2d[1]][size2d[0]];
		
		// fill the 3D array
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < size2d[1]; y++)
			{
				for (int x = 0; x < size2d[0]; x++)
				{
					mask[z][y][x] = mask2d[y][x];
				}
			}
		}
		return mask;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.Strel3D#getOffset()
	 */
	@Override
	public int[] getOffset()
	{
		int[] offset2d = this.strel2d.getOffset();
		int[] offset = new int[3];
		offset[0] = offset2d[0];
		offset[1] = offset2d[1];
		offset[2] = this.offsetZ;
		return offset;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.Strel3D#getShifts3D()
	 */
	@Override
	public int[][] getShifts3D()
	{
		int[][] shifts2d = this.strel2d.getShifts();
		int nShifts2d = shifts2d.length;
		
		int nShifts = nShifts2d * this.sizeZ;
		int[][] shifts = new int[nShifts][3];
		int s = 0;
		for (int z = 0; z < sizeZ; z++)
		{
			for (int s2 = 0; s2 < nShifts2d; s2++)
			{
				shifts[s][0] = shifts2d[s2][0]; 
				shifts[s][1] = shifts2d[s2][1]; 
				shifts[s][2] = z - this.offsetZ; 
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.Strel3D#dilation(ij.ImageStack)
	 */
	@Override
	public ImageStack dilation(ImageStack image)
	{
		ImageStack result = this.strel2d.dilation(image);
		if (this.sizeZ > 1)
		{
			Strel3D zStrel = new LinearDepthStrel3D(this.sizeZ, this.offsetZ);
			result = zStrel.dilation(result);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.Strel3D#erosion(ij.ImageStack)
	 */
	@Override
	public ImageStack erosion(ImageStack image)
	{
		ImageStack result = this.strel2d.erosion(image);
		if (this.sizeZ > 1)
		{
			Strel3D zStrel = new LinearDepthStrel3D(this.sizeZ, this.offsetZ);
			result = zStrel.erosion(result);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.Strel3D#reverse()
	 */
	@Override
	public Strel3D reverse()
	{
		return new ExtrudedStrel(this.strel2d.reverse(), this.sizeZ, this.sizeZ - this.offsetZ - 1);
	}

}
