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

package org.opencms.ui;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

/**
 * Abstract dialog context.<p>
 */
public abstract class A_CmsDialogContext implements I_CmsDialogContext {

    /** The window used to display the dialog. */
    protected Window m_window;

    /** The app id. */
    private String m_appId;

    /** The context type. */
    private ContextType m_contextType;

    /** The list of resources. */
    private List<CmsResource> m_resources;

    /**
     * Constructor.<p>
     *
     * @param appId the app id
     * @param contextType the context type, to be used for visibility evaluation
     * @param resources the list of resources
     */
    protected A_CmsDialogContext(String appId, ContextType contextType, List<CmsResource> resources) {
        m_appId = appId;
        m_resources = resources != null ? resources : Collections.<CmsResource> emptyList();
        m_contextType = contextType;
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#error(java.lang.Throwable)
     */
    public void error(Throwable error) {

        closeWindow();
        CmsErrorDialog.showErrorDialog(error, new Runnable() {

            public void run() {

                finish(null);
            }
        });
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#finish(org.opencms.file.CmsProject, java.lang.String)
     */
    public void finish(CmsProject project, String siteRoot) {

        if ((project != null) || (siteRoot != null)) {
            reload();
        } else {
            finish(null);
        }
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#finish(java.util.Collection)
     */
    public void finish(Collection<CmsUUID> result) {

        closeWindow();
        CmsAppWorkplaceUi.get().enableGlobalShortcuts();
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#getAppId()
     */
    public String getAppId() {

        return m_appId;
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#getCms()
     */
    public CmsObject getCms() {

        return A_CmsUI.getCmsObject();
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#getContextType()
     */
    public ContextType getContextType() {

        return m_contextType;
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#getResources()
     */
    public List<CmsResource> getResources() {

        return m_resources;
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#navigateTo(java.lang.String)
     */
    public void navigateTo(String appId) {

        closeWindow();
        A_CmsUI.get().getNavigator().navigateTo(appId);
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#onViewChange()
     */
    public void onViewChange() {

        if (m_window != null) {
            m_window.center();
        }
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#reload()
     */
    public void reload() {

        closeWindow();
        CmsAppWorkplaceUi.get().reload();
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#setWindow(com.vaadin.ui.Window)
     */
    public void setWindow(Window window) {

        m_window = window;
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#start(java.lang.String, com.vaadin.ui.Component)
     */
    public void start(String title, Component dialog) {

        start(title, dialog, DialogWidth.narrow);
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#start(java.lang.String, com.vaadin.ui.Component)
     */
    public void start(String title, Component dialog, DialogWidth style) {

        if (dialog != null) {
            CmsAppWorkplaceUi.get().disableGlobalShortcuts();
            m_window = CmsBasicDialog.prepareWindow(style);
            m_window.setCaption(title);
            m_window.setContent(dialog);
            CmsAppWorkplaceUi.get().addWindow(m_window);
            if (dialog instanceof CmsBasicDialog) {
                ((CmsBasicDialog)dialog).initActionHandler(m_window);
            }
        }
    }

    /**
     * Closes the dialog window.<p>
     */
    protected void closeWindow() {

        if (m_window != null) {
            m_window.close();
            m_window = null;
        }
    }
}
