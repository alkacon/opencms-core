(function(cms) {

   /** html-class for closed item. */
   var classClosed = 'closed';
   
   /** html-class for opener handle. */
   var classOpener = 'opener';
   
   /** html-class for dragged item. */
   var dragClass = 'dragging';
   
   /** html-class for dropzone. */
   var dropzoneClass = 'dropzone';
   
   /** html-class for sitemap-item. */
   var itemClass = 'item';
   
   /** html-code for opener handle. */
   var openerHtml = '<span class="' + classOpener + '"></span>';
   
   /** html-id for sitemap. */
   var sitemapId = 'sitemap';
   
   /** Map of jquery objects. */
   cms.sitemap.dom = {};
   
   /**
    * Timer object used for delayed hover effect.
    */
   var timer = cms.sitemap.timer = {
      id: null,
      handleDiv: null,
      adeMode: null
   };
   
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
            if (cms.sitemap.currentMode != null) {
               cms.sitemap.currentMode.disable();
            }
            cms.sitemap.currentMode = self;
            self.enable();
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
                  liItem.prependTo(cms.sitemap.dom.recentMenu.find('ul'));
               },
               'Chancel': function() {
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
         liItem.prependTo(cms.sitemap.dom.recentMenu.find('ul'));
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
      var input = $('<input name="directInput" type="text" class="direct-input" value="' + elem.text() + '" />');
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
         $(this).removeClass('hovered');
         var dragClone = ui.draggable.clone();
         dragClone.find('div.cms-handle').remove();
         if (dragClone.find('ul').length) {
            keepTree(dragClone);
            
         }
         dragClone.find('div.' + itemClass).removeClass(dragClass);
         dragClone.appendTo(cms.sitemap.dom.favoriteList);
         return;
      }
      var li = $(this).parent();
      var formerParent = ui.draggable.parent();
      var removeFormerParent = false;
      
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
      } else {
         // dropping over item, so insert into child-ul
         if (li.children('ul').length == 0) {
            // no child-ul present yet
            li.append('<ul/>');
            li.children('div.' + dropzoneClass).after(openerHtml);
         }
         li.removeClass(classClosed);
         li.children('ul').append(ui.draggable);
         
      }
      
      // remove the now empty former parent ul and the opener span
      if (removeFormerParent) {
         formerParent.siblings('span.' + classOpener).remove();
         formerParent.remove();
      }
      $(this).removeClass('hovered');
      
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
      dropClone.removeClass('subtree');
      if ($(this).hasClass(dropzoneClass)) {
         // dropping over dropzone, so insert before
         li.before(dropClone);
      } else {
         // dropping over item, so insert into child-ul
         if (li.children('ul').length == 0) {
            // no child-ul present yet
            li.append('<ul/>');
            li.children('div.' + dropzoneClass).after(openerHtml);
         }
         li.removeClass(classClosed);
         li.children('ul').append(dropClone);
         
      }
      dropClone.find('div.cms-handle').remove();
      dropClone.find('div.' + itemClass).removeClass(dragClass);
      $(this).removeClass('hovered');
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
            'Chancel': function() {
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
      $('#' + sitemapId + ' li:has(ul)').prepend(openerHtml);
      $('#' + sitemapId + ' li').prepend('<div class="' + dropzoneClass + '"></div>')
      
      // assigning event-handler
      $('#' + sitemapId + ' span.' + classOpener).live('click', function() {
         $(this).parent().toggleClass(classClosed);
      });
      $('a.cms-delete').live('click', deletePage);
      $('a.cms-new').live('click', newPage);
      $('a.cms-edit').live('click', editPage);
      
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
    */
   var keepTree = cms.sitemap.keepTree = function(dragClone) {
      var $dialog = $('<div id="cms-alert-dialog" style="display: none"></div>');
      $dialog.appendTo('body');
      $dialog.append('<p style="margin-bottom: 4px;">The page you copied to the \'Favorites\' has sub-pages.<br />Do you want to copy these sub-pages also?</p>');
      $dialog.dialog({
         zIndex: 9999,
         title: 'Favorites',
         modal: true,
         close: function() {
            return false;
         },
         buttons: {
            'Keep sub-pages': function() {
               dragClone.addClass('subtree');
               $dialog.dialog('destroy');
               $dialog.remove();
               
            },
            'Loose sub-pages': function() {
               dragClone.find('ul').remove();
               dragClone.find('span.' + classOpener).remove();
               $dialog.dialog('destroy');
               $dialog.remove();
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
      return '<li><div class="' + dropzoneClass + '"></div><div class="ui-state-hover ui-corner-all ' + itemClass + '"><h3>' + title + '</h3></div></li>';
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
                  
                  addHandles(newItem.children('div.' + itemClass), sitemapModes);
                  destroyDraggable();
                  initDraggable();
                  $dialog.dialog('destroy');
                  $dialog.remove();
                  
               } else {
                  if (!$('#dialogError').length) {
                     $('#pageTitle').after('<p id="dialogError" style="color: red; margin-top: 4px;">Please insert the page title.</p>');
                  }
               }
               
               
            },
            'Chancel': function() {
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
      $(this).removeClass('hovered');
      if (cms.sitemap.dom.currentMenu) {
         $('li', cms.sitemap.dom.currentMenu).draggable('option', 'refreshPositions', false);
      } else {
         $('#' + sitemapId + ' li').draggable('option', 'refreshPositions', false);
         if ($(this).attr('id') == 'favorite-drop-list') {
            ui.helper.removeClass('force-closed');
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
      $(this).addClass('hovered');
      if ($(this).hasClass(itemClass)) {
         $(this).closest('.closed').removeClass('closed');
         if (cms.sitemap.dom.currentMenu) {
            $('li', cms.sitemap.dom.currentMenu).draggable('option', 'refreshPositions', true);
         } else {
            $('#' + sitemapId + ' li').draggable('option', 'refreshPositions', true);
         }
      } else if ($(this).attr('id') == 'favorite-drop-list') {
         ui.helper.addClass('force-closed');
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
      var dragClone = $(this).clone();
      dragClone.find('div.cms-handle').remove();
      dragClone.find('ul').remove();
      dragClone.find('span.' + classOpener).remove();
      dragClone.appendTo(cms.sitemap.dom.recentMenu.find('ul'));
      $('div.cms-handle').css('display', 'block');
      cms.sitemap.dom.favoriteDrop.css('display', 'none');
   }
   
   /**
    * Event-handler for draggable stop-event dragging from menu into treetree.
    *
    * @param {Object} e event
    * @param {Object} ui ui-object
    */
   var stopDragMenu = cms.sitemap.stopDragMenu = function(e, ui) {
      $('div.' + itemClass, this).removeClass(dragClass);
      cms.sitemap.dom.currentMenu.children('div').css('display', 'block');
      // refresh droppable
      $('div.' + itemClass + ', div.' + dropzoneClass).droppable('destroy');
      $('div.' + itemClass + ', div.' + dropzoneClass).droppable(cms.sitemap.dropOptionsMenu);
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
      // reset mode
      name: 'reset',
      title: 'Reset',
      wide: false,
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
         $('#' + sitemapId + ' div.' + itemClass + ' h3').click(directInput);
      },
      disable: function() {
         this.button.removeClass('ui-state-active');
         destroyDraggable();
         $('div.cms-handle').remove();
         $('div.' + itemClass + ' h3').unbind('click', directInput);
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
      // save mode
      name: 'save',
      title: 'Save',
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
      // recent mode
      name: 'recent',
      title: 'Recent',
      wide: true,
      floatRight: true,
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
      init: function() {
         cms.sitemap.dom.recentMenu = $(cms.html.createMenu(cms.html.recentMenuId)).appendTo(cms.sitemap.dom.toolbarContent);
      }
   }, {
      // favorites mode
      name: 'favorites',
      title: 'Favorites',
      wide: true,
      floatRight: true,
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
      }
   }, {
      // new mode
      name: 'new',
      title: 'New',
      wide: true,
      floatRight: true,
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
         cms.sitemap.dom.newMenu.find('ul').append('<li><div class="' + dropzoneClass + '"></div><div class="ui-state-hover ui-corner-all ' + itemClass + '"><h3>New Page</h3></div></li>')
      }
   }];
   

})(cms);
