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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.vaadin.client.ui.VPopupView;

/**
 * Extending the VAADIN popup view only to add a corner element pointing at the opening button.<p>
 */
public class CmsVPopupView extends VPopupView {

    /** The drop down marker CSS class. */
    private static final String DROP_DOWN_CLASS = "o-navigator-dropdown";

    /** The pointy corner CSS class. */
    private static final int OFFSET = -4;

    /** The corner element. */
    private Element m_corner;

    /**
     * @see com.vaadin.client.ui.VPopupView#center()
     */
    @Override
    public void center() {

        super.center();
        if (getElement().hasClassName(DROP_DOWN_CLASS)) {
            if (m_corner == null) {
                m_corner = Document.get().createDivElement();
                m_corner.setClassName("o-toolbar-menu-corner");
                popup.getElement().appendChild(m_corner);
            }
            updateCornerLeft();

            // wait to reposition the corner, as their may be an animation effecting the position
            Timer timer = new Timer() {

                @Override
                public void run() {

                    updateCornerLeft();
                }
            };
            timer.schedule(100);
        }
    }

    /**
     * Updates the corner element left position.<p>
     */
    void updateCornerLeft() {

        if (m_corner != null) {
            int popupLeft = popup.getPopupLeft();
            int dif = (getAbsoluteLeft() - popupLeft) + OFFSET;
            m_corner.getStyle().setLeft(dif, Unit.PX);
        }
    }
}
