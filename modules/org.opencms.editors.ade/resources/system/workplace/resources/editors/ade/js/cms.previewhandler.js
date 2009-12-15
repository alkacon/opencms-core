(function(cms) {

   ///// Content Handler Function definition //////////////////////////    
   
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
      
      if (cms.galleries.hasContentHandler('default')) {
          // add button bar to the edit area
          var switchBar = $('<div class="button-bar cms-top-bottom"></div>');        
             switchBar.append('<button name="switchToProperties" class="cms-right ui-state-default ui-corner-all">\
                                         <span class="cms-galleries-button">Image&nbsp;Format</span>\
                                   </button>')
                      .append('<span class="cms-title">File Properties:</span>');
             /*.append('<button name="previewPublish" class="ui-state-default ui-corner-all">\                 
              <span class="cms-galleries-button cms-galleries-icon-publish cms-icon-text">Publish</span>\
              </button>');*/                                            
          target.append(switchBar);
      }
            
      // generate editable form
      var form = $('<div class="edit-form cms-scrolling-properties"></div>');
      for (var i = 0; i < itemProperties.length; i++) {
          if ( i%2  == 0) {
              $('<div class="cms-editable-field cms-left"></div>').attr('alt', itemProperties[i]['name'])
                 .appendTo(form)
                 .append('<span class="cms-item-title cms-width-80">' + itemProperties[i]['name'] + '</span>')
                 .append('<span class="cms-item-edit">' + (itemProperties[i]['value'] ? itemProperties[i]['value'] : '') + '</span>');    
          } else {
              $('<div class="cms-editable-field cms-right"></div>').attr('alt', itemProperties[i]['name'])
                 .appendTo(form)
                 .append('<span class="cms-item-title cms-width-80">' + itemProperties[i]['name'] + '</span>')
                 .append('<span class="cms-item-edit">' + (itemProperties[i]['value'] ? itemProperties[i]['value'] : '') + '</span>');
          }
          
      }
      
      
      /*$.each(itemProperties, function() {
         $('<div class="cms-editable-field"></div>').attr('alt', this.name)
         .appendTo(form)
         .append('<span class="cms-item-title">' + this.name + '</span>')
         .append('<span class="cms-item-edit">' + (this.value ? this.value : '') + '</span>');
      });*/
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
      
      // add button bar to the editet area
      var buttonBar = $('<div class="button-bar cms-top"></div>');      
      var closeButton = $('<button name="previewSave" disabled="true" class="cms-right ui-state-default ui-corner-all">\
                                 <span class="cms-galleries-button">Save</span>\
                           </button>')
                               .appendTo(buttonBar)
                               .after('<button name="previewClose" class="cms-right ui-state-default ui-corner-all">\
                                            <span class="cms-galleries-button">Close</span>\
                                       </button>');
        /*.append('<button name="previewPublish" class="ui-state-default ui-corner-all">\                 
          <span class="cms-galleries-button cms-galleries-icon-publish cms-icon-text">Publish</span>\
          </button>');*/                        
      target.append(buttonBar);
      $('.edit-area').find('button[name="previewSave"]').click(cms.galleries.getContentHandler()['saveChangedProperty']);
      // TODO: comment in for direct publish
      /* $('.edit-area button[name="publishSave"]').click(publishChangedProperty);*/
      
      if (cms.galleries.isSelectableItem()) {
         closeButton.before('<button name="previewSelect" class="cms-right ui-state-default ui-corner-all">\
                                <span class="cms-galleries-button cms-galleries-icon-apply cms-icon-text">Select</span>\
                          </button>');
         $('.edit-area').find('button[name="previewSelect"]').click(function() {
            var itemType = '';
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
   var refreshItemPreview = function(itemData) {
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
            'success': refreshItemPreview
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
      'setValues': {
         'widget': setResourcePath,
         'editor': 'test2'
      }
   };
      
   cms.galleries.contentTypeHandlers[cms.previewhandler.defaultContentTypeHandler['type']] = cms.previewhandler.defaultContentTypeHandler; 
   
})(cms);
