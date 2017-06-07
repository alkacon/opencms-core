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

package org.opencms.gwt.client.util;

import org.opencms.db.CmsResourceState;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsResourceStateCss;

/**
 * Utility class for the publish dialog.<p>
 *
 * @since 8.0.0
 */
public final class CmsResourceStateUtil {

    /** The CSS bundle for the publish dialog. <p> */
    private static final I_CmsResourceStateCss CSS = I_CmsLayoutBundle.INSTANCE.resourceStateCss();

    /**
     * Hide constructor.<p>
     */
    private CmsResourceStateUtil() {

        // empty
    }

    /**
     * Returns the human-readable name of a resource state.<p>
     *
     * @param state the resource state
     *
     * @return the human-readable name of the code
     */
    public static String getStateName(CmsResourceState state) {

        if (state.equals(CmsResourceState.STATE_NEW)) {
            return Messages.get().key(Messages.GUI_RESOURCE_STATE_NEW_0);
        } else if (state.equals(CmsResourceState.STATE_DELETED)) {
            return Messages.get().key(Messages.GUI_RESOURCE_STATE_DELETED_0);
        } else if (state.equals(CmsResourceState.STATE_CHANGED)) {
            return Messages.get().key(Messages.GUI_RESOURCE_STATE_CHANGED_0);
        } else if (state.equals(CmsResourceState.STATE_UNCHANGED)) {
            return Messages.get().key(Messages.GUI_RESOURCE_STATE_UNCHANGED_0);
        }
        return "";
    }

    /**
     * Returns the text style for a given resource state.<p>
     *
     * @param state the resource state
     *
     * @return the style name for the resource's state
     */
    public static String getStateStyle(CmsResourceState state) {

        if (state.equals(CmsResourceState.STATE_NEW)) {
            return CSS.stateNew();
        } else if (state.equals(CmsResourceState.STATE_DELETED)) {
            return CSS.stateDeleted();
        } else if (state.equals(CmsResourceState.STATE_CHANGED)) {
            return CSS.stateChanged();
        }
        return CSS.noState();
    }
}
