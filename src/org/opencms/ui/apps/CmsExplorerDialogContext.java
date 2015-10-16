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

package org.opencms.ui.apps;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.contextmenu.I_CmsContextMenuItem;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

/**
 * Dialog context for the explorer.<p>
 */
public class CmsExplorerDialogContext implements I_CmsDialogContext {

    /** Logger instance for this class. */
    @SuppressWarnings("unused")
    private static final Log LOG = CmsLog.getLog(CmsExplorerDialogContext.class);

    /** The explorer app context. */
    private I_CmsAppUIContext m_appContext;

    /** The explorer instance. */
    private CmsFileExplorer m_explorer;

    /** The current context menu item. */
    private I_CmsContextMenuItem m_item;

    /** List of selected resources. */
    private List<CmsResource> m_resources;

    /** The window used to display the dialog. */
    private Window m_window;

    /**
     * Creates a new instance.<p>
     *
     * @param appContext the app context
     * @param explorer the explorer app instance
     * @param resources the list of selected resources
     * @param item the context menu item for which this context is created
     */
    public CmsExplorerDialogContext(
        I_CmsAppUIContext appContext,
        CmsFileExplorer explorer,
        List<CmsResource> resources,
        I_CmsContextMenuItem item) {
        m_resources = resources;
        m_appContext = appContext;
        m_explorer = explorer;
        m_item = item;
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
     * @see org.opencms.ui.I_CmsDialogContext#finish(java.util.Collection)
     */
    public void finish(Collection<CmsUUID> ids) {

        if (m_window != null) {
            m_window.close();
            m_window = null;
        }
        if (ids == null) {
            ids = Lists.newArrayList();
            for (CmsResource res : getResources()) {
                ids.add(res.getStructureId());
            }
        }
        for (CmsUUID id : ids) {
            if (id.isNullUUID()) {
                m_explorer.updateAll();
            }
        }
        m_explorer.update(ids);
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#focus(org.opencms.util.CmsUUID)
     */
    public void focus(CmsUUID cmsUUID) {

        try {
            CmsObject cms = A_CmsUI.getCmsObject();
            CmsResource res = cms.readResource(cmsUUID, CmsResourceFilter.ALL);
            String rootPath = res.getRootPath();
            String siteRoot = OpenCms.getSiteManager().getSiteRoot(rootPath);
            if (siteRoot == null) {
                if (OpenCms.getSiteManager().startsWithShared(rootPath)) {
                    siteRoot = OpenCms.getSiteManager().getSharedFolder();
                } else {
                    siteRoot = "";
                }
            }
            CmsObject otherSiteCms = OpenCms.initCmsObject(cms);
            otherSiteCms.getRequestContext().setSiteRoot(siteRoot);
            String sitePath = otherSiteCms.getRequestContext().removeSiteRoot(CmsResource.getParentFolder(rootPath));
            m_explorer.changeSite(siteRoot, sitePath, true);
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);

        }
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
     * @see org.opencms.ui.I_CmsDialogContext#getMenuItem()
     */
    public I_CmsContextMenuItem getMenuItem() {

        return m_item;
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#getResources()
     */
    public List<CmsResource> getResources() {

        return m_resources;
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
