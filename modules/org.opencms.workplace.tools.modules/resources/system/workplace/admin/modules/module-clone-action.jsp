<%@page buffer="none" session="false" trimDirectiveWhitespaces="true"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<cms:bundle basename="org.opencms.workplace.tools.modules.messages">
<jsp:useBean id="cloneModule" scope="request" class="org.opencms.workplace.tools.modules.CmsCloneModule">
	<jsp:setProperty name="cloneModule" property="*" />
</jsp:useBean>
<c:if test="${param.submit}">
	<div id="success">
	<%
		Throwable error = null;
		try {
		    cloneModule.init(pageContext, request, response);
			cloneModule.executeModuleClone();
		} catch (Throwable t) {
		    error = t;
		    if (cloneModule.success()) {
			    %><h2>
			    <fmt:message key="clonemodule.success">
			    	<fmt:param value="${param.packageName}" />
			    </fmt:message>
			    </h2>
			    <%
		    } else {
			    %><h2>
			    <fmt:message key="clonemodule.failure">
			    	<fmt:param value="${param.packageName}" />
			    </fmt:message>
			    </h2><%
		    }
		}
		if (error == null) {
		    %><div><h2>
		        <fmt:message key="clonemodule.success">
			    	<fmt:param value="${param.packageName}" />
			    </fmt:message>
		    </h2></div><%
		} else { pageContext.setAttribute("errormessage", error.getLocalizedMessage()); 
		    %><p>
		    	<fmt:message key="clonemodule.errormessage">
		    		<fmt:param value="${errormessage}" />
		    	</fmt:message>
		     </p>
		    <p>
		    <fmt:message key="clonemodule.checklog" />
		    </p><%
		}
	%>
	</div>
</c:if>
</cms:bundle>