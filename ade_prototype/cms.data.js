(function(cms) {

   // constants
   var UNCHANGED = 'u';
   var CHANGED = 'c';
   var NEW = 'n';
   
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
   
   var prepareLoadedElements = function(elements) {
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
            afterLoad();
         }
      })
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
            afterPost();
         }
      });
   }
   
   
   var reloadElement = cms.data.reloadElement = function(id) {
   
      loadJSON({
         obj: OBJ_ELEM,
         elem: id
      }, function(data) {
         cms.data.elements[id] = data.elements[id];
         fillContainers();
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
