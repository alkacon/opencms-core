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

package org.opencms.ui.editors;

import org.opencms.file.CmsResource;
import org.opencms.ui.apps.I_CmsAppUIContext;

/**
 * Interface for resource editors.<p>
 */
public interface I_CmsEditor {

    /**
     * Gets the priority.<p>
     *
     * If multiple editors for the same resource type are available, the one with the highest priority will be picked.<p>
     *
     * @return the priority
     */
    int getPriority();

    /**
     * Within this method the editor UI should be initialized.<p>
     * Use the context to add the app's components to the UI.<p>
     *
     * @param context the UI context
     * @param resource the resource to edit
     * @param backLink the link to return to when closing the editor
     */
    void initUI(I_CmsAppUIContext context, CmsResource resource, String backLink);

    /**
     * Checks whether the editor is available for the given resource.<p>
     *
     * @param resource the resource to edit
     * @param plainText if plain text editing is required
     *
     * @return <code>true</code> if the editor is available for the given resource
     */
    boolean matchesResource(CmsResource resource, boolean plainText);

    /**
     * Returns a new editor instance.<p>
     *
     * @return the editor instance
     */
    I_CmsEditor newInstance();
}
