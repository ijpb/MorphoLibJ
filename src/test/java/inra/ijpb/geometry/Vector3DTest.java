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
package inra.ijpb.geometry;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author dlegland
 *
 */
public class Vector3DTest {

	/**
	 * Test method for {@link inra.ijpb.measure.Vector3D#crossProduct(inra.ijpb.measure.Vector3D, inra.ijpb.measure.Vector3D)}.
	 */
	@Test
	public void testCrossProduct() {
		Vector3D v1 = new Vector3D(2, 0, 0);
		Vector3D v2 = new Vector3D(0, 3.5, 0);
		
		Vector3D exp = new Vector3D(0, 0, 7);
		Vector3D res = Vector3D.crossProduct(v1, v2);
		assertEquals(exp.getX(), res.getX(), .01);
		assertEquals(exp.getY(), res.getY(), .01);
		assertEquals(exp.getZ(), res.getZ(), .01);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.Vector3D#dotProduct(inra.ijpb.measure.Vector3D, inra.ijpb.measure.Vector3D)}.
	 */
	@Test
	public void testDotProduct() {
		Vector3D v1 = new Vector3D(0, 1, 0);
		Vector3D v2 = new Vector3D(0, 2.5, 3);
		
		double res = Vector3D.dotProduct(v1, v2);
		assertEquals(2.5, res, .01);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.Vector3D#angle(inra.ijpb.measure.Vector3D, inra.ijpb.measure.Vector3D)}.
	 */
	@Test
	public void testAngle_01() {
		Vector3D e1 = new Vector3D(1, 0, 0);
		Vector3D e11 = new Vector3D(1, 1, 0);
		
		double angle = Vector3D.angle(e1, e11);
		
		assertEquals(Math.PI / 4, angle, .01);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.Vector3D#angle(inra.ijpb.measure.Vector3D, inra.ijpb.measure.Vector3D)}.
	 */
	@Test
	public void testAngle_02() {
		Vector3D e11 = new Vector3D(1, 1, 0);
		Vector3D e111 = new Vector3D(1, 1, 1);
		
		double angle = Vector3D.angle(e11, e111);
		
		double exp = Math.atan2(1, Math.sqrt(2));
		assertEquals(exp, angle, .01);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.Vector3D#plus(inra.ijpb.measure.Vector3D)}.
	 */
	@Test
	public void testPlus() {
		Vector3D v1 = new Vector3D(2, 3, 4);
		Vector3D v2 = new Vector3D(3, 4, 5);
		
		Vector3D exp = new Vector3D(5, 7, 9);
		
		Vector3D res = v1.plus(v2);
		assertTrue(exp.almostEquals(res, .01));
	}

	/**
	 * Test method for {@link inra.ijpb.measure.Vector3D#minus(inra.ijpb.measure.Vector3D)}.
	 */
	@Test
	public void testMinus() {
		Vector3D v1 = new Vector3D(2, 3, 4);
		Vector3D v2 = new Vector3D(1, 1, 1);
		
		Vector3D res = v1.minus(v2);
		
		Vector3D exp = new Vector3D(1, 2, 3);
		assertTrue(exp.almostEquals(res, .01));
	}

	/**
	 * Test method for {@link inra.ijpb.measure.Vector3D#times(double)}.
	 */
	@Test
	public void testTimes() {
		Vector3D v1 = new Vector3D(2, 3, 4);
		
		Vector3D res = v1.times(2.5);
		
		Vector3D exp = new Vector3D(5, 7.5, 10);
		assertTrue(exp.almostEquals(res, .01));
	}

	/**
	 * Test method for {@link inra.ijpb.measure.Vector3D#normalize()}.
	 */
	@Test
	public void testNormalize() {
		Vector3D vx = new Vector3D(3.2, 4.3, 5.4);
		assertEquals(1, vx.normalize().getNorm(), 1e-10);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.Vector3D#getNorm()}.
	 */
	@Test
	public void testGetNorm() {
		Vector3D v = new Vector3D(2, 3, 6);
		double norm = v.getNorm();
		assertEquals(7, norm, .1);
		
		v = v.times(.1);
		norm = v.getNorm();
		assertEquals(.7, norm, .1);
	}

}
