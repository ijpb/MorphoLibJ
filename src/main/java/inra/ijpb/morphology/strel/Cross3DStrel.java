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
package inra.ijpb.morphology.strel;

import static java.lang.Math.max;
import static java.lang.Math.min;
import ij.ImageStack;
import inra.ijpb.morphology.Strel3D;

/**
 * 3D structuring element representing a 3x3x3 cross, that considers the 
 * center voxel together with the six orthogonal neighbors.
 * @author David Legland
 *
 */
public class Cross3DStrel extends AbstractStrel3D {

	/**
	 * Create a new instance of this structuring element.
	 */
	public Cross3DStrel() {
	}

	@Override
	public int[] getSize() {
		return new int[]{3, 3, 3};
	}

	@Override
	public int[][][] getMask3D() {
		int[][][] mask = new int[3][3][3];
		mask[1][1][1] = 255;
		mask[1][1][0] = 255;
		mask[1][1][2] = 255;
		mask[1][0][1] = 255;
		mask[1][2][1] = 255;
		mask[0][1][1] = 255;
		mask[2][1][1] = 255;
		return mask;
	}

	@Override
	public int[] getOffset() {
		return new int[]{1, 1, 1};
	}

	@Override
	public int[][] getShifts3D() {
		return new int[][]{
				{ 0, 0, 0}, 
				{-1, 0, 0}, 
				{+1, 0, 0}, 
				{0, -1, 0}, 
				{0, +1, 0}, 
				{0, 0, -1}, 
				{0, 0, +1}, 
		};
	}

	@Override
	public Strel3D reverse() {
		return new Cross3DStrel();
	}

	@Override
	public ImageStack dilation(ImageStack image) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		ImageStack result = image.duplicate();
	
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					double value = image.getVoxel(x, y, z);
					double maxValue = value;
					
					// Iterate over the 3 'upper' neighbors of current pixel
					if (x > 0) 
						maxValue = max(maxValue, image.getVoxel(x-1, y, z));
					if (x < sizeX-1) 
						maxValue = max(maxValue, image.getVoxel(x+1, y, z));
					if (y > 0) 
						maxValue = max(maxValue, image.getVoxel(x, y-1, z));
					if (y < sizeY - 1) 
						maxValue = max(maxValue, image.getVoxel(x, y+1, z));
					if (z > 0) 
						maxValue = max(maxValue, image.getVoxel(x, y, z-1));
					if (z < sizeZ - 1) 
						maxValue = max(maxValue, image.getVoxel(x, y, z+1));
					result.setVoxel(x, y, z, maxValue);
				}
			}
		}
		return result;
	}

	@Override
	public ImageStack erosion(ImageStack image) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		ImageStack result = image.duplicate();
	
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					double value = image.getVoxel(x, y, z);
					double minValue = value;
					
					// Iterate over the 3 'upper' neighbors of current pixel
					if (x > 0) 
						minValue = min(minValue, image.getVoxel(x-1, y, z));
					if (x < sizeX-1) 
						minValue = min(minValue, image.getVoxel(x+1, y, z));
					if (y > 0) 
						minValue = min(minValue, image.getVoxel(x, y-1, z));
					if (y < sizeY - 1) 
						minValue = min(minValue, image.getVoxel(x, y+1, z));
					if (z > 0) 
						minValue = min(minValue, image.getVoxel(x, y, z-1));
					if (z < sizeZ - 1) 
						minValue = min(minValue, image.getVoxel(x, y, z+1));
					result.setVoxel(x, y, z, minValue);
				}
			}
		}
		return result;
	}

	@Override
	public ImageStack closing(ImageStack image) {
		return erosion(dilation(image));
	}

	@Override
	public ImageStack opening(ImageStack image) {
		return dilation(erosion(image));
	}


}
