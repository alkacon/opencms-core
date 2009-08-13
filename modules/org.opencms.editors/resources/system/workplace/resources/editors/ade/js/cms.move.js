(function (cms) {

var over = null;
var cancel = false;
cms.move.zIndexMap = {};

var isMenuContainer = cms.move.isMenuContainer = function(id) {
	return id == cms.html.favoriteListId || id == cms.html.recentListId;
}

var movePreparation = cms.move.movePreparation = function(event) {
	$(this).unbind('mouseenter').unbind('mouseleave').addClass('cms-trigger');
	hoverOut();
	$('a.cms-move:not(.cms-trigger)').hide();
}

var moveEnd = cms.move.moveEnd = function(event) {
	
	$(this).hover( function() {
		hoverIn($(this).parent(), 2)
	}, hoverOut).removeClass('cms-trigger');
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
	if (ui.self.cmsResource_id && cms.data.elements[ui.self.cmsResource_id]) {
		ui.self.cmsItem = cms.data.elements[ui.self.cmsResource_id];
		ui.self.cmsStartOffset = {
            top: ui.self.offset.top,
            left: ui.self.offset.left
        };
		ui.self.cmsHelpers = {};
		ui.self.cmsOrgPlaceholder = ui.placeholder.clone().insertBefore(
				ui.placeholder);
		ui.self.cmsOrgPlaceholder.addClass(ui.self.currentItem.attr('class'))
				.css( {
					'background-color' :'gray',
					'display' :'none',
					'height' :ui.self.currentItem.height()
				});
		cms.move.zIndexMap = {};
		for (container_name in cms.data.containers) {
            var containerType = cms.data.containers[container_name].type;
            // skip incompatible containers
            if (!cms.data.elements[ui.self.cmsResource_id].contents[containerType]) continue;
			
			var zIndex = $('#' + container_name).css('z-index');
			cms.move.zIndexMap[container_name] = zIndex;
			if (container_name != ui.self.cmsStartContainerId) {
				ui.self.cmsHoverList += ', #' + container_name;
				ui.self.cmsHelpers[container_name] = $(
						ui.self.cmsItem.contents[containerType]).appendTo(
						'#' + container_name).css( {
					'display' :'none',
					'position' :'absolute',
					'zIndex' :ui.self.options.zIndex
				}).addClass('ui-sortable-helper');
				if (ui.self.cmsStartContainerId != cms.toolbar.currentMenuItems) {
					// if we aren't starting from the favorite list, call
					// movePreparation on the handle
					// (hides all other handles)
					$('<a class="cms-handle cms-move"></a>').appendTo(
							ui.self.cmsHelpers[container_name]).mousedown(
							movePreparation).mouseup(moveEnd);
				} else {
					$('<a class="cms-handle cms-move"></a>').appendTo(
							ui.self.cmsHelpers[container_name]);
				}

			} else {
				ui.self.cmsHelpers[container_name] = ui.self.helper;
				ui.self.cmsOver = true;
			}
		}
		if (isMenuContainer(ui.self.cmsStartContainerId)) {
			ui.self.cmsHelpers[cms.toolbar.currentMenuItems] = ui.self.helper;
			var elem = $(document.createElement('div')).addClass(
					"placeholder" + " ui-sortable-placeholder box").css(
					'display', 'none');
			ui.placeholder.replaceWith(elem);
			ui.self.placeholder = elem;

			$('.cms-additional', ui.self.currentItem).hide();
			if (!$('#cms_appendbox').length) {
				$(document.body).append('<div id="cms_appendbox"></div>');
			}
			ui.self.helper.appendTo('#cms_appendbox');
			
            refreshHelperPositions(ui.self);
/*            ui.self._cacheHelperProportions();
			ui.self._adjustOffsetFromHelper(ui.self.options.cursorAt);
			ui.self.refreshPositions(true);*/
			
			$('#' + ui.self.cmsStartContainerId).closest('.cms-menu').css(
					'display', 'none');
			ui.self.cmsOver = false;
		} else {
            ui.self.cmsHoverList+=', #'+ ui.self.cmsStartContainerId;
			cms.util.fixZIndex(ui.self.cmsStartContainerId, cms.move.zIndexMap);

			// show drop zone for new favorites
			var list_item = '<li class="cms-item"  rel="'
					+ ui.self.cmsResource_id
					+ '"><div class=" ui-widget-content"><div class="cms-head ui-state-hover"><div class="cms-navtext"><a class="left ui-icon ui-icon-triangle-1-e"></a>'
					+ ui.self.cmsItem.navText
					+ '</div><span class="cms-title">'
					+ ui.self.cmsItem.title
					+ '</span><span class="cms-file-icon"></span><a class="cms-handle cms-move"></a></div><div class="cms-additional"><div alt="File: '
					+ ui.self.cmsItem.file
					+ '"><span class="left">File:</span>'
					+ ui.self.cmsItem.file + '</div><div alt="Date: '
					+ ui.self.cmsItem.date
					+ '"><span class="left">Date:</span>'
					+ ui.self.cmsItem.date + '</div><div alt="User: '
					+ ui.self.cmsItem.user
					+ '"><span class="left">User:</span>'
					+ ui.self.cmsItem.user + '</div><div alt="Type: '
					+ ui.self.cmsItem.type
					+ '"><span class="left">Type:</span>'
					+ ui.self.cmsItem.type + '</div></div></div></li>';
			ui.self.cmsHelpers[cms.html.favoriteListId] = $(list_item).appendTo(
					'#'+cms.html.favoriteListId).css( {
				'display' :'none',
				'position' :'absolute',
				'zIndex' :ui.self.options.zIndex
			}).addClass('ui-sortable-helper');
			$('#'+cms.html.favoriteMenuId).css('visibility', 'visible');
		}
		ui.self.placeholder.addClass(ui.self.currentItem.attr('class')).css( {
			'background-color' :'blue',
			'border' :'solid 2px black',
			'height' :ui.helper.height()
		});
		$(ui.self.cmsHoverList).each( function() {
			hoverInner($(this), 2);
		});

	} else {
		$(sortlist).sortable('cancel');
	}
}

var beforeStopFunction = cms.move.beforeStopFunction = function(event, ui) {
	if (!ui.self.cmsOver)
		cancel = true;
	else
		cancel = false;
}


var updateContainer = cms.move.updateContainer = function(id) {
    if (isMenuContainer(id)) return;
    var newContents = [];
    $('#'+id+' > *:visible').each(function(){
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
			cms.util.clearAttributes(helpers[startContainer].get(0).style,
					[ 'width', 'height', 'top', 'left', 'position', 'opacity',
							'zIndex', 'display' ]);
			$('a.cms-move', currentItem).remove();
			$('button.ui-state-active').trigger('click');

            // add item to endContainer
		} else {
			if (endContainer == cms.html.favoriteListId) {
				cms.util.addUnique(cms.toolbar.favorites, ui.self.cmsResource_id);
			}
			orgPlaceholder.remove();
            // add item to endContainer
		}
		cms.toolbar.addToRecent(ui.self.cmsResource_id);
	}
	for (var container_name in helpers) {
		if (container_name != endContainer
				&& !(startContainer == container_name && isMenuContainer(container_name))) {
			var helper = helpers[container_name];
            if (container_name == startContainer
					&& endContainer == cms.html.favoriteListId) {
			    var helperStyle = helper.get(0).style;
                helper.removeClass('ui-sortable-helper');
				// reset position (?) of helper that was dragged to favorites,
				// but don't remove it
				cms.util.clearAttributes(
						helperStyle, [
								'width', 'height', 'top', 'left', 'opacity',
								'zIndex', 'display' ]);

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

	cms.util.clearAttributes(currentItem.get(0).style, [ 'top', 'left',
			'zIndex', 'display' ]);
	if ($.browser.msie) {
		currentItem.get(0).style.removeAttribute('filter');

		// ui.self.currentItem.get(0).style.removeAttribute('position');

	} else if (currentItem) {

		// ui.self.currentItem.get(0).style.position='';
		currentItem.get(0).style.opacity = '';
	}
    updateContainer(startContainer);
    updateContainer(endContainer);
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
	if (ui.self.cmsStartContainerId != elemId
			&& ui.self.cmsStartContainerId != cms.html.favoriteListId
			&& ui.self.cmsStartContainerId != cms.html.recentListId) {
		// show pacelholder in start container if dragging over a different
		// container, but not from favorites or recent
		ui.self.cmsOrgPlaceholder.css( {
			'display' :'block',
			'border' :'dotted 2px black'
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
			ui.self.helper.height('auto');

		}

		ui.placeholder.height(ui.self.helper.height());
	} else {
		ui.placeholder.css('display', 'none');
		ui.self.cmsOver = false;
	}
	if (elemId == cms.html.favoriteListId
			&& ui.placeholder.parent().attr('id') != elemId)
		ui.placeholder.appendTo(elem);

	if (reDoHover) {
		hoverOut();
		$(ui.self.cmsHoverList).each( function() {
			hoverInner($(this), 2);
		});
	}

}

var outAdd = cms.move.outAdd = function(event, ui) {
	var elem = event.target ? event.target : event.srcElement;
	var elemId = $(elem).attr('id');
	if (ui.self.helper && elemId==ui.self.cmsCurrentContainerId){
		if (ui.self.cmsStartContainerId != ui.self.cmsCurrentContainerId) {
			ui.self.cmsCurrentContainerId = ui.self.cmsStartContainerId;
			cms.util.fixZIndex(ui.self.cmsStartContainerId, cms.move.zIndexMap);
			setHelper(ui.self, ui.self.cmsCurrentContainerId);
		}
		ui.placeholder.css('display', 'none');
		if (ui.self.cmsStartContainerId != cms.html.favoriteListId) {
			ui.self.cmsOrgPlaceholder.css( {
				'display' :'block',
				'border' :'solid 2px black'
			});
		}
		ui.self.cmsOver = false;
		hoverOut();
		$(ui.self.cmsHoverList).each( function() {
			hoverInner($(this), 2);
		});
	}

}

var hoverIn = cms.move.hoverIn = function(elem, hOff) {

	var position = cms.util.getElementPosition(elem);
	var tHeight = elem.outerHeight();
	var tWidth = elem.outerWidth();
	var hWidth = 2;
	var lrHeight = tHeight + 2 * (hOff + hWidth);
	var btWidth = tWidth + 2 * (hOff + hWidth);
	var tlrTop = position.top - (hOff + hWidth);
	var tblLeft = position.left - (hOff + hWidth);
	// top
	$('<div class="cms-hovering cms-hovering-top"></div>').height(hWidth)
			.width(btWidth).css('top', tlrTop).css('left', tblLeft).appendTo(
					document.body);

	// right
	$('<div class="cms-hovering cms-hovering-right"></div>').height(lrHeight).width(
			hWidth).css('top', tlrTop).css('left',
			position.left + tWidth + hOff).appendTo(document.body);
	// left
	$('<div class="cms-hovering cms-hovering-left"></div>').height(lrHeight).width(
			hWidth).css('top', tlrTop).css('left', tblLeft).appendTo(
			document.body);
	// bottom
	$('<div class="cms-hovering cms-hovering-bottom"></div>').height(hWidth).width(
			btWidth).css('top', position.top + tHeight + hOff).css('left',
			tblLeft).appendTo(document.body);

}

var hoverInner = cms.move.hoverInner = function(elem, hOff) {

	var position = {
		left :'x',
		top :'x'
	};
	var bottom = 'x';
	var right = 'x';

	$(elem.children('*:visible'))
			.each(
					function() {
						var el = $(this);
						if (!el.hasClass('ui-sortable-helper')) {
							var pos = cms.util.getElementPosition(el);
							position.left = (position.left == 'x' || pos.left < position.left) ? pos.left
									: position.left;
							position.top = (position.top == 'x' || pos.top < position.top) ? pos.top
									: position.top;
							bottom = (bottom == 'x' || bottom < (pos.top + el
									.outerHeight())) ? pos.top
									+ el.outerHeight() : bottom;
							right = (right == 'x' || right < (pos.left + el
									.outerWidth())) ? pos.left
									+ el.outerWidth() : right;
						}
					});
	var tHeight = bottom - position.top;
	var tWidth = right - position.left;
	var elemPos = cms.util.getElementPosition(elem);

	if (bottom == 'x') {
		tHeight = 25;
		tWidth = elem.innerWidth();
		position = elemPos;
	}

	var hWidth = 2;

	var inner = {
		top :position.top - (elemPos.top + hOff),
		left :position.left - (elemPos.left + hOff),
		height :tHeight + 2 * hOff,
		width :tWidth + 2 * hOff
	};
	// inner
	$(
			'<div class="cms-highlight-container" style="position: absolute; z-index:0; top: '
					+ inner.top + 'px; left: ' + inner.left + 'px; height: '
					+ inner.height + 'px; width: ' + inner.width
					+ 'px;"></div>').prependTo(elem);

	// top
	$('<div class="cms-hovering cms-hovering-top"></div>').height(hWidth).width(
			tWidth + 2 * (hOff + hWidth)).css('top',
			position.top - (hOff + hWidth)).css('left',
			position.left - (hOff + hWidth)).appendTo(document.body);
	// right
	$('<div class="cms-hovering cms-hovering-right"></div>').height(
			tHeight + 2 * (hOff + hWidth)).width(hWidth).css('top',
			position.top - (hOff + hWidth)).css('left',
			position.left + tWidth + hOff).appendTo(document.body);
	// left
	$('<div class="cms-hovering cms-hovering-left"></div>').height(
			tHeight + 2 * (hOff + hWidth)).width(hWidth).css('top',
			position.top - (hOff + hWidth)).css('left',
			position.left - (hOff + hWidth)).appendTo(document.body);
	// bottom
	$('<div class="cms-hovering cms-hovering-bottom"></div>').height(hWidth).width(
			tWidth + 2 * (hOff + hWidth)).css('top',
			position.top + tHeight + hOff).css('left',
			position.left - (hOff + hWidth)).appendTo(document.body);

}
var hoverOut = cms.move.hoverOut = function() {
	$('div.cms-hovering, div.cms-highlight-container').remove();

};
var setHelper = cms.move.setHelper = function (sortable, id) {
	sortable.helper.css('display','none');
	sortable.helper=sortable.cmsHelpers[id].css('display','block');
	sortable.currentItem=sortable.cmsHelpers[id];
	refreshHelperPositions(sortable);
};

var refreshHelperPositions = cms.move.refreshHelperPositions = function(sortable) {
	sortable._cacheHelperProportions();
	sortable._adjustOffsetFromHelper(sortable.options.cursorAt);
	sortable.refreshPositions(true);
};


})(cms);
