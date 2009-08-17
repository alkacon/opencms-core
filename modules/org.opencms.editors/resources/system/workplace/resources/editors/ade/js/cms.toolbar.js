(function(cms) {
   cms.toolbar.favorites = [];
   cms.toolbar.recent = [];
   cms.toolbar.recentSize = 10;
   var oldBodyMarginTop = 0;
   var menuIds = [cms.html.favoriteMenuId, cms.html.recentMenuId, 'cms-search'];
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
      var $item = $(this).parent();
      var $container = $item.parent();
      cms.move.hoverOut();
      addToRecent($item.attr('rel'));
      $(this).parent().remove();
      cms.move.updateContainer($container.attr('id'));
      setPageChanged(true);
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
			$(deleteitems).each(
					function() {
            var elem = $(this).css('position', 'relative');
						$('<a class="cms-handle cms-delete"></a>').appendTo(
								elem).hover( function() {
               cms.move.hoverIn(elem, 2)
            }, cms.move.hoverOut).click(deleteItem);
         });
         button.addClass('ui-state-active');
      }
   };
   
   var toggleEdit = cms.toolbar.toggleEdit = function() {
      var button = $(this);
      if (button.hasClass('ui-state-active')) {
         // disabling edit mode
         $('a.cms-edit').remove();
         button.removeClass('ui-state-active');
      } else {
         $('button.ui-state-active').trigger('click');
         // enabling edit mode
         $(sortitems).each(function() {
            var elem = $(this).css('position', 'relative');
                var elemId=elem.attr('rel');
                if (elemId && cms.data.elements[elemId]){
                    if (cms.data.elements[elemId].allowEdit && !cms.data.elements[elemId].locked){
                        $('<a class="cms-handle cms-edit"></a>')
                            .appendTo(elem)
                            .hover(function(){cms.move.hoverIn(elem, 2)}, cms.move.hoverOut)
                            .click(function() {
                                openEditDialog(elemId);
                            });
                    }else{
                    // Append edit-locked-handle
                    }
                }
            
         });
         button.addClass('ui-state-active');
      }
   };
   
    var openEditDialog = cms.toolbar.openEditDialog = function(elemId){
        if (elemId && cms.data.elements[elemId]) {
            if (cms.data.elements[elemId].allowEdit && !cms.data.elements[elemId].locked) {
                var dialogWidth=self.innerWidth ? self.innerWidth : self.document.body.clientWidth;
                dialogWidth = dialogWidth > 1360 ? 1360 : dialogWidth;
                var dialogHeight=self.innerHeight ? self.innerHeight : self.document.body.clientHeight;
                var iFrameHeight=dialogHeight - 126;
                var editorLink=cms.data.EDITOR_URL+'?resource='+cms.data.elements[elemId].file+'&amp;directedit=true&amp;elementlanguage='+cms.data.locale+'&amp;backlink='+cms.data.BACKLINK_URL+'&amp;redirect=true';
                var editorFrame='<iframe style="border:none; width:100%; height:'+iFrameHeight+'px;" name="cmsAdvancedDirectEditor" src="'+editorLink+'"></iframe>';
                var editorDialog=$('#cms-editor');
                if (!editorDialog.length){
                    editorDialog=$('<div id="cms-editor"  rel="'+elemId+'"></div>').appendTo(document.body);
                }else{
                    editorDialog.empty().attr('rel', elemId);
                }
                
                
                editorDialog.append('<div class="cms-editor-subtitle">Resource: '+cms.data.elements[elemId].file+'</div>').append(editorFrame);
                editorDialog.dialog( {
    			width :dialogWidth-50,
                height : dialogHeight - 60,
    			title :"Editor",
    			modal :true,
    			autoOpen :true,
                closeOnEscape : false,
    			draggable :true,
    			resizable :true,
                resize : function(event, ui){
                    $('#cms-editor iframe').height(ui.size.height-70);
                },
                resizeStop : function(event, ui){
                    $('#cms-editor iframe').height(ui.size.height-70);
                },
    			position : [ 'center', -20 ],
                open : function(event, ui){
                    $('a.ui-dialog-titlebar-close').hide();
                },
    			close : function() {
    				$('#toolbar button.ui-state-active[name!="Edit"]').trigger('click');
                    editorDialog.empty().dialog('destroy');
    			},
    			zIndex :10000
    		});
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
   
      
   var addToolbar = cms.toolbar.addToolbar = function() {
      $(window).unload(onUnload);
      initSaveDialog();
      var bodyEl = $(document.body).css('position', 'relative');
      oldBodyMarginTop = bodyEl.offset().top;
      var offsetLeft = bodyEl.offset().left;
      bodyEl.append(cms.html.toolbar);
      bodyEl.append(cms.html.createMenu(cms.html.favoriteMenuId));
      bodyEl.append(cms.html.favoriteDialog);
      
      bodyEl.append(cms.html.createMenu(cms.html.recentMenuId));
		resetFavList();
      bodyEl.append('<button id="show-button" title="toggle toolbar" class="ui-state-default ui-corner-all"><span class="ui-icon cms-icon-logo"/></button>');
      $('#show-button').click(toggleToolbar);
      $('button[name="Edit"]').click(toggleEdit);
      $('button[name="Move"]').click(toggleMove);
      $('button[name="Delete"]').click(toggleDelete);
      $('button[name="Publish"]').click(showPublishList);
      $('button[name="Favorites"]').click(function() {
         toggleList(this, cms.html.favoriteMenuId);
      });
      
      $('button[name="Save"]').click(showSaveDialog).hide();
      
      $('button[name="Recent"]').click(function() {
         toggleList(this, cms.html.recentMenuId);
      });
      $('#toolbar button, #show-button').mouseover(function() {
         $(this).addClass('ui-state-hover');
      }).mouseout(function() {
         $(this).removeClass('ui-state-hover');
      });
      bodyEl.animate({
         marginTop: oldBodyMarginTop + 34 + 'px'
      }, 200);
      
      initFavDialog();
      
   };
   
   var toggleMove = cms.toolbar.toggleMove = function(el) {
      var button = $(this);
      var containerSelector=cms.util.getContainerSelector();
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
         $(containerSelector).css('position', 'relative').children('.cms-element:visible').each(
			function() {
                var elem = $(this).css('position', 'relative');
                        if (elem.hasClass('cms-subcontainer') && (/left|right/).test(elem.css('float'))) {
                            var pos = cms.util.getElementPosition(elem);
                            var dimensions = cms.util.getInnerDimensions(elem, 1);
                            $('<a class="cms-handle cms-move"></a>')
                                .appendTo(elem).hover(function() {
                                    cms.move.hoverInner(elem, 2, false);
                                }, cms.move.hoverOut)
                                .mousedown(cms.move.movePreparation)
                                .mouseup(cms.move.moveEnd)
                                .css('left', dimensions.left - pos.left + dimensions.width - 20);
                        }else{
                            $('<a class="cms-handle cms-move"></a>').appendTo(elem)
								.hover( function() {
                                    if (elem.hasClass('cms-subcontainer')) {
                                        cms.move.hoverInner(elem, 2, false);
                                    } else {
                                        cms.move.hoverIn(elem, 2);
                                    }
                                }, cms.move.hoverOut).mousedown(cms.move.movePreparation)
								.mouseup(cms.move.moveEnd);
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
			$('#'+cms.html.favoriteListId).css('height', '40px');
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
				items :'.cms-element',
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
      
         $(cms.util.getContainerSelector() + ', ' + sortmenus).sortable('destroy');
         $(menuHandles).remove();
         $(menus).hide();
         button.removeClass('ui-state-active');
      } else {
         cms.toolbar.currentMenu = newMenu;
         cms.toolbar.currentMenuItems = newMenuItems
         var loadFunction;
         if (newMenuItems == cms.html.favoriteListId) {
             resetFavList();
             loadFunction = cms.data.loadFavorites;
         } else
         if (newMenuItems == cms.html.recentListId) {
             resetRecentList();
             loadFunction = cms.data.loadRecent;
         } else { alert("Invalid list id!");}
         
         $('button.ui-state-active').trigger('click');
         button.addClass('ui-state-active');
         // enabling move-mode
         // * current menu
         loadFunction(function() {
            if (!button.hasClass("ui-state-active")) return;
         list = $('#' + cms.toolbar.currentMenu);
         $('.cms-head', list).each(function() {
            var elem = $(this);
            $('<a class="cms-handle cms-move"></a>').appendTo(elem);
         });
         list.appendTo('#toolbar_content').css({
            /* position : 'fixed', */
            top: 35,
            left: $(buttonElem).position().left - 217
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
         $(cms.util.getContainerSelector()).children('*:visible').css('position', 'relative');
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
            items: sortitems + ', li.cms-item',
            revert: 100,
            deactivate: function(event, ui) {
               $('a.cms-move', $(this)).removeClass('cms-trigger');
               if ($.browser.msie) {
                  setTimeout("$(sortitems).css('display','block')", 10);
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
      cms.data.persistFavorites();
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
      $("#fav-dialog li").show(); // Make "deleted" items show up again
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
         $favlist.append(cms.html.createItemFavListHtml(cms.data.elements[cms.toolbar.favorites[i]]))
      }
      // $("#"+cms.html.favoriteMenuId+" a.ui-icon").click(function() {clickTriangle(this)});
   }
   
   var addToRecent = cms.toolbar.addToRecent = function(itemId) {
      cms.util.addUnique(cms.toolbar.recent, itemId, cms.toolbar.recentSize);
      cms.data.persistRecent();
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
               $(this).dialog('close');
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
      $('button[name="Save"]').addClass('ui-state-active');
      $('#cms-save-dialog').dialog('open');
   }
   
   var savePage = cms.toolbar.savePage = function() {
       //cms.data.saveContainers(...) 
       setPageChanged(false);
   }
   
   var pageChanged = cms.toolbar.pageChanged = false;
   var setPageChanged = cms.toolbar.setPageChanged = function(newValue) {
       pageChanged = cms.toolbar.pageChanged = true;
       if (newValue) {
           $('button[name="Save"]').show();
       } else {
           $('button[name="Save"]').hide();
       }    
   }
   
   var onUnload = cms.toolbar.onUnload = function() {
       if (cms.toolbar.pageChanged) {
           var saveChanges = window.confirm("Do you want to save your changes made on " +window.location.href + "?\n (Cancel will discard changes)");
           if (saveChanges) {
               cms.toolbar.savePage();
               //alert("Changes saved.")
           }
       }
   }
   
   //==================================================================================================================
   var resetRecentList = cms.toolbar.resetRecentList = function() {
      $("#" + cms.html.recentMenuId + " li.cms-item").remove();
      var $recentlist = $("#" + cms.html.recentListId);
      for (var i = 0; i < cms.toolbar.recent.length; i++) {
         $recentlist.append(cms.html.createItemFavListHtml(cms.data.elements[cms.toolbar.recent[i]]));
      }
   };
   
})(cms);
