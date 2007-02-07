/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsEditUserDialog.java,v $
 * Date   : $Date: 2007/02/07 17:06:11 $
 * Version: $Revision: 1.17.4.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit new or existing system user in the administration view.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.17.4.6 $ 
 * 
 * @since 6.0.0 
 */
public class CmsEditUserDialog extends A_CmsEditUserDialog {

    /** The additional information. */
    private Map m_addInfo = new TreeMap();

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsEditUserDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsEditUserDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the additional Info.<p>
     *
     * @return the additional Info
     */
    public Map getAddInfo() {

        return m_addInfo;
    }

    /**
     * Returns the description of the parent ou.<p>
     * 
     * @return the description of the parent ou
     */
    public String getAssignedOu() {

        try {
            return OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()).getDescription()
                + " ("
                + CmsOrganizationalUnit.SEPARATOR
                + getParamOufqn()
                + ")";
        } catch (CmsException e) {
            return null;
        }
    }

    /**
     * Sets the additional Info.<p>
     *
     * @param addInfo the additional Info to set
     */
    public void setAddInfo(Map addInfo) {

        m_addInfo = addInfo;
    }

    /**
     * This method is only needed for displaying reasons.<p>
     * 
     * @param assignedOu nothing to do with this parameter
     */
    public void setAssignedOu(String assignedOu) {

        // nothing will be done here, just to avoid warnings
        assignedOu.length();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsEditUserDialog#createDialogHtml(java.lang.String)
     */
    protected String createDialogHtml(String dialog) {

        if (dialog.equals(PAGES[0])) {
            return super.createDialogHtml(dialog);
        }

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());
        int todo;
        result.append(dialogBlockStart("" /*key(Messages.GUI_USER_EDITOR_LABEL_ADDITIONALINFO_BLOCK_0)*/));
        result.append(createWidgetTableStart());
        result.append(createDialogRowsHtml(14, 14));
        result.append(createWidgetTableEnd());
        result.append(dialogBlockEnd());

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsEditUserDialog#createUser(java.lang.String, java.lang.String, java.lang.String, java.util.Map)
     */
    protected CmsUser createUser(String name, String pwd, String desc, Map info) throws CmsException {

        return getCms().createUser(name, pwd, desc, info);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsEditUserDialog#defineWidgets()
     *-/
     protected void defineWidgets() {

     super.defineWidgets();
     addWidget(new CmsWidgetDialogParameter(this, "addInfo", PAGES[0], new CmsDisplayWidget()/*, getAdditionalInfos()*-/));
     }

     /**
     * @see org.opencms.workplace.tools.accounts.A_CmsEditUserDialog#getListClass()
     */
    protected String getListClass() {

        return CmsUsersList.class.getName();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsEditUserDialog#getListRootPath()
     */
    protected String getListRootPath() {

        return "/accounts/orgunit/users";
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsEditUserDialog#getPageArray()
     *-/
     protected String[] getPageArray() {

     return new String[] {"page1", "page2"};
     }

     /**
     * @see org.opencms.workplace.tools.accounts.A_CmsEditUserDialog#isEditable(org.opencms.file.CmsUser)
     */
    protected boolean isEditable(CmsUser user) {

        return true;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsEditUserDialog#writeUser(org.opencms.file.CmsUser)
     */
    protected void writeUser(CmsUser user) throws CmsException {

        getCms().writeUser(user);
    }

    /**
     * Returns all editable additional information.<p>
     * 
     * @return all editable additional information
     */
    private List getAdditionalInfos() {

        List addInfo = new ArrayList();
        addInfo.add("info1");
        addInfo.add("info1/info11");
        addInfo.add("info2");
        addInfo.add("info2/info21");
        addInfo.add("info2/info22");
        return addInfo;
    }
}