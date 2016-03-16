MorphoLibJ
============

Collection of mathematical morphology methods and plugins for ImageJ, created at INRA-IJPB Modeling and Digital Imaging lab.

The library implements several functionalities that were missing in the ImageJ software, and that were not or only partially covered by other plugins. Namely:

* **Morphological filtering**: erosion & dilation, closing & opening, morphological gradient & laplacian, top-hat...

* **Morphological reconstruction**, allowing fast detection of regional or extended extrema, removing of borders, or hole filling

* **Watershed segmentation + GUI**, making it possible to segment 2D/3D images of cell tissues

* **2D/3D measurements**: volume, surface area, inertia ellipse/ellipsoid...

* **Binary / label images utilities** for removing or keeping largest connected component, perform size opening, fill holes, kill borders...

Installation
------------
* In ImageJ, download the [latest released jar](https://github.com/ijpb/MorphoLibJ/releases) into the _plugins_ folder.

* In Fiji, you just need to add the IJPB-plugins update site:

> 1. Select _Help > Update..._ from the Fiji menu to start the updater.

> 2. Click on _Manage update sites_. This brings up a dialog where you can activate additional update sites.

> 3. Activate the IJPB-plugins update site and close the dialog. Now you should see an additional jar file for download.

> 4. Click _Apply changes_ and restart Fiji.

Documentation
-------------

A more detailed presentation of the library and its plugins is available on the [ImageJ/Fiji Wiki](http://imagej.net/MorphoLibJ). Some information may also be found on the [Internet page of MorphoLibJ](http://ijpb.github.io/MorphoLibJ/).

We have as well a [User Manual](https://github.com/ijpb/MorphoLibJ/releases/download/v1.1.1/MorphoLibJ-manual-v1.1.1.pdf) in pdf format.

The main source code directory is [src/main/java/inra/ijpb](http://github.com/ijpb/MorphoLibJ/tree/master/src/main/java/inra/ijpb).

You can browse the [javadoc](http://ijpb.github.io/MorphoLibJ/javadoc/) for more information about its API.
