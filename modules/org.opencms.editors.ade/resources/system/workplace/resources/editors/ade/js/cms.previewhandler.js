(function(cms){
  
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
       var buttonBar = $('<div class="button-bar"></div>');
       if (itemProperties.length > 0) {
             buttonBar.append('<button id="previewSave" disabled="true" class="ui-state-default ui-corner-all">\
                                                    <span class="cms-galleries-button">Save</span>\
                               </button>');
                     /*.append('<button id="previewPublish" class="ui-state-default ui-corner-all">\
                                                    <span class="cms-galleries-button cms-galleries-icon-publish cms-icon-text">Publish</span>\
                                </button>');*/
       }
                                      
                                        
       var target = $('.edit-area').append(buttonBar);
       $('#previewSave').click(cms.galleries.getContentHandler()['saveChangedProperty']);
       // TODO: comment in for direct publish
       /* $('#publishSave').click(publishChangedProperty);*/
       
       if (cms.galleries.displaySelectButton()) {
           $('.edit-area').find('.button-bar').append('<button id="previewSelect" class="ui-state-default ui-corner-all">\
                                <span class="cms-galleries-button cms-galleries-icon-apply cms-icon-text">Select</span>\
                          </button>');
           $('#previewSelect').click(function() {
               var itemType = '';
               var itemId = $(this).closest('#cms-preview').attr('alt');              
               cms.galleries.getContentHandler(itemType)['setValues'][cms.galleries.initValues['dialogMode']](itemId, cms.galleries.initValues['fieldId']);          
           });
       }           
                 
       // generate editable form
       var form = $('<div class="edit-from"></div>');
       $.each(itemProperties, function() {
              $('<div style="margin: 2px;  clear: left;"></div>').attr('alt', this.name).appendTo(form)
                   .append('<span class="cms-item-title" style="margin-right: 2%; width: 19%;">' + this.name + '</span>')
                   .append('<span class="cms-item-edit" style="width: 79%;">' + (this.value ? this.value : '') + '</span>');                                 
           }); 
       $(target).append(form);      
       
       // bind direct input to the editable fields
       $('.cms-item-edit').directInput({
                     marginHack: true,
                     live: false,
                     setValue: cms.galleries.getContentHandler()['markChangedProperty'],
                     onChange: function(element, input){
                         $('#previewSave').removeAttr('disabled');
                     }
               });             
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
   var refreshItemPreview = function (itemData) {
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
           'properties': []};
       $.each(changedProperties, function () {           
           var property = {};
           property['name'] =  $(this).closest('div').attr('alt');      
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
   
   var selectPath = function(itemId, fieldId) {
       if ( fieldId != null && fieldId != "") {
		var imgField = window.opener.document.getElementById(fieldId);
		imgField.value = itemId;
		try {
			// toggle preview icon if possible
			window.opener.checkPreview(fieldid);
		} catch (e) {}
	}
	window.close();
              
   }

///// Default Content Handler ////////////////              
   /**
    * Default handler to display the preview for a resource. 
    * It can be used for all possible resource types. 
    */
   var defaultContentTypeHandler = cms.previewhandler.defaultContentTypeHandler = {
       'init': showItemPreview,
       'showEditArea': showEditArea,
       'markChangedProperty': markChangedProperty, 
       'saveChangedProperty': saveChangedProperty,
       'setValues': { 'widget': selectPath,
               'editor': 'test2'}    
   };
     
})(cms);