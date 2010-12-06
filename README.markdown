iglaset Android App
===================

Search in one of Swedens largest liquor databases [iglaset.se](http://iglaset.se) 
with comments and ratings from real users.

![Screenshot of iglaset for Android](http://farm3.static.flickr.com/2711/4477147518_a9ca59a30c_o.png "iglaset for Android")

Setting up the project
----------------------

Once forked and imported to Eclipse you need to run "Fix Project Properties".
This is found under Android Tools. 

TODO and wish list
------------------

* Shopping list
* Find a systembolag near you
* Integration with [Systembevakningsagenten](http://agent.nocrew.org/)
* Sort by category
* Sort by rating
* more more

Schedule
--------
* JSON instead of SAX
* (Integrate droid-fu (http://github.com/kaeppler/droid-fu))

Changelog
---------
* Next:
    * Fixed crash on 1.6 devices (thanks to Daniel Ekström for the report)
    * Minor comment layout fix
    * Tags in the drink detail are now clickable (Issue #13)
    * Show estimated rating instead of average if available
    * Fixed a bug where drink images were loaded into the wrong search row (Issue #19)
    * Added sorting support in searches
* 1.4.0
    * Changed link to Systembolaget to link to their inventory mobile friendly page    
    * Added support for tag browsing
    * Crash fixes in the drink detail view
    * Optimization in the search result view
    * Show drink rate/comment count in search- and detailed view
    * Better flow when scanning new barcodes
    * Minor data status fix: Rating or commenting drinks will now update more parts of the UI.
    * Dropped support for Android 1.5
    * Install on SD card
    * Layout fixes for Sony Ericsson devices
* 1.3.2
    * Fixed several crashes when exiting various dialogs when a network activity was in progress
    * Larger thumbnails for HDPI screens
    * Aesthetic fixes for the search bar
* 1.3.1
    * Added a crash reporter
    * Show item descriptions
    * Searches with only 1 result will go straight to the drink details
* 1.3.0
    * Fixed bug with some articles ended up on 404 page after clicked on "view
      on iglaset.se"
    * Show icon that indicates if the user has rated an article
    * If origin and origin country is the same just show one of them 
    * Drink images in lists and drink detail views are now clickable for a
      fullscreen image view
    * Commenting support added
    * User recommended articles
    * User Rated articles
    * Redesigned start activity
    * Adaptions for different screen sizes
* 1.2.3
    * Added a retry dialog for network problems for searches and for crowdsourcing
      barcodes, Closes #3
    * Disabled Analytics tracking since it caused several FC, Closes #5
    * Added a way to manual add barcodes 
* 1.2.2
    * Removed the check for EAN_13 when scanning barcodes, now we are allowing
      all types of codes
* 1.2.1
    * Fixed layout problems on WVGA screens
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
