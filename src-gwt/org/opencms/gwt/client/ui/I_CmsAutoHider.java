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

package org.opencms.gwt.client.ui;

import com.google.gwt.dom.client.Element;

/**
 * Interface for all widgets capable of auto hide.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsAutoHider {

    /**
     * Mouse events that occur within an autoHide partner will not hide a panel set to autoHide.<p>
     *
     * @param partner the auto hide partner to add
     *
     * @see com.google.gwt.user.client.ui.PopupPanel#addAutoHidePartner(com.google.gwt.dom.client.Element)
     */
    void addAutoHidePartner(Element partner);

    /**
     * Hides the widget.<p>
     */
    void hide();

    /**
     * Returns if the auto hide feature is enabled.<p>
     *
     * @return true if auto hide is enabled
     *
     * @see com.google.gwt.user.client.ui.PopupPanel#isAutoHideEnabled()
     */
    boolean isAutoHideEnabled();

    /**
     * Returns if the auto hide on history event feature is enabled.<p>
     *
     * @return true if auto hide is enabled
     *
     * @see com.google.gwt.user.client.ui.PopupPanel#isAutoHideOnHistoryEventsEnabled()
     */
    boolean isAutoHideOnHistoryEventsEnabled();

    /**
     * Removes an auto-hide partner.<p>
     *
     * @param partner the auto-hide partner to remove
     *
     * @see com.google.gwt.user.client.ui.PopupPanel#removeAutoHidePartner(Element)
     */
    void removeAutoHidePartner(Element partner);

    /**
     * Enable or disable the autoHide feature. When enabled, the popup will be automatically hidden when the user clicks outside of it.<p>
     *
     * @param autoHide enable true to enable, false to disable
     *
     * @see com.google.gwt.user.client.ui.PopupPanel#setAutoHideEnabled(boolean)
     */
    void setAutoHideEnabled(boolean autoHide);

    /**
     * Enable or disable autoHide on history change events. When enabled, the popup will be automatically hidden when the history token changes, such as when the user presses the browser's back button. Disabled by default.<p>
     *
     * @param enabled enable true to enable, false to disable
     *
     * @see com.google.gwt.user.client.ui.PopupPanel#setAutoHideOnHistoryEventsEnabled(boolean)
     */
    void setAutoHideOnHistoryEventsEnabled(boolean enabled);
}
