(function(cms) {
   var M = cms.messages;
   var recentMenuId = cms.html.recentMenuId = 'recentlist';
   var recentListId = cms.html.recentListId = 'recent_list_items';
   var favoriteMenuId = cms.html.favoriteMenuId = 'favoritelist';
   var favoriteListId = cms.html.favoriteListId = 'favorite_list_items';
   
   var galleryMenuId = cms.html.galleryMenuId ='cms-gallery';
   var galleryTypeListId = cms.html.galleryTypeListId ='cms-type-list';
   var galleryResultListPrefix = cms.html.galleryResultListPrefix ='searchresults_page';
   
   var favoriteDropMenuId = cms.html.favoriteDropMenuId = 'favorite-drop';
   var favoriteDropListId = cms.html.favoriteDropListId = 'favorite-drop-list';
   
   var favoriteDialogId = cms.html.favoriteDialogId = 'fav-dialog';
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
        <ul id="' +   recentListId +    '" class="cms-item-list"></ul>\
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
	        <button id="fav-edit" name="Edit_Favorites" title="'+M.EDIT_FAVORITES_BUTTON+'" class="ui-state-default ui-corner-all">'+M.EDIT_FAVORITES_BUTTON+'</button>\
	    </ul>\
	</div></div>';
   
   /**
    * Creates the drop zone for favorites.
    *
    */
   var createFavDrop = cms.html.createFavDrop = function() {
      var menuId = cms.html.favoriteDropMenuId;
      var listId = cms.html.favoriteDropListId;
      
      var html = ['<div id="', menuId, '" class="cms-menu" style="width: 343px; display:none">\
    	<div class="connect ui-corner-top"></div>\
    	<div class="ui-widget-shadow ui-corner-all"></div>\
    	<div class="cms-menu-content ui-widget-content ui-corner-bottom ui-corner-tl">\
    	    <ul id="', listId, '" class="cms-item-list"></ul>\
    	</div></div>'];
      
      return html.join('');
   }
   
   
   
   var createMenu = cms.html.createMenu = function(menuId) {
      var listId = '';
      var addMenuItem = '';
      if (menuId == favoriteMenuId) {
         listId = favoriteListId;
         addMenuItem = '<button id="fav-edit" name="Edit_Favorites" title="'+M.EDIT_FAVORITES_BUTTON+'" class="ui-state-default ui-corner-all cms-edit-favorites">'+M.EDIT_FAVORITES_BUTTON+'</button>';
      } else if (menuId == recentMenuId) {
         listId = recentListId;
      } else {
         listId = menuId + "-list";
      }
      
      var html = ['<div id="', menuId, '" class="cms-menu" style="width: 343px; display:none">\
    	<div class="connect ui-corner-top"></div>\
    	<div class="ui-widget-shadow ui-corner-all" style="width: 345px"></div>\
    	<div class="cms-menu-content ui-widget-content ui-corner-bottom ui-corner-tl">\
            ', addMenuItem, '\
            <div class="cms-scrolling ui-corner-all">\
                <ul id="', listId, '" class="cms-scrolling-inner cms-item-list">\
                </ul>\
            </div>\
        </div></div>'];
      
      return html.join('');
   }
   
   var createGalleryMenu = cms.html.createGalleryMenu = function(){
       var html=['<div id="',galleryMenuId,'" class="cms-menu" style="width: 650px;">\
    	<div class="connect ui-corner-top" style="background: #BFBFBF;"></div>\
    	<div class="ui-widget-shadow ui-corner-all"></div>\
        <div id="cms-gallery-main" class="ui-corner-all">\
		    <div id="cms-gallery-tabs">\
                <ul>\
                     <li><a href="#tabs-result">Search results</a></li>\
                     <li><a href="#tabs-types">Type</a></li>\
                     <li><a href="#tabs-galleries">Galleries</a></li>\
                     <li><a href="#tabs-categories">Categories</a></li>\
                     <li><a href="#tabs-fulltextsearch">Full Text Search</a></li>\
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
              <button class="cms-search-button ui-corner-all ui-state-default">'+M.SEARCH_BUTTON+'</button>\
              <div class="cms-scrolling" style="display:none">\
                  <ul id="cms-search-list"  class="cms-scrolling-inner cms-item-list">\
                  </ul>\
              </div>\
              <div class="cms-loading">'+M.PLEASE_START_SEARCH+'</div>\
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
      $content.prepend(deleteIcon);
      $content.find('div.cms-list-itemcontent').append('<a class="cms-handle cms-move"></a>');
      return cms.util.jqueryToHtml($content);
   }
   
   
   /**
    * Creates the HTML for an item in the Fav/Recent menu from an element.<p>
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
	    <div id="toolbar_content" class="cms-toolbar-content"></div>\
     </div></div>';
   
   var toolbarOverlay = cms.html.toolbarOverlay = '<div id="' + toolbarOverlayId + '"></div>';
   
   var searchDialog = cms.html.searchDialog = function(/**Array*/types) {
      var/**String*/ html = ['<div id="', searchDialogId, '">\
          <form>\
              <ol class="ade-forms">\
                   <li class="ade-required">\
                       <label for="cms-search-query">'+M.SEARCH_DIALOG_QUERY+'<span class="ade-required">*</span></label>\
                       <input type="text" name="cms-search-query" class="cms-search-query" id="cms-search-query"></input>\
                   </li>\
                   <li><a href="#" class="cms-advanced-search cms-to-basic-search" style="display:none; text-decoration:underline">'+M.SEARCH_DIALOG_BASIC+'</a></li>\
                   <li><a href="#" class="cms-basic-search cms-to-advanced-search" style="text-decoration:underline">'+M.SEARCH_DIALOG_ADVANCED+'</a></li>\
                   <li class="cms-advanced-search" style="display:none">\
                      <label for="cms-search-path">'+M.SEARCH_DIALOG_PATH+'</label>\
                      <input type="text" name="cms-search-path" class="cms-search-path" id="cms-search-path"></input>\
                   </li>\
                   <br>\
                   <li class="cms-advanced-search" style="display:none"><b>'+M.SEARCH_DIALOG_TYPES+'</b><span class="ade-required">*</span></li>\
                   <li class="cms-advanced-search ade-required ade-grouping" style="display:none">\
                       <div class="cms-search-type-list">'];
      
      $.each(types, function() {
         html.push('<input type="checkbox" name="cms-search-type" class="cms-search-type" id="' + searchTypePrefix + this.type + '" value="' + this.type + '" checked="checked"/>');
         html.push('<label for="cms-type-' + this.type + '">' + this.name + '</label>');
      });
      
      html.push('</div></li></ol></form></div>');
      return html.join('');
   }
   
   
   
   
})(cms);
