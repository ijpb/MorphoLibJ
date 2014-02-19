/**
 * 
 */
package inra.ijpb.measure;

import java.util.Iterator;
import java.util.TreeSet;

import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

/**
 * Provides a set of static methods to compute geometric measures such as area,
 * perimeter, and their densities in planar binary or label images.
 * 
 * @author David Legland
 *
 */
public class GeometricMeasures2D {
	
    // ====================================================
    // Class constants 

	public static boolean debug = false;
	

	// ====================================================
    // Main processing functions 

    /**
     * Compute perimeter of each label using Crofton method.  
     * 
     * @param labelImage the input image containing label of particles
     * @param nDirs the number of directions to process, either 2 or 4
     * @param resol the spatial resolution
     * @return a data table containing the perimeter of each labeled particle 
     */
	public static ResultsTable croftonPerimeter(ImageProcessor labelImage,
			double[] resol, int nDirs) {
        if (nDirs == 2)
        	return croftonPerimeter_D2(labelImage, resol);
        else if (nDirs == 4)
        	return croftonPerimeter_D4(labelImage, resol);
        else
        	throw new IllegalArgumentException("Number of directions must be 2 or 4");
    }
    
    /**
     * Compute porosity and perimeter density of binary image.  
     * 
     */
	public static ResultsTable perimeterDensity(ImageProcessor image,
			double[] resol, int nDirs) {
        if (nDirs == 2)
        	return perimeterDensity_D2(image, resol);
        else 
        	return perimeterDensity_D4(image, resol);
    }

    /**
     * Compute perimeter of each label using Crofton method with 2 directions.  
     */
    private static ResultsTable croftonPerimeter_D2(ImageProcessor labelImage, 
    		double[] resol) {
        // Check validity of parameters
        if (labelImage==null) return null;
        
        int[] labels = findAllLabels(labelImage);
        int nbLabels = labels.length;

        // Create data table
        ResultsTable table = new ResultsTable();
        double perim;
                                     
        double d1  = resol[0];
        double d2  = resol[1];

        for (int i = 0; i < nbLabels; i++) {
        	int label = labels[i];

        	int area = particleArea(labelImage, label);

        	// Count number of transitions in each direction
        	int n1 = countTransitionsD00(labelImage, label, true); 
        	int n2 = countTransitionsD90(labelImage, label, true);
        	
        	// Compute perimeter
        	perim = (n1 * d2 + n2 * d1) * Math.PI / 4.0;
        	
        	table.incrementCounter();
        	table.addValue("Label", label);
        	
        	if (debug) {
        		table.addValue("N1", n1);
        		table.addValue("N2", n2);
        	}
        	
        	table.addValue("Perimeter", perim);

        	// Also compute circularity (ensure value is between 0 and 1)
        	double circu = Math.min(4 * Math.PI * area / (perim * perim), 1);
        	table.addValue("Circularity", circu);
        	table.addValue("Elong.", 1./circu);
        }

        return table;
    }
    
    /**
     * Compute perimeter of each label using Crofton method with 4 directions
     * (orthogonal and diagonal).  
     */
    private static ResultsTable croftonPerimeter_D4(ImageProcessor labelImage, 
    		double[] resol) {
        // Check validity of parameters
        if (labelImage==null) return null;
        
        int[] labels = findAllLabels(labelImage);
        int nbLabels = labels.length;

        // Create data table
        ResultsTable table = new ResultsTable();
        double perim;
                                     
        double d1  = resol[0];
        double d2  = resol[1];
        double d12 = Math.hypot(d1, d2);

        // area of a pixel (used for computing line densities)
        double vol = d1 * d2;

    	// compute weights associated to each direction
    	double[] weights = computeDirectionWeightsD4(resol);

    	for (int i = 0; i < nbLabels; i++) {
        	int label = labels[i];
        	
        	int area = particleArea(labelImage, label);

        	// Count number of transitions in each direction
        	int n1 = countTransitionsD00(labelImage, label, true); 
        	int n2 = countTransitionsD90(labelImage, label, true);
        	int n3 = countTransitionsD45(labelImage, label, true); 
        	int n4 = countTransitionsD135(labelImage, label, true); 
        	
        	// Compute weighted diameters and multiplies by associated
        	// direction weights 
        	double wd1 = n1 / d1  * weights[0];
        	double wd2 = n2 / d2  * weights[1];
        	double wd3 = n3 / d12 * weights[2];
        	double wd4 = n4 / d12 * weights[3];

        	// Compute perimeter
        	perim = (wd1 + wd2 + wd3 + wd4) * vol * Math.PI / 2;
        	
        	// Add new row in table
        	table.incrementCounter();
        	table.addValue("Label", label);
        	
        	// area
        	table.addValue("Area", area);
        	
        	if (debug) {
        		// Display individual counts
        		table.addValue("N1", n1);
        		table.addValue("N2", n2);
        		table.addValue("N3", n3);
        		table.addValue("N4", n4);
        	}
        	
        	// Display perimeter value
        	table.addValue("Perimeter", perim);
        	
        	// Also compute circularity (ensure value is between 0 and 1)
        	double circu = Math.min(4 * Math.PI * area / (perim * perim), 1);
        	table.addValue("Circularity", circu);
        	table.addValue("Elong.", 1./circu);
        }

        return table;
    }

	/**
	 * Computes perimeter density of the binary image using Crofton method with 2
	 * directions (horizontal and vertical).
	 */
    private static ResultsTable perimeterDensity_D2(ImageProcessor image, 
    		double[] resol) {

        // Create data table
        ResultsTable table = new ResultsTable();
        double perim;
                                     
        double d1  = resol[0];
        double d2  = resol[1];

        // area of a pixel (used for computing line densities)
        double pixelArea = d1 * d2;

    	// compute weights associated to each direction
    	double[] weights = computeDirectionWeightsD4(resol);

    	double area = particleArea(image, 255) * pixelArea;

    	// Count number of transitions in each direction
    	int n1 = countTransitionsD00(image, 255, false); 
    	int n2 = countTransitionsD90(image, 255, false);

    	// Compute weighted diameters and multiplies by associated
    	// direction weights 
    	double wd1 = n1 / d1  * weights[0];
    	double wd2 = n2 / d2  * weights[1];

    	// Compute perimeter
    	perim = (wd1 + wd2) * pixelArea * Math.PI / 2;

    	// Add new row in table
    	table.incrementCounter();

    	// area
    	table.addValue("Area", area);

    	// Compute porosity by dividing by observed area
    	int pixelCount = image.getWidth() * image.getHeight();
    	double refArea = pixelCount * pixelArea;
    	table.addValue("A. Density", area / refArea);

    	if (debug) {
    		// Display individual counts
    		table.addValue("N1", n1);
    		table.addValue("N2", n2);
    	}

    	// Display perimeter value
    	table.addValue("Perimeter", perim);

    	// compute perimeter density
		double refArea2 = (image.getWidth() - 1) * resol[0]
				* (image.getHeight() - 1) * resol[1];
		double perimDensity = perim / refArea2;
    	table.addValue("P. Density", perimDensity);

        return table;
    }

	/**
	 * Computes perimeter density of the binary image using Crofton method with 4
	 * directions (orthogonal and diagonal).
	 */
    private static ResultsTable perimeterDensity_D4(ImageProcessor image, 
    		double[] resol) {

        // Create data table
        ResultsTable table = new ResultsTable();
        double perim;
                                     
        double d1  = resol[0];
        double d2  = resol[1];
        double d12 = Math.hypot(d1, d2);

        // area of a pixel (used for computing line densities)
        double pixelArea = d1 * d2;

    	// compute weights associated to each direction
    	double[] weights = computeDirectionWeightsD4(resol);

    	double area = particleArea(image, 255) * pixelArea;

    	// Count number of transitions in each direction
    	int n1 = countTransitionsD00(image, 255, false); 
    	int n2 = countTransitionsD90(image, 255, false);
    	int n3 = countTransitionsD45(image, 255, false); 
    	int n4 = countTransitionsD135(image, 255, false); 

    	// Compute weighted diameters and multiplies by associated
    	// direction weights 
    	double wd1 = n1 / d1  * weights[0];
    	double wd2 = n2 / d2  * weights[1];
    	double wd3 = n3 / d12 * weights[2];
    	double wd4 = n4 / d12 * weights[3];

    	// Compute perimeter
    	perim = (wd1 + wd2 + wd3 + wd4) * pixelArea * Math.PI / 2;

    	// Add new row in table
    	table.incrementCounter();

    	// area
    	table.addValue("Area", area);

    	// Compute porosity by dividing by observed area
    	int pixelCount = image.getWidth() * image.getHeight();
    	double refArea = pixelCount * pixelArea;
    	table.addValue("A. Density", area / refArea);

    	if (debug) {
    		// Display individual counts
    		table.addValue("N1", n1);
    		table.addValue("N2", n2);
    		table.addValue("N3", n3);
    		table.addValue("N4", n4);
    	}

    	// Display perimeter value
    	table.addValue("Perimeter", perim);

    	// compute perimeter density
		double refArea2 = (image.getWidth() - 1) * (image.getHeight() - 1)
				* pixelArea;
		double perimDensity = perim / refArea2;
    	table.addValue("P. Density", perimDensity);

        return table;
    }

    /**
     * Count the number of transitions in the horizontal direction, by counting
     * +1 when the structure touches image edges.
     */
    private static int countTransitionsD00(ImageProcessor image, int label, boolean countBorder) {
        int width 	= image.getWidth();
        int height 	= image.getHeight();
    
        int count = 0;
        int previous = 0;
        int current;
        
        // iterate on image pixels
        for (int y = 0; y < height; y++) {
        	
        	// Count border of image
        	previous = image.get(0, y);
        	if (countBorder && previous == label)
        		count++;
        	
        	// count middle of image
            for (int x = 0; x < width; x++) {
                current = image.get(x, y);
                if (previous == label ^ current == label) // Exclusive or
                	count++;
                previous = current;
            }
            
            // Count border of image
        	if (countBorder && previous == label)
        		count++;
        }

        return count;
    }
    
    
    /**
     * Count the number of transitions in the horizontal direction, by counting
     * 1 when structure touches image edges.
     */
    private static int countTransitionsD90(ImageProcessor image, int label, boolean countBorder) {
        int width 	= image.getWidth();
        int height 	= image.getHeight();
    
        int count = 0;
        int previous = 0;
        int current;
        
        // iterate on image pixels
        for (int x = 0; x < width; x++) {
        	
        	// Count border of image
        	previous = image.get(x, 0);
        	if (countBorder && previous == label)
        		count++;
        	
        	// count middle of image
            for (int y = 0; y < height; y++) {
                current = image.get(x, y);
                if (previous == label ^ current == label) // Exclusive or
                	count++;
                previous = current;
            }
            
        	if (countBorder && previous == label)
        		count++;
        }

        return count;
    }
    
    /**
     * Count the number of transitions in the upper diagonal direction, by counting
     * 1 when structure touches image edges.
     */
    private static int countTransitionsD45(ImageProcessor image, int label, boolean countBorder) {
        int width 	= image.getWidth();
        int height 	= image.getHeight();
        int nDiags 	= width + height - 1;
    
        int count = 0;
        int previous = 0;
        int current;
        
        // iterate on image pixels
        for (int i = 0; i < nDiags; i++) {
        	int x0 = Math.max(width - i - 1, 0);
        	int x1 = Math.min(nDiags - i, width);
        	int y0 = Math.max(i + 1 - width, 0);
        	int y1 = Math.min(i + 1, height);
        	
        	
        	// Count first line border
        	previous = image.get(x0, y0);
        	if (countBorder && previous == label)
        		count++;
        	
        	// number of pixels on the diagonal line
        	int lineLength = x1 - x0;
        	int lineLength2 = y1 - y0;
        	assert lineLength == lineLength2 : "Bounds must be equal (upper diagonal " + i + ")";
        	
        	// count middle of line
            for (int j = 1; j < lineLength; j++) {
                current = image.get(x0 + j, y0 + j);
                if (previous == label ^ current == label) // Exclusive or
                	count++;
                previous = current;
            }
            
            // Count last line border
        	if (countBorder && previous == label)
        		count++;
        }

        return count;
    }
    
    /**
     * Count the number of transitions in the lower diagonal direction, by counting
     * 1 when structure touches image edges.
     */
    private static int countTransitionsD135(ImageProcessor image, int label, boolean countBorder) {
        int width 	= image.getWidth();
        int height 	= image.getHeight();
        int nDiags 	= width + height - 1;
    
        int count = 0;
        int previous = 0;
        int current;
        
        // iterate on image pixels
        for (int i = 0; i < nDiags; i++) {
        	int x0 = Math.max(i + 1 - height, 0);
        	int x1 = Math.min(i, width - 1);
        	int y0 = Math.min(i, height - 1);
        	int y1 = Math.max(i + 1 - width, 0);
        	
        	// Count first line border
        	previous = image.get(x0, y0);
        	if (countBorder && previous == label)
        		count++;
        	
        	// number of pixels on the diagonal line
        	int lineLength = x1 - x0;
        	int lineLength2 = y0 - y1;
        	assert lineLength == lineLength2 : "Bounds must be equal (lower diagonal " + i + ")";
        	
        	// count middle of line
            for (int j = 1; j <= lineLength; j++) {
                current = image.get(x0 + j, y0 - j);
                if (previous == label ^ current == label) // Exclusive or
                	count++;
                previous = current;
            }
            
            // Count last line border
        	if (countBorder && previous == label)
        		count++;
        }

        return count;
    }
    
    public static int particleArea(ImageProcessor image, int label) {
        int width 	= image.getWidth();
        int height 	= image.getHeight();
    
        int count = 0;
       
        // count all pixels belonging to the particle
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                if (image.get(x, y) == label)
                	count++;
  
        return count;
    }
    
    
    private static double[] computeDirectionWeightsD4(double[] resol) {
    	
    	// resolution in each direction
    	double d1 = resol[0];
    	double d2 = resol[1];
    	
    	// angle of the diagonal
    	double theta = Math.atan2(d2, d1);

    	// angular sector for direction 1 ([1 0])
    	double alpha1 = theta / Math.PI;

    	// angular sector for direction 2 ([0 1])
    	double alpha2 = (Math.PI / 2.0 - theta)  / Math.PI;

    	// angular sector for directions 3 and 4 ([1 1] and [-1 1])
    	double alpha34 = .25;

    	// concatenate the different weights
    	return new double[]{alpha1, alpha2, alpha34, alpha34};
    }
    
    // ====================================================
    // Utilitary functions 

    private static int[] findAllLabels(ImageProcessor image) {
        int width 	= image.getWidth();
        int height 	= image.getHeight();
        
        TreeSet<Integer> labels = new TreeSet<Integer> ();
        
        // iterate on image pixels
        for (int y = 0; y < height; y++) 
            for (int x = 0; x < width; x++) 
                labels.add(image.get(x, y));
        
        // remove 0 if it exists
        if (labels.contains(0))
            labels.remove(0);
        
        // convert to array
        int[] array = new int[labels.size()];
        Iterator<Integer> iterator = labels.iterator();
        for (int i = 0; i < labels.size(); i++) 
            array[i] = iterator.next();
        
        return array;
    }

}
