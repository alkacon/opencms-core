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

package org.opencms.ui.components;

import org.opencms.main.CmsLog;
import org.opencms.ui.CmsVaadinUtils;

import java.io.InputStream;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;

import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.JavaScript;

/**
 * Layout which looks like a Vaadin window without actually being a window.<p>
 */
public class CmsFakeWindow extends CustomLayout {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFakeWindow.class);

    /** The content location. */
    public static final String LOCATION_CONTENT = "content";

    /** The window title. */
    private String m_windowTitle;

    /**
     * Creates a new instance.<p>
     */
    public CmsFakeWindow() {

        super();

        // Need this for setWindowTitle, see below
        setId(RandomStringUtils.randomAlphabetic(8));

        try {
            @SuppressWarnings("resource")
            InputStream layoutStream = CmsVaadinUtils.readCustomLayout(getClass(), "CmsFakeWindow.html");
            initTemplateContentsFromInputStream(layoutStream);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Gets the window title.<p>
     *
     * @return the window title
     */
    public String getWindowTitle() {

        return m_windowTitle;
    }

    /**
     * Sets the window title.<p>
     *
     * @param title the new window title
     */
    public void setWindowTitle(String title) {

        /* HACK: Using a Label destroys the layout for some reason, so we resort to setting the caption directly in the
         element via an explicit JavaScript call. */
        m_windowTitle = title;
        JavaScript.eval(
            "document.querySelector('#"
                + getId()
                + " .fakewindowheader').innerHTML = '"
                + StringEscapeUtils.escapeJavaScript(title)
                + "'");
    }

}
