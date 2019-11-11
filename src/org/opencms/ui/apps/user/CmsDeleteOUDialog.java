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

package org.opencms.ui.apps.user;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;

/**
 * Dialog for delete of principals and ous.<p>
 */
public class CmsDeleteOUDialog extends CmsBasicDialog {

    /**vaadin serial id. */
    private static final long serialVersionUID = -7191571070148172989L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDeleteOUDialog.class);

    /**vaadin component. */
    private Label m_label;

    /**vaadin component. */
    private Label m_icon;

    /**vaadin component. */
    private Button m_cancelButton;

    /**vaadin component. */
    private Button m_okButton;

    /**vaadin component. */
    private FormLayout m_principalSelectLayout;

    /**CmsObject. */
    private CmsObject m_cms;

    /**The name of the ou which should be deleted, may be null.*/
    private String m_ouName;

    /**vaadin component. */
    private Panel m_dependencyPanel;

    /**
     * public constructor used for OUs.<p>
     *
     * @param cms CmsObject
     * @param ouName name of ou to delete
     * @param window window showing dialog
     * @param app
     */
    public CmsDeleteOUDialog(CmsObject cms, String ouName, Window window, CmsAccountsApp app) {

        m_ouName = ouName;
        init(cms, window, app);
        m_dependencyPanel.setVisible(false);
        m_principalSelectLayout.setVisible(false);
        try {
            List<CmsUser> userList = OpenCms.getOrgUnitManager().getUsers(m_cms, ouName, true);
            List<CmsOrganizationalUnit> oUs = OpenCms.getOrgUnitManager().getOrganizationalUnits(m_cms, ouName, true);

            if (userList.isEmpty() && oUs.isEmpty() && hasNoGroups()) {
                m_label.setValue(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_DELETE_OU_CONFIRM_1, ouName));
            } else {
                m_label.setValue(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_DELETE_OU_NOT_POSSIBLE_1, ouName));
                m_okButton.setEnabled(false);
            }
        } catch (CmsException e) {
            LOG.error("Unable to read OU", e);
        }
    }

    /**
     * Deletes the princiapl or OU.<p>
     */
    void deletePrincipal() {

        //Delete OU
        try {
            OpenCms.getOrgUnitManager().deleteOrganizationalUnit(m_cms, m_ouName);
        } catch (CmsException e) {
            LOG.error("Unable to delete OU", e);
        }
    }

    /**
     * Ou has no groups which have to be deleted first? (all except for the Users group)
     *
     * @return true if Ou can be deleted directly
     */
    private boolean hasNoGroups() {

        try {
            List<CmsGroup> groups = OpenCms.getOrgUnitManager().getGroups(m_cms, m_ouName, true);
            if (groups.size() == 0) {
                return true;
            }
            for (CmsGroup g : groups) {
                if (!g.getSimpleName().equals("Users")) {
                    return false;
                }
            }
            return true;
        } catch (CmsException e) {
            LOG.error("Unable to reade groups of OU", e);
            return false;

        }
    }

    /**
     * Initialized the dialog.<p>
     *
     * @param cms CmsObject
     * @param window window
     * @param app
     */
    private void init(CmsObject cms, final Window window, final CmsAccountsApp app) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        try {
            displayResourceInfoDirectly(
                Collections.singletonList(
                    CmsAccountsApp.getOUInfo(
                        OpenCms.getOrgUnitManager().readOrganizationalUnit(A_CmsUI.getCmsObject(), m_ouName))));
        } catch (CmsException e) {
            LOG.error("Unable to read OU", e);
        }
        m_icon.setContentMode(ContentMode.HTML);
        m_icon.setValue(FontOpenCms.WARNING.getHtml());
        m_cms = cms;
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -7845894751587879028L;

            public void buttonClick(ClickEvent event) {

                deletePrincipal();
                window.close();
                app.reload();

            }

        });

        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 6649262870116199591L;

            public void buttonClick(ClickEvent event) {

                window.close();

            }

        });
    }

}