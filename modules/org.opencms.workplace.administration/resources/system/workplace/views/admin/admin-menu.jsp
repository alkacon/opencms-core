<%@ page import="
	org.opencms.workplace.CmsWorkplace,
	org.opencms.workplace.administration.CmsAdminMenu,
	org.opencms.jsp.CmsJspActionElement,
	java.util.Collections
"%><%

	CmsJspActionElement jsp = new CmsJspActionElement(pageContext, request, response);
    CmsAdminMenu wp = new CmsAdminMenu(jsp);
    String req = wp.allRequestParamsAsUrl(Collections.singleton("scroll"));
    if (req.length() > 0) {
        req += "&";
    }

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <title>Administration Menu</title>
  <meta http-equiv='Content-Type' content='text/html; charset=<%= wp.getEncoding() %>'>
  <link rel='stylesheet' type='text/css' href='<%= CmsWorkplace.getStyleUri(wp.getJsp(),"menu.css") %>'>

  <script type='text/javascript' src='<%= CmsWorkplace.getSkinUri() %>admin/javascript/general.js'></script>
  <script type='text/javascript' src='<%= CmsWorkplace.getSkinUri() %>admin/javascript/adminmenu.js'></script>
  <script type='text/javascript'>
    function bodyLoad() {
      checkSize();
      setContextHelp();
      loadingOff();
      document.getElementById('loaderContainerH').height = pHeight();
    }
    function bodyUnload() {
      loadingOn();
    }
    function checkSize() {
      var req = 'admin-fs.jsp?<%=req %>scroll=';
      if (wHeight() <= pHeight() && wWidth() < 213) {
    	parent.location.href = req + 'true';
      } else if (wHeight() > pHeight() && wWidth() > 212) {
        parent.location.href = req + 'false';
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
