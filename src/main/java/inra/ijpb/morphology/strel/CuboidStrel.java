/**
 * 
 */
package inra.ijpb.morphology.strel;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A cubic structuring element, obtained by decomposition into linear
 * structuring elements with the same size along each dimension.
 * 
 * @see BallStrel
 * @see LinearHorizontalStrel
 * @see LinearVerticalStrel
 * @see LinearDepthStrel3D
 * 
 * @author David Legland
 *
 */
public class CuboidStrel extends AbstractSeparableStrel3D 
{
	// ==================================================
	// Static methods 
	
	public final static CuboidStrel fromDiameter(int diam) 
	{
		return new CuboidStrel(diam, diam, diam);
	}
	
	public final static CuboidStrel fromRadius(int radius)
	{
		int diam = 2 * radius + 1;
		return new CuboidStrel(diam, diam, diam, radius, radius, radius);
	}
	
	/**
	 * Creates a 3D structuring element with a cuboid shape and different sizes
	 * depending on direction.
	 * 
	 * @param radiusX
	 *            radius in x direction
	 * @param radiusY
	 *            radius in y direction
	 * @param radiusZ
	 *            radius in z direction
	 * @return a new 3D cuboidal structuring element
	 */
	public final static CuboidStrel fromRadiusList(int radiusX, int radiusY, int radiusZ)
	{
		int diamX = 2 * radiusX + 1;
		int diamY = 2 * radiusY + 1;
		int diamZ = 2 * radiusZ + 1;
		return new CuboidStrel(diamX, diamY, diamZ, radiusX, radiusY, radiusZ);
	}
	
	/**
	 * Creates a 3D structuring element with a cuboid shape and different sizes
	 * depending on direction.
	 * 
	 * @param diamX
	 *            diameter in x direction
	 * @param diamY
	 *            diameter in y direction
	 * @param diamZ
	 *            diameter in z direction
	 * @return a new 3D cuboidal structuring element
	 */
	public final static CuboidStrel fromDiameterList(int diamX, int diamY, int diamZ)
	{
		int offsetX = (diamX - 1) / 2;
		int offsetY = (diamY - 1) / 2;
		int offsetZ = (diamZ - 1) / 2;
		return new CuboidStrel(diamX, diamY, diamZ, offsetX, offsetY, offsetZ);
	}
	
	// ==================================================
	// Class variables
	
	/**
	 * The size of the cuboid in the X direction 
	 */
	int sizeX;
	
	/**
	 * The size of the cuboid in the Y direction 
	 */
	int sizeY;
	
	/**
	 * The size of the cuboid in the Z direction 
	 */
	int sizeZ;
	
	/**
	 * The offset of the cuboid in the X direction 
	 */
	int offsetX;

	/**
	 * The offset of the cuboid in the Y direction 
	 */
	int offsetY;

	/**
	 * The offset of the cuboid in the Y direction 
	 */
	int offsetZ;

	
	// ==================================================
	// Constructors 
	
	/**
	 * Creates a new cubic structuring element of a given size.
	 * @param size the length of each side of the cube
	 */
	private CuboidStrel(int sizeX, int sizeY, int sizeZ)
	{
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		
		this.offsetX = (sizeX - 1) / 2;
		this.offsetY = (sizeY - 1) / 2;
		this.offsetZ = (sizeZ - 1) / 2;
	}
	
	/**
	 * Creates a new cubic structuring element of a given size.
	 * @param size the length of each side of the cube
	 */
	private CuboidStrel(int sizeX, int sizeY, int sizeZ, int offsetX, int offsetY, int offsetZ)
	{
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
	}
	
	
	// ==================================================
	// General methods 
	
	/**
	 * Returns a collection of three linear-shape structuring element, along
	 * each principal direction.
	 * 
	 * @return a collection of three in place structuring elements
	 * 
	 * @see ijt.morphology.SeparableStrel#separate()
	 */
	@Override
	public Collection<InPlaceStrel3D> decompose()
	{
		ArrayList<InPlaceStrel3D> strels = new ArrayList<InPlaceStrel3D>(3);
		strels.add(new LinearHorizontalStrel(this.sizeX, this.offsetX));
		strels.add(new LinearVerticalStrel(this.sizeY, this.offsetY));
		strels.add(new LinearDepthStrel3D(this.sizeZ, this.offsetZ));
		return strels;
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getMask()
	 */
	@Override
	public int[][][] getMask3D()
	{
		int[][][] mask = new int[this.sizeZ][this.sizeY][this.sizeX];
		for (int z = 0; z < this.sizeZ; z++)
		{
			for (int y = 0; y < this.sizeY; y++)
			{
				for (int x = 0; x < this.sizeX; x++)
				{
					mask[z][y][x] = 255;
				}
			}
		}
		return mask;
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getOffset()
	 */
	@Override
	public int[] getOffset()
	{
		return new int[]{this.offsetX, this.offsetY, this.offsetZ};
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel3D#getShifts3D()
	 */
	@Override
	public int[][] getShifts3D()
	{
		int n = this.sizeX * this.sizeY * this.sizeZ;
		int[][] shifts = new int[n][3];
		int i = 0;

		for (int z = 0; z < this.sizeZ; z++)
		{
			for (int y = 0; y < this.sizeY; y++)
			{
				for (int x = 0; x < this.sizeX; x++)
				{
					shifts[i][0] = x - this.offsetX;
					shifts[i][1] = y - this.offsetY;
					shifts[i][2] = z - this.offsetZ;
					i++;
				}
			}
		}
		return shifts;
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getSize()
	 */
	@Override
	public int[] getSize()
	{
		return new int[]{this.sizeX, this.sizeY, this.sizeY};
	}

	/**
	 * Returns a cuboidal structuring element with same size, but offset located
	 * symmetrically with respect to structuring element center.
	 * 
	 * @return the reversed version of this strel
	 */
	@Override
	public CuboidStrel reverse()
	{
		return new CuboidStrel(
				this.sizeX, this.sizeY, this.sizeZ, 
				this.sizeX - this.offsetX - 1, 
				this.sizeY - this.offsetY - 1, 
				this.sizeZ - this.offsetZ - 1);
	}

}
