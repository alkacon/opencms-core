(function(cms) {    

   /** A map with all available content handlers. */
   var contentTypeHandlers = cms.galleries.contentTypeHandlers = {};
    
   /** html-id for tabs. */
   var idTabs = cms.galleries.idTabs = 'cms-gallery-tabs';
   /** html-id for tabs. */
   var idGalleriesMain = cms.galleries.idGalleriesMain = 'cms-gallery-main';   
   
   /** html-id for the tab with search results. */
   var idTabResult = cms.galleries.idTabResult = 'tabs-result';
   
   var arrayOfTabIds = cms.galleries.arrayOfTabIds =  {
       'tabs-result': 0,
       'tabs-types':  1, 
       'tabs-galleries': 2,
       'tabs-categories': 3,
       'tabs-fulltextsearch':4
   };
   
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
      tabid: 1,
      page: 1,
      searchfields: '',
      matchesperpage: 8,
      isChanged: {
         types: false,
         galleries: false,
         categories: false,
         query: false
      }
   };
   
   /** Saves the initial list of all available search criteria from server. */
   var searchCriteriaListsAsJSON = cms.galleries.searchCriteriaListsAsJSON = {};
   
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
                            <label>Sort by:&nbsp;</label>\
                        </span>\
             </div>\
             <div id="results" class="cms-list-scrolling ui-corner-all result-tab-scrolling">\
                        <ul class="cms-list-scrolling-innner"></ul>\
             </div>\
             <div class="result-pagination"></div>\
         </div>';
   
   /** html fragment for the tab with the types' list. */
   var htmlTabTypesSceleton = cms.galleries.htmlTabTypesSceleton = '<div id="tabs-types">\
                <div id="typesoptions" class="ui-widget ' + cms.galleries.classListOptions + '">\
                    <span class="cms-drop-down">\
                        <label>Sort by:</label>\
                    </span>\
                    <span class="cms-ft-search"><label>Search:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                </div>\
                <div id="types" class="cms-list-scrolling ui-corner-all criteria-tab-scrolling">\
                    <ul class="cms-list-scrolling-innner"></ul>\
                </div>\
              </div>';
   
   /** html fragment for the tab with the galleries' list. */
   var htmlTabGalleriesSceleton = cms.galleries.htmlTabGalleriesSceleton = '<div id="tabs-galleries">\
                <div id="galleriesoptions" class="ui-widget ' + cms.galleries.classListOptions + '">\
                    <span class="cms-drop-down">\
                        <label>Sort by:</label>\
                    </span>\
                    <span class="cms-ft-search"><label>Search:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                </div>\
                <div id="galleries" class="cms-list-scrolling ui-corner-all criteria-tab-scrolling">\
                    <ul class="cms-list-scrolling-innner"></ul>\
                </div>\
              </div>';
   
   /** html fragment for the tab with the categories' list. */
   var htmlTabCategoriesSceleton = cms.galleries.htmlTabCategoriesSceleton = '<div id="tabs-categories">\
                <div id="categoriesoptions" class="ui-widget ' + cms.galleries.classListOptions + '">\
                    <span class="cms-drop-down">\
                        <label>Sort by:</label>\
                    </span>\
                    <span class="cms-ft-search"><label>Search:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                </div>\
                <div id="categories" class="cms-list-scrolling ui-corner-all criteria-tab-scrolling">\
                    <ul class="cms-list-scrolling-innner"></ul>\
                </div>\
              </div>';
   
   /** html fragment for the tab with the types' list. */
   var htmlTabFTSeachSceleton = cms.galleries.htmlTabFTSeachSceleton = '<div id="tabs-fulltextsearch">\
             <div class="cms-search-panel ui-corner-all">\
                    <div class="ui-widget cms-search-options">\
                        <span id="searchQuery" class="cms-item-left"><label>Search for:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                    </div>\
                    <div class="ui-widget cms-search-options">\
                        <div class="cms-item-left">Search in:</div>\
                        <div id="searchInTitle" class="cms-list-checkbox"></div>\
                        <div class="cms-checkbox-label">Title</div>\
                        <div id="searchInContent" class="cms-list-checkbox"></div>\
                        <div class="cms-checkbox-label">Content</div>\
                    </div>\
                    <div class="ui-widget cms-search-options">\
                        <span id="searchBefore" class="cms-item-left cms-input-date"><label>Changed after:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                        <span id="searchBefore" class="cms-item-left cms-input-date"><label>Changed before:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                    </div>\
                    <div class="cms-search-options">\
                        <button class="ui-state-default ui-corner-all cms-item-left-bottom">Search</button>\
                    </div>\
             </div>\
          </div>';    
   
  
      
   /**
    * Init function for search/add dialog.
    * 
    * @param {Object} requestData the request parameter
    */
   var initAddDialog = cms.galleries.initAddDialog = function(requestData) {
      // handle the request parameter and initialize the search object
      var initTabId = cms.galleries.arrayOfTabIds['tabs-types'];
      if (requestData) {
          if (requestData.querydata) {
              if (requestData.querydata.galleries) {
                  cms.galleries.searchObject['galleries'] = requestData.querydata.galleries;
                  cms.galleries.searchObject.isChanged.galleries = true;
              }
              if (requestData.querydata.categories) {
                  cms.galleries.searchObject['categories'] = requestData.querydata.categories;
                  cms.galleries.searchObject.isChanged.categories = true;
              }
              /*if (requestData.querydata.types) {
                  cms.galleries.searchObject['types'] = requestData.querydata.types;
                  cms.galleries.searchObject.isChanged.types = true;
              }*/          
              if (requestData.querydata.query) {
                  cms.galleries.searchObject['query'] = requestData.querydata.query;
                  cms.galleries.searchObject.isChanged.query = true;
              }
              if (requestData.querydata.tabid) {
                  cms.galleries.searchObject['tabid'] = requestData.querydata.tabid;                 
              }
          }
          // TODO: remove if tabid moved to searchobject
          if (requestData.tabid) {
              cms.galleries.searchObject['tabid'] = cms.galleries.arrayOfTabIds[requestData.tabid];
          }                    
      }
      
      // init tabs for add dialog
      var resultTab = $(cms.galleries.htmlTabResultSceleton);
      resultTab.find('.cms-drop-down label').after($.fn.selectBox('generate',{
          values:[
              {value: 'title.desc',title: 'Title Ascending'}, 
              {value: 'title.desc',title: 'Title Descending'}, 
              {value: 'type.asc',title: 'Type Ascending'}, 
              {value: 'type.desc',title: 'Type Descending'}, 
              {value: 'datemodified.asc',title: 'Date Ascending'},
              {value: 'datemodified.desc',title: 'Date Descending'},
              {value: 'path.asc',title: 'Path Ascending'},
              {value: 'path.desc',title: 'Path Descending'}
          ],
          width: 150,
          /* TODO: bind sort functionality */
          select: function($this, self, value){$(self).closest('div.cms-list-options').attr('id')}}));
      // TODO blind out for the moment
      resultTab.find('.cms-drop-down').css('display','none');
      
      var typesTab = $(cms.galleries.htmlTabTypesSceleton);
      typesTab.find('.cms-drop-down label').after($.fn.selectBox('generate',{
          values:[
              {value: 'title,asc',title: 'Title Ascending'}, 
              {value: 'title,desc',title: 'Title Descending'}         
          ],
          width: 150,
          /* bind sort functionality to selectbox */
          select: function($this, self, value){
                  var criteria = $(self).closest('div.cms-list-options').attr('id');
                  criteria = criteria.replace('options', '');
                  var params = value.split(',');
                  var sortedArray = sortList(cms.galleries.searchCriteriaListsAsJSON[criteria], params[0], params[1]);
                  cms.galleries.refreshCriteriaList(sortedArray, criteria, params[0]);
              }
          })); 
          
      var galleriesTab = $(cms.galleries.htmlTabGalleriesSceleton);
      galleriesTab.find('.cms-drop-down label').after($.fn.selectBox('generate',{
          values:[
              {value: 'title,asc',title: 'Title Ascending'}, 
              {value: 'title,desc',title: 'Title Descending'},
              {value: 'gallerytypeid,asc',title: 'Type Ascending'}, 
              {value: 'gallerytypeid,desc',title: 'Type Descending'}          
          ],
          width: 150,
          /* bind sort functionality to selectbox */
          select: function($this, self, value){
                  var criteria = $(self).closest('div.cms-list-options').attr('id');
                  criteria = criteria.replace('options', '');
                  var params = value.split(',');
                  var sortedArray = sortList(cms.galleries.searchCriteriaListsAsJSON[criteria], params[0], params[1]);
                  cms.galleries.refreshCriteriaList(sortedArray, criteria, params[0]);
              }
          }));
      
      var categoriesTab = $(cms.galleries.htmlTabCategoriesSceleton);
      categoriesTab.find('.cms-drop-down label').after($.fn.selectBox('generate',{
          values:[
              {value: 'path,asc',title: 'Hierarchy'},
              {value: 'title,asc',title: 'Title Ascending'}, 
              {value: 'title,desc',title: 'Title Descending'}         
          ],
          width: 150,
          /* bind sort functionality to selectbox */
          select: function($this, self, value){
                  var criteria = $(self).closest('div.cms-list-options').attr('id');
                  criteria = criteria.replace('options', '');
                  var params = value.split(',');
                  var sortedArray = sortList(cms.galleries.searchCriteriaListsAsJSON[criteria], params[0], params[1]);
                  cms.galleries.refreshCriteriaList(sortedArray, criteria, params[0]);
              }
          }));  

      // add tabs html to tabs
      $('#' + cms.galleries.idTabs)
          .append(resultTab)
          .append(typesTab)
          .append(galleriesTab)
          .append(categoriesTab)
          .append(cms.galleries.htmlTabFTSeachSceleton);
          
      cms.galleries.loadSearchLists();
      
      //TODO: blind out quick search dialog for the moment
      $('span.cms-ft-search').css('display', 'none');
      
      // bind the select tab event, fill the content of the result tab on selection
      $('#' + cms.galleries.idTabs).tabs({
         select: function(event, ui) {
             // if result tab is selected
            if (ui.index == 0) {
                cms.galleries.fillResultTab();
            }
        },
        selected: 1
      });
                  
      // bind all other events at the end          
      // bind click, dbclick events on items in criteria lists
      $('#types li.cms-list, #galleries li.cms-list, #categories li.cms-list')
          .live('dblclick', cms.galleries.dblclickListItem)
          .live('click', cms.galleries.clickListItem);
      // bind dbclick event to the items in the result list
      $('#results li.cms-list').live('dblclick', cms.galleries.dblclickToShowPreview);
          
      // bind hover event on items in criteria and result lists
      $('li.cms-list')
          .live('mouseover', function() {
             $(this).addClass(cms.galleries.classListItemHover);
          }).live('mouseout', function() {
             $(this).removeClass(cms.galleries.classListItemHover);
      });        
      
      // add active class to checkbox of search tab  
      $('#searchInTitle, #searchInContent')
          .click(function() {
             $(this).toggleClass(cms.galleries.classListItemActive);
          });
      
      $('#searchQuery > input').blur(function() {
         cms.galleries.searchObject.query = $(this).val();
         cms.galleries.searchObject.isChanged.query = true;
      });           
         
      // bind click events to remove search criteria html from result tab            
      $('div.cms-search-remove').live('click', cms.galleries.removeCriteria);
      
      // bind the hover and click events to the ok button under the criteria lists    
      $('.cms-search-options button').hover(function() {
         $(this).addClass('ui-state-hover');
      }, function() {
         $(this).removeClass('ui-state-hover');
      }).click(function() {
         //switch to result tab index = 0
         $('#' + cms.galleries.idTabs).tabs("enable", 0);
         $('#' + cms.galleries.idTabs).tabs('select', 0);
      });          
           
      // add default content handler
      cms.galleries.contentTypeHandlers['default']= cms.previewhandler.defaultContentTypeHandler;           
   }
   
   
   
   /**
    * Add html for search criteria to result tab
    *
    * @param {String} content the nice-list of items for given search criteria
    * @param {String} searchCriteria the given search criteria
    */
   var addCreteriaToTab = cms.galleries.addCreteriaToTab =  function(/** String*/content, /** String*/ searchCriteria) {

      var target = $('<span id="selected' + searchCriteria + '" class="cms-criteria ui-widget-content ui-state-hover ui-corner-all"></span>')
          .appendTo($('.cms-result-criteria'));
      target.append('<div class="cms-search-title">' + content + '</div>')
          .append('<div class="cms-search-remove ui-icon ui-icon-closethick ui-corner-all"></div>');
   }

   var configContentTypes = [1, 2, 3, 4, 6, 7, 146, 147, 149];
   
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
         markSelectedCriteria('galleries');
      }
      if (cms.galleries.searchCriteriaListsAsJSON.categories) {
         cms.galleries.fillCategories(cms.galleries.searchCriteriaListsAsJSON.categories, 'path');
         markSelectedCriteria('categories');
      }
      if (cms.galleries.searchCriteriaListsAsJSON.types) {
         cms.galleries.fillTypes(cms.galleries.searchCriteriaListsAsJSON.types);
         markSelectedCriteria('types');
      }     
      // open the preselected tab
      $('#' + cms.galleries.idTabs).tabs('select', cms.galleries.searchObject.tabid);
      
      // TODO: insert the preselected search query!!!!
           
   }
   
   
   var markSelectedCriteria = function (/**String*/criteria) {       
       if (cms.galleries.searchObject[criteria]) {
             $.each(cms.galleries.searchObject[criteria], function() {
                 var path = this;
                 $('li[rel=' + path + ']').addClass(cms.galleries.classListItemActive);
             });
       }
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
      var resultCriteriaHeight = $('.cms-result-criteria').height();
      var scrollingHeight = 290;
      if(resultCriteriaHeight > 30 && resultCriteriaHeight < 80) {
          scrollingHeight = 265;
      } else if (resultCriteriaHeight > 79 && resultCriteriaHeight < 110) {
          scrollingHeight = 235;
      } else if (resultCriteriaHeight > 111) {
          scrollingHeight = 210;
      }      
      $('#results').height(scrollingHeight);
                      
      if (data.searchresult.resultcount > 0) {
         // display
         cms.galleries.fillResultPage(data);
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
                           'success': cms.galleries.fillResultPage
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
         }
      } else {
            // handle empty list for search
      }
   }
     
   var fillResultPage = cms.galleries.fillResultPage = function(pageData) {           
      var target = $('#results > ul').empty().removeAttr('id').attr('id', 'searchresults_page' + pageData.searchresult.resultpage);
      $.each(pageData.searchresult.resultlist, function() {
          $(this.itemhtml).appendTo(target).attr('rel', this.path);                              
          $('li[rel=' + this.path + ']').data('type', this.type);  
      });           
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
         $(categories[i].itemhtml).appendTo('#categories > ul')
             .attr('rel', categories[i].path).addClass(classActive + ' ' + classConstLevel + categories[i].level).addClass('cms-list-with-checkbox')
             .prepend('<div class="cms-list-checkbox"></div>');
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
      for (var i = 0; i < galleries.length; i++) {                  
         $(galleries[i].itemhtml).appendTo('#galleries > ul')
             .attr('rel', galleries[i].path).addClass('cms-list-with-checkbox')
             .prepend('<div class="cms-list-checkbox"></div>');
         $('li[rel=' + galleries[i].path + ']').data('type', galleries[i].type);
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
         $(types[i].itemhtml).appendTo('#types > ul')
             .attr('rel', types[i].typeid).addClass('cms-list-with-checkbox')
             .prepend('<div class="cms-list-checkbox"></div>');
         $('li[rel=' + types[i].typeid + ']').data('gallerytypeid', types[i].gallerytypeid);
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
               var selectedLis = cms.galleries.searchObject[searchCriteria];
               // if any search criteria is selected
               if (selectedLis) {
                   if (selectedLis.length == 1) {                       
                       titles = singleSelect.concat($('li[rel=' + selectedLis[0] + ']').find('.cms-list-title').text());
                       cms.galleries.addCreteriaToTab(titles, searchCriteria);
                   } else if (selectedLis.length > 1) {
                      $.each(selectedLis, function() {
                      if (titles.length == 0) {
                          titles = multipleSelect.concat($('li[rel=' + this + ']').find('.cms-list-title').text());
                      } else {
                          titles = titles.concat(", ").concat($('li[rel=' + this + ']').find('.cms-list-title').text());
                      }
                  });
                  cms.galleries.addCreteriaToTab(titles, searchCriteria);
               }
               cms.galleries.searchObject.isChanged[searchCriteria] = false;
               }              
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
          // CASE 1: gallery is selected, -> deselect the gallery on second click
          if (index != -1) {
             // remove gallery path from search object
             cms.galleries.searchObject[itemCriteria].splice(index, 1);
             // remove highlighting
             $(this).removeClass(cms.galleries.classListItemActive);
             
             // set isChanged flag, so next search will be send to server
             cms.galleries.searchObject.isChanged[itemCriteria] = true;
             // TODO: show galleries, which were hidden for disselected type
                          
             // CASE 2: gallery is not selected yet, -> select the gallery on click
          } else {
             // push the gallery path to the search object
             cms.galleries.searchObject[itemCriteria].push(itemId);
             // add highlighting
             $(this).addClass(cms.galleries.classListItemActive);             
             
             // set isChanged flag, so next search will be send to server
             cms.galleries.searchObject.isChanged[itemCriteria] = true;
             
             // TODO: hide galleries, which are not availabe for selectes type
             /*if (itemCriteria == 'types') {
                 var galleryTypes = $(this).data('gallerytypeid');
                 var galleries = $('#galleries li').data('type');
             } */            
          }     
   }
   
   /**
    * Callback function for the double click event in the search criteria list
    */
   var dblclickListItem = cms.galleries.dblclickListItem = function() {   
     
      // id of the li tag and type of search 
      var itemId = $(this).attr('rel');
      var itemCriteria = $(this).closest('div').attr('id');
      
      // adjust the active status of the gallery in the gallery list        
      var index = $.inArray(itemId, cms.galleries.searchObject[itemCriteria]);
      // case 1: gallery is not selected, -> select the gallery
      if (index == -1){
         // push the gallery path to the search object
         cms.galleries.searchObject[itemCriteria].push(itemId);
         // add highlighting
         $(this).addClass(cms.galleries.classListItemActive);         
         
         // set isChanged flag, so the search will be send to server
         cms.galleries.searchObject.isChanged[itemCriteria] = true;
         
      }      
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
      
      if (sortOrder == 'asc' || sortOrder == null) {
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
      return sortedArray;
   }
   
  /** 
   * Callback function for dbclick event on the item in the result list.
   * 
   */
  var dblclickToShowPreview = cms.galleries.dblclickToShowPreview = function() {
      // retrieve the resource id
      var itemId = $(this).attr('rel');
      
      // set the resouce id as rel attribute and empty the content of the preview
      $('#cms-preview').attr('rel', itemId);
      $('#cms-preview div.preview-area, #cms-preview div.edit-area').empty();
      
      // retrieve the resource type and load the preview      
      var itemType = $(this).data('type');
      loadItemPreview(itemId, itemType);
      
      
      // work around to prevent double loading for just opened preview
      /*var currPreviewId = $('#cms-preview').attr('rel');
      if (currPreviewId == null) {
          $('#cms-preview').attr('rel',itemId);
          loadItemPreview(itemId); 
      } else if (currPreviewId != null || itemId != currPreviewId) {
          $('#cms-preview').attr('rel', itemId);
          $('#cms-preview div.preview-area, #cms-preview div.edit-area').empty();
          loadItemPreview(itemId);
      } else {
          $('#cms-preview').fadeIn('slow');
      } */     
  } 
     
    /**
     * Ajax call for the content of the item preview.
     * 
     * @param {Object} itemId the path to the given resource
     * @param {Object} itemType the type of the resource
     */      
    var loadItemPreview = cms.galleries.loadItemPreview = function(/**String*/ itemId, /**String*/itemType) {
      
      $.ajax({
         'url': vfsPathAjaxJsp,
         'data': {
            'action': 'preview',
            'data': JSON.stringify({
               'path': itemId
            })
         },
         'type': 'POST',
         'dataType': 'json',
         'success': cms.galleries.getContentHandler(itemType)['init']
      });
    } 
    
    var setParamter = function () {
        return '';
    }    
  
        
    /**
     * Adds a new specific content handler for thr specified resource type.
     * 
     * @param {Object} typeId the type id of the resource
     * @param {Object} handler the specific handler for this resource
     */   
    var addContentTypeHandler = function(typeId, handler){
        cms.galleries.contentTypeHandlers[typeId]= $.extend({}, cms.previewhandler.defaultContentTypeHandler, handler);
    }


    /**
     * Returns the specifired handler for a resource type or the default handler.
     * 
     * @param {Object} typId the resource type id
     */
    var getContentHandler = cms.galleries.getContentHandler = function(typeId){
        if (typeId && cms.galleries.contentTypeHandlers[typeId]){
            return cms.galleries.contentTypeHandlers[typeId];
        }
        return cms.galleries.contentTypeHandlers['default'];
    }   
   
})(cms);
