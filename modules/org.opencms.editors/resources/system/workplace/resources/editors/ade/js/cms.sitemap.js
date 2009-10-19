(function(cms) {
   var sitemapId = 'sitemap';
   var dropzoneClass = 'dropzone';
   var itemClass = 'item';
   var dragClass = 'dragging';
   var classClosed = 'closed';
   var classOpener = 'opener';
   var openerHtml = '<span class="' + classOpener + '">o</span>';
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
      
      $('#' + sitemapId + ' div.' + itemClass + ', #' + sitemapId + ' .' + dropzoneClass).droppable({
         accept: '#' + sitemapId + ' li',
         tolerance: 'pointer',
         drop: function(e, ui) {
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
            
         },
         over: function() {
            $(this).addClass('hovered');
         },
         out: function() {
            $(this).removeClass('hovered');
         }
      });
      $('#' + sitemapId + ' li').draggable({
         handle: ' > div.' + itemClass + ' > a.move',
         opacity: .8,
         addClasses: false,
         helper: 'clone',
         zIndex: 100,
         start: function(e, ui) {
            $('div.' + itemClass, this).addClass(dragClass);
         },
         stop: function(e, ui) {
            $('div.' + itemClass, this).removeClass(dragClass);
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
   
   var createButton = function(){
            var self = this;
            if (self.wide) {
                self.button=makeWideButton(self.name, self.title, 'cms-icon-' + self.name);
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
            if (self.floatRight){
                self.button.removeClass('cms-left').addClass('cms-right');
            }
            return self.button;
         };
         
   var sitemapModes = [
      {
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
      },
      {
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
         init: function() {
                  }
      },
       {
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
         init: function() {
                  }
      },
       {
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
         init: function() {
                  }
      },
       {
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
         init: function() {
                  }
      },
       {
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
      },
       {
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
      }
   ];
   

})(cms);

/*

 Old sitemap...

 

 (function(cms) {

 var toggleTree = cms.sitemap.toggleTree = function() {

 var button = $(this);

 if (button.hasClass('ui-state-active')) {

 // disabling tree mode

 $("#tree-list").jTree.destroy();

 $("#tree-list").css('display', 'none');

 button.removeClass('ui-state-active');

 } else {

 $('button.ui-state-active').trigger('click');

 // enabling tree-mode

 $('<div></div>').appendTo(document.body).addClass('ui-widget-overlay')

 .css( {

 width :$(document).width(),

 height :$(document).height(),

 zIndex :11000,

 opacity :0.8

 });

 $('<a class="cms-handle cms-move"></a>').appendTo(

 '#tree-list .cms-head');

 $('#tree-list').css('display', 'block');

 $("#tree-list")

 .jTree(

 {

 showHelper :true,

 hOpacity :0.5,

 hBg :"transparent",

 hColor :"#222",

 pBorder :"none",

 pBg :"#EEE url(images/placeholder-bg.gif) no-repeat scroll 0px 0px",

 pColor :"#222",

 pHeight :"37px",

 snapBack :1200,

 childOff :40,

 handle :"a.cms-move",

 onDrop :treeDrop,

 beforeArea :".cms-navtext",

 intoArea :".cms-file-icon",

 afterArea :".cms-title"

 });

 $("#jTreeHelper").addClass('cms-item-list');

 button.addClass('ui-state-active');

 }

 };

 var collapse = cms.sitemap.collapse = function() {

 $(this).closest('li').toggleClass('cms-collapsed');

 };

 var treeWork = cms.sitemap.treeWork = function(){

 $('ul:empty', listElem).remove();

 $('li a.cms-collapse-icon').each(function(){

 

 if (1!=$(this).closest('li').children('ul').length) {

 this.parentNode.removeChild(this);

 }

 });

 

 $('li.last', listElem).removeClass('last');

 $('ul', listElem).andSelf().each(function(){

 $(this).children('li').filter(':last').addClass('last');

 });

 

 $('li:has(ul)', listElem).children('div.ui-widget-content:not(:has(a.cms-collapse-icon))').append('<a class="cms-collapse-icon"></a>');

 }

 var treeDrop = cms.sitemap.treeDrop = function(event) {

 var elem = event.target ? event.target : event.srcElement;

 treeWork(elem);

 };

 var treeDrop_ = cms.sitemap.treeDrop_ = function() {

 var elem = 'ul.cms-item-list';

 $('li.last', elem).removeClass('last');

 $('ul', elem).each( function() {

 $(this).children('li').filter(':last').addClass('last')

 });

 var collapseIcon = $('<a class="cms-collapse-icon"></a>').click(

 function() {

 $(this).closest('li').toggleClass('cms-collapsed');

 });

 $('li:has(ul:has(li))', elem).children(

 'div.ui-widget-content:not(:has(a.cms-collapse-icon))').append(

 collapseIcon);

 };

 })(cms);

 */

