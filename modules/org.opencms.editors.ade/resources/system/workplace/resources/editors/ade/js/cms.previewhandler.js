(function(cms) {

   ///// Content Handler Function definition //////////////////////////    
   
   var editableTabId = cms.previewhandler.editableTabId = 'cms-editable-tabs';
   
   var keys = cms.previewhandler.keys = {
       'propertiesTabId' : 'tabs-edit-area',
       'title': 'Title',
       'description': 'Description',
       'editorTarget': 'editorTarget'       
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
                                 <button name="previewSave" disabled="" class="cms-right ui-state-default ui-corner-all">\
                                     <span class="cms-galleries-button">Save</span>\
                                 </button>\
                           </div>';                             
   //<button name="previewClose" class="cms-right ui-state-default ui-corner-all">\
                                 //      <span class="cms-galleries-button">Close</span>\
                                 //</button>\
   /** html fragment for the item preview. */
   cms.previewhandler.htmlPreviewSceleton = '<div id="cms-preview" class="ui-corner-all">\
                                                    <div class="close-icon ui-icon ui-icon-closethick ui-corner-all" ></div>\
                                	                <div class="preview-area ui-widget-content ui-corner-all"></div>\
                                                    <div id="' + cms.previewhandler.editableTabId + '"></div>\
                                                </div>';
   
   /** Array with format values for the drop down. */
   cms.previewhandler.editorTargetDropDown = [
       {   'value':'',
           'title':'Not set' 
       },
       {   'value':'_blank',
           'title':'New Window (_blank)'                
       },
       {    'value':'_top',
            'title':'Topmost Window (_top)'           
       },        
       {    'value':'_self',
            'title':'Same Window (_self)'           
       },
       {    'value':'_parent',
            'title':'Parent Window (_parent)'           
       }       
   ];                                                
                                                
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
      cms.galleries.getContentHandler()['showEditArea'](itemData['previewdata']['properties'], true);
      cms.galleries.activeItem['linkpath'] =  itemData['previewdata']['linkpath'];
            
      if (cms.galleries.isEditorMode()) {                 
          // do additional action for editor
          activeItemAdditionalActions();
      }
      
      $('#' + cms.previewhandler.editableTabId).prepend(tabsNav);      
      // bind the select tab event, fill the content of the result tab on selection
      $('#' + cms.previewhandler.editableTabId).tabs({});                    
                  
      //bind click event to preview close button 
      $('#cms-preview div.close-icon').click(cms.galleries.getContentHandler()['closePreviewWithConfirmation']);             
      
      $('.button-bar button').live('mouseover', function() {
         $(this).toggleClass('ui-state-hover', true);
      }).live('mouseout', function() {
         $(this).toggleClass('ui-state-hover', false);
      });
      
      // fade in the preview      
      $('#cms-preview').fadeIn('slow');
      
      // change the initial flag 
      if (cms.galleries.activeItem['isInitial'] == true) {
         cms.galleries.activeItem['isInitial'] = false;         
      }
   }      
   
   /**
    * Generates html for the editable properties and add them to the dom.
    * 
    * @param {Object} itemProperties server response as JSON object with the editable properties
    * @param {Object} showTargetSelectBox true if the target selectbox should be shown in the edit properties tab for this resource (nessecary for editor mode)
    */
   var getEditArea = function(/**Json*/itemProperties, /**Boolean*/ showTargetSelectBox) {           
      $('<div id="' + cms.previewhandler.keys['propertiesTabId'] + '"></div>').appendTo('#' + cms.previewhandler.editableTabId);                                  

      var target = $('#' + cms.previewhandler.keys['propertiesTabId']);                               
      // add ok-close button bar to the edit area                        
      target.append($(okCloseButtonBar));
      $('#' + cms.previewhandler.keys['propertiesTabId']).find('button[name="previewSave"]')
          .click(cms.galleries.getContentHandler()['saveAndRefreshProperties']);
      /*target.find('button[name="previewClose"]')
          .click(cms.galleries.getContentHandler()['closePreview']);*/
      // TODO: comment in for direct publish
      /* $('.edit-area button[name="publishSave"]').click(publishChangedProperty);*/
      
      // add the select box in editor for default preview
      if (cms.galleries.isEditorMode() && showTargetSelectBox) {
          target.find('button[name="previewSave"]').before('<div class="cms-drop-down cms-format-line cms-left" id="' + cms.previewhandler.keys['editorTarget'] + '">\
                         <label class="cms-item-title cms-width-85">Target:</label>\
                     </div>');
          target.find('.cms-drop-down label').after($.fn.selectBox('generate', {
            values: cms.previewhandler.editorTargetDropDown,
            width: 204,
            selectorPosition: 'top',
            appendTo: '#' + cms.previewhandler.keys['propertiesTabId']           
         }));
         if (cms.galleries.initValues['target'] != null) {
             $('#' + cms.previewhandler.keys['editorTarget']).find('.cms-selectbox').selectBox('setValue', cms.galleries.initValues['target']);    
         }                                                
      }
      
      // add select button if in widget or editor mode
      if (cms.galleries.isSelectableItem()) {
         target.find('button[name="previewSave"]').before('<button name="previewSelect" class="cms-right ui-state-default ui-corner-all">\
                                <span class="cms-galleries-button cms-galleries-icon-apply cms-icon-text">Select</span>\
                          </button>');
         target.find('button[name="previewSelect"]').click(cms.galleries.getContentHandler()['selectItemWithConfirmation']);        
      }
      
      //add properties list
      cms.galleries.getContentHandler()['fillProperties'](itemProperties);                                                                                                                          
   }
   
   /**
    * Generate html for the given properties.
    * 
    * @param {Object} itemProperties
    */
   var fillProperties = function(/**Json array*/itemProperties) {
      var target = $('#' + cms.previewhandler.keys['propertiesTabId']);
      // generate editable form fields
      var form = $('<div class="edit-form cms-scrolling-properties"></div>');
      for (var i = 0; i < itemProperties.length; i++) {
          $('<div class="cms-editable-field '+( i%2  == 0 ? 'cms-left' : 'cms-right')+'"></div>').attr('alt', itemProperties[i]['name'])
             .appendTo(form)
             .append('<span class="cms-item-title cms-width-90">' + itemProperties[i]['name'] + '</span>')
             .append('<input class="cms-item-edit ui-corner-all" name="' + itemProperties[i]['name'] + '" title="Edit ' + itemProperties[i]['name'] + '" value="' + (itemProperties[i]['value'] ? itemProperties[i]['value'] : '') + '" />');
          // set the the activeItem properties 'title' and 'description'
          if (itemProperties[i]['name'] == cms.previewhandler.keys['title'] ) {
                cms.galleries.activeItem['title'] = itemProperties[i]['value'] ? itemProperties[i]['value'] : ''; 
          }else if (itemProperties[i]['name'] == cms.previewhandler.keys['description']) {
                cms.galleries.activeItem['description'] = itemProperties[i]['value'] ? itemProperties[i]['value'] : ''; 
          }    
      }      
      target.prepend(form);
      
      // bind direct input to the editable fields
      target.find('.cms-item-edit').change(function() {
           $('#' + cms.previewhandler.keys['propertiesTabId'])
               .find('button[name="previewSave"]')
               .removeAttr('disabled')
               .addClass('cms-properties-changed').removeClass('cms-properties-saved');
           $(this).addClass('cms-item-changed');
           $('#cms-preview div.close-icon').addClass('cms-properties-changed').removeClass('cms-properties-saved');
           $('#cms-preview').find('button[name="previewSelect"]').addClass('cms-properties-changed').removeClass('cms-properties-saved');
      });           
   }
   
   /**
    * Refresh the preview after changes.
    *
    * @param {Object} itemData the data to update the preview
    */
   var refreshDefaultPreview = function(itemData) {          
      
      $('#cms-preview div.close-icon').removeClass('cms-properties-changed').addClass('cms-properties-saved');
      $('#' + cms.previewhandler.keys['propertiesTabId'])
          .find('button[name="previewSave"]').attr("disabled", true)
          .removeClass('cms-properties-changed')
          .addClass('cms-properties-saved'); 
      $('#' + cms.previewhandler.keys['propertiesTabId'])
          .find('button[name="previewSave"]').click(cms.galleries.getContentHandler()['saveAndRefreshProperties']);
      $('#cms-preview').find('button[name="previewSelect"]').addClass('cms-properties-saved').removeClass('cms-properties-changed');           
      $('#' + cms.previewhandler.keys['propertiesTabId']).find('.edit-form').remove();                        
      cms.galleries.getContentHandler()['fillProperties'](itemData['previewdata']['properties']);
      
      // refresh html preview area
      $('#cms-preview div.preview-area').empty();
      $('#cms-preview div.preview-area').append(itemData['previewdata']['itemhtml']);
      
   }
   
   /**
    * Callback function for click event on the 'save' button.
    */
   var saveAndRefreshProperties = function() {       
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
      }     
   }
   
   /**
    * Callback function for the closing preview dialog with confirmation.
    * 
    * @param {Object} isConfirmed true if yes-button is clicked, false otherwise
    */
   var saveAndClosePreview = function(/***Boolean*/isConfirmed) {
      if (isConfirmed) {
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
                // TODO: error handling
                'success': function () {
                    cms.galleries.loadSearchResults();
                    cms.galleries.getContentHandler()['closePreview']();   
                }
             });         
          } else {
              
          }
      } else {
          cms.galleries.getContentHandler()['closePreview']();
      }            
   }
   
    /**
    * Callback function for the closing preview dialog with confirmation.
    * 
    * @param {Object} isConfirmed true if yes-button is clicked, false otherwise
    */
   var saveAndSelectItem = function(/***Boolean*/isConfirmed) {
      var resType = $('#results li[alt="' + $('#cms-preview').attr('alt') + '"]').data('type');
      if (isConfirmed) {
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
                // TODO: error handling
                'success': cms.galleries.getContentHandler(resType)['setValues'][cms.galleries.initValues['dialogMode']]
            });         
          } else {
              cms.galleries.getContentHandler(resType)['setValues'][cms.galleries.initValues['dialogMode']]();
          }
      } else {
          cms.galleries.getContentHandler(resType)['setValues'][cms.galleries.initValues['dialogMode']]();
      }            
   }
   
   /**
    * Callback function for click event on the 'publish' button.
    */
   var publishChangedProperty = cms.galleries.publishChangedProperty = function() {
   
   }
   
   /**
    * Select button was pressed, stores the item path back in the editor field.    
    */
   var setValues = function() {
      // the unique path to the resource
      var itemId = $('#cms-preview').attr('alt');
      setResourcePath(itemId);
      
      //the id of the input field in the xml content
      /*var fieldId = cms.galleries.initValues['fieldId'];
      
      if (fieldId != null && fieldId != "") {
         var imgField = window.opener.document.getElementById(fieldId);
         imgField.value = itemId;
         try {
            // toggle preview icon if possible
            window.opener.checkPreview(fieldid);
         } catch (e) {
                  }
      }
      window.close();*/      
   }
   
   /**
    * Select button was pressed, stores the item path back in the editor field.    
    */
   var setResourcePath = function(itemId) {      
      //the id of the input field in the xml content
      var fieldId = cms.galleries.initValues['fieldId'];
      
      if (fieldId != null && fieldId != "") {
         var imgField = window.parent.document.getElementById(fieldId);
         imgField.value = itemId;
         try {
            // toggle preview icon if possible
            window.parent.checkPreview(fieldid);
         } catch (e) {
                  }
      }
      window.parent.closeGallery(window.parent, fieldId);
   }
   
   /**
    * Closes the preview with confirmation to save changed properties.
    */
   var closePreviewWithConfirmation = function () {      
         if ($(this).hasClass('cms-properties-saved')) {
            cms.galleries.loadSearchResults();
            cms.galleries.getContentHandler()['closePreview']();
         } else if ($(this).hasClass('cms-properties-changed')) {            
            //text, title, yesLabel, noLabel, callback
            cms.util.dialogConfirmCancel('Do you want to save changed properties?', 'Save', 'Yes', 'No', 'Cancel', saveAndClosePreview);
         } else {
             cms.galleries.getContentHandler()['closePreview']();     
         }                   
   }
   
   /**
    * Pastes the path to the selected item with confirmation to save changed properties.
    */
   var selectItemWithConfirmation = function () { 
         var resType = $('#results li[alt="' + $('#cms-preview').attr('alt') + '"]').data('type');       
         if ($(this).hasClass('cms-properties-changed')) {            
            //text, title, yesLabel, noLabel, callback
            cms.util.dialogConfirmCancel('Do you want to save changed properties?', 'Save', 'Yes', 'No', 'Cancel', cms.galleries.getContentHandler()['saveAndSelectItem']);
         } else {
            cms.galleries.getContentHandler(resType)['setValues'][cms.galleries.initValues['dialogMode']]();  
         }                   
   }
   
   var closePreview = function() {
       $('#cms-preview').fadeOut('slow');
       cleanUpOnClose();       
   }
   
   var cleanUpOnClose = function() {
       $('#'+ cms.previewhandler.editableTabId).tabs('destroy');
       $('#'+ cms.previewhandler.editableTabId).removeAttr('class').empty();
       $('#cms-preview div.close-icon').removeClass('cms-properties-changed cms-properties-saved'); 
       
       //$('#' + cms.previewhandler.keys['propertiesTabId']).find('button[name="previewSelect"]').removeClass('cms-properties-changed');      
       $('#cms-preview div.preview-area').empty();       
   }
   
   ///// Default Content Handler ////////////////              
   /**
    * Default handler to display the preview for a resource.
    * It can be used for all possible resource types.
    */
   cms.previewhandler.defaultContentTypeHandler = {
      'type': 'default',
      'init': function() {
            },
      'openPreview': showItemPreview,
      'showEditArea': getEditArea,
      'fillProperties': fillProperties,
      'saveAndRefreshProperties': saveAndRefreshProperties,
      'selectItemWithConfirmation':selectItemWithConfirmation,
      'saveAndSelectItem':saveAndSelectItem,
      'refreshPreview': refreshDefaultPreview,
      'setValues': {
         'widget': setValues,
         'editor': 'test2',
         'property': cms.property.setGalleryValues
      },
      'setValuesFromList': {
         'widget': setResourcePath,
         'editor': 'test2',
         'property': cms.property.setGalleryResourcePath
      },
      'closePreviewWithConfirmation': closePreviewWithConfirmation,
      'closePreview': closePreview
      
   };
      
   cms.galleries.contentTypeHandlers[cms.previewhandler.defaultContentTypeHandler['type']] = cms.previewhandler.defaultContentTypeHandler; 
   
})(cms);
