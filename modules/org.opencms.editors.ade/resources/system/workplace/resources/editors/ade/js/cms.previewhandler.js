(function(cms) {

   ///// Content Handler Function definition //////////////////////////    
   
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
   cms.previewhandler.htmlPreviewSceleton = $('<div id="cms-preview" class="ui-corner-all">\
                                <div class="close-icon ui-icon ui-icon-closethick ui-corner-all" ></div>\
            	                <div class="preview-area ui-widget-content ui-corner-all"></div>\
				                <div class="edit-area ui-widget-content ui-corner-all">\
                                </div>\
                            </div>');                  
   
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
      
      //bind click event to preview close button 
      $('#cms-preview div.close-icon').click(function() {
         if ($(this).hasClass('cms-properties-changed')) {
            cms.galleries.loadSearchResults();
            $(this).removeClass('cms-properties-changed');
         }
         $(this).parent().fadeOut('slow');
         
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
    * Generates html for the editable properties and add to the dom.
    *
    * @param {Object} itemProperties server response as JSON object with the editable properties
    */
   var showEditArea = function(itemProperties) {
      var target = $('.edit-area');
      
      // add the upper button bar      
      target.append($(switchToFormatBar));
      $('button[name="switchToFormat"]').hide();
                                                                                                                 
      // generate editable form fields
      var form = $('<div class="edit-form cms-scrolling-properties"></div>');
      for (var i = 0; i < itemProperties.length; i++) {
          if ( i%2  == 0) {
              $('<div class="cms-editable-field cms-left"></div>').attr('alt', itemProperties[i]['name'])
                 .appendTo(form)
                 .append('<span class="cms-item-title cms-width-80">' + itemProperties[i]['name'] + '</span>')
                 .append('<span class="cms-item-edit">' + (itemProperties[i]['value'] ? itemProperties[i]['value'] : '&nbsp;') + '</span>');    
          } else {
              $('<div class="cms-editable-field cms-right"></div>').attr('alt', itemProperties[i]['name'])
                 .appendTo(form)
                 .append('<span class="cms-item-title cms-width-80">' + itemProperties[i]['name'] + '</span>')
                 .append('<span class="cms-item-edit">' + (itemProperties[i]['value'] ? itemProperties[i]['value'] : '&nbsp;') + '</span>');
          }
          
      }      
      $(target).append(form);
      
      // bind direct input to the editable fields
      $('.cms-item-edit').directInput({
         marginHack: true,
         live: false,
         setValue: cms.galleries.getContentHandler()['markChangedProperty'],
         onChange: function(element, input) {
            $('.edit-area').find('button[name="previewSave"]').removeAttr('disabled');
         }
      });    
         
      // add ok-close button bar to the edit area                        
      target.append($(okCloseButtonBar));
      target.find('button[name="previewSave"]')
          .click(cms.galleries.getContentHandler()['saveChangedProperty']);
      target.find('button[name="previewClose"]')
          .click(function() {
             $('#cms-preview').fadeOut('slow');
         });
      // TODO: comment in for direct publish
      /* $('.edit-area button[name="publishSave"]').click(publishChangedProperty);*/
      
      // add select button if in widget or editor mode
      if (cms.galleries.isSelectableItem()) {
         target.find('button[name="previewClose"]').after('<button name="previewSelect" class="cms-right ui-state-default ui-corner-all">\
                                <span class="cms-galleries-button cms-galleries-icon-apply cms-icon-text">Select</span>\
                          </button>');
         $('.edit-area').find('button[name="previewSelect"]').click(function() {            
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
      $('#cms-preview div.preview-area, #cms-preview div.edit-area').empty();
      //display the html preview 
      $('.preview-area').append(itemData['previewdata']['itemhtml']);
      showEditArea(itemData['previewdata']['properties']);
      
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
         property['value'] = $(this).text();
         changes['properties'].push(property);
      });
      
      var resType = $('#results li[alt="' + $('#cms-preview').attr('alt') + '"]').data('type');
      
      // save changes via ajax if there are any
      if (changes['properties'].length != 0) {
         $.ajax({
            'url': vfsPathAjaxJsp,
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
      'showEditArea': showEditArea,
      'markChangedProperty': markChangedProperty,
      'saveChangedProperty': saveChangedProperty,
      'refreshPreview': refreshDefaultPreview,
      'setValues': {
         'widget': setResourcePath,
         'editor': 'test2'
      }
   };
      
   cms.galleries.contentTypeHandlers[cms.previewhandler.defaultContentTypeHandler['type']] = cms.previewhandler.defaultContentTypeHandler; 
   
})(cms);
