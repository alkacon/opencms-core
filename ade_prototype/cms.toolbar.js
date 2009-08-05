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
