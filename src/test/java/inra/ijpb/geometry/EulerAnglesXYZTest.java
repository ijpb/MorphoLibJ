/**
 * 
 */
package inra.ijpb.geometry;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import Jama.Matrix;

/**
 * @author dlegland
 *
 */
public class EulerAnglesXYZTest
{
	/**
	 * Test method for {@link inra.ijpb.geometry.EulerAnglesXYZ#rotationMatrix()}.
	 */
	@Test
	public void test_convertToAndFromMatrix_30_20_10()
	{
		EulerAnglesXYZ angles = new EulerAnglesXYZ(30, 20, 10);
		Matrix matrix = angles.rotationMatrix();
		EulerAnglesXYZ angles2 = EulerAnglesXYZ.fromMatrix(matrix);
		
		assertEquals(angles.phi, angles2.phi, .01);
		assertEquals(angles.theta, angles2.theta, .01);
		assertEquals(angles.psi, angles2.psi, .01);
	}

	/**
	 * Test method for {@link inra.ijpb.geometry.EulerAnglesXYZ#rotationMatrix()}.
	 */
	@Test
	public void test_convertToAndFromMatrix_10_20_30()
	{
		EulerAnglesXYZ angles = new EulerAnglesXYZ(10, 20, 30);
		Matrix matrix = angles.rotationMatrix();
		EulerAnglesXYZ angles2 = EulerAnglesXYZ.fromMatrix(matrix);
		
		assertEquals(angles.phi, angles2.phi, .01);
		assertEquals(angles.theta, angles2.theta, .01);
		assertEquals(angles.psi, angles2.psi, .01);
	}

	@Test
	public void test_directionVectors_00_00_00()
	{
		EulerAnglesXYZ angles = new EulerAnglesXYZ();
		ArrayList<Vector3D> vectors = angles.directionVectors();

		assertTrue(vectors.get(0).almostEquals(new Vector3D(1.0, 0.0, 0.0), .01));
		assertTrue(vectors.get(1).almostEquals(new Vector3D(0.0, 1.0, 0.0), .01));
		assertTrue(vectors.get(2).almostEquals(new Vector3D(0.0, 0.0, 1.0), .01));
	}
}
