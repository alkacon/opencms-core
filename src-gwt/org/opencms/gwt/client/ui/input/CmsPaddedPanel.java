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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Basic panel class with a horizontal pseudo-padding.<p>
 *
 * This padding is implemented by dynamically setting the size and margins of the contained widget
 * after insertion into the DOM.<p>
 *
 * It only works with a single child widget right now and thus extends SimplePanel.
 *
 * @since 8.0.0
 *
 */
public class CmsPaddedPanel extends SimplePanel {

    /** The CSS bundle used for this widget. */
    public static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** The horizontal "padding" for the text box. */
    private int m_paddingX;

    /**
     * Constructs a new instance of this widget.<p>
     *
     * @param paddingX the horizontal padding to use
     *
     */
    public CmsPaddedPanel(int paddingX) {

        super();
        m_paddingX = paddingX;

    }

    /**
     * Sets the horizontal padding of this widget and updates the style for this widget.<p>
     *
     * @param paddingX the new padding value
     */
    public void setPaddingX(int paddingX) {

        m_paddingX = paddingX;
        updatePadding();
    }

    /**
     * Updates the horizontal "padding" for this text box.<p>
     *
     * This isn't done via the CSS padding property, because if we set the width of the widget
     * to 100%, a real padding would make a part of the widget stick outside of its parent element.
     * Instead, we change the width of the textbox and its left and right margins.
     *
     */
    public void updatePadding() {

        if (!isAttached()) {
            // only update panel when attached to DOM
            return;
        }

        int panelWidth = getOffsetWidth();
        // avoid negative widths
        if (panelWidth > (2 * m_paddingX)) {
            getWidget().setWidth((panelWidth - (2 * m_paddingX)) + "px");
        }
        getElement().getStyle().setPaddingLeft(m_paddingX, Unit.PX);
        getElement().getStyle().setPaddingRight(m_paddingX, Unit.PX);
    }

    /**
     * We override the onLoad method because the width of the internal text box needs
     * to be calculated after the widget is attached to the DOM.<p>
     *
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        // defer the layout update so that all the child elements have already been displayed
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            /**
             * @see com.google.gwt.core.client.Scheduler.ScheduledCommand#execute()
             */
            public void execute() {

                updatePadding();
            }
        });
    }
}