/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.login;

import org.opencms.file.CmsObject;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.login.CmsLoginController.CmsLoginTargetInfo;

import java.util.List;
import java.util.Locale;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;

/**
 * The UI class for the Vaadin-based login dialog.<p>
 */
public class CmsLoginUI extends A_CmsUI implements I_CmsLoginUI {

    /** 
     * Parameters which are initialized during the initial page load of the login dialog.<p>
     */
    public static class Parameters {

        /** The PC type (public or private). */
        public String m_pcType;

        /** The preselected OU. */
        public String m_preselectedOu;

        /** The locale. */
        public Locale m_locale;

        /** The requested resource. */
        public String m_requestedResource;

        /**
         * Creates a new instance.<p>
         * 
         * @param pcType the PC type 
         * @param preselectedOu the preselected OU 
         * @param locale the locale 
         * @param requestedResource the requested resource 
         */
        public Parameters(String pcType, String preselectedOu, Locale locale, String requestedResource) {

            m_pcType = pcType;
            m_preselectedOu = preselectedOu;
            m_locale = locale;
            m_requestedResource = requestedResource;
        }

        /** 
         * Gets the locale.<p>
         * 
         * @return the locale 
         */
        public Locale getLocale() {

            return m_locale;
        }

        /** 
         * Gets the PC type (private or public).<p>
         * 
         * @return the pc type 
         */
        public String getPcType() {

            return m_pcType;

        }

        /**
         * Gets the preselected OU.<p>
         * 
         * @return the preselected OU 
         */
        public String getPreselectedOu() {

            return m_preselectedOu;
        }

        /**
         * Gets the requested resource path.<p>
         * 
         * @return the requested resource path 
         */
        public String getRequestedResource() {

            return m_requestedResource;
        }

    }

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The login form. */
    private CmsLoginForm m_loginForm;

    /** The widget used to open the login target. */
    private CmsLoginTargetOpener m_targetOpener = new CmsLoginTargetOpener();

    /** The admin CMS context. */
    private static CmsObject m_adminCms;

    /** The login controller. */
    private CmsLoginController m_controller;

    /** 
     * Attribute used to store initialization data when the UI is first loaded.  
     */
    public static final String INIT_DATA_SESSION_ATTR = "CmsLoginUI_initData";

    /** 
     * Sets the admin CMS object.<p>
     * 
     * @param cms the admin cms object 
     */
    public static void setAdminCmsObject(CmsObject cms) {

        m_adminCms = cms;
    }

    /**
     * @see org.opencms.ui.login.I_CmsLoginUI#displayError(java.lang.String)
     */
    public void displayError(String message) {

        Notification.show(message, Type.ERROR_MESSAGE);

    }

    /**
     * @see org.opencms.ui.login.I_CmsLoginUI#getOrgUnit()
     */
    public String getOrgUnit() {

        String result = m_loginForm.getOrgUnit();
        if (result == null) {
            result = "";
        }
        return result;

    }

    /** 
     * @see org.opencms.ui.login.I_CmsLoginUI#getPassword()
     */
    public String getPassword() {

        return m_loginForm.getPassword();
    }

    /**
     * @see org.opencms.ui.login.I_CmsLoginUI#getPcType()
     */
    public String getPcType() {

        String result = m_loginForm.getPcType();
        if (result == null) {
            result = "public";
        }
        return result;
    }

    /**
     * @see org.opencms.ui.login.I_CmsLoginUI#getUser()
     */
    public String getUser() {

        return m_loginForm.getUser();
    }

    /** 
     * @see org.opencms.ui.login.I_CmsLoginUI#openLoginTarget(org.opencms.ui.login.CmsLoginController.CmsLoginTargetInfo)
     */
    public void openLoginTarget(CmsLoginTargetInfo targetInfo) {

        m_targetOpener.openTarget(targetInfo.getTarget(), targetInfo.getUser(), targetInfo.getPassword());
    }

    /**
     * @see org.opencms.ui.login.I_CmsLoginUI#setSelectableOrgUnits(java.util.List)
     */
    public void setSelectableOrgUnits(List<CmsOrganizationalUnit> ous) {

        m_loginForm.setSelectableOrgUnits(ous);
    }

    /** 
     * @see org.opencms.ui.login.I_CmsLoginUI#showAlreadyLoggedIn()
     */
    public void showAlreadyLoggedIn() {

        throw new UnsupportedOperationException("Not implemented yet, this shouldn't even be called.");
    }

    /**
     * @see org.opencms.ui.login.I_CmsLoginUI#showLoginView(java.lang.String)
     */
    public void showLoginView(String preselectedOu) {

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.addComponent(m_targetOpener);
        content.setExpandRatio(m_targetOpener, 0f);

        content.addComponent(m_loginForm);
        content.setComponentAlignment(m_loginForm, Alignment.MIDDLE_CENTER);
        content.setExpandRatio(m_loginForm, 1);

        setContent(content);
        if (preselectedOu == null) {
            preselectedOu = "/";
        }
        m_loginForm.selectOrgUnit(preselectedOu);

    }

    /**
     * @see com.vaadin.ui.UI#init(com.vaadin.server.VaadinRequest)
     */
    @Override
    protected void init(VaadinRequest request) {

        Parameters params = (Parameters)(request.getWrappedSession().getAttribute(INIT_DATA_SESSION_ATTR));
        m_controller = new CmsLoginController(m_adminCms, params);
        m_controller.setUi(this);
        setLocale(params.getLocale());
        m_loginForm = new CmsLoginForm(m_controller, params.getLocale());
        m_controller.onInit();

    }
}
