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
import org.opencms.setup.CmsSetupUI;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;

import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * Setup step: Configuration notes.
 */
public class CmsSetupStep07ConfigNotes extends A_CmsSetupStep {

    /** The main layout. */
    private VerticalLayout m_mainLayout;

    /** The forward button. */
    private Button m_forwardButton;

    /** The panel for displaying the notes. */
    private Panel m_htmlPanel;

    /**
     * Creates a new instance.
     *
     * @param context the setup context
     */
    public CmsSetupStep07ConfigNotes(I_SetupUiContext context) {

        super(context);

        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);
        BrowserFrame frame = new BrowserFrame();
        String name = "browser_config.html";
        Resource resource = CmsSetupUI.getSetupPage(context, name);
        frame.setSource(resource);
        frame.setWidth("100%");
        frame.setHeight("100%");
        m_htmlPanel.setContent(frame);

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
        if (!servletMapping.startsWith("/")) {
            servletMapping = "/" + servletMapping;
        }
        if (servletMapping.endsWith("/*")) {
            // usually a mapping must be in the form "/opencms/*", cut off all slashes
            servletMapping = servletMapping.substring(0, servletMapping.length() - 2);
        }
        String openLink = request.getContextPath() + servletMapping + "/index.html";
        A_CmsUI.get().getPage().setLocation(openLink);
    }

    /**
     * @see org.opencms.setup.ui.A_CmsSetupStep#getTitle()
     */
    @Override
    public String getTitle() {

        return "Review configuration notes";
    }

}
