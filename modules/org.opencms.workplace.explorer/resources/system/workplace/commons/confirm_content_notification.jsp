<%@ page import="org.opencms.workplace.CmsDialog,
                 org.opencms.file.*,
                 org.opencms.util.CmsUUID,
                 org.opencms.i18n.CmsEncoder,
                 org.opencms.main.CmsException,
                 org.opencms.db.CmsUserSettings,
                 org.opencms.notification.*,
                 org.opencms.jsp.CmsJspActionElement,
                 java.util.*"
%>

<%
    CmsJspActionElement jsp = new CmsJspActionElement(pageContext, request, response);
    CmsDialog wp = new CmsDialog(jsp);

switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed
        wp.getSettings().getFrameUris().put("body", "/system/workplace/views/explorer/explorer_fs.jsp"); 
	wp.actionCloseDialog();

break;

default:
%>

    <%= wp.htmlStart() %>
    <%= wp.bodyStart("Content Notification Confirmation") %>
    <%= wp.dialogStart() %>
    <%= wp.dialogContentStart("OpenCms content notification") %>
    <form name="main" class="nomargin" action="<%= wp.getDialogUri() %>" method="post">
    <%= wp.paramsAsHidden() %>
    <input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">
    <input type="hidden" name="<%= wp.PARAM_ACTION %>" value="<%= CmsDialog.ACTION_CANCEL %>">
<%
    CmsObject cms = wp.getCms();
    String resource = cms.getRequestContext().removeSiteRoot(CmsEncoder.decode(request.getParameter("resource")));
    try {
        
        CmsUUID userId = new CmsUUID(request.getParameter("userId"));
        String cause = request.getParameter("cause");
        CmsUser responsible = cms.readUser(userId);
        // update additional info "confirmed resources" of responsible
        List confirmedResources;
        confirmedResources = (List)responsible.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_CONFIRMED_RESOURCES);
        if (confirmedResources == null) {
            confirmedResources = new ArrayList();
            responsible.setAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_CONFIRMED_RESOURCES, confirmedResources);
        }
        CmsNotificationCause resourceInfo = new CmsNotificationCause(cms.readResource(resource, CmsResourceFilter.IGNORE_EXPIRATION), Integer.parseInt(cause), new Date());
        if (!confirmedResources.contains(resourceInfo)) {
            confirmedResources.add(resourceInfo);
        }
        cms.writeUser(responsible);
    } catch (Exception e) {
        throw new CmsException(org.opencms.notification.Messages.get().container(org.opencms.notification.Messages.ERR_CONFIRM_RESOURCE_1, resource), e);
    }
%>   
    <%= wp.key(org.opencms.workplace.commons.Messages.GUI_NOTIFICATION_CONFIRMED_1, new Object[]{resource}) %>
    <%= wp.dialogContentEnd() %>
    <%= wp.dialogButtonsClose() %>
</form>
    <%= wp.dialogEnd() %>
    <%= wp.bodyEnd() %>
    <%= wp.htmlEnd() %>

<% } %>