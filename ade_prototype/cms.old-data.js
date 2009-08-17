(function(cms) {

    
   var UNCHANGED = 'u';
   var CHANGED = 'c';
   var NEW = 'n';
   
   
   var serialize = function() {
      var ser = {
         'container': []
      };
      $(cms.util.getContainerSelector()).each(function(i) {
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
      $(cms.util.getContainerSelector()).each(function(i) {
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
   var locale = cms.data.locale = "en";
   var BACKLINK_URL = cms.data.BACKLINK_URL = "backlink.jsp";
   var EDITOR_URL = cms.data.EDITOR_URL = "/system/workplace/editors/editor.jsp";
   var DATA_URL = "data.txt";
   var AJAX_TIMEOUT = 5000;
   var RECENT_URL = "recent.txt";
   var FAVORITE_URL = "fav.txt";
   var ITEM_URL = "item.json";
   var FAV_SAVE_URL = "sdfafsd";
   var RECENT_SAVE_URL = "fgsdfsdfad";
   var CONTAINER_SAVE_URL = "fsdafafasfd";
   
   var JSON_PARSE_ERROR = "ERROR: Couldn't parse JSON data";
   
   var persistFavorites = cms.data.persistFavorites = function() {
      // dummy
   }
   
   var persistRecent = cms.data.persistRecent = function() {
      // dummy
   }
   
   var loadAllData = cms.data.loadAllData = function(afterLoad) {
      $.ajax({
         url: DATA_URL,
         timeout: AJAX_TIMEOUT,
         error: function(xhr, status, error) {
            alert("ERROR: couldn't load data from server");
         },
         success: function(data) {
            try {
               var jsonData = JSON.parse(data);
            } catch (e) {
               alert("ERROR: Couldn't parse JSON data");
               return;
            }
            prepareLoadedElements(jsonData.elements);
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
   
   var loadJSON = cms.data.loadJSON = function(url, data, afterLoad) {
      $.ajax({
         url: url,
         timeout: AJAX_TIMEOUT,
         data: data,
         error: function(xhr, status, error) {
            alert("ERROR: couldn't load data from server");
         },
         success: function(data) {
            try {
               var jsonData = JSON.parse(data);
            } catch (e) {
               alert(JSON_PARSE_ERROR);
               return;
            }
            afterLoad(jsonData);
         }
      });
   }
   
   var postJSON = cms.data.postJSON = function(url, data, afterPost) {
      $.ajax({
         type: 'POST',
         timeout: AJAX_TIMEOUT,
         data: data,
         error: function(xhr, status, error) {
            alert("ERROR: couldn't send data to server");
         },
         success: afterPost
      });
   }
   
   
   var reloadElement = cms.data.reloadElement = function(id) {
      loadJSON(ITEM_URL, {
         url: window.location.href,
         id: id
      }, function(data) {
         cms.data.elements[data.id] = data;
         fillContainers();
      });
   }
   
   var loadElements = cms.data.loadElements = function(ids) {
      loadJSON(ITEMS_URL, {
         url: window.location.href,
         ids: JSON.stringify(ids)
      }, function(data) {
         for (var id in data) {
            cms.data.elements[data.id] = data;
         }
      });
   }
   
   var loadFavorites = cms.data.loadFavorites = function(afterFavoritesLoad) {
      setTimeout(afterFavoritesLoad, 1000);
//      loadJSON(FAVORITES_URL, {
//         url: window.location.href,
//         id: id
//      }, function(data) {
//         cms.toolbar.favorites = data;
//         afterFavoritesLoad();
//         
//      });
   }
   
   var loadRecent = cms.data.loadRecent = function(afterRecentLoad) {
        setTimeout(afterRecentLoad, 1000);
//      loadJSON(RECENT_URL, {
//         url: window.location.href,
//         id: id
//      }, function(data) {
//         cms.toolbar.recent = data;
//         afterRecentLoad();
//      });
   }
   
   var persistContainers = cms.data.persistContainers = function(afterSave) {
      postJSON(CONTAINER_SAVE_URL, {
         'url': window.location.href,
         'containers': JSON.stringify(cms.data.containers)
      }, function() {
            });
   }
   
   var persistFavorites = cms.data.persistFavorites = function() {
      //postJSON(FAV_SAVE_URL, {'favorites': JSON.stringify(cms.toolbar.favorites)}, function() {});
   }
   
   var persistRecent = cms.data.persistRecent = function() {
      //postJSON(RECENT_SAVE_URL, {'recent': JSON.stringify(cms.toolbar.recent)}, function() {});
   }
   
   
   
})(cms);
