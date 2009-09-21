(function(cms) {
   var $ = jQuery;
   
   // current toolbar-mode ('Move', 'Edit', 'Delete' etc.)
   var mode = cms.toolbar.mode = '';
   
   // hashmap for jquery-dom-objects
   var dom = cms.toolbar.dom = {};
   
   var _bodyEl;
   var _oldBodyMargin;
   
   cms.toolbar.favorites = [];
   cms.toolbar.recent = [];
   cms.toolbar.recentSize = 10;
   var toolbarReady = cms.toolbar.toolbarReady = false;
   var leavingPage = cms.toolbar.leavingPage = false;
   var oldBodyMarginTop = 0;
   var menuIds = [cms.html.favoriteMenuId, cms.html.recentMenuId, cms.html.newMenuId, cms.html.searchMenuId];
   var sortmenus = cms.util.makeCombinedSelector(menuIds, "#% ul");
   var menuHandles = cms.util.makeCombinedSelector(menuIds, "#% a.cms-move")
   var menus = cms.util.makeCombinedSelector(menuIds, "#%");
   cms.toolbar.currentMenu = cms.html.favoriteMenuId;
   cms.toolbar.currentMenuItems = cms.html.favoriteListId;
   
   /**
    * This function will check if the given mode-string is one of the three editing modes (edit, move, delete).<p>
    *
    * @param {String} mode
    * @return {boolean}
    */
   var _isEditingMode = function(mode) {
      if (mode == 'edit' || mode == 'move' || mode == 'delete') {
         return true;
      }
      return false;
   }
   
   /**
    * This function will display the publish dialog.<p>
    */
   var showPublishList = cms.toolbar.showPublishList = function() {
      var button = $(this);
      if (button.hasClass('ui-state-active')) {
      
      
         button.removeClass('ui-state-active');
      } else {
         $('button.ui-state-active').trigger('click');
         // appending publish-dialog content
         $(document.body).append(cms.html.publishDialog);
         
         $('#' + cms.html.publishDialogId).dialog({
            buttons: {
               "Cancel": function() {
                  $(this).dialog("close");
               },
               "Publish": function() {
                  $(this).dialog("close");
               }
            },
            width: 340,
            title: "Publish",
            modal: true,
            autoOpen: true,
            draggable: true,
            resizable: false,
            position: ['center', 20],
            close: function() {
               $('button[name="Publish"]').removeClass('ui-state-active');
               $('#' + cms.html.publishDialogId).dialog('destroy');
            },
            zIndex: 10000
         });
         $('#' + cms.html.publishDialogId + ' span.cms-check-icon').click(function() {
            $(this).toggleClass('cms-check-icon-inactive')
         });
         
         button.addClass('ui-state-active');
      }
   };
   
   /**
    * This event-handler function will remove an element from a container.
    */
   var deleteItem = cms.toolbar.deleteItem = function() {
      var $item = $(this).closest('.cms-element');
      var $container = $item.parent();
      cms.move.hoverOut();
      var elemId = $item.attr('rel');
      if (elemId && cms.data.elements[elemId]) {
         if (cms.data.elements[elemId].status == cms.data.STATUS_CREATED) {
            cms.data.deleteResources([elemId], function(ok) {
               deleteFromFavListAndRecList([elemId]);
               if (!ok) {
                  // TODO
                  alert("error");
               }
            });
         } else {
            addToRecent($item.attr('rel'));
         }
      }
      $item.remove();
      cms.move.updateContainer($container.attr('id'));
      setPageChanged(true);
   };
   
   
   /**
    * Deletes the given elements from the favorites and recent list.<p>
    *
    * @param {Array} ids a list of ids of elements to be deleted
    */
   var deleteFromFavListAndRecList = function(/**Array<String>*/ids) {
   
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
   var startHoverTimeout = cms.toolbar.startHoverTimeout = function(handleDiv, adeMode) {
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
   var showAddButtons = cms.toolbar.showAddButtons = function() {
      timer.id = null;
      var right = '-48px';
      var showMoveLeft = true;
      var setRightToZero = false;
      var inwardsHandle = false;
      
      timer.handleDiv.addClass('ui-widget-header').css({
         'width': '72px',
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
   var stopHover = cms.toolbar.stopHover = function() {
      if (timer.id) {
         clearTimeout(timer.id);
         timer.id = null;
      }
      cms.move.hoverOut();
      
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
   var initHandleDiv = cms.toolbar.initHandleDiv = function(handleDiv, elem, /**String*/ adeMode) {
      handleDiv.hover(function() {
         cms.move.hoverOutFilter(elem, '.' + cms.move.HOVER_NEW);
         cms.move.hoverIn(elem, 2);
         //cms.move.hoverOutFilter(elem, '.'+cms.move.HOVER_NEW);
         startHoverTimeout(handleDiv, cms.toolbar.mode);
         $('body').children('.' + cms.move.HOVER_NEW).remove();
      }, function() {
         stopHover();
         if ($(elem).find('.' + cms.move.HOVER_NEW).size() == 0 && $(elem).hasClass('cms-new-element')) {
            cms.move.hoverInWithClass(elem, 2, cms.move.HOVER_NEW);
         }
         $('body').children('.' + cms.move.HOVER_NEW).remove();
      });
      $('body').children('.' + cms.move.HOVER_NEW).remove();
   }
   
   /**
    * Adds handle div to element.<p>
    * 
    * @param {Object} elem jquery-element-object
    * @param {Object} elemId the resource-id
    * @param {Object} adeMode current mode
    * @param {Object} isMoving indicates if the current element is a sortable helper object
    */
   var addHandles = cms.toolbar.addHandles = function(elem, elemId, adeMode, isMoving) {
      var handleDiv = $('<div class="cms-handle"></div>').appendTo(elem);
      
      var handles = {
         'edit': cms.data.elements[elemId].allowEdit ? $('<a class="cms-edit cms-edit-enabled"></a>').click(openEditDialog) : $('<a class="cms-edit cms-edit-locked" title="locked by ' + cms.data.elements[elemId].locked + '" onclick="return false;"></a>'),
         'move': $('<a class="cms-move"></a>'),
         'delete': $('<a class="cms-delete"></a>').click(deleteItem)
      };
      handles[adeMode].appendTo(handleDiv);
      for (handleName in handles) {
         if (handleName != adeMode) {
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
    * Click-event-handler for edit, move, delete, add, new, favorite and recent toolbar buttons.<p>
    * Will enable or disable the current and previous mode.<p>
    */
   var toggleMode = cms.toolbar.toggleMode = function() {
      if (!cms.toolbar.toolbarReady) {
         return;
      }
      var button = $(this);
      var buttonMode = button.attr('name').toLowerCase();
      var buttonName = button.attr('name').toLowerCase();
      if (button.hasClass('ui-state-active')) {
      
         _disableMode(buttonMode);
         
         // TODO: find a better way to rerender stuff in IE
         if ($.browser.msie) {
            // In IE7 html-block elements may disappear after sorting, this should trigger the html to be re-rendered.
            setTimeout(function() {
               $(cms.util.getContainerSelector()).hide().show();
            }, 0);
         }
         cms.toolbar.mode = '';
      } else {
      
         if (_isEditingMode(cms.toolbar.mode) && _isEditingMode(buttonMode)) {
            // reorder handles
            
            $('.cms-element div.cms-handle').each(function() {
               var handleDiv = $(this);
               $('a', handleDiv).css('display', 'none');
               $('a.cms-' + buttonMode, handleDiv).prependTo(handleDiv).css('display', 'block');
               
            });
            cms.toolbar.dom.buttons[cms.toolbar.mode].removeClass('ui-state-active');
            
         } else {
            _disableMode(cms.toolbar.mode);
            if (_isEditingMode(buttonMode)) {
               $(cms.util.getContainerSelector()).children('.cms-element').each(function() {
                  var elem = $(this).css('position', 'relative');
                  var elemId = elem.attr('rel');
                  if (elemId && cms.data.elements[elemId]) {
                     addHandles(elem, elemId, buttonMode);
                  }
                  
               });
               initMove();
            } else {
            
               var loadFunction;
               if (buttonMode == 'favorites') {
                  cms.toolbar.currentMenu = cms.html.favoriteMenuId;
                  loadFunction = cms.data.loadFavorites;
               } else if (buttonMode == 'recent') {
                  cms.toolbar.currentMenu = cms.html.recentMenuId;
                  loadFunction = cms.data.loadRecent;
               } else if (buttonMode == 'add') {
                  cms.toolbar.currentMenu = cms.html.searchMenuId;
                  loadFunction = cms.search.checkLastSearch;
               } else {
                  // set a dummy load function that will immediately 
                  // execute its callback
                  cms.toolbar.currentMenu = cms.html.newMenuId;
                  loadFunction = function(callback) {
                     callback(true, null);
                  }
               }
               
               $('button.ui-state-active').trigger('click');
               button.addClass('ui-state-active');
               // enabling move-mode
               // * current menu
               loadFunction(function(ok, data) {
                  if (!ok) {
                     // TODO
                     return;
                  }
                  if (buttonMode == 'favorites') {
                     resetFavList();
                  }
                  if (buttonMode == 'recent') {
                     resetRecentList();
                  }
                  if (!button.hasClass("ui-state-active")) {
                     return;
                  }
                  list = $('#' + cms.toolbar.currentMenu);
                  $('.cms-head:not(:has(a.cms-move))', list).each(function() {
                     var elem = $(this);
                     $('<a class="cms-handle cms-move"></a>').appendTo(elem);
                  });
                  list.css({
                     /* position : 'fixed', */
                     top: 35,
                     left: button.position().left - 245
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
                  $(cms.util.getContainerSelector() + ', #' + cms.toolbar.currentMenu + ' ul.cms-item-list').sortable({
                     // * current menu
                     connectWith: cms.util.getContainerSelector() + ', #' + cms.toolbar.currentMenu + ' ul.cms-item-list',
                     placeholder: 'placeholder',
                     dropOnEmpty: true,
                     start: cms.move.startAdd,
                     beforeStop: cms.move.beforeStopFunction,
                     over: cms.move.overAdd,
                     out: cms.move.outAdd,
                     tolerance: 'pointer',
                     stop: cms.move.stopAdd,
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
            
         }
         cms.toolbar.mode = buttonMode;
         cms.toolbar.dom.buttons[cms.toolbar.mode].addClass('ui-state-active');
      }
   };
   
   /**
    * Disables the given mode.<p>
    * 
    * @param {Object} mode
    */
   var _disableMode = function(mode) {
      if (mode == '') {
         return;
      }
      if (_isEditingMode(mode)) {
         // disabling edit/move/delete
         var containerSelector = cms.util.getContainerSelector();
         // replace ids
         $(containerSelector + ', #' + cms.html.favoriteDropListId).sortable('destroy');
         var list = $('#' + cms.html.favoriteDropMenuId);
         $('li.cms-item, button', list).css('display', 'block');
         resetFavList();
         $('.cms-element div.cms-handle').remove();
      } else {
         // disabling add/new/favorites/recent
         cms.toolbar.dom[mode + 'Menu'].hide();
         $('ul', cms.toolbar.dom[mode + 'Menu']).add(cms.util.getContainerSelector()).sortable('destroy');
      }
      cms.toolbar.dom.buttons[mode].removeClass('ui-state-active');
   }
   
   /**
    * Click-event-handler for edit-handles.<p>
    * Opens the content-editor-dialog.<p>
    */
   var openEditDialog = cms.toolbar.openEditDialog = function() {
      var $domElement = $(this).closest('.cms-element');
      var elemId = $domElement.attr('rel');
      
      if (elemId && cms.data.elements[elemId]) {
         if (cms.data.elements[elemId].allowEdit) {
            var element = cms.data.elements[elemId];
            var _openDialog = function(path, id, afterClose) {
               var dialogWidth = self.innerWidth ? self.innerWidth : self.document.body.clientWidth;
               dialogWidth = dialogWidth > 1360 ? 1360 : dialogWidth;
               var dialogHeight = self.innerHeight ? self.innerHeight : self.document.body.clientHeight;
               dialogHeight = dialogHeight < 700 ? dialogHeight : 700;
               var iFrameHeight = dialogHeight - 115 // mmoossen: resource name in body: - 126;
               var editorLink = cms.data.EDITOR_URL + '?resource=' + path + '&amp;directedit=true&amp;elementlanguage=' + cms.data.locale + '&amp;backlink=' + cms.data.BACKLINK_URL + '&amp;redirect=true';
               var editorFrame = '<iframe style="border:none; width:100%; height:' + iFrameHeight + 'px;" name="cmsAdvancedDirectEditor" src="' + editorLink + '"></iframe>';
               var editorDialog = $('#cms-editor');
               if (!editorDialog.length) {
                  editorDialog = $('<div id="cms-editor"  rel="' + id + '"></div>').appendTo(document.body);
               } else {
                  editorDialog.empty().attr('rel', id);
               }
               
               // mmoossen: resource name in body: editorDialog.append('<div class="cms-editor-subtitle">Resource: ' + path + '</div>')
               editorDialog.append(editorFrame);
               editorDialog.dialog({
                  width: dialogWidth - 50,
                  height: dialogHeight - 60,
                  title: "Editor - " + path, // mmoossen: resource name in title
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
                     $('#cms_appendbox').css('z-index', 10005).append(editorDialog.parent());
                     $('a.ui-dialog-titlebar-close').hide();
                     editorDialog.parent().css('top', '0px')
                  },
                  close: function() {
                     editorDialog.empty().dialog('destroy');
                     cms.data.reloadElement(id, function(ok) {
                        if (ok) {
                           // to reset the mode we turn it off and on again
                           var activeButton = $("#toolbar button.ui-state-active");
                           activeButton.trigger('click');
                           activeButton.trigger('click');
                        } else {
                                                // TODO
                        }
                     });
                  },
                  zIndex: 10000
               });
            }
            if (element.status == cms.data.STATUS_NEWCONFIG) {
               cms.data.createResource(element.type, function(ok, id, uri) {
                  if (!ok) {
                     // TODO
                     return;
                  }
                  cms.move.hoverOutFilter($domElement, '.' + cms.move.HOVER_NEW);
                  var elem = cms.data.elements[elemId];
                  delete cms.data.elements[elemId];
                  cms.data.elements[id] = elem;
                  elem.id = id;
                  elem.status = cms.data.STATUS_CREATED;
                  cms.util.replaceNewElement(elemId, id);
                  _openDialog(uri, id);
               });
               
            } else {
               _openDialog(element.file, elemId);
               
            }
         }
      }
   }
   
   /**
    * Removes the toolbar-html-element from the dom.<p>
    * Currently not used.<p>
    */
   var removeToolbar = cms.toolbar.removeToolbar = function() {
      $('#toolbar').remove();
      $(document.body).css('margin-top', oldBodyMarginTop + 'px');
   };
   
   /**
    * Hides the toolbar.<p>
    */
   var hideToolbar = cms.toolbar.hideToolbar = function() {
      $(document.body).animate({
         marginTop: oldBodyMarginTop + 'px'
      }, 200, 'swing', function() {
         $('#show-button').show(50);
      });
      
      return false;
   };
   
   /**
    * Toggles the toolbar.<p>
    */
   var toggleToolbar = cms.toolbar.toggleToolbar = function() {
      var button = $('#show-button');
      if (button.hasClass('toolbar_hidden')) {
         $('#toolbar').fadeIn(100);
         $(document.body).animate({
            marginTop: oldBodyMarginTop + 34 + 'px'
         }, 200, 'swing');
         button.removeClass('toolbar_hidden');
      } else {
         $(document.body).animate({
            marginTop: oldBodyMarginTop + 'px'
         }, 200, 'swing');
         $('#toolbar').fadeOut(100);
         button.addClass('toolbar_hidden');
      }
      return false;
   };
   
   /**
    * Helper class for displaying a 'Loading' sign when loading takes too long
    */
   var LoadingSign = cms.toolbar.LoadingSign = function(selector, waitTime, showLoading, hideLoading) {
   
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
    * Adds the toolbar and most other ADE elements to the dom and initializes dialogs and event-handlers.<p>
    */
   var addToolbar = cms.toolbar.addToolbar = function() {
      _bodyEl = $(document.body).css('position', 'relative');
      
      // remember old margins/offset of body 
      var offsetLeft = _bodyEl.offset().left;
      _oldBodyMarginTop = _bodyEl.offset().top;
      _bodyEl.css({
         marginTop: oldBodyMarginTop + 34 + 'px'
      });
      // appending all necessary toolbar components and keeping their references
      cms.toolbar.dom.toolbar = $(cms.html.toolbar).appendTo(_bodyEl);
      cms.toolbar.dom.toolbarContent = $('#toolbar_content', cms.toolbar.dom.toolbar);
      cms.toolbar.dom.favoritesMenu = $(cms.html.createMenu(cms.html.favoriteMenuId)).appendTo(cms.toolbar.dom.toolbarContent);
      cms.toolbar.dom.favoritesDrop = $(cms.html.createFavDrop()).appendTo(cms.toolbar.dom.toolbarContent);
      cms.toolbar.dom.newMenu = $(cms.html.createMenu(cms.html.newMenuId)).appendTo(cms.toolbar.dom.toolbarContent);
      cms.toolbar.dom.addMenu = $(cms.html.searchMenu).appendTo(cms.toolbar.dom.toolbarContent);
      cms.toolbar.dom.favoritesDialog = $(cms.html.favoriteDialog).appendTo(_bodyEl);
      cms.toolbar.dom.recentMenu = $(cms.html.createMenu(cms.html.recentMenuId)).appendTo(cms.toolbar.dom.toolbarContent);
      cms.toolbar.dom.appendBox = $('<div id="cms_appendbox"></div>').appendTo(_bodyEl);
      cms.toolbar.dom.buttons = {
         'edit': $('button[name="Edit"]', cms.toolbar.dom.toolbar),
         'move': $('button[name="Move"]', cms.toolbar.dom.toolbar),
         'delete': $('button[name="Delete"]', cms.toolbar.dom.toolbar),
         'add': $('button[name="Add"]', cms.toolbar.dom.toolbar),
         'new': $('button[name="New"]', cms.toolbar.dom.toolbar),
         'recent': $('button[name="Recent"]', cms.toolbar.dom.toolbar),
         'favorites': $('button[name="Favorites"]', cms.toolbar.dom.toolbar)
      };
      cms.toolbar.dom.showToolbar = $('<button id="show-button" title="toggle toolbar" class="ui-state-default ui-corner-all"><span class="ui-icon cms-icon-logo"/></button>').appendTo(_bodyEl);
      
      // initializing dialogs and event-handler
      $(window).unload(onUnload); /* TODO */
      initSaveDialog();
      $('button[name="Save"]', cms.toolbar.dom.toolbar).click(showSaveDialog);
      $(document).bind('cms-data-loaded', cms.search.initScrollHandler);
      cms.toolbar.dom.addMenu.find('button.cms-search-button').click(function() {
         if ($('#cms-search-dialog').length < 1) {
            cms.search.initSearch();
            this.click();
         }
      });
      
      cms.toolbar.dom.showToolbar.click(toggleToolbar);
      $.each(cms.toolbar.dom.buttons, function() {
         this.click(toggleMode);
      });
      
      $('#toolbar button, #show-button').mouseover(function() {
         if (!$(this).hasClass('cms-deactivated')) {
            $(this).addClass('ui-state-hover');
         }
      }).mouseout(function() {
         $(this).removeClass('ui-state-hover');
      });
      
      
      initFavDialog();
      initLinks();
      initReset();
      
      $(window).resize(fixMenuAlignment);
      
      
   };
   
   /**
    * Moves the dropdown menus to the right if required for small screen-sizes.<p>
    */
   var fixMenuAlignment = function() {
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
   var initMove = function() {
      var containerSelector = cms.util.getContainerSelector();
      
      // replace id
      var list = cms.toolbar.dom.favoritesDrop;
      var favbutton = $('button[name="Favorites"]');
      
      //unnecessary
      $('li.cms-item, button', list).css('display', 'none');
      
      list.css({
         top: 35,
         left: 97,
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
         start: cms.move.startAdd,
         beforeStop: cms.move.beforeStopFunction,
         over: cms.move.overAdd,
         out: cms.move.outAdd,
         change: function(event, ui) {
            cms.move.hoverOut(ui.helper.parent());
            cms.move.hoverInner(ui.helper.parent(), 2, true);
         },
         tolerance: 'pointer',
         //           opacity: 0.7,
         stop: cms.move.stopAdd,
         cursorAt: {
            right: 10,
            top: 10
         },
         zIndex: 20000,
         handle: 'a.cms-move',
         items: '.cms-element',
         revert: true,
         // replace ids
         deactivate: cms.move.deactivateAdd
      });
   }
   
   
   
   /**
    * Click-event-handler for delete icon in favorites dialog.<p>
    */
   var clickFavDeleteIcon = cms.toolbar.clickFavDeleteIcon = function() {
      var button = $(this);
      var toRemove = button.parent().parent();
      toRemove.remove();
   };
   
   /**
    * Utility function to generate a string representing an array.<p>
    * @param {Object} arr
    */
   var arrayToString = function(arr) {
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
      var buttons = {
         "Cancel": favEditCancel,
         "OK": favEditOK
      
      };
      $('#fav-dialog').dialog({
         width: 380,
         // height: 500,
         title: "Edit favorites",
         modal: true,
         autoOpen: false,
         draggable: true,
         resizable: true,
         position: ['center', 20],
         close: function() {
            $('#fav-edit').removeClass('ui-state-active');
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
         button.removeClass("ui-state-active");
      } else {
         $('button.ui-state-active').trigger('click');
         // enabling move-mode
         initFavDialogItems();
         $('#fav-dialog').dialog('open');
         button.addClass('ui-state-active');
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
   
  
   
//   var createNewListItemHtml = function(type, name) {
//      return '<div rel="' + type + '">' + name + '</div>';
//   };
//   
//   
//   var createNewListItemHtml = function(type, name) {
//      var html = ['<li class="cms-item" rel="', type, '">\
//		<div class="cms-left ui-widget-content">\
//			<div class="cms-head ui-state-hover">\
//				<div class="cms-navtext"><a class="cms-left ui-icon ui-icon-triangle-1-e"></a>', type, '</div>\
//				<span class="cms-title">', name, '</span>\
//				<span class="cms-file-icon"></span>\
//				<a class="cms-move cms-handle"></a>\
//			</div>\
//		</div>\
//		<br clear="all" />\
//	</li>'];
//      return html.join('');
//   };
   
   /**
    * Reloads the new items list.<p>
    */
   var resetNewList = cms.toolbar.resetNewList = function() {
      $('#' + cms.html.newMenuId + " li.cms-item").remove();
      var $newlist = $('#' + cms.html.newMenuId + " ul");
      for (var key in cms.data.elements) {
         if (cms.data.elements[key].status != cms.data.STATUS_NEWCONFIG) {
            continue;
         }
         
         $newlist.append(cms.html.createItemFavListHtml(key));
      }
   }
   
   /**
    * Adds item to recent list.<p>
    * 
    * @param {Object} itemId
    */
   var addToRecent = cms.toolbar.addToRecent = function(itemId) {
      cms.util.addUnique(cms.toolbar.recent, itemId, cms.toolbar.recentSize);
      cms.data.persistRecent(function(ok) {
         if (!ok) {
                  // TODO
         }
      });
   }
   
   
   /**
    * Initializes save dialog.<p>
    */
   var initSaveDialog = function() {
      $('<div id="cms-save-dialog" style="display:none;" title="Save page">\
      <p>\
        Do you really want to save your changes?\
      </p>\
    </div>').appendTo("body").dialog({
         autoOpen: false,
         buttons: {
            Cancel: function() {
               $(this).dialog('close');
               $('button[name="Save"]').removeClass('ui-state-active');
               
            },
            'Save': function() {
               // TODO: may be we can disable the dialog here
               // $(this).dialog('disable');
               $('button[name="Save"]').removeClass('ui-state-active');
               savePage();
            }
            
         },
         resizable: false,
         modal: true,
         zIndex: 10000
      
      });
   }
   
   
   /**
    * Opens save dialog.<p>
    */
   var showSaveDialog = function() {
      if (!$(this).hasClass('cms-deactivated')) {
         $('button[name="Save"]').addClass('ui-state-active');
         $('#cms-save-dialog').dialog('open');
      }
   }
   
   /**
    * Persists container-page on server.<p>
    * @param {Object} callback
    */
   var savePage = cms.toolbar.savePage = function(callback) {
   
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
    * @param {Object} newValue
    */
   var setPageChanged = cms.toolbar.setPageChanged = function(newValue) {
      pageChanged = cms.toolbar.pageChanged = newValue;
      if (newValue) {
         $('#toolbar button[name="Save"], #toolbar button[name="Reset"]').removeClass('cms-deactivated');
      } else {
         $('#toolbar button[name="Save"], #toolbar button[name="Reset"]').addClass('cms-deactivated');
      }
   }
   
   /**
    * On-unload event-handler to prevent accidental data-loss.<p> 
    */
   var onUnload = cms.toolbar.onUnload = function() {
      if (cms.toolbar.pageChanged) {
         var saveChanges = window.confirm("Do you want to save your changes made on " + window.location.href + "?\n (Cancel will discard changes)");
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
                     alert("error");
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
      $('<div id="cms-leave-dialog" style="display: none;">Do you really want to leave the page?</div>').appendTo('body');
      $('a:not(.cms-left, .cms-move, .cms-delete, .cms-edit)').live('click', function() {
         if (!cms.toolbar.pageChanged) {
            cms.toolbar.leavingPage = true;
            return;
         }
         var $link = $(this);
         var target = $link.attr('href');
         var buttons = {};
         
         buttons['Save and Leave'] = function() {
            $(this).dialog('destroy');
            savePage(function(ok) {
               if (ok) {
                  window.location.href = target;
               }
            });
         };
         
         buttons['Leave'] = function() {
            $(this).dialog('destroy');
            setPageChanged(false);
            window.location.href = target;
         };
         
         buttons['Cancel'] = function() {
            $(this).dialog('destroy');
         };
         
         $('#cms-leave-dialog').dialog({
            autoOpen: true,
            modal: true,
            title: 'Leaving the page',
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
    * Initializes the reset dialog.
    */
   var initReset = cms.toolbar.initReset = function() {
      $('<div id="cms-reset-dialog" style="display:none">Do you really want to discard your changes and reset the page?</div>').appendTo('body');
      $('button[name="Reset"]').live('click', function() {
         if ($(this).hasClass('cms-deactivated')) {
            return;
         }
         var buttons = {};
         buttons['Reset'] = function() {
            $(this).dialog('destroy');
            setPageChanged(false);
            window.location.reload();
         }
         
         buttons['Cancel'] = function() {
            $(this).dialog('destroy');
         }
         
         $('#cms-reset-dialog').dialog({
            autoOpen: true,
            modal: true,
            zIndex: 9999,
            title: 'Reset',
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
   
})(cms);
