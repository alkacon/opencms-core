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

import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsFileUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialogParameter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit new or existing organizational unit in the administration view.<p>
 *
 * @since 6.5.6
 */
public class CmsOrgUnitEditDialog extends A_CmsOrgUnitDialog {

    /** Request parameter name for the sub organizational unit fqn. */
    public static final String PARAM_SUBOUFQN = "suboufqn";

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsOrgUnitEditDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsOrgUnitEditDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() {

        List<Throwable> errors = new ArrayList<Throwable>();

        try {
            // if new create it first
            if (isNewOrgUnit()) {
                List<String> resourceNames = CmsFileUtil.removeRedundancies(m_orgUnitBean.getResources());
                CmsOrganizationalUnit newOrgUnit = OpenCms.getOrgUnitManager().createOrganizationalUnit(
                    getCms(),
                    m_orgUnitBean.getFqn(),
                    m_orgUnitBean.getDescription(),
                    m_orgUnitBean.getFlags(),
                    resourceNames.isEmpty() ? null : (String)resourceNames.get(0));

                if (!resourceNames.isEmpty()) {
                    resourceNames.remove(0);
                    Iterator<String> itResourceNames = CmsFileUtil.removeRedundancies(resourceNames).iterator();
                    while (itResourceNames.hasNext()) {
                        OpenCms.getOrgUnitManager().addResourceToOrgUnit(
                            getCms(),
                            newOrgUnit.getName(),
                            itResourceNames.next());
                    }
                }
            } else {
                CmsOrganizationalUnit orgunit = OpenCms.getOrgUnitManager().readOrganizationalUnit(
                    getCms(),
                    m_orgUnitBean.getFqn());
                orgunit.setDescription(m_orgUnitBean.getDescription());
                orgunit.setFlags(m_orgUnitBean.getFlags());
                List<String> resourceNamesNew = CmsFileUtil.removeRedundancies(m_orgUnitBean.getResources());
                List<CmsResource> resourcesOld = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                    getCms(),
                    orgunit.getName());
                List<String> resourceNamesOld = new ArrayList<String>();
                Iterator<CmsResource> itResourcesOld = resourcesOld.iterator();
                while (itResourcesOld.hasNext()) {
                    CmsResource resourceOld = itResourcesOld.next();
                    resourceNamesOld.add(getCms().getSitePath(resourceOld));
                }
                Iterator<String> itResourceNamesNew = resourceNamesNew.iterator();
                // add new resources to ou
                while (itResourceNamesNew.hasNext()) {
                    String resourceNameNew = itResourceNamesNew.next();
                    if (!resourceNamesOld.contains(resourceNameNew)) {
                        OpenCms.getOrgUnitManager().addResourceToOrgUnit(getCms(), orgunit.getName(), resourceNameNew);
                    }
                }
                Iterator<String> itResourceNamesOld = resourceNamesOld.iterator();
                // delete old resources from ou
                while (itResourceNamesOld.hasNext()) {
                    String resourceNameOld = itResourceNamesOld.next();
                    if (!resourceNamesNew.contains(resourceNameOld)) {
                        OpenCms.getOrgUnitManager().removeResourceFromOrgUnit(
                            getCms(),
                            orgunit.getName(),
                            resourceNameOld);
                    }
                }
                // write the edited organizational unit
                OpenCms.getOrgUnitManager().writeOrganizationalUnit(getCms(), orgunit);
            }
        } catch (Throwable t) {
            errors.add(t);
        }

        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
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
            result.append(dialogBlockStart(key(Messages.GUI_ORGUNIT_EDITOR_LABEL_FLAGS_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(3, 4));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockStart(key(Messages.GUI_ORGUNIT_EDITOR_LABEL_CONTENT_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(5, 5));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        super.defineWidgets();

        // widgets to display
        if (isNewOrgUnit()) {
            addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "name", PAGES[0], new CmsInputWidget()));
        } else {
            addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "name", PAGES[0], new CmsDisplayWidget()));
        }
        addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "description", PAGES[0], new CmsTextareaWidget()));
        addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "parentOuDesc", PAGES[0], new CmsDisplayWidget()));

        if (isNewOrgUnit()) {
            addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "nologin", PAGES[0], new CmsCheckboxWidget()));
            addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "webusers", PAGES[0], new CmsCheckboxWidget()));
        } else {
            if (m_orgUnitBean.isWebusers()) {
                addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "nologin", PAGES[0], new CmsDisplayWidget()));
            } else {
                addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "nologin", PAGES[0], new CmsCheckboxWidget()));
            }
            addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "webusers", PAGES[0], new CmsDisplayWidget()));
        }
        addWidget(
            new CmsWidgetDialogParameter(
                m_orgUnitBean,
                "resources",
                PAGES[0],
                new CmsVfsFileWidget(false, getCms().getRequestContext().getSiteRoot(), false)));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParamOufqn()));
        if (!isNewOrgUnit()) {
            // test the needed parameters
            OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()).getName();
        }
    }
}
