<%@ page import="
    org.opencms.file.CmsPropertyDefinition,
    org.opencms.util.CmsRequestUtil,
    org.opencms.workplace.*,
    org.opencms.workplace.help.*,
    org.opencms.jsp.*"

%><%
    CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
    CmsFrameset wp = new CmsFrameset(cms);

    int buttonStyle = wp.getSettings().getUserSettings().getWorkplaceButtonStyle();

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>

<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=<%= wp.getEncoding() %>">
<link rel="stylesheet" type="text/css" href="<%= CmsWorkplace.getStyleUri(wp.getJsp(), "workplace.css")%>">
<title>OpenCms Workplace Head Frame</title>
<script >
    var pfad="<%= wp.getResourceUri() %>";
    var encoding="<%= wp.getEncoding() %>";

<%	if (wp.isHelpEnabled()) {
        out.println(CmsHelpTemplateBean.buildOnlineHelpJavaScript(wp.getLocale()));
    }
%>
    function loadBody() {
        var link = document.forms.wpViewSelect.wpView.options[document.forms.wpViewSelect.wpView.selectedIndex].value;
        window.top.body.location.href = link;
    }
<%
    String loginJsp = cms.getCmsObject().readPropertyObject(cms.getRequestContext().getUri(), CmsPropertyDefinition.PROPERTY_LOGIN_FORM, true).getValue("/system/login/index.html");
    String exitLink = cms.link(CmsRequestUtil.appendParameter(loginJsp, CmsLogin.PARAM_ACTION_LOGOUT, String.valueOf(true)));
%>
    function doLogout() {
        var windowCoords = calculateWinCoords();
        window.top.location.href = "<%= exitLink %>&<%= CmsLogin.PARAM_WPDATA %>=" + encodeURIComponent(windowCoords);
    }

    function doReload() {
        window.top.location.href = "<%= wp.getWorkplaceReloadUri() %>";
    }

    function doShowPublishQueue(){
        window.top.location.href = '<%= cms.link("/system/workplace/views/admin/admin-fs.jsp") %>';
        loadBody();
    }

    function calculateWinCoords() {
        var winWidth = 0, winHeight = 0, winTop = 0, winLeft = 0;
        if(typeof( window.outerWidth ) == 'number' ) {
            //Non-IE
            winWidth = window.outerWidth;
            winHeight = window.outerHeight;
        } else if (top.document.documentElement && top.document.documentElement.clientHeight) {
            winHeight = top.document.documentElement.clientHeight + 20;
            winWidth = top.document.documentElement.clientWidth + 10;
        } else if (top.document.body && top.document.body.clientHeight) {
            winWidth = top.document.body.clientWidth + 10;
            winHeight = top.document.body.clientHeight + 20;
        }
        if (window.screenY) {
            winTop = window.screenY;
            winLeft = window.screenX;
        } else if (window.screenTop) {
            winTop = window.screenTop - 20;
            winLeft = window.screenLeft;
        }
        return winLeft + "|" + winTop + "|" + winWidth + "|" + winHeight;
    }

    function openwin(url, name, w, h) {
        window.open(url, name, 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,width='+w+',height='+h);
    }
</script>
</head>

<body class="buttons-head" unselectable="on"<%= wp.isReloadRequired()?" onload=\"loadBody();\"":"" %>>

<%= wp.buttonBar(CmsWorkplace.HTML_START) %>
<%= wp.buttonBarStartTab(0, 0) %>
<%= wp.buttonBarLabel(org.opencms.workplace.Messages.GUI_LABEL_PROJECT_0) %>

<td>
<form style="margin: 0; padding: 0;" name="wpProjectSelect" method="post" action="<%= cms.link(cms.getRequestContext().getUri()) %>">
<div>
<%= wp.getProjectSelect("name=\"wpProject\" onchange=\"document.forms.wpProjectSelect.submit()\"", "style=\"width:150px\"") %>
<input type="hidden" name="<%= CmsFrameset.PARAM_WP_FRAME %>" value="head">
</div>
</form></td>

<%= wp.getPublishButton() %>
<%= wp.getPublishQueueButton() %>
<%
if (wp.showSiteSelector()) {

%><%= wp.buttonBarSeparator(5, 0) %>
<%= wp.buttonBarLabel(org.opencms.workplace.Messages.GUI_LABEL_SITE_0) %>
<td>
<form style="margin: 0; padding: 0;" name="wpSiteSelect" method="post" action="<%= cms.link(cms.getRequestContext().getUri()) %>">
<div>
<%= wp.getSiteSelect("name=\"wpSite\" style=\"width:150px\" onchange=\"document.forms.wpSiteSelect.submit()\"") %>
<input type="hidden" name="<%= CmsFrameset.PARAM_WP_FRAME %>" value="head">
</div>
</form></td>

<% } %>

<%= wp.buttonBarSeparator(5, 0) %>
<%= wp.buttonBarLabel(org.opencms.workplace.Messages.GUI_LABEL_VIEW_0) %>
<td>
<form style="margin: 0; padding: 0;" name="wpViewSelect" method="post" action="<%= cms.link(cms.getRequestContext().getUri()) %>">
<div>
<%= wp.getViewSelect("name=\"wpView\" style=\"width:150px\" onchange=\"document.forms.wpViewSelect.submit()\"") %>
<input type="hidden" name="<%= CmsFrameset.PARAM_WP_FRAME %>" value="head">
</div>
</form></td>

<%= wp.buttonBarSeparator(5, 0) %>
<%= wp.button("javascript:doReload()", null, "reload.png", org.opencms.workplace.Messages.GUI_BUTTON_RELOAD_0, buttonStyle) %>
<%= wp.getPreferencesButton() %>

<%
if (wp.isSyncEnabled()) {
    out.println(wp.button("../commons/synchronize.jsp", "body", "folder_refresh.png", org.opencms.workplace.Messages.GUI_BUTTON_SYNCFOLDER_0, buttonStyle));
}
if (wp.isHelpEnabled()) {
    out.println(wp.button("javascript:openOnlineHelp();", null, "help.png", org.opencms.workplace.Messages.GUI_BUTTON_HELP_0, buttonStyle));
}
%>

<td style="width: 100%">&nbsp;</td>
<%= wp.buttonBarSeparator(5, 0) %>
<%= wp.button("javascript:doLogout()", null, "logout.png", org.opencms.workplace.Messages.GUI_BUTTON_EXIT_0, buttonStyle) %>

<% if (buttonStyle != 2) {%>

<td>&nbsp;</td>
<td><span style="display: block; width: 80px; height: 22px; background-image: url('<%= CmsWorkplace.getSkinUri() %>commons/workplace.png'); "></span></td>

<% } %>
<%= wp.buttonBar(CmsWorkplace.HTML_END) %>

</body>
</html>
