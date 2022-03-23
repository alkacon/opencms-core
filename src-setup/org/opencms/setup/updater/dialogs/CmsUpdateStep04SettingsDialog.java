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

package org.opencms.setup.updater.dialogs;

import org.opencms.setup.CmsUpdateUI;
import org.opencms.setup.ui.CmsSetupErrorDialog;
import org.opencms.setup.xml.CmsXmlConfigUpdater;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;

import com.vaadin.server.FontAwesome;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.PasswordField;
import com.vaadin.v7.ui.TextField;

/**
 * Settings dialog.<p>
 */
public class CmsUpdateStep04SettingsDialog extends A_CmsUpdateDialog {

    /**Validator for admin data. */
    class UserValidator implements Validator {

        private static final long serialVersionUID = 1L;

        public void validate(Object value) throws InvalidValueException {

            if (m_ui.getUpdateBean().isValidUser()) {
                return;
            }
            throw new InvalidValueException("The entered user / password is not correct");
        }

    }

    /**Vaddin serial id. */
    private static final long serialVersionUID = 1L;

    /**vaadin component. */
    private Label m_errorMessage;

    /**vaadin component. */
    private Label m_icon;

    /**vaadin component. */
    private TextField m_adminUser;

    /**vaadin component. */
    private PasswordField m_adminPassword;

    /**vaadin component. */
    private TextField m_siteRoot;

    /**vaadin component. */
    private TextField m_appRoot;

    /**vaadin component. */
    private TextField m_configFolder;

    /**vaadin component. */
    private HorizontalLayout m_beforeRun;

    /**vaadin component. */
    private Label m_icon0;

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#init(org.opencms.setup.CmsUpdateUI)
     */
    @Override
    public boolean init(CmsUpdateUI ui) {

        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);
        super.init(ui, true, true);
        setCaption("OpenCms Update-Wizard - Settings");
        m_icon.setContentMode(ContentMode.HTML);
        m_icon.setValue(FontOpenCms.WARNING.getHtml());

        m_adminUser.setValue(ui.getUpdateBean().getAdminUser());
        m_adminPassword.setValue(ui.getUpdateBean().getAdminPwd());
        m_siteRoot.setValue(ui.getUpdateBean().getUpdateSite());
        m_appRoot.setValue(ui.getUpdateBean().getWebAppRfsPath());
        m_configFolder.setValue(ui.getUpdateBean().getConfigRfsPath());
        m_icon0.setContentMode(ContentMode.HTML);
        m_icon0.setValue(FontAwesome.WRENCH.getHtml());
        return true;
    }

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#submitDialog()
     */
    @Override
    protected boolean submitDialog() {

        m_ui.getUpdateBean().setAdminPwd(m_adminPassword.getValue());
        m_ui.getUpdateBean().setAdminUser(m_adminUser.getValue());
        m_ui.getUpdateBean().setUpdateSite(m_siteRoot.getValue());

        CmsXmlConfigUpdater configUpdater = m_ui.getUpdateBean().getXmlConfigUpdater();
        if (!configUpdater.isDone()) {
            try {
                configUpdater.transformConfig();
                m_adminPassword.removeAllValidators();
                m_adminPassword.addValidator(new UserValidator());
            } catch (Exception e) {
                CmsSetupErrorDialog.showErrorDialog("Unable to transform configs", e);
                return false;
            }
        } else {
            m_adminPassword.removeAllValidators();
            m_adminPassword.addValidator(new UserValidator());
        }

        return m_adminPassword.isValid();
    }

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#getNextDialog()
     */
    @Override
    A_CmsUpdateDialog getNextDialog() {

        return new CmsUpdateStep05Modules();
    }

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#getPreviousDialog()
     */
    @Override
    A_CmsUpdateDialog getPreviousDialog() {

        return new CmsUpdateStep01LicenseDialog();
    }
}
