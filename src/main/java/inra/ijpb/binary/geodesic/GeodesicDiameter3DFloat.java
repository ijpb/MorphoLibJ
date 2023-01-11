/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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
package inra.ijpb.binary.geodesic;

import ij.IJ;
import ij.ImageStack;
import ij.measure.ResultsTable;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.algo.AlgoListener;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.binary.distmap.ChamferMask3D.FloatOffset;
import inra.ijpb.data.Cursor3D;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.label.LabelImages;

/**
 * Computes geodesic diameters in 3D label images using floating point chamfer
 * weights.
 *
 *
 * <p>
 * Example of use:
 *<pre>{@code
 *	GeodesicDiameterFloat3D gd3d = new GeodesicDiameter3DFloat(ChamferWeights3D.BORGEFORS);
 *	ResultsTable table = gd3d.process(inputLabelImage);
 *	table.show("Geodesic Diameter 3D");
 *}</pre>
 *
 * @deprecated replaced by inra.ijpb.measure.region3d.GeodesicDiameter3D
 * 
 * @see inra.ijpb.measure.region3d.GeodesicDiameter3D
 * @see inra.ijpb.binary.ChamferWeights3D
 * 
 * @author dlegland
 *
 */
@Deprecated
public class GeodesicDiameter3DFloat extends AlgoStub implements GeodesicDiameter3D, AlgoListener
{
	// ==================================================
	// Class variables
	
	/**
	 * The chamfer mask used to propagate distances to neighbor pixels.
	 */
	ChamferMask3D chamferMask;

	/**
	 * The string used for indicating the current step in algo events.
	 */
	String currentStep = "";
	
	
	// ==================================================
	// Constructors 
	
	/**
	 * Creates a new 3D geodesic diameter computation operator.
	 * 
	 * @param chamferMask
	 *            an instance of ChamferMask3D, which provides the float values
	 *            used for propagating distances
	 */
	public GeodesicDiameter3DFloat(ChamferMask3D chamferMask)
	{
		this.chamferMask = chamferMask;
	}
	
	/**
	 * Creates a new 3D geodesic diameter computation operator.
	 * 
	 * @deprecated new uses ChamferMask3D (since 1.4.4)
	 * 
	 * @param chamferWeights
	 *            an instance of ChamferWeights, which provides the float values
	 *            used for propagating distances
	 */
	@Deprecated
	public GeodesicDiameter3DFloat(inra.ijpb.binary.ChamferWeights3D chamferWeights)
	{
		this.chamferMask = ChamferMask3D.fromWeights(chamferWeights.getFloatWeights());
	}
	
	/**
	 * Creates a new 3D geodesic diameter computation operator.
	 * 
	 * @param weights
	 *            the array of weights for orthogonal, diagonal, and eventually
	 *            chess-knight moves neighbors
	 */
	public GeodesicDiameter3DFloat(float[] weights)
	{
		if (weights.length < 3)
		{
			throw new IllegalArgumentException("Requires an array with at least three elements");
		}
		this.chamferMask = ChamferMask3D.fromWeights(weights);
	}
	
	
	// ==================================================
	// Processing methods
	
	/**
	 * Computes the geodesic diameter of each particle within the given label
	 * image.
	 * 
	 * @param labelImage
	 *            a label image, containing either the label of a particle or
	 *            region, or zero for background
	 * @return a ResultsTable containing for each label the geodesic diameter of
	 *         the corresponding particle
	 */
	public ResultsTable process(ImageStack labelImage)
	{
		// Check validity of parameters
		if (labelImage==null) return null;
		
		// idenify labels within image
		int[] labels = LabelImages.findAllLabels(labelImage);
		int nbLabels = labels.length;
		
		// Create calculator for propagating distances
		GeodesicDistanceTransform3D geodDistMapAlgo;
		geodDistMapAlgo = new GeodesicDistanceTransform3DFloat(this.chamferMask, false);
		geodDistMapAlgo.addAlgoListener(this);

		// The array that stores Chamfer distances 
		ImageStack distanceMap;
		
		// Initialize mask as binarisation of labels
		ImageStack mask = BinaryImages.binarize(labelImage);
		
		// Initialize marker as complement of all labels
		ImageStack marker = createMarkerOutsideLabels(labelImage);

		this.currentStep = "initCenters";
		this.fireStatusChanged(this, "Initializing pseudo geodesic centers...");

		// first distance propagation to find an arbitrary center
		distanceMap = geodDistMapAlgo.geodesicDistanceMap(marker, mask);
		
		// Extract position of maxima
		Cursor3D[] posCenter = findPositionOfMaxValues(distanceMap, labelImage, labels);
		
		float[] radii = findMaxValues(distanceMap, labelImage, labels);
		
		// Create new marker image with position of maxima
		Images3D.fill(marker, 0);
		for (int i = 0; i < nbLabels; i++) 
		{
			Cursor3D pos = posCenter[i];
			if (pos.getX() == -1) 
			{
				IJ.showMessage("Particle Not Found", 
						"Could not find maximum for particle label " + i);
				continue;
			}
			marker.setVoxel(pos.getX(), pos.getY(), pos.getZ(), 255);
		}
		
		this.currentStep = "firstEnds";
		this.fireStatusChanged(this, "Computing first geodesic extremities...");

		// Second distance propagation from first maximum
		distanceMap = geodDistMapAlgo.geodesicDistanceMap(marker, mask);

		// find position of maximal value,
		// this is expected to correspond to a geodesic extremity 
		Cursor3D[] pos1 = findPositionOfMaxValues(distanceMap, labelImage, labels);
		
		// Create new marker image with position of maxima
		Images3D.fill(marker, 0);
		for (int i = 0; i < nbLabels; i++) 
		{
			Cursor3D pos = pos1[i];
			if (pos.getX() == -1) 
			{
				IJ.showMessage("Particle Not Found", 
						"Could not find maximum for particle label " + i);
				continue;
			}
			marker.setVoxel(pos.getX(), pos.getY(), pos.getZ(), 255);
		}
		
		this.currentStep = "secondEnds";
		this.fireStatusChanged(this, "Computing second geodesic extremities...");

		// third distance propagation from second maximum
		distanceMap = geodDistMapAlgo.geodesicDistanceMap(marker, mask);
		
		// compute max distance constrained to each label,
		float[] values = findMaxValues(distanceMap, labelImage, labels);
		//System.out.println("value: " + value);
		Cursor3D[] pos2 = findPositionOfMaxValues(distanceMap, labelImage, labels);
		
		
		// retrieve the minimum weight
		double w0 = Double.POSITIVE_INFINITY;
		for (FloatOffset offset : this.chamferMask.getFloatOffsets())
		{
			w0 = Math.min(w0, offset.weight);
		}

		// Initialize a new results table
		ResultsTable table = new ResultsTable();

		// populate the results table with features of each label
		for (int i = 0; i < nbLabels; i++) 
		{
			// Small conversion to normalize to pixel distances
			double radius = ((double) radii[i]) / w0;
			double value = ((double) values[i]) / w0 + 1;
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addValue("Label", labels[i]);
			table.addValue("Geod. Diam.", value);
			table.addValue("Radius", radius);
			table.addValue("Geod. Elong.", Math.max(value / (radius * 2), 1.0));
			table.addValue("xi", posCenter[i].getX());
			table.addValue("yi", posCenter[i].getY());
			table.addValue("zi", posCenter[i].getZ());
			table.addValue("x1", pos1[i].getX());
			table.addValue("y1", pos1[i].getY());
			table.addValue("z1", pos1[i].getZ());
			table.addValue("x2", pos2[i].getX());
			table.addValue("y2", pos2[i].getY());
			table.addValue("z2", pos2[i].getZ());
		}

		return table;
	}

	// ==================================================
	// Private processing methods
	
	/**
	 * Create the binary image with value 255 for mask pixels equal to 0, 
	 * and value 0 for any other value of mask.
	 */
	private ImageStack createMarkerOutsideLabels(ImageStack mask) 
	{
		// Extract image size
		int sizeX = mask.getWidth();
		int sizeY = mask.getHeight();
		int sizeZ = mask.getSize();
		
		// Create result image
		ImageStack marker = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		
		// Fill result image to either 255 or 0.
		for(int z = 0; z < sizeZ; z++) 
		{
			for(int y = 0; y < sizeY; y++) 
			{
				for(int x = 0; x < sizeX; x++) 
				{				
					marker.setVoxel(x, y, z, mask.getVoxel(x, y, z) == 0 ? 255 : 0);
				}
			}
		}
		
		// Return result
		return marker;
	}

	/**
	 * Find one position for each label. 
	 */
	private Cursor3D[] findPositionOfMaxValues(ImageStack image, 
			ImageStack labelImage, int[] labels)
	{
		// extract image size
		int sizeX 	= labelImage.getWidth();
		int sizeY 	= labelImage.getHeight();
		int sizeZ 	= labelImage.getSize();
		
		// Compute value of greatest label
		int nbLabel = labels.length;
		int maxLabel = 0;
		for (int i = 0; i < nbLabel; i++)
			maxLabel = Math.max(maxLabel, labels[i]);
		
		// init index of each label
		// to make correspondence between label value and label index
		int[] labelIndex = new int[maxLabel+1];
		for (int i = 0; i < nbLabel; i++)
			labelIndex[labels[i]] = i;
//		int[] inds = LabelImages.mapLabelIndices(...)
				
		// Init Position and value of maximum for each label
		Cursor3D[] posMax 	= new Cursor3D[nbLabel];
		float[] maxValues = new float[nbLabel];
		for (int i = 0; i < nbLabel; i++) 
		{
			maxValues[i] = -1;
			posMax[i] = new Cursor3D(-1, -1, -1);
		}
		
		// store current value
		float value;
		int index;
		
		// iterate on image pixels
		for (int z = 0; z < sizeZ; z++) 
		{
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int label = (int) labelImage.getVoxel(x, y, z);

					// do not process pixels that do not belong to particle
					if (label==0)
						continue;

					index = labelIndex[label];

					// update values and positions
					value = (float) image.getVoxel(x, y, z);
					if (value > maxValues[index]) 
					{
						posMax[index] = new Cursor3D(x, y, z);
						maxValues[index] = value;
					}
				}
			}
		}

		return posMax;
	}

	/**
	 * Finds maximum value within each label.
	 */
	private float[] findMaxValues(ImageStack image, 
			ImageStack labelImage, int[] labels) 
	{
		// extract image size
		int sizeX 	= labelImage.getWidth();
		int sizeY 	= labelImage.getHeight();
		int sizeZ 	= labelImage.getSize();
		
		// Compute value of greatest label
		int nbLabel = labels.length;
		int maxLabel = 0;
		for (int i = 0; i < nbLabel; i++)
			maxLabel = Math.max(maxLabel, labels[i]);
		
		// init index of each label
		// to make correspondence between label value and label index
		int[] labelIndex = new int[maxLabel+1];
		for (int i = 0; i < nbLabel; i++)
			labelIndex[labels[i]] = i;
				
		// Init Position and value of maximum for each label
		float[] maxValues = new float[nbLabel];
		for (int i = 0; i < nbLabel; i++)
			maxValues[i] = Float.MIN_VALUE;
		
		// store current value
		float value;
		int index;
		
		// iterate on image pixels
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int label = (int) labelImage.getVoxel(x, y, z);

					// do not process voxels that do not belong to particle
					if (label == 0)
						continue;

					index = labelIndex[label];

					// update values and positions
					value = (float) image.getVoxel(x, y, z);
					if (value > maxValues[index])
						maxValues[index] = value;
				}
			}
		}	
		return maxValues;
	}

	// ==================================================
	// Implementation of AlgoListener interface 
	
	@Override
	public void algoProgressChanged(AlgoEvent evt) 
	{
		fireProgressChanged(new Event(this, evt));
	}

	@Override
	public void algoStatusChanged(AlgoEvent evt) 
	{
		evt = new Event(this, evt);
		fireStatusChanged(evt);
	}
	
	/**
	 * Encapsulation class to add a semantic layer on the interpretation of the event.
	 */
	class Event extends AlgoEvent
	{
		public Event(GeodesicDiameter3DFloat source, AlgoEvent evt)
		{
			super(source, "(GeodDiam3d) " + evt.getStatus(), evt.getCurrentProgress(), evt.getTotalProgress());
			if (!currentStep.isEmpty())
			{
				this.status = "(GeodDiam3d-" + currentStep + ") " + evt.getStatus();
			}
		}
	}
}
