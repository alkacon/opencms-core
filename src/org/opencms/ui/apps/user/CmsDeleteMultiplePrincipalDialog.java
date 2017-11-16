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
import org.opencms.security.CmsPrincipal;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.A_CmsUI;
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

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;

/**
 * Dialog for delete multiple principal.<p>
 */
public class CmsDeleteMultiplePrincipalDialog extends CmsBasicDialog {

    /**vaadin serial id. */
    private static final long serialVersionUID = -1191281655158071555L;

    /**The icon. */
    private Label m_icon;

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

    /**vaadin component. */
    private CmsPrincipalSelect m_principalSelect;

    /**vaadin component. */
    private FormLayout m_principalSelectLayout;

    /**
     * Public constructor.<p>
     *
     * @param cms CmsObeject
     * @param context ids of principal to delete
     * @param window window
     */
    public CmsDeleteMultiplePrincipalDialog(CmsObject cms, Set<String> context, Window window) {
        init(cms, window);
        m_ids = context;
        m_groupIDs = new HashSet<CmsUUID>();
        m_userIDs = new HashSet<CmsUUID>();
        for (String id : m_ids) {
            try {
                if (CmsPrincipal.readPrincipal(cms, new CmsUUID(id)) instanceof CmsGroup) {
                    m_groupIDs.add(new CmsUUID(id));
                } else {
                    m_userIDs.add(new CmsUUID(id));
                }
            } catch (CmsException e) {
                //
            }
        }
        try {
            List<CmsResourceInfo> infos = new ArrayList<CmsResourceInfo>();
            for (String id : context) {
                infos.add(CmsAccountsApp.getPrincipalInfo(CmsPrincipal.readPrincipal(cms, new CmsUUID(id))));
            }
            displayResourceInfoDirectly(infos);
            String labelMessage = m_userIDs.isEmpty()
            ? Messages.GUI_USERMANAGEMENT_GROUP_DELETE_MULTIPLE_0
            : Messages.GUI_USERMANAGEMENT_USER_DELETE_MULTIPLE_0;
            m_label.setValue(CmsVaadinUtils.getMessageText(labelMessage));
            CmsResourceInfoTable table = new CmsResourceInfoTable(m_cms, m_userIDs, m_groupIDs);
            table.setHeight("300px");
            table.setWidth("100%");
            m_dependencyPanel.setVisible(table.size() > 0);
            m_principalSelectLayout.setVisible(table.size() > 0);
            if (m_dependencyPanel.isVisible()) {
                m_dependencyPanel.setContent(table);
                m_principalSelect.setRealPrincipalsOnly(true);

                if ((m_userIDs.size() == 0) | (m_groupIDs.size() == 0)) {
                    m_principalSelect.setWidgetType(
                        m_userIDs.size() > 0 ? WidgetType.userwidget : WidgetType.groupwidget);
                    m_principalSelect.setPrincipalType(
                        m_userIDs.size() > 0 ? I_CmsPrincipal.PRINCIPAL_USER : I_CmsPrincipal.PRINCIPAL_GROUP);
                } else {
                    m_principalSelect.setWidgetType(WidgetType.principalwidget);
                }

            }
        } catch (CmsException e) {
            //
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
            //
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
