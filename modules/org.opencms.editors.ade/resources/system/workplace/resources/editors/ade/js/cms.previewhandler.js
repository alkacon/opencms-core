(function(cms) {

   ///// Content Handler Function definition //////////////////////////    
   
   var editableTabId = cms.previewhandler.editableTabId = 'cms-editable-tabs';
   
   var keys = cms.previewhandler.keys = {
       'propertiesTabId' : 'tabs-edit-area'
   };
   
   
   
   /** Html sceleton for the upper button bar in the editable properties area. */
   var switchToFormatBar = '<div class="button-bar cms-top-bottom">\
                               <button name="switchToFormat" class="cms-right ui-state-default ui-corner-all">\
                                     <span class="cms-galleries-button">Image&nbsp;Format</span>\
                               </button>\
                               <span class="cms-title">File Properties:</span>\
                            </div>';
   /** Html sceleton for the lower button bar in the editable properties area. */                      
   var okCloseButtonBar = '<div class="button-bar cms-top">\
                                 <button name="previewClose" class="cms-right ui-state-default ui-corner-all">\
                                       <span class="cms-galleries-button">Close</span>\
                                 </button>\
                                 <button name="previewSave" disabled="true" class="cms-right ui-state-default ui-corner-all">\
                                     <span class="cms-galleries-button">Save</span>\
                                 </button>\
                           </div>';                             
   
   /** html fragment for the item preview. */
   cms.previewhandler.htmlPreviewSceleton = '<div id="cms-preview" class="ui-corner-all">\
                                                    <div class="close-icon ui-icon ui-icon-closethick ui-corner-all" ></div>\
                                	                <div class="preview-area ui-widget-content ui-corner-all"></div>\
                                                    <div id="' + cms.previewhandler.editableTabId + '"></div>\
                                                </div>';
                                                
   var tabsNav = '<ul><li><a href="#' + cms.previewhandler.keys['propertiesTabId'] + '">Properties</a></li></ul>'                                               
   
   /**
    * Displays the content of the preview item.
    *
    * @param {Object} itemData server response as JSON object with preview data
    */
   var showItemPreview = function(itemData) {
      //display the html preview
      $('.preview-area').append(itemData['previewdata']['itemhtml']);
      
      // display editable properties
      cms.galleries.getContentHandler()['showEditArea'](itemData['previewdata']['properties']);
      
      $('#' + cms.previewhandler.editableTabId).prepend(tabsNav);
      
      // bind the select tab event, fill the content of the result tab on selection
      $('#' + cms.previewhandler.editableTabId).tabs({});                    
                  
      //bind click event to preview close button 
      $('#cms-preview div.close-icon').click(function() {
         if ($(this).hasClass('cms-properties-changed')) {
            cms.galleries.loadSearchResults();
            $(this).removeClass('cms-properties-changed');
         }
         cms.galleries.getContentHandler()['closePreview']();
         
      });
      
      $('.button-bar button').live('mouseover', function() {
         $(this).toggleClass('ui-state-hover', true);
      }).live('mouseout', function() {
         $(this).toggleClass('ui-state-hover', false);
      });
      
      // fade in the preview      
      $('#cms-preview').fadeIn('slow');
   }
   
   /**
    * Generates html for the editable properties and add them to the dom.
    *
    * @param {Object} itemProperties server response as JSON object with the editable properties
    */
   var getEditArea = function(itemProperties) {      
      $('<div id="' + keys['propertiesTabId'] + '"></div>').appendTo('#' + cms.previewhandler.editableTabId);
      cms.galleries.getContentHandler()['fillProperties'](itemProperties);                                                                                                                         
   }
   
   /**
    * Generate html for the given properties.
    * 
    * @param {Object} itemProperties
    */
   var fillProperties = function(/**Json array*/itemProperties) {
      var target = $('#' + keys['propertiesTabId']);
      // generate editable form fields
      var form = $('<div class="edit-form cms-scrolling-properties"></div>');
      for (var i = 0; i < itemProperties.length; i++) {
          $('<div class="cms-editable-field '+( i%2  == 0 ? 'cms-left' : 'cms-right')+'"></div>').attr('alt', itemProperties[i]['name'])
             .appendTo(form)
             .append('<span class="cms-item-title cms-width-90">' + itemProperties[i]['name'] + '</span>')
             .append('<input class="cms-item-edit ui-corner-all" name="' + itemProperties[i]['name'] + '" title="Edit ' + itemProperties[i]['name'] + '" value="' + (itemProperties[i]['value'] ? itemProperties[i]['value'] : '') + '" />');    
      }      
      target.append(form);
      
      // bind direct input to the editable fields
      $('.cms-item-edit').change(function() {
            target.find('button[name="previewSave"]').removeAttr('disabled');
            $(this).addClass('cms-item-changed');
         });    
         
      // add ok-close button bar to the edit area                        
      target.append($(okCloseButtonBar));
      target.find('button[name="previewSave"]')
          .click(cms.galleries.getContentHandler()['saveChangedProperty']);
      target.find('button[name="previewClose"]')
          .click(cms.galleries.getContentHandler()['closePreview']);
      // TODO: comment in for direct publish
      /* $('.edit-area button[name="publishSave"]').click(publishChangedProperty);*/
      
      // add select button if in widget or editor mode
      if (cms.galleries.isSelectableItem()) {
         target.find('button[name="previewClose"]').after('<button name="previewSelect" class="cms-right ui-state-default ui-corner-all">\
                                <span class="cms-galleries-button cms-galleries-icon-apply cms-icon-text">Select</span>\
                          </button>');
         target.find('button[name="previewSelect"]').click(function() {            
            var itemType = $('#results li[alt="' + cms.galleries.initValues['path'] + '"]').data('type');
            var itemId = $(this).closest('#cms-preview').attr('alt');
            cms.galleries.getContentHandler(itemType)['setValues'][cms.galleries.initValues['dialogMode']](itemId, cms.galleries.initValues['fieldId']);
         });
      }
   }
   
   /**
    * Updates the value of the changed property in the preview and marks the element as changed.
    * This function overwrites 'setValue' method from the 'directInput' extension.
    *
    * @param {Object} elem the html element to be changed
    * @param {Object} input the input field used to change the content of the html element
    */
   var markChangedProperty = function(elem, input) {
      var previous = elem.text();
      var current = input.val();
      if (previous != current) {
         elem.text(current);
         elem.addClass('cms-item-changed');
      }
      elem.css('display', '');
      input.remove();
   }
   
   /**
    * Refresh the preview after changes.
    *
    * @param {Object} itemData the data to update the preview
    */
   var refreshDefaultPreview = function(itemData) {
      $('#cms-preview div.preview-area, #' + cms.previewhandler.keys['propertiesTabId']).empty();
      //display the html preview 
      $('.preview-area').append(itemData['previewdata']['itemhtml']);
      cms.galleries.getContentHandler()['fillProperties'](itemData['previewdata']['properties']);
      
   }
   
   /**
    * Callback function for click event on the 'save' button.
    */
   var saveChangedProperty = function() {
      var changedProperties = $('.cms-item-edit.cms-item-changed');
      
      // build json object with changed properties
      var changes = {
         'properties': []
      };
      $.each(changedProperties, function() {
         var property = {};
         property['name'] = $(this).closest('div').attr('alt');
         property['value'] = $(this).val();
         changes['properties'].push(property);
      });
      
      var resType = $('#results li[alt="' + $('#cms-preview').attr('alt') + '"]').data('type');
      
      // save changes via ajax if there are any
      if (changes['properties'].length != 0) {
         $.ajax({
            'url': cms.data.GALLERY_SERVER_URL,
            'data': {
               'action': 'setproperties',
               'data': JSON.stringify({
                  'path': $('#cms-preview').attr('alt'),
                  'properties': changes['properties']
               })
            },
            'type': 'POST',
            'dataType': 'json',
            'success': cms.galleries.getContentHandler(resType)['refreshPreview']
         });
         $('#cms-preview div.close-icon').addClass('cms-properties-changed');
      }
      
   }
   
   /**
    * Callback function for click event on the 'publish' button.
    */
   var publishChangedProperty = cms.galleries.publishChangedProperty = function() {
   
   }
   
   /**
    * Select button was pressed, stores the image information back in the editor fields.
    * 
    * @param {Object} itemId the unique path to the resource
    * @param {Object} fieldId the id of the input field in the xml content
    */
   var setResourcePath = function(/**String*/itemId, /**String*/fieldId) {
      if (fieldId != null && fieldId != "") {
         var imgField = window.opener.document.getElementById(fieldId);
         imgField.value = itemId;
         try {
            // toggle preview icon if possible
            window.opener.checkPreview(fieldid);
         } catch (e) {
                  }
      }
      window.close();
      
   }
   
   var closePreview = function() {
       $('#cms-preview').fadeOut('slow');
       cleanUpOnClose();       
   }
   
   var cleanUpOnClose = function() {
       $('#'+ cms.previewhandler.editableTabId).tabs('destroy');
       $('#'+ cms.previewhandler.editableTabId).removeAttr('class').empty();
       $('#cms-preview div.preview-area').empty();       
   }
   
   ///// Default Content Handler ////////////////              
   /**
    * Default handler to display the preview for a resource.
    * It can be used for all possible resource types.
    */
   var defaultContentTypeHandler = cms.previewhandler.defaultContentTypeHandler = {
      'type': 'default',
      'init': function() {
            },
      'openPreview': showItemPreview,
      'showEditArea': getEditArea,
      'fillProperties': fillProperties,
      'markChangedProperty': markChangedProperty,
      'saveChangedProperty': saveChangedProperty,
      'refreshPreview': refreshDefaultPreview,
      'setValues': {
         'widget': setResourcePath,
         'editor': 'test2'
      },
      'closePreview': closePreview
      
   };
      
   cms.galleries.contentTypeHandlers[cms.previewhandler.defaultContentTypeHandler['type']] = cms.previewhandler.defaultContentTypeHandler; 
   
})(cms);
