(function(cms) {
   var $ = jQuery;
   cms.toolbar.favorites = [];
   cms.toolbar.recent = [];
   cms.toolbar.recentSize = 10;
   var oldBodyMarginTop = 0;
   var menuIds = [cms.html.favoriteMenuId, cms.html.recentMenuId, cms.html.newMenuId, cms.html.searchMenuId];
   var sortmenus = cms.util.makeCombinedSelector(menuIds, "#% ul");
   var menuHandles = cms.util.makeCombinedSelector(menuIds, "#% a.cms-move")
   var menus = cms.util.makeCombinedSelector(menuIds, "#%");
   cms.toolbar.currentMenu = cms.html.favoriteMenuId;
   cms.toolbar.currentMenuItems = cms.html.favoriteListId;
   
   
   
   
   var searchLoadingSign = null;
   
   
   
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
      timer.id = setTimeout("cms.toolbar.showAddButtons()", 1000);
      timer.handleDiv = handleDiv;
      timer.adeMode = adeMode;
   }
   
   var showAddButtons = cms.toolbar.showAddButtons = function() {
      timer.id = null;
      var right = '-48px';
      if ($.browser.msie) {
         right = '0px';
      }
      timer.handleDiv.addClass('ui-widget-header').css({
         'width': '72px',
         'right': right
      }).children().css('display', 'block').addClass('ui-corner-all ui-state-default');
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
   
   var addHandles = cms.toolbar.addHandles = function(elem, elemId, adeMode, isMoving) {
      var handleDiv = $('<div class="cms-handle"></div>').appendTo(elem);
      
      var handles = {
         'edit': cms.data.elements[elemId].allowEdit ? $('<a class="cms-edit cms-edit-enabled"></a>').click(openEditDialog) : $('<a class="cms-edit cms-edit-locked" title="locked by ' + cms.data.elements[elemId].locked + '" onclick="return false;"></a>'),
         'move': $('<a class="cms-move"></a>').mousedown(cms.move.movePreparation).mouseup(cms.move.moveEnd),
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
         handleDiv.hover(function() {
            cms.move.hoverIn(elem, 2);
            startHoverTimeout(handleDiv, adeMode);
         }, function() {
            stopHover();
         });
      }
      handleDiv.css({
         'right': '0px',
         'width': '24px'
      });
   }
   
   var toggleMode = cms.toolbar.toggleMode = function() {
      var button = $(this);
      var adeMode = button.attr('name').toLowerCase();
      if (button.hasClass('ui-state-active')) {
         // disabling edit mode
         destroyMove();
         $('.cms-element div.cms-handle').remove();
         button.removeClass('ui-state-active');
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
      var elemId = $(this).closest('.cms-element').attr('rel');
      if (elemId && cms.data.elements[elemId]) {
         if (cms.data.elements[elemId].allowEdit) {
            var element = cms.data.elements[elemId];
            var _openDialog = function(path, id, afterClose) {
               var dialogWidth = self.innerWidth ? self.innerWidth : self.document.body.clientWidth;
               dialogWidth = dialogWidth > 1360 ? 1360 : dialogWidth;
               var dialogHeight = self.innerHeight ? self.innerHeight : self.document.body.clientHeight;
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
                  resize: function(event, ui) {
                     $('#cms-editor iframe').height(ui.size.height - 70);
                  },
                  resizeStop: function(event, ui) {
                     $('#cms-editor iframe').height(ui.size.height - 70);
                  },
                  position: ['center', -20],
                  open: function(event, ui) {
                     $('#cms_appendbox').css('z-index', 10005).append(editorDialog.parent());
                     $('a.ui-dialog-titlebar-close').hide();
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
   var LoadingSign = function(selector, waitTime, showLoading, hideLoading) {
   
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
   
   var saveSearchInput = cms.data.saveSearchInput = function(/**Object*/searchParams) {
   
      var/**JQuery*/ $context = $('#' + cms.html.searchDialogId);
      searchParams.query = $('input.cms-search-query', $context).val();
      searchParams.types = [];
      $('input.cms-search-type', $context).each(function() {
         var /**JQuery*/ $checkbox = $(this);
         searchParams.types.push({
            'name': $checkbox.attr('value'),
            'checked': $checkbox.is(":checked")
         });
      });
      searchParams.path = $('input.cms-search-path', $context).val();
   };
   
   var restoreSearchInput = cms.data.restoreSearchInput = function() {
   
      var/**JQuery*/ $context = $('#' + cms.html.searchDialogId);
      if (cms.data.searchParams.query) {
         $('input.cms-search-query', $context).val(cms.data.searchParams.query);
      }
      if (cms.data.searchParams.types) {
         $.each(cms.data.searchParams.types, function() {
            $('#' + cms.html.searchTypePrefix + this.name).attr('checked', this.checked);
         });
      }
      if (cms.data.searchParams.path) {
         $('input.cms-search-path', $context).val(cms.data.searchParams.path);
      }
   };
   
   var addToolbar = cms.toolbar.addToolbar = function() {
   
      $(window).unload(onUnload);
      initSaveDialog();
      var bodyEl = $(document.body).css('position', 'relative');
      oldBodyMarginTop = bodyEl.offset().top;
      var offsetLeft = bodyEl.offset().left;
      bodyEl.append(cms.html.toolbar);
      bodyEl.append(cms.html.createMenu(cms.html.favoriteMenuId));
      bodyEl.append(cms.html.createMenu(cms.html.newMenuId));
      bodyEl.append(cms.html.searchMenu);
      searchLoadingSign = cms.toolbar.searchLoadingSign = new LoadingSign(".cms-loading", 500, showLoading, hideLoading);
      $('#' + cms.html.searchMenuId).find('button.cms-search-button').click(function() {
         if ($('#cms-search-dialog').length < 1) {
            initSearch();
            this.click();
         }
      });
      bodyEl.append(cms.html.favoriteDialog);
      bodyEl.append('<div id="cms_appendbox"></div>');
      bodyEl.append(cms.html.createMenu(cms.html.recentMenuId));
      resetFavList();
      bodyEl.append('<button id="show-button" title="toggle toolbar" class="ui-state-default ui-corner-all"><span class="ui-icon cms-icon-logo"/></button>');
      $('#show-button').click(toggleToolbar);
      $('#toolbar button[name="Edit"], #toolbar button[name="Move"], #toolbar button[name="Delete"]').click(toggleMode);
      //      $('#toolbar button[name="Move"]').click(toggleMode);
      //      $('#toolbar button[name="Delete"]').click(toggleMode);
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
   };
   
   /**
    * Show the 'LOADING' text for the search menu.
    */
   var showLoading = function() {
      $('.cms-loading').text('LOADING');
   }
   
   /**
    * Returns the number of search results currently in the search menu.
    */
   var getNumberOfSearchResults = function() {
      return $('#cms-search-list').children().size();
   }
   
   /**
    * Hide the LOADING text for the search menu and display the number of search results loaded instead.
    */
   var hideLoading = function() {
      $('.cms-loading').text(getNumberOfSearchResults() + " results loaded");
   }
   
   /**
    * Initialize everything needed for the search.
    */
   var initSearch = cms.toolbar.initSearch = function() {
      var bodyEl = $(document.body);
      // scroll bar may not be at the top after reloading the page,  
      // which could cause multiple search result pages to be reloaded 
      $('.cms-scrolling').scrollTop(0);
      
      bodyEl.append(cms.html.searchDialog(cms.data.newTypes));
      
      $('#cms-search-dialog').dialog({
         autoOpen: false,
         modal: true,
         zIndex: 99999,
         title: 'Search',
         buttons: {
            'Search': function() {
               if (!cms.util.validateForm($('form', $('#cms-search-dialog')))) {
                  return;
               }
               $(this).dialog('close');
               var /**Object*/ sp = {};
               saveSearchInput(sp);
               cms.data.startNewSearch(sp.query, sp.types, sp.path);
               saveSearchInput(cms.data.searchParams);
            },
            'Cancel': function() {
               $(this).dialog('close');
               $('form span.error', $('#cms-search-dialog')).remove();
            }
         }
      });
      $('input', $('#cms-search-dialog')).customInput();
      
      $(".cms-search-tree").click(function() {
         window.open(cms.data.TREE_URL, '_blank', 'menubar=no');
      })
      
      $(".cms-search-button").click(function() {
         restoreSearchInput();
         $('#cms-search-dialog').dialog('open');
         
      });
      
      $(document).bind('cms-data-loaded', function() {
      
         loading = searchLoadingSign;
         var i = 0;
         var $inner = $("#cms-search-list");
         var $scrolling = $(".cms-scrolling");
         
         $scrolling.scroll(function() {
         
            var delta = $inner.height() - $scrolling.height() - $scrolling.scrollTop();
            if (delta < 64 && !loading.isLoading && cms.data.searchParams.hasMore) {
               cms.data.continueSearch();
               loading.start();
            }
         });
      });
   };
   
   var destroyMove = function() {
   
      var containerSelector = cms.util.getContainerSelector();
      $(containerSelector + ', #' + cms.html.favoriteListId).sortable('destroy');
      var list = $('#' + cms.html.favoriteMenuId);
      $('li.cms-item, button', list).css('display', 'block');
      list.css('display', 'none');
      list.get(0).style.visibility = '';
      $('#' + cms.html.favoriteListId).get(0).style.height = '';
      resetFavList();
      //      $('a.cms-move').remove();
   };
   
   var initMove = function() {
   
      var containerSelector = cms.util.getContainerSelector();
      var list = $('#' + cms.html.favoriteMenuId);
      var favbutton = $('button[name="Favorites"]');
      $('li.cms-item, button', list).css('display', 'none');
      list.appendTo('#toolbar_content').css({
         top: 35,
         left: favbutton.position().left - 217,
         display: 'block',
         visibility: 'hidden'
      });
      $('#' + cms.html.favoriteListId).css('height', '40px');
      $('div.ui-widget-shadow', list).css({
         top: 0,
         left: -4,
         width: list.outerWidth() + 8,
         height: list.outerHeight() + 2,
         border: '0px solid',
         opacity: 0.6
      });
      
      $(containerSelector).css('position', 'relative');
      $(containerSelector + ', #' + cms.html.favoriteListId).sortable({
         connectWith: containerSelector + ', #' + cms.html.favoriteListId,
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
         deactivate: function(event, ui) {
            $('#' + cms.html.favoriteListId + ' li').hide(200);
            $('#' + cms.html.favoriteMenuId).css('visibility', 'hidden');
            $('.cms-handle').show();
            if ($.browser.msie) {
               setTimeout("$('.cms-element').css('display','block')", 50);
            }
         }
      });
   }
   
   var toggleMove = cms.toolbar.toggleMove = function(el) {
      var button = $(this);
      var containerSelector = cms.util.getContainerSelector();
      if (button.hasClass('ui-state-active')) {
         // disabling move-mode
         $(containerSelector + ', #' + cms.html.favoriteListId).sortable('destroy');
         var list = $('#' + cms.html.favoriteMenuId);
         $('li.cms-item, button', list).css('display', 'block');
         list.css('display', 'none');
         list.get(0).style.visibility = '';
         $('#' + cms.html.favoriteListId).get(0).style.height = '';
         resetFavList();
         $('a.cms-move').remove();
         button.removeClass('ui-state-active');
      } else {
         $('button.ui-state-active').trigger('click');
         // enabling move mode
         $(containerSelector).children('.cms-element:visible').each(function() {
            var elem = $(this).css('position', 'relative');
            if (elem.hasClass('cms-subcontainer') && (/left|right/).test(elem.css('float'))) {
               var pos = cms.util.getElementPosition(elem);
               var dimensions = cms.util.getInnerDimensions(elem, 1);
               $('<a class="cms-handle cms-move"></a>').appendTo(elem).hover(function() {
                  cms.move.hoverInner(elem, 2, false);
               }, cms.move.hoverOut).mousedown(cms.move.movePreparation).mouseup(cms.move.moveEnd).css('left', dimensions.left - pos.left + dimensions.width - 20);
            } else {
               $('<a class="cms-handle cms-move"></a>').appendTo(elem).hover(function() {
                  if (elem.hasClass('cms-subcontainer')) {
                     cms.move.hoverInner(elem, 2, false);
                  } else {
                     cms.move.hoverIn(elem, 2);
                  }
               }, cms.move.hoverOut).mousedown(cms.move.movePreparation).mouseup(cms.move.moveEnd);
            }
            
         });
         
         var list = $('#' + cms.html.favoriteMenuId);
         var favbutton = $('button[name="Favorites"]');
         $('li.cms-item, button', list).css('display', 'none');
         list.appendTo('#toolbar_content').css({
            top: 35,
            left: favbutton.position().left - 217,
            display: 'block',
            visibility: 'hidden'
         });
         $('#' + cms.html.favoriteListId).css('height', '40px');
         $('div.ui-widget-shadow', list).css({
            top: 0,
            left: -4,
            width: list.outerWidth() + 8,
            height: list.outerHeight() + 2,
            border: '0px solid',
            opacity: 0.6
         });
         
         $(containerSelector).children('.cms-element:visible').css('position', 'relative');
         $(containerSelector + ', #' + cms.html.favoriteListId).sortable({
            connectWith: containerSelector + ', #' + cms.html.favoriteListId,
            placeholder: 'cms-placeholder',
            dropOnEmpty: true,
            start: cms.move.startAdd,
            beforeStop: cms.move.beforeStopFunction,
            over: cms.move.overAdd,
            out: cms.move.outAdd,
            tolerance: 'pointer',
            opacity: 0.7,
            stop: cms.move.stopAdd,
            cursorAt: {
               right: 10,
               top: 10
            },
            zIndex: 20000,
            handle: 'a.cms-move',
            items: '.cms-element',
            revert: true,
            deactivate: function(event, ui) {
               $('#' + cms.html.favoriteListId + ' li').hide(200);
               $('#' + cms.html.favoriteMenuId).css('visibility', 'hidden');
               $('a.cms-move').show();
               if ($.browser.msie) {
                  setTimeout("$('.cms-element').css('display','block')", 10);
               }
            }
         });
         // list.css('display', 'none');
         
         button.addClass('ui-state-active');
      }
   };
   
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
         
         
         
         $(menuHandles).remove();
         $(menus).hide();
         
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
            loadFunction = cms.data.checkLastSearch;
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
            var leftOffset = -217;
            if (buttonElem.name == 'Add') {
               leftOffset -= 36;
            }
            list.appendTo('#toolbar_content').css({
               /* position : 'fixed', */
               top: 35,
               left: $(buttonElem).position().left + leftOffset
            }).slideDown(100, function() {
               $('div.ui-widget-shadow', list).css({
                  top: 0,
                  left: -4,
                  width: list.outerWidth() + 8,
                  height: list.outerHeight() + 2,
                  border: '0px solid',
                  opacity: 0.6
               });
            });
            $(cms.util.getContainerSelector()).css('position', 'relative').children('*:visible').css('position', 'relative');
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
               opacity: 0.7,
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
      $("#fav-dialog li.cms-item").each(function() {
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
         width: 340,
         // height: 500,
         title: "Edit favorites",
         modal: true,
         autoOpen: false,
         draggable: true,
         resizable: false,
         position: ['center', 20],
         close: function() {
            $('#fav-edit').removeClass('ui-state-active');
         },
         buttons: buttons,
         zIndex: 10000
      });
      
   };
   
   var initFavDialogItems = cms.toolbar.initFavDialogItems = function() {
      $("#fav-dialog ul").empty();
      //$("#fav-dialog").append("<ul></ul>")
      var html = []
      for (var i = 0; i < cms.toolbar.favorites.length; i++) {
         html.push(cms.html.createItemFavDialogHtml(cms.data.elements[cms.toolbar.favorites[i]]));
      }
      $("#fav-dialog ul").append(html.join(''));
      $("#fav-dialog .cms-delete-icon").click(clickFavDeleteIcon);
      $("#fav-dialog ul").sortable();
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
      if (elem.hasClass('ui-icon-triangle-1-e')) {
         elem.removeClass('ui-icon-triangle-1-e').addClass('ui-icon-triangle-1-s');
         elem.parents('.ui-widget-content').children('.cms-additional').show(5, function() {
            var list = $(this).parents('div.cms-menu');
            $('div.ui-widget-shadow', list).css({
               height: list.outerHeight() + 2
            });
         });
      } else {
         elem.removeClass('ui-icon-triangle-1-s').addClass('ui-icon-triangle-1-e');
         elem.parents('.ui-widget-content').children('.cms-additional').hide(5, function() {
            var list = $(this).parents('div.cms-menu');
            $('div.ui-widget-shadow', list).css({
               height: list.outerHeight() + 2
            });
         });
      }
      return false;
   };
   
   var resetFavList = cms.toolbar.resetFavList = function() {
      $("#" + cms.html.favoriteMenuId + " li.cms-item").remove();
      var $favlist = $("#" + cms.html.favoriteMenuId + " ul");
      for (var i = 0; i < cms.toolbar.favorites.length; i++) {
         $favlist.append(cms.html.createItemFavListHtml(cms.toolbar.favorites[i]))
      }
      // $("#"+cms.html.favoriteMenuId+" a.ui-icon").click(function() {clickTriangle(this)});
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
   
   var savePage = cms.toolbar.savePage = function() {
   
      cms.data.persistContainers(function(ok) {
         $('#cms-save-dialog').dialog('close');
         if (ok) {
            setPageChanged(false);
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
            if (newElems.length() > 0) {
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
   
   //===========================================================================================================
   var resetRecentList = cms.toolbar.resetRecentList = function() {
      $("#" + cms.html.recentMenuId + " li.cms-item").remove();
      var $recentlist = $("#" + cms.html.recentListId);
      for (var i = 0; i < cms.toolbar.recent.length; i++) {
         $recentlist.append(cms.html.createItemFavListHtml(cms.toolbar.recent[i]));
      }
   };
   
})(cms);
