<%@page contentType="text/csv" %><%response.addHeader("Content-Disposition", "attachment; filename=aliases.csv");%><%
String site = request.getParameter("site");
org.opencms.jsp.CmsJspActionElement jsae = new org.opencms.jsp.CmsJspActionElement(pageContext, request, response);
org.opencms.file.CmsObject cms = jsae.getCmsObject();
org.opencms.gwt.CmsAliasHelper helper = new org.opencms.gwt.CmsAliasHelper();
cms.getRequestContext().setSiteRoot(site);
String csv = helper.exportAliasesAsCsv(cms);
out.println(csv);
%>