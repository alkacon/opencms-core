/*util*/
var log = function(s) {
	$("body").append("<p>" + s + "</p>");
}

/* util */
var dump = function(s) {
	$("body").append("<pre>" + $.dump(s) + "</pre>");
}

var addUnique = function(list, item, maxlen) {
	for ( var i = 0; i < list.length; i++) {
		if (list[i] == item) {
			list.splice(i, 1);
			break;
		}
	}
	list.splice(0, 0, item);
	if (maxlen) {
		var delLength = list.length - maxlen;
		if (delLength > 0)
			list.splice(maxlen, delLength);
	}
}

var addToList = function(resource_id, list, max_size) {
	var newList = [ resource_id ];
	for ( var i = 0; i < list.length; i++) {
		if (resource_id != list[i])
			newList.push(list[i]);
		if (max_size && newList.length >= max_size)
			break;
	}
	return newList;
}

var clearAttributes = function(elem, attrs) {
	var ie = $.browser.msie;
	for ( var i = 0; i < attrs.length; i++) {
		if (ie) {
			elem.removeAttribute(attrs[i]);
		} else {
			elem[attrs[i]] = '';
		}
	}
}

var getElementPosition = function(elem) {
	var position = {
		left :0,
		top :0
	};
	var offset = elem.offset();
	if ($(document.body).css('position') == 'relative'
			|| $(document.body).css('position') == 'absolute') {
		position.left = offset.left - $(document.body).offset().left;
		position.top = offset.top - $(document.body).offset().top;
	} else {
		position.left = offset.left;
		position.top = offset.top;
	}
	return position;
}

var fixZIndex = function(currentId, zmap) {
	if (!$.browser.msie)
		return;
	var z;
	for ( var key in zmap) {
		if (key == currentId) {
			z = 9999;
		} else {
			z = zmap[key];
		}
		setZIndex(key, z);
	}
}

var setZIndex = function(id, z) {
	$('#' + id).css('z-index', z);
}

var setHelper = function (sortable, id) {
	sortable.helper.css('display','none');
	sortable.helper=sortable.cmsHelpers[id].css('display','block');
	sortable.currentItem=sortable.cmsHelpers[id];
	refreshHelperPositions(sortable);
}

var refreshHelperPositions = function(sortable) {
	sortable._cacheHelperProportions();
	sortable._adjustOffsetFromHelper(sortable.options.cursorAt);
	sortable.refreshPositions(true);
}
