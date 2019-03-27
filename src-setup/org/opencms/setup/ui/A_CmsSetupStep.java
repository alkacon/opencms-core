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
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.FileInputStream;
import java.io.InputStream;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;

/**
 * Abstract base class for setup dialog steps.
 */
public class A_CmsSetupStep extends CmsBasicDialog {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * The setup context.
     */
    protected I_SetupUiContext m_context;

    /**
     * Constructor.
     *
     * @param context the setup context
     */
    public A_CmsSetupStep(I_SetupUiContext context) {

        super();
        m_context = context;
    }

    /**
     * Gets the title for the setup step.
     *
     * @return the title
     */
    public String getTitle() {

        return "OpenCms setup";
    }

    /**
     * Creates a new HTML-formatted label with the given content.
     *
     * @param html the label content
     */
    public Label htmlLabel(String html) {

        Label label = new Label();
        label.setContentMode(ContentMode.HTML);
        label.setValue(html);
        return label;

    }

    /**
     * Reads an HTML snippet with the given name.
     *
     * @return the HTML data
     */
    public String readSnippet(String name) {

        String path = CmsStringUtil.joinPaths(
            m_context.getSetupBean().getWebAppRfsPath(),
            CmsSetupBean.FOLDER_SETUP,
            "html",
            name);
        try (InputStream stream = new FileInputStream(path)) {
            byte[] data = CmsFileUtil.readFully(stream, false);
            String result = new String(data, "UTF-8");
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Makes max-height behavior easy to turn on/off in subclasses.
     */
    @Override
    protected void enableMaxHeight() {

        if (isEnableMaxHeight()) {
            super.enableMaxHeight();
        }
    }

    /**
     * If true, max-height resizing behavior is enabled.
     *
     * @return true if max-height resizing should be enabled
     */
    protected boolean isEnableMaxHeight() {

        return true;
    }

}
