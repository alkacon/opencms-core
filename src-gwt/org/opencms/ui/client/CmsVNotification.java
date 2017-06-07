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

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.vaadin.client.ui.VNotification;

/**
 * Notification widget which also hides itself when the Enter key is pressed.<p>
 */
public class CmsVNotification extends VNotification {

    /**
     * @see com.vaadin.client.ui.VNotification#onEventPreview(com.google.gwt.user.client.Event)
     */
    @Override
    public boolean onEventPreview(Event event) {

        if ((DOM.eventGetType(event) == Event.ONKEYDOWN) && (event.getKeyCode() == KeyCodes.KEY_ENTER)) {
            hide();
            return false;
        } else {
            return super.onEventPreview(event);
        }
    }

}
