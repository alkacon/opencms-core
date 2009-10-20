(function(cms) {
   var sitemapId = 'sitemap';
   var dropzoneClass = 'dropzone';
   var itemClass = 'item';
   var dragClass = 'dragging';
   var classClosed = 'closed';
   var classOpener = 'opener';
   var openerHtml = '<span class="' + classOpener + '"></span>';
   var handleHtml = '<div class="cms-handle"><a class="cms-move"></a></div>';
   var newItemHtml = function(title) {
       return '<li><div class="' + dropzoneClass + '"></div><div class="ui-widget-content ui-state-hover ui-corner-all ' + itemClass + '"><h3>' + title + '</h3></div></li>';
   }
   cms.sitemap.dom = {};
   cms.sitemap.currentMode = null;
   var initSitemap = cms.sitemap.initSitemap = function() {
      cms.sitemap.dom.toolbar = $(cms.html.toolbar).appendTo(document.body);
      cms.sitemap.dom.toolbarContent = $('#toolbar_content', cms.sitemap.dom.toolbar);
      //create buttons:
      for (i = 0; i < sitemapModes.length; i++) {
         sitemapModes[i].create().appendTo(cms.sitemap.dom.toolbarContent);
      }
      
      $('#' + sitemapId + ' li:has(ul)').prepend(openerHtml);
      $('#' + sitemapId + ' li').prepend('<div class="' + dropzoneClass + '"></div>')
      $('#' + sitemapId + ' span.' + classOpener).live('click', function() {
         $(this).parent().toggleClass(classClosed);
      });
      $('a.cms-delete').live('click', deletePage);
      $('a.cms-new').live('click', newPage);
      //initDraggable();
   }
   
   var destroyDraggable = cms.sitemap.destroyDraggable = function(){
       $('#' + sitemapId + ' div.' + itemClass + ', #' + sitemapId + ' .' + dropzoneClass).droppable('destroy');
       $('#' + sitemapId + ' li').draggable('destroy');
   }
   
   var initDraggable = cms.sitemap.initDraggable = function(){
       $('#' + sitemapId + ' div.' + itemClass + ', #' + sitemapId + ' .' + dropzoneClass).droppable({
         accept: '#' + sitemapId + ' li',
         tolerance: 'pointer',
         drop: dropItem,
         over: function() {
            $(this).addClass('hovered');
         },
         out: function() {
            $(this).removeClass('hovered');
         }
      });
      $('#' + sitemapId + ' li').draggable({
         handle: ' > div.' + itemClass + '> div.cms-handle > a.cms-move',
         opacity: .8,
         addClasses: false,
         helper: 'clone',
         zIndex: 100,
         start: startDrag,
         stop: stopDrag
      });
   }
   var stopDrag = cms.sitemap.stopDrag =function(e, ui) {
            $('div.' + itemClass, this).removeClass(dragClass);
            $('div.cms-handle').css('display', 'block');
         }
   
   var startDrag = cms.sitemap.startDrag = function(e, ui) {
            $('div.' + itemClass, this).addClass(dragClass);
            $('div.cms-handle').css('display', 'none');
            stopHover();
            $('div.cms-handle', ui.helper).removeClass('ui-widget-header').css({
               'width': '24px',
               'right': '0px',
               'display': 'block'
            }).children().removeClass('ui-corner-all ui-state-default').not('a.cms-move').css('display', 'none');
         }
   var dropItem = cms.sitemap.dropItem = function(e, ui) {
            var li = $(this).parent();
            var formerParent = ui.draggable.parent();
            var removeFormerParent = false;
            if (li != formerParent && formerParent.children().length == 2) {
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
            if (removeFormerParent) {
               formerParent.siblings('span.' + classOpener).remove();
               formerParent.remove();
            }
            $(this).removeClass('hovered');
            
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
   
   var createButton = function() {
      var self = this;
      if (self.wide) {
         self.button = makeWideButton(self.name, self.title, 'cms-icon-' + self.name);
      } else {
         self.button = makeModeButton(self.name, self.title, 'cms-icon-' + self.name);
      }
      self.button.click(function() {
          destroyDraggable();
         $('div.cms-handle').remove();
         if ((cms.sitemap.currentMode == null) || (cms.sitemap.currentMode.name != self.name)) {
            if (cms.sitemap.currentMode != null) {
               cms.sitemap.currentMode.disable();
            }
            cms.sitemap.currentMode = self;
            if ($.isFunction(self.createHandle)) {
                initDraggable();
               $('#' + sitemapId + ' li div.' + itemClass).each(function() {
                  addHandles(this, sitemapModes);
               });
            }
            self.enable();
         } else {
            self.disable();
            cms.sitemap.currentMode = null;
         }
      });
      if (self.floatRight) {
         self.button.removeClass('cms-left').addClass('cms-right');
      }
      return self.button;
   };
   
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
         cms.sitemap.startHoverTimeout(handleDiv, cms.sitemap.currentMode.name);
      }, function() {
         cms.sitemap.stopHover();
      });
   }
   
   var deletePage = function() {
      var liItem = $(this).closest('li');
      var $dialog = $('<div id="cms-alert-dialog" style="display: none"></div>');
      $dialog.appendTo('body');
      $dialog.text('Do you realy want to remove this page from the sitemap?');
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
               liItem.remove();
            },
            'Chancel': function() {
               $dialog.dialog('destroy');
               $dialog.remove();
            }
         }
      
      });
   }
   
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
                  var newItem=$(newItemHtml(newTitle)).appendTo(liItem.children('ul'));
                  
                  addHandles(newItem.children('div.'+itemClass), sitemapModes);
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
   
   var sitemapModes = [{
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
      name: 'move',
      title: 'Move',
      wide: false,
      create: createButton,
      enable: function() {
         this.button.addClass('ui-state-active');
      },
      disable: function() {
         this.button.removeClass('ui-state-active');
      },
      createHandle: function() {
         return $('<a class="cms-move"></a>');
      },
      init: function() {
            }
   }, {
      name: 'delete',
      title: 'Delete',
      wide: false,
      create: createButton,
      enable: function() {
         this.button.addClass('ui-state-active');
         
      },
      disable: function() {
         this.button.removeClass('ui-state-active');
      },
      createHandle: function() {
         return $('<a class="cms-delete"></a>');
      },
      init: function() {
            }
   }, {
      name: 'properties',
      title: 'Properties',
      wide: false,
      create: createButton,
      enable: function() {
         this.button.addClass('ui-state-active');
      },
      disable: function() {
         this.button.removeClass('ui-state-active');
      },
      createHandle: function() {
         return $('<a class="cms-properties"></a>');
      },
      init: function() {
            }
   }, {
      name: 'new',
      title: 'New',
      wide: true,
      create: createButton,
      enable: function() {
         this.button.addClass('ui-state-active');
      },
      disable: function() {
         this.button.removeClass('ui-state-active');
      },
      createHandle: function() {
         return $('<a class="cms-new"></a>');
      },
      init: function() {
            }
   }, {
      name: 'favorites',
      title: 'Favorites',
      wide: true,
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
   }];
   
   /**
    * Timer object used for delayed hover effect.
    */
   var timer = cms.sitemap.timer = {
      id: null,
      handleDiv: null,
      adeMode: null
   };
   
   /**
    * Starts a hover timeout.<p>
    *
    * @param {Object} handleDiv
    * @param {Object} adeMode
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
   
   
})(cms);