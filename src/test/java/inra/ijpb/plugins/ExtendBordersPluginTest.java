/**
 * 
 */
package inra.ijpb.plugins;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import inra.ijpb.data.border.ConstantBorder;

import org.junit.Test;

/**
 * @author dlegland
 *
 */
public class ExtendBordersPluginTest
{
	/**
	 * Test method for {@link inra.ijpb.plugins.ExtendBordersPlugin#process(ij.process.ImageProcessor, int, int, int, int, inra.ijpb.data.border.BorderManager)}.
	 */
	@Test
	public void testProcess2D()
	{
		ImagePlus inputPlus = IJ.openImage(getClass().getResource( "/files/grains.tif" ).getFile() );
		ImageProcessor input = inputPlus.getProcessor();
		
		ImageProcessor result = ExtendBordersPlugin.process(input, 5, 10, 15, 20, new ConstantBorder(input, 0));
		
		int exp = input.getWidth() + 15;
		assertEquals(exp, result.getWidth());
		exp = input.getHeight() + 35;
		assertEquals(exp, result.getHeight());
	}

}
