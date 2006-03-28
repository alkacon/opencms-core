<%@ page import="
	org.opencms.workplace.administration.CmsAdminMenu,
	org.opencms.jsp.CmsJspActionElement"
%>
<%
    CmsJspActionElement jsp = new CmsJspActionElement(pageContext, request, response);
    CmsAdminMenu wp = new CmsAdminMenu(jsp);

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <title>Administration Menu</title>
  <meta http-equiv='Content-Type' content='text/html; charset=<%= wp.getEncoding() %>'>
  <link rel='stylesheet' type='text/css' href='<%=wp.getStyleUri(wp.getJsp(),"menu.css")%>'>

  <script type='text/javascript' src='<%=wp.getSkinUri()%>admin/javascript/general.js'></script>
  <script type='text/javascript' src='<%=wp.getSkinUri()%>admin/javascript/adminmenu.js'></script>
  <script type='text/javascript'>
    function bodyLoad() {
      checkSize();
      setContextHelp();
      loadingOff();
      document.getElementById('loaderContainerH').height = pHeight();
      //window.onresize=checkSize;
    }
    function bodyUnload() {
      loadingOn();
    }
    function checkSize() {
      if (wHeight() <= pHeight() && wWidth() < 213) {
    	parent.location.href='admin-fs.jsp?scroll=true';
      } else if (wHeight() > pHeight() && wWidth() > 212) {
        parent.location.href='admin-fs.jsp?scroll=false';
      }
    }
    var activeItem = '/';
  </script>
</head>
<body onLoad='bodyLoad();' onUnload='bodyUnload();'>
    <a href='#' name='top' id='top'></a>
      <table border="0" cellspacing="0" cellpadding="0" id="loaderContainer" onClick="return false;">
          <tr><td id="loaderContainerH">&nbsp;</td></tr>
      </table>
      <div class='screenBody'>
        <table id='navArea' cellspacing='0' cellpadding='0' width='100%' border='0'>
          <tr>
            <td>
              <div id='navLayout'>
                <%=wp.groupHtml(wp)%>
              </div>
            </td>
          </tr>
        </table>
      </div>
</body>
</html>
