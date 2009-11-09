(function(cms) {

   /** html-id for tabs. */
   var idTabs = cms.galleries.idTabs = 'tabs';
   
   /** html-id for the tab with search results. */
   var idTabResult = cms.galleries.idTabResult = 'tabs-result';
   
   /** html-class for the inner of the scrolled list with items. */
   var classScrollingInner = cms.galleries.classScrollingInner = 'cms-list-scrolling-innner';
   
   /** html-class for hovered list item elements. */
   var classListItemHover = cms.galleries.classListItemHover = 'cms-list-item-hover';
   
   /** html-class for active list item elements. */
   var classListItemActive = cms.galleries.classListItemActive = 'cms-list-item-active';
   
   /** html-class for the item title in the list. */
   var classListItemTitle = cms.galleries.classListItemTitle = 'cms-list-title';
   
   /** html-class for the panel above or under the list of items. */
   var classListOptions = cms.galleries.classListOptions = 'cms-list-options';
   
   /** html-class fragment for level information of the categories. */
   var classConstLevel = 'cms-level-';
   
   /** Array with available search criteria. */
   var keysSearchObject = cms.galleries.keysSearchObject = ['types', 'galleries', 'categories', 'query'];
   
   /** Map of key words for the criteria buttons on the result tab. */
   var criteriaStr = cms.galleries.criteriaStr = {
      types: ['Type: ', 'Types: '],
      galleries: ['Gallery: ', 'Galleries: '],
      categories: ['Category: ', 'Categories: '],
      query: ['Search query: ', 'Seach queries: ']
   
   };
   
   
   
   /** html fragment for the tab with the results of the search. */
   var htmlTabResultSceleton = cms.galleries.htmlTabResultSceleton = '<div id="' + cms.galleries.idTabResult + '">\
            <div class="cms-result-criteria"></div>\
            <div id="resultoptions" class="ui-widget ' +
   cms.galleries.classListOptions +
   '">\
                        <span class="cms-drop-down">\
                            <label>Sort by:</label>\
                         //   <select name="categories" size="1">\
                       //         <option value="title.asc">Title Ascending</option>\
                     //           <option value="title.desc">Title Descending</option>\
                   //             <option value="type.asc">Type Ascending</option>\
                 //               <option value="type.desc">Type Descending</option>\
               //                 <option value="datemodified.asc">Date modifired Ascending</option>\
             //                   <option value="datemodified.desc">Date modifired Descending</option>\
           //                     <option value="path.asc">Path Ascending</option>\
         //                       <option value="path.desc">Path Descending</option>\
       //                     </select>\
                        </span>\
                        <span class="cms-ft-search"><label>Search:</label><input type="text"/></span>\
             </div>\
             <div id="results" class="cms-list-scrolling ui-corner-all">\
                        <ul class="cms-list-scrolling-innner"></ul>\
             </div>\
             <div class="result-pagination"></div>\
         </div>';
   
   /** html fragment for the tab with the types' list. */
   var htmlTabTypesSceleton = cms.galleries.htmlTabTypesSceleton = '<div id="tabs-types">\
                <div id="typesoptions" class="ui-widget ' + cms.galleries.classListOptions + '">\
                    <span class="cms-drop-down">\
                        <label>Sort by:</label>\
                        <select name="types" size="1">\
                            <option value="title,asc">Title Ascending</option>\
                            <option value="title,desc">Title Descending</option>\
                        </select>\
                    </span>\
                    <span class="cms-ft-search"><label>Search:</label><input type="text"/></span>\
                </div>\
                <div id="types" class="cms-list-scrolling ui-corner-all">\
                    <ul class="cms-list-scrolling-innner"></ul>\
                </div>\
                <div id="typesbuttons" class="' +
   cms.galleries.classListOptions +
   '">\
                    <button class="ui-state-default ui-corner-all cms-item-right">Select</button>\
                </div>\
              </div>';
   
   /** html fragment for the tab with the galleries' list. */
   var htmlTabGalleriesSceleton = cms.galleries.htmlTabGalleriesSceleton = '<div id="tabs-galleries">\
                <div id="galleriesoptions" class="ui-widget ' + cms.galleries.classListOptions + '">\
                    <span class="cms-drop-down">\
                        <label>Sort by:</label>\
                        <select name="galleries" size="1">\
                            <option value="title,asc">Title Ascending</option>\
                            <option value="title,desc">Title Descending</option>\
                            <option value="type,asc">Type Ascending</option>\
                            <option value="type,desc">Type Descending</option>\
                        </select>\
                    </span>\
                    <span class="cms-ft-search"><label>Search:</label><input type="text"/></span>\
                </div>\
                <div id="galleries" class="cms-list-scrolling ui-corner-all">\
                    <ul class="cms-list-scrolling-innner"></ul>\
                </div>\
                <div id="galleriesbuttons" class="' +
   cms.galleries.classListOptions +
   '">\
                    <button class="ui-state-default ui-corner-all cms-item-right">Select</button>\
                </div>\
              </div>';
   
   /** html fragment for the tab with the categories' list. */
   var htmlTabCategoriesSceleton = cms.galleries.htmlTabCategoriesSceleton = '<div id="tabs-categories">\
                <div id="categoriesoptions" class="ui-widget ' + cms.galleries.classListOptions + '">\
                    <span class="cms-drop-down">\
                        <label>Sort by:</label>\
                        <select name="categories" size="1">\
                            <option value="path,asc">Hierarchy</option>\
                            <option value="title,asc">Title Ascending</option>\
                            <option value="title,desc">Title Descending</option>\
                        </select>\
                    </span>\
                    <span class="cms-ft-search"><label>Search:</label><input type="text"/></span>\
                </div>\
                <div id="categories" class="cms-list-scrolling ui-corner-all">\
                    <ul class="cms-list-scrolling-innner"></ul>\
                </div>\
                <div id="categoriesbuttons" class="' +
   cms.galleries.classListOptions +
   '">\
                    <button class="ui-state-default ui-corner-all cms-item-right">Select</button>\
                </div>\
              </div>';
   
   /** html fragment for the tab with the types' list. */
   var htmlTabFTSeachSceleton = cms.galleries.htmlTabFTSeachSceleton = '<div id="tabs-fulltextsearch">\
                    <div class="ui-widget cms-search-options">\
                        <span id="searchQuery" class="cms-item-left"><label>Search for:</label><input type="text"/></span>\
                    </div>\
                    <div class="ui-widget cms-search-options">\
                        <div class="cms-item-left">Search in:</div>\
                        <div id="searchInTitle" class="cms-list-checkbox"></div>\
                        <div class="cms-checkbox-label">Title</div>\
                        <div id="searchInContent" class="cms-list-checkbox"></div>\
                        <div class="cms-checkbox-label">Content</div>\
                    </div>\
                    <div class="ui-widget cms-search-options">\
                        <span id="searchBefore" class="cms-item-left cms-input-date"><label>Changed after:</label><input type="text"/></span>\
                        <span id="searchBefore" class="cms-item-left cms-input-date"><label>Changed before:</label><input type="text"/></span>\
                    </div>\
                    <div class="cms-search-options">\
                        <button class="ui-state-default ui-corner-all cms-item-left">Search</button>\
                    </div>\
              </div>';
   
   /** html fragment for the <li> in the galleries list. */
   var listGalleryElement = cms.galleries.listGalleryElement = function(itemTitle, itemUrl) {
   
      return $('<li></li>').addClass('cms-list').attr('rel', itemUrl).append('<div class="cms-list-checkbox"></div>\
                         <div class="cms-list-item ui-widget-content ui-state-default ui-corner-all">\
                             <div class="cms-list-image"></div>\
                             <div class="cms-list-itemcontent">\
                                 <div class="' + cms.galleries.classListItemTitle + '">' + itemTitle + '</div>\
                                 <div class="cms-list-url">' +
      itemUrl +
      '</div>\
                             </div>\
                         </div>');
   }
   
   /** html fragment for the <li> in the types list. */
   var listTypeElement = cms.galleries.listTypeElement = function(itemTitle, itemId, itemDesc, itemTypes) {
   
      return $('<li></li>').addClass('cms-list').attr('rel', itemId).append('<div class="cms-list-checkbox"></div>\
                         <div class="cms-list-item ui-widget-content ui-state-default ui-corner-all">\
                             <div class="cms-list-image"></div>\
                             <div class="cms-list-itemcontent">\
                                 <div class="' + cms.galleries.classListItemTitle + '">' + itemTitle + '</div>\
                                 <div class="cms-list-url">' +
      itemDesc +
      '</div>\
                             </div>\
                         </div>');
   }
   
   /** html fragment for the <li> in the categories list. */
   var listCategoryElement = cms.galleries.listCategoryElement = function(itemTitle, itemUrl, itemLevel, classItemActive) {
   
      return $('<li></li>').addClass('cms-list ' + classItemActive + ' ' + classConstLevel + itemLevel).attr('rel', itemUrl).append('<div class="cms-list-checkbox"></div>\
                         <div class="cms-list-item ui-widget-content ui-state-default ui-corner-all">\
                             <div class="cms-list-image"></div>\
                             <div class="cms-list-itemcontent">\
                                 <div class="' + cms.galleries.classListItemTitle + '">' + itemTitle + '</div>\
                                 <div class="cms-list-url">' +
      itemUrl +
      '</div>\
                             </div>\
                         </div>');
   }
   
   var listResultElement = cms.galleries.listResultElement = function(itemTitle, itemPath) {
      return $('<li class="cms-result-list-item"></li>').attr('rel', itemPath).append('<div class="ui-widget-content ui-state-default ui-corner-all">\
                             <div class="cms-list-image"></div>\
                             <div>\
                                 <div class="cms-result-list-title">' + itemTitle + '</div>\
                                 <div class="cms-result-list-path">' +
      itemPath +
      '</div>\
                             </div>\
                         </div>');
   }
   
   /**
    * Map of selected search criteria.
    *
    * types: array of resource ids for the available resource types
    * galleries: array of paths to the available galleries
    * categories: array of paths to the available categories
    * searchquery: the search key word
    * page: the page number of the requested result page
    * isChanged: map of flags indicating if one of the search criteria is changed and should be taken into account
    */
   var searchObject = cms.galleries.searchObject = {
      types: [],
      galleries: [],
      categories: [],
      query: '',
      page: 1,
      searchfields: '',
      matchesperpage: 8,
      isChanged: {
         types: true,
         galleries: true,
         categories: true,
         query: false
      }
   };
   
   /** Saves the initial list of all available search criteria from server. */
   var searchCriteriaListsAsJSON = cms.galleries.searchCriteriaListsAsJSON = {};
   
   /**
    * Dummy content
    */
   var dummyGalleries = cms.galleries.dummyGalleries = [{
      title: 'Gallery 1',
      path: 'url/to/gallery1/',
      icon: '../../filetypes/downloadgallery.gif'
   }, {
      title: 'Gallery 2',
      path: 'url/to/gallery2/',
      icon: '../../filetypes/downloadgallery.gif'
   }, {
      title: 'Gallery 3',
      path: 'url/to/gallery3/',
      icon: '../../filetypes/downloadgallery.gif'
   }, {
      title: 'Gallery 4',
      path: 'url/to/gallery4/',
      icon: '../../filetypes/imagegallery.gif'
   }, {
      title: 'Gallery 5',
      path: 'url/to/gallery5/',
      icon: '../../filetypes/imagegallery.gif'
   }, {
      title: 'Gallery 6',
      path: 'url/to/gallery6/',
      icon: '../../filetypes/imagegallery.gif'
   }, {
      title: 'Gallery 7',
      path: 'url/to/gallery7/',
      icon: '../../filetypes/imagegallery.gif'
   }, {
      title: 'Gallery 8',
      path: 'url/to/gallery8/',
      icon: '../../filetypes/imagegallery.gif'
   }];
   
   /**
    * Dummy content
    */
   var dummyCategories = cms.galleries.dummyCategories = [{
      title: 'Category 1',
      path: 'url/to/Category1/',
      icon: '../../filetypes/folder.gif',
      level: 0
   }, {
      title: 'Category 2',
      path: 'url/to/Category2/',
      icon: '../../filetypes/folder.gif',
      level: 1
   }, {
      title: 'Category 3',
      path: 'url/to/Category3/',
      icon: '../../filetypes/folder.gif',
      level: 2
   }, {
      title: 'Category 4',
      path: 'url/to/Category4/',
      icon: '../../filetypes/folder.gif',
      level: 1
   }, {
      title: 'Category 5',
      path: 'url/to/Category5/',
      icon: '../../filetypes/folder.gif',
      level: 0
   }, {
      title: 'Category 6',
      path: 'url/to/Category6/',
      icon: '../../filetypes/folder.gif',
      level: 1
   }, {
      title: 'Category 7',
      path: 'url/to/Category7/',
      icon: '../../filetypes/folder.gif',
      level: 2
   }, {
      title: 'Category 8',
      path: 'url/to/gallery8/',
      icon: '../../filetypes/folder.gif',
      level: 3
   }];
   
   /** Dummy Array with availabe types for galleries, should be configurable. */
   var dummyTypes = cms.galleries.dummyTypes = [8, 9, 10, 11, 12];
   
   /**
    * Init function for search/add dialog.
    */
   var initAddDialog = cms.galleries.initAddDialog = function() {
      // add galleries tab html
      var resultTab=$(cms.galleries.htmlTabResultSceleton).append($.fn.selectBox('generate',{values:[], select: function($this, self, value){}}));
      
      /*value="title.asc">Title Ascending</option>\
                     //           <option value="title.desc">Title Descending</option>\
                   //             <option value="type.asc">Type Ascending</option>\
                 //               <option value="type.desc">Type Descending</option>\
               //                 <option value="datemodified.asc">Date modifired Ascending</option>\
             //                   <option value="datemodified.desc">Date modifired Descending</option>\
           //                     <option value="path.asc">Path Ascending</option>\
         //                       <option value="path.desc">Path Descending</option>*/
      $('#' + cms.galleries.idTabs)
          .append(resultTab)
          .append(cms.galleries.htmlTabTypesSceleton)
          .append(cms.galleries.htmlTabGalleriesSceleton)
          .append(cms.galleries.htmlTabCategoriesSceleton)
          .append(cms.galleries.htmlTabFTSeachSceleton);
      
      // bind the select tab event, fill the content of the result tab on selection
      $('#' + cms.galleries.idTabs).tabs({
         select: function(event, ui) {
            // if result tab is selected
            if (ui.index == 0) {
               cms.galleries.fillResultTab();
            }
         }
      });
      
      
      cms.galleries.loadSearchLists();
      $('#' + cms.galleries.idTabs).tabs("select", 1);
      $('#' + cms.galleries.idTabs).tabs("disable", 0);
      
      // bind all other events at the end          
      // bind click, dbclick and hover events on items in criteria lists
      $('li.cms-list').live('dblclick', cms.galleries.dblclickListItem).live('click', cms.galleries.clickListItem).live('mouseover', function() {
         $(this).addClass(cms.galleries.classListItemHover);
      }).live('mouseout', function() {
         $(this).removeClass(cms.galleries.classListItemHover);
      });
      /* .hover(function() {
       $(this).addClass(cms.galleries.classListItemHover);
       }, function() {
       $(this).removeClass(cms.galleries.classListItemHover);
       });*/
      $('#searchInTitle, #searchInContent').click(function() {
         $(this).toggleClass(cms.galleries.classListItemActive);
      });
      
      $('#searchQuery > input').blur(function() {
         cms.galleries.searchObject.query = $(this).val();
         cms.galleries.searchObject.isChanged.query = true;
      });
      
      /** Bind the click event to drop-down box and sort the list */
      $('span.cms-drop-down option').click(function() {
         //alert('click');
         var criteria = $(this).closest('select').attr('name');
         var params = $(this).val().split(',');
         var sortedArray = sortList(cms.galleries.searchCriteriaListsAsJSON[criteria], params[0], params[1]);
         cms.galleries.refreshCriteriaList(sortedArray, criteria, params[0]);
      });
      
      $('li.cms-result-list-item > div').live('mouseover', function() {
         $(this).toggleClass('ui-state-hover', true);
      }).live('mouseout', function() {
         $(this).toggleClass('ui-state-hover', false);
      });
      
      // bind click events to remove search criteria html from result tab            
      $('span.cms-search-remove').live('click', cms.galleries.removeCriteria);
      
      // bind the hover and click events to the ok button under the criteria lists    
      $('.' + cms.galleries.classListOptions + ' button,.cms-search-options button').hover(function() {
         $(this).addClass('ui-state-hover');
      }, function() {
         $(this).removeClass('ui-state-hover');
      }).click(function() {
         //switch to result tab index = 0
         $('#' + cms.galleries.idTabs).tabs("enable", 0);
         $('#' + cms.galleries.idTabs).tabs('select', 0);
      });
   }
   
   
   
   /**
    * Add html for search criteria to result tab
    *
    * @param {String} content the nice-list of items for given search criteria
    * @param {String} searchCriteria the given search criteria
    */
   var addCreteriaToTab = cms.galleries.addCreteriaToTab = function(/** String*/content, /** String*/ searchCriteria) {
      var target = $('<span id="selected' + searchCriteria + '" class="cms-searchquery ui-widget-content ui-state-hover ui-corner-all"></span>').appendTo($('.cms-result-criteria'));
      target.append('<span class="cms-search-title">' + content + '</span>').append('<span class="cms-search-remove">&nbsp;</span>');
   }
   
   var configContentTypes = [1, 2, 3, 4, 5, 6, 7];
   
   /**
    * Loads the lists with available resource types, galleries ans categories via ajax call.
    * TODO: generalize to make it possible to load some preselected
    */
   var loadSearchLists = cms.galleries.loadSearchLists = function() {
      $.ajax({
         'url': vfsPathAjaxJsp,
         'data': {
            'action': 'all',
            'data': JSON.stringify({
               'types': configContentTypes
            })
         },
         'type': 'POST',
         'dataType': 'json',
         'success': cms.galleries.fillCriteriaTabs
      });
   }
   
   /**
    * Fills the list in the search criteria tabs.
    *
    * @param {Object} JSON map object
    */
   var fillCriteriaTabs = cms.galleries.fillCriteriaTabs = function(/**JSON*/data) {
      cms.galleries.searchCriteriaListsAsJSON = data;
      if (cms.galleries.searchCriteriaListsAsJSON.galleries) {
         cms.galleries.fillGalleries(cms.galleries.searchCriteriaListsAsJSON.galleries);
      }
      if (cms.galleries.searchCriteriaListsAsJSON.categories) {
         cms.galleries.fillCategories(cms.galleries.searchCriteriaListsAsJSON.categories, 'path');
      }
      if (cms.galleries.searchCriteriaListsAsJSON.types) {
         cms.galleries.fillTypes(cms.galleries.searchCriteriaListsAsJSON.types);
      }
      // TODO: go through html and the search object and mark the already selected search criteria     
   }
   
   /**
    * Loads the lists with available resource types, galleries and categories via ajax call.
    * TODO: generalize to make it possible to load some preselected
    */
   var loadSearchResults = cms.galleries.loadSearchResults = function() {
      cms.galleries.searchObject.page = 1;
      $.ajax({
         'url': vfsPathAjaxJsp,
         'data': {
            'action': 'search',
            'data': JSON.stringify({
               'querydata': cms.galleries.searchObject
            })
         },
         'type': 'POST',
         'dataType': 'json',
         'success': cms.galleries.fillResultList
      });
   }
   
   var fillResultList = cms.galleries.fillResultList = function(/**JSON*/data) {
      // remove old list with search results and pagination
      $('#results > ul').empty();
      $('div.result-pagination').empty();
      //alert("fillResultList");
      if (data.searchresult.resultcount > 0) {
         // display
         cms.galleries.fillResultPage(data.searchresult.resultlist, data.searchresult.resultpage);
         // initialize pagination for result list, if there are many pages                  
         if (data.searchresult.resultcount > cms.galleries.searchObject.matchesperpage) {
            var firsttime = true;
            $('div.result-pagination').pagination(data.searchresult.resultcount, {
               items_per_page: cms.galleries.searchObject.matchesperpage,
               callback: function(page_id, jq) {
                  if (!firsttime) {
                     var currentPage = page_id + 1; 
                     if ($('#searchresults_page' + currentPage).children().length == 0) {
                        // adjust the page_id in the search object and load search results for this page
                        cms.galleries.searchObject.page = currentPage;
                        $.ajax({
                           'url': vfsPathAjaxJsp,
                           'data': {
                              'action': 'search',
                              'data': JSON.stringify({
                                 'querydata': cms.galleries.searchObject
                              })
                           },
                           'type': 'POST',
                           'dataType': 'json',
                           'success': cms.galleries.fillGivenResultPage
                        });
                     }
                  } else {
                     firsttime = false;
                  }
               },
               prev_text: 'Prev',
               next_text: 'Next',
               prev_show_always: false,
               next_show_always: false,
               num_edge_entries: 1
            });
            // build the list with search results without pagination
         }
      } else {
            // handle empty list for search
      }
   }
     
   var fillResultPage = cms.galleries.fillResultPage = function(pageData, page_id) {
     // alert("fillResultPage: " + page_id);
      
      var target = $('#results > ul').empty().removeAttr('id').attr('id', 'searchresults_page' + page_id);
      $.each(pageData, function() {
         // $(target).text('Hallo');
         $(target).append(cms.galleries.listResultElement(this.title, this.path));
      });
   }
   
   var fillGivenResultPage = cms.galleries.fillGivenResultPage = function(pageData) {
      cms.galleries.fillResultPage(pageData.searchresult.resultlist, pageData.searchresult.resultpage);
      
   }
   
   /**
    * Fills the list in the search criteria tabs.
    *
    * @param {Object} JSON map object
    */
   var refreshCriteriaList = cms.galleries.refreshCriteriaList = function(/**Array*/sortedList, /**String*/ criteria, option) {
   
      if (criteria == 'galleries') {
         cms.galleries.refreshGalleries(sortedList);
      } else if (criteria == 'categories') {
         cms.galleries.refreshCategories(sortedList, option);
      } else if (criteria == 'types') {
         cms.galleries.refreshTypes(sortedList);
      }
      // TODO: go through html and the search object and mark the already selected search criteria     
   }
   
   /**
    * Creates the HTML for the list of available categories from the given JSON map data.
    * @param {Object} JSON map object with categories
    * @param {Object} optional flag to set the categories view. Schould be 'path' for hierarchal view.
    */
   var fillCategories = cms.galleries.fillCategories = function(/**Json*/categories, /**String*/ option) {
   
      // switch on or to switch off the hierarchic view
      var classActive = '';
      if (option == 'path') {
         classActive = 'cms-active-level';
      }
      //add the types to the list
      for (var i = 0; i < categories.length; i++) {
         $('#categories > ul').append(listCategoryElement(categories[i].title, categories[i].path, categories[i].level, classActive));
      }
      // set isChanged flag, so the search will be send to server
      cms.galleries.searchObject.isChanged.categories = true;
   }
   
   /**
    * Refreshes the order of the item in the list as given in parameter array.
    *
    * @param {JSON} categories the ordered list with categories
    * @param {String} optional flag to set the categories view. Schould be 'path' for hierarchal view.
    */
   var refreshCategories = cms.galleries.refreshCategories = function(/**JSON*/categories, /**String*/ option) {
   
      // set the flag to switch on or to switch off the hierarchic view
      var isActive = false;
      if (option == 'path') {
         isActive = true;
      }
      
      // reorder the item in the list      
      $.each(categories, function() {
         $("li[rel='" + this.path + "']").appendTo('#categories > ul').toggleClass('cms-active-level', isActive);
      });
   }
   
   /** 
    * Creates the HTML for the list of available galleries from the given JSON array data.
    *
    * Comment: this is an adjusted copy from galleryfunctions.js of the old galleries
    *
    * @param {Object} JSON object with categories
    */
   var fillGalleries = cms.galleries.fillGalleries = function(/**JSON*/galleries) {
      // add the galleries to the list
      for (key in galleries) {
         $('#galleries > ul').append(listGalleryElement(galleries[key].title, galleries[key].path));
         $('li[rel=' + galleries[key].path + ']').data('type', galleries[key].type);
      }
      // set isChanged flag, so the search will be send to server
      cms.galleries.searchObject.isChanged.galleries = true;
   }
   
   /**
    * Refreshes the order of galleries list as given in parameter array.
    *
    * @param {JSON} galleries the ordered list with galleries
    */
   var refreshGalleries = cms.galleries.refreshGalleries = function(/**JSON*/galleries) {
   
      // reorder the item in the list      
      $.each(galleries, function() {
         $("li[rel='" + this.path + "']").appendTo('#galleries > ul');
      });
   }
   
   /** 
    * Creates the HTML for the list of available types from the given JSON array data.
    *
    * @param {Object} JSON object with resource types
    */
   var fillTypes = cms.galleries.fillTypes = function(/**JSON*/types) {
      // add the types to the list
      for (var i = 0; i < types.length; i++) {
         var currType = types[i];
         $('#types > ul').append(listTypeElement(currType.title, currType.typeid, 'TODO_This is the description for this resource type'));
         $('li[rel=' + currType.typeid + ']').data('galleryTypes', currType.gallerytypeid);
      }
      // set isChanged flag, so the search will be send to server
      cms.galleries.searchObject.isChanged.types = true;
   }
   
   /**
    * Refreshes the order of types list as given in parameter array.
    *
    * @param {JSON} types the ordered list with types
    */
   var refreshTypes = cms.galleries.refreshTypes = function(/**JSON*/types) {
      // reorder the item in the list      
      $.each(types, function() {
         $("li[rel='" + this.typeid + "']").appendTo('#types > ul');
      });
   }
   
   /**
    * Adds the search criteria html to the result tab.
    */
   var fillResultTab = cms.galleries.fillResultTab = function() {
      var searchEnables = false;
      // display the search criteria
      $.each(cms.galleries.keysSearchObject, function() {
         var searchCriteria = this;
         
         if (cms.galleries.searchObject.isChanged[searchCriteria]) {
            // is true, if at least one criteria isChanged
            searchEnables = true;
            
            var singleSelect = cms.galleries.criteriaStr[searchCriteria][0];
            var multipleSelect = cms.galleries.criteriaStr[searchCriteria][1];
            var titles = '';
            // remove criteria button from result tab
            $('#selected' + searchCriteria).remove();
            
            if (searchCriteria == 'query') {
               var searchQuery = $('#searchQuery input').val();
               titles = singleSelect.concat(searchQuery);
               cms.galleries.addCreteriaToTab(titles, searchCriteria);
               cms.galleries.searchObject.isChanged[searchCriteria] = false;
            } else {
               var selectedLis = $('#' + searchCriteria).find('.' + cms.galleries.classListItemActive).find('.' + cms.galleries.classListItemTitle);
               // if any search criteria is selected
               if (selectedLis.length == 1) {
                  titles = singleSelect.concat($(selectedLis[0]).text());
                  cms.galleries.addCreteriaToTab(titles, searchCriteria);
               } else if (selectedLis.length > 1) {
                  $.each(selectedLis, function() {
                     if (titles.length == 0) {
                        titles = multipleSelect.concat($(this).text());
                     } else {
                        titles = titles.concat(", ").concat($(this).text());
                     }
                  });
                  cms.galleries.addCreteriaToTab(titles, searchCriteria);
               }
               cms.galleries.searchObject.isChanged[searchCriteria] = false;
            }
         }
      });
      
      // display the search results
      if (searchEnables) {
         cms.galleries.loadSearchResults();
      }
      
      
   }
   
   /**
    * Callback function for the one click event in the gallery list
    */
   var clickListItem = cms.galleries.clickListItem = function() {
   
      // id of the li tag and type of search 
      var itemId = $(this).attr('rel');
      var itemCriteria = $(this).closest('div').attr('id');
      
      // adjust the active status of the gallery in the gallery list        
      var index = $.inArray(itemId, cms.galleries.searchObject[itemCriteria]);
      // case 1: gallery is selected, -> deselect the gallery on second click
      if (index != -1) {
         // remove gallery path from search object
         cms.galleries.searchObject[itemCriteria].splice(index, 1);
         // remove highlighting
         $(this).removeClass(cms.galleries.classListItemActive);
         
         // set isChanged flag, so the search will be send to server
         cms.galleries.searchObject.isChanged[itemCriteria] = true;
         
         
         // case 2: gallery is not selected yet, -> select the gallery on click
      } else {
         // push the gallery path to the search object
         cms.galleries.searchObject[itemCriteria].push(itemId);
         // add highlighting
         $(this).addClass(cms.galleries.classListItemActive);
         
         $('#' + cms.galleries.idTabs).tabs("enable", 0);
         
         // set isChanged flag, so the search will be send to server
         cms.galleries.searchObject.isChanged[itemCriteria] = true;
         
      }
   }
   
   /**
    * Callback function for the double click event in the search criteria list
    */
   var dblclickListItem = cms.galleries.dblclickListItem = function() {
   
      // id of the li tag and type of search 
      var itemId = $(this).attr('rel');
      var itemCriteria = $(this).closest('div').attr('id');
      
      // remove all selected items from search object
      cms.galleries.removeItemsFromSearchObject(itemCriteria);
      // add selected item to search object and set highlighting
      cms.galleries.searchObject[itemCriteria].push(itemId);
      $(this).addClass(cms.galleries.classListItemActive);
      // set isChanged flag, so the search will be send to server on 'select' tab event
      cms.galleries.searchObject.isChanged[itemCriteria] = true;
      
      //switch to result tab index = 0
      $('#' + cms.galleries.idTabs).tabs("enable", 0);
      $('#' + cms.galleries.idTabs).tabs('select', 0);
   }
   
   /**
    * Removes all selected items from the search object and adjust highlighting
    *
    * @param {String} searchCriteria key
    */
   var removeItemsFromSearchObject = cms.galleries.removeItemsFromSearchObject = function(/**String*/searchCriteria) {
      // if at least one item is selected
      if (cms.galleries.searchObject[searchCriteria].length > 0) {
         if (searchCriteria == 'query') {
            $('#searchQuery input').val('');
            // remove all items from search object                                                     
            cms.galleries.searchObject[searchCriteria] = '';
         } else {
            //remove highlighting for all selected items in the list
            $('#' + searchCriteria + ' li.cms-list').removeClass(cms.galleries.classListItemActive);
            // remove all items from search object                                                     
            cms.galleries.searchObject[searchCriteria] = [];
         }
         // refresh the searchResults
         cms.galleries.loadSearchResults();
      }
   }
   
   /**
    * Removes the html of the search criteria from the result tab and from search object
    */
   var removeCriteria = cms.galleries.removeCriteria = function() {
      var parentId = $(this).parent().attr('id');
      $.each(cms.galleries.keysSearchObject, function() {
         var selectedId = 'selected' + this;
         if (parentId == selectedId) {
            $('#' + selectedId).remove();
            cms.galleries.removeItemsFromSearchObject(this);
         }
      });
      
      // TODO: refresh the list of search results on the result tab
   }
   
   /**
    * Sorts the array of objects by given key and order.
    *
    * @param {Array} list the array to be sorted
    * @param {String} sortBy the name of the key
    * @param {String} sortOrder the sort order. It can be 'asc'(default) or 'desc'
    */
   var sortList = function(/** Array */list, /**String*/ sortBy, /**String*/ sortOrder) {
      var sortedArray = list;
      
      /*$.each(sortedArray, function(){
       alert(this.title);
       });*/
      if (sortOrder == 'asc' || sortOrder == null) {
         // alert('asc');
         sortedArray.sort(function(a, b) {
            if (a[sortBy] < b[sortBy]) {
               return -1;
            } else if (a[sortBy] > b[sortBy]) {
               return 1;
            } else {
               /*if (a['title'] < b['title']){
                return -1 ;
                } else if (a['title'] > b['title']) {
                return 1;
                } else {
                return 0;
                } */
               return 0;
            }
         });
      } else {
         //alert('desc');
         sortedArray.sort(function(a, b) {
            if (a[sortBy] > b[sortBy]) {
               return -1;
            } else if (a[sortBy] < b[sortBy]) {
               return 1;
            } else {
               /*if (a['title'] > b['title']){
                return -1 ;
                } else if (a['title'] < b['title']) {
                return 1;
                } else {
                return 0;
                } */
               return 0;
            }
         });
         
      }
      
      // alert('Nachher \n');
      /* $.each(sortedArray, function(){
       alert(this.title);
       });*/
      return sortedArray;
   }
   
   
})(cms);
