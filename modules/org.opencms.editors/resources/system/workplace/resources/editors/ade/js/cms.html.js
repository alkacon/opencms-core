(function(cms) {

   var recentMenuId = cms.html.recentMenuId = 'recentlist';
   var recentListId = cms.html.recentListId = 'recent_list_items';
   var favoriteMenuId = cms.html.favoriteMenuId = 'favoritelist';
   var favoriteListId = cms.html.favoriteListId = 'favorite_list_items';
   var favoriteDialogId = cms.html.favoriteDialogId = 'fav-dialog';
   var publishDialogId = cms.html.publishDialogId = 'publishlist';
   var toolbarId = cms.html.toolbarId = 'toolbar';
   
   var newMenuId = cms.html.newMenuId = 'cms-new';
   var newListId = cms.html.newListId = 'cms-new-list';
   
   var searchMenuId = cms.html.searchMenuId = 'cms-search';
   var searchListId = cms.html.searchListId = 'cms-search-list';
   var searchDialogId = cms.html.searchDialogId = 'cms-search-dialog';
   var searchTypePrefix = cms.html.searchTypePrefix = 'cms-type-';
   
   var recentList = cms.html.recentList = '<div id="' + recentMenuId + '" class="cms-menu">\
    <div class="connect ui-corner-top"></div>\
    <div class="ui-widget-shadow ui-corner-all"></div>\
    <div class="ui-widget-content ui-corner-bottom ui-corner-tl">\
        <ul id="' +
   recentListId +
   '" class="cms-item-list"></ul>\
    </div></div>';
   
   
   var favoriteList = cms.html.favoriteList = '<div id="' + favoriteMenuId + '" class="cms-menu">\
	<div class="connect ui-corner-top"></div>\
	<div class="ui-widget-shadow ui-corner-all"></div>\
	<div class="ui-widget-content ui-corner-bottom ui-corner-tl">\
	    <ul id="' +
   favoriteListId +
   '" class="cms-item-list">\
	        <button id="fav-edit" name="Edit_Favorites" title="Edit Favorites" class="ui-state-default ui-corner-all">Edit Favorites</button>\
	    </ul>\
	</div></div>';
   
   var publishDialog = cms.html.publishDialog = '<div id="' + publishDialogId + '" class="cms-dialog">\
	<form action="#">\
	    <ul  class="cms-item-list"><li class="cms-item">\
            <div class="cms-left">\
              <span class="cms-check-icon"></span>\
            </div>\
            <div class="cms-left ui-widget-content">\
              <div class="cms-head ui-state-hover">\
                <div class="cms-navtext">\
                  <a class="cms-left ui-icon ui-icon-triangle-1-e"></a>Extranet\
                </div>\
                <span class="cms-title">Flower extranet</span>\
                <span class="cms-file-icon"></span>\
                <span class="cms-led-icon"></span>\
                <span class="cms-lock-icon"></span>\
              </div>\
              <div class="cms-additional">\
                <div alt="File: /demo_en/site/extranet/extranet.html">\
                  <span class="cms-left">File:</span>extranet.html\
                </div>\
                <div alt="Date: 04/04/2009">\
                  <span class="cms-left">Date:</span>4/4/2009 5:30 PM\
                </div>\
                <div alt="User: Admin">\
                  <span class="cms-left">User:</span>Admin\
                </div>\
                <div alt="Type: xmlpage">\
                  <span class="cms-left">Type:</span>xmlpage\
                </div>\
              </div>\
            </div>\
            <br clear="all" />\
          </li></ul>\
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
   
   var createMenu = cms.html.createMenu = function(menuId) {
      var listId = '';
      var addMenuItem = '';
      if (menuId == favoriteMenuId) {
         listId = favoriteListId;
         addMenuItem = '<button id="fav-edit" name="Edit_Favorites" title="Edit Favorites" class="ui-state-default ui-corner-all cms-edit-favorites">Edit Favorites</button>';
      } else if (menuId == recentMenuId) {
         listId = recentListId;
      } else {
         listId = menuId + "-list";
      }
      
      var html = ['<div id="', menuId, '" class="cms-menu" style="display:none">\
    	<div class="connect ui-corner-top"></div>\
    	<div class="ui-widget-shadow ui-corner-all"></div>\
    	<div class="ui-widget-content ui-corner-bottom ui-corner-tl">\
    	    <ul id="', listId, '" class="cms-item-list">', addMenuItem, '</ul>\
    	</div></div>'];
      
      return html.join('');
   }
   
   var searchMenu = cms.html.searchMenu = '\
      <div id="cms-search" class="cms-menu" style="width: 350px; display:none">\
          <div class="connect ui-corner-top"></div>\
          <div class="ui-widget-shadow ui-corner-all" style="width: 345px"></div>\
          <div class="ui-widget-content ui-corner-bottom ui-corner-tl">\
              <button class="cms-search-button ui-corner-all ui-state-default">Search</button>\
              <div class="cms-scrolling">\
                  <ul id="cms-search-list"  class="cms-scrolling-inner">\
                  </ul>\
              </div>\
              <div class="cms-loading"><span style="opacity:0">.</span></div>\
          </div>\
      </div>';
   
   
   var favoriteDialog = cms.html.favoriteDialog = '<div id="' + favoriteDialogId + '" class="cms-dialog"><ul class="cms-item-list"></ul></div>';
   var deleteIcon = '<div class="cms-left"><span class="cms-delete-icon"></span></div>';
   
   /**
    * Creates the HTML for an item in the favorite dialog from an element.<p>
    * @param {Object} item the element for which to generate the HTML
    * @return the HTML for the element
    */
   var createItemFavDialogHtml = cms.html.createItemFavDialogHtml = /*Html*/ function(/*Element*/item) {
      var $content = $(item.contents['_DEFAULT_']);
      $content.prepend(deleteIcon);
      // hack needed to make the delete icon visible
      $('.ui-widget-content', $content).addClass("cms-left");
      return cms.util.jqueryToHtml($content);
   }
   
   
   /**
    * Creates the HTML for an item in the New/Fav/Recent menu from an element.<p>
    *
    * @param {String} id the id of the element for which the HTML should be generated
    */
   var createItemFavListHtml = cms.html.createItemFavListHtml = function(id) {
      var/**Object*/ elem = cms.data.elements[id];
      if (!elem) {
         // in case the element is not found reload it
         cms.data.reloadElement(id, function(ok) {
            return $(cms.data.elements[id].contents['_DEFAULT_']);
            if (!ok) {
               // TODO
               alert("Error!");
            }
         });
      } else {
         return $(elem.contents['_DEFAULT_']);
      }
   }
   
   var toolbar = cms.html.toolbar = '<div id="' + toolbarId + '">\
	<div class="ui-widget-shadow"></div>\
	<div id="toolbar_background" class="ui-widget-header cms-toolbar-background">\
	    <div id="toolbar_content" class="cms-toolbar-content">\
	        <button name="Reset" title="Reset" class="cms-left ui-state-default ui-corner-all cms-deactivated"><span class="ui-icon cms-icon-reset"></span>&nbsp;</button>\
	        <button name="Edit" title="Edit" class="cms-left ui-state-default ui-corner-all"><span class="ui-icon cms-icon-edit"></span>&nbsp;</button>\
		    <button name="Move" title="Move" class="cms-left ui-state-default ui-corner-all"><span class="ui-icon cms-icon-move"></span>&nbsp;</button>\
            <button name="Delete" title="Delete" class="cms-left ui-state-default ui-corner-all"><span class="ui-icon cms-icon-delete"></span>&nbsp;</button>\
            <button name="Add" title="Add" class="cms-left cms-button-wide ui-state-default ui-corner-all"><span class="ui-icon cms-icon-add"></span><span class="cms-button-text">Add</span></button>\
            <button name="New" title="New" class="cms-left cms-button-wide ui-state-default ui-corner-all"><span class="ui-icon cms-icon-new"></span><span class="cms-button-text">New</span></button>\
            <button name="Favorites" title="Favorites" class="cms-left cms-button-wide ui-state-default ui-corner-all"><span class="ui-icon cms-icon-favorites" /><span class="cms-button-text">Favorites</span></button>\
            <button name="Recent" title="Recent" class="cms-left cms-button-wide ui-state-default ui-corner-all"><span class="ui-icon cms-icon-recent" /><span class="cms-button-text">Recent</span></button>\
            <button name="Save" title="Save"  class="cms-right ui-state-default ui-corner-all cms-deactivated"><span class="ui-icon cms-icon-save"/>&nbsp;</button>\
            <button name="Sitemap" title="Sitemap" class="cms-right ui-state-default ui-corner-all"><span class="ui-icon cms-icon-sitemap"/>&nbsp;</button>\
            <button name="Publish" title="Publish" class="cms-right ui-state-default ui-corner-all"><span class="ui-icon cms-icon-publish"/>&nbsp;</button>\
        </div>\
     </div></div>';
   
   var searchDialog = cms.html.searchDialog = function(/**Array*/types) {
      var/**String*/ code = '<div id="' + searchDialogId + '"><form><ol class="ade-forms">';
      code += '<li><label for="cms-search-path">Path </label><input type="text" name="cms-search-path" class="cms-search-path" id="cms-search-path"></input></li>';
      code += '<li class="ade-required"><label for="cms-search-query">Query <span class="ade-required">*</span></label><input type="text" name="cms-search-query" class="cms-search-query" id="cms-search-query"></input></li>';
      code += '<li class="ade-required ade-grouping"><fieldset><legend>Resource Types <span class="ade-required">*</span></legend>';
      $.each(types, function() {
         code += '<input type="checkbox" name="cms-search-type" class="cms-search-type" id="' + searchTypePrefix + this.type + '" value="' + this.type + '" checked="checked"/><label for="cms-type-' + this.type + '">' + this.name + '</label>';
      });
      code += '</fieldset></li></ol></form></div>';
      return code;
   }
})(cms);
