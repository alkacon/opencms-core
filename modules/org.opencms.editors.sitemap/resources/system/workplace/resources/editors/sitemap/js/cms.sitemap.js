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
   var dataProperties = 'cmsdata-properties';
   var dataId = 'cmsdata-id';
   
   /** html-code for opener handle. */
   var openerHtml = '<span class="' + classOpener + '"></span>';
   
   /** html-id for sitemap. */
   var sitemapId = 'cms-sitemap';
   
   /** Map of jquery objects. */
   cms.sitemap.dom = {};
   cms.sitemap.favorites = [];
   cms.sitemap.recent = [];
   var MAX_RECENT = 10;
   
   /**
    * Timer object used for delayed hover effect.
    */
   var timer = cms.sitemap.timer = {
      id: null,
      handleDiv: null,
      adeMode: null
   };
   
   var _addRecent = function(entry) {
      cms.sitemap.recent.unshift(entry);
      while (cms.sitemap.recent.length > MAX_RECENT) {
         cms.sitemap.recent.pop();
      }
   }
   
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
   
   var setURLs = cms.sitemap.setURLs = function(listItem, parentUrl) {
      var currentItemDiv = listItem.children('.' + itemClass);
      var pathName = currentItemDiv.find('span.cms-url-name').attr('alt');
      setInfoValue(currentItemDiv.find('span.cms-url'), parentUrl + '/' + pathName + '.html', 37, true);
      listItem.children('ul').children().each(function() {
         setURLs($(this), parentUrl + '/' + pathName);
      });
   }
   
   var setURLName = cms.sitemap.setURLName = function(elem, input) {
      var previous = elem.attr('alt');
      var current = input.val();
      if (previous != current) {
         var currentLi = elem.closest('li');
         if (currentLi.children('ul').length) {
            var $dialog = $('<div id="cms-alert-dialog" style="display: none"></div>');
            $dialog.appendTo('body');
            $dialog.append('<p style="margin-bottom: 4px;">The page you are editing has subpages. Changing it\'s URL will also change the URLs of those.<br />Do you want to change it anyway?</p>');
            $dialog.dialog({
               zIndex: 9999,
               title: 'New Page',
               modal: true,
               close: function() {
                  $dialog.dialog('destroy');
                  $dialog.remove();
               },
               buttons: {
                  'OK': function() {
                     var previousUrl = elem.siblings('span.cms-url').attr('alt');
                     var parentUrl = previousUrl.substring(0, previousUrl.lastIndexOf('/'));
                     setInfoValue(elem, current, 37, true);
                     setURLs(currentLi, parentUrl);
                     $dialog.dialog('destroy');
                     $dialog.remove();
                  },
                  'Cancel': function() {
                     $dialog.dialog('destroy');
                     $dialog.remove();
                  }
               }
            });
         } else {
            var previousUrl = elem.siblings('span.cms-url').attr('alt');
            var parentUrl = previousUrl.substring(0, previousUrl.lastIndexOf('/'));
            setInfoValue(elem, current, 37, true);
            setURLs(currentLi, parentUrl);
         }
      }
      elem.css('display', '');
      input.remove();
      
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
      if (liItem.children('ul').length) {
         var $dialog = $('<div id="cms-alert-dialog" style="display: none"></div>');
         $dialog.appendTo('body');
         $dialog.text('Do you realy want to remove this page and all sub-pages from the sitemap?');
         $dialog.dialog({
            zIndex: 9999,
            title: 'Delete',
            modal: true,
            close: function() {
               $dialog.dialog('destroy');
               $dialog.remove();
            },
            buttons: {
               'OK': function() {
                  $dialog.dialog('destroy');
                  $dialog.remove();
                  liItem.find('ul').remove();
                  liItem.find('span.' + classOpener).remove();
                  liItem.find('div.cms-handle').remove();
                  
                  if (!liItem.siblings().length) {
                     var parentUl = liItem.parent();
                     parentUl.siblings('span.' + classOpener).remove();
                     parentUl.remove();
                  }
                  
                  var sitemapEntry = serializeSitemapElement(liItem, true);
                  _addRecent(sitemapEntry);
                  saveRecent(function(ok, data) {
                     if (!ok) {
                        return;
                     }
                     liItem.remove();
                  })
                  //liItem.prependTo(cms.sitemap.dom.recentMenu.find('ul'));
                  setSitemapChanged(true);
               },
               'Cancel': function() {
                  $dialog.dialog('destroy');
                  $dialog.remove();
               }
            }
         
         });
      } else {
         liItem.find('div.cms-handle').remove();
         if (!liItem.siblings().length) {
            var parentUl = liItem.parent();
            parentUl.siblings('span.' + classOpener).remove();
            parentUl.remove();
         }
         var sitemapEntry = serializeSitemapElement(liItem, true);
         _addRecent(sitemapEntry);
         saveRecent(function(ok, data) {
            if (!ok) {
               return;
            }
            liItem.remove();
         });
         
         //liItem.prependTo(cms.sitemap.dom.recentMenu.find('ul'));
      }
   }
   
   /**
    * Removes drag and drop from tree.
    *
    */
   var destroyDraggable = cms.sitemap.destroyDraggable = function() {
      $('#' + sitemapId + ' div.' + itemClass + ', #' + sitemapId + ' .' + dropzoneClass).droppable('destroy');
      $('li').draggable('destroy');
   }
   
   /**
    * Click-handler to allow direct editing of item-titles.
    *
    */
   var directInput = function() {
      var elem = $(this);
      var input = $('<input name="directInput" type="text" class="' + classDirectInput + '" value="' + elem.text() + '" />');
      copyCss(elem, input);
      input.insertBefore(elem);
      
      if ($.browser.msie && $.browser.version <= 7.0) {
         // HACK: to avoid the margin-left bug for input-elements in IE7 
         // (the problem occurs within elements where hasLayout: true and will introduce an 
         // inherited margin-left for the input-element)
         var parentMargin = parseInt(elem.parent().css('margin-left'));
         if (!isNaN(parentMargin)) {
         
            inputMargin = parseInt(input.css('margin-left'));
            if (isNaN(inputMargin)) {
               inputMargin = -parentMargin;
               
            } else {
               inputMargin = inputMargin - parentMargin;
            }
            input.css('margin-left', inputMargin + 'px');
         }
      }
      
      elem.css('display', 'none');
      
      // setting the focus and selecting text
      input.get(0).focus();
      input.get(0).select();
      
      // set the value on 'return' or 'enter'
      input.keypress(function(e) {
         if (e.which == 13) {
            setDirectValue(elem, input);
         };
               });
      
      // set the value on loosing focus
      input.blur(function() {
         setDirectValue(elem, input);
      });
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
    * Event-handler for droppable drop-event dragging within tree.
    *
    * @param {Object} e event
    * @param {Object} ui ui-object
    */
   var dropItem = cms.sitemap.dropItem = function(e, ui) {
      // target item
      if ($(this).attr('id') == 'favorite-drop-list') {
         $(this).removeClass(classHovered);
         var dragClone = ui.draggable.clone();
         dragClone.find('div.cms-handle').remove();
         var _next = function() {
            dragClone.find('div.' + itemClass).removeClass(dragClass);
            dragClone.find('.' + classAdditionalShow).removeClass(classAdditionalShow)
            dragClone.appendTo(cms.sitemap.dom.favoriteList);
            var newFav = serializeSitemapElement(dragClone, true);
            
            _addFavorite(newFav);
            saveFavorites(function() {
                        });
         }
         if (dragClone.find('ul').length) {
            keepTree(dragClone, _next);
         } else {
            _next();
         }
         
         return;
      }
      var li = $(this).parent();
      var formerParent = ui.draggable.parent();
      var removeFormerParent = false;
      var parentURL = '';
      // if the former parent li and the target li are not the same
      // and the former parent is going to lose it's last child 
      // (the currently dragged item will appear twice, once as the original and once as the ui-helper),
      // set the removeFormerParent flag
      if (li.get(0) != formerParent.parent().get(0) && formerParent.children().length == 2) {
         removeFormerParent = true;
      }
      if ($(this).hasClass(dropzoneClass)) {
         // dropping over dropzone, so insert before
         li.before(ui.draggable);
         var parentLi = li.parent().closest('li');
         if (parentLi.length) {
            parentURL = parentLi.children('div.' + itemClass).find('span.cms-url').attr('alt');
            parentURL = parentURL.substring(0, parentURL.lastIndexOf('.'));
         }
      } else {
         parentURL = li.children('div.' + itemClass).find('span.cms-url').attr('alt');
         parentURL = parentURL.substring(0, parentURL.lastIndexOf('.'));
         // dropping over item, so insert into child-ul
         if (li.children('ul').length == 0) {
            // no child-ul present yet
            li.append('<ul/>');
            li.children('div.' + dropzoneClass).after(openerHtml);
         }
         li.removeClass(classClosed);
         li.children('ul').append(ui.draggable);
      }
      setURLs(ui.draggable, parentURL);
      // remove the now empty former parent ul and the opener span
      if (removeFormerParent) {
         formerParent.siblings('span.' + classOpener).remove();
         formerParent.remove();
      }
      $(this).removeClass(classHovered);
      
   }
   
   /**
    * Event-handler for droppable drop-event dragging from menu into tree.
    *
    * @param {Object} e event
    * @param {Object} ui ui-object
    */
   var dropItemMenu = cms.sitemap.dropItemMenu = function(e, ui) {
      var li = $(this).parent();
      var dropClone = ui.draggable.clone();
      _addOpenerAndDropzone(dropClone);
      dropClone.removeClass(classSubtree);
      var parentURL = '';
      if ($(this).hasClass(dropzoneClass)) {
         // dropping over dropzone, so insert before
         li.before(dropClone);
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
         li.children('ul').append(dropClone);
         parentURL = li.children('div.' + itemClass).find('span.cms-url').attr('alt');
         parentURL = parentURL.substring(0, parentURL.lastIndexOf('.'));
      }
      dropClone.find('div.cms-handle').remove();
      dropClone.find('div.' + itemClass).removeClass(dragClass);
      setURLs(dropClone, parentURL);
      $(this).removeClass(classHovered);
   }
   
   /**
    * Asks the server whether a given url name is valid.
    *
    * The callback which is called after the AJAX request is finished receives a boolean status flag
    * and an object containing the properties invalid (true if the url name is not valid) and urlName
    * (which is an escaped version of the original url name).
    *
    * @param {Object} urlName the url name to be checked
    * @param {Object} callback the callback that is called after the check
    */
   var checkUrlName = function(urlName, callback) {
      callback(true, {
         urlName: urlName
      });
   }
   
   /**
    * Click-handler to edit the item title and properties for this handler.
    *
    */
   var editPage = function() {
      var itemDiv = $(this).closest('div.' + itemClass);
      var $dialog = $('<div id="cms-alert-dialog" style="display: none"></div>');
      $dialog.appendTo('body');
      $dialog.append('<p style="margin-bottom: 4px;">Edit page</p><label for="pageTitle">Title:</label>&nbsp;&nbsp;<input type="text" id="pageTitle" value="' + itemDiv.find('h3').text() + '" />');
      $('<label for="cms-page-urlname"></label>').text('URL name:').appendTo($dialog);
      var $urlnameInput = $('<input type="text" id="cms-page-urlname"></input>').val(itemDiv.find('.' + classUrlName).text()).appendTo($dialog);
      $dialog.dialog({
         zIndex: 9999,
         title: 'Edit Page',
         modal: true,
         close: function() {
            $dialog.dialog('destroy');
            $dialog.remove();
         },
         buttons: {
            'OK': function() {
               var newTitle = $('#pageTitle').val();
               var validationErrors = [];
               
               if (newTitle && newTitle != '') {
                  itemDiv.find('h3').text(newTitle);
                  $dialog.dialog('destroy');
                  $dialog.remove();
               } else {
                  validationErrors.push('Please insert the page title');
               }
               var newUrlName = $urlnameInput.val();
               if (newUrlName && newUrlName != '') {
                  checkUrlName(newUrlName, function(ok, data) {
                     if (data.invalid) {
                        alert("error");
                     } else {
                        newUrlName = data.urlName;
                        $dialog.dialog('destroy');
                     }
                  })
                  if (!$('#dialogError').length) {
                     $('#pageTitle').after('<p id="dialogError" style="color: red; margin-top: 4px;">Please insert the page title.</p>');
                  }
                  
               }
               
               
               
               
            },
            'Cancel': function() {
               $dialog.dialog('destroy');
               $dialog.remove();
            }
         }
      
      });
   }
   
   //   $.fn.validation = function($element, condition) {
   //       return this.each(function() {
   //           var $element = $(this);
   //           $element.addClass('cms-validator');
   //           $element.data('cms-validator-condition', condition)
   //       }
   //   }
   //   
   //   $.fn.validate = function() {
   //       return this.find('cms-validator').each(function() {
   //           var $form = $(this);
   //           $form.find('cms-validator').each(function() {
   //           var $v = $(this);
   //           if ($v.data('cms-validator-condition')()) {
   //               $v.hide();
   //           } else {
   //               $v.show();
   //           }
   //       })
   //   }
   //   
   //   
   
   
   /**
    * Click-handler to edit the item title and properties for this handler.
    *
    */
   var editPage = function() {
      var itemDiv = $(this).closest('div.' + itemClass);
      var $dialog = $('<div id="cms-alert-dialog" style="display: none"></div>');
      $dialog.appendTo('body');
      $dialog.append('<p style="margin-bottom: 4px;">Edit page</p><label for="pageTitle">Title:</label>&nbsp;&nbsp;<input type="text" id="pageTitle" value="' + itemDiv.find('h3').text() + '" />');
      $dialog.dialog({
         zIndex: 9999,
         title: 'Edit Page',
         modal: true,
         close: function() {
            $dialog.dialog('destroy');
            $dialog.remove();
         },
         buttons: {
            'OK': function() {
               var newTitle = $('#pageTitle').val();
               if (newTitle && newTitle != '') {
                  itemDiv.find('h3').text(newTitle);
                  $dialog.dialog('destroy');
                  $dialog.remove();
                  
               } else {
                  if (!$('#dialogError').length) {
                     $('#pageTitle').after('<p id="dialogError" style="color: red; margin-top: 4px;">Please insert the page title.</p>');
                  }
               }
               
               
            },
            'Cancel': function() {
               $dialog.dialog('destroy');
               $dialog.remove();
            }
         }
      
      });
   }
   
   
   /**
    * Initializes the sitemap editor.
    *
    */
   var initSitemap = cms.sitemap.initSitemap = function() {
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
         setURLs($(this), '');
      });
      
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
      
      // assigning event-handler
      $('#' + sitemapId + ' span.' + classOpener).live('click', function() {
         $(this).parent().toggleClass(classClosed);
      });
      $('a.cms-delete').live('click', deletePage);
      $('a.cms-new').live('click', newPage);
      $('a.cms-edit').live('click', editPage);
      $('#' + sitemapId + ' div.' + itemClass + ' h3').directInput({
         marginHack: true,
         live: true
      });
      
      $('#' + sitemapId + ' div.' + itemClass + ' span.cms-url-name').directInput({
         marginHack: true,
         live: true,
         readValue: function(elem) {
            return elem.attr('alt');
         },
         setValue: setURLName
      });
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
      $('#fav-edit').click(_editFavorites);
      $(window).unload(onUnload);
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
   var newItemHtml = function(title) {
      return '<li><div class="' + dropzoneClass + '"></div><div class="ui-state-hover ui-corner-all ' + itemClass + '"><a class="cms-left ui-icon cms-icon-triangle"></a>\
              <h3>' +
      title +
      '</h3>\
              <div class="cms-additional-info">\
                URL-Name:<span class="cms-url-name">' +
      title.toLowerCase() +
      '</span><br/>\
                URL:<span class="cms-url"></span><br/>\
                VFS-Path:<span class="cms-vfs-path">/demo3/123.xml</span><br/>\
              </div></div></li>';
   }
   
   /**
    * Click-handler to create a new sub-page. Opening an edit dialog for title and properties.
    */
   var newPage = function() {
      var liItem = $(this).closest('li');
      var $dialog = $('<div id="cms-alert-dialog" style="display: none"></div>');
      $dialog.appendTo('body');
      $dialog.append('<p style="margin-bottom: 4px;">Add a new page</p><label for="pageTitle">Title:</label>&nbsp;&nbsp;<input type="text" id="pageTitle" />');
      $dialog.dialog({
         zIndex: 9999,
         title: 'New Page',
         modal: true,
         close: function() {
            $dialog.dialog('destroy');
            $dialog.remove();
         },
         buttons: {
            'OK': function() {
               var newTitle = $('#pageTitle').val();
               if (newTitle && newTitle != '') {
                  if (liItem.children('ul').length == 0) {
                     // no child-ul present yet
                     liItem.append('<ul/>');
                     liItem.children('div.' + dropzoneClass).after(openerHtml);
                  }
                  liItem.removeClass(classClosed);
                  var newItem = $(newItemHtml(newTitle)).appendTo(liItem.children('ul'));
                  // setting URL
                  var parentURL = liItem.children('div.' + itemClass).find('span.cms-url').attr('alt');
                  parentURL = parentURL.substring(0, parentURL.lastIndexOf('.'));
                  setURLs(newItem, parentURL);
                  addHandles(newItem.children('div.' + itemClass), sitemapModes);
                  destroyDraggable();
                  initDraggable();
                  $dialog.dialog('destroy');
                  $dialog.remove();
                  setPageChanged(true);
                  
               } else {
                  if (!$('#dialogError').length) {
                     $('#pageTitle').after('<p id="dialogError" style="color: red; margin-top: 4px;">Please insert the page title.</p>');
                  }
               }
            },
            'Cancel': function() {
               $dialog.dialog('destroy');
               $dialog.remove();
            }
         }
      
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
   var setDirectValue = function(elem, input) {
      elem.text(input.val()).css('display', 'block');
      input.remove();
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
   
   
   var debug1 = function($elem) {
      var x = 1 + 1;
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
      debug1($(this));
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
      var sitemapEntry = serializeSitemapElement(dragClone, true);
      _addRecent(sitemapEntry);
      saveRecent(cms.sitemap.recent, function() {
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
         this.button.addClass('ui-state-active');
         var $sitemapElem = $('#' + sitemapId);
         var sitemap = serializeSitemap($sitemapElem);
         saveSitemap(sitemap, function(ok, data) {
            if (ok) {
               setSitemapChanged(false);
            } else {
               alert("error")
               //display error message ? 
            }
         });
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
         cms.sitemap.dom.newMenu.find('ul').append(newItemHtml('NewPage'));
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
         loadFavorites(function(ok, data) {
            var favorites = data.favorites;
            $('#favorite_list_items').empty();
            var $favContent = _buildSitemap(favorites).appendTo('#favorite_list_items');
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
         loadRecent(function(ok, data) {
            var recent = data.recent;
            $('#recent_list_items').empty();
            var $recContent = _buildSitemap(recent).appendTo('#recent_list_items');
            
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
            }
   }, {
      // publish mode
      name: 'publish',
      title: 'Publish',
      wide: false,
      floatRight: true,
      create: createButton,
      enable: function() {
         this.button.addClass('ui-state-active');
         var publishDialog = new cms.publish.PublishDialog();
         publishDialog.start();
      },
      disable: function() {
         this.button.removeClass('ui-state-active');
      },
      init: function() {
            }
   }];
   
   
   
   
   /**
    * Serializes a sitemap DOM element to a JSON structure
    * @param {Object} $element the DOM element representing the sitemap (wrapped in a jQuery object)
    */
   var serializeSitemap = function($element) {
      var result = [];
      $element.children('li').each(function() {
         result.push(serializeSitemapElement($(this)));
      });
      return result;
   }
   
   
   
   
   
   
   /**
    * Serializes a sitemap DOM element to a JSON data structure.
    *
    * @param {Object} $element the DOM element for the sitemap entry ( wrapped in a jQuery object)
    * @param {Boolean} includeContent if true, the HTML is included in the output object
    */
   var serializeSitemapElement = function($element, includeContent) {
      var $sitemapItem = $element.children('.' + itemClass);
      var title = $sitemapItem.children('h3:first').text();
      var $addInfo = $sitemapItem.children('.' + classAdditionalInfo);
      var urlName = $addInfo.children('.' + classUrlName).text();
      var vfsPath = $addInfo.children('.' + classVfsPath).text();
      var $children = $element.children('ul').children('li');
      var childObjects = [];
      $children.each(function() {
         childObjects.push(serializeSitemapElement($(this), includeContent));
      });
      var entryData = getEntryData($element);
      var dataNode = $sitemapItem.get(0);
      var result = {
         title: title,
         id: entryData.id,
         name: urlName,
         subentries: childObjects,
         properties: entryData.properties
      };
      if (includeContent) {
         var content = $('<p></p>').append($sitemapItem.clone()).html();
         result.content = content;
      }
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
      $('<div></div>').addClass(dropzoneClass).appendTo($li);
      if (data.subentries && data.subentries.length > 0) {
         $('<span></span>').addClass(classOpener).appendTo($li);
      }
      
      
      var $ul = null;
      var $item = $(data.content).appendTo($li);
      if (data.subentries && data.subentries.length > 0) {
         $li.addClass(classSubtree);
         $li.addClass(classClosed);
         var $ul = $('<ul></ul>').appendTo($li);
         var subEntries = $.map(data.subentries, _buildSitemapElement);
         $.each(subEntries, actionAppendTo($ul));
         
      }
      var dataNode = $item.get(0);
      setEntryData($li, {
         properties: data.properties,
         id: data.id
      });
      return $li;
   }
   
   /**
    * Builds a jQuery object containing sitemap DOM elements from a list of sitemap entry data.
    *
    * @param {Object} data the list of sitemap entries.
    */
   var _buildSitemap = function(data) {
      return $($.map(data, _buildSitemapElement))
   }
   
   var getOpeners = function($element, filterString) {
      var $lis = $element.find('.' + classOpener).closest('li');
      $lis.filter(filterString).child('.' + classOpener);
   }
   
   var getParentOpeners = function($element, filterString) {
      var $lis = $element.parents('.' + classOpener).closest('li');
      $lis.filter(filterString).child('.' + classOpener);
   }
   
   var closeAll = function($element) {
      getOpeners($element, '.' + classClosed).trigger('click');
   }
   
   var openAll = function($element) {
      getOpeners($element, ':not(.' + classClosed + ')').trigger('click');
   }
   
   var checkUrlName = function(urlname, callbackOk, callbackInvalid) {
      postJSON('urlname', urlname, function(ok, data) {
         if (data.invalid) {
            callbackInvalid();
         } else {
            callbackOk(data.urlname);
         }
      })
   }
   
   /**
    * Loads the sitemap and continues to initialize the sitemap editor after loading is finished.
    *
    */
   var loadAndInitSitemap = cms.sitemap.loadAndInitSitemap = function() {
      cms.data.sitemapPostJSON('all', {}, function(ok, data) {
         if (!ok) {
            return;
         }
         var sitemap = data.sitemap;
         var favorites = data.favorites;
         var recent = data.recent;
         _buildSitemap(sitemap).appendTo('#' + sitemapId);
         initSitemap();
      })
   }
   
   
   
   
   /**
    * AJAX call that sends the sitemap to the server to save it.
    * @param {Object} sitemap the sitemap
    * @param {Object} callback the callback that should be called after the server sends its response
    */
   var saveSitemap = cms.sitemap.saveSitemap = function(sitemap, callback) {
      cms.data.sitemapPostJSON('save', {
         sitemap: sitemap
      }, callback);
   }
   
   var sitemapChanged = false;
   
   /**
    * Sets the "changed" status of the sitemap editor
    * @param {Boolean} changed a flag that indicates whether the sitemap should be marked as changed or unchanged
    */
   var setSitemapChanged = function(changed) {
      sitemapChanged = changed;
      var $saveButton = $('button[name=save]');
      if (changed) {
         $saveButton.removeClass('cms-deactivated');
      } else {
         $saveButton.addClass('cms-deactivated');
      }
   }
   
   
   /**
    * Requests the favorite list from the server and stores it in cms.sitemap.favorites.
    *
    * @param {Object} callback the function to be called after the favorite list has been loaded
    */
   var loadFavorites = function(callback) {
      cms.data.sitemapPostJSON('get', {
         'fav': true
      }, function(ok, data) {
         if (!ok) {
            return;
         }
         cms.sitemap.favorites = data.favorites;
         callback(ok, data);
      })
   }
   
   
   
   /**
    * Requests the recent list from the server and stores it in cms.sitemap.recent.
    *
    * @param {Object} callback the function to be called after the recent list has been loaded
    */
   var loadRecent = function(callback) {
      cms.data.sitemapPostJSON('get', {
         'rec': true
      }, function(ok, data) {
         if (!ok) {
            return;
         }
         cms.sitemap.recent = data.recent;
         callback(ok, data)
      })
   }
   
   /**
    * Sends the favorite list to the server to save it.
    *
    * @param {Object} callback the function to be called after the server has replied
    */
   var saveFavorites = function(callback) {
      cms.data.sitemapPostJSON('set', {
         fav: cms.sitemap.favorites
      }, callback);
   }
   
   /**
    * Sends the recent list to the server to save it.
    *
    * @param {Object} callback the function to be called after the server has replied
    */
   var saveRecent = function(callback) {
      cms.data.sitemapPostJSON('set', {
         'rec': cms.sitemap.recent
      }, callback);
   }
   
   var getEntryData = function($item) {
      var resultStr = decodeURIComponent($item.attr("rel"));
      return JSON.parse(resultStr);
   }
   
   var setEntryData = function($item, obj) {
      var dataStr = encodeURIComponent(JSON.stringify(obj));
      $item.attr('rel', dataStr);
   }
   
   var onUnload = function() {
      if (sitemapChanged) {
         var saveChanges = window.confirm('Do you want to save your changes to the sitemap?');
         if (saveChanges) {
            saveSitemap(serializeSitemap($('#' + sitemapId)), function() {
                        });
         } else {
            setPageChanged(false);
         }
      }
   }
   
   var validateSitemapEntry = function(title, name, callback) {
      var errors = [];
      if (title == '') {
         errors.push('Title can\'t be empty');
      }
      if (name == '') {
         errors.push('URL name can\'t be empty')
      }
      callback(title, name, errors);
   }
   
   var showValidationResults = function($form, validationResults) {
      for (var i = 0; i < validationResults.length; i++) {
         var vr = validationResults[i];
         //$(vr.selector, $form)[vr.method](vr.text)
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
            return serializeSitemapElement($(sm), true);
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
   
})(cms);
