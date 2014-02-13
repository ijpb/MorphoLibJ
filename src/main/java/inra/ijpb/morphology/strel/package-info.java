/**
 * <p>A collection of Structuring Element ("Strel") implementations for mathematical morphology.</p> 
 * 
 * <p>
 * Contains the implementation of several types of structuring elements. 
 * The packages tries to takes advantage of the separability property of most 
 * structuring elements. </p>
 * 
 * <p> The package can be divided into:
 * <ul>
 * <li>Specialization Strel interfaces: {@link inra.ijpb.morphology.strel.SeparableStrel}, 
 * 	{@link inra.ijpb.morphology.strel.InPlaceStrel}</li>
 * <li>Abstract classes for facilitating implementations: {@link inra.ijpb.morphology.strel.AbstractStrel}, 
 * 	{@link inra.ijpb.morphology.strel.AbstractSeparableStrel}, 
 * 	{@link inra.ijpb.morphology.strel.AbstractInPlaceStrel}</li>
 * <li>Final Strel implementations: {@link inra.ijpb.morphology.strel.SquareStrel}, 
 * 	{@link inra.ijpb.morphology.strel.OctagonStrel}, {@link inra.ijpb.morphology.strel.DiamondStrel}, 
 * 	{@link inra.ijpb.morphology.strel.Cross3x3Strel}...</li>
 * <li>Utility classes that manage local histograms: {@link inra.ijpb.morphology.strel.LocalBufferMin}, 
 * 	{@link inra.ijpb.morphology.strel.LocalBufferMax}</li> 
 * </ul>
 * </p>
 * 
 */
package inra.ijpb.morphology.strel;


