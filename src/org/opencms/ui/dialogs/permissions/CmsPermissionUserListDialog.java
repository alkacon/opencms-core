/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.dialogs.permissions;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.user.CmsAccountsAppConfiguration;
import org.opencms.ui.components.CmsBasicDialog;

import java.util.HashMap;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;

/**
 * Shows user management screen for a specific group in an IFrame.
 */
public class CmsPermissionUserListDialog extends CmsBasicDialog {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The close button. */
    Button m_closeButton;

    /** The iframe. */
    BrowserFrame m_frame;

    public CmsPermissionUserListDialog(CmsObject cms, CmsGroup group) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), new HashMap<>());
        m_closeButton.addClickListener(event -> {
            CmsVaadinUtils.closeWindow(CmsPermissionUserListDialog.this);

        });
        m_frame.setWidth("100%");
        int height = A_CmsUI.get().getPage().getBrowserWindowHeight();
        height = Math.max(200, height - 150);
        m_frame.setHeight(height + "px");

        String id = "" + group.getId();
        String prefix = "g";
        if (0 != (group.getFlags() & 131072)) { // for OCEE
            prefix = "G";
        }
        String t = prefix + "!!" + group.getOuFqn() + "!!" + id + "!!";
        String target = CmsVaadinUtils.getWorkplaceLink(CmsAccountsAppConfiguration.APP_ID) + "/" + t;
        m_frame.setSource(new ExternalResource(target));
    }

}
