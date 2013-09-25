<%@page taglibs="cms" %><%= new org.opencms.ade.containerpage.CmsContainerpageActionElement(pageContext, request, response).exportAll() %>
<script type="text/javascript" src="<cms:link>/system/workplace/editors/tinymce/opencms_plugin.js</cms:link>"></script>
<style type="text/css">
/** always show scroll-bar to avoid jumping effect when toggling toolbar */ 
	html {
		overflow-y: scroll;
	}
</style>