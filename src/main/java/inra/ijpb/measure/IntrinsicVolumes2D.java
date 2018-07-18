/**
 * 
 */
package inra.ijpb.measure;

import java.util.ArrayList;
import java.util.HashMap;

import ij.measure.Calibration;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;

/**
 * Computation of intrinsic volumes (area, perimeter and Euler number) for
 * binary or label 2D images.
 *
 * This class provides only static methods. 
 * 
 * @author dlegland
 *
 */
public class IntrinsicVolumes2D
{
    // ==================================================
    // Static methods

	/**
	 * Computes the area for each particle in the label image, taking into
	 * account image resolution.
	 * 
	 * @see inra.ijpb.label.LabelImages#pixelCount(ij.process.ImageProcessor,
	 *      int[])
	 * 
	 * @see inra.ijpb.measure.region2d.Area 
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of unique labels in image
	 * @param calib
	 *            the spatial calibration of the image
	 * @return the area of each region
	 */
	public static final double[] areas(ImageProcessor image, int[] labels, Calibration calib)
	{
		// area of individual pixel
		double pixelArea = calib.pixelWidth * calib.pixelHeight;

		// initialize result
		int nLabels = labels.length;
		double[] areas = new double[nLabels];

		// First count the number of pixels in each region
		int[] counts = LabelImages.pixelCount(image, labels);

		// convert pixel count to areas
		for (int i = 0; i < areas.length; i++) 
		{
			areas[i] = counts[i] * pixelArea;
		}

		return areas;
	}

	/**
	 * Computes the area density for each particle in the label image, taking into
	 * account image resolution.
	 * 
	 * @see inra.ijpb.measure.region2d.Area 
	 * 
	 * @param image
	 *            the input binary image
	 * @return the area density of the binary structure
	 */
	public static final double areaDensity(ImageProcessor image)
	{
		// image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		// count the number of pixels with value greater than 0
		int count = 0;
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				if (image.getf(x, y) > 0) count++;
			}
		}

		// normalization by image size
		double totalArea = sizeX * sizeY;
		return count / totalArea;
	}


	public static final double perimeter(ImageProcessor image, Calibration calib, int nDirs)
	{
		switch (nDirs)
		{
		case 2:
			return perimeterD2(image, calib);
		case 4:
			return perimeterD4(image, calib);
		default:
			throw new IllegalArgumentException(
					"Requires number of directions being 2 or 4, not " + nDirs);
		}
	}
	
	private static final double perimeterD2(ImageProcessor image, Calibration calib)
	{
        // distances between a pixel and its neighbors.
        // di refer to orthogonal neighbors
        // dij refer to diagonal neighbors
        double d1 = calib.pixelWidth;
        double d2 = calib.pixelHeight;
        
        // size of image
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
		
        // number of transitions in horizontal and vertical directions
		int n1 = 0, n2 = 0;
		
        // initialize configuration values to background
        boolean b01 = false;
        boolean b10 = false;
        boolean b11 = false;

		// Iterate over all 2-by-2 configurations containing at least one pixel
		// within the image.
        // Current pixel is the lower-right pixel in configuration
		// (corresponding to b11).
        for (int y = 0; y < sizeY + 1; y++)
        {
        	b10 = false;
        	
        	for (int x = 0; x < sizeX + 1; x++)
        	{
        		// update configuration pixel values
				b01 = x < sizeX & y > 0 ? image.get(x, y - 1) > 0 : false;
				b11 = x < sizeX & y < sizeY ? image.get(x, y) > 0 : false;
				
				// Count horizontal and vertical transitions within current config
				if (b10 ^ b11) n1++;
				if (b01 ^ b11) n2++;

				// update for next iteration
				b10 = b11;
        	}
        }
        
        // divides by 4:
        // * factor 1/2 to convert between intersection counts and diameters, 
        // * factor 1/2 to compute average between two directions
        double perim = (n1 * d2 + n2 * d1) * Math.PI / 4.0;
        return perim;
	}
	
	private static final double perimeterD4(ImageProcessor image, Calibration calib)
	{
        // distances between a pixel and its neighbors.
        // di refer to orthogonal neighbors
        // dij refer to diagonal neighbors
        double d1 = calib.pixelWidth;
        double d2 = calib.pixelHeight;
        double d12 = Math.hypot(d1, d2);
        
        // area of pixel, used to compute line densities
        double area = d1 * d2;
        
        // size of image
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
		
        // number of transitions in horizontal and vertical directions
		// contributions for isothetic directions
		int n1 = 0, n2 = 0;
		// number of transitions in diagonal directions
		int n12 = 0;
		
        // initialize configuration values to background
        boolean b00 = false;
        boolean b01 = false;
        boolean b10 = false;
        boolean b11 = false;

		// Iterate over all 2-by-2 configurations containing at least one pixel
		// within the image.
        // Current pixel is the lower-right pixel in configuration
		// (corresponding to b11).
        for (int y = 0; y < sizeY + 1; y++)
        {
        	b00 = false;
        	b10 = false;
        	
        	for (int x = 0; x < sizeX + 1; x++)
        	{
        		// update configuration pixel values
				b01 = x < sizeX & y > 0 ? image.get(x, y - 1) > 0 : false;
				b11 = x < sizeX & y < sizeY ? image.get(x, y) > 0 : false;
				
				// Count horizontal and vertical transitions within current config
				if (b10 ^ b11) n1++;
				if (b01 ^ b11) n2++;
				// increment number of diagonal transitions 
				if (b00 ^ b11) n12++;
				if (b01 ^ b10) n12++;

				// update for next iteration
				b00 = b01;
				b10 = b11;
        	}
        }
        
        // compute projected diameters
        // (coefficient 0.5 is for conversion between intersection number to component number)
        double diam1  =  n1 * 0.5 * area /  d1;
        double diam2  =  n2 * 0.5 * area /  d2;
        double diam12 = n12 * 0.5 * area / d12;
        
        // compute weighted average over directions
        double[] weights = computeDirectionWeightsD4(d1, d2);
        double perim = (diam1 * weights[0] + diam2 * weights[1] + diam12 * weights[2]) * Math.PI;
        
        return perim;
	}
	
	public static final double[] perimeters(ImageProcessor image, int[] labels, Calibration calib, int nDirs)
	{
		// pre-compute LUT corresponding to resolution and number of directions
		double[] lut = computePerimeterLut(calib, nDirs);

		// apply Binary configuration look-up table
		return applyConfigurationLut(image, labels, lut);
	}

	/**
	 * Computes perimeter density of binary image.
	 * 
	 * @param image
	 *            the input binary image
	 * @param calib
	 *            the spatial calibration of the image
	 * @param nDirs
	 *            the number of directions to consider, either 2 or 4
	 * @return the perimeter density of the binary image
	 */
	public static final double perimeterDensity(ImageProcessor image,
			Calibration calib, int nDirs)
	{
		// create associative array to know index of each label
		double[] lut = computePerimeterLut(calib, 2);

		// initialize result
		double perimeter = 0;

		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		// iterate on image pixel configurations
		for (int y = 0; y < sizeY - 1; y++) 
		{
			for (int x = 0; x < sizeX - 1; x++) 
			{
				// Compute index of local binary configuration
				int index = 0;
				index += image.getf(    x,     y) > 0 ? 1 : 0;
				index += image.getf(x + 1,     y) > 0 ? 2 : 0;
				index += image.getf(    x, y + 1) > 0 ? 4 : 0;
				index += image.getf(x + 1, y + 1) > 0 ? 8 : 0;

				// update measure for current label
				perimeter += lut[index];
			}
		}

		double samplingArea = (sizeX - 1) * calib.pixelWidth * (sizeY - 1) * calib.pixelHeight;
		return perimeter / samplingArea;
	}

	/**
	 * Computes the Look-up table that is used to compute perimeter. The result
	 * is an array with 16 entries, each entry corresponding to a binary 2-by-2
	 * configuration of pixels.
	 * 
	 * @param calib
	 *            the calibration of the image
	 * @param nDirs
	 *            the number of directions to use (2 or 4)
	 * @return an array containing for each 2-by-2 configuration index, the
	 *         corresponding contribution to perimeter estimate
	 */
	private static final double[] computePerimeterLut(Calibration calib, int nDirs)
	{
		// distances between a pixel and its neighbors.
		// di refer to orthogonal neighbors
		// dij refer to diagonal neighbors
		double d1 = calib.pixelWidth;
		double d2 = calib.pixelHeight;
		double d12 = Math.hypot(d1, d2);
		double area = d1 * d2;

		// weights associated to each direction, computed only for four
		// directions
		double[] weights = null;
		if (nDirs == 4)
		{
			weights = computeDirectionWeightsD4(d1, d2);
		}

		// initialize output array (2^(2*2) = 16 configurations in 2D)
		final int nConfigs = 16;
		double[] tab = new double[nConfigs];

		// loop for each tile configuration
		for (int i = 0; i < nConfigs; i++)
		{
			// create the binary image representing the 2x2 tile
			boolean[][] im = new boolean[2][2];
			im[0][0] = (i & 1) > 0;
			im[0][1] = (i & 2) > 0;
			im[1][0] = (i & 4) > 0;
			im[1][1] = (i & 8) > 0;

			// contributions for isothetic directions
			double ke1, ke2;

			// contributions for diagonal directions
			double ke12;

			// iterate over the 4 pixels within the configuration
			for (int y = 0; y < 2; y++)
			{
				for (int x = 0; x < 2; x++)
				{
					if (!im[y][x])
						continue;

					// divides by two to convert intersection count to projected
					// diameter
					ke1 = im[y][1 - x] ? 0 : (area / d1) / 2;
					ke2 = im[1 - y][x] ? 0 : (area / d2) / 2;

					if (nDirs == 2) 
					{
						// Count only orthogonal directions
						// divides by two for average, and by two for
						// multiplicity
						tab[i] += (ke1 + ke2) / 4;

					} 
					else if (nDirs == 4) 
					{
						// compute contribution of diagonal directions
						ke12 = im[1 - y][1 - x] ? 0 : (area / d12) / 2;

						// Decomposition of Crofton formula on 4 directions,
						// taking into account multiplicities
						tab[i] += ((ke1 / 2) * weights[0] + (ke2 / 2)
								* weights[1] + ke12 * weights[2]);
					}
				}
			}

			// Add a normalisation constant
			tab[i] *= Math.PI;
		}

		return tab;
	}

	/**
	 * Computes a set of weights for the four main directions (orthogonal plus
	 * diagonal) in discrete image. The sum of the weights equals 1.
	 * 
	 * @param dx
	 *            the spatial calibration in the x direction
	 * @param dy
	 *            the spatial calibration in the y direction
	 * @return the set of normalized weights
	 */
	private static final double[] computeDirectionWeightsD4(double dx, double dy) 
	{
		// angle of the diagonal
		double theta = Math.atan2(dy, dx);

		// angular sector for direction 1 ([1 0])
		double alpha1 = theta / Math.PI;

		// angular sector for direction 2 ([0 1])
		double alpha2 = (Math.PI / 2.0 - theta) / Math.PI;

		// angular sector for directions 3 and 4 ([1 1] and [-1 1])
		double alpha34 = .25;

		// concatenate the different weights
		return new double[] { alpha1, alpha2, alpha34, alpha34 };
	}
	
	public static final int eulerNumber(ImageProcessor image, int conn)
	{
		switch(conn)
		{
		case 4: return eulerNumberC4(image);
		case 8: return eulerNumberC8(image);
		default:
			throw new IllegalArgumentException("Connectivity must be 4 or 8, not " + conn);
		}
	}
	
	private static final int eulerNumberC4(ImageProcessor image)
	{
        // size of image
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
		
        // number of vertices, edges, and faces
        int nVertices = 0;
        int nEdges = 0;
        int nSquares = 0;
        
        // Iterate over rows
        for (int y = 0; y < sizeY - 1; y++) 
        {
        	// initialize left values of 2-by-2 configuration to background
        	boolean b00 = false;
        	boolean b10 = false;
        	
        	// iterate over 2-by-2 configurations of current row
            for (int x = 0; x < sizeX; x++) 
            {
            	boolean b01 = image.get(x, y) > 0; 
            	boolean b11 = image.get(x, y + 1) > 0;
            	
            	// vertices
            	if (b01) nVertices++;
            	
            	// horizontal top edge
            	if (b00 && b01) nEdges++;
            	// vertical right edge
            	if (b01 && b11) nEdges++;
            	
            	// square faces
            	if (b00 && b01 && b10 && b11) nSquares++;
            	
            	// update left side of 2-by-2 configuration for next iteration
                b00 = b01;
                b10 = b11;
            }
        }
        
    	// iterate over pixels of last row
        boolean b00 = false; 
        for (int x = 0; x < sizeX; x++) 
        {
        	boolean b01 = image.get(x, sizeY - 1) > 0; 
        	if (b01) nVertices++;
        	if (b00 && b01) nEdges++;
        	b00 = b01;
        }
        
        // compute Euler number using graph formula
        int euler = nVertices - nEdges + nSquares;
        return euler;
	}
	
   
	private static final int eulerNumberC8(ImageProcessor image)
	{
        // size of image
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
		
        // number of vertices, edges, and faces
        int nVertices = 0;
        int nEdges = 0;
        int nTriangles = 0;
        int nSquares = 0;
        
        // Iterate over rows
        for (int y = 0; y < sizeY - 1; y++) 
        {
        	// initialize left values of 2-by-2 configuration to background
        	boolean b00 = false;
        	boolean b10 = false;
        	
        	// iterate over 2-by-2 configurations of current row
            for (int x = 0; x < sizeX; x++) 
            {
            	boolean b01 = image.get(x, y) > 0; 
            	boolean b11 = image.get(x, y + 1) > 0;
            	
            	// vertices
            	if (b01) nVertices++;
            	
            	// horizontal top edge
            	if (b00 && b01) nEdges++;
            	// vertical right edge
            	if (b01 && b11) nEdges++;
            	// the two diagonal edges
            	if (b00 && b11) nEdges++;
            	if (b01 && b10) nEdges++;
            	
            	// the four triangle faces
            	if (b00 && b01 && b10) nTriangles++;
            	if (b00 && b01 && b11) nTriangles++;
            	if (b00 && b10 && b11) nTriangles++;
            	if (b01 && b10 && b11) nTriangles++;
            	
            	// square faces
            	if (b00 && b01 && b10 && b11) nSquares++;
            	
            	// update left side of 2-by-2 configuration for next iteration
                b00 = b01;
                b10 = b11;
            }
        }
        
    	// iterate over pixels of last row
        boolean b00 = false; 
        for (int x = 0; x < sizeX; x++) 
        {
        	boolean b01 = image.get(x, sizeY - 1) > 0; 
        	if (b01) nVertices++;
        	if (b00 && b01) nEdges++;
        	b00 = b01;
        }
        
        // compute Euler number using graph formula
        int euler = nVertices - nEdges + nTriangles - nSquares;
        return euler;
	}
	
	public static final double[] eulerNumbers(ImageProcessor image, int[] labels, int conn)
	{
		// pre-compute LUT corresponding to connectivity
		double[] lut = computeEulerNumberLut(conn);

		// apply Binary configuration look-up table
		return applyConfigurationLut(image, labels, lut);
	}

	public static final double eulerNumberDensity(ImageProcessor image, Calibration calib, int conn)
	{
		// create associative array to know index of each label
		double[] lut = computeEulerNumberLut(conn);

		// initialize result
		double euler = 0;

		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		// iterate on image pixel configurations
		for (int y = 0; y < sizeY - 1; y++) 
		{
			for (int x = 0; x < sizeX - 1; x++) 
			{
				// Compute index of local binary configuration
				int index = 0;
				index += image.getf(    x,     y) > 0 ? 1 : 0;
				index += image.getf(x + 1,     y) > 0 ? 2 : 0;
				index += image.getf(    x, y + 1) > 0 ? 4 : 0;
				index += image.getf(x + 1, y + 1) > 0 ? 8 : 0;

				// update measure for current label
				euler += lut[index];
			}
		}

		double samplingArea = (sizeX - 1) * calib.pixelWidth * (sizeY - 1) * calib.pixelHeight;
		return euler / samplingArea;
	}

	/**
	 * Computes the Look-up table that is used to compute Euler number density.
	 * The result is an array with 16 entries, each entry corresponding to a
	 * binary 2-by-2 configuration of pixels.
	 * 
	 * @param conn
	 *            the connectivity to use (4 or 8)
	 * @return an array containing for each 2-by-2 configuration index, the
	 *         corresponding contribution to euler number estimate
	 */
	private static final double[] computeEulerNumberLut(int conn)
	{
		switch(conn)
		{
		case 4: return eulerNumberDensityC4();
		case 8: return eulerNumberDensityC8();
		default:
			throw new IllegalArgumentException("Connectivity must be 4 or 8, not " + conn);
		}
	}
	
	private final static double[] eulerNumberDensityC4()
	{
		return new double[] {
				0,  0.25, 0.25, 0,    0.25, 0, 0.5, -0.25,  
				0.25, 0.5, 0, -0.25,   0, -0.25, -0.25, 0};
	}

	private final static double[] eulerNumberDensityC8()
	{
		return new double[] {
				0,  0.25, 0.25, 0,    0.25, 0, 0.0, -0.25,  
				0.25, 0.0, 0, -0.25,   0, -0.25, -0.25, 0};
	}
	
	
	private static final double[] applyConfigurationLut(ImageProcessor image, int[] labels, double[] lut)
	{
		// create associative array to know index of each label
		int nLabels = labels.length;
		HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

		// initialize result
		double[] results = new double[nLabels];

		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		// for each configuration of 2x2 pixels, we identify the labels
		ArrayList<Integer> localLabels = new ArrayList<Integer>(4);

		// values of pixels within current 2-by-2 configuration
		// (first digit for y, second digit for x)
		int[] configValues = new int[4];
		
		// Iterate over all 2-by-2 configurations containing at least one pixel
		// within the image.
        // Current pixel is the lower-right pixel in configuration
		// (corresponding to b11).
		for (int y = 0; y < sizeY + 1; y++) 
		{
			configValues[0] = 0;
			configValues[2] = 0;
			
			for (int x = 0; x < sizeX + 1; x++) 
			{
        		// update pixel values of configuration
				configValues[1] = x < sizeX & y > 0 ? (int) image.getf(x, y - 1) : 0;
				configValues[3] = x < sizeX & y < sizeY ? (int) image.getf(x, y) : 0;

				// identify labels in current config
				localLabels.clear();
				for (int label : configValues)
				{
					if (label == 0)
						continue;
					// keep only one instance of each label
					if (!localLabels.contains(label))
						localLabels.add(label);
				}

				// if no label in local configuration contribution is zero
				if (localLabels.size() == 0)
				{
					continue;
				}

				// For each label, compute binary confi
				for (int label : localLabels) 
				{
					// Compute index of local configuration
					int index = 0;
					index += configValues[0] == label ? 1 : 0;
					index += configValues[1] == label ? 2 : 0;
					index += configValues[2] == label ? 4 : 0;
					index += configValues[3] == label ? 8 : 0;

					// retrieve label index from label value
					int labelIndex = labelIndices.get(label);

					// update measure for current label
					results[labelIndex] += lut[index];
				}

				// update values of configuration for next iteration
				configValues[0] = configValues[1];
				configValues[2] = configValues[3];
			}
		}

		return results;
	}


	// ==================================================
    // Constructor

    /**
     * Private constructor to prevent instantiation
     */
    private IntrinsicVolumes2D()
    {
    }
}
