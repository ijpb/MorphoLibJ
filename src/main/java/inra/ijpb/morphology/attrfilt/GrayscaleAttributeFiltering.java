/**
 * 
 */
package inra.ijpb.morphology.attrfilt;

import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.algo.AlgoListener;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.morphology.Connectivity2D;

/**
 * Filter regions within a grayscale image based on an attribute (area, diagonal
 * of bounding box...) and a minimum value.
 * 
 * @see AttributeFilterType
 * @see Attribute2D
 * 
 * @author dlegland
 *
 */
public class GrayscaleAttributeFiltering extends AlgoStub implements AlgoListener
{
	// ==============================================================
	// Class variables
	
	AttributeFilterType filterType = AttributeFilterType.OPENING;
	Attribute2D attribute = Attribute2D.AREA; 
	int minimumValue = 100;
	
	/** The value of connectivity, either 4 or 8 */
	Connectivity2D connectivity = Connectivity2D.C4;
	
	
	// ==============================================================
	// Constructors
	
	/**
	 * Constructor with default values.
	 */
	public GrayscaleAttributeFiltering()
	{
	}

	/**
	 * Constructor with specified values
	 */
	public GrayscaleAttributeFiltering(AttributeFilterType type, Attribute2D attribute, int minValue, Connectivity2D connectivity)
	{
		this.filterType = type;
		this.attribute = attribute;
		this.minimumValue = minValue;
		this.connectivity = connectivity;
	}

	
	// ==============================================================
	// Getter and Setter
	
	public AttributeFilterType getFilterType()
	{
		return filterType;
	}

	public void setFilterType(AttributeFilterType filterType)
	{
		this.filterType = filterType;
	}

	public Attribute2D getAttribute()
	{
		return attribute;
	}

	public void setAttribute(Attribute2D attribute)
	{
		this.attribute = attribute;
	}

	public int getMinimumValue()
	{
		return minimumValue;
	}

	public void setMinimumValue(int minimumValue)
	{
		this.minimumValue = minimumValue;
	}

	public Connectivity2D getConnectivity()
	{
		return connectivity;
	}

	public void setConnectivity(Connectivity2D connectivity)
	{
		this.connectivity = connectivity;
	}

	
	// ==============================================================
	// Process methods
	
	public ImageProcessor process(ImageProcessor image)
	{
		ImageProcessor result;
		
		// Identify image to process (original, or inverted)
		ImageProcessor image2 = image;
		if (this.filterType == AttributeFilterType.CLOSING || this.filterType == AttributeFilterType.BOTTOM_HAT)
		{
			image2 = image2.duplicate();
			image2.invert();
		}
		
		// switch depending on attribute to use
		if (attribute == Attribute2D.AREA)
		{
			AreaOpeningQueue algo = new AreaOpeningQueue();
			algo.setConnectivity(this.connectivity.getValue());
			algo.addAlgoListener(this);
			result = algo.process(image2, this.minimumValue);
		}
		else
		{
			BoxDiagonalOpeningQueue algo = new BoxDiagonalOpeningQueue();
			algo.setConnectivity(this.connectivity.getValue());
			algo.addAlgoListener(this);
			result = algo.process(image2, this.minimumValue);
		}
		
		// For top-hat and bottom-hat, we consider difference with original image
		if (this.filterType == AttributeFilterType.TOP_HAT || this.filterType == AttributeFilterType.BOTTOM_HAT)
		{
			double maxDiff = 0;
			for (int i = 0; i < image.getPixelCount(); i++)
			{
				float diff = Math.abs(result.getf(i) - image2.getf(i));
				result.setf(i, diff);
				maxDiff = Math.max(diff, maxDiff);
			}
			
			result.setMinAndMax(0, maxDiff);
		}
		
		// For closing, invert back the result
		else if (this.filterType == AttributeFilterType.CLOSING)
		{
			result.invert();
		}
		
		return result;
	}

	@Override
	public void algoProgressChanged(AlgoEvent evt)
	{
		this.fireProgressChanged(evt);
	}

	@Override
	public void algoStatusChanged(AlgoEvent evt)
	{
		this.fireStatusChanged(evt);
	}
}
