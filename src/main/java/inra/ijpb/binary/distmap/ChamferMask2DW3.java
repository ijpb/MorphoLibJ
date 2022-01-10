/**
 * 
 */
package inra.ijpb.binary.distmap;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of Chamfer Weights that manages three types of offsets types:
 * orthogonal, diagonal neighbors and "chess-knight move" neighbors.
 * 
 * By default, computation is performed using integers.
 * 
 * @author dlegland
 */
public class ChamferMask2DW3 extends ChamferMask2D
{
	/** The weight for orthogonal neighbors.*/
	short a;
	/** The weight for diagonal neighbors.*/
	short b;
	/** The weight for chess-knight move neighbors.*/
	short c;
	
	/**
	 * Creates a new ChamferWeights2DW3 object by specifying the weights
	 * associated to orthogonal and diagonal neighbors.
	 * 
	 * @param a
	 *            the weight associated to orthogonal neighbors
	 * @param b
	 *            the weight associated to diagonal neighbors
	 * @param c
	 *            the weight associated to chess-knight move neighbors
	 */
	public ChamferMask2DW3(int a, int b, int c)
	{
		this.a = (short) a;
		this.b = (short) b;
		this.c = (short) c;
	}

	/**
	 * Creates a new ChamferMask3D object by specifying the weights
	 * associated to orthogonal and diagonal neighbors.
	 * 
	 * @param weights the weights associated to the different types of offset
	 */	
	public ChamferMask2DW3(short[] weights)
	{
		if (weights.length != 3)
		{
			throw new RuntimeException("Number of weights must be 3, not " + weights.length);
		}
		this.a = weights[0];
		this.b = weights[1];
		this.c = weights[2];
	}

	@Override
	public Collection<ShortOffset> getForwardOffsets()
	{
		// create array of forward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();
	
		// offsets in the (y-2)-line
		offsets.add(new ShortOffset(-1, -2, c));
		offsets.add(new ShortOffset(+1, -2, c));
		
		// offsets in the (y-1)-line
		offsets.add(new ShortOffset(-2, -1, c));
		offsets.add(new ShortOffset(-1, -1, b));
		offsets.add(new ShortOffset( 0, -1, a));
		offsets.add(new ShortOffset(+1, -1, b));
		offsets.add(new ShortOffset(+2, -1, c));
		
		// offsets in the current line
		offsets.add(new ShortOffset(-1,  0, a));
	
		return offsets;
	}

	@Override
	public Collection<ShortOffset> getBackwardOffsets()
	{
		// create array of backward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();

		// offsets in the (y+2)-line
		offsets.add(new ShortOffset(-1, +2, c));
		offsets.add(new ShortOffset(+1, +2, c));
		
		// offsets in the (y+1)-line
		offsets.add(new ShortOffset(-2, +1, c));
		offsets.add(new ShortOffset(-1, +1, b));
		offsets.add(new ShortOffset( 0, +1, a));
		offsets.add(new ShortOffset(+1, +1, b));
		offsets.add(new ShortOffset(+2, +1, c));
		
		// offsets in the current line
		offsets.add(new ShortOffset(+1,  0, a));
		

		return offsets;
	}

	@Override
	public short getShortNormalizationWeight()
	{
		return a;
	}
}
