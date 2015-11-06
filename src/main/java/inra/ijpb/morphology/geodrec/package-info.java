/**
 * <p>Geodesic reconstruction by erosion and dilation.</p>
 * 
 * <p>This package provides interfaces for morphological reconstructions algorithms,
 * and several implementations.</p>
 *
 * <p>
 * For geodesic reconstruction on planar images, the class GeodesicReconstructionHybrid.
 * </p>
 * 
 * <p>
 * For geodesic reconstruction on 3D stacks, the class to use depends on the data type:
 * <ul>
 * <li>GeodesicReconstruction3DHybrid0Float for floating point images</li>
 * <li>GeodesicReconstruction3DHybrid0Gray8 for images with 255 gray levels</li>
 * <li>GeodesicReconstructionByDilation3DScanning and GeodesicReconstructionByErosion3DScanning for 16 bits images</li>
 * </ul> 
 * </p>
 */
package inra.ijpb.morphology.geodrec;


