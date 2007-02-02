/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsOrgUnitEditDialog.java,v $
 * Date   : $Date: 2007/02/02 13:58:10 $
 * Version: $Revision: 1.1.2.4 $
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

import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsFileUtil;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsOrgUnitWidget;
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
 * @author Raphael Schnuck 
 * 
 * @version $Revision: 1.1.2.4 $ 
 * 
 * @since 6.5.6
 */
public class CmsOrgUnitEditDialog extends A_CmsOrgUnitDialog {

    /** Request parameter name for the sub organizational unit fqn. */
    public static final String PARAM_SUBOUFQN = "suboufqn";

    /** Stores the value of the request parameter for the sub organizational unit fqn. */
    private String m_paramSuboufqn;

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
    public void actionCommit() {

        List errors = new ArrayList();

        try {
            // if new create it first
            if (m_orgunit == null) {
                List resourceNames = CmsFileUtil.removeRedundancies(m_orgUnitBean.getResources());
                CmsOrganizationalUnit newOrgUnit = OpenCms.getOrgUnitManager().createOrganizationalUnit(
                    getCms(),
                    m_orgUnitBean.getFqn(),
                    m_orgUnitBean.getDescription(),
                    0,
                    (String)resourceNames.get(0));

                m_orgunit = newOrgUnit;
                resourceNames.remove(0);
                Iterator itResourceNames = CmsFileUtil.removeRedundancies(resourceNames).iterator();
                while (itResourceNames.hasNext()) {
                    OpenCms.getOrgUnitManager().addResourceToOrgUnit(
                        getCms(),
                        m_orgunit.getName(),
                        (String)itResourceNames.next());
                }
            } else {
                // changes of ou / parent ou have to be written
                int todo = -1;

                //                if (!m_orgunit.getParentFqn().equals(m_orgUnitBean.getParentOu())) {
                //
                //                }

                m_orgunit.setDescription(m_orgUnitBean.getDescription());

                List resourceNamesNew = CmsFileUtil.removeRedundancies(m_orgUnitBean.getResources());
                List resourcesOld = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                    getCms(),
                    m_orgunit.getName());
                List resourceNamesOld = new ArrayList();
                Iterator itResourcesOld = resourcesOld.iterator();
                while (itResourcesOld.hasNext()) {
                    CmsResource resourceOld = (CmsResource)itResourcesOld.next();
                    resourceNamesOld.add(resourceOld.getRootPath());
                }
                Iterator itResourceNamesNew = resourceNamesNew.iterator();
                // add new resources to ou
                while (itResourceNamesNew.hasNext()) {
                    String resourceNameNew = (String)itResourceNamesNew.next();
                    if (!resourceNamesOld.contains(resourceNameNew)) {
                        OpenCms.getOrgUnitManager().addResourceToOrgUnit(
                            getCms(),
                            m_orgunit.getDescription(),
                            resourceNameNew);
                    }
                }
                Iterator itResourceNamesOld = resourceNamesOld.iterator();
                // delete old resources from ou
                while (itResourceNamesOld.hasNext()) {
                    String resourceNameOld = (String)itResourceNamesOld.next();
                    if (!resourceNamesNew.contains(resourceNameOld)) {
                        OpenCms.getOrgUnitManager().removeResourceFromOrgUnit(
                            getCms(),
                            m_orgunit.getName(),
                            resourceNameOld);
                    }
                }
            }
            // write the edited organizational unit
            OpenCms.getOrgUnitManager().writeOrganizationalUnit(getCms(), m_orgunit);
        } catch (Throwable t) {
            errors.add(t);
        }

        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    //    protected String defaultActionHtmlStart() throws JspException {
    //    
    //        // TODO Auto-generated method stub
    //        return super.defaultActionHtmlStart();
    //    }
    //
    //    /**
    //     * @see org.opencms.workplace.CmsWidgetDialog#dialogButtonsCustom()
    //     */
    //    public String dialogButtonsCustom() {
    //
    //        boolean onlyDisplay = true;
    //        Iterator it = m_widgets.iterator();
    //        while (it.hasNext()) {
    //            CmsWidgetDialogParameter wdp = (CmsWidgetDialogParameter)it.next();
    //            if (!(wdp.getWidget() instanceof CmsDisplayWidget)) {
    //                onlyDisplay = false;
    //                break;
    //            }
    //        }
    //        if (!onlyDisplay) {
    //            // this is a single page dialog, create common buttons
    //            return dialogButtons(new int[] {BUTTON_OK, BUTTON_CANCEL}, new String[2]);
    //        }
    //        // this is a display only dialog
    //        return "";
    //    }

    /**
     * Returns the sub organizational unit fqn parameter value.<p>
     * 
     * @return the sub organizational unit fqn parameter value
     */
    public String getParamSuboufqn() {

        return m_paramSuboufqn;
    }

    /**
     * Sets the sub organizational unit fqn parameter value.<p>
     * 
     * @param subOuFqn the sub organizational unit fqn parameter value
     */
    public void setParamSuboufqn(String subOuFqn) {

        m_paramSuboufqn = subOuFqn;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
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
            result.append(dialogBlockStart(key(Messages.GUI_ORGUNIT_EDITOR_LABEL_CONTENT_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(3, 3));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    protected void defineWidgets() {

        super.defineWidgets();

        // widgets to display
        if (m_orgUnitBean.getName() == null) {
            addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "name", PAGES[0], new CmsInputWidget()));
        } else {
            addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "name", PAGES[0], new CmsDisplayWidget()));
        }
        addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "description", PAGES[0], new CmsTextareaWidget()));
        addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "parentOu", PAGES[0], new CmsOrgUnitWidget()));
        addWidget(new CmsWidgetDialogParameter(m_orgUnitBean, "resources", PAGES[0], new CmsVfsFileWidget(
            false,
            getCms().getRequestContext().getSiteRoot())));
    }

    /**
     * Initializes the unit object to work with depending on the dialog state and request parameters.<p>
     * 
     */
    protected void initUserObject() {

        if (m_orgunit == null) {
            try {
                if (isNewOrgUnit()) {
                    m_orgunit = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamSuboufqn());
                } else {
                    m_orgunit = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn());
                }
                if (m_orgunit == null) {
                    m_orgUnitBean = new CmsOrgUnitBean();
                } else {
                    m_orgUnitBean = new CmsOrgUnitBean();
                    m_orgUnitBean.setDescription(m_orgunit.getDescription());
                    m_orgUnitBean.setName(m_orgunit.getName());
                    m_orgUnitBean.setParentOu(m_orgunit.getParentFqn());
                    m_orgUnitBean.setFqn(m_orgunit.getName());
                    List resources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                        getCms(),
                        m_orgunit.getName());
                    List resourceNames = new ArrayList();
                    Iterator itResources = resources.iterator();
                    while (itResources.hasNext()) {
                        CmsResource resource = (CmsResource)itResources.next();
                        resourceNames.add(resource.getRootPath());
                    }
                    m_orgUnitBean.setResources(resourceNames);
                }
            } catch (Exception e) {
                m_orgUnitBean = new CmsOrgUnitBean();
            }
        } else {
            try {
                m_orgUnitBean = new CmsOrgUnitBean();
                m_orgUnitBean.setDescription(m_orgunit.getDescription());
                m_orgUnitBean.setName(m_orgunit.getName());
                m_orgUnitBean.setParentOu(m_orgunit.getParentFqn());
                m_orgUnitBean.setFqn(m_orgunit.getName());
                List resources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                    getCms(),
                    m_orgunit.getName());
                List resourceNames = new ArrayList();
                Iterator itResources = resources.iterator();
                while (itResources.hasNext()) {
                    CmsResource resource = (CmsResource)itResources.next();
                    resourceNames.add(resource.getRootPath());
                }
                m_orgUnitBean.setResources(resourceNames);
            } catch (CmsException e) {
                // noop
            }
        }
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    protected void validateParamaters() throws Exception {

        if (!isNewOrgUnit()) {
            // test the needed parameters
            OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()).getName();
        }
    }

    /**
     * Checks if the new organizational unit dialog has to be displayed.<p>
     * 
     * @return <code>true</code> if the new organizational unit dialog has to be displayed
     */
    private boolean isNewOrgUnit() {

        return getCurrentToolPath().equals("/accounts/orgunit/new");
    }
}
