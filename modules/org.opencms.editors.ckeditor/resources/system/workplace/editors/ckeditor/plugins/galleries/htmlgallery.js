<%@ page import="org.opencms.editors.ckeditor.*,org.opencms.jsp.*,org.opencms.workplace.galleries.*" %><%

CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
A_CmsAjaxGallery wp = new CmsAjaxImageGallery(pageContext, request, response);
CmsCKEditor ck = new CmsCKEditor(cms);
String buttonPath = ck.getEditorResourceUri() + "skins/opencms/toolbar/";

%>CKEDITOR.plugins.add("htmlgallery", {

	requires : ["iframedialog"],

	// uncomment the following line in case of editor problems
	//beforeInit : function( editor ) { alert( 'HTML gallery for editor "' + editor.name + '" is to be initialized!' ); },

	init : function(editor) {		

		if (!CKEDITOR.dialog.exists("oc-htmlgallery")) {
			CKEDITOR.dialog.addIframe("oc-htmlgallery",
				"<%= wp.key(org.opencms.workplace.galleries.Messages.GUI_HTMLGALLERY_EDITOR_TITLE_0) %>",
				htmlGalleryDialogUrl(editor),
				680,
				630,
				function(){}
			);
		}

		editor.addCommand("oc-htmlgallery", {
			exec : function(editor) {
				dialogEditorInstanceName = editor.name;
				editor.openDialog("oc-htmlgallery");
			},
			canUndo : true
		});

		editor.ui.addButton("OcmsHtmlGallery", {
			label : "<%= wp.key(org.opencms.workplace.galleries.Messages.GUI_HTMLGALLERY_EDITOR_TOOLTIP_0) %>",
			command : "oc-htmlgallery",
			modes : { wysiwyg : 1, source : 0 },
			icon: "<%= buttonPath + "oc-htmlgallery.gif" %>"
		});

	}
});

// create the path to the HTML gallery dialog with some request parameters for the dialog
function htmlGalleryDialogUrl(editor) {
	var resParam = "";
	var editFrame = getFrame(self, "edit");
	if (editFrame.editedResource != null) {
		resParam = "&resource=" + editFrame.editedResource;
	} else {
		resParam = "&resource=" + editFrame.editform.editedResource;
	}	
	return "<%= cms.link("/system/workplace/galleries/htmlgallery/index.jsp") %>?dialogmode=editor&integrator=/system/workplace/editors/ckeditor/plugins/galleries/integrator_htmlgallery.js" + resParam;
}

// searches for a frame by the specified name. Will only return siblings or ancestors.
function getFrame(startFrame, frameName){
    if (startFrame == top){
        if (startFrame.name == frameName){
            return startFrame;
        }
        return null;
    }
    for (var i=0; i<startFrame.parent.frames.length; i++){
        if (startFrame.parent.frames[i].name == frameName) {
            return startFrame.parent.frames[i];
        }
    }
    return getFrame(startFrame.parent, frameName);
}