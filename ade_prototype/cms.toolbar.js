(function(cms) {
	cms.toolbar.favorites = ['item_010', 'item_011', 'item_012'];
    cms.toolbar.recent = [];
    cms.toolbar.recentSize = 3;
    var oldBodyMarginTop = 0;
    
    var sortmenus = "#"+cms.html.favoriteMenuId+" ul, #"+cms.html.recentMenuId+" ul"
    var menuHandles = "#"+cms.html.favoriteMenuId+" a.cms-move, #"+cms.html.recentMenuId+" a.cms-move"
    var menus = "#"+cms.html.favoriteMenuId+", #"+cms.html.recentMenuId;
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
            
            $('#'+cms.html.publishDialogId).dialog( {
    			buttons : {
    				"Cancel" : function() {
    					$(this).dialog("close");
    				},
    				"Publish" : function() {
    					$(this).dialog("close");
    				}
    			},
    			width :340,
    			title :"Publish",
    			modal :true,
    			autoOpen :true,
    			draggable :true,
    			resizable :false,
    			position : [ 'center', 20 ],
    			close : function() {
    				$('button[name="Publish"]').removeClass('ui-state-active');
                    $('#'+cms.html.publishDialogId).dialog('destroy');
    			},
    			zIndex :10000
    		});
    		$('#'+cms.html.publishDialogId+' span.cms-check-icon').click( function() {
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
    
    var toggleEdit = cms.toolbar.toggleEdit = function(){
    	var button=$(this);
    	if (button.hasClass('ui-state-active')){
    		// disabling edit mode
    		$('a.cms-edit').remove();
    		button.removeClass('ui-state-active');
    	}else{
    		$('button.ui-state-active').trigger('click');
    		// enabling edit mode
    		$(sortitems).each(function(){
    			var elem=$(this).css('position', 'relative');
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
                var editorLink='http://localhost:8080/opencms/opencms/system/workplace/editors/editor.jsp?resource=/demo_en/dictionary/weisswurst.html&amp;directedit=true&amp;elementlanguage=en&amp;backlink=http://127.0.0.1:8100/OpenCms/ade_prototype/closeDialog.html&amp;redirect=true';
                var editorFrame='<iframe width="99%" height="99%" name="cmsAdvancedDirectEditor" src="'+editorLink+'"></iframe>';
                var editorDialog=$('#cms-editor')
                if (!editorDialog.lenght){
                    editorDialog=$('<div id="cms-editor"></div>').appendTo(document.body);
                }else{
                    editorDialog.empty();
                }
                editorDialog.append(editorFrame);
                editorDialog.dialog( {
    			width :900,
                height : 600,
    			title :"Editor",
    			modal :true,
    			autoOpen :true,
    			draggable :true,
    			resizable :true,
    			position : [ 'center', 20 ],
    			close : function() {
    				$('button[name="Edit"]').removeClass('ui-state-active');
                    editorDialog.empty().dialog('destroy');
    			},
    			zIndex :10000
    		});
            }
        }
    }

	var removeToolbar = cms.toolbar.removeToolbar =  function() {
		$('#toolbar').remove();
		$(document.body).css('margin-top', oldBodyMarginTop + 'px');
	};

	var hideToolbar = cms.toolbar.hideToolbar = function() {
		$(document.body).animate( {
			marginTop :oldBodyMarginTop + 'px'
		}, 200, 'swing', function() {
			$('#show-button').show(50);
		});

		return false;
	};

	var toggleToolbar = cms.toolbar.toggleToolbar = function() {
		var button = $('#show-button');
		if (button.hasClass('toolbar_hidden')) {
			$('#toolbar').fadeIn(100);
			$(document.body).animate( {
				marginTop :oldBodyMarginTop + 34 + 'px'
			}, 200, 'swing');
			button.removeClass('toolbar_hidden');
		} else {
			$(document.body).animate( {
				marginTop :oldBodyMarginTop + 'px'
			}, 200, 'swing');
			$('#toolbar').fadeOut(100);
			button.addClass('toolbar_hidden');
		}
		return false;
	};

	
	var addToolbar = cms.toolbar.addToolbar = function() {
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
		$('button[name="Favorites"]').click( function() {
			toggleList(this, cms.html.favoriteMenuId);
		});
		$('button[name="Recent"]').click( function() {
			toggleList(this, cms.html.recentMenuId);
		});
		$('#toolbar button, #show-button').mouseover( function() {
			$(this).addClass('ui-state-hover');
		}).mouseout( function() {
			$(this).removeClass('ui-state-hover');
		});
		bodyEl.animate( {
			marginTop :oldBodyMarginTop + 34 + 'px'
		}, 200);
		
		initFavDialog();
        
        
        $('button[name="Save"]').click(function(){
            $(sortlist).filter(':has(div.cms-subcontainer-start)').each(function(){
                var contElem=$(this);
                contElem.children('div.cms-subcontainer-start').each(function(){
                    var subCont=$('<div class="cms-subcontainer"></div>');
                    var contStartElem=$(this);
                    subCont.attr('rel', contStartElem.attr('rel'));
                    var currentElem=contStartElem.next();
                    while (currentElem && !currentElem.hasClass('cms-subcontainer-end')){
                        subCont.append(currentElem.clone());
                        currentElem.css('display','none')
                        currentElem=currentElem.next();
                    }
                    subCont.insertBefore(contStartElem);
                });
            });
        });
	};

	var toggleMove = cms.toolbar.toggleMove = function(el) {
		var button = $(this);

		if (button.hasClass('ui-state-active')) {
			// disabling move-mode
			$(sortlist + ', #'+cms.html.favoriteListId).sortable('destroy');
			var list = $('#'+cms.html.favoriteMenuId);
			$('li.cms-item, button', list).css('display', 'block');
			list.css('display', 'none');
			list.get(0).style.visibility = '';
			$('#'+cms.html.favoriteListId).get(0).style.height = '';
			resetFavList();
			$('a.cms-move').remove();
			button.removeClass('ui-state-active');
		} else {
			$('button.ui-state-active').trigger('click');
			// enabling move mode
			$(sortlist).children('*:visible').each(
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

			var list = $('#'+cms.html.favoriteMenuId);
			var favbutton = $('button[name="Favorites"]');
			$('li.cms-item, button', list).css('display', 'none');
			list.appendTo('#toolbar_content').css( {
				top :35,
				left :favbutton.position().left - 217,
				display :'block',
				visibility :'hidden'
			});
			$('#'+cms.html.favoriteListId).css('height', '40px');
			$('div.ui-widget-shadow', list).css( {
				top :0,
				left :-4,
				width :list.outerWidth() + 8,
				height :list.outerHeight() + 2,
				border :'0px solid',
				opacity :0.6
			});

			$(sortlist).children('*:visible').css('position', 'relative');
			$(sortlist + ', #'+cms.html.favoriteListId).sortable( {
				connectWith :sortlist + ', #'+cms.html.favoriteListId,
				placeholder :'cms-placeholder',
				dropOnEmpty :true,
				start :cms.move.startAdd,
				beforeStop :cms.move.beforeStopFunction,
				over :cms.move.overAdd,
				out :cms.move.outAdd,
				tolerance :'pointer',
				opacity :0.7,
				stop :cms.move.stopAdd,
				cursorAt : {
					right :10,
					top :10
				},
				zIndex :20000,
				handle :'a.cms-move',
				items :sortitems+', div.cms-subcontainer',
				revert :true,
				deactivate : function(event, ui) {
					$('#'+cms.html.favoriteListId+' li').hide(200);
					$('#'+cms.html.favoriteMenuId).css('visibility', 'hidden');
                    $('a.cms-move').show();
					if ($.browser.msie) {
						setTimeout("$(sortitems).css('display','block')", 10);
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

			$(sortlist + ', ' + sortmenus).sortable('destroy');
			$(menuHandles).remove();
			$(menus).hide();
			button.removeClass('ui-state-active');
		} else {
			resetRecentList();
			cms.toolbar.currentMenu = newMenu;
			cms.toolbar.currentMenuItems = newMenuItems
			$('button.ui-state-active').trigger('click');

			// enabling move-mode
			// * current menu
			list = $('#' + cms.toolbar.currentMenu);
			$('.cms-head', list).each( function() {
				var elem = $(this);
				$('<a class="cms-handle cms-move"></a>').appendTo(elem);
			});
			list.appendTo('#toolbar_content').css( {
				/* position : 'fixed', */
				top :35,
				left :$(buttonElem).position().left - 217
			}).slideDown(100, function() {
				$('div.ui-widget-shadow', list).css( {
					top :0,
					left :-4,
					width :list.outerWidth() + 8,
					height :list.outerHeight() + 2,
					border :'0px solid',
					opacity :0.6
				});
			});
			$(sortlist).children('*:visible').css('position', 'relative');
			// * current menu
			$(sortlist + ', #' + cms.toolbar.currentMenuItems).sortable( {
				// * current menu
				connectWith :sortlist + ', #' + cms.toolbar.currentMenuItems,
				placeholder :'placeholder',
				dropOnEmpty :true,
				start :cms.move.startAdd,
				beforeStop :cms.move.beforeStopFunction,
				over :cms.move.overAdd,
				out :cms.move.outAdd,
				tolerance :'pointer',
				opacity :0.7,
				stop :cms.move.stopAdd,
				cursorAt : {
					right :15,
					top :10
				},
				handle :'a.cms-move',
				items :sortitems + ', li.cms-item',
				revert :100,
				deactivate : function(event, ui) {
					$('a.cms-move', $(this)).removeClass('cms-trigger');
					if ($.browser.msie) {
						setTimeout("$(sortitems).css('display','block')", 10);
					}
				}
			});
			button.addClass('ui-state-active');
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
		var newFavs = [];
		$("#fav-dialog li.cms-item").each( function() {
			var resource_id = this.getAttribute("rel");
			newFavs.push(resource_id);
		});
		cms.toolbar.favorites = newFavs;
		resetFavList();

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
			"Cancel" :favEditCancel,
			"OK" :favEditOK

		};
		$('#fav-dialog').dialog( {
			width :340,
			// height: 500,
			title :"Edit favorites",
			modal :true,
			autoOpen :false,
			draggable :true,
			resizable :false,
			position : [ 'center', 20 ],
			close : function() {
				$('#fav-edit').removeClass('ui-state-active');
			},
			buttons :buttons,
			zIndex :10000
		});

	};

	var initFavDialogItems = cms.toolbar.initFavDialogItems = function() {
		$("#fav-dialog ul").empty();
		//$("#fav-dialog").append("<ul></ul>")
		var html = []
		for ( var i = 0; i < cms.toolbar.favorites.length; i++) {
			html
					.push(cms.html
							.createItemFavDialogHtml(cms.data.elements[cms.toolbar.favorites[i]]));
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
			elem.removeClass('ui-icon-triangle-1-e').addClass(
					'ui-icon-triangle-1-s');
			elem.parents('.ui-widget-content').children('.cms-additional')
					.show(5, function() {
						var list = $(this).parents('div.cms-menu');
						$('div.ui-widget-shadow', list).css( {
							height :list.outerHeight() + 2
						});
					});
		} else {
			elem.removeClass('ui-icon-triangle-1-s').addClass(
					'ui-icon-triangle-1-e');
			elem.parents('.ui-widget-content').children('.cms-additional')
					.hide(5, function() {
						var list = $(this).parents('div.cms-menu');
						$('div.ui-widget-shadow', list).css( {
							height :list.outerHeight() + 2
						});
					});
		}
		return false;
	};

	var resetFavList = cms.toolbar.resetFavList = function() {
		$("#"+cms.html.favoriteMenuId+" li.cms-item").remove();
		var $favlist = $("#"+cms.html.favoriteMenuId+" ul");
		for ( var i = 0; i < cms.toolbar.favorites.length; i++) {
			$favlist
					.append(cms.html
							.createItemFavListHtml(cms.data.elements[cms.toolbar.favorites[i]]))
		}
		// $("#"+cms.html.favoriteMenuId+" a.ui-icon").click(function() {clickTriangle(this)});
	}

	var addToRecent = cms.toolbar.addToRecent = function(itemId) {
		cms.util.addUnique(cms.toolbar.recent, itemId, cms.toolbar.recentSize);
        cms.data.persistRecent();
	}

	var resetRecentList = cms.toolbar.resetRecentList = function() {
		$("#"+cms.html.recentMenuId+" li.cms-item").remove();
		var $recentlist = $("#"+cms.html.recentListId);
		for ( var i = 0; i < cms.toolbar.recent.length; i++) {
			$recentlist
					.append(cms.html
							.createItemFavListHtml(cms.data.elements[cms.toolbar.recent[i]]));
		}
	};

})(cms);