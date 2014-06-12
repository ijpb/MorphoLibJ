/**
 * <p>Mathematical Morphology filters and reconstructions.</p>
 * 
 * <p>This package contains several classes for computing various morphological 
 * filtering operations (such as dilation, erosion, opening...) 
 * as well as plugins based on geodesic reconstruction of images.</p>
 * 
 * <p><strong>Morphological filtering</strong> involves structuring elements of various shapes. 
 * Most of the processing is done by the structuring elements themselves.
 * The interface {@link inra.ijpb.morphology.Strel} defines the general contract 
 * for structuring element, and the class {@link inra.ijpb.morphology.Morphology}
 * contains static methods corresponding to each classical operation.</p>
 * 
 * <p>The class {@link inra.ijpb.morphology.GeodesicReconstruction} performs 
 * <strong>morphological geodesic reconstruction</strong> of a grayscale marker
 * image within a grayscale mask image. 
 * This class is used by the two plugins 
 * {@link inra.ijpb.plugins.FillHolesPlugin}
 * and {@link inra.ijpb.plugins.KillBordersPlugin}. 
 * More specialized algorithms are provided in the 
 * <code><a href="{@docRoot}/inra/ijpb/morphology/geodrec/package-summary.html">geodrec</a></code> package</p>
 * 
 * <p>Another common task is the detection of regional minima or maxima. 
 * A more powerful approach is to use extended minima or maxima, by specifying
 * a parameter of dynamic that allows to focus on relevant extrema. Static 
 * methods are provided in the {@link inra.ijpb.morphology.MinimaAndMaxima} and
 * {@link inra.ijpb.morphology.MinimaAndMaxima3D} classes. 
 * </p>
 */
package inra.ijpb.morphology;


