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
import org.opencms.security.CmsPrincipal;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect.WidgetType;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;

/**
 * Dialog for delete of principals and ous.<p>
 */
public class CmsDeletePrincipalDialog extends CmsBasicDialog {

    /**vaadin serial id. */
    private static final long serialVersionUID = -7191571070148172989L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDeletePrincipalDialog.class);

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

    /**The principal which should be deleted. */
    private I_CmsPrincipal m_principal;

    /**CmsObject. */
    private CmsObject m_cms;

    /**The name of the ou which should be deleted, may be null.*/
    private String m_ouName;

    /**vaadin component. */
    private Panel m_dependencyPanel;

    /**vaadin component. */
    private CmsPrincipalSelect m_principalSelect;

    /**
     * public constructor used for user or groups.<p>
     *
     * @param cms CmsObject
     * @param principalUUID id of principal
     * @param window window showing the dialog
     */
    public CmsDeletePrincipalDialog(CmsObject cms, CmsUUID principalUUID, final Window window) {
        init(cms, window);
        try {
            m_principal = CmsPrincipal.readPrincipal(cms, principalUUID);
            displayResourceInfoDirectly(Collections.singletonList(CmsAccountsApp.getPrincipalInfo(m_principal)));
            if (m_principal instanceof CmsUser) {
                m_label.setValue(
                    CmsVaadinUtils.getMessageText(
                        Messages.GUI_USERMANAGEMENT_DELETE_USER_1,
                        m_principal.getSimpleName()));
            } else {
                m_label.setValue(
                    CmsVaadinUtils.getMessageText(
                        Messages.GUI_USERMANAGEMENT_DELETE_GROUP_1,
                        m_principal.getSimpleName()));
            }
            List<CmsUser> user = null;
            if (m_principal instanceof CmsGroup) {
                user = m_cms.getUsersOfGroup(m_principal.getName());
            }

            CmsResourceInfoTable table = new CmsResourceInfoTable(
                cms.getResourcesForPrincipal(principalUUID, null, false),
                user);
            table.setHeight("300px");
            table.setWidth("100%");
            m_dependencyPanel.setVisible(table.size() > 0);
            m_principalSelectLayout.setVisible(table.size() > 0);
            if (m_dependencyPanel.isVisible()) {
                m_dependencyPanel.setContent(table);
                m_principalSelect.setUseVaadin(true);
                m_principalSelect.setCaption(
                    m_principal instanceof CmsGroup
                    ? CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GROUP_0)
                    : CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_0));
                m_principalSelect.setRealPrincipalsOnly(true);
                m_principalSelect.setWidgetType(
                    m_principal instanceof CmsUser ? WidgetType.userwidget : WidgetType.groupwidget);

                m_principalSelect.setPrincipalType(
                    m_principal instanceof CmsUser ? I_CmsPrincipal.PRINCIPAL_USER : I_CmsPrincipal.PRINCIPAL_GROUP);
            }
        } catch (CmsException e) {
            LOG.error("Unable to read principal", e);
        }
    }

    /**
     * public constructor used for OUs.<p>
     *
     * @param cms CmsObject
     * @param ouName name of ou to delete
     * @param window window showing dialog
     */
    public CmsDeletePrincipalDialog(CmsObject cms, String ouName, Window window) {
        m_ouName = ouName;
        init(cms, window);
        m_dependencyPanel.setVisible(false);
        m_principalSelectLayout.setVisible(false);
        try {
            List<CmsUser> userList = OpenCms.getOrgUnitManager().getUsers(m_cms, ouName, true);
            List<CmsOrganizationalUnit> oUs = OpenCms.getOrgUnitManager().getOrganizationalUnits(m_cms, ouName, true);

            if (userList.isEmpty() && oUs.isEmpty()) {
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

        if (m_ouName != null) {
            //Delete OU
            try {
                OpenCms.getOrgUnitManager().deleteOrganizationalUnit(m_cms, m_ouName);
            } catch (CmsException e) {
                LOG.error("Unable to delete OU");
            }
            return;
        }

        //Delete User or Group
        try {
            String principalNameToCopyTo = m_principalSelect.getValue();
            I_CmsPrincipal principalTarget = null;
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(principalNameToCopyTo)) {
                principalTarget = CmsPrincipal.readPrincipal(m_cms, principalNameToCopyTo);
            }
            if (m_principal instanceof CmsUser) {
                m_cms.deleteUser(m_principal.getId(), principalTarget != null ? principalTarget.getId() : null);
            } else {
                m_cms.deleteGroup(m_principal.getId(), principalTarget != null ? principalTarget.getId() : null);
            }
        } catch (CmsException e) {
            LOG.error("Unable to delete principal", e);
        }
    }

    /**
     * Initialized the dialog.<p>
     *
     * @param cms CmsObject
     * @param window window
     */
    private void init(CmsObject cms, final Window window) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_icon.setContentMode(ContentMode.HTML);
        m_icon.setValue(FontOpenCms.WARNING.getHtml());
        m_cms = cms;
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -7845894751587879028L;

            public void buttonClick(ClickEvent event) {

                deletePrincipal();
                window.close();
                A_CmsUI.get().reload();

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