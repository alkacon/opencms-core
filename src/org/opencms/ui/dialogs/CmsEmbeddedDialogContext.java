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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

/**
 * Context for dialogs embedded into plain GWT modules.<p>
 */
public class CmsEmbeddedDialogContext extends AbstractExtension implements I_CmsDialogContext {

    /** The serial version id. */
    private static final long serialVersionUID = -7446784547935775629L;

    /** Keeps the dialog frame on window close. */
    private boolean m_keepFrameOnClose;

    /** The list of resources. */
    private List<CmsResource> m_resources;

    /** The window used to display the dialog. */
    private Window m_window;

    /**
     * Constructor.<p>
     *
     * @param resources the resources
     */
    public CmsEmbeddedDialogContext(List<CmsResource> resources) {
        extend(UI.getCurrent());
        m_resources = resources != null ? resources : Collections.<CmsResource> emptyList();
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#error(java.lang.Throwable)
     */
    public void error(Throwable error) {

        m_keepFrameOnClose = true;
        closeWindow();
        CmsErrorDialog.showErrorDialog(error, new Runnable() {

            public void run() {

                removeDialogFrame();
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

        m_keepFrameOnClose = true;
        closeWindow();
        String resources = "";
        if (result != null) {
            for (CmsUUID id : result) {
                resources += id.toString() + ";";
            }
        }
        getRpcProxy(I_CmsEmbeddedDialogClientRPC.class).finish(resources);
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#focus(org.opencms.util.CmsUUID)
     */
    public void focus(CmsUUID structureId) {

        // does not apply
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#getAllStructureIdsInView()
     */
    public List<CmsUUID> getAllStructureIdsInView() {

        return Collections.emptyList();
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#getAppContext()
     */
    public I_CmsAppUIContext getAppContext() {

        return null;
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

        m_keepFrameOnClose = true;
        closeWindow();
        reloadParent();
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#start(java.lang.String, com.vaadin.ui.Component)
     */
    public void start(String title, Component dialog) {

        start(title, dialog, DialogWidth.narrow);
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#start(java.lang.String, com.vaadin.ui.Component, org.opencms.ui.components.CmsBasicDialog.DialogWidth)
     */
    public void start(String title, Component dialog, DialogWidth width) {

        if (dialog != null) {
            m_window = CmsBasicDialog.prepareWindow(width);
            m_window.setCaption(title);
            m_window.setContent(dialog);
            UI.getCurrent().addWindow(m_window);
            m_window.addCloseListener(new CloseListener() {

                private static final long serialVersionUID = 1L;

                public void windowClose(CloseEvent e) {

                    handleWindowClose();
                }
            });
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

    /**
     * Handles the window close event.<p>
     */
    void handleWindowClose() {

        if (!m_keepFrameOnClose) {
            removeDialogFrame();
        }
    }

    /**
     * Removes the dialog iFrame.<p>
     */
    void removeDialogFrame() {

        getRpcProxy(I_CmsEmbeddedDialogClientRPC.class).finish(null);
    }

    /**
     * Reloads the parent window.<p>
     */
    private void reloadParent() {

        getRpcProxy(I_CmsEmbeddedDialogClientRPC.class).reloadParent();
    }
}
