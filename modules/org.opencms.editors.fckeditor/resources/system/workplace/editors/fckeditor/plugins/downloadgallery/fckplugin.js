<%@ page import="org.opencms.jsp.*,
                 org.opencms.workplace.*,                 
				 org.opencms.main.*,
				 org.opencms.file.types.CmsResourceTypeBinary" %><%
%><%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %><% 
    CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);	
	pageContext.setAttribute("cms", cms);
	CmsDialog dialog = new CmsDialog(pageContext, request, response);
	pageContext.setAttribute("locale", dialog.getLocale().toString());
    int itemResType = OpenCms.getResourceManager().getResourceType(CmsResourceTypeBinary.getStaticTypeName()).getTypeId();
%>

<fmt:setLocale value="${locale}" />
<fmt:bundle basename="org.opencms.workplace.editors.ade.messagesADE">

// register the related commands
FCKCommands.RegisterCommand(
	"OcmsDownloadGallery",
	new FCKDialogCommand(
		"OcmsDownloadGallery",
		"<fmt:message key="GUI_EDITOR_TITLE_DOWNLOADGALLERY_0" />",
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
	var jsonQueryData = "{'querydata':{'types':[" + <%=itemResType %> + "],'galleries':[],'categories':[],'matchesperpage':12,'query':'','tabid':'cms_tab_results','page':1},'types':[" + <%=itemResType %> + "]}";
	var jsonTabsConfig = "['cms_tab_galleries','cms_tab_categories','cms_tab_search']";
    searchParam += "&data=" + jsonQueryData;
    searchParam += "&tabs=" + jsonTabsConfig;    
	return "<%= cms.link("/system/workplace/editors/ade/galleries.jsp") %>?dialogmode=editor&integrator=fckeditor/plugins/downloadgallery/integrator.js" + searchParam + resParam;
}


// create the "OcmsDownloadGallery" toolbar button
// syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsDownloadGalleryItem = new FCKToolbarButton("OcmsDownloadGallery", "<fmt:message key="GUI_EDITOR_TITLE_DOWNLOADGALLERY_0" />", "<fmt:message key="GUI_EDITOR_TOOLTIP_DOWNLOADGALLERY_0" />", null, false, true);
opencmsDownloadGalleryItem.IconPath = FCKConfig.SkinPath + "toolbar/oc-downloadgallery.gif";

// "OcmsDownloadGallery" is the name that is used in the toolbar configuration
FCKToolbarItems.RegisterItem("OcmsDownloadGallery", opencmsDownloadGalleryItem);

</fmt:bundle>