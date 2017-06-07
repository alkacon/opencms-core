/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.commons;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsHtmlList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Creates the dialogs for showing and restoring deleted resources.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/show-deleted.jsp
 * </ul>
 * <p>
 *
 * @since 6.9.1
 */
public class CmsDeletedResources extends CmsDialog {

    /** Request parameter name for the relatedresources parameter. */
    public static final String PARAM_READTREE = "readtree";

    /** The list action. */
    private String m_paramListAction;

    /** Parameter value, if the deleted resources should be displayed for the subtree. */
    private String m_paramReadtree;

    /** The selected items, comma separated list. */
    private String m_paramSelItems;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsDeletedResources(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDeletedResources(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the html code to include the needed js code.<p>
     *
     * @return html code
     */
    public String buildIncludeJs() {

        StringBuffer html = new StringBuffer(512);

        html.append("<script type='text/javascript' src='");
        html.append(CmsWorkplace.getSkinUri());
        html.append("commons/ajax.js'></script>\n");

        html.append("<script type='text/javascript' src='");
        html.append(CmsWorkplace.getSkinUri());
        html.append("editors/xmlcontent/help.js'></script>\n");

        html.append("<script type='text/javascript' src='");
        html.append(CmsWorkplace.getSkinUri());
        html.append("admin/javascript/general.js'></script>\n");

        html.append("<script type='text/javascript' src='");
        html.append(CmsWorkplace.getSkinUri());
        html.append("admin/javascript/list.js'></script>\n");

        html.append("<script type='text/javascript'><!--\n");

        html.append("\tvar ");
        html.append(CmsHtmlList.NO_SELECTION_HELP_VAR);
        html.append(" = '");
        html.append(
            CmsStringUtil.escapeJavaScript(key(org.opencms.workplace.list.Messages.GUI_LIST_ACTION_NO_SELECTION_0)));
        html.append("';\n");

        html.append("function doReportUpdate(msg, state) {\n");
        html.append("\tvar img = state + '.png';\n");
        html.append("\tvar txt = '';\n");
        html.append("\tvar elem = document.getElementById('ajaxreport');\n");
        html.append("\tif (state != 'ok') {\n");
        html.append("\t\tif (state == 'fatal') {\n");
        html.append("\t\t\timg = 'error.png';\n");
        html.append("\t\t\ttxt = '");
        html.append(key(org.opencms.workplace.Messages.GUI_AJAX_REPORT_GIVEUP_0));
        html.append("';\n");
        html.append("\t\t} else if (state == 'wait') {\n");
        html.append("\t\t\timg = 'wait.gif';\n");
        html.append("\t\t\ttxt = '");
        html.append(key(org.opencms.workplace.Messages.GUI_AJAX_REPORT_WAIT_0));
        html.append("'\n");
        html.append("\t\t} else if (state == 'error') {\n");
        html.append("\t\t\ttxt = '");
        html.append(key(org.opencms.workplace.Messages.GUI_AJAX_REPORT_ERROR_0));
        html.append("' + msg;\n");
        html.append("\t\t}\n");
        html.append("\t} else {");
        html.append("\t\telem.innerHTML = msg;\n");
        html.append("\t}\n");
        html.append("\tif (txt != '') {\n");
        html.append("\t\tvar html = \"<table border='0' style='vertical-align:middle; height: 200px;'>\";");
        html.append("\t\thtml += \"<tr><td width='40' align='center' valign='middle'><img src='");
        html.append(getSkinUri());
        html.append("commons/\";");
        html.append("\t\thtml += img;");
        html.append("\t\thtml += \"' width='32' height='32' alt=''></td>\";");
        html.append("\t\thtml += \"<td valign='middle'><span style='color: #000099; font-weight: bold;'>\";");
        html.append("\t\thtml += txt;");
        html.append("\t\thtml += \"</span><br></td></tr></table>\";");
        html.append("\t\telem.innerHTML = html;");
        html.append("\t}\n");
        html.append("}\n");

        html.append("function reloadReport() {\n");
        html.append("\tvar readtree = document.forms[\"");
        html.append(CmsDeletedResourcesList.LIST_ID);
        html.append("-form\"].");
        html.append(PARAM_READTREE);
        html.append(".checked;\n");
        html.append("\tmakeRequest('");
        html.append(getJsp().link("/system/workplace/commons/report-deleted.jsp"));
        html.append("', '");
        html.append(CmsDialog.PARAM_RESOURCE);
        html.append("=");
        html.append(getParamResource());
        html.append("&");
        html.append(PARAM_READTREE);
        html.append("=");
        html.append("' + readtree");
        html.append(", 'doReportUpdate');\n");
        html.append("}\n");

        html.append("// -->\n");
        html.append("</script>\n");

        return html.toString();
    }

    /**
     * Override to display additional options in the dialog.<p>
     *
     * @return html code to display additional options
     */
    public String buildOptions() {

        StringBuffer result = new StringBuffer(128);

        result.append("<input type='checkbox' name='");
        result.append(PARAM_READTREE);
        result.append("' value='true' onclick=\"reloadReport();\"");
        if (Boolean.valueOf(getParamReadtree()).booleanValue()) {
            result.append(" checked='checked'");
        }
        result.append(">&nbsp;");
        result.append(key(Messages.GUI_DELETED_RESOURCES_READ_TREE_0));

        return result.toString();
    }

    /**
     * Builds the html code for the report with the list of deleted resources.<p>
     *
     * @return html code for the report with the list of deleted resources
     *
     * @throws JspException if dialog actions fail
     * @throws IOException in case of errros forwarding to the required result page
     * @throws ServletException in case of errros forwarding to the required result page
     */
    public String buildReport() throws JspException, ServletException, IOException {

        CmsDeletedResourcesList list = new CmsDeletedResourcesList(
            getJsp(),
            getParamResource(),
            Boolean.valueOf(getParamReadtree()).booleanValue());

        list.actionDialog();
        list.getList().setBoxed(false);

        StringBuffer result = new StringBuffer(512);
        result.append("<div style='height:200px; overflow: auto;'>\n");
        result.append(list.getList().listHtml());
        result.append("</div>\n");
        return result.toString();
    }

    /**
     * Builds the java script code to build the report.<p>
     *
     * @return html code with the java script to use ajax to create the report
     */
    public String buildReportRequest() {

        StringBuffer html = new StringBuffer(512);

        html.append("<script type='text/javascript'><!--\n");
        html.append("makeRequest('");
        html.append(getJsp().link("/system/workplace/commons/report-deleted.jsp"));
        html.append("', '");
        html.append(CmsDialog.PARAM_RESOURCE);
        html.append("=");
        html.append(getParamResource());
        html.append("', 'doReportUpdate');\n");
        html.append("// -->\n");
        html.append("</script>\n");

        return html.toString();
    }

    /**
     * Builds the necessary button row.<p>
     *
     * @return the button row
     */
    public String dialogButtons() {

        return dialogButtonsOkCancel(
            " onclick=\"listMAction('"
                + CmsDeletedResourcesList.LIST_ID
                + "','"
                + CmsDeletedResourcesList.LIST_MACTION_RESTORE
                + "', '', noSelHelp);\"",
            null);
    }

    /**
     * Executes the actions from the deleted resources list.<p>
     *
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     *
     * @throws JspException if problems including sub-elements occur
     */
    public void executeListMultiActions() throws JspException {

        if (getParamListAction().equals(CmsDeletedResourcesList.LIST_MACTION_RESTORE)) {

            StringBuffer errors = new StringBuffer();

            Iterator<String> iter = getSelectedItems().iterator();
            while (iter.hasNext()) {
                CmsUUID id = new CmsUUID(iter.next());
                try {
                    getCms().restoreDeletedResource(id);
                } catch (Exception e) {
                    errors.append(e.getLocalizedMessage());
                }
            }

            if (errors.length() > 0) {
                throw new CmsRuntimeException(
                    Messages.get().container(Messages.ERR_RESTORE_SELECTED_RESOURCES_1, errors));
            } else {
                actionCloseDialog();
            }
        }
    }

    /**
     * Returns the paramListAction.<p>
     *
     * @return the paramListAction
     */
    public String getParamListAction() {

        return m_paramListAction;
    }

    /**
     * Returns the paramReadtree.<p>
     *
     * @return the paramReadtree
     */
    public String getParamReadtree() {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_paramReadtree)) {
            return "false";
        }

        return m_paramReadtree;
    }

    /**
     * Returns the paramSelItems.<p>
     *
     * @return the paramSelItems
     */
    public String getParamSelItems() {

        return m_paramSelItems;
    }

    /**
     * Returns a list of resource ids of the current selected items.<p>
     *
     * @return a list of resource ids of the current selected items
     */
    public List<String> getSelectedItems() {

        Iterator<?> it = CmsStringUtil.splitAsList(getParamSelItems(), CmsHtmlList.ITEM_SEPARATOR, true).iterator();
        List<String> items = new ArrayList<String>();
        while (it.hasNext()) {
            String id = (String)it.next();
            items.add(id);
        }
        return items;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#paramsAsHidden()
     */
    @Override
    public String paramsAsHidden() {

        List<String> excludes = new ArrayList<String>();
        excludes.add(PARAM_READTREE);
        return paramsAsHidden(excludes);
    }

    /**
     * Sets the paramListAction.<p>
     *
     * @param paramListAction the paramListAction to set
     */
    public void setParamListAction(String paramListAction) {

        m_paramListAction = paramListAction;
    }

    /**
     * Sets the paramReadtree.<p>
     *
     * @param paramReadtree the paramReadtree to set
     */
    public void setParamReadtree(String paramReadtree) {

        m_paramReadtree = paramReadtree;
    }

    /**
     * Sets the paramSelItems.<p>
     *
     * @param paramSelItems the paramSelItems to set
     */
    public void setParamSelItems(String paramSelItems) {

        m_paramSelItems = paramSelItems;
    }

    /**
     * @see org.opencms.workplace.CmsDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);

        // set title
        setParamTitle(key(Messages.GUI_DELETED_RESOURCES_TITLE_1, new Object[] {getParamResource()}));

        // init params to appear as hidden field
        if (getParamListAction() == null) {
            setParamListAction("");
        }
        if (getParamSelItems() == null) {
            setParamSelItems("");
        }

        if (A_CmsListDialog.LIST_MULTI_ACTION.equals(getParamAction())) {
            setAction(A_CmsListDialog.ACTION_LIST_MULTI_ACTION);
        }
    }
}
