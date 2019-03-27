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
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Setup step: Configuration notes.
 */
public class CmsSetupStep07ConfigNotes extends A_CmsSetupStep {

    /** The forward button. */
    private Button m_forwardButton;

    /** The main layout. */
    private VerticalLayout m_mainLayout;

    private VerticalLayout m_notesContainer;

    /**
     * Creates a new instance.
     *
     * @param context the setup context
     */
    public CmsSetupStep07ConfigNotes(I_SetupUiContext context) {

        super(context);

        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);
        String name = "browser_config.html";
        Label label = htmlLabel(readSnippet(name));
        label.setWidth("100%");
        m_notesContainer.addComponent(label);
        m_forwardButton.addClickListener(evt -> forward());
    }

    /**
     * Proceed to next step.
     */
    public void forward() {

        m_context.getSetupBean().prepareStep10();
        CmsSetupBean bean = m_context.getSetupBean();
        VaadinServletRequest request = (VaadinServletRequest)(VaadinRequest.getCurrent());
        String servletMapping = bean.getServletMapping();
        String openLink = null;

        if (!servletMapping.startsWith("/")) {
            servletMapping = "/" + servletMapping;
        }
        if (servletMapping.endsWith("/*")) {
            // usually a mapping must be in the form "/opencms/*", cut off all slashes
            servletMapping = servletMapping.substring(0, servletMapping.length() - 2);
        }
        openLink = request.getContextPath() + servletMapping + (bean.hasIndexHtml() ? "/index.html" : "/system/login");

        A_CmsUI.get().getPage().setLocation(openLink);
    }

}
