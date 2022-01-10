/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
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
package inra.ijpb.measure.region3d;

import java.util.Map;

import ij.IJ;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.algo.AlgoListener;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransform3D;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransform3DFloat;
import inra.ijpb.data.Cursor3D;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.label.LabelImages;
import inra.ijpb.label.LabelValues;
import inra.ijpb.label.LabelValues.Position3DValuePair;

/**
 * Computes the 3D geodesic diameters of regions within a 3D binary or label
 * images using floating point computation for propagating distances.
 *
 * <p>
 * Example of use:
 * 
 * <pre>
 * {@code
 * 	GeodesicDiameter3D gd3d = new GeodesicDiameter3D(ChamferMask3D.BORGEFORS);
 *  Map<Integer, GeodesicDiameter3D.Result> resMap = g3d.analyzeRegions(inputLabelImagePlus);
 * 	ResultsTable table = gd3d.createTable(resMap);
 * 	table.show("Geodesic Diameter 3D");
 * }
 * </pre>
 *
 * @see inra.ijpb.measure.region2d.GeodesicDiameter
 * @see inra.ijpb.binary.ChamferMask3D
 * 
 * @author dlegland
 *
 */
public class GeodesicDiameter3D extends RegionAnalyzer3D<GeodesicDiameter3D.Result> implements AlgoListener
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
	 * @param mask
	 *            an instance of ChamferMask3D, which provides the float values
	 *            used for propagating distances
	 */
	public GeodesicDiameter3D(ChamferMask3D mask)
	{
		this.chamferMask = mask;
	}
	
	
	// ==================================================
	// Processing methods
	
	public ResultsTable process(ImageStack labelImage)
	{
		// Check validity of parameters
		if (labelImage==null) return null;
		
		// identify labels within image
		int[] labels = LabelImages.findAllLabels(labelImage);
		
		Result[] results = analyzeRegions(labelImage, labels, new Calibration());
		Map<Integer, Result> map = createMap(labels, results);
		
		return createTable(map);
		
//		// Create calculator for propagating distances
//		GeodesicDistanceTransform3D geodDistMapAlgo;
//		geodDistMapAlgo = new GeodesicDistanceTransform3DFloat(this.chamferMask, false);
//		geodDistMapAlgo.addAlgoListener(this);
//
//		// The array that stores Chamfer distances 
//		ImageStack distanceMap;
//		
//		// Initialize mask as binarisation of labels
//		ImageStack mask = BinaryImages.binarize(labelImage);
//		
//		// Initialize marker as complement of all labels
//		ImageStack marker = createMarkerOutsideLabels(labelImage);
//
//		this.currentStep = "initCenters";
//		this.fireStatusChanged(this, "Initializing pseudo geodesic centers...");
//
//		// first distance propagation to find an arbitrary center
//		distanceMap = geodDistMapAlgo.geodesicDistanceMap(marker, mask);
//		
//		// Extract position of maxima
//		Cursor3D[] posCenter = findPositionOfMaxValues(distanceMap, labelImage, labels);
//		
//		float[] radii = findMaxValues(distanceMap, labelImage, labels);
//		
//		// Create new marker image with position of maxima
//		Images3D.fill(marker, 0);
//		for (int i = 0; i < nbLabels; i++) 
//		{
//			Cursor3D pos = posCenter[i];
//			if (pos.getX() == -1) 
//			{
//				IJ.showMessage("Particle Not Found", 
//						"Could not find maximum for particle label " + i);
//				continue;
//			}
//			marker.setVoxel(pos.getX(), pos.getY(), pos.getZ(), 255);
//		}
//		
//		this.currentStep = "firstEnds";
//		this.fireStatusChanged(this, "Computing first geodesic extremities...");
//
//		// Second distance propagation from first maximum
//		distanceMap = geodDistMapAlgo.geodesicDistanceMap(marker, mask);
//
//		// find position of maximal value,
//		// this is expected to correspond to a geodesic extremity 
//		Cursor3D[] pos1 = findPositionOfMaxValues(distanceMap, labelImage, labels);
//		
//		// Create new marker image with position of maxima
//		Images3D.fill(marker, 0);
//		for (int i = 0; i < nbLabels; i++) 
//		{
//			Cursor3D pos = pos1[i];
//			if (pos.getX() == -1) 
//			{
//				IJ.showMessage("Particle Not Found", 
//						"Could not find maximum for particle label " + i);
//				continue;
//			}
//			marker.setVoxel(pos.getX(), pos.getY(), pos.getZ(), 255);
//		}
//		
//		this.currentStep = "secondEnds";
//		this.fireStatusChanged(this, "Computing second geodesic extremities...");
//
//		// third distance propagation from second maximum
//		distanceMap = geodDistMapAlgo.geodesicDistanceMap(marker, mask);
//		
//		// compute max distance constrained to each label,
//		float[] values = findMaxValues(distanceMap, labelImage, labels);
//		//System.out.println("value: " + value);
//		Cursor3D[] pos2 = findPositionOfMaxValues(distanceMap, labelImage, labels);
//		
//		
//		// retrieve the minimum weight
//		double w0 = Double.POSITIVE_INFINITY;
//		for (FloatOffset offset : this.chamferMask.getFloatOffsets())
//		{
//			w0 = Math.min(w0, offset.weight);
//		}
//
//		// Initialize a new results table
//		ResultsTable table = new ResultsTable();
//
//		// populate the results table with features of each label
//		for (int i = 0; i < nbLabels; i++) 
//		{
//			// Small conversion to normalize to pixel distances
//			double radius = ((double) radii[i]) / w0;
//			double value = ((double) values[i]) / w0 + 1;
//			
//			// add an entry to the resulting data table
//			table.incrementCounter();
//			table.addValue("Label", labels[i]);
//			table.addValue("Geod. Diam.", value);
//			table.addValue("Radius", radius);
//			table.addValue("Geod. Elong.", Math.max(value / (radius * 2), 1.0));
//			table.addValue("xi", posCenter[i].getX());
//			table.addValue("yi", posCenter[i].getY());
//			table.addValue("zi", posCenter[i].getZ());
//			table.addValue("x1", pos1[i].getX());
//			table.addValue("y1", pos1[i].getY());
//			table.addValue("z1", pos1[i].getZ());
//			table.addValue("x2", pos2[i].getX());
//			table.addValue("y2", pos2[i].getY());
//			table.addValue("z2", pos2[i].getZ());
//		}
//
//		return table;
	}


	// ==================================================
	// Implementation of AlgoListener interface 
	
	// ==================================================
	// Processing methods
	
	@Override
	public ResultsTable createTable(Map<Integer, Result> map)
	{
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int label : map.keySet())
		{
			// current diameter
			Result res = map.get(label);
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			table.addValue("GeodesicDiameter", res.diameter);
			
			// coordinates of max inscribed circle
			table.addValue("Radius", res.innerRadius);
			table.addValue("InitPoint.X", res.initialPoint.getX());
			table.addValue("InitPoint.Y", res.initialPoint.getY());
			table.addValue("InitPoint.Z", res.initialPoint.getZ());
			table.addValue("GeodesicElongation", Math.max(res.diameter / (res.innerRadius * 2), 1.0));
			
		    // coordinate of first and second geodesic extremities 
			table.addValue("Extremity1.X", res.firstExtremity.getX());
			table.addValue("Extremity1.Y", res.firstExtremity.getY());
			table.addValue("Extremity1.Z", res.firstExtremity.getZ());
			table.addValue("Extremity2.X", res.secondExtremity.getX());
			table.addValue("Extremity2.Y", res.secondExtremity.getY());
			table.addValue("Extremity2.Z", res.secondExtremity.getZ());
		}
	
		return table;
	}


	/**
		 * Computes the geodesic diameter of each particle within the given label
		 * image.
		 * 
		 * @param labelImage
		 *            a 3D label image, containing either the label of a particle or
		 *            region, or zero for background
		 * @param the
		 *            array of region labels to process
		 * @param calin
		 *            the spatial calibration if the image
		 * @return an array of Result instances containing for each label the
		 *         geodesic diameter of the corresponding particle
		 */
		@Override
		public Result[] analyzeRegions(ImageStack labelImage, int[] labels,
				Calibration calib)
		{
			// Initial check-up
			if (calib.pixelWidth != calib.pixelHeight || calib.pixelWidth != calib.pixelDepth)
			{
				throw new RuntimeException("Requires image with cubic voxels");
			}
	
			// number of labels to process
			int nLabels = labels.length;
			
			// Create new marker image
			int sizeX = labelImage.getWidth();
			int sizeY = labelImage.getHeight();
			int sizeZ = labelImage.size();
			
			// Create calculator for computing geodesic distances within label image
			GeodesicDistanceTransform3D gdt;
			gdt = new GeodesicDistanceTransform3DFloat(this.chamferMask, false);
			gdt.addAlgoListener(this);
	
			
			ImageStack marker = ImageStack.create(sizeX, sizeY, sizeZ, 8);
			
			// Compute distance map from label borders to identify centers
			// (The distance map correctly processes adjacent borders)
			this.fireStatusChanged(this, "Initializing pseudo geodesic centers...");
			ImageStack distanceMap = LabelImages.distanceMap(labelImage, chamferMask, true, false);
		
			// Extract position of maxima
			Position3DValuePair[] innerCircles = LabelValues.findMaxValues(distanceMap, labelImage, labels);
			
			// initialize marker image with position of maxima
			Images3D.fill(marker, 0);
			for (int i = 0; i < nLabels; i++) 
			{
				Cursor3D center = innerCircles[i].getPosition();
				if (center.getX() == -1)
				{
					IJ.showMessage("Particle Not Found", 
							"Could not find maximum for particle label " + labels[i]);
					continue;
				}
				marker.setVoxel(center.getX(), center.getY(), center.getZ(), 255);
			}
		
			this.fireStatusChanged(this, "Computing first geodesic extremities...");
		
			// Second distance propagation from first maximum
			distanceMap = gdt.geodesicDistanceMap(marker, labelImage);
			
			// find position of maximal value for each label
			// this is expected to correspond to a geodesic extremity 
			Cursor3D[] firstGeodesicExtremities = LabelValues.findPositionOfMaxValues(distanceMap, labelImage, labels);
			
			// Create new marker image with position of maxima
			Images3D.fill(marker, 0);
			for (int i = 0; i < nLabels; i++)
			{
				Cursor3D pos = firstGeodesicExtremities[i];
				if (pos.getX() == -1) 
				{
					IJ.showMessage("Particle Not Found", 
							"Could not find maximum for particle label " + labels[i]);
					continue;
				}
				marker.setVoxel(pos.getX(), pos.getY(), pos.getZ(), 255);
			}
			
			this.fireStatusChanged(this, "Computing second geodesic extremities...");
		
			// third distance propagation from second maximum
			distanceMap = gdt.geodesicDistanceMap(marker, labelImage);
			
			// also computes position of maxima
			Position3DValuePair[] secondGeodesicExtremities = LabelValues.findMaxValues(distanceMap, labelImage, labels);
			
			// Create array of results and populate with computed values
			GeodesicDiameter3D.Result[] result = new GeodesicDiameter3D.Result[nLabels];
			double w0 = chamferMask.getNormalizationWeight();
			for (int i = 0; i < nLabels; i++)
			{
				Result res = new Result();
						
				// Get the maximum distance within each label
				double diam = secondGeodesicExtremities[i].getValue();
				// and add sqrt(3) to take into account maximum voxel thickness
				// and normalize by first weight of chamfer mask
				res.diameter = diam / w0 + Math.sqrt(3);
				
				// also keep references to characteristic points
				res.initialPoint = innerCircles[i].getPosition();
				res.innerRadius = innerCircles[i].getValue() / w0;
				res.firstExtremity = firstGeodesicExtremities[i];
				res.secondExtremity = secondGeodesicExtremities[i].getPosition();
				
				// store the result
				result[i] = res;
			}
			
	//		// calibrate the results
	//		if (calib.scaled())
	//		{
	//			this.fireStatusChanged(this, "Re-calibrating results");
	//			for (int i = 0; i < nLabels; i++)
	//			{
	//				result[i] = result[i].recalibrate(calib);
	//			}
	//		}
			
			// returns the results
			return result;
		}


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
		public Event(GeodesicDiameter3D source, AlgoEvent evt)
		{
			super(source, "(GeodDiam3d) " + evt.getStatus(), evt.getCurrentProgress(), evt.getTotalProgress());
			if (!currentStep.isEmpty())
			{
				this.status = "(GeodDiam3d-" + currentStep + ") " + evt.getStatus();
			}
		}
	}
	
	
	// ==================================================
	// Inner class used for representing computation results
	
	/**
	 * Inner class used for representing results of 3D geodesic diameters
	 * computations. Each instance corresponds to a single region / particle.
	 * 
	 * @author dlegland
	 *
	 */
	public class Result
	{
		/** The geodesic diameter of the region */
		public double diameter;

		/**
		 * The initial point used for propagating distances, corresponding the
		 * center of one of the minimum inscribed circles.
		 */
		public Cursor3D initialPoint;

		/**
		 * The radius of the largest inner ball. Value may depends on the
		 * chamfer weihgts.
		 */
		public double innerRadius;

		/**
		 * The first geodesic extremity found by the algorithm.
		 */
		public Cursor3D firstExtremity;

		/**
		 * The second geodesic extremity found by the algorithm.
		 */
		public Cursor3D secondExtremity;
	}
}
