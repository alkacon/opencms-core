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
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect.WidgetType;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Dialog for delete multiple principal.<p>
 */
public class CmsDeleteMultiplePrincipalDialog extends CmsBasicDialog {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDeleteMultiplePrincipalDialog.class);

    /**vaadin serial id. */
    private static final long serialVersionUID = -1191281655158071555L;

    /**The icon. */
    private Label m_icon;

    /**The icon. */
    private Label m_icon2;

    /**Vaadin component. */
    Button m_okButton;

    /**Vaadin component. */
    CmsObject m_cms;

    /**Vaadin component. */
    private Label m_label;

    /**Vaadin component. */
    Button m_cancelButton;

    /**The ids to delete. */
    private Set<String> m_ids;

    /**Ids of the user to delete. */
    private Set<CmsUUID> m_userIDs;

    /**Ids of the group to delete.*/
    private Set<CmsUUID> m_groupIDs;

    /**vaadin component. */
    private Panel m_dependencyPanel;

    /**The label shown in case of trying to delete a default user or group.*/
    private HorizontalLayout m_label_deleteDefault;

    /**Confirmation layout. */
    private HorizontalLayout m_deleteConfirm;

    /**vaadin component. */
    private CmsPrincipalSelect m_principalSelect;

    /**vaadin component. */
    private VerticalLayout m_principalSelectLayout;

    /**
     * Public constructor.<p>
     *
     * @param cms CmsObeject
     * @param context ids of principal to delete
     * @param window window
     * @param app
     */
    public CmsDeleteMultiplePrincipalDialog(CmsObject cms, Set<String> context, Window window, CmsAccountsApp app) {

        init(cms, window, app);
        boolean defaultUser = false;
        m_ids = context;
        m_groupIDs = new HashSet<CmsUUID>();
        m_userIDs = new HashSet<CmsUUID>();
        List<CmsResourceInfo> infos = new ArrayList<CmsResourceInfo>();
        for (String id : m_ids) {
            try {
                CmsPrincipal principal = (CmsPrincipal)CmsPrincipal.readPrincipal(cms, new CmsUUID(id));
                if (OpenCms.getDefaultUsers().isDefaultUser(principal.getName())
                    || OpenCms.getDefaultUsers().isDefaultGroup(principal.getName())) {
                    defaultUser = true;
                    continue;
                }
                infos.add(CmsAccountsApp.getPrincipalInfo(CmsPrincipal.readPrincipal(cms, new CmsUUID(id))));
                if (principal instanceof CmsGroup) {
                    m_groupIDs.add(new CmsUUID(id));
                } else {
                    m_userIDs.add(new CmsUUID(id));
                }
            } catch (CmsException e) {
                LOG.error("Unable to read Principal.", e);
            }
        }

        displayResourceInfoDirectly(infos);
        String labelMessage = m_userIDs.isEmpty()
        ? Messages.GUI_USERMANAGEMENT_GROUP_DELETE_MULTIPLE_0
        : Messages.GUI_USERMANAGEMENT_USER_DELETE_MULTIPLE_0;
        m_label_deleteDefault.setVisible(defaultUser && m_userIDs.isEmpty() && m_groupIDs.isEmpty());
        m_label.setValue(CmsVaadinUtils.getMessageText(labelMessage));
        CmsResourceInfoTable table = new CmsResourceInfoTable(m_cms, m_userIDs, m_groupIDs);
        table.setHeight("300px");
        table.setWidth("100%");
        m_dependencyPanel.setVisible(table.size() > 0);
        m_principalSelectLayout.setVisible(table.size() > 0);
        m_okButton.setVisible(!m_userIDs.isEmpty() || !m_groupIDs.isEmpty());
        m_deleteConfirm.setVisible(!m_userIDs.isEmpty() || !m_groupIDs.isEmpty());
        if (m_dependencyPanel.isVisible()) {
            m_dependencyPanel.setContent(table);
            m_principalSelect.setRealPrincipalsOnly(true);

            if ((m_userIDs.size() == 0) | (m_groupIDs.size() == 0)) {
                m_principalSelect.setWidgetType(m_userIDs.size() > 0 ? WidgetType.userwidget : WidgetType.groupwidget);
                m_principalSelect.setPrincipalType(
                    m_userIDs.size() > 0 ? I_CmsPrincipal.PRINCIPAL_USER : I_CmsPrincipal.PRINCIPAL_GROUP);
            } else {
                m_principalSelect.setWidgetType(WidgetType.principalwidget);
            }

        }
    }

    /**
     * Deletes the given user.<p>
     */
    protected void deletePrincipal() {

        try {
            String principalNameToCopyTo = m_principalSelect.getValue();
            I_CmsPrincipal principalTarget = null;
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(principalNameToCopyTo)) {
                principalTarget = CmsPrincipal.readPrincipal(m_cms, principalNameToCopyTo);
            }

            for (CmsUUID id : m_groupIDs) {
                m_cms.deleteGroup(id, principalTarget != null ? principalTarget.getId() : null);
            }
            for (CmsUUID id : m_userIDs) {
                m_cms.deleteUser(id, principalTarget != null ? principalTarget.getId() : null);
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
     * @param app
     */
    private void init(CmsObject cms, final Window window, final CmsAccountsApp app) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_icon.setContentMode(ContentMode.HTML);
        m_icon.setValue(FontOpenCms.WARNING.getHtml());
        m_icon2.setContentMode(ContentMode.HTML);
        m_icon2.setValue(FontOpenCms.WARNING.getHtml());
        m_label_deleteDefault.setVisible(false);
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
