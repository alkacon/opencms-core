var recentList = '<div id="recentlist" class="cms-item-list">\
<div class="connect ui-corner-top"></div>\
<div class="ui-widget-shadow ui-corner-all"></div>\
<div class="ui-widget-content ui-corner-bottom ui-corner-tl">\
<ul id="recent_list_items">\
</ul>\
</div>\
</div>';


var favoriteList = '<div id="favoritelist" class="cms-item-list">\
	<div class="connect ui-corner-top"></div>\
	<div class="ui-widget-shadow ui-corner-all"></div>\
	<div class="ui-widget-content ui-corner-bottom ui-corner-tl">\
	<ul id="favorite_list_items">\
	<button id="fav-edit" name="Edit_Favorites" title="Edit Favorites" class="ui-state-default ui-corner-all">Edit Favorites</button>\
	</ul>\
	</div>\
</div>';

var publishList= '<div id="publishlist" class="cms-item-list">\
	<form action="#">\
	<ul>\
	</ul>\
	<ul class="options">\
	<li>\
	<div class="left">\<span class="cms-check-icon"></span>\
	</div>\<label for="siblings">Publish all siblings</label>\
	<br clear="all"/>\
	</li>\
	<li>\
	<div class="left">\
	<span class="cms-check-icon"></span>\
	</div>\
	<label for="related">Publish with related resources</label>\
	<br  clear="all"/>\
	</li>\
	</ul>\
	</form>\
</div>';

var favoriteDialog='<div id="fav-dialog" class="cms-item-list"><ul></ul></div>';

var createItemFavDialogHtml = function(item) {
	var html = [
	'<li class="cms-item" rel="',item.resource_id,'">\
		<div class="left">\
			<span class="cms-delete-icon" ></span>\
		</div>\
		<div class="left ui-widget-content">\
			<div class="cms-head ui-state-hover">\
				<div class="cms-navtext"><a class="left ui-icon ui-icon-triangle-1-e"></a>',item.nav_text,'</div>\
				<span class="cms-title">',item.title,'</span>\
				<span class="cms-file-icon"></span>\
				<a class="cms-move cms-handle"></a>\
			</div>\
			<div class="cms-additional">\
				<div alt="File:',item.file,'"><span class="left">File:</span>',item.file,'</div>\
				<div alt="Date:',item.date,'"><span class="left">Date:</span>',item.date,'</div>\
				<div alt="User:',item.user,'"><span class="left">User:</span>',item.user,'</div>\
				<div alt="Type:',item.type,'"><span class="left">Type:</span>',item.type,'</div>\
			</div>\
		</div>\
		<br clear="all" />\
	</li>'];
	return html.join('');
}
var createItemFavListHtml = function(item) {
	var html =[
		'<li class="cms-item"  rel="',item.resource_id,'">\
			<div class=" ui-widget-content">\
				<div class="cms-head ui-state-hover">\
					<div class="cms-navtext"><a class="left ui-icon ui-icon-triangle-1-e"></a>',item.nav_text,'</div>\
					<span class="cms-title">',item.title,'</span>\
					<span class="cms-file-icon"></span>\
				</div>\
				<div class="cms-additional">\
					<div alt="File: ',item.file,'"><span class="left">File:</span>',item.file,'</div>\
					<div alt="Date: ',item.date,'"><span class="left">Date:</span>',item.date,'</div>\
					<div alt="User: ',item.user,'"><span class="left">User:</span>',item.user,'</div>\
					<div alt="Type: ',item.type,'"><span class="left">Type:</span>',item.type,'</div>\
				</div>\
			</div>\
			\
		</li>'
	];
	return html.join('');
}

var toolbar = '<div id="toolbar">\
	<div class="ui-widget-shadow">\
	</div>\
	<div id="toolbar_background" class="ui-widget-header">\
	<div id="toolbar_content">\
	<button name="Reset" title="Reset" class="left ui-state-default ui-corner-all">\
	<span class="ui-icon cms-icon-reset">\
	</span>&nbsp;\
	</button>\
	<button name="Edit" title="Edit" class="left ui-state-default ui-corner-all">\
	<span class="ui-icon cms-icon-edit">\
	</span>\
	&nbsp;</button>\
		<button name="Move" title="Move" class="left ui-state-default ui-corner-all"><span class="ui-icon cms-icon-move"></span>&nbsp;</button><button name="Delete" title="Delete" class="left ui-state-default ui-corner-all"><span class="ui-icon cms-icon-delete"></span>&nbsp;</button><button name="Add" title="Add" class="left wide ui-state-default ui-corner-all"><span class="ui-icon cms-icon-add"></span><span class="cms-button-text">Add</span></button><button name="New" title="New" class="left wide ui-state-default ui-corner-all"><span class="ui-icon cms-icon-new"></span><span class="cms-button-text">New</span></button><button name="Favorites" title="Favorites" class="left wide ui-state-default ui-corner-all"><span class="ui-icon cms-icon-favorites" /><span class="cms-button-text">Favorites</span></button><button name="Recent" title="Recent" class="left wide ui-state-default ui-corner-all"><span class="ui-icon cms-icon-recent" /><span class="cms-button-text">Recent</span></button><button name="Save" title="Save"  class="right ui-state-default ui-corner-all"><span class="ui-icon cms-icon-save"/>&nbsp;</button><button name="Sitemap" title="Sitemap" class="right ui-state-default ui-corner-all"><span class="ui-icon cms-icon-sitemap"/>&nbsp;</button><button name="Publish" title="Publish" class="right ui-state-default ui-corner-all"><span class="ui-icon cms-icon-publish"/>&nbsp;</button></div></div></div>';


