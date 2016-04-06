/**
 * 
 */
package inra.ijpb.morphology.directional;

import static java.lang.Math.*;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.strel.AbstractStrel;

/**
 * @author David Legland
 *
 */
public class OrientedLineStrel extends AbstractStrel implements Strel
{
	double length;
	double theta;
		
	int[][] shifts;

	public OrientedLineStrel(double length, double angleInDegrees)
	{
		this.length = length;
		this.theta = angleInDegrees;

		this.computeShifts();
	}

	private void computeShifts()
	{
	// Components of diection vector
		double thetaRads = Math.toRadians(this.theta);
		double dx = Math.cos(thetaRads);
		double dy = Math.sin(thetaRads);

		// length of projected line
		double dMax = max(abs(dx), abs(dy));
		double projLength = this.length * dMax;

		// half-size and size of the mask
		int n2 = (int) ceil((projLength - 1) / 2);
		int n = 2 * n2 + 1;

		// allocate memory for shifts array
		this.shifts = new int[n][2];

		// compute position of line pixels
		if (abs(dx) >= abs(dy))
		{
			for (int i = -n2; i <= n2; i++)
			{
				shifts[i + n2][0] = i;
				shifts[i + n2][1] = (int) round((double) i * dy / dx);
			}
		} 
		else
		{
			for (int i = -n2; i <= n2; i++)
			{
				shifts[i + n2][1] = i;
				shifts[i + n2][0] = (int) round((double) i * dx / dy);
			}
		}	
	}
	
	/**
	 * Returns the size of the structuring element, as an array of size in
	 * each direction.
	 * @return the size of the structuring element
	 */
	public int[] getSize() 
	{
		int n = this.shifts.length;
		return new int[]{n, n};
	}

	/**
	 * Returns the structuring element as a mask. Each value is either 0 or 255. 
	 * @return the mask of the structuring element
	 */
	public int[][] getMask() 
	{
		int n = this.shifts.length;
		int[][] mask = new int[n][n];
		
		// precompute offsets
		int[] offsets = this.getOffset();
		int ox = offsets[0];
		int oy = offsets[1];
		
		// fill up the mask
		for (int i = 0; i < n; i++)
		{
			mask[this.shifts[i][1] + oy][this.shifts[i][0] + ox] = 255;
		}
		
		return mask;
	}
	
	/**
	 * Returns the offset in the mask.
	 * @return the offset in the mask
	 */
	public int[] getOffset() 
	{
		int offset = (this.shifts.length - 1) / 2;
		return new int[]{offset, offset};
	}
	
	/**
	 * Returns the structuring element as a set of shifts.
	 * @return a set of shifts
	 */
	public int[][] getShifts() 
	{
		return this.shifts;
	}

	//TODO: well... should implement own version of morphological filters, 
	// or create an abstract class "SparseStrel" ?
	
	@Override
	public ImageProcessor dilation(ImageProcessor image)
	{
		return Filters.dilation(image, this);
	}

	@Override
	public ImageProcessor erosion(ImageProcessor image)
	{
		return Filters.erosion(image, this);
	}

	@Override
	public ImageProcessor closing(ImageProcessor image)
	{
		return this.erosion(this.dilation(image));
	}

	@Override
	public ImageProcessor opening(ImageProcessor image)
	{
		return this.dilation(this.erosion(image));
	}

	/**
	 * Returns this structuring element, as oriented line structuring elements
	 * are symmetric by definition.
	 */
	@Override
	public Strel reverse() 
	{
		return this;
	}
}
