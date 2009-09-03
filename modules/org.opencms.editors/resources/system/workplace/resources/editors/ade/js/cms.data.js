(function(cms) {

   /** Element state 'new' constant. */
   var /** String */ STATUS_NEW = cms.data.STATUS_NEW = 'N';
   
   /** Element state 'created' constant. */
   var /** String */ STATUS_CREATED = cms.data.STATUS_CREATED = 'X';
   
   /** Element state 'new config' constant. */
   var /** String */ STATUS_NEWCONFIG = cms.data.STATUS_NEWCONFIG = 'NC';
   
   /** Element state 'unchanged' constant. */
   var /** String */ STATUS_UNCHANGED = cms.data.STATUS_UNCHANGED = 'U';
   
   /** Element state 'changed' constant. */
   var /** String */ STATUS_CHANGED = cms.data.STATUS_CHANGED = 'C';
   
   /** Timeout in ms for ajax requests. */
   var /** long */ AJAX_TIMEOUT = 5000;
   
   /** Parameter 'obj' value 'all' constant. */
   var /** String */ OBJ_ALL = 'all';
   
   /** Parameter 'obj' value 'rec' constant. */
   var /** String */ OBJ_REC = 'rec';
   
   /** Parameter 'obj' value 'fav' constant. */
   var /** String */ OBJ_FAV = 'fav';
   
   /** Parameter 'obj' value 'cnt' constant. */
   var /** String */ OBJ_CNT = 'cnt';
   
   /** Parameter 'obj' value 'elem' constant. */
   var /** String */ OBJ_ELEM = 'elem';
   
   /** Parameter 'obj' value 'new' constant. */
   var /** String */ OBJ_NEW = 'new';
   
   /** Parameter 'obj' value 'del' constant. */
   var /** String */ OBJ_DEL = 'del';
   
   /** Editors back link uri. */
   var /** String */ BACKLINK_URL = cms.data.BACKLINK_URL = '/system/workplace/resources/editors/ade/backlink.html';
   
   /**  
    * The current container page uri.
    * @see /system/workplace/editors/ade/include.txt
    */
   var /** String */ CURRENT_URI = cms.data.CURRENT_URI;
   
   /**  
    * The xml content editor url.
    * @see /system/workplace/editors/ade/include.txt
    */
   var /** String */ EDITOR_URL = cms.data.EDITOR_URL;
   
   /**  
    * The url for 'get' requests.
    * @see /system/workplace/editors/ade/include.txt
    */
   var /** String */ SERVER_GET_URL = cms.data.SERVER_GET_URL;
   
   /**  
    * The url for 'set' requests.
    * @see /system/workplace/editors/ade/include.txt
    */
   var /** String */ SERVER_SET_URL = cms.data.SERVER_SET_URL;
   
   /** Generic error message for json parse errors. */
   var /** String */ JSON_PARSE_ERROR = 'ERROR: Couldn\'t parse JSON data';
   
   /** Generic error message for ajax load errors. */
   var /** String */ AJAX_LOAD_ERROR = 'ERROR: couldn\'t load data from server';
   
   /** Generic error message for ajax post errors. */
   var /** String */ AJAX_SENT_ERROR = 'ERROR: couldn\'t send data to server';
   
   /** Centralized repository for element objects. */
   var /** Object */ elements = cms.data.elements = {};
   
   /** Centralized repository for container objects. */
   var /** Object */ containers = cms.data.containers = {};
   
   /** The current locale used to render the container page. */
   var /** String */ locale = cms.data.locale = 'en';
   
   /** If the container page itself can be edited, ie. move and delete elements. */
   var /** boolean */ allowEdit = cms.data.allowEdit = true;
   
   /** The name of the user that has locked the container page. */
   var /** String */ lockedBy = cms.data.lockedBy = '';
   
   /** Selector for sortable items. */
   var /** int */ newCounter = cms.data.newCounter = 0;
   
   /** Selector for sortable items. */
   var /** String */ sortitems = cms.data.sortitems = '.cms-element';
   
   /** Selector for deletable items. */
   var /** String */ deleteitems = cms.data.deleteitems = '.cms-element';
   
   
   
   
   
   var currentSearchPage = cms.data.currentSearchPage = -1;
   var currentSearchQuery = cms.data.currentSearchQuery = null;
   var currentSearchPath = cms.data.currentSearchPath = null;
   var currentSearchType = cms.data.currentSearchType = null;
   var moreSearchResults = cms.data.moreSearchResults = false;
   var searchResultIds = cms.data.searchResultIds = [];
   
   
   
   
   
   
   
   
   
   
   /**
    * .<p>
    *
    * @param {Object} elements
    */
   var prepareLoadedElements = cms.data.prepareLoadedElements = /** void */ function(/** Object */elements) {
   
      for (var id in elements) {
         var element = elements[id];
         for (var containerType in element.contents) {
            var oldContent = element.contents[containerType];
            if (oldContent) {
               element.contents[containerType] = $(oldContent).attr('rel', element.id).addClass('cms-element').appendTo($('<div></div>')).parent().html();
            }
         }
      }
   }
   
   /**
    * Same as prepareLoadedElements, but works with an array.
    **/
   var prepareLoadedElementsArray = cms.data.prepareLoadedElementsArray = function(elements) {
      for (var i = 0; i < elements.length; i++) {
         var element = elements[i];
         var id = element.id;
         for (var containerType in element.contents) {
            var oldContent = element.contents[containerType];
            if (oldContent) {
               element.contents[containerType] = $(oldContent).attr('rel', element.id).addClass('cms-element').appendTo($('<div></div>')).parent().html();
            }
         }
      }
   }
   
   /**
    * .<p>
    *
    * @param {Function} afterLoad
    */
   var loadAllData = cms.data.loadAllData = /** void */ function(/** void Function(boolean) */afterLoad) {
   
      $.ajax({
         'url': SERVER_GET_URL,
         'data': {
            'obj': OBJ_ALL,
            'url': CURRENT_URI
         },
         'timeout': AJAX_TIMEOUT,
         'error': function(xhr, status, error) {
            alert(AJAX_LOAD_ERROR);
            afterLoad(false);
         },
         'success': function(data) {
            try {
               var jsonData = JSON.parse(data);
            } catch (e) {
               alert(JSON_PARSE_ERROR);
               afterLoad(false);
               return;
            }
            prepareLoadedElements(jsonData.elements);
            if (jsonData.state == 'error') {
               alert(jsonData.error);
               afterLoad(false);
               return;
            }
            
            if (jsonData.favorites) {
               favorites = cms.toolbar.favorites = jsonData.favorites;
            }
            if (jsonData.recent) {
               recent = cms.toolbar.recent = jsonData.recent;
            }
            if (jsonData.containers) {
               containers = cms.data.containers = jsonData.containers;
            }
            if (jsonData.elements) {
               elements = cms.data.elements = jsonData.elements;
               _initNewCounter(elements);
            }
            if (jsonData.newCounter) {
               newCounter = cms.data.newCounter = jsonData.newCounter;
            }
            if (jsonData.locale) {
               locale = cms.data.locale = jsonData.locale;
            }
            if (jsonData.allowEdit) {
               allowEdit = cms.data.allowEdit = jsonData.allowEdit;
            }
            if (jsonData.locked) {
               lockedBy = cms.data.lockedBy = jsonData.locked;
            }
            afterLoad(true);
         }
      });
   }
   
   /**
    * .<p>
    *
    * @param {Object} data
    * @param {Function} afterLoad
    */
   var loadJSON = cms.data.loadJSON = /** void */ function(/** Object */data, /** void Function(boolean, Object) */ afterLoad) {
   
      $.extend(data, {
         'url': CURRENT_URI
      });
      $.ajax({
         'url': SERVER_GET_URL,
         'data': data,
         'timeout': AJAX_TIMEOUT,
         'error': function(xhr, status, error) {
            alert(AJAX_LOAD_ERROR);
            afterLoad(false, {});
         },
         'success': function(data) {
            try {
               var jsonData = JSON.parse(data);
            } catch (e) {
               alert(JSON_PARSE_ERROR);
               afterLoad(false, {});
               return;
            }
            if (jsonData.state == 'error') {
               alert(jsonData.error);
               afterLoad(false, jsonData);
               return;
            }
            afterLoad(true, jsonData);
         }
      });
   }
   
   /**
    * .<p>
    *
    * @param {String} obj
    * @param {Object} data
    * @param {Function} afterPost
    */
   var postJSON = cms.data.postJSON = /** void */ function(/** String */obj, /** Object */ data, /** void Function(boolean, Object) */ afterPost) {
   
      $.ajax({
         'url': SERVER_SET_URL,
         'data': {
            'url': CURRENT_URI,
            'obj': obj,
            'data': JSON.stringify(data)
         },
         'type': 'POST',
         'timeout': AJAX_TIMEOUT,
         'error': function(xhr, status, error) {
            alert(AJAX_SENT_ERROR);
            afterPost(false);
         },
         'success': function(data) {
            try {
               var jsonData = JSON.parse(data);
            } catch (e) {
               alert(JSON_PARSE_ERROR);
               afterPost(false, {});
               return;
            }
            if (jsonData.state == 'error') {
               alert(jsonData.error);
               afterPost(false, jsonData);
               return;
            }
            afterPost(true, jsonData);
         }
      });
   }
   
   /**
    * .<p>
    *
    * @param {String} type
    * @param {Function} afterCreate
    *
    * @see cms.toolbar.openEditDialog
    */
   var createResource = cms.data.createResource = /** void */ function(/** String */type, /** void Function(boolean, String, String) */ afterCreate) {
   
      loadJSON({
         'obj': OBJ_NEW,
         'data': type
      }, function(ok, data) {
         afterCreate(ok, data.id, data.uri);
      });
   }
   
   /**
    * .<p>
    *
    * @param {Array} ids
    * @param {Function} afterDelete
    */
   var deleteResources = cms.data.deleteResources = /** void */ function(/** Array */ids, /** void Function(boolean) */ afterDelete) {
   
      postJSON(OBJ_DEL, ids, function(ok) {
         afterDelete(ok);
      });
   }
   
   /**
    * .<p>
    *
    * @param {String} id
    * @param {Function} afterReload
    */
   var reloadElement = cms.data.reloadElement = /** void */ function(/** String */id, /** void Function(boolean, Object) */ afterReload) {
   
      var/**boolean*/ restoreState = false;
      if (cms.data.elements[id] && (cms.data.elements[id].status == STATUS_CREATED)) {
         restoreState = true;
      }
      loadJSON({
         'obj': OBJ_ELEM,
         'elem': id
      }, function(ok, data) {
         if (ok) {
            cms.data.elements[id] = data.elements[id];
            if (restoreState) {
               // keep the state of client-side created
               cms.data.elements[id].status = STATUS_CREATED;
            }
            fillContainers();
         }
         afterReload(ok, data);
      });
   }
   
   /**
    * .<p>
    *
    * @param {String} ids
    * @param {Function} afterLoad
    */
   var loadElements = cms.data.loadElements = /** void */ function(/** Array<String> */ids, /** void Function(boolean, Object) */ afterLoad) {
   
      loadJSON({
         'obj': OBJ_ELEM,
         'elem': JSON.stringify(ids)
      }, function(ok, data) {
         if (ok) {
            for (var id in ids) {
               cms.data.elements[id] = data.elements[id];
            }
         }
         afterLoad(ok, data);
      });
   }
   
   /**
    * .<p>
    *
    * @param {Function} afterFavoritesLoad
    */
   var loadFavorites = cms.data.loadFavorites = /** void */ function(/** void Function(boolean, Object) */afterFavoritesLoad) {
   
      loadJSON({
         obj: OBJ_FAV
      }, function(ok, data) {
         if (ok) {
            cms.toolbar.favorites = data.favorites;
            
            var idsToLoad = getElementsToLoad(cms.toolbar.favorites);
            if (idsToLoad.length != 0) {
               loadElements(idsToLoad, function(ok2, data2) {
                  if (ok2) {
                     afterFavoritesLoad(ok2, data)
                  }
               });
            } else {
               afterFavoritesLoad(ok, data);
            }
         }
      });
      
   }
   
   var loadRecent = cms.data.loadRecent = /** void */ function(/** void Function(boolean, Object) */afterRecentLoad) {
   
      loadJSON({
         obj: OBJ_REC
      }, function(ok, data) {
         if (ok) {
            cms.toolbar.recent = data.recent;
            
            var idsToLoad = getElementsToLoad(cms.toolbar.recent);
            if (idsToLoad.length != 0) {
               loadElements(idsToLoad, function(ok2, data2) {
                  if (ok2) {
                     afterRecentLoad(ok2, data)
                  }
               });
            } else {
               afterRecentLoad(ok, data);
            }
         }
      });
      
   }
   
   /**
    * .<p>
    *
    * @param {Function} afterRecentLoad
    */
   var loadRecent = cms.data.loadRecent = /** void */ function(/** void Function(boolean, Object) */afterRecentLoad) {
   
      loadJSON({
         obj: OBJ_REC
      }, function(ok, data) {
         if (ok) {
            cms.toolbar.recent = data.recent;
         }
         afterRecentLoad(ok, data);
      });
   }
   
   /**
    * .<p>
    *
    * @param {Function} afterSave
    */
   var persistContainers = cms.data.persistContainers = /** void */ function(/** void Function(boolean, Object) */afterSave) {
   
      // create new object with additional info,
      // like formatter & element uris, just to improve performance
      var cnts = {
         'locale': locale
      };
      $.each(cms.data.containers, function(key, dataCnt) {
         cnts[key] = {
            'name': dataCnt.name,
            'type': dataCnt.type,
            'elements': []
         };
         var cntType = dataCnt.type;
         $.each(dataCnt.elements, function() {
            var elem = {
               'id': this,
               'uri': cms.data.elements[this].file,
               'formatter': cms.data.elements[this].formatters[cntType]
            };
            cnts[key].elements.push(elem);
         });
      });
      // send the data
      postJSON(OBJ_CNT, cnts, afterSave);
   }
   
   /**
    * .<p>
    *
    * @param {Function} afterSave
    */
   var persistFavorites = cms.data.persistFavorites = /** void */ function(/** void Function(boolean, Object) */afterSave) {
   
      postJSON(OBJ_FAV, cms.toolbar.favorites, afterSave);
   }
   
   /**
    * .<p>
    *
    * @param {Function} afterSave
    */
   var persistRecent = cms.data.persistRecent = /** void */ function(/** void Function(boolean, Object) */afterSave) {
   
      postJSON(OBJ_REC, cms.toolbar.recent, afterSave);
   }
   
   /**
    * .<p>
    */
   var fillContainers = cms.data.fillContainers = /** void */ function() {
   
      for (var containerName in containers) {
         $('#' + containerName + ' > *').remove();
         var elementIds = containers[containerName].elements;
         for (var i = 0; i < elementIds.length; i++) {
            var elem = elements[elementIds[i]];
            
            var html = '';
            var isSubcontainer = false;
            if (elem.subItems) {
            
               isSubcontainer = true;
               html = $('<div class="cms-subcontainer"></div>');
               for (var j = 0; j < elem.subItems.length; j++) {
                  var subElem = elements[elem.subItems[j]];
                  $(subElem.contents[containers[containerName].type]).attr('rel', subElem.id).addClass('cms-element').appendTo(html);
               }
            } else {
               html = $(elem.contents[containers[containerName].type]);
            }
            html.attr('rel', elem.id).addClass('cms-element');
            $('#' + containerName).append(html);
            if (isSubcontainer) {
            
               var floatDirection = html.children('*:first').css('float');
               if (floatDirection && (/left|right/).test(floatDirection)) {
                  var dimensions = cms.util.getInnerDimensions(html, 0);
                  var addMargin;
                  if (floatDirection == 'left') {
                     addMargin = parseFloat(html.children('*:first').css('margin-left')) + parseFloat(html.children('*:last').css('margin-right'));
                  } else {
                     addMargin = parseFloat(html.children('*:first').css('margin-right')) + parseFloat(html.children('*:last').css('margin-left'));
                  }
                  html.attr('title', addMargin);
                  html.children('*:visible').each(function() {
                     $(this).width($(this).width());
                  });
                  html.width(dimensions.width + addMargin).addClass('cms-' + floatDirection);
               }
            }
         }
      }
   }
   
   
   
   /**
    * Handler for search results, but not for the first page of results of a new search
    * @param {Object} ok status of the AJAX request
    * @param {Object} data the JSON data from the AJAX response.
    */
   var handleSearchResults = cms.data.handleSearchResults = function(ok, data) {
      if (!ok) {
         cms.toolbar.searchLoadingSign.stop();
         return;
      }
      cms.data.currentSearchPage += 1;
      var searchResults = data.elements;
      cms.data.prepareLoadedElementsArray(searchResults);
      for (var i = 0; i < searchResults.length; i++) {
         var result = searchResults[i];
         cms.data.elements[result.id] = result;
         addSearchResult(result);
      }
      cms.data.moreSearchResults = data.hasmore;
      cms.toolbar.searchLoadingSign.stop();
   }
   
   /**
    * Handler for the first page of search results of a new search.<p>
    *
    * @param {Object} ok
    * @param {Object} data
    */
   var handleNewSearchResults = cms.data.handleNewSearchResults = function(ok, data) {
      if (!ok) {
         return;
      }
      
      handleSearchResults(ok, data);
   }
   
   /**
    * Adds an element to the list of search results in the DOM.<p>
    * @param {Object} element the element which should be added to the search results.
    */
   var addSearchResult = cms.data.addSearchResult = function(/*Object*/result) {
      cms.data.searchResultIds.push(result.id);
      var $content = $(result.contents['_DEFAULT_']);
      var $inner = $('#cms-search-list');
      $('.cms-head', $content).append('<a class="cms-handle cms-move"></a>');
      $inner.append($content);
   }
   
   /**
    * Removes all search results from the DOM.<p>
    */
   var clearSearchResults = cms.data.clearSearchResults = function() {
      cms.data.searchResultIds.length = 0;
      var $inner = $('#cms-search-list');
      $inner.empty();
   }
   
   /**
    * Handler for the results of the checkLastSearch function.
    * @param {Object} ok the status of the AJAX call
    * @param {Object} data the JSON data from the AJAX call
    */
   var handleLastSearch = cms.data.handleLastSearch = function(ok, data) {
      if (!ok) {
         cms.toolbar.searchLoadingSign.stop();
         return;
      }
      if (data.elements) {
         prepareLoadedElementsArray(data.elements);
         cms.data.currentSearchPage = 1;
         cms.data.currentSearchQuery = data.text;
         cms.data.currentSearchPath = data.location;
         cms.data.currentSearchType = data.type
         
         clearSearchResults();
         var searchResults = data.elements;
         for (var i = 0; i < searchResults.length; i++) {
            var result = searchResults[i];
            cms.data.elements[result.id] = result;
            addSearchResult(result);
         }
         cms.data.moreSearchResults = data.hasmore;
         cms.toolbar.searchLoadingSign.stop();
      }
   }
   
   
   /**
    * Starts a new search.<p>
    *
    * @param {Object} query the search query string
    * @param {Object} type the string which is a comma-separated list of types to which the search is restricted
    * @param {Object} path the VFS path in which to search
    */
   var startNewSearch = cms.data.startNewSearch = function(query, type, path) {
      cms.data.currentSearchPage = 0;
      cms.data.currentSearchPath = path;
      cms.data.currentSearchType = type;
      cms.data.currentSearchQuery = query;
      clearSearchResults();
      loadJSON({
         obj: 'search',
         text: query,
         type: type,
         location: path,
         page: 0
      }, cms.data.handleNewSearchResults);
      
   }
   
   /**
    * Continues the last search, e.g. when scrolling past the last loaded search result.
    *
    */
   var continueSearch = cms.data.continueSearch = function() {
      loadJSON({
         obj: 'search',
         text: cms.data.currentSearchQuery,
         type: cms.data.currentSearchType,
         location: cms.data.currentSearchPath,
         page: cms.data.currentSearchPage
      }, cms.data.handleSearchResults)
      
   }
   
   /**
    * Sends the current search parameters to the server to check if they're the same
    * as the last search performed.<p>
    *
    * @param {Object} callback the callback that will be called after normal processing of the AJAX response.
    */
   var checkLastSearch = cms.data.checkLastSearch = function(callback) {
      loadJSON({
         obj: 'ls',
         text: cms.data.currentSearchQuery,
         type: cms.data.currentSearchType,
         location: cms.data.currentSearchPath,
         page: "0"
      }, function(ok, data) {
         cms.data.handleLastSearch(ok, data);
         callback(ok, data);
      });
   }
   
   /**
    * Initializes the counter for new elements by generating the first unused element id
    * @param {Object} elements the map in which to look for existing element ids
    */
   var _initNewCounter = function(/*Object*/elements) {
      var newCounter = 0;
      do {
         newCounter += 1;
         var newName = "new_" + newCounter;
      } while (cms.data.elements[newName]);
      cms.data.newCounter = newCounter;
   }
   
   var getElementsToLoad = /*Array*/ function(/*Array*/ids) {
      return $.grep(ids, function(id) {
         return !(cms.data.elements[id]) && id.match(/^ade_/);
      });
   }
   
   
   ///////// these function are for debugging /////////////
   var serialize = function() {
   
      var ser = {
         'container': []
      };
      $(sortlist).each(function(i) {
         ser.container[i] = {
            'id': $(this).attr('id'),
            'elements': []
         };
         $(this).children().each(function(ie, elem) {
            ser.container[i].elements[ie] = {
               'id': $(elem).attr('id'),
               'index': ie
            }
         });
      });
      $('body').append('<p>' + JSON.stringify(ser) + '</p>');
   };
   
   var getSerializeString = function() {
   
      var ser = {
         'container': []
      };
      $(sortlist).each(function(i) {
         ser.container[i] = {
            'id': $(this).attr('id'),
            'elements': []
         };
         $(this).children().each(function(ie, elem) {
            ser.container[i].elements[ie] = {
               'id': $(elem).attr('id'),
               'index': ie
            }
         });
      });
      return JSON.stringify(ser);
   };
   
   
   
   
})(cms);
