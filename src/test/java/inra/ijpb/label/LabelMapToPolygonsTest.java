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
package inra.ijpb.label;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;

import org.junit.Test;

import ij.process.ByteProcessor;
import inra.ijpb.data.image.ImageUtils;
import inra.ijpb.geometry.Polygon2D;
import inra.ijpb.label.LabelMapToPolygons.Direction;
import inra.ijpb.label.LabelMapToPolygons.Position;

/**
 * @author dlegland
 *
 */
public class LabelMapToPolygonsTest
{
	@Test
	public final void test_Down_turnLeft()
	{
		Direction direction = Direction.DOWN;
		Position pos = new Position(1, 2, Direction.DOWN);

		Position pos2 = direction.turnLeft(pos);
		assertEquals(1, pos2.x);
		assertEquals(2, pos2.y);
		assertEquals(Direction.RIGHT, pos2.direction);
	}

	@Test
	public final void test_Down_turnLeft2()
	{
		Direction direction = Direction.DOWN;
		Position pos = new Position(1, 1, direction);

		Position pos2 = direction.turnLeft(pos);
		assertEquals(1, pos2.x);
		assertEquals(1, pos2.y);
		assertEquals(Direction.RIGHT, pos2.direction);
	}

	@Test
	public final void test_Down_forward()
	{
		Direction direction = Direction.DOWN;
		Position pos = new Position(1, 1, direction);

		Position pos2 = direction.forward(pos);
		assertEquals(1, pos2.x);
		assertEquals(2, pos2.y);
		assertEquals(Direction.DOWN, pos2.direction);
	}

	@Test
	public final void test_Down_turnRight()
	{
		Direction direction = Direction.DOWN;
		Position pos = new Position(2, 1, direction);

		Position pos2 = direction.turnRight(pos);
		assertEquals(1, pos2.x);
		assertEquals(2, pos2.y);
		assertEquals(Direction.LEFT, pos2.direction);
	}

	@Test
	public final void test_Down_turnRight2()
	{
		Direction direction = Direction.DOWN;
		Position pos = new Position(2, 1, direction);

		Position pos2 = direction.turnRight(pos);
		assertEquals(1, pos2.x);
		assertEquals(2, pos2.y);
		assertEquals(Direction.LEFT, pos2.direction);
	}

	/**
	 * Test method for
	 * {@link net.ijt.labels.LabelMapToPolygons#trackBoundary(ij.process.ByteProcessor, int, int, net.ijt.labels.LabelMapToPolygons.Direction)}.
	 */
	@Test
	public final void testTrackBoundaryBinary_singleSquare()
	{
		ByteProcessor array = new ByteProcessor(4, 4);
		array.set(1, 1, 255);
		array.set(2, 1, 255);
		array.set(1, 2, 255);
		array.set(2, 2, 255);

		int x0 = 1;
		int y0 = 1;
		LabelMapToPolygons.Direction initialDirection = Direction.DOWN;

		LabelMapToPolygons tracker = new LabelMapToPolygons();
		ArrayList<Point2D> vertices = tracker.trackBoundary(array, x0, y0, initialDirection);

		assertFalse(vertices.isEmpty());
		assertEquals(8, vertices.size());
	}

	/**
	 * Test method for
	 * {@link net.ijt.labels.LabelMapToPolygons#trackBoundary(ij.process.ByteProcessor, int, int, net.ijt.labels.LabelMapToPolygons.Direction)}.
	 */
	@Test
	public final void testTrackBoundaryBinary_ExpandedCorners_C4()
	{
		ByteProcessor array = new ByteProcessor(8, 8);
		ImageUtils.fillRect(array, 2, 2, 4, 4, 255);
		ImageUtils.fillRect(array, 1, 1, 2, 2, 255);
		ImageUtils.fillRect(array, 5, 1, 2, 2, 255);
		ImageUtils.fillRect(array, 1, 5, 2, 2, 255);
		ImageUtils.fillRect(array, 5, 5, 2, 2, 255);
		// ImageUtils.print(array);

		int x0 = 1;
		int y0 = 1;
		LabelMapToPolygons.Direction initialDirection = Direction.DOWN;

		LabelMapToPolygons tracker = new LabelMapToPolygons();
		ArrayList<Point2D> vertices = tracker.trackBoundary(array, x0, y0, initialDirection);

		assertFalse(vertices.isEmpty());
		assertEquals(32, vertices.size());
	}

	/**
	 * Test method for
	 * {@link net.ijt.labels.LabelMapToPolygons#trackBoundary(ij.process.ByteProcessor, int, int, net.ijt.labels.LabelMapToPolygons.Direction)}.
	 */
	@Test
	public final void testTrackBoundaryBinary_ExpandedCorners_C4_TouchBorders()
	{
		ByteProcessor array = new ByteProcessor(6, 6);
		ImageUtils.fillRect(array, 1, 1, 4, 4, 255);
		ImageUtils.fillRect(array, 0, 0, 2, 2, 255);
		ImageUtils.fillRect(array, 4, 0, 2, 2, 255);
		ImageUtils.fillRect(array, 0, 4, 2, 2, 255);
		ImageUtils.fillRect(array, 4, 4, 2, 2, 255);
		// ImageUtils.print(array);

		int x0 = 0;
		int y0 = 0;
		LabelMapToPolygons.Direction initialDirection = Direction.DOWN;

		LabelMapToPolygons tracker = new LabelMapToPolygons();
		ArrayList<Point2D> vertices = tracker.trackBoundary(array, x0, y0, initialDirection);

		assertFalse(vertices.isEmpty());
		assertEquals(32, vertices.size());
	}

	/**
	 * Test method for
	 * {@link net.ijt.labels.LabelMapToPolygons#trackBoundary(ij.process.ByteProcessor, int, int, net.ijt.labels.LabelMapToPolygons.Direction)}.
	 */
	@Test
	public final void testTrackBoundaryBinary_ExpandedCorners_C8()
	{
		ByteProcessor array = new ByteProcessor(8, 8);
		ImageUtils.fillRect(array, 2, 2, 4, 4, 255);
		ImageUtils.fillRect(array, 1, 1, 2, 2, 255);
		ImageUtils.fillRect(array, 5, 1, 2, 2, 255);
		ImageUtils.fillRect(array, 1, 5, 2, 2, 255);
		ImageUtils.fillRect(array, 5, 5, 2, 2, 255);
		// ImageUtils.print(array);

		int x0 = 1;
		int y0 = 1;
		LabelMapToPolygons.Direction initialDirection = Direction.DOWN;

		LabelMapToPolygons tracker = new LabelMapToPolygons(8);
		ArrayList<Point2D> vertices = tracker.trackBoundary(array, x0, y0, initialDirection);

		assertFalse(vertices.isEmpty());
		assertEquals(32, vertices.size());
	}

	/**
	 * Test method for
	 * {@link net.ijt.labels.LabelMapToPolygons#trackBoundary(ij.process.ByteProcessor, int, int, net.ijt.labels.LabelMapToPolygons.Direction)}.
	 */
	@Test
	public final void testTrackBoundaryBinary_ExpandedCorners_C8_TouchBorders()
	{
		ByteProcessor array = new ByteProcessor(6, 6);
		ImageUtils.fillRect(array, 1, 1, 4, 4, 255);
		ImageUtils.fillRect(array, 0, 0, 2, 2, 255);
		ImageUtils.fillRect(array, 4, 0, 2, 2, 255);
		ImageUtils.fillRect(array, 0, 4, 2, 2, 255);
		ImageUtils.fillRect(array, 4, 4, 2, 2, 255);
		// ImageUtils.print(array);

		int x0 = 0;
		int y0 = 0;
		LabelMapToPolygons.Direction initialDirection = Direction.DOWN;

		LabelMapToPolygons tracker = new LabelMapToPolygons(8);
		ArrayList<Point2D> vertices = tracker.trackBoundary(array, x0, y0, initialDirection);

		assertFalse(vertices.isEmpty());
		assertEquals(32, vertices.size());
	}

	/**
	 * Test method for
	 * {@link net.ijt.labels.LabelMapToPolygons#trackBoundary(ij.process.ByteProcessor, int, int, net.ijt.labels.LabelMapToPolygons.Direction)}.
	 */
	@Test
	public final void testTrackBoundary_NestedLabels()
	{
		ByteProcessor array = new ByteProcessor(6, 6);
		ImageUtils.fillRect(array, 1, 1, 2, 2, 3);
		ImageUtils.fillRect(array, 3, 1, 2, 2, 5);
		ImageUtils.fillRect(array, 1, 3, 2, 2, 7);
		ImageUtils.fillRect(array, 3, 3, 2, 2, 9);
		ImageUtils.fillRect(array, 2, 2, 2, 2, 4);
		// ImageUtils.print(array);

		int x0 = 2;
		int y0 = 2;

		LabelMapToPolygons tracker = new LabelMapToPolygons(4);
		ArrayList<Point2D> vertices = tracker.trackBoundary(array, x0, y0, Direction.DOWN);

		assertFalse(vertices.isEmpty());
		assertEquals(8, vertices.size());
	}

	/**
	 * Test method for
	 * {@link net.ijt.labels.LabelMapToPolygons#trackBoundary(ij.process.ByteProcessor, int, int, net.ijt.labels.LabelMapToPolygons.Direction)}.
	 */
	@Test
	public final void test_process_square2x2()
	{
		ByteProcessor array = new ByteProcessor(4, 4);
		ImageUtils.fillRect(array, 1, 1, 2, 2, 3);
		// ImageUtils.print(array);

		LabelMapToPolygons tracker = new LabelMapToPolygons(4);
		Map<Integer, ArrayList<Polygon2D>> boundaries = tracker.process(array);

		assertFalse(boundaries.isEmpty());
		assertEquals(1, boundaries.size());

		Polygon2D poly3 = boundaries.get(3).get(0);
		assertEquals(8, poly3.vertexNumber());
	}

	/**
	 * Test method for
	 * {@link net.ijt.labels.LabelMapToPolygons#trackBoundary(ij.process.ByteProcessor, int, int, net.ijt.labels.LabelMapToPolygons.Direction)}.
	 */
	@Test
	public final void test_process_FourLabels()
	{
		ByteProcessor array = new ByteProcessor(6, 6);
		ImageUtils.fillRect(array, 1, 1, 2, 2, 3);
		ImageUtils.fillRect(array, 3, 1, 2, 2, 5);
		ImageUtils.fillRect(array, 1, 3, 2, 2, 7);
		ImageUtils.fillRect(array, 3, 3, 2, 2, 9);
		// ImageUtils.print(array);

		LabelMapToPolygons tracker = new LabelMapToPolygons(4);
		Map<Integer, ArrayList<Polygon2D>> boundaries = tracker.process(array);

		assertFalse(boundaries.isEmpty());
		assertEquals(4, boundaries.size());

		Polygon2D poly3 = boundaries.get(3).get(0);
		assertEquals(8, poly3.vertexNumber());
		Polygon2D poly5 = boundaries.get(5).get(0);
		assertEquals(8, poly5.vertexNumber());
		Polygon2D poly7 = boundaries.get(7).get(0);
		assertEquals(8, poly7.vertexNumber());
		Polygon2D poly9 = boundaries.get(9).get(0);
		assertEquals(8, poly9.vertexNumber());
	}

	/**
	 * Test method for
	 * {@link net.ijt.labels.LabelMapToPolygons#trackBoundary(ij.process.ByteProcessor, int, int, net.ijt.labels.LabelMapToPolygons.Direction)}.
	 */
	@Test
	public final void test_process_squareWithHole()
	{
		ByteProcessor array = new ByteProcessor(5, 5);
		ImageUtils.fillRect(array, 1, 1, 3, 3, 255);
		array.set(2, 2, 0);

		LabelMapToPolygons tracker = new LabelMapToPolygons(4);
		Map<Integer, ArrayList<Polygon2D>> boundaries = tracker.process(array);

		assertFalse(boundaries.isEmpty());
		assertEquals(1, boundaries.size());

		ArrayList<Polygon2D> polygons = boundaries.get(255);
		assertEquals(2, polygons.size());

		assertEquals(12, polygons.get(0).vertexNumber());
		assertEquals(4, polygons.get(1).vertexNumber());
	}
	
	/**
	 * Test method for
	 * {@link net.ijt.labels.LabelMapToPolygons#trackBoundary(ij.process.ByteProcessor, int, int, net.ijt.labels.LabelMapToPolygons.Direction)}.
	 */
	@Test
	public final void test_process_fiveSquares_C4()
	{
		ByteProcessor array = new ByteProcessor(8, 8);
		ImageUtils.fillRect(array, 1, 1, 2, 2, 255);
		ImageUtils.fillRect(array, 3, 3, 2, 2, 255);
		ImageUtils.fillRect(array, 5, 1, 2, 2, 255);
		ImageUtils.fillRect(array, 1, 5, 2, 2, 255);
		ImageUtils.fillRect(array, 5, 5, 2, 2, 255);

		LabelMapToPolygons tracker = new LabelMapToPolygons(4);
		Map<Integer, ArrayList<Polygon2D>> boundaries = tracker.process(array);

		assertFalse(boundaries.isEmpty());
		assertEquals(1, boundaries.size());

		ArrayList<Polygon2D> polygons = boundaries.get(255);
		assertEquals(5, polygons.size());
	}
	
	/**
	 * Test method for
	 * {@link net.ijt.labels.LabelMapToPolygons#trackBoundary(ij.process.ByteProcessor, int, int, net.ijt.labels.LabelMapToPolygons.Direction)}.
	 */
	@Test
	public final void test_process_fiveSquares_C8()
	{
		ByteProcessor array = new ByteProcessor(8, 8);
		ImageUtils.fillRect(array, 1, 1, 2, 2, 255);
		ImageUtils.fillRect(array, 3, 3, 2, 2, 255);
		ImageUtils.fillRect(array, 5, 1, 2, 2, 255);
		ImageUtils.fillRect(array, 1, 5, 2, 2, 255);
		ImageUtils.fillRect(array, 5, 5, 2, 2, 255);

		LabelMapToPolygons tracker = new LabelMapToPolygons(8);
		Map<Integer, ArrayList<Polygon2D>> boundaries = tracker.process(array);

		assertFalse(boundaries.isEmpty());
		assertEquals(1, boundaries.size());

		ArrayList<Polygon2D> polygons = boundaries.get(255);
		assertEquals(1, polygons.size());
	}
}
