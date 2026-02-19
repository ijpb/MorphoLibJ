/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
/**
 * 
 */
package inra.ijpb.label.distmap;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;

/**
 * Computes Euclidean distance transform of a 2D binary array using algorithm of
 * Saito and Toriwaki (1994). Uses floating point computations.
 * 
 * Details:
 * <ul>
 * <li>works for 2D</li>
 * <li>uses floating point computations</li>
 * <li>can manage spatial calibration</li>
 * </ul>
 */
public class SaitoToriwakiDistanceTransform2DFloat extends AlgoStub implements DistanceTransform2D
{
	boolean normalize;
	
	/**
	 * Default empty constructor for (normalized) Euclidean distance transform
	 * algorithm.
	 */
	public SaitoToriwakiDistanceTransform2DFloat()
	{
		this(true);
	}
	
	/**
	 * Consructor that specifies whether the resulting distance map should be
	 * normalized (corresponding to Euclidean distance), or not (corresponding
	 * to the square of the Euclidean distance).
	 * 
	 * @param normalize
	 *            boolean flag for normalizing the result to Euclidean distance.
	 *            If not, returns the squared distance transform.
	 */
	public SaitoToriwakiDistanceTransform2DFloat(boolean normalize)
	{
		this.normalize = normalize;
	}
	
	@Override
	public FloatProcessor distanceMap(ImageProcessor array)
	{
		return distanceMap(array, new double[] {1.0, 1.0});
	}
	
    /**
     * Computes the distance map of the specified binary array and the specified
     * array of spacings between array elements.
     * 
     * @param array
     *            the binary array to process
     * @param spacings
     *            the spacings between array elements (with as many elements as
     *            array dimensionality)
     * @return the distance map
     */
	public FloatProcessor distanceMap(ImageProcessor array, double[] spacings)
	{
        if (spacings.length != 2)
        {
            throw new IllegalArgumentException("Spacing array must have length 2");
        }
        
        this.fireStatusChanged(this, "Allocate memory");
        FloatProcessor output = new FloatProcessor(array.getWidth(), array.getHeight());
        
        // process along each dimension
        processStep1(array, output, spacings[0]);
        processStep2(array, output, spacings[1]);

        // convert squared distance to distance
        if (normalize)
        {
        	normalize(output);
        }
        
        return output;
    }
    
    /**
     * Computes the squared distance map of the specified binary array and the
     * specified array of spacings between array elements.
     * 
     * @param array
     *            the binary array to process
     * @param output
     *            the array of scalar values used to store the computation result
     * @param spacings
     *            the spacings between array elements (with as many elements as
     *            array dimensionality)
     * @return the squared distance map
     */
    public double distanceMapSquared2d(ImageProcessor array, FloatProcessor output,	double[] spacings)
    {
        if (spacings.length != 2)
        {
            throw new IllegalArgumentException("Spacing array must have length 2");
        }
        
        processStep1(array, output, spacings[0]);
        return processStep2(array, output, spacings[1]);
    }
    
    private void processStep1(ImageProcessor array, FloatProcessor output, double spacingX)
    {
        // retrieve size of array
        int sizeX = array.getWidth();
        int sizeY = array.getHeight();
        double absoluteMaximum = (sizeX + sizeY) * spacingX;
        
        // forward scan
        this.fireStatusChanged(this, "Process X-direction, forward pass");
        for (int y = 0; y < sizeY; y++)
        {
            this.fireProgressChanged(this, y, sizeY);
            double df = absoluteMaximum;
            for (int x = 0; x < sizeX; x++)
            {
                // either increment or reset current distance
                df = ((int) array.getf(x, y)) > 0 ? df + spacingX : 0;
                output.setf(x, y, (float) (df * df));
            }
        }
        
        // backward scan
        this.fireStatusChanged(this, "Process X-direction, backward pass");
        for (int y = sizeY - 1; y >= 0; y--)
        {
            this.fireProgressChanged(this, sizeY-1-y, sizeY);
            double db = absoluteMaximum;
            for (int x = sizeX - 1; x >= 0; x--)
            {
                // either increment or reset current distance
                db = ((int) array.getf(x, y)) > 0 ? db + spacingX : 0;
                output.setf(x, y, (float) Math.min(output.getf(x, y), db * db));
            }
        }
    }

    private double processStep2(ImageProcessor array, FloatProcessor output, double spacingY)
    {
        // retrieve size of array
        int sizeX = array.getWidth();
        int sizeY = array.getHeight();
        
        double[] buffer = new double[sizeY];
        double distMax = 0.0;
        double dy2 = spacingY * spacingY;
        
        // iterate over y-columns of the array
        this.fireStatusChanged(this, "Process Y-direction");
        for (int x = 0; x < sizeX; x++)
        {
            this.fireProgressChanged(this, x, sizeX);
            
            // init buffer
            for (int y = 0; y < sizeY; y++)
            {
                buffer[y] = output.getValue(x, y);
            }
            
            for (int y = 0; y < sizeY; y++)
            {
                double dist = buffer[y];
                if (dist > 0)
                {
                    // compute bounds of interval to look for min distance 
                    int rMax = (int) Math.ceil(Math.sqrt(dist) / spacingY + 1);
                    int rStart = Math.min(rMax, y);
                    int rEnd = Math.min(rMax, sizeY - y);
                    
                    for (int n = -rStart; n < rEnd; n++)
                    {
                        double w = buffer[y + n] + n * n * dy2;
                        if (w < dist) dist = w;
                    }
                    
                    if (dist < buffer[y])
                    {
                        output.setf(x, y, (float) dist);
                    }
                    
                    if (dist > distMax)
                    {
                        distMax = dist;
                    }
                }
            }
        }
        
        return distMax;
    }

	private static final void normalize(ImageProcessor array)
	{
        // convert squared distance to distance
        for (int y = 0; y < array.getHeight(); y++)
        {
            for (int x = 0; x < array.getWidth(); x++)
            {
            	array.setf(x, y, (float) Math.sqrt(array.getf(x, y)));
            }
        }
	}
}
