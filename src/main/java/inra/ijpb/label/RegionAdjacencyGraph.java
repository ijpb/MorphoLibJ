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
package inra.ijpb.label;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import java.util.Set;
import java.util.TreeSet;

/**
 * <p>
 * Contains several methods for extracting the set of adjacencies between
 * regions in a label image.</p>
 * 
 * <p>
 * The value zero is assumed to be background, and is not computed for
 * adjacencies. Moreover, in order to take into account label images obtained
 * after a watershed transform, adjacencies are computed by comparing label
 * values with the values of neighbors shifted by two pixels in the X, Y or Z
 * directions.</p>
 * 
 * <p>
 * Usage:
 * <pre>{@code
 * // create demo image with four regions, and four adjacencies 
 * byte[] data = new byte[]{
 * 			1, 1, 1, 0, 2, 2, 
 * 			1, 1, 1, 0, 2, 2, 
 * 			0, 0, 0, 0, 2, 2, 
 * 			3, 3, 0, 0, 0, 0,  
 * 			3, 3, 0, 4, 4, 4,  
 * 			3, 3, 0, 4, 4, 4};
 * ImageProcessor image = new ByteProcessor(6, 6, data);
 * 	
 * // compute adjacencies, and return result as a set of label pairs
 * Set<RegionAdjacencyGraph.LabelPair> adjacencies =
 *     RegionAdjacencyGraph.computeAdjacencies(image);
 * 
 * // display which regions are adjacent 
 * for (RegionAdjacencyGraph.LabelPair pair : adjacencies)
 * {
 *     System.out.println("(" + pair.label1 + "," + pair.label2 + ")");
 * }
 * }</pre>
 * @author dlegland
 *
 */
public class RegionAdjacencyGraph 
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	private RegionAdjacencyGraph()
	{
	}
	
	/**
	 * Returns the set of region adjacencies in an ImagePlus, that can contains
	 * either an ImageProcessor or an ImageStack.
	 * 
	 * @param image
	 *            an ImagePlus containing a 2D or 3D label image
	 * @return the set of adjacencies within the image
	 */
	public static final Set<LabelPair> computeAdjacencies(ImagePlus image)
	{
		if (image.getStackSize() == 1)
			return computeAdjacencies(image.getProcessor());
		else
			return computeAdjacencies(image.getStack());
	}

	/**
	 * Returns the set of region adjacencies in an ImageProcessor of labels.
	 * 
	 * @param image
	 *            an ImageProcesor containing a label image
	 * @return the set of adjacencies within the image
	 */
	public static final Set<LabelPair> computeAdjacencies(ImageProcessor image)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		
		TreeSet<LabelPair> list = new TreeSet<LabelPair>();
		
		// transitions in x direction
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width - 2; x++)
			{
				int label = (int) image.getf(x, y);
				if (label == 0)
					continue;
				int label2 = (int) image.getf(x + 2, y);
				if (label2 == 0 || label2 == label)
					continue;
				
				LabelPair pair = new LabelPair(label, label2);
				list.add(pair);
			}
		}
		
		// transitions in y direction
		for (int y = 0; y < height - 2; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int label = (int) image.getf(x, y);
				if (label == 0)
					continue;
				int label2 = (int) image.getf(x, y + 2);
				if (label2 == 0 || label2 == label)
					continue;
				
				LabelPair pair = new LabelPair(label, label2);
				list.add(pair);
			}
		}
		
		return list;
	}
	
	/**
	 * Returns the set of region adjacencies in an ImageProcessor of labels.
	 * 
	 * @param image
	 *            an ImageProcesor containing a label image
	 * @return the set of adjacencies within the image
	 */
	public static final Set<LabelPair> computeAdjacencies(ImageStack image)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		int depth = image.getSize();
		
		TreeSet<LabelPair> list = new TreeSet<LabelPair>();
		
		// transitions in x direction
		for (int z = 0; z < depth; z++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width - 2; x++)
				{
					int label = (int) image.getVoxel(x, y, z);
					if (label == 0)
						continue;
					int label2 = (int) image.getVoxel(x + 2, y, z);
					if (label2 == 0 || label2 == label)
						continue;

					LabelPair pair = new LabelPair(label, label2);
					list.add(pair);
				}
			}
		}

		// transitions in y direction
		for (int z = 0; z < depth; z++)
		{
			for (int y = 0; y < height - 2; y++)
			{
				for (int x = 0; x < width; x++)
				{
					int label = (int) image.getVoxel(x, y, z);
					if (label == 0)
						continue;
					int label2 = (int) image.getVoxel(x, y + 2, z);
					if (label2 == 0 || label2 == label)
						continue;

					LabelPair pair = new LabelPair(label, label2);
					list.add(pair);
				}
			}
		}

		// transitions in z direction
		for (int z = 0; z < depth - 2; z++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					int label = (int) image.getVoxel(x, y, z);
					if (label == 0)
						continue;
					int label2 = (int) image.getVoxel(x, y, z + 2);
					if (label2 == 0 || label2 == label)
						continue;

					LabelPair pair = new LabelPair(label, label2);
					list.add(pair);
				}
			}
		}
	
		return list;
	}

	/**
	 * Used to stores the adjacency information between two regions. In order to
	 * ensure symmetry of the relation, the value of label1 field always
	 * contains the lower label, while the value of label2 always contains the
	 * highest label.
	 */
	public static final class LabelPair implements Comparable <LabelPair>
	{
		/**
		 * The first label.
		 */
		
		public final int label1;
		/**
		 * The second label.
		 */
		public final int label2;
		
		/**
		 * Creates a new Label Pair.
		 * 
		 * @param label1
		 *            the first label.
		 * @param label2
		 *            the second label.
		 */
		public LabelPair(int label1, int label2)
		{
			if (label1 < label2) 
			{
				this.label1 = label1;
				this.label2 = label2;
			}
			else
			{
				this.label1 = label2;
				this.label2 = label1;
			}

		}

		@Override
		public int compareTo(LabelPair pair) 
		{
			// first check if label1 is different 
			if (this.label1 < pair.label1)
				return -1;
			if (this.label1 > pair.label1)
				return +1;
			// then check if label2 is different 
			if (this.label2 < pair.label2)
				return -1;
			if (this.label2 > pair.label2)
				return +1;
			// if both labels are the same, the two pairs are equal 
			return 0;
		}

		@Override
		public int hashCode()
		{
			int hash = 31;
			hash = hash * 17 + this.label1;
			hash = hash * 17 + this.label2;
			return hash;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof LabelPair)
			{
				return false;
			}
			LabelPair that = (LabelPair) obj;
			return this.label1 == that.label1 && this.label2 == that.label2; 
		}
	}
}
