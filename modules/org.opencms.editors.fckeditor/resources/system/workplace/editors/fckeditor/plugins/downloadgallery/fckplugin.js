<%@ page import="org.opencms.jsp.*" %><%
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
%>

// register the related commands
FCKCommands.RegisterCommand(
	"OcmsDownloadGallery",
	new FCKDialogCommand(
		"OcmsDownloadGallery",
		"TODO_Title",
		downloadGalleryDialogUrl(),
        685,
		566
	)
);

// Searches for a frame by the specified name. Will only return siblings or ancestors.
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

// create the path to the image gallery dialog with some request parameters for the dialog
function downloadGalleryDialogUrl() {
	var resParam = "";
    	var editFrame=getFrame(self, 'edit');
	if (editFrame.editedResource != null) {
		resParam = "&resource=" + editFrame.editedResource;
		
	} else {
		resParam = "&resource=" + editFrame.editform.editedResource;
	}

	var searchParam = "";
	var jsonQueryData = "{'querydata':{'types':[2],'galleries':[],'categories':[],'matchesperpage':8,'query':'','tabid':'cms_tab_results','page':1},'types':[2]}";
	var jsonTabsConfig = "['cms_tab_galleries','cms_tab_categories','cms_tab_search']";
    searchParam += "&data=" + jsonQueryData;
    searchParam += "&tabs=" + jsonTabsConfig;    
	return "<%= cms.link("/system/workplace/editors/ade/galleries.jsp") %>?dialogmode=editor&integrator=fckeditor/plugins/downloadgallery/integrator.js" + searchParam + resParam;
}


// create the "OcmsDownloadGallery" toolbar button
// syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsDownloadGalleryItem = new FCKToolbarButton("OcmsDownloadGallery", "TODO_Title", "TODO_Tooltip", null, false, true);
opencmsDownloadGalleryItem.IconPath = FCKConfig.SkinPath + "toolbar/oc-downloadgallery.gif";

// "OcmsDownloadGallery" is the name that is used in the toolbar configuration
FCKToolbarItems.RegisterItem("OcmsDownloadGallery", opencmsDownloadGalleryItem);