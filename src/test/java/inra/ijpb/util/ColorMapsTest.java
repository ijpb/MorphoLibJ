package inra.ijpb.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class ColorMapsTest
{

	@Test
	public void testCreateGoldenAngleLut()
	{
		byte[][] lut = ColorMaps.createGoldenAngleLut( 10 );
		
		byte[] color = lut[0];
		assertNotEquals(0, color[0]);
		assertNotEquals(0, color[1]);
		assertNotEquals(0, color[2]);
	}

}
