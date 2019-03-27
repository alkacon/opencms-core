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

package org.opencms.setup.ui;

import org.opencms.setup.CmsSetupBean;
import org.opencms.ui.CmsVaadinUtils;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;

/**
 * Setup step: Other settings.
 */
public class CmsSetupStep05ServerSettings extends A_CmsSetupStep {

    /** Back button. */
    private Button m_backButton;

    /** Forward button. */
    private Button m_forwardButton;

    /** MAC address. */
    private TextField m_macAddress;

    /** Server id. */
    private TextField m_serverId;

    /** Workplace server. */
    private TextField m_serverUrl;

    /**
     * Creates a new instance.
     *
     * @param context the setup context
     */
    public CmsSetupStep05ServerSettings(I_SetupUiContext context) {

        super(context);

        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);
        m_serverId.setValue(m_context.getSetupBean().getServerName());
        m_macAddress.setValue(m_context.getSetupBean().getEthernetAddress());
        VaadinServletRequest request = (VaadinServletRequest)(VaadinRequest.getCurrent());
        String serverUrl = request.getScheme() + "://" + request.getServerName();
        int serverPort = request.getServerPort();
        if (serverPort != 80) {
            serverUrl += ":" + serverPort;
        }
        m_serverUrl.setValue(serverUrl);
        m_forwardButton.addClickListener(evt -> forward());
        m_backButton.addClickListener(evt -> m_context.stepBack());
    }

    /**
     * Proceed to next step.
     */
    private void forward() {

        try {
            String macAddress = m_macAddress.getValue();
            String serverId = m_serverId.getValue();
            String serverUrl = m_serverUrl.getValue();
            CmsSetupBean setupBean = m_context.getSetupBean();
            setupBean.setEthernetAddress(macAddress);
            setupBean.setServerName(serverId);
            setupBean.setWorkplaceSite(serverUrl);
            setupBean.prepareStep8();
            m_context.stepForward();
        } catch (Exception e) {
            CmsSetupErrorDialog.showErrorDialog(e);

        }

    }

}
