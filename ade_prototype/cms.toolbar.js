var showPublishList = function() {
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
}

var deleteItem = function() {
	hoverOut();
	addToRecent($(this).parent().attr('rel'));
	$(this).parent().remove();

}

var toggleDelete = function(el) {
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
					$('<a class="cms-handle cms-delete"></a>').appendTo(elem)
							.hover( function() {
								hoverIn(elem, 2)
							}, hoverOut).click(deleteItem);
				});
		button.addClass('ui-state-active');
	}
};

var removeToolbar = function() {
	$('#toolbar').remove();
	$(document.body).css('margin-top', oldBodyMarginTop + 'px');
}

var hideToolbar = function() {
	$(document.body).animate( {
		marginTop :oldBodyMarginTop + 'px'
	}, 200, 'swing', function() {
		$('#show-button').show(50);
	});

	return false;
}

var toggleToolbar = function() {
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
}

var addToolbar = function() {
	var bodyEl = $(document.body).css('position', 'relative');
	oldBodyMarginTop = bodyEl.offset().top;
	var offsetLeft = bodyEl.offset().left;
	bodyEl.append(toolbar);
	bodyEl.append(favoriteList);
	bodyEl.append(favoriteDialog);
	bodyEl.append(recentList);
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

var toggleMove = function(el) {
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
								hoverIn(elem, 2)
							}, hoverOut).mousedown(movePreparation).mouseup(
									moveEnd);
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
			start :startAdd,
			beforeStop :beforeStopFunction,
			over :overAdd,
			out :outAdd,
			tolerance :'pointer',
			opacity :0.7,
			stop :stopAdd,
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



var toggleList = function(buttonElem, newMenu) {
	var button = $(buttonElem);
	var newMenuItems = $('#' + newMenu).find("ul").attr('id');
	if (button.hasClass('ui-state-active')) {

		$(sortlist + ', ' + sortmenus).sortable('destroy');
		$(menuHandles).remove();
		$(menus).hide();
		button.removeClass('ui-state-active');
	} else {
		resetRecentList();
		currentMenu = newMenu;
		currentMenuItems = newMenuItems
		$('button.ui-state-active').trigger('click');

		// enabling move-mode
		// * current menu
		list = $('#' + currentMenu);
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
		$(sortlist + ', #' + currentMenuItems).sortable( {
			// * current menu
			connectWith :sortlist + ', #' + currentMenuItems,
			placeholder :'placeholder',
			dropOnEmpty :true,
			start :startAdd,
			beforeStop :beforeStopFunction,
			over :overAdd,
			out :outAdd,
			tolerance :'pointer',
			opacity :0.7,
			stop :stopAdd,
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

var clickFavDeleteIcon = function() {
	var button = $(this);
	var toRemove = button.parent().parent();
	toRemove.remove();
}

var arrayToString = function(arr) {
	return "[" + arr.join(", ") + "]";
}

var saveFavorites = function() {
	var newFavs = [];
	$("#fav-dialog li.cms-item").each( function() {
		var resource_id = this.getAttribute("rel");
		addUnique(newFavs, resource_id);
	});
	favorites = newFavs;
	resetFavList();

}

var favEditOK = function() {
	$(this).dialog("close");
	saveFavorites();
}

var favEditCancel = function() {
	$(this).dialog("close");
}

var initFavDialog = function() {
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

}

var initFavDialogItems = function() {
	$("#fav-dialog ul").remove();
	$("#fav-dialog").append("<ul></ul>")
	var html = []
	for ( var i = 0; i < favorites.length; i++) {
		html.push(createItemFavDialogHtml(cms_elements_list[favorites[i]]));
	}
	$("#fav-dialog ul").append(html.join(''));
	$("#fav-dialog .cms-delete-icon").click(clickFavDeleteIcon);
	$("#fav-dialog ul").sortable();
	// $("#fav-dialog a.ui-icon").click(function() {clickTriangle(this);});
	$('#fav-dialog div.cms-additional div').jHelperTip( {
		trigger :'hover',
		source :'attribute',
		attrName :'alt',
		topOff :-30,
		opacity :0.8
	});
}

var showFavDialog = function() {
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
}

var toggleAdditionalInfo = function() {
	var elem = $(this);
	if (elem.hasClass('ui-icon-triangle-1-e')) {
		elem.removeClass('ui-icon-triangle-1-e').addClass(
				'ui-icon-triangle-1-s');
		elem.parents('.ui-widget-content').children('.cms-additional').show(5,
				function() {
					var list = $(this).parents('div.cms-item-list');
					$('div.ui-widget-shadow', list).css( {
						height :list.outerHeight() + 2
					});
				});
	} else {
		elem.removeClass('ui-icon-triangle-1-s').addClass(
				'ui-icon-triangle-1-e');
		elem.parents('.ui-widget-content').children('.cms-additional').hide(5,
				function() {
					var list = $(this).parents('div.cms-item-list');
					$('div.ui-widget-shadow', list).css( {
						height :list.outerHeight() + 2
					});
				});
	}
	return false;
}

var resetFavList = function() {
	$("#favoritelist li.cms-item").remove();
	var $favlist = $("#favoritelist ul");
	for ( var i = 0; i < favorites.length; i++) {
		$favlist.append(createItemFavListHtml(cms_elements_list[favorites[i]]))
	}
	// $("#favoritelist a.ui-icon").click(function() {clickTriangle(this)});
}

var addToRecent = function(itemId) {
	addUnique(recent, itemId, recentSize);
}

var resetRecentList = function() {
	$("#recentlist li.cms-item").remove();
	var $recentlist = $("#recent_list_items");
	for ( var i = 0; i < recent.length; i++) {
		$recentlist.append(createItemFavListHtml(cms_elements_list[recent[i]]));
	}
}

