/**
 * 
 */
package inra.ijpb;

import static org.junit.Assert.assertNotNull;
import ij.IJ;
import ij.ImagePlus;

import org.junit.Test;

/**
 * A simple test case to be sure it is possible to load images (and other 
 * resources) within the 'file' directory of the 'resources' arborescence.
 */
public class OpenResourceImage {

	@Test
	public final void testOpenImage() {
		String fileName = getClass().getResource("/files/peppers-crop.png").getFile();
		assertNotNull(fileName);
		
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
	}

}
