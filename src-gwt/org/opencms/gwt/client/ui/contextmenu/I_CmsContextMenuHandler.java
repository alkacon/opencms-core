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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.util.CmsUUID;

import java.util.Map;

/**
 * Interface for context menu commands.<p>
 *
 * @since version 8.0.1
 */
public interface I_CmsContextMenuHandler extends I_CmsActionHandler {

    /**
     * Tries to lock the given resource and returns <code>true</code> on success.
     * If not successful a warning should be displayed.<p>
     *
     * @param structureId the structure id of the resource to lock
     *
     * @return <code>true</code> if successful
     */
    boolean ensureLockOnResource(CmsUUID structureId);

    /**
     * Returns the available context menu commands as a map by class name.<p>
     *
     * @return the available context menu commands as a map by class name
     */
    Map<String, I_CmsContextMenuCommand> getContextMenuCommands();

    /**
     * Returns the context type.<p>
     *
     * @return the context type
     */
    String getContextType();

    /**
     * Returns the editor handler.<p>
     *
     * @return the editor handler
     */
    I_CmsContentEditorHandler getEditorHandler();

    /**
     * Unlocks the resource if appropriate.<p>
     *
     * @param structureId the structure id of the resource to lock
     */
    void unlockResource(CmsUUID structureId);
}
