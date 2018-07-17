/**
 * 
 */
package inra.ijpb.measure;

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
	 * @param resol
	 *            the size of a pixel in each direction
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
					"Requires number of directions being 2 or 4.");
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
			throw new IllegalArgumentException("Only Connectivity 4 is implemented...");
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
            	
            	// triangle faces
            	if (b00 && b01 && b10) nTriangles++;
            	if (b00 && b01 && b11) nTriangles++;
            	if (b00 && b10 && b11) nTriangles++;
            	if (b01 && b10 && b11) nTriangles++;
            	
            	// square faces
            	if (b00 && b01 && b10 && b11)
            	{
//            		nTriangles -= 4;
            		nSquares++;
            	}
            	
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
	
   
	// ==================================================
    // Constructor

    /**
     * Private constructor to prevent instantiation
     */
    private IntrinsicVolumes2D()
    {
    }
}
