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
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

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
public abstract class A_CmsOrgUnitDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "orgunit";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Request parameter name for the organizational unit fqn. */
    public static final String PARAM_OUFQN = "oufqn";

    /** The organizational unit bean object to work with in this dialog. */
    protected CmsOrgUnitBean m_orgUnitBean;

    /** Stores the value of the request parameter for the organizational unit fqn. */
    private String m_paramOufqn;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public A_CmsOrgUnitDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public A_CmsOrgUnitDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
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
     * Sets the resources for the given orgUnitBean.<p>
     *
     * @param orgUnitBean the <code>CmsOrgUnitBean</code> object
     * @param resources the list of resources
     */
    public void setResourcesInBean(CmsOrgUnitBean orgUnitBean, List<CmsResource> resources) {

        List<String> resourceNames = new ArrayList<String>();
        Iterator<CmsResource> itResources = resources.iterator();
        while (itResources.hasNext()) {
            CmsResource resource = itResources.next();
            resourceNames.add(getCms().getSitePath(resource));
        }
        orgUnitBean.setResources(resourceNames);
    }

    /**
     *
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        initOrgUnitObject();
        setKeyPrefix(KEY_PREFIX);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes the organizational unit object to work with depending
     * on the dialog state and request parameters.<p>
     */
    protected void initOrgUnitObject() {

        try {
            if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
                // edit an existing ou, get the ou object from database
                CmsOrganizationalUnit orgunit = OpenCms.getOrgUnitManager().readOrganizationalUnit(
                    getCms(),
                    getParamOufqn());
                m_orgUnitBean = new CmsOrgUnitBean();
                if (!isNewOrgUnit()) {
                    m_orgUnitBean.setName(orgunit.getName());
                    m_orgUnitBean.setDescription(orgunit.getDescription(getLocale()));
                    m_orgUnitBean.setParentOu(orgunit.getParentFqn());
                    m_orgUnitBean.setFqn(orgunit.getName());
                    m_orgUnitBean.setNologin(orgunit.hasFlagHideLogin());
                    m_orgUnitBean.setWebusers(orgunit.hasFlagWebuser());
                    if (orgunit.getParentFqn() != null) {
                        m_orgUnitBean.setParentOuDesc(
                            OpenCms.getOrgUnitManager().readOrganizationalUnit(
                                getCms(),
                                orgunit.getParentFqn()).getDescription(getLocale())
                                + " ("
                                + CmsOrganizationalUnit.SEPARATOR
                                + orgunit.getParentFqn()
                                + ")");
                    }
                } else {
                    m_orgUnitBean.setParentOu(orgunit.getName());
                    m_orgUnitBean.setParentOuDesc(
                        orgunit.getDescription(getLocale())
                            + " ("
                            + CmsOrganizationalUnit.SEPARATOR
                            + orgunit.getName()
                            + ")");
                }
                List<CmsResource> resources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                    getCms(),
                    orgunit.getName());
                setResourcesInBean(m_orgUnitBean, resources);
            } else {
                // this is not the initial call, get the ou object from session
                m_orgUnitBean = (CmsOrgUnitBean)getDialogObject();
                // test
                m_orgUnitBean.getName();
            }
        } catch (Exception e) {
            // create a new ou object
            m_orgUnitBean = new CmsOrgUnitBean();
            m_orgUnitBean.setParentOu(getParamOufqn());
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the ou (may be changed because of the widget values)
        setDialogObject(m_orgUnitBean);
    }

    /**
     * Checks if the new organizational unit dialog has to be displayed.<p>
     *
     * @return <code>true</code> if the new organizational unit dialog has to be displayed
     */
    protected boolean isNewOrgUnit() {

        return getCurrentToolPath().endsWith("/orgunit/mgmt/new");
    }
}
