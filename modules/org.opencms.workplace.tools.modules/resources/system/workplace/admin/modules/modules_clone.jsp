<%@page buffer="none" session="false" trimDirectiveWhitespaces="true"%>
<%@ page import="org.opencms.workplace.tools.modules.*,org.opencms.workplace.*,org.opencms.jsp.*,org.opencms.file.*,org.opencms.main.*" %>
<%@ page import = "java.util.Map" %>
<%@taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%
		CmsJspActionElement jsp = new CmsJspActionElement(pageContext, request, response);
		CmsObject cms = jsp.getCmsObject();
		pageContext.setAttribute("locale",OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
%>
<fmt:setLocale value="${locale}" />
<cms:bundle basename="org.opencms.workplace.tools.modules.messages">
<jsp:useBean id="cloneModule" scope="request" class="org.opencms.workplace.tools.modules.CmsCloneModule">
	<jsp:setProperty name="cloneModule" property="*" />
	<%
	    cloneModule.init(pageContext, request, response);
	%>
</jsp:useBean>
<html>
    <head>	
	<script type="text/javascript" src="<cms:link>/system/modules/org.opencms.jquery/resources/packed/jquery.js</cms:link>"></script>
	<style type="text/css">
	.control-group label {
		float: left;
		width: 150px; 
	}
	
	#clone-module-form {
		font-size: 12px; 
	}
	
	fieldset {
		margin:  10px; 
	}
	
	legend {
		font-weight: bold; 
	}

	.control-group {
		margin: 5px;
		clear: both;
	}
	
	.control-group input {
		width: 400px; 
	}
	</style>
	</head>
<body>

	<div>
		<div class="headline">
			<h1><fmt:message key="clonemodule.title" />: ${param.module}</h1>
		</div>
		<form class="form-horizontal" method="post" id="clone-module-form">
			<!-- Select a Source Module  -->
			
			<fieldset style="display: none; ">
				<legend><fmt:message key="clonemodule.selectmodule" /> </legend>
				<div class="control-group">
					<label class="control-label" for="sourceModuleName"><fmt:message key="clonemodule.sourcemodule" /></label>
					<div class="controls">
						<select class="input-xlarge" id="sourceModuleName" name="sourceModuleName">
							<c:forEach items="${cloneModule.allModuleNames}" var="moduleName">
								<c:choose>
									<c:when test="${moduleName eq param.module}">
										<option selected>${moduleName}</option>
									</c:when>
									<c:otherwise>
										<option>${moduleName}</option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>
					</div>
				</div>
			
			</fieldset>
			<!-- Module Information -->
			<fieldset>
				<legend> <fmt:message key="clonemodule.newmodule" /></legend>
				
				<!-- Enter new Module package name -->
				<div class="control-group">
					<label class="control-label" for="packageName"><fmt:message key="clonemodule.packagename" /></label>
					<div class="controls">
						<input type="text" id="packageName" name="packageName" class="border-radius-none input-xlarge"
							placeholder="Package name" value="${cloneModule.packageName}">
					</div>
				</div><!-- control-group for package name -->
				
				<!-- Enter new Module nice name -->
				<div class="control-group">
					<label class="control-label" for="niceName"><fmt:message key="clonemodule.modulename" /></label>
					<div class="controls">
						<input type="text" id="niceName" name="niceName" class="border-radius-none input-xlarge"
							placeholder="Module nice name" value="${cloneModule.niceName}">
					</div>
				</div><!-- Control group for nice name -->
				
				<!-- Enter new Module description -->
				<div class="control-group">
					<label class="control-label" for="description"> <fmt:message key="clonemodule.moduledescription" /></label>
					<div class="controls">
						<input type="text" id="description" name="description" class="border-radius-none input-xlarge"
							placeholder="Module description" value="${cloneModule.description}">
					</div>
				</div>
				
				<!-- Enter new Module Group -->
				<div class="control-group">
					<label class="control-label" for="group"><fmt:message key="clonemodule.modulegroup" /></label>
					<div class="controls">
						<input type="text" id="group" name="group" class="border-radius-none input-xlarge" placeholder="Module Group"
							value="${cloneModule.group}">
					</div>
				</div>
				
				<!-- Enter new Action class -->
				<div class="control-group">
					<label class="control-label" for="actionClass"><fmt:message key="clonemodule.actionclass" /></label>
					<div class="controls">
						<input type="text" id="actionClass" name="actionClass" class="border-radius-none input-xlarge"
							placeholder="Action class" value="${cloneModule.actionClass}">
					</div>
				</div>
				
			</fieldset>
			<!-- Author information -->
			<fieldset>
				<legend><fmt:message key="clonemodule.authorinfo" /></legend>
				<!-- Enter new Author name -->
				<div class="control-group">
					<label class="control-label" for="authorName"><fmt:message key="clonemodule.authorname" /></label>
					<div class="controls">
						<input type="text" id="authorName" name="authorName" class="border-radius-none input-xlarge"
							placeholder="Author name" value="${cloneModule.authorName}">
					</div>
				</div>
				<!-- Enter new Author email -->
				<div class="control-group">
					<label class="control-label" for="authorEmail"> <fmt:message key="clonemodule.authoremail" /></label>
					<div class="controls">
						<input type="text" id="authorEmail" name="authorEmail" class="border-radius-none input-xlarge"
							placeholder="Author email" value="${cloneModule.authorEmail}">
					</div>
				</div>
			</fieldset>
			<!-- Translation options  -->
			<fieldset>
				<legend> <fmt:message key="clonemodule.translationoptions" /></legend>
				<!-- Enter a source prefix name -->
				<div class="control-group">
					<label class="control-label" for="sourceNamePrefix"><fmt:message key="clonemodule.sourceprefix" /></label>
					<div class="controls">
						<input type="text" id="sourceNamePrefix" name="sourceNamePrefix" class="border-radius-none input-xlarge"
							placeholder="Source prefix name" value="${cloneModule.sourceNamePrefix}">
					</div>
				</div>
				<!-- Enter a target prefix name -->
				<div class="control-group">
					<label class="control-label" for="targetNamePrefix"> <fmt:message key="clonemodule.targetprefix" /></label>
					<div class="controls">
						<input type="text" id="targetNamePrefix" name="targetNamePrefix" class="border-radius-none input-xlarge"
							placeholder="Target prefix name" value="${cloneModule.targetNamePrefix}">
					</div>
				</div>
				<!-- Enter a source formatter module -->
				<div class="control-group">
					<label class="control-label" for="formatterSourceModule"> <fmt:message key="clonemodule.formattersource" /></label>
					<div class="controls">
						<input type="text" id="formatterSourceModule" name="formatterSourceModule" class="border-radius-none input-xlarge"
							placeholder="Formatters source module name" value="${cloneModule.formatterSourceModule}">
					</div>
				</div>
				<!-- Enter a new formatter path -->
				<div class="control-group">
					<label class="control-label" for="formatterTargetModule"><fmt:message key="clonemodule.formattertarget" /></label>
					<div class="controls">
						<input type="text" id="formatterTargetModule" name="formatterTargetModule" class="border-radius-none input-xlarge"
							placeholder="Formatters target module name" value="${cloneModule.formatterTargetModule}">
					</div>
				</div>
				<!-- Option to correct schema locations -->
				<div class="">
					<div class="controls">
						<label class="checkbox" for="changeResourceTypes">
							<input type="checkbox" value="true"
								<c:if test="${param.rewriteContainerPages}">checked</c:if> id="changeResourceTypes" name="changeResourceTypes">
								<fmt:message key="clonemodule.changeresourcetypes" />
						</label>
					</div>
					<div class="controls">
						<label class="checkbox" for="rewriteContainerPages">
							<input type="checkbox" value="true"
								<c:if test="${param.rewriteContainerPages}">checked</c:if> id="rewriteContainerPages" name="rewriteContainerPages">
								 <fmt:message key="clonemodule.rewriteContainerPages" />
						</label>
					</div>
					<div class="controls">
						<label class="checkbox" for="applyChangesEverywhere">
							<input type="checkbox" value="true"
								<c:if test="${param.applyChangesEverywhere}">checked</c:if> id="applyChangesEverywhere" name="applyChangesEverywhere">
								 <fmt:message key="clonemodule.applyChangesEverywhere" />
						</label>
					</div>
					
						<!-- Option to delete the source module after cloning -->
				
					<div class="controls">
						<label class="checkbox" for="deleteModule">
							<input type="checkbox" value="true" 
								<c:if test="${param.deleteModule}">checked</c:if> id="deleteModule" name="deleteModule">
								 <fmt:message key="clonemodule.deletesource" />
						</label>
					</div>
				
				</div>
			</fieldset>
			<input type="hidden" name="submit" value="true">
			<div class="control-group">
				<div class="controls">
					<button id ="submit-button" class="btn-u"> <fmt:message key="clonemodule.actionbutton" /></button>
				</div>
			</div>
		</form>
		<div id="wait">
			<h2><fmt:message key="clonemodule.loading" /></h2>
		</div>
		<div id="result">
		</div>
<script>
$('#wait').hide();
$('#result').hide();

function navigateToCloseLink() {
    window.location = "${param.closelink}";
}

$("#clone-module-form").submit(function(event) {
  $('#wait').show();
  $('#submit-button').hide();

  var params = $('#clone-module-form').serialize();
  var posting = $.post('<cms:link>/system/workplace/admin/modules/module-clone-action.jsp</cms:link>?' + params);
  posting.done(function( data ) {
      
      $('#wait').hide();
      $("<button>").text('<fmt:message key="clonemodule.closebutton" />').click(navigateToCloseLink).appendTo("body");
      var content = $(data).find("#success").addBack("#success");
      $("#result").empty().append(content);
      $('#result').show();
  });
  event.preventDefault();
  return false;
  
});
</script>
	</div>
</body>
</html>
</cms:bundle>