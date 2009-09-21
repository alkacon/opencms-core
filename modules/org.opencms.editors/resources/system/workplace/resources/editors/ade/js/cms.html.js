(function(cms) {

   var recentMenuId = cms.html.recentMenuId = 'recentlist';
   var recentListId = cms.html.recentListId = 'recent_list_items';
   var favoriteMenuId = cms.html.favoriteMenuId = 'favoritelist';
   var favoriteListId = cms.html.favoriteListId = 'favorite_list_items';
   
   var favoriteDropMenuId = cms.html.favoriteDropMenuId = 'favorite-drop';
   var favoriteDropListId = cms.html.favoriteDropListId = 'favorite-drop-list';
   
   var favoriteDialogId = cms.html.favoriteDialogId = 'fav-dialog';
   var publishDialogId = cms.html.publishDialogId = 'publishlist';
   var toolbarId = cms.html.toolbarId = 'toolbar';
   var toolbarOverlayId = cms.html.toolbarOverlayId = 'toolbarOverlay';
   
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
   
   var subcontainerItemStartHtml = '<li class="cms-subcontainer-item cms-item"><a class="cms-handle cms-move"></a><ul>';
   var subcontainerItemEndHtml = '</ul></li>';
   
   
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
   
   
   /**
    * Creates the drop zone for favorites.
    *
    */
   var createFavDrop = cms.html.createFavDrop = function() {
      var menuId = cms.html.favoriteDropMenuId;
      var listId = cms.html.favoriteDropListId;
      
      var html = ['<div id="', menuId, '" class="cms-menu" style="display:none">\
    	<div class="connect ui-corner-top"></div>\
    	<div class="ui-widget-shadow ui-corner-all"></div>\
    	<div class="ui-widget-content ui-corner-bottom ui-corner-tl">\
    	    <ul id="', listId, '" class="cms-item-list"></ul>\
    	</div></div>'];
      
      return html.join('');
   }
   
   
   
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
      
      var html = ['<div id="', menuId, '" class="cms-menu" style="width: 343px; display:none">\
    	<div class="connect ui-corner-top"></div>\
    	<div class="ui-widget-shadow ui-corner-all" style="width: 345px"></div>\
    	<div class="ui-widget-content ui-corner-bottom ui-corner-tl">\
            ', addMenuItem, '\
            <div class="cms-scrolling">\
                <ul id="', listId, '" class="cms-scrolling-inner cms-item-list">\
                </ul>\
            </div>\
        </div></div>'];
      
      return html.join('');
   }
   
   
   var searchMenu = cms.html.searchMenu = '\
      <div id="cms-search" class="cms-menu" style="width: 343px; display:none">\
          <div class="connect ui-corner-top"></div>\
          <div class="ui-widget-shadow ui-corner-all" style="width: 345px"></div>\
          <div class="ui-widget-content ui-corner-bottom ui-corner-tl">\
              <button class="cms-search-button ui-corner-all ui-state-default">Search</button>\
              <div class="cms-scrolling" style="display:none">\
                  <ul id="cms-search-list"  class="cms-scrolling-inner cms-item-list">\
                  </ul>\
              </div>\
              <div class="cms-loading">Please start your search</div>\
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
      var $content = createItemFavListHtml(item.id);
      $content.prepend(deleteIcon).append('<br style="clear:both;" />');
      $('.cms-move', $content).remove();
      $content.children('.ui-widget-content').addClass('cms-left');
      return cms.util.jqueryToHtml($content);
   }
   
   
   /**
    * Creates the HTML for an item in the New/Fav/Recent menu from an element.<p>
    *
    * @param {String} id the id of the element for which the HTML should be generated
    */
   var createItemFavListHtml = cms.html.createItemFavListHtml = function(id) {
      var/**Object*/ elem = cms.data.elements[id];
      return formatFavListItem(elem);
   }
   
   /**
    * Formats a favorite list item.<p>
    *
    * @param element the CmsElement to format as a favorite list item
    * @return the jQuery object representing the formatted element
    */
   var formatFavListItem = cms.html.formatFavListItem = /*jQuery*/ function(/*Object*/element) {
      if (element.subItems) {
         var result = subcontainerItemStartHtml;
         for (var i = 0; i < element.subItems.length; i++) {
            var subElement = cms.data.elements[element.subItems[i]];
            result += subElement.getContent('_DEFAULT_');
         }
         result += subcontainerItemEndHtml;
         return $(result).attr('rel', element.id).addClass('cms-element');
      } else {
         return element.getContent('_DEFAULT_');
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
        </div>\
     </div></div>';
   
   var toolbarOverlay = cms.html.toolbarOverlay = '<div id="' + toolbarOverlayId + '"></div>';
   
   var searchDialog = cms.html.searchDialog = function(/**Array*/types) {
      var/**String*/ html = ['<div id="', searchDialogId, '">\
          <form>\
              <ol class="ade-forms">\
                   <li class="ade-required">\
                       <label for="cms-search-query">Query <span class="ade-required">*</span></label>\
                       <input type="text" name="cms-search-query" class="cms-search-query" id="cms-search-query"></input>\
                   </li>\
                   <li>\
                      <label for="cms-search-path">Path</label>\
                      <input type="text" name="cms-search-path" class="cms-search-path" id="cms-search-path"></input>\
                   </li>\
                   <br>\
                   <li><b>Resource Types</b><span class="ade-required">*</span></li>\
                   <li class="ade-required ade-grouping">\
                       <div class="cms-search-type-list">'];
      
      $.each(types, function() {
         html.push('<input type="checkbox" name="cms-search-type" class="cms-search-type" id="' + searchTypePrefix + this.type + '" value="' + this.type + '" checked="checked"/>');
         html.push('<label for="cms-type-' + this.type + '">' + this.name + '</label>');
      });
      
      html.push('</div></li></ol></form></div>');
      return html.join('');
   }
   
   
   
   
   
   
   
   
   
   
   
})(cms);
