/**
 * 
 */
package inra.ijpb.measure;

import ij.measure.Calibration;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region2d.BinaryConfigurations2D;

/**
 * Computation of intrinsic volumes (area, perimeter and Euler number) for
 * binary or label 2D images.
 *
 * This class provides only static methods.
 * 
 * @see IntrinsicVolumes3D
 * 
 * @author dlegland
 *
 */
public class IntrinsicVolumes2D
{
    // ==================================================
    // Static methods

	/**
	 * Measures the area of the foreground region within a binary image.
	 * 
	 * @see #perimeter(ImageProcessor, Calibration, int)
	 * @see #eulerNumber(ImageProcessor, int)
	 *  
	 * @see inra.ijpb.binary.BinaryImages#countForegroundPixels(ImageProcessor) 
	 * 
	 * @param image
	 *            the input binary image containing the region to analyze
	 * @param calib
	 *            the spatial calibration of the image
	 * @return the area of each region
	 */
	public static final double area(ImageProcessor image, Calibration calib)
	{
		// area of individual pixel
		double pixelArea = calib.pixelWidth * calib.pixelHeight;

		// First count the number of pixels in each region
		int count = BinaryImages.countForegroundPixels(image);
		double area = count * pixelArea;
		return area;
	}


	/**
	 * Measures the area of each region within a label image, taking into
	 * account image resolution.
	 * 
	 * @see inra.ijpb.label.LabelImages#pixelCount(ImageProcessor, int[]) 
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

		// convert pixel counts to areas
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
	 * @param image
	 *            the input binary image
	 * @return the area density of the binary structure
	 */
	public static final double areaDensity(ImageProcessor image)
	{
		// image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		double totalArea = sizeX * sizeY;
		
		// count the number of pixels with value greater than 0
		double count = BinaryImages.countForegroundPixels(image);

		// normalization by image size
		return count / totalArea;
	}


	/**
	 * Measures the perimeter of the foreground region within a binary image.
	 * 
	 * @see #area(ImageProcessor, Calibration)
	 * @see #eulerNumber(ImageProcessor, int)
	 * 
	 * @param image
	 *            the input binary image containing the region to analyze
	 * @param calib
	 *            the spatial calibration of the image
	 * @param nDirs
	 *            the number of directions to consider, either 2 or 4
	 * @return the perimeter of the binary region
	 */
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
	
	/**
	 * Measures the perimeter of each region within a label image.
	 * 
	 * @see #areas(ImageProcessor, int[], Calibration)
	 * @see #eulerNumbers(ImageProcessor, int[], int)
	 * 
	 * @param image
	 *            the input image containing the labels of regions to analyze
	 * @param labels
	 *            the labels of the regions within the image
	 * @param calib
	 *            the spatial calibration of the image
	 * @param nDirs
	 *            the number of directions to consider, either 2 or 4
	 * @return the perimeter of each region within the image
	 */
	public static final double[] perimeters(ImageProcessor image, int[] labels, Calibration calib, int nDirs)
	{
		// pre-compute LUT corresponding to resolution and number of directions
		double[] lut = perimeterLut(calib, nDirs);

		// histogram of configurations for each label
		int[][] histos = BinaryConfigurations2D.histograms(image, labels);
		
		// apply Binary configuration look-up table
		return BinaryConfigurations2D.applyLut(histos, lut);
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
		double[] lut = perimeterLut(calib, nDirs);

		// histogram of configurations for each label
		int[] histo = BinaryConfigurations2D.innerHistogram(image);
		
		// apply Binary configuration look-up table
		double perim = BinaryConfigurations2D.applyLut(histo, lut);
		return perim / samplingArea(image, calib);
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
	private static final double[] perimeterLut(Calibration calib, int nDirs)
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
	
	/**
	 * Measures the Euler number of the foreground region within a binary image.
	 * 
	 * The Euler number is equal to the number of connected components minus the
	 * number of holes within the structure. The result depends on the
	 * connectivity used for computation.
	 * 
	 * @see #area(ImageProcessor, Calibration)
	 * @see #perimeter(ImageProcessor, Calibration, int)
	 * 
	 * @param image
	 *            the input binary image containing the region to analyze
	 * @param conn
	 *            the connectivity to use, either 4 or 8
	 * @return the Euler number of the binary region
	 */
	public static final int eulerNumber(ImageProcessor image, int conn)
	{
		switch(conn)
		{
		case 4:
			return eulerNumberC4(image);
		case 8:
			return eulerNumberC8(image);
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
		double[] lut = eulerNumberLut(conn);

		// histogram of configurations for each label
		int[][] histos = BinaryConfigurations2D.histograms(image, labels);
		
		// apply Binary configuration look-up table
		return BinaryConfigurations2D.applyLut(histos, lut);
	}

	public static final double eulerNumberDensity(ImageProcessor image, Calibration calib, int conn)
	{
		// create associative array to know index of each label
		double[] lut = eulerNumberLut(conn);

		// histogram of configurations for each label
		int[] histo = BinaryConfigurations2D.innerHistogram(image);
		
		// apply Binary configuration look-up table
		double euler = BinaryConfigurations2D.applyLut(histo, lut);
		return euler / samplingArea(image, calib);
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
	private static final double[] eulerNumberLut(int conn)
	{
		switch(conn)
		{
		case 4:
			return eulerNumberLutC4();
		case 8:
			return eulerNumberLutC8();
		default:
			throw new IllegalArgumentException("Connectivity must be 4 or 8, not " + conn);
		}
	}
	
	private final static double[] eulerNumberLutC4()
	{
		return new double[] {
				0,  0.25, 0.25, 0,    0.25, 0, 0.5, -0.25,  
				0.25, 0.5, 0, -0.25,   0, -0.25, -0.25, 0};
	}

	private final static double[] eulerNumberLutC8()
	{
		return new double[] {
				0,  0.25, 0.25, 0,    0.25, 0, -0.5, -0.25,  
				0.25, -0.5, 0, -0.25,   0, -0.25, -0.25, 0};
	}
	
	/**
	 * Computes the area of the sampling window, for estimation of densities.
	 * 
	 * @param image
	 *            the image to analyze
	 * @param calib
	 *            the spatial calibration
	 * @return the calibrated area of the sampling window
	 */
	private static final double samplingArea(ImageProcessor image, Calibration calib)
	{
        // size of image
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
		return (sizeX - 1) * calib.pixelWidth * (sizeY - 1) * calib.pixelHeight;
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
