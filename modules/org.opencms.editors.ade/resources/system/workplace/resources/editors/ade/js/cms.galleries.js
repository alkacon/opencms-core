(function(cms) {    
   
   /** Message bundle. */
   var M = cms.messages;

   /** A map with all available content handlers. */
   var contentTypeHandlers = cms.galleries.contentTypeHandlers = {};
    
   /** Array with resource types available for this galleries dialog. */
   var configContentTypes = cms.galleries.configContentTypes = [];
    
   /** html-id for tabs. */
   var idTabs = cms.galleries.idTabs = 'cms-gallery-tabs';
   
   /** html-id for tabs. */
   var idGalleriesMain = cms.galleries.idGalleriesMain = 'cms-gallery-main';   
   
   //TODO: must be more generic, so at the beginning the indeces can be changed on the fly
   /** A Map of tab ids. */
   var arrayTabIndexes = cms.galleries.arrayTabIndexes =  {};
   
   /** A Map of tab ids. */
   var arrayTabIds = cms.galleries.arrayTabIds =  {
       'cms_tab_results': 'cms_tab_results',
       'cms_tab_types': 'cms_tab_types', 
       'cms_tab_galleries': 'cms_tab_galleries',
       'cms_tab_categories': 'cms_tab_categories',
       'cms_tab_search':'cms_tab_search',
       'cms_tab_vfs':'cms_tab_vfs',
       'cms_tab_containertypes':'cms_tab_containertypes',
       'cms_tab_sitemap':'cms_tab_sitemap'
   };
   
   /** 
    * 'dialogMode': The current mode of the dialog. It can be 'widget','editor','ade', 'sitemap' or 'view'.
    * 'fieldId': The field id of the input field inside of the xmlcontent. 
    */
   var initValues = cms.galleries.initValues = {
       'dialogMode': null,
       'fieldId'   : null,
       'target'   : null
   };
   
   /** 
    * 'path': vfs path to the image, also used as id for the preview
    * 'linkpath': OpenCms link to the resource
    * 'isInitial': flag to indicate is the the resource is loaded from xmlContent
    * 'newwidth': customizes width of an image
    * 'newheight': customizes height of an image
    * 'title': title property of the resouce
    * 'description': description property of the resource
    * 'target': the link tagert for the resouce
    */
   var activeItem = cms.galleries.activeItem = {
       'path': '',
       'linkpath':'',
       'isInitial': false,
       'newwidth': 0,
       'newheight': 0,
       'title': '',
       'description': ''      
   };
   
   /** html-class for the inner of the scrolled list with items. */
   var classScrollingInner = cms.galleries.classScrollingInner = 'cms-list-scrolling-inner';
   
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
   
   /** html-class for the level margin mode. */
   var classLevelActive = 'cms-active-level';
   
   /** html-class for opened subtree in a sitemap. */
   var classTreeOpened = 'cms-opened';
   
   /** css-class for sitemap entries with subtree. */
   var classTreeWithSubtree = 'cms-list-with-subtree';
   
   /** css-class for sitemap leafs. */
   var classTreeWithoutSubtree = 'cms-list-without-subtree';
   
   /** Constant value for the sitemap margin. */
   cms.galleries.constSitemapMargin = 22;
  
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
    * 'sortorder': the sort oder parameter, if null title_desc is used on server
    * 'locale': optinal parameter, is should not be null
    * 'isChanged': map of flags indicating if one of the search criteria is changed and should be taken into account. It is used internally. 
    */
   var searchObject = cms.galleries.searchObject = {
      types: [],
      galleries: [],
      categories: [],
      query: '',
      tabid: cms.galleries.arrayTabIndexes['cms_tab_types'],
      page: 1,
      searchfields: '',
      matchesperpage: 8,
      sortorder: null,
      tabs: [],
      //locale: null,
      isChanged: {
         types: false,
         galleries: false,
         categories: false,
         query: false
      }
   };
   
   var tabs = {
       'cms_tab_results': {
           addTabToList: function () {
               $('#' + cms.galleries.idTabs + ' > ul').append('<li><a href="#' + cms.galleries.arrayTabIds['cms_tab_results'] + '">Search Results</a></li>');
           },
           addTabHtml: function(localesArray){
                      var resultTab = $(cms.galleries.htmlTabResultSceleton);
                      resultTab.find('.cms-drop-down label').after($.fn.selectBox('generate',{
                          values:[
                              {value: 'title_asc',title: 'Title Ascending'}, 
                              {value: 'title_desc',title: 'Title Descending'}, 
                              {value: 'type_asc',title: 'Type Ascending'}, 
                              {value: 'type_desc',title: 'Type Descending'}, 
                              {value: 'dateLastModified_asc',title: 'Date Ascending'},
                              {value: 'dateLastModified_desc',title: 'Date Descending'},
                              {value: 'path_asc',title: 'Path Ascending'},
                              {value: 'path_desc',title: 'Path Descending'}
                          ],
                          width: 150,
                          /* TODO: bind sort functionality */
                          select: function($this, self, value){              
                              cms.galleries.searchObject['sortorder'] = value;
                              // send new search for given sort oder and refresh the result list
                              // display the first pagination page for sorted results    
                              cms.galleries.loadSearchResults();          
                          }}));
                          resultTab.find('.cms-result-criteria').css('display','none');
                          
                      
                      /*resultTab.find('.cms-drop-down').after('<span alt="locale" class="cms-drop-down">\
                                                                    <label>&nbsp;</label>\
                                                              </span>');
                      resultTab.find('span[alt="locale"]').find('label').after($.fn.selectBox('generate',{
                          values:[{value:'all_files',title:'All files'},
                                  {value:'user_created',title:'Files created by me'},
                                  {value:'user_modified',title:'Fieles last modified by me'}],
                          width: 150,                          
                          select: function($this, self, value){
                              var tab = $(self).closest('div.cms-list-options').attr('id');
                              cms.galleries.searchObject['locale'] = value;
                              // send new search for given sort oder and refresh the result list
                              // display the first pagination page for sorted results    
                              //TODO implement function on selection          
                          }}));  */                       
                             
                      // display the locale select box, if more then one locale is available
                      if (localesArray.length > 1) {
                          resultTab.find('.cms-drop-down').after('<span alt="locale" class="cms-drop-down">\
                                                                    <label>Locale:</label>\
                                                              </span>');
                      resultTab.find('span[alt="locale"]').find('label').after($.fn.selectBox('generate',{
                          values:localesArray,
                          width: 150,
                          /* TODO: bind sort functionality */
                          select: function($this, self, value){
                              var tab = $(self).closest('div.cms-list-options').attr('id');
                              cms.galleries.searchObject['locale'] = value;
                              // send new search for given sort oder and refresh the result list
                              // display the first pagination page for sorted results    
                              cms.galleries.loadSearchResults();          
                          }}));
                       // TODO: set the preselected locales from search object
                     
                      }
                      // add tabs html to tabs
                      $('#' + cms.galleries.idTabs).append(resultTab);
               },
           fillTab: function () {}            
       },
       'cms_tab_types': {
           addTabToList: function () {
                $('#' + cms.galleries.idTabs + ' > ul').append('<li><a href="#' + cms.galleries.arrayTabIds['cms_tab_types'] + '">Types</a></li>');
           },
           addTabHtml: function (param) { 
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
                           // add tabs html to tabs
                          $('#' + cms.galleries.idTabs).append(typesTab); 
                       },
          fillTab: function () {
                      if (cms.galleries.searchCriteriaListsAsJSON.types) {
                         cms.galleries.configContentTypes = cms.galleries.searchCriteriaListsAsJSON.typeids;
                         cms.galleries.fillTypes(cms.galleries.searchCriteriaListsAsJSON.types);
                         markSelectedCriteria('types');
                      }
          }
      },
      'cms_tab_galleries': {
          addTabToList: function () {
               $('#' + cms.galleries.idTabs + ' > ul').append('<li><a href="#' + cms.galleries.arrayTabIds['cms_tab_galleries'] + '">Galleries</a></li>');
           },
          addTabHtml : function (param) {
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
                              // add tabs html to tabs
                              $('#' + cms.galleries.idTabs).append(galleriesTab);
                          },
          fillTab: function () {
              if (cms.galleries.searchCriteriaListsAsJSON.galleries) {
                 cms.galleries.fillGalleries(cms.galleries.searchCriteriaListsAsJSON.galleries);
                 markSelectedCriteria('galleries');
              }
          }         
      },
      'cms_tab_categories':{
          addTabToList: function () {
               $('#' + cms.galleries.idTabs + ' > ul').append('<li><a href="#' + cms.galleries.arrayTabIds['cms_tab_categories'] + '">Categories</a></li>');
           }, 
          addTabHtml: function (param) {
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
                              $('#' + cms.galleries.idTabs).append(categoriesTab); 
    
                           },
          fillTab: function () {
              if (cms.galleries.searchCriteriaListsAsJSON.categories) {
                 cms.galleries.fillCategories(cms.galleries.searchCriteriaListsAsJSON.categories, 'path');
                 markSelectedCriteria('categories');
              }                           
          }
      },
      'cms_tab_search': {
          addTabToList: function () {
               $('#' + cms.galleries.idTabs + ' > ul').append('<li><a href="#' + cms.galleries.arrayTabIds['cms_tab_search'] + '">Full Text Search</a></li>');
           },
          addTabHtml: function(param) {
                          // add tabs html to tabs
                          $('#' + cms.galleries.idTabs).append(cms.galleries.htmlTabFTSeachSceleton);
                     },
          fillTab: function () {}                           
      },
      'cms_tab_vfs': {
          addTabToList: function () {
               $('#' + cms.galleries.idTabs + ' > ul').append('<li><a href="#' + cms.galleries.arrayTabIds['cms_tab_vfs'] + '">Vfs Tree</a></li>');
           },
          addTabHtml: function(param) {
                          // add tabs html to tabs
                          $('#' + cms.galleries.idTabs).append(cms.galleries.htmlTabVfsTreeSceleton);
                     },
          fillTab: function () {}
      },
      'cms_tab_containertypes' :{
          addTabToList: function () {
               $('#' + cms.galleries.idTabs + ' > ul').append('<li><a href="#' + cms.galleries.arrayTabIds['cms_tab_containertypes'] + '">Full Text Search</a></li>');
           },
          addTabHtml: function(param) {
                          // add tabs html to tabs
                          $('#' + cms.galleries.idTabs).append(cms.galleries.htmlTabContainerTypesSceleton);
                     },
          fillTab: function () {}
      },
      'cms_tab_sitemap' :{
          addTabToList: function () {
               $('#' + cms.galleries.idTabs + ' > ul').append('<li><a href="#' + cms.galleries.arrayTabIds['cms_tab_sitemap'] + '">Sitemap</a></li>');
           },
          addTabHtml: function(localesArray) {
                      var sitemapTab = $(cms.galleries.htmlTabSitemapSceleton);
 
                      // display the locale select box, if more then one locale is available
                      if (localesArray.length > 1) {
                          sitemapTab.find('.cms-drop-down').after('<span alt="locale" class="cms-drop-down">\
                                                                    <label>Locale:</label>\
                                                              </span>');
                          sitemapTab.find('span[alt="locale"]').find('label').after($.fn.selectBox('generate',{
                              values:localesArray,
                              width: 150,
                              select: function($this, self, value){
                                  var sitemapUri = cms.galleries.searchCriteriaListsAsJSON.sitemap.rootEntry.sitemapUri;
                                  var siteRoot = $( '#' + cms.galleries.arrayTabIds['cms_tab_sitemap']).find('.cms-selectbox:first')
                                      .selectBox('getValue');
                                  cms.galleries.loadSitemap(sitemapUri, siteRoot, value);         
                          }
                          }));                     
                      }
                      // add tabs html to tabs
                      $('#' + cms.galleries.idTabs).append(sitemapTab);

                        
                      $('#sitemap li.' + classTreeWithSubtree).find('div.cms-tree-opener').live('click', function (e) {
                          e.stopPropagation();
                          var selectLi = $(this).closest('li');
                          var sitemapUri = selectLi.attr('alt');
                          if (selectLi.hasClass(classTreeOpened)) {
                              var rootLevel = getEntryLevel(selectLi);
                              removeSubEntry(selectLi, rootLevel);
                              selectLi.removeClass(classTreeOpened);
                          } else {                              
                              var siteRoot = $('#' + cms.galleries.arrayTabIds['cms_tab_sitemap']).find('.cms-selectbox:first')
                                  .selectBox('getValue');
                              var locale = $('#' + cms.galleries.arrayTabIds['cms_tab_sitemap']).find('.cms-selectbox:last')
                                  .selectBox('getValue');    
                              cms.galleries.loadSitemapEntry(sitemapUri, siteRoot, locale);
                          }                          
                       });                        
                     },
          fillTab: function () {
              //if ()
              if (cms.galleries.searchCriteriaListsAsJSON.sitemap) {
                  // TODO: extend the siteRoot selectbox with the given list of the siteroots    
                  var siteRoot = cms.galleries.searchCriteriaListsAsJSON.sitemap.siteRoot;
                  var sitemapUri = cms.galleries.searchCriteriaListsAsJSON.sitemap.rootEntry.sitemapUri;
                  $('#' + cms.galleries.arrayTabIds['cms_tab_sitemap'])
                      .find('span.cms-drop-down:first label')
                      .after($.fn.selectBox('generate',{
                          values:[
                              {value: siteRoot ,title: siteRoot}                             
                          ],
                          width: 150,
                          select: function($this, self, value){
                              var locale = $('#' + cms.galleries.arrayTabIds['cms_tab_sitemap']).find('.cms-selectbox:last')
                                  .selectBox('getValue');
                              cms.galleries.loadSitemap(sitemapUri, value, locale);         
                          }                                               
                      }));
                 // TODO implement
                 cms.galleries.fillSitemap(cms.galleries.searchCriteriaListsAsJSON);
                 // TODO: mark selected resource
              }                                         
          }
      }
   }
   
   
   
   
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
   var htmlTabResultSceleton = cms.galleries.htmlTabResultSceleton = '<div id="' + cms.galleries.arrayTabIds['cms_tab_results'] + '">\
            <div class="cms-result-criteria"></div>\
            <div id="resultoptions" class="ui-widget ' +
   cms.galleries.classListOptions +
   '">\
                        <span class="cms-drop-down">\
                            <label>Sort by:</label>\
                        </span>\
             </div>\
             <div id="results" class="cms-list-scrolling ui-corner-all result-tab-scrolling">\
                        <ul class="'+classScrollingInner+' cms-item-list"></ul>\
             </div>\
             <div class="result-pagination"></div>\
         </div>';    
   
   /** html fragment for the tab with the types' list. */
   var htmlTabTypesSceleton = cms.galleries.htmlTabTypesSceleton = '<div id="' + cms.galleries.arrayTabIds['cms_tab_types'] + '">\
                <div id="typesoptions" class="ui-widget ' + cms.galleries.classListOptions + '">\
                    <span class="cms-drop-down">\
                        <label>Sort by:</label>\
                    </span>\
                    <span class="cms-ft-search"><label>Search:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                </div>\
                <div id="types" class="cms-list-scrolling ui-corner-all criteria-tab-scrolling">\
                    <ul id="'+cms.html.galleryTypeListId+'" class="'+classScrollingInner+' cms-item-list"></ul>\
                </div>\
              </div>';
   
   /** html fragment for the tab with the galleries' list. */
   var htmlTabGalleriesSceleton = cms.galleries.htmlTabGalleriesSceleton = '<div id="' + cms.galleries.arrayTabIds['cms_tab_galleries'] + '">\
                <div id="galleriesoptions" class="ui-widget ' + cms.galleries.classListOptions + '">\
                    <span class="cms-drop-down">\
                        <label>Sort by:</label>\
                    </span>\
                    <span class="cms-ft-search"><label>Search:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                </div>\
                <div id="galleries" class="cms-list-scrolling ui-corner-all criteria-tab-scrolling">\
                    <ul class="'+classScrollingInner+'"></ul>\
                </div>\
              </div>';
   
   /** html fragment for the tab with the categories' list. */
   var htmlTabCategoriesSceleton = cms.galleries.htmlTabCategoriesSceleton = '<div id="' + cms.galleries.arrayTabIds['cms_tab_categories'] + '">\
                <div id="categoriesoptions" class="ui-widget ' + cms.galleries.classListOptions + '">\
                    <span class="cms-drop-down">\
                        <label>Sort by:</label>\
                    </span>\
                    <span class="cms-ft-search"><label>Search:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                </div>\
                <div id="categories" class="cms-list-scrolling ui-corner-all criteria-tab-scrolling">\
                    <ul class="'+classScrollingInner+'"></ul>\
                </div>\
              </div>';
   
   /** html fragment for the tab with the full text search. */
   var htmlTabFTSeachSceleton = cms.galleries.htmlTabFTSeachSceleton = '<div id="' + cms.galleries.arrayTabIds['cms_tab_search'] + '">\
             <div class="cms-search-panel ui-corner-all">\
                    <div class="cms-search-options"><b>Search the offline-index:</b></div>\
                    <div class="cms-search-options">\
                        <span id="searchQuery" class="cms-item-left"><label>Search for:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                    </div>\
                    <!-- <div class="cms-search-options">\
                        <div class="cms-item-left">Search in:</div>\
                        <div id="searchInTitle" class="cms-list-checkbox"></div>\
                        <div class="cms-checkbox-label">Title</div>\
                        <div id="searchInContent" class="cms-list-checkbox"></div>\
                        <div class="cms-checkbox-label">Content</div>\
                    </div> -->\
                    <div class="cms-search-options">\
                        <span id="searchBefore" class="cms-item-left cms-input-date"><label>Changed after:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                        <span id="searchBefore" class="cms-item-left cms-input-date"><label>Changed before:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                   </div>\
                   <div class="cms-search-options">\
                        <span id="searchBefore" class="cms-item-left cms-input-date"><label>Created after:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                        <span id="searchBefore" class="cms-item-left cms-input-date"><label>Created before:</label><input type="text" class="ui-corner-all ui-widget-content" /></span>\
                    </div>\
                    <div class="cms-search-options">\
                        <button class="ui-state-default ui-corner-all cms-item-left-bottom">Search</button>\
                    </div>\
             </div>\
          </div>'; 
   
   /** html fragment for the tab with vfs tree. */
   var htmlTabVfsTreeSceleton = cms.galleries.htmlTabVfsTreeSceleton = '<div id="' + cms.galleries.arrayTabIds['cms_tab_vfs'] + '">\
             <div class="cms-search-panel ui-corner-all">Vfs tree</div></div>'; 
             
   /** html fragment for the tab with container types. */
   var htmlTabContainerTypesSceleton = cms.galleries.htmlTabContainerTypesSceleton = '<div id="' + cms.galleries.arrayTabIds['cms_tab_containertypes'] + '">\
             <div class="cms-search-panel ui-corner-all">Container types</div></div>';
             
   /** html fragment for the tab with sitemap. */
   var htmlTabSitemapSceleton = cms.galleries.htmlTabSitemapSceleton = '<div id="' + cms.galleries.arrayTabIds['cms_tab_sitemap'] + '">\
                <div id="sitemapoptions" class="ui-widget ' + cms.galleries.classListOptions + '">\
                    <span class="cms-drop-down">\
                        <label>' + M.GUI_GALLERIES_DROP_DOWN_LABEL_SITEROOT_0 + '</label>\
                    </span>\
                </div>\
                <div id="sitemap" class="cms-list-scrolling ui-corner-all criteria-tab-scrolling">\
                    <ul class="'+classScrollingInner+'"></ul>\
                </div>\</div>';      
   
   /**
    * Init function for search/add dialog.
    * 
    * @param {Object} requestData the request parameter
    */
   var initAddDialog = cms.galleries.initAddDialog = function(tabsContent, requestData) {          
      // handle the request parameter:
      
      // set the indices for the tabs
      var tabIndex = 0;
      cms.galleries.arrayTabIndexes[cms.galleries.arrayTabIds['cms_tab_results']] = tabIndex;
      tabIndex = tabIndex + 1;                   
      $.each(cms.galleries.initValues['tabs'], function () {
          var tabId = this;
          cms.galleries.arrayTabIndexes[this] = tabIndex;
          tabIndex = tabIndex + 1; 
      });
      
      // initialize the search object and the initial search 
      var initSearchResult = null;          
      if (requestData) {
          cms.galleries.setSearchObject(requestData);    
          // set the initial search if there search results or sitemap entries
          if (requestData['searchresult'] || requestData['sitemap']) {
              initSearchResult = requestData;
          }
      }
            
      // read the standard locale and the available locales
      var localesArray = [];      
      if (tabsContent) {
          cms.galleries.searchObject['locale'] = tabsContent['locale'];
          localesArray = tabsContent['locales'];
      }
      
      // always add the result tab
      tabs[cms.galleries.arrayTabIds['cms_tab_results']].addTabToList();
      tabs[cms.galleries.arrayTabIds['cms_tab_results']].addTabHtml(localesArray);
      
      // add another tabs accoding to configuration                 
      $.each(cms.galleries.initValues['tabs'], function () {
          var tabId = this;
          tabs[tabId].addTabToList();
          tabs[tabId].addTabHtml(localesArray);
      });
        
      // add preview to the galleries html
      $('#' + cms.galleries.idGalleriesMain).append($(cms.previewhandler.htmlPreviewSceleton));                  
      
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
      // bind hover event on items in criteria and result lists
      $('li.cms-list')
          .live('mouseover', function() {
             $(this).addClass(cms.galleries.classListItemHover);
          }).live('mouseout', function() {
             $(this).removeClass(cms.galleries.classListItemHover);
      }); 
                
      // click events on items in criteria lists
      $('#types li.cms-list, #galleries li.cms-list, #categories li.cms-list')
          .live('dblclick', cms.galleries.dblclickListItem)
          .live('click', cms.galleries.clickListItem);
      // click event on items in the result list
      $('#results div.cms-preview-item').live('click', clickResultPreview );
      // click event on select-button of the item in the result list(dialogmode = widget|editor)          
      $('#results div.cms-select-item').live('click' , clickResultSelect );
      $('#results li.cms-list, #sitemap li.cms-list').live('click', clickListItemToHightlight);
      
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
      
      // bind click event to the preview and select handle of the sitemap tab
      $('#sitemap div.cms-select-item').live('click' , clickSitemapSelect);      
      $('#sitemap div.cms-preview-item').live('click', clickSitemapPreview); 

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
                        
      $('.cms-item a.ui-icon').live('click', cms.galleries.toggleAdditionalInfo); 
      
      // load content of the search criteris tabs    
      cms.galleries.loadSearchLists(tabsContent, initSearchResult);     
          
                            
   }
   
   /**
    * Callback function for clicking preview handle in the result list.
    */
   var clickResultPreview = function () {       
          var resType = $(this).closest('li').data('type');               
          var itemId = $(this).closest('li').attr('alt'); 
          cms.galleries.clickToShowPreview(itemId, resType);                
   }
   
   /**
    * Callback function for clicking preview handle in the sitemap tree.
    */
   var clickSitemapPreview = function() {        
        var resType = cms.galleries.getContentHandler()['type'];
        var itemId = $(this).closest('li').attr('alt');
        cms.galleries.clickToShowPreview(itemId, resType);
    }
   
   /**
    * Callbacl function for selecting resource from the result list.
    * @param {Object} event the click event
    */
   var clickResultSelect = function(event) {
          // avoid event propagation to the surround 'li'
          event.stopPropagation();
          var resType = $(this).closest('li').data('type');               
          var itemId = $(this).closest('li').attr('alt');          
          cms.galleries.getContentHandler()['setValuesFromList'][cms.galleries.initValues['dialogMode']](itemId);                            
   }
   
   /**
    * Callback function for select sitemap entry from list.
    * 
    * @param {Object} event the click event
    */
   var clickSitemapSelect = function(event) {
       // avoid event propagation to the surround 'li'
       event.stopPropagation();
       var resType = cms.galleries.getContentHandler()['type'];               
       var itemId = $(this).closest('li').attr('alt');          
       cms.galleries.getContentHandler()['setValuesFromList'][cms.galleries.initValues['dialogMode']](itemId);                              
   }
   
   /**
    * Callback function for highlighting a list item on click.
    * 
    * @param {Object} event the click event
    */
   var clickListItemToHightlight = function (event) {
           if (!event.isPropagationStopped()) {
               var isSelected = $(this).hasClass('cms-list-item-active');
               // deselect selected items
               $('#results li, #sitemap li').toggleClass('cms-list-item-active', false);
               // set the selection
               if (isSelected) {
                   $(this).toggleClass('cms-list-item-active', false);             
               } else {
                   $(this).toggleClass('cms-list-item-active', true);
               }    
           }                             
   }
         
   /**
    * Add html for search criteria to result tab
    *
    * @param {String} content the nice-list of items for given search criteria
    * @param {String} searchCriteria the given search criteria
    */
   var addCreteriaToTab = cms.galleries.addCreteriaToTab =  function(/** String*/content, /** String*/ searchCriteria) {
      $('.cms-result-criteria').removeAttr('style');      
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
             'url': cms.data.GALLERY_SERVER_URL,
             'data': {
                'action': 'all',
                'data': JSON.stringify({
                   'types': cms.galleries.configContentTypes
                }),
                'tabs':JSON.stringify(cms.galleries.initValues['tabs'])     
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
    * @param {Object} data the content of the tabs
    * @param {Object} message status message of the callback function in ajax call
    * @param {Object} initSearchResult the initial search results for selected resource or sitemap entry
    */
   var fillCriteriaTabs = cms.galleries.fillCriteriaTabs = function(/**JSON*/data, message, initSearchResult) {       
      cms.galleries.searchCriteriaListsAsJSON = data;
      // set the initial sitemap tree to the selected entry 
      if (initSearchResult.sitemap) {
          cms.galleries.searchCriteriaListsAsJSON.sitemap = initSearchResult.sitemap;
      }
      $.each(cms.galleries.initValues['tabs'], function () {
          var tabId = this;
          tabs[tabId].fillTab();
      });
      
      // set the available resource types for this galleries
      // should always be provided for the search
      if (cms.galleries.searchCriteriaListsAsJSON.types) {
          cms.galleries.configContentTypes = cms.galleries.searchCriteriaListsAsJSON.typeids;      
      }
               
      if (initSearchResult && initSearchResult.searchresult) {
          cms.galleries.fillResultTab(initSearchResult);         
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
               if (cms.galleries.initValues['dialogMode'] == 'widget' || cms.galleries.initValues['dialogMode'] == 'editor' ) {
                   // get all available galleries
                   var galleriesInfos = cms.galleries.searchCriteriaListsAsJSON['galleries'];
                   var galleryPaths = [];
                   for (var i = 0; i < galleriesInfos.length; i++) {
                       galleryPaths.push(galleriesInfos[i]['path']);
                   }                   
                   return $.extend(preparedSearchObject, cms.galleries.searchObject, {'types': cms.galleries.configContentTypes, 'galleries': galleryPaths});
               } else {
                   return $.extend(preparedSearchObject, cms.galleries.searchObject, {'types':cms.galleries.configContentTypes});    
               }                             
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
             'url': cms.data.GALLERY_SERVER_URL,
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
      $('div.result-pagination').empty().css('display', 'none');
      
      // adjust the height of the result list 
      var resultContentInnerHeight = $('#' + cms.galleries.idTabs).innerHeight();
      var resultTabsOuterHeight = $('#' + cms.galleries.idTabs + ' ul').outerHeight();
      var resultCriteriaOuterHeight = $('.cms-result-criteria').outerHeight(true);
      var resultOptionsOuterHeight = $('#resultoptions').outerHeight(true);      
      var scrollingHeight = resultContentInnerHeight - (resultTabsOuterHeight + resultCriteriaOuterHeight + resultOptionsOuterHeight + 20);      
      $('#results').height(scrollingHeight);
                      
      if (data.searchresult.resultcount > 0) {
         // display        
         cms.galleries.fillResultPage(data);
         // initialize pagination for result list, if there are many pages                  
         if (data.searchresult.resultcount > cms.galleries.searchObject.matchesperpage) {
            
            // adjust the height of the result list with pagination            
            $('.result-pagination').removeAttr('style');
            var resultPaginationOuterHeight = $('.result-pagination').outerHeight(true);
            $('#results').height(scrollingHeight - resultPaginationOuterHeight);
            
            // initialize pagination            
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
                           'url': cms.data.GALLERY_SERVER_URL,
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
            
      }
   }
     
   /**
    * Returns true, if the select button should be displayed.
    * TODO: to rename in displaySelectButton()!!!!
    */
   var isSelectableItem = cms.galleries.isSelectableItem = function () {      // displaySelectButton
      if (cms.galleries.initValues['dialogMode'] == 'widget' || cms.galleries.initValues['dialogMode'] == 'property' ){
          return true;
      }
      return false;
   }
   
   /**
    * Display all possible options for the gallery in the given mode.
    */
   var isFullMode = cms.galleries.isFullDisplayMode = function () {
       if (cms.galleries.initValues['dialogMode'] == 'editor' || cms.galleries.initValues['dialogMode'] == 'widget') {
           return true;
       }
       return false;
   }
   
   /**
    * Returns true, if the select button should be displayed.
    */
   var isEditorMode = cms.galleries.isEditorMode = function () {      // displaySelectButton
      if (cms.galleries.initValues['dialogMode'] == 'editor'){
          return true;
      }
      return false;
   }
     
   var fillResultPage = cms.galleries.fillResultPage = function(pageData) {           
      var target = $('#results > ul').empty().removeAttr('id').attr('id', cms.html.galleryResultListPrefix + pageData.searchresult.resultpage);
      $.each(pageData.searchresult.resultlist, function() {
          var resultElement=$(this.itemhtml).appendTo(target);
          resultElement.attr('alt', this.path)          
              .data('type', this.type);
          resultElement.find('.cms-list-itemcontent')
              .append($('<div/>',{ 'class': "cms-handle-button"}));
          resultElement.find('.cms-handle-button')
              .append($('<div/>',{'class':'cms-preview-item'}));
          if(isSelectableItem()) {
              resultElement.find('.cms-handle-button').prepend($('<div/>',{'class':'cms-select-item'}));
              
              //.append(<div class="cms-select-item">&nbsp;</div><div class="cms-preview-item">&nbsp;</div></div>');
                  //.append('<div class="cms-handle-button cms-select-item"></div>');
          }
          // if in ade container-page
         if ((cms.toolbar && cms.toolbar.toolbarReady) || cms.sitemap) {
             resultElement.attr('rel', this.clientid);             
             resultElement.find('.cms-handle-button')
              .prepend($('<div/>',{'class':'cms-move'}));
             //resultElement.find('.cms-list-itemcontent').append('<a class="cms-handle cms-move"></a>');
         }
         if (cms.sitemap) {
             cms.sitemap.initDragForGallery(this, resultElement);
         }
      });          
      
      // if a resource is selected open the preview     
      if (cms.galleries.activeItem['path'] != null && cms.galleries.activeItem['path'] != "" ){
          $('#results li.cms-list[alt="' + cms.galleries.activeItem['path'] + '"]').trigger('click');              	                    
          
          if (cms.galleries.activeItem['isInitial'] == true) {              
              $('#results li.cms-list[alt="' + cms.galleries.activeItem['path'] + '"]').find('div.cms-preview-item').trigger('click');
          }
              	          
      } 
   }
   
   /**
     * Sets the values of the search object.
     * The parameter should look like: {'querydata': {'galleries':...,}', 'tabid':..,''sitemap':{}}
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
                    cms.galleries.searchObject['tabid'] = cms.galleries.arrayTabIndexes[requestData.querydata.tabid];
                }
            }
            
            // set the sitemap tab, if sitemap entry was preselected
            if(requestData.sitemap) {
                cms.galleries.searchObject['tabid'] = cms.galleries.arrayTabIndexes['cms_tab_sitemap'];
            }
            
            if (cms.galleries.initValues['dialogMode'] == 'editor') {
                    // Set the path to currently selected item            
                    if (cms.galleries.initValues['path'] != null && cms.galleries.initValues['path'] != 'null') {
                        cms.galleries.activeItem['path'] = cms.galleries.initValues['path'];
                        cms.galleries.activeItem['isInitial'] = true;
                    }
                    
            } else if (cms.galleries.initValues['dialogMode'] == 'widget') {
                // Set the path to currently selected item            
                if (cms.galleries.initValues['fieldId'] != null && cms.galleries.initValues['fieldId'] != 'null' &&
                    cms.galleries.initValues['path'] != null &&
                    cms.galleries.initValues['path'] != 'null') {
                        cms.galleries.activeItem['path'] = cms.galleries.initValues['path'];
                        cms.galleries.activeItem['isInitial'] = true;
                }
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
         classActive = classLevelActive;
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
             typeElement.find('.cms-list-itemcontent')
              .append($('<div/>',{ 'class': "cms-handle-button"}));
             typeElement.find('.cms-handle-button')
              .append($('<div/>',{'class':'cms-move'}));
             //typeElement.find('.cms-list-itemcontent').append('<a class="cms-handle cms-move"></a>');
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
   * Loads the sitemap opened to show the selected sitemap entri
   * 
   * @param {Object} sitemapUri the sitemap uri of the selected entry
   * @param {Object} siteRoot the selected siteroot
   * @param {Object} locale the selected locale
   */ 
  var loadSitemap = cms.galleries.loadSitemap  = function (sitemapUri, siteRoot, locale) {       
       // TODO: siteRoot!!!
       // TODO: locale from selectbox              
       
       $.ajax({
         'url': cms.data.GALLERY_SERVER_URL,
         'data': {
            'action': 'SITEMAPTREE',
            'data': JSON.stringify({
               'siteRoot': siteRoot,
               'locale': locale, 
               'sitemapUri': sitemapUri
            })
         },
         'type': 'POST',
         'dataType': 'json',
         'success': cms.galleries.fillSitemap
      });
   }
   
   /**
    * Callback function dor displaying the the sitemap tree opened to show the selected entry.
    * 
    * @param {Object} data the sitemap data as JSON
    */
   var fillSitemap = cms.galleries.fillSitemap = function (/**JSON*/data) {           
      
      $('#sitemap > ul').children().remove();
      // get the rootEntry and append it to the sitemap window       
      if (data.sitemap.rootEntry) {
          var rootEntry = $(data.sitemap.rootEntry.itemhtml).appendTo('#sitemap > ul')
              .attr('alt', data.sitemap.rootEntry.sitemapUri).addClass(classLevelActive + ' ' + classConstLevel + '0')
              .css('margin-left',getLevelMargin(0,cms.galleries.constSitemapMargin));              
          rootEntry.find('div[rel=""]').remove();
          // add preview and select handle
          rootEntry.find('.cms-list-itemcontent')
              .append($('<div/>',{ 'class': "cms-handle-button"}));
          rootEntry.find('.cms-handle-button')
              .append($('<div/>',{'class':'cms-select-item'}))
              .append($('<div/>',{'class':'cms-preview-item'}));
          // set the siteRoot value and the locale from json
          $( '#' + cms.galleries.arrayTabIds['cms_tab_sitemap']).find('.cms-selectbox:first').selectBox('setValue',data.sitemap.siteRoot);                
          $( '#' + cms.galleries.arrayTabIds['cms_tab_sitemap']).find('.cms-selectbox:last').selectBox('setValue',data.sitemap.locale);
          // add the sub tree in the first level    
          if (data.sitemap.rootEntry.hasSubEntries) {              
              rootEntry.addClass(classTreeWithSubtree + ' ' + classTreeOpened).prepend('<div class="cms-tree-opener"></div>');
              var subEntries = data.sitemap.rootEntry.subEntries;
              addSitemapLevel(subEntries, rootEntry, 1);             
          }
          // if a sitemap entry was preselected open the preview     
          if (cms.galleries.activeItem['path'] != null && cms.galleries.activeItem['path'] != "" ){
                             	                    
                  if (cms.galleries.activeItem['isInitial'] == true) {
                     $('#sitemap li.cms-list[alt="' + cms.galleries.activeItem['path'] + '"]').find('div.cms-preview-item').trigger('click');                                                 
                  } else {
                      $('#sitemap li.cms-list[alt="' + cms.galleries.activeItem['path'] + '"]').trigger('click');   
                  }                	                            
          }                    
      }
      // TODO do something to perfrom the search
      /* cms.galleries.searchObject.isChanged.categories = true;*/
   }
   
   
   /**
    * Displayes recusive the sitemap leaves.
    * 
    * @param {Object} subEntries the subentries of one tree level of the sitemap
    * @param {Object} rootEntry the parent entry
    * @param {Object} level the level inside the tree
    */
    var addSitemapLevel = cms.galleries.addSitemapLevel = function (/**JSON*/subEntries, /**Object*/ rootEntry, /**Integer*/level) {                                              
              for (var i = 0; i < subEntries.length; i++) {
                  var subEntry = $(subEntries[i].itemhtml);                 
                  rootEntry.after(subEntry);
                  subEntry.attr('alt', subEntries[i].sitemapUri)
                          .addClass(classLevelActive + ' ' + classConstLevel + level)
                          .css('margin-left',getLevelMargin(level, cms.galleries.constSitemapMargin))
                          .prepend('<div class="cms-tree-opener"></div>');
                  subEntry.find('div[rel=""]').remove();
                  // add preview and select handle
                  subEntry.find('.cms-list-itemcontent')
                      .append($('<div/>',{ 'class': "cms-handle-button"}));
                  subEntry.find('.cms-handle-button')
                      .append($('<div/>',{'class':'cms-select-item'}))
                      .append($('<div/>',{'class':'cms-preview-item'}));
                  if (subEntries[i].hasSubEntries) {                      
                      subEntry.addClass(classTreeWithSubtree);
                      if (subEntries[i].subEntries) {
                          subEntry.addClass(classTreeOpened);
                          addSitemapLevel(subEntries[i].subEntries, subEntry, level+1);
                      }                      
                  } else {
                      subEntry.addClass(classTreeWithoutSubtree);
                  }                         
      }     
   }
      
   /**
    * Returns the calculated margin value for the given level and margin unit.
    * 
    * @param {Object} level
    * @param {Object} margin
    */
   var getLevelMargin = function (/**Integer*/level,/**Integer*/ margin) {
       return level * margin;
   }
   
   /**
    * Returns the indetion level of the selected entry.
    * 
    * @param {Object} liEntry the clicked html tag 
    */
   var getEntryLevel = function (/**Object*/ liEntry) {
       var classAttr =  $(liEntry).attr('class');
       if (classAttr.length > 0) {
            var classAttr = classAttr.split(' ');        
            for (var i = 0; i < classAttr.length; i++ ) {
                var className = classAttr[i];                                 
                if (classAttr[i].indexOf(classConstLevel) != -1) {
                    var levelClass = classAttr[i]; 
                    var firstIndex = levelClass.lastIndexOf('-') + 1;
                    var level = levelClass.substring(firstIndex);                    
                    return parseInt(level);                                     
                }
            }   
       }
                                              
   }
   
   /**
    * Removes the subentries of the given entry from the html. 
    * Only the entries with the bigger indention level will be removed.
    *  
    * @param {Object} rootEntry
    * @param {Object} rootLevel
    */
   var removeSubEntry = function (/**Object*/rootEntry, /**int*/ rootLevel) {
       var subEntry =  $(rootEntry).next();
       var subEntryLevel = getEntryLevel(subEntry);
       while (parseInt(subEntryLevel) > parseInt(rootLevel)) {
           $(subEntry).remove();
           subEntry = $(rootEntry).next();
           if (subEntry.length > 0) {
               subEntryLevel = getEntryLevel(subEntry);    
           } else {
               subEntryLevel = rootLevel;    
           }              
       }       
   }
  
  /**
   * Loads the subentries to the given sitemap uri
   * @param {Object} sitemapUri the sitemap uri of the selected entry
   */ 
  var loadSitemapEntry = cms.galleries.loadSitemapEntry  = function (/**String*/sitemapUri, siteRoot, locale) {       
       // TODO: locale from selectbox              
       
       $.ajax({
         'url': cms.data.GALLERY_SERVER_URL,
         'data': {
            'action': 'SITEMAPENTRY',
            'data': JSON.stringify({
               'siteRoot': siteRoot,
               'locale': locale, 
               'sitemapUri': sitemapUri
            })
         },
         'type': 'POST',
         'dataType': 'json',
         'success': cms.galleries.refreshSitemap
      });
   }
   
   /**
    * Callback function to refresh the subtry entries of the selected entry in the sitemap, after clicking on entry.
    * 
    * @param {Object} data the subentries of the selected entry
    */
   var refreshSitemap = cms.galleries.refreshSitemap = function (/**JSON object*/data) {
       
      // get the rootEntry and append it to the sitemap window       
      if (data.sitemap.rootEntry) {        
          var rootEntry = $('li[alt="' + data.sitemap.rootEntry.sitemapUri + '"]');          
          var rootLevel = getEntryLevel(rootEntry);          
          // add the Sub tree in the first level    
          if (data.sitemap.rootEntry.subEntries) {          
              rootEntry.addClass(classTreeOpened);
              var subEntries = data.sitemap.rootEntry.subEntries;
              for (var i = 0; i < subEntries.length; i++) {
                  var subEntry = $(subEntries[i].itemhtml);
                  // TODO: the order of the entries
                  rootEntry.after(subEntry);                  
                  var subLevel = rootLevel + 1;                
                  //TODO: bind click event to close the subtree                  
                  subEntry.attr('alt', subEntries[i].sitemapUri)
                          .addClass(classLevelActive + ' ' + classConstLevel + subLevel)
                          .css('margin-left',getLevelMargin((subLevel < 12 ? subLevel : subLevel-5),cms.galleries.constSitemapMargin))
                          .prepend('<div class="cms-tree-opener"></div>');
                  subEntry.find('div[rel=""]').remove();
                  subEntry.find('.cms-list-itemcontent')
                      .append($('<div/>',{ 'class': "cms-handle-button"}));
                  subEntry.find('.cms-handle-button')
                      .append($('<div/>',{'class':'cms-select-item'}))
                      .append($('<div/>',{'class':'cms-preview-item'}));
                  if (subEntries[i].hasSubEntries) {                      
                      subEntry.addClass(classTreeWithSubtree);
                      // TODO: bind click event
                  } else {
                      subEntry.addClass(classTreeWithoutSubtree);
                 }
                 rootEntry = subEntry;                                               
              }   
          }                  
      } 
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
                       // use the title in normal case
                       var content = $('li[alt=' + selectedLis[0] + ']').find('.cms-list-title').text();
                       // use the sitepath for a gallery without title
                       if (searchCriteria == 'galleries' && content.length == 0) {
                           content = $('li[alt=' + selectedLis[0] + ']').find('div[rel="path"]').text();
                       }
                       titles = singleSelect.concat(content);
                       cms.galleries.addCreteriaToTab(titles, searchCriteria);
                   } else if (selectedLis.length > 1) {
                      $.each(selectedLis, function() {
                          if (titles.length == 0) {
                             // use the title in normal case
                             var content1 = $('li[alt="' + selectedLis[0] + '"]').find('.cms-list-title').text();
                             // use the sitepath for a gallery without title
                             if (searchCriteria == 'galleries' && content1.length == 0) {
                                 content1 = $('li[alt="' + selectedLis[0] + '"]').find('div[rel="path"]').text();
                             }
                             titles = multipleSelect.concat(content1);
                          } else {
                              // use the title in normal case
                             var content = $('li[alt="' + this + '"]').find('.cms-list-title').text();
                             // use the sitepath for a gallery without title
                             if (searchCriteria == 'galleries' && content.length == 0) {
                                 content = $('li[alt="' + this + '"]').find('div[rel="path"]').text();
                             }
                             titles = titles.concat(", ").concat(content);
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
      $('.cms-result-criteria').css('display', 'none');
      $('.cms-result-criteria:has(span)').removeAttr('style');
      
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
      
       //var isSelected = $(this.hasClass('cms-list-item-active');
       // deselect selected items
       $('#results li').toggleClass('cms-list-item-active', false);
       if ($(this).hasClass('cms-list-item-active')) {
           this.toggleClass('cms-list-item-active', false);             
       } else {
           this.toggleClass('cms-list-item-active', true);
       }
       
   }
   
  /** 
   * Callback function for dbclick event on the item in the result list.
   * @param {Object} element the selected clicked html element
   */
  var clickToShowPreview = cms.galleries.clickToShowPreview = function(itemId, itemType) {      
      // set the resouce id as alt attribute and empty the content of the preview
      $('#cms-preview').attr('alt', itemId);  

      // reset the active item object
      cms.galleries.resetActiveItem();
      
      // retrieve the resource type and load the preview      
      loadItemPreview(itemId, itemType);                  
  } 
     
    /**
     * Ajax call for the content of the item preview.
     * 
     * @param {Object} itemId the path to the given resource
     * @param {Object} itemType the type of the resource
     */      
    var loadItemPreview = cms.galleries.loadItemPreview = function(/**String*/ itemId, /**String*/itemType) {
      itemType = itemType!=null ? itemType : ''; 
      $.ajax({
         'url': cms.data.GALLERY_SERVER_URL,
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
     * Checkes or uncheckes the checkbox element.
     * 
     * @param {Object} elem the checkbox element to be checked
     * @param {Object} flag true or false
     */
    var checkGalleryCheckbox = cms.galleries.checkGalleryCheckbox = function (elem, flag){
        if (flag) {
            $(elem).addClass('cms-checkbox-checked').removeClass('cms-checkbox-uncheched');    
        } else {
            $(elem).addClass('cms-checkbox-unchecked').removeClass('cms-checkbox-cheched');
        }    
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
