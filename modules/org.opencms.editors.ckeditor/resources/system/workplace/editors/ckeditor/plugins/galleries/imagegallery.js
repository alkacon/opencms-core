<%@ page import="org.opencms.editors.ckeditor.*,org.opencms.jsp.*,org.opencms.workplace.galleries.*" %><%

CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
A_CmsAjaxGallery wp = new CmsAjaxImageGallery(pageContext, request, response);
CmsCKEditor ck = new CmsCKEditor(cms);
String buttonPath = ck.getEditorResourceUri() + "skins/opencms/toolbar/";

%>CKEDITOR.plugins.add("imagegallery", {

	requires : ["iframedialog"],

	// uncomment the following line in case of editor problems
	//beforeInit : function( editor ) { alert( 'Image gallery for editor "' + editor.name + '" is to be initialized!' ); },

	init : function(editor) {		

		if (!CKEDITOR.dialog.exists("oc-imagegallery")) {
			CKEDITOR.dialog.addIframe("oc-imagegallery",
				"<%= wp.key(org.opencms.workplace.galleries.Messages.GUI_IMAGEGALLERY_EDITOR_TITLE_0) %>",
				imageGalleryDialogUrl(editor),
				680,
				630,
				function(){}
			);
		}

		editor.addCommand("oc-imagegallery", {
			exec : function(editor) {
				dialogEditorInstanceName = editor.name;
				editor.openDialog("oc-imagegallery");
			},
			canUndo : true
		});

		editor.ui.addButton("OcmsImageGallery", {
			label : "<%= wp.key(org.opencms.workplace.galleries.Messages.GUI_IMAGEGALLERY_EDITOR_TOOLTIP_0) %>",
			command : "oc-imagegallery",
			modes : { wysiwyg : 1, source : 0 },
			icon: "<%= buttonPath + "oc-imagegallery.gif" %>"
		});

		// if the "menu" plugin is loaded, register the menu items
		if (editor.addMenuItems) {
			editor.addMenuItems({
				image : {
					label : editor.lang.image.menu,
					command : "oc-imagegallery",
					// important: replace default "image" menu group in config by "imageocms" to hide standard image entry
					group : "imageocms"
				}
			});
		};

	}
});

// create the path to the image gallery dialog with some request parameters for the dialog
function imageGalleryDialogUrl(editor) {
	var resParam = "";
	var editFrame = getFrame(self, "edit");
	if (editFrame.editedResource != null) {
		resParam = "&resource=" + editFrame.editedResource;
	} else {
		resParam = "&resource=" + editFrame.editform.editedResource;
	}
	return "<%= cms.link("/system/workplace/galleries/imagegallery/index.jsp") %>?dialogmode=editor&integrator=/system/workplace/editors/ckeditor/plugins/galleries/integrator_imagegallery.js" + resParam;
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