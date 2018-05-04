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

package org.opencms.ade.galleries.client.preview.ui;

import org.opencms.ade.galleries.client.preview.CmsFocalPointController;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget which displays the focal point for an image.<p>
 */
public class CmsFocalPoint extends Composite {

    /** The ui-binder for this widget. */
    interface I_UiBinder extends UiBinder<Widget, CmsFocalPoint> {
        // GWT interface, nothing to do
    }

    /** The ui binder instance. */
    private static I_UiBinder m_uiBinder = GWT.create(I_UiBinder.class);

    /** The controller. */
    private CmsFocalPointController m_controller;

    /**
     * Creates a new instance.<p>
     *
     * @param controller the controller instance
     */
    public CmsFocalPoint(CmsFocalPointController controller) {

        m_controller = controller;
        initWidget(m_uiBinder.createAndBindUi(this));
        addDomHandler(new MouseDownHandler() {

            @SuppressWarnings("synthetic-access")
            public void onMouseDown(MouseDownEvent event) {

                event.preventDefault();
                m_controller.onStartDrag();
            }
        }, MouseDownEvent.getType());
    }

    /**
     * Positions the center of this widget over the given coordinates.<p>
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void setCenterCoordsRelativeToParent(int x, int y) {

        Style style = getElement().getStyle();
        style.setLeft(x - 10, Unit.PX);
        style.setTop(y - 10, Unit.PX);
    }

    /**
     * Sets or clears the 'is default' style on the focal point.<p>
     *
     * @param isDefault true if the 'is default' style should be set, false if it should be cleared
     */
    public void setIsDefault(boolean isDefault) {

        String style = "imagepointdefault";
        if (isDefault) {
            addStyleName(style);
        } else {
            removeStyleName(style);
        }
    }

}
