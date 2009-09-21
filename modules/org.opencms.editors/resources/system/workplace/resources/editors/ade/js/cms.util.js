(function(cms) {
   /*util*/
   var log = cms.util.log = function(s) {
      $("body").append("<p>" + s + "</p>");
   }
   
   /* util */
   var dump = cms.util.dump = function(s) {
      $("<pre></pre>").text($.dump(s)).appendTo("body");
   }
   
   var addUnique = cms.util.addUnique = function(list, item, maxlen) {
      for (var i = 0; i < list.length; i++) {
         if (list[i] == item) {
            list.splice(i, 1);
            break;
         }
      }
      list.splice(0, 0, item);
      if (maxlen) {
         var delLength = list.length - maxlen;
         if (delLength > 0) 
            list.splice(maxlen, delLength);
      }
   }
   
   var addToList = cms.util.addToList = function(resource_id, list, max_size) {
      var newList = [resource_id];
      for (var i = 0; i < list.length; i++) {
         if (resource_id != list[i]) {
            newList.push(list[i]);
         }
         if (max_size && newList.length >= max_size) {
            break;
         }
      }
      return newList;
   }
   
   var clearAttributes = cms.util.clearAttributes = function(elem, attrs) {
      var ie = $.browser.msie;
      for (var i = 0; i < attrs.length; i++) {
         if (ie) {
            elem.removeAttribute(attrs[i]);
         } else {
            elem[attrs[i]] = '';
         }
      }
   }
   
   var getElementPosition = cms.util.getElementPosition = function(elem) {
      var position = {
         left: 0,
         top: 0
      };
      var offset = elem.offset();
      if ($(document.body).css('position') == 'relative' ||
      $(document.body).css('position') == 'absolute') {
         position.left = offset.left - $(document.body).offset().left;
         position.top = offset.top - $(document.body).offset().top;
      } else {
         position.left = offset.left;
         position.top = offset.top;
      }
      return position;
   }
   
   var getInnerDimensions = cms.util.getInnerDimensions = function(elem, minHeight) {
      var dimension = {
         left: 'x',
         top: 'x',
         bottom: 'x',
         right: 'x',
         height: '',
         width: ''
      
      };
      var bottom = 'x';
      var right = 'x';
      var contentElements = elem.children('*:visible:not(.ui-sortable-helper)');
      contentElements = contentElements.add(contentElements.filter('.cms-subcontainer').children('*:visible')).not('.cms-subcontainer');
      if (contentElements.length) {
         contentElements.each(function() {
            var el = $(this);
            var pos = cms.util.getElementPosition(el);
            dimension.left = (dimension.left == 'x' || pos.left < dimension.left) ? pos.left : dimension.left;
            dimension.top = (dimension.top == 'x' || pos.top < dimension.top) ? pos.top : dimension.top;
            dimension.bottom = (dimension.bottom == 'x' ||
            dimension.bottom <
            (pos.top +
            el.outerHeight())) ? pos.top +
            el.outerHeight() : dimension.bottom;
            dimension.right = (dimension.right == 'x' ||
            dimension.right <
            (pos.left +
            el.outerWidth())) ? pos.left +
            el.outerWidth() : dimension.right;
            
         });
      } else {
         var elemPos = getElementPosition(elem);
         dimension.top = elemPos.top;
         dimension.left = elemPos.left;
         dimension.right = dimension.left + elem.innerWidth();
         dimension.bottom = dimension.top + minHeight;
      }
      dimension.height = dimension.bottom - dimension.top;
      dimension.width = dimension.right - dimension.left;
      return dimension;
   }
   
   //var getRequiredWidth = cms.util.getRequiredWidth = function(elem){
   //    
   //}
   
   var fixZIndex = cms.util.fixZIndex = function(currentId, zmap) {
      if (!$.browser.msie) {
         return;
      }
      var z;
      for (var key in zmap) {
         if (key == currentId) {
            z = 9999;
         } else {
            z = zmap[key];
         }
         setZIndex(key, z);
      }
   }
   
   var setZIndex = function(id, z) {
      $('#' + id).css('z-index', z);
   }
   
   /**
    * Creates a selector by replaing the '%' character in a selector template
    * with each of a list of strings, and joining all the resulting strings
    * with commas.
    */
   var makeCombinedSelector = cms.util.makeCombinedSelector = function(strs, template) {
      return $.map(strs, function(s) {
         return template.replace("%", s);
      }).join(", ");
   }
   
   var getKeys = cms.util.getKeys = function(obj) {
      var keys = [];
      for (var key in obj) {
         keys.push(key);
      }
      return keys;
   }
   
   /**
    * Returns a selector that matches all containers on the page.
    */
   var getContainerSelector = cms.util.getContainerSelector = function() {
      return cms.util.makeCombinedSelector(cms.util.getKeys(cms.data.containers), '#%')
   }
   
   
   
   var replaceListElements = function(list, oldElem, newElem) {
      var result = [];
      for (var i = 0; i < list.length; i++) {
         result[i] = list[i] == oldElem ? newElem : list[i];
         
      }
      return result;
   }
   
   
   var replaceNewElement = cms.util.replaceNewElement = function(oldName, newName) {
      for (var containerName in cms.data.containers) {
         var container = cms.data.containers[containerName];
         container.elements = replaceListElements(container.elements, oldName, newName);
      }
   }
   
   
   
   
   var loadFavoriteAndRecentElements = function(afterLoad) {
      var toLoad = {};
      for (var fav in cms.toolbar.favorites) {
         if (!cms.data.elements[fav]) {
            toLoad[fav] = true;
         }
      }
      for (var recent in cms.toolbar.recent) {
         if (!cms.data.elements[recent]) {
            toLoad[recent] = true;
         }
      }
      cms.data.reloadItems(getKeys(toLoad), afterLoad);
      
   };
   
   /**
    * Converts a jquery object to HTML.<p>
    * Only use this for jquery objects which don't have elements attached to the DOM.
    * @param {Object} $jquery the jquery object to convert to html.
    */
   var jqueryToHtml = cms.util.jqueryToHtml = function($jquery) {
      return $jquery.appendTo("<div></div>").parent().html();
   }
   
   
   var validateForm = cms.util.validateForm = function($form) {
   
      $('span.ade-error', $form).remove();
      var hasError = false;
      
      jQuery.each($('ol.ade-forms li.ade-required', $form), function() {
         var $item = $(this);
         if ($item.hasClass('ade-grouping')) {
            var numSelected = $item.find('input:checked').length;
            if (numSelected == 0) {
               var labelText = $('legend', $item).text();
               labelText = labelText.replace(' *', '');
               $item.append('<span class="ade-error">' + labelText + ' can not be empty.</span>');
               hasError = true;
            }
         } else {
            if (jQuery.trim($('input, textarea', $item).val()) == '') {
               var labelText = $('label', $item).text();
               labelText = labelText.replace(' *', '');
               $item.append('<span class="ade-error">' + labelText + ' can not be empty.</span>');
               hasError = true;
            }
         }
      });
      return (!hasError);
   };
   
   /**
    * Aligns the element's left side with a given coordinate
    */
   var setLeft = cms.util.setLeft = function(element, x) {
      var $elem = $(element);
      if ($elem.css('left')) {
         $elem.css('left', x);
      } else {
         $elem.css('right', x + $elem.width());
      }
   }
   
   
   /**
    * Replacement for alert using a jQuery dialog.<p>
    *
    * @param {String} text the text to display in the dialog
    * @param {String} title the title of the dialog
    */
   var dialogAlert = cms.util.dialogAlert = function(/**String*/text, /**String*/ title) {
      var $dialog = $('<div id="cms-alert-dialog" style="display: none"></div>');
      $dialog.appendTo('body');
      $dialog.text(text);
      $dialog.dialog({
         zIndex: 9999,
         title: title,
         modal: true,
         close: function() {
            $dialog.dialog('destroy');
            $dialog.remove();
         },
         buttons: {
            'OK': function() {
               $dialog.dialog('destroy');
               $dialog.remove();
            }
         }
      });
   }
   
   
   /**
    * Checks whether Firebug is active.
    *
    */
   var isFirebugActive = cms.util.isFirebugActive = function() {
      return $('#_firebugConsole').size() > 0;
   }
   
   
   
   
})(cms);
