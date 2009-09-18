(function(cms) {
   var $ = jQuery;
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
   
   var toggleDelete = cms.toolbar.toggleDelete = function(el) {
      var button = $(this);
      
      if (button.hasClass('ui-state-active')) {
         // disabling delete mode
         $('a.cms-delete').remove();
         button.removeClass('ui-state-active');
      } else {
         $('button.ui-state-active').trigger('click');
         
         // enabling delete mode
         $(cms.data.deleteitems).each(function() {
            var elem = $(this).css('position', 'relative');
            $('<a class="cms-handle cms-delete"></a>').appendTo(elem).hover(function() {
               cms.move.hoverIn(elem, 2);
            }, cms.move.hoverOut).click(deleteItem);
         });
         button.addClass('ui-state-active');
      }
   };
   var timer = cms.toolbar.timer = {
      id: null,
      handleDiv: null,
      adeMode: null
   };
   var startHoverTimeout = cms.toolbar.startHoverTimeout = function(handleDiv, adeMode) {
      if (timer.id) {
         clearTimeout(timer.id);
      }
      timer.id = setTimeout(cms.toolbar.showAddButtons, 1000);
      timer.handleDiv = handleDiv;
      timer.adeMode = adeMode;
   }
   
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
         timer.handleDiv.addClass('cms-handle-reverse').css('right', '0px');
      } else {
         timer.handleDiv.removeClass('cms-handle-reverse');
      }
   }
   
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
   
   var toggleEdit = cms.toolbar.toggleEdit = function() {
      var button = $(this);
      if (button.hasClass('ui-state-active')) {
         // disabling edit mode
         $('.cms-element div.cms-handle').remove();
         button.removeClass('ui-state-active');
      } else {
         $('button.ui-state-active').trigger('click');
         // enabling edit mode
         $(cms.data.sortitems).each(function() {
            var elem = $(this).css('position', 'relative');
            var elemId = elem.attr('rel');
            if (elemId && cms.data.elements[elemId]) {
               if (cms.data.elements[elemId].allowEdit) {
                  var handleDiv = $('<div class="cms-handle"></div>').appendTo(elem).hover(function() {
                     cms.move.hoverIn(elem, 2);
                     startHoverTimeout(handleDiv, 'edit');
                  }, function() {
                     stopHover();
                  });
                  
                  $('<a class="cms-edit"></a>').appendTo(handleDiv).click(function() {
                     openEditDialog(elemId);
                  });
                  $('<a class="cms-move"></a>').css('display', 'none').appendTo(handleDiv);
                  $('<a class="cms-delete"></a>').css('display', 'none').appendTo(handleDiv).click(deleteItem);
                  handleDiv.css({
                     'left': handleDiv.position().left,
                     'width': 24
                  });
               } else {
                              // Append edit-locked-handle
               }
            }
            
         });
         button.addClass('ui-state-active');
      }
   };
   
   var setModePositionLeft = function(handle) {
      if ($(handle).children('*:last').hasClass('cms-' + timer.adeMode)) {
         cms.util.reverse(handle, '*');
      }
   }
   
   var setModePositionRight = function(handle) {
      if (!$(handle).children('*:last').hasClass('cms-' + timer.adeMode)) {
         cms.util.reverse(handle, '*');
      }
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
         startHoverTimeout(handleDiv, adeMode);
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
   
   var toggleMode = cms.toolbar.toggleMode = function() {
      if (!cms.toolbar.toolbarReady) {
         return;
      }
      var button = $(this);
      var adeMode = button.attr('name').toLowerCase();
      if (button.hasClass('ui-state-active')) {
         // disabling edit mode
         destroyMove();
         $('.cms-element div.cms-handle').remove();
         button.removeClass('ui-state-active');
         // TODO: find a better way to rerender stuff in IE
         if ($.browser.msie) {
            setTimeout(function() {
               $(cms.util.getContainerSelector()).hide().show();
            }, 0);
         }
      } else {
         $('button.ui-state-active').trigger('click');
         // enabling edit mode
         $(cms.util.getContainerSelector()).children('.cms-element').each(function() {
            var elem = $(this).css('position', 'relative');
            var elemId = elem.attr('rel');
            if (elemId && cms.data.elements[elemId]) {
               addHandles(elem, elemId, adeMode);
            }
            
         });
         initMove();
         button.addClass('ui-state-active');
      }
   };
   
   
   
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
   
   var removeToolbar = cms.toolbar.removeToolbar = function() {
      $('#toolbar').remove();
      $(document.body).css('margin-top', oldBodyMarginTop + 'px');
   };
   
   var hideToolbar = cms.toolbar.hideToolbar = function() {
      $(document.body).animate({
         marginTop: oldBodyMarginTop + 'px'
      }, 200, 'swing', function() {
         $('#show-button').show(50);
      });
      
      return false;
   };
   
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
   
   
   var addToolbar = cms.toolbar.addToolbar = function() {
      var bodyEl = $(document.body).css('position', 'relative');
      $(window).unload(onUnload);
      initSaveDialog();
      oldBodyMarginTop = bodyEl.offset().top;
      var offsetLeft = bodyEl.offset().left;
      bodyEl.append(cms.html.toolbar);
      
      bodyEl.append(cms.html.createMenu(cms.html.favoriteMenuId));
      bodyEl.append(cms.html.createFavDrop());
      
      bodyEl.append(cms.html.createMenu(cms.html.newMenuId));
      bodyEl.append(cms.html.searchMenu);
      $(document).bind('cms-data-loaded', cms.search.initScrollHandler);
      $('#' + cms.html.searchMenuId).find('button.cms-search-button').click(function() {
         if ($('#cms-search-dialog').length < 1) {
            cms.search.initSearch();
            this.click();
         }
      });
      bodyEl.append(cms.html.favoriteDialog);
      bodyEl.append('<div id="cms_appendbox"></div>');
      bodyEl.append(cms.html.createMenu(cms.html.recentMenuId));
      bodyEl.append('<button id="show-button" title="toggle toolbar" class="ui-state-default ui-corner-all"><span class="ui-icon cms-icon-logo"/></button>');
      $('#show-button').click(toggleToolbar);
      $('#toolbar button[name="Edit"], #toolbar button[name="Move"], #toolbar button[name="Delete"]').click(toggleMode);
      $('#toolbar button[name="Publish"]').click(showPublishList);
      $('#toolbar button[name="Favorites"]').click(function() {
         toggleList(this, cms.html.favoriteMenuId);
      });
      
      $('button[name="Save"]').click(showSaveDialog);
      
      $('button[name="Recent"]').click(function() {
         toggleList(this, cms.html.recentMenuId);
      });
      
      $('button[name="New"]').click(function() {
         toggleList(this, cms.html.newMenuId);
      });
      
      $('button[name="Add"]').click(function() {
         toggleList(this, cms.html.searchMenuId);
      });
      
      $('#toolbar button, #show-button').mouseover(function() {
         if (!$(this).hasClass('cms-deactivated')) {
            $(this).addClass('ui-state-hover');
         }
      }).mouseout(function() {
         $(this).removeClass('ui-state-hover');
      });
      bodyEl.animate({
         marginTop: oldBodyMarginTop + 34 + 'px'
      }, 200);
      
      initFavDialog();
      initLinks();
      initReset();
      
      $(window).resize(fixMenuAlignment);
      
   };
   
   var fixMenuAlignment = function() {
      $('.cms-menu').each(function() {
         var $elem = $(this);
         if ($elem.offset().left < 0) {
            cms.util.setLeft($elem, 0);
         }
      });
   }
   
   
   
   //   /**
   //    * Show the 'LOADING' text for the search menu.
   //    */
   //   var showLoading = function() {
   //      $('.cms-loading').text('LOADING');
   //   }
   
   //   /**
   //    * Returns the number of search results currently in the search menu.
   //    */
   //   var getNumberOfSearchResults = function() {
   //      return $('#cms-search-list').children().size();
   //   }
   //   
   //   var getTotalNumberOfSearchResults = function() {
   //      return cms.search.searchParams.totalResults;
   //   }
   
   //   /**
   //    * Hide the LOADING text for the search menu and display the number of search results loaded instead.
   //    */
   //   var hideLoading = function() {
   //      $('.cms-loading').text(cms.search.getNumberOfResults() + " of ~" + cms.search.getTotalNumberOfResults() + " results loaded");
   //   }
   //   
   
   var destroyMove = function() {
   
      var containerSelector = cms.util.getContainerSelector();
      // replace ids
      $(containerSelector + ', #' + cms.html.favoriteDropListId).sortable('destroy');
      var list = $('#' + cms.html.favoriteDropMenuId);
      $('li.cms-item, button', list).css('display', 'block');
      //list.css('display', 'none');
      //list.get(0).style.visibility = '';
      
      // replace ids
      //$('#' + cms.html.favoriteDropListId).get(0).style.height = '';
      resetFavList();
      //      $('a.cms-move').remove();
   };
   
   var initMove = function() {
      var containerSelector = cms.util.getContainerSelector();
      
      // replace id
      var list = $('#' + cms.html.favoriteDropMenuId);
      var favbutton = $('button[name="Favorites"]');
      
      //unnecessary
      $('li.cms-item, button', list).css('display', 'none');
      
      //stays
      var left = 97;
      if ($.browser.msie) {
         left = 97;
      }
      list.appendTo('#toolbar_content').css({
         top: 35,
         left: left,
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
   
   
   
   var toggleList = cms.toolbar.toggleList = function(buttonElem, newMenu) {
      var button = $(buttonElem);
      var newMenuItems = $('#' + newMenu).find("ul").attr('id');
      if (button.hasClass('ui-state-active')) {
      
      
         //this line makes all elements vanish in IE
         $(cms.util.getContainerSelector() + ', ' + sortmenus).sortable('destroy');
         // TODO: find a better way to render stuff in IE
         if ($.browser.msie) {
            setTimeout(function() {
               $(cms.util.getContainerSelector()).hide().show();
            }, 0);
         }
         
         
         var hideMenus = function() {
            $(menus).hide();
         }
         $(menuHandles).remove();
         hideMenus();
         // HACK: sometimes hideMenus has no effect is executed synchronously
         window.setTimeout(hideMenus, 30);
         
         button.removeClass('ui-state-active');
         
      } else {
         cms.toolbar.currentMenu = newMenu;
         cms.toolbar.currentMenuItems = newMenuItems
         var loadFunction;
         if (newMenuItems == cms.html.favoriteListId) {
            //resetFavList();
            loadFunction = cms.data.loadFavorites;
         } else if (newMenuItems == cms.html.recentListId) {
            //resetRecentList();
            loadFunction = cms.data.loadRecent;
         } else if (newMenuItems == cms.html.searchListId) {
            loadFunction = cms.search.checkLastSearch;
         } else {
            // set a dummy load function that will immediately 
            // execute its callback
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
            if (newMenuItems == cms.html.favoriteListId) {
               resetFavList();
            }
            if (newMenuItems == cms.html.recentListId) {
               resetRecentList();
            }
            if (!button.hasClass("ui-state-active")) {
               return;
            }
            list = $('#' + cms.toolbar.currentMenu);
            $('.cms-head', list).each(function() {
               var elem = $(this);
               $('<a class="cms-handle cms-move"></a>').appendTo(elem);
            });
            var leftOffset = -209;
            if (true || buttonElem.name == 'Add') {
               leftOffset -= 36;
            }
            list.appendTo('#toolbar_content').css({
               /* position : 'fixed', */
               top: 35,
               left: $(buttonElem).position().left + leftOffset
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
            $(cms.util.getContainerSelector() + ', #' + cms.toolbar.currentMenuItems).sortable({
               // * current menu
               connectWith: cms.util.getContainerSelector() + ', #' + cms.toolbar.currentMenuItems,
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
                     // TODO: please comment this kind of code!!!
                     setTimeout(function() {
                        $(cms.data.sortitems).css('display', 'block');
                     }, 10);
                  }
               }
            });
         });
      }
   };
   
   var clickFavDeleteIcon = cms.toolbar.clickFavDeleteIcon = function() {
      var button = $(this);
      var toRemove = button.parent().parent();
      toRemove.remove();
   };
   
   var arrayToString = function(arr) {
      return "[" + arr.join(", ") + "]";
   };
   
   var saveFavorites = cms.toolbar.saveFavorites = function() {
      cms.toolbar.favorites.length = 0;
      $("#fav-dialog > ul > li.cms-item").each(function() {
         var resource_id = this.getAttribute("rel");
         cms.toolbar.favorites.push(resource_id);
      });
   };
   
   var favEditOK = cms.toolbar.favEditOK = function() {
      $(this).dialog("close");
      saveFavorites();
      cms.data.persistFavorites(function(ok) {
         if (!ok) {
                  // TODO
         }
      });
   }
   
   var favEditCancel = cms.toolbar.favEditCancel = function() {
      $(this).dialog("close");
   }
   
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
      //		$('#fav-dialog div.cms-additional div').jHelperTip( {
      //			trigger :'hover',
      //			source :'attribute',
      //			attrName :'alt',
      //			topOff :-30,
      //			opacity :0.8
      //		});
   };
   
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
   
   
   var resetFavList = cms.toolbar.resetFavList = function() {
      $("#" + cms.html.favoriteMenuId + " .cms-item-list > li.cms-item").remove();
      var $favlist = $("#" + cms.html.favoriteMenuId + " .cms-item-list");
      for (var i = 0; i < cms.toolbar.favorites.length; i++) {
         $favlist.append(cms.html.createItemFavListHtml(cms.toolbar.favorites[i]))
      }
   }
   
   
   
   var typesForNew = {
      'ttnews': 'News article',
      'ttevent': 'Event'
   }
   
   
   var createNewListItemHtml = function(type, name) {
      return '<div rel="' + type + '">' + name + '</div>';
   };
   
   
   var createNewListItemHtml = function(type, name) {
      var html = ['<li class="cms-item" rel="', type, '">\
		<div class="cms-left ui-widget-content">\
			<div class="cms-head ui-state-hover">\
				<div class="cms-navtext"><a class="cms-left ui-icon ui-icon-triangle-1-e"></a>', type, '</div>\
				<span class="cms-title">', name, '</span>\
				<span class="cms-file-icon"></span>\
				<a class="cms-move cms-handle"></a>\
			</div>\
		</div>\
		<br clear="all" />\
	</li>'];
      return html.join('');
   };
   
   
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
   
   var addToRecent = cms.toolbar.addToRecent = function(itemId) {
      cms.util.addUnique(cms.toolbar.recent, itemId, cms.toolbar.recentSize);
      cms.data.persistRecent(function(ok) {
         if (!ok) {
                  // TODO
         }
      });
   }
   
   
   //====================================================================================================
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
   
   
   
   var showSaveDialog = function() {
      if (!$(this).hasClass('cms-deactivated')) {
         $('button[name="Save"]').addClass('ui-state-active');
         $('#cms-save-dialog').dialog('open');
      }
   }
   
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
   
   var pageChanged = cms.toolbar.pageChanged = false;
   var setPageChanged = cms.toolbar.setPageChanged = function(newValue) {
      pageChanged = cms.toolbar.pageChanged = newValue;
      if (newValue) {
         $('#toolbar button[name="Save"], #toolbar button[name="Reset"]').removeClass('cms-deactivated');
      } else {
         $('#toolbar button[name="Save"], #toolbar button[name="Reset"]').addClass('cms-deactivated');
      }
   }
   
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
   
   //===========================================================================================================
   var resetRecentList = cms.toolbar.resetRecentList = function() {
      $("#" + cms.html.recentMenuId + " li.cms-item").remove();
      var $recentlist = $("#" + cms.html.recentListId);
      for (var i = 0; i < cms.toolbar.recent.length; i++) {
         $recentlist.append(cms.html.createItemFavListHtml(cms.toolbar.recent[i]));
      }
   };
   
})(cms);
