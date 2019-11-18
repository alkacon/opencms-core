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

package org.opencms.ui.favorites;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.apps.CmsExplorerDialogContext;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsExtendedSiteSelector.SiteSelectorOption;
import org.opencms.ui.dialogs.CmsProjectSelectDialog;
import org.opencms.ui.favorites.CmsFavoriteEntry.Type;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Optional;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Component;

/**
 * Context for the favorite dialog opened from the workplace.
 */
public class CmsExplorerFavoriteContext implements I_CmsFavoriteContext {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExplorerFavoriteContext.class);

    /** Current dialog instance. */
    private Component m_dialog;

    /** Favorite entry for current location. */
    private CmsFavoriteEntry m_entry;

    /** The active explorer instance. */
    private CmsFileExplorer m_explorer;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the current CMS context
     * @param explorer the current file explorer instance
     */
    public CmsExplorerFavoriteContext(CmsObject cms, CmsFileExplorer explorer) {

        if (explorer != null) {
            m_explorer = explorer;
            CmsUUID currentFolder = explorer.getCurrentFolder();
            String siteRoot = cms.getRequestContext().getSiteRoot();
            CmsUUID project = cms.getRequestContext().getCurrentProject().getId();
            CmsFavoriteEntry entry = new CmsFavoriteEntry();
            entry.setType(Type.explorerFolder);
            entry.setSiteRoot(siteRoot);
            entry.setStructureId(currentFolder);
            entry.setProjectId(project);
            m_entry = entry;
        }
    }

    /**
     * @see org.opencms.ui.favorites.I_CmsFavoriteContext#changeProject(org.opencms.util.CmsUUID)
     */
    public void changeProject(CmsUUID value) {

        close();
        CmsExplorerDialogContext context = new CmsExplorerDialogContext(
            ContextType.fileTable,
            null,
            m_explorer,
            new ArrayList<>());
        CmsProjectSelectDialog.changeSiteOrProject(context, value, null);
    }

    /**
     * @see org.opencms.ui.favorites.I_CmsFavoriteContext#changeSite(org.opencms.ui.components.CmsExtendedSiteSelector.SiteSelectorOption)
     */
    public void changeSite(SiteSelectorOption value) {

        close();
        CmsExplorerDialogContext context = new CmsExplorerDialogContext(
            ContextType.fileTable,
            null,
            m_explorer,
            new ArrayList<>());
        CmsProjectSelectDialog.changeSiteOrProject(context, null, value);
    }

    /**
     * @see org.opencms.ui.favorites.I_CmsFavoriteContext#close()
     */
    public void close() {

        CmsVaadinUtils.getWindow(m_dialog).close();
    }

    /**
     * @see org.opencms.ui.favorites.I_CmsFavoriteContext#getFavoriteForCurrentLocation()
     */
    public Optional<CmsFavoriteEntry> getFavoriteForCurrentLocation() {

        return Optional.ofNullable(m_entry);
    }

    /**
     * @see org.opencms.ui.favorites.I_CmsFavoriteContext#openFavorite(org.opencms.ui.favorites.CmsFavoriteEntry)
     */
    public void openFavorite(CmsFavoriteEntry entry) {

        try {
            String url = entry.updateContextAndGetFavoriteUrl(A_CmsUI.getCmsObject());
            close(); // necessary for the case where we already are at the location of the entry
            A_CmsUI.get().getPage().open(url, null);
        } catch (Exception e) {
            CmsErrorDialog.showErrorDialog(e);
        }
    }

    /**
     * @see org.opencms.ui.favorites.I_CmsFavoriteContext#setDialog(com.vaadin.ui.Component)
     */
    public void setDialog(Component component) {

        m_dialog = component;
    }

}
