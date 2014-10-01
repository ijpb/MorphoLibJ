ijpb-plugins
============

Collection of ImageJ plugins created at INRA-IJPB Modeling and Digital Imaging lab

The library implements several functionalities that were missing in the ImageJ software, and that were not or only partially covered by other plugins. 

* morphological filtering: erosion & dilation, closing & opening, morphological gradient & laplacian, top-hat...

* morphological reconstruction, allowing fast detection of regional or extended extrema, removing of borders, or hole filling

* watershed segmentation + GUI, making it possible to segment 2D/3D images of cell tissues

* 2D/3D measurements: volume, surface area, inertia ellipse/ellipsoid...

* binary / label images utilities

Installation
------------
* In ImageJ, download the latest released jar into the _plugins_ folder.


* In Fiji, you just need to add the IJPB-plugins update site:

> 1. Select _Help > Update..._ from the Fiji menu to start the updater.

> 2. Click on _Manage update sites_. This brings up a dialog where you can activate additional update sites.

> 3. Activate the IJPB-plugins update site and close the dialog. Now you should see an additional jar file for download.

> 4. Click _Apply changes_ and restart Fiji.
