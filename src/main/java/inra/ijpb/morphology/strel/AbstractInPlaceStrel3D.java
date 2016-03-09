/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.ImageStack;

/**
 * Implementation stub for in place 3D Structuring elements. Implements
 * operations methods by calling in-place versions.
 * 
 * @author David Legland
 *
 */
public abstract class AbstractInPlaceStrel3D extends AbstractStrel3D implements
		InPlaceStrel3D
{

	public ImageStack dilation(ImageStack stack)
	{
		ImageStack result = stack.duplicate();
		this.inPlaceDilation(result);
		return result;
	}

	public ImageStack erosion(ImageStack stack)
	{
		ImageStack result = stack.duplicate();
		this.inPlaceErosion(result);
		return result;
	}

	public ImageStack closing(ImageStack stack)
	{
		ImageStack result = stack.duplicate();
		this.inPlaceDilation(result);
		this.reverse().inPlaceErosion(result);
		return result;
	}

	public ImageStack opening(ImageStack stack)
	{
		ImageStack result = stack.duplicate();
		this.inPlaceErosion(result);
		this.reverse().inPlaceDilation(result);
		return result;
	}
}
