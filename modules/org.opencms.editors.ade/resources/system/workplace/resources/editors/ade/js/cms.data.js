﻿(function(cms) {

   /** Element state 'new' constant. */
   var /** String */ STATUS_NEW = cms.data.STATUS_NEW = 'N';
   
   /** Element state 'created' constant. */
   var /** String */ STATUS_CREATED = cms.data.STATUS_CREATED = 'X';
   
   /** Element state 'new/search config' constant. */
   var /** String */ STATUS_NEWCONFIG = cms.data.STATUS_NEWCONFIG = 'NC';
   
   /** Element state 'unchanged' constant. */
   var /** String */ STATUS_UNCHANGED = cms.data.STATUS_UNCHANGED = 'U';
   
   /** Element state 'changed' constant. */
   var /** String */ STATUS_CHANGED = cms.data.STATUS_CHANGED = 'C';
   
   /** Timeout in ms for ajax requests. */
   var /** long */ AJAX_TIMEOUT = 20000;
   
   /** Parameter 'action' value 'all' constant. */
   var /** String */ ACTION_ALL = 'all';
   
   /** Parameter 'action' value 'get' constant. */
   var /** String */ ACTION_GET = 'get';
   
   /** Parameter 'action' value 'set' constant. */
   var /** String */ ACTION_SET = 'set';
   
   /** Parameter 'action' value 'cnt' constant. */
   var /** String */ ACTION_CNT = 'cnt';
   
   /** Parameter 'action' value 'elem' constant. */
   var /** String */ ACTION_ELEM = 'elem';
   
   /** Parameter 'action' value 'new' constant. */
   var /** String */ ACTION_NEW = 'new';
   
   /** Parameter 'action' value 'del' constant. */
   var /** String */ ACTION_DEL = 'del';
   
   /** Parameter 'action' value 'startedit' constant. */
   var /** String */ ACTION_STARTEDIT = 'startedit';
   
   /** Parameter 'action' value 'stopedit' constant. */
   var /** String */ ACTION_STOPEDIT = 'stopedit';
   
   /** Parameter 'action' value 'search' constant. */
   var /** String */ ACTION_SEARCH = cms.data.ACTION_SEARCH = 'search';
   
   /** Parameter 'action' value 'ls' constant. */
   var /** String */ ACTION_LS = cms.data.ACTION_LS = 'ls';
   
   /** Editors back link uri. */
   var /** String */ BACKLINK_URL = cms.data.BACKLINK_URL = '/system/workplace/resources/editors/ade/backlink.html';
   
   /**  
    * The current page uri.
    * @see /system/workplace/editors/ade/include.txt
    */
   var /** String */ CURRENT_URI = cms.data.CURRENT_URI;
   
   /**  
    * The current locale.
    * @see /system/workplace/editors/ade/include.txt
    */
   var /** String */ LOCALE = cms.data.LOCALE;
   
   /**  
    * The current container page uri.
    * @see /system/workplace/editors/ade/include.txt
    */
   var /** String */ CURRENT_CNT_PAGE = cms.data.CURRENT_CNT_PAGE;
   
   /**  
    * The xml content editor url.
    * @see /system/workplace/editors/ade/include.txt
    */
   var /** String */ EDITOR_URL = cms.data.EDITOR_URL;
   
   /**  
    * The url for server requests.
    * @see /system/workplace/editors/ade/include.txt
    */
   var /** String */ SERVER_URL = cms.data.SERVER_URL;
   
   /** Generic error message for json parse errors. */
   var /** String */ JSON_PARSE_ERROR = cms.messages.JSON_PARSE_ERROR;
   
   /** Generic error message for ajax load errors. */
   var /** String */ AJAX_LOAD_ERROR = cms.messages.AJAX_LOAD_ERROR;
   
   /** Generic error message for ajax post errors. */
   var /** String */ AJAX_SENT_ERROR = cms.messages.AJAX_SENT_ERROR;
   
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
   
   
   /** Search result list. */
   var /** Array<String> */ searchResultList = cms.data.searchResultList = [];
   
   /** New element types, with name and nice name. */
   var /** Array */ newTypes = cms.data.newTypes = [];
   
   /** Keeping track of requests to be able to abort them. */
   var /** Array<XMLHttpRequest> */ requests = [];
   
   /**
    * The request did finish, so dismiss it.<p>
    *
    * @param {XMLHttpRequest} xhr the request object
    */
   var _removeRequest = /** void */ function(/** XMLHttpRequest */ xhr) {
       var pos = requests.indexOf(xhr);
       if (pos >= 0) {
           requests.splice(pos, 1);
       }       
   };
   
   /**
    * Aborts all pending requests.<p>
    */
   var abortAllRequests = cms.data.abortAllRequests = /** void */ function() {
       for (i = 0, l = requests.length; i < l; i++) {
           try {
               requests[i].abort();
           } catch(e) {
               // ignore
           }
       }
   };
   
   /**
    * Initial load function that loads all data needed for ADE to start.
    *
    * @param {Function} afterLoad the function that should be called after loading is finished
    */
   var loadAllData = cms.data.loadAllData = /** void */ function(/** void Function(boolean) */afterLoad) {
   
        postJSON(ACTION_ALL, {}, function(ok, jsonData) {
                        
            if (jsonData.favorites) {
               cms.toolbar.favorites = jsonData.favorites;
            }
            if (jsonData.recent) {
               cms.toolbar.recent = jsonData.recent;
            }
            if (jsonData.containers) {
               containers = cms.data.containers = jsonData.containers;
            }
            if (jsonData.elements) {
            
               elements = cms.data.elements = jsonData.elements;
               
               var newOrder = cms.data.elements.newOrder;
               delete cms.data.elements.newOrder;
               cms.data.newTypes = [];
               var newPos = -1;
               
               var searchOrder = cms.data.elements.searchOrder;
               delete cms.data.elements.searchOrder;
               cms.search.searchParams.types = [];
               cms.search.searchTypes = [];
               var searchPos = -1;
               
               $.each(cms.data.elements, function() {
                  if (this.status == cms.data.STATUS_NEWCONFIG) {
                     if (newOrder == undefined) {
                        newPos++;
                     } else {
                        newPos = newOrder.indexOf(this.type);
                     }
                     if (newPos < 0) {
                         // this element is not a creatable type
                     } else {
                        cms.data.newTypes[newPos] = {
                        'type': this.type,
                        'name': this.typename
                        };
                     }
                     
                     if (searchOrder == undefined) {
                        searchPos++;
                     } else {
                        searchPos = searchOrder.indexOf(this.type);
                     }
                     if (searchPos < 0) {
                        // this element is not a searchable type
                     } else {
                     cms.search.searchParams.types.push({
                        'name': this.type,
                        'checked': true
                     });
                        cms.search.searchTypes.push({
                           'type': this.type,
                           'name': this.typename
                        });
                  }
                  }
               });
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
            if (jsonData.recentListSize) {
               cms.toolbar.recentSize = jsonData.recentListSize;
            }
            afterLoad(true);
      });
   }
   
   /**
    * Generic function for posting JSON data to the server.
    *
    * @param {String} action a string to tell the server what to do with the data
    * @param {Object} data the JSON data
    * @param {Function} afterPost the callback that should be called after the server replied
    * @param {boolean} async optional flag to indicate is the request should synchronized or not, by default it is not
    * @param {int} timeout optional timeout in millisecs, default is #AJAX_TIMEOUT
    */
   var postJSON = cms.data.postJSON = /** void */ function(/** String */action, /** Object */ data, /** void Function(boolean, Object) */ afterPost, /** boolean */ sync, /** int */ timeout) {
   
      var async = !sync;
      if (!timeout) {
          timeout = AJAX_TIMEOUT;
      }
      var xhr = $.ajax({
         'url': SERVER_URL,
         'data': {
            'cntpage': CURRENT_CNT_PAGE,
            'uri': CURRENT_URI,
            'locale': LOCALE,
            'action': action,
            'data': JSON.stringify(data)
         },
         'type': 'POST',
         'timeout': timeout,
         'async': async,
         'error': function(xhr, status, error) {
             _removeRequest(xhr);
            if (cms.toolbar.leavingPage) {
               return;
            }
            alert(AJAX_SENT_ERROR);
            afterPost(false);
         },
         'success': function(data) {
             _removeRequest(xhr);
            try {
               var jsonData = JSON.parse(data, _jsonRevive);
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
      requests.push(xhr);
   }
   
   /**
    * Creates a new resource on the server via AJAX
    *
    * @param {String} type the type of resource to create
    * @param {Function} afterCreate the callback that should be called after creation
    *
    * @see cms.toolbar.openEditDialog
    */
   var createResource = cms.data.createResource = /** void */ function(/** String */type, /** void Function(boolean, String, String) */ afterCreate) {
   
      postJSON(ACTION_NEW, {
          'type': type
      }, function(ok, data) {
         afterCreate(ok, data.id, data.uri);
      });
   }
   
   /**
    * Deletes a list of resources on the server via AJAX.
    *
    * @param {Array} ids the list of ids (ade_structureid) of the resources to delete
    * @param {Function} afterDelete the callback that should be called after deletion
    */
   var deleteResources = cms.data.deleteResources = /** void */ function(/** Array */ids, /** void Function(boolean) */ afterDelete) {
   
      postJSON(ACTION_DEL, {
          'elem': ids
      }, afterDelete);
   }
   
   /**
    * Locks the container page on the server via AJAX.
    *
    * @param {Function} callback the callback that should be called
    */
   var startEdit = cms.data.startEdit = /** void */ function(/** void Function(boolean) */ callback) {
   
      postJSON(ACTION_STARTEDIT, {}, callback);
   }
   
   /**
    * Unlocks the container page on the server via AJAX.
    *
    * @param {Function} callback the callback that should be called
    */
   var stopEdit = cms.data.stopEdit = /** void */ function(/** void Function(boolean) */ callback) {
   
      postJSON(ACTION_STOPEDIT, {}, callback);
   }
   
   /**
    * Reloads a single element and resets the containers on the page.
    *
    * @param {String} id the id (ade_structureid) of the element to reload
    * @param {Function} afterReload the callback that should be called after loading is finished
    */
   var reloadElement = cms.data.reloadElement = /** void */ function(/** String */id, /** void Function(boolean, Object) */ afterReload) {
   
      var/**boolean*/ restoreState = false;
      if (cms.data.elements[id] && (cms.data.elements[id].status == STATUS_CREATED)) {
         restoreState = true;
      }
      postJSON(ACTION_ELEM, {
          'elem': id
      }, function(ok, data) {
         if (ok) {
            for (var id in data.elements) {
               cms.data.elements[id] = data.elements[id];
               if (restoreState) {
                  // keep the state of client-side created
                  cms.data.elements[id].status = STATUS_CREATED;
               }
            }
         }
         afterReload(ok, data);
      });
   }
   
   /**
    * Loads (or reloads) a set of elements via AJAX and stores them in cms.data.elements.
    *
    * @param {String} ids a list of ids of the form ade_structureid
    * @param {Function} afterLoad the callback that should be called after loading is finished
    */
   var loadElements = cms.data.loadElements = /** void */ function(/** Array<String> */ids, /** void Function(boolean, Object) */ afterLoad) {
   
      postJSON(ACTION_ELEM, {
          'elem': ids
      }, function(ok, data) {
         if (ok) {
            for (var id in data.elements) {
               cms.data.elements[id] = data.elements[id];
            }
         }
         afterLoad(ok, data);
      });
   }
   
   /**
    * Loads the favorite list using an AJAX call and stores it in cms.toolbar.favorites.
    *
    * @param {Function} afterFavoritesLoad the callback that should be called after loading is finished
    */
   var loadFavorites = cms.data.loadFavorites = /** void */ function(/** void Function(boolean, Object) */afterFavoritesLoad) {
   
      postJSON(ACTION_GET, {'fav': true}, function(ok, data) {
         if (ok) {
            cms.toolbar.favorites = data.favorites;
            
            var idsToLoad = getElementsToLoad(cms.toolbar.favorites);
            if (idsToLoad.length != 0) {
               loadElements(idsToLoad, function(ok2, data2) {
                  // check the actually loaded elements
                  $.each(cms.data.elements, function(key, val) {
                     var pos = $.inArray(key, idsToLoad);
                     if (pos >= 0) {
                        idsToLoad.splice(pos, 1);
                     }
                  });
                  // remove the missing elements from the favlist
                  $.each(idsToLoad, function(key, val) {
                     var pos = $.inArray(key, cms.toolbar.favorites);
                     if (pos >= 0) {
                        cms.toolbar.favorites.splice(pos, 1);
                     }
                  });
                  if (ok2) {
                     loadNecessarySubcontainerElements(cms.data.elements, function(ok3, data3) {
                        afterFavoritesLoad(ok2, data)
                     });
                  }
               });
            } else {
               loadNecessarySubcontainerElements(cms.data.elements, function(ok4, data4) {
                  afterFavoritesLoad(ok, data);
               });
            }
         }
      });
      
   }
   
   
   /**
    * Loads the recent list and saves it in cms.toolbar.recent.
    *
    * @param {Function} afterRecentLoad the callback that should be called after loading is finished
    */
   var loadRecent = cms.data.loadRecent = /** void */ function(/** void Function(boolean, Object) */afterRecentLoad) {
   
      postJSON(ACTION_GET, {'rec': true}, function(ok, data) {
         if (ok) {
            cms.toolbar.recent = data.recent;
            
            var idsToLoad = getElementsToLoad(cms.toolbar.recent);
            if (idsToLoad.length != 0) {
               loadElements(idsToLoad, function(ok2, data2) {
                  if (ok2) {
                     loadNecessarySubcontainerElements(cms.data.elements, function(ok3, data3) {
                        afterRecentLoad(ok2, data)
                     });
                  }
               });
            } else {
               loadNecessarySubcontainerElements(cms.data.elements, function(ok3, data3) {
                  afterRecentLoad(ok, data);
               });
            }
         }
      });
      
   }
   
   /**
    * Persists the container page using an AJAX call.
    *
    * @param {Function} afterSave the callback that should be called after saving
    */
   var persistContainers = cms.data.persistContainers = /** void */ function(/** void Function(boolean, Object) */afterSave) {
   
      // create new object with additional info,
      // like formatter & element uris, just to improve performance
      var data = {
         'locale': locale,
         'containers': {}
      };
      var cnts = data.containers;
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
      postJSON(ACTION_CNT, data, afterSave);
   }
   
   
   /**
    * Persists the favorite list using an AJAX call.
    *
    * @param {Function} afterSave the callback that should be called after saving
    */
   var persistFavorites = cms.data.persistFavorites = /** void */ function(/** void Function(boolean, Object) */afterSave) {
   
      postJSON(ACTION_SET, {'fav': cms.toolbar.favorites}, afterSave);
   }
   
   /**
    * Persists the recent list using an AJAX call.
    *
    * @param {Function} afterSave
    */
   var persistRecent = cms.data.persistRecent = /** void */ function(/** void Function(boolean, Object) */afterSave) {
   
      postJSON(ACTION_SET, {'rec': cms.toolbar.recent}, afterSave);
   }
   
   /**
    * Empties all containers and fills them with the appropriate html from the element data.
    *
    */
   var fillContainers = cms.data.fillContainers = /** void */ function() {
   
      for (var containerName in containers) {
         var containerType = containers[containerName].type;
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
                  subElem.getContent(containerType).appendTo(html);
               }
            } else {
               html = elem.getContent(containerType);
            }
            html.css('position', 'relative');
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
      cms.move.resetNewElementBorders();
      
   }
   
   
   /**
    * Initializes the counter for new elements by generating the first unused element id.<p>
    *
    * @param {Object} elements the map in which to look for existing element ids
    */
   var _initNewCounter = function(/**Object*/elements) {
   
      var newCounter = 0;
      do {
         newCounter += 1;
         var newName = "new_" + newCounter;
      } while (cms.data.elements[newName]);
      cms.data.newCounter = newCounter;
   };
   
   var getElementsToLoad = /*Array*/ function(/*Array*/ids) {
   
      var result = $.grep(ids, function(id) {
         return !(cms.data.elements[id]) && id.match(/^ade_/);
      });
      return result;
   }
   
   /**
    * Loads all subcontainer elements referenced from an existing element collection
    * which haven't already been loaded.
    * @param {Object} elements the elements which may be subcontainers
    * @param {Object} callback the callback which is called after the subcontainer elements are loaded.
    */
   var loadNecessarySubcontainerElements = cms.data.loadNecessarySubcontainerElements = function(elements, callback) {
      var necessaryElements = {}
      var callbackWrapper = function(ok, data) {
         callback(ok, elements);
      }
      $.each(elements, function(key, element) {
         if (element && element.subItems) {
            $.each(element.subItems, function(index, subitem) {
               if (!(elements[subitem] || cms.data.elements[subitem])) {
                  necessaryElements[subitem] = true;
               }
            });
         }
      });
      var ids = cms.util.getKeys(necessaryElements);
      if (ids.length > 0) {
         loadElements(ids, callbackWrapper);
      } else {
         callback(true, elements);
      }
   }
   
   /**
    * Element constructor.
    * @param {Object} data the raw JSON element data
    */
   var Element = cms.data.Element = function(data) {
      for (var key in data) {
         this[key] = data[key];
      }
   }
   
   Element.prototype = {
      /**
       * Retrieves the contents of this element for a given container type.
       *
       * @param {Object} containerType the type of the container
       * @return {jQuery} the content as a fresh jQuery object
       */
      getContent: function(containerType) {
         var content = this.contents[containerType];
         if (content) {
            var $result = $(content).addClass('cms-element').attr('rel', this.id).css('position', 'relative');
            if (containerType != '_DEFAULT_' && (this.status == STATUS_NEWCONFIG)) {
               $result = $result.addClass('cms-new-element');
            }
            return $result;
         } else {
            throw "invalid container for this element";
         }
      },
      
      
      /**
       * This method does the same as getContent, but the parameter is a container.
       * or container name instead of a container type
       *
       * @param {Object} container a container name or container
       */
      getContentForContainer: function(container) {
         if (typeof container == 'string') {
            container = containers[container];
         }
         var containerType = container.type;
         return this.getContent(containerType);
      },
      
      
      /**
       * Clones this object.
       *
       * @return the clone
       */
      clone: function() {
         return new Element(JSON.parse(JSON.stringify(this)));
      },
      
      
      /**
       * Clones this element, gives it an id of the form new_$i and stores in the global element table.
       * @return {Object} the clone
       */
      cloneAsNew: function() {
         var result = this.clone();
         result.id = "new_" + (newCounter++);
         elements[result.id] = result;
         return result;
      }
   }
   
   /**
    * Container constructor.
    * @param {Object} data the raw JSON container data
    */
   var Container = function(data) {
      for (var key in data) {
         this[key] = data[key];
      }
   }
   
   Container.prototype = {
      /**
       * Returns the element objects for elements in this container.
       *
       * @return {Array} an array of Element objects.
       */
      getElements: function() {
         return $.map(this.elements, function(id, i) {
            return cms.data.elements[id];
         });
      }
   }
   
   var reviveTypeMap = {
      'Element': Element,
      'Container': Container
   }
   
   var getProperties = cms.data.getProperties = function(id, callback) {

      postJSON('props', {
          'elem': id
      }, callback);
   }
   
   var getElementWithProperties = cms.data.getElementWithProperties = function(id, properties, callback) {

      postJSON('elemProps', {
         elem: id,
         properties: properties
      }, function(ok, data) {
         for (var elemId in data.elements) {
            cms.data.elements[elemId] = data.elements[elemId];
         }
         callback(ok, data);
      });
   }
   
   /**
    * JSON "revive" function which replaces JSON data structures that represent
    * objects with methods with the actual objects.
    */
   var _jsonRevive = function(key, value) {

      var Type = reviveTypeMap[value.objtype];
      if (Type) {
         return new Type(value);
      }
      return value;
   }
   
   /**
   * AJAX call for getting the publish problem list from the server
   */
   var getPublishProblemList = cms.data.getPublishProblemList = function(callback) {

       postJSON('publish_list', {}, callback);
   }
   
   /**
   * AJAX call for getting the publish list from the server 
   */
   var getPublishList = cms.data.getPublishList = function(related, siblings, project, callback) {

       var params = {
           related: related,
           siblings: siblings
       }
       if ((project != null) && (project != '')) {
           params.project = project;
       } 
       postJSON('publish_list', params, callback, false, 120000)
   }
   
   /**
   * AJAX call for publishing resources.
   */
   var publishResources = cms.data.publishResources = function(resources, removeResources, force, callback) {

       var params = {
           'resources': resources,
           'remove-resources': removeResources,
           'force': force
       }
       postJSON('publish', params, callback, false, 120000);
   }
   
   var getProjects = cms.data.getProjects = function(callback) {

       postJSON('projects', {}, callback);
   }
   
   var getPublishOptions = cms.data.getPublishOptions = function(callback) {

       postJSON('publish_options', {}, callback);
   }
})(cms);

