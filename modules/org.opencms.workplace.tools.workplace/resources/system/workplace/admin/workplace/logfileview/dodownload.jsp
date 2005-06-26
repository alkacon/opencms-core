<%@ page import="org.opencms.main.*,org.opencms.workplace.tools.workplace.rfsfile.*, org.opencms.util.*, org.opencms.workplace.*, java.io.*" %>
<% // cannot use new_admin style, link class shifts vertical-align from baseline (firefox looks awful). %>
<style type="text/css">
body, table {
  font-size: 11px;
  font-family: Verdana, Arial, Helvetica, sans-serif;   
}

.link,
.link a {
  display: inline;
  background: none;
  background-color: transparent;
  vertical-align: baseline;
  cursor: hand;
  cursor: pointer;
  color: WindowText;
  text-decoration: none;
}

.link:hover a,
.link a:hover {
  text-decoration: underline;
  color: #000088;
}

.link:hover img,
.link img:hover { 
  text-decoration: none;
}
</style>
<%
CmsRfsFileViewer viewer = OpenCms.getWorkplaceManager().getFileViewSettings();
String filePath = viewer.getFilePath();

if (CmsStringUtil.isEmpty(filePath)) {
  throw new ServletException(org.opencms.workplace.tools.workplace.rfsfile.Messages.get().key(org.opencms.workplace.tools.workplace.rfsfile.Messages.ERR_DOWNLOAD_SERVLET_FILE_ARG_0));
}
File file = new File(filePath);
%>
<p>
<table style="margin:6px;padding:4px;text-align:left;">
<tr>
<td colspan="2">
<b> 
<%=org.opencms.workplace.tools.workplace.rfsfile.Messages.get().key(org.opencms.workplace.tools.workplace.rfsfile.Messages.GUI_WORLKPLACE_LOGVIEW_DODOWNLOAD_HEADER_1, file.getName())%> 
</b>
</td>
</tr>
<tr>
<td colspan="2">
<%=org.opencms.workplace.tools.workplace.rfsfile.Messages.get().key(org.opencms.workplace.tools.workplace.rfsfile.Messages.GUI_WORLKPLACE_LOGVIEW_DODOWNLOAD_MESSAGE_0)%> 
<span class="link"><a href="javascript:download()" onClick="javascript:download"><%=org.opencms.workplace.tools.workplace.rfsfile.Messages.get().key(org.opencms.workplace.tools.workplace.rfsfile.Messages.GUI_WORLKPLACE_LOGVIEW_DODOWNLOAD_LINKTXT_0)%></a></span>. 
</td>
</tr>
<tr id="spacer">
<td colspan="2">&nbsp;</td>
</tr>
<tr>
<td colspan="2" style="text-align:center;">
<input type="button" value="Close" onClick="javascript:window.close()"/>
</td>
</p>
<script type="text/javascript">
function download(){
  window.location.href = "<%=request.getParameter("servletUrl")%>?filePath=<%=file.getAbsolutePath().replace('\\', '/')%>"
}

window.setTimeout("download()",500);
</script>

