/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsErrorDialog;

import java.util.List;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

/**
 * Abstract dialog context.<p>
 */
public abstract class A_CmsDialogContext implements I_CmsDialogContext {

    /** The app context. */
    private I_CmsAppUIContext m_appContext;

    /** The list of resources. */
    private List<CmsResource> m_resources;

    /** The window used to display the dialog. */
    protected Window m_window;

    /**
     * Constructor.<p>
     *
     * @param appContext the app context
     * @param resources the list of resources
     */
    protected A_CmsDialogContext(I_CmsAppUIContext appContext, List<CmsResource> resources) {
        m_appContext = appContext;
        m_resources = resources;
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#error(java.lang.Throwable)
     */
    public void error(Throwable error) {

        if (m_window != null) {
            m_window.close();
            m_window = null;
        }
        CmsErrorDialog.showErrorDialog(error, new Runnable() {

            public void run() {

                finish(null);
            }
        });
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#getAppContext()
     */
    public I_CmsAppUIContext getAppContext() {

        return m_appContext;
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#getCms()
     */
    public CmsObject getCms() {

        return A_CmsUI.getCmsObject();
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#getResources()
     */
    public List<CmsResource> getResources() {

        return m_resources;
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#reload()
     */
    public void reload() {

        if (m_window != null) {
            m_window.close();
            m_window = null;
        }
        CmsAppWorkplaceUi.get().reload();
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#start(java.lang.String, com.vaadin.ui.Component)
     */
    public void start(String title, Component dialog) {

        if (dialog != null) {
            m_window = CmsBasicDialog.prepareWindow();
            m_window.setCaption(title);
            m_window.setContent(dialog);
            A_CmsUI.get().addWindow(m_window);
        }
    }
}
