/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;

/**
 * Structuring element representing a 3x3 cross, that considers the reference
 * pixel together with four neighbors located either on the left or on the
 * right of the reference pixel.
 * 
 * @see DiamondStrel
 * @author David Legland
 */
public class ShiftedCross3x3Strel {
	
	/**
	 * A cross-shaped structuring element located on the left of the reference
	 * pixel.<p>
	 * 
	 * The structuring has the following shape (x: neighbor, o: reference
	 * pixel, .: irrelevant): 
s	 * <pre><code>
	 *  . . . . . 
	 *  . . x . . 
	 *  . x x o . 
	 *  . . x . . 
	 *  . . . . . 
	 * </code></pre>
	 */
	public final static InPlaceStrel LEFT = new ShiftedCross3x3Strel.Left();
	
	/**
	 * A cross-shaped structuring element located on the right of the
	 * reference pixel.<p>
	 * 
	 * The structuring has the following shape (x: neighbor, o: reference
	 * pixel, .: irrelevant): 
	 * <pre><code>
	 *  . . . . . 
	 *  . . x . . 
	 *  . o x x . 
	 *  . . x . . 
	 *  . . . . . 
	 * </code></pre>
	 */
	public final static InPlaceStrel RIGHT = new ShiftedCross3x3Strel.Right();

	/**
	 * A cross-shaped structuring element located on the left of the reference
	 * pixel.</p>
	 * 
	 * The structuring has the following shape (x: neighbor, o: reference
	 * pixel, .: irrelevant): 
	 * <code><pre>
	 *  . . . . . 
	 *  . . x . . 
	 *  . x x o . 
	 *  . . x . . 
	 *  . . . . . 
	 * </pre></code>
	 */
	private final static class Left extends AbstractInPlaceStrel {
		
		/**
		 * Default constructor.
		 */
		public Left() {
		}

		/* (non-Javadoc)
		 * @see Strel#getSize()
		 */
		@Override
		public int[] getSize() {
			return new int[]{3, 3};
		}

		/* (non-Javadoc)
		 * @see Strel#getMask()
		 */
		@Override
		public int[][] getMask() {
			int[][] mask = new int[3][];
			mask[0] = new int[]{  0, 255,   0};
			mask[1] = new int[]{255, 255, 255};
			mask[2] = new int[]{  0, 255,   0};
			return mask;
		}

		/* (non-Javadoc)
		 * @see Strel#getOffset()
		 */
		@Override
		public int[] getOffset() {
			return new int[]{2, 1};
		}

		/* (non-Javadoc)
		 * @see Strel#getShifts()
		 */
		@Override
		public int[][] getShifts() {
			int[][] shifts = new int[][] {
					{-1, -1}, 
					{-2,  0}, 
					{-1,  0}, 
					{ 0,  0}, 
					{-1, +1} };
			return shifts;
		}

		/**
		 * Returns this structuring element, as is is self-reverse.
		 * @see InPlaceStrel#reverse()
		 */
		@Override
		public InPlaceStrel reverse() {
			return RIGHT;
		}

		/* (non-Javadoc)
		 * @see InPlaceStrel#inPlaceDilation(ij.process.ImageProcessor)
		 */
		@Override
		public void inPlaceDilation(ImageProcessor image) {
			if (image instanceof ByteProcessor)
				inPlaceDilationGray8(image);
			else
				inPlaceDilationFloat(image);
		}
		
		private void inPlaceDilationGray8(ImageProcessor image) {
			// size of image
			int width = image.getWidth();
			int height = image.getHeight();

			int[][] buffer = new int[3][width];

			// init buffer with background and first two lines
			for (int x = 0; x < width; x++) {
				buffer[0][x] = Strel.BACKGROUND;
				buffer[1][x] = Strel.BACKGROUND;
				buffer[2][x] = image.get(x, 0);
			}

			// Iterate over image lines
			int valMax;
			for (int y = 0; y < height; y++) {
				fireProgressChanged(this, y, height);
				
				// permute lines in buffer
				int[] tmp = buffer[0];
				buffer[0] = buffer[1];
				buffer[1] = buffer[2];

				// initialize values of the last line in buffer 
				if (y < height - 1) {
					for (int x = 0; x < width; x++) 
						tmp[x] = image.get(x, y+1);
				} else {
					for (int x = 0; x < width; x++) 
						tmp[x] = Strel.BACKGROUND;
				}
				buffer[2] = tmp;

				// process first two pixels independently
				valMax = Math.max(buffer[1][0], Strel.BACKGROUND);
				image.set(0, y, valMax);
				valMax = max5(buffer[0][0], buffer[1][0], 
						buffer[1][1], buffer[2][0], Strel.BACKGROUND);
				image.set(1, y, valMax);

				// Iterate over pixel of the line, starting from the third one
				for (int x = 2; x < width; x++) {
					valMax = max5(buffer[0][x-1], buffer[1][x-2], buffer[1][x-1], 
							buffer[1][x], buffer[2][x-1]);
					image.set(x, y, valMax);
				}
			}
			
			// clear the progress bar
			fireProgressChanged(this, height, height);
		}

		private void inPlaceDilationFloat(ImageProcessor image) {
			// size of image
			int width = image.getWidth();
			int height = image.getHeight();

			float[][] buffer = new float[3][width];

			// init buffer with background and first two lines
			for (int x = 0; x < width; x++) {
				buffer[0][x] = Float.NEGATIVE_INFINITY;
				buffer[1][x] = Float.NEGATIVE_INFINITY;
				buffer[2][x] = image.getf(x, 0);
			}

			// Iterate over image lines
			float valMax;
			for (int y = 0; y < height; y++) {
				fireProgressChanged(this, y, height);
				
				// permute lines in buffer
				float[] tmp = buffer[0];
				buffer[0] = buffer[1];
				buffer[1] = buffer[2];

				// initialize values of the last line in buffer 
				if (y < height - 1) {
					for (int x = 0; x < width; x++) 
						tmp[x] = image.getf(x, y+1);
				} else {
					for (int x = 0; x < width; x++) 
						tmp[x] = Float.NEGATIVE_INFINITY;
				}
				buffer[2] = tmp;

				// process first two pixels independently
				valMax = Math.max(buffer[1][0], Float.NEGATIVE_INFINITY);
				image.setf(0, y, valMax);
				valMax = max5(buffer[0][0], buffer[1][0], 
						buffer[1][1], buffer[2][0], Float.NEGATIVE_INFINITY);
				image.setf(1, y, valMax);

				// Iterate over pixel of the line, starting from the third one
				for (int x = 2; x < width; x++) {
					valMax = max5(buffer[0][x-1], buffer[1][x-2], buffer[1][x-1], 
							buffer[1][x], buffer[2][x-1]);
					image.setf(x, y, valMax);
				}
			}
			
			// clear the progress bar
			fireProgressChanged(this, height, height);
		}

		/* (non-Javadoc)
		 * @see InPlaceStrel#inPlaceErosion(ij.process.ImageProcessor)
		 */
		@Override
		public void inPlaceErosion(ImageProcessor image) {
			if (image instanceof ByteProcessor)
				inPlaceErosionGray8(image);
			else
				inPlaceErosionFloat(image);
		}
		
		private void inPlaceErosionGray8(ImageProcessor image) {
			// size of image
			int width = image.getWidth();
			int height = image.getHeight();

			int[][] buffer = new int[3][width];

			// init buffer with background and first two lines
			for (int x = 0; x < width; x++) {
				buffer[0][x] = Strel.FOREGROUND;
				buffer[1][x] = Strel.FOREGROUND;
				buffer[2][x] = image.get(x, 0);
			}

			// Iterate over image lines
			int valMin;
			for (int y = 0; y < height; y++) {
				fireProgressChanged(this, y, height);
				
				// permute lines in buffer
				int[] tmp = buffer[0];
				buffer[0] = buffer[1];
				buffer[1] = buffer[2];

				// initialize values of the last line in buffer 
				if (y < height - 1) {
					for (int x = 0; x < width; x++) 
						tmp[x] = image.get(x, y+1);
				} else {
					for (int x = 0; x < width; x++) 
						tmp[x] = Strel.FOREGROUND;
				}
				buffer[2] = tmp;

				// process first pixel independently
				valMin = Math.min(buffer[1][0], Strel.FOREGROUND);
				image.set(0, y, valMin);
				valMin = min5(buffer[0][0], buffer[1][0], 
						buffer[1][1], buffer[2][0], Strel.FOREGROUND);
				image.set(1, y, valMin);

				// Iterate over pixel of the line
				for (int x = 2; x < width; x++) {
					valMin = min5(buffer[0][x-1], buffer[1][x-2], buffer[1][x-1], 
							buffer[1][x], buffer[2][x-1]);
					image.set(x, y, valMin);
				}
			}
			
			// clear the progress bar
			fireProgressChanged(this, height, height);
		}

		private void inPlaceErosionFloat(ImageProcessor image) {
			// size of image
			int width = image.getWidth();
			int height = image.getHeight();

			float[][] buffer = new float[3][width];

			// init buffer with background and first two lines
			for (int x = 0; x < width; x++) {
				buffer[0][x] = Float.POSITIVE_INFINITY;
				buffer[1][x] = Float.POSITIVE_INFINITY;
				buffer[2][x] = image.getf(x, 0);
			}

			// Iterate over image lines
			float valMin;
			for (int y = 0; y < height; y++) {
				fireProgressChanged(this, y, height);
				
				// permute lines in buffer
				float[] tmp = buffer[0];
				buffer[0] = buffer[1];
				buffer[1] = buffer[2];

				// initialize values of the last line in buffer 
				if (y < height - 1) {
					for (int x = 0; x < width; x++) 
						tmp[x] = image.getf(x, y+1);
				} else {
					for (int x = 0; x < width; x++) 
						tmp[x] = Float.POSITIVE_INFINITY;
				}
				buffer[2] = tmp;

				// process first pixel independently
				valMin = Math.min(buffer[1][0], Float.POSITIVE_INFINITY);
				image.setf(0, y, valMin);
				valMin = min5(buffer[0][0], buffer[1][0], 
						buffer[1][1], buffer[2][0], Float.POSITIVE_INFINITY);
				image.setf(1, y, valMin);

				// Iterate over pixel of the line
				for (int x = 2; x < width; x++) {
					valMin = min5(buffer[0][x-1], buffer[1][x-2], buffer[1][x-1], 
							buffer[1][x], buffer[2][x-1]);
					image.setf(x, y, valMin);
				}
			}
			
			// clear the progress bar
			fireProgressChanged(this, height, height);
		}
	}

	/**
	 * A cross-shaped structuring element located on the right of the
	 * reference pixel.</p>
	 * 
	 * The structuring has the following shape (x: neighbor, o: reference
	 * pixel, .: irrelevant): 
	 * <code><pre>
	 *  . . . . . 
	 *  . . x . . 
	 *  . o x x . 
	 *  . . x . . 
	 *  . . . . . 
	 * </pre></code>
	 */
	private final static class Right extends AbstractInPlaceStrel {
		
		/**
		 * Default constructor.
		 */
		public Right() {
		}

		/* (non-Javadoc)
		 * @see Strel#getSize()
		 */
		@Override
		public int[] getSize() {
			return new int[]{3, 3};
		}

		/* (non-Javadoc)
		 * @see Strel#getMask()
		 */
		@Override
		public int[][] getMask() {
			int[][] mask = new int[3][];
			mask[0] = new int[]{  0, 255,   0};
			mask[1] = new int[]{255, 255, 255};
			mask[2] = new int[]{  0, 255,   0};
			return mask;
		}

		/* (non-Javadoc)
		 * @see Strel#getOffset()
		 */
		@Override
		public int[] getOffset() {
			return new int[]{0, 1};
		}

		/* (non-Javadoc)
		 * @see Strel#getShifts()
		 */
		@Override
		public int[][] getShifts() {
			int[][] shifts = new int[][] {
					{+1, -1}, 
					{ 0,  0}, 
					{+1,  0}, 
					{+2,  0}, 
					{+1, +1} };
			return shifts;
		}

		/**
		 * Returns this structuring element, as is is self-reverse.
		 * @see InPlaceStrel#reverse()
		 */
		@Override
		public InPlaceStrel reverse() {
			return LEFT;
		}

		/* (non-Javadoc)
		 * @see InPlaceStrel#inPlaceDilation(ij.process.ImageProcessor)
		 */
		@Override
		public void inPlaceDilation(ImageProcessor image) {
			if (image instanceof ByteProcessor)
				inPlaceDilationGray8(image);
			else
				inPlaceDilationFloat(image);
		}
		
		private void inPlaceDilationGray8(ImageProcessor image) {
			// size of image
			int width = image.getWidth();
			int height = image.getHeight();

			int[][] buffer = new int[3][width];

			// init buffer with background and first two lines
			for (int x = 0; x < width; x++) {
				buffer[0][x] = Strel.BACKGROUND;
				buffer[1][x] = Strel.BACKGROUND;
				buffer[2][x] = image.get(x, 0);
			}

			// Iterate over image lines
			int valMax;
			for (int y = 0; y < height; y++) {
				fireProgressChanged(this, y, height);
				
				// permute lines in buffer
				int[] tmp = buffer[0];
				buffer[0] = buffer[1];
				buffer[1] = buffer[2];

				// initialize values of the last line in buffer 
				if (y < height - 1) {
					for (int x = 0; x < width; x++) 
						tmp[x] = image.get(x, y+1);
				} else {
					for (int x = 0; x < width; x++) 
						tmp[x] = Strel.BACKGROUND;
				}
				buffer[2] = tmp;

				// Iterate over pixels of the line
				for (int x = 0; x < width - 2; x++) {
					valMax = max5(buffer[0][x+1], buffer[1][x], buffer[1][x+1], 
							buffer[1][x+2], buffer[2][x+1]);
					image.set(x, y, valMax);
				}
				
				// process last two pixels independently
				valMax = max5(buffer[0][width-1], buffer[1][width-2], 
						buffer[1][width-1], buffer[2][width-1], Strel.BACKGROUND);
				image.set(width-2, y, valMax);
				valMax = Math.max(buffer[1][width-1], Strel.BACKGROUND);
				image.set(width-1, y, valMax);
			}
			
			// clear the progress bar
			fireProgressChanged(this, height, height);
		}

		private void inPlaceDilationFloat(ImageProcessor image) {
			// size of image
			int width = image.getWidth();
			int height = image.getHeight();

			float[][] buffer = new float[3][width];

			// init buffer with background and first two lines
			for (int x = 0; x < width; x++) {
				buffer[0][x] = Float.NEGATIVE_INFINITY;
				buffer[1][x] = Float.NEGATIVE_INFINITY;
				buffer[2][x] = image.getf(x, 0);
			}

			// Iterate over image lines
			float valMax;
			for (int y = 0; y < height; y++) {
				fireProgressChanged(this, y, height);

				// permute lines in buffer
				float[] tmp = buffer[0];
				buffer[0] = buffer[1];
				buffer[1] = buffer[2];

				// initialize values of the last line in buffer 
				if (y < height - 1) {
					for (int x = 0; x < width; x++) 
						tmp[x] = image.getf(x, y+1);
				} else {
					for (int x = 0; x < width; x++) 
						tmp[x] = Float.NEGATIVE_INFINITY;
				}
				buffer[2] = tmp;

				// Iterate over pixels of the line
				for (int x = 0; x < width - 2; x++) {
					valMax = max5(buffer[0][x+1], buffer[1][x], buffer[1][x+1], 
							buffer[1][x+2], buffer[2][x+1]);
					image.setf(x, y, valMax);
				}
				
				// process last two pixels independently
				valMax = max5(buffer[0][width-1], buffer[1][width-2], 
						buffer[1][width-1], buffer[2][width-1], Float.NEGATIVE_INFINITY);
				image.setf(width-2, y, valMax);
				valMax = Math.max(buffer[1][width-1], Float.NEGATIVE_INFINITY);
				image.setf(width-1, y, valMax);
			}
			
			// clear the progress bar
			fireProgressChanged(this, height, height);
		}

		/* (non-Javadoc)
		 * @see InPlaceStrel#inPlaceErosion(ij.process.ImageProcessor)
		 */
		@Override
		public void inPlaceErosion(ImageProcessor image) {
			if (image instanceof ByteProcessor)
				inPlaceErosionGray8(image);
			else
				inPlaceErosionFloat(image);
		}
		
		private void inPlaceErosionGray8(ImageProcessor image) {
			// size of image
			int width = image.getWidth();
			int height = image.getHeight();

			int[][] buffer = new int[3][width];

			// init buffer with background and first two lines
			for (int x = 0; x < width; x++) {
				buffer[0][x] = Strel.FOREGROUND;
				buffer[1][x] = Strel.FOREGROUND;
				buffer[2][x] = image.get(x, 0);
			}

			// Iterate over image lines
			int valMin;
			for (int y = 0; y < height; y++) {
				fireProgressChanged(this, y, height);
				
				// permute lines in buffer
				int[] tmp = buffer[0];
				buffer[0] = buffer[1];
				buffer[1] = buffer[2];

				// initialize values of the last line in buffer 
				if (y < height - 1) {
					for (int x = 0; x < width; x++) 
						tmp[x] = image.get(x, y+1);
				} else {
					for (int x = 0; x < width; x++) 
						tmp[x] = Strel.FOREGROUND;
				}
				buffer[2] = tmp;

				// Iterate over pixels of the line
				for (int x = 0; x < width - 2; x++) {
					valMin = min5(buffer[0][x+1], buffer[1][x], buffer[1][x+1], 
							buffer[1][x+2], buffer[2][x+1]);
					image.set(x, y, valMin);
				}

				// process last two pixels independently
				valMin = min5(buffer[0][width-1], buffer[1][width-2], 
						buffer[1][width-1], buffer[2][width-1], Strel.FOREGROUND);
				image.set(width-2, y, valMin);
				valMin = Math.min(buffer[1][width-1], Strel.FOREGROUND);
				image.set(width-1, y, valMin);
			}
			
			// clear the progress bar
			fireProgressChanged(this, height, height);
		}
		
		private void inPlaceErosionFloat(ImageProcessor image) {
			// size of image
			int width = image.getWidth();
			int height = image.getHeight();

			float[][] buffer = new float[3][width];

			// init buffer with background and first two lines
			for (int x = 0; x < width; x++) {
				buffer[0][x] = Float.POSITIVE_INFINITY;
				buffer[1][x] = Float.POSITIVE_INFINITY;
				buffer[2][x] = image.getf(x, 0);
			}

			// Iterate over image lines
			float valMin;
			for (int y = 0; y < height; y++) {
				fireProgressChanged(this, y, height);

				// permute lines in buffer
				float[] tmp = buffer[0];
				buffer[0] = buffer[1];
				buffer[1] = buffer[2];

				// initialize values of the last line in buffer 
				if (y < height - 1) {
					for (int x = 0; x < width; x++) 
						tmp[x] = image.getf(x, y+1);
				} else {
					for (int x = 0; x < width; x++) 
						tmp[x] = Float.POSITIVE_INFINITY;
				}
				buffer[2] = tmp;

				// Iterate over pixels of the line
				for (int x = 0; x < width - 2; x++) {
					valMin = min5(buffer[0][x+1], buffer[1][x], buffer[1][x+1], 
							buffer[1][x+2], buffer[2][x+1]);
					image.setf(x, y, valMin);
				}

				// process last two pixels independently
				valMin = min5(buffer[0][width-1], buffer[1][width-2], 
						buffer[1][width-1], buffer[2][width-1], Float.POSITIVE_INFINITY);
				image.setf(width-2, y, valMin);
				valMin = Math.min(buffer[1][width-1], Float.POSITIVE_INFINITY);
				image.setf(width-1, y, valMin);
			}
			
			// clear the progress bar
			fireProgressChanged(this, height, height);
		}

	}

	/**
	 * Makes default constructor private to avoid instantiation. 
	 */
	private ShiftedCross3x3Strel(){
	}
	
	/**
	 * Computes the minimum of the 5 values.
	 */
	private final static int min5(int v1, int v2, int v3, int v4, int v5) {
		int min1 = Math.min(v1, v2);
		int min2 = Math.min(v3, v4);
		min1 = Math.min(min1, v5);
		return Math.min(min1, min2);
	}

	/**
	 * Computes the minimum of the 5 float values.
	 */
	private final static float min5(float v1, float v2, float v3, float v4, float v5) {
		float min1 = Math.min(v1, v2);
		float min2 = Math.min(v3, v4);
		min1 = Math.min(min1, v5);
		return Math.min(min1, min2);
	}
	
	/**
	 * Computes the maximum of the 5 values.
	 */
	private final static int max5(int v1, int v2, int v3, int v4, int v5) {
		int max1 = Math.max(v1, v2);
		int max2 = Math.max(v3, v4);
		max1 = Math.max(max1, v5);
		return Math.max(max1, max2);
	}
	
	/**
	 * Computes the maximum of the 5 float values.
	 */
	private final static float max5(float v1, float v2, float v3, float v4, float v5) {
		float max1 = Math.max(v1, v2);
		float max2 = Math.max(v3, v4);
		max1 = Math.max(max1, v5);
		return Math.max(max1, max2);
	}
	
}
