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

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;

/**
 * On hover intent handler.<p>
 *
 * @since 8.0.0
 */
public abstract class A_CmsHoverHandler implements MouseOutHandler, MouseOverHandler {

    /** Timer to achieve the hover intent effect. */
    protected Timer m_timer;

    /**
     * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
     */
    public final void onMouseOut(MouseOutEvent event) {

        if (m_timer != null) {
            m_timer.cancel();
            m_timer = null;
        } else {
            onHoverOut(event);
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
     */
    public final void onMouseOver(final MouseOverEvent event) {

        if (m_timer != null) {
            return;
        }
        m_timer = new Timer() {

            /**
             * @see com.google.gwt.user.client.Timer#run()
             */
            @Override
            public void run() {

                m_timer = null;
                onHoverIn(event);
            }
        };
        m_timer.schedule(200);
    }

    /**
     * Will be executed for starting the hover effect.<p>
     *
     * @param event the mouse event
     */
    protected abstract void onHoverIn(MouseOverEvent event);

    /**
     * Will be executed for finishing the hover effect.<p>
     *
     * @param event the mouse event
     */
    protected abstract void onHoverOut(MouseOutEvent event);
}
