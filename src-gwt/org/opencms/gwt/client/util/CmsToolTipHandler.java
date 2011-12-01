/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * A tool-tip handler. Allowing to show any HTML as a tool-tip on mouse over.<p>
 * 
 * @since 8.0.0
 */
public class CmsToolTipHandler implements MouseOverHandler, MouseMoveHandler, MouseOutHandler {

    /** The default tool-tip left offset. */
    private static final int DEFAULT_OFFSET_LEFT = 10;

    /** The default tool-tip top offset. */
    private static final int DEFAULT_OFFSET_TOP = 10;

    /** The mouse move handler registration. */
    private HandlerRegistration m_moveHandlerRegistration;

    /** The tool-tip left offset. */
    private int m_offsetLeft;

    /** The tool-tip top offset. */
    private int m_offsetTop;

    /** The mouse out handler registration. */
    private HandlerRegistration m_outHandlerRegistration;

    /** The mouse over handler registration. */
    private HandlerRegistration m_overHandlerRegistration;

    /** Flag indicating if the tool-tip is currently showing. */
    private boolean m_showing;

    /** The widget to show the tool-tip for. */
    private HasAllMouseHandlers m_target;

    /** The tool-tip element. */
    private Element m_toolTip;

    /** The tool-tip HTML to show. */
    private String m_toolTipHtml;

    private Timer m_removeTimer;

    private static final int REMOVE_SCHEDULE = 10000;

    /**
     * Constructor. Adds the tool-tip handler to the target.<p>
     * 
     * @param target the target to show the tool-tip on
     * @param toolTipHtml the tool-tip content
     */
    public CmsToolTipHandler(HasAllMouseHandlers target, String toolTipHtml) {

        m_target = target;
        m_toolTipHtml = toolTipHtml;
        m_offsetLeft = DEFAULT_OFFSET_LEFT;
        m_offsetTop = DEFAULT_OFFSET_TOP;
        m_overHandlerRegistration = m_target.addMouseOverHandler(this);
    }

    /**
     * Returns the tool-tip HTML.<p>
     *
     * @return the tool-tip HTML
     */
    public String getToolTipHtml() {

        return m_toolTipHtml;
    }

    /**
     * Returns if the tool-tip is showing.<p>
     *
     * @return <code>true</code> if the tool-tip is showing
     */
    public boolean isShowing() {

        return m_showing;
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseMoveHandler#onMouseMove(com.google.gwt.event.dom.client.MouseMoveEvent)
     */
    public void onMouseMove(MouseMoveEvent event) {

        m_removeTimer.schedule(REMOVE_SCHEDULE);
        setToolTipPosition(event.getClientX(), event.getClientY());
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
     */
    public void onMouseOut(MouseOutEvent event) {

        if (m_removeTimer != null) {
            m_removeTimer.cancel();
        }
        clearShowing();
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
     */
    public void onMouseOver(MouseOverEvent event) {

        // make sure not to double assign any handlers
        if (m_removeTimer != null) {
            m_removeTimer.cancel();
        }
        clearShowing();

        createTimer();
        m_removeTimer.schedule(REMOVE_SCHEDULE);

        m_showing = true;
        m_moveHandlerRegistration = m_target.addMouseMoveHandler(this);
        m_outHandlerRegistration = m_target.addMouseOutHandler(this);
        if (m_toolTip == null) {
            m_toolTip = DOM.createDiv();
            m_toolTip.addClassName(I_CmsLayoutBundle.INSTANCE.generalCss().toolTip());
            m_toolTip.addClassName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        }
        m_toolTip.setInnerHTML(m_toolTipHtml);
        RootPanel.getBodyElement().appendChild(m_toolTip);
        setToolTipPosition(event.getClientX(), event.getClientY());
    }

    /**
     * Removes this tool-tip handler completely. This instance will not be reusable.<p>
     */
    public void removeHandler() {

        clearShowing();
        if (m_overHandlerRegistration != null) {
            m_overHandlerRegistration.removeHandler();
            m_overHandlerRegistration = null;
        }
        m_target = null;
    }

    /**
     * Sets the tool-tip left offset.<p>
     *
     * @param offsetLeft the tool-tip left offset to set
     */
    public void setOffsetLeft(int offsetLeft) {

        m_offsetLeft = offsetLeft;
    }

    /**
     * Sets the tool-tip top offset.<p>
     *
     * @param offsetTop the tool-tip top offset to set
     */
    public void setOffsetTop(int offsetTop) {

        m_offsetTop = offsetTop;
    }

    /**
     * Sets the tool-tip HTML.<p>
     *
     * @param toolTipHtml the tool-tip HTML to set
     */
    public void setToolTipHtml(String toolTipHtml) {

        m_toolTipHtml = toolTipHtml;
        if (m_showing) {
            m_toolTip.setInnerHTML(toolTipHtml);
        }
    }

    /**
     * Removes the tool-tip and mouse move and out handlers.<p>
     */
    public void clearShowing() {

        m_showing = false;
        if (m_toolTip != null) {
            m_toolTip.removeFromParent();
            m_toolTip = null;
        }
        if (m_moveHandlerRegistration != null) {
            m_moveHandlerRegistration.removeHandler();
            m_moveHandlerRegistration = null;
        }
        if (m_outHandlerRegistration != null) {
            m_outHandlerRegistration.removeHandler();
            m_outHandlerRegistration = null;
        }
        m_removeTimer = null;
    }

    private void createTimer() {

        m_removeTimer = new Timer() {

            /**
             * @see com.google.gwt.user.client.Timer#run()
             */
            @Override
            public void run() {

                clearShowing();
            }
        };
    }

    /** 
     * Sets the tool-tip position.<p>
     * 
     * @param absLeft the mouse pointer absolute left
     * @param absTop the mouse pointer absolute top
     */
    private void setToolTipPosition(int absLeft, int absTop) {

        if (m_toolTip != null) {
            int height = m_toolTip.getOffsetHeight();
            int width = m_toolTip.getOffsetWidth();
            int windowHeight = Window.getClientHeight();
            int windowWidth = Window.getClientWidth();
            int left = absLeft + m_offsetLeft;
            if ((left + width) > windowWidth) {
                left = windowWidth - m_offsetLeft - width;
            }
            m_toolTip.getStyle().setLeft(left, Unit.PX);
            int top = absTop + m_offsetTop;
            if (((top + height) > windowHeight) && ((height + m_offsetTop) < absTop)) {
                top = absTop - m_offsetTop - height;
            }
            m_toolTip.getStyle().setTop(top, Unit.PX);
        }
    }
}
