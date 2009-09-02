(function(cms) {
   var $ = jQuery;
   var over = null;
   var cancel = false;
   cms.move.zIndexMap = {};
   
   var isMenuContainer = cms.move.isMenuContainer = function(id) {
      return id == cms.html.favoriteListId || id == cms.html.recentListId || id == cms.html.newListId || id == cms.html.searchListId ;
   }
   
   var movePreparation = cms.move.movePreparation = function(event) {
      if (cms.toolbar.timer.id) {
         clearTimeout(cms.toolbar.timer.id);
         cms.toolbar.timer.id = null;
      }
      var thisHandleDiv = $(this).closest('.cms-handle').unbind('mouseenter').unbind('mouseleave');
      thisHandleDiv.children().removeClass('ui-corner-all ui-state-default');
      hoverOut();
      $('div.cms-handle').not(thisHandleDiv).hide();
      thisHandleDiv.removeClass('ui-widget-header').children('*:not(.cms-move)').hide();
   }
   
   var moveEnd = cms.move.moveEnd = function(event) {
      var handleDiv = $(this).closest('.cms-handle');
      if (handleDiv) {
         handleDiv.hover(function() {
            cms.move.hoverIn($(this).closest('.cms-element'), 2);
            cms.toolbar.startHoverTimeout(handleDiv, cms.toolbar.timer.adeMode);
         }, function() {
            cms.toolbar.stopHover();
         });
      }
      
      if ('move' != cms.toolbar.timer.adeMode) {
         $(this).hide();
         handleDiv.children('.cms-' + cms.toolbar.timer.adeMode).show();
      }
   }
   
   var saveZIndex = cms.move.saveZInde = function(containerId) {
      cms.move.zIndexMap[containerId] = $('#' + containerId).css('z-index');
   }
   
   var initContainerForDrag = cms.move.initContainerForDrag = function(sortable, container) {
      var containerType = container.type;
      // skip incompatible containers
      if (!cms.data.elements[sortable.cmsResource_id].contents[containerType]) 
         return;
      saveZIndex(container.name);
      
      if (container.name != sortable.cmsStartContainerId) {
         sortable.cmsHoverList += ', #' + container.name;
         var helperElem;
         if (sortable.cmsItem.subItems) {
            helperElem = $('<div class="cms-subcontainer"></div>');
            for (var j = 0; j < sortable.cmsItem.subItems.length; j++) {
               var subElem = cms.data.elements[sortable.cmsItem.subItems[j]];
               $(subElem.contents[containerType]).attr('rel', subElem.id).addClass('cms-element').appendTo(helperElem);
            }
         } else {
            helperElem = $(sortable.cmsItem.contents[containerType]);
         }
         sortable.cmsHelpers[container.name] = helperElem.css({
            'display': 'none',
            'position': 'absolute',
            'zIndex': sortable.options.zIndex
         }).addClass('ui-sortable-helper cms-element').attr('rel', sortable.cmsItem.id).appendTo('#' + container.name);
         cms.toolbar.addHandles(sortable.cmsHelpers[container.name], sortable.cmsResource_id, cms.toolbar.timer.adeMode ? cms.toolbar.timer.adeMode : 'move', true);
         if (sortable.cmsStartContainerId != cms.toolbar.currentMenuItems) {
            $('a.cms-move', sortable.cmsHelpers[container.name]).mousedown(movePreparation).mouseup(moveEnd);
         }
         
         // to increase visibility of the helper
         if (helperElem.css('background-color') == 'transparent' && helperElem.css('background-image') == 'none') {
            helperElem.addClass('cms-helper-background');
         }
         if (!helperElem.css('border') || !helperElem.css('border') == 'none' || helperElem.css('border') == '') {
            helperElem.addClass('cms-helper-border');
         }
      } else {
         sortable.cmsHelpers[container.name] = sortable.helper;
         // to increase visibility of the helper
         if (sortable.helper.css('background-color') == 'transparent' && sortable.helper.css('background-image') == 'none') {
            sortable.helper.addClass('cms-helper-background');
         }
         if (!sortable.helper.css('border') || sortable.helper.css('border') == 'none' || sortable.helper.css('border') == '') {
            sortable.helper.addClass('cms-helper-border');
         }
         sortable.cmsOver = true;
      }
   }
   
   
   var startDragFromMenu = cms.move.startDragFromMenu = function(sortable) {
      sortable.cmsHelpers[cms.toolbar.currentMenuItems] = sortable.helper;
      var elem = $(document.createElement('div')).addClass("placeholder" + " ui-sortable-placeholder box").css('display', 'none');
      sortable.placeholder.replaceWith(elem);
      sortable.placeholder = elem;
      
      $('.cms-additional', sortable.currentItem).hide();
      
      sortable.helper.appendTo('#cms_appendbox');
      
      $('#' + sortable.cmsStartContainerId).closest('.cms-menu').css('display', 'none');
      sortable.cmsOver = false;
   }
   
   
   
   var startDragFromNormalContainer = cms.move.startDragFromNormalContainer = function(sortable) {
      sortable.cmsHoverList += ', #' + sortable.cmsStartContainerId;
      cms.util.fixZIndex(sortable.cmsStartContainerId, cms.move.zIndexMap);
      // show drop zone for new favorites
      var list_item = '<li class="cms-item"  rel="' +
      sortable.cmsResource_id +
      '"><div class=" ui-widget-content"><div class="cms-head ui-state-hover"><div class="cms-navtext"><a class="left ui-icon ui-icon-triangle-1-e"></a>' +
      sortable.cmsItem.navText +
      '</div><span class="cms-title">' +
      sortable.cmsItem.title +
      '</span><span class="cms-file-icon"></span><a class="cms-handle cms-move"></a></div><div class="cms-additional"><div alt="File: ' +
      sortable.cmsItem.file +
      '"><span class="left">File:</span>' +
      sortable.cmsItem.file +
      '</div><div alt="Date: ' +
      sortable.cmsItem.date +
      '"><span class="left">Date:</span>' +
      sortable.cmsItem.date +
      '</div><div alt="User: ' +
      sortable.cmsItem.user +
      '"><span class="left">User:</span>' +
      sortable.cmsItem.user +
      '</div><div alt="Type: ' +
      sortable.cmsItem.type +
      '"><span class="left">Type:</span>' +
      sortable.cmsItem.type +
      '</div></div></div></li>';
      sortable.cmsHelpers[cms.html.favoriteListId] = $(list_item).appendTo('#' + cms.html.favoriteListId).css({
         'display': 'none',
         'position': 'absolute',
         'zIndex': sortable.options.zIndex
      }).addClass('ui-sortable-helper');
      $('#' + cms.html.favoriteMenuId).css('visibility', 'visible');
   }
   
   
   var startAdd = cms.move.startAdd = function(event, ui) {
      
      ui.self.cmsStartContainerId = ui.self.currentItem.parent().attr('id');
      // if (ui.self.cmsStartContainerId!=cms.html.favoriteListId){
      // $('#'+cms.html.favoriteMenuId).css('display', 'block');
      // ui.self._refreshItems(event);
      // }
      ui.self.cmsHoverList = '';
      ui.self.cmsCurrentContainerId = ui.self.cmsStartContainerId;
      ui.self.cmsResource_id = ui.self.currentItem.attr('rel');
      
      
      if (ui.self.cmsStartContainerId == cms.html.newListId) {
         var typeElem = cms.data.elements[ui.self.cmsResource_id];
         if (typeElem) {
            var newItem = ui.self.cmsItem = cms.util.createInstanceForNewItem(typeElem.id);
            ui.self.cmsResource_id = newItem.id;
         }
      } else {
         ui.self.cmsItem = cms.data.elements[ui.self.cmsResource_id];
      }
      if (!(ui.self.cmsResource_id && ui.self.cmsItem)) {
         $(cms.util.getContainerSelector()).sortable('cancel');
         return;
      }
      
      ui.self.cmsStartOffset = {
         top: ui.self.offset.top,
         left: ui.self.offset.left
      };
      
      
      
      ui.self.cmsHelpers = {};
      ui.self.cmsOrgPlaceholder = ui.placeholder.clone().insertBefore(ui.placeholder);
      //        ui.self.cmsOrgPlaceholder = $('<div />').insertBefore(
      //				ui.placeholder);
      ui.self.cmsOrgPlaceholder.css({
         'background-color': 'gray',
         'display': 'none',
         'height': ui.self.currentItem.height()
      });
      
      cms.move.zIndexMap = {};
      for (container_name in cms.data.containers) {
         initContainerForDrag(ui.self, cms.data.containers[container_name]);
      }
      if (isMenuContainer(ui.self.cmsStartContainerId)) {
         startDragFromMenu(ui.self);
      } else {
         startDragFromNormalContainer(ui.self);
      }
      
      var placeholderSize = {
         height: ui.helper.height(),
         width: ui.helper.width()
      }
      ui.self.placeholder.addClass(ui.self.currentItem.attr('class'));
      // placeholder should not have class cms-subcontainer
      if (ui.placeholder.hasClass('cms-subcontainer')) {
         ui.placeholder.removeClass('cms-subcontainer');
         var dim = cms.util.getInnerDimensions(ui.helper, 1);
         placeholderSize = {
            height: dim.height,
            width: dim.width
         }
         
      }
      ui.self.cmsOrgPlaceholder.addClass(ui.self.placeholder.attr('class'));
      ui.self.placeholder.css({
         'background-color': 'blue',
         'border': 'solid 2px black',
         'height': placeholderSize.height,
         'width': (/left|right/).test(ui.placeholder.css('float')) ? placeholderSize.width : ''
      });
      
      refreshHelperPositions(ui.self);
      
      $(ui.self.cmsHoverList).css('position', 'relative').each(function() {
         hoverInner($(this), 2, true);
      });
      
      
   }
   
   var beforeStopFunction = cms.move.beforeStopFunction = function(event, ui) {
      if (!ui.self.cmsOver) 
         cancel = true;
      else 
         cancel = false;
   }
   
   
   var updateContainer = cms.move.updateContainer = function(id) {
      if (isMenuContainer(id)) 
         return;
      var newContents = [];
      $('#' + id + ' > *:visible').each(function() {
         newContents.push($(this).attr("rel"));
      });
      
      cms.data.containers[id].elements = newContents;
   }
   
   var stopAdd = cms.move.stopAdd = function(event, ui) {
      cms.util.fixZIndex(null, cms.move.zIndexMap);
      var helpers = ui.self.cmsHelpers;
      var orgPlaceholder = ui.self.cmsOrgPlaceholder;
      var startContainer = ui.self.cmsStartContainerId;
      var endContainer = ui.self.cmsCurrentContainerId;
      var currentItem = ui.self.currentItem;
      if (cancel) {
         cancel = false;
         
         if (isMenuContainer(ui.self.cmsStartContainerId)) {
            // show favorite list again after dragging a favorite from it.
            $('#' + cms.toolbar.currentMenu).css('display', 'block');
         }
         
         $(this).sortable('cancel');
         orgPlaceholder.remove();
      } else {
         if (isMenuContainer(startContainer)) {
            orgPlaceholder.replaceWith(helpers[startContainer]);
            helpers[startContainer].removeClass('ui-sortable-helper');
            cms.util.clearAttributes(helpers[startContainer].get(0).style, ['width', 'height', 'top', 'left', 'position', 'opacity', 'zIndex', 'display']);
            $('div.cms-handle', currentItem).remove();
            $('button.ui-state-active').trigger('click');
            
            // add item to endContainer
         } else {
            if (endContainer == cms.html.favoriteListId) {
               cms.util.addUnique(cms.toolbar.favorites, ui.self.cmsResource_id);
               cms.data.persistFavorites(function(ok) {
                  if (!ok) {
                      // TODO
                  }
               });
               
            }
            orgPlaceholder.remove();
            // add item to endContainer
         }
         if (endContainer != cms.html.favoriteListId && startContainer != cms.html.newListId) {
            cms.toolbar.addToRecent(ui.self.cmsResource_id);
         }
      }
      for (var container_name in helpers) {
         if (container_name != endContainer &&
         !(startContainer == container_name && isMenuContainer(container_name))) {
            var helper = helpers[container_name];
            if (container_name == startContainer &&
            endContainer == cms.html.favoriteListId) {
               var helperStyle = helper.get(0).style;
               helper.removeClass('ui-sortable-helper cms-helper-border cms-helper-background');
               // reset position (?) of helper that was dragged to favorites,
               // but don't remove it
               cms.util.clearAttributes(helperStyle, ['width', 'height', 'top', 'left', 'opacity', 'zIndex', 'display']);
               
               // reset handles
               var handleDiv = $('div.cms-handle', helper);
               handleDiv.hover(function() {
                  cms.move.hoverIn(helper, 2);
                  cms.toolbar.startHoverTimeout(handleDiv, cms.toolbar.timer.adeMode);
               }, function() {
                  cms.toolbar.stopHover();
               });
               if ('move' != cms.toolbar.timer.adeMode) {
                  handleDiv.children('.cms-move').hide();
                  handleDiv.children('.cms-' + cms.toolbar.timer.adeMode).show();
               }
               
               helperStyle.position = 'relative';
               if ($.browser.msie) {
                  helperStyle.removeAttribute('filter');
               }
            } else {
               // remove helper
               helper.remove();
               
            }
         }
      }
      
      //$(ui.self.cmsHoverList).removeClass('show-sortable');
      
      hoverOut();
      
      cms.util.clearAttributes(currentItem.get(0).style, ['top', 'left', 'zIndex', 'display']);
      currentItem.removeClass('cms-helper-border cms-helper-background');
      // opacity is no longer used
      //      if ($.browser.msie) {
      //         currentItem.get(0).style.removeAttribute('filter');
      //         
      //         // ui.self.currentItem.get(0).style.removeAttribute('position');
      //      
      //      } else if (currentItem) {
      //      
      //         // ui.self.currentItem.get(0).style.position='';
      //         currentItem.get(0).style.opacity = '';
      //      }
      updateContainer(startContainer);
      updateContainer(endContainer);
      if (endContainer != cms.html.favoriteListId) 
         cms.toolbar.setPageChanged(true);
   }
   
   
   /**
    * sertzsrthzs
    *
    * @param {Event}
    *            event fff
    * @param {}
    *            ui
    */
   var overAdd = cms.move.overAdd = function(event, ui) {
   
      var elem = event.target ? event.target : event.srcElement;
      var elemId = $(elem).attr('id');
      var reDoHover = !ui.self.cmsOver;
      if (ui.self.cmsStartContainerId != elemId &&
      ui.self.cmsStartContainerId != cms.html.favoriteListId &&
      ui.self.cmsStartContainerId != cms.html.recentListId) {
         // show pacelholder in start container if dragging over a different
         // container, but not from favorites or recent
         ui.self.cmsOrgPlaceholder.css({
            'display': 'block',
            'border': 'dotted 2px black'
         });
      } else {
         // hide placeholder (otherwise both the gray and blue boxes would be
         // shown)
         ui.self.cmsOrgPlaceholder.css('display', 'none');
      }
      if (ui.self.cmsHelpers[elemId]) {
         cms.util.fixZIndex(elemId, cms.move.zIndexMap);
         ui.placeholder.css('display', 'block');
         ui.self.cmsOver = true;
         if (elemId != ui.self.cmsCurrentContainerId) {
         
            ui.self.cmsCurrentContainerId = elemId;
            
            reDoHover = true;
            // hide dragged helper, display helper for container instead
            setHelper(ui.self, elemId);
            ui.self.helper.width(ui.placeholder.width());
            ui.self.helper.height('');
            
         }
         
         // in case of a subcontainer use inner height for placeholder and set a margin
         var helperHeight = ui.self.helper.height();
         if (ui.self.helper.hasClass('cms-subcontainer')) {
            var dimensions = cms.util.getInnerDimensions(ui.self.helper, 10);
            ui.placeholder.height(dimensions.height).css({
               'margin-bottom': helperHeight - dimensions.height
            });
         } else {
            ui.placeholder.height(helperHeight);
         }
         
      } else {
         ui.placeholder.css('display', 'none');
         ui.self.cmsOver = false;
      }
      if (elemId == cms.html.favoriteListId &&
      ui.placeholder.parent().attr('id') != elemId) 
         ui.placeholder.appendTo(elem);
      
      if (reDoHover) {
         hoverOut();
         $(ui.self.cmsHoverList).each(function() {
            hoverInner($(this), 2, true);
         });
      }
      
   }
   
   var outAdd = cms.move.outAdd = function(event, ui) {
      var elem = event.target ? event.target : event.srcElement;
      var elemId = $(elem).attr('id');
      if (ui.self.helper && elemId == ui.self.cmsCurrentContainerId) {
         if (ui.self.cmsStartContainerId != ui.self.cmsCurrentContainerId) {
            ui.self.cmsCurrentContainerId = ui.self.cmsStartContainerId;
            cms.util.fixZIndex(ui.self.cmsStartContainerId, cms.move.zIndexMap);
            setHelper(ui.self, ui.self.cmsCurrentContainerId);
         }
         ui.placeholder.css('display', 'none');
         if (ui.self.cmsStartContainerId != cms.html.favoriteListId) {
            ui.self.cmsOrgPlaceholder.css({
               'display': 'block',
               'border': 'solid 2px black'
            });
         }
         ui.self.cmsOver = false;
         hoverOut();
         $(ui.self.cmsHoverList).each(function() {
            hoverInner($(this), 2, true);
         });
      }
      
   }
   
   var hoverIn = cms.move.hoverIn = function(elem, hOff) {
   
      var tHeight = elem.outerHeight();
      var tWidth = elem.outerWidth();
      var hWidth = 2;
      var lrHeight = tHeight + 2 * (hOff + hWidth);
      var btWidth = tWidth + 2 * (hOff + hWidth);
      
      
      if (elem.css('position') == 'relative') {
         // if position relative highlighting div's are appended to the element itself
         var tlrTop = -(hOff + hWidth);
         var tblLeft = -(hOff + hWidth);
         // top
         $('<div class="cms-hovering cms-hovering-top"></div>').height(hWidth).width(btWidth).css('top', tlrTop).css('left', tblLeft).appendTo(elem);
         
         // right
         $('<div class="cms-hovering cms-hovering-right"></div>').height(lrHeight).width(hWidth).css('top', tlrTop).css('left', tWidth + hOff).appendTo(elem);
         // left
         $('<div class="cms-hovering cms-hovering-left"></div>').height(lrHeight).width(hWidth).css('top', tlrTop).css('left', tblLeft).appendTo(elem);
         // bottom
         $('<div class="cms-hovering cms-hovering-bottom"></div>').height(hWidth).width(btWidth).css('top', tHeight + hOff).css('left', tblLeft).appendTo(elem);
      } else {
         // if position not relative highlighting div's are appended to the body element
         var position = cms.util.getElementPosition(elem);
         var tlrTop = position.top - (hOff + hWidth);
         var tblLeft = position.left - (hOff + hWidth);
         // top
         $('<div class="cms-hovering cms-hovering-top"></div>').height(hWidth).width(btWidth).css('top', tlrTop).css('left', tblLeft).appendTo(document.body);
         
         // right
         $('<div class="cms-hovering cms-hovering-right"></div>').height(lrHeight).width(hWidth).css('top', tlrTop).css('left', position.left + tWidth + hOff).appendTo(document.body);
         // left
         $('<div class="cms-hovering cms-hovering-left"></div>').height(lrHeight).width(hWidth).css('top', tlrTop).css('left', tblLeft).appendTo(document.body);
         // bottom
         $('<div class="cms-hovering cms-hovering-bottom"></div>').height(hWidth).width(btWidth).css('top', position.top + tHeight + hOff).css('left', tblLeft).appendTo(document.body);
      }
   }
   
   var hoverInner = cms.move.hoverInner = function(elem, hOff, showBackground) {
   
      var dimension = cms.util.getInnerDimensions(elem, 25);
      var elemPos = cms.util.getElementPosition(elem);
      var hWidth = 2;
      var inner = {
         top: dimension.top - (elemPos.top + hOff),
         left: dimension.left - (elemPos.left + hOff),
         height: dimension.height + 2 * hOff,
         width: dimension.width + 2 * hOff
      };
      if (showBackground) {
         // inner
         
         $('<div class="cms-highlight-container" style="position: absolute; z-index:0; top: ' +
         inner.top +
         'px; left: ' +
         inner.left +
         'px; height: ' +
         inner.height +
         'px; width: ' +
         inner.width +
         'px;"></div>').prependTo(elem);
      }
      
      if (elem.css('position') == 'relative') {
         // top
         $('<div class="cms-hovering cms-hovering-top"></div>').height(hWidth).width(inner.width + 2 * hWidth).css('top', inner.top - hWidth).css('left', inner.left - hWidth).appendTo(elem);
         // right
         $('<div class="cms-hovering cms-hovering-right"></div>').height(inner.height + 2 * hWidth).width(hWidth).css('top', inner.top - hWidth).css('left', inner.left + inner.width + hOff).appendTo(elem);
         // left
         $('<div class="cms-hovering cms-hovering-left"></div>').height(inner.height + 2 * hWidth).width(hWidth).css('top', inner.top - hWidth).css('left', inner.left - hWidth).appendTo(elem);
         // bottom
         $('<div class="cms-hovering cms-hovering-bottom"></div>').height(hWidth).width(inner.width + 2 * hWidth).css('top', inner.top + inner.height).css('left', inner.left - hWidth).appendTo(elem);
      } else {
         // top
         $('<div class="cms-hovering cms-hovering-top"></div>').height(hWidth).width(dimension.width + 2 * (hOff + hWidth)).css('top', dimension.top - (hOff + hWidth)).css('left', dimension.left - (hOff + hWidth)).appendTo(document.body);
         // right
         $('<div class="cms-hovering cms-hovering-right"></div>').height(dimension.height + 2 * (hOff + hWidth)).width(hWidth).css('top', dimension.top - (hOff + hWidth)).css('left', dimension.left + dimension.width + hOff).appendTo(document.body);
         // left
         $('<div class="cms-hovering cms-hovering-left"></div>').height(dimension.height + 2 * (hOff + hWidth)).width(hWidth).css('top', dimension.top - (hOff + hWidth)).css('left', dimension.left - (hOff + hWidth)).appendTo(document.body);
         // bottom
         $('<div class="cms-hovering cms-hovering-bottom"></div>').height(hWidth).width(dimension.width + 2 * (hOff + hWidth)).css('top', dimension.top + dimension.height + hOff).css('left', dimension.left - (hOff + hWidth)).appendTo(document.body);
      }
   }
   var hoverOut = cms.move.hoverOut = function(context) {
      if (context) {
         $('div.cms-hovering, div.cms-highlight-container', context).remove();
      } else {
         $('div.cms-hovering, div.cms-highlight-container').remove();
      }
   };
   var setHelper = cms.move.setHelper = function(sortable, id) {
      sortable.helper.css('display', 'none');
      sortable.helper = sortable.cmsHelpers[id].css('display', 'block');
      sortable.currentItem = sortable.cmsHelpers[id];
      sortable.placeholder.attr('class', sortable.currentItem.attr('class') + ' cms-placeholder').removeClass('ui-sortable-helper');
      refreshHelperPositions(sortable);
   };
   
   var setHelperObj = cms.move.setHelperObj = function(sortable, helper) {
      sortable.helper.css('display', 'none');
      sortable.helper = helper;
      sortable.currentItem = helper;
      refreshHelperPositions(sortable);
   }
   
   var refreshHelperPositions = cms.move.refreshHelperPositions = function(sortable) {
      sortable._cacheHelperProportions();
      sortable._adjustOffsetFromHelper(sortable.options.cursorAt);
      sortable.refreshPositions(true);
   };
   
   
})(cms);
