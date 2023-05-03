/**
 * 
 */
package inra.ijpb.data.image;

/**
 * A simple functional interface declaration to be used for populating 3D
 * images.
 * 
 * In general, it should not be necessary to reference the interface. Instead,
 * one can use lambda syntax. Example:
 * 
 * <pre>{@code
 * ImageStack image = ImageStack.create(7, 5, 3, 8);
 * ImageUtils.fill(image, (x, y, z) -> (x + y * 10.0 + z * 100.0));
 * }</pre>
 * 
 * 
 * @see inra.ijpb.data.image.ImageUtils#fill(ij.ImageStack, TriFunction)
 * 
 * @author dlegland
 */
@FunctionalInterface
public interface TriFunction<T, U, V, R>
{
    /**
     * Applies the function to the triplet of input arguments.
     * 
     * @param t
     *            the first input argument
     * @param u
     *            the second input argument
     * @param v
     *            the third input argument
     * @return the result of the function evaluation at (t, u, v)
     */
    public R apply(T t, U u, V v);
}
