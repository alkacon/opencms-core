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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPrincipal;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.util.CmsUUID;

import java.util.Collections;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Dialog to show resources with permissions for principle.<p>
 */
public class CmsShowResourcesDialog extends CmsBasicDialog {

    /**Type of Dialog. */
    protected enum DialogType {
        Group, User, Error;
    }

    /**vaadin serial id. */
    private static final long serialVersionUID = -1744033403586325260L;

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsShowResourcesDialog.class);

    /**vaadin component.*/
    private Button m_cancel;

    /**vaadin component.*/
    private VerticalLayout m_layout;

    /**CmsPrincipal. */
    private CmsPrincipal m_principal;

    /**CmsObject. */
    private CmsObject m_cms;

    /**Type of dialog. */
    private DialogType m_type;

    /**
     * public constructor.<p>
     *
     * @param id of principal
     * @param window holding dialog
     */
    public CmsShowResourcesDialog(String id, final Window window) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        iniCmsObject();

        try {
            m_principal = A_CmsUI.getCmsObject().readUser(new CmsUUID(id));
            m_type = DialogType.User;
        } catch (CmsException e) {
            try {
                m_principal = A_CmsUI.getCmsObject().readGroup(new CmsUUID(id));
                m_type = DialogType.Group;
            } catch (CmsException e1) {
                m_type = DialogType.Error;
            }
        }
        displayResourceInfoDirectly(Collections.singletonList(CmsAccountsApp.getPrincipalInfo(m_principal)));
        CmsShowResourceTable table = new CmsShowResourceTable(m_cms, m_principal.getId(), m_type);
        if (table.hasNoEntries()) {
            m_layout.addComponent(
                CmsVaadinUtils.getInfoLayout(
                    org.opencms.ui.apps.Messages.GUI_USERMANAGEMENT_NO_RESOURCES_WITH_PERMISSIONS_0));
        } else {
            m_layout.addComponent(table);
        }
        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -2117164384116082079L;

            public void buttonClick(ClickEvent event) {

                window.close();

            }
        });
    }

    /**
     * Initializes CmsObject.<p>
     */
    private void iniCmsObject() {

        try {
            m_cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            m_cms.getRequestContext().setSiteRoot("");
        } catch (CmsException e) {
            LOG.error("Unable to clone CmsObject", e);
        }
    }

}
