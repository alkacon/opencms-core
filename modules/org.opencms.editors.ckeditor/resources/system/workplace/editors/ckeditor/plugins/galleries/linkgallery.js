<%@ page import="org.opencms.editors.ckeditor.*,org.opencms.jsp.*,org.opencms.workplace.galleries.*" %><%

CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
A_CmsAjaxGallery wp = new CmsAjaxImageGallery(pageContext, request, response);
CmsCKEditor ck = new CmsCKEditor(cms);
String buttonPath = ck.getEditorResourceUri() + "skins/opencms/toolbar/";

%>CKEDITOR.plugins.add("linkgallery", {

	requires : ["iframedialog"],

	// uncomment the following line in case of editor problems
	//beforeInit : function( editor ) { alert( 'Link gallery for editor "' + editor.name + '" is to be initialized!' ); },

	init : function(editor) {		

		if (!CKEDITOR.dialog.exists("oc-linkgallery")) {
			CKEDITOR.dialog.addIframe("oc-linkgallery",
				"<%= wp.key(org.opencms.workplace.galleries.Messages.GUI_LINKGALLERY_EDITOR_TITLE_0) %>",
				linkGalleryDialogUrl(editor),
				680,
				630,
				function(){}
			);
		}

		editor.addCommand("oc-linkgallery", {
			exec : function(editor) {
				dialogEditorInstanceName = editor.name;
				editor.openDialog("oc-linkgallery");
			},
			canUndo : true
		});


		editor.ui.addButton("OcmsLinkGallery", {
			label : "<%= wp.key(org.opencms.workplace.galleries.Messages.GUI_LINKGALLERY_EDITOR_TOOLTIP_0) %>",
			command : "oc-linkgallery",
			modes : { wysiwyg : 1, source : 0 },
			icon: "<%= buttonPath + "oc-linkgallery.gif" %>"
		});

	}
});

// create the path to the link gallery dialog with some request parameters for the dialog
function linkGalleryDialogUrl(editor) {
	var resParam = "";
	if (top.edit.editedResource != null) {
		resParam = "&resource=" + top.edit.editedResource;
	} else {
		resParam = "&resource=" + top.edit.editform.editedResource;
	}
	
	return "<%= cms.link("/system/workplace/galleries/linkgallery/index.jsp") %>?dialogmode=editor&integrator=/system/workplace/editors/ckeditor/plugins/galleries/integrator_linkgallery.js" + resParam;
}