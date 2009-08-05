(function(cms){
    
var recentMenuId=cms.html.recentMenuId='recentlist';
var recentListId=cms.html.recentListId='recent_list_items';
var favoriteMenuId=cms.html.favoriteMenuId='favoritelist';
var favoriteListId=cms.html.favoriteListId='favorite_list_items';
var favoriteDialogId=cms.html.favoriteDialogId='fav-dialog';
var publishDialogId=cms.html.publishDialogId='publishlist';
var toolbarId=cms.html.toolbarId='toolbar';


var recentList = cms.html.recentList = '<div id="'+recentMenuId+'" class="cms-menu">\
    <div class="connect ui-corner-top"></div>\
    <div class="ui-widget-shadow ui-corner-all"></div>\
    <div class="ui-widget-content ui-corner-bottom ui-corner-tl">\
        <ul id="'+recentListId+'" class="cms-item-list"></ul>\
    </div></div>';


var favoriteList = cms.html.favoriteList = '<div id="'+favoriteMenuId+'" class="cms-menu">\
	<div class="connect ui-corner-top"></div>\
	<div class="ui-widget-shadow ui-corner-all"></div>\
	<div class="ui-widget-content ui-corner-bottom ui-corner-tl">\
	    <ul id="'+favoriteListId+'" class="cms-item-list">\
	        <button id="fav-edit" name="Edit_Favorites" title="Edit Favorites" class="ui-state-default ui-corner-all">Edit Favorites</button>\
	    </ul>\
	</div></div>';

var publishList = cms.html.publishList = '<div id="'+publishDialogId+'" class="cms-dialog">\
	<form action="#">\
	    <ul  class="cms-item-list"></ul>\
	    <ul class="cms-publish-options">\
	        <li>\
	            <div class="cms-left">\<span class="cms-check-icon"></span></div>\
                <label for="siblings">Publish all siblings</label>\
	            <br clear="all"/>\
	        </li>\
	        <li>\
	            <div class="cms-left"><span class="cms-check-icon"></span></div>\
	            <label for="related">Publish with related resources</label>\
	            <br  clear="all"/>\
	        </li>\
	    </ul>\
	</form></div>';
    
var createMenu = cms.html.createMenu = function(menuId){
    var listId='';
    var addMenuItem = '';
    if (menuId == favoriteMenuId) {
        listId = favoriteListId;
        addMenuItem='<button id="fav-edit" name="Edit_Favorites" title="Edit Favorites" class="ui-state-default ui-corner-all cms-edit-favorites">Edit Favorites</button>';
    }else if (menuId == recentMenuId){
        listId = recentListId;
    }
    
    var html = ['<div id="', menuId, '" class="cms-menu">\
    	<div class="connect ui-corner-top"></div>\
    	<div class="ui-widget-shadow ui-corner-all"></div>\
    	<div class="ui-widget-content ui-corner-bottom ui-corner-tl">\
    	    <ul id="', listId, '" class="cms-item-list">', addMenuItem,'</ul>\
    	</div></div>'];
        
    return html.join('');
}

var favoriteDialog= cms.html.favoriteDialog = '<div id="'+favoriteDialogId+'" class="cms-dialog"><ul class="cms-item-list"></ul></div>';

var createItemFavDialogHtml = cms.html.createItemFavDialogHtml = function(item) {
	var html = [
	'<li class="cms-item" rel="',item.resource_id,'">\
		<div class="cms-left">\
			<span class="cms-delete-icon" ></span>\
		</div>\
		<div class="cms-left ui-widget-content">\
			<div class="cms-head ui-state-hover">\
				<div class="cms-navtext"><a class="cms-left ui-icon ui-icon-triangle-1-e"></a>',item.nav_text,'</div>\
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
var createItemFavListHtml = cms.html.createItemFavListHtml = function(item) {
	var html =[
		'<li class="cms-item"  rel="',item.resource_id,'">\
			<div class=" ui-widget-content">\
				<div class="cms-head ui-state-hover">\
					<div class="cms-navtext"><a class="cms-left ui-icon ui-icon-triangle-1-e"></a>',item.nav_text,'</div>\
					<span class="cms-title">',item.title,'</span>\
					<span class="cms-file-icon"></span>\
				</div>\
				<div class="cms-additional">\
					<div alt="File: ',item.file,'"><span class="cms-left">File:</span>',item.file,'</div>\
					<div alt="Date: ',item.date,'"><span class="cms-left">Date:</span>',item.date,'</div>\
					<div alt="User: ',item.user,'"><span class="cms-left">User:</span>',item.user,'</div>\
					<div alt="Type: ',item.type,'"><span class="cms-left">Type:</span>',item.type,'</div>\
				</div>\
			</div>\
			\
		</li>'];
	return html.join('');
}

var toolbar = cms.html.toolbar = '<div id="'+toolbarId+'">\
	<div class="ui-widget-shadow"></div>\
	<div id="toolbar_background" class="ui-widget-header cms-toolbar-background">\
	    <div id="toolbar_content" class="cms-toolbar-content">\
	        <button name="Reset" title="Reset" class="cms-left ui-state-default ui-corner-all"><span class="ui-icon cms-icon-reset"></span>&nbsp;</button>\
	        <button name="Edit" title="Edit" class="cms-left ui-state-default ui-corner-all"><span class="ui-icon cms-icon-edit"></span>&nbsp;</button>\
		    <button name="Move" title="Move" class="cms-left ui-state-default ui-corner-all"><span class="ui-icon cms-icon-move"></span>&nbsp;</button>\
            <button name="Delete" title="Delete" class="cms-left ui-state-default ui-corner-all"><span class="ui-icon cms-icon-delete"></span>&nbsp;</button>\
            <button name="Add" title="Add" class="cms-left cms-button-wide ui-state-default ui-corner-all"><span class="ui-icon cms-icon-add"></span><span class="cms-button-text">Add</span></button>\
            <button name="New" title="New" class="cms-left cms-button-wide ui-state-default ui-corner-all"><span class="ui-icon cms-icon-new"></span><span class="cms-button-text">New</span></button>\
            <button name="Favorites" title="Favorites" class="cms-left cms-button-wide ui-state-default ui-corner-all"><span class="ui-icon cms-icon-favorites" /><span class="cms-button-text">Favorites</span></button>\
            <button name="Recent" title="Recent" class="cms-left cms-button-wide ui-state-default ui-corner-all"><span class="ui-icon cms-icon-recent" /><span class="cms-button-text">Recent</span></button>\
            <button name="Save" title="Save"  class="cms-right ui-state-default ui-corner-all"><span class="ui-icon cms-icon-save"/>&nbsp;</button>\
            <button name="Sitemap" title="Sitemap" class="cms-right ui-state-default ui-corner-all"><span class="ui-icon cms-icon-sitemap"/>&nbsp;</button>\
            <button name="Publish" title="Publish" class="cms-right ui-state-default ui-corner-all"><span class="ui-icon cms-icon-publish"/>&nbsp;</button>\
        </div>\
     </div></div>';


})(cms);