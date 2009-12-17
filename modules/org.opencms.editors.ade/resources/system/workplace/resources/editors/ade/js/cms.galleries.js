(function(cms) {    

   /** A map with all available content handlers. */
   var contentTypeHandlers = cms.galleries.contentTypeHandlers = {};
    
   /** Array with resource types available for this galleries dialog. */
   var configContentTypes = cms.galleries.configContentTypes = [];
    
   /** html-id for tabs. */
   var idTabs = cms.galleries.idTabs = 'cms-gallery-tabs';
   
   /** html-id for tabs. */
   var idGalleriesMain = cms.galleries.idGalleriesMain = 'cms-gallery-main';   
   
   /** html-id for the tab with search results. */
   var idTabResult = cms.galleries.idTabResult = 'tabs-result';
   
   /** A Map of tab ids. */
   var arrayOfTabIds = cms.galleries.arrayOfTabIds =  {
       'tabs-result': 0,
       'tabs-types':  1, 
       'tabs-galleries': 2,
       'tabs-categories': 3,
       'tabs-fulltextsearch':4
   };
   
   /** 
    * 'dialogMode': The current mode of the dialog. It can be 'widget','editor','ade', 'sitemap' or 'view'.
    * 'fieldId': The field id of the input field inside of the xmlcontent. 
    */
   var initValues = cms.galleries.initValues = {
       'dialogMode': null,
       'fieldId'   : null
   };
   
   /** 
    * 'path': path to the image, also used as id for the preview
    * 'isInitial': flag to indicate is the the resource is loaded from xmlContent
    */
   var activeItem = cms.galleries.activeItem = {
       'path': '',
       'isInitial': false       
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
    * 'types': array of resource ids for the selected resource types
    * 'galleries': array of paths to the selected galleries
    * 'categories': array of paths to the selected categories
    * 'query': the search key word
    * 'tabid'; the currently selected tab
    * 'page': the page number of the requested result page
    * 'searchfields':
    * 'matchesperpage': the number of items pro result page
    * 'isChanged': map of flags indicating if one of the search criteria is changed and should be taken into account. It is used internally.
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
      types: ['<b>Type:&nbsp;</b>', '<b>Types:&nbsp;</b>'],
      galleries: ['<b>Gallery:&nbsp;</b>', '<b>Galleries:&nbsp;</b>'],
      categories: ['<b>Category:&nbsp;</b>', '<b>Categories:&nbsp;</b>'],
      query: ['<b>Search query:&nbsp;</b>', 'Seach queries:&nbsp;</b>']
   
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
                        <ul class="cms-list-scrolling-innner cms-item-list"></ul>\
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
                    <ul id="'+cms.html.galleryTypeListId+'" class="cms-list-scrolling-innner cms-item-list"></ul>\
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
                    <div class="cms-search-options"><b>Search the offline-index:</b></div>\
                    <div class="cms-search-options">\
                        <span id="searchQuery" class="cms-item-left"><label>Search for:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                    </div>\
                 <!--   <div class="cms-search-options">\
                        <div class="cms-item-left">Search in:</div>\
                        <div id="searchInTitle" class="cms-list-checkbox"></div>\
                        <div class="cms-checkbox-label">Title</div>\
                        <div id="searchInContent" class="cms-list-checkbox"></div>\
                        <div class="cms-checkbox-label">Content</div>\
                    </div>\
                    <div class="cms-search-options">\
                        <span id="searchBefore" class="cms-item-left cms-input-date"><label>Changed after:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                        <span id="searchBefore" class="cms-item-left cms-input-date"><label>Changed before:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                    </div> -->\
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
   var initAddDialog = cms.galleries.initAddDialog = function(tabsContent, requestData) {       
      // handle the request parameter:
      // initialize the search object and the initial search 
      var initSearchResult = null;          
      if (requestData) {
          cms.galleries.setSearchObject(requestData);    
          if (requestData['searchresult']) {
              initSearchResult = requestData;
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
      // add preview to the galleries html
      $('#' + cms.galleries.idGalleriesMain).append(cms.previewhandler.htmlPreviewSceleton);                  
      
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
        selected: cms.galleries.searchObject['tabid']    // should be the result tab, so the it does not have to switch
      });
      
      
                  
      // removing ui-widget-header and ui-corner-all from ui-tabs-nav for layout reasons
      $('#' + cms.galleries.idGalleriesMain + ' .ui-tabs-nav').removeClass('ui-widget-header').removeClass('ui-corner-all');
               
      // bind all other events at the end          
      // bind click, dbclick events on items in criteria lists
      $('#types li.cms-list, #galleries li.cms-list, #categories li.cms-list')
          .live('dblclick', cms.galleries.dblclickListItem)
          .live('click', cms.galleries.clickListItem);
      // bind dbclick event to the items in the result list
      $('#results li.cms-list').live('dblclick', cms.galleries.dblclickToShowPreview);
      $('#results li.cms-list').live('click', cms.galleries.clickResultItem); 
      
      // load content of the search criteris tabs    
      cms.galleries.loadSearchLists(tabsContent, initSearchResult);     
          
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
         var currentQuery = $(this).val();         
         // update the search object, if at least one search character is given 
         // or the query string is just deleted          
         if (currentQuery.match(/[^\s]/) 
                 || currentQuery.match(/\s*/g) && cms.galleries.searchObject.query.match(/[^\s]/)) {
                      cms.galleries.searchObject.query = currentQuery;
                     cms.galleries.searchObject.isChanged.query = true;     
                 }         
      });           
         
      // bind click events to the close button of the search criteria on the result tab            
      $('div.cms-search-remove').live('click', cms.galleries.removeCriteria);
      
      // bind the hover and click events to the ok button on the full text search tab 
      $('.cms-search-options button').hover(function() {
         $(this).addClass('ui-state-hover');
      }, function() {
         $(this).removeClass('ui-state-hover');
      }).click(function() {
         //switch to result tab index = 0
         $('#' + cms.galleries.idTabs).tabs("enable", 0);
         $('#' + cms.galleries.idTabs).tabs('select', 0);
      });          
      
      // bind click event to the select-button of the item in the result list(dialogmode = widget|editor)     
      $('.cms-handle-button.cms-select-item').live('click',function(e){        
          var itemType = $(this).closest('li').data('type');          
          var itemId = $(this).closest('li').attr('alt');          
          cms.galleries.getContentHandler(itemType)['setValues'][cms.galleries.initValues['dialogMode']](itemId, cms.galleries.initValues['fieldId']);
          // avoid event propagation to the surround 'li'
          e.stopPropagation();                    
      });
           
      $('.cms-item a.ui-icon').live('click', cms.galleries.toggleAdditionalInfo);                       
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
    
  /**
    * Loads the lists with available resource types, galleries and categories via ajax call.    
    */
   var loadSearchLists = cms.galleries.loadSearchLists = function(tabsContent, initSearchResult) {
      if (tabsContent) {
          cms.galleries.fillCriteriaTabs(tabsContent, "success", initSearchResult);
      } else {
          $.ajax({
             'url': vfsPathAjaxJsp,
             'data': {
                'action': 'all',
                'data': JSON.stringify({
                   'types': cms.galleries.configContentTypes
                })
             },
             'type': 'POST',
             'dataType': 'json',
             'success': cms.galleries.fillCriteriaTabs
          });   
      }
   }
   
   /**
    * Fills the list in the search criteria tabs.
    *
    * @param {Object} JSON map object
    */
   var fillCriteriaTabs = cms.galleries.fillCriteriaTabs = function(/**JSON*/data, message, initSearchResult) {       
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
         cms.galleries.configContentTypes = cms.galleries.searchCriteriaListsAsJSON.typeids;
         cms.galleries.fillTypes(cms.galleries.searchCriteriaListsAsJSON.types);
         markSelectedCriteria('types');
      }
      
      if (initSearchResult) {
          fillResultTab(initSearchResult);          
      } else {
          // open the preselected tab
          $('#' + cms.galleries.idTabs).tabs('select', cms.galleries.searchObject.tabid);    
      }       
   }
   
   /**
    * Marks the preloaded selected criteria set in the search object
    * 
    * @param {Object} criteria the search criteria (galleries, categories or types) to be selected
    */
   var markSelectedCriteria = function (/**String*/criteria) {       
       if (cms.galleries.searchObject[criteria]) {
             $.each(cms.galleries.searchObject[criteria], function() {
                 var path = this;
                 $('li[alt=' + path + ']').addClass(cms.galleries.classListItemActive);
             });
       }
   }   
   
   /**
    * Handle different states of the search object, so the search is consistent.
    */
   var prepareSearchObject = cms.galleries.prepareSearchObject = function() {
       var preparedSearchObject = {};
       var types = {};
       
       // add the available types to the search object used for next search, 
       // if the criteria for types are empty
       if (cms.galleries.searchObject['types'].length == 0) {
           // no galleries is selected
           if (cms.galleries.searchObject['galleries'].length == 0) {               
               return $.extend(preparedSearchObject, cms.galleries.searchObject, {'types':cms.galleries.configContentTypes});
           // at least one gallery is selected                              
           } else if (cms.galleries.searchObject['galleries'].length > 0) {               
                var selectedGalleries = cms.galleries.searchObject['galleries'];                
                var availableTypes = cms.galleries.searchCriteriaListsAsJSON['types'];
                var availableGalleries = cms.galleries.searchCriteriaListsAsJSON['galleries'];
                // get the resource types associated with the selected galleries
              var contentTypes = [];
                for (var i = 0; i < availableGalleries.length; i++) {                   
                    if ( $.inArray(availableGalleries[i]['path'], selectedGalleries) != -1){                                                                            
                            contentTypes = contentTypes.concat(availableGalleries[i]['contenttypes']);                                                    
                    }
                }
                // check if the associated resource types are available for this gallery
                var checkedTypes = [];
                for (var i = 0; i < contentTypes.length; i++) {
                    if ( $.inArray(contentTypes[i], cms.galleries.configContentTypes) != -1 && $.inArray(contentTypes[i], checkedTypes) == -1){                                                                            
                            checkedTypes.push(contentTypes[i]);                                                    
                    }
                }                                                                
                return $.extend(preparedSearchObject, cms.galleries.searchObject, {'types' : checkedTypes});                                              
            } else {
                return cms.galleries.searchObject;
                       }           
       // just use the unchanged search object      
       } else {
           return  cms.galleries.searchObject;
       }
   }
   
   /**
    * Loads the lists with available resource types, galleries and categories via ajax call.
    * TODO: generalize to make it possible to load some preselected
    */
   var loadSearchResults = cms.galleries.loadSearchResults = function(initSearchResult) {
      if (initSearchResult) {
          cms.galleries.fillResultList(initSearchResult);
      } else {
          cms.galleries.searchObject.page = 1;
          // ajust the search object to provide a consistent search
          var preparedSearchObject = prepareSearchObject();
          
          $.ajax({
             'url': vfsPathAjaxJsp,
             'data': {
                'action': 'search',
                'data': JSON.stringify({
                   'querydata': preparedSearchObject
                })
             },
             'type': 'POST',
             'dataType': 'json',
             'success': cms.galleries.fillResultList
          });
      }
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
                        // ajust the search object to provide a consistent search
                        var preparedSearchObject = prepareSearchObject();
                        $.ajax({
                           'url': vfsPathAjaxJsp,
                           'data': {
                              'action': 'search',
                              'data': JSON.stringify({
                                 'querydata': preparedSearchObject
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
     
   /**
    * Returns true, if the select button should be displayed.
    */
   var isSelectableItem = cms.galleries.isSelectableItem = function () {      // displaySelectButton
      if (cms.galleries.initValues['dialogMode'] == 'widget' || cms.galleries.initValues['dialogMode'] == 'editor'){
          return true;
      }
      return false;
   }
     
   var fillResultPage = cms.galleries.fillResultPage = function(pageData) {           
      var target = $('#results > ul').empty().removeAttr('id').attr('id', cms.html.galleryResultListPrefix + pageData.searchresult.resultpage);
      $.each(pageData.searchresult.resultlist, function() {
          var resultElement=$(this.itemhtml).appendTo(target);
          resultElement.attr('alt', this.path);                              
          resultElement.data('type', this.type);
          if(isSelectableItem()) {
              resultElement.find('.cms-list-itemcontent')
                  .append('<div class="cms-handle-button cms-select-item"></div>');
          }
          // if in ade container-page
         if ((cms.toolbar && cms.toolbar.toolbarReady) || cms.sitemap) {
             resultElement.attr('rel', this.clientid);
             resultElement.find('.cms-list-itemcontent').append('<a class="cms-handle cms-move"></a>');
         }
         if (cms.sitemap) {
             cms.sitemap.initDragForGallery(this, resultElement);
         }
      });          
      
      // if a resource is selected open the preview     
      if (cms.galleries.activeItem['path'] != null && cms.galleries.activeItem['path'] != "" ){
          $('#results li.cms-list[alt=' + cms.galleries.activeItem['path'] + ']').trigger('click');              	                    
          if (cms.galleries.activeItem['isInitial'] == true) {
              $('#results li.cms-list[alt=' + cms.galleries.activeItem['path'] + ']').trigger('dblclick');
              //cms.galleries.activeItem['isInitial'] = false;
          }
              	          
      } 
   }
   
   /**
     * Sets the values of the search object.
     * The parameter should look like: {'querydata': {'galleries':...,}', 'tabid':..,}
     * @param {Object} requestData a JSON object with search object data 
     */
    var setSearchObject = cms.galleries.setSearchObject = function(/**JSON object*/requestData) {
        // reset the values of the search object
        cms.galleries.searchObject['galleries'] = [];
        cms.galleries.searchObject['categories'] = [];        
        cms.galleries.searchObject['types'] = [];
        cms.galleries.searchObject['query'] = '';
        cms.galleries.searchObject['tabid'] = 2;
        
        if (requestData) {
            // initialize the search object
            if (requestData.querydata) {
                if (requestData.querydata.galleries) {
                    cms.galleries.searchObject['galleries'] = requestData.querydata.galleries;
                    cms.galleries.searchObject.isChanged.galleries = true;
                }
                if (requestData.querydata.categories) {
                    cms.galleries.searchObject['categories'] = requestData.querydata.categories;
                    cms.galleries.searchObject.isChanged.categories = true;
                }
                if (requestData.querydata.types) {
                    // Do not set the type the criteria for type, so only the gallery is selected
                    cms.galleries.searchObject.isChanged.types = true;
                }
                if (requestData.querydata.query) {
                    cms.galleries.searchObject['query'] = requestData.querydata.query;
                    cms.galleries.searchObject.isChanged.query = true;
                }
                if (requestData.querydata.tabid) {
                    cms.galleries.searchObject['tabid'] = cms.galleries.arrayOfTabIds[requestData.querydata.tabid];
                }
            }
            
            // Set the path to currently selected item            
            if (cms.galleries.initValues['fieldId'] != null && cms.galleries.initValues['fieldId'] != 'null' 
                && cms.galleries.initValues['path'] != null && cms.galleries.initValues['path'] != 'null'){          
                  //var itemField = window.opener.document.getElementById(cms.galleries.initValues['fieldId']);
        	      //if (itemField.value != null && itemField.value != '') {
                        cms.galleries.activeItem['path'] = cms.galleries.initValues['path'];
                        cms.galleries.activeItem['isInitial'] = true;
            	  //}          
            	  }          
            }
        
    }
   
   /**
    * Refresh the list for given criteria after sorting.
    * 
    * @param {Object} sortedList the list after sorting
    * @param {Object} criteria the name of the list
    * @param {Object} option the option for the hierarchic list
    */
   var refreshCriteriaList = cms.galleries.refreshCriteriaList = function(/**Array*/sortedList, /**String*/ criteria, option) {   
      if (criteria == 'galleries') {
         cms.galleries.refreshGalleries(sortedList);
      } else if (criteria == 'categories') {
         cms.galleries.refreshCategories(sortedList, option);
      } else if (criteria == 'types') {
         cms.galleries.refreshTypes(sortedList);
      }
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
             .attr('alt', categories[i].path).addClass(classActive + ' ' + classConstLevel + categories[i].level).addClass('cms-list-with-checkbox')
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
         $("li[alt='" + this.path + "']").appendTo('#categories > ul').toggleClass('cms-active-level', isActive);
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
         $(galleries[i].itemhtml)
             .appendTo('#galleries > ul')
             .attr('alt', galleries[i].path)
             .addClass('cms-list-with-checkbox')            
             .prepend('<div class="cms-list-checkbox"></div>');         
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
         $("li[alt='" + this.path + "']").appendTo('#galleries > ul');
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
         var typeElement=$(types[i].itemhtml).appendTo('#types > ul');
         typeElement.attr('alt', types[i].typeid).addClass('cms-list-with-checkbox')
             .prepend('<div class="cms-list-checkbox"></div>');
         typeElement.data('gallerytypeid', types[i].gallerytypeid);
         var typeName=types[i].type;
         // if in ade container-page and type is an creatable element
         var toolbarReady = cms.toolbar && cms.toolbar.toolbarReady;
         if (toolbarReady && $.inArray(typeName, cms.data.newTypes)>=0) {
             typeElement.attr('rel', typeName);
             typeElement.find('.cms-list-itemcontent').append('<a class="cms-handle cms-move"></a>');
             if (cms.sitemap) {
                 cms.sitemap.initDragForGalleryType(types[i], typeElement);
             }
         }
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
         $("li[alt='" + this.typeid + "']").appendTo('#types > ul');
      });
   }
   
   /**
    * Adds the search criteria html to the result tab.
    */
   var fillResultTab = cms.galleries.fillResultTab = function(initSearchResult) {      
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
               // only show the criteria button, query is not empty or white space only               
               if (searchQuery.length > 0 && searchQuery.match(/[^\s]/)) {
                   titles = singleSelect.concat(searchQuery);    
                   cms.galleries.addCreteriaToTab(titles, searchCriteria);
               }                         
               cms.galleries.searchObject.isChanged[searchCriteria] = false;
            } else {
               var selectedLis = cms.galleries.searchObject[searchCriteria];
               // if any search criteria is selected
               if (selectedLis) {
                   if (selectedLis.length == 1) {                       
                       titles = singleSelect.concat($('li[alt=' + selectedLis[0] + ']').find('.cms-list-title').text());
                       cms.galleries.addCreteriaToTab(titles, searchCriteria);
                   } else if (selectedLis.length > 1) {
                      $.each(selectedLis, function() {
                      if (titles.length == 0) {
                          titles = multipleSelect.concat($('li[alt=' + this + ']').find('.cms-list-title').text());
                      } else {
                          titles = titles.concat(", ").concat($('li[alt=' + this + ']').find('.cms-list-title').text());
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
         cms.galleries.loadSearchResults(initSearchResult);
      }           
   }
   
   /**
    * Callback function for the one click event in the gallery list
    */
   var clickListItem = cms.galleries.clickListItem = function() {         
          // id of the li tag and type of search 
          var itemId = $(this).attr('alt');
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
             if (itemCriteria == 'types') {                 
                 switchGalleriesForType();                          
             }   
                          
             // CASE 2: gallery is not selected yet, -> select the gallery on click
          } else {
             // push the gallery path to the search object
             cms.galleries.searchObject[itemCriteria].push(itemId);
             // add highlighting
             $(this).addClass(cms.galleries.classListItemActive);             
             
             // set isChanged flag, so next search will be send to server
             cms.galleries.searchObject.isChanged[itemCriteria] = true;
             
             // hide galleries, which are not available for selected type
             // TODO: do not hide gallery if it is already selected
             if (itemCriteria == 'types') {                 
                 switchGalleriesForType();                             
             }             
          }     
   }
   
   var switchGalleriesForType = function () {
       //  collect all gallery type ids for galleries associated with selected types
       var galleryTypes = [];
       var selectedTypes = cms.galleries.searchObject['types']
       var galleryObjects = cms.galleries.searchCriteriaListsAsJSON['galleries'];  
       // at least one type is selected
       if (selectedTypes.length > 0) {
           var availableTypes = cms.galleries.searchCriteriaListsAsJSON['types'];
           for (i = 0; i < availableTypes.length; i++) {           
               if ($.inArray(availableTypes[i]['typeid'].toString(), selectedTypes) != -1) {
                   galleryTypes = galleryTypes.concat(availableTypes[i]['gallerytypeid']);    
               }           
           }
           for ( j = 0; j < galleryObjects.length; j++ ){
              var selectStatus = $('li[alt=' + galleryObjects[j].path + ']').hasClass(cms.galleries.classListItemActive);
              var index = $.inArray(galleryObjects[j]['gallerytypeid'], galleryTypes);
              // case 1: gallery is not acossiated with type and should be hide
              if (index == -1 && !selectStatus) {
                  $('li[alt=' + galleryObjects[j].path + ']').toggleClass('cms-item-invisible', true);
              } else {
                  $('li[alt=' + galleryObjects[j].path + ']').toggleClass('cms-item-invisible', false);
              }              
           }         
           
       } else {
           for ( j = 0; j < galleryObjects.length; j++ ){          
                 $('li[alt=' + galleryObjects[j].path + ']').toggleClass('cms-item-invisible', false);
          }
       }                                                                                                
   }
  
   /**
    * Callback function for the double click event in the search criteria list
    */
   var dblclickListItem = cms.galleries.dblclickListItem = function() {   
     
      // id of the li tag and type of search 
      var itemId = $(this).attr('alt');
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
         
         // hide galleries, which are not available for selected type
         // TODO: do not hide gallery if it is already selected
         if (itemCriteria == 'types') {                 
             switchGalleriesForType();                             
         }          
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
            
            // hide galleries, which are not available for selected type
            // do not hide gallery if it is already selected
            if (searchCriteria == 'types') {                 
                switchGalleriesForType();                             
            } 
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
    * Callback function for click event on the item in the result list.
    */
   var clickResultItem = cms.galleries.clickResultItem = function () {       
       var isSelected = $(this).hasClass('cms-list-item-active');
       // deselect selected items
       $('#results li').toggleClass('cms-list-item-active', false);
       if (isSelected) {
           $(this).toggleClass('cms-list-item-active', false);             
       } else {
           $(this).toggleClass('cms-list-item-active', true);
       }
   }
   
  /** 
   * Callback function for dbclick event on the item in the result list.
   * 
   */
  var dblclickToShowPreview = cms.galleries.dblclickToShowPreview = function() {      
      // retrieve the resource id
      var itemId = $(this).attr('alt');
      
      // set the resouce id as alt attribute and empty the content of the preview
      $('#cms-preview').attr('alt', itemId);            
      $('#cms-preview div.preview-area, #cms-preview div.edit-area').empty();
      $('#cms-preview div.edit-format-area').remove();
      
      // reset the active item object
      cms.galleries.resetActiveItem();
      
      // retrieve the resource type and load the preview      
      var itemType = $(this).data('type');
      loadItemPreview(itemId, itemType);
      
      //deselect items in the list and set active class to the item which was dblclicked
      $('#result li.list-item').toggleClass('cms-list-item-active', false);
      $(this).toggleClass('cms-list-item-active', true);
       
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
         'success': cms.galleries.getContentHandler(itemType)['openPreview']
      });
    } 
    
    
    var resetActiveItem = cms.galleries.resetActiveItem = function() {        
        cms.galleries.activeItem['isCropped'] = null;
    }
 
        
    /**
     * Adds a new specific content handler for thr specified resource type.
     * 
     * @param {Object} typeId the type id of the resource
     * @param {Object} handler the specific handler for this resource
     */   
    cms.galleries.addContentTypeHandler = function(typeId, handler){
        cms.galleries.contentTypeHandlers[typeId]= $.extend({}, cms.previewhandler.defaultContentTypeHandler, handler);
    }


    /**
     * Returns the specifired handler for a resource type or the default handler.
     * 
     * @param {Object} typId the resource type id
     */
    cms.galleries.getContentHandler = function(typeId){
        if (typeId && cms.galleries.contentTypeHandlers[typeId]){
            return cms.galleries.contentTypeHandlers[typeId];
        }
        return cms.galleries.contentTypeHandlers['default'];
    }  
    
    /**
     * Returns true, if specified handler is given and false otherwise.
     * 
     * @param {Object} typId the resource type id
     */
    cms.galleries.hasContentHandler = function(typeId){
        if (typeId && cms.galleries.contentTypeHandlers[typeId]){
            return true;
        }
        return false;
    }    
   
    var toggleAdditionalInfo = cms.galleries.toggleAdditionalInfo = function() {
      var elem = $(this);
      var $additionalInfo = elem.closest('.ui-widget-content').children('.cms-additional');
      if (elem.hasClass('ui-icon-triangle-1-e')) {
         elem.removeClass('ui-icon-triangle-1-e').addClass('ui-icon-triangle-1-s');
         $additionalInfo.show(5, function() {
            var list = $(this).parents('div.cms-menu');
            $('div.ui-widget-shadow', list).css({
               height: list.outerHeight() + 1
            });
         });
      } else {
         elem.removeClass('ui-icon-triangle-1-s').addClass('ui-icon-triangle-1-e');
         
         $additionalInfo.hide(5, function() {
            var list = $(this).parents('div.cms-menu');
            $('div.ui-widget-shadow', list).css({
               height: list.outerHeight() + 1
            });
         });
      }
      return false;
   };
})(cms);
