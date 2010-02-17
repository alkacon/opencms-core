<%@ page import="org.opencms.workplace.galleries.*, org.opencms.workplace.*, java.util.*"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %>
<% 
	CmsImageFormatterHelper cms = new CmsImageFormatterHelper(pageContext, request, response);
	pageContext.setAttribute("cms", cms);

	CmsDialog dialog = new CmsDialog(pageContext, request, response);
	pageContext.setAttribute("locale", dialog.getLocale().toString());
%>
<div>
<style>
	.cms-image-preview {
		text-align: center;
	}
	
	/*h3 {
		font-size:14px;
		margin:3px 3px 6px;
		padding:0;
		position:absolute;		
	}*/
	
	.cms-image-info {
		margin-top:1px;
	}
		
	
	.cms-image-info .cms-field {
		margin:2px 0;		
	}
	
	.cms-image-info .cms-value {
		display:inline-block;
		height:16px;
		overflow:hidden;
		vertical-align:middle;			
	}
	
	.cms-width-300 {
		width:300px;
	}
	
	.cms-width-200 {
		width:200px;
	}
	
	.cms-width-180 {
		width:180px;
	}
	
	.cms-width-600 {
		width:600px;
	}
	
	.cms-width-500 {
		width:500px;
	}
	
	.cms-width-95 {
		width:95px;
	}
	
	.cms-width-120 {
		width: 120px;
	}
	
	.cms-width-50 {
		width:50px;
	}		
		 
</style>
<fmt:setLocale value="${locale}" />
<fmt:bundle basename="org.opencms.workplace.editors.ade.messagesADE">
	<%-- <h3>${cms.title}</h3> --%>
	<div class="cms-image-preview"></div>
	<div class="cms-image-info">
			<div class="cms-field cms-left cms-width-600"><span class="cms-item-title cms-width-95"><fmt:message key="GUI_IMAGEPREVIEW_FORMATTER_PATH_0" /></span><span class="cms-value cms-width-500">${cms.path}</span></div>			
			<div class="cms-field cms-left cms-width-300"><span class="cms-item-title cms-width-95"><fmt:message key="GUI_IMAGEPREVIEW_FORMATTER_NAME_0" /></span><span class="cms-value cms-width-200">${cms.title}</span></div>
			<div class="cms-field cms-right cms-width-300"><span class="cms-item-title cms-width-120"><fmt:message key="GUI_IMAGEPREVIEW_FORMATTER_FORMAT_0" /></span><span class="cms-value cms-width-180">${cms.format}</span></div>
			<div class="cms-field cms-left cms-width-300"><span class="cms-item-title cms-width-95"><fmt:message key="GUI_IMAGEPREVIEW_FORMATTER_TITLE_0" /></span><span class="cms-value cms-width-200">${cms.propertyTitle}</span></div>
			<c:set var="dateModified" value="<%=new Date(cms.getResource().getDateLastModified()) %>" />
			<div class="cms-field cms-right cms-width-300"><span class="cms-item-title cms-width-120"><fmt:message key="GUI_IMAGEPREVIEW_FORMATTER_LAST_MODIFIED_0" /></span><span class="cms-value cms-width-180"><fmt:formatDate value="${dateModified}" dateStyle="MEDIUM" type="both" /></span></div>
			<div class="cms-field cms-left cms-width-300"><span class="cms-item-title cms-width-95"><fmt:message key="GUI_IMAGEPREVIEW_FORMATTER_TYPE_0" /></span><span class="cms-value cms-width-200">${cms.ending}</span></div>						
			<div class="cms-field cms-right cms-width-300"><span class="cms-item-title cms-width-120"><fmt:message key="GUI_IMAGEPREVIEW_FORMATTER_SIZE_0" /></span><span class="cms-value cms-width-50">${cms.size}</span></div>
    </div>	
	<input type="hidden" value='${cms.jsonForActiveImage}'>
</fmt:bundle>
</div>