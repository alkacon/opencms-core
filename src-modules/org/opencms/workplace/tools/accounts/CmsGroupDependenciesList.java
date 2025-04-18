/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.accounts;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.CmsHtmlList;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Group dependencies list view including delete and transfer functionality.<p>
 *
 * @since 6.0.0
 */
public class CmsGroupDependenciesList extends CmsGroupPrincipalDependenciesList {

    /** Value for the delete action. */
    public static final int ACTION_DELETE = 131;

    /** Value for the transfer action. */
    public static final int ACTION_TRANSFER = 132;

    /** Request parameter value for the delete action. */
    public static final String DELETE_ACTION = "delete";

    /** Request parameter name for the group id, could be a list of ids. */
    public static final String PARAM_GROUPID = "groupid";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/accounts/buttons/";

    /** Request parameter value for the transfer action. */
    public static final String TRANSFER_ACTION = "transfer";

    /** Stores the value of the group name, could be a list of names. */
    private String m_groupName;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsGroupDependenciesList(CmsJspActionElement jsp) {

        this(LIST_ID, jsp);
        m_showAttributes = true;
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsGroupDependenciesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
        m_showAttributes = true;
    }

    /**
     * Protected constructor.<p>
     *
     * @param listId the id of the specialized list
     * @param jsp an initialized JSP action element
     */
    protected CmsGroupDependenciesList(String listId, CmsJspActionElement jsp) {

        super(listId, jsp);
        m_showAttributes = true;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#actionDialog()
     */
    @Override
    public void actionDialog() throws JspException, ServletException, IOException {

        switch (getAction()) {
            case ACTION_DELETE:
                Iterator<String> it = CmsStringUtil.splitAsList(
                    getGroupName(),
                    CmsHtmlList.ITEM_SEPARATOR,
                    true).iterator();
                while (it.hasNext()) {
                    String name = it.next();
                    try {
                        getCms().deleteGroup(name);
                    } catch (CmsException e) {
                        throw new CmsRuntimeException(e.getMessageContainer(), e);
                    }
                }
                setAction(ACTION_CANCEL);
                actionCloseDialog();
                break;
            case ACTION_TRANSFER:
                Map<String, String[]> params = new HashMap<String, String[]>();
                // set action parameter to initial dialog call
                params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});
                // forward to the select replacement screen
                params.put(PARAM_GROUPID, new String[] {getParamGroupid()});
                getToolManager().jspForwardPage(
                    this,
                    getJsp().getRequestContext().getFolderUri() + "group_transfer.jsp",
                    params);
                break;

            default:
                super.actionDialog();
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlContent()
     */
    @Override
    public String defaultActionHtmlContent() {

        if (getList().getTotalSize() > 0) {
            return super.defaultActionHtmlContent();
        }
        return "";
    }

    /**
     * Returns the group Name.<p>
     *
     * @return the group Name
     */
    public String getGroupName() {

        return m_groupName;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#customHtmlEnd()
     */
    @Override
    protected String customHtmlEnd() {

        StringBuffer result = new StringBuffer(512);
        result.append(super.customHtmlEnd());
        result.append("<form name='actions' method='post' action='");
        result.append(getDialogRealUri());
        result.append("' class='nomargin' onsubmit=\"return submitAction('ok', null, 'actions');\">\n");
        result.append(allParamsAsHidden());
        result.append(dialogButtonRow(HTML_START));
        result.append("<input name='");
        result.append(DELETE_ACTION);
        result.append("' type='button' value='");
        result.append(Messages.get().container(Messages.GUI_DEPENDENCIES_BUTTON_DELETE_0).key(getLocale()));
        result.append("' onclick=\"submitAction('");
        result.append(DELETE_ACTION);
        result.append("', form);\" class='dialogbutton'>\n");
        if (getList().getTotalSize() > 0) {
            result.append("<input name='");
            result.append(TRANSFER_ACTION);
            result.append("' type='button' value='");
            result.append(Messages.get().container(Messages.GUI_DEPENDENCIES_BUTTON_TRANSFER_0).key(getLocale()));
            result.append("' onclick=\"submitAction('");
            result.append(TRANSFER_ACTION);
            result.append("', form);\" class='dialogbutton'>\n");
        }
        dialogButtonsHtml(result, BUTTON_CANCEL, "");
        result.append(dialogButtonRow(HTML_END));
        result.append("</form>\n");
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#customHtmlStart()
     */
    @Override
    protected String customHtmlStart() {

        StringBuffer result = new StringBuffer(512);
        result.append(
            dialogBlockStart(Messages.get().container(Messages.GUI_GROUP_DEPENDENCIES_NOTICE_0).key(getLocale())));
        if (getCurrentToolPath().indexOf("/edit/") < 0) {
            result.append(key(Messages.GUI_GROUP_DEPENDENCIES_SELECTED_GROUPS_0));
            result.append(":<br>\n");
            List<String> users = CmsStringUtil.splitAsList(getGroupName(), CmsHtmlList.ITEM_SEPARATOR, true);
            result.append("<ul>\n");
            Iterator<String> it = users.iterator();
            while (it.hasNext()) {
                String name = it.next();
                result.append("<li>");
                result.append(name);
                result.append("</li>\n");
            }
            result.append("</ul>\n");
        }
        if (getList().getTotalSize() > 0) {
            result.append(key(Messages.GUI_GROUP_DEPENDENCIES_NOTICE_TEXT_0));
        } else {
            result.append(key(Messages.GUI_GROUP_DEPENDENCIES_DELETE_0));
        }
        result.append(dialogBlockEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        if (DELETE_ACTION.equals(getParamAction())) {
            setAction(ACTION_DELETE);
        } else if (TRANSFER_ACTION.equals(getParamAction())) {
            setAction(ACTION_TRANSFER);
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // test the needed parameters
        m_groupName = "";
        Iterator<String> itGroups = CmsStringUtil.splitAsList(
            getParamGroupid(),
            CmsHtmlList.ITEM_SEPARATOR,
            true).iterator();
        while (itGroups.hasNext()) {
            CmsUUID id = new CmsUUID(itGroups.next());
            m_groupName += getCms().readGroup(id).getName();
            if (itGroups.hasNext()) {
                m_groupName += CmsHtmlList.ITEM_SEPARATOR;
            }
        }
    }
}