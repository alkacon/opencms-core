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

import org.opencms.ui.components.CmsExtendedSiteSelector.SiteSelectorOption;
import org.opencms.util.CmsUUID;

import java.util.Optional;

import com.vaadin.ui.Component;

/**
 * Interface the favorite dialog uses to interact with the rest of the application.
 */
public interface I_CmsFavoriteContext {

    /**
     * Change the project to one with the given id.
     *
     * @param id the project id
     */
    public void changeProject(CmsUUID id);

    /**
     * Changes current site.
     *
     * @param option the site selector option
     */
    public void changeSite(SiteSelectorOption option);

    /**
     * Sets the dialog instance.<p>
     *
     * This must be called by the favorite dialog when it is loaded.
     *
     * @param component the favorite dialog
     */
    public void setDialog(Component component);

    /**
     * Closes the favorite dialog.
     */
    void close();

    /**
     * Gets the favorite entry for the current location, as an Optional.
     *
     * If the result is empty, the current location can not be used as a favorite.
     *
     * @return an optional favorite entry
     */
    Optional<CmsFavoriteEntry> getFavoriteForCurrentLocation();

    /**
     * Opens the favorite location.
     *
     * @param entry the favorite entry whose location should be opened
     */
    void openFavorite(CmsFavoriteEntry entry);

}
