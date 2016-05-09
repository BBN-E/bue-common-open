bue-common-open
===============

The BBN Speech, Language, and Multimedia Group uses an internal Java library of common
utility functions written by many people, `bue-common`.  We sometimes make releases of open-source 
software which depend on parts of this library, requiring that certain classes
be open-sourced as well.  This repository contains the (small) open-source 
portion of this library. 

While anyone is welcome to use this code, in general no support is provided.

Contact: rgabbard@bbn.com

## Requirements
* Maven

## Building
* from the root of this repository, `mvn install`

## Note on Maven group IDs

This repository departs from the usual practice of all sub-modules sharing
or appending to the parent's group ID because it resulted from the merger
of multiple git repositories and we did not want to break existing Maven
references.

<sub>**Legal notes**</sub>

<sub>Distribution Statement "A" (Approved for Public Release, Distribution Unlimited)</sub>

<sub>WARNING - This document contains technology whose export or disclosure to
Non-U.S. persons, wherever located, is subject to the Export Administrations
Regulations (EAR) (15 C.F.R. Sections 730-774). Violations are subject to
severe criminal penalties.</sub>
