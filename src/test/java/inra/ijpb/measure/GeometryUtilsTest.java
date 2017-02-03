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
package inra.ijpb.measure;

import static org.junit.Assert.*;

import org.junit.Test;

public class GeometryUtilsTest {

	/**
	 * Check for direction (1, 0, 0).
	 */
	@Test
	public void testSphericalVoronoiDomainArea_D13_Poo() {
		Vector3d germ = new Vector3d(1, 0, 0);
		Vector3d[] neighbors = new Vector3d[] { 
				new Vector3d(+1, -1, -1),
				new Vector3d(+1, -1,  0),
				new Vector3d(+1, -1, +1),
				new Vector3d(+1,  0, +1),
				new Vector3d(+1, +1, +1),
				new Vector3d(+1, +1,  0),
				new Vector3d(+1, +1, -1),
				new Vector3d(+1,  0, -1),
		};
		
		double area = GeometryUtils.sphericalVoronoiDomainArea(germ, neighbors);
		double exp = 0.04577789120476 * Math.PI * 4;
		assertEquals(exp, area, 1e-10);
	}

	/**
	 * Check for direction (1, 1, 0).
	 */
	@Test
	public void testSphericalVoronoiDomainArea_D13_PPo() {
		Vector3d germ = new Vector3d(1, 1, 0);
		Vector3d[] neighbors = new Vector3d[] { 
				new Vector3d(+1,  0,  0),
				new Vector3d(+1, +1, +1),
				new Vector3d( 0, +1,  0),
				new Vector3d(+1, +1, -1),
		};
		
		double area = GeometryUtils.sphericalVoronoiDomainArea(germ, neighbors);
		double exp = 0.03698062787608 * Math.PI * 4;
		assertEquals(exp, area, 1e-10);
	}

	/**
	 * Check for direction (1, 1, 1).
	 */
	@Test
	public void testSphericalVoronoiDomainArea_D13_PPP() {
		Vector3d germ = new Vector3d(1, 1, 1);
		Vector3d[] neighbors = new Vector3d[] { 
				new Vector3d(+1,  0, +1),
				new Vector3d( 0,  0, +1),
				new Vector3d( 0, +1, +1),
				new Vector3d( 0, +1,  0),
				new Vector3d(+1, +1,  0),
				new Vector3d(+1,  0,  0),
		};
		
		double area = GeometryUtils.sphericalVoronoiDomainArea(germ, neighbors);
		double exp = 0.03519563978232 * Math.PI * 4;
		assertEquals(exp, area, 1e-10);
	}

	
	@Test
	public void testSphericalAngle() {
		Vector3d v1 = new Vector3d(1, 0, 0);
		Vector3d v2 = new Vector3d(0, 1, 0);
		Vector3d v3 = new Vector3d(0, 0, 1);
		
		double angle = GeometryUtils.sphericalAngle(v1, v2, v3);
		assertEquals(Math.PI / 2, angle, .001);
	}
	
}
