/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsOrgUnitOverviewDialog.java,v $
 * Date   : $Date: 2007/01/31 14:23:18 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.workplace.CmsWidgetDialogParameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to get an overview of an organizational unit in the administration view.<p>
 * 
 * @author Raphael Schnuck 
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.5.6
 */
public class CmsOrgUnitOverviewDialog extends A_CmsOrgUnitDialog {

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsOrgUnitOverviewDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    public void actionCommit() {

        // noop
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     * 
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     * 
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_ORGUNIT_EDITOR_LABEL_IDENTIFICATION_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 2));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defaultActionHtmlEnd()
     */
    protected String defaultActionHtmlEnd() {

        return "";
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    protected void defineWidgets() {

        // initialize the user object to use for the dialog
        initUserObject();

        setKeyPrefix(KEY_PREFIX);

        // widgets to display
        addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "name", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "description", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "parentOu", PAGES[0], new CmsDisplayWidget()));
    }

    /**
     * Initializes the organizational unit object to work with depending on the dialog state and request parameters.<p>
     * 
     */
    protected void initUserObject() {

        if (m_orgunit == null) {
            try {
                m_orgunit = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn());
                m_orgUnitBean = new CmsOrgUnitBean();
                m_orgUnitBean.setDescription(m_orgunit.getDescription());
                m_orgUnitBean.setName(m_orgunit.getName());
                m_orgUnitBean.setParentOu(m_orgunit.getParentFqn());
                m_orgUnitBean.setFqn(m_orgunit.getName());
                m_orgUnitBean.setResources(OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                    getCms(),
                    m_orgunit.getName()));
            } catch (Exception e) {
                // noop
            }
        } else {
            try {
                m_orgUnitBean = new CmsOrgUnitBean();
                m_orgUnitBean.setDescription(m_orgunit.getDescription());
                m_orgUnitBean.setName(m_orgunit.getName());
                m_orgUnitBean.setParentOu(m_orgunit.getParentFqn());
                m_orgUnitBean.setFqn(m_orgunit.getName());
                m_orgUnitBean.setResources(OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                    getCms(),
                    m_orgunit.getName()));
            } catch (CmsException e) {
                // noop
            }
        }
    }
}
