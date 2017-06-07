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

package org.opencms.workplace.tools.accounts;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.CmsListItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Sub organization units list.<p>
 *
 * @since 6.5.6
 */
public class CmsOrgUnitsSubList extends A_CmsOrgUnitsList {

    /** list id constant. */
    public static final String LIST_ID = "lsous";

    /** Stores the value of the request parameter for the user id. */
    private String m_paramOufqn;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsOrgUnitsSubList(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_SUBORGUNITS_LIST_NAME_0));
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsOrgUnitsSubList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Deletes the given organizational unit.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void actionDelete() throws Exception {

        OpenCms.getOrgUnitManager().deleteOrganizationalUnit(getCms(), getParamOufqn());
        actionCloseDialog();
    }

    /**
     * Deletes the given organizational unit.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void actionParent() throws Exception {

        String ouFqn = CmsOrganizationalUnit.getParentFqn(getParamOufqn());

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(A_CmsOrgUnitDialog.PARAM_OUFQN, new String[] {ouFqn});
        params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});
        String toolPath = getCurrentToolPath().substring(0, getCurrentToolPath().lastIndexOf("/"));
        getToolManager().jspForwardTool(this, toolPath, params);
        actionCloseDialog();
    }

    /**
     *
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtml()
     */
    @Override
    public String defaultActionHtml() {

        if ((getList() != null) && getList().getAllContent().isEmpty()) {
            // TODO: check the need for this
            refreshList();
        }
        StringBuffer result = new StringBuffer(2048);
        result.append(defaultActionHtmlStart());
        result.append(customHtmlStart());
        try {
            if (hasSubOUs()) {
                result.append(defaultActionHtmlContent());
            }
        } catch (CmsException e) {
            // noop
        }
        result.append(customHtmlEnd());
        result.append(defaultActionHtmlEnd());
        return result.toString();
    }

    /**
     * Returns the organizational unit fqn parameter value.<p>
     *
     * @return the organizational unit fqn parameter value
     */
    public String getParamOufqn() {

        return m_paramOufqn;
    }

    /**
     * Checks if the user has more then one organizational unit to administrate.<p>
     *
     * @return true if the user has more then then one organizational unit to administrate
     *         otherwise false
     * @throws CmsException if the organizational units can not be read
     */
    public boolean hasSubOUs() throws CmsException {

        List<CmsOrganizationalUnit> orgUnits = OpenCms.getOrgUnitManager().getOrganizationalUnits(
            getCms(),
            m_paramOufqn,
            true);
        if (orgUnits == null) {
            return false;
        }
        if (orgUnits.size() < 1) {
            return false;
        }
        return true;
    }

    /**
     * Sets the organizational unit fqn parameter value.<p>
     *
     * @param ouFqn the organizational unit fqn parameter value
     */
    public void setParamOufqn(String ouFqn) {

        if (ouFqn == null) {
            ouFqn = "";
        }
        m_paramOufqn = ouFqn;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();
        List<CmsOrganizationalUnit> orgUnits = OpenCms.getOrgUnitManager().getOrganizationalUnits(
            getCms(),
            m_paramOufqn,
            true);
        Iterator<CmsOrganizationalUnit> itOrgUnits = orgUnits.iterator();
        while (itOrgUnits.hasNext()) {
            CmsOrganizationalUnit childOrgUnit = itOrgUnits.next();
            CmsListItem item = getList().newItem(childOrgUnit.getName());
            item.set(LIST_COLUMN_NAME, CmsOrganizationalUnit.SEPARATOR + childOrgUnit.getName());
            item.set(LIST_COLUMN_DESCRIPTION, childOrgUnit.getDescription(getLocale()));
            item.set(LIST_COLUMN_ADMIN, Boolean.valueOf(
                OpenCms.getRoleManager().hasRole(getCms(), CmsRole.ADMINISTRATOR.forOrgUnit(childOrgUnit.getName()))));
            item.set(LIST_COLUMN_WEBUSER, Boolean.valueOf(childOrgUnit.hasFlagWebuser()));
            ret.add(item);
        }
        return ret;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // test the needed parameters
        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParamOufqn()));
        OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), m_paramOufqn).getName();
    }
}
