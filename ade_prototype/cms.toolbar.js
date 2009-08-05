(function(cms) {
	cms.toolbar.favorites = ['item_010', 'item_011', 'item_012'];
    cms.toolbar.recent = [];
    cms.toolbar.recentSize = 3;
    var oldBodyMarginTop = 0;
    
    var sortmenus = "#favoritelist ul, #recentlist ul"
    var menuHandles = "#favoritelist a.cms-move, #recentlist a.cms-move"
    var menus = "#favoritelist, #recentlist";
    cms.toolbar.currentMenu = "favoritelist";
    cms.toolbar.currentMenuItems = "favorite_list_items";


	var showPublishList = cms.toolbar.showPublishList = function() {
		var button = $(this);
		if (button.hasClass('ui-state-active')) {
			// disabling move mode

			button.removeClass('ui-state-active');
		} else {
			$('button.ui-state-active').trigger('click');
			// enabling move-mode
			$('#publishlist').dialog('open');
			button.addClass('ui-state-active');
		}
	};

	var deleteItem = cms.toolbar.deleteItem = function() {
		cms.move.hoverOut();
		addToRecent($(this).parent().attr('rel'));
		$(this).parent().remove();

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
		bodyEl.append(cms.html.favoriteList);
		bodyEl.append(cms.html.favoriteDialog);
		bodyEl.append(cms.html.recentList);
		resetFavList();
		bodyEl
				.append('<button id="show-button" title="toggle toolbar" class="ui-state-default ui-corner-all"><span class="ui-icon cms-icon-logo"/></button>');
		$('#show-button').click(toggleToolbar);
		$('button[name="Move"]').click(toggleMove);
		$('button[name="Delete"]').click(toggleDelete);
		$('button[name="Publish"]').click(showPublishList);
		$('button[name="Favorites"]').click( function() {
			toggleList(this, "favoritelist");
		});
		$('button[name="Recent"]').click( function() {
			toggleList(this, "recentlist");
		});
		$('#toolbar button, #show-button').mouseover( function() {
			$(this).addClass('ui-state-hover');
		}).mouseout( function() {
			$(this).removeClass('ui-state-hover');
		});
		bodyEl.animate( {
			marginTop :oldBodyMarginTop + 34 + 'px'
		}, 200);
		$('#publishlist').dialog( {
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
			autoOpen :false,
			draggable :false,
			resizable :false,
			position : [ 'center', 20 ],
			close : function() {
				$('button[name="Publish"]').removeClass('ui-state-active');
			},
			zIndex :10000
		});
		$('#publishlist span.cms-check-icon').click( function() {
			$(this).toggleClass('cms-check-icon-inactive')
		});
		initFavDialog();
	};

	var toggleMove = cms.toolbar.toggleMove = function(el) {
		var button = $(this);

		if (button.hasClass('ui-state-active')) {
			// disabling move-mode
			$(sortlist + ', #favorite_list_items').sortable('destroy');
			var list = $('#favoritelist');
			$('li.cms-item, button', list).css('display', 'block');
			list.css('display', 'none');
			list.get(0).style.visibility = '';
			$('#favorite_list_items').get(0).style.height = '';
			resetFavList();
			$('a.cms-move').remove();
			button.removeClass('ui-state-active');
		} else {
			$('button.ui-state-active').trigger('click');
			// enabling move mode
			$(sortitems).each(
					function() {
						var elem = $(this).css('position', 'relative');
						$('<a class="cms-handle cms-move"></a>').appendTo(elem)
								.hover( function() {
									cms.move.hoverIn(elem, 2)
								}, cms.move.hoverOut).mousedown(cms.move.movePreparation)
								.mouseup(cms.move.moveEnd);
					});

			var list = $('#favoritelist');
			var favbutton = $('button[name="Favorites"]');
			$('li.cms-item, button', list).css('display', 'none');
			list.appendTo('#toolbar_content').css( {
				top :35,
				left :favbutton.position().left - 217,
				display :'block',
				visibility :'hidden'
			});
			$('#favorite_list_items').css('height', '37px');
			$('div.ui-widget-shadow', list).css( {
				top :0,
				left :-4,
				width :list.outerWidth() + 8,
				height :list.outerHeight() + 2,
				border :'0px solid',
				opacity :0.6
			});

			$(sortlist).children('*:visible').css('position', 'relative');
			$(sortlist + ', #favorite_list_items').sortable( {
				connectWith :sortlist + ', #favorite_list_items',
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
					right :10,
					top :10
				},
				zIndex :20000,
				handle :'a.cms-move',
				items :sortitems,
				revert :true,
				deactivate : function(event, ui) {
					$('#favorite_list_items li').hide(200);
					$('#favoritelist').css('visibility', 'hidden');
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
			cms.util.addUnique(newFavs, resource_id);
		});
		cms.toolbar.favorites = newFavs;
		resetFavList();

	};

	var favEditOK = cms.toolbar.favEditOK = function() {
		$(this).dialog("close");
		saveFavorites();
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
		$("#fav-dialog ul").remove();
		$("#fav-dialog").append("<ul></ul>")
		var html = []
		for ( var i = 0; i < cms.toolbar.favorites.length; i++) {
			html
					.push(cms.html
							.createItemFavDialogHtml(cms.data.cms_elements_list[cms.toolbar.favorites[i]]));
		}
		$("#fav-dialog ul").append(html.join(''));
		$("#fav-dialog .cms-delete-icon").click(clickFavDeleteIcon);
		$("#fav-dialog ul").sortable();
		$('#fav-dialog div.cms-additional div').jHelperTip( {
			trigger :'hover',
			source :'attribute',
			attrName :'alt',
			topOff :-30,
			opacity :0.8
		});
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
						var list = $(this).parents('div.cms-item-list');
						$('div.ui-widget-shadow', list).css( {
							height :list.outerHeight() + 2
						});
					});
		} else {
			elem.removeClass('ui-icon-triangle-1-s').addClass(
					'ui-icon-triangle-1-e');
			elem.parents('.ui-widget-content').children('.cms-additional')
					.hide(5, function() {
						var list = $(this).parents('div.cms-item-list');
						$('div.ui-widget-shadow', list).css( {
							height :list.outerHeight() + 2
						});
					});
		}
		return false;
	};

	var resetFavList = cms.toolbar.resetFavList = function() {
		$("#favoritelist li.cms-item").remove();
		var $favlist = $("#favoritelist ul");
		for ( var i = 0; i < cms.toolbar.favorites.length; i++) {
			$favlist
					.append(cms.html
							.createItemFavListHtml(cms.data.cms_elements_list[cms.toolbar.favorites[i]]))
		}
		// $("#favoritelist a.ui-icon").click(function() {clickTriangle(this)});
	}

	var addToRecent = cms.toolbar.addToRecent = function(itemId) {
		cms.util.addUnique(cms.toolbar.recent, itemId, cms.toolbar.recentSize);
	}

	var resetRecentList = cms.toolbar.resetRecentList = function() {
		$("#recentlist li.cms-item").remove();
		var $recentlist = $("#recent_list_items");
		for ( var i = 0; i < cms.toolbar.recent.length; i++) {
			$recentlist
					.append(cms.html
							.createItemFavListHtml(cms.data.cms_elements_list[cms.toolbar.recent[i]]));
		}
	};

})(cms);