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
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.List;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

/**
 * Context for dialogs opened from the context menu.<p>
 */
public interface I_CmsDialogContext {

    /**
     * The available context types.<p>
     */
    enum ContextType {
        /** The app toolbar context. */
        appToolbar,

        /** The container page toolbar context. */
        containerpageToolbar,

        /** The file table context. */
        fileTable,

        /** The sitemap toolbar context. */
        sitemapToolbar
    }

    /**
     * Signals an error which occurred in the dialog.<p>
     *
     * @param error the error which occcurred
     */
    void error(Throwable error);

    /**
     * Signals that the dialog has finished.<p>
     * Call when current project and or site have been changed.<p>
     *
     * @param project changed project
     * @param siteRoot changed site root
     */
    void finish(CmsProject project, String siteRoot);

    /**
     * Signals that the dialog has finished.<p>
     *
     * @param result the list of structure ids of changed resources
     */
    void finish(Collection<CmsUUID> result);

    /**
     * Tell the system that the resource with the given id should be shown somehow.<p>
     *
     * @param structureId the structure id of a resource
     */
    void focus(CmsUUID structureId);

    /**
     * Gets a list of structure ids of all visible resources, not just the ones selected for the dialog.<p>
     *
     * @return the structure ids of all the resources in the current view
     */
    List<CmsUUID> getAllStructureIdsInView();

    /**
     * Returns the app id.<p>
     *
     * @return the app id
     */
    String getAppId();

    /**
     * Gets the CMS context to be used for dialog operations.<p>
     *
     * @return the CMS context
     */
    CmsObject getCms();

    /**
     * Returns the context type.<p>
     * May be used for visibility evaluation.<p>
     *
     * @return the context type
     */
    ContextType getContextType();

    /**
     * Gets the list of resources for which the dialog should be opened.<p>
     *
     * @return the list of resources
     */
    List<CmsResource> getResources();

    /**
     * Navigates to the given app.<p>
     *
     * @param appId the app id
     */
    void navigateTo(String appId);

    /**
     * Call when the dialog view has changed to re-center the dialog window.<p>
     */
    void onViewChange();

    /**
     * Reloads the UI.<p>
     */
    void reload();

    /**
     * Sets the current window.<p>
     *
     * @param window the current dialog window
     */
    void setWindow(Window window);

    /**
     * Called to start up the dialog with the given main widget and title string.<p>
     *
     * @param title the title to display
     * @param dialog the dialog main widget
     */
    void start(String title, Component dialog);

    /**
     * Called to start up the dialog with the given main widget and title string.<p>
     *
     * @param title the title to display
     * @param dialog the dialog main widget
     * @param width the preferred width for the dialog
     */
    void start(String title, Component dialog, DialogWidth width);

    /**
     * Called when the user info was changed.<p>
     */
    void updateUserInfo();

}
