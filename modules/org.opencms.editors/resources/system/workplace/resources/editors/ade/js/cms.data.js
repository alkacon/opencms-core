(function(cms) {

   
   var STATUS_NEW = cms.data.STATUS_NEW = 'n';
   var STATUS_CREATED = cms.data.STATUS_CREATED = 'nc';
   var STATUS_UNCHANGED = cms.data.STATUS_UNCHANGED = 'u';
   var STATUS_CHANGED = cms.data.STATUS_CHANGED = 'c';
   
   var AJAX_TIMEOUT = 5000;
   
   // obj parameter value constants
   var OBJ_ALL = "all";
   var OBJ_REC = "rec";
   var OBJ_FAV = "fav";
   var OBJ_CNT = "cnt";
   var OBJ_ELEM = "elem";
   
   // shortcuts
   var CURRENT_URI = cms.data.CURRENT_URI;
   var BACKLINK_URL = cms.data.BACKLINK_URL;
   var EDITOR_URL = cms.data.EDITOR_URL;
   var SERVER_GET_URL = cms.data.SERVER_GET_URL;
   var SERVER_SET_URL = cms.data.SERVER_SET_URL;
   
   // error messages
   var JSON_PARSE_ERROR = "ERROR: Couldn't parse JSON data";
   var AJAX_LOAD_ERROR = "ERROR: couldn't load data from server";
   var AJAX_SENT_ERROR = "ERROR: couldn't send data to server";
   
   // data definition with initial values
   var elements = cms.data.elements = {};
   var containers = cms.data.containers = {};
   var locale = cms.data.locale = "en";
   
   var newCounter = cms.data.newCounter = 0;
   
   var prepareLoadedElements = cms.data.prepareLoadedElements = function(elements) {
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
   
   
   var loadAllData = cms.data.loadAllData = function(afterLoad) {
   
      $.ajax({
         url: SERVER_GET_URL,
         data: {
            obj: OBJ_ALL,
            url: CURRENT_URI
         },
         timeout: AJAX_TIMEOUT,
         error: function(xhr, status, error) {
            alert(AJAX_LOAD_ERROR);
         },
         success: function(data) {
            try {
               var jsonData = JSON.parse(data);
            } catch (e) {
               alert(JSON_PARSE_ERROR);
               return;
            }
            prepareLoadedElements(jsonData);
            if (jsonData.state == "error") {
               alert(jsonData.error);
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
            }
            
            if (jsonData.newCounter) {
               newCounter = cms.data.newCounter = jsonData.newCounter;
            }
            addDummyTypes();
            afterLoad();
         }
      })
   }
   
   var addDummyTypes = function() {
      var news = {
         "id": "news",
         "navText": "news (navText)",
         "title": "news (title)",
         "file": null,
         "date": null,
         "user": null,
         "type": "news",
         "contents": {
            "leftColumn": "<div class=\"box box_schema2\" rel=\"news\"><h4>(Title)</h4><div class=\"boxbody\"><p>(Content)</p></div></div>"
         },
         "subItems": null,
         "allowMove": true,
         "allowEdit": true,
         "locked": false,
         "status": cms.data.STATUS_NEW
      };
      var event = {
         "id": "event",
         "navText": "event (navText)",
         "title": "event (title)",
         "file": null,
         "date": null,
         "user": null,
         "type": "event",
         "contents": {
            "leftColumn": "<div class=\"box box_schema2\" rel=\"event\"><h4>(Title)</h4><div class=\"boxbody\"><p>(Content)</p></div></div>"
         },
         "subItems": null,
         "allowMove": true,
         "allowEdit": true,
         "locked": false,
         "status": cms.data.STATUS_NEW
      };
      cms.data.elements["news"] = news;
      cms.data.elements["event"] = event;
      
   }
   
   var loadJSON = cms.data.loadJSON = function(data, afterLoad) {
   
      $.extend(data, {
         url: CURRENT_URI
      });
      $.ajax({
         url: SERVER_GET_URL,
         data: data,
         timeout: AJAX_TIMEOUT,
         error: function(xhr, status, error) {
            alert(AJAX_LOAD_ERROR);
         },
         success: function(data) {
            try {
               var jsonData = JSON.parse(data);
            } catch (e) {
               alert(JSON_PARSE_ERROR);
               return;
            }
            if (jsonData.state == "error") {
               alert(jsonData.error);
               return;
            }
            afterLoad(jsonData);
         }
      });
   }
   
   var postJSON = cms.data.postJSON = function(obj, data, afterPost) {
   
      $.ajax({
         url: SERVER_SET_URL,
         data: {
            url: CURRENT_URI,
            obj: obj,
            data: JSON.stringify(data)
         },
         type: 'POST',
         timeout: AJAX_TIMEOUT,
         error: function(xhr, status, error) {
            alert(AJAX_SENT_ERROR);
         },
         success: function(data) {
            try {
               var jsonData = JSON.parse(data);
            } catch (e) {
               alert(JSON_PARSE_ERROR);
               return;
            }
            if (jsonData.state == "error") {
               alert(jsonData.error);
               return;
            }
            if ($.isFunction(afterPost)) 
               afterPost(jsonData);
         }
      });
   }
   
   var createResource = cms.data.createResource = function(type, afterCreate) {
      afterCreate("/demo_en/new_news.html", "ade_1b2ba42a-8c0a-11de-affd-f538a2445923");
      //       postJSON('create', type, function(data) {
      //           afterCreate(data.path, data.id);
      //       });
   }
   
   var reloadElement = cms.data.reloadElement = function(id, afterReload) {
   
      loadJSON({
         obj: OBJ_ELEM,
         elem: id
      }, function(data) {
         cms.data.elements[id] = data.elements[id];
         fillContainers();
         if (afterReload) 
            afterReload(data);
      });
   }
   
   var loadElements = cms.data.loadElements = function(ids) {
   
      loadJSON({
         obj: OBJ_ELEM,
         elem: JSON.stringify(ids)
      }, function(data) {
         for (var id in ids) {
            cms.data.elements[id] = data.elements[id];
         }
      });
   }
   
   var loadFavorites = cms.data.loadFavorites = function(afterFavoritesLoad) {
   
      loadJSON({
         obj: OBJ_FAV
      }, function(data) {
         cms.toolbar.favorites = data.favorites;
         afterFavoritesLoad();
      });
   }
   
   var loadRecent = cms.data.loadRecent = function(afterRecentLoad) {
   
      loadJSON({
         obj: OBJ_REC
      }, function(data) {
         cms.toolbar.recent = data.recent;
         afterRecentLoad();
      });
   }
   
   var persistContainers = cms.data.persistContainers = function(afterSave) {
   
      postJSON(OBJ_CNT, cms.data.containers, afterSave);
   }
   
   var persistFavorites = cms.data.persistFavorites = function(afterSave) {
   
      postJSON(OBJ_FAV, cms.toolbar.favorites, afterSave);
   }
   
   var persistRecent = cms.data.persistRecent = function(afterSave) {
   
      postJSON(OBJ_REC, cms.toolbar.recent, afterSave);
   }
   
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
   
   var fillContainers = cms.data.fillContainers = function() {
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
})(cms);
