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
/**
 * 
 */
package inra.ijpb.label;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.geometry.Polygon2D;

/**
 * Converts a label map into a collection of polygons.
 * 
 * As regions may be disconnected and/or may contain hole(s), the resulting
 * polygons are not necessarily connected.
 * 
 * @author dlegland
 *
 */
public class LabelMapToPolygons
{
	/**
	 * The connectivity to use for tracking boundary. Should be either 4 or 8.
	 * Default is 4.
	 */
	int conn = 4;

	/**
	 * Defines where the polygon vertices are located according to position of
	 * current pixel.
	 */
	VertexLocation vertexLocation = VertexLocation.EDGE_CENTER;

	/**
	 * Enumeration of the different directions that be considered during
	 * processing.
	 * 
	 * For each direction, different methods allow for iterating between
	 * successive positions depending on the direction.
	 */
	public enum Direction
	{
		/**
		 * Iterates to the right, by incrementing the x-coordinate without
		 * modifying the y-coordinate.
		 */
		RIGHT
		{
			@Override
			public int[][] coordsShifts()
			{
				return new int[][] { { 1, 0 }, { 1, 1 } };
			}

			@Override
			public Point getVertex(Position pos)
			{
				return new Point(pos.x, pos.y + 1);
			}

			@Override
			public Position turnLeft(Position pos)
			{
				return new Position(pos.x, pos.y, UP);
			}

			@Override
			public Position forward(Position pos)
			{
				return new Position(pos.x + 1, pos.y, RIGHT);
			}

			@Override
			public Position turnRight(Position pos)
			{
				return new Position(pos.x + 1, pos.y + 1, DOWN);
			}
		},

		/**
		 * Iterates in the upward direction, by decrementing the y-coordinate without
		 * modifying the x-coordinate.
		 */
		UP
		{
			@Override
			public int[][] coordsShifts()
			{
				return new int[][] { { 0, -1 }, { 1, -1 } };
			}

			@Override
			public Point getVertex(Position pos)
			{
				return new Point(pos.x + 1, pos.y + 1);
			}

			@Override
			public Position turnLeft(Position pos)
			{
				return new Position(pos.x, pos.y, LEFT);
			}

			@Override
			public Position forward(Position pos)
			{
				return new Position(pos.x, pos.y - 1, UP);
			}

			@Override
			public Position turnRight(Position pos)
			{
				return new Position(pos.x + 1, pos.y - 1, RIGHT);
			}
		},

		/**
		 * Iterates to the left, by decrementing the x-coordinate without
		 * modifying the y-coordinate.
		 */
		LEFT
		{
			@Override
			public int[][] coordsShifts()
			{
				return new int[][] { { -1, 0 }, { -1, -1 } };
			}

			@Override
			public Point getVertex(Position pos)
			{
				return new Point(pos.x + 1, pos.y);
			}

			@Override
			public Position turnLeft(Position pos)
			{
				return new Position(pos.x, pos.y, DOWN);
			}

			@Override
			public Position forward(Position pos)
			{
				return new Position(pos.x - 1, pos.y, LEFT);
			}

			@Override
			public Position turnRight(Position pos)
			{
				return new Position(pos.x - 1, pos.y - 1, UP);
			}
		},

		/**
		 * Iterates in the downward direction, by incrementing the y-coordinate without
		 * modifying the x-coordinate.
		 */
		DOWN
		{
			@Override
			public int[][] coordsShifts()
			{
				return new int[][] { { 0, +1 }, { -1, 1 } };
			}

			@Override
			public Point getVertex(Position pos)
			{
				return new Point(pos.x, pos.y);
			}

			@Override
			public Position turnLeft(Position pos)
			{
				return new Position(pos.x, pos.y, RIGHT);
			}

			@Override
			public Position forward(Position pos)
			{
				return new Position(pos.x, pos.y + 1, DOWN);
			}

			@Override
			public Position turnRight(Position pos)
			{
				return new Position(pos.x - 1, pos.y + 1, LEFT);
			}
		};

		/**
		 * Returns a 2-by-2 array corresponding to a pair of coordinates shifts,
		 * that will be used to access coordinates of next pixels within
		 * configuration.
		 * 
		 * The first coordinates will be the pixel in the continuation of the
		 * current direction. The second coordinate will be the pixel in the
		 * opposite current 2-by-2 configuration.
		 * 
		 * @return a 2-by-2 array corresponding to a pair of coordinates shifts.
		 */
		public abstract int[][] coordsShifts();

		/**
		 * Retrieves the position of the vertex associated to the
		 * (position,direction) pair specified by the <code>pos</code> argument.
		 * 
		 * @param pos
		 *            the position of the current pixel and the direction of
		 *            tracking
		 * @return the coordinates to the vertex
		 */
		public abstract Point getVertex(Position pos);

		/**
		 * Keeps current reference pixel and turn direction by +90 degrees in
		 * counter-clockwise direction.
		 * 
		 * @param pos
		 *            the position to update
		 * @return the new position
		 */
		public abstract Position turnLeft(Position pos);

		/**
		 * Updates the specified position by iterating by one step in the
		 * current direction.
		 * 
		 * @param pos
		 *            the position to update
		 * @return the new position
		 */
		public abstract Position forward(Position pos);

		/**
		 * Keeps current reference pixel and turn direction by -90 degrees in
		 * counter-clockwise direction.
		 * 
		 * @param pos
		 *            the position to update
		 * @return the new position
		 */
		public abstract Position turnRight(Position pos);
	}
	
	/**
	 * Encapsulates a 2D (integer) position and a (isothetic) direction. The
	 * coordinates of the position correspond to those of the current pixel. The
	 * current direction has to be chosen among the four main directions.
	 * 
	 * @see Direction
	 */
	public static final class Position
	{
		int x;
		int y;
		Direction direction;

		/**
		 * Creates a new <code>Position</code> from two coordinates, and a
		 * direction.
		 * 
		 * @param x
		 *            the x-coordiante of the position
		 * @param y
		 *            the y-coordiante of the position
		 * @param direction
		 *            the direction this position is pointing to.
		 */
		Position(int x, int y, Direction direction)
		{
			this.x = x;
			this.y = y;
			this.direction = direction;
		}

		/**
		 * Retrieves the position of a vertex associated to the current pixel,
		 * depending on both the type of vertex (corner, pixel, or edge center),
		 * and the current direction of this position.
		 * 
		 * @param vertex
		 *            the type of vertex to retrieve
		 * @return the position a vertex associated to the current pixel.
		 */
		public Point2D getVertex(VertexLocation vertex)
		{
			switch (vertex)
			{
				case CORNER:
					return this.direction.getVertex(this);
				case EDGE_CENTER:
					switch (direction)
					{
						case DOWN:
							return new Point2D.Double(this.x, this.y + 0.5);
						case UP:
							return new Point2D.Double(this.x + 1.0, this.y + 0.5);
						case LEFT:
							return new Point2D.Double(this.x + 0.5, this.y);
						case RIGHT:
							return new Point2D.Double(this.x + 0.5, this.y + 1.0);
					}
				case PIXEL:
					return new Point2D.Double(this.x + 0.5, this.y + 0.5);
				default:
					throw new IllegalArgumentException("Unexpected value: " + vertex);
			}
		}

		@Override
		public boolean equals(Object obj)
		{
			// check class
			if (!(obj instanceof Position)) return false;
			Position that = (Position) obj;

			// check each class member
			if (this.x != that.x) return false;
			if (this.y != that.y) return false;
			if (this.direction != that.direction) return false;

			// return true when all tests checked
			return true;
		}
	}

	/**
	 * The different locations of polygon vertices with respect to current
	 * pixel.
	 */
	public enum VertexLocation
	{
		/** The polygon surrounds completely the square areas of the pixels. */
		CORNER,
		/**
		 * The polygon follows the pixel sides, but joining the centers of pixel
		 * edges instead of their extremities. This results in a more "smooth"
		 * polygon.
		 */
		EDGE_CENTER,
		/**
		 * The polygon joins the centers of the pixels, resulting in a very
		 * tight polygon.
		 */
		PIXEL;
	}

	/**
	 * Default empty constructor, using Connectivity 4.
	 */
	public LabelMapToPolygons()
	{
	}

	/**
	 * Constructor that allows to specify connectivity.
	 * 
	 * @param conn
	 *            the connectivity to use (must be either 4 or 8)
	 */
	public LabelMapToPolygons(int conn)
	{
		if (conn != 4 && conn != 8)
		{ throw new IllegalArgumentException("Connectivity must be either 4 or 8"); }
		this.conn = conn;
	}

	/**
	 * Constructor that allows to specify connectivity and location of vertices.
	 * 
	 * @param conn
	 *            the connectivity to use (must be either 4 or 8)
	 * @param loc
	 *            the location of boundary vertices with respect to the pixels.
	 */
	public LabelMapToPolygons(int conn, VertexLocation loc)
	{
		if (conn != 4 && conn != 8)
		{ throw new IllegalArgumentException("Connectivity must be either 4 or 8"); }
		this.conn = conn;
		this.vertexLocation = loc;
	}

	/**
	 * Tracks the boundary that starts at the current position by iterating on
	 * successive neighbor positions, and returns the set of boundary points.
	 * 
	 * The positions are defined by two coordinates and a direction. The initial
	 * position must correspond to a transition into a region, and the resulting
	 * boundary will surround this region.
	 * 
	 * @param array
	 *            the array containing binary or label representing the
	 *            region(s)
	 * @param x0
	 *            the x-coordinate of the start position
	 * @param y0
	 *            the y-coordinate of the start position
	 * @param initialDirection
	 *            the direction of the start position
	 * @return the list of points that form the boundary starting at specified
	 *         position
	 */
	public ArrayList<Point2D> trackBoundary(ImageProcessor array, int x0, int y0, Direction initialDirection)
	{
		// retrieve image size
		int sizeX = array.getWidth();
		int sizeY = array.getHeight();

		// initialize result array
		ArrayList<Point2D> vertices = new ArrayList<Point2D>();

		// initialize tracking algo state
		int value = (int) array.getf(x0, y0);
		Position pos0 = new Position(x0, y0, initialDirection);
		Position pos = new Position(x0, y0, initialDirection);

		// iterate over boundary until we come back at initial position
		do
		{
			vertices.add(pos.getVertex(vertexLocation));

			// compute position of the two other points in current 2-by-2
			// configuration
			int[][] shifts = pos.direction.coordsShifts();
			// the pixel in the continuation of current direction
			int xn = pos.x + shifts[0][0];
			int yn = pos.y + shifts[0][1];
			// the pixel in the diagonal position within current configuration
			int xd = pos.x + shifts[1][0];
			int yd = pos.y + shifts[1][1];

			// determine configuration of the two pixels in current direction
			// initialize with false, to manage the case of configuration on the
			// border. In any cases, assume that reference pixel in current
			// position belongs to the array.
			boolean b0 = false;
			if (xn >= 0 && xn < sizeX && yn >= 0 && yn < sizeY)
			{
				b0 = ((int) array.getf(xn, yn)) == value;
			}
			boolean b1 = false;
			if (xd >= 0 && xd < sizeX && yd >= 0 && yd < sizeY)
			{
				b1 = ((int) array.getf(xd, yd)) == value;
			}

			// Depending on the values of the two other pixels in configuration,
			// update the current position
			if (!b0 && (!b1 || conn == 4))
			{
				// corner configuration -> +90 direction
				pos = pos.direction.turnLeft(pos);
			}
			else if (b1 && (b0 || conn == 8))
			{
				// reentrant corner configuration -> -90 direction
				pos = pos.direction.turnRight(pos);
			}
			else if (b0 && !b1)
			{
				// straight border configuration -> same direction
				pos = pos.direction.forward(pos);
			}
			else
			{
				throw new RuntimeException("Should not reach this part...");
			}
		} while (!pos0.equals(pos));

		return vertices;
	}

	/**
	 * Computes region boundaries from a label map and returns the result as a
	 * Map from region label to polygon.
	 * 
	 * @param array
	 *            the label map to process
	 * @return an associative array between the region label (as integer) and
	 *         the region boundary (as polygon)
	 */
	public Map<Integer, ArrayList<Polygon2D>> process(ImageProcessor array)
	{
		// retrieve image size
		int sizeX = array.getWidth();
		int sizeY = array.getHeight();

		ByteProcessor maskArray = new ByteProcessor(sizeX, sizeY);

		Map<Integer, ArrayList<Polygon2D>> boundaries = new HashMap<>();

		// iterate over all image pixels
		for (int y = 0; y < sizeY; y++)
		{
			int currentLabel = 0;

			for (int x = 0; x < sizeX; x++)
			{
				int label = (int) array.getf(x, y);

				// first check if this is a transition between two labels
				if (label == currentLabel)
				{
					continue;
				}
				currentLabel = label;

				// do not process background values
				if (label == 0)
				{
					continue;
				}
				// if the boundary was already tracked, no need to work again
				if ((maskArray.get(x, y) & 0x08) > 0)
				{
					continue;
				}

				// ok, we are at a transition that can be used to initialize a
				// new boundary
				// -> track the boundary, and convert to polygon object
				ArrayList<Point2D> vertices = trackBoundary(array, maskArray, x, y, Direction.DOWN);
				Polygon2D poly = Polygon2D.create(vertices);

				// update map from labels to array of polygons
				ArrayList<Polygon2D> polygons = boundaries.get(label);
				if (polygons == null)
				{
					polygons = new ArrayList<Polygon2D>(4);
				}
				polygons.add(poly);
				boundaries.put(label, polygons);
			}
		}

		return boundaries;
	}

	/**
	 * Tracks the boundary that starts at the current position by iterating on
	 * successive neighbor positions, and returns the set of boundary points.
	 * 
	 * The positions are defined by two coordinates and a direction. The initial
	 * position must correspond to a transition into a region, and the resulting
	 * boundary will surround this region.
	 * 
	 * @param array
	 *            the array containing binary or label representing the
	 *            region(s)
	 * @param maskArray
	 *            an array the same size as <code>array</code> containing a
	 *            4-bits values that indicates which directions of current pixel
	 *            have been visited.
	 * @param x0
	 *            the x-coordinate of the start position
	 * @param y0
	 *            the y-coordinate of the start position
	 * @param initialDirection
	 *            the direction of the start position
	 * @return the list of points that form the boundary starting at specified
	 *         position
	 */
	private ArrayList<Point2D> trackBoundary(ImageProcessor array, ImageProcessor maskArray, int x0, int y0,
			Direction initialDirection)
	{
		// retrieve image size
		int sizeX = array.getWidth();
		int sizeY = array.getHeight();

		// initialize result array
		ArrayList<Point2D> vertices = new ArrayList<Point2D>();

		// initialize tracking algo state
		int value = (int) array.getf(x0, y0);
		Position pos0 = new Position(x0, y0, initialDirection);
		Position pos = new Position(x0, y0, initialDirection);

		// iterate over boundary until we come back at initial position
		do
		{
			// update vertices
			vertices.add(pos.getVertex(vertexLocation));

			// mark the current pixel with integer that depends on position
			updateMaskArray(maskArray, pos);

			// compute position of the two other points in current 2-by-2
			// configuration
			int[][] shifts = pos.direction.coordsShifts();
			// the pixel in the continuation of current direction
			int xn = pos.x + shifts[0][0];
			int yn = pos.y + shifts[0][1];
			// the pixel in the diagonal position within current configuration
			int xd = pos.x + shifts[1][0];
			int yd = pos.y + shifts[1][1];

			// determine configuration of the two pixels in current direction
			// initialize with false, to manage the case of configuration on the
			// border. In any cases, assume that reference pixel in current
			// position belongs to the array.
			boolean b0 = false;
			if (xn >= 0 && xn < sizeX && yn >= 0 && yn < sizeY)
			{
				b0 = ((int) array.getf(xn, yn)) == value;
			}
			boolean b1 = false;
			if (xd >= 0 && xd < sizeX && yd >= 0 && yd < sizeY)
			{
				b1 = ((int) array.getf(xd, yd)) == value;
			}

			// Depending on the values of the two other pixels in configuration,
			// update the current position
			if (!b0 && (!b1 || conn == 4))
			{
				// corner configuration -> +90 direction
				pos = pos.direction.turnLeft(pos);
			}
			else if (b1 && (b0 || conn == 8))
			{
				// reentrant corner configuration -> -90 direction
				pos = pos.direction.turnRight(pos);
			}
			else if (b0 && !b1)
			{
				// straight border configuration -> same direction
				pos = pos.direction.forward(pos);
			}
			else
			{
				throw new RuntimeException("Should not reach this part...");
			}
		} while (!pos0.equals(pos));

		return vertices;
	}
	
	/**
	 * Updates, within the mask array, the pixel identified with the position,
	 * by adding a marker that depends on the direction of the position.
	 * 
	 * @param maskArray
	 *            the array of markers.
	 * @param pos
	 *            the data structure containing position of current pixel and
	 *            current direction
	 */
	private static final void updateMaskArray(ImageProcessor maskArray, Position pos)
	{
		// mark the current pixel with integer that depends on position
		int mask = maskArray.get(pos.x, pos.y);
		switch (pos.direction)
		{
			case RIGHT:
				mask = mask | 0x01;
				break;
			case UP:
				mask = mask | 0x02;
				break;
			case LEFT:
				mask = mask | 0x04;
				break;
			case DOWN:
				mask = mask | 0x08;
				break;
		}
		maskArray.set(pos.x, pos.y, mask);
	}
}
