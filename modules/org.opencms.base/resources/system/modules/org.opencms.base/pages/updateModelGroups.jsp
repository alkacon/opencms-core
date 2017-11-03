<% if (request.getParameter("basePath")!=null){
	String basePath=request.getParameter("basePath");
	String baseContainerName=request.getParameter("baseContainerName");
	org.opencms.ade.containerpage.CmsModelGroupHelper.updateModelGroupResources(request, response,basePath,baseContainerName);
}else{ %>
	<div>
	<h1>
	Update model groups
	</h1>
	<p>
	The data structure of model groups has been changed slightly between OpenCms versions 10.0.1 and 10.5.0. This JSP will update existing model groups so they will be rendered correctly and will be editable once again.
	</p>
	<p>
	Set the base path to a folder within model groups will be searched or to a specific model group resource.<br />
	Set the base container name to the name of the main container of your template, for example 'page-complete' when using the apollo template.
	</p>
	<form>
	
	<label for="basePath">Base path:</label><br />
	<input type="text" name="basePath" id="basePath"/><br />
	<label for="baseContainerName">Base container name:</label><br />
	<input type="text" name="baseContainerName" id="baseContainerName" /><br />
	<input type="submit" value="submit" />
	
	</form>
	</div>
<% } %>