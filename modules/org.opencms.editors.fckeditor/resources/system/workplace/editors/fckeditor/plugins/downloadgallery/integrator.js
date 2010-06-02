<%@ page import="org.opencms.jsp.*" %><%
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
%><%= cms.getContent("/system/workplace/resources/editors/fckeditor/editor/dialog/common/fck_dialog_common.js") %>

/* Initialize important FCKeditor variables from editor. */
var dialog		= window.parent;
var oEditor		= dialog.InnerDialogLoaded();
var FCK			= oEditor.FCK;
var FCKConfig		= oEditor.FCKConfig;
var FCKBrowserInfo	= oEditor.FCKBrowserInfo;

/**
 * Enables and shows the ok-button of the fck editor.<p>
 * 
 * Editor, download gallery
 * Old name: activeItemAdditionalActions
 */
function activeItemAdditionalActions() {
	// activate the "OK" button of the dialog
	window.parent.SetOkButton(true);
}

/**
 * Check if a link is selected.<p>
 * 
 * FCK API!
 * Use JQuery!
 * 
 */
// TODO: remove after moved to plugin.js
function prepareEditor() {
    if (hasSelectedText() == true) {
		var a = FCK.Selection.MoveToAncestorNode('A') ;
    	if (a) {
    		// link present
        	FCK.Selection.SelectNode(a);
        	//path to resource
        	cms.galleries.initValues['linkpath'] = a.getAttribute("_fcksavedurl");
        	// target attribute
		    cms.galleries.initValues['target'] = a.getAttribute("target");
	    }	
	}
}

/**
 *  The OK button was hit, called by editor button click event. 
 *  
 *  FCK API! Event callback
 *  JQuery!
 */
function Ok() {

	//TODO: this part should be checked in the preview tab
    // if changed properties are not saved yet         
    if ($('button[name="previewSave"]').hasClass('cms-properties-changed')) {
        //text, title, yesLabel, noLabel, callback
        cms.util.dialogConfirmCancel('Do you want to save changed properties?', 'Save', 'Yes', 'No', 'Cancel', saveProperties);       
    } else { // not properties changed, call editor close function               
        
    	link(cms.galleries.activeItem['linkpath'], cms.galleries.activeItem['title'], cms.galleries.activeItem['description']);
        
        // return true to close dialog
        return true;
    }    
}

//TODO: remove, properties are saved from gwt!!
/* Saves changed properties and triggers ok button.*/
function saveProperties(isConfirmed) {
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
                'async': true,
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
                'success': triggerOkEvent
             });      
                
          } else {
              triggerOkEvent();
          }
      } else {
          triggerOkEvent();
      }           
}

/**
 *  Triggers the ok- button of the fck editor.<p> 
 *  
 *  Editor mode, download gallery, image gallery.
 */
function triggerOkEvent() {
    $('button[name="previewSave"]').removeClass('cms-properties-changed');
    window.parent.Ok();
}

/**
 * Insert a new link or update the old after a resource is selected.<p>
 * @param uri
 * @param title
 * @param desc
 */
function link(uri, title, desc) {
		
	if (hasSelectedText() == true) {
		// text selected.
		setLink(uri, title);	
	} else {
		pasteLink(uri, title, desc);
	}
		
}

/**
 * Pastes a new link to the current position of the editor.<p>
 * 
 * @param uri the link
 * @param title to set as link text
 * @param desc to set as link title
 */
function pasteLink(uri, title, desc) {
	
	var result = "<a href=\"";
	result += uri;
	result += "\" title=\"";
	result += escapeBrackets(desc);
	result += "\" target=\"";
    result += $('#' + cms.previewhandler.keys['editorTarget']).find('.cms-selectbox').selectBox('getValue');
	result += "\">";
	result += escapeBrackets(title);
	result += "<\/a>";
	insertHtml(result);
}

/**
 * Checks if a text part has been selected by the user.<p>
 * 
 * FCK API
 */
function hasSelectedText() {
	var sel = dialog.Selection.GetSelection();
	var text = "";
	if (oEditor.FCKSelection.GetSelection().createRange){
		text = oEditor.FCKSelection.GetSelection().createRange().text;
	} else {
		text = oEditor.FCKSelection.GetSelection();
	}
	
	if ((sel.GetType() == 'Text' || sel.GetType() == 'Control') && text != '') {
		return true;
	}
	return false; 
	
}

/**
 * Collects link infos and update or set a new link. <p>
 * 
 * @param uri the link
 * @param title to set as title attribute
 */
// TODO: add 'target' param instead of reading from gallery
function setLink(uri, title) {
	
	var linkInformation = new Object();
	linkInformation["type"] = "link";
	linkInformation["href"] = uri;
    linkInformation["target"] = $('#' + cms.previewhandler.keys['editorTarget']).find('.cms-selectbox').selectBox('getValue');   
	linkInformation["style"] = "";
	linkInformation["class"] = "";
	linkInformation["title"] = title;
	createLink(linkInformation);		
}	

/* Gets the linkpath and target for selected link. */
function getSelectedLinkUri() {
	
}

/*TODO remove after improvement: function getSelectedLinkUri() {
	var loadItem = new ItemSitepath();
	var a = FCK.Selection.MoveToAncestorNode('A') ;
    	if (a) {
    		// link present
        	FCK.Selection.SelectNode(a);
        	//path to resource
        	loadItem.path = a.getAttribute("_fcksavedurl");
        	// target attribute
		loadItem.target = a.getAttribute("target");
	}
	return loadItem;
}*/

/**
 * Updates the selected link or set a new link around the selected text.<p>
 * 
 * Editor mode, download gallery.
 * Creates a named anchor or a link from the OpenCms link dialog.
 * Called by Ok()!
 * Old name: setLink()
 */
function createLink(linkInformation) {

    var a = FCK.Selection.MoveToAncestorNode('A') ;
    if (a) {
    	// link present, manipulate it
        FCK.Selection.SelectNode(a);
	    a = FCK.CreateLink(linkInformation["href"])[0];
		
    } else {
    	// new link, create it
        a = FCK.CreateLink(linkInformation["href"])[0];        
    }
    
    // set or remove target attribute
    if (linkInformation["target"] != "") {
		a.target = linkInformation["target"];
	} else {
		a.removeAttribute("target");
	}

    // set or remove title attribute
    if (linkInformation["title"] != null && linkInformation["title"] != "") {
    		a.title = linkInformation["title"];
    } else {
		a.removeAttribute("title");
    }
} 


/**
 * inserts the passed html fragment at the current cursor position.<p>
 * 
 * FCK API
 */
function insertHtml(htmlContent) {
	FCK.InsertHtml(htmlContent);
}


/**
 * Escape the brackets.<p>
 * @param s string
 * @return escaped string
 */
function escapeBrackets(s) {
	var searchResultStart = s.search(/\[.+/);
	var searchResultEnd = s.search(/.+\]/);
	var cut = (searchResultStart == 0 && searchResultEnd != -1 && s.charAt(s.length - 1) == ']');
	if (cut) {
		// cut off the first '['
		s = s.substring(1,s.length);
		// cut off the last ']'
		s = s.substring(0,s.length-1);
	}

	return s;
}