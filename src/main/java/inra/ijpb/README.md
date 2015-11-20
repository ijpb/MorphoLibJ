Contents of the src/main/java/inra/ijpb directory
------------

This directory contains various packages, organized according to their functionalities
or to the type of images they are working on.

* **algo** utilities to propagate events (progression, status change...) during execution of algorithms

* **binary** a set of utilities for working on binary images (connected component 
labeling, distance transform, geodesic distance transform...)

* **data** contains various data structure to represent images as well as utility 
data structures (connectivities, cursors...)

* **filter** utilities for filtering

* **math** re-implementation of some mathematical operations on images, that
can be accessed programmatically (without calling a plugin)

* **measure** several classes for extracting quantified parameters from 2D or 3D images

* **morphology** a collection of mathematical morphology operators, comprising morphological
filtering (opening, closing, dilation, top-hat...), geodesic reconstruction and related
operators (hole filling, border killing, extraction of regional and/or extended minima or 
maxima...), and some utilities for managing label images

* **plugins** the set of plugins that is accessible from ImageJ/Fiji Plugins menu

* **segment** should contain several segmentation algorithms, but contains only 
threshold classes for the moment

* **util** various utilities, for managing colormaps, or communicating with ImageJ

* **watershed** morphological segmentation of grey-level images using watershed algorithm
