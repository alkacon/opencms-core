(function(cms) {

   /** html-class for additional info. */
   var classAdditionalInfo = 'cms-additional-info';
   
   /** html-class to show additional info. */
   var classAdditionalShow = 'cms-additional-show';
   
   /** html-class for closed item. */
   var classClosed = 'cms-closed';
   
   /** html-class for opener handle. */
   var classOpener = 'cms-opener';
   
   /** html-class for direct input. */
   var classDirectInput = 'cms-direct-input';
   
   /** html-class for subtree items within menu-lists to hide sub-pages. */
   var classForceClosed = 'cms-force-closed';
   
   /** html-class for hovered elements. */
   var classHovered = 'cms-hovered';
   
   /** html-class for subtree items. */
   var classSubtree = 'cms-subtree';
   
   /** html-class for dragged item. */
   var dragClass = 'cms-dragging';
   
   var helperClass = 'cms-drag-helper';
   
   /** html-class for dropzone. */
   var dropzoneClass = 'cms-dropzone';
   
   /** html-class for sitemap-item. */
   var itemClass = 'cms-sitemap-item';
   
   var classUrlName = 'cms-url-name';
   var classVfsPath = 'cms-vfs-path';
   var classSitemapEntry = 'cms-sitemap-entry';
   
   /** html-code for opener handle. */
   var openerHtml = '<span class="' + classOpener + '"></span>';
   
   /** html-id for sitemap. */
   var sitemapId = 'cms-sitemap';
   
   /** Map of jquery objects. */
   cms.sitemap.dom = {};
   cms.sitemap.favorites = [];
   cms.sitemap.recent = [];
   var MAX_RECENT = 10;
   
   var ACTION_SAVE = 'save';
   var ACTION_GET = 'get';
   var ACTION_SET = 'set';
   var ACTION_ALL = 'all';
   var ACTION_VALIDATE = 'validate';
   
   
   /**
    * Timer object used for delayed hover effect.
    */
   var timer = cms.sitemap.timer = {
      id: null,
      handleDiv: null,
      adeMode: null
   };
   
   
   /**
    * Adds a sitemap entry to the recent list
    * @param {Object} entry the sitemap entry as JSON to add to the recent list
    */
   var _addRecent = function(entry) {
      cms.sitemap.recent.unshift(entry);
      while (cms.sitemap.recent.length > MAX_RECENT) {
         cms.sitemap.recent.pop();
      }
   }
   
   /**
    * Adds a sitemap entry to the list of favorites.
    * @param {Object} entry the sitemap entry as JSON to add to the favorites
    */
   var _addFavorite = function(entry) {
      cms.sitemap.favorites.unshift(entry);
   }
   
   /**
    * Adjusts the shadow of the given menu.
    *
    * @param {Object} menu the menu jQuery-object
    */
   var adjustMenuShadow = cms.sitemap.adjustMenuShadow = function(menu) {
      $('div.ui-widget-shadow', menu).css({
         width: menu.outerWidth() + 8,
         height: menu.outerHeight() + 1
      });
   }
   
   /**
    * Asks the user whether he really wants to set the url name for a sitemap entry.
    * If he clicks OK, the item will be renamed.
    *
    * @param {Object} elem a DOM element in the sitemap entry to be renamed
    * @param {Object} newValue the new URL name
    */
   var _askSetURLName = function(elem, newValue) {
      var currentLi = elem.closest('li');
      var currentEntry = new SitemapEntry(currentLi);
      var $ul = currentLi.closest('ul');
      var otherUrlNames = getOtherUrlNames($ul, currentLi.get(0));
      if (otherUrlNames[newValue]) {
         cms.util.dialogAlert('The url name "' + newValue + '" already exists at this level.', 'Duplicate URL name');
         return;
      }
      var _renameEntry = function() {
         var previousUrl = currentEntry.getUrl();
         var parentUrl = previousUrl.substring(0, previousUrl.lastIndexOf('/'));
         currentEntry.setUrlName(newValue);
         currentEntry.setUrls(parentUrl);
      }
      var _destroy = function() {
         $dialog.dialog('destroy');
         $dialog.remove();
      }
      if (currentLi.children('ul').length) {
         var $dialog = $('<div id="cms-alert-dialog" style="display: none"></div>');
         $dialog.appendTo('body');
         $dialog.append('<p style="margin-bottom: 4px;">The page you are editing has subpages. Changing its URL will also change the URLs of those.<br />Do you want to change it anyway?</p>');
         $dialog.dialog({
            zIndex: 9999,
            title: 'Changing URL name to "' + newValue + '"',
            modal: true,
            close: function() {
               _destroy();
            },
            buttons: {
               'OK': function() {
                  _renameEntry();
                  _destroy();
               },
               'Cancel': function() {
                  _destroy();
               }
            }
         });
      } else {
         _renameEntry();
      }
   }
   
   /**
    * setValue handler for the directInput of url name fields
    * @param {Object} elem
    * @param {Object} input
    */
   var directInputSetUrlName = function(elem, input) {
      var previous = elem.attr('alt');
      var current = input.val();
      cms.data.convertUrlName(current, function(newValue) {
         if (previous != newValue) {
            _askSetURLName(elem, newValue);
         }
         elem.css('display', '');
         input.remove();
      });
   }
   
   /**
    * Adds handles for the given modes to the given item.
    *
    * @param {Object} elem the tree item
    * @param {Object} modes array of mode-objects
    */
   var addHandles = function(elem, modes) {
      var handleDiv = $('div.cms-handle', elem).empty();
      if (!handleDiv.length) {
         handleDiv = $('<div class="cms-handle"></div>').appendTo(elem);
      }
      var handles = {}
      for (i = 0; i < modes.length; i++) {
         var mode = modes[i];
         if (mode.createHandle && $.isFunction(mode.createHandle)) {
            handles[mode.name] = mode.createHandle();
         }
      }
      handles[cms.sitemap.currentMode.name].appendTo(handleDiv);
      for (handleName in handles) {
         if (handleName != cms.sitemap.currentMode.name) {
            handles[handleName].appendTo(handleDiv).css('display', 'none');
         }
      }
      handleDiv.hover(function() {
         drawBorder(elem, 2);
         cms.sitemap.startHoverTimeout(handleDiv, cms.sitemap.currentMode.name);
      }, function() {
         cms.sitemap.stopHover();
         $('.cms-hovering', elem).remove();
      });
   }
   
   /**
    * Copies css-properties form one item to another ('width', 'height', 'font-size', 'font-weight',
    * 'font-family', 'line-height', 'color', 'padding-top', 'padding-right', 'padding-bottom',
    * 'padding-left', 'margin-top', 'margin-right', 'margin-bottom', 'margin-left').
    *
    * @param {Object} orig the original item
    * @param {Object} target the target item
    */
   var copyCss = function(orig, target) {
      // list of styles that will be copied. 'display' is left out intentionally.
      var styleNames = ['width', 'height', 'font-size', 'font-weight', 'font-family', 'line-height', 'color', 'padding-top', 'padding-right', 'padding-bottom', 'padding-left', 'margin-top', 'margin-right', 'margin-bottom', 'margin-left'];
      var styles = {};
      for (i = 0; i < styleNames.length; i++) {
         styles[styleNames[i]] = orig.css(styleNames[i]);
      }
      target.css(styles);
   }
   
   /**
    * Creates an initialized toolbar button object and inserts it into the dom. This function is to be called on a mode-object only.
    *
    */
   var createButton = function() {
      var self = this;
      if (self.wide) {
         self.button = makeWideButton(self.name, self.title, 'cms-icon-' + self.name);
      } else {
         self.button = makeModeButton(self.name, self.title, 'cms-icon-' + self.name);
      }
      self.button.click(function() {
      
         if ((cms.sitemap.currentMode == null) || (cms.sitemap.currentMode.name != self.name)) {
            var dummyLoader = function(callback) {
               callback();
            }
            var loader = self.load || dummyLoader;
            loader.call(self, function() {
               if (cms.sitemap.currentMode != null) {
                  cms.sitemap.currentMode.disable();
               }
               cms.sitemap.currentMode = self;
               self.enable();
            });
         } else {
            self.disable();
            cms.sitemap.currentMode = null;
         }
      });
      if (self.floatRight) {
         self.button.removeClass('cms-left').addClass('cms-right');
      }
      if ($.isFunction(self.init)) {
         self.init();
      }
      return self.button;
   };
   
   /**
    * Click-handler that will delete the item of this handler after showing a confirmation-dialog.
    *
    */
   var deletePage = function() {
      var liItem = $(this).closest('li');
      var _deleteEntry = function() {
         if (!liItem.siblings().length) {
            var parentUl = liItem.parent();
            parentUl.siblings('span.' + classOpener).remove();
            parentUl.remove();
         }
         liItem.remove();
         
         setSitemapChanged(true);
         var sitemapEntry = new SitemapEntry(liItem.get(0));
         _addRecent(sitemapEntry.serialize(false));
         cms.data.saveRecent(function(ok, data) {
                  });
      }
      if (isLastRootEntry(liItem)) {
         cms.util.dialogAlert("You can't delete this entry because it is the last remaining root level entry.");
         return;
      }
      if (liItem.children('ul').length) {
         var $dialog = $('<div id="cms-alert-dialog" style="display: none"></div>');
         var _destroy = function() {
            $dialog.dialog('destroy');
            $dialog.remove();
         }
         $dialog.appendTo('body');
         $dialog.text('Do you realy want to remove this page and all sub-pages from the sitemap?');
         $dialog.dialog({
            zIndex: 9999,
            title: 'Delete',
            modal: true,
            close: _destroy,
            buttons: {
               'OK': function() {
                  _destroy();
                  // we don't want sub-pages in the recent list
                  liItem.find('ul').remove();
                  _deleteEntry();
               },
               'Cancel': _destroy()
            }
         });
      } else {
         _deleteEntry();
      }
   }
   
   /**
    * Removes drag and drop from tree.
    *
    */
   var destroyDraggable = cms.sitemap.destroyDraggable = function() {
      $('#' + sitemapId + ' div.' + itemClass + ', #' + sitemapId + '.cms-sitemap-entry:not(.cms-sub-sitemap) > .' + dropzoneClass).droppable('destroy');
      $('li').draggable('destroy');
   }
   
   /**
    * Highlighting function. This will draw a border around the given element.
    *
    * @param {Object} elem the element
    * @param {Object} hOff the border offset
    * @param {Object} additionalClass additional css-class
    */
   var drawBorder = cms.sitemap.drawBorder = function(elem, hOff, additionalClass) {
      elem = $(elem);
      var tHeight = elem.outerHeight();
      var tWidth = elem.outerWidth();
      var hWidth = 2;
      var lrHeight = tHeight + 2 * (hOff + hWidth);
      var btWidth = tWidth + 2 * (hOff + hWidth);
      if (!additionalClass) {
         additionalClass = '';
      }
      var tlrTop = -(hOff + hWidth);
      var tblLeft = -(hOff + hWidth);
      // top
      $('<div class="cms-hovering cms-hovering-top"></div>').addClass(additionalClass).height(hWidth).width(btWidth).css('top', tlrTop).css('left', tblLeft).appendTo(elem);
      
      // right
      $('<div class="cms-hovering cms-hovering-right"></div>').addClass(additionalClass).height(lrHeight).width(hWidth).css('top', tlrTop).css('left', tWidth + hOff).appendTo(elem);
      // left
      $('<div class="cms-hovering cms-hovering-left"></div>').addClass(additionalClass).height(lrHeight).width(hWidth).css('top', tlrTop).css('left', tblLeft).appendTo(elem);
      // bottom
      $('<div class="cms-hovering cms-hovering-bottom"></div>').addClass(additionalClass).height(hWidth).width(btWidth).css('top', tHeight + hOff).css('left', tblLeft).appendTo(elem);
   }
   
   /**
    * Checks for duplicate url names before a sitemap entry is dropped.
    * @param {Object} $dragged the dragged sitemap entry
    * @param {Object} $dropzone the dropzone into which the entry should be dropped
    */
   var checkDuplicateOnDrop = function($dragged, $dropzone) {
      var currentEntry = new SitemapEntry($dragged.get(0));
      var otherUrlNames = {};
      if ($dropzone.hasClass(itemClass)) {
         var $targetUl = $dropzone.siblings('ul');
         if ($targetUl.size() > 0) {
            otherUrlNames = getOtherUrlNames($targetUl, $dragged.get(0));
         }
      } else {
         var $targetUl = $dropzone.closest('ul');
         otherUrlNames = getOtherUrlNames($targetUl, $dragged.get(0));
      }
      var urlName = currentEntry.getUrlName();
      return {
         duplicate: !!otherUrlNames[urlName],
         urlName: urlName,
         otherUrlNames: otherUrlNames
      };
   }
   
   /**
    * Handler for dropping sitemap entry into favorites
    * @param {Object} e event
    * @param {Object} ui ui-object
    * @param {Object} $dropzone the drop zone (in this case, the favorite list)
    */
   var dropItemIntoFavorites = function(e, ui, $dropzone) {
      var $dragged = ui.draggable;
      
      $dropzone.removeClass(classHovered);
      var dragClone = ui.draggable.clone();
      dragClone.find('div.cms-handle').remove();
      var _next = function() {
         dragClone.find('div.' + itemClass).removeClass(dragClass);
         dragClone.find('.' + classAdditionalShow).removeClass(classAdditionalShow);
         dragClone.appendTo(cms.sitemap.dom.favoriteList);
         var dragCloneEntry = new SitemapEntry(dragClone.get(0));
         var newFav = dragCloneEntry.serialize(false);
         
         _addFavorite(newFav);
         cms.data.saveFavorites(function() {
                  });
      }
      if (dragClone.find('ul').length) {
         keepTree(dragClone, _next);
      } else {
         _next();
      }
   }
   
   /**
    * Returns the parent URL for a given dropzone.
    * @param {Object} $dropzone the dropzone for which the parent URL should be returned
    */
   var getDropzoneParentUrl = function($dropzone) {
      var parentURL = '';
      var $li = $dropzone.parent();
      var dropzoneEntry = new SitemapEntry($li.get(0));
      if ($dropzone.hasClass(dropzoneClass)) {
         var $parentLi = $li.parent().closest('li');
         // drop zone is at root level
         if ($parentLi.size() == 0) {
            return '';
         }
         var parentEntry = new SitemapEntry($parentLi.get(0));
         
         if ($parentLi.length) {
            parentURL = parentEntry.getUrl();
            parentURL = parentURL.substring(0, parentURL.lastIndexOf('.'));
         }
      } else {
         parentURL = dropzoneEntry.getUrl();
         parentURL = parentURL.substring(0, parentURL.lastIndexOf('.'));
      }
      return parentURL;
   }
   
   
   /**
    * Event-handler for droppable drop-event dragging within tree.
    *
    * @param {Object} e event
    * @param {Object} ui ui-object
    */
   var dropItem = cms.sitemap.dropItem = function(e, ui) {
      // target item
      var $dropzone = $(this);
      var $dragged = ui.draggable;
      if ($dropzone.attr('id') == 'favorite-drop-list') {
         dropItemIntoFavorites(e, ui, $dropzone);
         return;
      }
      var li = $(this).parent();
      var dropzoneEntry = new SitemapEntry(li.get(0));
      var formerParent = ui.draggable.parent();
      var draggedEntry = new SitemapEntry($dragged.get(0));
      // if the former parent li and the target li are not the same
      // and the former parent is going to lose it's last child 
      // (the currently dragged item will appear twice, once as the original and once as the ui-helper),
      // set the removeFormerParent flag
      var parentUl = null;
      if ($dropzone.hasClass(dropzoneClass)) {
         parentUl = li.closest('ul').get(0);
      } else {
         parentUl = li.children('ul').get(0);
      }
      var sameParent = parentUl == formerParent.get(0);
      
      formerParent.parent().css('border')
      var removeFormerParent = (li.get(0) != formerParent.parent().get(0) && formerParent.children().length == 2);
      
      var _completeDrop = function() {
         if ($dropzone.hasClass(dropzoneClass)) {
            // dropping over dropzone, so insert before
            li.before($dragged);
         } else {
            // dropping over item, so insert into child-ul
            if (li.children('ul').length == 0) {
               // no child-ul present yet
               li.append('<ul/>');
               li.children('div.' + dropzoneClass).after(openerHtml);
            }
            li.removeClass(classClosed);
            li.children('ul').append($dragged);
         }
         setSitemapChanged(true);
         draggedEntry.setUrls(getDropzoneParentUrl($dropzone));
         // remove the now empty former parent ul and the opener span
         if (removeFormerParent) {
            formerParent.siblings('span.' + classOpener).andSelf().remove();
         }
         $dropzone.removeClass(classHovered);
      }
      var li = $(this).parent();
      var duplicateStatus = checkDuplicateOnDrop($dragged, $(this));
      if (!sameParent && duplicateStatus.duplicate) {
         var otherUrlNames = duplicateStatus.otherUrlNames;
         showEntryEditor(draggedEntry, true, otherUrlNames, function(newTitle, newUrlName, newProperties) {
            var previousUrl = draggedEntry.getUrl();
            var parentUrl = previousUrl.substring(0, previousUrl.lastIndexOf('/'));
            draggedEntry.setUrls(parentUrl);
            _completeDrop();
         }, function() {
            $dropzone.removeClass(classHovered);
            return;
         });
      } else {
         _completeDrop();
      }
   }
   
   /**
    * Event-handler for droppable drop-event dragging from menu into tree.
    *
    * @param {Object} e event
    * @param {Object} ui ui-object
    */
   var dropItemMenu = cms.sitemap.dropItemMenu = function(e, ui) {
      var $dropzone = $(this);
      var li = $dropzone.parent();
      var $dropClone = ui.draggable.clone();
      _addOpenerAndDropzone($dropClone);
      $dropClone.removeClass(classSubtree);
      var parentURL = '';
      var dropEntry = new SitemapEntry($dropClone.get(0));
      
      var _completeDrop = function() {
         if ($dropzone.hasClass(dropzoneClass)) {
            // dropping over dropzone, so insert before
            li.before($dropClone);
            var parentLi = li.parent().closest('li');
            if (parentLi.length) {
               parentURL = parentLi.children('div.' + itemClass).find('span.cms-url').attr('alt');
               parentURL = parentURL.substring(0, parentURL.lastIndexOf('.'));
            }
         } else {
            // dropping over item, so insert into child-ul
            if (li.children('ul').length == 0) {
               // no child-ul present yet
               li.append('<ul/>');
               li.children('div.' + dropzoneClass).after(openerHtml);
            }
            li.removeClass(classClosed);
            li.children('ul').append($dropClone);
            parentURL = li.children('div.' + itemClass).find('span.cms-url').attr('alt');
            parentURL = parentURL.substring(0, parentURL.lastIndexOf('.'));
         }
         $dropClone.find('div.cms-handle').remove();
         $dropClone.find('div.' + itemClass).removeClass(dragClass);
         dropEntry.setUrls(parentURL);
         $dropzone.removeClass(classHovered);
         setSitemapChanged(true);
      }
      
      var duplicateStatus = checkDuplicateOnDrop($dropClone, $dropzone);
      if (duplicateStatus.duplicate) {
         var otherUrlNames = duplicateStatus.otherUrlNames;
         showEntryEditor(dropEntry, true, otherUrlNames, function(newTitle, newUrlName, newProperties) {
            _completeDrop();
         }, function() {
            $dropzone.removeClass(classHovered);
            return;
         });
      } else {
         _completeDrop();
      }
   }
   
   
   /**
    * Click-handler to edit the item title and properties for this handler.
    *
    */
   var editPage = function() {
      var itemDiv = $(this).closest('div.' + itemClass);
      var $entry = itemDiv.closest('li');
      var currentEntry = new SitemapEntry($entry.get(0));
      var $ul = $entry.closest('ul');
      var otherUrlNames = getOtherUrlNames($ul, $entry.get(0));
      showEntryEditor(currentEntry, true, otherUrlNames, function(newTitle, newUrlName, newProperties) {
         var previousUrl = currentEntry.getUrl();
         var parentUrl = previousUrl.substring(0, previousUrl.lastIndexOf('/'));
         currentEntry.setUrls(parentUrl)
         setSitemapChanged(true);
      });
   }
   
   /**
    * Returns the URL names of the sitemap entries inside a given ul  which are not identical to a given entry
    * @param {Object} $ul the ul from which the url names should be retrieved
    * @param {Object} newItem the DOM element for which the URL name shouldn't be included in the result
    * @return an object containing the collected url names as properties (which all have the value true)
    **/
   var getOtherUrlNames = function($ul, newItem) {
      var result = {};
      $ul.children('li').each(function() {
         if (this != newItem) {
            result[$(this).find('.cms-url-name:first').attr('alt')] = true;
         }
      });
      return result;
   }
   
   
   /**
    * Merges a set of property definition with a set of properties
    *
    * @param {Object} propDefs object containing the property definitions
    * @param {Object} properties object containing the properties
    *
    **/
   var mergePropertiesWithDefinitions = function(propDefs, properties) {
      var result = JSON.parse(JSON.stringify(propDefs));
      for (var key in result) {
         if (properties.hasOwnProperty(key)) {
            result[key].value = properties[key];
         }
      }
      return result;
   }
   
   
   /**
    * Returns the properties of a sitemap entry, merged with the property settings of the sitemap.
    *
    * @param {Object} $entry the sitemap entry for which the properties should be read
    * @return an object containing the merged properties with their settings
    */
   var getMergedProperties = function($entry) {
      var propDefs = JSON.parse(JSON.stringify(cms.sitemap.propertyDefinitions)).properties;
      var entryData = getEntryData($entry);
      var props = entryData.properties;
      var propEntries = mergePropertiesWithDefinitions(propDefs, props);
      return propEntries;
   }
   
   /**
    * Checks whether a given sitemap entry is the last remaining entry at the root level of the sitemap.
    * @param {Object} $entry the entry which should be checked
    */
   var isLastRootEntry = function($entry) {
      var $parent = $entry.parent();
      return $parent.attr('id') == sitemapId && $parent.children().size() == 1;
   }
   
   
   /**
    * Initializes the sitemap editor.
    *
    * @param {boolean} allowEdit if true, sets the event handlers for editing the sitemap
    * @param {boolean} displayToolbar if to display the toolbar or not
    */
   var initSitemap = cms.sitemap.initSitemap = function(allowEdit, displayToolbar) {
      // setting options for draggable and sortable for dragging within tree
      cms.sitemap.dragOptions = {
         handle: ' > div.' + itemClass + '> div.cms-handle > a.cms-move',
         opacity: 0.8,
         addClasses: false,
         helper: 'clone',
         zIndex: 11000,
         start: cms.sitemap.startDrag,
         stop: cms.sitemap.stopDrag
      };
      cms.sitemap.dropOptions = {
         accept: 'li',
         tolerance: 'pointer',
         drop: cms.sitemap.dropItem,
         over: cms.sitemap.overDrop,
         out: cms.sitemap.outDrop
      };
      
      // setting options for draggable and sortable for dragging from menu to tree
      cms.sitemap.dragOptionsMenu = $.extend({}, cms.sitemap.dragOptions, {
         start: cms.sitemap.startDragMenu,
         stop: cms.sitemap.stopDragMenu
      });
      cms.sitemap.dropOptionsMenu = $.extend({}, cms.sitemap.dropOptions, {
         drop: cms.sitemap.dropItemMenu
      });
      
      $('#' + sitemapId).children().each(function() {
         (new SitemapEntry(this)).setUrls('');
      });
      
      if (displayToolbar) {
         // generating toolbar
         cms.sitemap.dom.toolbar = $(cms.html.toolbar).appendTo(document.body);
         cms.sitemap.dom.toolbarContent = $('#toolbar_content', cms.sitemap.dom.toolbar);
         cms.sitemap.currentMode = null;
         //create buttons:
         for (i = 0; i < sitemapModes.length; i++) {
            sitemapModes[i].create().appendTo(cms.sitemap.dom.toolbarContent);
         }
         cms.sitemap.dom.favoriteDrop.css({
            top: '35px',
            left: $('button[name="favorites"]').position().left - 1 + 'px'
         });
         
         // preparing tree for drag and drop
         //$('#' + sitemapId + ' li:has(ul)').prepend(openerHtml);
         //$('#' + sitemapId + ' li').prepend('<div class="' + dropzoneClass + '"></div>')
      }
      
      // assigning event-handler
      $('#' + sitemapId + ' span.' + classOpener).live('click', function() {
         $(this).parent().toggleClass(classClosed);
      });
      if (allowEdit && displayToolbar) {
         $('a.cms-delete').live('click', deletePage);
         $('a.cms-new').live('click', newPage);
         $('a.cms-edit').live('click', editPage);
         $('#' + sitemapId + ' div.' + itemClass + ' h3').directInput({
            marginHack: true,
            live: true,
            valueChanged: function() {
               setSitemapChanged(true);
            }
         });
         
         $('#' + sitemapId + ' div.' + itemClass + ' span.cms-url-name').directInput({
            marginHack: true,
            live: true,
            readValue: function(elem) {
               return elem.attr('alt');
            },
            setValue: directInputSetUrlName,
            valueChanged: function() {
               setSitemapChanged(true);
            }
         });
         $('#fav-edit').click(_editFavorites);         
         $(window).unload(onUnload);
      }
      
      $('a.cms-icon-triangle').live('click', function() {
         $(this).parent().toggleClass(classAdditionalShow);
         var menu = $(this).closest('.cms-menu');
         if (menu.length) {
            adjustMenuShadow(menu);
         }
      });
      $('.' + classAdditionalInfo + ' span').jHelperTip({
         trigger: 'hover',
         source: 'attribute',
         attrName: 'alt',
         topOff: -20,
         opacity: 0.8,
         live: true
      });
      $('.cms-vfs-path').live('click', function() {
         var target = cms.data.CONTEXT + $(this).attr('alt');
         window.open(target, '_blank');
         //showLeaveDialog(target, 'Do you really want to leave the sitemap editor and discard your changes?', 'Leaving editor');
      });
      $('#' + sitemapId).children('li').each(function() {
         var entry = new SitemapEntry(this);
         entry.openRecursively(true, 2);
      });
   }
   
   /**
    * Adds drag and drop to tree.
    *
    */
   var initDraggable = cms.sitemap.initDraggable = function() {
      $('#' + sitemapId + ' div.' + itemClass + ', #' + sitemapId + ' .' + dropzoneClass + ', #favorite-drop-list').droppable(cms.sitemap.dropOptions);
      $('#' + sitemapId + ' li').draggable(cms.sitemap.dragOptions);
   }
   
   /**
    * Generates a confirm-dialog to keep subpages when dragging from tree to favorites.
    *
    * @param {Object} dragClone the item to be added to favorites
    * @param {Function} callback the function that should be called after the dialog is finished
    */
   var keepTree = cms.sitemap.keepTree = function(dragClone, callback) {
      var $dialog = $('<div id="cms-alert-dialog" style="display: none"></div>');
      $dialog.appendTo('body');
      $dialog.append('<p style="margin-bottom: 4px;">The page you copied to the \'Favorites\' has sub-pages.<br />Do you want to copy these sub-pages also?</p>');
      $dialog.dialog({
         zIndex: 9999,
         title: 'Favorites',
         modal: true,
         close: function() {
            callback();
            return false;
         },
         buttons: {
            'Keep sub-pages': function() {
               dragClone.addClass(classSubtree);
               $dialog.dialog('destroy');
               $dialog.remove();
               callback();
               
            },
            'Lose sub-pages': function() {
               dragClone.find('ul').remove();
               dragClone.find('span.' + classOpener).remove();
               $dialog.dialog('destroy');
               $dialog.remove();
               callback();
            }
         }
      });
   }
   
   /**
    * Creates a short menu bar button.
    * @param {Object} name the button name
    * @param {Object} title the button title
    * @param {Object} cssClass the css class to add to the button
    */
   var makeModeButton = function(name, title, cssClass) {
      return $('<button name="' + name + '" title="' + title + '" class="cms-left ui-state-default ui-corner-all"><span class="ui-icon ' + cssClass + '"></span>&nbsp;</button>');
   }
   
   /**
    * Creates a wide menu bar button.
    * @param {Object} name the button name
    * @param {Object} title the button title
    * @param {Object} cssClass the css class to add to the button
    */
   var makeWideButton = function(name, title, cssClass) {
      return $('<button name="' + name + '" title="' + title + '" class="cms-left cms-button-wide ui-state-default ui-corner-all"><span class="ui-icon ' + cssClass + '"></span><span class="cms-button-text">' + title + '</span></button>');
   };
   
   /**
    * Creates html-code for new sitemap-item.
    *
    * @param {Object} title item-title
    */
   var createNewItem = function(title) {
      var $result = $('<li><div class="' + dropzoneClass + '"></div><div class="ui-state-hover ui-corner-all ' + itemClass + '"><a class="cms-left ui-icon cms-icon-triangle"></a>\
              <h3>' +
      title +
      '</h3>\
              <div class="cms-additional-info">\
                URL-Name:<span class="cms-url-name">' +
      title.toLowerCase() +
      '</span><br/>\
                URL:<span class="cms-url"></span><br/>\
                VFS-Path:<span class="cms-vfs-path">/demo3/123.xml</span><br/>\
              </div></div></li>');
      $('.cms-url-name', $result).attr('alt', title.toLowerCase());
      $result.addClass(classSitemapEntry);
      setEntryData($result, {
         properties: {}
      });
      return $result;
   }
   
   /**
    * Click-handler to create a new sub-page. Opening an edit dialog for title and properties.
    */
   var newPage = function() {
      var title = 'New name';
      var urlName = 'newname';
      var $entry = $(this).closest('li');
      var currentEntry = new SitemapEntry($entry.get(0));
      var otherUrlNames = {};
      var $ul = $entry.children('ul');
      if ($ul.size() > 0) {
         otherUrlNames = getOtherUrlNames($ul, null);
      }
      var properties = getMergedProperties($entry);
      var dummyEntry = {
         getTitle: function() {
            return title;
         },
         getUrlName: function() {
            return urlName;
         },
         getPropertiesWithDefinitions: function() {
            return properties;
         }
      }
      showEntryEditor(dummyEntry, false, otherUrlNames, function(newTitle, newUrlName, newProperties) {
         if ($entry.children('ul').length == 0) {
            // no child-ul present yet
            $entry.append('<ul/>');
            $entry.children('div.' + dropzoneClass).after(openerHtml);
         }
         $entry.removeClass(classClosed);
         var $newItem = createNewItem(newTitle).appendTo($entry.children('ul'));
         var newEntry = new SitemapEntry($newItem.get(0));
         // setting URL
         newEntry.setProperties(newProperties);
         newEntry.setId('0');
         newEntry.setUrlName(newUrlName);
         var parentURL = currentEntry.getUrl();
         parentURL = parentURL.substring(0, parentURL.lastIndexOf('.'));
         newEntry.setUrls(parentURL + '/' + newUrlName);
         addHandles($newItem.children('div.' + itemClass), sitemapModes);
         destroyDraggable();
         initDraggable();
         setSitemapChanged(true);
      });
   }
   
   /**
    * Event-handler for droppable out-event.
    *
    * @param {Object} e event
    * @param {Object} ui ui-object
    */
   var outDrop = cms.sitemap.outDrop = function(e, ui) {
      $(this).removeClass(classHovered);
      if (cms.sitemap.dom.currentMenu) {
         $('li', cms.sitemap.dom.currentMenu).draggable('option', 'refreshPositions', false);
      } else {
         $('#' + sitemapId + ' li').draggable('option', 'refreshPositions', false);
         if ($(this).attr('id') == 'favorite-drop-list') {
            ui.helper.removeClass(classForceClosed);
         }
      }
   }
   
   /**
    * Event-handler for droppable over-event.
    *
    * @param {Object} e event
    * @param {Object} ui ui-object
    */
   var overDrop = cms.sitemap.overDrop = function(e, ui) {
      $(this).addClass(classHovered);
      if ($(this).hasClass(itemClass)) {
         $(this).closest('.' + classClosed).removeClass(classClosed);
         if (cms.sitemap.dom.currentMenu) {
            $('li', cms.sitemap.dom.currentMenu).draggable('option', 'refreshPositions', true);
         } else {
            $('#' + sitemapId + ' li').draggable('option', 'refreshPositions', true);
         }
      } else if ($(this).attr('id') == 'favorite-drop-list') {
         ui.helper.addClass(classForceClosed);
      }
   }
   
   /**
    * Sets the value from input to element.
    *
    * @param {Object} elem the element
    * @param {Object} input the input-element
    */
   var setDirectValue = function(elem, input, changeValue) {
      var oldValue = input.val();
      changeValue(oldValue, function(newValue) {
         elem.text(newValue).css('display', 'block');
         input.remove();
      })
   }
   
   var setInfoValue = cms.sitemap.setInfoValue = function(elem, value, maxLength, showEnd) {
      elem.attr('alt', value);
      var valueLength = value.length;
      if (valueLength > maxLength) {
         var shortValue = showEnd ? '... ' + value.substring(valueLength - maxLength + 5) : value.substring(0, maxLength - 5) + ' ...';
         elem.text(shortValue);
      } else {
         elem.text(value);
      }
   }
   
   /**
    * Shows additional editing buttons within hover effect.
    */
   var showAddButtons = cms.sitemap.showAddButtons = /** void */ function() {
      timer.id = null;
      var numButtons = $(timer.handleDiv).children().size();
      
      var right = (1 - numButtons) * 24 + 'px';
      var width = numButtons * 24 + 'px';
      
      timer.handleDiv.addClass('ui-widget-header').css({
         'width': width,
         'right': right
      }).children().css('display', 'block').addClass('ui-corner-all ui-state-default');
   }
   
   /**
    * Event-handler for draggable start-event dragging within tree.
    *
    * @param {Object} e event
    * @param {Object} ui ui-object
    */
   var startDrag = cms.sitemap.startDrag = function(e, ui) {
      $('input', this).blur();
      $('.cms-hovering').remove();
      $('div.' + itemClass, this).addClass(dragClass);
      $('div.cms-handle').css('display', 'none');
      stopHover();
      $('div.cms-handle', ui.helper).removeClass('ui-widget-header').css({
         'width': '24px',
         'right': '0px',
         'display': 'block'
      }).children().removeClass('ui-corner-all ui-state-default').not('a.cms-move').css('display', 'none');
      cms.sitemap.dom.favoriteDrop.css('display', 'block');
   }
   
   /**
    * Event-handler for draggable start-event dragging from menu into tree.
    *
    * @param {Object} e event
    * @param {Object} ui ui-object
    */
   var startDragMenu = cms.sitemap.startDragMenu = function(e, ui) {
      //$(ui.helper).addClass(dragClass);
      //$('<pre/>').text($(this).attr('class')).appendTo('body');
      $('div.' + itemClass, this).addClass(dragClass);
      ui.helper.appendTo(cms.sitemap.dom.currentMenu);
      cms.sitemap.dom.currentMenu.children('div').css('display', 'none');
      
   }
   
   /**
    * Starts a hover timeout.<p>
    *
    * @param {Object} handleDiv the div containing the handles
    * @param {Object} adeMode current mode
    */
   var startHoverTimeout = cms.sitemap.startHoverTimeout = /** void */ function(/** jquery-object */handleDiv, /** string */ adeMode) {
      if (timer.id) {
         clearTimeout(timer.id);
      }
      timer.id = setTimeout(cms.sitemap.showAddButtons, 1000);
      timer.handleDiv = handleDiv;
      timer.adeMode = adeMode;
   }
   
   /**
    * Event-handler for draggable stop-event dragging within tree.
    *
    * @param {Object} e event
    * @param {Object} ui ui-object
    */
   var stopDrag = cms.sitemap.stopDrag = function(e, ui) {
      $('div.' + itemClass, this).removeClass(dragClass);
      //$(ui.helper).removeClass(dragClass);
      var dragClone = $(this).clone();
      dragClone.find('div.cms-handle').remove();
      dragClone.find('ul').remove();
      dragClone.find('span.' + classOpener).remove();
      
      var sitemapEntry = new SitemapEntry(dragClone.get(0));
      _addRecent(sitemapEntry.serialize(false));
      cms.data.saveRecent(function() {
            })
      //dragClone.appendTo(cms.sitemap.dom.recentMenu.find('ul'));
      $('div.cms-handle').css('display', 'block');
      cms.sitemap.dom.favoriteDrop.css('display', 'none');
      setSitemapChanged(true);
   }
   
   /**
    * Event-handler for draggable stop-event dragging from menu into treetree.
    *
    * @param {Object} e event
    * @param {Object} ui ui-object
    */
   var stopDragMenu = cms.sitemap.stopDragMenu = function(e, ui) {
      $('div.' + itemClass, this).removeClass(dragClass);
      //$(ui.helper).removeClass(dragClass);
      cms.sitemap.dom.currentMenu.children('div').css('display', 'block');
      // refresh droppable
      $('div.' + itemClass + ', div.' + dropzoneClass).droppable('destroy');
      $('div.' + itemClass + ', div.' + dropzoneClass).droppable(cms.sitemap.dropOptionsMenu);
      setSitemapChanged(true);
   }
   
   /**
    * Cancels current hover timeout.<p>
    */
   var stopHover = cms.sitemap.stopHover = /** void */ function() {
      if (timer.id) {
         clearTimeout(timer.id);
         timer.id = null;
      }
      // sometimes out is triggered without over being triggered before, especially in IE
      if (!timer.handleDiv) {
         return;
      }
      timer.handleDiv.removeClass('ui-widget-header').css({
         'width': '24px',
         'right': '0px'
      }).children().removeClass('ui-corner-all ui-state-default').not('a.cms-' + timer.adeMode).css('display', 'none');
   }
   
   /** Array of mode-objects. These correspond to the buttons shown in the toolbar. */
   var sitemapModes = [{
      // save mode
      name: 'save',
      title: 'Save',
      wide: false,
      floatRight: false,
      create: createButton,
      enable: function() {
         if (!this.button.hasClass('cms-deactivated')) {
            this.button.addClass('ui-state-active');
            var $sitemapElem = $('#' + sitemapId);
            var sitemap = serializeSitemap($sitemapElem);
            cms.data.saveSitemap(sitemap, function(ok, data) {
               if (ok) {
                  setSitemapChanged(false);
               } else {
                  alert("error")
                  //display error message ? 
               }
            });
         }
      },
      disable: function() {
         this.button.removeClass('ui-state-active');
      },
      init: function() {
            }
   }, {
      // edit mode
      name: 'edit',
      title: 'Edit',
      wide: false,
      create: createButton,
      enable: function() {
         this.button.addClass('ui-state-active');
         initDraggable();
         $('#' + sitemapId + ' li div.' + itemClass).each(function() {
            addHandles(this, sitemapModes);
         });
         //         $('#' + sitemapId + ' div.' + itemClass + ' h3').directInput({marginHack: true});
         //         $('#' + sitemapId + ' div.' + itemClass + ' h3').click(directInput);
      },
      disable: function() {
         this.button.removeClass('ui-state-active');
         destroyDraggable();
         $('div.cms-handle').remove();
         //         $('div.' + itemClass + ' h3').directInput.destroy();
         //         $('div.' + itemClass + ' h3').unbind('click', directInput);
      },
      createHandle: function() {
         return $('<a class="cms-edit cms-edit-enabled"></a>');
      },
      init: function() {
            }
   }, {
      // move mode
      name: 'move',
      title: 'Move',
      wide: false,
      create: createButton,
      enable: function() {
         this.button.addClass('ui-state-active');
         initDraggable();
         $('#' + sitemapId + ' li div.' + itemClass).each(function() {
            addHandles(this, sitemapModes);
         });
      },
      disable: function() {
         this.button.removeClass('ui-state-active');
         destroyDraggable();
         $('div.cms-handle').remove();
      },
      createHandle: function() {
         return $('<a class="cms-move"></a>');
      },
      init: function() {
            }
   }, {
      // delete mode
      name: 'delete',
      title: 'Delete',
      wide: false,
      create: createButton,
      enable: function() {
         this.button.addClass('ui-state-active');
         initDraggable();
         $('#' + sitemapId + ' li div.' + itemClass).each(function() {
            addHandles(this, sitemapModes);
         });
         
      },
      disable: function() {
         this.button.removeClass('ui-state-active');
         destroyDraggable();
         $('div.cms-handle').remove();
      },
      createHandle: function() {
         return $('<a class="cms-delete"></a>');
      },
      init: function() {
            }
   }, {
      // new mode
      name: 'new',
      title: 'New',
      wide: true,
      floatRight: false,
      create: createButton,
      enable: function() {
         this.button.addClass('ui-state-active');
         cms.sitemap.dom.currentMenu = cms.sitemap.dom.newMenu.css({
            /* position : 'fixed', */
            top: 35,
            left: this.button.position().left - 1
         }).slideDown(100, function() {
            $('div.ui-widget-shadow', cms.sitemap.dom.newMenu).css({
               top: 0,
               left: -3,
               width: cms.sitemap.dom.newMenu.outerWidth() + 8,
               height: cms.sitemap.dom.newMenu.outerHeight() + 1,
               border: '0px solid',
               opacity: 0.6
            });
         });
         $('#' + sitemapId + ' div.' + itemClass + ', #' + sitemapId + ' .' + dropzoneClass).droppable(cms.sitemap.dropOptionsMenu);
         $('#' + cms.html.newMenuId + ' li').draggable(cms.sitemap.dragOptionsMenu);
      },
      disable: function() {
         this.button.removeClass('ui-state-active');
         destroyDraggable();
         cms.sitemap.dom.newMenu.css('display', 'none');
         cms.sitemap.dom.currentMenu = null;
      },
      createHandle: function() {
         return $('<a class="cms-new"></a>');
      },
      init: function() {
         cms.sitemap.dom.newMenu = $(cms.html.createMenu(cms.html.newMenuId)).appendTo(cms.sitemap.dom.toolbarContent);
         cms.sitemap.dom.newMenu.find('ul').append(createNewItem('NewPage'));
      }
   }, {
      // favorites mode
      name: 'favorites',
      title: 'Favorites',
      wide: true,
      floatRight: false,
      create: createButton,
      enable: function() {
         this.button.addClass('ui-state-active');
         cms.sitemap.dom.currentMenu = cms.sitemap.dom.favoriteMenu.css({
            /* position : 'fixed', */
            top: 35,
            left: this.button.position().left - 1
         }).slideDown(100, function() {
            $('div.ui-widget-shadow', cms.sitemap.dom.favoriteMenu).css({
               top: 0,
               left: -3,
               width: cms.sitemap.dom.favoriteMenu.outerWidth() + 8,
               height: cms.sitemap.dom.favoriteMenu.outerHeight() + 1,
               border: '0px solid',
               opacity: 0.6
            });
         });
         $('#' + sitemapId + ' div.' + itemClass + ', #' + sitemapId + ' .' + dropzoneClass).droppable(cms.sitemap.dropOptionsMenu);
         $('#' + cms.html.favoriteMenuId + ' li').draggable(cms.sitemap.dragOptionsMenu);
      },
      disable: function() {
         this.button.removeClass('ui-state-active');
         destroyDraggable();
         cms.sitemap.dom.favoriteMenu.css('display', 'none');
         cms.sitemap.dom.currentMenu = null;
      },
      init: function() {
         cms.sitemap.dom.favoriteMenu = $(cms.html.createMenu(cms.html.favoriteMenuId)).appendTo(cms.sitemap.dom.toolbarContent);
         cms.sitemap.dom.favoriteDrop = $(cms.html.createFavDrop()).appendTo(cms.sitemap.dom.toolbarContent);
         cms.sitemap.dom.favoriteList = cms.sitemap.dom.favoriteMenu.find('ul');
      },
      
      load: function(callback) {
         cms.data.loadFavorites(function(ok, data) {
            var favorites = data.favorites;
            $('#favorite_list_items').empty();
            var $favContent = buildSitemap(favorites).appendTo('#favorite_list_items');
            callback();
         });
      }
   }, {
      // recent mode
      name: 'recent',
      title: 'Recent',
      wide: true,
      floatRight: false,
      create: createButton,
      enable: function() {
         this.button.addClass('ui-state-active');
         cms.sitemap.dom.currentMenu = cms.sitemap.dom.recentMenu.css({
            /* position : 'fixed', */
            top: 35,
            left: this.button.position().left - 1
         }).slideDown(100, function() {
            $('div.ui-widget-shadow', cms.sitemap.dom.recentMenu).css({
               top: 0,
               left: -3,
               width: cms.sitemap.dom.recentMenu.outerWidth() + 8,
               height: cms.sitemap.dom.recentMenu.outerHeight() + 1,
               border: '0px solid',
               opacity: 0.6
            });
         });
         $('div.' + itemClass + ', div.' + dropzoneClass).droppable(cms.sitemap.dropOptionsMenu);
         $('#' + cms.html.recentMenuId + ' li').draggable(cms.sitemap.dragOptionsMenu);
      },
      disable: function() {
         this.button.removeClass('ui-state-active');
         destroyDraggable();
         cms.sitemap.dom.recentMenu.css('display', 'none');
         cms.sitemap.dom.currentMenu = null;
         
      },
      
      load: function(callback) {
         cms.data.loadRecent(function(ok, data) {
            var recent = data.recent;
            $('#recent_list_items').empty();
            var $recContent = buildSitemap(recent).appendTo('#recent_list_items');
            
            callback();
         });
      },
      
      init: function() {
         cms.sitemap.dom.recentMenu = $(cms.html.createMenu(cms.html.recentMenuId)).appendTo(cms.sitemap.dom.toolbarContent);
      }
   }, {
      // reset mode
      name: 'reset',
      title: 'Reset',
      wide: false,
      floatRight: true,
      create: createButton,
      enable: function() {
         this.button.addClass('ui-state-active');
      },
      disable: function() {
         this.button.removeClass('ui-state-active');
      },
      init: function() {
         $('button[name=reset]').live('click', function() {
            showLeaveDialog(window.location.href, 'Do you really want to reset and discard your changes?', 'Reset');
         });
         
      }
   }, {
      // publish mode
      name: 'publish',
      title: 'Publish',
      wide: false,
      floatRight: true,
      create: createButton,
      enable: function() {
         var self = this;
         this.button.addClass('ui-state-active');
         cms.publish.initProjects(function() {
            var publishDialog = new cms.publish.PublishDialog();
            publishDialog.finish = function() {
               self.button.trigger('click');
            }
            publishDialog.start();
         });
      },
      disable: function() {
         this.button.removeClass('ui-state-active');
      },
      init: function() {
            }
   }];
   
   /**
    * Displays a dialog asking the user if he wants to leave the page.
    *
    * If the user clicks OK, he will be sent to the target.
    *
    * @param {Object} target the URL of the site which the user should be sent to if he clicks OK
    * @param {Object} dialogText the text of the dialog
    * @param {Object} dialogTitle the title of the dialog
    */
   var showLeaveDialog = function(target, dialogText, dialogTitle) {
      if (sitemapChanged) {
      
         $('#cms-leave-dialog').remove();
         var $dlg = $('<div id="cms-leave-dialog"></div>').appendTo('body');
         $dlg.text(dialogText);
         var buttons = {};
         
         buttons['No'] = function() {
            $dlg.dialog('destroy');
         }
         buttons['Yes'] = function() {
            sitemapChanged = false;
            $dlg.dialog('destroy');
            window.location.href = target;
         }
         var dlgOptions = {
            modal: true,
            zIndex: 9999,
            title: dialogTitle,
            autoOpen: true,
            buttons: buttons,
            close: function() {
               $dlg.dialog('destroy');
            }
         }
         $dlg.dialog(dlgOptions);
      } else {
         window.location.href = target;
      }
      
   }
   
   
   /**
    * Serializes a sitemap DOM element to a JSON structure
    * @param {Object} $element the DOM element representing the sitemap (wrapped in a jQuery object)
    */
   var serializeSitemap = function($element) {
      var result = [];
      $element.children('li').each(function() {
         var entry = new SitemapEntry(this);
         result.push(entry.serialize(false));
      });
      return result;
   }
   
   /**
    * Helper function that generates a callback for jQuery.each which appends its "this" argument to a given parent
    *
    * @param {Object} $parent the parent object to which the callback returned should append its argument
    */
   var actionAppendTo = function($parent) {
      return function() {
         $parent.append(this);
      }
   }
   
   
   /**
    * Builds a DOM sitemap element from JSON sitemap entry data.
    *
    * This function also recursively processes all subentries of the entry.
    *
    * @param {Object} data a JSON object representing a sitemap entry
    */
   var _buildSitemapElement = function(data) {
      var $li = $('<li></li>').addClass(classSitemapEntry);
      $('.' + dropzoneClass, $li).remove();
      $('.' + classOpener, $li).remove();
      var isSitemap = data.properties.sitemap;
      $('<div></div>').addClass(dropzoneClass).appendTo($li);
      if (data.subentries && data.subentries.length > 0) {
         $('<span></span>').addClass(classOpener).appendTo($li);
      }
      var $item = $(data.content).appendTo($li);
      
      if (!isSitemap && data.subentries && data.subentries.length > 0) {
         $li.addClass(classSubtree);
         $li.addClass(classClosed);
         var $ul = $('<ul></ul>').appendTo($li);
         var subEntries = $.map(data.subentries, _buildSitemapElement);
         $.each(subEntries, actionAppendTo($ul));
         
      }
      var dataNode = $item.get(0);
      var isSitemap = data.properties.sitemap;
      if (isSitemap) {
         delete data.properties['sitemap'];
      }
      setEntryData($li, {
         properties: data.properties,
         id: data.id,
         sitemap: isSitemap
      });
      if (isSitemap) {
         $li.addClass('cms-sub-sitemap')
      }
      return $li;
   }
   
   /**
    * AJAX handler that initializes the sitemap after loading it as JSON.
    * @param {Object} ok
    * @param {Object} data
    */
   var onLoadSitemap = cms.sitemap.onLoadSitemap = function(ok, data) {
      cms.sitemap.setWaitOverlayVisible(false);
      if (!ok) {
         return;
      }
      var sitemap = data.sitemap;
      cms.sitemap.buildSitemap(sitemap).appendTo('#' + sitemapId);
      initSitemap(!data.noEditReason, data.toolbar);
      // don't display the toolbar for historical resources
      if (!data.toolbar) {
         return;
      }
      setSitemapChanged(false);
      cms.data.getSitemapProperties(function(ok, data) {
         cms.sitemap.propertyDefinitions = data;
      });
      if (data.noEditReason) {
         $('#toolbar_content button').addClass('cms-deactivated').unbind('click');
         // TODO: better display an red-square-icon in the toolbar with a tooltip
         cms.util.dialogAlert(data.noEditReason, "Can't edit sitemap");
      }
   }
   
   /**
    * Builds a jQuery object containing sitemap DOM elements from a list of sitemap entry data.
    *
    * @param {Object} data the list of sitemap entries.
    */
   var buildSitemap = cms.sitemap.buildSitemap = function(data) {
      return $($.map(data, _buildSitemapElement))
   }
   
   var sitemapChanged = false;
   
   /**
    * Sets the "changed" status of the sitemap editor
    * @param {Boolean} changed a flag that indicates whether the sitemap should be marked as changed or unchanged
    */
   var setSitemapChanged = function(changed) {
      if (!sitemapChanged && changed) {
         cms.data.sitemapPostJSON('startedit', {}, function() {
                  });
      }
      if (sitemapChanged && !changed) {
         cms.data.sitemapPostJSON('stopedit', {}, function() {
                  });
      }
      sitemapChanged = changed;
      var $saveButton = $('button[name=save]');
      if (changed) {
         $saveButton.removeClass('cms-deactivated');
      } else {
         $saveButton.addClass('cms-deactivated');
      }
   }
   
   /**
    * Gets the entry data for a sitemap entry.
    * @param {Object} $item the sitemap entry for which the entry data should be read
    */
   var getEntryData = function($item) {
      var resultStr = decodeURIComponent($item.attr("rel"));
      return JSON.parse(resultStr);
   }
   
   /**
    * Sets the entry data for a sitemap entry.
    * @param {Object} $item the sitemap entry
    * @param {Object} obj the entry data
    */
   var setEntryData = function($item, obj) {
      var dataStr = encodeURIComponent(JSON.stringify(obj));
      $item.attr('rel', dataStr);
   }
   
   /**
    * Unload handler which asks the user whether he wants to save his changes.
    */
   var onUnload = function() {
      if (sitemapChanged) {
         var saveChanges = window.confirm('Do you want to save your changes to the sitemap?');
         if (saveChanges) {
            cms.data.saveSitemap(serializeSitemap($('#' + sitemapId)), function() {
                        });
         } else {
            setSitemapChanged(false);
         }
      }
   }
   
   
   /**
    * Adds an opener and a dropzone to the DOM of a sitemap entry.
    * @param {Object} $element the DOM element, wrapped in a jQuery object
    */
   var _addOpenerAndDropzone = function($element) {
      if ($element.children('.' + classOpener).size() == 0 && $element.children('ul').size() != 0) {
         $('<span></span>').addClass(classOpener).prependTo($element);
      }
      
      if ($element.children('.' + dropzoneClass).size() == 0) {
         $('<div></div>').addClass(dropzoneClass).prependTo($element);
      }
   }
   
   
   /**
    * Opens  the "edit favorites" dialog.
    */
   var _editFavorites = function() {
      $('#cms-sitemap-favedit').remove();
      $('<div id="cms-sitemap-favedit"/>').appendTo('body');
      var buttons = {}
      var $dlg = $('#cms-sitemap-favedit');
      var $ul = $('<div></div>').appendTo($dlg);
      
      
      $.each(cms.sitemap.favorites, function() {
         var $smItem = _buildSitemapElement(this).addClass('cms-toplevel-entry');
         var $row = $('<div></div>').appendTo($ul)
         var $del = $('<span></span>').addClass('cms-sitemap-favdel').css('float', 'left').css('position', 'relative').css('top', '7px').width(24).height(24);
         $del.click(function() {
            $row.remove();
         });
         $del.appendTo($row);
         $('<ul/>').append($smItem).css('margin-left', '25px').appendTo($row);
         $('<div></div>').css('clear', 'both').appendTo($row);
      });
      $ul.sortable();
      
      buttons['Ok'] = function() {
      
         var newFav = $.map($ul.find('.cms-toplevel-entry').get(), function(sm) {
            var entry = new SitemapEntry(sm);
            return entry.serialize(false);
         });
         $dlg.dialog('destroy');
         cms.data.sitemapPostJSON('set', {
            'fav': newFav
         }, function(ok, callback) {
            $('button[name=favorites].ui-state-active').trigger('click');
            // do nothing
         });
      };
      buttons['Cancel'] = function() {
         $dlg.dialog('destroy');
      };
      
      $('#cms-sitemap-favedit').dialog({
         autoOpen: true,
         modal: true,
         zIndex: 9999,
         resizable: true,
         width: 500,
         buttons: buttons,
         title: 'Edit favorites'
      });
   }
   
   /**
    * Enables or disables a "waiting" sign for long-running operations
    * @param {Boolean} visible enable the waiting sign if this is true, else disable it
    */
   var setWaitOverlayVisible = cms.sitemap.setWaitOverlayVisible = function(visible) {
      $('#cms-waiting-layer').remove();
      if (visible) {
         var $layer = $('<div id="cms-waiting-layer"></div>');
         $layer.appendTo('body');
         $layer.css({
            position: 'fixed',
            top: '0px',
            left: '0px',
            'z-index': 99999,
            width: '100%',
            height: '100%',
            'background-color': '#000000',
            'opacity': '0.5'
         });
         $('<img></img>').attr('src', cms.data.SKIN_URL + 'commons/wait.gif').appendTo($layer);
      }
   }
   
   var assert = function(condition, desc) {
      if (!condition) {
         throw desc;
      }
   };
   
   /**
    * Helper function that converts a constructor function to a factory function which returns the newly created object and can be used without "new".
    * @param {Object} constructor the constructor function
    */
   var wrapConstructor = function(constructor) {
      return function() {
         return new constructor(arguments);
      }
   }
   
   
   /**
    * Constructor function for sitemap entries.
    * @param {Object} li the LI DOM element representing a sitemap entry
    */
   var SitemapEntry = function(li) {
   
      this.$li = $(li);
      assert(this.$li.hasClass(classSitemapEntry), "wrong element for sitemap entry");
      this.$item = this.$li.children('.' + itemClass);
      
   }
   
   /**
    * Prototype for the SitemapEntry class.
    *
    * This class is a wrapper around a DOM element representing a sitemap entry which can be used to conveniently set
    * or get attributes of that entry without directly using DOM manipulation functions.
    */
   SitemapEntry.prototype = {
      getChildren: function() {
         var $ul = this.$li.children('ul');
         if ($ul.size() > 0) {
            var $childrenLi = $ul.children('li');
            return $.map($childrenLi.get(), wrapConstructor(SitemapEntry));
         } else {
            return [];
         }
      },
      
      setOpen: function(opened) {
         if (opened) {
            this.$li.removeClass('cms-closed');
         } else {
            this.$li.addClass('cms-closed');
         }
      },
      
      openRecursively: function(open, depth) {
         var self = this;
         if (depth != 0) {
            self.setOpen(open);
            var children = self.getChildren();
            for (var i = 0; i < children.length; i++) {
               children[i].openRecursively(open, depth - 1);
            }
         }
      },
      
      getUrlName: function() {
         return this.$li.find('.cms-url-name:first').attr('alt');
      },
      
      setUrlName: function(newValue) {
         setInfoValue(this.$item.find('.cms-url-name'), newValue, 37, true);
      },
      
      getTitle: function() {
         return this.$item.find('h3').text();
      },
      
      setTitle: function(newValue) {
         this.$item.find('h3').text(newValue);
      },
      
      setUrl: function(newValue) {
         setInfoValue(this.$item.find('span.cms-url'), newValue, 37, true);
      },
      
      getUrl: function() {
         return this.$item.find('span.cms-url').attr('alt');
      },
      
      /**
       * Recursively adjusts the URLs of the subtree starting at this sitemap entry.
       * @param {Object} parentUrl the parent url of this sitemap entry
       */
      setUrls: function(parentUrl) {
         var self = this;
         var pathName = self.getUrlName();
         self.setUrl(parentUrl + '/' + pathName + '.html');
         var children = self.getChildren();
         for (var i = 0; i < children.length; i++) {
            children[i].setUrls(parentUrl + '/' + pathName);
         }
      },
      
      getId: function() {
         var entryData = getEntryData(this.$li);
         return entryData.id;
      },
      
      setId: function(newValue) {
         var entryData = getEntryData(this.$li);
         entryData.id = newValue;
         setEntryData(this.$li, entryData);
      },
      
      getProperties: function() {
         var entryData = getEntryData(this.$li);
         return entryData.properties;
      },
      
      setProperties: function(newValue) {
         var entryData = getEntryData(this.$li);
         entryData.properties = newValue;
         setEntryData(this.$li, entryData);
      },
      
      getPropertiesWithDefinitions: function() {
         var self = this;
         var propDefs = JSON.parse(JSON.stringify(cms.sitemap.propertyDefinitions)).properties;
         var props = self.getProperties();
         var propEntries = mergePropertiesWithDefinitions(propDefs, props);
         return propEntries;
      },
      
      getPath: function() {
         var self = this;
         return self.$item.find('.cms-vfs-path').attr('alt');
      },
      
      isSitemap: function() {
         var entryData = getEntryData(this.$li);
         return entryData.sitemap;
      },
      
      /**
       * Recursively converts this entry and its subentries to JSON.
       * @param {Boolean} includeContent if true, the content is included in the JSON output
       * @return a JSON object describing the sitemap subtree starting from this entry
       */
      serialize: function(includeContent) {
         var self = this;
         var title = self.getTitle();
         var urlName = self.getUrlName();
         var vfsPath = self.getPath();
         var children = self.getChildren();
         var childrenResults = [];
         for (var i = 0; i < children.length; i++) {
            var child = children[i];
            childrenResults.push(child.serialize(includeContent));
         }
         var properties = self.getProperties();
         properties.sitemap = self.isSitemap();
         var result = {
            title: title,
            id: self.getId(),
            name: urlName,
            subentries: childrenResults,
            properties: properties
         };
         if (includeContent) {
            var content = $('<p></p>').append(self.$item.clone()).html();
            result.content = content;
         }
         return result;
      }
   }
   
   
   /**
    * Checks whether an object has no properties.
    * @param {Object} obj
    */
   var _isEmpty = function(obj) {
      for (var key in obj) {
         return false;
      }
      return true;
   }
   
   var makeDialogDiv = cms.property.makeDialogDiv = function(divId) {
      var $d = $('#' + divId);
      if ($d.size() == 0) {
         $d = $('<div></div>').attr('id', divId).css('display', 'none').appendTo('body');
      }
      $d.empty();
      return $d;
   }
   
   
   var TitleAndUrlName = function() {
      var self = this;
      self.$dom = $('<div/>');
      var _rowInput = function(label) {
         var $tr = $('<tr/>');
         $('<td></td>').text(label).appendTo($tr);
         var $input = $('<input type="text"></input>');
         $('<td></td>').append($input).appendTo($tr);
         return $tr;
      }
      
      var _rowText = function(text) {
         var $tr = $('<tr/>');
         $('<td/>').attr('colspan', '2').text(text).appendTo($tr);
         return $tr;
      }
      
      var $table = $('<table border="0"></table>').appendTo(self.$dom);
      var $titleRow = _rowInput('Title: ').appendTo($table);
      var $titleErrorRow = _rowText('').appendTo($table).hide();
      $titleErrorRow.css('color', '#ff0000');
      var $urlNameRow = _rowInput('URL name: ').appendTo($table);
      var $urlNameErrorRow = _rowText('').appendTo($table).hide();
      $urlNameErrorRow.css('color', '#ff0000');
      self.$titleRow = $titleRow;
      self.$urlNameRow = $urlNameRow;
      self.$titleErrorRow = $titleErrorRow;
      self.$urlNameErrorRow = $urlNameErrorRow;
   }
   
   TitleAndUrlName.prototype = {
      getTitle: function() {
         var self = this;
         return self.$titleRow.find('input').val();
      },
      
      setTitle: function(newValue) {
         var self = this;
         self.$titleRow.find('input').val(newValue);
      },
      
      getUrlName: function() {
         var self = this;
         return self.$urlNameRow.find('input').val();
      },
      
      setUrlName: function(newValue) {
         var self = this;
         self.$urlNameRow.find('input').val(newValue);
      },
      
      showUrlNameError: function(errorMessage) {
         var self = this;
         if (errorMessage) {
            self.$urlNameErrorRow.show().find('td').text(errorMessage);
         } else {
            self.$urlNameErrorRow.hide();
         }
      },
      
      showTitleError: function(errorMessage) {
         var self = this;
         if (errorMessage) {
            self.$titleErrorRow.show().find('td').text(errorMessage);
         } else {
            self.$titleErrorRow.hide();
         }
      }
   }
   
   /**
    * Displays the property editor.
    *
    * @param {Object} properties the current non-default properties
    * @param {Object} defaults the property configuration containing defaults and widget types
    */
   var showEntryEditor = cms.sitemap.showEntryEditor = function(entry, writeData, otherUrlNames, callback, cancelCallback) {
      var title = entry.getTitle();
      var urlName = entry.getUrlName();
      var properties = entry.getPropertiesWithDefinitions();
      var widgets = {};
      var newProps = {};
      var $table = cms.property.buildPropertyTable(properties, widgets);
      var $dlg = makeDialogDiv('cms-property-dialog');
      var titleAndName = new TitleAndUrlName();
      
      titleAndName.setTitle(title);
      titleAndName.setUrlName(urlName);
      titleAndName.$dom.appendTo($dlg);
      
      $('<hr/>').css({
         'margin-top': '15px',
         'margin-bottom': '15px'
      }).appendTo($dlg);
      $('<div></div>').css('font-weight', 'bold').text('Properties: ').appendTo($dlg);
      
      $dlg.append($table);
      
      var _destroy = function() {
         $dlg.dialog('destroy');
      }
      
      var buttons = {};
      buttons['Cancel'] = function() {
         _destroy();
         if (cancelCallback) {
            cancelCallback();
         }
      };
      var options = {
         title: 'Edit entry',
         modal: true,
         autoOpen: true,
         width: 440,
         zIndex: 9999,
         close: _destroy,
         buttons: buttons
      }
      
      $dlg.dialog(options);
      
      // align middle columns of both tables
      var colOffset1 = $table.find('td:eq(1)').position().left;
      var colOffset2 = titleAndName.$dom.find('td:eq(1)').position().left;
      if (colOffset1 > colOffset2) {
         titleAndName.$dom.css('margin-left', (colOffset1 - colOffset2) + 'px');
      } else {
         $table.css('margin-left', (colOffset2 - colOffset1) + 'px');
      }
      
      
      var $ok = $('<button></button>').addClass('ui-corner-all').addClass('ui-state-default').text('OK');
      
      var _validateAll = function(validationCallback) {
         var result = true;
         cms.property.setDialogButtonEnabled($ok, false);
         var urlName = titleAndName.getUrlName();
         var urlNameError = null;
         var hasUrlNameError = false;
         cms.data.convertUrlName(urlName, function(newUrlName) {
            var titleError = null;
            if (titleAndName.getTitle() == '') {
               titleError = 'The title must not be empty.';
               result = false;
            }
            titleAndName.showTitleError(titleError);
            
            var isDuplicate = otherUrlNames[newUrlName];
            hasUrlNameError = isDuplicate || (urlName != newUrlName)
            if (isDuplicate && (urlName != newUrlName)) {
               urlNameError = 'The translation of the name "' + urlName + '" already exists at this level';
            } else if (isDuplicate) {
               urlNameError = 'The name already exists at this level';
            } else if (urlName != newUrlName) {
               urlNameError = 'The name was translated from "' + urlName + '".';
            }
            titleAndName.setUrlName(newUrlName);
            titleAndName.showUrlNameError(urlNameError);
            
            if (hasUrlNameError) {
               result = false;
            }
            
            for (var key in widgets) {
               if (!widgets[key].validate()) {
                  result = false;
               }
            }
            if (validationCallback) {
               validationCallback(result);
            }
         });
      }
      
      function _validationCallback(ok) {
         cms.property.setDialogButtonEnabled($ok, ok);
      }
      
      _validateAll(_validationCallback);
      
      
      $dlg.click(function() {
         _validateAll(_validationCallback);
         return true;
      });
      $dlg.nextAll().click(function() {
         _validateAll(_validationCallback);
      });
      $dlg.keydown(function(e) {
         // user pressed Tab or Enter
         if (e.keyCode == 9 || e.keyCode == 13) {
            _validateAll(_validationCallback);
         }
      });
      
      $ok.click(function() {
         _validateAll(function(ok) {
            cms.property.setDialogButtonEnabled($ok, ok);
            if (ok) {
               _destroy();
               for (widgetName in widgets) {
                  widgets[widgetName].save(newProps);
               }
               if (writeData) {
                  entry.setTitle(titleAndName.getTitle());
                  entry.setUrlName(titleAndName.getUrlName());
                  entry.setProperties(newProps);
               }
               callback(titleAndName.getTitle(), titleAndName.getUrlName(), newProps);
            }
         });
      });
      $dlg.nextAll('.ui-dialog-buttonpane').append($ok);
   }
   
})(cms);
