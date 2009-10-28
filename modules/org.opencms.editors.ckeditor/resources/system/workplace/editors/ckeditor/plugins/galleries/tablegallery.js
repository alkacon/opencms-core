<%@ page import="org.opencms.editors.ckeditor.*,org.opencms.jsp.*,org.opencms.workplace.galleries.*" %><%

CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
A_CmsAjaxGallery wp = new CmsAjaxTableGallery(pageContext, request, response);
CmsCKEditor ck = new CmsCKEditor(cms);
String buttonPath = ck.getEditorResourceUri() + "skins/opencms/toolbar/";

%>CKEDITOR.plugins.add("tablegallery", {

	requires : ["iframedialog"],

	// uncomment the following line in case of editor problems
	//beforeInit : function( editor ) { alert( 'Table gallery for editor "' + editor.name + '" is to be initialized!' ); },

	init : function(editor) {		

		if (!CKEDITOR.dialog.exists("oc-tablegallery")) {
			CKEDITOR.dialog.addIframe("oc-tablegallery",
				"<%= wp.key(org.opencms.workplace.galleries.Messages.GUI_TABLEGALLERY_EDITOR_TITLE_0) %>",
				tableGalleryDialogUrl(editor),
				680,
				630,
				function(){}
			);
		}

		editor.addCommand("oc-tablegallery", {
			exec : function(editor) {
				dialogEditorInstanceName = editor.name;
				editor.openDialog("oc-tablegallery");
			},
			canUndo : true
		});

		editor.ui.addButton("OcmsTableGallery", {
			label : "<%= wp.key(org.opencms.workplace.galleries.Messages.GUI_TABLEGALLERY_EDITOR_TOOLTIP_0) %>",
			command : "oc-tablegallery",
			modes : { wysiwyg : 1, source : 0 },
			icon: "<%= buttonPath + "oc-tablegallery.gif" %>"
		});

	}
});

// create the path to the table gallery dialog with some request parameters for the dialog
function tableGalleryDialogUrl(editor) {
	var resParam = "";
	if (top.edit.editedResource != null) {
		resParam = "&resource=" + top.edit.editedResource;
	} else {
		resParam = "&resource=" + top.edit.editform.editedResource;
	}
	return "<%= cms.link("/system/workplace/galleries/tablegallery/index.jsp") %>?dialogmode=editor&integrator=/system/workplace/editors/ckeditor/plugins/galleries/integrator_tablegallery.js" + resParam;
}