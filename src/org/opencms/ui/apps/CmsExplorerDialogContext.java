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
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsDialogContext;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsEditPropertyContext;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * Dialog context for the explorer.<p>
 */
public class CmsExplorerDialogContext extends A_CmsDialogContext implements I_CmsEditPropertyContext {

    /** The explorer instance. */
    private CmsFileExplorer m_explorer;

    /**
     * Creates a new instance.<p>
     *
     * @param appContext the app context
     * @param explorer the explorer app instance
     * @param resources the list of selected resources
     */
    public CmsExplorerDialogContext(
        I_CmsAppUIContext appContext,
        CmsFileExplorer explorer,
        List<CmsResource> resources) {
        super(appContext, resources);
        m_explorer = explorer;
    }

    /**
     * @see org.opencms.ui.I_CmsEditPropertyContext#editProperty(java.lang.Object)
     */
    public void editProperty(Object propertyId) {

        m_explorer.editItemProperty(getResources().get(0).getStructureId(), (CmsResourceTableProperty)propertyId);
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
     * @see org.opencms.ui.I_CmsEditPropertyContext#isPropertyEditable(java.lang.Object)
     */
    public boolean isPropertyEditable(Object propertyId) {

        return (getResources().size() == 1)
            && (propertyId instanceof CmsResourceTableProperty)
            && m_explorer.isPropertyEditable((CmsResourceTableProperty)propertyId);
    }
}
