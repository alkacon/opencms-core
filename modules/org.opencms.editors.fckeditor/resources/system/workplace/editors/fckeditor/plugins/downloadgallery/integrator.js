<%@ page import="org.opencms.jsp.*" %><%
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
%><%= cms.getContent("/system/workplace/resources/editors/fckeditor/editor/dialog/common/fck_dialog_common.js") %>

/* Initialize important FCKeditor variables from editor. */
var dialog		= window.parent;
var oEditor		= dialog.InnerDialogLoaded();
var FCK			= oEditor.FCK;
var FCKConfig		= oEditor.FCKConfig;
var FCKBrowserInfo	= oEditor.FCKBrowserInfo;

/* Do additional stuff when active item is loaded. */
// TODO: isInititial flag
function activeItemAdditionalActions() {
	// activate the "OK" button of the dialog
	window.parent.SetOkButton(true);
}

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

/* The OK button was hit, called by editor button click event. */
function Ok() {

    // if changed properties are not saved yet         
    if ($('button[name="previewSave"]').hasClass('cms-properties-changed')) {
        //text, title, yesLabel, noLabel, callback
        cms.util.dialogConfirmCancel('Do you want to save changed properties?', 'Save', 'Yes', 'No', 'Cancel', saveProperties);       
    } else { // not properties changed, call editor close function               
        link(cms.galleries.activeItem['linkpath'], cms.galleries.activeItem['title'], cms.galleries.activeItem['description']);
        return true;
    }    
}

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

/* Trigger the ok- button of the editor. */
function triggerOkEvent() {
    $('button[name="previewSave"]').removeClass('cms-properties-changed');
    window.parent.Ok();
}

function link(uri, title, desc) {
		
	if (hasSelectedText() == true) {
		// text selected.
		setLink(uri, title);	
	} else {
		pasteLink(uri, title, desc);
	}
		
}

/* Pastes a link to the current position of the editor */
function pasteLink(uri, title, desc) {
	
	var result = "<a href=\"";
	result += uri;
	result += "\" title=\"";
	result += escapeBrackets(desc);
	result += "\" target=\"";
    result += $('#' + cms.previewhandler.keys['editorTarget']).find('.cms-selectbox').selectBox('getValue');
    // TODO read the value of the target select box
	/*if (modeType == "gallery") {
		result += $("#gallerylinktarget").val();
	} else {
		result += $("#categorylinktarget").val();
	}*/
	result += "\">";
	result += escapeBrackets(title);
	result += "<\/a>";
	insertHtml(result);
}

// checks if a text part has been selected by the user
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

function setLink(uri, title) {
	
	var linkInformation = new Object();
	linkInformation["type"] = "link";
	linkInformation["href"] = uri;
    linkInformation["target"] = $('#' + cms.previewhandler.keys['editorTarget']).find('.cms-selectbox').selectBox('getValue');
    //TODO: read the value from select box
	/*if (modeType == "gallery") {
		linkInformation["target"] = $("#gallerylinktarget").val();
	} else {
		linkInformation["target"] = $("#categorylinktarget").val();
	}*/
    
   
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

// creates a named anchor or a link from the OpenCms link dialog, called from popup window
function createLink(linkInformation) {

    var a = FCK.Selection.MoveToAncestorNode('A') ;
    if (a) {
    	// link present, manipulate it
        FCK.Selection.SelectNode(a);
        //a.href= linkInformation["href"];
	    a = FCK.CreateLink(linkInformation["href"])[0];
		
    } else {
    	// new link, create it
        a = FCK.CreateLink(linkInformation["href"])[0];
        
    }
    
    if (linkInformation["target"] != "") {
		a.target = linkInformation["target"];
	} else {
		a.removeAttribute("target");
	}

    if (linkInformation["title"] != null && linkInformation["title"] != "") {
    		a.title = linkInformation["title"];
    } else {
		a.removeAttribute("title");
    }
	
	//if (USE_LINKSTYLEINPUTS) {
	//	if (linkInformation["class"] != "") {
	//		a.setAttribute("class", linkInformation["class"]);
	//	} else {
	//		a.removeAttribute("class");
	//	}
	//	if (linkInformation["style"] != "") {
	//		a.style.cssText = linkInformation["style"];
	//	} else {
	//		a.removeAttribute("style");
	//	}

	//}
} 


// inserts the passed html fragment at the current cursor position
function insertHtml(htmlContent) {
	FCK.InsertHtml(htmlContent);
}



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