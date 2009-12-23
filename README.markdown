iglaset Android App
===================

Search in one of Swedens largest liquor databases [iglaset.se](http://iglaset.se) 
with comments and ratings from real users.

Note, the application is in Swedish.

Setting up the project
----------------------

Once forked and imported to Eclipse you need to run "Fix Project Properties".
This is found under Android Tools. 

TODO and wish list
------------------

* Comment on drinks
* Shopping list
* Find a systembolag near you
* Integration with [Systembevakningsagenten](http://agent.nocrew.org/)
* Sort by category
* Sort by rating
* more more

Changelog
---------
* 1.2.0
    * Added support for scanning and crowdsourcing barcodes
    * Fixed problem with some product names not being parsed correct, closes #4
    * Added SB text icon when listing volumes
    * Added support for all screens QVGA etc, also changed to compile against 1.6 
    * Fixed problem with the google analytics integration
* 1.1.0
    * Light Theme
    * Added category listings
    * iglaset integration, and possibility to add set ratings
    * Direct link to systembolaget.se from the article detail view
    * Scrollable text if the article title is to long in the search result 
    * Added Google Analytics integration
    * Preparations for bar code scanning
* 1.0.3
    * Now using apache http client for http request
    * Added pagination to the search result
    * Replaced ImageUtilities with ImageLoader by Guojian Miguel Wu <http://wu-media.com>
      This also solves issue 2 with images not always being loaded correct  
* 1.0.2
    * Added access to search via context menu from the search results and the
      detailed view
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
    * Added link to iglaset.se from the drink detail activity
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
