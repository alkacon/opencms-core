<%@page import="org.opencms.ade.containerpage.CmsContainerpageActionElement"%><%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%
  CmsContainerpageActionElement containerpage= new CmsContainerpageActionElement(pageContext, request, response);
%><script type="text/javascript" src="<cms:link>/system/modules/org.opencms.ade.containerpage/resources/resources.nocache.js</cms:link>"></script>
<%= containerpage.exportAll() %>