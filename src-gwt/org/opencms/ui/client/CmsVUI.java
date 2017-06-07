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

package org.opencms.ui.client;

import org.opencms.gwt.client.util.CmsDebugLog;

import com.vaadin.client.ui.VUI;

/**
 * UI widget overriding some functionality from the Vaadin implementation.<p>
 */
public class CmsVUI extends VUI {

    /** The current instance. */
    public static CmsVUI INSTANCE;

    /** Boolean flag which indicates whether the stored focus should be cleared. */
    private boolean m_clearedStoredFocus;

    /**
     * Clears the stored focus for the current UI instance.<p>
     *
     * Useful when you want more explicit control over the focus after closing a window.<p>
     */
    public static void clearStoredFocusForCurrentInstance() {

        if (INSTANCE != null) {
            INSTANCE.clearStoredFocus();
        } else {
            CmsDebugLog.consoleLog("clearStoredFocus called, but UI instance not set.");
        }

    }

    /**
     * Clears the stored focus.<p>
     *
     * Use this when you want to set the focus explicitly after closing a Vaadin window.
     */
    public void clearStoredFocus() {

        m_clearedStoredFocus = true;
    }

    /**
     * @see com.vaadin.client.ui.VUI#focusStoredElement()
     */
    @Override
    public void focusStoredElement() {

        if (!m_clearedStoredFocus) {
            super.focusStoredElement();
        }
    }

    /**
     * @see com.vaadin.client.ui.VUI#storeFocus()
     */
    @Override
    public void storeFocus() {

        m_clearedStoredFocus = false;
        super.storeFocus();
    }

    /**
     * @see com.vaadin.client.ui.VUI#onLoad()
     */
    @Override
    protected void onLoad() {

        super.onLoad();
        INSTANCE = this;
    }
}
