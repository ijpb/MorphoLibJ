package inra.ijpb.morphology.strel;

import static org.junit.Assert.assertNotNull;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;

import org.junit.Test;

public class SquareStrelTiming {

	@Test
	public void timingLeafImage_gray8() {
		ImageProcessor image = getLeafImage();
		assertNotNull(image);
		
		int[] sizeArray = new int[]{3, 5, 7, 9, 11, 15, 21, 31, 51, 71, 101};
		double[] timingArray = new double[sizeArray.length];

		System.out.println("Dilation using gray8 image");
		for (int i = 0; i < sizeArray.length; i++)
		{
			Strel strel = SquareStrel.fromDiameter(sizeArray[i]);
			long t0 = System.currentTimeMillis();
			strel.dilation(image);
			long t1 = System.currentTimeMillis();
			timingArray[i] = (t1 - t0) / 1000.0;
			
			System.out.println(String.format("size = %d: %7.5f s", sizeArray[i], timingArray[i]));
		}
	}

//	@Test
	public void timingLeafImage_Float() {
		ImageProcessor image = getLeafImage();
		assertNotNull(image);
		
		int[] sizeArray = new int[]{3, 5, 7, 9, 11, 15, 21, 31, 51, 71, 101};
		double[] timingArray = new double[sizeArray.length];

		image = image.convertToFloat();
		
		System.out.println("Dilation using float image");
		
		for (int i = 0; i < sizeArray.length; i++)
		{
			Strel strel = SquareStrel.fromDiameter(sizeArray[i]);
			long t0 = System.currentTimeMillis();
			strel.dilation(image);
			long t1 = System.currentTimeMillis();
			timingArray[i] = (t1 - t0) / 1000.0;
			
			System.out.println(String.format("size = %d: %7.5f s", sizeArray[i], timingArray[i]));
		}
	}

	
	@Test
	public void timingMaizeImage() {
		ImageProcessor image = getMaizeImage();
		assertNotNull(image);
		
		int[] sizeArray = new int[]{3, 5, 7, 9, 11, 15, 21, 31, 51, 71, 101};
		double[] timingArray = new double[sizeArray.length];

		System.out.println("Dilation using rgb8 image");
		for (int i = 0; i < sizeArray.length; i++)
		{
			Strel strel = SquareStrel.fromDiameter(sizeArray[i]);
			long t0 = System.currentTimeMillis();
			strel.dilation(image);
			long t1 = System.currentTimeMillis();
			timingArray[i] = (t1 - t0) / 1000.0;
			
			System.out.println(String.format("size = %d: %7.5f s", sizeArray[i], timingArray[i]));
		}
	}


	private ImageProcessor getRiceImage() {
		String fileName = getClass().getResource("/files/grains.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		
		assertNotNull(imagePlus);

		ImageProcessor image = imagePlus.getProcessor();
		return image;
	}
	
	private ImageProcessor getLeafImage() {
		String fileName = getClass().getResource("/files/CA_QK_004_H1.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		
		assertNotNull(imagePlus);

		ImageProcessor image = imagePlus.getProcessor();
		return image;
	}
	
	private ImageProcessor getMaizeImage() {
		String fileName = getClass().getResource("/files/2432a_corr.png").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		
		assertNotNull(imagePlus);

		ImageProcessor image = imagePlus.getProcessor();
		return image;
	}
}
