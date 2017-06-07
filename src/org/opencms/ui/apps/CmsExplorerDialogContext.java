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

package org.opencms.ui.apps;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsFileTableDialogContext;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.List;

/**
 * Dialog context for the explorer.<p>
 */
public class CmsExplorerDialogContext extends CmsFileTableDialogContext {

    /** The explorer instance. */
    private CmsFileExplorer m_explorer;

    /**
     * Creates a new instance.<p>
     *
     * @param contextType the context type
     * @param fileTable the file table
     * @param explorer the explorer app instance
     * @param resources the list of selected resources
     */
    public CmsExplorerDialogContext(
        ContextType contextType,
        CmsFileTable fileTable,
        CmsFileExplorer explorer,
        List<CmsResource> resources) {
        super(CmsFileExplorerConfiguration.APP_ID, contextType, fileTable, resources);
        m_explorer = explorer;
    }

    /**
     * @see org.opencms.ui.A_CmsDialogContext#finish(org.opencms.file.CmsProject, java.lang.String)
     */
    @Override
    public void finish(CmsProject project, String siteRoot) {

        finish(null);
        m_explorer.onSiteOrProjectChange(project, siteRoot);
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#finish(java.util.Collection)
     */
    @Override
    public void finish(Collection<CmsUUID> ids) {

        closeWindow();
        CmsAppWorkplaceUi.get().enableGlobalShortcuts();
        if (ids != null) {
            for (CmsUUID id : ids) {
                if (id.isNullUUID()) {
                    m_explorer.updateAll(false);
                    return;
                }
            }
            m_explorer.update(ids);
        }
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#focus(org.opencms.util.CmsUUID)
     */
    @Override
    public void focus(CmsUUID cmsUUID) {

        try {
            CmsObject cms = A_CmsUI.getCmsObject();
            CmsResource res = cms.readResource(cmsUUID, CmsResourceFilter.ALL);
            String rootPath = res.getRootPath();
            String siteRoot = OpenCms.getSiteManager().getSiteRoot(rootPath);
            String sitePath = null;
            if (siteRoot == null) {
                if (OpenCms.getSiteManager().startsWithShared(rootPath)) {
                    siteRoot = OpenCms.getSiteManager().getSharedFolder();
                    sitePath = CmsResource.getParentFolder(
                        rootPath.substring(OpenCms.getSiteManager().getSharedFolder().length()));
                } else {
                    sitePath = CmsResource.getParentFolder(rootPath);
                    siteRoot = "";
                }
            } else {
                CmsObject otherSiteCms = OpenCms.initCmsObject(cms);
                otherSiteCms.getRequestContext().setSiteRoot(siteRoot);
                sitePath = otherSiteCms.getRequestContext().removeSiteRoot(CmsResource.getParentFolder(rootPath));
            }
            m_explorer.changeSite(siteRoot, sitePath, true);
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
        }
    }

    /**
     * @see org.opencms.ui.components.CmsFileTableDialogContext#updateUserInfo()
     */
    @Override
    public void updateUserInfo() {

        m_explorer.m_appContext.updateUserInfo();
    }
}
