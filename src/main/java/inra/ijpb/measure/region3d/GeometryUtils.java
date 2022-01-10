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

import inra.ijpb.geometry.Vector3D;

/**
 * Some utilities for geometric computations.
 * 
 * @author dlegland
 *
 */
public class GeometryUtils 
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	private GeometryUtils()
	{
	}

	/**
	 * Computes area of a spherical Voronoi domain, based on a germ and several
	 * neighbors.
	 * 
	 * @param germ
	 *            the germ of the region
	 * @param neighbors
	 *            the neighbor germs
	 * @return the area of the spherical Voronoi domain
	 */
	public static final double sphericalVoronoiDomainArea(Vector3D germ, Vector3D[] neighbors)
	{
		// ensure unit vector 
		germ = germ.normalize();
		
		int nNeighbors = neighbors.length;
		
		// For each neighbor, compute normal vector of the median plane between
		// neighbor and germ. Normal is oriented towards germ.
		Vector3D[] planeNormals = new Vector3D[nNeighbors];
		for (int i = 0; i < nNeighbors; i++)
		{
			planeNormals[i] = neighbors[i].normalize().minus(germ).normalize();
		}
		
		// Compute intersection line of each couple of median planes.
		// keep only the direction of the line.
		Vector3D[] lineDirections = new Vector3D[nNeighbors];
		for (int i = 0; i < nNeighbors; i++)
		{
			Vector3D v1 = planeNormals[i];
			Vector3D v2 = planeNormals[(i + 1) % nNeighbors];
			lineDirections[i] = Vector3D.crossProduct(v1, v2);
		}
		
		// Compute spherical angle at each vertex of the spherical polygon
		// defined by intersections of lines with unit sphere
		double[] angles = new double[nNeighbors];
		for (int i = 0; i < nNeighbors; i++)
		{
			Vector3D v1 = lineDirections[i];
			Vector3D v2 = lineDirections[(i + 1) % nNeighbors];
			Vector3D v3 = lineDirections[(i + 2) % nNeighbors];
			angles[i] = sphericalAngle(v1, v2, v3);
			
			// ensure angle is comprised between 0 and PI
			if (angles[i] > Math.PI)
			{
				angles[i] = 2 * Math.PI - angles[i];
			}
		}
		
		// area of spherical polygon is obtained as the sum of angles of vertices, 
		// minus a value depending on side number
		double area = 0;
		for (int i = 0; i < nNeighbors; i++)
		{
			area += angles[i];
		}
		area = area - Math.PI * (nNeighbors - 2);
		
		return area;
	}
	
	/**
	 * Computes the spherical angle of three points on the unit sphere, based on
	 * the vectors.
	 * 
	 * @param v1
	 *            the first vector
	 * @param v2
	 *            the second vector
	 * @param v3
	 *            the third vector
	 * @return the spherical angle
	 */
	public static final double sphericalAngle(Vector3D v1, Vector3D v2, Vector3D v3)
	{
		// compute normal vectors of planes containing couple of vectors
		Vector3D normal12 = Vector3D.crossProduct(v1, v2); 
		Vector3D normal23 = Vector3D.crossProduct(v2, v3);

		// project vector differences onto the plane normal to the vector v2
		Vector3D v21o = Vector3D.crossProduct(v2, normal12);
		Vector3D v23o = Vector3D.crossProduct(normal23, v2);
		
		// compute angle between the two vectors, between 0 and PI
		double theta = Vector3D.angle(v21o,  v23o);
		
		return theta;
	}
}
