package inra.ijpb.data.image;

import ij.ImageStack;

public class Images3D {

	public final static Image3D createWrapper(ImageStack stack) {
		switch(stack.getBitDepth()) {
		case 8:
			return new ByteStackWrapper(stack);
		case 16:
			return new ShortStackWrapper(stack);
		case 32:
			return new FloatStackWrapper(stack);
		default:
			throw new IllegalArgumentException(
					"Can not manage image stacks with bit depth "
							+ stack.getBitDepth());
		}
	}
}
