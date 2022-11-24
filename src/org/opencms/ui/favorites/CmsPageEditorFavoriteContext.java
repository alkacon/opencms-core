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
import org.opencms.file.CmsProject;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsExtendedSiteSelector.SiteSelectorOption;
import org.opencms.ui.dialogs.CmsEmbeddedDialogContext;
import org.opencms.ui.dialogs.CmsSiteSelectDialog;
import org.opencms.ui.favorites.CmsFavoriteEntry.Type;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Optional;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Component;

/**
 * Favorite dialog context for the case where the dialog is opened from the page editor,
 * in an iframe.
 */
public class CmsPageEditorFavoriteContext implements I_CmsFavoriteContext {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPageEditorFavoriteContext.class);

    /** The dialog context. */
    private CmsEmbeddedDialogContext m_dialogContext;

    /** The current favorite location. */
    private CmsFavoriteEntry m_entry;

    /**
     * Creates a new instance.
     *
     * @param context the embedded dialog context
     * @param req the current request
     */
    public CmsPageEditorFavoriteContext(CmsEmbeddedDialogContext context) {

        CmsUUID detailId = toUuid(context.getParameters().get(CmsGwtConstants.Favorites.PARAM_DETAIL));
        CmsUUID pageId = toUuid(context.getParameters().get(CmsGwtConstants.Favorites.PARAM_PAGE));
        CmsUUID project = toUuid(context.getParameters().get(CmsGwtConstants.Favorites.PARAM_PROJECT));
        String siteRoot = context.getParameters().get(CmsGwtConstants.Favorites.PARAM_SITE);
        CmsFavoriteEntry entry = new CmsFavoriteEntry();
        entry.setDetailId(detailId);
        entry.setProjectId(project);
        entry.setSiteRoot(siteRoot);
        entry.setStructureId(pageId);
        entry.setType(Type.page);
        m_entry = entry;
        m_dialogContext = context;
    }

    /**
     * Converts string to UUID and returns it, or null if the conversion is not possible.
     *
     * @param uuid the potential UUID string
     * @return the UUID, or null if conversion is not possible
     */
    private static CmsUUID toUuid(String uuid) {

        if ("null".equals(uuid) || CmsStringUtil.isEmpty(uuid)) {
            return null;
        }
        return new CmsUUID(uuid);

    }

    /**
     * @see org.opencms.ui.favorites.I_CmsFavoriteContext#changeProject(org.opencms.util.CmsUUID)
     */
    public void changeProject(CmsUUID value) {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            CmsProject project = cms.readProject(value);
            close();
            A_CmsUI.get().changeProject(project);
            m_dialogContext.finish(project, null);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            CmsErrorDialog.showErrorDialog(e);
        }

    }

    /**
     * @see org.opencms.ui.favorites.I_CmsFavoriteContext#changeSite(org.opencms.ui.components.CmsExtendedSiteSelector.SiteSelectorOption)
     */
    public void changeSite(SiteSelectorOption value) {

        CmsSiteSelectDialog.changeSite(m_dialogContext, value);
    }

    /**
     * @see org.opencms.ui.favorites.I_CmsFavoriteContext#close()
     */
    public void close() {

        m_dialogContext.finish(new ArrayList<>());

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
            m_dialogContext.leavePage(url);
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
        }
    }

    /**
     * @see org.opencms.ui.favorites.I_CmsFavoriteContext#setDialog(com.vaadin.ui.Component)
     */
    public void setDialog(Component component) {

        // not needed
    }

}
