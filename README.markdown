iglaset Android App
===================

Search in one of Swedens largest liquor databases [iglaset.se](http://iglaset.se) 
with comments and ratings from real users.

*Note*, this is still a work in progress and not yet released on market.

[![screen shot of the detail view](http://farm4.static.flickr.com/3469/3889614494_ee943520f1_m.jpg "Detail view")](http://www.flickr.com/photos/johannilsson/3889614494/)

Setting up the project
----------------------

Once forked and imported to Eclipse you need to run "Fix Project Properties".
This is found under Android Tools. 

TODO and wish list
------------------

* Comment on drinks
* Set ratings
* Shopping list
* Find a systembolag near you
* Sort by category
* Sort by rating

Changelog
---------
* 1.0.1
    * Fixed issue 1, comment section duplicated when screen was rotated.

* 1.0.0
    * Separated the custom progress bar from search_result.xml to a separate
      more reusable layout
    * Added loading comments adapter
    * Added no comments yet text when no comments is set
    * Added a removeSection method to the SectionedAdapter
* 0.8
    * Changed name of the link to iglaset.se
    * Fixed bug that caused user ratings in comments to be hidden
    * Added a progress bar in the middle of the activity when searching
* 0.7
    * Added link to iglaset.se from drink detail activity
* 0.6
    * Added maxLength to origin and country to prevent text overflows
* 0.5
    * Fixed force close if user rating in comments was missing
* 0.4
    * User rating now visible in comments
    * Added about dialog
    * Added search history
    * Added preferences activity
    * Possible to remove search search history