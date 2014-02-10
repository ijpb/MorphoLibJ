/**
 * <p>Mathematical Morphology filters and reconstructions.</p>
 * 
 * <p>This package contains several classes for computing various morphological 
 * filtering operations (such as dilation, erosion, opening...) 
 * as well as plugins based on geodesic reconstruction of images.</p>
 * 
 * <p>Morphological filtering involves structuring elements of various shapes. 
 * Most of the processing is done by the structuring elements themselves.
 * The interface {@link inra.ijpb.morphology.Strel} defines the general contract 
 * for structuring element, and the class {@link inra.ijpb.morphology.Morphology}
 * contains static methods corresponding to each classical operation.</p>
 * 
 * <p>The class {@link inra.ijpb.morphology.GeodesicReconstruction} performs 
 * morphological geodesic reconstruction of a grayscale marker image within a 
 * grayscale mask image. This class is used by the two plugins 
 * {@link inra.ijpb.plugins.FillHolesPlugin}
 * and {@link inra.ijpb.plugins.KillBordersPlugin}.</p>
 */
package inra.ijpb.morphology;


