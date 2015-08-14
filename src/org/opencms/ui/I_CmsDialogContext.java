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
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.vaadin.ui.Component;

/**
 * Context for dialogs opened from the context menu.<p>
 */
public interface I_CmsDialogContext {

    /**
     * Signals an error which occurred in the dialog.<p>
     *
     * @param error the error which occcurred
     */
    public void error(Throwable error);

    /**
     * Signals that the dialog has finished.<p>
     *
     * @param result the list of structure ids of changed resources
     */
    public void finish(List<CmsUUID> result);

    /**
     * Returns the app UI context.<p>
     *
     * @return the app UI context
     */
    I_CmsAppUIContext getAppContext();

    /**
     * Gets the CMS context to be used for dialog operations.<p>
     *
     * @return the CMS context
     */
    CmsObject getCms();

    /**
     * Gets the list of resources for which the dialog should be opened.<p>
     *
     * @return the list of resources
     */
    List<CmsResource> getResources();

    /**
     * Called to start up the dialog with the given main widget and title string.<p>
     *
     * @param title the title to display
     * @param dialog the dialog main widget
     */
    void start(String title, Component dialog);

}
