(function(cms) {
   var $ = jQuery;
   var M = cms.messages;
   
   /** current toolbar-mode ('Move', 'Edit', 'Delete' etc.) */
   var /** string */ mode = cms.toolbar.mode = '';
   
   /** hashmap for jquery-dom-objects */
   var /** map */ dom = cms.toolbar.dom = {};
   
   /** jquery-document.body-object */
   var /** jquery */ _bodyEl;
   
   /** original body offset */
   var /** integer */ _oldBodyMargin = 0;
   
   /** favorite-list ids */
   /** array */
   cms.toolbar.favorites = [];
   
   /** recent-list ids */
   /** array */
   cms.toolbar.recent = [];
   
   /** max-size of recent-list */
   /** integer */
   cms.toolbar.recentSize = 10;
   
   /** flag to indicate all necessary data has been loaded */
   var /** boolean */ toolbarReady = cms.toolbar.toolbarReady = false;
   
   /** flag to indicate the user is leaving the page */
   var /** boolean */ leavingPage = cms.toolbar.leavingPage = false;
   
   /** id of current menu */
   /** string */
   cms.toolbar.currentMenu = cms.html.favoriteMenuId;
   
   /** id of current menu item list */
   /** string */
   cms.toolbar.currentMenuItems = cms.html.favoriteListId;
   
   /**
    * This function will check if the given mode-string is one of the three editing modes (edit, move, delete).<p>
    *
    * @param {String} mode
    * @return {boolean}
    */
   var _isEditingMode = function(/** String */mode) {
      return (modeMap[mode].isEdit);
   }
   
   
   /**
    * Dummy function which does nothing.
    */
   var doNothing = function() {
      }
   
   /**
    * This function will display the publish dialog.<p>
    */
   var showPublishList = cms.toolbar.showPublishList = /** void */ function() {
      var button = $(this);
      if (button.hasClass('ui-state-active')) {
      
      
         markAsInactive(button);
      } else {
         $('button.ui-state-active').trigger('click');
         // appending publish-dialog content
         $(document.body).append(cms.html.publishDialog);
         var buttons = {};
         buttons[M.PUBLISH_DIALOG_OK] = function() {
            $(this).dialog('close');
         }
         buttons[M.PUBLISH_DIALOG_CANCEL] = function() {
            $(this).dialog('close');
         }
         $('#' + cms.html.publishDialogId).dialog({
            buttons: buttons,
            width: 340,
            title: M.PUBLISH_DIALOG_TITLE,
            modal: true,
            autoOpen: true,
            draggable: true,
            resizable: false,
            position: ['center', 20],
            close: function() {
               markAsInactive($('button[name="Publish"]'));
               $('#' + cms.html.publishDialogId).dialog('destroy');
            },
            zIndex: 10000
         });
         $('#' + cms.html.publishDialogId + ' span.cms-check-icon').click(function() {
            $(this).toggleClass('cms-check-icon-inactive')
         });
         
         markAsActive(button);
      }
   };
   
   /**
    * Marks a button as active
    * @param {Object} $button
    */
   var markAsActive = function($button) {
      $button.addClass('ui-state-active');
   }
   
   /**
    * Marks a button as inactive.
    * @param {Object} $button
    */
   var markAsInactive = function($button) {
      $button.removeClass('ui-state-active');
   }
   
   /**
    * This event-handler function will remove an element from a container.
    */
   var deleteItem = cms.toolbar.deleteItem = /** void */ function() {
      var $item = $(this).closest('.cms-element');
      var $container = $item.parent();
      cms.move.hoverOut();
      var elemId = $item.attr('rel');
      if (elemId && cms.data.elements[elemId]) {
      
         // a resource that has been edited and saved should not be removed from the vfs without comment/warning.
         
         //         if (cms.data.elements[elemId].status == cms.data.STATUS_CREATED) {
         //            cms.data.deleteResources([elemId], function(ok) {
         //               deleteFromFavListAndRecList([elemId]);
         //               if (!ok) {
         //                  // TODO
         //                  alert("error");
         //               }
         //            });
         //         } else {
         //            addToRecent($item.attr('rel'));
         //         }
         
         addToRecent($item.attr('rel'));
      }
      $item.remove();
      cms.move.updateContainer($container.attr('id'));
      setPageChanged(true);
   };
   
   /**
    * This event-handler function will delete an element from the vfs. It is to be assigned to delete buttons in content-collector lists only.
    * It matches the functionallity of the 'old' direct edit.
    */
   var directDeleteItem = cms.toolbar.directDeleteItem = /** void */ function() {
      var elemId = $(this).closest('.cms-editable').attr('rel');
      var buttons = {};
      buttons[M.DIRECT_DELETE_OK] = function() {
         $(this).dialog('close');
         cms.data.deleteResources([elemId], function(ok) {
            var elemList = [elemId];
            deleteFromFavListAndRecList(elemList);
            $(cms.util.getContainerSelector()).find('.cms-element[rel="' + elemId + '"]').remove();
            for (var key in cms.data.containers) {
               cms.move.updateContainer(key);
            }
            $(cms.util.getContainerSelector()).find('.cms-element:has(div.cms-editable)').each(function() {
               elemList.push($(this).attr('rel'));
            });
            cms.data.loadElements(elemList, function(ok) {
               if (ok) {
                  cms.data.fillContainers();
                  // to reset the mode we turn it off and on again
                  var activeButton = $("#toolbar button.ui-state-active");
                  activeButton.trigger('click');
                  activeButton.trigger('click');
               } else {
                              // TODO
               }
            });
         });
      }
      
      buttons[N.DIRECT_DELETE_CANCEL] = function() {
         $(this).dialog('close');
      }
      $('<div id="cms-delete-dialog" style="display:none;" title="' + M.DIRECT_DELETE_TITLE + '">\
          <p>\
            ' +
      M.DIRECT_DELETE_CONFIRM +
      '\
          </p>\
        </div>').appendTo("body").dialog({
         autoOpen: true,
         buttons: buttons,
         resizable: false,
         modal: true,
         zIndex: 10000
      
      });
   }
   
   /**
    * Deletes the given elements from the favorites and recent list.<p>
    *
    * @param {Array} ids a list of ids of elements to be deleted
    */
   var deleteFromFavListAndRecList = cms.toolbar.deleteFromFavListAndRecList = /** void */ function(/**Array<String>*/ids) {
   
      var /**boolean*/ saveFavorites = false;
      var /**boolean*/ saveRecentList = false;
      $.each(ids, function() {
         // HACK: javascript converts the list elements from native string to String objects :(
         var /**string*/ id = "" + this;
         // remove from favorites
         var /**Number*/ pos = $.inArray(id, cms.toolbar.favorites);
         if (pos >= 0) {
            saveFavorites = true;
            cms.toolbar.favorites.splice(pos, 1);
         }
         // remove from recent list
         pos = $.inArray(id, cms.toolbar.recent);
         if (pos >= 0) {
            saveRecentList = true;
            cms.toolbar.recent.splice(pos, 1);
         }
      });
      if (saveFavorites) {
         // save favorites
         cms.data.persistFavorites(function(ok) {
            resetFavList();
            if (!ok) {
                        // TODO
            }
         });
      }
      if (saveRecentList) {
         // save recent list
         cms.data.persistRecent(function(ok) {
            resetRecentList();
            if (!ok) {
                        // TODO
            }
         });
      }
   };
   
   /**
    * Timer object used for delayed hover effect.
    */
   var timer = cms.toolbar.timer = {
      id: null,
      handleDiv: null,
      adeMode: null
   };
   
   /**
    * Starts a hover timeout.<p>
    *
    * @param {Object} handleDiv
    * @param {Object} adeMode
    */
   var startHoverTimeout = cms.toolbar.startHoverTimeout = /** void */ function(/** jquery-object */handleDiv, /** string */ adeMode) {
      if (timer.id) {
         clearTimeout(timer.id);
      }
      timer.id = setTimeout(cms.toolbar.showAddButtons, 1000);
      timer.handleDiv = handleDiv;
      timer.adeMode = adeMode;
   }
   
   /**
    * Shows additional editing buttons within hover effect.
    */
   var showAddButtons = cms.toolbar.showAddButtons = /** void */ function() {
      timer.id = null;
      var numButtons = $(timer.handleDiv).children().size();
      
      var right = (1 - numButtons) * 24 + 'px';
      var width = numButtons * 24 + 'px';
      
      timer.handleDiv.addClass('ui-widget-header').css({
         'width': width,
         'right': right
      }).children().css('display', 'block').addClass('ui-corner-all ui-state-default');
      
      if ($.browser.msie || (timer.handleDiv.offset().left + timer.handleDiv.width() > $(window).width())) {
         // always show the additional handles within the element for IE to avoid z-index problems.
         timer.handleDiv.addClass('cms-handle-reverse').css('right', '0px');
      } else {
         timer.handleDiv.removeClass('cms-handle-reverse');
      }
   }
   
   /**
    * Cancels current hover timeout.<p>
    */
   var stopHover = cms.toolbar.stopHover = /** void */ function() {
      if (timer.id) {
         clearTimeout(timer.id);
         timer.id = null;
      }
      
      
      // sometimes out is triggered without over being triggered before, especially in IE
      if (!timer.handleDiv) {
         return;
      }
      timer.handleDiv.removeClass('ui-widget-header').css({
         'width': '24px',
         'right': '0px'
      }).children().removeClass('ui-corner-all ui-state-default').not('a.cms-' + timer.adeMode).css('display', 'none');
   }
   
   
   /**
    * Initializes the hover event handler for a handle div.
    *
    * @param {Object} handleDiv the handle div
    * @param {Object} elem the element which the handle div belongs to
    * @param {String} adeMode the current mode
    */
   var initHandleDiv = cms.toolbar.initHandleDiv = /** void */ function(/** jquery-object */handleDiv, /** jquery-object */ elem, /** String */ adeMode) {
      handleDiv.hover(function() {
         cms.move.removeBorder(elem, '.' + cms.move.HOVER_NEW);
         cms.move.hoverIn(elem, 2);
         startHoverTimeout(handleDiv, cms.toolbar.mode);
         $('body').children('.' + cms.move.HOVER_NEW).remove();
      }, function() {
         cms.move.hoverOut();
         stopHover();
         if ($(elem).find('.' + cms.move.HOVER_NEW).size() == 0 && $(elem).hasClass('cms-new-element')) {
            cms.move.drawBorder(elem, 2, cms.move.HOVER_NEW);
         }
         $('body').children('.' + cms.move.HOVER_NEW).remove();
      });
      $('body').children('.' + cms.move.HOVER_NEW).remove();
   }
   
   /**
    * Event handler for the 'edit properties' button.
    */
   var openPropertyDialog = function() {
      var $elem = $(this).closest('.cms-element');
      cms.property.editProperties($elem);
   }
   
   /**
    * Adds handle div to element.<p>
    *
    * @param {Object} elem jquery-element-object
    * @param {Object} elemId the resource-id
    * @param {Object} adeMode current mode
    * @param {Object} isMoving indicates if the current element is a sortable helper object
    */
   var addHandles = cms.toolbar.addHandles = /** void */ function(/** jquery-object */elem, /** String */ elemId, /** String */ adeMode, /** boolean */ isMoving) {
      var handleDiv = $('<div class="cms-handle"></div>').appendTo(elem);
      
      var handles = {}
      for (var modeName in modeMap) {
         var modeObj = modeMap[modeName];
         if (modeObj.isEdit) {
            handles[modeName] = modeMap[modeName].createHandle(elemId, elem);
         }
      }
      var currentMode = modeMap[adeMode];
      
      
      if (currentMode.isCompatibleWith(elemId)) {
         handles[adeMode].appendTo(handleDiv);
      }
      for (handleName in handles) {
         handleMode = modeMap[handleName];
         if (handleName != adeMode && handleMode.isCompatibleWith(elemId)) {
            handles[handleName].appendTo(handleDiv).css('display', 'none');
         }
      }
      if (isMoving) {
         if (adeMode != 'move') {
            handles[adeMode].css('display', 'none');
            handles['move'].css('display', 'block');
         }
      } else {
         initHandleDiv(handleDiv, elem, adeMode);
      }
      handleDiv.css({
         'right': '0px',
         'width': '24px'
      });
   }
   
   
   /**
    * Click-event-handler for edit-handles.<p>
    * Opens the content-editor-dialog.<p>
    */
   var openEditDialog = cms.toolbar.openEditDialog = /** void */ function() {
      var $domElement = $(this).closest('.cms-element');
      var elemId = $domElement.attr('rel');
      
      if (elemId && cms.data.elements[elemId]) {
         if (cms.data.elements[elemId].allowEdit) {
            var element = cms.data.elements[elemId];
            
            if (element.status == cms.data.STATUS_NEWCONFIG) {
               cms.data.createResource(element.type, function(ok, id, uri) {
                  if (!ok) {
                     // TODO
                     return;
                  }
                  cms.move.removeBorder($domElement, '.' + cms.move.HOVER_NEW);
                  var elem = cms.data.elements[elemId];
                  delete cms.data.elements[elemId];
                  cms.data.elements[id] = elem;
                  elem.id = id;
                  elem.status = cms.data.STATUS_CREATED;
                  cms.util.replaceNewElement(elemId, id);
                  _openContentEditor(uri, id);
               });
               
            } else {
               _openContentEditor(element.file, elemId);
               
            }
         }
      }
   }
   
   /**
    * Opens the content editor for elements loaded within an element by a content-collector. 'Old' direct edit function.
    */
   var openSubelementEditDialog = cms.toolbar.openSubelementDialog = /** boolean */ function() {
      var editable = $(this).closest('.cms-editable');
      var elemId = editable.attr('rel');
      var parentId = $(this).closest('.cms-element').attr('rel');
      var path = editable.find('input[name="resource"]').val();
      _openContentEditor(path, [elemId, parentId]);
      return false;
   }
   
   /**
    * Opens the content editor for new elements loaded within an element by a content-collector. 'Old' direct edit function.
    */
   var openEditNewDialog = cms.toolbar.openEditNewDialog = /** boolean */ function() {
      var editable = $(this).closest('.cms-editable');
      var elemId = editable.attr('rel');
      var parentId = $(this).closest('.cms-element').attr('rel');
      var path = editable.find('input[name="resource"]').val();
      var newLink = '&amp;newlink=' + escape(editable.find('input[name="newlink"]').val()) + '&amp;editortitle=Editing+%28new+resource%29';
      _openContentEditor(path, [elemId, parentId], newLink);
      return false;
   }
   
   /**
    * Opens the content-editor dialog for a regular container element (if a single id string is given),
    * a content-collector element (if an array of ids is given, editing the element of the first)
    * or for a new content-collector element (if newLink != null)
    *
    * @param {String} path
    * @param {String / Array} ids may be an array or a single element-id
    * @param {String} newLink
    */
   var _openContentEditor = /** void */ function( /** String */path, /** String / Array<String> */ ids, /** String */ newLink) {
      var _afterReload = function(ok) {
         if (ok) {
            cms.data.fillContainers();
            // to reset the mode we turn it off and on again
            var activeButton = $("#toolbar button.ui-state-active");
            activeButton.trigger('click');
            activeButton.trigger('click');
         } else {
                  // TODO
         }
      };
      var id = $.isArray(ids) ? ids[0] : ids;
      var dialogWidth = self.innerWidth ? self.innerWidth : self.document.body.clientWidth;
      dialogWidth = dialogWidth > 1360 ? 1360 : dialogWidth;
      var dialogHeight = self.innerHeight ? self.innerHeight : self.document.body.clientHeight;
      dialogHeight = dialogHeight < 700 ? dialogHeight : 700;
      var iFrameHeight = dialogHeight - 115 // mmoossen: resource name in body: - 126;
      var editorLink = cms.data.EDITOR_URL + '?resource=' + path + '&amp;directedit=true&amp;elementlanguage=' + cms.data.locale + '&amp;backlink=' + cms.data.BACKLINK_URL + '&amp;redirect=true';
      if (newLink) {
         editorLink += newLink;
      }
      var editorFrame = '<iframe style="border:none; width:100%; height:' + iFrameHeight + 'px;" name="cmsAdvancedDirectEditor" src="' + editorLink + '"></iframe>';
      var editorDialog = $('#cms-editor');
      if (!editorDialog.length) {
         editorDialog = $('<div id="cms-editor"  rel="' + id + '"></div>').appendTo(document.body);
      } else {
         editorDialog.empty().attr('rel', id);
      }
      
      // mmoossen: resource name in body: editorDialog.append('<div class="cms-editor-subtitle">Resource: ' + (newLink) ? "new resource" : path + '</div>')
      editorDialog.append(editorFrame);
      editorDialog.dialog({
         width: dialogWidth - 50,
         height: dialogHeight - 60,
         title: cms.util.format(M.EDITOR_TITLE, ((newLink) ? M.EDITOR_NEW_RESOURCE : path)), // mmoossen: resource name in title
         modal: true,
         autoOpen: true,
         closeOnEscape: false,
         draggable: true,
         resizable: true,
         resize: function(event) {
            $('#cms-editor iframe').height($(this).height() - 20);
         },
         resizeStop: function(event) {
            $('#cms-editor iframe').height($(this).height() - 20);
         },
         position: ['center', 0],
         open: function(event) {
            cms.toolbar.dom.appendBox.css({
               zIndex: 10100,
               position: 'fixed'
            }).append(editorDialog.parent());
            $('a.ui-dialog-titlebar-close').hide();
            editorDialog.parent().css('top', '0px')
         },
         close: function() {
            var idsToLoad = [];
            
            editorDialog.empty().dialog('destroy');
            if ($.isArray(ids)) {
               idsToLoad = ids
               
            } else {
               idsToLoad = [id];
            }
            idsToLoad = idsToLoad.concat(cms.property.getElementsWithSamePrefix(id));
            cms.data.loadElements(idsToLoad, _afterReload);
         },
         zIndex: 10000
      });
   }
   
   /**
    * Removes the toolbar-html-element from the dom.<p>
    * Currently not used.<p>
    */
   var removeToolbar = cms.toolbar.removeToolbar = /** void */ function() {
      $('#toolbar').remove();
      $(document.body).css('margin-top', _oldBodyMarginTop + 'px');
   };
   
   /**
    * Hides the toolbar.<p>
    */
   var hideToolbar = cms.toolbar.hideToolbar = /** boolean */ function() {
      $(document.body).animate({
         marginTop: _oldBodyMarginTop + 'px'
      }, 200, 'swing', function() {
         $('#show-button').show(50);
      });
      
      return false;
   };
   
   /**
    * Toggles the toolbar.<p>
    */
   var toggleToolbar = cms.toolbar.toggleToolbar = /** boolean */ function() {
      var button = $('#show-button');
      if (button.hasClass('toolbar_hidden')) {
         $('#toolbar').fadeIn(100);
         $(document.body).animate({
            marginTop: _oldBodyMarginTop + 34 + 'px'
         }, 200, 'swing');
         button.removeClass('toolbar_hidden');
      } else {
         $(document.body).animate({
            marginTop: _oldBodyMarginTop + 'px'
         }, 200, 'swing');
         $('#toolbar').fadeOut(100);
         button.addClass('toolbar_hidden');
      }
      return false;
   };
   
   /**
    * Helper class for displaying a 'Loading' sign when loading takes too long
    */
   var LoadingSign = cms.toolbar.LoadingSign = /** Object */ function(/** String */selector, /** long */ waitTime, /** function */ showLoading, /** function */ hideLoading) {
   
      this.isLoading = false;
      
      var self = this;
      
      this.start = function() {
         self.isLoading = true;
         window.setTimeout(function() {
            if (self.isLoading) {
               showLoading();
            }
         }, waitTime);
      }
      
      this.stop = function() {
         self.isLoading = false;
         hideLoading();
      }
      
      return self;
   }
   
   
   /**
    * Moves the dropdown menus to the right if required for small screen-sizes.<p>
    */
   var fixMenuAlignment = /** void */ function() {
      $('.cms-menu').each(function() {
         var $elem = $(this);
         if ($elem.offset().left < 0) {
            cms.util.setLeft($elem, 0);
         }
      });
   }
   
   
   
   /**
    * Removes the sortable-object and event-handler.<p>
    */
   var destroyMove = function() {
   
      var containerSelector = cms.util.getContainerSelector();
      // replace ids
      $(containerSelector + ', #' + cms.html.favoriteDropListId).sortable('destroy');
      var list = $('#' + cms.html.favoriteDropMenuId);
      $('li.cms-item, button', list).css('display', 'block');
      resetFavList();
      $('.cms-element div.cms-handle').remove();
   };
   
   
   /**
    * Initializes the sortable.<p>
    */
   var initMove = /** void */ function() {
      var containerSelector = cms.util.getContainerSelector();
      
      // replace id
      var list = cms.toolbar.dom.favoritesDrop;
      var favbutton = $('button[name="Favorites"]');
      
      //unnecessary
      $('li.cms-item, button', list).css('display', 'none');
      var favLeft = $('button[name="favorites"]').position().left - 1;
      list.css({
         top: 35,
         left: favLeft,
         display: 'block',
         visibility: 'hidden'
      });
      
      // replace id
      $('#' + cms.html.favoriteDropListId).css('height', '40px');
      $('div.ui-widget-shadow', list).css({
         top: 0,
         left: -4,
         width: list.outerWidth() + 8,
         height: list.outerHeight() + 1,
         border: '0px solid',
         opacity: 0.6
      });
      
      $(containerSelector).css('position', 'relative');
      
      // replace id
      var sortSelector = containerSelector + ', #' + cms.html.favoriteDropListId;
      var $lists = $(sortSelector);
      $lists.sortable({
         connectWith: containerSelector + ', #' + cms.html.favoriteDropListId,
         placeholder: 'cms-placeholder',
         dropOnEmpty: true,
         start: cms.move.onStartDrag,
         beforeStop: cms.move.beforeStopFunction,
         over: cms.move.onDragOverContainer,
         out: cms.move.onDragOutOfContainer,
         change: function(event, ui) {
            cms.move.hoverOut(ui.helper.parent());
            cms.move.hoverInner(ui.helper.parent(), 2, true);
         },
         tolerance: 'pointer',
         //           opacity: 0.7,
         stop: cms.move.onStopDrag,
         cursorAt: {
            right: 10,
            top: 10
         },
         zIndex: 20000,
         handle: 'a.cms-move',
         items: '.cms-element',
         revert: true,
         // replace ids
         deactivate: cms.move.onDeactivateDrag
      });
   }
   
   
   
   /**
    * Click-event-handler for delete icon in favorites dialog.<p>
    */
   var clickFavDeleteIcon = cms.toolbar.clickFavDeleteIcon = /** void */ function() {
      var button = $(this);
      var toRemove = button.parent().parent();
      toRemove.remove();
   };
   
   /**
    * Utility function to generate a string representing an array.<p>
    * @param {Array} arr
    */
   var arrayToString = function(/** Array<String> */arr) {
      return "[" + arr.join(", ") + "]";
   };
   
   /**
    * Adds another item to the favorite-list.<p>
    */
   var saveFavorites = cms.toolbar.saveFavorites = function() {
      cms.toolbar.favorites.length = 0;
      $("#fav-dialog > ul > li.cms-item").each(function() {
         var resource_id = this.getAttribute("rel");
         cms.toolbar.favorites.push(resource_id);
      });
   };
   
   /**
    * Persists favorite-list on server.<p>
    */
   var favEditOK = cms.toolbar.favEditOK = function() {
      $(this).dialog("close");
      saveFavorites();
      cms.data.persistFavorites(function(ok) {
         if (!ok) {
                  // TODO
         }
      });
   }
   
   /**
    * Closes favorites edit dialog.<p>
    */
   var favEditCancel = cms.toolbar.favEditCancel = function() {
      $(this).dialog("close");
   }
   
   /**
    * Initializes the favorites edit dialog.<p>
    */
   var initFavDialog = cms.toolbar.initFavDialog = function() {
      $("#fav-edit").click(showFavDialog);
      var buttons = {};
      buttons[M.FAV_EDIT_OK] = favEditOK;
      buttons[M.FAV_EDIT_CANCEL] = favEditCancel;
      $('#fav-dialog').dialog({
         width: 380,
         // height: 500,
         title: M.FAV_EDIT_TITLE,
         modal: true,
         autoOpen: false,
         draggable: true,
         resizable: true,
         position: ['center', 20],
         close: function() {
            markAsInactive($('#fav-edit'));
         },
         buttons: buttons,
         zIndex: 10000
      });
      
   };
   
   /**
    * Initializes the favorites edit dialog items.<p>
    */
   var initFavDialogItems = cms.toolbar.initFavDialogItems = function() {
      $("#fav-dialog > ul").empty();
      var html = []
      for (var i = 0; i < cms.toolbar.favorites.length; i++) {
         html.push(cms.html.createItemFavDialogHtml(cms.data.elements[cms.toolbar.favorites[i]]));
      }
      $("#fav-dialog > ul").append(html.join(''));
      $("#fav-dialog .cms-delete-icon").click(clickFavDeleteIcon);
      $("#fav-dialog > ul").sortable({
         axis: 'y',
         forcePlaceholderSize: true
      });
   };
   
   /**
    * Opens the favorites edit dialog.<p>
    */
   var showFavDialog = cms.toolbar.showFavDialog = function() {
      var button = $(this);
      if (button.hasClass("ui-state-active")) {
         markAsInactive(button);
      } else {
         $('button.ui-state-active').trigger('click');
         // enabling move-mode
         initFavDialogItems();
         $('#fav-dialog').dialog('open');
         markAsActive(button);
      }
   };
   
   /**
    * Shows/hides the additional item info in list-views.<p>
    */
   var toggleAdditionalInfo = cms.toolbar.toggleAdditionalInfo = function() {
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
   
   /**
    * Reloads the favorites-list.<p>
    */
   var resetFavList = cms.toolbar.resetFavList = function() {
      $("#" + cms.html.favoriteMenuId + " .cms-item-list > li.cms-item").remove();
      var $favlist = $("#" + cms.html.favoriteMenuId + " .cms-item-list");
      for (var i = 0; i < cms.toolbar.favorites.length; i++) {
         $favlist.append(cms.html.createItemFavListHtml(cms.toolbar.favorites[i]))
      }
   }
   
   
   /**
    * Reloads the new items list.<p>
    */
   var resetNewList = cms.toolbar.resetNewList = function() {
      $('#' + cms.html.newMenuId + " li.cms-item").remove();
      var $newlist = $('#' + cms.html.newMenuId + " ul");
      for (var i = 0; i < cms.data.newTypes.length; i++) {
         $newlist.append(cms.html.createItemFavListHtml(cms.data.newTypes[i].type));
         }
      }
   
   /**
    * Adds item to recent list.<p>
    *
    * @param {String} itemId
    */
   var addToRecent = cms.toolbar.addToRecent = function(/** String */itemId) {
      cms.util.addToElementList(cms.toolbar.recent, itemId, cms.toolbar.recentSize);
      //cms.util.addUnique(cms.toolbar.recent, itemId, cms.toolbar.recentSize);
      cms.data.persistRecent(function(ok) {
         if (!ok) {
                  // TODO
         }
      });
   }
   
   /**
    * Opens the save dialog.
    */
   var showSaveDialog = function() {
      if ($(this).hasClass('cms-deactivated')) {
         return;
      }
      
      if ($('#cms-save-dialog').size() == 0) {
         $('<div id="cms-save-dialog" style="display:none;" title="' + M.SAVE_PAGE_TITLE + '"></div>').appendTo('body');
      }
      $dlg = $('#cms-save-dialog');
      $dlg.empty();
      $dlg.append('<p>' + M.SAVE_PAGE_CONFIRM + '</p>');
      var buttons = {};
      
      buttons[M.SAVE_PAGE_CANCEL] = function() {
         $dlg.dialog('destroy');
         markAsInactive($('button[name="save"]'));
      }
      
      buttons[M.SAVE_PAGE_OK] = function() {
         markAsInactive($('button[name="save"]'));
         $dlg.dialog('destroy');
         savePage();
      };
      
      
      var options = {
         autoOpen: true,
         modal: true,
         zIndex: 10000,
         resizable: false,
         buttons: buttons
      };
      
      $dlg.dialog(options);
   }
   
   /**
    * Persists container-page on server.<p>
    * @param {Object} callback
    */
   var savePage = cms.toolbar.savePage = function(/** function */callback) {
   
      cms.data.persistContainers(function(ok) {
         $('#cms-save-dialog').dialog('close');
         if (ok) {
            setPageChanged(false);
         }
         if (callback) {
            callback(ok);
         }
      });
   }
   
   /**
    * Flag to indicate the status of the page (changed/unchanged).<p>
    */
   var pageChanged = cms.toolbar.pageChanged = false;
   
   /**
    * Sets the pageChanged flag and activates the save and reset-buttons.<p>
    * @param {boolean} newValue
    */
   var setPageChanged = cms.toolbar.setPageChanged = function(/** boolean */newValue) {
      pageChanged = cms.toolbar.pageChanged = newValue;
      if (newValue) {
         $('#toolbar button[name="Save"], #toolbar button[name="reset"]').removeClass('cms-deactivated');
      } else {
         $('#toolbar button[name="Save"], #toolbar button[name="reset"]').addClass('cms-deactivated');
      }
   }
   
   /**
    * On-unload event-handler to prevent accidental data-loss.<p>
    */
   var onUnload = cms.toolbar.onUnload = function() {
      if (cms.toolbar.pageChanged) {
         var saveChanges = window.confirm(cms.util.format(M.UNLOAD_SAVE_CONFIRM, window.location.href));
         if (saveChanges) {
            cms.toolbar.savePage();
         } else {
            var newElems = [];
            $.each(cms.data.elements, function(key, value) {
               if (value.status == cms.data.STATUS_CREATED) {
                  newElems.push(value.id);
               }
            });
            if (newElems.length > 0) {
               cms.data.deleteResources(newElems, function(ok) {
                  deleteFromFavListAndRecList(newElems);
                  if (!ok) {
                     // TODO
                     alert(M.UNLOAD_DELETE_ERROR);
                  }
               });
            }
         }
      }
   }
   
   /**
    * Initializes the link click handlers for links in elements.<p>
    *
    * If the user clicks on a link, the handler will ask the user whether
    * they really want to leave the page, and if they want to save before this.
    *
    */
   var initLinks = cms.toolbar.initLinks = function() {
      $('<div id="cms-leave-dialog" style="display: none;">' + M.LEAVE_PAGE_CONFIRM + '</div>').appendTo('body');
      $('a:not(.cms-left, .cms-move, .cms-delete, .cms-edit, .cms-properties, .cms-advanced-search, .cms-basic-search)').live('click', function() {
         if (!cms.toolbar.pageChanged) {
            cms.toolbar.leavingPage = true;
            return;
         }
         var $link = $(this);
         var target = $link.attr('href');
         var buttons = {};
         
         
         
         buttons[M.LEAVE_PAGE_SAVE] = function() {
            $(this).dialog('destroy');
            savePage(function(ok) {
               if (ok) {
                  window.location.href = target;
               }
            });
         };
         
         buttons[M.LEAVE_PAGE_OK] = function() {
            $(this).dialog('destroy');
            setPageChanged(false);
            window.location.href = target;
         };
         
         buttons[M.LEAVE_PAGE_CANCEL] = function() {
            $(this).dialog('destroy');
         };
         
         $('#cms-leave-dialog').dialog({
            autoOpen: true,
            modal: true,
            title: M.LEAVE_PAGE_TITLE,
            zIndex: 9999,
            buttons: buttons,
            close: function() {
               $(this).dialog('destroy');
            }
         });
         return false;
      });
   }
   
   
   /**
    * Reloads the recent-list.<p>
    */
   var resetRecentList = cms.toolbar.resetRecentList = function() {
      $("#" + cms.html.recentMenuId + " li.cms-item").remove();
      var $recentlist = $("#" + cms.html.recentListId);
      for (var i = 0; i < cms.toolbar.recent.length; i++) {
         $recentlist.append(cms.html.createItemFavListHtml(cms.toolbar.recent[i]));
      }
   };
   
   /**
    * Creates a short menu bar button.
    * @param {Object} name the button name
    * @param {Object} title the button title
    * @param {Object} cssClass the css class to add to the button
    */
   var makeModeButton = function(name, title, cssClass) {
      return $('<button name="' + name + '" title="' + title + '" class="cms-left ui-state-default ui-corner-all"><span class="ui-icon ' + cssClass + '"></span>&nbsp;</button>');
   }
   
   
   /**
    * Creates a wide menu bar button.
    * @param {Object} name the button name
    * @param {Object} title the button title
    * @param {Object} cssClass the css class to add to the button
    */
   var makeWideButton = function(name, title, cssClass) {
      return $('<button name="' + name + '" title="' + title + '" class="cms-left cms-button-wide ui-state-default ui-corner-all"><span class="ui-icon ' + cssClass + '"></span><span class="cms-button-text">' + title + '</span></button>');
   }
   
   /**
    * Shared method that toggles a mode (enables or disables it depending on the state of the mode's button).
    * @param {Object} button the button of the mode
    */
   var toggleMode = cms.toolbar.toggleMode = /** void */ function(modeObject) {
      if (!cms.toolbar.toolbarReady) {
         return;
      }
      if (modeObject.name == cms.toolbar.mode) {
         modeObject.disable();
         cms.toolbar.mode = '';
         doHideShowHackForIE();
         markAsInactive(modeObject.button);
      } else {
         modeObject.enable(modeObject.button);
         cms.toolbar.mode = modeObject.name;
         markAsActive(modeObject.button);
      }
   };
   
   
   /**
    * Shared method that enables an edit mode (edit, move, delete, properties).
    */
   var _enableEditMode = function() {
      var buttonMode = this.name;
      var containers = $(cms.util.getContainerSelector());
      $('button.ui-state-active').trigger('click');
      if (modeMap[cms.toolbar.mode].isEdit) {
         // reorder handles
         $('.cms-element div.cms-handle').each(function() {
            var handleDiv = $(this);
            $('a', handleDiv).css('display', 'none');
            $('a.cms-' + buttonMode, handleDiv).prependTo(handleDiv).css('display', 'block');
         });
         markAsInactive(cms.toolbar.dom.buttons[cms.toolbar.mode]);
         containers.find('.cms-element .cms-editable:not(:has(div.cms-hovering))').each(function() {
            cms.move.drawSiblingBorder($(this), 2, 'cms-editable', false, 'cms-test');
         });
         containers.find('div.cms-editable div.cms-directedit-buttons').removeClass('cms-' + cms.toolbar.mode + 'mode').addClass('cms-' + buttonMode + 'mode');
      } else {
         modeMap[cms.toolbar.mode].disable();
         
         containers.children('.cms-element').each(function() {
            var elem = $(this).css('position', 'relative');
            var elemId = elem.attr('rel');
            if (elemId && cms.data.elements[elemId]) {
               addHandles(elem, elemId, buttonMode);
            }
         });
         initMove();
         containers.find('.cms-element .cms-editable:not(:has(div.cms-hovering))').each(function() {
            cms.move.drawSiblingBorder($(this), 2, 'cms-editable', false, 'cms-test');
         });
         containers.find('div.cms-editable div.cms-directedit-buttons').addClass('cms-' + buttonMode + 'mode');
      }
      
   }
   
   /**
    * Shared method that enables a list mode (favorites, recent, new, add).
    * @param {Object} button the button of the mode
    */
   var _enableListMode = function(button) {
      var self = this;
      var currentMode = getCurrentMode();
      cms.toolbar.currentMenu = self.menuId;
      currentMode.disable();
      $('button.ui-state-active').trigger('click');
      markAsActive(button);
      self.load(function(ok, data) {
         self.prepareAfterLoad();
         list = $('#' + self.menuId);
         $('.cms-head:not(:has(a.cms-move))', list).each(function() {
            var elem = $(this);
            $('<a class="cms-handle cms-move"></a>').appendTo(elem);
         });
         list.css({
            /* position : 'fixed', */
            top: 35,
            left: button.position().left - 1
         }).slideDown(100, function() {
            $('div.ui-widget-shadow', list).css({
               top: 0,
               left: -3,
               width: list.outerWidth() + 8,
               height: list.outerHeight() + 1,
               border: '0px solid',
               opacity: 0.6
            });
         });
         $(cms.util.getContainerSelector()).css('position', 'relative').children('*:visible').css('position', 'relative');
         fixMenuAlignment();
         // * current menu
         $(cms.util.getContainerSelector() + ', #' + self.menuId + ' ul.cms-item-list').sortable({
            // * current menu
            connectWith: cms.util.getContainerSelector() + ', #' + self.menuId + ' ul.cms-item-list',
            placeholder: 'placeholder',
            dropOnEmpty: true,
            start: cms.move.onStartDrag,
            beforeStop: cms.move.beforeStopFunction,
            over: cms.move.onDragOverContainer,
            out: cms.move.onDragOutOfContainer,
            tolerance: 'pointer',
            stop: cms.move.onStopDrag,
            cursorAt: {
               right: 15,
               top: 10
            },
            handle: 'a.cms-move',
            items: cms.data.sortitems + ', li.cms-item',
            revert: 100,
            deactivate: function(event, ui) {
               $('a.cms-move', $(this)).removeClass('cms-trigger');
               if ($.browser.msie) {
                  // In IE7 html-block elements may disappear after sorting, this should trigger the html to be re-rendered.
                  setTimeout(function() {
                     $(cms.data.sortitems).css('display', 'block');
                  }, 10);
               }
            }
         });
      });
      
   }
   
   /**
    * Shared method that disables an edit mode (edit, move, delete, properties).
    */
   var _disableEditMode = /** void */ function() {
      var mode = this.name;
      if (mode == '') {
         return;
      }
      // disabling edit/move/delete
      var containerSelector = cms.util.getContainerSelector();
      // replace ids
      $(containerSelector + ', #' + cms.html.favoriteDropListId).sortable('destroy');
      var list = $('#' + cms.html.favoriteDropMenuId);
      $('li.cms-item, button', list).css('display', 'block');
      resetFavList();
      $('.cms-element div.cms-handle').remove();
      $(containerSelector).find('div.cms-editable div.cms-directedit-buttons').removeClass('cms-' + mode + 'mode');
      //cms.toolbar.dom.buttons[mode].removeClass('ui-state-active');
   }
   
   /**
    * Hides and shows all containers if the browser is IE.<p>
    *
    * This is sometimes necessary because containers vanish for no reason in IE after closing a menu.
    */
   var doHideShowHackForIE = function() {
      // TODO: find a better way to rerender stuff in IE
      if ($.browser.msie) {
         // In IE7 html-block elements may disappear after sorting, this should trigger the html to be re-rendered.
         setTimeout(function() {
            $(cms.util.getContainerSelector()).hide().show();
         }, 0);
      }
   }
   
   /**
    * Shared method for disabling a list mode (favorites, recent, add, new).
    */
   var _disableListMode = /** void */ function() {
      var mode = this.name;
      if (mode == '') {
         return;
      }
      // disabling add/new/favorites/recent
      cms.toolbar.dom[mode + 'Menu'].hide();
      $('ul', cms.toolbar.dom[mode + 'Menu']).add(cms.util.getContainerSelector()).sortable('destroy');
   }
   
   var getCurrentMode = function() {
      if (cms.toolbar.mode == '') {
         return NullMode;
      }
      return modeMap[cms.toolbar.mode];
      
   }
   
   /*======================================= START MODE DEFINITIONS ==============================================*/
   
   /**
    * Save action.
    */
   var SaveMode = {
      name: 'save',
      createButton: function() {
         var self = this;
         self.button = $('<button name="Save" title="' + M.SAVE_BUTTON_TITLE + '"  class="cms-right ui-state-default ui-corner-all cms-deactivated"><span class="ui-icon cms-icon-save"/>&nbsp;</button>');
         self.button.click(showSaveDialog);
         return self.button;
      },
      
      initialize: doNothing
   
   
   }
   
   var PublishMode = {
       name: 'publish',
       createButton: function() {
           var self = this;
           self.button = $('<button name="publish" title="Publish" class="cms-right ui-state-default ui-corner-all"><span class="ui-icon cms-icon-publish"/>&nbsp;</button>');
           self.button.click(function() {
               if (!$(this).hasClass('cms-deactivated')) {
                   (new cms.publish.PublishDialog('')).start();
               }
           });
           return self.button;
       },
       initialize: doNothing
       
       
       
   }
   
   
   var FavoritesListMode = {
      name: 'favorites',
      createButton: function() {
         var self = this;
         self.button = makeWideButton('favorites', M.FAV_BUTTON_TITLE, 'cms-icon-favorites');
         self.button.click(function() {
            toggleMode(self);
         })
         return self.button;
      },
      menuId: cms.html.favoriteMenuId,
      load: cms.data.loadFavorites,
      prepareAfterLoad: resetFavList,
      enable: _enableListMode,
      disable: _disableListMode,
      initialize: function() {
         cms.toolbar.dom.favoritesMenu = $(cms.html.createMenu(cms.html.favoriteMenuId)).appendTo(cms.toolbar.dom.toolbarContent);
         cms.toolbar.dom.favoritesDrop = $(cms.html.createFavDrop()).appendTo(cms.toolbar.dom.toolbarContent);
         cms.toolbar.dom.favoritesDialog = $(cms.html.favoriteDialog).appendTo(_bodyEl);
         initFavDialog();
      }
   }
   
   var AddListMode = {
      name: 'add',
      createButton: function() {
         var self = this;
         self.button = makeWideButton('add', M.ADD_BUTTON_TITLE, 'cms-icon-add');
         self.button.click(function() {
            toggleMode(self);
         });
         return self.button;
      },
      menuId: cms.html.searchMenuId,
      load: cms.search.checkLastSearch,
      prepareAfterLoad: doNothing,
      enable: _enableListMode,
      disable: _disableListMode,
      initialize: function() {
         cms.toolbar.dom.addMenu = $(cms.html.searchMenu).appendTo(cms.toolbar.dom.toolbarContent);
      }
   }
   
   var RecentListMode = {
      name: 'recent',
      createButton: function() {
         var self = this;
         self.button = makeWideButton('recent', M.RECENT_BUTTON_TITLE, 'cms-icon-recent').click(function() {
            toggleMode(self);
         });
         return self.button;
         
      },
      menuId: cms.html.recentMenuId,
      load: cms.data.loadRecent,
      prepareAfterLoad: resetRecentList,
      disable: _disableListMode,
      enable: _enableListMode,
      initialize: function() {
         cms.toolbar.dom.recentMenu = $(cms.html.createMenu(cms.html.recentMenuId)).appendTo(cms.toolbar.dom.toolbarContent);
      }
   }
   
   var NewListMode = {
      name: 'new',
      menuId: cms.html.newMenuId,
      createButton: function() {
         var self = this;
         self.button = makeWideButton('new', M.NEW_BUTTON_TITLE, 'cms-icon-new').click(function() {
            toggleMode(self);
         });
         return self.button;
      },
      prepareAfterLoad: doNothing,
      
      load: function(callback) {
         callback(true, null);
      },
      disable: _disableListMode,
      enable: _enableListMode,
      initialize: function() {
         cms.toolbar.dom.newMenu = $(cms.html.createMenu(cms.html.newMenuId)).appendTo(cms.toolbar.dom.toolbarContent);
      }
   }
   
   var PropertyMode = {
      name: 'properties',
      isEdit: true,
      createButton: function() {
         var self = this;
         self.button = makeModeButton(this.name, M.PROP_BUTTON_TITLE, 'cms-icon-prop').click(function() {
            toggleMode(self);
         });
         return self.button;
      },
      
      isCompatibleWith: function(elemId) {
         return elemId.match(/^ade_/);
      },
      
      createHandle: function(elemId, elem) {
         return $('<a class="cms-properties"></a>');
      },
      
      enable: _enableEditMode,
      disable: _disableEditMode,
      initialize: function() {
         $('a.cms-properties').live('click', openPropertyDialog);
      }
      
   }
   
   var DeleteMode = {
      name: 'delete',
      isEdit: true,
      createButton: function() {
         var self = this;
         self.button = makeModeButton(this.name, M.DELETE_BUTTON_TITLE, 'cms-icon-delete').click(function() {
            toggleMode(self);
         });
         return self.button;
      },
      
      isCompatibleWith: function(elem) {
         return true;
      },
      
      createHandle: function(elemId, elem) {
         return $('<a class="cms-delete"></a>');
      },
      
      enable: _enableEditMode,
      disable: _disableEditMode,
      initialize: function() {
         $('a.cms-delete').live('click', deleteItem);
      }
   }
   
   
   var EditMode = {
      name: 'edit',
      isEdit: true,
      createButton: function() {
         var self = this;
         self.button = makeModeButton(this.name, M.EDIT_BUTTON_TITLE, 'cms-icon-edit').click(function() {
            toggleMode(self);
         });
         return self.button;
      },
      
      isCompatibleWith: function(elem) {
         return true;
      },
      
      createHandle: function(elemId, elem) {
         if (cms.data.elements[elemId].allowEdit) {
            return $('<a class="cms-edit cms-edit-enabled"></a>');
         } else {
            return $('<a class="cms-edit cms-edit-locked" title="' + cms.util.format(M.LOCKED_BY, cms.data.elements[elemId].locked) + '" onclick="return false;"></a>');
         }
      },
      
      enable: _enableEditMode,
      disable: _disableEditMode,
      initialize: function() {
         $('a.cms-edit.cms-edit-enabled').live('click', openEditDialog);
      }
      
   }
   
   
   var MoveMode = {
      name: 'move',
      isEdit: true,
      createButton: function() {
         var self = this;
         self.button = makeModeButton(this.name, M.MOVE_BUTTON_TITLE, 'cms-icon-move').click(function() {
            toggleMode(self);
         });
         return self.button;
      },
      
      isCompatibleWith: function(elem) {
         return true;
      },
      
      createHandle: function(elemId, elem) {
         return $('<a class="cms-move"></a>');
      },
      
      enable: _enableEditMode,
      disable: _disableEditMode,
      initialize: doNothing
   }
   
   var NullMode = {
      name: '',
      disable: doNothing,
      enable: doNothing
   }
   
   
   var ResetMode = {
      name: 'reset',
      createButton: function() {
         var self = this;
         self.button = makeModeButton(this.name, M.RESET_BUTTON_TITLE, 'cms-icon-reset').click(function() {
            if ($('#cms-reset-dialog').size() == 0) {
               $('<div id="cms-reset-dialog" style="display:none">' + M.RESET_CONFIRM + '</div>').appendTo('body');
            }
            var $dlg = $('#cms-reset-dialog');
            if (self.button.hasClass('cms-deactivated')) {
               return;
            }
            var buttons = {};
            buttons[M.RESET_OK] = function() {
               $(this).dialog('destroy');
               setPageChanged(false);
               window.location.reload();
            }
            
            buttons[M.RESET_CANCEL] = function() {
               $(this).dialog('destroy');
            }
            
            $('#cms-reset-dialog').dialog({
               autoOpen: true,
               modal: true,
               zIndex: 9999,
               title: M.RESET_TITLE,
               buttons: buttons,
               close: function() {
                  $(this).dialog('destroy');
               }
            });
            return false;
         });
         self.button.addClass('cms-deactivated');
         return self.button;
      },
      
      initialize: doNothing
   }
   
   
   /*====================================== END MODE DEFINITIONS =================================*/
   
   
   var addToolbar = cms.toolbar.addToolbar = /** void */ function() {
      _bodyEl = $(document.body).css('position', 'relative');
      
      // remember old margins/offset of body 
      _oldBodyMarginTop = _bodyEl.offset().top;
      _bodyEl.css({
         marginTop: _oldBodyMarginTop + 34 + 'px'
      });
      // appending all necessary toolbar components and keeping their references
      cms.toolbar.dom.toolbar = $(cms.html.toolbar).appendTo(_bodyEl);
      cms.toolbar.dom.toolbarContent = $('#toolbar_content', cms.toolbar.dom.toolbar);
      cms.toolbar.dom.appendBox = $('<div id="cms_appendbox"></div>').appendTo(_bodyEl);
      cms.toolbar.dom.buttons = {};
      for (var i = 0; i < modeNames.length; i++) {
         //we iterate over editModes instead of editModeMap because the order should be fixed
         var modeName = modeNames[i];
         var modeObj = modeMap[modeName];
         modeObj.initialize();
         modeObj.createButton();
         cms.toolbar.dom.toolbarContent.append(modeObj.button);
         cms.toolbar.dom.buttons[modeName] = modeObj.button;
      }
      
      cms.toolbar.dom.showToolbar = $('<button id="show-button" title="toggle toolbar" class="ui-state-default ui-corner-all"><span class="ui-icon cms-icon-logo"/></button>').appendTo(_bodyEl);
      
      // initializing dialogs and event-handler
      window.onbeforeunload = function() {  
         // DO NOT use jquery to bind this!
         cms.data.abortAllRequests();
      }; 
      $(window).unload(onUnload); /* TODO */
      $('button[name="Save"]', cms.toolbar.dom.toolbar).click(showSaveDialog);
      $(document).bind('cms-data-loaded', cms.search.initScrollHandler);
      cms.toolbar.dom.addMenu.find('button.cms-search-button').click(function() {
         if ($('#cms-search-dialog').length < 1) {
            cms.search.initSearch();
            this.click();
         }
      });
      
      cms.toolbar.dom.showToolbar.click(toggleToolbar);
      $('#toolbar button, #show-button').mouseover(function() {
         if (!$(this).hasClass('cms-deactivated')) {
            $(this).addClass('ui-state-hover');
         }
      }).mouseout(function() {
         $(this).removeClass('ui-state-hover');
      });
      
      initLinks();
      $(window).resize(fixMenuAlignment);
   };
   
   /**
    * The mode objects in the order in which the buttons should appear in the toolbar.
    */
   var modes = [ResetMode, EditMode, MoveMode, DeleteMode, PropertyMode, AddListMode, NewListMode, FavoritesListMode, RecentListMode, SaveMode, PublishMode];
   
   /**
    * Gets a mode by mode name.
    * @param {Object} modeName
    */
   var getMode = function(modeName) {
      return modeMap[modeName];
   }
   
   /**
    * Builds a map from a list of objects by using a key function to generate the property name under which the object should be stored.
    * @param {Object} items the list of objects
    * @param {Object} keyFunction the function to generate a key from an object
    */
   var buildMap = function(items, keyFunction) {
      var result = {};
      for (var i = 0; i < items.length; i++) {
         var item = items[i];
         result[keyFunction(item)] = item;
      }
      return result;
   }
   
   /**
    * The map of modes, using the mode names as keys.
    */
   var modeMap = buildMap(modes, function(mode) {
      return mode.name;
   });
   modeMap[''] = NullMode;
   
   /**
    * The list of mode names.
    */
   var modeNames = $.map(modes, function(mode) {
      return mode.name;
   })
   
   
})(cms);

