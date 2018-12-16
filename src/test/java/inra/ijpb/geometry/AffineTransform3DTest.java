/**
 * 
 */
package inra.ijpb.geometry;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author dlegland
 *
 */
public class AffineTransform3DTest
{

	/**
	 * Test method for {@link inra.ijpb.geometry.AffineTransform3D#createTranslation(inra.ijpb.geometry.Vector3D)}.
	 */
	@Test
	public void testCreateTranslationVector3D()
	{
		AffineTransform3D trans1 = AffineTransform3D.createTranslation(new Vector3D(5, 4, 3));
		AffineTransform3D trans2 = AffineTransform3D.createTranslation(new Vector3D(-5, -4, -3));
		AffineTransform3D res = trans1.concatenate(trans2);
		assertTrue(res.almostEquals(new AffineTransform3D(), 1e-3));
	}

	/**
	 * Test method for {@link inra.ijpb.geometry.AffineTransform3D#createScaling(double, double, double)}.
	 */
	@Test
	public void testCreateScalingDoubleDoubleDouble()
	{
		AffineTransform3D trans1 = AffineTransform3D.createScaling(5, 4, 3);
		AffineTransform3D trans2 = AffineTransform3D.createScaling(1.0/5.0, 1.0/4.0, 1.0/3.0);
		AffineTransform3D res = trans1.concatenate(trans2);
		assertTrue(res.almostEquals(new AffineTransform3D(), 1e-3));
	}

	/**
	 * Test method for {@link inra.ijpb.geometry.AffineTransform3D#createRotationOx(double)}.
	 */
	@Test
	public void testCreateRotationOxDouble()
	{
		AffineTransform3D trans1 = AffineTransform3D.createRotationOx(1.0);
		AffineTransform3D trans2 = AffineTransform3D.createRotationOx(-1.0);
		AffineTransform3D res = trans1.concatenate(trans2);
		assertTrue(res.almostEquals(new AffineTransform3D(), 1e-3));
	}

	/**
	 * Test method for {@link inra.ijpb.geometry.AffineTransform3D#createRotationOy(double)}.
	 */
	@Test
	public void testCreateRotationOy()
	{
		AffineTransform3D trans1 = AffineTransform3D.createRotationOy(1.0);
		AffineTransform3D trans2 = AffineTransform3D.createRotationOy(-1.0);
		AffineTransform3D res = trans1.concatenate(trans2);
		assertTrue(res.almostEquals(new AffineTransform3D(), 1e-3));
	}

	/**
	 * Test method for {@link inra.ijpb.geometry.AffineTransform3D#createRotationOz(double)}.
	 */
	@Test
	public void testCreateRotationOz()
	{
		AffineTransform3D trans1 = AffineTransform3D.createRotationOz(1.0);
		AffineTransform3D trans2 = AffineTransform3D.createRotationOz(-1.0);
		AffineTransform3D res = trans1.concatenate(trans2);
		assertTrue(res.almostEquals(new AffineTransform3D(), 1e-3));
	}

	/**
	 * Test method for {@link inra.ijpb.geometry.AffineTransform3D#concatenate(inra.ijpb.geometry.AffineTransform3D)}.
	 */
	@Test
	public void testConcatenate()
	{
		AffineTransform3D trans1 = AffineTransform3D.createRotationOx(1.0);
		AffineTransform3D trans2 = AffineTransform3D.createRotationOz(1.2);

		AffineTransform3D res1 = trans1.concatenate(trans2);
		AffineTransform3D res2 = trans2.preConcatenate(trans1);
		assertTrue(res1.almostEquals(res2, 1e-3));
	}

}
