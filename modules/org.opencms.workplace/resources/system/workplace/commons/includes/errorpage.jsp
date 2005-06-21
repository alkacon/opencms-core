<%@ page import="org.opencms.workplace.*,
                 org.opencms.workplace.editors.*,
                 org.opencms.jsp.util.*" buffer="none" %><%
	
	// get workplace class from request attribute
	CmsDialog wp = CmsDialog.initCmsDialog(pageContext, request, response);
        wp.setParamAction(CmsDialog.DIALOG_CANCEL);

        CmsErrorBean errorBean = new CmsErrorBean(wp.getCms(), (Throwable)wp.getJsp().getRequest().getAttribute("throwable"));
        errorBean.setTitle(wp.getParamTitle());
        String detailsAttribute = "onclick=\"toggleElement('errordetails');\"";
        String buttons = "";
	if (wp instanceof CmsEditor) {
	    String okAttribute = "";
            String discardAttribute = "onclick=\"confirmAction('" + CmsDialog.DIALOG_CANCEL + "', form);\"";
            buttons = wp.dialogButtons(new int[] {wp.BUTTON_EDIT, wp.BUTTON_DISCARD, wp.BUTTON_DETAILS}, new String[] {okAttribute, discardAttribute, detailsAttribute});
        } else { 
            buttons = wp.dialogButtonsCloseDetails("onclick=\"closeErrorDialog('" + wp.getCancelAction() + "', form);\"", detailsAttribute);
	} 
        errorBean.setButtons(buttons);
        errorBean.setHiddenParams(wp.paramsAsHidden());
        errorBean.setParamAction(wp.getDialogUri());

        out.println(errorBean.toHtml());
%>