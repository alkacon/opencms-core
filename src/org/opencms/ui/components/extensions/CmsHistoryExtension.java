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

package org.opencms.ui.components.extensions;

import org.opencms.ui.shared.components.CmsHistoryState;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

/**
 * Extension to allow clientside history back and forward.<p>
 */
public class CmsHistoryExtension extends AbstractExtension {

    /** The serial version id. */
    private static final long serialVersionUID = -1461819527273730247L;

    /**
     * Constructor.<p>
     *
     * @param ui the UI to extend
     */
    public CmsHistoryExtension(UI ui) {
        extend(ui);
    }

    /**
     * Triggers a history back.<p>
     */
    public void historyBack() {

        getState().setHistoryDirection(CmsHistoryState.HISTORY_BACK);
    }

    /**
     * Triggers a history forward.<p>
     */
    public void historyForward() {

        getState().setHistoryDirection(CmsHistoryState.HISTORY_FORWARD);
    }

    /**
     * @see com.vaadin.server.AbstractClientConnector#getState()
     */
    @Override
    protected CmsHistoryState getState() {

        return (CmsHistoryState)super.getState();
    }
}
