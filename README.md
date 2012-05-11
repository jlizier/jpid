JPID - The Java Partial Information Decomposition Toolkit
====

A Java software toolkit for computing partial information decomposition (PID) - both on average and on a local scale.
---------------------------------------------------------------------------------------------------------------------

This document describes the usage for this toolkit. It is included in the distribution as readme.md.

Copyright (C) 2012 Joseph Lizier (joseph.lizier at gmail.com) and Benjamin Flecker (btflecker at gmail.com)

License
-------

Licensed under [GNU General Public License v3](http://www.gnu.org/licenses/gpl.html "GPL v3") or any later version - see *license-gplv3.txt* included in the distribution.

Dependencies
------------

JUnit 3 - main toolkit code does not require this, but you will need it to run unit tests.

Referencing/Acknowledgement
---------------------------

You are asked to reference your use of this toolkit in any resulting publications to the following:
- Joseph T. Lizier and Benjamin Flecker, "Java Partial Information Decomposition toolkit", 2012;

You should also reference the underlying PID method to the original paper:
- Paul L. Williams and Randall D. Beer, "Nonnegative Decomposition of Multivariate Information", [arXiv:1004.2515](http://arxiv.org/abs/1004.2515 "arXiv:1004.2515"), 2010.

Download
--------

Download the most recent distribution of the toolkit from [github](https://github.com/jlizier/jpid.git "jpid").

Usage
-----

**Structure:** The distribution is structured as follows:

1. **java/source** - source files for the toolkit.
2. **java/javadocs** - Javadocs for the toolkit.
3. **java/unittests** - JUnit tests for the toolkit.
4. **python** - code to run demonstration applications calling the java toolkit.
5. **readme.html** - this html file.
6. **license-gplv3** - GNU General Public License v3.

**Using the toolkit**: main use of the toolkit is via the class **partialinfodecomp.discrete.PartialInfoDecomposer**. One would typically call its methods in the following order:

1. Construct, e.g.:

            PartialInfoDecomposer pid = new PartialInfoDecomposer(int, int[])

2. Provide observations (these can be accumulated, i.e. added multiple times):

            pid.addObservations(int[], int[][])
            pid.addObservation(int, int[])

3. Compute required Imin and PI entities (all returned in bits):
        
            pid.Imin(NodeOfElements)
            pid.PI(NodeOfElements)

4. Re-initialise (then start adding observations again):

            pid.initialise()

Sample code
-----------

A sample application of the code is provided in the python directory (to be added at a later date)

Contact
-------

[Joe Lizier](mailto:lizier at mis dot mpg dot de) and [Ben Flecker](mailto:btflecker at gmail dot com)

Last updated 10/05/2012.