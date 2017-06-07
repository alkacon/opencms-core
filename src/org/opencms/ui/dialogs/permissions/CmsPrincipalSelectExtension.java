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

package org.opencms.ui.dialogs.permissions;

import org.opencms.ui.A_CmsUI;
import org.opencms.ui.shared.rpc.I_CmsPrincipalSelectRpc;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

/**
 * The principal select extension. Handles communication between the select dialog iframe and the server.<p>
 */
public class CmsPrincipalSelectExtension extends AbstractExtension implements I_CmsPrincipalSelectRpc {

    /** The serial version id. */
    private static final long serialVersionUID = -38736017917448694L;

    /** The select widget. */
    private CmsPrincipalSelect m_select;

    /**
     * Constructor.<p>
     *
     * @param ui the select widget
     */
    private CmsPrincipalSelectExtension(UI ui) {
        extend(ui);
        registerRpc(this);
    }

    /**
     * Returns the principal select extension instance for the current UI.<p>
     *
     * @return the instance
     */
    protected static CmsPrincipalSelectExtension getInstance() {

        A_CmsUI ui = A_CmsUI.get();
        CmsPrincipalSelectExtension instance = (CmsPrincipalSelectExtension)ui.getAttribute(
            CmsPrincipalSelectExtension.class.getName());
        if (instance == null) {
            instance = new CmsPrincipalSelectExtension(ui);
            ui.setAttribute(CmsPrincipalSelectExtension.class.getName(), instance);
        }
        return instance;
    }

    /**
     * Sets the current select widget.<p>
     * This needs to be called, when the select window is opened.<p>
     *
     * @param select the select widget
     */
    public void setCurrentSelect(CmsPrincipalSelect select) {

        m_select = select;
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsPrincipalSelectRpc#setPrincipal(int, java.lang.String)
     */
    public void setPrincipal(int type, String principalName) {

        if (m_select != null) {
            m_select.setPrincipal(type, principalName);
            m_select.closeWindow();
            m_select = null;
        }
    }
}
